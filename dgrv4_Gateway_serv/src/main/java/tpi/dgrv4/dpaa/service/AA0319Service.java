package tpi.dgrv4.dpaa.service;

import java.util.AbstractMap.SimpleEntry;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.job.NoticeClearCacheEventsJob;
import tpi.dgrv4.dpaa.constant.TsmpApiSrc;
import tpi.dgrv4.dpaa.service.composer.ComposerService;
import tpi.dgrv4.dpaa.vo.AA0319Pair;
import tpi.dgrv4.dpaa.vo.AA0319Req;
import tpi.dgrv4.dpaa.vo.AA0319ReqItem;
import tpi.dgrv4.dpaa.vo.AA0319Resp;
import tpi.dgrv4.dpaa.vo.AA0319RespItem;
import tpi.dgrv4.dpaa.vo.AA0319Trunc;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.component.cache.proxy.TsmpRtnCodeCacheProxy;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpApiRegId;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.entity.jpql.DgrComposerFlow;
import tpi.dgrv4.entity.entity.jpql.TsmpApiImp;
import tpi.dgrv4.entity.entity.jpql.TsmpApiImpId;
import tpi.dgrv4.entity.entity.jpql.TsmpRegModule;
import tpi.dgrv4.entity.repository.DgrAuditLogDDao;
import tpi.dgrv4.entity.repository.DgrAuditLogMDao;
import tpi.dgrv4.entity.repository.DgrComposerFlowDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiImpDao;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpRegModuleDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.TCP.Packet.UpdateComposerTSPacket;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.DigiRunnerGtwDeployProperties;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0319Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpApiDao tsmpApiDao;
	private TsmpApiRegDao tsmpApiRegDao;
	private TsmpApiImpDao tsmpApiImpDao;
	private TsmpRegModuleDao tsmpRegModuleDao;
	private TsmpRtnCodeCacheProxy tsmpRtnCodeCacheProxy;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private ApplicationContext ctx;
	private JobHelper jobHelper;
	private ObjectMapper objectMapper;
	private ComposerService composerService;
	private DgrAuditLogService dgrAuditLogService;
	private DgrAuditLogMDao dgrAuditLogMDao;
	private DgrAuditLogDDao dgrAuditLogDDao;
	private DaoGenericCacheService daoGenericCacheService;
	private DgrComposerFlowDao dgrComposerFlowDao;
	private DigiRunnerGtwDeployProperties digiRunnerGtwDeployProperties;
	
	@Autowired
	public AA0319Service(TsmpApiDao tsmpApiDao, TsmpApiRegDao tsmpApiRegDao, TsmpApiImpDao tsmpApiImpDao,
			TsmpRegModuleDao tsmpRegModuleDao, TsmpRtnCodeCacheProxy tsmpRtnCodeCacheProxy,
			TsmpOrganizationDao tsmpOrganizationDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy,
			ApplicationContext ctx, JobHelper jobHelper, ObjectMapper objectMapper, ComposerService composerService,
			DgrAuditLogService dgrAuditLogService, DgrAuditLogMDao dgrAuditLogMDao, DgrAuditLogDDao dgrAuditLogDDao,
			DaoGenericCacheService daoGenericCacheService, DgrComposerFlowDao dgrComposerFlowDao,
			DigiRunnerGtwDeployProperties digiRunnerGtwDeployProperties) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiRegDao = tsmpApiRegDao;
		this.tsmpApiImpDao = tsmpApiImpDao;
		this.tsmpRegModuleDao = tsmpRegModuleDao;
		this.tsmpRtnCodeCacheProxy = tsmpRtnCodeCacheProxy;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.ctx = ctx;
		this.jobHelper = jobHelper;
		this.objectMapper = objectMapper;
		this.composerService = composerService;
		this.dgrAuditLogService = dgrAuditLogService;
		this.dgrAuditLogMDao = dgrAuditLogMDao;
		this.dgrAuditLogDDao = dgrAuditLogDDao;
		this.daoGenericCacheService = daoGenericCacheService;
		this.dgrComposerFlowDao = dgrComposerFlowDao;
		this.digiRunnerGtwDeployProperties = digiRunnerGtwDeployProperties;
	}

	@Transactional
	public AA0319Resp importRegCompAPIs(TsmpAuthorization auth, AA0319Req req, String locale, InnerInvokeParam iip) {
		String userName = auth.getUserName();
		String orgId = auth.getOrgId();
		List<String> userOrgIdList = new ArrayList<>();
		List<TsmpApiImp> tsmpApiImpList = checkParams(userName, orgId, req, userOrgIdList);

		AA0319Resp resp = new AA0319Resp();

		Integer batchNo = req.getBatchNo();
		AA0319RespItem respItem = null;
		int uuidIndex = 1;

		String uuid = iip != null ? iip.getTxnUid() : null;

		HashMap<String, TsmpApiImp> nodeDataHM = new HashMap<String, TsmpApiImp>();
		HashMap<String, AA0319RespItem> respItemHM = new HashMap<String, AA0319RespItem>();

		for (TsmpApiImp tsmpApiImp : tsmpApiImpList) {
			if (iip != null) {
				iip.setTxnUid(uuid + "_" + uuidIndex);
				uuidIndex++;
			}
			// 寫入 Audit Log M
			if ("C".equals(tsmpApiImp.getCheckAct())) {
				String lineNumber = StackTraceUtil.getLineNumber();
				if (TsmpApiSrc.REGISTERED.value().equals(tsmpApiImp.getApiSrc())) {
					getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.ADD_REGISTER_API.value());
				} else if (TsmpApiSrc.COMPOSED.value().equals(tsmpApiImp.getApiSrc())) {
					getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.ADD_COMPOSER_API.value());
				}

			} else if ("U".equals(tsmpApiImp.getCheckAct())) {
				String lineNumber = StackTraceUtil.getLineNumber();
				if (TsmpApiSrc.REGISTERED.value().equals(tsmpApiImp.getApiSrc())) {
					getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_REGISTER_API.value());
				} else if (TsmpApiSrc.COMPOSED.value().equals(tsmpApiImp.getApiSrc())) {
					getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_COMPOSER_API.value());
				}
			}

			// 從 TsmpApiImp 匯入到 TsmpApi, TsmpApiReg
			respItem = doImport(auth, batchNo, tsmpApiImp, locale, userOrgIdList, iip);

			// 將匯入結果帶回 Response
			if (resp.getApiList() == null) {
				resp.setApiList(new ArrayList<>());
			}
			resp.getApiList().add(respItem);

			// 收集全部的moduleName與ApiName
			String flow = tsmpApiImp.getFlow();
			if (StringUtils.hasText(flow)) {
				String apiKey = tsmpApiImp.getApiKey();
				String moduleName = tsmpApiImp.getModuleName();
				String key = moduleName + apiKey;
				this.logger.debug(String.format("flow data: apiKey=%s, moduleName=%s", apiKey, moduleName));				
				nodeDataHM.put(key, tsmpApiImp);
				respItemHM.put(key, respItem);
			}
		}

		// 一次request api跟Composer後端新增資料。
		if (nodeDataHM.isEmpty() == false) {
			doImportAllComposerNodes(auth, batchNo, nodeDataHM, locale, userOrgIdList, iip, respItemHM);
		}

		clearAPICache();

		// 判斷是否為記憶體角色
		boolean isMemoryRole = getDigiRunnerGtwDeployProperties().isMemoryRole();
		if (isMemoryRole) {
			removeNonExistentData(tsmpApiImpList);
		}

		// in-memory, 用列舉的值傳入值
		TPILogger.updateTime4InMemory(DgrDataType.API.value());
		return resp;
	}

	/**
	 * 移除不存在的數據。
	 * <p>
	 * 這個方法用於過濾出存在的數據，並將其從相關的資料庫表中刪除。
	 * </p>
	 *
	 * @param tsmpApiImpList 提供一個包含 TsmpApiImp 實例的列表，從中提取存在的數據
	 */
	private void removeNonExistentData(List<TsmpApiImp> tsmpApiImpList) {
		
		if (CollectionUtils.isEmpty(tsmpApiImpList)) {
			getTsmpApiDao().deleteAll();
			getTsmpApiRegDao().deleteAll();
			return;
		}

		// 將 TsmpApiImp 列表轉換為包含 apiKey 和 moduleName 的 SimpleEntry 列表
		List<SimpleEntry<String, String>> existentData = tsmpApiImpList.stream()
				.map(imp -> new SimpleEntry<>(imp.getApiKey(), imp.getModuleName())).collect(Collectors.toList());

		// 從 TsmpApi 表中刪除不存在的數據
		getTsmpApiDao().deleteNonSpecifiedContent(existentData);
		// 從 TsmpApiReg 表中刪除不存在的數據
		getTsmpApiRegDao().deleteNonSpecifiedContent(existentData);
	}

	protected List<TsmpApiImp> checkParams(String userName, String orgId, AA0319Req req, List<String> userOrgIdList) {
		boolean isMemoryRole = getDigiRunnerGtwDeployProperties().isMemoryRole();
		
		if (StringUtils.isEmpty(userName)) {
			throw TsmpDpAaRtnCode._1258.throwing();
		}

		if (StringUtils.isEmpty(orgId)) {
			throw TsmpDpAaRtnCode._1273.throwing();
		}

		List<AA0319ReqItem> reqItemList = req.getApiList();
		if (CollectionUtils.isEmpty(reqItemList) && !isMemoryRole) {
			throw TsmpDpAaRtnCode._2009.throwing("1");
		}

		Integer batchNo = req.getBatchNo();
		if (!CollectionUtils.isEmpty(reqItemList)) {
			if (batchNo == null) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}

			// batchNo 必須存在
			long impCnt = getTsmpApiImpDao().countByBatchNo(batchNo);
			if (impCnt < 1) {
				throw TsmpDpAaRtnCode._1354.throwing("batchNo", String.valueOf(batchNo));
			}
		}

		// 取出組織原則
		userOrgIdList.addAll(checkOrgExists(orgId));

		List<TsmpApiImp> tsmpApiImpList = new ArrayList<>();

		if (CollectionUtils.isEmpty(reqItemList) && isMemoryRole) {
			return tsmpApiImpList;
		}
		
		// moduleName, apiKey 皆不得為空
		final String recordType = "I";
		for (AA0319ReqItem reqItem : reqItemList) {
			String apiKey = reqItem.getApiKey();
			String moduleName = reqItem.getModuleName();

			if (StringUtils.isEmpty(apiKey) || StringUtils.isEmpty(moduleName)) {
				throw TsmpDpAaRtnCode._1469.throwing();
			}

			// 檢查 TSMP_API_IMP 是否存在
			TsmpApiImpId id = new TsmpApiImpId(apiKey, moduleName, recordType, batchNo);
			Optional<TsmpApiImp> opt = getTsmpApiImpDao().findById(id);
			if (!opt.isPresent()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			tsmpApiImpList.add(opt.get());
		}

		return tsmpApiImpList;
	}

	protected List<String> checkOrgExists(String userOrgId) {
		List<String> userOrgIdList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(userOrgId, null);
		if (CollectionUtils.isEmpty(userOrgIdList)) {
			throw TsmpDpAaRtnCode._1222.throwing();
		}
		return userOrgIdList;
	}

	protected AA0319RespItem doImport(TsmpAuthorization auth, Integer batchNo, TsmpApiImp tsmpApiImp, String locale,
			List<String> userOrgIdList, InnerInvokeParam iip) {
		String apiKey = tsmpApiImp.getApiKey();
		String moduleName = tsmpApiImp.getModuleName();

		AA0319RespItem respItem = new AA0319RespItem();
		respItem.setApiKey(apiKey);
		respItem.setModuleName(moduleName);
		respItem.setResult(toSuccess(locale)); // 預設匯入結果為"成功"，匯入過程中有誤再改 Result

		// 檢查要匯入的這支API，該模組是否與上傳OpenAPI文件所匯入的模組相同
		String apiSrc = tsmpApiImp.getApiSrc();
		boolean isInRegModule = checkRegModule(apiSrc, moduleName);
		if (isInRegModule) {
			return returnWithResult(respItem, tsmpApiImp, TsmpDpAaRtnCode._1426.getCode(), locale);
		}

		// 備份原有資料
		String rtnCode = doBackup(batchNo, tsmpApiImp, locale, userOrgIdList);
		if (!StringUtils.isEmpty(rtnCode)) {
			return returnWithResult(respItem, tsmpApiImp, rtnCode, locale);
		}

		// 依據 checkAct 決定更新或新增 API 資料
		Map<String, String> rtnParams = executeByCheckAct(auth, batchNo, tsmpApiImp, locale, iip);
		if (!CollectionUtils.isEmpty(rtnParams)) {
			return returnWithResult(respItem, tsmpApiImp, TsmpDpAaRtnCode._1452.getCode(), locale, rtnParams);
		}

		/*
		 * 因為用迴圈方式查資料，效率不好。改要一次方式查資料。
		 * 
		 * // Composer 資料 rtnParams = importComposerData(apiSrc, batchNo, tsmpApiImp,
		 * auth.getUserName(), iip); if (!CollectionUtils.isEmpty(rtnParams)) { if
		 * (rtnParams.get("rtnCode").equals("1453")) { return returnWithResult(respItem,
		 * tsmpApiImp, TsmpDpAaRtnCode._1453.getCode(), locale, rtnParams); } else if
		 * (rtnParams.get("rtnCode").equals("1454")) { return returnWithResult(respItem,
		 * tsmpApiImp, TsmpDpAaRtnCode._1454.getCode(), locale, rtnParams); } }
		 */

		return respItem;
	}

	protected boolean checkRegModule(String apiSrc, String moduleName) {
		if (!TsmpApiSrc.REGISTERED.value().equals(apiSrc)) {
			return false;
		}
		List<TsmpRegModule> mList = getTsmpRegModuleDao().findByModuleName(moduleName);
		return !CollectionUtils.isEmpty(mList);
	}

	protected AA0319RespItem returnWithResult(AA0319RespItem respItem, TsmpApiImp tsmpApiImp, String rtnCode,
			String locale) {
		return returnWithResult(respItem, tsmpApiImp, rtnCode, locale, null);
	}

	protected AA0319RespItem returnWithResult(AA0319RespItem respItem, TsmpApiImp tsmpApiImp, //
			String rtnCode, String locale, Map<String, String> params) {
		respItem.setResult(toFail(locale));
		respItem.setDesc(null);

		// 查詢回傳訊息(多國語系)
		TsmpRtnCode c = getTsmpRtnCode(rtnCode, locale);
		if (c != null) {
			// 取代變數
			String tsmpRtnMsg = c.getTsmpRtnMsg();
			if (!CollectionUtils.isEmpty(params)) {
				tsmpRtnMsg = MailHelper.buildContent(tsmpRtnMsg, params);
			}
			respItem.setDesc(trunc(tsmpRtnMsg, 50));
		} else {
			// 找不到翻譯就直接填入 rtnCode
			respItem.setDesc(trunc(rtnCode, 50));
		}

		// 回寫 TSMP_API_IMP
		tsmpApiImp.setResult(respItem.getResult().getV());
		tsmpApiImp = getTsmpApiImpDao().saveAndFlush(tsmpApiImp);

		return respItem;
	}

	protected TsmpRtnCode getTsmpRtnCode(String rtnCode, String locale) {
		return getTsmpRtnCodeCacheProxy().findById(rtnCode, locale).orElse(null);
	}

	// 有錯，就回傳 rtnCode
	protected String doBackup(Integer batchNo, TsmpApiImp tsmpApiImp, String locale, List<String> userOrgIdList) {
		String apiKey = tsmpApiImp.getApiKey();
		String moduleName = tsmpApiImp.getModuleName();

		TsmpApiId tsmpApiId = new TsmpApiId(apiKey, moduleName);
		Optional<TsmpApi> opt_tsmpApi = getTsmpApiDao().findById(tsmpApiId);

		TsmpApiRegId tsmpApiRegId = new TsmpApiRegId(apiKey, moduleName);
		Optional<TsmpApiReg> opt_tsmpApiReg = getTsmpApiRegDao().findById(tsmpApiRegId);

		if (!(opt_tsmpApi.isPresent() && opt_tsmpApiReg.isPresent())) {
			this.logger.debug(String.format("Skip backup: apiKey=%s, moduleName=%s", apiKey, moduleName));
			return null;
		}

		TsmpApi tsmpApi = opt_tsmpApi.get();

		// API組織原則
		String apiOrgId = tsmpApi.getOrgId();
		if (!StringUtils.isEmpty(apiOrgId) && !userOrgIdList.contains(apiOrgId)) {
			this.logger.debug(
					String.format("Violate organization principle: apiOrgId(%s) not in %s", apiOrgId, userOrgIdList));
			return TsmpDpAaRtnCode._1219.getCode();
		}

		try {
			// 如果有既有的備份資料就刪除
			TsmpApiImpId tsmpApiImpId = new TsmpApiImpId(apiKey, moduleName, "B", batchNo);
			Optional<TsmpApiImp> opt_tsmpApiImp = getTsmpApiImpDao().findById(tsmpApiImpId);
			if (opt_tsmpApiImp.isPresent()) {
				getTsmpApiImpDao().delete(opt_tsmpApiImp.get());
			}

			// 將現有 TSMP_API, TSMP_API_REG 資料備份進 TSMP_API_IMP
			insertTsmpApiImp(batchNo, tsmpApi, opt_tsmpApiReg.get());
		} catch (Exception e) {
			this.logger.debug("Backup error: apiKey=" + apiKey + ", moduleName=" + moduleName + "\n"
					+ StackTraceUtil.logStackTrace(e));
			return e.getMessage();
		}

		return null;
	}

	protected void insertTsmpApiImp(Integer batchNo, TsmpApi tsmpApi, TsmpApiReg tsmpApiReg) {
		TsmpApiImp i = new TsmpApiImp();
		i.setApiKey(tsmpApi.getApiKey());
		i.setModuleName(tsmpApi.getModuleName());
		i.setRecordType("B");
		i.setBatchNo(batchNo);
		i.setFilename(" ");
		i.setApiName(tsmpApi.getApiName());
		i.setApiDesc(tsmpApi.getApiDesc());
		i.setApiOwner(tsmpApi.getApiOwner());
		i.setUrlRid(tsmpApiReg.getUrlRid());
		i.setApiSrc(tsmpApi.getApiSrc());
		i.setSrcUrl(tsmpApiReg.getSrcUrl());
		i.setApiUuid(tsmpApiReg.getApiUuid());
		i.setPathOfJson(tsmpApiReg.getPathOfJson());
		i.setMethodOfJson(tsmpApiReg.getMethodOfJson());
		i.setParamsOfJson(tsmpApiReg.getParamsOfJson());
		i.setHeadersOfJson(tsmpApiReg.getHeadersOfJson());
		i.setConsumesOfJson(tsmpApiReg.getConsumesOfJson());
		i.setProducesOfJson(tsmpApiReg.getProducesOfJson());
		i.setFlow(new String());
		i.setCreateTime(tsmpApi.getCreateTime());
		i.setCreateUser(tsmpApi.getCreateUser());
		i.setCheckAct("N");
		i.setResult("I");
		i.setMemo("for restore");
		i.setNoOauth(tsmpApiReg.getNoOauth());
		i.setJweFlag(tsmpApi.getJewFlag());
		i.setJweFlagResp(tsmpApi.getJewFlagResp());
		i.setFunFlag(tsmpApiReg.getFunFlag());
		i.setMockBody(tsmpApi.getMockBody());
		i.setMockHeaders(tsmpApi.getMockHeaders());
		i.setMockStatusCode(tsmpApi.getMockStatusCode());
		i.setRedirectByIp(tsmpApiReg.getRedirectByIp());
		i.setApiCacheFlag(tsmpApi.getApiCacheFlag());
		i.setFixedCacheTime(tsmpApi.getFixedCacheTime());

		i.setIpForRedirect1(tsmpApiReg.getIpForRedirect1());
		i.setIpSrcUrl1(tsmpApiReg.getIpSrcUrl1());

		i.setIpForRedirect2(tsmpApiReg.getIpForRedirect2());
		i.setIpSrcUrl2(tsmpApiReg.getIpSrcUrl2());

		i.setIpForRedirect3(tsmpApiReg.getIpForRedirect3());
		i.setIpSrcUrl3(tsmpApiReg.getIpSrcUrl3());

		i.setIpForRedirect4(tsmpApiReg.getIpForRedirect4());
		i.setIpSrcUrl4(tsmpApiReg.getIpSrcUrl4());
		i.setIpForRedirect5(tsmpApiReg.getIpForRedirect5());
		i.setIpSrcUrl5(tsmpApiReg.getIpSrcUrl5());

		String headerMaskPolicy = tsmpApiReg.getHeaderMaskPolicy();
		i.setHeaderMaskPolicy(StringUtils.hasLength(headerMaskPolicy) ? headerMaskPolicy : "0");
		i.setHeaderMaskKey(tsmpApiReg.getHeaderMaskKey());
		i.setHeaderMaskPolicyNum(tsmpApiReg.getHeaderMaskPolicyNum());
		i.setHeaderMaskPolicySymbol(tsmpApiReg.getHeaderMaskPolicySymbol());

		String bodyMaskPolicy = tsmpApiReg.getBodyMaskPolicy();
		i.setBodyMaskKeyword(tsmpApiReg.getBodyMaskKeyword());
		i.setBodyMaskPolicy(StringUtils.hasLength(bodyMaskPolicy) ? bodyMaskPolicy : "0");
		i.setBodyMaskPolicyNum(tsmpApiReg.getBodyMaskPolicyNum());
		i.setBodyMaskPolicySymbol(tsmpApiReg.getBodyMaskPolicySymbol());

		String failDiscoveryPolicy = tsmpApiReg.getFailDiscoveryPolicy();
		i.setFailDiscoveryPolicy(StringUtils.hasLength(failDiscoveryPolicy) ? failDiscoveryPolicy : "0");

		String failHandlePolicy = tsmpApiReg.getFailHandlePolicy();
		i.setFailHandlePolicy(StringUtils.hasLength(failHandlePolicy) ? failHandlePolicy : "0");

		i.setLabel1(tsmpApi.getLabel1());
		i.setLabel2(tsmpApi.getLabel2());
		i.setLabel3(tsmpApi.getLabel3());
		i.setLabel4(tsmpApi.getLabel4());
		i.setLabel5(tsmpApi.getLabel5());

		i.setApiStatus(tsmpApi.getApiStatus());

		getTsmpApiImpDao().saveAndFlush(i);
	}

	protected Map<String, String> executeByCheckAct(TsmpAuthorization auth, Integer batchNo, TsmpApiImp tsmpApiImp,
			String locale, InnerInvokeParam iip) {
		String userName = auth.getUserName();
		String checkAct = tsmpApiImp.getCheckAct();
		String apiKey = tsmpApiImp.getApiKey();
		String moduleName = tsmpApiImp.getModuleName();

		try {
			if ("C".equals(checkAct)) {
				String orgId = auth.getOrgId();
				createTsmpApi(orgId, tsmpApiImp, iip);
				createTsmpApiReg(orgId, tsmpApiImp, iip);
			} else if ("U".equals(checkAct)) {
				updateTsmpApi(userName, tsmpApiImp, iip);
				updateTsmpApiReg(userName, tsmpApiImp, iip);
			} else {
				this.logger.debug(
						String.format("Unknown checkAct: %s, apiKey=%s, moduleName=%s", checkAct, apiKey, moduleName));
			}
			return null;
		} catch (Exception e) {
			// 復原資料
			restoreFromTsmpApiImp(e, checkAct, apiKey, moduleName, batchNo, userName, iip);
			return createParams_1452(checkAct, apiKey, moduleName, locale, e);
		}
	}

	protected void restoreFromTsmpApiImp(Throwable e, String checkAct, String apiKey, String moduleName, //
			Integer batchNo, String userName, InnerInvokeParam iip) {
		if ("C".equals(checkAct)) {
			this.logger.debug("Create API error: " + StackTraceUtil.logStackTrace(e));
			deleteTsmpApi(apiKey, moduleName);
			deleteTsmpApiReg(apiKey, moduleName);
			// 刪除auditLog
			getDgrAuditLogDDao().deleteByTxnUid(iip.getTxnUid());
			getDgrAuditLogMDao().deleteByTxnUid(iip.getTxnUid());
		} else if ("U".equals(checkAct)) {
			this.logger.debug("Update API error: " + StackTraceUtil.logStackTrace(e));
			// 找出先前備份的資料
			TsmpApiImpId tsmpApiImpId = new TsmpApiImpId(apiKey, moduleName, "B", batchNo);
			Optional<TsmpApiImp> opt_forRestore = getTsmpApiImpDao().findById(tsmpApiImpId);
			if (opt_forRestore.isPresent()) {
				try {
					updateTsmpApi(userName, opt_forRestore.get(), iip);
					updateTsmpApiReg(userName, opt_forRestore.get(), iip);

					// 刪除auditLog
					getDgrAuditLogDDao().deleteByTxnUid(iip.getTxnUid());
					getDgrAuditLogMDao().deleteByTxnUid(iip.getTxnUid());
				} catch (Exception ee) {
					this.logger.debug("Restore data error\n" + StackTraceUtil.logStackTrace(ee));
				}
			} else {
				this.logger.debug("No backup data found");
			}
		}
	}

	protected Map<String, String> createParams_1452(String checkAct, String apiKey, String moduleName, //
			String locale, Throwable e) {
		// 錯誤訊息範本：{{0}} API 失敗：apiKey=[{{1}}], moduleName=[{{2}}], msg={{3}}
		Map<String, String> rtnParams = new HashMap<>();
		rtnParams.put("0", checkAct);
		rtnParams.put("1", apiKey);
		rtnParams.put("2", moduleName);
		rtnParams.put("3", e.getMessage());
		TsmpDpItems items = getTsmpDpItemsCacheProxy()
				.findById(new TsmpDpItemsId("API_IMP_CHECK_ACT", checkAct, locale));
		if (items != null) {
			rtnParams.put("0", items.getSubitemName());
		}
		return rtnParams;
	}

	protected TsmpApi createTsmpApi(String orgId, TsmpApiImp source, InnerInvokeParam iip) {
		TsmpApi tsmpApi = new TsmpApi();
		tsmpApi.setApiKey(source.getApiKey());
		tsmpApi.setModuleName(source.getModuleName());
		tsmpApi.setApiName(source.getApiName());
		tsmpApi.setApiStatus("2");
		tsmpApi.setApiSrc(source.getApiSrc());
		tsmpApi.setApiDesc(source.getApiDesc());
		tsmpApi.setApiOwner(source.getApiOwner());
		String apiUid = source.getApiUuid();
		if (StringUtils.isEmpty(apiUid)) {
			apiUid = UUID.randomUUID().toString().toUpperCase();
		}
		tsmpApi.setApiUid(apiUid);
		tsmpApi.setCreateTime(source.getCreateTime());
		tsmpApi.setCreateUser(source.getCreateUser());
		tsmpApi.setOrgId(orgId);
		tsmpApi.setJewFlag(source.getJweFlag());
		tsmpApi.setJewFlagResp(source.getJweFlagResp());
		tsmpApi.setSrcUrl(source.getSrcUrl());
		tsmpApi.setMockBody(source.getMockBody());
		tsmpApi.setMockHeaders(source.getMockHeaders());
		tsmpApi.setMockStatusCode(source.getMockStatusCode());
		tsmpApi.setApiCacheFlag(source.getApiCacheFlag());
		tsmpApi.setFixedCacheTime(source.getFixedCacheTime());
		tsmpApi.setPublicFlag(source.getPublicFlag());
		tsmpApi.setApiReleaseTime(source.getApiReleaseTime());
		tsmpApi.setScheduledLaunchDate(source.getScheduledLaunchDate());
		tsmpApi.setScheduledRemovalDate(source.getScheduledRemovalDate());
		tsmpApi.setEnableScheduledDate(source.getEnableScheduledDate());
		tsmpApi.setDisableScheduledDate(source.getDisableScheduledDate());
		tsmpApi.setLabel1(source.getLabel1());
		tsmpApi.setLabel2(source.getLabel2());
		tsmpApi.setLabel3(source.getLabel3());
		tsmpApi.setLabel4(source.getLabel4());
		tsmpApi.setLabel5(source.getLabel5());

		tsmpApi.setApiStatus(source.getApiStatus());

		tsmpApi = getTsmpApiDao().saveAndFlush(tsmpApi);

		if (TsmpApiSrc.REGISTERED.value().equals(source.getApiSrc())
				|| TsmpApiSrc.COMPOSED.value().equals(source.getApiSrc())) {
			// 寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, TsmpApi.class.getSimpleName(), TableAct.C.value(),
					null, tsmpApi);
		}

		return tsmpApi;
	}

	protected TsmpApiReg createTsmpApiReg(String orgId, TsmpApiImp source, InnerInvokeParam iip) {
		TsmpApiReg tsmpApiReg = new TsmpApiReg();
		tsmpApiReg.setApiKey(source.getApiKey());
		tsmpApiReg.setModuleName(source.getModuleName());
		tsmpApiReg.setSrcUrl(source.getSrcUrl());
		tsmpApiReg.setUrlRid(source.getUrlRid());
		tsmpApiReg.setRegStatus("1");
		tsmpApiReg.setApiUuid(source.getApiUuid());
		tsmpApiReg.setReghostId(new String());
		tsmpApiReg.setPathOfJson(source.getPathOfJson());
		tsmpApiReg.setMethodOfJson(source.getMethodOfJson());
		tsmpApiReg.setParamsOfJson(source.getParamsOfJson());
		tsmpApiReg.setHeadersOfJson(source.getHeadersOfJson());
		tsmpApiReg.setConsumesOfJson(source.getConsumesOfJson());
		tsmpApiReg.setProducesOfJson(source.getProducesOfJson());
		tsmpApiReg.setCreateTime(source.getCreateTime());
		tsmpApiReg.setCreateUser(source.getCreateUser());
		tsmpApiReg.setNoOauth(source.getNoOauth());
		tsmpApiReg.setFunFlag(source.getFunFlag());

		tsmpApiReg.setRedirectByIp(source.getRedirectByIp());

		tsmpApiReg.setIpForRedirect1(source.getIpForRedirect1());
		tsmpApiReg.setIpSrcUrl1(source.getIpSrcUrl1());

		tsmpApiReg.setIpForRedirect2(source.getIpForRedirect2());
		tsmpApiReg.setIpSrcUrl2(source.getIpSrcUrl2());

		tsmpApiReg.setIpForRedirect3(source.getIpForRedirect3());
		tsmpApiReg.setIpSrcUrl3(source.getIpSrcUrl3());

		tsmpApiReg.setIpForRedirect4(source.getIpForRedirect4());
		tsmpApiReg.setIpSrcUrl4(source.getIpSrcUrl4());

		tsmpApiReg.setIpForRedirect5(source.getIpForRedirect5());
		tsmpApiReg.setIpSrcUrl5(source.getIpSrcUrl5());

		String headerMaskPolicy = source.getHeaderMaskPolicy();
		tsmpApiReg.setHeaderMaskPolicy(headerMaskPolicy);
		tsmpApiReg.setHeaderMaskKey(source.getHeaderMaskKey());
		tsmpApiReg.setHeaderMaskPolicyNum(source.getHeaderMaskPolicyNum());
		tsmpApiReg.setHeaderMaskPolicySymbol(source.getHeaderMaskPolicySymbol());

		tsmpApiReg.setBodyMaskKeyword(source.getBodyMaskKeyword());
		tsmpApiReg.setBodyMaskPolicy(source.getBodyMaskPolicy());
		tsmpApiReg.setBodyMaskPolicyNum(source.getBodyMaskPolicyNum());
		tsmpApiReg.setBodyMaskPolicySymbol(source.getBodyMaskPolicySymbol());

		String failDiscoveryPolicy = source.getFailDiscoveryPolicy();
		tsmpApiReg.setFailDiscoveryPolicy(failDiscoveryPolicy);

		String failHandlePolicy = source.getFailHandlePolicy();
		tsmpApiReg.setFailHandlePolicy(failHandlePolicy);

		tsmpApiReg = getTsmpApiRegDao().saveAndFlush(tsmpApiReg);

		if (TsmpApiSrc.REGISTERED.value().equals(source.getApiSrc())
				|| TsmpApiSrc.COMPOSED.value().equals(source.getApiSrc())) {
			// 寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, TsmpApiReg.class.getSimpleName(),
					TableAct.C.value(), null, tsmpApiReg);
		}

		return tsmpApiReg;
	}

	protected TsmpApi updateTsmpApi(String userName, TsmpApiImp source, InnerInvokeParam iip) throws Exception {
		String apiKey = source.getApiKey();
		String moduleName = source.getModuleName();
		Optional<TsmpApi> opt = getTsmpApiDao().findById(new TsmpApiId(apiKey, moduleName));
		if (!opt.isPresent()) {
			throw new Exception("TSMP_API not exists");
		}
		TsmpApi tsmpApi = opt.get();

		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpApi); // 舊資料統一轉成 String

		tsmpApi.setApiName(source.getApiName());
		tsmpApi.setApiStatus("2");
		tsmpApi.setApiSrc(source.getApiSrc());
		tsmpApi.setApiDesc(source.getApiDesc());
		tsmpApi.setApiOwner(source.getApiOwner());
		tsmpApi.setUpdateTime(DateTimeUtil.now());
		tsmpApi.setUpdateUser(userName);
		tsmpApi.setJewFlag(source.getJweFlag());
		tsmpApi.setJewFlagResp(source.getJweFlagResp());
		tsmpApi.setMockBody(source.getMockBody());
		tsmpApi.setMockHeaders(source.getMockHeaders());
		tsmpApi.setMockStatusCode(source.getMockStatusCode());
		tsmpApi.setApiCacheFlag(source.getApiCacheFlag());
		tsmpApi.setFixedCacheTime(source.getFixedCacheTime());
		tsmpApi.setPublicFlag(source.getPublicFlag());
		tsmpApi.setApiReleaseTime(source.getApiReleaseTime());
		tsmpApi.setScheduledLaunchDate(source.getScheduledLaunchDate());
		tsmpApi.setScheduledRemovalDate(source.getScheduledRemovalDate());
		tsmpApi.setEnableScheduledDate(source.getEnableScheduledDate());
		tsmpApi.setDisableScheduledDate(source.getDisableScheduledDate());
		
		tsmpApi.setLabel1(source.getLabel1());
		tsmpApi.setLabel2(source.getLabel2());
		tsmpApi.setLabel3(source.getLabel3());
		tsmpApi.setLabel4(source.getLabel4());
		tsmpApi.setLabel5(source.getLabel5());
		tsmpApi.setApiStatus(source.getApiStatus());

		tsmpApi = getTsmpApiDao().saveAndFlush(tsmpApi);

		if (TsmpApiSrc.REGISTERED.value().equals(source.getApiSrc())
				|| TsmpApiSrc.COMPOSED.value().equals(source.getApiSrc())) {
			// 寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, TsmpApi.class.getSimpleName(), TableAct.U.value(),
					oldRowStr, tsmpApi);
		}

		return tsmpApi;
	}

	protected TsmpApiReg updateTsmpApiReg(String userName, TsmpApiImp source, InnerInvokeParam iip) throws Exception {
		String apiKey = source.getApiKey();
		String moduleName = source.getModuleName();
		Optional<TsmpApiReg> opt = getTsmpApiRegDao().findById(new TsmpApiRegId(apiKey, moduleName));
		if (!opt.isPresent()) {
			throw new Exception("TSMP_API_REG not exists");
		}
		TsmpApiReg tsmpApiReg = opt.get();

		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpApiReg); // 舊資料統一轉成 String

		tsmpApiReg.setSrcUrl(source.getSrcUrl());
		tsmpApiReg.setUrlRid(source.getUrlRid());
		tsmpApiReg.setReghostId(new String());
		tsmpApiReg.setMethodOfJson(source.getMethodOfJson());
		tsmpApiReg.setParamsOfJson(source.getParamsOfJson());
		tsmpApiReg.setHeadersOfJson(source.getHeadersOfJson());
		tsmpApiReg.setConsumesOfJson(source.getConsumesOfJson());
		tsmpApiReg.setUpdateTime(DateTimeUtil.now());
		tsmpApiReg.setUpdateUser(userName);
		tsmpApiReg.setNoOauth(source.getNoOauth());
		tsmpApiReg.setFunFlag(source.getFunFlag());

		tsmpApiReg.setRedirectByIp(source.getRedirectByIp());

		tsmpApiReg.setIpForRedirect1(source.getIpForRedirect1());
		tsmpApiReg.setIpSrcUrl1(source.getIpSrcUrl1());

		tsmpApiReg.setIpForRedirect2(source.getIpForRedirect2());
		tsmpApiReg.setIpSrcUrl2(source.getIpSrcUrl2());

		tsmpApiReg.setIpForRedirect3(source.getIpForRedirect3());
		tsmpApiReg.setIpSrcUrl3(source.getIpSrcUrl3());

		tsmpApiReg.setIpForRedirect4(source.getIpForRedirect4());
		tsmpApiReg.setIpSrcUrl4(source.getIpSrcUrl4());

		tsmpApiReg.setIpForRedirect5(source.getIpForRedirect5());
		tsmpApiReg.setIpSrcUrl5(source.getIpSrcUrl5());

		String headerMaskPolicy = source.getHeaderMaskPolicy();
		tsmpApiReg.setHeaderMaskPolicy(headerMaskPolicy);
		tsmpApiReg.setHeaderMaskKey(source.getHeaderMaskKey());
		tsmpApiReg.setHeaderMaskPolicyNum(source.getHeaderMaskPolicyNum());
		tsmpApiReg.setHeaderMaskPolicySymbol(source.getHeaderMaskPolicySymbol());

		tsmpApiReg.setBodyMaskKeyword(source.getBodyMaskKeyword());
		tsmpApiReg.setBodyMaskPolicy(source.getBodyMaskPolicy());
		tsmpApiReg.setBodyMaskPolicyNum(source.getBodyMaskPolicyNum());
		tsmpApiReg.setBodyMaskPolicySymbol(source.getBodyMaskPolicySymbol());

		tsmpApiReg = getTsmpApiRegDao().saveAndFlush(tsmpApiReg);

		if (TsmpApiSrc.REGISTERED.value().equals(source.getApiSrc())
				|| TsmpApiSrc.COMPOSED.value().equals(source.getApiSrc())) {
			// 寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, TsmpApiReg.class.getSimpleName(),
					TableAct.U.value(), oldRowStr, tsmpApiReg);
		}

		return tsmpApiReg;
	}

	protected void deleteTsmpApi(String apiKey, String moduleName) {
		TsmpApiId tsmpApiId = new TsmpApiId(apiKey, moduleName);
		if (getTsmpApiDao().existsById(tsmpApiId)) {
			getTsmpApiDao().deleteById(tsmpApiId);
		}
	}

	protected void deleteTsmpApiReg(String apiKey, String moduleName) {
		TsmpApiRegId tsmpApiRegId = new TsmpApiRegId(apiKey, moduleName);
		if (getTsmpApiRegDao().existsById(tsmpApiRegId)) {
			getTsmpApiRegDao().deleteById(tsmpApiRegId);
		}
	}

	protected Map<String, String> createParams_1453(String apiKey, String moduleName, Throwable e) {
		// 錯誤訊息範本：Composer Flow 轉型失敗：apiKey=[{{0}}, moduleName=[{{1}}], msg={{2}}
		Map<String, String> rtnParams = new HashMap<>();
		rtnParams.put("rtnCode", "1453");
		rtnParams.put("0", apiKey);
		rtnParams.put("1", moduleName);
		rtnParams.put("2", e.getMessage());
		return rtnParams;
	}

	protected Map<String, String> createParams_1454(String apiKey, String moduleName, Throwable e) {
		// 錯誤訊息範本：寫入 Composer 資料錯誤：apiKey=[{{0}}, moduleName=[{{1}}], msg={{2}}
		Map<String, String> rtnParams = new HashMap<>();
		rtnParams.put("rtnCode", "1454");
		rtnParams.put("0", apiKey);
		rtnParams.put("1", moduleName);
		rtnParams.put("2", e.getMessage());
		return rtnParams;
	}

	protected AA0319Pair toSuccess(String locale) {
		return toImpResult("S", locale);
	}

	protected AA0319Pair toFail(String locale) {
		return toImpResult("F", locale);
	}

	protected AA0319Pair toImpResult(String value, String locale) {
		return toPair(value, "API_IMP_RESULT", locale);
	}

	protected AA0319Pair toPair(String value, String itemNo, String locale) {
		AA0319Pair pair = new AA0319Pair();
		pair.setV(value);
		pair.setN(value);
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, value, locale);
		TsmpDpItems items = getTsmpDpItemsCacheProxy().findById(id);
		if (items != null) {
			pair.setN(items.getSubitemName());
		}
		return pair;
	}

	protected AA0319Trunc trunc(String value, int maxLength) {
		AA0319Trunc aa0319Trunc = new AA0319Trunc();
		aa0319Trunc.setT(Boolean.FALSE);
		aa0319Trunc.setVal(value);
		if (!StringUtils.isEmpty(value) && value.length() > maxLength) {
			aa0319Trunc.setT(Boolean.TRUE);
			aa0319Trunc.setOri(value);
			aa0319Trunc.setVal(value.substring(0, maxLength));
		}
		return aa0319Trunc;
	}

	private void doImportAllComposerNodes(TsmpAuthorization auth, Integer batchNo,
			HashMap<String, TsmpApiImp> nodeDataHM, String locale, List<String> userOrgIdList, InnerInvokeParam iip,
			HashMap<String, AA0319RespItem> respItemHM) {

		String userName = auth.getUserName();

		HashMap<String, Map<String, String>> rtnParamsHM = new HashMap<String, Map<String, String>>();

		for (TsmpApiImp tsmpApiImpData : nodeDataHM.values()) {
			TsmpApiImp tsmpApiImp = tsmpApiImpData;
			String apiKey = tsmpApiImpData.getApiKey();
			String moduleName = tsmpApiImpData.getModuleName();
			String key = moduleName + apiKey;
			try {
				String flow = tsmpApiImp.getFlow();
				DgrComposerFlow resp = getDgrComposerFlowDao().findByModuleNameAndApiId(moduleName, apiKey)
						.orElse(null);
				if (resp == null) {
					resp = new DgrComposerFlow();
				}
				
				//20240716, 因composer是以UUID為主, 但可能A匯出時, B卻自己有建立該API, 
				//此時匯入後他們UUID是不一樣的(DgrComposerFlow和TsmpApiReg或TsmpApi),會造成composer無法開啟問題
				if(StringUtils.hasText(flow) && flow.length() > 4) {
					String temp = flow.substring(4);
					temp = new String (Base64Util.base64URLDecode(temp), StandardCharsets.UTF_8);
					int applicationIndex = -1;
					String searchApp = "\"application\"";
					applicationIndex = temp.indexOf(searchApp);
					if(applicationIndex > -1) {
						int apiIdIndex = -1;
						String searchApiId = "\"apiId\"";
						apiIdIndex = temp.indexOf(searchApiId, applicationIndex);
						if(apiIdIndex > -1) {
							int uuidStartIndex = temp.indexOf("\"", apiIdIndex + searchApiId.length());
							int uuidEndIndex = -1;
							if(uuidStartIndex > -1) {
								uuidEndIndex = temp.indexOf("\"", uuidStartIndex + 1);
							}
							if(uuidStartIndex > -1 && uuidEndIndex > -1) {
								String uuid = temp.substring(uuidStartIndex + 1, uuidEndIndex);
								TsmpApi tsmpApiVo = this.getTsmpApiDao().findByModuleNameAndApiKey(moduleName, apiKey);
								if(tsmpApiVo != null) {
									if(tsmpApiVo.getApiUid() != null && !tsmpApiVo.getApiUid().equals(uuid)) {
										temp = temp.replaceAll(uuid, tsmpApiVo.getApiUid());
										flow = flow.substring(0, 4) + Base64Util.base64URLEncode(temp.getBytes());
										this.logger.debug("apiId change");
									}
								}
							}else {
								this.logger.debug("apiId not found");
							}
						}
					}
				}
				
				resp.setApiId(apiKey);
				resp.setModuleName(moduleName);
				resp.setUpdateDateTime(DateTimeUtil.now());
				resp.setFlowData(flow.getBytes());
				getDgrComposerFlowDao().save(resp);

			} catch (Exception e) {
				// 復原資料
				Map<String, String> rtnParams = restoreDataAndGenerate1453(batchNo, iip, tsmpApiImp, userName,
						moduleName, apiKey, e);
				rtnParamsHM.put(key, rtnParams);
			}
		}

		// 組合產生錯誤訊息
		if (!rtnParamsHM.isEmpty()) {
			for (String key : rtnParamsHM.keySet()) {
				Map<String, String> rtnParams = rtnParamsHM.get(key);
				TsmpApiImp tsmpApiImp = nodeDataHM.get(key);
				AA0319RespItem respItem = respItemHM.get(key);
				if (rtnParams.get("rtnCode").equals("1453")) {
					returnWithResult(respItem, tsmpApiImp, TsmpDpAaRtnCode._1453.getCode(), locale, rtnParams);
				} else if (rtnParams.get("rtnCode").equals("1454")) {
					returnWithResult(respItem, tsmpApiImp, TsmpDpAaRtnCode._1454.getCode(), locale, rtnParams);
				}
			}
		}
		// 需要判斷TPILogger.lc是否為null，因為在單元測試中，TPILogger.lc是沒有連接會是null。
		if (TPILogger.lc != null) {
			TPILogger.lc.send(new UpdateComposerTSPacket());
		}
	}

	private Map<String, String> restoreDataAndGenerate1453(Integer batchNo, InnerInvokeParam iip, TsmpApiImp tsmpApiImp,
			String userName, String moduleName, String apiKey, Exception e) {
		Map<String, String> rtnParams;
		String checkAct = tsmpApiImp.getCheckAct();
		restoreFromTsmpApiImp(e, checkAct, apiKey, moduleName, batchNo, userName, iip);
		rtnParams = createParams_1453(apiKey, moduleName, e);
		return rtnParams;
	}

	protected void clearAPICache() {
		getDaoGenericCacheService().clearAndNotify();
	}

	protected DgrComposerFlowDao getDgrComposerFlowDao() {
		return dgrComposerFlowDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpApiRegDao getTsmpApiRegDao() {
		return this.tsmpApiRegDao;
	}

	protected TsmpApiImpDao getTsmpApiImpDao() {
		return this.tsmpApiImpDao;
	}

	protected TsmpRegModuleDao getTsmpRegModuleDao() {
		return this.tsmpRegModuleDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected TsmpRtnCodeCacheProxy getTsmpRtnCodeCacheProxy() {
		return this.tsmpRtnCodeCacheProxy;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected ComposerService getComposerService() {
		return this.composerService;
	}

	protected NoticeClearCacheEventsJob getNoticeClearCacheEventsJob(Integer action, String cacheName, //
			List<String> tableNameList) {
		return (NoticeClearCacheEventsJob) getCtx().getBean("noticeClearCacheEventsJob", action, cacheName,
				tableNameList);
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}

	protected DgrAuditLogMDao getDgrAuditLogMDao() {
		return dgrAuditLogMDao;
	}

	protected DgrAuditLogDDao getDgrAuditLogDDao() {
		return dgrAuditLogDDao;
	}

	protected DaoGenericCacheService getDaoGenericCacheService() {
		return daoGenericCacheService;
	}

	protected DigiRunnerGtwDeployProperties getDigiRunnerGtwDeployProperties() {
		return digiRunnerGtwDeployProperties;
	}
}