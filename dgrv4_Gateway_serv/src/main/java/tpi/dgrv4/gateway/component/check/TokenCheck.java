package tpi.dgrv4.gateway.component.check;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.TsmpOpenApiKey;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyDao;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.component.TokenHelper.BasicAuthClientData;
import tpi.dgrv4.gateway.component.TokenHelper.DgrkAuthData;
import tpi.dgrv4.gateway.component.TokenHelper.JwtPayloadData;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.JsonNodeUtil;

@Component
public class TokenCheck {
	
	private TokenHelper tokenHelper;
	private TsmpOpenApiKeyDao tsmpOpenApiKeyDao;
 
	@Autowired
	public TokenCheck(TokenHelper tokenHelper, TsmpOpenApiKeyDao tsmpOpenApiKeyDao) {
		super();
		this.tokenHelper = tokenHelper;
		this.tsmpOpenApiKeyDao = tsmpOpenApiKeyDao;
	}

	/**
	 * 取得 cid, 依表頭 Authorization 的各種格式 <br>
	 * cid 為 v3 底層的名稱, 即 client ID <br>
	 */
	public String getCid(String authorization) {
		try {
			String cid = null;
			
			// 1.是否有 authorization
			if (!StringUtils.hasText(authorization)) {// 沒有 authorization
				String errMsg = TokenHelper.AUTHORIZATION_HAS_NO_VALUE;
				TPILogger.tl.debug(errMsg);
				return null;
			}
			
			// 2.basic 格式, Base64Encode(id:pwd)
			// 是否有"basic "字樣,忽略大小寫
			boolean hasBasic = getTokenHelper().checkHasKeyword(authorization, TokenHelper.BASIC);
			if(hasBasic) {
				cid = getCidForBasic(authorization);
				return cid;
			}
			
			// 3.bearer 格式, JWE/JWS
			// 是否有"bearer "字樣,忽略大小寫
			boolean hasBearer = getTokenHelper().checkHasKeyword(authorization, TokenHelper.BEARER);
			if(hasBearer) {
				cid = getCidForBearer(authorization);
				return cid;
			}

			// 4.DGRK 格式
			// 是否有"DGRK "字樣,忽略大小寫
			boolean hasDgrk = getTokenHelper().checkHasKeyword(authorization, TokenHelper.DGRK);
			if(hasDgrk) {
				cid = getCidForDgrk(authorization);
				return cid;
			}
			
			return cid;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw e;
		}
	}
	
	/**
	 * 取得 client ID, 依 basic 格式 <br>
	 */
	public String getCidForBasic(String authorization) {
		String cid = null;
		
		//取得 Basic Authorization 的 Client ID
		BasicAuthClientData basicAuthClientData = getTokenHelper().getAuthClientDataForBasic(authorization, null);
		ResponseEntity<?> respEntity = basicAuthClientData.getErrRespEntity();
		if(respEntity != null) {
			return null;
		}
		
		String[] cliendData = basicAuthClientData.getCliendData();
		cid = cliendData[0];
		
		return cid;
	}
	
	/**
	 * 取得 client ID, 依 bearer 格式
	 */
	public String getCidForBearer(String authorization) {
		String cid = null;
		boolean hasKeyword = getTokenHelper().checkHasKeyword(authorization, TokenHelper.BEARER);
		if(!hasKeyword) {
			return null;
		}
		try {
			String tokenStr = authorization.substring(TokenHelper.BEARER.length());
			JwtPayloadData jwtPayloadData = getTokenHelper().getJwtPayloadData(tokenStr);
			ResponseEntity<?> respEntity = jwtPayloadData.errRespEntity;
			if(respEntity != null) {
				return null;
			}
			JsonNode payloadJsonNode = jwtPayloadData.payloadJsonNode;
			cid = JsonNodeUtil.getNodeAsText(payloadJsonNode, "client_id");
			
		} catch (Exception e) {
			String error = TokenHelper.TOKEN_PARSING_ERROR;//token 解析錯誤
			TPILogger.tl.debug(error);
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			
			error = TokenHelper.CANNOT_CONVERT_ACCESS_TOKEN_TO_JSON;
			TPILogger.tl.debug(error);
			return null;
		}
		return cid;
	}
	
	/**
	 * 取得 client ID, 依 DGRK 格式
	 */
	public String getCidForDgrk(String authorization) {
		String cid = null;
		
		//取得 DGRK Authorization 的資料
		DgrkAuthData dgrkAuthData = getTokenHelper().getAuthDataForDgrk(authorization, null);
		ResponseEntity<?> respEntity = dgrkAuthData.getErrRespEntity();
		if(respEntity != null) {
			return null;
		}
		
		String[] authData = dgrkAuthData.getAuthData();
		String openApiKey = authData[0];
		
		TsmpOpenApiKey tsmpOpenApiKey = getTsmpOpenApiKeyDao().findFirstByOpenApiKey(openApiKey);
		//找不到 client
		if (tsmpOpenApiKey == null) {
			return null;
		}
		
		cid = tsmpOpenApiKey.getClientId();
 
		return cid;
	}
 
	protected TsmpOpenApiKeyDao getTsmpOpenApiKeyDao() {
		return tsmpOpenApiKeyDao;
	}
 
	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
}
