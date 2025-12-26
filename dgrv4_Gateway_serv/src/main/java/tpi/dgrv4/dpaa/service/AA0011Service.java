package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.isValueTooLargeException;

import java.util.List;

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
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0011Req;
import tpi.dgrv4.dpaa.vo.AA0011Resp;
import tpi.dgrv4.entity.constant.TsmpSequenceName;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.TsmpFunc;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpRoleFunc;
import tpi.dgrv4.entity.entity.TsmpRoleRoleMapping;
import tpi.dgrv4.entity.repository.SeqStoreDao;
import tpi.dgrv4.entity.repository.TsmpFuncDao;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpRoleFuncDao;
import tpi.dgrv4.entity.repository.TsmpRoleRoleMappingDao;
import tpi.dgrv4.entity.repository.TsmpSequenceDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0011Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpFuncDao tsmpFuncDao;
	private TsmpRoleDao tsmpRoleDao;
	private TsmpRoleFuncDao tsmpRoleFuncDao;
	private TsmpRoleRoleMappingDao tsmpRoleRoleMappingDao;
	private SeqStoreService seqStoreService;
	private TsmpSequenceDao tsmpSequenceDao;
	private SeqStoreDao seqStoreDao;
	private DgrAuditLogService dgrAuditLogService;
	
	@Autowired
	public AA0011Service(TsmpFuncDao tsmpFuncDao, TsmpRoleDao tsmpRoleDao, TsmpRoleFuncDao tsmpRoleFuncDao,
			TsmpRoleRoleMappingDao tsmpRoleRoleMappingDao, SeqStoreService seqStoreService,
			TsmpSequenceDao tsmpSequenceDao, SeqStoreDao seqStoreDao, DgrAuditLogService dgrAuditLogService) {
		super();
		this.tsmpFuncDao = tsmpFuncDao;
		this.tsmpRoleDao = tsmpRoleDao;
		this.tsmpRoleFuncDao = tsmpRoleFuncDao;
		this.tsmpRoleRoleMappingDao = tsmpRoleRoleMappingDao;
		this.seqStoreService = seqStoreService;
		this.tsmpSequenceDao = tsmpSequenceDao;
		this.seqStoreDao = seqStoreDao;
		this.dgrAuditLogService = dgrAuditLogService;
	}

	@Transactional
	public AA0011Resp addTRole (TsmpAuthorization auth, AA0011Req req, InnerInvokeParam iip) {
		
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.ADD_ROLE.value());
 
		AA0011Resp resp = new AA0011Resp();
		
		try {
			checkParams(req);
			
			String roleId = updateTables(auth, req, iip);	// update tables
			
			resp.setRoleId(roleId);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			if (isValueTooLargeException(e)) {
				throw TsmpDpAaRtnCode._1220.throwing();
			} else {
				throw TsmpDpAaRtnCode._1288.throwing();
			}
				
		}
		return resp;
	}
	
	/**
	 * 檢查AA0011Req.roleAlias是否存在，在TSMP_ROLE資料表(ROLE_ALIAS欄位)查詢，若存在則throw RTN CODE 1239。
	 * 檢查AA0011Req.roleName是否存在，在TSMP_ROLE資料表(ROLE_NAME欄位)查詢，若存在則throw RTN CODE 1240。
	 * 檢查AA0011Req.roleAlias是否存在，在TSMP_ROLE資料表(ROLE_NAME欄位)查詢，若存在則throw RTN CODE 1240。
	 * 檢查AA0011Req.funcCodeList，在TSMP_FUNC資料表(FUNC_CODE欄位)查詢，
	 * 檢查AA0011Req.funcCodeList是否有資料，若沒資料存在則throw RTN CODE 1309。
	 * 條件FUNC_CODE = 每一筆AA0011Req.funcCodeList，若不存在則throw RTN CODE 1240。
	 * 
	 * @param tsmpAuthorization
	 * @param req
	 * @throws Exception 
	 */
	protected void checkParams(AA0011Req req) throws Exception {
		String roleAlias = ServiceUtil.nvl(req.getRoleAlias());		//畫面「角色名稱」欄位
		String roleName = ServiceUtil.nvl(req.getRoleName());		//畫面「角色代號」欄位
		List<String> funcCodeList = req.getFuncCodeList();

		//roleAlias
		// 1302:角色名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字, 
		// 1240:角色名稱重複
		checkRoleAlias(roleAlias);
		
		// 1239:角色代號重複
		boolean isRoleNameDuplicated = isRoleDuplicated(roleAlias) || isRoleDuplicated(roleName);
		if(isRoleNameDuplicated) 
			throw TsmpDpAaRtnCode._1239.throwing();
		
		//roleName
		// 1301:角色代號:只能輸入英文字母(a~z,A~Z)及數字且不含空白, 
		// 1300:角色代號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字,
		checkRoleName(roleName);
		
		// 1309:功能清單:必填參數
		checkFuncCode(funcCodeList);
		
		// 1241:功能不存在 (含locale)
		boolean isFuncCodeExisted = isFuncCodeExisted(funcCodeList);
		if(!isFuncCodeExisted) 
			throw TsmpDpAaRtnCode._1241.throwing();
		
	}
	
	
	//	1302:角色名稱:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字, 
	//	1240:角色名稱重複
	private void checkRoleAlias(String roleAlias) {
		
		// 1352:[{{0}}] 格式不正確
		if (!StringUtils.hasText(roleAlias)) {
			throw TsmpDpAaRtnCode._1352.throwing("{{roleAlias}}");
		}
		
		String msg = "";
		if(roleAlias.length() > 255) {
			int length = roleAlias.length();
			msg = String.valueOf(length);
			throw TsmpDpAaRtnCode._1302.throwing("255",msg);
		}
		
		if(isRoleAliasDuplicated(roleAlias)) 
			throw TsmpDpAaRtnCode._1240.throwing();
	}
	
	
	// 1301:角色代號:只能輸入英文字母(a~z,A~Z)及數字且不含空白, 
	// 1300:角色代號:長度限制 [{{0}}] 字內，您輸入[{{1}}] 個字,
	private void checkRoleName(String roleName) {
		String msg = "";
		
		if(roleName.length() > 30) {
			int length = roleName.length();
			msg = String.valueOf(length);
			throw TsmpDpAaRtnCode._1300.throwing("30",msg);
		}
		
		if (!ServiceUtil.isNumericOrAlphabetic(roleName))
			throw TsmpDpAaRtnCode._1301.throwing();
	}
	
	/**
	 * 檢查是否存在在TSMP_ROLE資料表(ROLE_ALIAS欄位)查詢
	 * 
	 * 
	 * @param role
	 * @return
	 */
	private boolean isRoleDuplicated(String role) {
		boolean isRoleAliasDuplicated = false;
		List<TsmpRole> list = getTsmpRoleDao().findByRoleName(role);
		
		if(list != null && list.size() > 0)
			isRoleAliasDuplicated = true;
		
		return isRoleAliasDuplicated;
	}
	
	/**
	 * 檢查AA0011Req.roleName是否存在，在TSMP_ROLE資料表(ROLE_NAME欄位)查詢
	 * 檢查AA0011Req.roleAlias是否存在，在TSMP_ROLE資料表(ROLE_NAME欄位)查詢
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
	 * 檢查AA0011Req.funcCodeList是否有資料，若沒資料存在則throw RTN CODE 1309。
	 * 
	 * @param funcCodeList
	 * @return
	 */
	private void checkFuncCode(List<String> funcCodeList) {
		if(funcCodeList == null || funcCodeList.size() == 0)
			throw TsmpDpAaRtnCode._1309.throwing();
		
	}
	
	/**
	 * 檢查AA0011Req.funcCodeList，在TSMP_FUNC資料表(FUNC_CODE欄位)查詢
	 * FUNC_CODE = 每一筆AA0011Req.funcCodeList
	 * @param funcCodeList
	 * @return
	 */
	private boolean isFuncCodeExisted(List<String> funcCodeList) {
		boolean isFuncCodeExisted = true ;
		if(funcCodeList == null || funcCodeList.size() == 0)
			return false;
		
		//檢查傳入的funcCodeList是否都存在在 TSMP_FUNC(FUNC_CODE)
		
		for (String funcCode : funcCodeList) {
			List<TsmpFunc> list = getTsmpFuncDao().findByFuncCode(funcCode);
			if(list == null ||list.size() ==0) {
				isFuncCodeExisted = false;
				break;
			}
		}
		
		return isFuncCodeExisted;
	}
		
	private String updateTables(TsmpAuthorization auth, AA0011Req req, InnerInvokeParam iip) throws Exception {
		
		String  roleId = addTsmpRoleTable(auth, req, iip);	//新增TSMP_ROLE
		
		addTsmpRoleFuncTable(roleId, req, iip);				//新增TSMP_ROLE_FUNC
				
		addTsmpRoleMappingTable(req, iip);					//新增TSMP_ROLE_ROLE_MAPPING
		
		return roleId;
		
	}

	private String addTsmpRoleTable(TsmpAuthorization auth, AA0011Req req, InnerInvokeParam iip) throws Exception {
		//以SEQ_TSMP_ROLE_PK來取得roleId sequence。新增TSMP_ROLE資料，以AA0011Req為資料來源，
		//需要新增注意的欄位TSMP_ROLE.ROLE_ID=roleId sequence、TSMP_ROLE.CREATE_USER=登入者userName、TSMP_ROLE.CREATE_TIME=現在時間。
		String userName = auth.getUserName();
		String roleAlias = req.getRoleAlias();
		String roleName = req.getRoleName();
		String roleId = getRoleId();
		
		TsmpRole tsmpRole = new TsmpRole();
		tsmpRole.setCreateUser(userName);
		tsmpRole.setRoleAlias(roleAlias);
		tsmpRole.setRoleId(roleId);
		tsmpRole.setRoleName(roleName);
		tsmpRole.setCreateTime(DateTimeUtil.now());
		
		tsmpRole = getTsmpRoleDao().saveAndFlush(tsmpRole);
		
		//寫入 Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber,
				TsmpRole.class.getSimpleName(), TableAct.C.value(), null, tsmpRole);// C
		
		return roleId;
	}
	
	private void addTsmpRoleFuncTable(String roleId, AA0011Req req, InnerInvokeParam iip) throws Exception {
		//新增到TSMP_ROLE_FUNC資料表，欄位對應,TSMP_ROLE_FUNC.ROLE_ID=roleId sequence、
		//TSMP_ROLE_FUNC.FUNC_CODE=AA0011Req.funcCodeList每一筆.toUpperCase()。
		List<String> funcCodeList = req.getFuncCodeList();
		
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
	
	private void addTsmpRoleMappingTable(AA0011Req req, InnerInvokeParam iip) throws Exception {
		//新增到TSMP_ROLE_ROLE_MAPPING資料表，欄位對應TSMP_ROLE_ROLE_MAPPING.ROLE_NAME=""ADMIN""、
		//TSMP_ROLE_ROLE_MAPPING.ROLE_NAME_MAPPING=AA0011Req.roleName。
		
		TsmpRoleRoleMapping roleMapping = new TsmpRoleRoleMapping();
		roleMapping.setRoleName("ADMIN");		//固定給ADMIN
		roleMapping.setRoleNameMapping(req.getRoleName());
		
		roleMapping = getTsmpRoleRoleMappingDao().saveAndFlush(roleMapping);
		
		//寫入 Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber,
				TsmpRoleRoleMapping.class.getSimpleName(), TableAct.C.value(), null, roleMapping);// C
	}
	
	protected String getRoleId() throws Exception {
		String roleId = "";
		
		Long roleIdLong = getSeqStoreService().nextTsmpSequence(TsmpSequenceName.SEQ_TSMP_ROLE_PK);
		if (roleIdLong != null) {
			roleId = roleIdLong.toString();
		}
		
		if(StringUtils.isEmpty(roleId)) {
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		return roleId;
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

	protected TsmpRoleRoleMappingDao getTsmpRoleRoleMappingDao() {
		return tsmpRoleRoleMappingDao;
	}
	
	protected SeqStoreService getSeqStoreService() {
		return this.seqStoreService;
	}

	protected TsmpSequenceDao getTsmpSequenceDao() {
		return this.tsmpSequenceDao;
	}
	
	protected SeqStoreDao getSeqStoreDao() {
		return this.seqStoreDao;
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
}
