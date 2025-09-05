package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.job.NoticeClearCacheEventsJob;
import tpi.dgrv4.dpaa.component.scheduledEnableDisable.ApiListAndAlgorithmClassifier;
import tpi.dgrv4.dpaa.component.scheduledEnableDisable.ApiPublicFlagHandlerData;
import tpi.dgrv4.dpaa.component.scheduledEnableDisable.ApiPublicFlagHandlerInterface;
import tpi.dgrv4.dpaa.component.scheduledEnableDisable.EnableDisableProcessingFlow;
import tpi.dgrv4.dpaa.constant.TsmpApiSrc;
import tpi.dgrv4.dpaa.util.TimestampConverterUtil;
import tpi.dgrv4.dpaa.vo.AA0303Item;
import tpi.dgrv4.dpaa.vo.AA0303Req;
import tpi.dgrv4.dpaa.vo.AA0303Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpRtnCodeCacheProxy;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.DgrWebhookApiMap;
import tpi.dgrv4.entity.entity.ITsmpRtnCode;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpApiRegId;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.jpql.DgrComposerFlow;
import tpi.dgrv4.entity.entity.jpql.TsmpApiDetail;
import tpi.dgrv4.entity.entity.jpql.TsmpApiExt;
import tpi.dgrv4.entity.entity.jpql.TsmpApiExtId;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrders;
import tpi.dgrv4.entity.entity.jpql.TsmpRegModule;
import tpi.dgrv4.entity.entity.jpql.TsmpnApiDetail;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.DgrComposerFlowDao;
import tpi.dgrv4.entity.repository.DgrWebhookApiMapDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiDetailDao;
import tpi.dgrv4.entity.repository.TsmpApiExtDao;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.entity.repository.TsmpClientGroupDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdersDao;
import tpi.dgrv4.entity.repository.TsmpGroupApiDao;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesMapDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpRegModuleDao;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.entity.repository.TsmpVgroupGroupDao;
import tpi.dgrv4.entity.repository.TsmpnApiDetailDao;
import tpi.dgrv4.gateway.TCP.Packet.UpdateComposerTSPacket;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0303Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpUserDao tsmpUserDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpApiRegDao tsmpApiRegDao;
	private TsmpApiDetailDao tsmpApiDetailDao;
	private TsmpnApiDetailDao tsmpnApiDetailDao;
	private TsmpRtnCodeCacheProxy tsmpRtnCodeCacheProxy;
	private TsmpApiExtDao tsmpApiExtDao;
	private ApplicationContext ctx;
	private JobHelper jobHelper;
	private TsmpDpReqOrdersDao tsmpDpReqOrdersDao;
	private TsmpRegModuleDao tsmpRegModuleDao;
	private TsmpGroupDao tsmpGroupDao;
	private TsmpGroupApiDao tsmpGroupApiDao;
	private TsmpVgroupGroupDao tsmpVgroupGroupDao;
	private TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao;
	private TsmpClientGroupDao tsmpClientGroupDao;
	private DgrAuditLogService dgrAuditLogService;
	private DgrComposerFlowDao dgrComposerFlowDao;
	private DaoGenericCacheService daoGenericCacheService;
	private DgrAcIdpUserDao dgrAcIdpUserDao;
	private TsmpRtnCodeDao tsmpRtnCodeDao;
	private EnableDisableProcessingFlow enableDisableProcessingFlow;
	private List<ApiPublicFlagHandlerInterface> apiPublicFlagHandlers;

	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookApiMapDao dgrWebhookApiMapDao;

	public AA0303Service(TsmpUserDao tsmpUserDao, TsmpApiDao tsmpApiDao, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpApiRegDao tsmpApiRegDao, TsmpApiDetailDao tsmpApiDetailDao, TsmpnApiDetailDao tsmpnApiDetailDao,
			TsmpRtnCodeCacheProxy tsmpRtnCodeCacheProxy, TsmpApiExtDao tsmpApiExtDao, ApplicationContext ctx,
			JobHelper jobHelper, TsmpDpReqOrdersDao tsmpDpReqOrdersDao, TsmpRegModuleDao tsmpRegModuleDao,
			TsmpGroupDao tsmpGroupDao, TsmpGroupApiDao tsmpGroupApiDao, TsmpVgroupGroupDao tsmpVgroupGroupDao,
			TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao, TsmpClientGroupDao tsmpClientGroupDao,
			DgrAuditLogService dgrAuditLogService, DgrComposerFlowDao dgrComposerFlowDao,
			DaoGenericCacheService daoGenericCacheService, DgrAcIdpUserDao dgrAcIdpUserDao,
			TsmpRtnCodeDao tsmpRtnCodeDao, EnableDisableProcessingFlow enableDisableProcessingFlow,
			List<ApiPublicFlagHandlerInterface> apiPublicFlagHandlers, DgrWebhookApiMapDao dgrWebhookApiMapDao) {
		super();
		this.tsmpUserDao = tsmpUserDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpApiRegDao = tsmpApiRegDao;
		this.tsmpApiDetailDao = tsmpApiDetailDao;
		this.tsmpnApiDetailDao = tsmpnApiDetailDao;
		this.tsmpRtnCodeCacheProxy = tsmpRtnCodeCacheProxy;
		this.tsmpApiExtDao = tsmpApiExtDao;
		this.ctx = ctx;
		this.jobHelper = jobHelper;
		this.tsmpDpReqOrdersDao = tsmpDpReqOrdersDao;
		this.tsmpRegModuleDao = tsmpRegModuleDao;
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpGroupApiDao = tsmpGroupApiDao;
		this.tsmpVgroupGroupDao = tsmpVgroupGroupDao;
		this.tsmpGroupAuthoritiesMapDao = tsmpGroupAuthoritiesMapDao;
		this.tsmpClientGroupDao = tsmpClientGroupDao;
		this.dgrAuditLogService = dgrAuditLogService;
		this.dgrComposerFlowDao = dgrComposerFlowDao;
		this.daoGenericCacheService = daoGenericCacheService;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
		this.tsmpRtnCodeDao = tsmpRtnCodeDao;
		this.enableDisableProcessingFlow = enableDisableProcessingFlow;
		this.apiPublicFlagHandlers = apiPublicFlagHandlers;
		this.dgrWebhookApiMapDao = dgrWebhookApiMapDao;
	}

	@Transactional
	public AA0303Resp updateAPIStatus_1(TsmpAuthorization auth, AA0303Req req, ReqHeader reqHeader,
			InnerInvokeParam iip) {
		String userName = auth.getUserName();
		String userNameForQuery = auth.getUserNameForQuery();
		String idPType = auth.getIdpType();
		String orgId = auth.getOrgId();

		ApiListAndAlgorithmClassifier classifier = null;

		checkParams(userName, orgId, req, idPType, userNameForQuery);

		// 檢查提醒事項
		AA0303Resp resp = checkAlert(req, reqHeader.getLocale());
		if (resp != null && StringUtils.hasLength(resp.getMsg())) {
			return resp;
		}

		try {
			String req_apiStatus = req.getApiStatus();
			if ("0".equals(req_apiStatus)) { // Delete
				doDelete(userName, orgId, req, iip);
			} else { // Update
				classifier = doUpdate(userName, req, iip, reqHeader);
			}
			// 清除快取
			clearAPICache();
			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.API.value());
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1286.throwing();
		}

		List<AA0303Item> apiList = getAA0303ItemList(classifier, reqHeader.getLocale());
		resp = new AA0303Resp();
		resp.setApiList(apiList);
		return resp;
	}

	private List<AA0303Item> getAA0303ItemList(ApiListAndAlgorithmClassifier classifier, String locale) {

		if (classifier == null || getApiPublicFlagHandlers() == null) {
			return Collections.emptyList();
		}

		List<AA0303Item> result = new ArrayList<>();

		Map<String, List<TsmpApi>> map = classifier.getApiListMap();

		for (ApiPublicFlagHandlerInterface handler : getApiPublicFlagHandlers()) {
			String name = handler.getClass().getSimpleName();
			List<TsmpApi> ls = map.get(name);

			if (ls != null) {
				List<AA0303Item> respList = handler.getAA0303ItemRespList(ls, locale);
				result.addAll(respList);
			}
		}

		return result;
	}

	protected ApiPublicFlagHandlerInterface getDisableProcessingFlow() {
		return enableDisableProcessingFlow.getDisableProcessingFlow();
	}

	protected ApiPublicFlagHandlerInterface getEnableProcessingFlow() {
		return enableDisableProcessingFlow.getEnableProcessingFlow();
	}

	protected List<ApiPublicFlagHandlerInterface> getApiPublicFlagHandlers() {
		return apiPublicFlagHandlers;
	}

	protected void setApiPublicFlagHandlers(List<ApiPublicFlagHandlerInterface> apiPublicFlagHandlers) {
		this.apiPublicFlagHandlers = apiPublicFlagHandlers;
	}

	private ApiPublicFlagHandlerInterface getProcessingChain(List<ApiPublicFlagHandlerInterface> handlers) {
		for (int i = 0; i < handlers.size() - 1; i++) {
			handlers.get(i).setNext(handlers.get(i + 1));
		}
		return handlers.get(0);
	}

	public void checkParams(String userName, String orgId, AA0303Req req, String idPType, String userNameForQuery) {
		if (StringUtils.isEmpty(userName)) {
			throw TsmpDpAaRtnCode._1258.throwing();
		}

		if (StringUtils.isEmpty(userNameForQuery)) {
			throw TsmpDpAaRtnCode._1258.throwing();
		}

		if (StringUtils.isEmpty(orgId)) {
			throw TsmpDpAaRtnCode._1273.throwing();
		}

		// 檢查 AA0303Req.ignoreAlert 是否符合正規式：(^[Y|N]$)，否則 throw 1352。([ignoreAlert]
		// 格式不正確)
		String ignoreAlert = req.getIgnoreAlert();
		if (StringUtils.isEmpty(ignoreAlert)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		} else if (!ignoreAlert.matches("^[Y|N]$")) {
			throw TsmpDpAaRtnCode._1352.throwing("ignoreAlert");
		}

		// 若同時未傳入 AA0303Req.jweFlag、AA0303Req.jweFlagResp 及 AA0303Req.apiStatus，則 throw
		// 1296。
		String jweFlag = req.getJweFlag();
		String jweFlagResp = req.getJweFlagResp();
		String apiStatus = req.getApiStatus();
		if (StringUtils.isEmpty(jweFlag) && StringUtils.isEmpty(jweFlagResp) && StringUtils.isEmpty(apiStatus)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		// 若有傳入 AA0303Req.apiStatus，則檢查是否符合正規式：(^[0|1|2]$)，否則 throw 1352。([apiStatus]
		// 格式不正確)
		if (!StringUtils.isEmpty(apiStatus) && !apiStatus.matches("^[0|1|2]$")) {
			throw TsmpDpAaRtnCode._1352.throwing("apiStatus");
		}

		// 若有傳入 AA0303Req.jweFlag 或 AA0303Req.jweFlagResp，則檢查是否符合正規式：(^[0|1|2]$)，否則
		// throw 1352。([jweFlag/jweFlagResp] 格式不正確)
		if (!StringUtils.isEmpty(jweFlag) && !jweFlag.matches("^[0|1|2]$")) {
			throw TsmpDpAaRtnCode._1352.throwing("jweFlag");
		}

		if (!StringUtils.isEmpty(jweFlagResp) && !jweFlagResp.matches("^[0|1|2]$")) {
			throw TsmpDpAaRtnCode._1352.throwing("jweFlagResp");
		}

		checkUserExists(userNameForQuery, idPType);

		List<String> userOrgIdList = checkOrgExists(orgId);

		// 檢查每一筆 AA0303Req.apiList，是否存在 TSMP_API，任何一筆不存在，或是 AA0303Item.moduleName /
		// AA0303Item.apiKey 為空，則 throw 1116。
		checkApiList(userOrgIdList, req);
	}

	protected void checkUserExists(String userNameForQuery, String idPType) {
		if (StringUtils.hasLength(idPType)) {// 以 IdP 登入 AC
			DgrAcIdpUser dgrAcIdpUser = getDgrAcIdpUserDao().findFirstByUserNameAndIdpType(userNameForQuery, idPType);
			if (dgrAcIdpUser == null) {
				// Table 查不到 user
				TPILogger.tl.debug("Table [DGR_AC_IDP_USER] can not find user, user_name: " + userNameForQuery
						+ ", idp_type: " + idPType);
				throw TsmpDpAaRtnCode._1231.throwing();
			}

		} else {// 以 AC 登入
			TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userNameForQuery);
			// Table 查不到 user
			TPILogger.tl.debug("Table [TSMP_USER] can not find user, user_name: " + userNameForQuery);
			if (tsmpUser == null) {
				throw TsmpDpAaRtnCode._1231.throwing();
			}
		}
	}

	protected List<String> checkOrgExists(String userOrgId) {
		List<String> userOrgIdList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(userOrgId, null);
		if (CollectionUtils.isEmpty(userOrgIdList)) {
			throw TsmpDpAaRtnCode._1222.throwing();
		}
		return userOrgIdList;
	}

	protected void checkApiList(List<String> userOrgIdList, AA0303Req req) {
		List<AA0303Item> apiList = req.getApiList();

		if (CollectionUtils.isEmpty(apiList)) {
			throw TsmpDpAaRtnCode._2009.throwing("1");
		}

		String moduleName;
		String apiKey;
		TsmpApi tsmpApi;
		String req_apiStatus;
		for (AA0303Item api : apiList) {
			moduleName = api.getModuleName();
			apiKey = api.getApiKey();

			if (api == null || StringUtils.isEmpty(moduleName) || StringUtils.isEmpty(apiKey)) {
				this.logger.debug("incomplete API info.");
				throw TsmpDpAaRtnCode.NO_API_INFO.throwing();
			}

			tsmpApi = checkApiExists(apiKey, moduleName);
			api.setTsmpApi(tsmpApi); // checkAlert() 會用到

			// API組織原則
			checkApiOrg(api, userOrgIdList);

			req_apiStatus = req.getApiStatus();
			if (StringUtils.isEmpty(req_apiStatus)) {
				continue;
			}

			if ("1".equals(req_apiStatus)) { // Enabled

				// 若 TSMP_API.api_src = "C"，則檢查 註冊狀態(TSMP_API_REG.reg_status) 是否已確認，否則 throw
				// 1460。
				checkApiRegStatus(api);

			} else if ("0".equals(req_apiStatus)) { // Delete

				// TSMP_API.api_status = "1" (Enabled) 則 throw 1463。
				checkApiStatus(api);

				// 若 TSMP_API.api_src = "M"，則檢查此 API 所屬的各版本Java模組都已不存在才可刪除，否則 throw 1464。
				// 若 TSMP_API.api_src = "N"，則檢查此 API 所屬的各版本.NET模組都已不存在才可刪除，否則 throw 1464。
				// 若 TSMP_API.api_src = "R" 或 "C"，則檢查 TSMP_API_REG (api_key, module_name)
				// 是否存在，不存在則 throw 1298。
				checkApiDetail(api, userOrgIdList);
			}
		}
	}

	protected TsmpApi checkApiExists(String apiKey, String moduleName) {
		Optional<TsmpApi> opt = getTsmpApiDao().findById(new TsmpApiId(apiKey, moduleName));
		if (!opt.isPresent()) {
			this.logger.debug(String.format("API doesn't exist: %s-%s", moduleName, apiKey));
			throw TsmpDpAaRtnCode.NO_API_INFO.throwing();
		}
		return opt.get();
	}

	protected void checkApiOrg(AA0303Item api, List<String> userOrgIdList) {
		String apiOrgId = api.getTsmpApi().getOrgId();
		if (!StringUtils.isEmpty(apiOrgId) && !userOrgIdList.contains(apiOrgId)) {
			this.logger.debug(String.format("Violate organization principle: apiOrgId(%s) not in %s", apiOrgId,
					userOrgIdList.toString()));
			throw TsmpDpAaRtnCode.NO_API_INFO.throwing();
		}
	}

	protected void checkApiRegStatus(AA0303Item api) {
		String apiSrc = api.getTsmpApi().getApiSrc();
		String apiKey = api.getApiKey();
		String moduleName = api.getModuleName();
		if (TsmpApiSrc.COMPOSED.value().equals(apiSrc)) {
			// 若查無資料則 throw 1460
			Optional<TsmpApiReg> opt = getTsmpApiRegDao().findById(new TsmpApiRegId(apiKey, moduleName));
			if (!opt.isPresent() || !"1".equals(opt.get().getRegStatus())) {
				throw TsmpDpAaRtnCode._1460.throwing();
			}
		}
	}

	protected void checkApiStatus(AA0303Item api) {
		String apiStatus = api.getTsmpApi().getApiStatus();
		if ("1".equals(apiStatus)) {
			throw TsmpDpAaRtnCode._1463.throwing();
		}
	}

	protected void checkApiDetail(AA0303Item api, List<String> userOrgIdList) {
		String apiSrc = api.getTsmpApi().getApiSrc();
		String apiKey = api.getApiKey();
		String moduleName = api.getModuleName();
		if (TsmpApiSrc.JAVA_MODULE.value().equals(apiSrc)) {
			List<TsmpApiDetail> apiDetailList = getTsmpApiDetailDao().query_AA0303Service_01(apiKey, moduleName,
					userOrgIdList);
			if (!CollectionUtils.isEmpty(apiDetailList)) {
				throw TsmpDpAaRtnCode._1464.throwing();
			}
		} else if (TsmpApiSrc.NET_MODULE.value().equals(apiSrc)) {
			List<TsmpnApiDetail> nApiDetailList = getTsmpnApiDetailDao().query_AA0303Service_01(apiKey, moduleName,
					userOrgIdList);
			if (!CollectionUtils.isEmpty(nApiDetailList)) {
				throw TsmpDpAaRtnCode._1464.throwing();
			}
		} else if (TsmpApiSrc.REGISTERED.value().equals(apiSrc) || TsmpApiSrc.COMPOSED.value().equals(apiSrc)) {
			Optional<TsmpApiReg> opt = getTsmpApiRegDao().findById(new TsmpApiRegId(apiKey, moduleName));
			if (!opt.isPresent()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
		}
	}

	protected AA0303Resp checkAlert(AA0303Req req, String locale) {
		if (!"N".equals(req.getIgnoreAlert())) {
			return null;
		}

		List<AA0303Item> apiList = req.getApiList();

		String apiKey;
		String moduleName;
		String apiUid;
		String apiSrc;
		for (AA0303Item api : apiList) {
			apiKey = api.getApiKey();
			moduleName = api.getModuleName();
			apiUid = api.getTsmpApi().getApiUid();
			apiSrc = api.getTsmpApi().getApiSrc();

			// API 是否已下架
			if (isApiLaunched(apiKey, moduleName))
				return wrapResponse(TsmpDpAaRtnCode._1461.getCode(), locale);

			// 是否有與此 API 相關的申請單正在流程中
			if (isApiInProgress(apiUid))
				return wrapResponse(TsmpDpAaRtnCode._1462.getCode(), locale);

			// 若 API 來源為 "註冊" (TSMP_API.api_src = "R")，則檢查是否以上傳外部介接規格所註冊
			if (TsmpApiSrc.REGISTERED.value().equals(apiSrc) && isRegModule(moduleName))
				return wrapResponse(TsmpDpAaRtnCode._1465.getCode(), locale);
		}

		return null; // Pass
	}

	protected boolean isApiLaunched(String apiKey, String moduleName) {
		Optional<TsmpApiExt> opt = getTsmpApiExtDao().findById(new TsmpApiExtId(apiKey, moduleName));
		return (opt.isPresent() && "1".equals(opt.get().getDpStatus()));
	}

	protected boolean isApiInProgress(String apiUid) {
		List<TsmpDpReqOrders> sList = getTsmpDpReqOrdersDao().query_AA0303Service_01(apiUid);
		return !CollectionUtils.isEmpty(sList);
	}

	protected boolean isRegModule(String moduleName) {
		List<TsmpRegModule> mList = getTsmpRegModuleDao().findByModuleNameAndLatest(moduleName, "Y");
		return !CollectionUtils.isEmpty(mList);
	}

	protected void doDelete(String userName, String orgId, AA0303Req req, InnerInvokeParam iip) {
		List<AA0303Item> apiList = req.getApiList();
		String apiKey;
		String moduleName;
		List<TsmpGroup> gList;
		String apiSrc;
		TsmpApiRegId tsmpApiRegId;
		int uuidIndex = 1;
		String uuid = iip != null ? iip.getTxnUid() : null;
		for (AA0303Item api : apiList) {
			if (iip != null) {
				iip.setTxnUid(uuid + "_" + uuidIndex);
				uuidIndex++;
			}
			apiKey = api.getApiKey();
			moduleName = api.getModuleName();
			apiSrc = api.getTsmpApi().getApiSrc();

			// 寫入 Audit Log M
			String lineNumber = StackTraceUtil.getLineNumber();
			if (TsmpApiSrc.REGISTERED.value().equals(apiSrc)) {
				getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.DELETE_REGISTER_API.value());
			} else if (TsmpApiSrc.COMPOSED.value().equals(apiSrc)) {
				getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.DELETE_COMPOSER_API.value());
			}

			// 先將與此 API 相關的群組資料(由虛擬群組所建立)備份起來
			gList = backUpGroups(apiKey, moduleName);

			if (TsmpApiSrc.REGISTERED.value().equals(apiSrc) || TsmpApiSrc.COMPOSED.value().equals(apiSrc)) {
				tsmpApiRegId = new TsmpApiRegId(apiKey, moduleName);
				if (TsmpApiSrc.COMPOSED.value().equals(apiSrc)) {
					Optional<DgrComposerFlow> opt = getDgrComposerFlowDao().findByModuleNameAndApiId(moduleName,
							apiKey);
					if (opt.isPresent()) {
						// 刪除
						final DgrComposerFlow entity = opt.get();
						getDgrComposerFlowDao().delete(entity);
						// 寫入 Audit Log D
						lineNumber = StackTraceUtil.getLineNumber();
						String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, entity); // 舊資料統一轉成 String
						getDgrAuditLogService().createAuditLogD(iip, lineNumber, DgrComposerFlow.class.getSimpleName(),
								TableAct.D.value(), oldRowStr, null);// D
						// 需要判斷TPILogger.lc是否為null，因為在單元測試中，TPILogger.lc是沒有連接會是null。
						if (TPILogger.lc != null) {
							TPILogger.lc.send(new UpdateComposerTSPacket());
						}
					}
				}

				TsmpApiReg regVo = getTsmpApiRegDao().findById(tsmpApiRegId).orElse(null);
				getTsmpApiRegDao().delete(regVo);

				// 寫入 Audit Log D
				lineNumber = StackTraceUtil.getLineNumber();
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, regVo); // 舊資料統一轉成 String
				getDgrAuditLogService().createAuditLogD(iip, lineNumber, TsmpApiReg.class.getSimpleName(),
						TableAct.D.value(), oldRowStr, null);// D
			}

			TsmpApi apiVo = getTsmpApiDao().findById(new TsmpApiId(apiKey, moduleName)).orElse(null);
			getTsmpApiDao().delete(apiVo);

			if (TsmpApiSrc.REGISTERED.value().equals(apiSrc) || TsmpApiSrc.COMPOSED.value().equals(apiSrc)) {
				// 寫入 Audit Log D
				lineNumber = StackTraceUtil.getLineNumber();
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, apiVo); // 舊資料統一轉成 String
				getDgrAuditLogService().createAuditLogD(iip, lineNumber, TsmpApi.class.getSimpleName(),
						TableAct.D.value(), oldRowStr, null);// D
			}

			getTsmpGroupApiDao().deleteByApiKeyAndModuleName(apiKey, moduleName);

			// 因為建立虛擬群組時，一支 API 就會建立一個 TSMP_GROUP，因此刪除 API 時，就要連帶刪除與此 TSMP_GROUP 相關的資料
			cascadeDeleteGroups(gList);
						
			cascadeDeleteWebhookNotify(apiKey, moduleName);
		}
	}

	protected ApiListAndAlgorithmClassifier doUpdate(String userName, AA0303Req req, InnerInvokeParam iip,
			ReqHeader reqHeader) {
		String req_apiStatus = req.getApiStatus();
		String jweFlag = req.getJweFlag();
		String jweFlagResp = req.getJweFlagResp();
		List<AA0303Item> apiList = req.getApiList();

		ApiListAndAlgorithmClassifier classifier = new ApiListAndAlgorithmClassifier();

		int updCnt = 0;
		TsmpApi tsmpApi;
		int uuidIndex = 1;
		String uuid = iip != null ? iip.getTxnUid() : null;
		for (AA0303Item api : apiList) {
			if (iip != null) {
				iip.setTxnUid(uuid + "_" + uuidIndex);
				uuidIndex++;
			}
			tsmpApi = api.getTsmpApi();
			// 寫入 Audit Log M
			String lineNumber = StackTraceUtil.getLineNumber();
			if (TsmpApiSrc.REGISTERED.value().equals(tsmpApi.getApiSrc())) {
				getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_REGISTER_API.value());
			} else if (TsmpApiSrc.COMPOSED.value().equals(tsmpApi.getApiSrc())) {
				getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_COMPOSER_API.value());
			}
			String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpApi); // 舊資料統一轉成 String

			// api_status
			if (!StringUtils.isEmpty(req_apiStatus)) {
				classifier = setApiStatus(tsmpApi, req_apiStatus, req, classifier);
				classifier.getAlgorithm().setTsmpApi(tsmpApi);
			}

			// jwe_flag
			if (!StringUtils.isEmpty(jweFlag)) {
				tsmpApi.setJewFlag(jweFlag);
			}
			// jwe_flag_resp
			if (!StringUtils.isEmpty(jweFlagResp)) {
				tsmpApi.setJewFlagResp(jweFlagResp);
			}
			// update_time, update_user
			tsmpApi.setUpdateTime(DateTimeUtil.now());
			tsmpApi.setUpdateUser(userName);
			try {
				tsmpApi = getTsmpApiDao().saveAndFlush(tsmpApi);
				updCnt++;
				if (TsmpApiSrc.REGISTERED.value().equals(tsmpApi.getApiSrc())
						|| TsmpApiSrc.COMPOSED.value().equals(tsmpApi.getApiSrc())) {
					// 寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogD(iip, lineNumber, TsmpApi.class.getSimpleName(),
							TableAct.U.value(), oldRowStr, tsmpApi);
				}
			} catch (Exception e) {
				this.logger.debug(String.format("Update API error: apiKey=%s, moduleName=%s, msg=\n%s", //
						api.getApiKey(), api.getModuleName(), StackTraceUtil.logStackTrace(e)));
			}
		}

		if (updCnt != apiList.size()) {
			this.logger.debug(String.format("Unexpected error has occurred: expected count=%d, actual count=%d", //
					apiList.size(), updCnt));
			throw TsmpDpAaRtnCode._1286.throwing();
		}

		return classifier;
	}

	private ApiListAndAlgorithmClassifier setApiStatus(TsmpApi tsmpApi, String reqApiStatus, AA0303Req req,
			ApiListAndAlgorithmClassifier classifier) {

		long scheduledDate = req.getScheduledDate();
		scheduledDate = TimestampConverterUtil.getDateOnlyTimestamp(scheduledDate);
		checkScheduledDate(scheduledDate);

		ApiPublicFlagHandlerData data = new ApiPublicFlagHandlerData();
		data.setDisableScheduledDate(scheduledDate);
		data.setEnableScheduledDate(scheduledDate);
		data.setStatus(req.getApiStatus());
		data.setApiDisableScheduledDate(tsmpApi.getDisableScheduledDate());
		data.setApiEnableScheduledDate(tsmpApi.getEnableScheduledDate());
		data.setApiStatus(tsmpApi.getApiStatus());

		switch (reqApiStatus.toLowerCase()) {

		case "1":
			return getEnableProcessingFlow().handle(data, classifier, tsmpApi);

		case "2":
			return getDisableProcessingFlow().handle(data, classifier, tsmpApi);

		default:
			String errMsg = "Invalid API status: " + reqApiStatus;
			this.logger.error(errMsg);

			classifier.setAlgorithm(api -> {
			});

			return classifier;
		}
	}

	private void checkScheduledDate(long scheduledDate) {
		if (scheduledDate != 0) {
			scheduledDate = TimestampConverterUtil.getDateOnlyTimestamp(scheduledDate);
			long today = TimestampConverterUtil.getDateOnlyTimestamp(System.currentTimeMillis());
			if (scheduledDate <= today) {
				// _1549(TsmpDpModule.DP5, "49", "設定啟用停用日期不可為今日")
				throw TsmpDpAaRtnCode._1549.throwing();
			}
		}
	}

	protected List<TsmpGroup> backUpGroups(String apiKey, String moduleName) {
		return getTsmpGroupDao().query_AA0303Service_01(apiKey, moduleName);
	}

	protected void cascadeDeleteGroups(List<TsmpGroup> gList) {
		String groupId;
		for (TsmpGroup g : gList) {
			groupId = g.getGroupId();

			// TSMP_VGROUP_GROUP
			getTsmpVgroupGroupDao().findByGroupId(groupId).forEach((vgg) -> {
				getTsmpVgroupGroupDao().delete(vgg);
			});

			// TSMP_GROUP_AUTHORITIES_MAP
			getTsmpGroupAuthoritiesMapDao().deleteByGroupId(groupId);

			// TSMP_CLIENT_GROUP
			getTsmpClientGroupDao().findByGroupId(groupId).forEach((cg) -> {
				getTsmpClientGroupDao().delete(cg);
			});

			// TSMP_GROUP (雙欄位更新)
			getTsmpGroupDao().deleteByGroupIdAndGroupName(groupId, g.getGroupName());
		}
	}
	
	private void cascadeDeleteWebhookNotify(String apiKey, String moduleName) {
		List<DgrWebhookApiMap> mList = getDgrWebhookApiMapDao().findByApiKeyAndModuleName(apiKey, moduleName);
		getDgrWebhookApiMapDao().deleteAll(mList);
	}

	private String getErrMsg(TsmpDpAaRtnCode tsmpDpAaRtnCode, String locale) {

		if (tsmpDpAaRtnCode == null) {
			return null;
		}

		Optional<ITsmpRtnCode> opt = getTsmpRtnCodeDao().findByTsmpRtnCodeAndLocale(tsmpDpAaRtnCode.getCode(), locale);
		if (opt.isEmpty()) {
			opt = getTsmpRtnCodeDao().findByTsmpRtnCodeAndLocale(tsmpDpAaRtnCode.getCode(), LocaleType.EN_US);
		}
		return opt.isPresent() ? opt.get().getTsmpRtnMsg() : null;
	}

	protected void clearAPICache() {
		/*
		 * 20221130 改用 Keeper 通知其他節點 NoticeClearCacheEventsJob job =
		 * getNoticeClearCacheEventsJob("TSMP_API", "TSMP_API_REG");
		 * getJobHelper().add(job);
		 */
		getDaoGenericCacheService().clearAndNotify();
	}

	protected AA0303Resp wrapResponse(String rtnCode, String locale) {
		AA0303Resp resp = new AA0303Resp();
		resp.setMsg(getTsmpRtnCodeById(rtnCode, locale));
		return resp;
	}

	/**
	 * Cache版
	 */
	protected String getTsmpRtnCodeById(String rtnCode, String locale) {
		TsmpRtnCode c = getTsmpRtnCodeCacheProxy().findById(rtnCode, locale).orElse(null);
		if (c != null) {
			return c.getTsmpRtnMsg();
		}
		return new String();
	}

	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected TsmpApiRegDao getTsmpApiRegDao() {
		return this.tsmpApiRegDao;
	}

	protected TsmpApiDetailDao getTsmpApiDetailDao() {
		return this.tsmpApiDetailDao;
	}

	protected TsmpnApiDetailDao getTsmpnApiDetailDao() {
		return this.tsmpnApiDetailDao;
	}

	protected TsmpRtnCodeCacheProxy getTsmpRtnCodeCacheProxy() {
		return this.tsmpRtnCodeCacheProxy;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected TsmpApiExtDao getTsmpApiExtDao() {
		return this.tsmpApiExtDao;
	}

	protected TsmpDpReqOrdersDao getTsmpDpReqOrdersDao() {
		return this.tsmpDpReqOrdersDao;
	}

	protected TsmpRegModuleDao getTsmpRegModuleDao() {
		return this.tsmpRegModuleDao;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return this.tsmpGroupDao;
	}

	protected TsmpGroupApiDao getTsmpGroupApiDao() {
		return this.tsmpGroupApiDao;
	}

	protected TsmpVgroupGroupDao getTsmpVgroupGroupDao() {
		return this.tsmpVgroupGroupDao;
	}

	protected TsmpGroupAuthoritiesMapDao getTsmpGroupAuthoritiesMapDao() {
		return this.tsmpGroupAuthoritiesMapDao;
	}

	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return this.tsmpClientGroupDao;
	}

	protected NoticeClearCacheEventsJob getNoticeClearCacheEventsJob(String tableName1, String tableName2) {
		Integer action = Integer.valueOf(2);
		List<String> tableNameList = new ArrayList<>();
		tableNameList.add(tableName1);
		tableNameList.add(tableName2);
		return (NoticeClearCacheEventsJob) getCtx().getBean("noticeClearCacheEventsJob", action, null, tableNameList);
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}

	protected DgrComposerFlowDao getDgrComposerFlowDao() {
		return this.dgrComposerFlowDao;
	}

	protected DaoGenericCacheService getDaoGenericCacheService() {
		return daoGenericCacheService;
	}

	protected TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return this.tsmpRtnCodeDao;
	}

	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}
}