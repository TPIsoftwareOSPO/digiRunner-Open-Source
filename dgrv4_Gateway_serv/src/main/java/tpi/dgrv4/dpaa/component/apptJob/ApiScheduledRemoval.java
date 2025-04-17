package tpi.dgrv4.dpaa.component.apptJob;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.transaction.Transactional;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.constant.TsmpDpItem;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Transactional
public class ApiScheduledRemoval extends ApptJob {

	private TsmpApiDao tsmpApiDao;

	@Autowired
	public ApiScheduledRemoval(TsmpDpApptJob tsmpDpApptJob, ApptJobDispatcher apptJobDispatcher,
			TsmpDpApptJobDao tsmpDpApptJobDao, TsmpApiDao tsmpApiDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		this.tsmpApiDao = tsmpApiDao;
	}

	public ApiScheduledRemoval(TsmpDpApptJob tsmpDpApptJob, TPILogger logger, ApptJobDispatcher apptJobDispatcher,
			TsmpDpApptJobDao tsmpDpApptJobDao, TsmpApiDao tsmpApiDao) {
		super(tsmpDpApptJob, logger, apptJobDispatcher, tsmpDpApptJobDao);
		this.tsmpApiDao = tsmpApiDao;
	}

	@Override
	public String runApptJob() throws Exception {

		step(TsmpDpItem.API_SCH_WAKE_UP.getSubitemNo());

		step(TsmpDpItem.FISHING_SCH_RE_API.getSubitemNo());

		List<TsmpApi> ls = getApis();

		step(TsmpDpItem.CHANGE_REMOVAL_STATE.getSubitemNo());

		removalApis(ls);

		return "SUCCESS";
	}

	private void removalApis(List<TsmpApi> ls) {

		if (ls != null) {
			for (TsmpApi tsmpApi : ls) {
				tsmpApi.setPublicFlag(TsmpDpItem.DP_PUBLIC_FLAG_D.getSubitemNo());
				tsmpApi.setScheduledRemovalDate(0L);
				tsmpApi.setUpdateUser("SYSTEM");
				tsmpApi.setUpdateTime(DateTimeUtil.now());
			}
			getTsmpApiDao().saveAll(ls);
		}

	}

	private List<TsmpApi> getApis() {

		return getTsmpApiDao()
				.findByScheduledRemovalDateLessThanEqualAndScheduledRemovalDateNot(System.currentTimeMillis(), 0L);
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}
}
