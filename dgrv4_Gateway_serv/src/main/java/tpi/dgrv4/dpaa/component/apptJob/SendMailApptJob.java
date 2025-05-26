package tpi.dgrv4.dpaa.component.apptJob;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.dpaa.vo.TsmpMailFileContent;
import tpi.dgrv4.dpaa.vo.TsmpMailJobParams;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public class SendMailApptJob extends ApptJob {
	
	private TPILogger logger = TPILogger.tl;
	
	private MailHelper mailHelper;
	private FileHelper fileHelper;
	private TsmpDpFileDao tsmpDpFileDao;

	@Autowired
	public SendMailApptJob(TsmpDpApptJob tsmpDpApptJob, MailHelper mailHelper, FileHelper fileHelper,
			TsmpDpFileDao tsmpDpFileDao, ApptJobDispatcher apptJobDispatcher, TsmpDpApptJobDao tsmpDpApptJobDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		this.mailHelper = mailHelper;
		this.fileHelper = fileHelper;
		this.tsmpDpFileDao = tsmpDpFileDao;
	}

	@Override
	public String runApptJob() throws Exception {
		
		String jsonStr = getTsmpDpApptJob().getInParams();
		if (StringUtils.isEmpty(jsonStr)) {
			throw new Exception("無法取到 Mail 檔案名稱, 無法寄出通知信");
		}
		
		ObjectMapper mapper = new ObjectMapper();
		TsmpMailJobParams params = mapper.readValue(jsonStr, TsmpMailJobParams.class);
		if (params == null) {
			throw new Exception("無法取到 Mail 檔案名稱, 無法寄出通知信");
		}
		
		String fileName = params.getMailFileName();
		if (StringUtils.isEmpty(fileName)) {
			throw new Exception("無法取到 Mail 檔案名稱, 無法寄出通知信");
		}
		
		//讀取 Mail 檔案
		String fileJsonStr = read(fileName);
		if (StringUtils.isEmpty(fileJsonStr)) {
			throw new Exception("無法讀取 Mail 檔案內容, 無法寄出通知信");
		}
		
		//取得要寄發的Mail資料
		TsmpMailFileContent c = mapper.readValue(fileJsonStr, TsmpMailFileContent.class);
		List<String> recipientsList = c.getRecipientsList();
		String title = c.getSubject();
		String content = c.getContent(); 
		String createUser = c.getCreateUser();
		String refCode = c.getRefCode();
		
		List<TsmpMailEvent> mailEvents = new ArrayList<TsmpMailEvent>();
		for (String recipients : recipientsList) {
			TsmpMailEvent mailEvent = new TsmpMailEventBuilder() //
					.setSubject(title)
					.setContent(content)
					.setRecipients(recipients)
					.setCreateUser(createUser)
					.setRefCode(refCode)
					.build();
			
			if (mailEvent != null) {
				mailEvents.add(mailEvent);
			}
		}
		if (mailEvents == null || mailEvents.isEmpty()) {
			throw new Exception("取得信件參數失敗, 無法寄出通知信");
		}
		
		//寄發 Mail		
		step("PREP_SEND");//準備寄出通知信
		
		return send(mailEvents);
	}

	private String read(String fileName) throws Exception {
		byte[] content = getFileHelper().downloadByTsmpDpFile(TsmpDpFileType.MAIL_CONTENT, 1L, fileName);
		return new String(content,"UTF-8");
		/*String tsmpDpFilePath = FileHelper.getTsmpDpFilePath(TsmpDpFileType.MAIL_CONTENT, 1L);
		String filePathName = getFileHelper().getTsmpDpFileAbsolutePath(tsmpDpFilePath, fileName);
 
		String str = null;
		StringBuilder buffer = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePathName), "UTF-8")); // 指定讀取文件的編碼格式，以免出現中文亂碼
			while ((str = reader.readLine()) != null) {
				buffer.append(str);
			}
			return buffer.toString();
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				logger.debug("" + e);
			}
		}*/
	}
	
	private String send(List<TsmpMailEvent> mailEvents) {
		int successCnt = 0;
		for (int i = 0; i < mailEvents.size(); i++) {
			try {
				TsmpMailEvent e = mailEvents.get(i);
				getMailHelper().sendEmail(e);
				successCnt++;
			} catch (Exception e) {
				logger.debug("" + e);
			} finally {
				step((i + 1) + "/" + mailEvents.size());
			}
		}
		
		return successCnt  + "/" + mailEvents.size();
 	}

	protected MailHelper getMailHelper() {
		return this.mailHelper;
	}
	
	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}
	
	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}
}
