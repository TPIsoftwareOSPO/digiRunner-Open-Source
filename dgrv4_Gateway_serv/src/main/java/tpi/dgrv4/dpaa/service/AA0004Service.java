package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
import tpi.dgrv4.dpaa.component.job.AA0004Job;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredMailJob;
import tpi.dgrv4.dpaa.util.OAuthUtil;
import tpi.dgrv4.dpaa.util.RandomUtils;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0004Req;
import tpi.dgrv4.dpaa.vo.AA0004Resp;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpTokenHistory;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.Users;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailTplt;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpDpMailTpltDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpTokenHistoryDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.entity.repository.UsersDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.component.cache.proxy.AuthoritiesCacheProxy;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0004Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpUserDao tsmpUserDao;
	private TsmpRoleDao tsmpRoleDao;
	private AuthoritiesDao authoritiesDao;
	private UsersDao usersDao;
	private BcryptParamHelper bcryptParamHelper;
	private ApplicationContext ctx;
	private JobHelper jobHelper;
	private TsmpDpMailTpltDao tsmpDpMailTpltDao;
	private DgrAuditLogService dgrAuditLogService;
	private DgrAcIdpUserDao dgrAcIdpUserDao;
	private TsmpTokenHistoryDao tsmpTokenHistoryDao;
	private TsmpSettingService tsmpSettingService;
	private DaoGenericCacheService daoGenericCacheService;
	private AuthoritiesCacheProxy authoritiesCacheProxy;
	
	private String sendTime;
	
	@Autowired
	public AA0004Service(TsmpOrganizationDao tsmpOrganizationDao, TsmpUserDao tsmpUserDao, TsmpRoleDao tsmpRoleDao,
			AuthoritiesDao authoritiesDao, UsersDao usersDao, BcryptParamHelper bcryptParamHelper,
			ApplicationContext ctx, JobHelper jobHelper, TsmpDpMailTpltDao tsmpDpMailTpltDao,
			DgrAuditLogService dgrAuditLogService, DgrAcIdpUserDao dgrAcIdpUserDao,
			TsmpTokenHistoryDao tsmpTokenHistoryDao, TsmpSettingService tsmpSettingService,
			DaoGenericCacheService daoGenericCacheService, AuthoritiesCacheProxy authoritiesCacheProxy) {
		super();
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpUserDao = tsmpUserDao;
		this.tsmpRoleDao = tsmpRoleDao;
		this.authoritiesDao = authoritiesDao;
		this.usersDao = usersDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.ctx = ctx;
		this.jobHelper = jobHelper;
		this.tsmpDpMailTpltDao = tsmpDpMailTpltDao;
		this.dgrAuditLogService = dgrAuditLogService;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
		this.tsmpTokenHistoryDao = tsmpTokenHistoryDao;
		this.tsmpSettingService = tsmpSettingService;
		this.daoGenericCacheService = daoGenericCacheService;
		this.authoritiesCacheProxy = authoritiesCacheProxy;
	}

	@PostConstruct
	public void init() {
		this.sendTime = "0"; //設為0,不等待,因重置密碼,必須馬上寄發Email
	}
	
	@Transactional
	public AA0004Resp updateTUserState(TsmpAuthorization authorization, AA0004Req req, ReqHeader reqHeader, 
			InnerInvokeParam iip) {
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_USER.value());
		
		AA0004Resp resp = new AA0004Resp();

		try {
			String newOrgId = req.getNewOrgID();
			List<String> newRoleIdList = req.getNewRoleIDList();
			String newStatus = req.getNewStatus();
			String newUserAlias = req.getNewUserAlias();
			String newUserMail = req.getNewUserMail();
			String newUserName = req.getNewUserName();
			String orgId = req.getOrgID();
			List<String> roleIdList = req.getRoleIDList();
			String status = req.getStatus();
			String userAlias = req.getUserAlias();
			String userId = req.getUserID();
			String userMail = req.getUserMail();
			String userName = req.getUserName();
			boolean isResetBlock = req.isResetBlock();
			boolean isResetPwdFailTimes = req.isResetPwdFailTimes();
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());
			String authName = authorization.getUserName();
			//check param
			checkParam(authName, req);
			
			status = getValueByBcryptParamHelper(status, "ENABLE_FLAG", locale);
			newStatus = getValueByBcryptParamHelper(newStatus, "ENABLE_FLAG", locale);
			boolean tokenFlag = false;
			//更新邏輯
			if(!newUserName.equals(userName)) {//使用者帳號不同值
				tokenFlag = true;
				TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(newUserName);
				if(tsmpUser != null) {
					//使用者名稱已存在
					throw TsmpDpAaRtnCode._1232.throwing();
				}
				
				// 檢查 userName是否與Delegate AC User重複
				List<DgrAcIdpUser> dgrAcIdpUserList = getDgrAcIdpUserDao().findByUserName(newUserName);
				if (!CollectionUtils.isEmpty(dgrAcIdpUserList)) {
					throw TsmpDpAaRtnCode._1539.throwing();
				}
				
				//Authorities
				List<Authorities> authoritiesList = getAuthoritiesDao().findByUsername(userName);
				getAuthoritiesDao().deleteAll(authoritiesList);				
				//寫入 Audit Log D
				if(iip != null) {
					lineNumber = StackTraceUtil.getLineNumber();
					for (Authorities authorities : authoritiesList) {
						String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, authorities); //舊資料統一轉成 String
						getDgrAuditLogService().createAuditLogD(iip, lineNumber,
								Authorities.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);// D
					}
				}
				
				//USERS

				Users newUsersVo = new Users();

				var user = getUsersDao().findById(userName);

				if (user.isPresent()) {
					newUsersVo.setPassword(user.get().getPassword());
					newUsersVo.setUserStatus(user.get().getUserStatus());
				}
				newUsersVo.setUserName(newUserName);
				
				Optional<Users> opt_users = getUsersDao().findById(userName);
				if(opt_users.isPresent()) {
					getUsersDao().delete(opt_users.get());
					//寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();
					String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, opt_users.get()); //舊資料統一轉成 String
					getDgrAuditLogService().createAuditLogD(iip, lineNumber,
							Users.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);// D
				}
				
				newUsersVo = getUsersDao().save(newUsersVo);
				//寫入 Audit Log D
				lineNumber = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogD(iip, lineNumber,
						Users.class.getSimpleName(), TableAct.C.value(), null, newUsersVo);// C
				
				//AUTHORITIES
				newRoleIdList.forEach(roleId ->{
					Authorities authVo = new Authorities();
					authVo.setUsername(newUserName);
					authVo.setAuthority(roleId);
					authVo = getAuthoritiesDao().save(authVo);
					//寫入 Audit Log D
					String lineNumber2 = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogD(iip, lineNumber2,
							Authorities.class.getSimpleName(), TableAct.C.value(), null, authVo);// C
				});
				
				//TSMP_USER
				updateTsmpUser(req, authorization.getUserName(), locale, iip);
				
			}else {//其他不同值
				
				if(this.isRoleIdDiffer(roleIdList, newRoleIdList)) {
					tokenFlag = true;
					//AUTHORITIES
					List<Authorities> authoritiesList = getAuthoritiesDao().findByUsername(userName);
					getAuthoritiesDao().deleteAll(authoritiesList);
					//寫入 Audit Log D
					if(iip != null) {
						for (Authorities authorities : authoritiesList) {
							String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, authorities); //舊資料統一轉成 String
							lineNumber = StackTraceUtil.getLineNumber();
							getDgrAuditLogService().createAuditLogD(iip, lineNumber,
									Authorities.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);// D
						}
					}
					
					newRoleIdList.forEach(roleId ->{
						Authorities authVo = new Authorities();
						authVo.setUsername(newUserName);
						authVo.setAuthority(roleId);
						authVo = getAuthoritiesDao().save(authVo);
						//寫入 Audit Log D
						String lineNumber2 = StackTraceUtil.getLineNumber();
						getDgrAuditLogService().createAuditLogD(iip, lineNumber2,
								Authorities.class.getSimpleName(), TableAct.C.value(), null, authVo);// C
					});
				}
				
				//是否有變更組織單位
				tokenFlag = !newOrgId.equals(orgId) ;
				
				if(!newOrgId.equals(orgId) || !newStatus.equals(status) || (newUserAlias != null && !newUserAlias.equals(userAlias))
						|| !newUserMail.equals(userMail) || isResetPwdFailTimes) {
					//TSMP_USER
					updateTsmpUser(req, authorization.getUserName(), locale, iip);
				}
			}
			
			//重置密碼
			if(isResetBlock) {
				String pwd = this.getRandom(10);
				String base64Encode = ServiceUtil.base64Encode(pwd.getBytes());
				String bcryptEncode = OAuthUtil.bCryptEncode(base64Encode);
				
				//更新Users資料
				Users usersVo = getUsersDao().findById(newUserName).orElse(null);
				if(usersVo != null) {
					String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, usersVo); //舊資料統一轉成 String
 
					usersVo.setPassword(bcryptEncode);
					usersVo = getUsersDao().save(usersVo);
					//寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
							Users.class.getSimpleName(), TableAct.U.value(), oldRowStr, usersVo);// U
				}
				
				// 更新成功後, 發送Email
				sendEmail(req, authorization, pwd);
				
				// 刪除過期的 Mail log
				deleteExpiredMail();
			}
			
