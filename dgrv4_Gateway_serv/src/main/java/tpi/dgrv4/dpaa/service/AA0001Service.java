package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.BcryptFieldValueEnum;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpModule;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.dpaa.component.job.AA0001Job;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredMailJob;
import tpi.dgrv4.dpaa.util.OAuthUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0001Req;
import tpi.dgrv4.dpaa.vo.AA0001Resp;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.constant.TsmpSequenceName;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.Users;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailTplt;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpDpMailTpltDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpSequenceDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.entity.repository.UsersDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@RequiredArgsConstructor
@Service
@Getter(AccessLevel.PROTECTED)
public class AA0001Service {
	
	private TPILogger logger = TPILogger.tl;
	
	private final TsmpRoleDao tsmpRoleDao;
	private final SeqStoreService seqStoreService;
	private final TsmpOrganizationDao tsmpOrganizationDao;
	private final UsersDao usersDao;
	private final AuthoritiesDao authoritiesDao;
	private final TsmpSequenceDao tsmpSequenceDao;
	private final TsmpUserDao tsmpUserDao;
	private final BcryptParamHelper bcryptParamHelper;
	private final ServiceConfig serviceConfig;
	private final JobHelper jobHelper;
	private final ApplicationContext ctx;
	private final TsmpApiDao tsmpApiDao;
	private final TsmpDpMailTpltDao tsmpDpMailTpltDao;
	private final DgrAuditLogService dgrAuditLogService;
	private final TsmpSettingService tsmpSettingService;
	private final DgrAcIdpUserDao dgrAcIdpUserDao;
	private final TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	
	private String sendTime;

	@PostConstruct
	public void init() {
	}

	@Transactional
	public AA0001Resp addTUser(TsmpAuthorization auth, AA0001Req req, ReqHeader reqHeader, InnerInvokeParam iip) {
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.ADD_USER.value());
		
		AA0001Resp resp = new AA0001Resp();
		String userId = "";
		
		try {
			checkParams(req);
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());
			String encodePassword = getEncodePassword(req.getUserBlock());
			String status = getStatusByBcryptParamHelper(req.getEncodeStatus(), locale);
			
			// update tables
			userId = updateTables(auth, req, encodePassword, status, iip);
			
			// 更新成功後, 發送Email
			sendEmail(req, auth);
			
			// 刪除過期的 Mail log
			deleteExpiredMail();
			
