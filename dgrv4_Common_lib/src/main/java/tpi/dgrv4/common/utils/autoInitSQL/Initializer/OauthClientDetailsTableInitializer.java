package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.OauthClientDetailsVo;
@Service
public class OauthClientDetailsTableInitializer {
	
	private  List<OauthClientDetailsVo> oauthClientDetailslist = new LinkedList<>();
	
	public List<OauthClientDetailsVo> insertOauthClientDetails() {
		try {
			String clientId;
			String resourceIds;
			String clientMima;
			String scope;
			String authorizedGrantTypes;
			String webServerRedirectUri;
			String authorities;
		    Long accessTokenValidity;
		    Long refreshTokenValidity;
			String additionalInformation;
			String autoapprove;
			//這段 hardcoded IP 已被 Tom Review 過了, 故取消 hotspot 標記
			createOauthClientDetails((clientId = "YWRtaW5Db25zb2xl"), (resourceIds = "YWRtaW5BUEk"), (clientMima = "$2a$10$i8Gm6mD4zZAlfznMCPWZpe2zNYkp74MzD.lP7suP.cgnXMijNcqGK"), (scope = "1000"), (authorizedGrantTypes = "refresh_token,password,client_credentials"), (webServerRedirectUri = "https://10.20.30.88:18442/dgrv4/tsmpac4/ac02/ac0202"), (authorities = "client"), (accessTokenValidity = 1800L), (refreshTokenValidity = 3600L), (additionalInformation = "{}"), (autoapprove = null));

		} catch (Exception e) {
			StackTraceUtil.logStackTrace(e);
			throw e;
		}
		return oauthClientDetailslist;
	}
	

	
	protected void createOauthClientDetails(String clientId, String resourceIds, String clientSecret, String scope,
			String authorizedGrantTypes, String webServerRedirectUri, String authorities, Long accessTokenValidity,
			Long refreshTokenValidity, String additionalInformation, String autoapprove) {
			OauthClientDetailsVo oauthClientDetails = new OauthClientDetailsVo();
			oauthClientDetails.setClientId(clientId);
			oauthClientDetails.setResourceIds(resourceIds);
			oauthClientDetails.setClientSecret(clientSecret);
			oauthClientDetails.setScope(scope);
			oauthClientDetails.setAuthorizedGrantTypes(authorizedGrantTypes);
			oauthClientDetails.setWebServerRedirectUri(webServerRedirectUri);
			oauthClientDetails.setAuthorities(authorities);
			oauthClientDetails.setAccessTokenValidity(accessTokenValidity);
			oauthClientDetails.setRefreshTokenValidity(refreshTokenValidity);
			oauthClientDetails.setAdditionalInformation(additionalInformation);
			oauthClientDetailslist.add(oauthClientDetails);
		
	}
	

}
