package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.BcryptFieldValueEnum;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.cache.proxy.TsmpOrganizationCacheProxy;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0301Item;
import tpi.dgrv4.dpaa.vo.AA0301Pair;
import tpi.dgrv4.dpaa.vo.AA0301Req;
import tpi.dgrv4.dpaa.vo.AA0301Resp;
import tpi.dgrv4.dpaa.vo.AA0301Trunc;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpApiRegId;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.vo.AA0301SearchCriteria;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0301Service {

	private TPILogger logger = TPILogger.tl;

	private Integer pageSize;

	private TsmpApiDao tsmpApiDao;
	private TsmpApiRegDao tsmpApiRegDao;
	private ServiceConfig serviceConfig;
	private BcryptParamHelper bcryptParamHelper;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpOrganizationCacheProxy tsmpOrganizationCacheProxy;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;

	private String[] keyList = { "apiKey", "apiSrc", "moduleName", "apiName" };
	private String[] valueList = { "asc", "desc" };

	@Autowired
	public AA0301Service(TsmpApiDao tsmpApiDao, ServiceConfig serviceConfig, BcryptParamHelper bcryptParamHelper,
			TsmpOrganizationDao tsmpOrganizationDao, TsmpOrganizationCacheProxy tsmpOrganizationCacheProxy,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy,TsmpApiRegDao tsmpApiRegDao) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.serviceConfig = serviceConfig;
		this.bcryptParamHelper = bcryptParamHelper;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpOrganizationCacheProxy = tsmpOrganizationCacheProxy;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.tsmpApiRegDao = tsmpApiRegDao;
	}

	public AA0301Resp queryAPIList(TsmpAuthorization authorization, AA0301Req req, ReqHeader reqHeader) {
		AA0301Resp resp = new AA0301Resp();
		// 1273:組織單位ID:必填參數, 1290:參數錯誤, 1298:查無資料, 1354:[{{0}}] 不存在: {{1}}
		try {
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());
			AA0301SearchCriteria cri = setCriAndCheckData(authorization, req, locale);

			List<TsmpApi> tspmApi = getTsmpApiDao().query_AA0301Service(cri);

			if (tspmApi.size() == 0) {
				// 1298:查無資料
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			List<AA0301Item> itemList = setAA0301Data(tspmApi, locale);

			if (req.getSortBy() != null && req.getSortBy().size() > 0) {
				resp.setSortBy(req.getSortBy());
			} else {
				Map<String, String> sortBy = new HashMap<>();
				sortBy.put("apiKey", "asc");
				resp.setSortBy(sortBy);
			}
			resp.setDataList(itemList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			// 1297:執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	/**
	 * 1. 若 AA0301Req.apiSrc 有傳值，則檢核每一個元素是否存在 TSMP_DP_ITEMS.ITEM_NO = 'API_SRC' 中，否則
	 * throw 1354 ( [{{apiSrc}}] 不存在: {{1}} )。 2. 若 AA0301Req.sortBy 有傳值，則檢核 Key 是否為
	 * ["apiKey","apiSrc","moduleName","apiName"]、Val 是否為 ["asc", "desc"]，否則 throw
	 * 1290。 3. 若 AA0301Req.paging 有傳值，則檢核是否為 "Y" 或 "N"，否則 throw 1290；若未傳值，則設值為 "N"。
	 * 
	 * @param authorization
	 * @param req
	 * @return
	 */
	private AA0301SearchCriteria setCriAndCheckData(TsmpAuthorization authorization, AA0301Req req, String locale) {
		String orgId = authorization.getOrgId();
		if (StringUtils.isEmpty(orgId)) {
			throw TsmpDpAaRtnCode._1273.throwing(); // 1273:組織單位ID:必填參數
		}

		List<String> apiSrcList = req.getApiSrc();
		Map<String, String> sortMap = req.getSortBy();
		String paging = nvl(req.getPaging());
		String reqKey = "";
		String reqValue = "";

		checkApiSrc(apiSrcList, locale); // 檢查傳入的src是否存在TSMP_DP_ITEMS.ITEM_NO = 'API_SRC' 中

		if (sortMap != null && sortMap.size() > 0) {
			for (String string : sortMap.keySet()) {
				reqKey = nvl(string);
				reqValue = nvl(sortMap.get(reqKey));
			}
			if (!Arrays.asList(keyList).contains(reqKey) || !Arrays.asList(valueList).contains(reqValue)) {
				throw TsmpDpAaRtnCode._1290.throwing(); // 1290:參數錯誤
			}
		}

		if (!StringUtils.isEmpty(paging)) {
			if (!"Y".equals(paging) && !"N".equals(paging)) {
				throw TsmpDpAaRtnCode._1290.throwing(); // 1290:參數錯誤
			}
		} else {
			paging = "N"; // 若 AA0301Req.paging 若未傳值，則設值為 "N"。
		}

		AA0301SearchCriteria cri = new AA0301SearchCriteria();
		cri.setApiSrc(apiSrcList);

		String state = nvl(req.getApiStatus());
		if (!"".equals(state)) {
			cri.setApiStatus(getDecodeEnableFlag(state, locale));
		}
//		String prblicFlag = nvl(req.getPublicFlag());
//		if(!"".equals(prblicFlag)) {
//			prblicFlag = getDecodeDpItem(prblicFlag, "API_AUTHORITY", locale);
//		}

		String jweFlag = nvl(req.getJweFlag());
		if (!"".equals(jweFlag)) {
			jweFlag = getDecodeDpItem(jweFlag, "API_JWT_FLAG", locale);
		}

		String jweFlagResp = nvl(req.getJweFlagResp());
		if (!"".equals(jweFlagResp)) {
			jweFlagResp = getDecodeDpItem(jweFlagResp, "API_JWT_FLAG", locale);
		}

//		cri.setPublicFlag(prblicFlag); // bcrypt加密，ITEM_NO = 'API_AUTHORITY'
		cri.setJweFlag(jweFlag); // bcrypt加密，ITEM_NO = 'API_JWT_FLAG'
		cri.setJweFlagResp(jweFlagResp); // bcrypt加密，ITEM_NO = 'API_JWT_FLAG'
		cri.setPaging(paging);

		String[] words = ServiceUtil.getKeywords(req.getKeyword(), " ");
		cri.setKeywords(words);

		String apiKey = nvl(req.getApiKey());
		String moduleName = nvl(req.getModuleName());
		TsmpApi lastTsmpApi = null;

		TsmpApiId id = new TsmpApiId(apiKey, moduleName);
		Optional<TsmpApi> optApi = getTsmpApiDao().findById(id);
		if (optApi.isPresent()) {
			lastTsmpApi = optApi.get();
		}
		cri.setLastTsmpApi(lastTsmpApi);

		// 取得組織原則
		List<String> orgList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(authorization.getOrgId(),
				Integer.MAX_VALUE); // 組織與子組織的orgId
		cri.setOrgList(orgList);

		cri.setPageSize(getPageSize());
		cri.setSortColumn(nvl(reqKey));
		cri.setSort(nvl(reqValue));
		return cri;
	}

	private void checkApiSrc(List<String> apiSrcList, String locale) {
		if (apiSrcList != null) {
			List<TsmpDpItems> items = getTsmpDpItemsCacheProxy().findByItemNoAndLocale("API_SRC", locale);
			if (items == null || items.size() == 0) {
				logger.debug("tsmp_dp_item itemNo not found API_SRC ! ");
				throw TsmpDpAaRtnCode._1297.throwing();
			}

			List<String> subItemNoList = items.stream().map(TsmpDpItems::getSubitemNo).collect(Collectors.toList());

			apiSrcList.forEach((src) -> {
				if (!subItemNoList.contains(src)) {
					throw TsmpDpAaRtnCode._1354.throwing("{{apiSrc}}", src); // 1354 ( [{{apiSrc}}] 不存在: {{1}} )
				}
			});
		}
	}

	/**
	 * 使用BcryptParam, ITEM_NO='ENABLE_FLAG' , DB儲存值對應代碼如下: DB值 (PARAM1);
	 * 
	 * @param encodeStatus
	 * @return
	 */
	protected String getDecodeEnableFlag(String encodeStatus, String locale) {
		String status = null;
		try {
			status = getBcryptParamHelper().decode(encodeStatus, "ENABLE_FLAG", BcryptFieldValueEnum.PARAM1, locale);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return status;
	}

	private String getDecodeDpItem(String status, String itemNo, String locale) {
		String decodeHostStatus = null;
		try {
			decodeHostStatus = getBcryptParamHelper().decode(status, itemNo, locale);
		} catch (BcryptParamDecodeException e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return decodeHostStatus;
	}

	/**
	 * 2024-11-18 標籤搜尋,AA0428 共用
	 * @param tspmApi
	 * @param locale
	 * @return
	 */
	public List<AA0301Item> setAA0301Data(List<TsmpApi> tspmApi, String locale) {
		List<AA0301Item> dataList = new ArrayList<>();
		tspmApi.forEach((api) -> {
			AA0301Item item = new AA0301Item();
			String moduleName = nvl(api.getModuleName());
			item.setModuleName(setTrunc(moduleName, 15));

			String apiKey = nvl(api.getApiKey());
			item.setApiKey(setTrunc(apiKey, 20));

			String apiName = nvl(api.getApiName());
			item.setApiName(setTrunc(apiName, 20));

			String apiDesc = api.getApiDesc();
			if (apiDesc != null) {
				item.setApiDesc(setTrunc(apiDesc, 30));
			} else {
				AA0301Trunc trunc = new AA0301Trunc();
				trunc.setOri(apiDesc);
				trunc.setT(false);
				trunc.setVal(apiDesc);
			}

			String apiStatus = nvl(api.getApiStatus());
			TsmpDpItems dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndParam1AndLocale("ENABLE_FLAG", apiStatus,
					locale);
			if (dpItem != null) {
				String subitemName = dpItem.getSubitemName();
				AA0301Pair pair = setPair(apiStatus, subitemName);
				item.setApiStatus(pair);
			} else {
				AA0301Pair pair = setPair(apiStatus, apiStatus);
				item.setApiStatus(pair);
			}

			String apiSrc = nvl(api.getApiSrc());
			dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("API_SRC", apiSrc, locale);
			if (dpItem != null) {
				String subitemName = dpItem.getSubitemName();
				AA0301Pair pair = setPair(apiSrc, subitemName);
				item.setApiSrc(pair);
			} else {
				AA0301Pair pair = setPair(apiSrc, apiSrc);
				item.setApiSrc(pair);
			}

			String jweFlag = nvl(api.getJewFlag());
			if (StringUtils.isEmpty(jweFlag)) {
				jweFlag = "0";
			}
			dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("API_JWT_FLAG", jweFlag, locale);
			if (dpItem != null) {
				String subitemName = dpItem.getSubitemName();
				AA0301Pair pair = setPair(jweFlag, subitemName);
				item.setJweFlag(pair);
			} else {
				AA0301Pair pair = setPair(jweFlag, jweFlag);
				item.setJweFlag(pair);
			}

			String jweFlagResp = nvl(api.getJewFlagResp());
			if (StringUtils.isEmpty(jweFlagResp)) {
				jweFlagResp = "0";
			}
			dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("API_JWT_FLAG", jweFlagResp, locale);
			if (dpItem != null) {
				String subitemName = dpItem.getSubitemName();
				AA0301Pair pair = setPair(jweFlagResp, subitemName);
				item.setJweFlagResp(pair);
			} else {
				AA0301Pair pair = setPair(jweFlagResp, jweFlagResp);
				item.setJweFlagResp(pair);
			}

			// "TSMP_API.create_time / update_time，yyyy-MM-dd HH:mm:ss 優先使用
			// update_time，若無值才取 create_time"
			Date updateTiime = api.getUpdateTime();
			if (updateTiime == null) {
				updateTiime = api.getCreateTime();
			}
			Optional<String> opt = DateTimeUtil.dateTimeToString(updateTiime, DateTimeFormatEnum.西元年月日時分秒);
			if (opt.isPresent()) {
				item.setUpdateTime(opt.get());
			} else {
				item.setUpdateTime("");
			}

			// TSMP_API.org_id，對應文字 TSMP_ORGANIZATION.org_name
			String oId = api.getOrgId();
			if (null == oId) {
				item.setOrg(null);
			} else {
				TsmpOrganization org = getOrganization(oId);
				if (org != null) {
					String orgName = nvl(org.getOrgName());
					if (StringUtils.isEmpty(orgName)) {
						orgName = api.getOrgId();
					}
					AA0301Pair pair = setPair(api.getOrgId(), orgName);
					item.setOrg(pair);
				} else {
					AA0301Pair pair = setPair(oId, oId);
					item.setOrg(pair);
				}
			}
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
			set.removeIf(value -> !StringUtils.hasLength(value));
			List<String> labeList = new ArrayList<>();

			set.forEach(s -> labeList.add(s.toLowerCase()));

			labeList.sort(null);
			item.setLabelList(labeList);

			Long enableScheduledDate = api.getEnableScheduledDate();
			Long disableScheduledDate = api.getDisableScheduledDate();

			item.setEnableScheduledDate(enableScheduledDate);
			item.setDisableScheduledDate(disableScheduledDate);
			
			String createDateStr = "";
			Date createDate = api.getCreateTime();
			Optional<String> createDateOpt = DateTimeUtil.dateTimeToString(createDate, null);
			if(createDateOpt.isPresent()) {
				createDateStr = createDateOpt.get();
			}
			item.setCreateDate(createDateStr);
			item.setCreateUser(api.getCreateUser());
			
			String updateDateStr = "";
			Date updateDate = api.getUpdateTime();
			Optional<String> updateDateOpt = DateTimeUtil.dateTimeToString(updateDate, null);
			if(updateDateOpt.isPresent()) {
				updateDateStr = updateDateOpt.get();
			}
			item.setUpdateDate(updateDateStr);
			item.setUpdateUser(StringUtils.hasText(api.getUpdateUser()) ? api.getUpdateUser() : "");
			
			//apiCacheFlag
			String apiCacheFlag = api.getApiCacheFlag();
			if (!StringUtils.hasText(apiCacheFlag)) {
				apiCacheFlag = "";
				String subitemName = "";
				AA0301Pair pair = setPair(apiCacheFlag, subitemName);
				item.setApiCacheFlag(pair);
			} else {
				String subitemName = apiCacheFlag;
				dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale("API_CACHE_FLAG",
						apiCacheFlag, locale);
				if (dpItem != null) {
					subitemName = dpItem.getSubitemName();
				}
				AA0301Pair pair = setPair(apiCacheFlag, subitemName);
				item.setApiCacheFlag(pair);
			}

			//method, no auth
			TsmpApiRegId regId = new TsmpApiRegId(api.getApiKey(), api.getModuleName());
			TsmpApiReg tsmpApiRegVo = getTsmpApiRegDao().findById(regId).orElse(null);
			if(tsmpApiRegVo != null) {
				item.setMethodOfJson(tsmpApiRegVo.getMethodOfJson().replace("[", "").replace("]", "").replace("\"", ""));
				item.setNoAuth(tsmpApiRegVo.getNoOauth());
			}else {
				item.setMethodOfJson("");
				item.setNoAuth("0");
			}
			
			dataList.add(item);
		});
		return dataList;
	}

	private boolean isNeedTrunc(String value, int size) {
		boolean isNeedTrunc = false;
		if (value.length() > size) {
			isNeedTrunc = true;
		}
		return isNeedTrunc;
	}

	private AA0301Trunc setTrunc(String value, int size) {
		AA0301Trunc trunc = new AA0301Trunc();
		boolean isNeedTrunc = isNeedTrunc(value, size);
		if (isNeedTrunc) {
			trunc.setOri(value);
			trunc.setT(isNeedTrunc);
			if (value.length() > size) {
				value = value.substring(0, size);
				trunc.setVal(value);
			}
		} else {
			trunc.setT(isNeedTrunc);
			trunc.setVal(value);
		}
		return trunc;
	}

	private AA0301Pair setPair(String valueV, String valueN) {
		AA0301Pair pair = new AA0301Pair();
		pair.setV(valueV);
		pair.setN(valueN);
		return pair;
	}

	/**
	 * 由 orgId ,使用快取取得getTsmpOrganization
	 * 
	 * @param authoritiesId
	 * @return
	 */
	private TsmpOrganization getOrganization(String orgId) {
		TsmpOrganization i = getTsmpOrganizationCacheProxy().findById(orgId);
		return i;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("aa0301");
		return pageSize;
	}

	protected ServiceConfig getServiceConfig() {
		return serviceConfig;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
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

	protected TsmpApiRegDao getTsmpApiRegDao() {
		return tsmpApiRegDao;
	}
	
	
}
