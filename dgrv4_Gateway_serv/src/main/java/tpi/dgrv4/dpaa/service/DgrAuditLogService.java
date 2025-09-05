package tpi.dgrv4.dpaa.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.UUID64Util;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.*;
import tpi.dgrv4.entity.entity.jpql.DgrComposerFlow;
import tpi.dgrv4.entity.entity.jpql.TsmpClientHost;
import tpi.dgrv4.entity.repository.*;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DgrAuditLogService {
	
	private TPILogger logger = TPILogger.tl;
	
	private DgrAuditLogMDao dgrAuditLogMDao;
	private DgrAuditLogDDao dgrAuditLogDDao;
	private AuthoritiesDao authoritiesDao;
	private TsmpRoleDao tsmpRoleDao;
	private FileHelper fileHelper;
	private ObjectMapper objectMapper;
	private TsmpUserDao tsmpUserDao;
	private TsmpSettingService tsmpSettingService;
	private TsmpGroupDao tsmpGroupDao;
	private TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao;
	private TsmpClientDao tsmpClientDao;
	private DgrWebhookNotifyDao dgrWebhookNotifyDao;
	
	@Autowired
	public DgrAuditLogService(DgrAuditLogMDao dgrAuditLogMDao, DgrAuditLogDDao dgrAuditLogDDao,
			AuthoritiesDao authoritiesDao, TsmpRoleDao tsmpRoleDao, FileHelper fileHelper, ObjectMapper objectMapper,
			TsmpUserDao tsmpUserDao, TsmpSettingService tsmpSettingService, TsmpGroupDao tsmpGroupDao,
			TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao, TsmpClientDao tsmpClientDao,DgrWebhookNotifyDao dgrWebhookNotifyDao) {
		super();
		this.dgrAuditLogMDao = dgrAuditLogMDao;
		this.dgrAuditLogDDao = dgrAuditLogDDao;
		this.authoritiesDao = authoritiesDao;
		this.tsmpRoleDao = tsmpRoleDao;
		this.fileHelper = fileHelper;
		this.objectMapper = objectMapper;
		this.tsmpUserDao = tsmpUserDao;
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpGroupAuthoritiesDao = tsmpGroupAuthoritiesDao;
		this.tsmpClientDao = tsmpClientDao;
		this.dgrWebhookNotifyDao = dgrWebhookNotifyDao;
	}

	/**
	 * 寫入 Audit Log M
	 * 
	 * @param iip
	 * @param lineNumber
	 * @param eventNo
	 */
	public DgrAuditLogM createAuditLogM(InnerInvokeParam iip, String lineNumber, String eventNo) {
		try {
			if(iip == null) {
				return null;
			}
			
			//記錄Audit Log功能是否啟用 (true/false)
			boolean isEnable = getTsmpSettingService().getVal_AUDIT_LOG_ENABLE();
			if(!isEnable) {
				return null;
			}
			
			DgrAuditLogM dgrAuditLogM = null;
			try {
				dgrAuditLogM = createAuditLogMData(iip, lineNumber, eventNo);
			} catch (Exception e) {
				logger.debug("=========Write Audit Log M errors:=========\n" + StackTraceUtil.logStackTrace(e));
			}
			return dgrAuditLogM;
		}catch(Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			return null;
		}
	}
	
	/**
	 * 寫入 Audit Log D
	 */
	public DgrAuditLogD createAuditLogD(InnerInvokeParam iip, String lineNumber, 
			String entityName, String cud, String oldRowStr, Object newRowObj) {
		try {
			if(iip == null) {
				return null;
			}
			
			String createUser = iip.getTsmpAuthorization().getUserName();
			if(!StringUtils.hasLength(createUser)) {
				//使用 client_credentials 取得的 token 沒有 userName,所以欄位 createUser 記錄 clientId 值
				createUser = iip.getTsmpAuthorization().getClientId();
			}
			
			if(!StringUtils.hasLength(createUser)) {
				createUser = "SYSTEM";
			}
			
			DgrAuditLogD dgrAuditLogD = createAuditLogD(lineNumber, createUser, 
					iip.getTxnUid(), iip.getApiUrl(), entityName, cud, oldRowStr, newRowObj);
			return dgrAuditLogD;
		}catch(Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			return null;
		}
	}

	/**
	 * 寫入 Audit Log D,
	 * 客製包使用的 method, 不用傳入 InnerInvokeParam
	 * 
	 * @param lineNumber : 由程式 StackTraceUtil.getLineNumber()取得
	 * @param userName : TsmpAuthorization.getUserName()
	 * @param txnUid : UUID-64bit
	 * @param apiUrl : Controller上定義的網址
	 * @param entityName : Entity 的名稱, 由程式 Entity.class.getSimpleName()取得
	 * @param cud 對資料的操作 : C/U/D
	 * @param oldRowStr 舊資料
	 * @param newRowObj 新資料
	 * @return
	 */
	public DgrAuditLogD createAuditLogD(String lineNumber, String userName, String txnUid, String apiUrl, 
			String entityName, String cud, String oldRowStr, Object newRowObj) {
		//記錄Audit Log功能是否啟用 (true/false)
		boolean isEnable = getTsmpSettingService().getVal_AUDIT_LOG_ENABLE();
		if(!isEnable) {
			return null;
		}
		
		DgrAuditLogD dgrAuditLogD = null;
		try {
			String newRowStr = null;
			if(newRowObj != null) {
				newRowStr = getObjectMapper().writeValueAsString(newRowObj);
			}
			
			dgrAuditLogD = createAuditLogDData(lineNumber, userName, txnUid, apiUrl, entityName, cud, oldRowStr, newRowStr);
			
		} catch (Exception e) {
			logger.debug("=========Write Audit Log D errors:=========\n" + StackTraceUtil.logStackTrace(e));
		}
		
		return dgrAuditLogD;
	}
	
	/**
	 * 寫入 Audit Log M for 登出
	 * 
	 * @param iip
	 * @param lineNumber
	 * @param eventNo
	 */
	public DgrAuditLogM createAuditLogMForLogout(InnerInvokeParam iip, String lineNumber, String eventNo, 
			String state, String httpCode, String idPType, String idPSub) {
		if(iip == null) {
			return null;
		}
		
		//記錄Audit Log功能是否啟用 (true/false)
		boolean isEnable = getTsmpSettingService().getVal_AUDIT_LOG_ENABLE();
		if(!isEnable) {
			return null;
		}
		
		DgrAuditLogM dgrAuditLogM = createAuditLogMData(iip, lineNumber, eventNo, state, httpCode);
		
		return dgrAuditLogM;
	}
	
	/**
	 * 寫入 Audit Log M for 登入
	 * 
	 * @param iip
	 * @param lineNumber
	 * @param eventNo
	 */
	public DgrAuditLogM createAuditLogMForLogin(InnerInvokeParam iip, String lineNumber, String eventNo, String apiUrl, String userName,
			String clientId, String userIp, String userHostname, String state, String httpCode, String txnUid,
			String tokenJti) {
		
		//記錄Audit Log功能是否啟用 (true/false)
		boolean isEnable = getTsmpSettingService().getVal_AUDIT_LOG_ENABLE();
		if(!isEnable) {
			return null;
		}
		DgrAuditLogM dgrAuditLogM = createAuditLogMData(iip, lineNumber, eventNo, state, httpCode);
		
		return dgrAuditLogM;
	}
	
	/**
	 * 寫入 Audit Log M for 登入
	 * 
	 * @param iip
	 * @param lineNumber
	 * @param eventNo
	 */
	public DgrAuditLogM createAuditLogMForLogin(String lineNumber, String eventNo, String apiUrl, String userName,
			String clientId, String userIp, String userHostname, String state, String httpCode, String txnUid,
			String tokenJti, String idPType) {

		//記錄Audit Log功能是否啟用 (true/false)
		boolean isEnable = getTsmpSettingService().getVal_AUDIT_LOG_ENABLE();
		if(!isEnable) {
			return null;
		}
		InnerInvokeParam iip = getInnerInvokeParam(apiUrl, userName, clientId, userIp, userHostname, txnUid, tokenJti, idPType);
		DgrAuditLogM dgrAuditLogM = createAuditLogMData(iip, lineNumber, eventNo, state, httpCode);
 
		return dgrAuditLogM;
	}
	
	private DgrAuditLogM createAuditLogMData(InnerInvokeParam iip, String lineNumber, String eventNo) {
		
		DgrAuditLogM dgrAuditLogM = createAuditLogMData(iip, lineNumber, eventNo, "", "");
		return dgrAuditLogM;
	}
	
	private DgrAuditLogM createAuditLogMData(InnerInvokeParam iip, String lineNumber, String eventNo, 
			String state, String httpCode) {
		TsmpAuthorization auth = iip.getTsmpAuthorization();
		String txnUid = iip.getTxnUid();
		String userName = auth.getUserName();
		String clientId = auth.getClientId();
		String apiUrl = iip.getApiUrl();
		String origApiUrl = iip.getOrigApiUrl();
		String userIp = iip.getUserIp();
		String userHostname = iip.getUserHostname();
		
		String idPType = auth.getIdpType();
		String idPSub = auth.getUserNameForQuery();
		
		DgrAuditLogM dgrAuditLogM = new DgrAuditLogM();
		dgrAuditLogM.setAuditExtId(System.currentTimeMillis());
		dgrAuditLogM.setTxnUid(txnUid);
		
		String userRole = "";
		if(StringUtils.hasLength(idPType)) {//以 AC IdP 方式登入 AC
			dgrAuditLogM.setUserName(userName);//為 idPType + "." + userAlias 格式, 例如:"GOOGLE.李OO Mini Lee"
			if(StringUtils.hasLength(idPSub)) {
				userRole = getUserRoleData(idPSub);
			}
			
		}else if(StringUtils.hasLength(userName)) {//以 AC 登入
			dgrAuditLogM.setUserName(userName);
			userRole = getUserRoleData(userName);
			
		}else {
			dgrAuditLogM.setUserName("N/A");
		}
		
		if(StringUtils.hasLength(clientId)) {
			dgrAuditLogM.setClientId(clientId);
		}else {
			dgrAuditLogM.setClientId("N/A");
		}
		
		dgrAuditLogM.setApiUrl(apiUrl);
		dgrAuditLogM.setEventNo(eventNo);
		dgrAuditLogM.setUserIp(userIp);
		dgrAuditLogM.setUserHostname(userHostname);
		dgrAuditLogM.setStackTrace(lineNumber);
		dgrAuditLogM.setUserRole(userRole);
		dgrAuditLogM.setOrigApiUrl(origApiUrl);
		
		Date nowTime = new Date(System.currentTimeMillis());
		dgrAuditLogM.setCreateDateTime(nowTime);
		if(StringUtils.hasLength(userName)) {
			dgrAuditLogM.setCreateUser(userName);
		}else if(StringUtils.hasLength(clientId)){
			dgrAuditLogM.setCreateUser(clientId);
		}
		
		if(AuditLogEvent.LOGIN.value().equals(eventNo) //登入
				|| AuditLogEvent.LOGOUT.value().equals(eventNo)) { //登出
			
			String param2 = null;
			String param5 = null;
			if (StringUtils.hasLength(idPType)) {// 以 IdP 登入 AC
				String[] arrUserName = userName.split("\\.");
				String userNameOrUserAlias = "";
				if (arrUserName.length >= 2) {
					userNameOrUserAlias = arrUserName[1];
					param2 = userNameOrUserAlias;
				}
 
				// IdPType + "." + user_name, 例如: Google 時,為 "GOOGLE.101872102234493560911"
				param5 = idPType + "." + idPSub;

			} else {// 以 AC 登入
				if (StringUtils.hasLength(userName)) {
					param2 = getUserAlias(userName);// 存入值為 user_alias
					if (!StringUtils.hasText(param2)) { // 若沒有值, 則存入 user_name
						param2 = userName;
					}

					param5 = userName;// user_name
				}
			}
			
			String tokenJti = iip.getTsmpAuthorization().getJti();
			
			dgrAuditLogM.setParam1(state);// "SUCCESS" 或 "FAILED"
			dgrAuditLogM.setParam2(param2);// user_alias 或 idPSub
			dgrAuditLogM.setParam3(httpCode);// httpCode
			dgrAuditLogM.setParam4(tokenJti);// Token jti
			dgrAuditLogM.setParam5(param5);
		}
		
		dgrAuditLogM = getDgrAuditLogMDao().saveAndFlush(dgrAuditLogM);
		
		return dgrAuditLogM;
	}
	
	private String getUserRoleData(String userName) {
		String userRole = "";
		List<Authorities> authoritiesList = getAuthoritiesDao().findByUsername(userName);
		if (CollectionUtils.isEmpty(authoritiesList)==false) {
			for (Authorities authorities : authoritiesList) {
				Optional<TsmpRole> opt_role = getTsmpRoleDao().findById(authorities.getAuthority());
				if (opt_role.isPresent()) {
					TsmpRole tsmpRole = opt_role.get();
					String roleId = tsmpRole.getRoleId();
					String roleName = tsmpRole.getRoleName();
					
					String temp = roleId + ":" + roleName;
					if(StringUtils.hasLength(userRole)) {
						userRole += ",";
					}
					userRole += temp;
				}
			}
		}
		return userRole;
	}
	
	private DgrAuditLogD createAuditLogDData(String lineNumber, String userName, String txnUid, String apiUrl, String entityName, String cud, 
			String oldRowStr, String newRowStr) throws Exception {
		DgrAuditLogD dgrAuditLogD = new DgrAuditLogD();
		
		dgrAuditLogD.setTxnUid(txnUid);
		dgrAuditLogD.setEntityName(entityName);
		dgrAuditLogD.setCud(cud);
		dgrAuditLogD.setStackTrace(lineNumber);
		
		Date nowTime = new Date(System.currentTimeMillis());
		dgrAuditLogD.setCreateDateTime(nowTime);
		dgrAuditLogD.setCreateUser(userName);
		
		if(StringUtils.hasLength(oldRowStr)) {
			dgrAuditLogD.setOldRow(oldRowStr.getBytes());
		}
		if(StringUtils.hasLength(newRowStr)) {
			dgrAuditLogD.setNewRow(newRowStr.getBytes());
		}
		
		//不判斷路徑,則自訂欄位都填入預設的值
		dgrAuditLogD = setDefault(dgrAuditLogD, entityName, oldRowStr, newRowStr, cud);
		
		dgrAuditLogD = getDgrAuditLogDDao().saveAndFlush(dgrAuditLogD);
		
		return dgrAuditLogD;
	}
	 
	/**
	 * 舊資料統一轉成 String
	 * 
	 * @param iip
	 * @param oldRowObj
	 * @return
	 */
	public String writeValueAsString(InnerInvokeParam iip, Object oldRowObj) {
		if(iip == null) {
			return "";
		}
		String oldRowStr = "";
		try {
			oldRowStr = writeValueAsString(oldRowObj); //舊資料統一轉成 String
		} catch (Exception e) {
			logger.debug("=========Write Audit Log D errors:=========\n" + StackTraceUtil.logStackTrace(e));
		}
		return oldRowStr;
	}
	
	/**
	 * 舊資料統一轉成 String,
	 * 客製包使用的 method, 不用傳入 InnerInvokeParam
	 * 
	 * @param oldRowObj
	 * @return
	 */
	public String writeValueAsString(Object oldRowObj) {
		String oldRowStr = "";
		try {
			oldRowStr = getObjectMapper().writeValueAsString(oldRowObj); //舊資料統一轉成 String
		} catch (Exception e) {
			logger.debug("=========Write Audit Log D errors:=========\n" + StackTraceUtil.logStackTrace(e));
		}
		return oldRowStr;
	}
	
	/**
	 * 自訂欄位 param1 ~ param5 填入預設的值
	 * 
	 * @param dgrAuditLogD
	 * @param entityName
	 * @param oldRowStr
	 * @param newRowStr
	 * @param cud
	 * @return
	 * @throws Exception
	 */
	private DgrAuditLogD setDefault(DgrAuditLogD dgrAuditLogD, String entityName, String oldRowStr, String newRowStr,
			String cud) throws Exception {
		String rowStr = "";
		if (TableAct.D.value().equals(cud)) {// D
			rowStr = oldRowStr;

		} else if (TableAct.C.value().equals(cud) || TableAct.U.value().equals(cud)) {// C or U
			rowStr = newRowStr;
		}

		if (Authorities.class.getSimpleName().equals(entityName)) {
			dgrAuditLogD = doAuthorities(dgrAuditLogD, rowStr);

		} else if (Users.class.getSimpleName().equals(entityName)) {
			dgrAuditLogD = doUsers(dgrAuditLogD, rowStr);

		} else if (TsmpUser.class.getSimpleName().equals(entityName)) {
			dgrAuditLogD = doTsmpUser(dgrAuditLogD, rowStr);

		} else if (TsmpRole.class.getSimpleName().equals(entityName)) {
			dgrAuditLogD = doTsmpRole(dgrAuditLogD, rowStr);

		} else if (TsmpRoleFunc.class.getSimpleName().equals(entityName)) {
			dgrAuditLogD = doTsmpRoleFunc(dgrAuditLogD, rowStr);

		} else if (TsmpRoleRoleMapping.class.getSimpleName().equals(entityName)) {
			// 無動作

		} else if (TsmpGroupApi.class.getSimpleName().equals(entityName)) {
			dgrAuditLogD = doTsmpGroupApi(dgrAuditLogD, rowStr);

		} else if (TsmpGroupAuthoritiesMap.class.getSimpleName().equals(entityName)) {
			dgrAuditLogD = doTsmpGroupAuthoritiesMap(dgrAuditLogD, rowStr);

		} else if (TsmpGroup.class.getSimpleName().equals(entityName)) {
			dgrAuditLogD = doTsmpGroup(dgrAuditLogD, rowStr);

		} else if (TsmpApi.class.getSimpleName().equals(entityName)) {
			dgrAuditLogD = doTsmpApi(dgrAuditLogD, rowStr);

		} else if (TsmpApiReg.class.getSimpleName().equals(entityName)) {
			dgrAuditLogD = doTsmpApiReg(dgrAuditLogD, rowStr);

		} else if (TsmpClient.class.getSimpleName().equals(entityName)) {
			dgrAuditLogD = doTsmpClient(dgrAuditLogD, rowStr);

		} else if (TsmpClientHost.class.getSimpleName().equals(entityName)) {
			dgrAuditLogD = doTsmpClientHost(dgrAuditLogD, rowStr);

		} else if (TsmpDpClientext.class.getSimpleName().equals(entityName)) {
			dgrAuditLogD = doTsmpDpClientext(dgrAuditLogD, rowStr);

		} else if (OauthClientDetails.class.getSimpleName().equals(entityName)) {
			dgrAuditLogD = doOauthClientDetails(dgrAuditLogD, rowStr);

		} else if (DgrComposerFlow.class.getSimpleName().equals(entityName)) {
			dgrAuditLogD = doDgrComposerFlow(dgrAuditLogD, rowStr);

		} else if (DgrAcIdpUser.class.getSimpleName().equals(entityName)) {
			dgrAuditLogD = doDgrAcIdpUser(dgrAuditLogD, rowStr);

		} else if (TsmpSetting.class.getSimpleName().equals(entityName)) {
			dgrAuditLogD = doTsmpSetting(dgrAuditLogD, rowStr);
		}else if(DgrWebhookApiMap.class.getSimpleName().equals(entityName)){
			dgrAuditLogD = doDgrWebhookApiMap(dgrAuditLogD, rowStr);
		}

		return dgrAuditLogD;
	}

	private DgrAuditLogD doDgrWebhookApiMap(DgrAuditLogD dgrAuditLogD, String rowStr) throws JsonProcessingException {
		DgrWebhookApiMap dgrWebhookApiMap = getObjectMapper().readValue(rowStr, DgrWebhookApiMap.class);
		dgrAuditLogD.setParam1(String.valueOf(dgrWebhookApiMap.getModuleName()));
		dgrAuditLogD.setParam2(String.valueOf(dgrWebhookApiMap.getApiKey()));
		DgrWebhookNotify notify = getDgrWebhookNotifyDao().findById(dgrWebhookApiMap.getWebhookNotifyId()).orElse(null);
		dgrAuditLogD.setParam3(notify != null ? notify.getNotifyName() : null);
		//新增/更新
		return dgrAuditLogD;
	}

	private DgrAuditLogD doTsmpSetting(DgrAuditLogD dgrAuditLogD, String rowStr) throws Exception {
		TsmpSetting tsmpSetting = getObjectMapper().readValue(rowStr, TsmpSetting.class);
		dgrAuditLogD.setParam1(String.valueOf(tsmpSetting.getId()));//新增/更新/刪除的 Id
		
		return dgrAuditLogD;
	}
	
	private DgrAuditLogD doDgrAcIdpUser(DgrAuditLogD dgrAuditLogD, String rowStr) throws Exception {
		DgrAcIdpUser dgrAcIdpUser = getObjectMapper().readValue(rowStr, DgrAcIdpUser.class);
		String userRole = getUserRoleData(dgrAcIdpUser.getUserName());
		dgrAuditLogD.setParam1(String.valueOf(dgrAcIdpUser.getAcIdpUserId()));//新增/更新/刪除的 UserId
		dgrAuditLogD.setParam2(dgrAcIdpUser.getUserName());//新增/更新/刪除的 user_name
		dgrAuditLogD.setParam3(dgrAcIdpUser.getUserAlias());
		dgrAuditLogD.setParam4(userRole);

		return dgrAuditLogD;
	}

	private DgrAuditLogD doUsers(DgrAuditLogD dgrAuditLogD, String rowStr) throws Exception{
		Users users = getObjectMapper().readValue(rowStr, Users.class);
		dgrAuditLogD.setParam1(users.getUserName());//新增/更新/刪除的 user_name
		
		return dgrAuditLogD;
	}
	
	private DgrAuditLogD doAuthorities(DgrAuditLogD dgrAuditLogD, String rowStr) throws Exception {
		Authorities authorities = getObjectMapper().readValue(rowStr, Authorities.class);
		
		Optional<TsmpRole> opt_role = getTsmpRoleDao().findById(authorities.getAuthority());
		String param3 = "";
		if(opt_role.isPresent()) {
			TsmpRole tsmpRole = opt_role.get();
			param3 = tsmpRole.getRoleName();
		}
		
		dgrAuditLogD.setParam1(authorities.getUsername());//新增/刪除的 user_name
		dgrAuditLogD.setParam2(authorities.getAuthority());//新增/刪除的 role_id
		dgrAuditLogD.setParam3(param3);//新增/刪除的 role_name
		
		return dgrAuditLogD;
	}
	
	private DgrAuditLogD doTsmpUser(DgrAuditLogD dgrAuditLogD, String rowStr) throws Exception {
		
		TsmpUser tsmpUser = getObjectMapper().readValue(rowStr, TsmpUser.class);
		String userRole = getUserRoleData(tsmpUser.getUserName());
		
		dgrAuditLogD.setParam1(tsmpUser.getUserId());//新增/更新/刪除的 user_id	
		dgrAuditLogD.setParam2(tsmpUser.getUserName());//新增/更新/刪除的 user_name	
		dgrAuditLogD.setParam3(tsmpUser.getUserAlias());//新增/更新/刪除的 user_alias	
		dgrAuditLogD.setParam4(userRole);//role_id:role_name,role_id:role_name 等...
		return dgrAuditLogD;
	}
	
	private DgrAuditLogD doTsmpRole(DgrAuditLogD dgrAuditLogD, String rowStr) throws Exception {
		TsmpRole tsmpRole = getObjectMapper().readValue(rowStr, TsmpRole.class);
		
		dgrAuditLogD.setParam1(tsmpRole.getRoleId());//新增/刪除的 role_id
		dgrAuditLogD.setParam2(tsmpRole.getRoleName());//新增/刪除的 roleName
		
		return dgrAuditLogD;
	}
	
	private DgrAuditLogD doTsmpRoleFunc(DgrAuditLogD dgrAuditLogD, String rowStr) throws Exception {
		TsmpRoleFunc tsmpRoleFunc = getObjectMapper().readValue(rowStr, TsmpRoleFunc.class);

		dgrAuditLogD.setParam1(tsmpRoleFunc.getRoleId());//新增/刪除的 role_id
		dgrAuditLogD.setParam3(tsmpRoleFunc.getFuncCode());//新增/刪除的 func_code
		
		return dgrAuditLogD;
	}
	
	private DgrAuditLogD doTsmpGroup(DgrAuditLogD dgrAuditLogD, String rowStr) throws Exception {
		TsmpGroup tsmpGroupVo = getObjectMapper().readValue(rowStr, TsmpGroup.class);
		
		dgrAuditLogD.setParam1(tsmpGroupVo.getGroupId());
		dgrAuditLogD.setParam2(tsmpGroupVo.getGroupName());
		dgrAuditLogD.setParam3(tsmpGroupVo.getGroupAlias());
		
		return dgrAuditLogD;
	}
	
	private DgrAuditLogD doTsmpApi(DgrAuditLogD dgrAuditLogD, String rowStr) throws Exception {
		TsmpApi tsmpApiVo = getObjectMapper().readValue(rowStr, TsmpApi.class);
		
		dgrAuditLogD.setParam1(tsmpApiVo.getModuleName());
		dgrAuditLogD.setParam2(tsmpApiVo.getApiKey());
		
		return dgrAuditLogD;
	}
	
	private DgrAuditLogD doTsmpApiReg(DgrAuditLogD dgrAuditLogD, String rowStr) throws Exception {
		TsmpApiReg tsmpApiRegVo = getObjectMapper().readValue(rowStr, TsmpApiReg.class);
		
		dgrAuditLogD.setParam1(tsmpApiRegVo.getModuleName());
		dgrAuditLogD.setParam2(tsmpApiRegVo.getApiKey());
		
		return dgrAuditLogD;
	}
	
	private DgrAuditLogD doTsmpClient(DgrAuditLogD dgrAuditLogD, String rowStr) throws Exception {
		TsmpClient vo = getObjectMapper().readValue(rowStr, TsmpClient.class);
		
		dgrAuditLogD.setParam1(vo.getClientId());
		dgrAuditLogD.setParam2(vo.getClientName());
		dgrAuditLogD.setParam3(vo.getClientAlias());
		
		return dgrAuditLogD;
	}
	
	private DgrAuditLogD doTsmpClientHost(DgrAuditLogD dgrAuditLogD, String rowStr) throws Exception {
		TsmpClientHost vo = getObjectMapper().readValue(rowStr, TsmpClientHost.class);
		TsmpClient clientVo = getTsmpClientDao().findById(vo.getClientId()).orElse(null);
		String p2 = "";
		String p3 = "";
		if(clientVo != null) {
			p2 = clientVo.getClientName();
			p3 = clientVo.getClientAlias();
		}
		
		dgrAuditLogD.setParam1(vo.getClientId());
		dgrAuditLogD.setParam2(p2);
		dgrAuditLogD.setParam3(p3);
		dgrAuditLogD.setParam4(vo.getHostIp());
		dgrAuditLogD.setParam5(vo.getHostName());
		
		return dgrAuditLogD;
	}
	
	private DgrAuditLogD doTsmpDpClientext(DgrAuditLogD dgrAuditLogD, String rowStr) throws Exception {
		TsmpDpClientext vo = getObjectMapper().readValue(rowStr, TsmpDpClientext.class);
		TsmpClient clientVo = getTsmpClientDao().findById(vo.getClientId()).orElse(null);
		String p2 = "";
		String p3 = "";
		if(clientVo != null) {
			p2 = clientVo.getClientName();
			p3 = clientVo.getClientAlias();
		}
		
		dgrAuditLogD.setParam1(vo.getClientId());
		dgrAuditLogD.setParam2(p2);
		dgrAuditLogD.setParam3(p3);
		dgrAuditLogD.setParam4(vo.getContentTxt());
		
		return dgrAuditLogD;
	}
	
	private DgrAuditLogD doOauthClientDetails(DgrAuditLogD dgrAuditLogD, String rowStr) throws Exception {
		OauthClientDetails vo = getObjectMapper().readValue(rowStr, OauthClientDetails.class);
		TsmpClient clientVo = getTsmpClientDao().findById(vo.getClientId()).orElse(null);
		String p2 = "";
		String p3 = "";
		if(clientVo != null) {
			p2 = clientVo.getClientName();
			p3 = clientVo.getClientAlias();
		}
		
		dgrAuditLogD.setParam1(vo.getClientId());
		dgrAuditLogD.setParam2(p2);
		dgrAuditLogD.setParam3(p3);
		
		return dgrAuditLogD;
	}
	
	private DgrAuditLogD doDgrComposerFlow(DgrAuditLogD dgrAuditLogD, String rowStr) throws Exception {
		DgrComposerFlow vo = getObjectMapper().readValue(rowStr, DgrComposerFlow.class);
		dgrAuditLogD.setParam1(vo.getModuleName());
		dgrAuditLogD.setParam2(vo.getApiId());
		return dgrAuditLogD;
	}
	
	private DgrAuditLogD doTsmpGroupApi(DgrAuditLogD dgrAuditLogD, String rowStr) throws Exception {
		TsmpGroupApi tsmpGroupApiVo = getObjectMapper().readValue(rowStr, TsmpGroupApi.class);
		
		Optional<TsmpGroup> opt = getTsmpGroupDao().findById(tsmpGroupApiVo.getGroupId());
		String param2 = "";
		if(opt.isPresent()) {
			TsmpGroup tsmpGroupVo = opt.get();
			param2 = tsmpGroupVo.getGroupName();
		}
		
		dgrAuditLogD.setParam1(tsmpGroupApiVo.getGroupId());
		dgrAuditLogD.setParam2(param2);
		dgrAuditLogD.setParam3(tsmpGroupApiVo.getModuleName());
		dgrAuditLogD.setParam4(tsmpGroupApiVo.getApiKey());
		
		return dgrAuditLogD;
	}
	
	private DgrAuditLogD doTsmpGroupAuthoritiesMap(DgrAuditLogD dgrAuditLogD, String rowStr) throws Exception {
		TsmpGroupAuthoritiesMap tsmpGroupAuthoritiesMapVo = getObjectMapper().readValue(rowStr, TsmpGroupAuthoritiesMap.class);
		
		Optional<TsmpGroup> opt = getTsmpGroupDao().findById(tsmpGroupAuthoritiesMapVo.getGroupId());
		String param2 = "";
		if(opt.isPresent()) {
			TsmpGroup TsmpGroupVo = opt.get();
			param2 = TsmpGroupVo.getGroupName();
		}
		
		Optional<TsmpGroupAuthorities> opt4 = getTsmpGroupAuthoritiesDao().findById(tsmpGroupAuthoritiesMapVo.getGroupAuthoritieId());
		String param4 = "";
		if(opt.isPresent() && opt4.isPresent()) {
			TsmpGroupAuthorities tsmpGroupAuthoritiesVo = opt4.get();
			param4 = tsmpGroupAuthoritiesVo.getGroupAuthoritieName();
		}
		
		dgrAuditLogD.setParam1(tsmpGroupAuthoritiesMapVo.getGroupId());
		dgrAuditLogD.setParam2(param2);
		dgrAuditLogD.setParam3(tsmpGroupAuthoritiesMapVo.getGroupAuthoritieId());
		dgrAuditLogD.setParam4(param4);
		
		return dgrAuditLogD;
	}
 
	private String getUserAlias(String userName) {
		String userAlias = "";
		TsmpUser user = getTsmpUserDao().findFirstByUserName(userName);
		if(user != null) {
			userAlias = user.getUserAlias();
		}
		return userAlias;
	}

	public InnerInvokeParam getInnerInvokeParam(String apiUrl, String userName, String clientId, 
			String userIp, String userHostname) {
		
		String txnUid = getTxnUid();
		String tokenJti = null;
		String idPType = null;
		return getInnerInvokeParam(apiUrl, userName, clientId, userIp, userHostname, txnUid, tokenJti, idPType);
	}
	
	public InnerInvokeParam getInnerInvokeParam(String apiUrl, String userName, String clientId, 
			String userIp, String userHostname, String txnUid, String tokenJti, String idPType) {
		
		//記錄Audit Log功能是否啟用 (true/false)
		boolean isEnable = getTsmpSettingService().getVal_AUDIT_LOG_ENABLE();
		if(!isEnable) {
			return null;
		}
		
		class Iip extends InnerInvokeParam{
			@SuppressWarnings("unused")
			public InnerInvokeParam init() {
				TsmpAuthorization auth = new TsmpAuthorization();
				auth.setUserName(userName);
				auth.setClientId(clientId);
				auth.setJti(tokenJti);
				auth.setIdpType(idPType);
				String origApiUrl = null;
				super.initData(auth, txnUid, apiUrl, userIp, userHostname, origApiUrl);
				return this;
			}
		}
		
		InnerInvokeParam iip = new Iip().init();
		return iip;
	}
	
	public InnerInvokeParam getInnerInvokeParam(TsmpAuthorization auth, String apiUrl, 
			String userIp, String userHostname, String txnUid) {
		
		//記錄Audit Log功能是否啟用 (true/false)
		boolean isEnable = getTsmpSettingService().getVal_AUDIT_LOG_ENABLE();
		if(!isEnable) {
			return null;
		}
		
		class Iip extends InnerInvokeParam{
			@SuppressWarnings("unused")
			public InnerInvokeParam init() {
				String origApiUrl = null;
				super.initData(auth, txnUid, apiUrl, userIp, userHostname, origApiUrl);
				return this;
			}
		}
		
		InnerInvokeParam iip = new Iip().init();
		return iip;
	}
	
	public String getTxnUid() {
		UUID obj = UUID.randomUUID();
		String uid64 = UUID64Util.UUID64(obj);
		return uid64;
	}
	
	protected DgrAuditLogMDao getDgrAuditLogMDao() {
		return dgrAuditLogMDao;
	}
	
	protected DgrAuditLogDDao getDgrAuditLogDDao() {
		return dgrAuditLogDDao;
	}
	
	protected AuthoritiesDao getAuthoritiesDao() {
		return authoritiesDao;
	}
	
	protected TsmpRoleDao getTsmpRoleDao() {
		return tsmpRoleDao;
	}
	
	protected FileHelper getFileHelper() {
		return fileHelper;
	}
	
	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}
	
	protected TsmpUserDao getTsmpUserDao() {
		return tsmpUserDao;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}

	protected TsmpGroupAuthoritiesDao getTsmpGroupAuthoritiesDao() {
		return tsmpGroupAuthoritiesDao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected  DgrWebhookNotifyDao getDgrWebhookNotifyDao(){
		return dgrWebhookNotifyDao;
	}
}

