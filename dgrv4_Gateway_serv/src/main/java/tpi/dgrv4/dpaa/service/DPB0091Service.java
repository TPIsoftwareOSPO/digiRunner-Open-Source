package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0091ApiItem;
import tpi.dgrv4.dpaa.vo.DPB0091Req;
import tpi.dgrv4.dpaa.vo.DPB0091Resp;
import tpi.dgrv4.dpaa.vo.TsmpApiItem;
import tpi.dgrv4.entity.daoService.OpenApiKeyService;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpOpenApiKey;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiExtDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpApiThemeDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpThemeCategoryDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0091Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpApiDao tsmpApiDao;
	private TsmpOpenApiKeyDao tsmpOpenApiKeyDao; 
	private TsmpDpApiThemeDao tsmpDpApiThemeDao;
	private TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao;
	private TsmpApiExtDao tsmpApiExtDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private ApiItemService apiItemService;
	private TsmpClientDao tsmpClientDao;
	private ServiceConfig serviceConfig;
	
	private Integer pageSize;
	
	@Autowired
	public DPB0091Service(TsmpApiDao tsmpApiDao, TsmpOpenApiKeyDao tsmpOpenApiKeyDao,
			TsmpDpApiThemeDao tsmpDpApiThemeDao, TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao,
			TsmpApiExtDao tsmpApiExtDao, TsmpOrganizationDao tsmpOrganizationDao, TsmpDpFileDao tsmpDpFileDao,
			ApiItemService apiItemService, TsmpClientDao tsmpClientDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpOpenApiKeyDao = tsmpOpenApiKeyDao;
		this.tsmpDpApiThemeDao = tsmpDpApiThemeDao;
		this.tsmpDpThemeCategoryDao = tsmpDpThemeCategoryDao;
		this.tsmpApiExtDao = tsmpApiExtDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.apiItemService = apiItemService;
		this.tsmpClientDao = tsmpClientDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB0091Resp queryOpenApiKeyDetailByPk(TsmpAuthorization tsmpAuthorization, DPB0091Req req, ReqHeader reqHeader) {
		DPB0091Resp resp = new DPB0091Resp();

		try {
 			Long openApiKeyId = req.getOpenApiKeyId();
			if(StringUtils.isEmpty(openApiKeyId)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
 
			String apiKey = req.getApiKey();
			String moduleName = req.getModuleName();
			TsmpApi lastRecord = getLastRecordFromPrevPage(apiKey, moduleName);
			
			if(lastRecord == null) {//只有在第一頁時,才會取得 Open API Key 的值(DPB0091Resp 的欄位,在第一頁才會有資料)
				Optional<TsmpOpenApiKey> opt = getTsmpOpenApiKeyDao().findById(openApiKeyId);
				if (!opt.isPresent()) {
					throw TsmpDpAaRtnCode._1298.throwing();
				}
				TsmpOpenApiKey openApiKey = opt.get();
				resp = getResp(openApiKey, reqHeader.getLocale());
			}
			
			//每一頁,都會取得 API 的分頁資料 (也就是每一頁都有 DPB0091Resp 的 dataList 資料)
			List<TsmpApi> apiList = getTsmpApiDao().queryByOpenApiKeyId(lastRecord, getPageSize(), openApiKeyId);			
			if (apiList == null || apiList.isEmpty()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			List<TsmpApiItem> apiItemList = getApiItemService().getApiItemList(apiList, reqHeader.getLocale());
			List<DPB0091ApiItem> dataList = getApiItemList(apiItemList, resp);
			resp.setDataList(dataList);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private List<DPB0091ApiItem> getApiItemList(List<TsmpApiItem> apiItemList, DPB0091Resp resp) {
		List<DPB0091ApiItem> dataList = resp.getDataList();
		if(dataList == null) {
			dataList = new ArrayList<DPB0091ApiItem>();
		}
		for (TsmpApiItem apiItem : apiItemList) {			
			DPB0091ApiItem dpb0091_item = new DPB0091ApiItem();
			dpb0091_item.setApiKey(apiItem.getApiKey());
			dpb0091_item.setModuleName(apiItem.getModuleName());
			dpb0091_item.setApiName(apiItem.getApiName());
			dpb0091_item.setThemeDatas(apiItem.getThemeDatas());
			dpb0091_item.setOrgId(apiItem.getOrgId());
			dpb0091_item.setOrgName(apiItem.getOrgName());
			dpb0091_item.setApiDesc(apiItem.getApiDesc());
			dpb0091_item.setApiExtId(apiItem.getApiExtId());
			dpb0091_item.setApiUid(apiItem.getApiUid());
			dpb0091_item.setFileName(apiItem.getFileName());
			dpb0091_item.setFilePath(apiItem.getFilePath());
			
			dataList.add(dpb0091_item);
		}
		return dataList;
	}
	
	private DPB0091Resp getResp(TsmpOpenApiKey openApiKey, String locale) throws Exception {
		String clientId = openApiKey.getClientId();
		Optional<TsmpClient> opt_client = getTsmpClientDao().findById(clientId);
		if (!opt_client.isPresent()) {
			this.logger.error("用戶不存在: " + clientId);
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		TsmpClient client = opt_client.get();
		String clientName = client.getClientName();
		String clientAlias = client.getClientAlias();
		
		Optional<String> opt_date = DateTimeUtil.dateTimeToString(openApiKey.getCreateDateTime(),
				DateTimeFormatEnum.西元年月日_2);// yyyy/MM/dd
		if (!opt_date.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}		
		
		String secretKey = openApiKey.getSecretKey();
		String secretKeyMask = ServiceUtil.dataMask(secretKey, 5, 5);//只顯示前後五個字,中間其餘隱藏加星號(*)
		
		String expiredAtStr = getDateTime(openApiKey.getExpiredAt());
		String status = openApiKey.getOpenApiKeyStatus();// 狀態,為 0 或 1
		String rolloverFlag = openApiKey.getRolloverFlag();
		String statusName = OpenApiKeyService.getStatusName(expiredAtStr, status, rolloverFlag, locale);
		
		DPB0091Resp resp = new DPB0091Resp();
		resp.setOpenApiKeyId(openApiKey.getOpenApiKeyId());
		resp.setClientId(clientId);//用戶代碼
		resp.setClientName(clientName);//用戶端代號
		resp.setClientAlias(clientAlias);//用戶端名稱
		resp.setOpenApiKey(openApiKey.getOpenApiKey());
		resp.setMimaKey(secretKeyMask);
		resp.setOpenApiKeyAlias(openApiKey.getOpenApiKeyAlias());
		resp.setTimesQuota(openApiKey.getTimesQuota());
		resp.setTimesThreshold(openApiKey.getTimesThreshold());
		resp.setCreateDateTime(opt_date.get());
		resp.setExpiredAt(expiredAtStr);// 格式: yyyy/MM/dd
		resp.setRevokedAt(getDateTime(openApiKey.getRevokedAt()));// 格式: yyyy/MM/dd
		resp.setOpenApiKeyStatus(status);
		resp.setOpenApiKeyStatusName(statusName);

		return resp;
	}

	private TsmpApi getLastRecordFromPrevPage(String apiKey, String moduleName) {
		if (!StringUtils.isEmpty(apiKey) && !StringUtils.isEmpty(moduleName)) {
			TsmpApiId id = new TsmpApiId(apiKey, moduleName);
			Optional<TsmpApi> opt = getTsmpApiDao().findById(id);
			return opt.orElse(null);
		}
		return null;
	}

	private String getDateTime(Long timeInMillis) {
		if(timeInMillis == null) {
			return "";
		}
		Date dt = new Date(timeInMillis);
		String dtStr = DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日_2).orElse(null);// yyyy/MM/dd
		return dtStr;
	}
 
	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}
	
	protected TsmpOpenApiKeyDao getTsmpOpenApiKeyDao() {
		return this.tsmpOpenApiKeyDao;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0091");
		return this.pageSize;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
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
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
	
	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}
	
	protected ApiItemService getApiItemService() {
		return this.apiItemService;
	}
	
	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}
 
}
