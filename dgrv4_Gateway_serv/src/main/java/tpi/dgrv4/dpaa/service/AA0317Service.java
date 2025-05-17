package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.constant.TsmpApiSrc;
import tpi.dgrv4.dpaa.service.composer.ComposerService;
import tpi.dgrv4.dpaa.vo.AA0317Data;
import tpi.dgrv4.dpaa.vo.AA0317Module;
import tpi.dgrv4.dpaa.vo.AA0317RedirectByIpData;
import tpi.dgrv4.dpaa.vo.AA0317RegModule;
import tpi.dgrv4.dpaa.vo.AA0317Req;
import tpi.dgrv4.dpaa.vo.AA0317ReqItem;
import tpi.dgrv4.dpaa.vo.AA0317Resp;
import tpi.dgrv4.dpaa.vo.AA0317RespItem;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpApiRegId;
import tpi.dgrv4.entity.entity.jpql.DgrComposerFlow;
import tpi.dgrv4.entity.entity.jpql.TsmpRegModule;
import tpi.dgrv4.entity.repository.DgrComposerFlowDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpRegModuleDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;


@Service
public class AA0317Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpApiRegDao tsmpApiRegDao;
	private TsmpRegModuleDao tsmpRegModuleDao;
	private ComposerService composerService;
	private ObjectMapper objectMapper;
	private DgrComposerFlowDao dgrComposerFlowDao;
	
	@Autowired
	public AA0317Service(TsmpOrganizationDao tsmpOrganizationDao, TsmpApiDao tsmpApiDao, TsmpApiRegDao tsmpApiRegDao,
			TsmpRegModuleDao tsmpRegModuleDao, ComposerService composerService, ObjectMapper objectMapper,
			DgrComposerFlowDao dgrComposerFlowDao) {
		super();
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiRegDao = tsmpApiRegDao;
		this.tsmpRegModuleDao = tsmpRegModuleDao;
		this.composerService = composerService;
		this.objectMapper = objectMapper;
		this.dgrComposerFlowDao = dgrComposerFlowDao;
	}

	protected class ApiData {

		private TsmpApi tsmpApi;

		private TsmpApiReg tsmpApiReg;

		private AA0317RespItem respItem;

		public TsmpApi getTsmpApi() {
			return tsmpApi;
		}

		public void setTsmpApi(TsmpApi tsmpApi) {
			this.tsmpApi = tsmpApi;
		}

		public TsmpApiReg getTsmpApiReg() {
			return tsmpApiReg;
		}

		public void setTsmpApiReg(TsmpApiReg tsmpApiReg) {
			this.tsmpApiReg = tsmpApiReg;
		}

		public AA0317RespItem getRespItem() {
			return respItem;
		}

		public void setRespItem(AA0317RespItem respItem) {
			this.respItem = respItem;
		}
	}

	public AA0317Resp exportRegCompAPIs(TsmpAuthorization auth, AA0317Req req) {
		String userName = auth.getUserName();
		String orgId = auth.getOrgId();

		return exportRegCompAPIs(userName, orgId, req);
	}

	/**
	 * In-Memory, <br> 
	 * Landing 端匯出資料,由此進入
	 */
	public AA0317Resp exportRegCompAPIs(String userName, String orgId, AA0317Req req) {
		List<ApiData> apiDataList = new ArrayList<>();
		checkParams(orgId, userName, req, apiDataList);

		AA0317Resp resp = new AA0317Resp();
		try {
			resp = exportAPI(apiDataList);

			String fileName = getFileName();
			resp.setFileName(fileName);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected void checkParams(String orgId, String userName, AA0317Req req, List<ApiData> apiDataList) {
		if (StringUtils.isEmpty(userName)) {
			throw TsmpDpAaRtnCode._1258.throwing();
		}
		
		if (StringUtils.isEmpty(orgId)) {
			throw TsmpDpAaRtnCode._1273.throwing();
		}

		// apiList 不得為空
		List<AA0317ReqItem> reqItemList = req.getApiList();
		if (CollectionUtils.isEmpty(reqItemList)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		// 取出組織原則
		List<String> userOrgIdList = checkOrgExists(orgId);

		TsmpApiId tsmpApiId;
		TsmpApi tsmpApi;
		String apiSrc;
		int ignoreCnt = 0;
		TsmpApiRegId tsmpApiRegId;
		TsmpApiReg tsmpApiReg;
		ApiData apiData;
		for (AA0317ReqItem reqItem : reqItemList) {
			// apiKey, moduleName 不得為空
			if (StringUtils.isEmpty(reqItem.getApiKey()) || //
				StringUtils.isEmpty(reqItem.getModuleName())) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			tsmpApiId = reqItem.toTsmpApiId();
			
			// 取出 TSMP_API 資料
			Optional<TsmpApi> opt_tsmpApi = getTsmpApiDao().findById(tsmpApiId);
			if (!opt_tsmpApi.isPresent()) {
				this.logger.debug(String.format("TSMP_API not found: %s-%s", tsmpApiId.getModuleName(), tsmpApiId.getApiKey()));
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			tsmpApi = opt_tsmpApi.get();
			
			// 只處理 "R" 或 "C" 的 API
			apiSrc = tsmpApi.getApiSrc();
			if (!(TsmpApiSrc.REGISTERED.value().equals(apiSrc) || TsmpApiSrc.COMPOSED.value().equals(apiSrc))) {
				ignoreCnt++;
				this.logger.debug(String.format("Not registered or composed API: %s-%s", tsmpApiId.getModuleName(), tsmpApiId.getApiKey()));
				continue;
			}

			// API組織原則
			checkApiOrg(tsmpApi, userOrgIdList);
			
			tsmpApiRegId = reqItem.toTsmpApiRegId();
			Optional<TsmpApiReg> opt_tsmpApiReg = getTsmpApiRegDao().findById(tsmpApiRegId);
			if (!opt_tsmpApiReg.isPresent()) {
				this.logger.debug(String.format("TSMP_API_REG not found: %s-%s", tsmpApiId.getModuleName(), tsmpApiId.getApiKey()));
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			tsmpApiReg = opt_tsmpApiReg.get();
	
			apiData = new ApiData();
			apiData.setTsmpApi(tsmpApi);
			apiData.setTsmpApiReg(tsmpApiReg);
			apiDataList.add(apiData);
		}
		
		if (reqItemList.size() == ignoreCnt) {
			throw TsmpDpAaRtnCode._1444.throwing();
		}
	}

	protected List<String> checkOrgExists(String userOrgId) {
		List<String> userOrgIdList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(userOrgId, null);
		if (CollectionUtils.isEmpty(userOrgIdList)) {
			throw TsmpDpAaRtnCode._1222.throwing();
		}
		return userOrgIdList;
	}

	protected void checkApiOrg(TsmpApi tsmpApi, List<String> userOrgIdList) {
		String apiOrgId = tsmpApi.getOrgId();
		if (!StringUtils.isEmpty(apiOrgId) && !userOrgIdList.contains(apiOrgId)) {
			this.logger.debug(String.format("Violate organization principle: apiOrgId(%s) not in %s", apiOrgId, userOrgIdList));
			throw TsmpDpAaRtnCode._1298.throwing();
		}
	}

	protected AA0317Resp exportAPI(List<ApiData> apiDataList){
		String apiSrc;
		Map<String, List<AA0317RespItem>> rModuleList = new HashMap<>();
		Map<String, List<AA0317RespItem>> cModuleList = new HashMap<>();
		Map<String, AA0317RegModule> regModuleMapping = new HashMap<>();
		List<List<String>> moduleNameAndApiNameList = new ArrayList<List<String>>();
		HashMap<String, ApiData> composerApiDataHM = new HashMap<String, ApiData>();
		for (ApiData apiData : apiDataList) {
			setRespItem(apiData);
	
			apiSrc = apiData.getTsmpApi().getApiSrc();
			if (TsmpApiSrc.REGISTERED.value().equals(apiSrc)) {
				addToModuleList(rModuleList, apiData);
				addToRegModuleList(regModuleMapping, apiData);
			} else if (TsmpApiSrc.COMPOSED.value().equals(apiSrc)) {
				/* 因為用迴圈方式查資料，效率不好。改要一次方式查資料。
				setFlow(apiData);
				*/
				
				// 收集全部的moduleName與ApiName，在一次request api跟Composer後端查詢資料。
				List<String> data = new ArrayList<String>();
				String moduleName = apiData.getTsmpApi().getModuleName();
				String apiKey = apiData.getTsmpApi().getApiKey(); 
				data.add(moduleName);
				data.add(apiKey);
				moduleNameAndApiNameList.add(data);
				String key = moduleName+apiKey;
				composerApiDataHM.put(key, apiData);
				
				addToModuleList(cModuleList, apiData);
			}
		}
		
		//向Composer後端查詢資料，並將資料放到對應的apiDataList內的ApiData物件
		if (!composerApiDataHM.isEmpty()) {
			queryAndAddNodesData(composerApiDataHM, moduleNameAndApiNameList);
		}
		
		List<AA0317Module> rList = integrate(rModuleList, regModuleMapping);
		List<AA0317Module> cList = integrate(cModuleList);

		AA0317Resp resp = new AA0317Resp();
		if (!CollectionUtils.isEmpty(rList) || !CollectionUtils.isEmpty(cList)) {
			resp.setData(new AA0317Data());
			resp.getData().setR(rList);
			resp.getData().setC(cList);
		}
		return resp;
	}

	// 將 TSMP_API, TSMP_API_REG 資料轉換成 AA0317RespItem，並放入 ApiData 中
	protected void setRespItem(ApiData apiData) {
		AA0317RespItem respItem = new AA0317RespItem();
		respItem.setApiKey(apiData.getTsmpApi().getApiKey());
		respItem.setModuleName(apiData.getTsmpApi().getModuleName());
		respItem.setApiName(apiData.getTsmpApi().getApiName());
		respItem.setApiDesc(nvl(apiData.getTsmpApi().getApiDesc()));
		respItem.setApiOwner(nvl(apiData.getTsmpApi().getApiOwner()));
		respItem.setUrlRID(nvl(apiData.getTsmpApiReg().getUrlRid(), "0"));
		respItem.setApiSrc(apiData.getTsmpApi().getApiSrc());
		respItem.setSrcURL(nvl(apiData.getTsmpApiReg().getSrcUrl()));
		respItem.setApiUUID(nvl(apiData.getTsmpApiReg().getApiUuid()));
		respItem.setContentType(nvl(apiData.getTsmpApiReg().getConsumesOfJson()));
		respItem.setEnpoint(nvl(apiData.getTsmpApiReg().getPathOfJson()));
		respItem.setHttpHeader(nvl(apiData.getTsmpApiReg().getHeadersOfJson()));
		respItem.setHttpMethod(nvl(apiData.getTsmpApiReg().getMethodOfJson()));
		respItem.setParams(nvl(apiData.getTsmpApiReg().getParamsOfJson()));
		respItem.setProduce(nvl(apiData.getTsmpApiReg().getProducesOfJson()));
		// respItem.setFlow(flow); // 留給 setFlow() 方法設值
		respItem.setNo_oauth(nvl(apiData.getTsmpApiReg().getNoOauth(), "0"));
		respItem.setJweFlag(nvl(apiData.getTsmpApi().getJewFlag(), "0"));
		respItem.setJweFlagResp(nvl(apiData.getTsmpApi().getJewFlagResp(), "0"));
		respItem.setFunFlag(nvl(apiData.getTsmpApiReg().getFunFlag(), 0));
		respItem.setMockBody(nvl(apiData.getTsmpApi().getMockBody()));
		respItem.setMockHeaders(nvl(apiData.getTsmpApi().getMockHeaders()));
		respItem.setMockStatusCode(apiData.getTsmpApi().getMockStatusCode());
		respItem.setApiCacheFlag(apiData.getTsmpApi().getApiCacheFlag());
		respItem.setFixedCacheTime(apiData.getTsmpApi().getFixedCacheTime());
		respItem.setApiStatus(apiData.getTsmpApi().getApiStatus());
		respItem.setPublicFlag(apiData.getTsmpApi().getPublicFlag());
		respItem.setApiReleaseTime(null!=apiData.getTsmpApi().getApiReleaseTime()?Long.toString(apiData.getTsmpApi().getApiReleaseTime().getTime()):null);
		respItem.setScheduledLaunchDate(null!=apiData.getTsmpApi().getScheduledLaunchDate()?Long.toString(apiData.getTsmpApi().getScheduledLaunchDate()):null);
		respItem.setScheduledRemovalDate(null!=apiData.getTsmpApi().getScheduledRemovalDate()?Long.toString(apiData.getTsmpApi().getScheduledRemovalDate()):null);
		respItem.setEnableScheduledDate(null!=apiData.getTsmpApi().getEnableScheduledDate()?Long.toString(apiData.getTsmpApi().getEnableScheduledDate()):null);
		respItem.setDisableScheduledDate(null!=apiData.getTsmpApi().getDisableScheduledDate()?Long.toString(apiData.getTsmpApi().getDisableScheduledDate()):null);
		String label1 = apiData.getTsmpApi().getLabel1();
		String label2 = apiData.getTsmpApi().getLabel2();
		String label3 = apiData.getTsmpApi().getLabel3();
		String label4 = apiData.getTsmpApi().getLabel4();
		String label5 = apiData.getTsmpApi().getLabel5();
		Set<String> set = new HashSet<>();
		set.add(label1);
		set.add(label2);
		set.add(label3);
		set.add(label4);
		set.add(label5);
		List<String> labelList = new ArrayList<>(set);
		labelList.removeIf(value -> value == null);
		labelList.sort(null);

		respItem.setLabelList(labelList);
		
		
		String isRedirectByIp = apiData.getTsmpApiReg().getRedirectByIp();
		if (StringUtils.hasLength(isRedirectByIp)) {
			respItem.setIsRedirectByIp(isRedirectByIp);
		} else {
			respItem.setIsRedirectByIp("N");
		}

		List<AA0317RedirectByIpData> redirectByIpDataList = new ArrayList<>();

		AA0317RedirectByIpData data1 = new AA0317RedirectByIpData();
		String ipForRedirect1 = apiData.getTsmpApiReg().getIpForRedirect1();
		String ipSrcUrl1 = apiData.getTsmpApiReg().getIpSrcUrl1(); // ipSrcUrl1

		if (StringUtils.hasLength(ipForRedirect1) && StringUtils.hasLength(ipSrcUrl1)) {

			data1.setIpForRedirect(ipForRedirect1);
			data1.setIpSrcUrl(nvl(ipSrcUrl1));
			redirectByIpDataList.add(data1);
		}

		AA0317RedirectByIpData data2 = new AA0317RedirectByIpData();
		String ipForRedirect2 = apiData.getTsmpApiReg().getIpForRedirect2();
		String ipSrcUrl2 = apiData.getTsmpApiReg().getIpSrcUrl2(); // ipSrcUrl2

		if (StringUtils.hasLength(ipForRedirect2) && StringUtils.hasLength(ipSrcUrl2)) {
			data2.setIpForRedirect(ipForRedirect2);
			data2.setIpSrcUrl(nvl(ipSrcUrl2));
			redirectByIpDataList.add(data2);
		}

		AA0317RedirectByIpData data3 = new AA0317RedirectByIpData();
		String ipForRedirect3 = apiData.getTsmpApiReg().getIpForRedirect3();
		String ipSrcUrl3 = apiData.getTsmpApiReg().getIpSrcUrl3();
		if (StringUtils.hasLength(ipForRedirect3) && StringUtils.hasLength(ipSrcUrl3)) {
			data3.setIpForRedirect(ipForRedirect3);
			data3.setIpSrcUrl(nvl(ipSrcUrl3));
			redirectByIpDataList.add(data3);
		}

		AA0317RedirectByIpData data4 = new AA0317RedirectByIpData();
		String ipForRedirect4 = apiData.getTsmpApiReg().getIpForRedirect4();
		String ipSrcUrl4 = apiData.getTsmpApiReg().getIpSrcUrl4();
		if (StringUtils.hasLength(ipForRedirect4) && StringUtils.hasLength(ipSrcUrl4)) {
			data4.setIpForRedirect(nvl(ipForRedirect4));
			data4.setIpSrcUrl(nvl(ipSrcUrl4));
			redirectByIpDataList.add(data4);
		}

		AA0317RedirectByIpData data5 = new AA0317RedirectByIpData();// ipSrcUrl5
		String ipForRedirect5 = apiData.getTsmpApiReg().getIpForRedirect5();
		String ipSrcUrl5 = apiData.getTsmpApiReg().getIpSrcUrl5();
		if (StringUtils.hasLength(ipForRedirect5) && StringUtils.hasLength(ipSrcUrl5)) {
			data5.setIpForRedirect(nvl(ipForRedirect5));
			data5.setIpSrcUrl(nvl(ipSrcUrl5));
			redirectByIpDataList.add(data5);
		}
		
		respItem.setRedirectByIpDataList(redirectByIpDataList);
		respItem.setHeaderMaskKey(nvl(apiData.getTsmpApiReg().getHeaderMaskKey()));
		respItem.setHeaderMaskPolicy(StringUtils.hasLength(apiData.getTsmpApiReg().getHeaderMaskPolicy())
				? apiData.getTsmpApiReg().getHeaderMaskPolicy()
				: "0");
		respItem.setHeaderMaskPolicyNum(apiData.getTsmpApiReg().getHeaderMaskPolicyNum());
		respItem.setHeaderMaskPolicySymbol(nvl(apiData.getTsmpApiReg().getHeaderMaskPolicySymbol()));

		respItem.setBodyMaskKeyword(nvl(apiData.getTsmpApiReg().getBodyMaskKeyword()));
		respItem.setBodyMaskPolicy(StringUtils.hasLength(apiData.getTsmpApiReg().getBodyMaskPolicy())
				? apiData.getTsmpApiReg().getBodyMaskPolicy()
				: "0");
		respItem.setBodyMaskPolicyNum(apiData.getTsmpApiReg().getBodyMaskPolicyNum());
		respItem.setBodyMaskPolicySymbol(nvl(apiData.getTsmpApiReg().getBodyMaskPolicySymbol()));
 
		respItem.setFailDiscoveryPolicy(nvl(apiData.getTsmpApiReg().getFailDiscoveryPolicy(), "0"));
		respItem.setFailHandlePolicy(nvl(apiData.getTsmpApiReg().getFailHandlePolicy(), "0"));

		apiData.setRespItem(respItem);
	}

	protected void addToModuleList(Map<String, List<AA0317RespItem>> modules, ApiData apiData) {
		String moduleName = apiData.getTsmpApi().getModuleName();
		List<AA0317RespItem> itemList = modules.get(moduleName);
		if (itemList == null) {
			itemList = new ArrayList<>();
			modules.put(moduleName, itemList);
		}
		
		itemList.add(apiData.getRespItem());
	}

	protected void addToRegModuleList(Map<String, AA0317RegModule> regModuleMapping, ApiData apiData) {
		String moduleName = apiData.getTsmpApi().getModuleName();
		if (regModuleMapping.containsKey(moduleName)) {
			return;
		}
		
		AA0317RegModule aa0317RegModule = null;
		List<TsmpRegModule> tsmpRegModuleList = getTsmpRegModuleDao().findByModuleNameAndLatest(moduleName, "Y");
		if (!CollectionUtils.isEmpty(tsmpRegModuleList)) {
			TsmpRegModule tsmpRegModule = tsmpRegModuleList.get(0);
			aa0317RegModule = new AA0317RegModule();
			aa0317RegModule.setRegModuleId(tsmpRegModule.getRegModuleId());
			aa0317RegModule.setModuleName(tsmpRegModule.getModuleName());
			aa0317RegModule.setModuleVersion(tsmpRegModule.getModuleVersion());
			aa0317RegModule.setModuleSrc(tsmpRegModule.getModuleSrc());
		}
		regModuleMapping.put(moduleName, aa0317RegModule);
	}
	
	protected String getAppId(String applicationStr) {
		if (StringUtils.isEmpty(applicationStr)) return null;
		try {
			// ex: {"id": "123456", "nodes": [...], ...}
			Map<String, Object> application = getObjectMapper().readValue(applicationStr, //
				new TypeReference<Map<String, Object>>(){});
			return (String) application.get("id");
		} catch (Exception e) {
			this.logger.debug("Cannot parse [application] as Map<String, Object>: " + applicationStr);
		}
		return null;
	}


	protected List<AA0317Module> integrate(Map<String, List<AA0317RespItem>> moduleList) {
		return integrate(moduleList, null);
	}

	protected List<AA0317Module> integrate(Map<String, List<AA0317RespItem>> moduleList, Map<String, AA0317RegModule> regModuleMapping) {
		List<AA0317Module> rtnList = null;
		if (!CollectionUtils.isEmpty(moduleList)) {
			rtnList = moduleList.entrySet().stream().map((entry) -> {
				String moduleName = entry.getKey();
				List<AA0317RespItem> apiList = entry.getValue();
				
				AA0317Module module = new AA0317Module();
				module.setModuleName(moduleName);
				module.setApiList(apiList);
				if (!CollectionUtils.isEmpty(regModuleMapping)) {
					AA0317RegModule regModule = regModuleMapping.get(moduleName);
					module.setRegModule(regModule);
				}
				return module;
			}).collect(Collectors.toList());
		}
		return rtnList;
	}
	
	private void queryAndAddNodesData(HashMap<String, ApiData> composerApiDataHM, List<List<String>> moduleNameAndApiNameList){

		List<List<String>>  request = moduleNameAndApiNameList;

		request.forEach(l->{
			String moduleName = l.get(0);
			String apiName= l.get(1);
			DgrComposerFlow resp = getDgrComposerFlowDao().findByModuleNameAndApiId(moduleName,apiName ).orElse(null);
			if (resp!=null) {
				String flowData = resp.getFlowDataAsString();
				String key = moduleName + apiName;
				if (composerApiDataHM.containsKey(key)) {
					ApiData apiData = composerApiDataHM.get(key);
					AA0317RespItem respItem = apiData.getRespItem();
						respItem.setFlow(flowData);
				}
			}
		});
	}
	
	// ex: "exportAPI_2020-11-11 10-04-31.json"
	protected String getFileName() {
		Date now = DateTimeUtil.now();
		String dateTime = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(now);
		return String.format("exportAPI_%s.json", dateTime);
	}
	protected DgrComposerFlowDao getDgrComposerFlowDao() {
		return dgrComposerFlowDao;
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
	
	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}
	
	protected TsmpApiRegDao getTsmpApiRegDao() {
		return this.tsmpApiRegDao;
	}

	protected TsmpRegModuleDao getTsmpRegModuleDao() {
		return this.tsmpRegModuleDao;
	}

	protected ComposerService getComposerService() {
		return this.composerService;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

}