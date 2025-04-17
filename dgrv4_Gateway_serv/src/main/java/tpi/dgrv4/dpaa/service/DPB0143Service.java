package tpi.dgrv4.dpaa.service;

import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.JWEcodec;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0143Req;
import tpi.dgrv4.dpaa.vo.DPB0143Resp;
import tpi.dgrv4.entity.component.cipher.TsmpCoreTokenEntityHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class DPB0143Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpCoreTokenEntityHelper tsmpCoreTokenHelper;
	private ObjectMapper objectMapper;

	@Autowired
	public DPB0143Service(TsmpCoreTokenEntityHelper tsmpCoreTokenHelper, ObjectMapper objectMapper) {
		super();
		this.tsmpCoreTokenHelper = tsmpCoreTokenHelper;
		this.objectMapper = objectMapper;
	}

	public DPB0143Resp checkACEntryTicket(DPB0143Req req) {
		String reqJwe = req.getAuthCode();
		String reqUserIp = req.getUserIp();
		// 驗證參數
		checkParams(reqJwe, reqUserIp);
		
		// 解密 JWE
		String payload = decodePayload(reqJwe);
		
		// 檢查資料
		DPB0142Service.JWEInfo info = verifyPayload(reqUserIp, payload);
		
		// 取得 Token
		String jwtToken = info.getToken();
		DPB0143Resp resp = new DPB0143Resp();
		resp.setToken(jwtToken);
		return resp;
	}

	protected void checkParams(String authCode, String userIP) {
		if (!(StringUtils.hasLength(authCode) && StringUtils.hasLength(userIP))) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	protected String decodePayload(String payload) {
		PrivateKey privateKey = getTsmpCoreTokenHelper().getKeyPair().getPrivate();
		try {
			payload = JWEcodec.jweDecryption(privateKey, payload);
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1519.throwing();
		}
		return payload;
	}

	protected DPB0142Service.JWEInfo verifyPayload(String reqUserIp, String payload) {
		DPB0142Service.JWEInfo info = null;
		try {
			info = getObjectMapper().readValue(payload, DPB0142Service.JWEInfo.class);
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		// 判斷二者 User IP 是否相同
		String jwtUserIp = info.getUserIp();
		checkUserIp(reqUserIp, jwtUserIp);
		
		// 判斷時間是否在15秒內
		long jwtTime = info.getTime();
		checkTime(jwtTime);
		
		return info;
	}

	protected void checkUserIp(String reqUserIp, String jwtUserIp) {
		if (!reqUserIp.equals(jwtUserIp)) {
			this.logger.error(String.format("UserIp is not matched, expected = %s, actual = %s", jwtUserIp, reqUserIp));
			throw TsmpDpAaRtnCode._1520.throwing();
		}
	}

	protected void checkTime(long jwtTime) {
		long currentMillis = getCurrentMillis();
		if (jwtTime > currentMillis || currentMillis - jwtTime > 15000) {
			this.logger.error(String.format("Time is invalid or expired: %s", DateTimeUtil.dateTimeToString(new Date(jwtTime), DateTimeFormatEnum.西元年月日時分秒毫秒).orElse(null)));
			throw TsmpDpAaRtnCode._1520.throwing();
		}
	}

	protected long getCurrentMillis() {
		return Instant.now().toEpochMilli();
	}

	protected TsmpCoreTokenEntityHelper getTsmpCoreTokenHelper() {
		return this.tsmpCoreTokenHelper;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

}
