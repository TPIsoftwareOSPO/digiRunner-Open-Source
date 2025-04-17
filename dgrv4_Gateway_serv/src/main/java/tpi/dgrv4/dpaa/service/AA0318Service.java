package tpi.dgrv4.dpaa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.*;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.component.cache.proxy.TsmpRtnCodeCacheProxy;
import tpi.dgrv4.entity.entity.*;
import tpi.dgrv4.entity.entity.jpql.TsmpApiImp;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiImpDao;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.util.*;
import java.util.function.Function;

@Service
public class AA0318Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private TsmpRtnCodeCacheProxy tsmpRtnCodeCacheProxy;
	private TsmpDpFileDao tsmpDpFileDao;
	private TsmpApiImpDao tsmpApiImpDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpApiRegDao tsmpApiRegDao;
	private FileHelper fileHelper;
	private ObjectMapper objectMapper;

	@Autowired
	public AA0318Service(TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpRtnCodeCacheProxy tsmpRtnCodeCacheProxy,
			TsmpDpFileDao tsmpDpFileDao, TsmpApiImpDao tsmpApiImpDao, TsmpApiDao tsmpApiDao,
			TsmpApiRegDao tsmpApiRegDao, FileHelper fileHelper, ObjectMapper objectMapper) {
		super();
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.tsmpRtnCodeCacheProxy = tsmpRtnCodeCacheProxy;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.tsmpApiImpDao = tsmpApiImpDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiRegDao = tsmpApiRegDao;
		this.fileHelper = fileHelper;
		this.objectMapper = objectMapper;
	}

	public AA0318Resp uploadRegCompAPIs(TsmpAuthorization auth, AA0318Req req, final String locale) {
		String userName = auth.getUserName();
		String orgId = auth.getOrgId();
		AA0317Data aa0317Data = checkParams(userName, orgId, req);
		
		AA0318Resp resp = new AA0318Resp();
		try {
			String fileName = req.getFileName();
			uploadAPIList(resp, aa0317Data.getR(), fileName, userName, locale);	// 註冊
			uploadAPIList(resp, aa0317Data.getC(), fileName, userName, locale);	// 組合
			
			String tempFileName = req.getTempFileName();
			saveAPIFile(userName, resp.getBatchNo(), tempFileName);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.debug(String.format("Upload Reg/Comp API error: %s", StackTraceUtil.logStackTrace(e)));
			throw TsmpDpAaRtnCode._1203.throwing();
		}
		return resp;
	}
	
	/**
	 * for In-Memory, <br>
	 * GTW(In-Memory) 端匯入資料,由此進入
	 */
	public AA0318Resp uploadRegCompAPIsForInMemoryInput(String userName, String orgId, AA0317Data aa0317Data) {
		String locale = "en-US";// 固定用英文
		AA0318Resp resp = new AA0318Resp();
		try {
			String fileName = System.currentTimeMillis() + "";
			uploadAPIList(resp, aa0317Data.getR(), fileName, userName, locale); // 註冊
			uploadAPIList(resp, aa0317Data.getC(), fileName, userName, locale); // 組合

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.debug(String.format("Upload Reg/Comp API error: %s", StackTraceUtil.logStackTrace(e)));
			throw TsmpDpAaRtnCode._1203.throwing();
		}
		return resp;
	}

	protected AA0317Data checkParams(String userName, String orgId, AA0318Req req) {
		if (StringUtils.isEmpty(userName)) {
			throw TsmpDpAaRtnCode._1258.throwing();
		}

		if (StringUtils.isEmpty(orgId)) {
			throw TsmpDpAaRtnCode._1273.throwing();
		}

		// 沒有傳入暫存檔名
		String tempFileName = req.getTempFileName();
		if (StringUtils.isEmpty(tempFileName)) {
			throw TsmpDpAaRtnCode.NO_INCLUDING_FILE.throwing();
		}

		// 暫存檔案不存在
		String refFileCateCode = TsmpDpFileType.TEMP.value();
		Long refId = -1L;
		List<TsmpDpFile> tsmpDpFileList = getTsmpDpFileDao().findByRefFileCateCodeAndRefIdAndFileName( //
			refFileCateCode, refId, tempFileName);
		if (CollectionUtils.isEmpty(tsmpDpFileList)) {
			throw TsmpDpAaRtnCode.NO_FILE.throwing();
		}

		// 副檔名不是  .JSON
		String fileName = getFileHelper().restoreOrginalFilename(tempFileName);
		String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
		if (!"json".equalsIgnoreCase(extension)) {
			throw TsmpDpAaRtnCode._1443.throwing();
		}
		req.setFileName(fileName);	// 之後會用到

		// 暫存檔案內容為空
		byte[] fileContent = null;
		try {
			fileContent = getFileHelper().download( tsmpDpFileList.get(0) );
		} catch (Exception e) {
			this.logger.debug(String.format("File download error: %s", StackTraceUtil.logStackTrace(e)));
		}
		if (fileContent == null || fileContent.length == 0) {
			throw TsmpDpAaRtnCode._1233.throwing();
		}
		
		// JSON 轉成物件
		AA0317Data aa0317Data = null;
		try {
			aa0317Data = convertToAA0317Data(fileContent);
		} catch (Exception e) {
			this.logger.debug(String.format("Cannot convert json to AA0317Data: %s", StackTraceUtil.logStackTrace(e)));
		}
		if (aa0317Data == null) {
			throw TsmpDpAaRtnCode._1291.throwing();
		}

		// 註冊或組合API清單不得為空
		boolean isREmpty = isApiListEmpty(aa0317Data.getR());
		boolean isCEmpty = isApiListEmpty(aa0317Data.getC());
		if (isREmpty && isCEmpty) {
			throw TsmpDpAaRtnCode._1445.throwing();
		}
		
		return aa0317Data;
	}

	protected AA0317Data convertToAA0317Data(byte[] fileContent) throws Exception {
		JsonNode root = getObjectMapper().readTree(fileContent);
		if (root == null || root.isNull() || !root.isObject()) {
			return null;
		}
		// 如果成功，只會轉出 AA0317Data，裡面屬性轉不出來
		AA0317Data aa0317Data = getObjectMapper().readValue(fileContent, AA0317Data.class);
		aa0317Data.setR( convertToAA0317ModuleList( getNodeAsArrayNode(root, "r") ) );
		aa0317Data.setC( convertToAA0317ModuleList( getNodeAsArrayNode(root, "c") ) );
		return aa0317Data;
	}

	protected List<AA0317Module> convertToAA0317ModuleList(ArrayNode moduleList) {
		if (moduleList == null) return null;

		List<AA0317Module> aa0317ModuleList = new ArrayList<>();
		moduleList.forEach((module) -> {
			AA0317Module aa0317Module = convertToAA0317Module(module);
			if (aa0317Module != null) {
				aa0317ModuleList.add(aa0317Module);
			}
		});
		return aa0317ModuleList;
	}

	protected AA0317Module convertToAA0317Module(JsonNode node) {
		if (node == null || node.isNull() || !node.isObject()) {
			return null;
		}

		AA0317Module aa0317Module = new AA0317Module();
		aa0317Module.setModuleName( getNodeAsText(node, "moduleName") );
		aa0317Module.setRegModule( convertToAA0317RegModule(node.get("regModule")) );
		ArrayNode apiList = getNodeAsArrayNode(node, "apiList");
		if (apiList != null) {
			List<AA0317RespItem> aa0317RespItemList = new ArrayList<>();
			apiList.forEach((api) -> {
				AA0317RespItem respItem = convertToAA0317RespItem(api);
				if (respItem != null) {
					aa0317RespItemList.add(respItem);
				}
			});
			aa0317Module.setApiList(aa0317RespItemList);
		}
		return aa0317Module;
	}

	protected AA0317RegModule convertToAA0317RegModule(JsonNode node) {
		if (node == null || node.isNull()) {
			return null;
		}

		AA0317RegModule aa0317RegModule = new AA0317RegModule();
		aa0317RegModule.setRegModuleId( getNodeAsLong(node, "regModuleId") );
		aa0317RegModule.setModuleName( getNodeAsText(node, "moduleName") );
		aa0317RegModule.setModuleVersion( getNodeAsText(node, "moduleVersion") );
		aa0317RegModule.setModuleSrc( getNodeAsText(node, "moduleSrc") );
		return aa0317RegModule;
	}

	protected AA0317RespItem convertToAA0317RespItem(JsonNode node) {
		if (node == null || node.isNull() || !node.isObject()) {
			return null;
		}
		
		AA0317RespItem aa0317RespItem = new AA0317RespItem();
		aa0317RespItem.setApiKey( getNodeAsText(node, "apiKey") );
		aa0317RespItem.setModuleName( getNodeAsText(node, "moduleName") );
		aa0317RespItem.setApiName( getNodeAsText(node, "apiName") );
		aa0317RespItem.setApiDesc( getNodeAsText(node, "apiDesc") );
		aa0317RespItem.setApiOwner( getNodeAsText(node, "apiOwner") );
		aa0317RespItem.setUrlRID( getNodeAsText(node, "urlRID") );
		aa0317RespItem.setApiSrc( getNodeAsText(node, "apiSrc") );
		aa0317RespItem.setSrcURL( getNodeAsText(node, "srcURL") );
		aa0317RespItem.setApiUUID( getNodeAsText(node, "apiUUID") );
		aa0317RespItem.setContentType( getNodeAsText(node, "contentType") );
		aa0317RespItem.setEnpoint( getNodeAsText(node, "enpoint") );
		aa0317RespItem.setHttpHeader( getNodeAsText(node, "httpHeader") );
		aa0317RespItem.setHttpMethod( getNodeAsText(node, "httpMethod") );
		aa0317RespItem.setParams( getNodeAsText(node, "params") );
		aa0317RespItem.setProduce( getNodeAsText(node, "produce") );
//		ArrayNode flowNode = getNodeAsArrayNode(node, "flow");
//		List<String> flow = null;
//		if (flowNode != null) {
//			flow = new ArrayList<>();
//			Iterator<JsonNode> iterator = flowNode.iterator();
//			while(iterator.hasNext()) {
//				flow.add(iterator.next().asText());
//			}
//		}
		aa0317RespItem.setFlow(getNodeAsText(node, "flow") );
		aa0317RespItem.setNo_oauth( getNodeAsText(node, "no_oauth") );
		aa0317RespItem.setJweFlag( getNodeAsText(node, "jweFlag") );
		aa0317RespItem.setJweFlagResp( getNodeAsText(node, "jweFlagResp") );
		aa0317RespItem.setFunFlag( getNodeAsInt(node, "funFlag", 0) );	// 如果是 null 就轉成 0
		aa0317RespItem.setMockBody( getNodeAsText(node, "mockBody") );
		aa0317RespItem.setMockHeaders( getNodeAsText(node, "mockHeaders") );
		aa0317RespItem.setMockStatusCode( getNodeAsText(node, "mockStatusCode") );
		aa0317RespItem.setApiCacheFlag( getNodeAsText(node, "apiCacheFlag") );
		aa0317RespItem.setFixedCacheTime( getNodeAsInt(node, "fixedCacheTime", 0) );
		aa0317RespItem.setIsRedirectByIp(getNodeAsText(node, "isRedirectByIp"));
		aa0317RespItem.setApiStatus(getNodeAsText(node, "apiStatus"));

		ArrayNode redirectByIpDataListNode = getNodeAsArrayNode(node, "redirectByIpDataList");

		List<AA0317RedirectByIpData> redirectByIpDataList = new ArrayList<>();
		if (redirectByIpDataListNode != null) {

			redirectByIpDataListNode.forEach(n -> {
				AA0317RedirectByIpData data = new AA0317RedirectByIpData();
				data.setIpForRedirect(getNodeAsText(n, "ipForRedirect"));
				data.setIpSrcUrl(getNodeAsText(n, "ipSrcUrl"));
				redirectByIpDataList.add(data);

			});
		}
		aa0317RespItem.setRedirectByIpDataList(redirectByIpDataList);
		aa0317RespItem.setHeaderMaskKey(getNodeAsText(node, "headerMaskKey"));
		String haderMaskPolicy = getNodeAsText(node, "headerMaskPolicy");

		aa0317RespItem.setHeaderMaskPolicy(StringUtils.hasLength(haderMaskPolicy) ? haderMaskPolicy : "0");
		aa0317RespItem.setHeaderMaskPolicyNum(getNodeAsInt(node, "headerMaskPolicyNum", 0));
		aa0317RespItem.setHeaderMaskPolicySymbol(getNodeAsText(node, "headerMaskPolicySymbol"));

		aa0317RespItem.setBodyMaskKeyword(getNodeAsText(node, "bodyMaskKeyword"));
		String bodyMaskPolicy = getNodeAsText(node, "bodyMaskPolicy");
		aa0317RespItem.setBodyMaskPolicy(StringUtils.hasLength(bodyMaskPolicy) ? bodyMaskPolicy : "0");
		aa0317RespItem.setBodyMaskPolicyNum(getNodeAsInt(node, "bodyMaskPolicyNum", 0));
		aa0317RespItem.setBodyMaskPolicySymbol(getNodeAsText(node, "bodyMaskPolicySymbol"));
	
		aa0317RespItem.setFailDiscoveryPolicy(getNodeAsText(node, "failDiscoveryPolicy"));
		aa0317RespItem.setFailHandlePolicy(getNodeAsText(node, "failHandlePolicy"));
		
		ArrayNode labelListNode = getNodeAsArrayNode(node, "labelList");
		List<String> labelList = new ArrayList<>();
		if (labelListNode != null) {
			labelListNode.forEach(n -> labelList.add(n.asText()));
		}
		aa0317RespItem.setLabelList(labelList);
		
		aa0317RespItem.setPublicFlag(getNodeAsText(node, "publicFlag"));
		aa0317RespItem.setApiReleaseTime(getNodeAsText(node, "apiReleaseTime"));
		aa0317RespItem.setScheduledLaunchDate(getNodeAsText(node, "scheduledLaunchDate"));
		aa0317RespItem.setScheduledRemovalDate(getNodeAsText(node, "scheduledRemovalDate"));
		aa0317RespItem.setEnableScheduledDate(getNodeAsText(node, "enableScheduledDate"));
		aa0317RespItem.setDisableScheduledDate(getNodeAsText(node, "disableScheduledDate"));
		return aa0317RespItem;
	}

	protected boolean isApiListEmpty(List<AA0317Module> mList) {
		if (CollectionUtils.isEmpty(mList)) {
			return true;
		}
		
		boolean hasApiList = false;
		for (AA0317Module m : mList) {
			if (!CollectionUtils.isEmpty(m.getApiList())) {
				hasApiList = true;
				break;
			}
		}
		
		return !hasApiList;
	}

	protected void uploadAPIList(AA0318Resp resp, List<AA0317Module> mList, String fileName //
			, String userName, String locale) {
		if (CollectionUtils.isEmpty(mList)) {
			return;
		}

		List<AA0318Item> aa0318ItemList = null;
		
		List<AA0317RespItem> apiList = null;
		TsmpApiImp tsmpApiImp = null;
		AA0318Item aa0318Item = null;
		for (AA0317Module m : mList) {
			apiList = m.getApiList();
			if (CollectionUtils.isEmpty(apiList)) {
				continue;
			}
			
			for (AA0317RespItem api : apiList) {
				// 將 AA0317RespItem 轉換成 TSMP_API_IMP Entity
				try {
					tsmpApiImp = convertToTsmpApiImp(api, fileName, userName);
				} catch (Exception e) {
					this.logger.debug(String.format("AA0317RespItem convert to TsmpApiImp error: %s", StackTraceUtil.logStackTrace(e)));
					throw TsmpDpAaRtnCode._1203.throwing();
				}

				try {
					// 寫入批號 (第一次寫入 TsmpApiImp 時才取得批號)
					if (resp.getBatchNo() == null) {
						resp.setBatchNo( getBatchNo() );
					}
					tsmpApiImp.setBatchNo( resp.getBatchNo() );

					// 檢查 Entity
					tsmpApiImp = checkTsmpApiImp(tsmpApiImp, locale);

					// 寫入 TsmpApiImp
					tsmpApiImp = getTsmpApiImpDao().save(tsmpApiImp);
				} catch (Exception e) {
					TPILogger.tl.error(StackTraceUtil.logTpiShortStackTrace(e)); //為顯示 postgres 在 TPI 程式中的哪一行抛出
					TPILogger.tl.error(String.format("Check TsmpApiImp error: %s", StackTraceUtil.logStackTrace(e)));
					tsmpApiImp.setCheckAct("N");
					tsmpApiImp.setMemo(e.getMessage());
				}

				// 順便帶出要回傳的東西
				aa0318Item = convertToAA0318Item(tsmpApiImp, locale);
				if (aa0318ItemList == null) {
					aa0318ItemList = (resp.getApiList() == null ? new ArrayList<>() : resp.getApiList());
				}
				aa0318ItemList.add(aa0318Item);
			}
		}
		
		resp.setApiList(aa0318ItemList);
	}

	// 找出 TSMP_API_IMP 中最大的 batch_no，將其 + 1 作為此次上傳 API 的批號，若 TSMP_API_IMP 無資料，則批號預設為 1000。
	protected Integer getBatchNo() {
		Integer maxBatchNo = getTsmpApiImpDao().queryMaxBatchNo();
		if (maxBatchNo == null) {
			maxBatchNo = Integer.valueOf(1000);
		} else {
			maxBatchNo = maxBatchNo.intValue() + 1;
		}
		this.logger.debug(String.format("Get batch no = %d", maxBatchNo));
		return maxBatchNo;
	}

	/**
	 * 將 AA0317RespItem 轉換成 TSMP_API_IMP Entity
	 */
	protected TsmpApiImp convertToTsmpApiImp(AA0317RespItem api, String filename, String userName) throws Exception {
		TsmpApiImp i = new TsmpApiImp();
		i.setApiKey(api.getApiKey());
		i.setModuleName(api.getModuleName());
		i.setRecordType("I");	// Import
		i.setBatchNo(null);	// 之後再填入
		i.setFilename(filename);	// 原始檔名，非暫存檔名
		i.setApiName(api.getApiName());
		i.setApiDesc(api.getApiDesc());
		i.setApiOwner(api.getApiOwner());
		i.setUrlRid(api.getUrlRID());
		i.setApiSrc(api.getApiSrc());
		i.setSrcUrl(api.getSrcURL());
		i.setApiUuid(api.getApiUUID());
		i.setPathOfJson(api.getEnpoint());
		i.setMethodOfJson(api.getHttpMethod());
		i.setParamsOfJson(api.getParams());
		i.setHeadersOfJson(api.getHttpHeader());
		i.setConsumesOfJson(api.getContentType());
		i.setProducesOfJson(api.getProduce());
		String flow = api.getFlow();
		if (flow != null) {
			i.setFlow(flow);
		}
		i.setCreateTime(DateTimeUtil.now());
		i.setCreateUser(userName);
		i.setCheckAct("N");	// Not available, 等執行 checkTsmpApiImp() 再修改
		i.setResult("I");	// Initial
		i.setMemo("");	// 等執行 checkTsmpApiImp() 再修改
		i.setNoOauth(api.getNo_oauth());
		i.setJweFlag(api.getJweFlag());
		i.setJweFlagResp(api.getJweFlagResp());
		i.setFunFlag(api.getFunFlag());
		i.setMockBody(api.getMockBody());
		i.setMockHeaders(api.getMockHeaders());
		i.setMockStatusCode(api.getMockStatusCode());
		i.setApiCacheFlag(StringUtils.hasLength(api.getApiCacheFlag()) ? api.getApiCacheFlag() : "1");
		i.setFixedCacheTime(api.getFixedCacheTime());
		i.setRedirectByIp(api.getIsRedirectByIp());
		i.setApiStatus(StringUtils.hasLength(api.getApiStatus()) ? api.getApiStatus() : "2");
		i.setPublicFlag(api.getPublicFlag());
		i.setApiReleaseTime(null != api.getApiReleaseTime() ? new Date(Long.parseLong(api.getApiReleaseTime())) : null);
		i.setScheduledLaunchDate(null != api.getScheduledLaunchDate() ? Long.parseLong(api.getScheduledLaunchDate()) : null);
		i.setScheduledRemovalDate(null != api.getScheduledRemovalDate() ? Long.parseLong(api.getScheduledRemovalDate()) : null);
		i.setEnableScheduledDate(null != api.getEnableScheduledDate() ? Long.parseLong(api.getEnableScheduledDate()) : null);
		i.setDisableScheduledDate(null != api.getDisableScheduledDate() ? Long.parseLong(api.getDisableScheduledDate()) : null);
		
		List<AA0317RedirectByIpData> redirectByIpDataList = api.getRedirectByIpDataList();
		if (redirectByIpDataList.size() > 0) {

			AA0317RedirectByIpData ipList1 = redirectByIpDataList.get(0);
			if (ipList1 != null) {
				i.setIpForRedirect1(ipList1.getIpForRedirect());
				i.setIpSrcUrl1(ipList1.getIpSrcUrl());
			}
			if (redirectByIpDataList.size() > 1) {
				AA0317RedirectByIpData ipList2 = redirectByIpDataList.get(1);
				if (ipList2 != null) {
					i.setIpForRedirect2(ipList2.getIpForRedirect());
					i.setIpSrcUrl2(ipList2.getIpSrcUrl());
				}
			}
			if (redirectByIpDataList.size() > 2) {
				AA0317RedirectByIpData ipList3 = redirectByIpDataList.get(2);
				if (ipList3 != null) {
					i.setIpForRedirect3(ipList3.getIpForRedirect());
					i.setIpSrcUrl3(ipList3.getIpSrcUrl());
				}
			}
			if (redirectByIpDataList.size() > 3) {
				AA0317RedirectByIpData ipList4 = redirectByIpDataList.get(3);
				if (ipList4 != null) {
					i.setIpForRedirect4(ipList4.getIpForRedirect());
					i.setIpSrcUrl4(ipList4.getIpSrcUrl());
				}
			}
			if (redirectByIpDataList.size() > 4) {
				AA0317RedirectByIpData ipList5 = redirectByIpDataList.get(4);
				if (ipList5 != null) {
					i.setIpForRedirect5(ipList5.getIpForRedirect());
					i.setIpSrcUrl5(ipList5.getIpSrcUrl());
				}
			}
		}
		
		String headerMaskPolicy = api.getHeaderMaskPolicy();
		i.setHeaderMaskPolicy(StringUtils.hasLength(headerMaskPolicy) ? headerMaskPolicy : "0");
		i.setHeaderMaskKey(api.getHeaderMaskKey());
		i.setHeaderMaskPolicyNum(api.getHeaderMaskPolicyNum());
		i.setHeaderMaskPolicySymbol(api.getHeaderMaskPolicySymbol());

		String bodyMaskPolicy = api.getBodyMaskPolicy();
		i.setBodyMaskKeyword(api.getBodyMaskKeyword());
		i.setBodyMaskPolicy(StringUtils.hasLength(bodyMaskPolicy) ? bodyMaskPolicy : "0");
		i.setBodyMaskPolicyNum(api.getBodyMaskPolicyNum());
		i.setBodyMaskPolicySymbol(api.getBodyMaskPolicySymbol());
		
		String failDiscoveryPolicy = api.getFailDiscoveryPolicy();
		i.setFailDiscoveryPolicy(StringUtils.hasLength(failDiscoveryPolicy) ? failDiscoveryPolicy : "0");

		String failHandlePolicy = api.getFailHandlePolicy();
		i.setFailHandlePolicy(StringUtils.hasLength(failHandlePolicy) ? failHandlePolicy : "0");
		
		List<String> labelList = api.getLabelList();
		String[] label = new String[5];

		if (!CollectionUtils.isEmpty(labelList)) {
			for (int j = 0; j < labelList.size(); j++) {
				label[j] = (labelList.get(j).toLowerCase());
			}
		}
		i.setLabel1(label[0]);
		i.setLabel2(label[1]);
		i.setLabel3(label[2]);
		i.setLabel4(label[3]);
		i.setLabel5(label[4]);
		
		return i;
	}

	protected TsmpApiImp checkTsmpApiImp(TsmpApiImp tsmpApiImp, String locale) {
		String apiKey = tsmpApiImp.getApiKey();
		String moduleName = tsmpApiImp.getModuleName();
		TsmpApiId tsmpApiId = new TsmpApiId(apiKey, moduleName);
		TsmpApiRegId tsmpApiRegId = new TsmpApiRegId(apiKey, moduleName);
		Optional<TsmpApi> opt_tsmpApi = getTsmpApiDao().findById(tsmpApiId);
		Optional<TsmpApiReg> opt_tsmpApiReg = getTsmpApiRegDao().findById(tsmpApiRegId);
		if (opt_tsmpApi.isPresent() && opt_tsmpApiReg.isPresent()) {
			tsmpApiImp.setCheckAct("U");
		} else if (!opt_tsmpApi.isPresent() && !opt_tsmpApiReg.isPresent()) {
			tsmpApiImp.setCheckAct("C");
		} else {
			tsmpApiImp.setCheckAct("N");
			String memo = getMessage(TsmpDpAaRtnCode._1481, locale, apiKey, moduleName);
			tsmpApiImp.setMemo(memo);
		}
		return tsmpApiImp;
	}

	protected AA0318Item convertToAA0318Item(TsmpApiImp tsmpApiImp, String locale) {
		AA0318Item item = new AA0318Item();
		item.setApiKey( trunc(tsmpApiImp.getApiKey(), 20) );
		item.setModuleName( trunc(tsmpApiImp.getModuleName(), 15) );
		item.setApiName( trunc(tsmpApiImp.getApiName(), 20) );
		item.setApiSrc( toPair(tsmpApiImp.getApiSrc(), "API_SRC", locale) );
		item.setSrcURL(tsmpApiImp.getSrcUrl());
		item.setEndpoint(tsmpApiImp.getPathOfJson());
		item.setCheckAct( toPair(tsmpApiImp.getCheckAct(), "API_IMP_CHECK_ACT", locale) );
		item.setMemo( trunc(tsmpApiImp.getMemo(), 50) );
		
		Map<String, String> map = new HashMap<>();
		map.put(tsmpApiImp.getIpForRedirect1(), tsmpApiImp.getIpSrcUrl1());
		map.put(tsmpApiImp.getIpForRedirect2(), tsmpApiImp.getIpSrcUrl2());
		map.put(tsmpApiImp.getIpForRedirect3(), tsmpApiImp.getIpSrcUrl3());
		map.put(tsmpApiImp.getIpForRedirect4(), tsmpApiImp.getIpSrcUrl4());
		map.put(tsmpApiImp.getIpForRedirect5(), tsmpApiImp.getIpSrcUrl5());
        map.entrySet().removeIf(entry -> !StringUtils.hasLength(entry.getKey()));

		item.setSrcURLByIpRedirectMap(map );
		
		return item;
	}

	protected void saveAPIFile(String userName, Integer batchNo, String tempFilename) {
		TsmpDpFileType refFileCateCode = TsmpDpFileType.REG_COMP_API;
		long refId = batchNo.longValue();
		boolean isCreate = Boolean.TRUE;
		boolean isUpdate = Boolean.FALSE;
		try {
			getFileHelper().moveTemp(userName, refFileCateCode, refId, tempFilename, isCreate, isUpdate);
		} catch (Exception e) {
			this.logger.debug(String.format("File save error: %s", StackTraceUtil.logStackTrace(e)));
		}
	}

	protected String getMessage(TsmpDpAaRtnCode tsmpDpAaRtnCode, String locale, Object... params) {
		String rtnCode = tsmpDpAaRtnCode.getCode();
		TsmpRtnCode tsmpRtnCode = getTsmpRtnCodeCacheProxy().findById(rtnCode, locale).orElse(null);
		if (tsmpRtnCode != null) {
			if (ObjectUtils.isEmpty(params)) {
				return tsmpRtnCode.getTsmpRtnMsg();
			} else {
				Map<String, String> map = new HashMap<>();
				for (int i = 0; i < params.length; i++) {
					map.put(String.valueOf(i), params[i].toString());
				}
				return MailHelper.buildContent(tsmpRtnCode.getTsmpRtnMsg(), map);
			}
		}
		return tsmpDpAaRtnCode.getDefaultMessage();
	}

	protected AA0318Trunc trunc(String value, int maxLength) {
		AA0318Trunc aa0318Trunc = new AA0318Trunc();
		aa0318Trunc.setT(Boolean.FALSE);
		aa0318Trunc.setVal(value);
		if (!StringUtils.isEmpty(value) && value.length() > maxLength) {
			aa0318Trunc.setT(Boolean.TRUE);
			aa0318Trunc.setOri(value);
			aa0318Trunc.setVal(value.substring(0, maxLength));
		}
		return aa0318Trunc;
	}
	
	private AA0318Pair toPair(String value, String itemNo, String locale) {
		AA0318Pair pair = new AA0318Pair();
		pair.setV(value);
		pair.setN(value);
		// 找出對應的 Name
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, value, locale);
		TsmpDpItems items = getTsmpDpItemsCacheProxy().findById(id);
		if (items != null) {
			pair.setN(items.getSubitemName());
		}
		return pair;
	}

	private String getNodeAsText(JsonNode node, String fieldName) {
		return getNodeAsValue(node, fieldName, (n) -> {
			return n.asText();
		});
	}

	private Long getNodeAsLong(JsonNode node, String fieldName) {
		return getNodeAsValue(node, fieldName, (n) -> {
			return n.asLong();
		});
	}

	private int getNodeAsInt(JsonNode node, String fieldName, int defaultVal) {
		JsonNode n = node.get(fieldName);
		if (n == null || n.isNull()) {
			return defaultVal;
		}
		return n.asInt();
	}

	private ArrayNode getNodeAsArrayNode(JsonNode node, String fieldName) {
		return getNodeAsValue(node, fieldName, (n) -> {
			return (ArrayNode) n;
		});
	}

	private <R> R getNodeAsValue(JsonNode node, String fieldName, Function<JsonNode, R> func) {
		JsonNode n = node.get(fieldName);
		if (n == null || n.isNull()) {
			return null;
		}
		return func.apply(n);
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected TsmpRtnCodeCacheProxy getTsmpRtnCodeCacheProxy() {
		return this.tsmpRtnCodeCacheProxy;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected TsmpApiImpDao getTsmpApiImpDao() {
		return this.tsmpApiImpDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpApiRegDao getTsmpApiRegDao() {
		return this.tsmpApiRegDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

}