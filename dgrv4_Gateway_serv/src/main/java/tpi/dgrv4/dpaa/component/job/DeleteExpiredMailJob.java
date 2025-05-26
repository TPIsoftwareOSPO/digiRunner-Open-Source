package tpi.dgrv4.dpaa.component.job;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailLog;
import tpi.dgrv4.entity.repository.TsmpDpMailLogDao;
import tpi.dgrv4.gateway.component.job.DeferrableJob;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 刪除過期的Mail log
 */ 
@SuppressWarnings("serial")
public class DeleteExpiredMailJob extends DeferrableJob {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpMailLogDao tsmpDpMailLogDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	
	public DeleteExpiredMailJob(TsmpDpMailLogDao tsmpDpMailLogDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.tsmpDpMailLogDao = tsmpDpMailLogDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	@Override
	public void runJob(JobHelperImpl jobHelper, JobManager jobManager) {
		try {
			this.logger.debug("--- Begin DeleteExpiredMailJob ---");
			deleteExpMail();

		} catch (Exception e) {
			this.logger.error("Delete expired mail log error!" + StackTraceUtil.logStackTrace(e));
		} finally{
			this.logger.debug("--- Finish DeleteExpiredMailJob ---");
		}
	}

	@Override
	public void replace(DeferrableJob source) {
		this.logger.trace(String.format("%s-%s is replaced.", this.getGroupId(), this.getId()));
	}
	
	protected Integer getExpDay() {
		TsmpDpItemsId id = new TsmpDpItemsId("HOUSEKEEPING", "short", LocaleType.EN_US);
		TsmpDpItems vo = getTsmpDpItemsCacheProxy().findById(id);
		String expDay = null;
		try {
			expDay = vo.getParam1();
			return Integer.valueOf(expDay);
		} catch (Exception e) {
			this.logger.warn(String.format("Invalid setting: exp-day = %s, set to default 30.", expDay));
		}
		return Integer.valueOf(30);
	}
	
	public void deleteExpMail() {
		Integer expDay = getExpDay();
		// 找出 Mail log 建立時間為 expDay 天前
		List<TsmpDpMailLog> mailLogList = getExpMailList(expDay);
		if (mailLogList == null || mailLogList.isEmpty()) {
			this.logger.debug("No expired mail log.");
			return;
		}
		this.logger.debug(String.format("Expired mail logs: %d", mailLogList.size()));
	
		for (TsmpDpMailLog log : mailLogList) {
			getTsmpDpMailLogDao().delete(log);
		}
	}
	
	private List<TsmpDpMailLog> getExpMailList(Integer expDay){
		LocalDate ld = LocalDate.now();
		ld = ld.minusDays(expDay);
		Date expDate = Date.from(ld.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		
		Optional<String> opt = DateTimeUtil.dateTimeToString(expDate, DateTimeFormatEnum.西元年月日時分秒_2);
		if (opt.isPresent()) {
			this.logger.debug("expDate = " + opt.get());
		}
		 List<TsmpDpMailLog> logList = getTsmpDpMailLogDao().queryExpiredMail(expDate);
		return logList;
	}
	
	protected TsmpDpMailLogDao getTsmpDpMailLogDao() {
		return this.tsmpDpMailLogDao;
	}
	
	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}
}
