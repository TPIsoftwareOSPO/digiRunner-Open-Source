package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.cache.proxy.TsmpOrganizationCacheProxy;
import tpi.dgrv4.dpaa.constant.TsmpApiSrc;
import tpi.dgrv4.dpaa.vo.AA0302Controls;
import tpi.dgrv4.dpaa.vo.AA0302KeyVal;
import tpi.dgrv4.dpaa.vo.AA0302Pair;
import tpi.dgrv4.dpaa.vo.AA0302RedirectByIpData;
import tpi.dgrv4.dpaa.vo.AA0302Req;
import tpi.dgrv4.dpaa.vo.AA0302Resp;
import tpi.dgrv4.dpaa.vo.AA0302Trunc;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.DgrWebhookApiMap;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpApiRegId;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.entity.jpql.TsmpApiDetail;
import tpi.dgrv4.entity.entity.jpql.TsmpApiModule;
import tpi.dgrv4.entity.entity.jpql.TsmpRegHost;
import tpi.dgrv4.entity.entity.jpql.TsmpRegModule;
import tpi.dgrv4.entity.entity.jpql.TsmpnApiDetail;
import tpi.dgrv4.entity.entity.jpql.TsmpnApiModule;
import tpi.dgrv4.entity.repository.DgrWebhookApiMapDao;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiDetailDao;
import tpi.dgrv4.entity.repository.TsmpApiModuleDao;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.entity.repository.TsmpDcDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpRegHostDao;
import tpi.dgrv4.entity.repository.TsmpRegModuleDao;
import tpi.dgrv4.entity.repository.TsmpnApiDetailDao;
import tpi.dgrv4.entity.repository.TsmpnApiModuleDao;
import tpi.dgrv4.entity.repository.TsmpnSiteDao;
import tpi.dgrv4.entity.repository.TsmpnSiteModuleDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0302Service {

	private Long moduleId;

	private TsmpApiDao tsmpApiDao;
	private TsmpnApiModuleDao tsmpnApiModuleDao;
	private TsmpApiModuleDao tsmpApiModuleDao;
	private TsmpnSiteModuleDao tsmpnSiteModuleDao;
	private TsmpnSiteDao tsmpnSiteDao;
	private TsmpnApiDetailDao tsmpnApiDetailDao;
	private TsmpApiDetailDao tsmpApiDetailDao;
	private TsmpDcDao tsmpDcDao;
	private TsmpApiRegDao tsmpApiRegDao;
	private TsmpRegModuleDao tsmpRegModuleDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpOrganizationCacheProxy tsmpOrganizationCacheProxy;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private ObjectMapper objectMapper;
	private TsmpRegHostDao tsmpRegHostDao;

	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookApiMapDao dgrWebhookApiMapDao;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookNotifyDao dgrWebhookNotifyDao;

	public AA0302Service(TsmpApiDao tsmpApiDao, TsmpnApiModuleDao tsmpnApiModuleDao, TsmpApiModuleDao tsmpApiModuleDao,
			TsmpnSiteModuleDao tsmpnSiteModuleDao, TsmpnSiteDao tsmpnSiteDao, TsmpnApiDetailDao tsmpnApiDetailDao,
			TsmpApiDetailDao tsmpApiDetailDao, TsmpDcDao tsmpDcDao, TsmpApiRegDao tsmpApiRegDao,
			TsmpRegModuleDao tsmpRegModuleDao, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpOrganizationCacheProxy tsmpOrganizationCacheProxy, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy,
			ObjectMapper objectMapper, TsmpRegHostDao tsmpRegHostDao,
			DgrWebhookApiMapDao dgrWebhookApiMapDao, DgrWebhookNotifyDao dgrWebhookNotifyDao) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpnApiModuleDao = tsmpnApiModuleDao;
		this.tsmpApiModuleDao = tsmpApiModuleDao;
		this.tsmpnSiteModuleDao = tsmpnSiteModuleDao;
		this.tsmpnSiteDao = tsmpnSiteDao;
		this.tsmpnApiDetailDao = tsmpnApiDetailDao;
		this.tsmpApiDetailDao = tsmpApiDetailDao;
		this.tsmpDcDao = tsmpDcDao;
		this.tsmpApiRegDao = tsmpApiRegDao;
		this.tsmpRegModuleDao = tsmpRegModuleDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpOrganizationCacheProxy = tsmpOrganizationCacheProxy;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.objectMapper = objectMapper;
		this.tsmpRegHostDao = tsmpRegHostDao;
		this.dgrWebhookApiMapDao = dgrWebhookApiMapDao;
		this.dgrWebhookNotifyDao = dgrWebhookNotifyDao;
	}

	public AA0302Resp queryAPIDetail(TsmpAuthorization authorization, AA0302Req req, ReqHeader reqHeader) {
		AA0302Resp resp = new AA0302Resp();
		// 1108:查無API明細資料, 1296:缺少必填參數, 1298:查無資料, 1436:找不到已啟動或最近上傳的模組
		try {

			TsmpApi api = checkData(authorization, req);

			// 依照 TSMP_API.api_src 查詢不同來源的 模組資料 與 API明細
			Object obj = findDetailByApiSrc(api);

			// parse data
			resp = setAA0302Data(obj, api, reqHeader.getLocale());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			// 1297:執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private TsmpApi checkData(TsmpAuthorization authorization, AA0302Req req) {
		TsmpApi api = null;
		String apiKey = req.getApiKey();
		String moduleName = req.getModuleName();
		String oId = authorization.getOrgId();

		if (!StringUtils.hasLength(apiKey) || !StringUtils.hasLength(moduleName)) {
			throw TsmpDpAaRtnCode._1296.throwing(); // 1296:缺少必填參數
		}

		if (!StringUtils.hasLength(oId)) {
			throw TsmpDpAaRtnCode._1273.throwing(); // 1273:組織單位ID:必填參數
		}

		// 以 AA0302Req.moduleName, AA0302Req.apiKey 查詢 TSMP_API (module_name, apI_key)，
		// 若無資料則 throw 1298。(查詢須符合組織原則：使用 AA0302Req.orgId)
		TsmpApiId id = new TsmpApiId(apiKey, moduleName);
		Optional<TsmpApi> optApi = getTsmpApiDao().findById(id);
		if (!optApi.isPresent()) {
			throw TsmpDpAaRtnCode._1298.throwing(); // 1298:查無資料
		}

		// 取得組織原則 (組織與子組織的orgId)
		List<String> userOrgIdList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(oId, Integer.MAX_VALUE);
		if (CollectionUtils.isEmpty(userOrgIdList)) {
			TPILogger.tl.debug("Violation organization principle, org not found.");
			throw TsmpDpAaRtnCode._1298.throwing(); // 1298:查無資料
		}

		api = optApi.get();
		String apiOId = api.getOrgId();
		if (StringUtils.hasLength(apiOId)) {
			if (!userOrgIdList.contains(apiOId)) {
				TPILogger.tl.debug(String.format("Violation organization principle. API orgId %s not in %s", apiOId,
						userOrgIdList.toString()));
				throw TsmpDpAaRtnCode._1298.throwing(); // 1298:查無資料
			}
		}

		return api;
	}

	private Object findDetailByApiSrc(TsmpApi api) {
		String apiSrc = nvl(api.getApiSrc());
		String moduleName = nvl(api.getModuleName());
		String oId = nvl(api.getOrgId());
		String apiKey = nvl(api.getApiKey());

		if (TsmpApiSrc.NET_MODULE.value().equals(apiSrc)) {
			return getNetDetail(moduleName, oId, apiKey);

		} else if (TsmpApiSrc.JAVA_MODULE.value().equals(apiSrc)) {
			return getJavaDetail(moduleName, oId, apiKey);

		} else if (TsmpApiSrc.REGISTERED.value().equals(apiSrc)) {
			/*
			 * 以 TSMP_API.module_name 查詢 TSMP_REG_MODULE (module_name) 取出第一筆最新的模組資料
			 * (TSMP_REG_MODULE.latest = "Y")
			 */
			Long moduleId = -1L;
			List<TsmpRegModule> regModuleList = getTsmpRegModuleDao().findByModuleNameAndLatest(moduleName, "Y");
			if (regModuleList != null && regModuleList.size() > 0) {
				TsmpRegModule regModule = regModuleList.get(0);
				Long regModuleId = regModule.getRegModuleId();
				if (regModuleId != null) {
					moduleId = regModuleId;
				}
			}
			setModuleId(moduleId);
			return getApiRegDetail(moduleName, apiKey);

		} else if (TsmpApiSrc.COMPOSED.value().equals(apiSrc)) {
			return getApiRegDetail(moduleName, apiKey);
		}

		TPILogger.tl.debugDelay2sec("API Not Found:");
		TPILogger.tl.debugDelay2sec("moduleName = " + moduleName);
		TPILogger.tl.debugDelay2sec("apiKey = " + apiKey);
		TPILogger.tl.debugDelay2sec("apiSrc = " + apiSrc);
		TPILogger.tl.debugDelay2sec("oId = " + oId);
		throw TsmpDpAaRtnCode._1298.throwing(); // 1298:查無資料
	}

	private TsmpnApiDetail getNetDetail(String moduleName, String oId, String apiKey) {
		// 找出有啟動的 .NET module：
		boolean isNetActive = false;
		TsmpnApiModule nApiModule = null;
		TsmpnApiDetail nApiDetail = null;
		List<TsmpnApiModule> tsmpnApiModuleList = getTsmpnApiModuleDao()
				.queryActiveModuleByModuleNameAndOrgId(moduleName, oId);
		if (tsmpnApiModuleList != null) {
			for (TsmpnApiModule tsmpnApiModule : tsmpnApiModuleList) {
				isNetActive = true;
				nApiModule = tsmpnApiModule;
			}
		}

		if (!isNetActive) {
			// 找出最近上傳的 .NET module：
			List<TsmpnApiModule> amList = getTsmpnApiModuleDao()
					.queryByModuleNameAndOrgIdOrderByUploadTimeDesc(moduleName, oId);
			if (amList != null && amList.size() > 0) {
				nApiModule = amList.get(0);
				isNetActive = true;
			}
		}

		if (!isNetActive) {
			throw TsmpDpAaRtnCode._1436.throwing(); // 1436:找不到已啟動或最近上傳的模組
		}
		if (nApiModule == null) {
			TPILogger.tl.debug("TsmpnApiModule not found ! ");
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		Optional<TsmpnApiDetail> optAi = getTsmpnApiDetailDao().findFirstByApiModuleIdAndApiKey(nApiModule.getId(),
				apiKey);
		if (!optAi.isPresent()) {
			throw TsmpDpAaRtnCode.NO_API_DETAIL.throwing(); // 1108:查無API明細資料
		}
		nApiDetail = optAi.get();

		return nApiDetail;
	}

	private TsmpApiDetail getJavaDetail(String moduleName, String oId, String apiKey) {
		// 找出有啟動的 Java module：
		boolean isJavaActive = false;
		TsmpApiModule apiModule = null;
		TsmpApiDetail apiDetail = null;

		// 先找v2是否有啟動
		List<TsmpApiModule> tsmpApiModuleList = getTsmpApiModuleDao().queryActiveV2ByModuleNameAndOrgId(moduleName,
				oId);
		if (tsmpApiModuleList != null && tsmpApiModuleList.size() > 0) {
			isJavaActive = true;
			apiModule = tsmpApiModuleList.get(0);
		}
		// v2沒有啟動 再找v3是否有啟動
		if (!isJavaActive) {
			tsmpApiModuleList = getTsmpApiModuleDao().queryActiveV3ByModuleNameAndOrgId(moduleName, oId);
			if (tsmpApiModuleList != null && tsmpApiModuleList.size() > 0) {
				isJavaActive = true;
				apiModule = tsmpApiModuleList.get(0);
			}
		}

		if (!isJavaActive) {
			// 找出最近上傳的 Java module：
			List<TsmpApiModule> amList = getTsmpApiModuleDao()
					.queryByModuleNameAndOrgIdOrderByUploadTimeDesc(moduleName, oId);
			if (amList != null && amList.size() > 0) {
				apiModule = amList.get(0);
				isJavaActive = true;
			}
		}

		if (!isJavaActive) {
			throw TsmpDpAaRtnCode._1436.throwing(); // 1436:找不到已啟動或最近上傳的模組
		}

		if (apiModule == null) {
			TPILogger.tl.debug("TsmpApiModule not found ! ");
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		Optional<TsmpApiDetail> optAi = getTsmpApiDetailDao().findFirstByApiModuleIdAndApiKey(apiModule.getId(),
				apiKey);
		if (!optAi.isPresent()) {
			throw TsmpDpAaRtnCode.NO_API_DETAIL.throwing(); // 1108:查無API明細資料
		}

		apiDetail = optAi.get();

		return apiDetail;
	}

	private TsmpApiReg getApiRegDetail(String moduleName, String apiKey) {
		TsmpApiReg apiReg = null;
		TsmpApiRegId id = new TsmpApiRegId(apiKey, moduleName);
		Optional<TsmpApiReg> apiRegOpt = getTsmpApiRegDao().findById(id);
		if (!apiRegOpt.isPresent()) {
			throw TsmpDpAaRtnCode.NO_API_DETAIL.throwing(); // 1108:查無API明細資料
		}

		apiReg = apiRegOpt.get();
		return apiReg;
	}

	private AA0302Resp setAA0302Data(Object obj, TsmpApi api, String locale) {
		AA0302Resp resp = new AA0302Resp();

		// tsmp_api→apiStatus moduleName apiKey apipName apiSrc
		// jweFlag<null→0> jweFlagResp <null→0>
		// dataFormat<null→1> apiDesc<null→""> orgId orgName<找關聯>
		//
		// apiUUID N/M→null

		// ----------------------------------------------------------
		// moduleId:N/M→/明細資料.api_module_id
		// R→getModuleId
		// C→ -1
		// ------------------------------- ---------------------------
		// 明細資料→methodOfJson(需處理) pathOfJson
		// headersOfJson paramsOfJson consumesOfJson producesOfJson
		// ----------------------------------------------------------
		// srcUrl:N/M→null
		// C/R→TsmpApiReg.src_url
		// ----------------------------------------------------------
		// urlRID:N/M→null
		// C/R→TsmpApiReg.url_rid <null→0>
		// ----------------------------------------------------------
		// noOAuth:N/M→null
		// C/R→TsmpApiReg.no_noOAuth <null→0>
		// ----------------------------------------------------------
		// funFlag:N/M→null
		// C/R→TsmpApiReg.fun_flag <null→0>
		// ----------------------------------------------------------
		// regHostId:N/M/C→null
		// R→TsmpApiReg.regHost_id <null→"">
		// ----------------------------------------------------------
		// regHostName:N//M/C→null
		// R→TsmpApiReg.regHost_name <找關聯>
		// ----------------------------------------------------------
		// apiUUID:N/M/C→null
		// R→TsmpApiReg.api_uid <null→"">
		// ----------------------------------------------------------

		// ----------------------------------------------------------

		// set tsmp_api相關欄位
		resp = setTsmpApiDetail(api, resp, locale);

		// set uuid moduleId 明細資料 AA0302Controls
		String apiSrc = nvl(api.getApiSrc());

		if (TsmpApiSrc.NET_MODULE.value().equals(apiSrc)) {
			TsmpnApiDetail detail = (TsmpnApiDetail) obj;
			resp.setApiUUID(null);
			resp.setModuleId(detail.getApiModuleId());
			resp = setNetDetailData(api, obj, resp, apiSrc);
			resp.setControls(setNetContrlos());

		} else if (TsmpApiSrc.JAVA_MODULE.value().equals(apiSrc)) {
			TsmpApiDetail detail = (TsmpApiDetail) obj;
			resp.setApiUUID(null);
			resp.setModuleId(detail.getApiModuleId());
			resp = setJavaDetailData(api, obj, resp, apiSrc);
			resp.setControls(setJavaContrlos());

		} else if (TsmpApiSrc.REGISTERED.value().equals(apiSrc)) {
			resp.setApiUUID(nvl(api.getApiUid()));
			resp.setModuleId(getModuleId());
			resp = setRegDetailData(obj, resp, apiSrc);
			AA0302Controls con = setRegisteredContrlos(api);
			resp.setControls(con);

		} else if (TsmpApiSrc.COMPOSED.value().equals(apiSrc)) {
			resp.setApiUUID(nvl(api.getApiUid()));
			resp.setModuleId((long) -1);
			resp = setRegDetailData(obj, resp, apiSrc);
			resp.setControls(setComposedContrlos());
		}

		this.setPath(api, resp);

		Long enableScheduledDate = api.getEnableScheduledDate();
		Long disableScheduledDate = api.getDisableScheduledDate();
		resp.setEnableScheduledDate(enableScheduledDate);
		resp.setDisableScheduledDate(disableScheduledDate);

		return resp;
	}

	private void setPath(TsmpApi api, AA0302Resp resp) {
		if (TsmpApiSrc.REGISTERED.value().equals(api.getApiSrc())) {
			if (api.getModuleName().startsWith("/")) {
				resp.setPathType("dgrc");
				resp.setDgrPath(api.getApiKey());
			} else {
				resp.setPathType("tsmpc");
				resp.setDgrPath("/" + api.getModuleName() + "/" + api.getApiKey());
			}
		} else {
			resp.setPathType("tsmpc");
			resp.setDgrPath("/" + api.getModuleName() + "/" + api.getApiKey());
		}
	}

	private AA0302Resp setTsmpApiDetail(TsmpApi api, AA0302Resp resp, String locale) {

		resp = setApiStatus(api, resp, locale);
		resp = setApiSrc(api, resp, locale);
		resp = setJweFlag(api, resp, locale);
		resp = setJweFlagResp(api, resp, locale);
		resp = setDataFormat(api, resp, locale);
		resp = setOrg(api, resp);
		resp = setApiCacheFlag(api, resp, locale);

		String moduleName = nvl(api.getModuleName());
		resp.setModuleName(setTruncHasValue(moduleName, 15));

		String apiKey = nvl(api.getApiKey());
		resp.setApiKey(setTruncHasValue(apiKey, 20));

		String apiName = nvl(api.getApiName());
		resp.setApiName(setTruncHasValue(apiName, 20));

		String apiDesc = nvl(api.getApiDesc());
		resp.setApiDesc(apiDesc);

		// 設定 Mock 的值
		String mockHeaders = api.getMockHeaders();

		resp.setMockStatusCode(api.getMockStatusCode());
		resp.setMockHeadersOfJson(api.getMockHeaders());
		resp.setMockHeaders(getMockHeadersFromJson(mockHeaders));
		resp.setMockBody(api.getMockBody());
		
		String createDateStr = "";
		Date createDate = api.getCreateTime();
		Optional<String> createDateOpt = DateTimeUtil.dateTimeToString(createDate, null);
		if(createDateOpt.isPresent()) {
			createDateStr = createDateOpt.get();
		}
		resp.setCreateDate(createDateStr);
		resp.setCreateUser(api.getCreateUser());
		
		String updateDateStr = "";
		Date updateDate = api.getUpdateTime();
		Optional<String> updateDateOpt = DateTimeUtil.dateTimeToString(updateDate, null);
		if(updateDateOpt.isPresent()) {
			updateDateStr = updateDateOpt.get();
		}
		resp.setUpdateDate(updateDateStr);
		resp.setUpdateUser(StringUtils.hasText(api.getUpdateUser()) ? api.getUpdateUser() : "");
		
		String label1 = api.getLabel1();
		String label2 = api.getLabel2();
		String label3 = api.getLabel3();
		String label4 = api.getLabel4();
		String label5 = api.getLabel5();
		Set<String> set = new HashSet<>();
		set.add(label1);
		set.add(label2);
		set.add(label3);
		set.add(label4);
		set.add(label5);
		List<String> labeList = new ArrayList<>(set);
		labeList.removeIf(value -> !StringUtils.hasLength(value));
		labeList.sort(null);
		resp.setLabelList(labeList);

		resp.setFixedCacheTime(api.getFixedCacheTime());

		return resp;

	}

	// 將 json 字串轉為物件
	private List<AA0302KeyVal> getMockHeadersFromJson(String json) {

		List<AA0302KeyVal> list = null;

		if (!StringUtils.hasLength(json)) {
			return list;
		}

		try {
			list = getObjectMapper().readValue(json, new TypeReference<List<AA0302KeyVal>>() {
			});
		} catch (JsonProcessingException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}

		return list;
	}

	private AA0302Resp setApiCacheFlag(TsmpApi api, AA0302Resp resp, String locale) {
		String subitemName = "";
		String apiCacheFlag = api.getApiCacheFlag();

		if (!StringUtils.hasText(apiCacheFlag)) {
			apiCacheFlag = "";
		} else {
			subitemName = apiCacheFlag;
			TsmpDpItems dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("API_CACHE_FLAG",
					apiCacheFlag, locale);
			if (dpItem != null) {
				subitemName = dpItem.getSubitemName();
			}
		}

		AA0302Pair pair = setPair(apiCacheFlag, subitemName);
		resp.setApiCacheFlag(pair);
		return resp;
	}

	private AA0302Resp setApiStatus(TsmpApi api, AA0302Resp resp, String locale) {
		String subitemName = "";
		String apiStatus = api.getApiStatus();

		if (!StringUtils.hasLength(apiStatus)) {
			apiStatus = "";
		} else {
			subitemName = apiStatus;
			TsmpDpItems dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndParam1AndLocale("ENABLE_FLAG", apiStatus,
					locale);
			if (dpItem != null) {
				subitemName = dpItem.getSubitemName();
			}
		}

		AA0302Pair pair = setPair(apiStatus, subitemName);
		resp.setApiStatus(pair);
		return resp;
	}

	private AA0302Resp setApiSrc(TsmpApi api, AA0302Resp resp, String locale) {
		String subitemName = "";
		String apiSrc = api.getApiSrc();

		if (!StringUtils.hasLength(apiSrc)) {
			apiSrc = "";
		} else {
			subitemName = apiSrc;
			TsmpDpItems dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("API_SRC", apiSrc,
					locale);
			if (dpItem != null) {
				subitemName = nvl(dpItem.getSubitemName());
			}
		}

		AA0302Pair pair = setPair(apiSrc, subitemName);
		resp.setApiSrc(pair);
		;

		return resp;
	}

	/**
	 * 由 itemNo 和 itemNo 取得TsmpDpItems 如果subitemNo = null → subitemNo = 0
	 * 
	 * @param itemNo
	 * @param subitemNo
	 * @return
	 */
	private TsmpDpItems getDpItemsByItemNoAndSubitemNo(String itemNo, String subitemNo, String locale) {
		if (subitemNo == null) {
			subitemNo = "0";
		}
		return getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale(itemNo, subitemNo, locale);
	}

	private AA0302Resp setJweFlag(TsmpApi api, AA0302Resp resp, String locale) {
		String jweFlag = api.getJewFlag();
		if (!StringUtils.hasLength(jweFlag)) {
			jweFlag = "0";
		}
		String subitemName = jweFlag;

		TsmpDpItems dpItem = getDpItemsByItemNoAndSubitemNo("API_JWT_FLAG", jweFlag, locale);
		if (dpItem != null) {
			subitemName = nvl(dpItem.getSubitemName());
		}

		AA0302Pair pair = setPair(jweFlag, subitemName);
		resp.setJweFlag(pair);
		return resp;
	}

	private AA0302Resp setJweFlagResp(TsmpApi api, AA0302Resp resp, String locale) {
		String jweFlagResp = api.getJewFlagResp();
		if (!StringUtils.hasLength(jweFlagResp)) {
			jweFlagResp = "0";
		}
		String subitemName = jweFlagResp;

		TsmpDpItems dpItem = getDpItemsByItemNoAndSubitemNo("API_JWT_FLAG", jweFlagResp, locale);
		if (dpItem != null) {
			subitemName = nvl(dpItem.getSubitemName());
		}

		AA0302Pair pair = setPair(jweFlagResp, subitemName);
		resp.setJweFlagResp(pair);
		return resp;
	}

	private AA0302Resp setDataFormat(TsmpApi api, AA0302Resp resp, String locale) {
		String subitemName = "";
		String dataFormat = api.getDataFormat();
		if (!StringUtils.hasLength(dataFormat)) {
			dataFormat = "1";
		}
		subitemName = dataFormat;

		TsmpDpItems dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("API_DATA_FORMAT", dataFormat,
				locale);
		if (dpItem != null) {
			subitemName = nvl(dpItem.getSubitemName());
		}

		AA0302Pair pair = setPair(dataFormat, subitemName);
		resp.setDataFormat(pair);
		return resp;
	}

	private AA0302Resp setOrg(TsmpApi api, AA0302Resp resp) {
		String orgId = api.getOrgId();
		String orgName = null;
		if (null == orgId) {
			resp.setOrgId(null);
			resp.setOrgName(null);
			return resp;
		}

		TsmpOrganization org = getOrganization(orgId);
		if (org != null) {
			orgName = org.getOrgName();
			if (!StringUtils.hasLength(orgName)) {
				orgName = org.getOrgId();
			}
		} else {
			orgName = orgId;
		}
		resp.setOrgId(orgId);
		resp.setOrgName(orgName);
		return resp;
	}

	private AA0302Resp setNetDetailData(TsmpApi api, Object obj, AA0302Resp aa0302N_resp, String apiSrc) {
		TsmpnApiDetail detail = (TsmpnApiDetail) obj;

		String aa0302N_moj = detail.getMethodOfJson(); // methodOfJson
		if (aa0302N_moj == null) {
			aa0302N_resp.setMethodOfJson(null);
		} else {
			aa0302N_resp.setMethodOfJson(getObjectMapper(aa0302N_moj));
		}

		String aa0302N_poj = detail.getPathOfJson();
		if (aa0302N_poj == null) {
			aa0302N_resp.setPathOfJson(null);
		} else {
			aa0302N_resp.setPathOfJson(setTruncHasValue(aa0302N_poj, 15));
		}

		aa0302N_resp.setProtocol(getProtocol(apiSrc, null));
		aa0302N_resp.setUrlRID(null);
		aa0302N_resp.setNoOAuth(null);
		aa0302N_resp.setFunFlag(null);

		String aa0302N_hos = detail.getHeadersOfJson();
		if (aa0302N_hos == null) {
			aa0302N_resp.setHeadersOfJson(null);
		} else {
			aa0302N_resp.setHeadersOfJson(setTruncHasValue(aa0302N_hos, 15));
		}

		String aa0302N_pos = detail.getParamsOfJson();
		if (aa0302N_pos == null) {
			aa0302N_resp.setParamsOfJson(null);
		} else {
			aa0302N_resp.setParamsOfJson(setTruncHasValue(aa0302N_hos, 15));
		}

		String aa0302N_cos = detail.getConsumesOfJson();
		if (aa0302N_cos == null) {
			aa0302N_resp.setConsumesOfJson(null);
		} else {
			aa0302N_resp.setConsumesOfJson(setTruncHasValue(aa0302N_cos, 15));
		}

		String aa0302N_producesOfJson = detail.getProducesOfJson();
		if (aa0302N_producesOfJson == null) {
			aa0302N_resp.setProducesOfJson(null);
		} else {
			aa0302N_resp.setProducesOfJson(setTruncHasValue(aa0302N_producesOfJson, 15));
		}
		return aa0302N_resp;
	}

	private AA0302Resp setJavaDetailData(TsmpApi api, Object obj, AA0302Resp aa0302J_resp, String apiSrc) {
		TsmpApiDetail detail = (TsmpApiDetail) obj;

		String aa0302J_moj = detail.getMethodOfJson(); // methodOfJson
		if (aa0302J_moj == null) {
			aa0302J_resp.setMethodOfJson(null);
		} else {
			aa0302J_resp.setMethodOfJson(getObjectMapper(aa0302J_moj)); // 將內容轉成以,分隔 "POST,GET,xxx"
		}

		String aa0302J_poj = detail.getPathOfJson();
		if (aa0302J_poj == null) {
			aa0302J_resp.setPathOfJson(null);
		} else {
			aa0302J_resp.setPathOfJson(setTruncHasValue(aa0302J_poj, 15));
		}

		aa0302J_resp.setProtocol(getProtocol(apiSrc, null));

		aa0302J_resp.setUrlRID(null);
		aa0302J_resp.setNoOAuth(null);
		aa0302J_resp.setFunFlag(null);

		String aa0302J_hos = detail.getHeadersOfJson();
		if (aa0302J_hos == null) {
			aa0302J_resp.setHeadersOfJson(null);
		} else {
			aa0302J_resp.setHeadersOfJson(setTruncHasValue(aa0302J_hos, 15));
		}

		String aa0302J_pos = detail.getParamsOfJson();
		if (aa0302J_pos == null) {
			aa0302J_resp.setParamsOfJson(null);
		} else {
			aa0302J_resp.setParamsOfJson(setTruncHasValue(aa0302J_pos, 15));
		}

		String aa0302J_cos = detail.getConsumesOfJson();
		if (aa0302J_cos == null) {
			aa0302J_resp.setConsumesOfJson(null);
		} else {
			aa0302J_resp.setConsumesOfJson(setTruncHasValue(aa0302J_cos, 15));
		}

		String aa0302J_producesOfJson = detail.getProducesOfJson();
		if (aa0302J_producesOfJson == null) {
			aa0302J_resp.setProducesOfJson(null);
		} else {
			aa0302J_resp.setProducesOfJson(setTruncHasValue(aa0302J_producesOfJson, 15));
		}
		return aa0302J_resp;
	}

	private AA0302Resp setRegDetailData(Object obj, AA0302Resp resp, String apiSrc) {
		TsmpApiReg detail = (TsmpApiReg) obj;

		String moj = detail.getMethodOfJson(); // methodOfJson
		if (moj == null) {
			resp.setMethodOfJson(null);
		} else {
			resp.setMethodOfJson(getObjectMapper(moj).toUpperCase()); // 將內容轉成以,分隔 "POST,GET,xxx"。前端收到小寫無法 mapping
																		// 到清單，由後端做 uppercase全部變為大寫，前端就可以mapping 到清單。
		}

		String poj = detail.getPathOfJson(); // pathOfJson
		if (poj == null) {
			resp.setPathOfJson(null);
		} else {
			resp.setPathOfJson(setTruncHasValue(poj, 15));
		}

		resp.setProtocol(getProtocol(apiSrc, detail)); // protocol

		String srcUrl = detail.getSrcUrl(); // srcUrl
		if (srcUrl == null) {
			resp.setSrcUrl(setTruncHasValue("", 30));
		} else {
			resp.setSrcUrl(setTruncHasValue(srcUrl, 30));
		}

		String urlRid = detail.getUrlRid(); // urlRID
		if (!StringUtils.hasLength(urlRid)) {
			urlRid = "0";
		}
		resp.setUrlRID(urlRid);

		String noOauth = detail.getNoOauth(); // noOAuth
		if (!StringUtils.hasLength(noOauth)) {
			noOauth = "0";
		}
		resp.setNoOAuth(noOauth);

		Integer funFlag = detail.getFunFlag(); // funFlag
		if (funFlag == null) {
			funFlag = 0;
		}
		resp.setFunFlag(funFlag);

		String regHostId = null;
		String regHostName = null;
		if (TsmpApiSrc.REGISTERED.value().equals(apiSrc)) {
			regHostId = nvl(detail.getReghostId());// regHostId
			regHostName = "";
			Optional<TsmpRegHost> opt = getTsmpRegHostDao().findById(regHostId);
			if (opt.isPresent()) {
				regHostName = nvl(opt.get().getReghost());
			}
		}

		String aa0302R_hos = detail.getHeadersOfJson(); // headersOfJson
		if (aa0302R_hos == null) {
			resp.setHeadersOfJson(null);
		} else {
			resp.setHeadersOfJson(setTruncHasValue(aa0302R_hos, 15));
		}

		String aa0302R_pos = detail.getParamsOfJson(); // paramsOfJson
		if (aa0302R_pos == null) {
			resp.setParamsOfJson(null);
		} else {
			resp.setParamsOfJson(setTruncHasValue(aa0302R_pos, 15));
		}

		String aa0302R_cos = detail.getConsumesOfJson(); // consumesOfJson
		if (aa0302R_cos == null) {
			resp.setConsumesOfJson(null);
		} else {
			resp.setConsumesOfJson(setTruncHasValue(aa0302R_cos, 15));
		}

		String aa0302R_producesOfJson = detail.getProducesOfJson(); // producesOfJson
		if (aa0302R_producesOfJson == null) {
			resp.setProducesOfJson(null);
		} else {
			resp.setProducesOfJson(setTruncHasValue(aa0302R_producesOfJson, 15));
		}

		String isRedirectByIp = detail.getRedirectByIp();
		if (StringUtils.hasLength(isRedirectByIp)) {
			resp.setIsRedirectByIp(isRedirectByIp.equals("Y") ? true : false);
		} else {
			resp.setIsRedirectByIp(false);
		}

		List<AA0302RedirectByIpData> redirectByIpDataList = new ArrayList<>();

		AA0302RedirectByIpData data1 = new AA0302RedirectByIpData();
		String ipForRedirect1 = detail.getIpForRedirect1();
		String ipSrcUrl1 = detail.getIpSrcUrl1(); // ipSrcUrl1

		if (StringUtils.hasLength(ipForRedirect1) && StringUtils.hasLength(ipSrcUrl1)) {

			data1.setIpForRedirect(ipForRedirect1);
			data1.setIpSrcUrl(setTruncHasValue(ipSrcUrl1, 30));
			redirectByIpDataList.add(data1);
		}

		AA0302RedirectByIpData data2 = new AA0302RedirectByIpData();
		String ipForRedirect2 = detail.getIpForRedirect2();
		String ipSrcUrl2 = detail.getIpSrcUrl2(); // ipSrcUrl2

		if (StringUtils.hasLength(ipForRedirect2) && StringUtils.hasLength(ipSrcUrl2)) {
			data2.setIpForRedirect(ipForRedirect2);
			data2.setIpSrcUrl(setTruncHasValue(ipSrcUrl2, 30));
			redirectByIpDataList.add(data2);
		}

		AA0302RedirectByIpData data3 = new AA0302RedirectByIpData();
		String ipForRedirect3 = detail.getIpForRedirect3();
		String ipSrcUrl3 = detail.getIpSrcUrl3();
		if (StringUtils.hasLength(ipForRedirect3) && StringUtils.hasLength(ipSrcUrl3)) {
			data3.setIpForRedirect(ipForRedirect3);
			data3.setIpSrcUrl(setTruncHasValue(ipSrcUrl3, 30));
			redirectByIpDataList.add(data3);
		}

		AA0302RedirectByIpData data4 = new AA0302RedirectByIpData();
		String ipForRedirect4 = detail.getIpForRedirect4();
		String ipSrcUrl4 = detail.getIpSrcUrl4();
		if (StringUtils.hasLength(ipForRedirect4) && StringUtils.hasLength(ipSrcUrl4)) {
			data4.setIpForRedirect(ipForRedirect4);
			data4.setIpSrcUrl(setTruncHasValue(ipSrcUrl4, 30));
			redirectByIpDataList.add(data4);
		}

		AA0302RedirectByIpData data5 = new AA0302RedirectByIpData();// ipSrcUrl5
		String ipForRedirect5 = detail.getIpForRedirect5();
		String ipSrcUrl5 = detail.getIpSrcUrl5();
		if (StringUtils.hasLength(ipForRedirect5) && StringUtils.hasLength(ipSrcUrl5)) {
			data5.setIpForRedirect(ipForRedirect5);
			data5.setIpSrcUrl(setTruncHasValue(ipSrcUrl5, 30));
			redirectByIpDataList.add(data5);
		}

		resp.setRedirectByIpDataList(redirectByIpDataList);
		resp.setHeaderMaskKey(nvl(detail.getHeaderMaskKey()));

		resp.setHeaderMaskPolicy(detail.getHeaderMaskPolicy() == null ? "0" : detail.getHeaderMaskPolicy());

		resp.setHeaderMaskPolicyNum(detail.getHeaderMaskPolicyNum());
		resp.setHeaderMaskPolicySymbol(nvl(detail.getHeaderMaskPolicySymbol()));

		resp.setBodyMaskKeyword(nvl(detail.getBodyMaskKeyword()));
		resp.setBodyMaskPolicy(detail.getBodyMaskPolicy() == null ? "0" : detail.getBodyMaskPolicy());
		resp.setBodyMaskPolicyNum(detail.getBodyMaskPolicyNum());
		resp.setBodyMaskPolicySymbol(nvl(detail.getBodyMaskPolicySymbol()));

		String moduleName = detail.getModuleName();
		String apiKey = detail.getApiKey();
		String type = getType(apiSrc, moduleName, apiKey);// 模式
		resp.setType(type);

		resp.setFailDiscoveryPolicy(nvl(detail.getFailDiscoveryPolicy(), "0"));// 失敗判定策略
		resp.setFailHandlePolicy(nvl(detail.getFailHandlePolicy(), "0"));// 失敗處置策略
		
		List<String> notifyNameList = new ArrayList<>();		
		List<DgrWebhookApiMap> mList = getDgrWebhookApiMapDao().findByApiKeyAndModuleName(apiKey, moduleName);
		Optional.ofNullable(mList).ifPresent(lst -> {
		    for (DgrWebhookApiMap m : lst) {		    	
		    	notifyNameList.add(m.getDgrWebhookNotify().getNotifyName());		    	
		    }
		});
		resp.setNotifyNameList(notifyNameList);
		
		// CORS header
		resp.setIsCorsAllowOrigin(nvl(detail.getIsCorsAllowOrigin(), "N")); // 若沒有值, 則設為"N"
		resp.setIsCorsAllowMethods(nvl(detail.getIsCorsAllowMethods(), "N"));// 若沒有值, 則設為"N"
		resp.setIsCorsAllowHeaders(nvl(detail.getIsCorsAllowHeaders(), "N"));// 若沒有值, 則設為"N"
		resp.setCorsAllowOrigin(nvl(detail.getCorsAllowOrigin()));
		resp.setCorsAllowMethods(nvl(detail.getCorsAllowMethods()));
		resp.setCorsAllowHeaders(nvl(detail.getCorsAllowHeaders()));

		return resp;
	}

	/**
	 * 取得模式, "0":tsmpc, "1":dgrc <br>
	 * 註冊時,若 TSMP_API/TSMP_API_REG 的 module_name 和 api_key，第1個字元為"/"，則為 dgrc <br>
	 * type 為 "1"，否則為 "0" <br>
	 */
	public static String getType(String apiSrc, String moduleName, String apiKey) {
		if (!TsmpApiSrc.REGISTERED.value().equals(apiSrc)) {
			return null;
		}

		if (!StringUtils.hasLength(moduleName) || !StringUtils.hasLength(apiKey)) {
			return null;
		}

		String type = null;
		String moduleNameSub = moduleName.substring(0, 1);
		String apiKeySub = apiKey.substring(0, 1);
		if ("/".equals(moduleNameSub) && "/".equals(apiKeySub)) {
			type = "1";// dgrc
		} else {
			type = "0";// tsmpc
		}

		return type;
	}

	private AA0302Controls setNetContrlos() {
		AA0302Controls con = new AA0302Controls();
		con.setApiName(true);
		return con;
	}

	private AA0302Controls setJavaContrlos() {
		AA0302Controls con = new AA0302Controls();
		con.setApiName(true);
		return con;
	}

	private AA0302Controls setRegisteredContrlos(TsmpApi api) {
		/*
		 * apiName: TRUE。 protocol: 若 moduleName 存在 TSMP_REG_MODULE 中，則 FALSE，否則為 TRUE。
		 * srcUrl: 若 moduleName 存在 TSMP_REG_MODULE 中，則 FALSE，否則為 TRUE。 methodOfJson: 若
		 * moduleName 存在 TSMP_REG_MODULE 中，則 FALSE，否則為 TRUE。 dataFormat: TRUE regHostId:
		 * TRUE
		 */
		AA0302Controls con = new AA0302Controls();
		String moduleName = nvl(api.getModuleName());
		List<TsmpRegModule> regModuleList = getTsmpRegModuleDao().findByModuleName(moduleName);
		if (regModuleList == null || regModuleList.size() == 0) {
			con.setProtocol(true);
			con.setSrcUrl(true);
			con.setMethodOfJson(true);
		}
		con.setApiName(true);
		con.setDataFormat(true);
		return con;
	}

	private AA0302Controls setComposedContrlos() {
		AA0302Controls con = new AA0302Controls();
		con.setApiName(true);
		con.setMethodOfJson(true);
		con.setDataFormat(true);
		return con;
	}

	private String getObjectMapper(String str) {
		String methodOfJson = null;
		try {
			List<String> strList;
			strList = getObjectMapper().readValue(str, new TypeReference<List<String>>() {
			});
			if (!CollectionUtils.isEmpty(strList)) {
				methodOfJson = String.join(",", strList);
			}
		} catch (IOException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing(); // 1297:執行錯誤
		}
		return methodOfJson;
	}

	// "http://", "https://", "http:abc", "https:" -> "http://" or "https://"
	private String getProtocol(String apiSrc, TsmpApiReg detail) {
		String protocol = "https://";
		if (TsmpApiSrc.REGISTERED.value().equals(apiSrc)) {
			String srcUrl = "";
			if (detail != null) {
				srcUrl = nvl(detail.getSrcUrl());
			}
			if (srcUrl.toLowerCase().startsWith(("https"))) {
				protocol = "https://";
			} else if (srcUrl.toLowerCase().startsWith(("http"))) {
				protocol = "http://";
			}
			return protocol;
		} else {
			return null;
		}

	}

	private boolean isNeedTrunc(String value, int size) {
		boolean isNeedTrunc = false;
		if (value.length() > size) {
			isNeedTrunc = true;
		}
		return isNeedTrunc;
	}

	private AA0302Trunc setTruncHasValue(String value, int size) {
		AA0302Trunc trunc = new AA0302Trunc();

		if (isNeedTrunc(value, size)) {
			trunc.setO(value);
			trunc.setT(isNeedTrunc(value, size));
			if (value.length() > size) {
				trunc.setT(isNeedTrunc(value, size));
				value = value.substring(0, size);
				trunc.setV(value);
			}
		} else {
			trunc.setT(isNeedTrunc(value, size));
			trunc.setV(value);
		}
		return trunc;
	}

	private AA0302Pair setPair(String valueV, String valueN) {
		AA0302Pair pair = new AA0302Pair();
		pair.setV(valueV);
		pair.setN(valueN);
		return pair;
	}

	/**
	 * 由 orgId ,使用快取取得getTsmpOrganization
	 * 
	 * @param orgId
	 * @return
	 */
	private TsmpOrganization getOrganization(String orgId) {
		TsmpOrganization i = getTsmpOrganizationCacheProxy().findById(orgId);
		return i;
	}

	private Long getModuleId() {
		return moduleId;
	}

	private void setModuleId(Long moduleId) {
		this.moduleId = moduleId;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpApiModuleDao getTsmpApiModuleDao() {
		return this.tsmpApiModuleDao;
	}

	protected TsmpnApiModuleDao getTsmpnApiModuleDao() {
		return this.tsmpnApiModuleDao;
	}

	protected TsmpnSiteModuleDao getTsmpnSiteModuleDao() {
		return this.tsmpnSiteModuleDao;
	}

	protected TsmpnSiteDao getTsmpnSiteDao() {
		return this.tsmpnSiteDao;
	}

	protected TsmpnApiDetailDao getTsmpnApiDetailDao() {
		return this.tsmpnApiDetailDao;
	}

	protected TsmpApiDetailDao getTsmpApiDetailDao() {
		return this.tsmpApiDetailDao;
	}

	protected TsmpDcDao getTsmpDcDao() {
		return this.tsmpDcDao;
	}

	protected TsmpApiRegDao getTsmpApiRegDao() {
		return this.tsmpApiRegDao;
	}

	protected TsmpRegModuleDao getTsmpRegModuleDao() {
		return this.tsmpRegModuleDao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected TsmpOrganizationCacheProxy getTsmpOrganizationCacheProxy() {
		return this.tsmpOrganizationCacheProxy;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected TsmpRegHostDao getTsmpRegHostDao() {
		return this.tsmpRegHostDao;
	}
}
