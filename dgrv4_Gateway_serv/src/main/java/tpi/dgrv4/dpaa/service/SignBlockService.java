package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.dpaa.vo.SignBlockResp;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class SignBlockService {
	
	public static final String SIGNBLOCK_FIXED_VALUE = "DzANBgNVBAgTBnRhaXdhbjEPMA0GA1UEBxMGdGFpcGVpMRMwEQYDVQQKEwp0aGlu";
	
	private ServiceConfig serviceConfig;

	@Autowired
	public SignBlockService(ServiceConfig serviceConfig) {
		super();
		this.serviceConfig = serviceConfig;
	}

	public SignBlockResp getSignBlock(TsmpAuthorization tsmpAuthorization) throws Exception {
		String signBlock = SIGNBLOCK_FIXED_VALUE;
		SignBlockResp resp = new SignBlockResp();
		resp.setSignBlock(signBlock);
		return resp;
	}
	
	/* 2022-09-13; tsmpdpaa-v4 不使用 SignBlock 機制

	public static final String CACHE_KEY = "signblock";

	@Autowired
	private TsmpSettingService tsmpSettingService;
	
	@Autowired
	private TsmpRedisBuilder tsmpRedisBuilder;

	private TsmpRedis tsmpRedis;

	public SignBlockResp getSignBlock(TsmpAuthorization tsmpAuthorization) throws Exception {
		String signBlock = "";
		SignBlockResp resp = new SignBlockResp();
		try {
			String clientId = tsmpAuthorization.getClientId();

			this.logger.info(String.format("clientId:%s{}", clientId));
			if (!StringUtils.hasLength(clientId)) {
				throw TsmpDpAaRtnCode._1352.throwing("HTTP Authorization");
			}
			String signBlockKey = CACHE_KEY + ":" + clientId;
			this.logger.info(String.format("signBlockKey:%s", signBlockKey));

			String storeType = getServiceConfig().get("signblock.store-type");
			if (!StringUtils.hasLength(storeType)) {
				throw TsmpDpAaRtnCode._1474.throwing("store-type");
			}
			this.logger.info(String.format("storeType:%s", storeType));
			
			if ("fixed".equals(storeType)) {
				// 若 signblock.store-type 為 "fixed",則 signBlock 為設定值中 signblock.fixed.value 的固定值
				signBlock = getServiceConfigVal("signblock.fixed.value");

			} else if ("redis".contentEquals(storeType)){
				// 若 signblock.store-type 為 "redis",依 signBlockKey 在 Redis (24小時效期) 取得
				// signBlock值
				String redisType = getServiceConfig().get("signblock.redis-type");
				if (redisType == null) {
					throw TsmpDpAaRtnCode._1474.throwing("signblock.redis-type");
				}
				
				try {
					this.tsmpRedis = getTsmpRedisBuilder().build();
					signBlock = getSignBlock(signBlockKey);

					if (!StringUtils.hasLength(signBlock)) {
						// 若為空值,則使用 UUID random 取得新值,即為 signBlock,將 signBlock 和建立時間寫入 Redis
						signBlock = setSignBlock(signBlockKey);
					}
				} catch (Exception e) {
					// 取得 Redis 過程中,若發生任何Exception,則改為取 fixed 的固定值
					signBlock = getServiceConfigVal("signblock.fixed.value");
					this.logger.error(String.format("從 Redis 取得 SignBlock 失敗! 回傳 fixed 值, signBlockKey=%s, %s", 
							signBlockKey, StackTraceUtil.logStackTrace(e)));
				} finally {
					// 20211008, 由 try with 機制 close connection , 不可 close connection pool
//					if(tsmpRedis != null) {
//						tsmpRedis.close();
//					}
				}
				
			} else {
				this.logger.info("signblock.store-type 不是 \"fixed\", 也不是 \"redis\", 而是\"" + storeType + "\"");
				throw TsmpDpAaRtnCode._1290.throwing();
			}
			
			if(!StringUtils.hasLength(signBlock)) {
				throw TsmpDpAaRtnCode._1354.throwing("signblock.fixed.value", "from " + storeType);
			}
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		resp.setSignBlock(signBlock);
		return resp;
	}

	private String getSignBlock(String signBlockKey) throws Exception {
		String value = getValueByScore(signBlockKey, 1.0);
		return value == null ? "" : value.toString();
	}

	private String getValueByScore(String redisKey, double score) throws Exception {
		if (redisKey == null) {
			return null;
		}
		try {
			Map<String, Double> map = tsmpRedis.zrangeByScoreWithScores(redisKey, 2, 1);
			for (Map.Entry<String, Double> entry : map.entrySet()) {
				if (entry.getValue() == score) {
					return entry.getKey().replace("\"", "");
				}
			}
		} catch (Exception e) {
			logger.error(String.format("Jedis getValueByScore error: : %s", StackTraceUtil.logStackTrace(e)));
			throw e;
		}
		return null;
	}

	public String setSignBlock(String signBlockKey) throws Exception {
		try {
			// 2021.05.12 修正 signBlock 回傳空字串的問題
			String uuid = getUUID();
			zadd(signBlockKey, 1, uuid);
			zadd(signBlockKey, 2, String.valueOf(getTimeMilliseconds()));
			int signBlockTTL = getTsmpSettingService().getVal_TSMP_SIGNBLOCK_EXPIRED() * 60 * 60; // 86400 seconds
																									// //設定24小時過期
			expire(signBlockKey, signBlockTTL);
			return uuid;
		} catch (Exception e) {
			logger.error(String.format("Jedis setSignBlock error: %s", StackTraceUtil.logStackTrace(e)));
			throw new Exception(e);
		}
	}

	private Long zadd(String key, double score, String member) throws Exception {
		try {
			return tsmpRedis.zadd(key, score, member);
		} catch (Exception ex) {
			logger.error(String.format("Jedis zadd error: %s", StackTraceUtil.logStackTrace(ex)));
			throw new Exception(ex);
		}
	}

	private Long expire(String key, int seconds) throws Exception {
		try {
			return tsmpRedis.expire(key, seconds);
		} catch (Exception ex) {
			logger.error(String.format("Jedis expire error: %s", StackTraceUtil.logStackTrace(ex)));
			throw new Exception(ex);
		}
	}

	private String getUUID() {
		return UUID.randomUUID().toString();
	}

	private long getTimeMilliseconds() {
		Date date = new Date();
		return date.getTime();
	}

	private String getServiceConfigVal(String key) throws Exception {
		String val = getServiceConfig().get(key);
		if (!StringUtils.hasLength(val)) {
			this.logger.debug("key:" + key);
			throw TsmpDpAaRtnCode._1474.throwing(key);
		}
		val = val.trim();
		return val;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}
	
	protected TsmpRedisBuilder getTsmpRedisBuilder() {
		return this.tsmpRedisBuilder;
	}
	
	*/
	
	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

}
