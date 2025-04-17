package tpi.dgrv4.dpaa.component.apptJob;

import org.springframework.beans.factory.annotation.Autowired;

import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public class BroadcastJob extends ApptJob {

	@Autowired
	public BroadcastJob(TsmpDpApptJob tsmpDpApptJob, ApptJobDispatcher apptJobDispatcher,
			TsmpDpApptJobDao tsmpDpApptJobDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
	}

	@Override
	public String runApptJob() throws Exception {
		/*
		 * TODO 2022.10.28, 可以將欲廣播的內容 (ex: Packet類別名稱) 寫在 in_params 中，
		 * 就可以從 [排程作業] 發布工作給各節點執行
		 */
		getTsmpDpApptJob().setIdentifData("Not implemented yet.");
		return "END_OF_CALL";
	}

}