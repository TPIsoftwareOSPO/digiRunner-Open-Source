package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.OAuthUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0006Req;
import tpi.dgrv4.dpaa.vo.AA0006Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.Users;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.entity.repository.UsersDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0006Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpOrganizationDao tsmpOrganizationDao;
	private DgrAuditLogService dgrAuditLogService;
	private AuthoritiesDao authoritiesDao;
	private BcryptParamHelper bcryptParamHelper;
	private TsmpUserDao tsmpUserDao;
	private UsersDao usersDao;
	private TsmpRoleDao tsmpRoleDao;
	
	@Autowired
	public AA0006Service(TsmpOrganizationDao tsmpOrganizationDao, DgrAuditLogService dgrAuditLogService,
			AuthoritiesDao authoritiesDao, BcryptParamHelper bcryptParamHelper, TsmpUserDao tsmpUserDao,
			UsersDao usersDao, TsmpRoleDao tsmpRoleDao) {
		super();
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.dgrAuditLogService = dgrAuditLogService;
		this.authoritiesDao = authoritiesDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.tsmpUserDao = tsmpUserDao;
		this.usersDao = usersDao;
		this.tsmpRoleDao = tsmpRoleDao;
	}

	@Transactional
	public AA0006Resp updateTUserData(TsmpAuthorization auth, AA0006Req req, InnerInvokeParam iip) {
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_USER_PROFILE.value());
		
		AA0006Resp resp = new AA0006Resp();
		
		try {
			checkParams(req);
			String encodePassword = getEncodePassword(ServiceUtil.nvl(req.getNewUserBlock()));
			
			// update tables
			updateTables(auth, req, encodePassword, iip);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1286.throwing();
				
		}
		
		return resp;
	}
	
	/**
	 * 檢查輸入的參數
	 * 1.若AA0006Req.userBlock為空，AA0006Req.newUserBlock不回空，則throw RTN CODE 1236。
	 * 2.若AA0006Req.userBlock不為空，AA0006Req.newUserBlock為空，則throw RTN CODE 1234。
	 * 3.若AA0006Req.userBlock與AA0006Req.newUserBlock一樣，則throw RTN CODE 1235。
	 * 4.若AA0006Req.newUserName、AA0006Req.newUserMail、AA0006Req.userBlock、AA0006Req.newUserBlock、AA0006Req.userAlias同時為空，則throw RTN CODE 1237。
	 * 5.以AA0006Req.userName對TSMP_USER資料表(USER_NAME欄位)查詢，若不存在則throw RTN CODE 1231。
	 * 6.檢查AA0006Req.userBlock是否為原密碼。在查詢USERS資料表(條件AA0006Req.newUserName=USERS.USERNAME)，AA0006Req.userBlock與USERS.PASSWORD欄位比對(有bcrypt加密)，若不正確則throw RTN CODE 1238。
	 * 7.AA0006Req.userName與AA0006Req.newUserName一樣，則步驟8與9不用執行。
	 * 8.以AA0006Req.newUserName對TSMP_USER資料表(USER_ID與USER_NAME欄位)查詢，若存在資料則throw RTN CODE 1232。
	 * 9.對AUTHORITIES進行更新，以AUTHORITIES.USERNAME欄位 = AA0006Req.userName為條件，更新AUTHORITIES.USERNAME=AA0006Req.newUserName。
	 * 				
	 * @param tsmpAuthorization
	 * @param req
	 * @throws Exception 
	 */
	protected void checkParams(AA0006Req req) throws Exception {
		String userName = ServiceUtil.nvl(req.getUserName());
		String newUserName = ServiceUtil.nvl(req.getNewUserName());
		String newUserAlias = ServiceUtil.nvl(req.getNewUserAlias());
		String userBlock = ServiceUtil.nvl(req.getUserBlock());
		String newUserBlock = ServiceUtil.nvl(req.getNewUserBlock());
		String newUserMail = ServiceUtil.nvl(req.getNewUserMail());
		
		//1237:至少輸入一項
//		if(StringUtils.isEmpty(newUserName) && StringUtils.isEmpty(newUserMail) && StringUtils.isEmpty(userBlock) &&
//				StringUtils.isEmpty(newUserBlock) && StringUtils.isEmpty(userAlias)) 
//			throw TsmpDpAaRtnCode._1237.throwing();
		
		// 1257:使用者帳號:必填參數
		if(StringUtils.isEmpty(newUserName)) 
			throw TsmpDpAaRtnCode._1257.throwing();
		
		//1258:使用者名稱:必填參數
		if(StringUtils.isEmpty(newUserAlias)) 
			throw TsmpDpAaRtnCode._1258.throwing();
		
		//1260:使用者E-mail:必填參數
		if(StringUtils.isEmpty(newUserMail)) 
			throw TsmpDpAaRtnCode._1260.throwing();
		
		//1270:使用者帳號已存在
		if(!newUserName.equals(userName)) {
			boolean isUserExisted = isUserExisted(newUserName);
			if(isUserExisted)
				throw TsmpDpAaRtnCode._1270.throwing();
			
		}
		//==========================================================================================================
		
		//1244:使用者E-mail:只能為Email格式
		//1252:使用者E-mail:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
		checkEmail(newUserMail);
		
		//1313:使用者帳號：只能輸入英文字母(a~z,A~Z)、@及數字且不含空白
		//1246:使用者帳號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
		checkUSerName(newUserName);
		
		//1247:使用者名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
		checkUserAlias(newUserAlias);
		
		
		//==========================================================================================================

		// 若原密碼為空,但新密碼有值,則拋出錯誤
		// If the original password is empty but the new password has a value, an error is thrown
		if(!StringUtils.hasLength(userBlock) && StringUtils.hasLength(newUserBlock) )
			throw TsmpDpAaRtnCode._1236.throwing(); // 1236:原密碼不可為空
		
		// 若原密碼有值,但新密碼為空,則拋出錯誤
		// If the original password has a value but the new password is empty, an error is thrown
		if(StringUtils.hasLength(userBlock) && !StringUtils.hasLength(newUserBlock))
			throw TsmpDpAaRtnCode._1234.throwing(); // 1234:新密碼不可為空
	
		// 原密碼和新密碼都有值,則檢查資料
		// If both the original password and the new password have values, check the data
		if(StringUtils.hasLength(userBlock) && StringUtils.hasLength(newUserBlock)) {
			// 檢查原密碼是否正確
			//Check if the original password is correct
			boolean checkOriginPassword = checkOriginPassword(userName, userBlock);
			if(!checkOriginPassword)
				throw TsmpDpAaRtnCode._1238.throwing(); // 1238:原密碼不正確
			
			// 檢查新密碼與原密碼是否相同
			// Check if the new password is the same as the original password
			if (userBlock.equals(newUserBlock)) 
				throw TsmpDpAaRtnCode._1235.throwing(); // 1235:新密碼與原密碼相同
			
			// 檢查新密碼長度
			// Check new password length
			String decodePassword = base64Decode(req.getNewUserBlock());
			checkPassword(decodePassword); // 1248:密碼:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
		}
		
		//userName equlas newUserName
		if(!userName.equals(newUserName)) {
			// 1232。使用者名稱已存在
			boolean isNewUserNameExisted = isNewUserNameExisted(newUserName);
			if(isNewUserNameExisted)
				throw TsmpDpAaRtnCode._1232.throwing();
		}
	}
		
		
	/**
	 * 1244:使用者E-mail:只能為Email格式
	 * 1252:使用者E-mail:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
	 * @param email
	 */
	private void checkEmail(String newUserMail) {
		String aa0006_msg = "";
		boolean bool = ServiceUtil.checkEmail(newUserMail);
		if (!bool)
			throw TsmpDpAaRtnCode._1244.throwing();
		
		if(newUserMail.length() > 100) {
			int length = newUserMail.length();
			aa0006_msg = String.valueOf(length);
			throw TsmpDpAaRtnCode._1252.throwing("100",aa0006_msg);
		}
	}
	
	/**
	 * 1247:使用者名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字,
	 * 
	 * @param userAlias
	 */
	private void checkUserAlias(String userAlias) {
		String aa0006_msg = "";
		if(userAlias.length() > 30) {
			int aa0006_length = userAlias.length();
			aa0006_msg = String.valueOf(aa0006_length);
			throw TsmpDpAaRtnCode._1247.throwing("30",aa0006_msg);
		}
	}
	
	/**
	 * 1313:使用者帳號：只能輸入英文字母(a~z,A~Z)、@及數字且不含空白
	 * 1246:使用者帳號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
	 * 
	 * @param userName
	 */
	private void checkUSerName(String userName) {
		String aa0006_msg = "";
		String rule = "^[\\w|\\-|\\.|@]+$";
		boolean aa0006_isMatches = ServiceUtil.checkDataByPattern(userName, rule);
	
		if (!aa0006_isMatches)
			throw TsmpDpAaRtnCode._1313.throwing();
		
		if(userName.length() > 50) {
			int aa0006_length = userName.length();
			aa0006_msg = String.valueOf(aa0006_length);
			throw TsmpDpAaRtnCode._1246.throwing("50",aa0006_msg);
		}
	}
	
	/**
	 * 1248:密碼:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
	 * 
	 * @param userName
	 */
	private void checkPassword(String decodePassword) {
		String aa0006_msg = "";
		if(decodePassword.length() > 128) {
			int aa0006_length = decodePassword.length();
			aa0006_msg = String.valueOf(aa0006_length);
			throw TsmpDpAaRtnCode._1248.throwing("128", aa0006_msg);
		}
	}
	
	private boolean isUserExisted(String userName) {
		boolean isUserExisted = false;
		TsmpUser tsmpUSer = getTsmpUserDao().findFirstByUserName(userName);
		if(tsmpUSer != null)
			isUserExisted = true;
			
		return isUserExisted;
		
	}
	
	private boolean checkOriginPassword(String userName, String userBlock) {
	//查詢USERS資料表(條件AA0006Req.newUserName=USERS.USERNAME)，AA0006Req.userBlock與USERS.PASSWORD欄位比對(有bcrypt加密)，若不正確則throw RTN CODE 1238。	
		boolean isOriginPassword = false;
		Optional<Users> user = getUsersDao().findById(userName);
		if(user.isPresent()) {
			String password = user.get().getPassword();
			isOriginPassword = OAuthUtil.bCryptPasswordCheck(userBlock, password);
			
		}
		
		return isOriginPassword;
	}
	
	private boolean isNewUserNameExisted(String newUserName) {
		//AA0006Req.newUserName對TSMP_USER資料表(USER_ID與USER_NAME欄位)查詢，若存在資料則throw RTN CODE 1232
		boolean isNewUserNameExisted = false;
		TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(newUserName);
		if(tsmpUser != null)
			isNewUserNameExisted = true;
		return isNewUserNameExisted;
		
	}
	private void updateTables(TsmpAuthorization auth, AA0006Req req, String password, InnerInvokeParam iip) throws Exception {
		// * 11.將AA0006Req的newXXX欄位更新到TSMP_USER，記得更新TSMP_USER.UPDATE_USER=TsmpHttpHeader.TsmpAuthorization.userName與TSMP_USER.UPDATE_TIME=現在時間。
		updateTsmpUserTable(auth, req, iip);

		List<String> authorityList = updateAuthoritiesTable(req, iip);	//如果有改userName就要刪除AUTHORITIES原本的那一筆資料

		// * 10.若""新密碼""不為空，更新回USERS資料表(條件USERS.USERNAME=AA0006Req.newUserName)，更新PASSWORD欄位=bcrypt(AA0006Req.newUserBlock)。"
		// 如果有改userName AUTHORITIES必須新增一筆資料
		updateUsersTable(req, password, authorityList, iip);
		
	}

	//對AUTHORITIES進行更新，以AUTHORITIES.USERNAME欄位 = AA0006Req.userName為條件，更新AUTHORITIES.USERNAME=AA0006Req.newUserName。
	private List<String> updateAuthoritiesTable(AA0006Req req, InnerInvokeParam iip) throws Exception {
		List<String> authorityList = new ArrayList<>();
		String userName = req.getUserName();
		String newUserName = req.getNewUserName();
		
		if(!userName.equals(newUserName)) {
			List<Authorities> authoritiesList = getAuthoritiesDao().findByUsername(userName);
			
			authoritiesList.forEach(auth -> {
				authorityList.add(auth.getAuthority());
				getAuthoritiesDao().delete(auth);
				
				//寫入 Audit Log D
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, auth); //舊資料統一轉成 String
				String lineNumber = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogD(iip, lineNumber,
						Authorities.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);// D
			});
			
		}
		return authorityList;
	}
	
	// 若""新密碼""不為空，更新回USERS資料表(條件USERS.USERNAME=AA0006Req.newUserName)，更新PASSWORD欄位=bcrypt(AA0006Req.newUserBlock)。"
	private void updateUsersTable(AA0006Req req, String password, List<String> authorityList, InnerInvokeParam iip) throws Exception {

		String userName = req.getUserName();
		String newUserName = req.getNewUserName();

		if (!userName.equals(newUserName) && !StringUtils.isEmpty(password)) {
			//改userName & password
			Users users = new Users();
			users.setUserName(newUserName);
			users.setPassword(password);

			users = getUsersDao().saveAndFlush(users);
			//寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber,
					Users.class.getSimpleName(), TableAct.C.value(), null, users);// C

			Optional<Users> user = getUsersDao().findById(userName);
			if (user.isPresent()) {
				getUsersDao().delete(user.get());
				//寫入 Audit Log D
				lineNumber = StackTraceUtil.getLineNumber();
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, user.get()); //舊資料統一轉成 String
				getDgrAuditLogService().createAuditLogD(iip, lineNumber,
						Users.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);// D
			}
			
			addAuthoritiesTable(users, authorityList, iip);

		} else if (!userName.equals(newUserName) && StringUtils.isEmpty(password)) {
			// 改名
			Optional<Users> user = getUsersDao().findById(userName);
			Users users = new Users();
			users.setUserName(newUserName);
			users.setPassword(user.map(Users::getPassword).orElse(null));

			users = getUsersDao().saveAndFlush(users);
			//寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber,
					Users.class.getSimpleName(), TableAct.C.value(), null, users);// C

			if (user.isPresent()) {
				getUsersDao().delete(user.get());
				//寫入 Audit Log D
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, user.get()); //舊資料統一轉成 String
				lineNumber = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogD(iip, lineNumber,
						Users.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);// D
			}
			
			addAuthoritiesTable(users, authorityList, iip);
		} else if (userName.equals(newUserName) && !StringUtils.isEmpty(password)) {
			// 改密碼
			Optional<Users> user = getUsersDao().findById(userName);
			if (user.isPresent()) {
				Users users = user.get();
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, users); //舊資料統一轉成 String
				
				users.setPassword(password);
				users = getUsersDao().saveAndFlush(users);
				//寫入 Audit Log D
				String lineNumber = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
						Users.class.getSimpleName(), TableAct.U.value(), oldRowStr, users);// U
			}
		}
	}
	
	//寫入AUTHORITIES資料表，欄位對應USERNAME=userName、AUTHORITY=每一筆roleID。
	private void addAuthoritiesTable(Users users, List<String> authorityList, InnerInvokeParam iip) throws Exception {
		authorityList.forEach(authority -> {
			Authorities authorities = new Authorities();
			authorities.setUsername(users.getUserName());
			authorities.setAuthority(authority);
			authorities = getAuthoritiesDao().saveAndFlush(authorities);
			//寫入 Audit Log D
			String lineNumber2 = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber2,
					Authorities.class.getSimpleName(), TableAct.C.value(), null, authorities);// C
		});
	}
	
	
	//將AA0006Req的newXXX欄位更新到TSMP_USER，記得更新TSMP_USER.UPDATE_USER=TsmpHttpHeader.TsmpAuthorization.userName與TSMP_USER.UPDATE_TIME=現在時間。
	private void updateTsmpUserTable(TsmpAuthorization auth, AA0006Req req, InnerInvokeParam iip) throws Exception {
		boolean flag = false;
		String newUserAlias =ServiceUtil.nvl( req.getNewUserAlias());
		String newUserMail = ServiceUtil.nvl(req.getNewUserMail());
		String newUserName = ServiceUtil.nvl(req.getNewUserName());
		String userName = ServiceUtil.nvl(req.getUserName());
		String userMail = ServiceUtil.nvl(req.getUserMail());
		String userAlias = ServiceUtil.nvl(req.getUserAlias());
		
		TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userName);
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpUser); //舊資料統一轉成 String
		
		if(!newUserAlias.equals(userAlias)) {
			tsmpUser.setUserAlias(newUserAlias);
			flag = true;
		}
			
		if(!newUserMail.equals(userMail)) {
			tsmpUser.setUserEmail(newUserMail);
			flag = true;
		}
			
		if(!newUserName.equals(userName)) {
			tsmpUser.setUserName(newUserName);
			flag = true;
		}
		
		if(flag) {
			tsmpUser.setUpdateUser(auth.getUserName());
			tsmpUser.setUpdateTime(DateTimeUtil.now());
			
			tsmpUser = getTsmpUserDao().saveAndFlush(tsmpUser);
			//寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
					TsmpUser.class.getSimpleName(), TableAct.U.value(), oldRowStr, tsmpUser);// U
		}
	}
	
	private String getEncodePassword(String password) throws Exception {
		String decodePassword = null;
		if(!StringUtils.isEmpty(password))
			decodePassword = OAuthUtil.bCryptEncode(password);
		return decodePassword;
		
	}
	
	protected String base64Decode(String userBlock) throws Exception {
		return new String(ServiceUtil.base64Decode(userBlock));
			
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected UsersDao getUsersDao() {
		return this.usersDao;
	}
	
	protected AuthoritiesDao getAuthoritiesDao() {
		return this.authoritiesDao;
	}
	
	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}
	
	protected TsmpRoleDao getTsmpRoleDao() {
		return tsmpRoleDao;
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}

}