//			(若ROFILEUPDATE_INVALIDATE_TOKEN為true && 有異動到TSMP_USER資料)則要註銷Token
			boolean settingFalg = getTsmpSettingService().getVal_PROFILEUPDATE_INVALIDATE_TOKEN();
			if (settingFalg && tokenFlag) {
				invalidateToken(userName);
			}

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1286.throwing();
		}

		return resp;
	}
	
	private void invalidateToken(String userName) {
		List<TsmpTokenHistory> tokenHistories = getTsmpTokenHistoryDao().findByUserName(userName);
		Date now = DateTimeUtil.now();
		String revokedStatus = null;
		String rftRevokedStatus = null;
		if (!CollectionUtils.isEmpty(tokenHistories)) {
			for (TsmpTokenHistory tsmpTokenHistory : tokenHistories) {
				
				revokedStatus = tsmpTokenHistory.getRevokedStatus();
				if (!StringUtils.hasLength(revokedStatus)) {
					tsmpTokenHistory.setRevokedStatus("R");
					tsmpTokenHistory.setRevokedAt(now);
				}
				
				rftRevokedStatus = tsmpTokenHistory.getRftRevokedStatus();
				if (!StringUtils.hasLength(rftRevokedStatus)) {
					tsmpTokenHistory.setRftRevokedStatus("R");
					tsmpTokenHistory.setRftRevokedAt(now);
				}

			}
			
			getTsmpTokenHistoryDao().saveAllAndFlush(tokenHistories);
			//清除快取
			getDaoGenericCacheService().clearAndNotify();
		}
	}

	protected String getValueByBcryptParamHelper(String encodeValue, String itemNo, String locale) {
		String value = null;
		try {
			value = getBcryptParamHelper().decode(encodeValue, itemNo, BcryptFieldValueEnum.PARAM1, locale);// BcryptParam解碼
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return value;
	}
	
	private void checkParam(String authName, AA0004Req req) {
		
		String newOrgId = req.getNewOrgID();
		List<String> newRoleIdList = req.getNewRoleIDList();
		String newUserAlias = req.getNewUserAlias();
		String newUserMail = req.getNewUserMail();
		String newUserName = req.getNewUserName();
		String userName = req.getUserName();
		
		String newUserNameRule = "^[\\w|\\-|\\.|@]+$";
		boolean isNewUserNameMatches = ServiceUtil.checkDataByPattern(newUserName, newUserNameRule);
		
		//check param
		if(StringUtils.isEmpty(newUserName)) {
			//使用者帳號:必填參數
			throw TsmpDpAaRtnCode._1257.throwing();
		}
		
		if(StringUtils.isEmpty(newUserMail)) {
			//使用者E-mail:必填參數
			throw TsmpDpAaRtnCode._1260.throwing();
		}
		
		if(StringUtils.isEmpty(newOrgId)) {
			//組織名稱:必填參數
			throw TsmpDpAaRtnCode._1250.throwing();
		}
		
		if(newRoleIdList == null || newRoleIdList.size() == 0) {
			//角色清單:必填參數
			throw TsmpDpAaRtnCode._1249.throwing();
		}
		
		if(newUserName.length() > 50) {
			//使用者帳號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
			throw TsmpDpAaRtnCode._1246.throwing("50", String.valueOf(newUserName.length()));
		}
		
		if(!isNewUserNameMatches) {
			//使用者帳號：只能輸入英文字母(a~z,A~Z)、@及數字且不含空白
			throw TsmpDpAaRtnCode._1313.throwing();
		}
		
		if(newUserMail.length() > 100) {
			//使用者E-mail:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
			throw TsmpDpAaRtnCode._1252.throwing("100", String.valueOf(newUserMail.length()));
		}
		
		if(!ServiceUtil.checkEmail(newUserMail)) {
			//使用者E-mail:只能為Email格式
			throw TsmpDpAaRtnCode._1244.throwing();
		}
		
		if(newUserAlias != null && newUserAlias.length() > 30) {
			//使用者名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
			throw TsmpDpAaRtnCode._1247.throwing("30", String.valueOf(newUserAlias.length()));
		}
		
		if(newOrgId.length() > 30) {
			//組織名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
			throw TsmpDpAaRtnCode._1253.throwing("30", String.valueOf(newOrgId.length()));
		}
		
		Optional<TsmpOrganization> orgOptional = getTsmpOrganizationDao().findById(newOrgId);
		if (!orgOptional.isPresent()) {
			//組織名稱不存在
			throw TsmpDpAaRtnCode._1229.throwing();
		}
		
		TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userName);
		if(tsmpUser == null) {
			//使用者不存在
			throw TsmpDpAaRtnCode._1231.throwing();
		}
		
		checkPermissions(authName, req.getUserName());
		
		newRoleIdList.forEach(roleId ->{
			Optional<TsmpRole> roleOptional = getTsmpRoleDao().findById(roleId);
			if (!roleOptional.isPresent()) {
				//角色不存在
				throw TsmpDpAaRtnCode._1230.throwing();
			}
		});
	}
	
	private void checkPermissions(String authName, String userName) {
		boolean settingFlag = getTsmpSettingService().getVal_USER_UPDATE_BY_SELF();
		Authorities authorities = getAuthoritiesCacheProxy().findFirstByUserName(authName);
		String authority = authorities.getAuthority();
		boolean roleFlag = "1000".equals(authority);
		// ADMIN角色可修改自己 &&是否允許修正自身帳號(預設是可以) && 自己不可異動自己的資訊
		if (!roleFlag && settingFlag && authName.equals(userName)) {
			throw TsmpDpAaRtnCode._1219.throwing();
		}
	}
	
	private void updateTsmpUser(AA0004Req req, String updateUser, String locale, InnerInvokeParam iip) {
		String newOrgId = req.getNewOrgID();
		String newStatus = req.getNewStatus();
		String newUserAlias = req.getNewUserAlias();
		String newUserMail = req.getNewUserMail();
		String newUserName = req.getNewUserName();
		String userName = req.getUserName();
		boolean isResetPwdFailTimes = req.isResetPwdFailTimes();
		
		newStatus = getValueByBcryptParamHelper(newStatus, "ENABLE_FLAG", locale);
		
		TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userName);
		
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpUser); //舊資料統一轉成 String
		
		tsmpUser.setUserName(newUserName);
		tsmpUser.setUserEmail(newUserMail);
		tsmpUser.setUserAlias(newUserAlias);
		tsmpUser.setUserStatus(newStatus);
		tsmpUser.setOrgId(newOrgId);
		if(isResetPwdFailTimes) {
			tsmpUser.setPwdFailTimes(0);
		}
		tsmpUser.setUpdateUser(updateUser);
		tsmpUser.setUpdateTime(DateTimeUtil.now());
		tsmpUser = getTsmpUserDao().save(tsmpUser);
		//寫入 Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
				TsmpUser.class.getSimpleName(), TableAct.U.value(), oldRowStr, tsmpUser);// U
	}
	
	public boolean isRoleIdDiffer(List<String> roleIdList, List<String> newRoleIdList) {
		if(roleIdList.size() != newRoleIdList.size()) {
			return true;
		}else if(!newRoleIdList.containsAll(roleIdList)) {
			return true;
		}else {
			return false;
		}
	}
	
	private String getRandom(int len) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ; i < len ; i++) {
			String number = RandomUtils.randomString(1, false, true);//取得亂數
			if(i == 0 && "0".equals(number)) {
				number = "1";
			}
			sb.append(number);
		}
		return sb.toString();
	}
	
	public AA0004Job sendEmail(AA0004Req req, TsmpAuthorization auth, String pwd) {
		List<TsmpMailEvent> mailEvents = new ArrayList<>();
			TsmpMailEvent mailEvent = getTsmpMailEvent(req, auth, pwd);
			if (mailEvent != null) {
				mailEvents.add(mailEvent);
			}
			
		AA0004Job job = getAA0004Job(auth, mailEvents, getSendTime());
		getJobHelper().add(job);
			
		return job;
	}
	
	public DeleteExpiredMailJob deleteExpiredMail() {
		DeleteExpiredMailJob job = (DeleteExpiredMailJob) getCtx().getBean("deleteExpiredMailJob");
		getJobHelper().add(job);
		return job;
	}
	
	private TsmpMailEvent getTsmpMailEvent( AA0004Req req , TsmpAuthorization authorization, String pwd) {
		String aa0004_clientId = authorization.getClientId();
		String aa0004_recipients = req.getNewUserMail();
		
		if (aa0004_recipients == null || aa0004_recipients.isEmpty()) {
			this.logger.debug(String.format("Client %s has empty emails!", aa0004_clientId));
			return null;
		}

		String aa0004_subject = getTemplate("subject.updUser-pwd");
		String aa0004_body = getTemplate("body.updUser-pwd");
		
		if (aa0004_subject == null || aa0004_body == null) {
			return null;
		}

		Map<String, String> aa0004_subjectParams = getSubjectParams();
		if (aa0004_subjectParams == null || aa0004_subjectParams.isEmpty()) {
			this.logger.debug(String.format("Client %s has empty subject params!", aa0004_clientId));
			return null;
		}

		Map<String, String> aa0004_bodyParams = getBodyParams(req, pwd);
		if (aa0004_bodyParams == null || aa0004_bodyParams.isEmpty()) {
			this.logger.debug(String.format("Client %s has empty body params!", aa0004_clientId));
			return null;
		}

		final String aa0004_title = MailHelper.buildContent(aa0004_subject, aa0004_subjectParams);
		final String aa0004_content = MailHelper.buildContent(aa0004_body, aa0004_bodyParams);
		this.logger.debug("Email title = " + aa0004_title);
		this.logger.debug("Email content = " + aa0004_content);
		
		return new TsmpMailEventBuilder() //
		.setSubject(aa0004_title)
		.setContent(aa0004_content)
		.setRecipients(aa0004_recipients)
		.setCreateUser(authorization.getUserName())
		.setRefCode("body.updUser-pwd")
		.build();
	}

	private String getTemplate(String code) {
		List<TsmpDpMailTplt> aa0004_list = getTsmpDpMailTpltDao().findByCode(code);
		if (aa0004_list != null && !aa0004_list.isEmpty()) {
			return aa0004_list.get(0).getTemplateTxt();
		}
		return null;
	}
	
	private Map<String, String> getSubjectParams() {
		Map<String, String> aa0004_emailParams = new HashMap<>();
		aa0004_emailParams.put("projectName", TsmpDpModule.DP.getChiDesc());
		return aa0004_emailParams;
	}
	
	private Map<String, String> getBodyParams(AA0004Req req, String pwd) {

		Map<String, String> emailParams = new HashMap<>();
		
		String now = "";
		Optional<String> opt = DateTimeUtil.dateTimeToString(DateTimeUtil.now(), DateTimeFormatEnum.西元年月日_2);
		if (opt.isPresent()) {
			now = opt.get();
		}
		emailParams.put("date", now);
		emailParams.put("tUser", req.getNewUserName());
		emailParams.put("newBlock", pwd);
		
		return emailParams;
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return tsmpOrganizationDao;
	}

	protected TsmpUserDao getTsmpUserDao() {
		return tsmpUserDao;
	}

	protected TsmpRoleDao getTsmpRoleDao() {
		return tsmpRoleDao;
	}

	protected AuthoritiesDao getAuthoritiesDao() {
		return authoritiesDao;
	}

	protected UsersDao getUsersDao() {
		return usersDao;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected ApplicationContext getCtx() {
		return ctx;
	}

	protected JobHelper getJobHelper() {
		return jobHelper;
	}
	
	protected TsmpDpMailTpltDao getTsmpDpMailTpltDao() {
		return this.tsmpDpMailTpltDao;
	}

	protected String getSendTime() {
		return this.sendTime;
	}
	
	protected AA0004Job getAA0004Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime) {
		return (AA0004Job) getCtx().getBean("aa0004Job", auth, mailEvents, getSendTime());
	}
 
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return this.dgrAcIdpUserDao;
	}
	
	protected TsmpTokenHistoryDao getTsmpTokenHistoryDao() {
		return tsmpTokenHistoryDao;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
	protected DaoGenericCacheService getDaoGenericCacheService() {
		return daoGenericCacheService;
	}
	
	protected AuthoritiesCacheProxy getAuthoritiesCacheProxy() {
		return authoritiesCacheProxy;
	}
}
