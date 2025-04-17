package tpi.dgrv4.gateway.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpClientVgroup;
import tpi.dgrv4.entity.entity.TsmpGroupApi;
import tpi.dgrv4.entity.entity.TsmpVgroup;
import tpi.dgrv4.entity.entity.TsmpVgroupGroup;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpClientVgroupDao;
import tpi.dgrv4.entity.repository.TsmpGroupApiDao;
import tpi.dgrv4.entity.repository.TsmpVgroupDao;
import tpi.dgrv4.entity.repository.TsmpVgroupGroupDao;
import tpi.dgrv4.gateway.component.GtwIdPHelper;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.component.TokenHelper;
import tpi.dgrv4.gateway.exceptions.UnitTestExcption;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.GtwIdPVgroupApiItem;
import tpi.dgrv4.gateway.vo.GtwIdPVgroupItem;
import tpi.dgrv4.gateway.vo.GtwIdPVgroupResp;

@Service
public class GtwIdPVgroupService {
	
	private TsmpClientVgroupDao tsmpClientVgroupDao;
	private TsmpVgroupDao tsmpVgroupDao;
	private TsmpVgroupGroupDao tsmpVgroupGroupDao;
	private TsmpGroupApiDao tsmpGroupApiDao;
	private TsmpApiDao tsmpApiDao;
	private OauthClientDetailsDao oauthClientDetailsDao;
	private TokenHelper tokenHelper;
	private TsmpClientDao tsmpClientDao;
	private IdPHelper idPHelper;
	private GtwIdPHelper gtwIdPHelper;
	
	@Autowired
	public GtwIdPVgroupService(TsmpClientVgroupDao tsmpClientVgroupDao, TsmpVgroupDao tsmpVgroupDao,
			TsmpVgroupGroupDao tsmpVgroupGroupDao, TsmpGroupApiDao tsmpGroupApiDao, TsmpApiDao tsmpApiDao,
			OauthClientDetailsDao oauthClientDetailsDao, TokenHelper tokenHelper, TsmpClientDao tsmpClientDao,
			IdPHelper idPHelper, GtwIdPHelper gtwIdPHelper) {
		super();
		this.tsmpClientVgroupDao = tsmpClientVgroupDao;
		this.tsmpVgroupDao = tsmpVgroupDao;
		this.tsmpVgroupGroupDao = tsmpVgroupGroupDao;
		this.tsmpGroupApiDao = tsmpGroupApiDao;
		this.tsmpApiDao = tsmpApiDao;
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.tokenHelper = tokenHelper;
		this.tsmpClientDao = tsmpClientDao;
		this.idPHelper = idPHelper;
		this.gtwIdPHelper = gtwIdPHelper;
	}

	public GtwIdPVgroupResp getVgroupList(HttpHeaders httpHeaders, HttpServletRequest httpReq,
			HttpServletResponse httpResp, String idPType) throws Exception {
		
		String redirectUri = httpReq.getParameter("redirect_uri");
		try {
			String reqUri = httpReq.getRequestURI();
			String clientId = httpReq.getParameter("client_id");
			
			GtwIdPVgroupResp resp = getVgroupList(httpResp, idPType, clientId, redirectUri, reqUri);
			return resp;
			
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
    		// 重新導向到前端,顯示訊息
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
    		getGtwIdPHelper().redirectToShowMsg(httpResp, errMsg, idPType, redirectUri);
    		return null;
		}
	}

