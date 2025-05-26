package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0021Api;
import tpi.dgrv4.dpaa.vo.DPB0021Req;
import tpi.dgrv4.dpaa.vo.DPB0021Resp;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.jpql.TsmpApiModule;
import tpi.dgrv4.entity.entity.jpql.TsmpDpApiTheme;
import tpi.dgrv4.entity.entity.sql.TsmpDpThemeCategory;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiModuleDao;
import tpi.dgrv4.entity.repository.TsmpDpApiThemeDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpThemeCategoryDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0021Service {

	private TPILogger logger = TPILogger.tl;;

	private TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao;
	private TsmpDpApiThemeDao tsmpDpApiThemeDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpApiModuleDao tsmpApiModuleDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private FileHelper fileHelper;

	@Autowired
	public DPB0021Service(TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao, TsmpDpApiThemeDao tsmpDpApiThemeDao,
			TsmpApiDao tsmpApiDao, TsmpApiModuleDao tsmpApiModuleDao, TsmpDpFileDao tsmpDpFileDao,
			FileHelper fileHelper) {
		super();
		this.tsmpDpThemeCategoryDao = tsmpDpThemeCategoryDao;
		this.tsmpDpApiThemeDao = tsmpDpApiThemeDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiModuleDao = tsmpApiModuleDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.fileHelper = fileHelper;
	}

	public DPB0021Resp queryThemeById(TsmpAuthorization authorization, DPB0021Req req) {
		TsmpDpThemeCategory category = getCategory(req.getApiThemeId());
		if (category == null) {
			throw TsmpDpAaRtnCode.NO_THEME_CATE_BY_ID.throwing();
		}

		DPB0021Resp resp = new DPB0021Resp();
		resp.setApiThemeName(category.getApiThemeName());
		resp.setDataStatus(category.getDataStatus());
		resp.setDataSort(category.getDataSort());
		List<DPB0021Api> dpb0021ApiList = getDpb0021ApiList(category.getApiThemeId());
		resp.setOrgApiList(dpb0021ApiList);
		setRespOrgIcon(resp, category.getApiThemeId());
		return resp;
	}

	private TsmpDpThemeCategory getCategory(Long apiThemeId) {
		if (apiThemeId != null) {
			Optional<TsmpDpThemeCategory> opt = getTsmpDpThemeCategoryDao().findById(apiThemeId);
			if (opt.isPresent()) {
				return opt.get();
			}
		}
		return null;
	}

	private List<DPB0021Api> getDpb0021ApiList(Long refApiThemeId) {
		List<DPB0021Api> dpb0021ApiList = new ArrayList<>();

		List<TsmpApi> apiList = getApiList(refApiThemeId);
		if (apiList != null && !apiList.isEmpty()) {
			//TsmpApiModule module;
			DPB0021Api dpb0021Api;
			for(TsmpApi api : apiList) {
				dpb0021Api = new DPB0021Api();
				dpb0021Api.setApiKey(api.getApiKey());
				dpb0021Api.setModuleName(api.getModuleName());
				dpb0021Api.setApiName(api.getApiName());
				dpb0021Api.setApiStatus(api.getApiStatus());
				dpb0021Api.setApiSrc(api.getApiSrc());
				dpb0021Api.setApiDesc(api.getApiDesc());
				dpb0021Api.setApiUid(api.getApiUid());
				/*
				module = getTsmpApiModule(api.getModuleName());
				if (module != null) {
					dpb0021Api.setModuleVersion(module.getModuleVersion());
				}
				*/
				dpb0021ApiList.add(dpb0021Api);
			}
		}
		
		return dpb0021ApiList;
	}

	private List<TsmpApi> getApiList(Long refApiThemeId) {
		List<TsmpDpApiTheme> apiThemeList = getTsmpDpApiThemeDao() //
				.findAllByRefApiThemeId(refApiThemeId);
		if (apiThemeList == null || apiThemeList.isEmpty()) {
			return null;
		}

		List<TsmpApi> apiList = new ArrayList<>();
		TsmpApi api;
		for(TsmpDpApiTheme apiTheme : apiThemeList) {
			api = getApi(apiTheme.getRefApiUid());
			if (api != null) {
				apiList.add(api);
			}
		}
		
		return apiList;
	}

	private TsmpApi getApi(String apiUid) {
		List<TsmpApi> apiList = getTsmpApiDao().findByApiUid(apiUid);
		if (apiList != null && !apiList.isEmpty()) {
			return apiList.get(0);
		}
		return null;
	}

	private TsmpApiModule getTsmpApiModule(String moduleName) {
		TsmpApiModule module = null;

		List<TsmpApiModule> moduleList = getTsmpApiModuleDao()//
				.findByModuleNameAndActive(moduleName, true);
		if (moduleList != null && !moduleList.isEmpty()) {
			module = moduleList.get(0);
		}
		
		return module;
	}

	private void setRespOrgIcon(DPB0021Resp resp, Long refId) {
		List<TsmpDpFile> iconList = getTsmpDpFileDao()//
				.findByRefFileCateCodeAndRefId(TsmpDpFileType.API_TH.value(), refId);

		if (iconList != null && !iconList.isEmpty()) {
			TsmpDpFile icon = iconList.get(0);
			byte[] iconFile = null;
			try {
				if("Y".equals(icon.getIsBlob())) {
					iconFile = getFileHelper().download(icon);
				}else {
					iconFile = getFileHelper().download01(icon.getFilePath(), icon.getFileName());
				}
				if (iconFile != null && iconFile.length > 0) {
					String orgIcon = Base64Util.base64EncodeWithoutPadding(iconFile);
					resp.setFileName(icon.getFileName());
					resp.setIconFileContent(orgIcon);
				}
			} catch (Exception e) {
				this.logger.error(StackTraceUtil.logStackTrace(e));
			}
		}
	}

	protected TsmpDpThemeCategoryDao getTsmpDpThemeCategoryDao() {
		return this.tsmpDpThemeCategoryDao;
	}

	protected TsmpDpApiThemeDao getTsmpDpApiThemeDao() {
		return this.tsmpDpApiThemeDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpApiModuleDao getTsmpApiModuleDao() {
		return this.tsmpApiModuleDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

}
