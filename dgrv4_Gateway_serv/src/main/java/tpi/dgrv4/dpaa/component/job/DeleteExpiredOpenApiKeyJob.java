package tpi.dgrv4.dpaa.component.job;

import jakarta.transaction.Transactional;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpOpenApiKey;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyMapDao;
import tpi.dgrv4.gateway.component.job.DeferrableJob;
import tpi.dgrv4.gateway.component.job.JobHelperImpl;
import tpi.dgrv4.gateway.component.job.JobManager;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 刪除過期30天的Open API Key
 */ 
@SuppressWarnings("serial")
public class DeleteExpiredOpenApiKeyJob extends DeferrableJob {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpOpenApiKeyDao tsmpOpenApiKeyDao;
	private TsmpOpenApiKeyMapDao tsmpOpenApiKeyMapDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	
	public DeleteExpiredOpenApiKeyJob(TsmpOpenApiKeyDao tsmpOpenApiKeyDao, TsmpOpenApiKeyMapDao tsmpOpenApiKeyMapDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.tsmpOpenApiKeyDao = tsmpOpenApiKeyDao;
		this.tsmpOpenApiKeyMapDao = tsmpOpenApiKeyMapDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	@Override
	public void runJob(JobHelperImpl jobHelper, JobManager jobManager) {
		try {
			this.logger.debug("--- Begin DeleteExpiredOpenApiKeyJob ---");
			deleteExpOpenApiKey();

		} catch (Exception e) {
			this.logger.error("Delete expired open api key error!" + StackTraceUtil.logStackTrace(e));
		} finally{
			this.logger.debug("--- Finish DeleteExpiredOpenApiKeyJob ---");
		}
	}

	@Override
	public void replace(DeferrableJob source) {
		this.logger.trace(String.format("%s-%s is replaced.", this.getGroupId(), this.getId()));
	}
	
	private Integer getExpDay() {
		TsmpDpItems vo = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("HOUSEKEEPING", "short", LocaleType.EN_US);
		String expDay = null;
		try {
			expDay = vo.getParam1();
			return Integer.valueOf(expDay);
		} catch (Exception e) {
			this.logger.warn(String.format("Invalid setting: exp-day = %s, set to default 30.", expDay));
		}
		return Integer.valueOf(30);
	}
	
	@Transactional
	public void deleteExpOpenApiKey() {
		Integer expDay = getExpDay();
		// 找出 Open API Key, 效期為 expDay 天前
		List<TsmpOpenApiKey> oakList = getExpOpenApiKeyList(expDay);
		if (oakList == null || oakList.isEmpty()) {
			this.logger.debug("No expired open api key.");
			return;
		}
		this.logger.debug("Expired open api keys: " + oakList.size());
	
		for (TsmpOpenApiKey oak : oakList) {
			getTsmpOpenApiKeyDao().delete(oak);
			
			// 刪除對應的API
			getTsmpOpenApiKeyMapDao().deleteByRefOpenApiKeyId(oak.getOpenApiKeyId());

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());
		}
	}
	
	private List<TsmpOpenApiKey> getExpOpenApiKeyList(Integer expDay){
		LocalDate ld = LocalDate.now();
		ld = ld.minusDays(expDay);
		Date expDate = Date.from(ld.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		
		Optional<String> opt = DateTimeUtil.dateTimeToString(expDate, DateTimeFormatEnum.西元年月日時分秒_2);
		if (opt.isPresent()) {
			this.logger.debug("expDate = " + opt.get());
		}
		return this.getTsmpOpenApiKeyDao().queryExpiredOpenApiKey(expDate.getTime());
	}
	
	protected TsmpOpenApiKeyDao getTsmpOpenApiKeyDao() {
		return this.tsmpOpenApiKeyDao;
	}
	
	protected TsmpOpenApiKeyMapDao getTsmpOpenApiKeyMapDao() {
		return this.tsmpOpenApiKeyMapDao;
	}
	
	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}
}