			resp.setUserID(userId);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		return resp;
	}
 
	/**
	 * 
	 * 1244:使用者E-mail:只能為Email格式
	 * 1313:使用者帳號：只能輸入英文字母(a~z,A~Z)、@及數字且不含空白
	 * 1246:使用者帳號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
	 * 1247:使用者名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
	 * 1248:密碼:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
	 * 1249:角色清單:必填參數
	 * 1250:組織名稱:必填參數
	 * 1257:使用者帳號:必填參數, 
	 * 1258:使用者名稱:必填參數, 
	 * 1259:密碼:必填參數, 
	 * 1260:使用者E-mail:必填參數
	 * 檢查roleID是否存在，若不存在則throw RTN Code 1230。
	 * 1288:新增失敗
	 * 1229:組織名稱不存在
	 * 在TSMP_ORGANIZATION資料表用ORG_ID欄位比對"orgID"檢查，若不存在則throw RTN Code 1229
	 * 				
	 * @param tsmpAuthorization
	 * @param req
	 * @throws Exception 
	 */
	protected void checkParams(AA0001Req req) throws Exception {
		String userName = req.getUserName();
		String encodeStatus = req.getEncodeStatus();
		String orgid = req.getOrgID();
		String userAlias = req.getUserAlias();
		String userBlock = req.getUserBlock();
		String userMail = req.getUserMail();
		List<String> roleIds = req.getRoleIDList();
		
		//1249:角色清單:必填參數
		if(roleIds == null )
			throw TsmpDpAaRtnCode._1249.throwing();
		
		//1250:組織名稱:必填參數
		if(StringUtils.isEmpty(orgid))
			throw TsmpDpAaRtnCode._1250.throwing();
		
		//1257:使用者帳號:必填參數
		if (StringUtils.isEmpty(userName)) 
			throw TsmpDpAaRtnCode._1257.throwing();
		
		//1258:使用者名稱:必填參數
		if(StringUtils.isEmpty(userAlias))
			throw TsmpDpAaRtnCode._1258.throwing();
		
		//1259:密碼:必填參數
		if(StringUtils.isEmpty(userBlock))
			throw TsmpDpAaRtnCode._1259.throwing();
		
		//1260:使用者E-mail:必填參數
		if(StringUtils.isEmpty(userMail))
			throw TsmpDpAaRtnCode._1260.throwing();
		
		//1261:狀態:必填參數
		if(StringUtils.isEmpty(encodeStatus))
			throw TsmpDpAaRtnCode._1261.throwing();
		
		//1244:使用者E-mail:只能為Email格式
		//1252:使用者E-mail:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
		checkEmail(userMail);
		
		//1313:使用者帳號：只能輸入英文字母(a~z,A~Z)、@及數字且不含空白
		//1246:使用者帳號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
		checkUSerName(userName);
		
		//1247:使用者名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
		checkUserAlias(userAlias);
		
		//1248:密碼:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
		String decodePassword = base64Decode(req.getUserBlock());
		checkPassword(decodePassword);
		
		// 檢查roleID是否存在，否則拋出 1230:角色不存在
		roleIds.forEach(roleId -> {
			Optional<TsmpRole> opt_rtnRoleId = getTsmpRoleDao().findById(roleId);
			if (!opt_rtnRoleId.isPresent()) {
				throw TsmpDpAaRtnCode._1230.throwing();
			}
		});
		
		//2. 查orgID是否存在，否則拋出1229:組織名稱不存在
		boolean isOrgIdExist = getTsmpOrganizationDao().existsById(orgid);
		if (!isOrgIdExist) {
			throw TsmpDpAaRtnCode._1229.throwing();
		}
		
		TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userName);
        if(tsmpUser != null) {
			//使用者名稱已存在
			throw TsmpDpAaRtnCode._1232.throwing();
		}
		
		// 檢查 userName是否與Delegate AC User重複
		List<DgrAcIdpUser> dgrAcIdpUserList = getDgrAcIdpUserDao().findByUserName(userName);
		if (!CollectionUtils.isEmpty(dgrAcIdpUserList)) {
			throw TsmpDpAaRtnCode._1539.throwing();
		}
		
	}
	
	/**
	 * 1244:使用者E-mail:只能為Email格式
	 * 1252:使用者E-mail:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
	 * 
	 * @param email
	 */
	private void checkEmail(String userMail) {
		String msg = "";
		if (!ServiceUtil.checkEmail(userMail))
			throw TsmpDpAaRtnCode._1244.throwing();
		
		if(userMail.length() > 100) {
			int aa0001_length = userMail.length();
			msg = String.valueOf(aa0001_length);
			throw TsmpDpAaRtnCode._1252.throwing("100",msg);
		}
		
	}
	
	/**
	 * 1247:使用者名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字,
	 * 
	 * @param userAlias
	 */
	private void checkUserAlias(String userAlias) {
		String msg = "";
		if(userAlias.length() > 30) {
			int aa0001_length = userAlias.length();
			msg = String.valueOf(aa0001_length);
			throw TsmpDpAaRtnCode._1247.throwing("30",msg);
		}
	}
	
	/**
	 * 1313:使用者帳號：只能輸入英文字母(a~z,A~Z)、@及數字且不含空白
	 * 1246:使用者帳號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
	 * 
	 * @param userName
	 */
	private void checkUSerName(String userName) {
		String msg = "";
		String rule = "^[\\w|\\-|\\.|@]+$";
		boolean aa0001_isMatches = ServiceUtil.checkDataByPattern(userName, rule);
	
		if (!aa0001_isMatches)
			throw TsmpDpAaRtnCode._1313.throwing();
		
		if(userName.length() > 50) {
			int aa0001_length = userName.length();
			msg = String.valueOf(aa0001_length);
			throw TsmpDpAaRtnCode._1246.throwing("50",msg);
		}
	}
	
	/**
	 * 1248:密碼:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
	 * 
	 * @param userName
	 */
	private void checkPassword(String decodePassword) {
		//Check password strength
		//檢查密碼強度
		if(!decodePassword.matches(this.getTsmpSettingService().getVal_PWD_STRENGTH())) {
			throw TsmpDpAaRtnCode._1352.throwing("{{userBlock}}");
		}
		
		String aa0001_msg = "";
		if(decodePassword.length() > 128) {
			int aa0001_length = decodePassword.length();
			aa0001_msg = String.valueOf(aa0001_length);
			throw TsmpDpAaRtnCode._1248.throwing("128",aa0001_msg);
		}
	}
	
	private String updateTables(TsmpAuthorization auth, AA0001Req req, 
			String password, String status, InnerInvokeParam iip) throws Exception {
		String userId = "";
		addUsersTable(req, password, iip);
		addAuthoritiesTable(req, iip);
		
		userId = addTsmpUserTable(auth, req, status, iip);
		return userId ;
		
	}
	//使用者資料正確就寫入資料表：
	//寫入TSMP_USER資料表，欄位對應USER_ID=userID、USER_NAME=userName、
	//USER_STATUS=status、USER_EMAIL=userMail、
	//CREATE_USER=authorization.getUserName()、CREATE_TIME=DateTimeUtil.now()、
	//PWD_FAIL_TIMES=0、ORG_ID=orgID、USER_ALIAS=userAlias
	private String addTsmpUserTable(TsmpAuthorization auth, AA0001Req req, String status, InnerInvokeParam iip) throws Exception {
		String userId = getUserId();
		
		TsmpUser tsmpUser = new TsmpUser();
		tsmpUser.setCreateTime(DateTimeUtil.now());	//CREATE_TIME=DateTimeUtil.now()
		tsmpUser.setCreateUser(auth.getUserName());	//CREATE_USER=authorization.getUserName()
		tsmpUser.setOrgId(req.getOrgID());				//ORG_ID=orgID
		tsmpUser.setPwdFailTimes(0);	//PWD_FAIL_TIMES=0
		tsmpUser.setUserAlias(req.getUserAlias());	//USER_ALIAS=userAlias
		tsmpUser.setUserEmail(req.getUserMail());	//USER_EMAIL=userMail
		tsmpUser.setUserId(userId);		//USER_ID=userID
		tsmpUser.setUserName(req.getUserName());	//USER_NAME=userName
		tsmpUser.setUserStatus(status);	//USER_STATUS=status
		
		tsmpUser = getTsmpUserDao().saveAndFlush(tsmpUser);
		
		//寫入 Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber,
				TsmpUser.class.getSimpleName(), TableAct.C.value(), null, tsmpUser);// C
		return userId;
	}
	
	//寫入USERS資料表，欄位對應USERNAME=userName、PASSWORD=password 、ENABLED = 1。
	private void addUsersTable(AA0001Req req, String password, InnerInvokeParam iip) throws Exception {
		Users users = new Users();
		users.setUserName(req.getUserName());
		users.setPassword(password);
		users = getUsersDao().saveAndFlush(users);
		
		//寫入 Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber,
				Users.class.getSimpleName(), TableAct.C.value(), null, users);// C
		
	}
	
	//寫入AUTHORITIES資料表，欄位對應USERNAME=userName、AUTHORITY=每一筆roleID。
	private void addAuthoritiesTable(AA0001Req req, InnerInvokeParam iip) throws Exception {
		List<String> roleIds = req.getRoleIDList();
		roleIds.forEach(roleId -> {
			Authorities authorities = new Authorities();
			authorities.setUsername(req.getUserName());
			authorities.setAuthority(roleId);
			authorities = getAuthoritiesDao().saveAndFlush(authorities);
			
			//寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber,
					Authorities.class.getSimpleName(), TableAct.C.value(), null, authorities);// C
		});
	}
	
	protected String getEncodePassword(String password) throws Exception {
		String encodePassword = null;
		encodePassword = OAuthUtil.bCryptEncode(password);
		return encodePassword;
		
	}
	
	/**
	 * 後台-tsmpaa( v3.8) API
	 * 
	 * 使用BcryptParam, 
	 * ITEM_NO='ENABLE_FLAG' , DB儲存值對應代碼如下:
	 * DB值 (PARAM1) = 中文說明; 
	 * 1=啟用, 2=停用
	 * @param encodeStatus
	 * @return
	 */
	protected String getStatusByBcryptParamHelper(String encodeStatus, String locale) {
		String status = null;
		try {
			status = getBcryptParamHelper().decode(encodeStatus, "ENABLE_FLAG", BcryptFieldValueEnum.PARAM1, locale);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return status;
	}
	
	
	protected String getUserId() throws Exception {
		/*取得userID流程說明:*/
		String userId = "";
		
		Long userID = getSeqStoreService().nextTsmpSequence(TsmpSequenceName.SEQ_TSMP_USER_PK);
		userId = userID+"";
		if (userID != null) {
			userId = userID.toString();
		}
		if(StringUtils.isEmpty(userId)) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return userId;
	}
	
	public AA0001Job sendEmail(AA0001Req req, TsmpAuthorization auth) {
		List<TsmpMailEvent> mailEvents = new ArrayList<>();
			TsmpMailEvent mailEvent = getTsmpMailEvent(req, auth);
			if (mailEvent != null) {
				mailEvents.add(mailEvent);
			}
		//使用 Job 寫入 APPT_JOB Table & 建立 Mail 檔案, 由排程來寄信
//		AA0001Job job = (AA0001Job) getCtx().getBean("aa0001Job", auth, mailEvents, getSendTime());
//		getJobHelper().add(job);
			
		AA0001Job job = getAA0001Job(auth, mailEvents, getSendTime());
		getJobHelper().add(job);
			
		return job;
	}

	protected DeleteExpiredMailJob deleteExpiredMail() {
		DeleteExpiredMailJob job = (DeleteExpiredMailJob) getCtx().getBean("deleteExpiredMailJob");
		getJobHelper().add(job);
		return job;
	}
	
	private TsmpMailEvent getTsmpMailEvent( AA0001Req req , TsmpAuthorization authorization) {
		String aa0001_clientId = authorization.getClientId();
		String aa0001_recipients = req.getUserMail();
		
		if (aa0001_recipients == null || aa0001_recipients.isEmpty()) {
			this.logger.debug(String.format("Client %s has empty emails!", aa0001_clientId));
			return null;
		}

		String aa0001_subject = null;
		String aa0001_body = null;
		aa0001_subject = getTemplate("subject.addUser-pass");
		aa0001_body = getTemplate("body.addUser-pass");
		
		if (aa0001_subject == null || aa0001_body == null) {
			return null;
		}

		Map<String, String> subjectParams = getSubjectParams();
		if (subjectParams == null || subjectParams.isEmpty()) {
			this.logger.debug(String.format("Client %s has empty subject params!", aa0001_clientId));
			return null;
		}

		Map<String, String> bodyParams = getBodyParams(req);
		if (bodyParams == null || bodyParams.isEmpty()) {
			this.logger.debug(String.format("Client %s has empty body params!", aa0001_clientId));
			return null;
		}

		final String title = MailHelper.buildContent(aa0001_subject, subjectParams);
		final String content = MailHelper.buildContent(aa0001_body, bodyParams);
		this.logger.debug("Email title = " + title);
		this.logger.debug("Email content = " + content);
		
		return new TsmpMailEventBuilder() //
		.setSubject(title)
		.setContent(content)
		.setRecipients(aa0001_recipients)
		.setCreateUser(authorization.getUserName())
		.setRefCode("body.addUser-pass")
		.build();
	}
	
	private String getTemplate(String code) {
		List<TsmpDpMailTplt> aa0001_list = getTsmpDpMailTpltDao().findByCode(code);
		if (aa0001_list != null && !aa0001_list.isEmpty()) {
			return aa0001_list.get(0).getTemplateTxt();
		}
		return null;
	}
	
	private Map<String, String> getSubjectParams() {
		Map<String, String> aa0001_emailParams = new HashMap<>();
		aa0001_emailParams.put("projectName", TsmpDpModule.DP.getChiDesc());
		return aa0001_emailParams;
	}
	
	protected String base64Decode(String userBlock) {
		return new String(ServiceUtil.base64Decode(userBlock));
			
	}
	
	private Map<String, String> getBodyParams(AA0001Req req) {

		Map<String, String> emailParams = new HashMap<>();
		
		String now = "";
		Optional<String> opt = DateTimeUtil.dateTimeToString(DateTimeUtil.now(), DateTimeFormatEnum.西元年月日_2);
		if (opt.isPresent()) {
			now = opt.get();
		}
		emailParams.put("date", now);
		emailParams.put("tUser", req.getUserName());
		return emailParams;
	}
 
	protected String getSendTime() {
		this.sendTime = this.getTsmpSettingService().getVal_MAIL_SEND_TIME();//多久後寄發Email(ms)
		return this.sendTime;
	}

	protected AA0001Job getAA0001Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime) {
		return (AA0001Job) getCtx().getBean("aa0001Job", auth, mailEvents, getSendTime());
	}
}
