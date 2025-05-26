package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.getKeywords;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.constant.TsmpDpPublicFlag;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.vo.DPB0072Req;
import tpi.dgrv4.dpaa.vo.DPB0072Resp;
import tpi.dgrv4.dpaa.vo.DPB0072RespItem;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.entity.jpql.TsmpApiExt;
import tpi.dgrv4.entity.entity.jpql.TsmpApiExtId;
import tpi.dgrv4.entity.entity.jpql.TsmpDpApiTheme;
import tpi.dgrv4.entity.entity.sql.TsmpDpThemeCategory;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiExtDao;
import tpi.dgrv4.entity.repository.TsmpDpApiThemeDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpThemeCategoryDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0072Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpApiExtDao tsmpApiExtDao;
	private TsmpDpApiThemeDao tsmpDpApiThemeDao;
	private TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private ServiceConfig serviceConfig;
	private BcryptParamHelper bcryptParamHelper;

	private Integer pageSize;

	@Autowired
	public DPB0072Service(TsmpOrganizationDao tsmpOrganizationDao, TsmpApiDao tsmpApiDao, TsmpApiExtDao tsmpApiExtDao,
			TsmpDpApiThemeDao tsmpDpApiThemeDao, TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao,
			TsmpDpFileDao tsmpDpFileDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, ServiceConfig serviceConfig,
			BcryptParamHelper bcryptParamHelper) {
		super();
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiExtDao = tsmpApiExtDao;
		this.tsmpDpApiThemeDao = tsmpDpApiThemeDao;
		this.tsmpDpThemeCategoryDao = tsmpDpThemeCategoryDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.serviceConfig = serviceConfig;
		this.bcryptParamHelper = bcryptParamHelper;
	}

	public DPB0072Resp queryApiDpStatusLikeList(TsmpAuthorization auth, DPB0072Req req, ReqHeader reqHeader) {

		// 檢查必要參數
		final Date sdt = checkDt(req.getStartDate(), false);
		final Date edt = checkDt(req.getEndDate(), true);
		String orgFlagEncode = req.getOrgFlagEncode();//ex:0 / 1 , 使用BcryptParam設計, itemNo="ORG_FLAG"
		String orgId = auth.getOrgId();// 組織單位ID
		
		if (edt.compareTo(sdt) <= 0) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		this.logger.debug(String.format("duration: %s ~ %s", printDt(sdt), printDt(edt)));
		
		if (StringUtils.isEmpty(orgFlagEncode)){
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		if (StringUtils.isEmpty(orgId)){
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		String orgFlag = getDecodeOrgFlag(orgFlagEncode, reqHeader.getLocale());//解碼
		
		String[] words = getKeywords(req.getKeyword(), " ");
		String apiKey = req.getApiKey();
		String moduleName = req.getModuleName();
		
		// 找出包含此 orgId 及其向下的所有組織
		List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);

		return queryApiDpStatusLikeList(sdt, edt, words, getPageSize(), orgDescList, orgFlag, apiKey, moduleName, reqHeader.getLocale());
	}

	public DPB0072Resp queryApiDpStatusLikeList(Date sdt, Date edt, String[] words, Integer pageSize, 
			List<String> orgDescList, String orgFlag, String apiKey, String moduleName, String locale) {
				
		TsmpApi lastRecord = getLastRecordFromPrevPage(apiKey, moduleName);
		List<TsmpApi> apiList = getTsmpApiDao().query_dpb0072Service(sdt, edt, words, lastRecord, 
				getPageSize(), orgDescList, orgFlag);
		if (apiList == null || apiList.isEmpty()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		
		DPB0072Resp resp = new DPB0072Resp();
		List<DPB0072RespItem> dataList = getDataList(apiList, locale);
		resp.setDataList(dataList);
		return resp;
	}
	
	public String getDecodeOrgFlag(String orgFlagEncode, String locale) {
		String orgFlag = null;
		try {
			orgFlag = getBcryptParamHelper().decode(orgFlagEncode, "ORG_FLAG", locale);//BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return orgFlag;
	}
	
	private TsmpApi getLastRecordFromPrevPage(String apiKey, String moduleName) {
		if (!StringUtils.isEmpty(apiKey) && !StringUtils.isEmpty(moduleName)) {
			TsmpApiId id = new TsmpApiId(apiKey, moduleName);
			Optional<TsmpApi> opt = getTsmpApiDao().findById(id);
			return opt.orElse(null);
		}
		return null;
	}

	private List<DPB0072RespItem> getDataList(List<TsmpApi> apiList, String locale) {
		List<DPB0072RespItem> dpb0072RespItems = new ArrayList<>();
		DPB0072RespItem dpb0072RespItem = null;
		String apiUid = null;
		String apiName = null;
		String apiDesc = null;
		String themeName = null;
		String orgName = null;
		TsmpApiExt apiExt = null;
		String dpStuDateTime = null;
		Long apiExtId = null;
		TsmpDpFile dpFile = null;
		String fileName = null;
		String filePath = null;
		String publicFlag = null;
		String publicFlagName = null;
		for (TsmpApi api : apiList) {
			apiUid = nvl(api.getApiUid());
			apiName = nvl(api.getApiName());
			apiDesc = nvl(api.getApiDesc());
			themeName = getThemeName(api.getApiUid());
			orgName = getOrgName(api.getOrgId());

			apiExt = getApiExt(api.getApiKey(), api.getModuleName());
			dpStuDateTime = new String();
			apiExtId = -1L;
			fileName = new String();
			filePath = new String();
			
			publicFlag = nvl(api.getPublicFlag());
			publicFlagName = getPublicFlagName(publicFlag, locale);
			
			if (apiExt != null) {
				dpStuDateTime = getDpStuDateTime(apiExt);
				apiExtId = apiExt.getApiExtId();
				dpFile = getDpFile(apiExtId);
				if (dpFile != null) {
					fileName = dpFile.getFileName();
					filePath = dpFile.getFilePath() + fileName;
				}
			}
			
			dpb0072RespItem = new DPB0072RespItem();
			dpb0072RespItem.setApiKey(api.getApiKey());
			dpb0072RespItem.setModuleName(api.getModuleName());
			dpb0072RespItem.setApiUid(apiUid);
			dpb0072RespItem.setApiName(apiName);
			dpb0072RespItem.setApiDesc(apiDesc);
			dpb0072RespItem.setThemeName(themeName);
			dpb0072RespItem.setOrgName(orgName);
			dpb0072RespItem.setDpStuDateTime(dpStuDateTime);
			dpb0072RespItem.setApiExtId(apiExtId);
			dpb0072RespItem.setFileName(fileName);
			dpb0072RespItem.setFilePath(filePath);
			dpb0072RespItem.setPublicFlag(publicFlag);
			dpb0072RespItem.setPublicFlagName(publicFlagName);
			dpb0072RespItems.add(dpb0072RespItem);
		}
		return dpb0072RespItems;
	}

	private String getThemeName(String apiUid) {
		List<TsmpDpApiTheme> atList = getTsmpDpApiThemeDao().findAllByRefApiUid(apiUid);
		if (atList == null || atList.isEmpty()) {
			return new String();
		}
		List<String> themeNameList = new ArrayList<>();
		String themeName = null;
		for(TsmpDpApiTheme at : atList) {
			themeName = getThemeNameById(at.getRefApiThemeId());
			if (!StringUtils.isEmpty(themeName)) {
				themeNameList.add(themeName);
			}
		}
		return String.join(", ", themeNameList);
	}

	private String getThemeNameById(Long apiThemeId) {
		Optional<TsmpDpThemeCategory> opt = getTsmpDpThemeCategoryDao().findById(apiThemeId);
		if (opt.isPresent()) {
			return opt.get().getApiThemeName();
		}
		return new String();
	}

	private TsmpApiExt getApiExt(String apiKey, String moduleName) {
		TsmpApiExtId id = new TsmpApiExtId(apiKey, moduleName);
		Optional<TsmpApiExt> opt = getTsmpApiExtDao().findById(id);
		return opt.orElse(null);
	}

	private String getDpStuDateTime(TsmpApiExt apiExt) {
		Date dt = apiExt.getDpStuDateTime();
		return DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日).orElse(new String());
	}

	private TsmpDpFile getDpFile(Long apiExtId) {
		List<TsmpDpFile> dpFiles = getTsmpDpFileDao().findByRefFileCateCodeAndRefId( //
				TsmpDpFileType.API_ATTACHMENT.value(), apiExtId);
		if (dpFiles != null && !dpFiles.isEmpty()) {
			return dpFiles.get(0);
		}
		return null;
	}

	private String getOrgName(String orgId) {
		if (!StringUtils.isEmpty(orgId)) {
			Optional<TsmpOrganization> opt = getTsmpOrganizationDao().findById(orgId);
			if (opt.isPresent()) {
				return opt.get().getOrgName();
			}
		}
		return new String();
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

	private Date checkDt(String dtStr, boolean isEnd) {
		if (StringUtils.isEmpty(dtStr)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		Optional<LocalDate> opt = DateTimeUtil.stringToLocalDate(dtStr, DateTimeFormatEnum.西元年月日_2);
		if (!opt.isPresent()) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		LocalDate ld = opt.get();
		if (isEnd) {
			ld = ld.plusDays(1L);
		}
		// set time to 00:00:00
		final ZonedDateTime ldt = ld.atStartOfDay(ZoneId.systemDefault());
		return Date.from(ldt.toInstant());
	}

	private String printDt(Date dt) {
		return DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日時分秒_2).orElse(null);
	}

	private String nvl(Object input) {
		if (input == null) {
			return new String();
		}
		return input.toString();
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpApiExtDao getTsmpApiExtDao() {
		return this.tsmpApiExtDao;
	}

	protected TsmpDpApiThemeDao getTsmpDpApiThemeDao() {
		return this.tsmpDpApiThemeDao;
	}

	protected TsmpDpThemeCategoryDao getTsmpDpThemeCategoryDao() {
		return this.tsmpDpThemeCategoryDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0072");
		return this.pageSize;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

}
