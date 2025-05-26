package tpi.dgrv4.dpaa.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFbFlag;
import tpi.dgrv4.common.constant.TsmpDpModule;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0092ApiItem;
import tpi.dgrv4.dpaa.vo.DPB0092Req;
import tpi.dgrv4.dpaa.vo.DPB0092Resp;
import tpi.dgrv4.dpaa.vo.TsmpApiItem;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.TsmpOpenApiKey;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiExtDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpApiThemeDao;
import tpi.dgrv4.entity.repository.TsmpDpThemeCategoryDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0092Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpApiDao tsmpApiDao;
	private TsmpClientDao tsmpClientDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpOpenApiKeyDao tsmpOpenApiKeyDao;
	private TsmpDpApiThemeDao tsmpDpApiThemeDao;
	private TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao;
	private TsmpApiExtDao tsmpApiExtDao;
	private ApiItemService apiItemService;
	private BcryptParamHelper helper;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private ServiceConfig serviceConfig;
 
	@Autowired
	public DPB0092Service(TsmpApiDao tsmpApiDao, TsmpClientDao tsmpClientDao, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpOpenApiKeyDao tsmpOpenApiKeyDao, TsmpDpApiThemeDao tsmpDpApiThemeDao,
			TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao, TsmpApiExtDao tsmpApiExtDao, ApiItemService apiItemService,
			BcryptParamHelper helper, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, ServiceConfig serviceConfig) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpOpenApiKeyDao = tsmpOpenApiKeyDao;
		this.tsmpDpApiThemeDao = tsmpDpApiThemeDao;
		this.tsmpDpThemeCategoryDao = tsmpDpThemeCategoryDao;
		this.tsmpApiExtDao = tsmpApiExtDao;
		this.apiItemService = apiItemService;
		this.helper = helper;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.serviceConfig = serviceConfig;
	}
	
	public DPB0092Resp writeOpenApiKeyReq(TsmpAuthorization tsmpAuthorization, DPB0092Req req, ReqHeader reqHeader) {
		DPB0092Resp resp = new DPB0092Resp();
		try {
			//chk param
			String fbTypeEncode = req.getFbTypeEncode();//前後台分類	ex:FRONT , 使用BcryptParam設計, itemNo="FB_FLAG"
			String fbType = null;
			String clientId = null;
			String userName = null;
			String orgId = null;
			String orgName = null;
			String effectiveDate = null;
			String local = ServiceUtil.getLocale(reqHeader.getLocale());
			if(fbTypeEncode != null) {// 為前台
				fbType = getDecode(fbTypeEncode, "FB_FLAG", reqHeader.getLocale());//解碼
				clientId = tsmpAuthorization.getClientId();//前台: from Token
				orgName = TsmpDpModule.DP.getChiDesc();
			}else {// 為後台
				fbType = TsmpDpFbFlag.BACK.value();
				clientId = req.getClientId();//後台: 挑選的 Client Id
				userName = tsmpAuthorization.getUserName();
				orgId = tsmpAuthorization.getOrgId();
				if (StringUtils.isEmpty(orgId)) {
					throw TsmpDpAaRtnCode._1296.throwing();
				}
				Optional<TsmpOrganization> opt_org = getTsmpOrganizationDao().findById(orgId);
				if(!opt_org.isPresent()) {
					throw TsmpDpAaRtnCode._1296.throwing();
				}
				TsmpOrganization org = opt_org.get();
				orgName = org.getOrgName();
				effectiveDate = getNowStr();
			}			
			
			if (StringUtils.isEmpty(clientId)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			Optional<TsmpClient> opt_client = getTsmpClientDao().findById(clientId);
			if (!opt_client.isPresent()) {
				this.logger.error("用戶不存在: " + clientId);
				throw TsmpDpAaRtnCode._1297.throwing();
			}
			TsmpClient client = opt_client.get();
			String clientName = client.getClientName();
			String clientAlias = client.getClientAlias();
 
			String encodeReqSubtype = req.getEncodeReqSubtype();
			if (StringUtils.isEmpty(encodeReqSubtype)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}

			String reqSubtype = getDecodeReqSubtype(encodeReqSubtype, local);//BcryptParam解碼
			
			TsmpOpenApiKey oak = null;
			Long openApiKeyId = null;
			
			// 取得申請項目名稱
			TsmpDpItems tsmpDpItems = getTsmpDpItemsCacheProxy().findById(new TsmpDpItemsId("OPEN_API_KEY", reqSubtype, local));
			String reqTypeName = Optional.ofNullable(tsmpDpItems) //
					.map((i) -> i.getSubitemName()) //
					.orElseGet(() -> new String());
			
			String openApiKey = "";
			String secretKey = "";
			String secretKeyMask = "";
			String openApiKeyAlias = "";
			String expiredAtStr = "";
			String timesThreshold = "";
			List<DPB0092ApiItem> dataList = new ArrayList<DPB0092ApiItem>();
			
			// Open API Key 申請
			if (TsmpDpReqReviewType.OPEN_API_KEY.OPEN_API_KEY_APPLICA.isValueEquals(reqSubtype)){
				if(fbType.contentEquals(TsmpDpFbFlag.FRONT.value())) {//前台
					expiredAtStr = "";
					List<TsmpDpItems> itemsList = getTsmpDpItemsCacheProxy().queryLike(null, null, "OAK_PARA", 
							"N", Integer.MAX_VALUE, local);
					if(itemsList != null && !itemsList.isEmpty()) {
						TsmpDpItems items = itemsList.get(0);
						timesThreshold = items.getParam1();
						String param2 = items.getParam2();
						expiredAtStr = getExpiredAtStr(param2);
					}
				}
						
			// Open API Key 異動 / Open API Key 撤銷, 要傳入參數
			}else if (TsmpDpReqReviewType.OPEN_API_KEY.OPEN_API_KEY_UPDATE.isValueEquals(reqSubtype)
					|| TsmpDpReqReviewType.OPEN_API_KEY.OPEN_API_KEY_REVOKE.isValueEquals(reqSubtype)) {				
				openApiKeyId = req.getOpenApiKeyId();
				if (StringUtils.isEmpty(openApiKeyId)) {
					throw TsmpDpAaRtnCode._1296.throwing();
				}
				
				Optional<TsmpOpenApiKey> opt_oak = getTsmpOpenApiKeyDao().findById(openApiKeyId);
				if(!opt_oak.isPresent()) {
					throw TsmpDpAaRtnCode._1298.throwing();
				}
				oak = opt_oak.get();

				if(oak != null) {
					openApiKey = oak.getOpenApiKey();
					secretKey = oak.getSecretKey();
					secretKeyMask = ServiceUtil.dataMask(secretKey, 5, 5);//只顯示前後五個字,中間其餘隱藏加星號(*)
					openApiKeyAlias = oak.getOpenApiKeyAlias();
					timesThreshold = oak.getTimesThreshold() + "";
					expiredAtStr = getDateTime(oak.getExpiredAt());
				}
				
				TsmpApi lastRecord = null;
				List<TsmpApi> apiList = getTsmpApiDao().queryByOpenApiKeyId(lastRecord, Integer.MAX_VALUE, openApiKeyId);		
				List<TsmpApiItem> apiItemList = getApiItemService().getApiItemList(apiList, local);
				dataList = getApiItemList(apiItemList, resp);
			}

			resp.setClientId(clientId);//用戶代碼
			resp.setClientName(clientName);//用戶端代號
			resp.setClientAlias(clientAlias);//用戶端名稱
			resp.setReqDate(getNowStr());
			resp.setUserName(userName);
			resp.setOrgId(orgId);
			resp.setOrgName(orgName);
			resp.setEffectiveDate(effectiveDate);
			resp.setReqSubtype(reqSubtype);
			resp.setReqSubtypeName(reqTypeName);
			resp.setOpenApiKeyId(openApiKeyId);
			resp.setOpenApiKey(openApiKey);
			resp.setMimaKey(secretKeyMask);
			resp.setOpenApiKeyAlias(openApiKeyAlias);
			resp.setExpiredAt(expiredAtStr);
			resp.setTimesThreshold(timesThreshold);
			resp.setDataList(dataList);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	/**
	 * 前台: 效期 = 現在時間 + 預設效期天數
	 * @return
	 */
	private String getExpiredAtStr(String param1) {
		if(StringUtils.isEmpty(param1)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		LocalDateTime ld = LocalDateTime.now();
		ld = ld.plusDays(Integer.valueOf(param1));
		Optional<String> opt_dateStr = DateTimeUtil.dateTimeToString(ld, DateTimeFormatEnum.西元年月日_2);
		if (!opt_dateStr.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}
		return opt_dateStr.get();
	}
	
	private String getNowStr() {
		Optional<String> opt_date = DateTimeUtil.dateTimeToString(DateTimeUtil.now(), DateTimeFormatEnum.西元年月日_2);// yyyy/MM/dd
		if (!opt_date.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}	
		String nowStr = opt_date.get();
		return nowStr;
	}
	
	protected String getDecode(String encode, String itemNo, String locale) {
		String decode = null;
		try {
			decode = getBcryptParamHelper().decode(encode, itemNo, locale);//BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return decode;
	}
	
	private List<DPB0092ApiItem> getApiItemList(List<TsmpApiItem> apiItemList, DPB0092Resp resp) {
		List<DPB0092ApiItem> dataList = resp.getDataList();
		if(dataList == null) {
			dataList = new ArrayList<DPB0092ApiItem>();
		}
		for (TsmpApiItem apiItem : apiItemList) {			
			DPB0092ApiItem dpb0092_item = new DPB0092ApiItem();
			dpb0092_item.setApiKey(apiItem.getApiKey());
			dpb0092_item.setModuleName(apiItem.getModuleName());
			dpb0092_item.setApiName(apiItem.getApiName());
			dpb0092_item.setThemeDatas(apiItem.getThemeDatas());
			dpb0092_item.setOrgId(apiItem.getOrgId());
			dpb0092_item.setOrgName(apiItem.getOrgName());
			dpb0092_item.setApiDesc(apiItem.getApiDesc());
			dpb0092_item.setApiExtId(apiItem.getApiExtId());
			dpb0092_item.setApiUid(apiItem.getApiUid());
			dpb0092_item.setFileName(apiItem.getFileName());
			dpb0092_item.setFilePath(apiItem.getFilePath());
			
			dataList.add(dpb0092_item);
		}
		return dataList;
	}

	private String getDateTime(Long timeInMillis) {
		if(timeInMillis == null) {
			return "";
		}
		Date dt = new Date(timeInMillis);
		String dtStr = DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日_2).orElse(null);// yyyy/MM/dd
		return dtStr;
	}
	
	protected String getDecodeReqSubtype(String reqSubtype, String locale) {
		try {
			reqSubtype = getBcryptParamHelper().decode(reqSubtype, "OPEN_API_KEY", locale);//BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return reqSubtype;
	}
	
	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
	
	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}
	 
	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
	
	protected TsmpOpenApiKeyDao getTsmpOpenApiKeyDao() {
		return this.tsmpOpenApiKeyDao;
	}
	
	protected TsmpDpApiThemeDao getTsmpDpApiThemeDao() {
		return this.tsmpDpApiThemeDao;
	}
	
	protected TsmpDpThemeCategoryDao getTsmpDpThemeCategoryDao() {
		return this.tsmpDpThemeCategoryDao;
	}
	
	protected TsmpApiExtDao getTsmpApiExtDao() {
		return this.tsmpApiExtDao;
	}
	
	protected ApiItemService getApiItemService() {
		return this.apiItemService;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.helper;
	}
	
	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}
}
