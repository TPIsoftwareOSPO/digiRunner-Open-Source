package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.constant.TsmpDpPublicFlag;
import tpi.dgrv4.dpaa.vo.TsmpApiItem;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.entity.jpql.TsmpApiExt;
import tpi.dgrv4.entity.entity.jpql.TsmpApiExtId;
import tpi.dgrv4.entity.entity.jpql.TsmpDpApiTheme;
import tpi.dgrv4.entity.entity.sql.TsmpDpThemeCategory;
import tpi.dgrv4.entity.repository.TsmpApiExtDao;
import tpi.dgrv4.entity.repository.TsmpDpApiThemeDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpThemeCategoryDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;

@Service
public class ApiItemService {

	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private TsmpDpApiThemeDao tsmpDpApiThemeDao;
	private TsmpApiExtDao tsmpApiExtDao;
	private TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpDpFileDao tsmpDpFileDao;

	@Autowired
	public ApiItemService(TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpDpApiThemeDao tsmpDpApiThemeDao,
			TsmpApiExtDao tsmpApiExtDao, TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao,
			TsmpOrganizationDao tsmpOrganizationDao, TsmpDpFileDao tsmpDpFileDao) {
		super();
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.tsmpDpApiThemeDao = tsmpDpApiThemeDao;
		this.tsmpApiExtDao = tsmpApiExtDao;
		this.tsmpDpThemeCategoryDao = tsmpDpThemeCategoryDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
	}

	public List<TsmpApiItem> getApiItemList(List<TsmpApi> apiList, String locale) {
		List<TsmpApiItem> itemList = new ArrayList<TsmpApiItem>();
		for (TsmpApi api : apiList) {
			TsmpApiItem item = getTsmpApiItem(api, locale); 
			itemList.add(item);
		}
		return itemList;
	}

	public TsmpApiItem getTsmpApiItem(TsmpApi api, String locale) {
		// 主題分類 & API對應檔
		String apiUid = api.getApiUid();
		String publicFlagName = getPublicFlagName(api.getPublicFlag(), locale);
		List<TsmpDpApiTheme> apiThemeList = getTsmpDpApiThemeDao().findAllByRefApiUid(apiUid);
		Map<Long, String> themeDatas = new HashMap<Long, String>();// Map<Long:themeId, String:themeName>
		for (TsmpDpApiTheme apiTheme : apiThemeList) {
			String themeName = null;
			Long refApiThemeId = null;
			if (apiThemeList != null && !apiThemeList.isEmpty()) {
				refApiThemeId = apiTheme.getRefApiThemeId();
				// 主題分類
				if (refApiThemeId != null) {
					Optional<TsmpDpThemeCategory> opt_themCate = getTsmpDpThemeCategoryDao().findById(refApiThemeId);
					if (opt_themCate.isPresent()) {
						TsmpDpThemeCategory themeCate = opt_themCate.get();
						themeName = themeCate.getApiThemeName();
					}
					themeDatas.put(refApiThemeId, themeName);
				}
			}
		}

		// API延伸欄位
		String dpStatus = null;
		Long apiExtId = null;
		TsmpApiExtId apiExtIdObj = getTsmpApiExtId(api.getApiKey(), api.getModuleName());
		if (apiExtIdObj != null) {
			Optional<TsmpApiExt> opt_apiExt = getTsmpApiExtDao().findById(apiExtIdObj);
			if (opt_apiExt.isPresent()) {
				TsmpApiExt apiExt = opt_apiExt.get();
				dpStatus = apiExt.getDpStatus();
				apiExtId = apiExt.getApiExtId();
			}
		}

		// 組織
		String orgId = null;
		String orgName = null;
		orgId = api.getOrgId();
		if (!StringUtils.isEmpty(orgId)) {
			Optional<TsmpOrganization> opt_org = getTsmpOrganizationDao().findById(orgId);
			if (opt_org.isPresent()) {
				TsmpOrganization o = opt_org.get();
				orgName = o.getOrgName();
			}
		}

		// 檔案(API說明文件)
		String fileName = null;
		String filePath = null;
		List<TsmpDpFile> fileList = getTsmpDpFileDao()
				.findByRefFileCateCodeAndRefId(TsmpDpFileType.API_ATTACHMENT.value(), apiExtId);
		if (fileList != null && !fileList.isEmpty()) {
			TsmpDpFile f = fileList.get(0);
			fileName = f.getFileName();
			filePath = f.getFilePath() + fileName;
		}

		TsmpApiItem item = new TsmpApiItem();
		item.setApiKey(api.getApiKey());
		item.setModuleName(api.getModuleName());
		item.setApiName(api.getApiName());
		item.setThemeDatas(themeDatas);
		item.setOrgId(orgId);
		item.setOrgName(orgName);
		item.setApiDesc(api.getApiDesc());
		item.setDpStatus(dpStatus);
		item.setApiExtId(apiExtId);
		item.setApiUid(apiUid);
		item.setFileName(fileName);
		item.setFilePath(filePath);
		item.setPublicFlag(nvl(api.getPublicFlag()));
		item.setPublicFlagName(publicFlagName);

		return item;
	}

	private String getPublicFlagName(String publicFlag, String locale) {
		if (StringUtils.isEmpty(publicFlag)) {
			publicFlag = TsmpDpPublicFlag.EMPTY.value();
		}
		
		TsmpDpItemsId id = new TsmpDpItemsId("API_AUTHORITY", publicFlag, locale);
		TsmpDpItems vo = getTsmpDpItemsCacheProxy().findById(id);
		if (vo != null) {
			return vo.getSubitemName();
		}
		return new String();
	}

	private TsmpApiExtId getTsmpApiExtId(String apiKey, String moduleName) {
		if (apiKey != null && !apiKey.isEmpty() && moduleName != null && !moduleName.isEmpty()) {
			return new TsmpApiExtId(apiKey, moduleName);
		}
		return null;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected TsmpDpApiThemeDao getTsmpDpApiThemeDao() {
		return this.tsmpDpApiThemeDao;
	}

	protected TsmpApiExtDao getTsmpApiExtDao() {
		return this.tsmpApiExtDao;
	}
	
	protected TsmpDpThemeCategoryDao getTsmpDpThemeCategoryDao() {
		return this.tsmpDpThemeCategoryDao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}
}
