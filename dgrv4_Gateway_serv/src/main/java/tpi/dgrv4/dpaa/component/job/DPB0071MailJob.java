package tpi.dgrv4.dpaa.component.job;

import java.util.List;

import tpi.dgrv4.dpaa.constant.TsmpDpMailType;
import tpi.dgrv4.dpaa.service.PrepareMailService;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.gateway.component.job.Job;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@SuppressWarnings("serial")
public class DPB0071MailJob extends Job {

	private TPILogger logger = TPILogger.tl;

	private TsmpAuthorization auth;

	private List<TsmpMailEvent> mailEvents;

	private String sendTime;
	
	private Long reqOrdermId;

	private PrepareMailService prepareMailService;

	public DPB0071MailJob(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime, Long reqOrdermId,
			PrepareMailService prepareMailService) {
		this.auth = auth;
		this.mailEvents = mailEvents;
		this.sendTime = sendTime;
		this.reqOrdermId = reqOrdermId;
		this.prepareMailService = prepareMailService;
	}

	@Override
	public void run(JobHelperImpl jobHelper, JobManager jobManager) {
		try {
			this.logger.debug("--- Begin DPB0071MailJob ---");

			// 準備好資料,以寫入排程
			// identif 可寫入識別資料，ex: userName=mini 或 userName=mini, reqOrdermId=17002
			// 若有多個資料則以逗號和全型空白分隔
			String identif = "userName=" + auth.getUserName()+", reqOrdermId=" +reqOrdermId; 
			getPrepareMailService().createMailSchedule(mailEvents, identif, TsmpDpMailType.SAME.text(), sendTime);

		} catch (Exception e) {
			logger.debug("" + e);
		} finally {
			this.logger.debug("--- Finish DPB0071MailJob ---");
		}
	}
	
	protected PrepareMailService getPrepareMailService() {
		return prepareMailService;
	}
	
}
