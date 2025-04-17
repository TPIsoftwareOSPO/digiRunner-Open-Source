package tpi.dgrv4.dpaa.component.apptJob;


import org.springframework.beans.factory.annotation.Autowired;

import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public class CallApiJob extends ApptJob {

	@Autowired
	public CallApiJob(TsmpDpApptJob tsmpDpApptJob, ApptJobDispatcher apptJobDispatcher,
			TsmpDpApptJobDao tsmpDpApptJobDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
	}

	@Override
	public String runApptJob() throws Exception {
		// TODO 尚未實作
		// apiHelper.call(reqUrl, params, method);
		throw new Exception("尚未實作");
	}

	@Override
	public TsmpDpApptJob set(TsmpDpApptJob job) {
		// TODO
		return super.set(job);
	}

}
