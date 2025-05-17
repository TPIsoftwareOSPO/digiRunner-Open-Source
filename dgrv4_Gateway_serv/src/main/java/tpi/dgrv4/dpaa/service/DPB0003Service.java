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
import tpi.dgrv4.dpaa.vo.DPB0003Api;
import tpi.dgrv4.dpaa.vo.DPB0003Req;
import tpi.dgrv4.dpaa.vo.DPB0003Resp;
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
public class DPB0003Service {

	private TsmpApiDao tsmpApiDao;
	private TsmpApiModuleDao tsmpApiModuleDao;
	private TsmpDpApiAuth2Dao tsmpDpApiAuth2Dao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpnApiModuleDao tsmpnApiModuleDao;
	private TsmpClientDao tsmpClientDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public DPB0003Service(TsmpApiDao tsmpApiDao, TsmpApiModuleDao tsmpApiModuleDao, TsmpDpApiAuth2Dao tsmpDpApiAuth2Dao,
			TsmpOrganizationDao tsmpOrganizationDao, TsmpnApiModuleDao tsmpnApiModuleDao, TsmpClientDao tsmpClientDao,
			ServiceConfig serviceConfig) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiModuleDao = tsmpApiModuleDao;
		this.tsmpDpApiAuth2Dao = tsmpDpApiAuth2Dao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpnApiModuleDao = tsmpnApiModuleDao;
		this.tsmpClientDao = tsmpClientDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB0003Resp queryApiLikeHistory(TsmpAuthorization authorization, DPB0003Req req) {
		String orgId = authorization.getOrgId();
		List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
		List<String> applyStatus = new ArrayList<>();
		applyStatus.add(TsmpDpApplyStatus.PASS.value());	// 通過
		applyStatus.add(TsmpDpApplyStatus.FAIL.value());	// 不通過
		String[] words = getKeywords(req.getKeyword(), " ");
		words = transWords(words);
		TsmpDpApiAuth2 lastRecord = getLastRecordFromPrevPage(req.getApiAuthId());
		Integer pageSize = getPageSize();
		List<TsmpDpApiAuth2> authHistory = getTsmpDpApiAuth2Dao()//
				.query_dpb0003Service_01(orgDescList, applyStatus, words, lastRecord, pageSize);
		if (authHistory == null || authHistory.isEmpty()) {
			throw TsmpDpAaRtnCode.NO_AUTHORIZATION_API_HISTORY.throwing();
		}

		DPB0003Resp resp = new DPB0003Resp();
		List<DPB0003Api> dpb0003ApiList = getDpb0003ApiList(authHistory);
		resp.setApiList(dpb0003ApiList);
		return resp;
	}

	// 若模糊查詢申請狀態, 須將中文關鍵字轉為代碼再搜尋 (ex: "通過" -> "PASS")
	private String[] transWords(String[] words) {
		if (words != null && words.length > 0) {
			for(int i = 0; i < words.length; i++) {
				words[i] = TsmpDpApplyStatus.getValue(words[i]);
			}
		}
		return words;
	}

	private TsmpDpApiAuth2 getLastRecordFromPrevPage(Long apiAuthId) {
		if (apiAuthId != null) {
			Optional<TsmpDpApiAuth2> opt = getTsmpDpApiAuth2Dao().findById(apiAuthId);
			if (opt.isPresent()) {
				return opt.get();
			}
		}
		return null;
	}

	private List<DPB0003Api> getDpb0003ApiList(List<TsmpDpApiAuth2> authHistory) {
		List<DPB0003Api> dpb0003ApiList = new ArrayList<>();

		TsmpApi tsmpApi;
		//String moduleVersion = null;
		String applyStatus = null;
		String refClientName = null;
		DPB0003Api dpb0003Api;
		for(TsmpDpApiAuth2 auth : authHistory) {
			tsmpApi = getTsmpApi(auth.getRefApiUid());
			if (tsmpApi == null) {
				throw TsmpDpAaRtnCode.NO_UNAUTHORIZED_API_DETAIL.throwing();
			}
			
			dpb0003Api = new DPB0003Api();
			dpb0003Api.setApiAuthId(auth.getApiAuthId());
			dpb0003Api.setRefClientId(auth.getRefClientId());
			refClientName = getRefClientName(auth.getRefClientId());
			dpb0003Api.setRefClientName(refClientName);
			dpb0003Api.setRefApiUid(auth.getRefApiUid());
			dpb0003Api.setApplyPurpose(auth.getApplyPurpose());
			dpb0003Api.setApiKey(tsmpApi.getApiKey());
			dpb0003Api.setModuleName(tsmpApi.getModuleName());
			dpb0003Api.setApiName(tsmpApi.getApiName());
			dpb0003Api.setApiDesc(tsmpApi.getApiDesc());

			//moduleVersion = getModuleVersion(tsmpApi);
			//dpb0003Api.setModuleVersion(moduleVersion);

			applyStatus = TsmpDpApplyStatus.getText(auth.getApplyStatus());
			dpb0003Api.setApplyStatus(applyStatus);
			dpb0003Api.setRefReviewUser(auth.getRefReviewUser());	// refReviewUser直接就是放userName
			dpb0003Api.setReviewRemark(auth.getReviewRemark());
			dpb0003ApiList.add(dpb0003Api);
		}

		return dpb0003ApiList;
	}

	private TsmpApi getTsmpApi(String apiUid) {
		TsmpApi tsmpApi = null;

		List<TsmpApi> apiList = getTsmpApiDao().findByApiUid(apiUid);
		if (apiList != null && !apiList.isEmpty()) {
			tsmpApi = apiList.get(0);
		}
		
		return tsmpApi;
	}

	private String getRefClientName(String refClientId) {
		if (refClientId != null) {
			Optional<TsmpClient> opt = getTsmpClientDao().findById(refClientId);
			if (opt.isPresent()) {
				return opt.get().getClientName();
			}
		}
		return new String();
	}

	private String getModuleVersion(TsmpApi api) {
		String apiSrc = api.getApiSrc();
		String moduleName = api.getModuleName();
		if ("N".equals(apiSrc)) {
			List<TsmpnApiModule> nModules = getTsmpnApiModuleDao().findByModuleNameAndActive(moduleName, true);
			if (nModules == null || nModules.isEmpty()) {
				throw TsmpDpAaRtnCode.NO_MODULE_DATA.throwing();
			}
			TsmpnApiModule nModule = nModules.get(0);
			return nModule.getModuleVersion();
		} else if ("M".equals(apiSrc)) {
			List<TsmpApiModule> modules = getTsmpApiModuleDao().findByModuleNameAndActive(moduleName, true);
			if (modules == null || modules.isEmpty()) {
				throw TsmpDpAaRtnCode.NO_MODULE_DATA.throwing();
			}
			TsmpApiModule module = modules.get(0);
			return module.getModuleVersion();
		// apiSrc = 'R' or apiSrc = 'C' 並無模組
		} else {
			return new String();
		}
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

	protected TsmpnApiModuleDao getTsmpnApiModuleDao() {
		return this.tsmpnApiModuleDao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0003");
		return this.pageSize;
	}

}
