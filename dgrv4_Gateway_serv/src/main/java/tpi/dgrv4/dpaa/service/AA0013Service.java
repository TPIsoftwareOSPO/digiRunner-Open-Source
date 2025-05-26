package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.isValueTooLargeException;

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
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0013Req;
import tpi.dgrv4.dpaa.vo.AA0013Resp;
import tpi.dgrv4.entity.entity.TsmpFunc;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpRoleFunc;
import tpi.dgrv4.entity.repository.TsmpFuncDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpRoleFuncDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0013Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpFuncDao tsmpFuncDao;
	private TsmpRoleDao tsmpRoleDao;
	private TsmpRoleFuncDao tsmpRoleFuncDao;
	private DgrAuditLogService dgrAuditLogService;
	
	@Autowired
	public AA0013Service(TsmpFuncDao tsmpFuncDao, TsmpRoleDao tsmpRoleDao, TsmpRoleFuncDao tsmpRoleFuncDao,
			DgrAuditLogService dgrAuditLogService) {
		super();
		this.tsmpFuncDao = tsmpFuncDao;
		this.tsmpRoleDao = tsmpRoleDao;
		this.tsmpRoleFuncDao = tsmpRoleFuncDao;
		this.dgrAuditLogService = dgrAuditLogService;
	}

	@Transactional
	public AA0013Resp updateTRoleFunc (TsmpAuthorization auth, AA0013Req req, InnerInvokeParam iip) {
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_ROLE.value());
		
		AA0013Resp resp = new AA0013Resp();
		
		try {
			checkParams(req);
			
			updateTables(auth, req, iip);	// update tables
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			if (isValueTooLargeException(e)) {
				throw TsmpDpAaRtnCode._1220.throwing();
			} else {
				throw TsmpDpAaRtnCode._1286.throwing();
			}
			
			
				
		}
		
		return resp;
	}
	
	/**
	 * 檢查AA0013Req.roleId與AA0013Req.roleName有沒有存在，在TSMP_ROLE資料表，
	 * 查詢條件TSMP_ROLE.ROLE_ID=AA0013Req.roleId與TSMP_ROLE.ROLE_NAME=AA0013Req.roleName，若不存在則throw RTN CODE 1230。若存在將TSMP_ROLE.ROLE_ID取出，後須步驟會使用到。
	 * 
	 * 檢查AA0013Req.newFuncCodeList內的資料有沒有存在TSMP_FUNC資料表，查詢條件TSMP_FUNC.FUNC_CODE = AA0013Req.newFuncCodeList每一筆，若有一筆不存在則throw RTN CODE 1241。
	 * 
	 * @param tsmpAuthorization
	 * @param req
	 * @throws Exception 
	 */
	protected void checkParams(AA0013Req req) throws Exception {
		String roleId = ServiceUtil.nvl(req.getRoleId());
		String newRoleAlias = ServiceUtil.nvl(req.getNewRoleAlias());
		String roleName = ServiceUtil.nvl(req.getRoleName());
		List<String> newFuncCodeList = req.getNewFuncCodeList();
		
		// 1283:新角色名稱:必填參數(20230407:和建立相同,所以取消)
		// 1256:新角色名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
		checkRoleAlias(newRoleAlias);
		
		// 1282:新角色名稱已存在
		// 需先檢查新角色名稱 與 原角色名稱是否不同
		boolean isRoleAliasRename = isRoleAliasRename(roleId, newRoleAlias);
		if(isRoleAliasRename) {
			if(isRoleDuplicated(newRoleAlias) || isRoleAliasDuplicated(newRoleAlias))
				throw TsmpDpAaRtnCode._1282.throwing();
			
		}
		
		// 1230:角色不存在
		if(!isRoleExised(roleName, roleId)) 
			throw TsmpDpAaRtnCode._1230.throwing();
		
		// 1309:功能清單:必填參數(20230407:和建立相同)
		if(newFuncCodeList == null || newFuncCodeList.size() == 0)
			throw TsmpDpAaRtnCode._1309.throwing();
		
		//1241:功能不存在 (含locale)
		if(newFuncCodeList != null && !isFuncCodeExisted(newFuncCodeList)) 
			throw TsmpDpAaRtnCode._1241.throwing();
		
	}
	
	/**
	 * 1283:新角色名稱:必填參數
	 * 1256:新角色名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字
	 * 
	 * @param newRoleAlias
	 */
	private void checkRoleAlias(String newRoleAlias) {
		String msg = "";
		//if(StringUtils.isEmpty(newRoleAlias))
			//throw TsmpDpAaRtnCode._1283.throwing();
		
		// 1352:[{{0}}] 格式不正確
		if (!StringUtils.hasText(newRoleAlias)) {
			throw TsmpDpAaRtnCode._1352.throwing("{{newRoleAlias}}");
		}
			
		if(newRoleAlias.length() > 255) {
			int length = newRoleAlias.length();
			msg = String.valueOf(length);
			throw TsmpDpAaRtnCode._1256.throwing("255",msg);
		}
	}
	
	private boolean isRoleAliasRename(String roleId, String newRoleAlias) {
		boolean isRoleAliasRename = false;
		String originalAlias = "";
		Optional<TsmpRole> optRole = getTsmpRoleDao().findById(roleId);
		if(optRole.isPresent()) {
			originalAlias = optRole.get().getRoleAlias();

			if(!newRoleAlias.equals(originalAlias)) {
				isRoleAliasRename = true;
			}
		}
		
		
		return isRoleAliasRename;
	}
	
	/**
	 * 檢查AA0013Req.roleAlias是否存在，在TSMP_ROLE資料表(ROLE_ALIAS欄位)查詢
	 * 
	 * @param newRoleAlias
	 * @return
	 */
	private boolean isRoleDuplicated(String newRoleAlias) {
		boolean isRoleAliasDuplicated = false;
		List<TsmpRole> list = getTsmpRoleDao().findByRoleName(newRoleAlias);
		//newRoleAlias(角色名稱) 只能 與自己的角色代號重複
        if(list != null && list.size() > 1)
			isRoleAliasDuplicated = true;
		
		return isRoleAliasDuplicated;
	}
	
	/**
	 * 檢查AA0013Req.roleAlias是否存在，在TSMP_ROLE資料表(ROLE_NAME欄位)查詢
	 * 
	 * @param roleAlias
	 * @return
	 */
	private boolean isRoleAliasDuplicated(String roleAlias) {
		boolean isRoleAliasDuplicated = false;
		List<TsmpRole> list = getTsmpRoleDao().findByRoleAlias(roleAlias);
		
		if(list != null && list.size() > 0)
			isRoleAliasDuplicated = true;
		
		return isRoleAliasDuplicated;
	}
	
	/**
	 * 檢查AA0013Req.roleId與AA0013Req.roleName有沒有存在，在TSMP_ROLE資料表
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
	 * 檢查AA0013Req.newFuncCodeList內的資料有沒有存在TSMP_FUNC資料表，查詢條件TSMP_FUNC.FUNC_CODE = AA0013Req.newFuncCodeList每一筆
	 * @param newFuncCodeList
	 * @return
	 */
	private boolean isFuncCodeExisted(List<String> newFuncCodeList) {
		boolean isFuncCodeExisted = true ;
		// 更新時，允許不選功能清單 但新建時功能清單為必填
		if(newFuncCodeList == null || newFuncCodeList.size() == 0)
			return isFuncCodeExisted;
		
		//檢查傳入的funcCodeList是否都存在在 TSMP_FUNC(FUNC_CODE)
		
		for (String funcCode : newFuncCodeList) {
			List<TsmpFunc> list = getTsmpFuncDao().findByFuncCode(funcCode);
			if(list == null ||list.size() ==0) {
				isFuncCodeExisted = false;
				break;
			}
		}
		
		return isFuncCodeExisted;
	}
		
	private void updateTables(TsmpAuthorization auth, AA0013Req req, InnerInvokeParam iip) throws Exception {
		
		updateTsmpRoleTable(auth, req, iip);			//更新TSMP_ROLE
		
		modifyTsmpRoleFuncTable(auth, req, iip);		//新增TSMP_ROLE_FUNC
				
	}

	private void updateTsmpRoleTable(TsmpAuthorization auth, AA0013Req req, InnerInvokeParam iip) throws Exception {
//		更新TSMP_ROLE資料表，欄位對應TSMP_ROLE.ROLE_ALIAS=AA0013Req.roleId，條件TSMP_ROLE.ROLE_ID=AA0013Req.roleId與TSMP_ROLE.ROLE_NAME=AA0013Req.roleName。
		String userName = auth.getUserName();
		String newRoleAlias = req.getNewRoleAlias();
		String roleName = req.getRoleName();
		String roleId = req.getRoleId();
		
		Optional<TsmpRole> optRole = getTsmpRoleDao().findById(roleId);
		if(optRole.isPresent()) {
			if(optRole.get().getRoleName().equals(roleName)) {
				TsmpRole tsmpRole = optRole.get();
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpRole); //舊資料統一轉成 String
				
				tsmpRole.setRoleAlias(newRoleAlias);
				tsmpRole = getTsmpRoleDao().saveAndFlush(tsmpRole);
				
				//寫入 Audit Log D
				String lineNumber = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogD(iip, lineNumber,
						TsmpRole.class.getSimpleName(), TableAct.U.value(), oldRowStr, tsmpRole);// U
			}
		}
	}
	
	private void modifyTsmpRoleFuncTable(TsmpAuthorization auth, AA0013Req req, InnerInvokeParam iip) throws Exception {
		// 刪除TSMP_ROLE_FUNC資料表，條件為TSMP_ROLE_FUNC.ROLE_ID =TSMP_ROLE.ROLE_ID
		String roleId = ServiceUtil.nvl(req.getRoleId());
		List<TsmpRoleFunc> getFuncCodeList = getTsmpRoleFuncDao().findByRoleId(roleId);
		getTsmpRoleFuncDao().deleteAll(getFuncCodeList);
		//寫入 Audit Log D
		if(iip != null) {
			String lineNumber = StackTraceUtil.getLineNumber();
			for (TsmpRoleFunc tsmpRoleFunc : getFuncCodeList) {
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpRoleFunc); //舊資料統一轉成 String
				getDgrAuditLogService().createAuditLogD(iip, lineNumber,
						TsmpRoleFunc.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);// D
			}
		}
		
		// 新增TSMP_ROLE_FUNC資料表，欄位對應TSMP_ROLE_FUNC.ROLE_ID=TSMP_ROLE.ROLE_ID與TSMP_ROLE_FUNC.FUNC_CODE=AA0013Req.newFuncCodeList每一筆.toUpperCase()。
		List<String> funcCodeList = req.getNewFuncCodeList();
		
		funcCodeList.forEach((funcCode)->{
			TsmpRoleFunc tsmpRoleFunc = new TsmpRoleFunc();
			tsmpRoleFunc.setRoleId(roleId);
			tsmpRoleFunc.setFuncCode(funcCode.toUpperCase());
			
			tsmpRoleFunc = getTsmpRoleFuncDao().saveAndFlush(tsmpRoleFunc);
			
			//寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber,
					TsmpRoleFunc.class.getSimpleName(), TableAct.C.value(), null, tsmpRoleFunc);// C
		});
			
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
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
}
