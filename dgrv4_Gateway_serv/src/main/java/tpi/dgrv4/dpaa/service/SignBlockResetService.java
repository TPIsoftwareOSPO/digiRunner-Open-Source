package tpi.dgrv4.dpaa.service;

import org.springframework.stereotype.Service;

import tpi.dgrv4.dpaa.vo.SignBlockResetResp;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class SignBlockResetService {
	
	public SignBlockResetResp resetSignBlock(TsmpAuthorization tsmpAuthorization) throws Exception {
		String signBlock = SignBlockService.SIGNBLOCK_FIXED_VALUE;
		SignBlockResetResp resp = new SignBlockResetResp();
		resp.setSignBlock(signBlock);
		return resp;
	}
	
	/* 2022-09-13; tsmpdpaa-v4 不使用 SignBlock 機制

	private TPILogger logger = TPILogger.tl;

	public static final String CACHE_KEY = "signblock";

	@Autowired
	private ServiceConfig serviceConfig;
	
	@Autowired
	private TsmpSettingService tsmpSettingService;
	
	@Autowired
	private TsmpRedisBuilder tsmpRedisBuilder;

	private TsmpRedis tsmpRedis;

	public SignBlockResetResp resetSignBlock(TsmpAuthorization tsmpAuthorization) throws Exception {
		SignBlockResetResp resp = new SignBlockResetResp();
		String signBlock = "";
		try {

			String clientId = tsmpAuthorization.getClientId();
			if (!StringUtils.hasLength(clientId)) {
				return null;
			}

			String storeType = getServiceConfigVal("signblock.store-type");

			if ("fixed".equals(storeType)) {
				// 若 signblock.store-type 為 "fixed",則不處理, signBlock 為 application.properties 中
				// signblock.fixed.value 的固定值
				signBlock = getServiceConfigVal("signblock.fixed.value");

			} else {
				try {
					this.tsmpRedis = getTsmpRedisBuilder().build();
					signBlock = reset(clientId);

				} catch (TsmpDpAaException e) {
					throw e;
				} catch (Exception e) {
					signBlock = getServiceConfigVal("signblock.fixed.value");
					this.logger.error(String.format("resetSignBlock:從 Redis 取得 SignBlock 失敗: %s", StackTraceUtil.logStackTrace(e)));
					throw e;
				}
			}
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		resp.setSignBlock(signBlock);
		return resp;
	}

	protected long getNowTime() {
		long nowTime = new Date().getTime();
		return nowTime;
	}
	
	*/

	/**
	 * 若 signblock.store-type 為 "redis",依 clientId 從 Redis 取得 signBlock 的建立時間
	 * a.檢查現在時間和建立時間相差 < 5分鐘,則 throw 9928。 (5分鐘內不可重置signBlock) b.從 Redis 刪除 clientId
	 * 的 signBlock, c.使用 UUID random 取得新值,即為 signBlock,將 signBlock 和建立時間寫入 Redis
	 
	public String reset(String clientId) throws Exception {
		String signBlock;
		long createTime = getCreateTime(clientId);
		long nowTime = getNowTime();
		long minute = (nowTime - createTime) / (60 * 1000);
		if (minute < 5) {
			throw TsmpDpAaRtnCode._1470.throwing();
		}
		this.tsmpRedis.del(CACHE_KEY + ":" + clientId);
		String signBlockKey = CACHE_KEY + ":" + clientId;
		tsmpRedis.zadd(signBlockKey, 1, UUID.randomUUID().toString());
		tsmpRedis.zadd(signBlockKey, 2, String.valueOf(getTimeMilliseconds()));
		TsmpSettingService tsmpSettingService = getTsmpSettingService();
		int signBlockTTL = tsmpSettingService.getVal_TSMP_SIGNBLOCK_EXPIRED() * 60 * 60; // 86400 seconds
		// //設定24小時過期
		tsmpRedis.expire(signBlockKey, signBlockTTL);
		signBlock = getValueByScore(signBlockKey, 1.0);
		return signBlock == null ? "" : signBlock;
	}

	private String getValueByScore(String redisKey, double score) throws Exception {
		if (redisKey == null) {
			return null;
		}

		Map<String, Double> map = tsmpRedis.zrangeByScoreWithScores(redisKey, 2, 1);
		for (Map.Entry<String, Double> entry : map.entrySet()) {
			if (entry.getValue() == score) {
				return entry.getKey().replace("\"", "");
			}
		}
		return null;
	}

	private long getTimeMilliseconds() {
		Date date = new Date();
		return date.getTime();
	}

	private String getServiceConfigVal(String key) throws Exception {
		String val = getServiceConfig().get(key);
		if (StringUtils.isEmpty(val)) {
			throw TsmpDpAaRtnCode._1474.throwing(key);
		}
		val = val.trim();
		return val;
	}

	public long getCreateTime(String clientId) throws Exception {
		String value = getValueByScore(CACHE_KEY + ":" + clientId, 2.0);
		return StringUtils.isEmpty(value) ? 0 : Long.parseLong(value);
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}

	protected TsmpRedisBuilder getTsmpRedisBuilder() {
		return this.tsmpRedisBuilder;
	}
	
	*/

}
