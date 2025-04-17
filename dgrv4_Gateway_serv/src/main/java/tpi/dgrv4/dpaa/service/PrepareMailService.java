package tpi.dgrv4.dpaa.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.constant.TsmpDpMailType;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.dpaa.vo.TsmpMailFileContent;
import tpi.dgrv4.dpaa.vo.TsmpMailJobParams;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * 準備寄信所需的檔案,並將寄信排程寫入 APPT_JOB Table
 * 
 * @author Mini
 */
@Service
public class PrepareMailService {

	private TPILogger logger = TPILogger.tl;

//	@Autowired(required=false)
	private ApptJobDispatcher apptJobDispatcher;
	
	private FileHelper fileHelper;
	private TsmpDpApptJobDao tsmpDpApptJobDao;
	
	@Autowired
	public PrepareMailService(@Nullable ApptJobDispatcher apptJobDispatcher, FileHelper fileHelper,
			TsmpDpApptJobDao tsmpDpApptJobDao) {
		super();
		this.apptJobDispatcher = apptJobDispatcher;
		this.fileHelper = fileHelper;
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
	}

	public void createMailSchedule(List<TsmpMailEvent> mailEvents, String identif,
			String mailType, String sendTime) throws Exception{
		
		if (mailEvents == null || mailEvents.isEmpty()) {
			throw new Exception("取得信件參數失敗, 無法寄出簽核通知信");
		}

		//取得要寫成 Mail 檔案的內容,一個 TsmpMailFileContent 要產生一個檔案
		List<TsmpMailFileContent> fileContentList = new ArrayList<TsmpMailFileContent>();
		if(TsmpDpMailType.SAME.text().contentEquals(mailType)) {
			fileContentList = getTsmpMailFileContentForSame(mailEvents, identif, mailType);
			
		}else {
			fileContentList = getTsmpMailFileContentForDiff(mailEvents, identif, mailType);
		}
		
		ObjectMapper mapper = new ObjectMapper();
		
		//寫入 APPT_JOB Table
		for (TsmpMailFileContent c : fileContentList) {
			Date startDateTime = getStartDateTime(sendTime);
			TsmpDpApptJob apptJob = createApptJob(c, "SEND_MAIL", null, startDateTime);
			Long apptJobId = apptJob.getApptJobId();
			String filename = getTempFilename(apptJobId + "");
			
			//將取得的檔名寫回 APPT_JOB Table & 檔案內容
			TsmpMailJobParams params = new TsmpMailJobParams();
			params.setMailType(mailType);
			params.setMailFileName(filename);
			String jsonStr = "";
			try {
				jsonStr = mapper.writeValueAsString(params);// 轉成json字串
			} catch (JsonProcessingException e) {
				this.logger.error(StackTraceUtil.logStackTrace(e));
			} 
			c.setFilename(filename);
			apptJob.setInParams(jsonStr);
			
			//建立 Mail 檔案,內文為Json格式
			createMailFile(c);
			
			// refresh memList
			getApptJobDispatcher().addAndRefresh(apptJob);
			
			printJob(apptJob);
		}
	}
	
