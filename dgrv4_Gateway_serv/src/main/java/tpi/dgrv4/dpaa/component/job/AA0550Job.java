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
public class AA0550Job extends Job {

	private TPILogger logger = TPILogger.tl;

	private PrepareMailService prepareMailService;
	
	private final TsmpAuthorization aa0550_auth;
	
	private final List<TsmpMailEvent> aa0550_mailEvents;

	private final String aa0550_sendTime;
	
	@Autowired
	public AA0550Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime, PrepareMailService prepareMailService) {
		this.aa0550_auth = auth;
		this.aa0550_mailEvents = mailEvents;
		this.aa0550_sendTime = sendTime;
		this.prepareMailService = prepareMailService;
	}

	@Override
	public void run(JobHelperImpl jobHelper, JobManager jobManager) {
		if (aa0550_mailEvents != null && !aa0550_mailEvents.isEmpty()) {
			try {
				this.logger.debug("--- Begin AA0550Job ---");
			
				//準備好資料,以寫入排程
				String identif = "userName=" + aa0550_auth.getUserName();
				getPrepareMailService().createMailSchedule(aa0550_mailEvents, identif
						, TsmpDpMailType.DIFFERENT.text(), aa0550_sendTime);
				
			} catch (Exception e) {
				logger.debug("" + e);
			} finally {
				this.logger.debug("--- Finish AA0550Job ---");
			}
		}
	}

	protected PrepareMailService getPrepareMailService() {
		return prepareMailService;
	}

}
