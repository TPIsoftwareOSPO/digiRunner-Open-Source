package tpi.dgrv4.dpaa.service;

import java.security.KeyPair;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.JWEcodec;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0142Req;
import tpi.dgrv4.dpaa.vo.DPB0142Resp;
import tpi.dgrv4.entity.component.cipher.TsmpCoreTokenEntityHelper;
import tpi.dgrv4.entity.component.cipher.TsmpCoreTokenInitializer;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.ComposerWebSocketClientConn;

@Service
public class DPB0142Service {

	public static class JWEInfo {

		// 現在時間(long)
		private long time = Instant.now().toEpochMilli();

		// User IP
		private String userIp;

		// 傳入的 token
		private String token;

		public long getTime() {
			return time;
		}

		public void setTime(long time) {
			this.time = time;
		}

		public String getUserIp() {
			return userIp;
		}

		public void setUserIp(String userIp) {
			this.userIp = userIp;
		}

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}
		
	}

	private TPILogger logger = TPILogger.tl;

	private TsmpApiDao tsmpApiDao;
	private TsmpSettingService tsmpSettingService;
	private TsmpCoreTokenEntityHelper tsmpCoreTokenHelper;
	private ObjectMapper objectMapper;
	private ComposerWebSocketClientConn composerWebSocketClient;

	@Autowired
	public DPB0142Service(TsmpApiDao tsmpApiDao, TsmpSettingService tsmpSettingService,
			TsmpCoreTokenEntityHelper tsmpCoreTokenHelper, ObjectMapper objectMapper,
			ComposerWebSocketClientConn composerWebSocketClient) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpCoreTokenHelper = tsmpCoreTokenHelper;
		this.objectMapper = objectMapper;
		this.composerWebSocketClient = composerWebSocketClient;
	}

	public DPB0142Resp getACEntryTicket(DPB0142Req req, String userIP, String authorization) {
		String resource = req.getResource();
		String subclass = req.getSubclass();
		// 檢查參數
		checkParams(resource, subclass);

		// 取得 API UUID
		String apiUuid = getTsmpApiUid(resource, subclass);
		
		// 取得 targetPort
		Integer targetPort = getTargetPort();

		// 取得 targetPath
		String targetPath = getTargetPath(apiUuid);
		
		// 取得 JWE
		String jweString = null;
		try {
			jweString = getJWEString(userIP, authorization);
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		//ws client可能還沒連線,所以要連線看看
		this.startWS();
		
		DPB0142Resp resp = new DPB0142Resp();
		resp.setAuthCode(jweString);
		resp.setTargetPort(targetPort);
		resp.setTargetPath(targetPath);
		resp.setApiUid(apiUuid);
		return resp;
	}
	
	protected void startWS() {
		getComposerWebSocketClient().startWS();
	}

	protected void checkParams(String resource, String subclass) {
		if (StringUtils.isEmpty(resource) || StringUtils.isEmpty(subclass)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	protected String getTsmpApiUid(String moduleName, String apiKey) {
		TsmpApiId apiId = new TsmpApiId(apiKey, moduleName);
		Optional<TsmpApi> opt = getTsmpApiDao().findById(apiId);
		if (!opt.isPresent()) {
			throw TsmpDpAaRtnCode.NO_API_INFO.throwing();
		}
		String apiUuid = opt.get().getApiUid();
		if (StringUtils.isEmpty(apiUuid)) {
			throw TsmpDpAaRtnCode.NO_API_INFO.throwing();
		}
		return apiUuid;
	}

	protected String getTargetPath(String apiUuid) {
		String targetPath = getTargetPath();
		targetPath += "/" + apiUuid;	// ex: "/editor/tsmpApi/89AC884B-CB9B-44C6-A5C4-4E929A78AA17"
		return targetPath;
	}

	protected String getJWEString(String userIP, String authorization) throws Exception {
		JWEInfo info = generateJWEInfo(null, userIP, authorization);
		String payload = new ObjectMapper().writeValueAsString(info);
		String encodedPayload = encodePayload(payload);
		return encodedPayload;
	}

	protected JWEInfo generateJWEInfo(Long time, String userIP, String token) {
		JWEInfo info = new JWEInfo();
		if (time != null) {
			info.setTime(time.longValue());
		}
		info.setUserIp(userIP);
		info.setToken(token);
		return info;
	}

	protected String encodePayload(String payload) throws Exception {
		KeyPair keyPair = getTsmpCoreTokenHelper().getKeyPair();
		if (keyPair == null) {
			throw new Exception("KeyPair was not found. "
				+ "Does '" + TsmpCoreTokenInitializer.KEY_PAIR_FILE_NAME + "' still exist in TSMP_DP_FILE?");
		}
		PublicKey publicKey = keyPair.getPublic();
		payload = JWEcodec.jweEncryption(publicKey, payload);
		return payload;
	}

	protected Integer getTargetPort() {
		Integer port = this.tsmpSettingService.getVal_TSMP_COMPOSER_PORT();
		return port;
	}

	protected String getTargetPath() {
		String path = this.tsmpSettingService.getVal_TSMP_COMPOSER_PATH();
		return path;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpCoreTokenEntityHelper getTsmpCoreTokenHelper() {
		return this.tsmpCoreTokenHelper;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected ComposerWebSocketClientConn getComposerWebSocketClient() {
		return composerWebSocketClient;
	}


}
