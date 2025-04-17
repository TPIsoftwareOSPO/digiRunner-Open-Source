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
public class DPB0002Job extends Job {

	private TPILogger logger = TPILogger.tl;

	private PrepareMailService prepareMailService;
	
	private final TsmpAuthorization dpb0002_auth;
	
	private final List<TsmpMailEvent> dpb0002_mailEvents;

	private final String dpb0002_sendTime;
	
	@Autowired
	public DPB0002Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime,
			PrepareMailService prepareMailService) {
		this.dpb0002_auth = auth;
		this.dpb0002_mailEvents = mailEvents;
		this.dpb0002_sendTime = sendTime;
		this.prepareMailService = prepareMailService;
	}

	@Override
	public void run(JobHelperImpl jobHelper, JobManager jobManager) {
		if (dpb0002_mailEvents != null && !dpb0002_mailEvents.isEmpty()) {
			try {
				this.logger.debug("--- Begin DPB0002Job ---");
			
				/* 20200525; Mini; 寫入 APPT_JOB Table & 建立 Mail 檔案, 改由排程來寄信
				for(TsmpMailEvent e : mailEvents) {
					mailHelper.sendEmail(e);
				}
				*/

				//準備好資料,以寫入排程
				String identif = "userName=" + dpb0002_auth.getUserName();
				getPrepareMailService().createMailSchedule(dpb0002_mailEvents, identif
						, TsmpDpMailType.DIFFERENT.text(), dpb0002_sendTime);
				
			} catch (Exception e) {
				logger.debug("" + e);
			} finally {
				this.logger.debug("--- Finish DPB0002Job ---");
			}
		}
	}
	
	protected PrepareMailService getPrepareMailService() {
		return prepareMailService;
	}
}
