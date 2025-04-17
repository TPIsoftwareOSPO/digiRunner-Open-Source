package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0021Req;
import tpi.dgrv4.dpaa.vo.AA0021Resp;
import tpi.dgrv4.dpaa.vo.AA0021RoleInfo;
import tpi.dgrv4.entity.entity.TsmpRole;
import tpi.dgrv4.entity.entity.TsmpRoleRoleMapping;
import tpi.dgrv4.entity.repository.TsmpRoleDao;
import tpi.dgrv4.entity.repository.TsmpRoleRoleMappingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0021Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpRoleDao tsmpRoleDao;
	private TsmpRoleRoleMappingDao tsmpRoleRoleMappingDao;

	@Autowired
	public AA0021Service(TsmpRoleDao tsmpRoleDao, TsmpRoleRoleMappingDao tsmpRoleRoleMappingDao) {
		super();
		this.tsmpRoleDao = tsmpRoleDao;
		this.tsmpRoleRoleMappingDao = tsmpRoleRoleMappingDao;
	}

	public AA0021Resp queryTRoleRoleMapDetail(TsmpAuthorization authorization, AA0021Req req) {
		AA0021Resp resp = new AA0021Resp();

		try {
			//1.檢查AA0021Req.roleName是否存在，在TSMP_ROLE資料表(ROLE_NAME欄位)查詢，若不存在則throw RTN CODE 1230。
			//  若存在TSMP_ROLE資料放進AA0021Resp.roleName與AA0021Resp.roleAlias。
			String roleName = req.getRoleName();
			
			if(StringUtils.isEmpty(roleName)) {
				throw TsmpDpAaRtnCode._1267.throwing();
			}
			
			TsmpRole tsmpRoleVo = getTsmpRoleDao().findFirstByRoleName(roleName);
			if(tsmpRoleVo == null) {
				throw TsmpDpAaRtnCode._1230.throwing();
			}
			
			resp.setRoleName(tsmpRoleVo.getRoleName());
			if(tsmpRoleVo.getRoleAlias() == null) {
				resp.setRoleAlias("unknown:"+ tsmpRoleVo.getRoleName());
			}else {
				resp.setRoleAlias(tsmpRoleVo.getRoleAlias());
			}
			
			//1.查詢TSMP_ROLE_ROLE_MAPPING資料表，條件TSMP_ROLE_ROLE_MAPPING.ROLE_NAME=AA0021Resp.roleName
			//查詢出TSMP_ROLE_ROLE_MAPPING.ROLE_NAME_MAPPING資料。
			List<TsmpRoleRoleMapping> roleRoleList = getTsmpRoleRoleMappingDao().findByRoleName(resp.getRoleName());
			
			//2.查詢TSMP_ROLE資料表，條件TSMP_ROLE.ROLE_NAME=步驟1的查詢結果TSMP_ROLE_ROLE_MAPPING.ROLE_NAME_MAPPING。
			//查詢到的TSMP_ROLE資料放進AA0021Resp.roleMappingInfo，欄位對應請看AA0021RoleInfo的REMARK
			List<AA0021RoleInfo> infoList = new ArrayList<>();
			roleRoleList.forEach(rrm ->{
				TsmpRole roleVo = getTsmpRoleDao().findFirstByRoleName(rrm.getRoleNameMapping());
				if(roleVo != null) {
					AA0021RoleInfo infoVo = new AA0021RoleInfo();
					infoVo.setRoleName(roleVo.getRoleName());
					infoVo.setRoleAlias(roleVo.getRoleAlias());
					if(roleVo.getRoleAlias() == null) {
						infoVo.setRoleAliasRoleName("unknown:"+ roleVo.getRoleName());
					}else {
						infoVo.setRoleAliasRoleName(roleVo.getRoleAlias());
					}
					infoList.add(infoVo);
				}
				
			});
			
			resp.setRoleMappingInfo(infoList);

		} catch (TsmpDpAaException aa0021_e) {
			throw aa0021_e;
		} catch (Exception aa0021_e) {
			this.logger.error(StackTraceUtil.logStackTrace(aa0021_e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	protected TsmpRoleRoleMappingDao getTsmpRoleRoleMappingDao() {
		return tsmpRoleRoleMappingDao;
	}
	
	protected TsmpRoleDao getTsmpRoleDao() {
		return tsmpRoleDao;
	}


}
