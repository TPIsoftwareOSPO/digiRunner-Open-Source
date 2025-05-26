package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.isValueTooLargeException;
import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.RegexpConstant;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.job.NoticeClearCacheEventsJob;
import tpi.dgrv4.dpaa.constant.TsmpApiSrc;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0313Func;
import tpi.dgrv4.dpaa.vo.AA0313KeyVal;
import tpi.dgrv4.dpaa.vo.AA0313RedirectByIpData;
import tpi.dgrv4.dpaa.vo.AA0313Req;
import tpi.dgrv4.dpaa.vo.AA0313Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.TsmpApiRegId;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.jpql.TsmpRegModule;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpRegModuleDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.CommForwardProcService;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0313Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpUserDao tsmpUserDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpApiRegDao tsmpApiRegDao;
	private TsmpRegModuleDao tsmpRegModuleDao;
	private BcryptParamHelper bcryptParamHelper;
	private ApplicationContext ctx;
	private JobHelper jobHelper;
	private ObjectMapper objectMapper;
	private DgrAuditLogService dgrAuditLogService;
	private CommForwardProcService commForwardProcService;
	private DaoGenericCacheService daoGenericCacheService;
	private DgrAcIdpUserDao dgrAcIdpUserDao;

	@Autowired
	public AA0313Service(TsmpUserDao tsmpUserDao, TsmpOrganizationDao tsmpOrganizationDao, TsmpApiDao tsmpApiDao,
			TsmpApiRegDao tsmpApiRegDao, TsmpRegModuleDao tsmpRegModuleDao, BcryptParamHelper bcryptParamHelper,
			ApplicationContext ctx, JobHelper jobHelper, ObjectMapper objectMapper,
			DgrAuditLogService dgrAuditLogService, CommForwardProcService commForwardProcService,
			DaoGenericCacheService daoGenericCacheService, DgrAcIdpUserDao dgrAcIdpUserDao) {
		super();
		this.tsmpUserDao = tsmpUserDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpApiRegDao = tsmpApiRegDao;
		this.tsmpRegModuleDao = tsmpRegModuleDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.ctx = ctx;
		this.jobHelper = jobHelper;
		this.objectMapper = objectMapper;
		this.dgrAuditLogService = dgrAuditLogService;
		this.commForwardProcService = commForwardProcService;
		this.daoGenericCacheService = daoGenericCacheService;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
	}

	@Transactional
	public AA0313Resp updateRegCompAPI(TsmpAuthorization auth, AA0313Req req, ReqHeader reqHeader, InnerInvokeParam iip) {
		String userName = auth.getUserName();
		String userNameForQuery = auth.getUserNameForQuery();
		String orgId = auth.getOrgId();
		String idPType = auth.getIdpType();
		String srcUrl = req.getSrcUrl();
		String locale = ServiceUtil.getLocale(reqHeader.getLocale());

		Pair<TsmpApi, TsmpApiReg> apiPair = checkParams(userName, orgId, req, locale, idPType, userNameForQuery);

		Boolean redirectByIp = req.getRedirectByIp();
		if (redirectByIp == null) {
			redirectByIp = false;
		}
		if (!redirectByIp) {
			checkSrcUrl(srcUrl);
		} else {
			List<AA0313RedirectByIpData> redirectByIpDataList = req.getRedirectByIpDataList();

			for (AA0313RedirectByIpData aa0313RedirectByIpData : redirectByIpDataList) {
				checkSrcUrl(aa0313RedirectByIpData.getIpSrcUrl());
			}
		}

		try {
			updateApi(userName, req, apiPair, iip);
			clearAPICache();
			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.API.value());
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			if (isValueTooLargeException(e)) {
				throw TsmpDpAaRtnCode._1220.throwing();
			}
			throw TsmpDpAaRtnCode._1286.throwing();
		}
		return new AA0313Resp();
	}

	protected Pair<TsmpApi, TsmpApiReg> checkParams(String userName, String orgId, AA0313Req req, String locale,
			String idPType, String userNameForQuery) {

		if (!StringUtils.hasLength(userName)) {
			throw TsmpDpAaRtnCode._1258.throwing();
		}

		if (!StringUtils.hasLength(userNameForQuery)) {
			throw TsmpDpAaRtnCode._1258.throwing();
		}

		if (!StringUtils.hasLength(orgId)) {
			throw TsmpDpAaRtnCode._1273.throwing();
		}

		if (CollectionUtils.isEmpty(req.getMethodOfJson())) {
			throw TsmpDpAaRtnCode._1503.throwing();
		}

		// 若傳入空值則後端轉為空陣列
		List<String> methodOfJson = req.getMethodOfJson();
		if (CollectionUtils.isEmpty(methodOfJson)) {
			req.setMethodOfJson(Collections.emptyList());
		}

		AA0313Func funFlag = req.getFunFlag();
		if (funFlag == null || funFlag.getTokenPayload() == null) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		String jweFlag = req.getJweFlag();
		String jweFlagResp = req.getJweFlagResp();
		String dataFormat = req.getDataFormat();
		String apiCacheFlag = req.getApiCacheFlag();
		try {
			jweFlag = getBcryptParamHelper().decode(jweFlag, "API_JWT_FLAG", locale);
			jweFlagResp = getBcryptParamHelper().decode(jweFlagResp, "API_JWT_FLAG", locale);
			dataFormat = getBcryptParamHelper().decode(dataFormat, "API_DATA_FORMAT", locale);
			apiCacheFlag = getBcryptParamHelper().decode(apiCacheFlag, "API_CACHE_FLAG", locale);
			req.setJweFlag(jweFlag);
			req.setJweFlagResp(jweFlagResp);
			req.setDataFormat(dataFormat);
			req.setApiCacheFlag(apiCacheFlag);
		} catch (BcryptParamDecodeException e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		
		if("3".equals(apiCacheFlag)) {
			if(req.getFixedCacheTime() == null) {
				throw TsmpDpAaRtnCode._1350.throwing("{{fixedCacheTime}}");
			}
			
			if(req.getFixedCacheTime().intValue() < 0) {
				throw TsmpDpAaRtnCode._1355.throwing("{{fixedCacheTime}}", "0", req.getFixedCacheTime().toString());
			}
			int max = 60 * 24 * 366;
			if(req.getFixedCacheTime().intValue() > max) {
				throw TsmpDpAaRtnCode._1356.throwing("{{fixedCacheTime}}", max + "", req.getFixedCacheTime().toString());
			}
		}

		checkUserExists(userNameForQuery, idPType);

		List<String> userOrgIdList = checkOrgExists(orgId);

		String apiKey = req.getApiKey();
		String moduleName = req.getModuleName();
		TsmpApi tsmpApi = checkApiExists(apiKey, moduleName);
		TsmpApiReg tsmpApiReg = checkApiRegExists(apiKey, moduleName);

		String apiSrc = tsmpApi.getApiSrc();
		if (!(TsmpApiSrc.REGISTERED.value().equals(apiSrc) || TsmpApiSrc.COMPOSED.value().equals(apiSrc))) {
			throw TsmpDpAaRtnCode._1466.throwing();
		}

		// API組織原則
		checkApiOrg(tsmpApi, userOrgIdList);
		// Mock 狀態碼、Headers、Body
		checkApiMockSetting(req);

		// 組合API部署狀態
		String req_apiStatus = req.getApiStatus();
		checkRegStatus(tsmpApiReg, req_apiStatus);

		Boolean isRedirectByIp = req.getRedirectByIp();

		if (TsmpApiSrc.REGISTERED.value().equals(apiSrc)) {
			if (isRedirectByIp == null) {
				throw TsmpDpAaRtnCode._1350.throwing("{{redirectByIp}}");
			}

			if (isRedirectByIp) {
				List<AA0313RedirectByIpData> redirectByIpDataList = req.getRedirectByIpDataList();
				if (redirectByIpDataList == null || redirectByIpDataList.size() == 0) {
					throw TsmpDpAaRtnCode._1350.throwing("{{redirectByIpDataList}}");
				}
				if (redirectByIpDataList.size() > 5) {
					throw TsmpDpAaRtnCode._1407.throwing("{{redirectByIpDataList}}", "5",
							String.valueOf(redirectByIpDataList.size()));
				}
				int i=0;
				if (redirectByIpDataList.size()>0) {
					for (AA0313RedirectByIpData data : redirectByIpDataList) {
						String ip = data.getIpForRedirect();
						if (!StringUtils.hasLength(ip)) {
							throw TsmpDpAaRtnCode._1350.throwing("{{ipForRedirect}}");
						}
						if (!ip.matches(RegexpConstant.IP_CIDR_FQDN)){
							throw  TsmpDpAaRtnCode._1352.throwing("{{ipForRedirect}}");
						}
						if ((ip.contains("0.0.0.0/0") || ip.contains("::/0")) && i != redirectByIpDataList.size() - 1) {
							throw TsmpDpAaRtnCode._1559.throwing("If you want to allow all IPs, please set it as the last item.");
						}
						if (!StringUtils.hasLength(data.getIpSrcUrl())) {
							throw TsmpDpAaRtnCode._1350.throwing("{{ipSrcUrl}}");
						}
						i++;
					}
				}
			}

			String bodyMaskPolicy = req.getBodyMaskPolicy();
			if (!StringUtils.hasLength(bodyMaskPolicy)) {
				throw TsmpDpAaRtnCode._1350.throwing("{{bodyMaskPolicy}}");
			}
			
			if (!bodyMaskPolicy.equals("0")) {
				if (!StringUtils.hasLength(req.getBodyMaskKeyword())) {
					throw TsmpDpAaRtnCode._1350.throwing("{{bodyMaskKeyword}}");
				}
				if (!StringUtils.hasLength(req.getBodyMaskPolicySymbol())) {
					throw TsmpDpAaRtnCode._1350.throwing("{{bodyMaskPolicySymbol}}");
				}
				if (req.getBodyMaskPolicySymbol().length() > 10) {
					throw TsmpDpAaRtnCode._1351.throwing("{{bodyMaskPolicySymbol}}", String.valueOf(10),
							String.valueOf(req.getBodyMaskPolicySymbol().length()));
				}

				if (req.getBodyMaskPolicyNum() <= 0) {
					throw TsmpDpAaRtnCode._1355.throwing("{{bodyMaskPolicyNum}}", String.valueOf(1),
							String.valueOf(req.getBodyMaskPolicyNum()));
				}
				if (req.getBodyMaskPolicyNum() > 9999) {
					throw TsmpDpAaRtnCode._1355.throwing("{{bodyMaskPolicyNum}}", String.valueOf(9999),
							String.valueOf(req.getBodyMaskPolicyNum()));
				}
			}
			
			String headerMaskPolicy = req.getHeaderMaskPolicy();
			if (!StringUtils.hasLength(headerMaskPolicy)) {
				throw TsmpDpAaRtnCode._1350.throwing("{{headerMaskPolicy}}");
			}

			if (!headerMaskPolicy.equals("0")) {
				if (!StringUtils.hasLength(req.getHeaderMaskKey())) {
					throw TsmpDpAaRtnCode._1350.throwing("{{headerMaskKey}}");
				}
				if (!StringUtils.hasLength(req.getHeaderMaskPolicySymbol())) {
					throw TsmpDpAaRtnCode._1350.throwing("{{headerMaskPolicySymbol}}");
				}
				if (req.getHeaderMaskPolicySymbol().length() > 10) {
					throw TsmpDpAaRtnCode._1351.throwing("{{headerMaskPolicySymbol}}", String.valueOf(10),
							String.valueOf(req.getHeaderMaskPolicySymbol().length()));
				}
				if (req.getHeaderMaskPolicyNum() <= 0) {
					throw TsmpDpAaRtnCode._1355.throwing("{{headerMaskPolicyNum}}", String.valueOf(1),
							String.valueOf(req.getHeaderMaskPolicyNum()));
				}
				if (req.getHeaderMaskPolicyNum() > 9999) {
					throw TsmpDpAaRtnCode._1355.throwing("{{headerMaskPolicyNum}}", String.valueOf(9999),
							String.valueOf(req.getHeaderMaskPolicyNum()));
				}
			}
		}
		
		String type = AA0302Service.getType(apiSrc, moduleName, apiKey);// 模式
		if (TsmpApiSrc.REGISTERED.value().equals(apiSrc) && "1".equals(type)) {
			// 當為 註冊 且 dgrc 時, 必須有值
			// 失敗判定策略
			if (!StringUtils.hasLength(req.getFailDiscoveryPolicy())) {
				throw TsmpDpAaRtnCode._1350.throwing("{{failDiscoveryPolicy}}");
			}

			// 失敗處置策略
			if (!StringUtils.hasLength(req.getFailHandlePolicy())) {
				throw TsmpDpAaRtnCode._1350.throwing("{{failHandlePolicy}}");
			}
		}
		
		List<String> labelList = req.getLabelList();
		if (labelList != null) {
			if (labelList.size() > 5)
				throw TsmpDpAaRtnCode._1407.throwing("{{labelList}}", "5", String.valueOf(labelList.size()));
			if (labelList.size() > 0) {
				labelList.forEach(l -> {
					if (l.length() > 20) {
						throw TsmpDpAaRtnCode._1351.throwing("{{labelList}}", "20", String.valueOf(l.length()));
					}
				});
			}
		}
		return Pair.of(tsmpApi, tsmpApiReg);
	}

	protected void checkApiMockSetting(AA0313Req req) {

		// 取出值
		String mockStatusCode = req.getMockStatusCode();
		List<AA0313KeyVal> mockHeaders = req.getMockHeaders();
		String mockBody = req.getMockBody();
		// 是否空值
		boolean hasMockStatusCode = StringUtils.hasText(mockStatusCode);
		boolean hasMockBody = StringUtils.hasText(mockBody);
		boolean hasMockHeaders = mockHeaders != null && !mockHeaders.isEmpty();
		
		// 檢查值是否符合規則
		isHeadersOrBodyTogetherWithMockStatusCode(hasMockStatusCode, hasMockBody, hasMockHeaders);
		if (hasMockHeaders) { // 當 mockHeaders 為空時，略過檢查
			isMockHeadersHasKeyValue(mockHeaders);
			isMockHeadersJsonTooLong(mockHeaders);
		}
		
		// 統一 DB 存入數值，當值為 Null、空字串、空白字串 時，一律改為 Null
		if (!hasMockStatusCode) {
			req.setMockStatusCode(null);
		}
		if (!hasMockBody) {
			req.setMockBody(null);
		}
		if (!hasMockHeaders) {
			req.setMockHeaders(null);
		}
	}
	
	/*
	 * 當 MockStatusCode 為空值時，MockBody 或 MockHeaders 必須為空
	 * 當 MockStatusCode 有值時，MockBody 或 MockHeaders 至少一個必須有值
	 */
	private void isHeadersOrBodyTogetherWithMockStatusCode( //
			boolean hasMockStatusCode, //
			boolean hasMockBody, //
			boolean hasMockHeaders) {

		// MockBody 或 MockHeaders 是否至少一個有值
		boolean hasHeadersOrBody = hasMockBody || hasMockHeaders;

		if (hasHeadersOrBody && !hasMockStatusCode) {
			throw TsmpDpAaRtnCode._1350.throwing("{{mockStatusCode}}");
		}
		
	}
	
	// 檢查mockHeaders的key和value是否都有值
	private void isMockHeadersHasKeyValue(List<AA0313KeyVal> mockHeaders) {

		mockHeaders.forEach(header -> {
			String key = header.getKey();
			String value = header.getValue();

			boolean hasKey = StringUtils.hasText(key);
			boolean hasValue = StringUtils.hasText(value);

			if (!hasKey) {
				throw TsmpDpAaRtnCode._1350.throwing("Mock Headers Key");
			}

			if (!hasValue) {
				throw TsmpDpAaRtnCode._1350.throwing("Mock Headers Vaule");
			}
		});
	}

	// 檢查mockHeaders有值轉換成json時,長度是否超過2000
	private void isMockHeadersJsonTooLong(List<AA0313KeyVal> mockHeaders) {
		String json = "";
		try {
			json = getObjectMapper().writeValueAsString(mockHeaders);
		} catch (JsonProcessingException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}

		if (json.length() >= 2001) {
			throw TsmpDpAaRtnCode._1351.throwing("Mock Headers Key And Value", "2000", json.length() + "");
		}
	}
	
	protected void checkUserExists(String userNameForQuery, String idPType) {
		if (StringUtils.hasLength(idPType)) {// 以 IdP 登入 AC
			DgrAcIdpUser dgrAcIdpUser = getDgrAcIdpUserDao().findFirstByUserNameAndIdpType(userNameForQuery, idPType);
			if (dgrAcIdpUser == null) {
				//Table 查不到 user
				TPILogger.tl.debug("Table [DGR_AC_IDP_USER] can not find user, user_name: " + userNameForQuery + ", idp_type: " + idPType);
				throw TsmpDpAaRtnCode._1231.throwing();
			}
			
		} else {//以 AC 登入
			TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userNameForQuery);
			
			if (tsmpUser == null) {
				//Table 查不到 user
				TPILogger.tl.debug("Table [TSMP_USER] can not find user, user_name: " + userNameForQuery);
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

	protected TsmpApi checkApiExists(String apiKey, String moduleName) {
		Optional<TsmpApi> opt = getTsmpApiDao().findById(new TsmpApiId(apiKey, moduleName));
		if (!opt.isPresent()) {
			this.logger.debug(String.format("API doesn't exist: %s-%s", moduleName, apiKey));
			throw TsmpDpAaRtnCode.NO_API_INFO.throwing();
		}
		return opt.get();
	}

	protected TsmpApiReg checkApiRegExists(String apiKey, String moduleName) {
		Optional<TsmpApiReg> opt = getTsmpApiRegDao().findById(new TsmpApiRegId(apiKey, moduleName));
		if (!opt.isPresent()) {
			this.logger.debug(String.format("API REG doesn't exist: %s-%s", moduleName, apiKey));
			throw TsmpDpAaRtnCode.NO_API_INFO.throwing();
		}
		return opt.get();
	}

	protected void checkApiOrg(TsmpApi tsmpApi, List<String> userOrgIdList) {
		String apiOrgId = tsmpApi.getOrgId();
		if (StringUtils.hasLength(apiOrgId) && !userOrgIdList.contains(apiOrgId)) {
			this.logger.debug(String.format("Violate organization principle: apiOrgId(%s) not in %s", apiOrgId, userOrgIdList));
			throw TsmpDpAaRtnCode.NO_API_INFO.throwing();
		}
	}

	protected void checkRegStatus(TsmpApiReg tsmpApiReg, String req_apiStatus) {
		if ("1".equals(req_apiStatus) && !"1".equals(tsmpApiReg.getRegStatus())) {
			throw TsmpDpAaRtnCode._1460.throwing();
		}
	}

	protected boolean isRegModule(Pair<TsmpApi, TsmpApiReg> apiPair) {
		TsmpApi tsmpApi = apiPair.getFirst();
		String apiSrc = tsmpApi.getApiSrc();
		if (TsmpApiSrc.REGISTERED.value().equals(apiSrc)) {
			String moduleName = tsmpApi.getModuleName();
			List<TsmpRegModule> mList = getTsmpRegModuleDao().findByModuleName(moduleName);
			return !CollectionUtils.isEmpty(mList);
		}
		return false;
	}
	
	protected LinkedList<String[]> checkSrcUrl(String srcUrl) {
		boolean isThrowing = true;
		LinkedList<String[]> dataList = (LinkedList<String[]>) getCommForwardProcService().getSrcUrlToTargetUrlAndProbabilityList(srcUrl, isThrowing);
		return dataList;
	}
	
	protected void updateApi(String userName, AA0313Req req, Pair<TsmpApi, TsmpApiReg> apiPair, InnerInvokeParam iip) {
		boolean isRegModule = isRegModule(apiPair);
		
		// 更新 TSMP_API
		TsmpApi tsmpApi = apiPair.getFirst();
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		if (TsmpApiSrc.COMPOSED.value().equals(tsmpApi.getApiSrc())) {
			getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_COMPOSER_API.value());
		} else if(TsmpApiSrc.REGISTERED.value().equals(tsmpApi.getApiSrc())){
			getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_REGISTER_API.value());
		}
		
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpApi); //舊資料統一轉成 String
		
		tsmpApi.setUpdateTime(DateTimeUtil.now());
		tsmpApi.setUpdateUser(userName);
		tsmpApi.setApiDesc(req.getApiDesc());
		tsmpApi.setApiStatus(req.getApiStatus());
		tsmpApi.setDataFormat(req.getDataFormat());
		tsmpApi.setJewFlag(req.getJweFlag());
		tsmpApi.setJewFlagResp(req.getJweFlagResp());
		tsmpApi.setApiCacheFlag(req.getApiCacheFlag());
		tsmpApi.setApiName(req.getApiName());
		tsmpApi.setMockStatusCode(req.getMockStatusCode());
		tsmpApi.setMockBody(req.getMockBody());
		if("3".equals(req.getApiCacheFlag())) {
			tsmpApi.setFixedCacheTime(req.getFixedCacheTime());
		}
		
		try {
			if (req.getMockHeaders() == null) {
				tsmpApi.setMockHeaders(null);
			}else {
				tsmpApi.setMockHeaders(getObjectMapper().writeValueAsString(req.getMockHeaders()));
			}
		} catch (JsonProcessingException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
		
		List<String> labList = req.getLabelList();
		String[] label = new String[5];

		if (labList != null && labList.size() > 0) {
			for (int i = 0; i < labList.size(); i++) {
				label[i] = (labList.get(i).toLowerCase());

			}

		}
		tsmpApi.setLabel1(label[0]);
		tsmpApi.setLabel2(label[1]);
		tsmpApi.setLabel3(label[2]);
		tsmpApi.setLabel4(label[3]);
		tsmpApi.setLabel5(label[4]);
		tsmpApi = getTsmpApiDao().saveAndFlush(tsmpApi);
		
		if (TsmpApiSrc.COMPOSED.value().equals(tsmpApi.getApiSrc()) 
				|| TsmpApiSrc.REGISTERED.value().equals(tsmpApi.getApiSrc())) {
			//寫入 Audit Log D
			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
					TsmpApi.class.getSimpleName(), TableAct.U.value(), oldRowStr, tsmpApi);
		}
		
		
		String apiSrc = tsmpApi.getApiSrc();
		// 更新 TSMP_API_REG
		TsmpApiReg tsmpApiReg = apiPair.getSecond();
		
		oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpApi); //舊資料統一轉成 String
		
		tsmpApiReg.setUpdateTime(DateTimeUtil.now());
		tsmpApiReg.setUpdateUser(userName);
		
		tsmpApiReg.setUrlRid( (req.getUrlRID() ? "1" : "0") );
		tsmpApiReg.setNoOauth( (req.getNoOAuth() ? "1" : "0") );
		tsmpApiReg.setFunFlag( req.getFunFlag().convert() );
		Boolean isRedirectByIp = req.getRedirectByIp();
		if (!isRegModule) {
			if (!TsmpApiSrc.COMPOSED.value().equals(apiSrc)) {
				String protocol = req.getProtocol();
				if (!isRedirectByIp) {
					String srcUrl = req.getSrcUrl();
					if (StringUtils.hasLength(protocol)) {
						// 若srcUrl值不是"b64.", 也不是"http"開頭, 才需要加協定
						int index = srcUrl.indexOf("b64.");
						int index2 = srcUrl.indexOf("http");
						if (index != 0 && index2 != 0) {// 不是"b64.", 也不是"http"開頭
							srcUrl = String.format("%s://%s", protocol, srcUrl);
						}
					}

					tsmpApiReg.setSrcUrl(srcUrl);
				} else {
					tsmpApiReg.setSrcUrl(null);
				}
				
			}
			
			try {
				String methodOfJson = getObjectMapper().writeValueAsString(req.getMethodOfJson());
				tsmpApiReg.setMethodOfJson(methodOfJson);
			} catch (Exception e) {
				this.logger.debug(String.format("Method of json convert error: %s-%s-%s", tsmpApiReg.getModuleName(), tsmpApiReg.getApiKey(), req.getMethodOfJson()));
				this.logger.error(StackTraceUtil.logStackTrace(e));
			}
		}
		
		if (TsmpApiSrc.REGISTERED.value().equals(apiSrc)) {
			tsmpApiReg.setRedirectByIp(isRedirectByIp ? "Y" : "N");

			List<AA0313RedirectByIpData> redirectByIpDataList = req.getRedirectByIpDataList();
			if (redirectByIpDataList != null && redirectByIpDataList.size() > 0) {
				AA0313RedirectByIpData ipList1 = redirectByIpDataList.get(0);
				tsmpApiReg.setIpForRedirect1(ipList1.getIpForRedirect());
				tsmpApiReg.setIpSrcUrl1(ipList1.getIpSrcUrl());
			} else {
				tsmpApiReg.setIpForRedirect1(null);
				tsmpApiReg.setIpSrcUrl1(null);
			}
			if (redirectByIpDataList != null && redirectByIpDataList.size() > 1) {
				AA0313RedirectByIpData ipList2 = redirectByIpDataList.get(1);
				if (ipList2 != null) {
					tsmpApiReg.setIpForRedirect2(ipList2.getIpForRedirect());
					tsmpApiReg.setIpSrcUrl2(ipList2.getIpSrcUrl());
				}

			} else {
				tsmpApiReg.setIpForRedirect2(null);
				tsmpApiReg.setIpSrcUrl2(null);
			}
			if (redirectByIpDataList != null && redirectByIpDataList.size() > 2) {
				AA0313RedirectByIpData ipList3 = redirectByIpDataList.get(2);
				if (ipList3 != null) {
					tsmpApiReg.setIpForRedirect3(ipList3.getIpForRedirect());
					tsmpApiReg.setIpSrcUrl3(ipList3.getIpSrcUrl());
				}

			} else {
				tsmpApiReg.setIpForRedirect3(null);
				tsmpApiReg.setIpSrcUrl3(null);
			}
			if (redirectByIpDataList != null && redirectByIpDataList.size() > 3) {
				AA0313RedirectByIpData ipList4 = redirectByIpDataList.get(3);
				if (ipList4 != null) {
					tsmpApiReg.setIpForRedirect4(ipList4.getIpForRedirect());
					tsmpApiReg.setIpSrcUrl4(ipList4.getIpSrcUrl());
				}

			} else {
				tsmpApiReg.setIpForRedirect4(null);
				tsmpApiReg.setIpSrcUrl4(null);
			}
			if (redirectByIpDataList != null && redirectByIpDataList.size() > 4) {
				AA0313RedirectByIpData ipList5 = redirectByIpDataList.get(4);
				if (ipList5 != null) {
					tsmpApiReg.setIpForRedirect5(ipList5.getIpForRedirect());
					tsmpApiReg.setIpSrcUrl5(ipList5.getIpSrcUrl());
				}

			} else {
				tsmpApiReg.setIpForRedirect5(null);
				tsmpApiReg.setIpSrcUrl5(null);
			}
			String headerMaskPolicy = req.getHeaderMaskPolicy();
			tsmpApiReg.setHeaderMaskPolicy(headerMaskPolicy);
			tsmpApiReg.setHeaderMaskKey(req.getHeaderMaskKey());
			tsmpApiReg.setHeaderMaskPolicyNum(req.getHeaderMaskPolicyNum());
			tsmpApiReg.setHeaderMaskPolicySymbol(req.getHeaderMaskPolicySymbol());

			tsmpApiReg.setBodyMaskKeyword(req.getBodyMaskKeyword());
			tsmpApiReg.setBodyMaskPolicy(req.getBodyMaskPolicy());
			tsmpApiReg.setBodyMaskPolicyNum(req.getBodyMaskPolicyNum());
			tsmpApiReg.setBodyMaskPolicySymbol(req.getBodyMaskPolicySymbol());
			
		} else {
			tsmpApiReg.setRedirectByIp("N");
			tsmpApiReg.setHeaderMaskPolicy("0");
			tsmpApiReg.setBodyMaskPolicy("0");
		}
		
		// 失敗判定策略 & 失敗處置策略
		String failDiscoveryPolicy = req.getFailDiscoveryPolicy();
		String failHandlePolicy = req.getFailHandlePolicy();
		tsmpApiReg.setFailDiscoveryPolicy(nvl(failDiscoveryPolicy, "0"));
		tsmpApiReg.setFailHandlePolicy(nvl(failHandlePolicy, "0"));
		
		tsmpApiReg = getTsmpApiRegDao().saveAndFlush(tsmpApiReg);
		
		if (TsmpApiSrc.COMPOSED.value().equals(tsmpApi.getApiSrc()) 
				|| TsmpApiSrc.REGISTERED.value().equals(tsmpApi.getApiSrc())) {
			//寫入 Audit Log D
			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
					TsmpApiReg.class.getSimpleName(), TableAct.U.value(), oldRowStr, tsmpApiReg);
		}
	}

	protected void clearAPICache() {
		/* 20221130 改用 Keeper 通知其他節點
		NoticeClearCacheEventsJob job = getNoticeClearCacheEventsJob("TSMP_API", "TSMP_API_REG");
		getJobHelper().add(job);
		*/
		getDaoGenericCacheService().clearAndNotify();
	}

	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
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

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
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

	protected NoticeClearCacheEventsJob getNoticeClearCacheEventsJob(String tableName1, String tableName2) {
		Integer action = 2;
		List<String> tableNameList = new ArrayList<>();
		tableNameList.add(tableName1);
		tableNameList.add(tableName2);
		return (NoticeClearCacheEventsJob) getCtx().getBean("noticeClearCacheEventsJob", action, null, tableNameList);
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected CommForwardProcService getCommForwardProcService() {
		return commForwardProcService;
	}
	
	protected DaoGenericCacheService getDaoGenericCacheService() {
		return daoGenericCacheService;
	}

	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}

}