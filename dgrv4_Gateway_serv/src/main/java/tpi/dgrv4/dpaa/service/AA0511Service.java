package tpi.dgrv4.dpaa.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.ifs.TsmpCoreTokenBase;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.job.RefreshAuthCodeJob;
import tpi.dgrv4.dpaa.vo.AA0511Req;
import tpi.dgrv4.dpaa.vo.AA0511Resp;
import tpi.dgrv4.dpaa.vo.TsmpAuthCodeInfo;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpAuthCode;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpAuthCodeDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0511Service {

	private TPILogger logger = TPILogger.tl;

//	@Autowired(required = false)
	private TsmpCoreTokenBase tsmpCoreTokenBase;
	
	private ApplicationContext ctx;
	private TsmpSettingService tsmpSettingService;
	private JobHelper jobHelper;
	private ServiceConfig serviceConfig;
	private TsmpApiDao tsmpApiDao;
	private TsmpAuthCodeDao tsmpAuthCodeDao;
	private ObjectMapper objectMapper;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;

	private Long expTime;
	private Long expDay;
	
	@Autowired
	public AA0511Service(@Nullable TsmpCoreTokenBase tsmpCoreTokenBase, ApplicationContext ctx,
			TsmpSettingService tsmpSettingService, JobHelper jobHelper, ServiceConfig serviceConfig,
			TsmpApiDao tsmpApiDao, TsmpAuthCodeDao tsmpAuthCodeDao, ObjectMapper objectMapper,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.tsmpCoreTokenBase = tsmpCoreTokenBase;
		this.ctx = ctx;
		this.tsmpSettingService = tsmpSettingService;
		this.jobHelper = jobHelper;
		this.serviceConfig = serviceConfig;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpAuthCodeDao = tsmpAuthCodeDao;
		this.objectMapper = objectMapper;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	public AA0511Resp getAuthCode(TsmpAuthorization auth, AA0511Req req) {
		AA0511Resp resp = null;
		try {
			String userName = auth.getUserName();
			String authType = req.getAuthType();
			String resource = req.getResource();
			String subclass = req.getSubclass();
			
			resp = getResponseByAuthType(authType, resource, subclass);
			
			Date expiredTime = calExpiredTime();
			String authCode = genAuthCode(authType, expiredTime, userName);
			
			saveAndRefreshAuthCode(expiredTime, authCode, authType, userName);

			resp.setAuthCode(authCode);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.debug("Get auth code error: " + StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1430.throwing();
		}
		return resp;
	}

	public AA0511Resp getResponseByAuthType(String authType, String resource, String subclass) {
		AA0511Resp resp = new AA0511Resp();
		resp.setTargetPort(0);
		resp.setTargetPath(new String());

		if ("Composer".equals(authType)) {

			if (StringUtils.isEmpty(resource) || StringUtils.isEmpty(subclass)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			Integer targetPort = getTsmpSettingService().getVal_TSMP_COMPOSER_PORT();
			String targetPath = getTsmpSettingService().getVal_TSMP_COMPOSER_PATH();
			String apiUid = getTsmpApiUid(resource, subclass);
			targetPath += "/" + apiUid;	// ex: "/editor/tsmpApi/89AC884B-CB9B-44C6-A5C4-4E929A78AA17"
			
			resp.setTargetPort(targetPort);
			resp.setTargetPath(targetPath);
		} else if ("Proxy".equals(authType)) {
			Integer targetPort = getTsmpSettingService().getVal_TSMP_PROXY_PORT();
			String targetPath = "/login";
			
			resp.setTargetPort(targetPort);
			resp.setTargetPath(targetPath);
		} else if ("DevelopPortal".equals(authType)) {
			// DO NOTHING...
		} else if ("Net".equals(authType)) {
			/* 20221031, v4 無此項設定
			Integer targetPort = 0;
			String targetPath = getTsmpSettingService().getVal_TSMP_NET_ADDRESS();
			
			resp.setTargetPort(targetPort);
			resp.setTargetPath(targetPath);
			*/
			throw TsmpDpAaRtnCode._1432.throwing();
		} else if ("Other".equals(authType)) {
			// DO NOTHING...
		} else {
			throw TsmpDpAaRtnCode._1432.throwing();
		}

		return resp;
	}

	public String getTsmpApiUid(String moduleName, String apiKey) {
		TsmpApiId apiId = new TsmpApiId(apiKey, moduleName);
		Optional<TsmpApi> opt = getTsmpApiDao().findById(apiId);
		if (!opt.isPresent()) {
			throw TsmpDpAaRtnCode.NO_API_INFO.throwing();
		}
		String apiUid = opt.get().getApiUid();
		if (StringUtils.isEmpty(apiUid)) {
			throw TsmpDpAaRtnCode.NO_API_INFO.throwing();
		}
		return apiUid;
	}

	public String genAuthCode(String authType, Date expiredTime, String userName) throws Exception {
		TsmpAuthCodeInfo info = new TsmpAuthCodeInfo();
		info.setClient(authType);
		info.setUser(userName);
		info.setExpiredTime(expiredTime);
		info.setProxy_login_path(new String());
		info.setType(new String());
		info.setProxy_redirect_path(new String());
		
		String json = getObjectMapper().writeValueAsString(info);
		
		// 使用 Tsmp Server 憑證取公鑰做 RSA 加密, fileName: "tsmp-core-token.jks"
		String encJson = getTsmpCoreTokenBase().encrypt(json);
		encJson = URLEncoder.encode(encJson, StandardCharsets.UTF_8.name());
		return encJson;
	}

	// 以 (現在時間 + 授權碼有效期間)，計算出有效日期
	public Date calExpiredTime() {
		LocalDateTime ldt = LocalDateTime.now().plus( getExpTime() , ChronoUnit.MILLIS);
		ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault());
		Date expiredTime = Date.from( zdt.toInstant() );
		return expiredTime;
	}

	/**
	 * 依正常 OAuth 流程，會將 authCode 存入 DB，取用時，
	 * 再依 authCode 刪除這筆資料，本次實作因已將所有資料 (含 authcode) 以 RSA 加密傳送，
	 * 故實務上可不用儲存本資料表，但依資安記錄原則，仍會記錄一筆 authCode 資料，
	 * 並於取用 token 時，更新此資料的狀態為失效
	 * @param expiredTime
	 * @param authCode
	 * @param authType
	 * @param userName
	 */
	public void saveAndRefreshAuthCode(Date expiredTime, String authCode, String authType, String userName) {
		try {
			TsmpAuthCode tsmpAuthCode = new TsmpAuthCode();
			tsmpAuthCode.setAuthCode(authCode);
			Long expireDateTime = expiredTime.getTime();
			tsmpAuthCode.setExpireDateTime(expireDateTime);
			tsmpAuthCode.setAuthType(authType);
			tsmpAuthCode.setCreateDateTime(DateTimeUtil.now());
			tsmpAuthCode.setCreateUser(userName);
			tsmpAuthCode = getTsmpAuthCodeDao().save(tsmpAuthCode);
			
			RefreshAuthCodeJob job = getRefreshAuthCodeJob( getExpDay() );
			getJobHelper().add(job);
		} catch (Exception e) {
			this.logger.debug("Fail to save and refresh tsmp_auth_code: " + StackTraceUtil.logStackTrace(e));
		}
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}

	protected TsmpCoreTokenBase getTsmpCoreTokenBase() {
		return this.tsmpCoreTokenBase;
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpAuthCodeDao getTsmpAuthCodeDao() {
		return this.tsmpAuthCodeDao;
	}

	protected ObjectMapper getObjectMapper() {
		if (this.objectMapper == null) {
			this.objectMapper = new ObjectMapper();
		}
		return this.objectMapper;
	}

	protected Long getExpTime() {
		if (this.expTime == null) {
			String val = getTsmpSettingService().getVal_AUTH_CODE_EXP_TIME();
			if (StringUtils.isEmpty(val)) {
				// 預設 10 分鐘
				val = "600000";
			}
			this.expTime = Long.valueOf(val);
		}
		return this.expTime;
	}

	protected Long getExpDay() {
		if (this.expDay == null) {
			TsmpDpItems vo = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("HOUSEKEEPING", "short", LocaleType.EN_US);
			String val = null;
			if( vo != null) {
				val = vo.getParam1();
			}
			if (StringUtils.isEmpty(val)) {
				// 預設 30 天
				val = "30";
			}
			this.expDay = Long.valueOf(val);
		}
		return this.expDay;
	}

	protected RefreshAuthCodeJob getRefreshAuthCodeJob(Long expDay) {
		return (RefreshAuthCodeJob) getCtx().getBean("refreshAuthCodeJob", expDay);
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}
}
