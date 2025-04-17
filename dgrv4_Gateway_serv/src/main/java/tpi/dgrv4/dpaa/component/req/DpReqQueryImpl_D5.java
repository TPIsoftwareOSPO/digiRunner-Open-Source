package tpi.dgrv4.dpaa.component.req;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpModule;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.dpaa.component.cache.proxy.TsmpDpMailTpltCacheProxy;
import tpi.dgrv4.dpaa.service.ApiItemService;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.TsmpApiItem;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailTplt;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd5;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd5d;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd5Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd5dDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdersDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.util.*;

/**
 * 處理"Open API Key"簽核單查詢
 * @author Mini
 *
 */
@Service
public class DpReqQueryImpl_D5 extends DpReqQueryAbstract<DpReqQueryResp_D5> //
	implements DpReqQueryIfs<DpReqQueryResp_D5> {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpReqOrderd5Dao tsmpDpReqOrderd5Dao;
	private TsmpDpReqOrderd5dDao tsmpDpReqOrderd5dDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpDpMailTpltCacheProxy tsmpDpMailTpltCacheProxy;
	private ApiItemService apiItemService;
	private MailHelper mailHelper;
	
	@Autowired
	public DpReqQueryImpl_D5(TsmpDpReqOrdermDao tsmpDpReqOrdermDao, TsmpDpReqOrdersDao tsmpDpReqOrdersDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpUserDao tsmpUserDao, TsmpClientDao tsmpClientDao, TsmpDpFileDao tsmpDpFileDao,
			DpReqServiceFactory dpReqServiceFactory, TsmpDpReqOrderd5Dao tsmpDpReqOrderd5Dao,
			TsmpDpReqOrderd5dDao tsmpDpReqOrderd5dDao, TsmpApiDao tsmpApiDao,
			TsmpDpMailTpltCacheProxy tsmpDpMailTpltCacheProxy, ApiItemService apiItemService, MailHelper mailHelper) {
		super(tsmpDpReqOrdermDao, tsmpDpReqOrdersDao, tsmpDpItemsCacheProxy, tsmpOrganizationDao, tsmpUserDao,
				tsmpClientDao, tsmpDpFileDao, dpReqServiceFactory);
		this.tsmpDpReqOrderd5Dao = tsmpDpReqOrderd5Dao;
		this.tsmpDpReqOrderd5dDao = tsmpDpReqOrderd5dDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpDpMailTpltCacheProxy = tsmpDpMailTpltCacheProxy;
		this.apiItemService = apiItemService;
		this.mailHelper = mailHelper;
	}

	@Override
	public List<DpReqQueryResp_D5> doQueryDetail(Long reqOrdermId, String locale) {
		Optional<TsmpDpReqOrderm> opt_m = getTsmpDpReqOrdermDao().findById(reqOrdermId);
		if(!opt_m.isPresent()) {
			return Collections.emptyList();
		}
		
		List<TsmpDpReqOrderd5> d5List = getTsmpDpReqOrderd5Dao().findByRefReqOrdermId(reqOrdermId);
		if (d5List == null || d5List.isEmpty()) {
			return Collections.emptyList();
		}

		String clientId = "";
		String clientName = "";
		String clientAlias = "";
		List<DpReqQueryResp_D5> d5RespList = new ArrayList<>();
		for (TsmpDpReqOrderd5 d5 : d5List) {
			clientId = d5.getClientId();
			TsmpClient client = getClient(clientId);
			if (client != null) {
				clientName = client.getClientName();
				clientAlias = client.getClientAlias();
			}
			
			//D5
			String secretKey = d5.getSecretKey();
			String secretKeyMask = ServiceUtil.dataMask(secretKey, 5, 5);//只顯示前後五個字,中間其餘隱藏加星號(*)
			
			DpReqQueryResp_D5 d5Resp = new DpReqQueryResp_D5();
			d5Resp.setReqOrderd5Id(d5.getReqOrderd5Id());
			d5Resp.setClientId(nvl(clientId));
			d5Resp.setClientName(nvl(clientName));
			d5Resp.setClientAlias(nvl(clientAlias));
			d5Resp.setOpenApiKeyId(d5.getRefOpenApiKeyId());
			d5Resp.setOpenApiKey(nvl(d5.getOpenApiKey()));
			d5Resp.setSecretKey(nvl(secretKeyMask));
			d5Resp.setOpenApiKeyAlias(d5.getOpenApiKeyAlias());
			String expiredAtStr = getDateTime(d5.getExpiredAt());	
			d5Resp.setExpiredAt(expiredAtStr);// 格式: yyyy/MM/dd
			d5Resp.setTimesThreshold(d5.getTimesThreshold());
			
			//D5D
			List<DpReqQueryResp_D5d> d5dRespList = getD5dRespList(d5.getReqOrderd5Id(), locale);
			d5Resp.setD5dRespList(d5dRespList);
			
			d5RespList.add(d5Resp);
		}
		return d5RespList;
	}
	
	@Override
	protected TsmpMailEvent getTsmpMailEvent(String userId, String recipients, TsmpAuthorization auth,
			DpReqQueryResp<DpReqQueryResp_D5> resp) {
		if (resp == null) {
			this.logger.debug("Query response is empty!");
			return null;
		}
		if (StringUtils.isEmpty(recipients)) {
			this.logger.debug(String.format("USER %s has empty emails!", userId));
			return null;
		}
		
		String subject = getTemplate("subject.revi-wait.D5");
		String body = getTemplate("body.revi-wait.D5");
		if (StringUtils.isEmpty(subject) || StringUtils.isEmpty(body)) {
			this.logger.debug(String.format("Cannot find email templates: %s, %s", "subject.revi-wait.D5", "body.revi-wait.D5"));
			return null;
		}
		
		Map<String, Object> subjectParams = new HashMap<>();
		subjectParams.put("projectName", TsmpDpModule.DP.getChiDesc());
		subjectParams.put("reqOrderNo", resp.getReqOrderNo());
		subjectParams.put("reviewType", resp.getReqTypeName().replace("Open ", ""));
		subject = getMailHelper().buildNestedContent("subject.revi-wait.D5", subjectParams);
 
		List<Map<String, Object>> apiMappings = getApiMappings(resp.getDetailList());
		Map<String, Object> bodyParams = new HashMap<>();
		bodyParams.put("reviewType", resp.getReqTypeName().replace("Open ", ""));
		bodyParams.put("reqOrderNo", resp.getReqOrderNo());
		bodyParams.put("createDateTime", DateTimeUtil.dateTimeToString(resp.getCreateDateTime(), DateTimeFormatEnum.西元年月日).orElse(String.valueOf(TsmpDpAaRtnCode._1295)));
		bodyParams.put("applyUserName", resp.getApplierName());
		bodyParams.put("orgName", resp.getOrgName());
		bodyParams.put("subTitle", resp.getReqSubtypeName().replace("Open ", ""));
		bodyParams.put("chkStatusName", resp.getCurrentReviewStatusName());
		bodyParams.put("chkPointName", resp.getCurrentLayerName());
		bodyParams.put("apiMappings", apiMappings);
		
		String content = getMailHelper().buildNestedContent("body.revi-wait.D5", bodyParams);
		
		return new TsmpMailEventBuilder() //
		.setSubject(subject)
		.setContent(content)
		.setRecipients(recipients)
		.setCreateUser(auth.getUserName())
		.setRefCode("body.revi-wait.D5")
		.build();
	}
	
	private List<Map<String, Object>> getApiMappings(List<DpReqQueryResp_D5> detailList){
		List<Map<String, Object>> apiMappings = new ArrayList<>();
		Map<String, Object> apiMapping = new HashMap<>();
		List<DpReqQueryResp_D5d> d5dList = null;
		List<String> apiNameList = null;
		for (DpReqQueryResp_D5 detail : detailList) {
			apiMapping.put("clientId", detail.getClientId());
			apiMapping.put("clientName", detail.getClientName());
			apiMapping.put("clientAlias", detail.getClientAlias());

			d5dList = detail.getD5dRespList();
			if (d5dList != null && !d5dList.isEmpty()) {
				apiNameList = new ArrayList<>();
				for (DpReqQueryResp_D5d d5d : d5dList) {
					apiNameList.add(d5d.getApiName());
				}
			}
			
			apiMapping.put("apiNames", apiNameList);
			apiMappings.add(apiMapping);
		}

		return apiMappings;
	}
	
	private String getDateTime(Long timeInMillis) {
		if(timeInMillis == null) {
			return "";
		}
		Date dt = new Date(timeInMillis);
		return DateTimeUtil.dateTimeToString(dt, DateTimeFormatEnum.西元年月日_2).orElse(String.valueOf(TsmpDpAaRtnCode._1295));
	}
 
	private List<DpReqQueryResp_D5d> getD5dRespList(Long d5Id, String locale) {
		List<TsmpDpReqOrderd5d> d5dList = getTsmpDpReqOrderd5dDao().findByRefReqOrderd5Id(d5Id);
		if (d5dList == null || d5dList.isEmpty()) {
			return Collections.emptyList();
		}

		DpReqQueryResp_D5d d5dResp = null;
		Long d5dId = null;
		String apiUid = null;
		TsmpApi api = null;
		String apiName = null;
		String moduleName = null;
		String apiDesc = null;
		String orgId = null;
		String orgName = null;
		String apiKey = null;
		Long apiExtId = null;
		List<DpReqQueryResp_D5d> d5dRespList = new ArrayList<>();
		Map<String, String> docFileInfo = null;
		for (TsmpDpReqOrderd5d d5d : d5dList) {
			d5dId = d5d.getReqOrderd5dId();
			apiUid = d5d.getRefApiUid();
			api = getApi(apiUid);
			apiName = new String();
			moduleName = new String();
			apiDesc = new String();
			orgName = new String();
			apiKey = new String();
			orgId = new String();
			apiExtId = null;
			Map<Long, String> themeList = new HashMap<Long, String>();
			if (api != null) {
				TsmpApiItem item = getApiItemService().getTsmpApiItem(api, locale);
				apiName = item.getApiName();
				moduleName = item.getModuleName();
				apiDesc = nvl(item.getApiDesc());
				orgId = nvl(item.getOrgId());
				orgName = item.getOrgName();
				apiKey = item.getApiKey();
				apiExtId = item.getApiExtId();
				themeList = item.getThemeDatas();
				docFileInfo = getD5DocFileInfo(item);
			}

			d5dResp = new DpReqQueryResp_D5d();
			d5dResp.setReqOrderd5dId(d5dId);
			d5dResp.setApiUid(apiUid);
			d5dResp.setApiName(apiName);
			d5dResp.setModuleName(moduleName);
			d5dResp.setApiDesc(apiDesc);
			d5dResp.setThemeList(themeList);
			d5dResp.setOrgId(orgId);
			d5dResp.setOrgName(orgName);
			d5dResp.setApiKey(apiKey);
			d5dResp.setApiExtId(apiExtId);
			d5dResp.setDocFileInfo(docFileInfo);
			
			d5dRespList.add(d5dResp);
		}
		
		return d5dRespList;
	}
	
	private Map<String, String> getD5DocFileInfo(TsmpApiItem item) {
		String fileName = item.getFileName();
		String filePath= item.getFilePath();
		if (StringUtils.isEmpty(fileName)) {
			return Collections.emptyMap();
		}
		Map<String, String> d5DocFileInfo = new HashMap<>();
		d5DocFileInfo.put(fileName, filePath);
		return d5DocFileInfo;
	}

	private TsmpApi getApi(String apiUid) {
		List<TsmpApi> apiList = getTsmpApiDao().findByApiUid(apiUid);
		if (apiList == null || apiList.isEmpty()) {
			return null;
		}
		return apiList.get(0);
	}
	
	private String getTemplate(String code) {
		List<TsmpDpMailTplt> list = getTsmpDpMailTpltCacheProxy().findByCode(code);
		if (list != null && !list.isEmpty()) {
			return list.get(0).getTemplateTxt();
		}
		return null;
	}

	private TsmpClient getClient(String ClinetId) {
		return getTsmpClientDao().findById(ClinetId).orElse(null);
	}
	
	protected TsmpDpReqOrderd5Dao getTsmpDpReqOrderd5Dao() {
		return this.tsmpDpReqOrderd5Dao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpDpReqOrderd5dDao getTsmpDpReqOrderd5dDao() {
		return this.tsmpDpReqOrderd5dDao;
	}
 
	protected TsmpDpMailTpltCacheProxy getTsmpDpMailTpltCacheProxy() {
		return this.tsmpDpMailTpltCacheProxy;
	}
	
	protected ApiItemService getApiItemService() {
		return this.apiItemService;
	}

	protected MailHelper getMailHelper() {
		return this.mailHelper;
	}

}
