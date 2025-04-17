package tpi.dgrv4.dpaa.service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0219Req;
import tpi.dgrv4.dpaa.vo.AA0219Resp;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0219Service {

	private TPILogger logger = TPILogger.tl;
	
	private OauthClientDetailsDao oauthClientDetailsDao;
	private TsmpClientDao tsmpClientDao;
	
	@Autowired
	public AA0219Service(OauthClientDetailsDao oauthClientDetailsDao, TsmpClientDao tsmpClientDao) {
		super();
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.tsmpClientDao = tsmpClientDao;
	}

	/**
	 * 1.查詢oauth_client_details資料表，條件 client_id = AA0219Req.clientID，將查詢出來的資料放進AA0219Resp。
	 * */
	public AA0219Resp getTokenSettingbyClient(TsmpAuthorization authorization, AA0219Req req) {
		AA0219Resp resp = new AA0219Resp();;

		try {
			String clientId = req.getClientID();
			
			checkParam(req);
			
			OauthClientDetails oauthClientDetails = getOauthClientDetailsDao().findById(clientId).orElse(null);
			resp.setClientID(clientId);
			Set<String> typeSet = null;

			if (oauthClientDetails == null || oauthClientDetails.getAuthorizedGrantTypes() == null || oauthClientDetails.getAuthorizedGrantTypes().isEmpty()) {
				typeSet = new LinkedHashSet<>();
			} else {
				String[] arrType = oauthClientDetails.getAuthorizedGrantTypes().split(",");
				typeSet = new LinkedHashSet<>(Arrays.asList(arrType));
			}

			
			resp.setAuthorizedGrantType(typeSet);

			Optional.ofNullable(oauthClientDetails).ifPresent(oDetails -> {
				resp.setAccessTokenValidity(oDetails.getAccessTokenValidity());
				resp.setRaccessTokenValidity(oDetails.getRefreshTokenValidity());
				resp.setWebServerRedirectUri(oDetails.getWebServerRedirectUri());
				resp.setWebServerRedirectUri1(oDetails.getWebServerRedirectUri1());
				resp.setWebServerRedirectUri2(oDetails.getWebServerRedirectUri2());
				resp.setWebServerRedirectUri3(oDetails.getWebServerRedirectUri3());
				resp.setWebServerRedirectUri4(oDetails.getWebServerRedirectUri4());
				resp.setWebServerRedirectUri5(oDetails.getWebServerRedirectUri5());
			});


			
			Optional<TsmpClient> opt_tsmpClient = getTsmpClientDao().findById(clientId);
			if (opt_tsmpClient.isPresent()) {
				TsmpClient tsmpClient  = opt_tsmpClient.get();
				resp.setAccessTokenQuota(tsmpClient.getAccessTokenQuota()==null?0:tsmpClient.getAccessTokenQuota());
				resp.setRefreshTokenQuota(tsmpClient.getRefreshTokenQuota()==null?0:tsmpClient.getRefreshTokenQuota());
			}
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private void checkParam(AA0219Req req) {
		String clientId = req.getClientID();
		
		if(StringUtils.isEmpty(clientId)) {
			//1343:用戶端帳號:必填參數
			throw TsmpDpAaRtnCode._1343.throwing();
		}
	}

	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return oauthClientDetailsDao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}
}
