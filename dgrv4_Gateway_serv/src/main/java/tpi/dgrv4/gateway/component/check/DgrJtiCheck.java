package tpi.dgrv4.gateway.component.check;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.component.TokenHelper.JwtPayloadData;
import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpRtnCodeService;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.util.JsonNodeUtil;

@Component
public class DgrJtiCheck {
	
	private TokenHelper tokenHelper;
	private TsmpSettingService tsmpSettingService;
	private TsmpRtnCodeService tsmpRtnCodeService;
	
	@Autowired
	public DgrJtiCheck(TokenHelper tokenHelper, TsmpSettingService tsmpSettingService,
			TsmpRtnCodeService tsmpRtnCodeService) {
		super();
		this.tokenHelper = tokenHelper;
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpRtnCodeService = tsmpRtnCodeService;
	}

	public ResponseEntity<?> check(String uri, HttpServletRequest request) {
		String reqUri = request.getRequestURI();
		
		boolean isEnabled = getTsmpSettingService().getVal_CHECK_JTI_ENABLE();
		if(isEnabled) {
			boolean isDgrUrl = GatewayFilter.isDgrUrl(uri);// 是否為 dgR AC API
			
			//不驗証的API
			if(uri.length() >= 17 && (uri.substring(0, 17).equals("/dgrv4/11/DPB0189"))) {
				isDgrUrl = false;
			}
			
			if (isDgrUrl) {
				// 用CApiKey就不驗token
				String cuuid = request.getHeader("cuuid");
				if (StringUtils.hasLength(cuuid)) {
					return null;
				}
 
				String authorization = request.getHeader("Authorization");
				// 是否有 "Authorization"
				ResponseEntity<?> respEntity = getTokenHelper().checkHasAuthorization(authorization, reqUri);
				if (respEntity != null) {
					return respEntity;
				}

				// 是否有"bearer "字樣,忽略大小寫
				boolean hasBearer = getTokenHelper().checkHasKeyword(authorization, TokenHelper.BEARER);
				if (hasBearer == false) {// 沒有字樣
					String errMsg = TokenHelper.UNAUTHORIZED;
					TPILogger.tl.debug(errMsg);
					return getTokenHelper().getUnauthorizedErrorResp(reqUri, errMsg);
				}
			    
			    String tokenStr = authorization.substring(TokenHelper.BEARER.length());

			    // 取得 access token 的 payload,做 JWS 驗章 或 JWE 解密
			    JwtPayloadData oauthIntrospection_jwtPayloadData = getTokenHelper().getJwtPayloadData(tokenStr);
			    respEntity = oauthIntrospection_jwtPayloadData.errRespEntity;
			    if (respEntity != null) {
			        return respEntity;
			    }

			    JsonNode payloadJsonNode = oauthIntrospection_jwtPayloadData.payloadJsonNode;
			    
				// 檢查是否 token exp 沒有值 或 token 過期
				Long exp = JsonNodeUtil.getNodeAsLong(payloadJsonNode, "exp");
				respEntity = tokenHelper.checkAccessTokenExp(tokenStr, exp);
				if (respEntity != null) {// 資料有錯誤
					return respEntity;
				}

				// 檢查是否 token 已撤銷
				String jti = JsonNodeUtil.getNodeAsText(payloadJsonNode, "jti");
				respEntity = tokenHelper.checkAccessTokenRevoked(jti);
				if (respEntity != null) {// 資料有錯誤
					return respEntity;
				}
			}
		}
		
		return null;
	}
 
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
	
	protected TsmpRtnCodeService getTsmpRtnCodeService() {
		return tsmpRtnCodeService;
	}
}
