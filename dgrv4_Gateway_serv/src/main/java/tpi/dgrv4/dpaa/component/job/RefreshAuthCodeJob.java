package tpi.dgrv4.dpaa.component.job;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import org.springframework.util.CollectionUtils;

import tpi.dgrv4.common.constant.TsmpAuthCodeStatus;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.TsmpAuthCode;
import tpi.dgrv4.entity.repository.TsmpAuthCodeDao;
import tpi.dgrv4.gateway.component.job.DeferrableJob;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.keeper.TPILogger;

@SuppressWarnings("serial")
public class RefreshAuthCodeJob extends DeferrableJob {

	private TPILogger logger = TPILogger.tl;

	private TsmpAuthCodeDao tsmpAuthCodeDao;

	private Long expDay;

	public RefreshAuthCodeJob(Long expDay, TsmpAuthCodeDao tsmpAuthCodeDao) {
		// 預設 30 天
		if (expDay == null) {
			expDay = 30L;
		}
		this.expDay = expDay;
		this.tsmpAuthCodeDao = tsmpAuthCodeDao;
	}

	@Override
	public void runJob(JobHelperImpl jobHelper, JobManager jobManager) {
		try {
			Long expireDateTime = calExpireDateTime();
			
			// #1. 刪除無用許久的授權碼
			int delCnt = deleteExpiredAuthCodes(expireDateTime);
			this.logger.debug(String.format("已刪除 %d 筆授權碼", delCnt));
			
			// #2. 更新已失效的授權碼狀態
			int updCnt = updateExpiredAuthCodes();
			this.logger.debug(String.format("已更新 %d 筆授權碼", updCnt));
		} catch (Exception e) {
			this.logger.debug("Refresh tsmp_auth_code error\n" + StackTraceUtil.logStackTrace(e));
		}
	}

	public int deleteExpiredAuthCodes(Long expireDateTime) {
		int delCnt_1 = 0;
		List<TsmpAuthCode> authCodes = getTsmpAuthCodeDao().findByExpireDateTimeLessThan(expireDateTime);
		if (!CollectionUtils.isEmpty(authCodes)) {
			for (TsmpAuthCode authCode : authCodes) {
				getTsmpAuthCodeDao().delete(authCode);
				delCnt_1++;
			}
		}
		
		int delCnt_2 = 0;
		String status = TsmpAuthCodeStatus.USED.value();
		Date updateDateTime = new Date(expireDateTime);
		authCodes = getTsmpAuthCodeDao().findByStatusAndUpdateDateTimeLessThan(status, updateDateTime);
		if (!CollectionUtils.isEmpty(authCodes)) {
			for (TsmpAuthCode authCode : authCodes) {
				getTsmpAuthCodeDao().delete(authCode);
				delCnt_2++;
			}
		}
		
		delCnt_1 += delCnt_2;

		return (delCnt_1 > 0 ? delCnt_1 : -1);
	}

	public Long calExpireDateTime() {
		ZonedDateTime expireDateTime = ZonedDateTime.now().minusDays( getExpDay() );
		Long epochMilliSecond = expireDateTime.toEpochSecond() * 1000;
		this.logger.debug(String.format("準備刪除 %s 以前無效的授權碼 (%d)", expireDateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME), epochMilliSecond));
		return epochMilliSecond;
	}

	public int updateExpiredAuthCodes() {
		Long now = ZonedDateTime.now().toEpochSecond() * 1000;
		String status = TsmpAuthCodeStatus.INVALID.value();
		List<TsmpAuthCode> authCodes = getTsmpAuthCodeDao().findByExpireDateTimeLessThanAndStatusNot(now, status);
		if (CollectionUtils.isEmpty(authCodes)) {
			return -1;
		}

		int updCnt = 0;
		for (TsmpAuthCode tsmpAuthCode : authCodes) {
			tsmpAuthCode.setStatus(TsmpAuthCodeStatus.INVALID.value());
			tsmpAuthCode.setUpdateDateTime(DateTimeUtil.now());
			tsmpAuthCode.setUpdateUser("SYS");
			getTsmpAuthCodeDao().save(tsmpAuthCode);
			updCnt++;
		}
		
		return updCnt;
	}

	@Override
	public void replace(DeferrableJob source) {
		this.expDay = ((RefreshAuthCodeJob) source).getExpDay();
	}

	private Long getExpDay() {
		return this.expDay;
	}

	protected TsmpAuthCodeDao getTsmpAuthCodeDao() {
		return this.tsmpAuthCodeDao;
	}

}