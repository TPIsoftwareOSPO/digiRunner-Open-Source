package tpi.dgrv4.dpaa.service;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0016Req;
import tpi.dgrv4.dpaa.vo.AA0016Resp;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpRoleRoleMapping;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpRoleRoleMappingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0016Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpRoleDao tsmpRoleDao;
	private TsmpRoleRoleMappingDao tsmpRoleRoleMappingDao;
	
	@Autowired
    public AA0016Service(TsmpRoleDao tsmpRoleDao, TsmpRoleRoleMappingDao tsmpRoleRoleMappingDao) {
		super();
		this.tsmpRoleDao = tsmpRoleDao;
		this.tsmpRoleRoleMappingDao = tsmpRoleRoleMappingDao;
	}

	@Transactional
	public AA0016Resp addTRoleRoleMap(TsmpAuthorization authorization, AA0016Req req) {
		AA0016Resp resp = new AA0016Resp();

		try {
			checkParam(req);
			
			//4.新增資料到TSMP_ROLE_ROLE_MAPPING資料表，欄位對應ROLE_NAME=AA0016Req.roleName、ROLE_NAME_MAPPING=AA0016Req.roleNameMapping每一筆資料。
			String roleName = req.getRoleName();
			List<String> mappingList = req.getRoleNameMapping();
			
			mappingList.forEach(x ->{
				TsmpRoleRoleMapping roleMappingVo = new TsmpRoleRoleMapping();
				roleMappingVo.setRoleName(roleName);
				roleMappingVo.setRoleNameMapping(x);
				getTsmpRoleRoleMappingDao().save(roleMappingVo);
			});
			
			resp.setRoleName(roleName);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		

		return resp;
	}
	
	private void checkParam(AA0016Req req) {
		String roleName = req.getRoleName();
		List<String> mappingList = req.getRoleNameMapping();
		
		if(StringUtils.isEmpty(roleName)) {
			throw TsmpDpAaRtnCode._1267.throwing();
		}
		
		if(roleName.length() > 50) {
			throw TsmpDpAaRtnCode._1265.throwing("50", String.valueOf(roleName.length()));
		}
		
		if(mappingList == null || mappingList.size() == 0) {
			throw TsmpDpAaRtnCode._1266.throwing();
		}
		
		mappingList.forEach(x->{
			if(x.length() > 50) {
				throw TsmpDpAaRtnCode._1263.throwing("50", x, String.valueOf(x.length()));
			}
		});
		
		//1.檢查AA0016Req.roleName是否存在，查詢TSMP_ROLE資料表，若不存在則throw RTN CODE 1264。
		TsmpRole tsmpRole = getTsmpRoleDao().findFirstByRoleName(roleName);
		if(tsmpRole == null) {
			throw TsmpDpAaRtnCode._1264.throwing();
		}
		
		//2.檢查AA0016Req.roleName是否存在，查詢TSMP_ROLE_ROLE_MAPPING資料表，若存在則throw RTN CODE 1268。
		List<TsmpRoleRoleMapping> checkMappingList = getTsmpRoleRoleMappingDao().findByRoleName(roleName);
		if(checkMappingList != null && checkMappingList.size() > 0) {
			throw TsmpDpAaRtnCode._1268.throwing();
		}
		
		//3.檢查AA0016Req.roleNameMapping是否存在，查詢TSMP_ROLE資料表，若不存在則throw RTN CODE 1262。
		mappingList.forEach(x->{
			TsmpRole checkTsmpRole = getTsmpRoleDao().findFirstByRoleName(x);
			if(checkTsmpRole == null) {
				throw TsmpDpAaRtnCode._1262.throwing(x);
			}
			
		});
		
	}

	protected TsmpRoleDao getTsmpRoleDao() {
		return tsmpRoleDao;
	}

	protected TsmpRoleRoleMappingDao getTsmpRoleRoleMappingDao() {
		return tsmpRoleRoleMappingDao;
	}

}
