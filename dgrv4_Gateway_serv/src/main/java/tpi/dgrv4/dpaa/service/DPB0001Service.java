package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.getKeywords;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpApplyStatus;
import tpi.dgrv4.dpaa.vo.DPB0001Api;
import tpi.dgrv4.dpaa.vo.DPB0001Req;
import tpi.dgrv4.dpaa.vo.DPB0001Resp;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.jpql.TsmpApiModule;
import tpi.dgrv4.entity.entity.jpql.TsmpDpApiAuth2;
import tpi.dgrv4.entity.entity.jpql.TsmpnApiModule;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiModuleDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpApiAuth2Dao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpnApiModuleDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0001Service {

	private TsmpApiDao tsmpApiDao;
	private TsmpApiModuleDao tsmpApiModuleDao;
	private TsmpDpApiAuth2Dao tsmpDpApiAuth2Dao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpClientDao tsmpClientDao;
	private TsmpnApiModuleDao tsmpnApiModuleDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public DPB0001Service(TsmpApiDao tsmpApiDao, TsmpApiModuleDao tsmpApiModuleDao, TsmpDpApiAuth2Dao tsmpDpApiAuth2Dao,
			TsmpOrganizationDao tsmpOrganizationDao, TsmpClientDao tsmpClientDao, TsmpnApiModuleDao tsmpnApiModuleDao,
			ServiceConfig serviceConfig) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiModuleDao = tsmpApiModuleDao;
		this.tsmpDpApiAuth2Dao = tsmpDpApiAuth2Dao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpnApiModuleDao = tsmpnApiModuleDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB0001Resp queryUnauthorizedLikeApi(TsmpAuthorization authorization, DPB0001Req req) {
		String orgId = authorization.getOrgId();
		List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
		String applyStatus = TsmpDpApplyStatus.REVIEW.value();	// 審核中
		String[] words = getKeywords(req.getKeyword(), " ");
		Long lastId = req.getApiAuthId();
		Integer pageSize = getPageSize();
		List<TsmpDpApiAuth2> unauthorizedList = getTsmpDpApiAuth2Dao()//
				.query_dpb0001Service_01(orgDescList, applyStatus, words, lastId, pageSize);
		if (unauthorizedList == null || unauthorizedList.isEmpty()) {
			throw TsmpDpAaRtnCode.NO_UNAUTHORIZED_API_INFO.throwing();
		}

		DPB0001Resp resp = new DPB0001Resp();
		List<DPB0001Api> dpb0001ApiList = getDpb0001ApiList(unauthorizedList);
		resp.setApiList(dpb0001ApiList);
		return resp;
	}

	private List<DPB0001Api> getDpb0001ApiList(List<TsmpDpApiAuth2> unauthorizedList) {
		List<DPB0001Api> dpb0001ApiList = new ArrayList<>();

		TsmpApi tsmpApi;
		//String moduleVersion = null;
		String clientName = null;
		DPB0001Api dpb0001Api;
		for(TsmpDpApiAuth2 auth : unauthorizedList) {
			tsmpApi = getTsmpApi(auth.getRefApiUid());
			if (tsmpApi == null) {
				throw TsmpDpAaRtnCode.NO_UNAUTHORIZED_API_DETAIL.throwing();
			}
			
			clientName = getClientName(auth.getRefClientId());
			
			dpb0001Api = new DPB0001Api();
			dpb0001Api.setApiAuthId(auth.getApiAuthId());
			dpb0001Api.setRefClientId(auth.getRefClientId());
			dpb0001Api.setRefClientName(clientName);
			dpb0001Api.setRefApiUid(auth.getRefApiUid());
			dpb0001Api.setApplyPurpose(auth.getApplyPurpose());
			dpb0001Api.setApiKey(tsmpApi.getApiKey());
			dpb0001Api.setModuleName(tsmpApi.getModuleName());
			dpb0001Api.setApiName(tsmpApi.getApiName());
			dpb0001Api.setApiDesc(tsmpApi.getApiDesc());

			//moduleVersion = getModuleVersion(tsmpApi);
			//dpb0001Api.setModuleVersion(moduleVersion);

			dpb0001Api.setLv(auth.getVersion());

			dpb0001ApiList.add(dpb0001Api);
		}

		return dpb0001ApiList;
	}

	private TsmpApi getTsmpApi(String apiUid) {
		TsmpApi tsmpApi = null;

		List<TsmpApi> apiList = getTsmpApiDao().findByApiUid(apiUid);
		if (apiList != null && !apiList.isEmpty()) {
			tsmpApi = apiList.get(0);
		}
		
		return tsmpApi;
	}

	private String getModuleVersion(TsmpApi api) {
		String apiSrc = api.getApiSrc();
		String moduleName = api.getModuleName();
		if ("N".equals(apiSrc)) {
			List<TsmpnApiModule> nModules = getTsmpnApiModuleDao().findByModuleNameAndActive(moduleName, true);
			if (nModules == null || nModules.isEmpty()) {
				throw TsmpDpAaRtnCode.NO_UNAUTHORIZED_API_MODULE.throwing();
			}
			TsmpnApiModule nModule = nModules.get(0);
			return nModule.getModuleVersion();
		} else if ("M".equals(apiSrc)) {
			List<TsmpApiModule> modules = getTsmpApiModuleDao().findByModuleNameAndActive(moduleName, true);
			if (modules == null || modules.isEmpty()) {
				throw TsmpDpAaRtnCode.NO_UNAUTHORIZED_API_MODULE.throwing();
			}
			TsmpApiModule module = modules.get(0);
			return module.getModuleVersion();
		// apiSrc = 'R' or apiSrc = 'C' 並無模組
		} else {
			return new String();
		}
	}

	private String getClientName(String clientId) {
		Optional<TsmpClient> opt = getTsmpClientDao().findById(clientId);
		if (opt.isPresent()) {
			return opt.get().getClientName();
		}
		return clientId;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpApiModuleDao getTsmpApiModuleDao() {
		return this.tsmpApiModuleDao;
	}

	protected TsmpDpApiAuth2Dao getTsmpDpApiAuth2Dao() {
		return this.tsmpDpApiAuth2Dao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}

	protected TsmpnApiModuleDao getTsmpnApiModuleDao() {
		return this.tsmpnApiModuleDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0001");
		return this.pageSize;
	}

}
