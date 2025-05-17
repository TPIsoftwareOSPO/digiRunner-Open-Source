package tpi.dgrv4.dpaa.service;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0017Req;
import tpi.dgrv4.dpaa.vo.AA0017Resp;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpRoleRoleMappingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0017Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpRoleDao tsmpRoleDao;
	private TsmpRoleRoleMappingDao tsmpRoleRoleMappingDao;
	private TsmpSettingService tsmpSettingService;	
	
	@Autowired
	public AA0017Service(TsmpRoleDao tsmpRoleDao, TsmpRoleRoleMappingDao tsmpRoleRoleMappingDao,
			TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpRoleDao = tsmpRoleDao;
		this.tsmpRoleRoleMappingDao = tsmpRoleRoleMappingDao;
		this.tsmpSettingService = tsmpSettingService;
	}

	@Transactional
	public AA0017Resp deleteTRoleRoleMap(TsmpAuthorization authorization, AA0017Req req) {
		AA0017Resp resp = new AA0017Resp();

		try {
			checkParam(req);
			
			//2.刪除TSMP_ROLE_ROLE_MAPPING資料表，以 ROLE_NAME = AA0017Req.roleName。
			String roleName = req.getRoleName();
			getTsmpRoleRoleMappingDao().deleteByRoleName(roleName);
			
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();
		}
		
		
		return resp;
	}
	
	private void checkParam(AA0017Req req) {
		String roleName = req.getRoleName();
		
		boolean settingFlag = getTsmpSettingService().getVal_DEFAULT_DATA_CHANGE_ENABLED();
		if("ADMIN".equals(roleName) && !settingFlag)
			throw TsmpDpAaRtnCode._1548.throwing();
		
		//1.檢查AA0017Req.roleName是否存在，在TSMP_ROLE資料表(ROLE_NAME欄位)查詢，若不存在則throw RTN CODE 1230。
		TsmpRole tsmpRole = getTsmpRoleDao().findFirstByRoleName(roleName);
		if(tsmpRole == null) {
			throw TsmpDpAaRtnCode._1230.throwing();
		}	
		
	}

	protected TsmpRoleDao getTsmpRoleDao() {
		return tsmpRoleDao;
	}

	protected TsmpRoleRoleMappingDao getTsmpRoleRoleMappingDao() {
		return tsmpRoleRoleMappingDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
}
