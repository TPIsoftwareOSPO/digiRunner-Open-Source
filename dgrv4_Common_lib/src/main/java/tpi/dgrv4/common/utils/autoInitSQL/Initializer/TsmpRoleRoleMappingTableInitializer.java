package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.AutoInitSQLTsmpRoleRoleMappingVo;
@Service
public class TsmpRoleRoleMappingTableInitializer {

    
    private  List<AutoInitSQLTsmpRoleRoleMappingVo> autoInitSQLTsmpRoleRoleMappinglist = new LinkedList<>();
    
	public List<AutoInitSQLTsmpRoleRoleMappingVo> insertTsmpRoleRoleMapping() {
		try {
			Long roleRoleId;
			String roleName;
			String roleNameMapping;
			createTsmpRoleRoleMapping((roleRoleId = 1L), (roleName = "ADMIN"), (roleNameMapping = "ADMIN"));

		} catch (Exception e) {
			StackTraceUtil.logStackTrace(e);
			throw e;
		}
		
		return autoInitSQLTsmpRoleRoleMappinglist;
	}
    
    protected void createTsmpRoleRoleMapping(Long roleRoleId, String roleName, String roleNameMapping) {
        	AutoInitSQLTsmpRoleRoleMappingVo autoInitSQLTsmpRoleRoleMapping = new AutoInitSQLTsmpRoleRoleMappingVo();
        	autoInitSQLTsmpRoleRoleMapping.setRoleRoleId(roleRoleId);
            autoInitSQLTsmpRoleRoleMapping.setRoleName(roleName);
            autoInitSQLTsmpRoleRoleMapping.setRoleNameMapping(roleNameMapping);
            autoInitSQLTsmpRoleRoleMappinglist.add(autoInitSQLTsmpRoleRoleMapping);

    }

}
