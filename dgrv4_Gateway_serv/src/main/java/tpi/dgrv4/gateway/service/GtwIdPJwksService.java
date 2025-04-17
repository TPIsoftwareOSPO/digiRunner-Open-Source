package tpi.dgrv4.gateway.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWK;

import tpi.dgrv4.codec.utils.JWKcodec;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class GtwIdPJwksService {

	ObjectMapper objectMapper;
	TsmpSettingService tsmpSettingService;
	TokenHelper tokenHelper;

	@Autowired
	public GtwIdPJwksService(ObjectMapper objectMapper, TsmpSettingService tsmpSettingService,
			TokenHelper tokenHelper) {
		super();
		this.objectMapper = objectMapper;
		this.tsmpSettingService = tsmpSettingService;
		this.tokenHelper = tokenHelper;
	}

	public ResponseEntity<?> getGtwIdPJwks(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpResp) throws Exception {
		String reqUri = httpReq.getRequestURI();
		ResponseEntity<?> respEntity = null;
		try {
			String respJsonStr = getJwksJsonStr();
			return new ResponseEntity<Object>(respJsonStr, HttpStatus.OK);

		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			respEntity = getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
			return respEntity;
		}
	}

	/**
	 * 取得 JWKS 公鑰 JSON
	 */
	public String getJwksJsonStr() throws Exception {
		String jwkStr1 = getTsmpSettingService().getVal_GTW_IDP_JWK1();// 第1組
		String jwkStr2 = getTsmpSettingService().getVal_GTW_IDP_JWK2();// 第2組

		JWK jwk1 = JWK.parse(jwkStr1);
		JWK jwk2 = JWK.parse(jwkStr2);

		String respJsonStr = JWKcodec.generateJwkUri(jwk1, jwk2);// 取得公鑰
		respJsonStr = JWKcodec.toPrettyJson(respJsonStr);

		return respJsonStr;
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
}
