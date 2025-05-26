package tpi.dgrv4.dpaa.component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.SHA256Util;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpUtil;

@Component
public class ApiHelper_TSMP implements ApiHelper {

	private TPILogger logger = TPILogger.tl;

	private ObjectMapper om;
	
	private static final String AUTHORIZATION = "Authorization";

	@PostConstruct
	public void init() {
		this.om = new ObjectMapper();
	}

	/**
	 * 所需參數:<br>
	 * tokenUrl<br>
	 * auth<br>
	 * signBlockUrl<br>
	 * reqBody<br>
	 */
	@Override
	public String call(String reqUrl, Map<String, Object> params, HttpMethod method) throws Exception {
		HttpUtil.disableCertificateValidation();

		// 取Token
		String token = getTokenByClientCredentials(params);
		this.logger.debug(String.format("Token: %s", token));
		
		// 取SignBlock
		String signCode = getSignCode(token, params);
		this.logger.debug(String.format("SignCode: %s", signCode));

		this.logger.debug(String.format("reqUrl: %s", reqUrl));
		String reqBody = getString("reqBody", params, false);
		String resp = HttpUtil.post(reqUrl, reqBody, (conn) -> {
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty(AUTHORIZATION, token);
			conn.setRequestProperty("signCode", signCode);
		});
		this.logger.debug(String.format("Resp: %s", resp));

		return resp;
	}

	// 使用 client_credentials
	public String getTokenByClientCredentials(Map<String, Object> params) throws Exception {
		String tokenUrl = getString("tokenUrl", params, true);
		if (!StringUtils.hasLength(tokenUrl)) {
			throw new Exception("TokenUrl must be specified!");
		}
		
		final String authorization = getString("auth", params, true);
		if (!StringUtils.hasLength(authorization)) {
			throw new Exception("Auth is required!");
		}
		
		String grantType = "grant_type=client_credentials";

		this.logger.debug(String.format("tokenUrl: %s", tokenUrl));
		this.logger.debug(String.format("auth: %s", authorization));
		String body = HttpUtil.post(tokenUrl, grantType, (conn) -> {
			conn.setRequestProperty(AUTHORIZATION, authorization);
		});

		String token = null;
		try {
			JsonNode resBody = om.readTree(body);
			// access_token
			String accessToken = resBody.get("access_token").asText();
			// token_type
			String tokenType = resBody.get("token_type").asText();
			token = tokenType + " " + accessToken;
		} catch (Exception e) {
			logger.debug("" + e);
		}
		if (!StringUtils.hasLength(token)) {
			throw new Exception("Unable to get token!");
		}
		return token;
	}

	public String getSignCode(String token, Map<String, Object> params) throws Exception {
		String signBlockUrl = getString("signBlockUrl", params, true);
		if (!StringUtils.hasLength(signBlockUrl)) {
			throw new Exception("SignBlockUrl must be specified!");
		}
		String reqBody = getString("reqBody", params, false);

		this.logger.debug(String.format("signBlockUrl: %s", signBlockUrl));
		this.logger.debug(String.format("reqBody: %s", reqBody));
		String respJson = HttpUtil.get(signBlockUrl, null, (conn) -> {
			conn.setRequestProperty(AUTHORIZATION, token);
			conn.setRequestProperty("Content-Type", "application/json");
		});
		
		String signCode = null;
		try {
			JsonNode resp = om.readTree(respJson);
			JsonNode resHeader = resp.get("ResHeader");
			String rtnCode = resHeader.get("rtnCode").asText();
			if ("0000".equals(rtnCode)) {
				JsonNode resGetSignBlock = resp.get("Res_getSignBlock");
				String signBlock = resGetSignBlock.get("signBlock").asText();
				if (signBlock != null && !signBlock.isEmpty()) {
					byte[] inputByte = SHA256Util.getSHA256((signBlock + reqBody).getBytes(StandardCharsets.UTF_8));
					signCode = HttpUtil.byte2Hex(inputByte);
				}
			}
		} catch (Exception e) {
			logger.debug("" + e);
		}
		if (!StringUtils.hasLength(signCode)) {
			throw new Exception("Unable to get signCode!");
		}
		return signCode;
	}

}
