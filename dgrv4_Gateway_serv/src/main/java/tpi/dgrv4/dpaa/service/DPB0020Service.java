package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.getKeywords;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.dpaa.vo.DPB0020Api;
import tpi.dgrv4.dpaa.vo.DPB0020Req;
import tpi.dgrv4.dpaa.vo.DPB0020Resp;
import tpi.dgrv4.dpaa.vo.DPB0020Theme;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.jpql.TsmpApiModule;
import tpi.dgrv4.entity.entity.jpql.TsmpDpApiTheme;
import tpi.dgrv4.entity.entity.sql.TsmpDpThemeCategory;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiModuleDao;
import tpi.dgrv4.entity.repository.TsmpDpApiThemeDao;
import tpi.dgrv4.entity.repository.TsmpDpThemeCategoryDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0020Service {

	private TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpApiModuleDao tsmpApiModuleDao;
	private TsmpDpApiThemeDao tsmpDpApiThemeDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public DPB0020Service(TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao, TsmpApiDao tsmpApiDao,
			TsmpApiModuleDao tsmpApiModuleDao, TsmpDpApiThemeDao tsmpDpApiThemeDao,
			TsmpOrganizationDao tsmpOrganizationDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpDpThemeCategoryDao = tsmpDpThemeCategoryDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiModuleDao = tsmpApiModuleDao;
		this.tsmpDpApiThemeDao = tsmpDpApiThemeDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB0020Resp queryThemeLikeList(TsmpAuthorization authorization, DPB0020Req req) {
		String orgId = authorization.getOrgId();
		List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
		TsmpDpThemeCategory lastRecord = getLastRecordFromPrevPage(req);
		String dataStatus = req.getDataStatus();
		String[] words = getKeywords(req.getKeyword(), " ");
		Integer pageSize = getPageSize();
		List<TsmpDpThemeCategory> cateList = getTsmpDpThemeCategoryDao() //
				.query_dpb0020Service(orgDescList, dataStatus, words, lastRecord, pageSize);
		if (cateList == null || cateList.isEmpty()) {
			throw TsmpDpAaRtnCode.NO_THEME_CATE.throwing();
		}

		DPB0020Resp resp = new DPB0020Resp();
		List<DPB0020Theme> dpb0020ThemeList = getDpb0020ThemeList(cateList);
		resp.setThemeList(dpb0020ThemeList);
		return resp;
	}

	private TsmpDpThemeCategory getLastRecordFromPrevPage(DPB0020Req req) {
		Long apiThemeId = req.getApiThemeId();
		if (apiThemeId != null) {
			Optional<TsmpDpThemeCategory> opt = getTsmpDpThemeCategoryDao().findById(apiThemeId);
			if (opt.isPresent()) {
				return opt.get();
			}
		}
		return null;
	}

	private List<DPB0020Theme> getDpb0020ThemeList(List<TsmpDpThemeCategory> cateList){
		List<DPB0020Theme> dpb0020ThemeList = new ArrayList<>();

		List<DPB0020Api> dpb0020ApiList;
		DPB0020Theme dpb0020Theme;
		for(TsmpDpThemeCategory cate : cateList) {
			dpb0020Theme = new DPB0020Theme();
			dpb0020Theme.setApiThemeId(cate.getApiThemeId());
			dpb0020Theme.setApiThemeName(cate.getApiThemeName());
			dpb0020Theme.setDataStatus(TsmpDpDataStatus.text(cate.getDataStatus()));
			dpb0020Theme.setDataSort(cate.getDataSort());
			dpb0020ApiList = getDpb0020ApiList(cate.getApiThemeId());
			dpb0020Theme.setOrgApiList(dpb0020ApiList);
			dpb0020ThemeList.add(dpb0020Theme);
		}
		
		return dpb0020ThemeList;
	}

	private List<DPB0020Api> getDpb0020ApiList(Long refApiThemeId) {
		List<DPB0020Api> dpb0020ApiList = new ArrayList<>();

		List<TsmpApi> apiList = getApiList(refApiThemeId);
		if (apiList != null && !apiList.isEmpty()) {
			//TsmpApiModule module;
			DPB0020Api dpb0020Api;
			for(TsmpApi api : apiList) {
				dpb0020Api = new DPB0020Api();
				dpb0020Api.setApiKey(api.getApiKey());
				dpb0020Api.setModuleName(api.getModuleName());
				dpb0020Api.setApiName(api.getApiName());
				dpb0020Api.setApiStatus(api.getApiStatus());
				dpb0020Api.setApiSrc(api.getApiSrc());
				dpb0020Api.setApiDesc(api.getApiDesc());
				dpb0020Api.setApiUid(api.getApiUid());
				/*
				module = getTsmpApiModule(api.getModuleName());
				if (module != null) {
					dpb0020Api.setModuleVersion(module.getModuleVersion());
				}
				*/
				dpb0020ApiList.add(dpb0020Api);
			}
		}
		
		return dpb0020ApiList;
	}

	private List<TsmpApi> getApiList(Long refApiThemeId) {
		List<TsmpDpApiTheme> apiThemeList = getTsmpDpApiThemeDao() //
				.findAllByRefApiThemeId(refApiThemeId);
		if (apiThemeList == null || apiThemeList.isEmpty()) {
			return null;
		}

		List<TsmpApi> dpb0020_apiList = new ArrayList<>();
		TsmpApi api;
		for(TsmpDpApiTheme apiTheme : apiThemeList) {
			api = getApi(apiTheme.getRefApiUid());
			if (api != null) {
				dpb0020_apiList.add(api);
			}
		}
		
		return dpb0020_apiList;
	}

	private TsmpApi getApi(String apiUid) {
		List<TsmpApi> dpb0020_apiList = getTsmpApiDao().findByApiUid(apiUid);
		if (dpb0020_apiList != null && !dpb0020_apiList.isEmpty()) {
			return dpb0020_apiList.get(0);
		}
		return null;
	}

	private TsmpApiModule getTsmpApiModule(String moduleName) {
		TsmpApiModule dpb0020_module = null;

		List<TsmpApiModule> moduleList = getTsmpApiModuleDao()//
				.findByModuleNameAndActive(moduleName, true);
		if (moduleList != null && !moduleList.isEmpty()) {
			dpb0020_module = moduleList.get(0);
		}
		
		return dpb0020_module;
	}

	protected TsmpDpThemeCategoryDao getTsmpDpThemeCategoryDao() {
		return this.tsmpDpThemeCategoryDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpApiModuleDao getTsmpApiModuleDao() {
		return this.tsmpApiModuleDao;
	}

	protected TsmpDpApiThemeDao getTsmpDpApiThemeDao() {
		return this.tsmpDpApiThemeDao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0020");
		return this.pageSize;
	}

}
