package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.getKeywords;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpPublicFlag;
import tpi.dgrv4.dpaa.vo.DPB0018Api;
import tpi.dgrv4.dpaa.vo.DPB0018Req;
import tpi.dgrv4.dpaa.vo.DPB0018Resp;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpDpClientext;
import tpi.dgrv4.entity.entity.jpql.TsmpApiModule;
import tpi.dgrv4.entity.entity.jpql.TsmpnApiModule;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiModuleDao;
import tpi.dgrv4.entity.repository.TsmpDpClientextDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpnApiModuleDao;
import tpi.dgrv4.entity.vo.DPB0018SearchCriteria;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0018Service {

	private TsmpApiDao tsmpApiDao;
	private TsmpApiModuleDao tsmpApiModuleDao;
	private TsmpDpClientextDao tsmpDpClientextDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpnApiModuleDao tsmpnApiModuleDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public DPB0018Service(TsmpApiDao tsmpApiDao, TsmpApiModuleDao tsmpApiModuleDao,
			TsmpDpClientextDao tsmpDpClientextDao, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpnApiModuleDao tsmpnApiModuleDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiModuleDao = tsmpApiModuleDao;
		this.tsmpDpClientextDao = tsmpDpClientextDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpnApiModuleDao = tsmpnApiModuleDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB0018Resp queryApiLikeList_1(TsmpAuthorization authorization, DPB0018Req req) {
		String orgId = authorization.getOrgId();
		List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
		TsmpApiId lastId = getLastIdFromPrevPage(req);
		String[] words = getKeywords(req.getKeyword(), " ");
		Integer pageSize = getPageSize();
		String publicFlag = getPublicFlag(authorization.getClientId());

		DPB0018SearchCriteria cri = new DPB0018SearchCriteria();
		cri.setLastId(lastId);
		cri.setWords(words);
		cri.setPageSize(pageSize);
		cri.setApiStatus("1");	// 啟用
		cri.setPublicFlag(publicFlag);
		cri.setOrgDescList(orgDescList);
		List<TsmpApi> apiList = getTsmpApiDao().query_dpb0018Service(cri);
		if (apiList == null || apiList.isEmpty()) {
			throw TsmpDpAaRtnCode.NO_API_LIST.throwing();
		}

		DPB0018Resp resp = new DPB0018Resp();
		List<DPB0018Api> dpb0018ApiList = getDpb0018ApiList(apiList);
		resp.setApiList(dpb0018ApiList);
		return resp;
	}

	private TsmpApiId getLastIdFromPrevPage(DPB0018Req req) {
		String apiKey = req.getApiKey();
		String moduleName = req.getModuleName();
		if (apiKey != null && !apiKey.isEmpty() &&
			moduleName != null && !moduleName.isEmpty()) {
			return new TsmpApiId(apiKey, moduleName);
		}
		return null;
	}

	// 20200804; Kim; 沒有設定權限就預設"-1"
	private String getPublicFlag(String clientId) {
		if (!StringUtils.isEmpty(clientId)) {
			Optional<TsmpDpClientext> opt = getTsmpDpClientextDao().findById(clientId);
			if (opt.isPresent()) {
				String publicFlag = opt.get().getPublicFlag();
				return (publicFlag == null ? TsmpDpPublicFlag.EMPTY.value() : publicFlag);
			}
		}
		return TsmpDpPublicFlag.PUBLIC.value();
	}

	private List<DPB0018Api> getDpb0018ApiList(List<TsmpApi> apiList) {
		List<DPB0018Api> dpb0018ApiList = new ArrayList<>();

		//String moduleVersion = null;
		DPB0018Api dpb0018Api;
		for(TsmpApi api : apiList) {
			dpb0018Api = new DPB0018Api();
			dpb0018Api.setApiKey(api.getApiKey());
			dpb0018Api.setModuleName(api.getModuleName());
			dpb0018Api.setApiName(api.getApiName());
			dpb0018Api.setApiStatus(api.getApiStatus());
			dpb0018Api.setApiSrc(api.getApiSrc());
			dpb0018Api.setApiDesc(api.getApiDesc());
			dpb0018Api.setApiUid(api.getApiUid());
			//moduleVersion = getModuleVersion(api);
			//dpb0018Api.setModuleVersion(moduleVersion);
			dpb0018ApiList.add(dpb0018Api);
		}
		
		return dpb0018ApiList;
	}

	private String getModuleVersion(TsmpApi api) {
		String apiSrc = api.getApiSrc();
		String moduleName = api.getModuleName();
		String moduleVersion = new String();
		if ("N".equals(apiSrc)) {
			List<TsmpnApiModule> nModules = getTsmpnApiModuleDao().queryActiveV3ModulesByModuleName(moduleName);
			if (nModules != null && !nModules.isEmpty()) {
				moduleVersion = nModules.get(0).getModuleVersion();
			}
		} else if ("M".equals(apiSrc)) {
			List<TsmpApiModule> modules = getTsmpApiModuleDao().queryActiveV3ModulesByModuleName(moduleName);
			if (modules != null && !modules.isEmpty()) {
				moduleVersion = modules.get(0).getModuleVersion();
			}
		// apiSrc = 'R' or apiSrc = 'C' 並無模組
		} else {
			moduleVersion = new String();
		}
		return moduleVersion;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0018");
		return this.pageSize;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpApiModuleDao getTsmpApiModuleDao() {
		return this.tsmpApiModuleDao;
	}

	protected TsmpDpClientextDao getTsmpDpClientextDao() {
		return this.tsmpDpClientextDao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected TsmpnApiModuleDao getTsmpnApiModuleDao() {
		return this.tsmpnApiModuleDao;
	}

}
