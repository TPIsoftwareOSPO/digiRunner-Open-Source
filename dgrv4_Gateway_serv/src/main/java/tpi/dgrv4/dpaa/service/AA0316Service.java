package tpi.dgrv4.dpaa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.*;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.job.NoticeClearCacheEventsJob;
import tpi.dgrv4.dpaa.constant.RegexpConstant;
import tpi.dgrv4.dpaa.constant.TsmpApiSrc;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0316Func;
import tpi.dgrv4.dpaa.vo.AA0316Item;
import tpi.dgrv4.dpaa.vo.AA0316Req;
import tpi.dgrv4.dpaa.vo.AA0316Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.*;
import tpi.dgrv4.entity.entity.jpql.TsmpRegHost;
import tpi.dgrv4.entity.entity.jpql.TsmpRegModule;
import tpi.dgrv4.entity.repository.*;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.util.*;

@Service
public class AA0316Service {

	private TPILogger logger = TPILogger.tl;

	private BcryptParamHelper bcryptParamHelper;
	private FileHelper fileHelper;
	private JobHelper jobHelper;
	private TsmpUserDao tsmpUserDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private TsmpRegHostDao tsmpRegHostDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpRegModuleDao tsmpRegModuleDao;
	private TsmpApiRegDao tsmpApiRegDao;
	private ObjectMapper objectMapper;
	private ApplicationContext ctx;
	private DgrAuditLogService dgrAuditLogService;
	private DgrAcIdpUserDao dgrAcIdpUserDao;

	private enum Act {
		CREATE,	// 新增
		UPDATE;	// 更新
	}

	@Autowired
	public AA0316Service(BcryptParamHelper bcryptParamHelper, FileHelper fileHelper, JobHelper jobHelper,
			TsmpUserDao tsmpUserDao, TsmpDpFileDao tsmpDpFileDao, TsmpRegHostDao tsmpRegHostDao, TsmpApiDao tsmpApiDao,
			TsmpRegModuleDao tsmpRegModuleDao, TsmpApiRegDao tsmpApiRegDao, ObjectMapper objectMapper,
			ApplicationContext ctx, DgrAuditLogService dgrAuditLogService, DgrAcIdpUserDao dgrAcIdpUserDao) {
		super();
		this.bcryptParamHelper = bcryptParamHelper;
		this.fileHelper = fileHelper;
		this.jobHelper = jobHelper;
		this.tsmpUserDao = tsmpUserDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.tsmpRegHostDao = tsmpRegHostDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpRegModuleDao = tsmpRegModuleDao;
		this.tsmpApiRegDao = tsmpApiRegDao;
		this.objectMapper = objectMapper;
		this.ctx = ctx;
		this.dgrAuditLogService = dgrAuditLogService;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
	}

	@Transactional
	public AA0316Resp registerAPIList(TsmpAuthorization auth, AA0316Req req, ReqHeader reqHeader, InnerInvokeParam iip) {
		AA0316Resp resp = new AA0316Resp();
		
		String userName = auth.getUserName();
		String userNameForQuery = auth.getUserNameForQuery();
		String idPType = auth.getIdpType();
		String locale = ServiceUtil.getLocale(reqHeader.getLocale());

		check_AA0316Req(userNameForQuery, req, idPType);

		try {
			boolean isNewRegModule = false;
			TsmpRegModule regModule = null;
			Long regModuleId = null;
			if(!(TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType()))) {
				// 確認是不是上傳全新的模組
				String moduleName = req.getModuleName();
				String moduleVersion = req.getModuleVersion();
				isNewRegModule = (getTsmpRegModuleDao().countByModuleName(moduleName) <= 0);
				if (!isNewRegModule) {
					// 將本次上傳的模組押為最新版本
					resetLatestByModuleName(moduleName, userName);
	
					// 找出既有的 TSMP_REG_MODULE
					regModule = getTsmpRegModuleDao().findFirstByModuleNameAndModuleVersion(moduleName, moduleVersion);
				}
	
				// 更新/新增 TSMP_REG_MODULE
				String moduleSrc = req.getModuleSrc();
				regModule = saveRegModule(userName, moduleName, moduleVersion, moduleSrc, regModule);
			}
			// 檢查要註冊的 API
			Map<TsmpApiId, TsmpApi> tsmpApiMapping = new HashMap<>();
			Map<TsmpApiRegId, TsmpApiReg> tsmpApiRegMapping = new HashMap<>();
			check_AA0316Item(isNewRegModule, req, tsmpApiMapping, tsmpApiRegMapping, locale);

			// 更新/新增 TSMP_API & TSMP_API_REG
			saveAPI(auth, req, tsmpApiMapping, tsmpApiRegMapping, iip);
			
			if(!(TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType()))) {
				// 儲存外部介接規格文件
				regModuleId = regModule == null ? 0L : regModule.getRegModuleId();//為了sonarQube而有判斷
				String tempFileName = req.getTempFileName();
				saveDoc(userName, regModuleId, tempFileName);
			}
			