	public GtwIdPVgroupResp getVgroupList(HttpServletResponse httpResp, String idPType, String clientId,
			String redirectUri, String reqUri) throws Exception {
		GtwIdPVgroupResp resp = new GtwIdPVgroupResp();
		
		if (!StringUtils.hasLength(redirectUri)) {
			// 缺少必填參數 '%s'
			String errMsg = String.format(IdPHelper.MSG_MISSING_REQUIRED_PARAMETER, "redirect_uri");
			TPILogger.tl.debug(errMsg);
			if(httpResp == null) {// for Unit test
				throw new UnitTestExcption(errMsg);
			}
			getGtwIdPHelper().redirectToShowMsg(httpResp, errMsg, idPType, redirectUri);
			return null;
		}
		
		// 檢查傳入的資料
		if (!StringUtils.hasLength(clientId)) {
			// 缺少必填參數 '%s'
			String errMsg = String.format(IdPHelper.MSG_MISSING_REQUIRED_PARAMETER, "client_id");
			TPILogger.tl.debug(errMsg);
			if(httpResp == null) {// for Unit test
				throw new UnitTestExcption(errMsg);
			}
			getGtwIdPHelper().redirectToShowMsg(httpResp, errMsg, idPType, redirectUri);
			return null;
		}
		
		// 檢查 TSMP_CLIENT
		Optional<TsmpClient> opt_client = getTsmpClientDao().findById(clientId);
		if (!opt_client.isPresent()) {
			// Table [TSMP_CLIENT] 查不到 client
			String errMsg1 = "Table [TSMP_CLIENT] can't find client, client_id: " + clientId;
			String errMsg2 = TokenHelper.THE_CLIENT_WAS_NOT_FOUND + clientId;
			TPILogger.tl.debug(errMsg1 + ",\n" + errMsg2);
			if (httpResp == null) {// for Unit test
				throw new UnitTestExcption("TSMP_CLIENT " + errMsg2);
			}
			getGtwIdPHelper().redirectToShowMsg(httpResp, errMsg2, idPType, redirectUri);
			return null;
		}
		
		// 取得 Token 的授權期限
		Optional<OauthClientDetails> opt_authClientDetails = getOauthClientDetailsDao().findById(clientId);
		if (!opt_authClientDetails.isPresent()) {
			// Table [OAUTH_CLIENT_DETAILS] 查不到 client
			String errMsg1 = "Table [OAUTH_CLIENT_DETAILS] can't find client, client_id: " + clientId;
			String errMsg2 = TokenHelper.THE_CLIENT_WAS_NOT_FOUND + clientId;
			TPILogger.tl.debug(errMsg1 + ",\n" + errMsg2);

			if (httpResp == null) {// for Unit test
				throw new UnitTestExcption("OAUTH_CLIENT_DETAILS " + errMsg2);
			}
			getGtwIdPHelper().redirectToShowMsg(httpResp, errMsg2, idPType, redirectUri);
			return null;
		}

		OauthClientDetails authClientDetails = opt_authClientDetails.get();
		Long accessTokenValidity = authClientDetails.getAccessTokenValidity();// access token 授權期限, 單位為秒
		accessTokenValidity = getTokenHelper().getTokenValidity(accessTokenValidity);// 若授權期限沒有值, 設為10分鐘

		Long refreshTokenValidity = authClientDetails.getRefreshTokenValidity();// refresh token 授權期限, 單位為秒
		refreshTokenValidity = getTokenHelper().getTokenValidity(refreshTokenValidity);// 若授權期限沒有值, 設為10分鐘

		resp.setAccessTokenValidity(accessTokenValidity);
		resp.setRefreshTokenValidity(refreshTokenValidity);

		// 取得 client 虛擬群組的API清單
		resp = getVgroupApiData(resp, clientId);
		resp.setClientId(clientId);

		return resp;
	}
	
