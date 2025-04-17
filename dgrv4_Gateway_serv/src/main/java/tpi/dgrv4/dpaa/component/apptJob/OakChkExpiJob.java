package tpi.dgrv4.dpaa.component.apptJob;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.service.SendOpenApiKeyExpiringMailService;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpOpenApiKey;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyDao;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;


/**
 * <b> 排程工作  </b><br>
 * 每天由排程執行檢查, 距離效期只剩 30 天的 Open API Key, 並 mail 通知 client
 * @author mini
 */
@SuppressWarnings("serial")
public class OakChkExpiJob extends ApptJob {

	private TPILogger logger = TPILogger.tl;

	private SendOpenApiKeyExpiringMailService sendOpenApiKeyExpiringMailService;
	private TsmpOpenApiKeyDao tsmpOpenApiKeyDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
 
	@Autowired
	public OakChkExpiJob(TsmpDpApptJob tsmpDpApptJob,
			SendOpenApiKeyExpiringMailService sendOpenApiKeyExpiringMailService, TsmpOpenApiKeyDao tsmpOpenApiKeyDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, ApptJobDispatcher apptJobDispatcher, TsmpDpApptJobDao tsmpDpApptJobDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		this.sendOpenApiKeyExpiringMailService = sendOpenApiKeyExpiringMailService;
		this.tsmpOpenApiKeyDao = tsmpOpenApiKeyDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}
	
	@Override
	public String runApptJob() throws Exception {
 
		step("PREP_OAK_CHK_EXPI");// 準備檢查快到期 Open API Key

		return checkExpiringOpenApiKey();
	}
 
	private Integer getExpDay() {
		TsmpDpItems vo = tsmpDpItemsCacheProxy.findByItemNoAndSubitemNoAndLocale("HOUSEKEEPING", "short", LocaleType.EN_US);
		String expDay = null;
		try {
			expDay = vo.getParam1();
			return Integer.valueOf(expDay);
		} catch (Exception e) {
			this.logger.warn(String.format("Invalid setting: exp-day = %s, set to default 30.", expDay));
		}
		return Integer.valueOf(30);
	}
	
	public String checkExpiringOpenApiKey() {
		int successCnt = 0;
		Integer expDay = getExpDay();
		// 找出 Open API Key, 距離效期只剩 expDay 天的 Open API Key
		List<TsmpOpenApiKey> oakList = getExpiringOpenApiKeyList(expDay);
	
		TsmpOpenApiKey oak = null;
		for (int i = 0; i < oakList.size(); i++) {
			oak = oakList.get(i);
			Long openApiKeyId = oak.getOpenApiKeyId();
			try {
				// 寄發Mail通知			
				sendEmail(openApiKeyId);
				
				successCnt++;
			} catch (Exception e) {
				logger.debug("" + e);
			} finally {
				step((i + 1) + "/" + oakList.size());
			}
		}
		return successCnt + "/" + oakList.size();
	}
	
	/**
	 * 找出距離效期只剩 N 天 且 尚未展期,快到期的Open API Key
	 * 
	 * @param expDay
	 * @return
	 */
	private List<TsmpOpenApiKey> getExpiringOpenApiKeyList(Integer expDay){
		LocalDate ld = LocalDate.now();
		ld = ld.plusDays(expDay);
		
		Date expDate = Date.from(ld.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		
		Optional<String> opt = DateTimeUtil.dateTimeToString(expDate, DateTimeFormatEnum.西元年月日_2);
		if (opt.isPresent()) {
			this.logger.debug("expDate = " + opt.get());
		}
		
		//
		Date today = DateTimeUtil.now();
		Optional<String> opt_today = DateTimeUtil.dateTimeToString(today, DateTimeFormatEnum.西元年月日_2);
		if (opt_today.isPresent()) {
			this.logger.debug("today = " + opt_today.get());
			String todayStr = opt_today.get();
			Optional<Date> opt_date = DateTimeUtil.stringToDateTime(todayStr, DateTimeFormatEnum.西元年月日_2);//yyyy/MM/dd
			if (opt_date.isPresent()) {
				today = opt_date.get();
			}
		}
		return this.tsmpOpenApiKeyDao.queryExpiringOpenApiKey(today.getTime(), expDate.getTime());
	}

	public void sendEmail(Long openApiKeyId) {
		getSendOpenApiKeyExpiringMailService().sendEmail(openApiKeyId);
	}
	
	protected SendOpenApiKeyExpiringMailService getSendOpenApiKeyExpiringMailService() {
		return this.sendOpenApiKeyExpiringMailService;
	}
}