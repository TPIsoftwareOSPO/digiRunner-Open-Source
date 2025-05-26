package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0229ModuleAPIKey;
import tpi.dgrv4.dpaa.vo.AA0229Req;
import tpi.dgrv4.dpaa.vo.AA0229Resp;
import tpi.dgrv4.dpaa.vo.AA0229VgroupInfo;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpVgroup;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpSecurityLevelDao;
import tpi.dgrv4.entity.repository.TsmpVgroupDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0229Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpVgroupDao tsmpVgroupDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpSecurityLevelDao securityLVDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public AA0229Service(TsmpVgroupDao tsmpVgroupDao, TsmpApiDao tsmpApiDao, TsmpSecurityLevelDao securityLVDao,
			ServiceConfig serviceConfig) {
		super();
		this.tsmpVgroupDao = tsmpVgroupDao;
		this.tsmpApiDao = tsmpApiDao;
		this.securityLVDao = securityLVDao;
		this.serviceConfig = serviceConfig;
	}

	/**
	 * 1.將TSMP_VGROUP、TSMP_VGROUP_GROUP與TSMP_GROUP_API與TSMP_API進行查詢並ORDER BY TSMP_VGROUP.VGROUP_NAME, TSMP_VGROUP.VGROUP_ID，條件 TSMP_VGROUP.SECURITY_LEVEL_ID =AA0229Req.securityLevelID + TSMP_VGROUP.VGROUP_ID NOT IN (select VGROUP_ID TSMP_CLIENT_VGROUP.CLIENT_ID = AA0229Req.clientID) +分頁 + 關鍵字搜尋，資料不可以重複。
	 * 2.將步驟1查詢到資料轉換成AA0229VgroupInfo。
	 * 3.查詢TSMP_API、TSMP_GROUP_API與TSMP_VGROUP_GROUP資料表( TSMP_GROUP_API.GROUP_ID= TSMP_VGROUP_GROUP.GROUP_ID AND TSMP_API.API_KEY=tsmp_group_api.API_KEY AND TSMP_API.MODULE_NAME = tsmp_group_api.MODULE_NAME)，條件 TSMP_VGROUP_GROUP.GROUP_ID = AA0228Resp.vgroupId每一筆資料並ORDER BY TSMP_API.MODULE_NAME, TSMP_API.API_NAME ，TSMP_API為主要查詢資料表，將查詢到資料放進AA0229Resp.moduleAPIKeyList。
	 * */
	public AA0229Resp queryVGroupList(TsmpAuthorization authorization, AA0229Req req) {
		AA0229Resp resp = new AA0229Resp();

		try {
			String vgroupId = req.getVgroupId();
			String securityLevelId = req.getSecurityLevelID();
			String clientId = req.getClientID();
			String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
			
			//1.將TSMP_VGROUP、TSMP_VGROUP_GROUP與TSMP_GROUP_API與TSMP_API進行查詢並ORDER BY TSMP_VGROUP.VGROUP_NAME
			//, TSMP_VGROUP.VGROUP_ID，條件 TSMP_VGROUP.SECURITY_LEVEL_ID =AA0229Req.securityLevelID 
			//+ TSMP_VGROUP.VGROUP_ID NOT IN (select VGROUP_ID TSMP_CLIENT_VGROUP.CLIENT_ID = AA0229Req.clientID) +分頁 + 關鍵字搜尋，資料不可以重複。
			TsmpVgroup lastTsmpVgroup = null;
			if(!StringUtils.isEmpty(vgroupId)) {
				Optional<TsmpVgroup> tsmpVgroup = getTsmpVgroupDao().findById(vgroupId);
				if (tsmpVgroup.isPresent()) {
					lastTsmpVgroup = tsmpVgroup.get();
				}
			}
			List<TsmpVgroup> vgroupRsList = getTsmpVgroupDao().findByAA0229Service(lastTsmpVgroup, securityLevelId, clientId, words, getPageSize());

			if(vgroupRsList.size() == 0) {
				//1298:查無資料
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			//2.將步驟1查詢到資料轉換成AA0229VgroupInfo。
			List<AA0229VgroupInfo> vgroupInfoList = new ArrayList<>();
			vgroupRsList.forEach(vo ->{
				AA0229VgroupInfo infoVo = new AA0229VgroupInfo();
				if(vo.getSecurityLevelId() == null) {
					infoVo.setSecurityLevelName("");
				}else {
					TsmpSecurityLevel securityLv= getSecurityLVDao().findById(vo.getSecurityLevelId()).orElse(null);
					if(securityLv != null) {
						infoVo.setSecurityLevelName(securityLv.getSecurityLevelName());
					}else {
						infoVo.setSecurityLevelName("");
					}
				}
				
				infoVo.setVgroupAlias(ServiceUtil.nvl(vo.getVgroupAlias()));
				infoVo.setVgroupDesc(ServiceUtil.nvl(vo.getVgroupDesc()));
				infoVo.setVgroupID(vo.getVgroupId());
				infoVo.setVgroupName(vo.getVgroupName());
				
				//3.查詢TSMP_API、TSMP_GROUP_API與TSMP_VGROUP_GROUP資料表( TSMP_GROUP_API.GROUP_ID= TSMP_VGROUP_GROUP.GROUP_ID
				//AND TSMP_API.API_KEY=tsmp_group_api.API_KEY AND TSMP_API.MODULE_NAME = tsmp_group_api.MODULE_NAME)
				//，條件 TSMP_VGROUP_GROUP.GROUP_ID = AA0228Resp.vgroupId每一筆資料並ORDER BY TSMP_API.MODULE_NAME
				//, TSMP_API.API_NAME ，TSMP_API為主要查詢資料表，將查詢到資料放進AA0229Resp.moduleAPIKeyList。
				List<AA0229ModuleAPIKey> aa0229ApiKeyList = new ArrayList<>();
				
				List<TsmpApi> apiList = getTsmpApiDao().findByAA0229Service(infoVo.getVgroupID());
				//同一個Module的放在一起
				String baseModuleName = null;
				List<String>apiKeyNameList = null;
				for(int i=0; i<apiList.size();i++) {
					TsmpApi apiVo = apiList.get(i);
					if(i == 0) {
						baseModuleName = apiVo.getModuleName();
						apiKeyNameList = new ArrayList<>();
					}

					//moduleName不相同時就裝載aa0229ApiKeyList
					if(!apiVo.getModuleName().equals(baseModuleName)) {

						AA0229ModuleAPIKey aa0229N_apiKeyVo = new AA0229ModuleAPIKey();
						String aa0229N_orgApiNameApiKey = String.join(",", apiKeyNameList);
						String apiNameApiKey = null;
						//若orgApiNameApiKey長度超過100，則截斷至長度100並補上"..."填入此欄位, 並設定 isTruncated 欄位為 true
						if(aa0229N_orgApiNameApiKey.length() > 100) {
							apiNameApiKey = aa0229N_orgApiNameApiKey.substring(0,100) + "...";
							aa0229N_apiKeyVo.setTruncated(true);
						}else {
							apiNameApiKey = aa0229N_orgApiNameApiKey;
							aa0229N_apiKeyVo.setTruncated(false);
						}
						aa0229N_apiKeyVo.setModuleName(baseModuleName);
						aa0229N_apiKeyVo.setApiNameApiKey(apiNameApiKey);
						aa0229N_apiKeyVo.setOrgApiNameApiKey(aa0229N_orgApiNameApiKey);
						aa0229ApiKeyList.add(aa0229N_apiKeyVo);
						
						baseModuleName = apiVo.getModuleName();
						apiKeyNameList = new ArrayList<>();
						apiKeyNameList.add(apiVo.getApiName() + "(" + apiVo.getApiKey() + ")");
					}else {//代表同一個moduleName,増加apiKey和apiName的值
						apiKeyNameList.add(apiVo.getApiName() + "(" + apiVo.getApiKey() + ")");
					}

				}
				//同一個Module的放在一起,最後一筆資料就裝載aa0229ApiKeyList
				if(apiList.size() > 0) {
					AA0229ModuleAPIKey aa0229L_apiKeyVo = new AA0229ModuleAPIKey();
					String aa0229L_orgApiNameApiKey = String.join(",", apiKeyNameList);
					String apiNameApiKey = null;
					//若orgApiNameApiKey長度超過100，則截斷至長度100並補上"..."填入此欄位, 並設定 isTruncated 欄位為 true
					if(aa0229L_orgApiNameApiKey.length() > 100) {
						apiNameApiKey = aa0229L_orgApiNameApiKey.substring(0,100) + "...";
						aa0229L_apiKeyVo.setTruncated(true);
					}else {
						apiNameApiKey = aa0229L_orgApiNameApiKey;
						aa0229L_apiKeyVo.setTruncated(false);
					}
					aa0229L_apiKeyVo.setModuleName(baseModuleName);
					aa0229L_apiKeyVo.setApiNameApiKey(apiNameApiKey);
					aa0229L_apiKeyVo.setOrgApiNameApiKey(aa0229L_orgApiNameApiKey);
					aa0229ApiKeyList.add(aa0229L_apiKeyVo);
				}
				
				
				infoVo.setModuleAPIKeyList(aa0229ApiKeyList);
				
				vgroupInfoList.add(infoVo);
			});
			
			resp.setVgroupInfoList(vgroupInfoList);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//1297:執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected TsmpVgroupDao getTsmpVgroupDao() {
		return tsmpVgroupDao;
	}

	protected TsmpSecurityLevelDao getSecurityLVDao() {
		return securityLVDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0229");
		return this.pageSize;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}
	
	

}
