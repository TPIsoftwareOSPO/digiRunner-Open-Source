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
import tpi.dgrv4.dpaa.vo.AA0014Req;
import tpi.dgrv4.dpaa.vo.AA0014Resp;
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpRoleFunc;
import tpi.dgrv4.entity.entity.TsmpRoleRoleMapping;
import tpi.dgrv4.entity.entity.TsmpRoleTxidMap;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.TsmpFuncDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpRoleFuncDao;
import tpi.dgrv4.entity.repository.TsmpRoleRoleMappingDao;
import tpi.dgrv4.entity.repository.TsmpRoleTxidMapDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0014Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpFuncDao tsmpFuncDao;
	private TsmpRoleDao tsmpRoleDao;
	private TsmpRoleFuncDao tsmpRoleFuncDao;
	private AuthoritiesDao authoritiesDao;
	private TsmpRoleRoleMappingDao tsmpRoleRoleMappingDao;
	private DgrAuditLogService dgrAuditLogService;
	private  TsmpRoleTxidMapDao tsmpRoleTxidMapDao;
	private TsmpSettingService tsmpSettingService;
	
	@Autowired
	public AA0014Service(TsmpFuncDao tsmpFuncDao, TsmpRoleDao tsmpRoleDao, TsmpRoleFuncDao tsmpRoleFuncDao,
			AuthoritiesDao authoritiesDao, TsmpRoleRoleMappingDao tsmpRoleRoleMappingDao,
			DgrAuditLogService dgrAuditLogService, TsmpRoleTxidMapDao tsmpRoleTxidMapDao,
			TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpFuncDao = tsmpFuncDao;
		this.tsmpRoleDao = tsmpRoleDao;
		this.tsmpRoleFuncDao = tsmpRoleFuncDao;
		this.authoritiesDao = authoritiesDao;
		this.tsmpRoleRoleMappingDao = tsmpRoleRoleMappingDao;
		this.dgrAuditLogService = dgrAuditLogService;
		this.tsmpRoleTxidMapDao = tsmpRoleTxidMapDao;
		this.tsmpSettingService = tsmpSettingService;
	}

	@Transactional
	public AA0014Resp deleteTRole (TsmpAuthorization auth, AA0014Req req, InnerInvokeParam iip) {
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.DELETE_ROLE.value());
		
		AA0014Resp resp = new AA0014Resp();
		
		try {
			checkParams(req);
			
			deleteTables(auth, req, iip);	// delete tables
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();
				
		}
		
		return resp;
	}
	
	/**
	 * 檢查邏輯
	 * 
	 * @param req
	 * @throws Exception
	 */
	protected void checkParams(AA0014Req req) throws Exception {
		String roleId = ServiceUtil.nvl(req.getRoleId());
		String roleName = ServiceUtil.nvl(req.getRoleName());
		
		boolean settingFlag = getTsmpSettingService().getVal_DEFAULT_DATA_CHANGE_ENABLED();
		if("1000".equals(roleId) && !settingFlag)
			throw TsmpDpAaRtnCode._1548.throwing();
		
		// 1230:角色不存在
		if(!isRoleExised(roleName, roleId)) 
			throw TsmpDpAaRtnCode._1230.throwing();
		
		// 1243:該角色有使用者
		if(isRoleUsedbyUser(roleId)) 
			throw TsmpDpAaRtnCode._1243.throwing();
		
	}
	
	
	/**
	 * 檢查AA0014Req.roleId與AA0014Req.roleName是否存在，在TSMP_ROLE資料表查詢，
	 * 條件TSMP_ROLE.ROLE_ID=AA0014Req.roleId與TSMP_ROLE.ROLE_NAME=AA0014Req.roleName，
	 * 若不存在則throw RTN CODE 1230:角色不存在。
	 * 
	 * @param role
	 * @return
	 */
	private boolean isRoleExised(String roleName, String roleId) {
		boolean isRoleAliasDuplicated = false;
		
		Optional<TsmpRole> opt_tsmpRole2 = getTsmpRoleDao().findById(roleId);
		if(opt_tsmpRole2.isPresent()) {
			if(roleName.equals(opt_tsmpRole2.get().getRoleName()))
				return true;
		}
		
		return isRoleAliasDuplicated;
	}
	
	/**
	 * 在AUTHORITIES資料表查詢，
	 * 條件AUTHORITIES.AUTHORITY = AA0014Req.roleId，若有存在則throw RTN CODE 1243:該角色有使用者。
	 * 
	 * @param roleId
	 * @return
	 */
	private boolean isRoleUsedbyUser(String roleId) {
		boolean isRoleUsedbyUser = false;
		
		List<Authorities> authoritiesList = getAuthoritiesDao().findByAuthority(roleId);
		if(authoritiesList != null && authoritiesList.size() > 0)
			isRoleUsedbyUser = true;
		
		return isRoleUsedbyUser;
	}
		
	private void deleteTables(TsmpAuthorization auth, AA0014Req req, InnerInvokeParam iip) throws Exception {
		
		deleteTsmpRoleFuncTable(auth, req, iip);		//刪除TSMP_ROLE_FUNC
		
		deleteTsmpRoleTable(auth, req, iip);			//刪除TSMP_ROLE
		
		deleteTsmpRoleRoleMappingTable(auth, req, iip);		//新增TSMP_ROLE_FUNC

		deleteTsmpRoleTxidMap(auth, req, iip);	 //刪除角色TXID對應
	}

	/**
	 * 刪除TSMP_ROLE_FUNC資料表，條件為TSMP_ROLE_FUNC.ROLE_ID=AA0014Req.roleId。
	 * 
	 * @param auth
	 * @param req
	 * @throws Exception
	 */
	private void deleteTsmpRoleFuncTable(TsmpAuthorization auth, AA0014Req req, InnerInvokeParam iip) throws Exception {
		String roleId = req.getRoleId();
		
		List<TsmpRoleFunc> tsmpRoleFuncList =  getTsmpRoleFuncDao().findByRoleId(roleId);
		getTsmpRoleFuncDao().deleteAll(tsmpRoleFuncList);
		
		//寫入 Audit Log D
		if(iip != null) {
			String lineNumber = StackTraceUtil.getLineNumber();
			for (TsmpRoleFunc tsmpRoleFunc : tsmpRoleFuncList) {
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpRoleFunc); //舊資料統一轉成 String
				getDgrAuditLogService().createAuditLogD(iip, lineNumber,
						TsmpRoleFunc.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);// D
			}
		}
	}
	
	/**
	 * 刪除TSMP_ROLE資料表，條件為TSMP_ROLE.ROLE_ID=AA0014Req.roleId。
	 * 
	 * @param auth
	 * @param req
	 * @throws Exception
	 */
	private void deleteTsmpRoleTable(TsmpAuthorization auth, AA0014Req req, InnerInvokeParam iip) throws Exception {
		String roleId = req.getRoleId();
		
		Optional<TsmpRole> optRole = getTsmpRoleDao().findById(roleId);
		if(optRole.isPresent()) {
			getTsmpRoleDao().delete(optRole.get());
			
			//寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, optRole.get()); //舊資料統一轉成 String
			getDgrAuditLogService().createAuditLogD(iip, lineNumber,
					TsmpRole.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);// D
		}
	}
	
	/**
	 * 刪除TSMP_ROLE_ROLE_MAPPING資料表，
	 * 條件為TSMP_ROLE_ROLE_MAPPING.ROLE_NAME_MAPPING=AA0014Req.roleName。
	 * 
	 * @param req
	 * @throws Exception
	 */
	private void deleteTsmpRoleRoleMappingTable(TsmpAuthorization auth, AA0014Req req, InnerInvokeParam iip) throws Exception {
		String roleName = ServiceUtil.nvl(req.getRoleName());
		
//		getTsmpRoleRoleMappingDao().deleteByRoleNameMapping(roleName);
		
		List<TsmpRoleRoleMapping> roleMappingList = getTsmpRoleRoleMappingDao().findByRoleNameMapping(roleName);
		getTsmpRoleRoleMappingDao().deleteAll(roleMappingList);
		
		//寫入 Audit Log D
		if(iip != null) {
			String lineNumber = StackTraceUtil.getLineNumber();
			for (TsmpRoleRoleMapping tsmpRoleRoleMapping : roleMappingList) {
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpRoleRoleMapping); //舊資料統一轉成 String
				getDgrAuditLogService().createAuditLogD(iip, lineNumber,
						TsmpRoleRoleMapping.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);// D
			}
		}
	}
	
	/**
	 * 刪除TSMP_ROLE_TXID_MAP資料表，
	 * 條件為TSMP_ROLE_TXID_MAP.ROLE_ID=AA0014Req.roleId。
	 * @param auth
	 * @param req
	 * @param iip
	 * @throws Exception
	 */
	private void deleteTsmpRoleTxidMap(TsmpAuthorization auth, AA0014Req req, InnerInvokeParam iip)throws Exception{
		String roleId = req.getRoleId();
		List<TsmpRoleTxidMap> roleTxidMapList = getRoleTxidMapDao().findByRoleId(roleId);
		getRoleTxidMapDao().deleteAll(roleTxidMapList);

		//寫入 Audit Log D
		if(iip != null) {
			String lineNumber = StackTraceUtil.getLineNumber();
			for (TsmpRoleTxidMap roleTxidMap : roleTxidMapList) {
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, roleTxidMap); //舊資料統一轉成 String
				getDgrAuditLogService().createAuditLogD(iip, lineNumber,
				TsmpRoleTxidMap.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);// D
			}
		}
	}
	protected TsmpRoleTxidMapDao getRoleTxidMapDao(){
		return tsmpRoleTxidMapDao;
	}

	protected TsmpRoleDao getTsmpRoleDao() {
		return tsmpRoleDao;
	}
	
	protected TsmpFuncDao getTsmpFuncDao() {
		return tsmpFuncDao;
	}
	
	protected TsmpRoleFuncDao getTsmpRoleFuncDao() {
		return tsmpRoleFuncDao;
	}
	
	protected AuthoritiesDao getAuthoritiesDao() {
		return authoritiesDao;
	}
	
	protected TsmpRoleRoleMappingDao getTsmpRoleRoleMappingDao() {
		return tsmpRoleRoleMappingDao;
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
}
