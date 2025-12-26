package tpi.dgrv4.dpaa.component.job;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import tpi.dgrv4.dpaa.constant.TsmpDpMailType;
import tpi.dgrv4.dpaa.service.PrepareMailService;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.gateway.component.job.Job;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@SuppressWarnings("serial")
public class AA0552Job extends Job {

	private TPILogger logger = TPILogger.tl;

	private PrepareMailService prepareMailService;
	
	private final TsmpAuthorization aa0552_auth;
	
	private final List<TsmpMailEvent> aa0552_mailEvents;

	private final String aa0552_sendTime;
	
	@Autowired
	public AA0552Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime, PrepareMailService prepareMailService) {
		this.aa0552_auth = auth;
		this.aa0552_mailEvents = mailEvents;
		this.aa0552_sendTime = sendTime;
		this.prepareMailService = prepareMailService;
	}

	@Override
	public void run(JobHelperImpl jobHelper, JobManager jobManager) {
		if (aa0552_mailEvents != null && !aa0552_mailEvents.isEmpty()) {
			try {
				this.logger.debug("--- Begin AA0552Job ---");
			
				//準備好資料,以寫入排程
				String identif = "userName=" + aa0552_auth.getUserName();
				getPrepareMailService().createMailSchedule(aa0552_mailEvents, identif
						, TsmpDpMailType.DIFFERENT.text(), aa0552_sendTime);
				
			} catch (Exception e) {
				logger.debug("" + e);
			} finally {
				this.logger.debug("--- Finish AA0552Job ---");
			}
		}
	}

	protected PrepareMailService getPrepareMailService() {
		return prepareMailService;
	}

}