	/**
	 * 取得 client 虛擬群組的API清單
	 */
	private GtwIdPVgroupResp getVgroupApiData(GtwIdPVgroupResp resp, String clientId) {
		// 1.GtwIdPVgroupResp - clientId (TSMP_CLIENT_VGROUP)
		List<GtwIdPVgroupItem> gtwIdPVgroupItemList = new ArrayList<GtwIdPVgroupItem>();
		resp.setVgroupDataList(gtwIdPVgroupItemList);

		List<TsmpClientVgroup> tsmpClientVgroupList = getTsmpClientVgroupDao().findByClientId(clientId);
		tsmpClientVgroupList.forEach((tsmpClientVgroup) -> {
			String vgroupId = tsmpClientVgroup.getVgroupId();

			// 2.GtwIdPVgroupItem - vgroupId (TsmpVgroup)
			List<TsmpVgroup> tsmpVgroupList = getTsmpVgroupDao().findByVgroupId(vgroupId);
			tsmpVgroupList.forEach((tsmpVgroup) -> {
				GtwIdPVgroupItem gtwIdPVgroupItem = new GtwIdPVgroupItem();
				List<GtwIdPVgroupApiItem> gtwIdPVgroupApiItemList = new ArrayList<GtwIdPVgroupApiItem>();

				gtwIdPVgroupItemList.add(gtwIdPVgroupItem);
				String vgroupName = tsmpVgroup.getVgroupName();
				String vgroupAlias = tsmpVgroup.getVgroupAlias();
				// 顯示在畫面的值, 取 vgroupAlias,若沒值,則取 vgroupName
				String vgroupAliasShowUi = (StringUtils.hasLength(vgroupAlias)) ? vgroupAlias : vgroupName;
				gtwIdPVgroupItem.setVgroupId(vgroupId);
				gtwIdPVgroupItem.setVgroupName(vgroupName);
				gtwIdPVgroupItem.setVgroupAlias(vgroupAlias);
				gtwIdPVgroupItem.setVgroupAliasShowUi(vgroupAliasShowUi);
				gtwIdPVgroupItem.setApiDataList(gtwIdPVgroupApiItemList);

				List<TsmpVgroupGroup> tsmpVgroupGroupList = getTsmpVgroupGroupDao().findByVgroupId(vgroupId);
				tsmpVgroupGroupList.forEach((tsmpVgroupGroup) -> {
					String groupId = tsmpVgroupGroup.getGroupId();
					List<TsmpGroupApi> tsmpGroupApiList = getTsmpGroupApiDao().findByGroupId(groupId);
					tsmpGroupApiList.forEach((tsmpGroupApi) -> {
						String moduleName = tsmpGroupApi.getModuleName();
						String apiKey = tsmpGroupApi.getApiKey();
						// 3.GtwIdPVgroupApiItem - groupId (TSMP_API)
						TsmpApi tsmpApi = getTsmpApiDao().findByModuleNameAndApiKey(moduleName, apiKey);
						String apiName = tsmpApi.getApiName();
						String apiDesc = tsmpApi.getApiDesc();
						// 顯示在畫面的值,取 apiName,若沒值,則取 apiKey
						String apiNameShowUi = (StringUtils.hasLength(apiName)) ? apiName : apiKey;

						GtwIdPVgroupApiItem gtwIdPVgroupApiItem = new GtwIdPVgroupApiItem();
						gtwIdPVgroupApiItemList.add(gtwIdPVgroupApiItem);
						gtwIdPVgroupApiItem.setGroupId(groupId);
						gtwIdPVgroupApiItem.setApiKey(apiKey);
						gtwIdPVgroupApiItem.setApiName(apiName);
						gtwIdPVgroupApiItem.setApiNameShowUi(apiNameShowUi);
						gtwIdPVgroupApiItem.setApiDesc(apiDesc);

					});
				});
			});
		});

		return resp;
	}

	protected TsmpClientVgroupDao getTsmpClientVgroupDao() {
		return tsmpClientVgroupDao;
	}
	
	protected TsmpVgroupDao getTsmpVgroupDao() {
		return tsmpVgroupDao;
	}
	
	protected  TsmpVgroupGroupDao getTsmpVgroupGroupDao() {
		return tsmpVgroupGroupDao;
	}
	
	protected  TsmpGroupApiDao getTsmpGroupApiDao() {
		return tsmpGroupApiDao;
	}
	
	protected  TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}
	
	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return oauthClientDetailsDao;
	}
	
	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
	
	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}
	
	protected IdPHelper getIdPHelper() {
		return idPHelper;
	}
	
	protected GtwIdPHelper getGtwIdPHelper() {
		return gtwIdPHelper;
	}
}
