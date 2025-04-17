package tpi.dgrv4.dpaa.component.apptJob;

import org.springframework.beans.factory.annotation.Autowired;

import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public class SystemMonitorJob extends ApptJob {

	@Autowired
	public SystemMonitorJob(TsmpDpApptJob tsmpDpApptJob, ApptJobDispatcher apptJobDispatcher, TsmpDpApptJobDao tsmpDpApptJobDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
	}

	@Override
	public String runApptJob() throws Exception {
		// 尚未實作
		TPILogger.tl.trace("[SystemMonitorJob] Not implemented yet");
		return "END_OF_CALL";
	}

}