			// 清除快取
			clearAPICache();
			resp.setRegModuleId(regModuleId);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.API.value());
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			if (ServiceUtil.isValueTooLargeException(e)) {
				throw TsmpDpAaRtnCode._1220.throwing();
			}
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return resp;
	}

	protected void check_AA0316Req(String userNameForQuery, AA0316Req req, String idPType) {
		
		if (TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc())) {
			if(req.getType() == null) {
				throw TsmpDpAaRtnCode._1350.throwing("{{type}}");
			}
		}
		
		if(!(TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType()))) {
			
			if(!StringUtils.hasText(req.getModuleName())) {
				throw TsmpDpAaRtnCode._1350.throwing("{{moduleName}}");
			}
			
			if(req.getModuleName().length() > 50) {
				throw TsmpDpAaRtnCode._1351.throwing("{{moduleName}}", "50", String.valueOf(req.getModuleName().length()));
			}
			
			if(!ServiceUtil.checkDataByPattern(req.getModuleName(), RegexpConstant.ENGLISH_NUMBER)) {
				throw TsmpDpAaRtnCode._1352.throwing("{{moduleName}}");
			}
		}
		
		if (StringUtils.isEmpty(userNameForQuery)) {
			throw TsmpDpAaRtnCode._1258.throwing();
		}
 
		//使用者不存在
		checkUserExists(userNameForQuery, idPType);

		String tempFileName = req.getTempFileName();
		if (StringUtils.isEmpty(tempFileName)) {
			throw TsmpDpAaRtnCode._1418.throwing();
		}

		String refFileCateCode = TsmpDpFileType.TEMP.value();
		Long refId = -1L;
		String fileName = req.getTempFileName();
		List<TsmpDpFile> tsmpDpFileList = getTsmpDpFileDao().findByRefFileCateCodeAndRefIdAndFileName( //
			refFileCateCode, refId, fileName);
		if (CollectionUtils.isEmpty(tsmpDpFileList)) {
			throw TsmpDpAaRtnCode.NO_FILE.throwing();
		}

		String regHostId = req.getRegHostId();
		if (!StringUtils.isEmpty(regHostId)) {
			Optional<TsmpRegHost> opt_host = getTsmpRegHostDao().findById(regHostId);
			if (!opt_host.isPresent()) {
				throw TsmpDpAaRtnCode._1354.throwing("{{regHostId}}", regHostId);
			}
		} else {
			req.setRegHostId(null);
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
			//Table 查不到 user
			if (tsmpUser == null) {
				TPILogger.tl.debug("Table [TSMP_USER] can not find user, user_name: " + userNameForQuery);
				throw TsmpDpAaRtnCode._1231.throwing();
			}
		}
	}

	/**
	 * 檢查的同時，將 AA0316Item 所有 bcrypt 欄位解密
	 * @param isNewRegModule
	 * @param req
	 * @param tsmpApiMapping
	 * @param tsmpApiRegMapping
	 */
	protected void check_AA0316Item(boolean isNewRegModule, AA0316Req req, //
			Map<TsmpApiId, TsmpApi> tsmpApiMapping, //
			Map<TsmpApiRegId, TsmpApiReg> tsmpApiRegMapping, String locale) {

		String moduleName = req.getModuleName();
		List<AA0316Item> itemList = req.getRegApiList();

		Set<String> apiIdSet = new HashSet<>();
		String apiId = null;
		TsmpApiId tsmpApiId = null;
		TsmpApiRegId tsmpApiRegId = null;
		
		
		for (AA0316Item item : itemList) {

			checkItmeModuleName(req, item);
			
			checkMethods(item.getMethods());

			checkSrcUrl(item.getSrcUrl(), req);
			
			apiId = item.getApiId();
			if (apiIdSet.contains(apiId)) {
				logger.info("apId:" + apiId);
				logger.info("apiIdSet:" + apiIdSet);
				//走訪 apiId 值
				for (AA0316Item oneItem : itemList) {
					logger.info("RegApiList:" + oneItem.getApiId());
				}
				throw TsmpDpAaRtnCode._1284.throwing("API ID:" + apiId);
			}
			checkApiId(apiId, req);
			apiIdSet.add(apiId);
			
			checkUrlRID(item.getUrlRID());
			
			checkNoOAuth(item.getNoOAuth());
			
			checkFunFlag(item.getFunFlag());

			List<String> consumes = checkRepeatValues("consumes", item.getConsumes());
			item.setConsumes(consumes);

			List<String> produces = checkRepeatValues("produces", item.getProduces());
			item.setProduces(produces);

			List<String> headers = checkRepeatValues("headers", item.getHeaders());
			item.setHeaders(headers);

			List<String> params = checkRepeatValues("params", item.getParams());
			item.setParams(params);

			decryptItem(item, locale);	// 包含 dataFormat, jweFlag, jweFlagResp 的檢核

			// 找出已存在的 TSMP_API
			if(TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType())) {
				tsmpApiId = new TsmpApiId(apiId, item.getModuleName());
			}else {
				tsmpApiId = new TsmpApiId(apiId, moduleName);
			}
			tsmpApiMapping.put(tsmpApiId, null);
			Optional<TsmpApi> opt_api = getTsmpApiDao().findById(tsmpApiId);
			if (opt_api.isPresent()) {
				if (isNewRegModule) {
					throw TsmpDpAaRtnCode._1353.throwing("API", String.format("%s-%s", tsmpApiId.getModuleName(), apiId));
				}
				tsmpApiMapping.put(tsmpApiId, opt_api.get());
			}
			
			// 找出已存在的 TSMP_API_REG
			if(TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType())) {
				tsmpApiRegId = new TsmpApiRegId(apiId, item.getModuleName());
			}else {
				tsmpApiRegId = new TsmpApiRegId(apiId, moduleName);
			}
			
			tsmpApiRegMapping.put(tsmpApiRegId, null);
			Optional<TsmpApiReg> opt_reg = getTsmpApiRegDao().findById(tsmpApiRegId);
			if (opt_reg.isPresent()) {
				tsmpApiRegMapping.put(tsmpApiRegId, opt_reg.get());
			}

			if (opt_api.isPresent() != opt_reg.isPresent()) {
				throw TsmpDpAaRtnCode.SYSTEM_ERROR.throwing();
			}
		}
	}
	
	private void checkItmeModuleName(AA0316Req req, AA0316Item item) {
		if(TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType())) {

			if(!StringUtils.hasText(item.getModuleName())) {
				throw TsmpDpAaRtnCode._1350.throwing("{{moduleName}}");
			}
			
			if(item.getModuleName().length() > 50) {
				throw TsmpDpAaRtnCode._1351.throwing("{{moduleName}}", "50", String.valueOf(item.getModuleName().length()));
			}
		}
	}
	
	protected void checkMethods(List<String> methods) {
		if (CollectionUtils.isEmpty(methods)) {
			throw TsmpDpAaRtnCode._1350.throwing("{{methods}}");
		}
		
		Set<String> set = new HashSet<>();
		methods.forEach((method) -> {
			if (!set.contains(method) && (SafeHttpMethod.resolve(method.toUpperCase()) != null)) {
				set.add(method);
			} else {
				this.logger.debug("Invalid methods: " + methods);
				throw TsmpDpAaRtnCode._1290.throwing();
			}
		});
	}

	protected void checkSrcUrl(String srcUrl, AA0316Req req) {
		if (StringUtils.isEmpty(srcUrl)) {
			throw TsmpDpAaRtnCode._1350.throwing("{{srcUrl}}");
		}
		
		if(TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(0).equals(req.getType())) {
			if(srcUrl.indexOf("{") > -1) {
				throw TsmpDpAaRtnCode._1352.throwing("{{srcUrl}}");
			}
		}
	}

	protected void checkApiId(String apiId, AA0316Req req) {
		if (StringUtils.isEmpty(apiId)) {
			throw TsmpDpAaRtnCode._1350.throwing("{{apiId}}");
		}
		
		if (apiId.length() > 255) {
			throw TsmpDpAaRtnCode._1351.throwing("{{apiId}}", "255", String.valueOf(apiId.length()));
		}
		
		if(!(TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType()))) {
			if (!apiId.matches(RegexpConstant.ENGLISH_NUMBER)) {
				throw TsmpDpAaRtnCode._1352.throwing("{{apiId}}");
			}
		}
	}

	protected void checkUrlRID(Boolean urlRID) {
		if (urlRID == null) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	protected void checkNoOAuth(Boolean noOAuth) {
		if (noOAuth == null) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}
	
	protected void checkFunFlag(AA0316Func funFlag) {
		if ((funFlag == null) || (funFlag.getTokenPayload() == null)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	protected List<String> checkRepeatValues(String fieldName, List<String> values) {
		Set<String> set = new HashSet<>();
		if (!CollectionUtils.isEmpty(values)) {
			values.forEach((val) -> {
				if (!set.contains(val)) {
					set.add(val);
				} else {
					throw TsmpDpAaRtnCode._1284.throwing("{{" + fieldName + "}}");
				}
			});
		}
		return new ArrayList<>(set);
	}

	protected void decryptItem(AA0316Item item, String locale) {
		String dataFormat = item.getDataFormat();
		String jweFlag = item.getJweFlag();
		String jweFlagResp = item.getJweFlagResp();
		
		if (StringUtils.isEmpty(dataFormat) || StringUtils.isEmpty(jweFlag) || StringUtils.isEmpty(jweFlagResp)) {
			this.logger.debug(String.format("dataFormat: %s, jweFlag: %s, jweFlagResp: %s", dataFormat, jweFlag, jweFlagResp));
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		try {
			dataFormat = getBcryptParamHelper().decode(dataFormat, "API_DATA_FORMAT", locale);
			jweFlag =  getBcryptParamHelper().decode(jweFlag, "API_JWT_FLAG", locale);
			jweFlagResp =  getBcryptParamHelper().decode(jweFlagResp, "API_JWT_FLAG", locale);

			item.setDataFormat(dataFormat);
			item.setJweFlag(jweFlag);
			item.setJweFlagResp(jweFlagResp);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
	}

	// UPDATE TsmpRegModule M SET M.latest = 'N', M.updateDateTime = ?2, M.updateUser = ?3 WHERE M.moduleName = ?1
	protected void resetLatestByModuleName(String moduleName, String userName) {
		List<TsmpRegModule> mList = getTsmpRegModuleDao().findByModuleName(moduleName);
		if (!CollectionUtils.isEmpty(mList)) {
			mList.forEach((m) -> {
				m.setLatest("N");
				m.setUpdateDateTime(DateTimeUtil.now());
				m.setUpdateUser(userName);
				m = getTsmpRegModuleDao().saveAndFlush(m);
			});
		}
	}

	protected TsmpRegModule saveRegModule(String userName, String moduleName, String moduleVersion, String moduleSrc, //
			TsmpRegModule m) {
		if (m == null) {
			m = new TsmpRegModule();
			m.setModuleName(moduleName);
			m.setModuleVersion(moduleVersion);
			m.setCreateDateTime(DateTimeUtil.now());
			m.setCreateUser(userName);
		} else {
			m.setUpdateDateTime(DateTimeUtil.now());
			m.setUpdateUser(userName);
		}
		m.setModuleSrc(moduleSrc);
		m.setLatest("Y");
		m.setUploadDateTime(DateTimeUtil.now());
		m.setUploadUser(userName);
		m = getTsmpRegModuleDao().save(m);
		return m;
	}

	protected void saveAPI(TsmpAuthorization auth, AA0316Req req, //
			Map<TsmpApiId, TsmpApi> tsmpApiMapping, //
			Map<TsmpApiRegId, TsmpApiReg> tsmpApiRegMapping, InnerInvokeParam iip) {

		String userName = auth.getUserName();
		String orgId = auth.getOrgId();
		String moduleName = req.getModuleName();
		List<AA0316Item> itemList = req.getRegApiList();
		TsmpApiId tsmpApiId = null;
		TsmpApiRegId tsmpApiRegId = null;
		Act act_tsmpApi = null;
		Act act_tsmpApiReg = null;
		TsmpApi tsmpApi = null;
		TsmpApiReg tsmpApiReg = null;
		String regStatus = null;
		String apiUUID = null;
		int uuidIndex = 1;
		String uuid = iip != null ? iip.getTxnUid() : null;
		for (AA0316Item item : itemList) {
			if(iip != null) {
				iip.setTxnUid(uuid + "_" + uuidIndex);
				uuidIndex++;
			}
			if(TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType())) {
				tsmpApiId = new TsmpApiId(item.getApiId(), item.getModuleName());
				tsmpApiRegId = new TsmpApiRegId(item.getApiId(), item.getModuleName());
			}else {
				tsmpApiId = new TsmpApiId(item.getApiId(), moduleName);
				tsmpApiRegId = new TsmpApiRegId(item.getApiId(), moduleName);
			}
			tsmpApi = tsmpApiMapping.get(tsmpApiId);
			tsmpApiReg = tsmpApiRegMapping.get(tsmpApiRegId);
			act_tsmpApi = tsmpApi != null ? Act.UPDATE : Act.CREATE;
			act_tsmpApiReg = tsmpApiReg != null ? Act.UPDATE : Act.CREATE;
			
			//寫入 Audit Log M
			String lineNumber = StackTraceUtil.getLineNumber();
			if(TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc())) {
				if (Act.UPDATE.equals(act_tsmpApi)) {
					getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_REGISTER_API.value());
				} else if (Act.CREATE.equals(act_tsmpApi)) {
					getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.ADD_REGISTER_API.value());
				}
				
			}else if(TsmpApiSrc.COMPOSED.value().equals(req.getApiSrc())){
				if (Act.UPDATE.equals(act_tsmpApi)) {
					getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_COMPOSER_API.value());
				} else if (Act.CREATE.equals(act_tsmpApi)) {
					getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.ADD_COMPOSER_API.value());
				}
			}
			
			// 更新/新增 TSMP_API
			String oldRowStr = null;
			if (Act.UPDATE.equals(act_tsmpApi)) {
				oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpApi); //舊資料統一轉成 String
				tsmpApi.setUpdateTime(DateTimeUtil.now());
				tsmpApi.setUpdateUser(userName);
				
				if(TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType())) {
					tsmpApi.setApiName(item.getSummary());
				}else {
					// 若API_NAME已經有值則不異動，反之沒有值則填入AA0316Item.apiId
					if (StringUtils.isEmpty(tsmpApi.getApiName())) {
						tsmpApi.setApiName(item.getApiId());
					}
				}
				
			} else if (Act.CREATE.equals(act_tsmpApi)) {
				tsmpApi = new TsmpApi();
				tsmpApi.setApiKey(item.getApiId());
				if(TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType())) {
					tsmpApi.setModuleName(item.getModuleName());
				}else {
					tsmpApi.setModuleName(moduleName);
				}
				tsmpApi.setApiStatus("2");	// Disabled
				tsmpApi.setApiSrc(req.getApiSrc());
				tsmpApi.setCreateTime(DateTimeUtil.now());
				tsmpApi.setCreateUser(userName);
				tsmpApi.setOrgId(orgId);
				tsmpApi.setPublicFlag(TsmpDpPublicFlag.PRIVATE.value());
				tsmpApi.setApiUid(UUID.randomUUID().toString().toUpperCase());
				if(TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType())) {
					tsmpApi.setApiName(item.getSummary());
				}else {
					tsmpApi.setApiName(item.getApiId());
				}
				
			}
			
			tsmpApi.setApiDesc(item.getApiDesc());
			tsmpApi.setSrcUrl(item.getSrcUrl());
			tsmpApi.setDataFormat(item.getDataFormat());
			tsmpApi.setJewFlag(item.getJweFlag());
			tsmpApi.setJewFlagResp(item.getJweFlagResp());
			tsmpApi = getTsmpApiDao().saveAndFlush(tsmpApi);
			
			if(TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) || TsmpApiSrc.COMPOSED.value().equals(req.getApiSrc())) {
				if (Act.UPDATE.equals(act_tsmpApi)) {
					//寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
							TsmpApi.class.getSimpleName(), TableAct.U.value(), oldRowStr, tsmpApi);
				} else if (Act.CREATE.equals(act_tsmpApi)) {
					//寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
							TsmpApi.class.getSimpleName(), TableAct.C.value(), null, tsmpApi);
				}
			}
			
			// 更新/新增 TSMP_API_REG
			if (Act.UPDATE.equals(act_tsmpApiReg)) {
				oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpApiReg); //舊資料統一轉成 String
				if(tsmpApiReg != null) {//因為sonarQube而修改
					tsmpApiReg.setUpdateTime(DateTimeUtil.now());
					tsmpApiReg.setUpdateUser(userName);
				}
			} else if (Act.CREATE.equals(act_tsmpApiReg)) {
				tsmpApiReg = new TsmpApiReg();
				tsmpApiReg.setApiKey(item.getApiId());
				if(TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType())) {
					tsmpApiReg.setModuleName(item.getModuleName());
				}else {
					tsmpApiReg.setModuleName(moduleName);
				}
				if (TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc())) {
					regStatus = "1";
					apiUUID = null;
				} else if (TsmpApiSrc.COMPOSED.value().equals(req.getApiSrc())) {
					regStatus = "0";
					apiUUID = tsmpApi.getApiUid();
				}
				tsmpApiReg.setRegStatus(regStatus);
				tsmpApiReg.setApiUuid(apiUUID);
				if(TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) && Integer.valueOf(1).equals(req.getType())) {
					tsmpApiReg.setPathOfJson("[" + item.getApiId() + "]");
				}else {
					tsmpApiReg.setPathOfJson("[/" + moduleName + "/" + item.getApiId() + "]");
				}
				tsmpApiReg.setCreateTime(DateTimeUtil.now());
				tsmpApiReg.setCreateUser(userName);
			}
			
			try {
				if(tsmpApiReg != null) {//因為sonarQube而修改
					tsmpApiReg.setSrcUrl(item.getSrcUrl());
					tsmpApiReg.setReghostId(req.getRegHostId());
					tsmpApiReg.setUrlRid(item.getUrlRID() ? "1": "0");
					tsmpApiReg.setNoOauth(item.getNoOAuth() ? "1": "0");
					tsmpApiReg.setFunFlag(item.getFunFlag().convert());
					tsmpApiReg.setMethodOfJson(getObjectMapper().writeValueAsString(item.getMethods()));
					tsmpApiReg.setParamsOfJson(getObjectMapper().writeValueAsString(item.getParams()));
					tsmpApiReg.setHeadersOfJson(getObjectMapper().writeValueAsString(item.getHeaders()));
					tsmpApiReg.setConsumesOfJson(getObjectMapper().writeValueAsString(item.getConsumes()));
					tsmpApiReg.setProducesOfJson(getObjectMapper().writeValueAsString(item.getProduces()));
				}
			} catch (Exception e) {
				this.logger.debug(String.format("Convert json error: %s\\", StackTraceUtil.logStackTrace(e)));
			}
			tsmpApiReg = getTsmpApiRegDao().saveAndFlush(tsmpApiReg);
			
			if(TsmpApiSrc.REGISTERED.value().equals(req.getApiSrc()) || TsmpApiSrc.COMPOSED.value().equals(req.getApiSrc())) {
				if (Act.UPDATE.equals(act_tsmpApiReg)) {
					//寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
							TsmpApiReg.class.getSimpleName(), TableAct.U.value(), oldRowStr, tsmpApiReg);
				} else if (Act.CREATE.equals(act_tsmpApiReg)) {
					//寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
							TsmpApiReg.class.getSimpleName(), TableAct.C.value(), null, tsmpApiReg);
				}
			}
		}
	}

	protected void saveDoc(String userName, Long refId, String tempFileName) throws Exception {
		List<TsmpDpFile> tsmpDpFileList = getTsmpDpFileDao().findByRefFileCateCodeAndRefId( //
			TsmpDpFileType.REG_MODULE_DOC.value(), refId);
		boolean isCreate = CollectionUtils.isEmpty(tsmpDpFileList);
		boolean isUpdate = !isCreate;
		if (isUpdate) {
			// 若同一版本有多個規格文件，則全數刪除只剩下最新上傳的即可
			TsmpDpFile tsmpDpFile = null;
			if (tsmpDpFileList.size() > 1) {
				tsmpDpFile = findLatestUpload(tsmpDpFileList);
			} else {
				tsmpDpFile = tsmpDpFileList.get(0);
			}
			// 更新檔名跟內容
			String fileName = getFileHelper().restoreOrginalFilename(tempFileName);
			tsmpDpFile.setFileName(fileName);
			byte[] content = getFileHelper().downloadByTsmpDpFile(TsmpDpFileType.TEMP, -1L, tempFileName);
			tsmpDpFile.setBlobData(content);
			tsmpDpFile.setUpdateDateTime(DateTimeUtil.now());
			tsmpDpFile.setUpdateUser(userName);
			tsmpDpFile = getTsmpDpFileDao().save(tsmpDpFile);
			// 由排程刪除過期的暫存檔
		} else {
			getFileHelper().moveTemp(userName, TsmpDpFileType.REG_MODULE_DOC, refId, tempFileName, isCreate, isUpdate);
		}
	}

	protected TsmpDpFile findLatestUpload(List<TsmpDpFile> tsmpDpFileList) {
		TsmpDpFile latestUpload = tsmpDpFileList.get(0);
		for (TsmpDpFile tsmpDpFile : tsmpDpFileList) {
			if (tsmpDpFile.getUpdateDateTime().compareTo(latestUpload.getUpdateDateTime()) >= 0) {
				latestUpload = tsmpDpFile;
			} else {
				getTsmpDpFileDao().delete(tsmpDpFile);
			}
		}
		return latestUpload;
	}

	protected void clearAPICache() {
		NoticeClearCacheEventsJob job = getNoticeClearCacheEventsJob("tsmp_api", "tsmp_api_reg");
		getJobHelper().add(job);
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected TsmpRegHostDao getTsmpRegHostDao() {
		return this.tsmpRegHostDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpRegModuleDao getTsmpRegModuleDao() {
		return this.tsmpRegModuleDao;
	}

	protected TsmpApiRegDao getTsmpApiRegDao() {
		return this.tsmpApiRegDao;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
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
	
	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}
}