	/**
	 * 建立 Mail 檔案,內文為Json格式
	 * 
	 * @param fileContentList
	 */
	private void createMailFile(TsmpMailFileContent c) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String jsonStr = mapper.writeValueAsString(c);// 轉成json字串
			byte[] content = jsonStr.getBytes(StandardCharsets.UTF_8);
			String filename = c.getFilename();
			//String filePathName = getFileHelper().getTsmpDpFileAbsolutePath(tsmpDpFilePath, filename);
			//Path filePath = getFileHelper().upload(tsmpDpFilePath, filename, content);
			getFileHelper().upload(c.getCreateUser(), TsmpDpFileType.MAIL_CONTENT, 1l, filename, content, "Y");
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}
	}
 
	/**
	 * 取得要存成 Mail 檔案的內容,供寄信排程使用
	 * 內文相同
	 */
	private List<TsmpMailFileContent> getTsmpMailFileContentForSame(List<TsmpMailEvent> mailEvents, 
			String identif, String mailType) {
		//mailQty=12,userName=JUNIT_TEST,reqOrdermId=ON-20200505-0001,recipients=xxxx@gmail.com,yyy@gmail.com,...etc
		String identifData = identif + ",　" + "recipients=";//識別字
		boolean isEnd = false;
		
		List<TsmpMailFileContent> fileContentList = new ArrayList<TsmpMailFileContent>();
		List<String> recipientsList = new ArrayList<String>();
		
		for (int i=0; i<mailEvents.size(); i++) {
			TsmpMailEvent event = mailEvents.get(i);			
			String recipients = event.getRecipients();
			identifData += recipients + ",";
			recipientsList.add(recipients);
			
			if(identifData.length() > 3000) {//當識別字超過長度3000
				if(i == mailEvents.size()-1) {//是否是最後一筆資料
					isEnd = true;
				}
				
				identifData = "mailQty=" + recipientsList.size() + ",　" + identifData;
				fileContentList.add(getTsmpMailFileContent(event, mailType, identifData, recipientsList));
				
				//設初始值
				identifData = identif + ",　" + "recipients=";//識別字
				recipientsList = new ArrayList<String>();
			}	
			
			if(i == mailEvents.size()-1) {//是否是最後一筆資料
				if(!isEnd) {
					identifData = "mailQty=" + recipientsList.size() + ",　" + identifData;
					fileContentList.add(getTsmpMailFileContent(event, mailType, identifData, recipientsList));
				}
			}			
        }
		return fileContentList;
	}
	
	
	/**
	 * 取得要存成 Mail 檔案的內容,供寄信排程使用 
	 * 內文不同
	 */
	private List<TsmpMailFileContent> getTsmpMailFileContentForDiff(List<TsmpMailEvent> mailEvents, String identif, String mailType) {
		List<TsmpMailFileContent> fileContentList = new ArrayList<TsmpMailFileContent>();
		for (TsmpMailEvent event : mailEvents) {
			String recipients = event.getRecipients();
			String identifData = "mailQty=1" + ",　" + identif + ",　" + "recipients=" + recipients;//識別字
			
			List<String> recipientsList = new ArrayList<String>();
			recipientsList.add(recipients);
			fileContentList.add(getTsmpMailFileContent(event, mailType, identifData, recipientsList));
		}
		return fileContentList;
	}
	
	private Date getStartDateTime(String sendTime) {
		int seconds = 0;
		try {
			seconds = Integer.valueOf(sendTime);
			seconds = seconds/1000;//多久後寄發Email(ms)
		} catch (Exception e) {
			this.logger.warn(String.format("Invalid setting: job.mail.send.time = %d, set to default 10 minute.", seconds));
			seconds = 10;//預設10分鐘
		}
		
		Date startDateTime = Date.from(LocalDateTime.now().plusSeconds(seconds).atZone(ZoneId.systemDefault()).toInstant());
		
		return startDateTime;
	}
	
	/**
	 * Mail 檔案的內容
	 * 
	 * @param event
	 * @param mailType
	 * @param identifData
	 * @param recipientsList
	 * @return
	 */
	private TsmpMailFileContent getTsmpMailFileContent(TsmpMailEvent event, String mailType,
			String identifData, List<String> recipientsList) {
		TsmpMailFileContent c = new TsmpMailFileContent();
		c.setMailType(mailType);
		c.setIdentifData(identifData);
		c.setSubject(event.getSubject());
		c.setContent(event.getContent());
		c.setRecipientsList(recipientsList);
		c.setCreateUser(event.getCreateUser());
		c.setRefCode(event.getRefCode());
		
		return c;
	}
	
	private TsmpDpApptJob createApptJob(TsmpMailFileContent c, String refItemNo, String refSubitemNo, Date startDateTime) {		
		TsmpDpApptJob job = new TsmpDpApptJob();
		job.setRefItemNo(refItemNo);
		job.setRefSubitemNo(refSubitemNo);
		job.setIdentifData(c.getIdentifData());
		job.setStartDateTime(startDateTime);
		job.setCreateDateTime(DateTimeUtil.now());
		job.setCreateUser(c.getCreateUser());
		job.setJobStep("WAIT_SEND");//"等待寄出通知信"
		job = getTsmpDpApptJobDao().saveAndFlush(job);
		
		return job;
	}
	
	/**
	 * 取得檔名
	 * 檔名: timestamp.wait.jobID.mail
	 * ex: 1588844697800.wait.100001.mail
	 * 
	 * @param apptJobId
	 * @return
	 */
	private String getTempFilename(String apptJobId) {
		long timestampMillis = Instant.now().toEpochMilli();
		String timestamp = String.valueOf(timestampMillis);
		String filename = timestamp.concat(".wait.").concat(apptJobId).concat(".mail");
		
		return filename;
	}
	
	private void printJob(TsmpDpApptJob job) {
		String refSubitemNo = job.getRefSubitemNo() == null ? "" : job.getRefSubitemNo();
		this.logger.debug(String.format("\n排程已建立:\n\t名稱:%s\n\t參數:%s\n\t預定時間:%s" //
			, job.getRefItemNo().concat("_").concat(refSubitemNo) //
			, job.getInParams() //
			, DateTimeUtil.dateTimeToString(job.getStartDateTime(), DateTimeFormatEnum.西元年月日時分秒_2).orElseThrow(TsmpDpAaRtnCode._1295::throwing)));
	}

	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return this.tsmpDpApptJobDao;
	}
	
	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}
	
	protected ApptJobDispatcher getApptJobDispatcher() {
		return this.apptJobDispatcher;
	}
}
