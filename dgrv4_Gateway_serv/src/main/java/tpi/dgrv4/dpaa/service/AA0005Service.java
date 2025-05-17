package tpi.dgrv4.dpaa.service;

import java.util.List;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0005Req;
import tpi.dgrv4.dpaa.vo.AA0005Resp;
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.Users;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.entity.repository.UsersDao;
import tpi.dgrv4.gateway.component.cache.proxy.AuthoritiesCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0005Service {

	private TPILogger logger = TPILogger.tl;
	
	private AuthoritiesDao authoritiesDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpRoleDao tsmpRoleDao;
	private UsersDao usersDao;
	private DgrAuditLogService dgrAuditLogService;
	private TsmpUserDao tsmpUserDao;
	private TsmpSettingService tsmpSettingService;
	private AuthoritiesCacheProxy authoritiesCacheProxy;
	
	@Autowired
	public AA0005Service(AuthoritiesDao authoritiesDao, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpRoleDao tsmpRoleDao, UsersDao usersDao, DgrAuditLogService dgrAuditLogService, TsmpUserDao tsmpUserDao,
			TsmpSettingService tsmpSettingService, AuthoritiesCacheProxy authoritiesCacheProxy) {
		super();
		this.authoritiesDao = authoritiesDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpRoleDao = tsmpRoleDao;
		this.usersDao = usersDao;
		this.dgrAuditLogService = dgrAuditLogService;
		this.tsmpUserDao = tsmpUserDao;
		this.tsmpSettingService = tsmpSettingService;
		this.authoritiesCacheProxy = authoritiesCacheProxy;
	}

	@Transactional
	public AA0005Resp deleteTUser(TsmpAuthorization auth, AA0005Req req, InnerInvokeParam iip) {
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.DELETE_USER.value());
		
		AA0005Resp resp = new AA0005Resp();
		TsmpUser tsmpUser = null;

		try {
			String userId = ServiceUtil.nvl(req.getUserID());
			String userName = ServiceUtil.nvl(req.getUserName());	
			String authName = auth.getUserName();
			checkPermissions(authName, userName, userId);
			
			tsmpUser = qureyTUserByName(userName);
			if(tsmpUser != null) {
				deleteTable(userName, iip);
			}
			
		}catch (TsmpDpAaException e){
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();
		}
		
		return resp;
	}
	
	public TsmpUser qureyTUserByName(String userName) throws Exception {
		TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userName);
		
		// 檢查userName是否存在，在TSMP_USER.USER_NAME欄位檢查，若不存在則throw RTN CODE 1231。
		if(tsmpUser == null) {
			throw TsmpDpAaRtnCode._1231.throwing();
		}
		
		return tsmpUser;
	}
	
	private void deleteTable(String userName, InnerInvokeParam iip) throws Exception{
		//刪除AUTHORITIES資料表，以AUTHORITIES.USERNAME=userName為條件。
		//刪除USERS資料表，以USERS.USERNAME=userName為條件。
		//刪除TSMP_USER資料表，以TSMP_USER.USER_ID=userID和TSMP_USER.USER_NAME=userName為條件。
		deleteAuthorities(userName, iip);
		deleteUser(userName, iip);
		deleteTsmpUser(userName, iip);
		
	}
	
	/**
	 * 刪除AUTHORITIES資料表，以AUTHORITIES.USERNAME=userName為條件
	 * 
	 */
	private void deleteAuthorities(String userName, InnerInvokeParam iip) throws Exception{
		List<Authorities> authoritiesList = getAuthoritiesDao().findByUsername(userName);
		getAuthoritiesDao().deleteAll(authoritiesList);
		
		//寫入 Audit Log D
		if(iip != null) {
			String lineNumber = StackTraceUtil.getLineNumber();
			for (Authorities authorities : authoritiesList) {
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, authorities); //舊資料統一轉成 String
				getDgrAuditLogService().createAuditLogD(iip, lineNumber,
						Authorities.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);// D
			}
		}
	}
	
	/**
	 * 刪除USERS資料表，以USERS.USERNAME=userName為條件
	 */
	private void deleteUser(String userName, InnerInvokeParam iip) throws Exception{
		Optional<Users> userOpt = getUsersDao().findById(userName);
		if(userOpt.isPresent()) {
			getUsersDao().delete(userOpt.get());
			
			//寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, userOpt.get()); //舊資料統一轉成 String
			getDgrAuditLogService().createAuditLogD(iip, lineNumber,
					Users.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);// D
		}
	}
	
	private void checkPermissions(String authName, String userName, String userId) {
		boolean changeDataFlag = getTsmpSettingService().getVal_DEFAULT_DATA_CHANGE_ENABLED();
		if(("1000000000".equals(userId) || "1200000000".equals(userId)) && !changeDataFlag)
			throw TsmpDpAaRtnCode._1548.throwing();
		
		boolean settingFlag = getTsmpSettingService().getVal_USER_UPDATE_BY_SELF();
		Authorities authorities = getAuthoritiesCacheProxy().findFirstByUserName(authName);
		String authority = authorities.getAuthority();
		boolean roleFlag = "1000".equals(authority);
		// ADMIN角色可修改自己 &&是否允許修正自身帳號(預設是可以) && 自己不可異動自己的資訊
		if (!roleFlag && settingFlag && authName.equals(userName)) {
			throw TsmpDpAaRtnCode._1219.throwing();
		}
	}
	
	/**
	 * 刪除TSMP_USER資料表，以TSMP_USER.USER_ID=userID和TSMP_USER.USER_NAME=userName為條件
	 */
	private void deleteTsmpUser(String userName, InnerInvokeParam iip) throws Exception{
		TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userName);
		if(tsmpUser != null) {
			getTsmpUserDao().delete(tsmpUser);
			
			//寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpUser); //舊資料統一轉成 String
			getDgrAuditLogService().createAuditLogD(iip, lineNumber,
					TsmpUser.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);// D
		}
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
	
	protected TsmpRoleDao getTsmpRoleDao() {
		return this.tsmpRoleDao;
	}
	
	protected AuthoritiesDao getAuthoritiesDao() {
		return this.authoritiesDao;
	}

	protected UsersDao getUsersDao() {
		return this.usersDao;
	}
	
	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
	protected AuthoritiesCacheProxy getAuthoritiesCacheProxy() {
		return authoritiesCacheProxy;
	}
}
