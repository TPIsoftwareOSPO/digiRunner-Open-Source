package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.TsmpRoleFunc;

public class TsmpRoleFuncDaoImpl extends BaseDao {
	// add custom methods here
	
	public List<TsmpRoleFunc> queryByUserName(String userName){
		Map<String, Object> params = new HashMap<>();
	
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT f ");
		sb.append(" FROM TsmpRoleFunc f ");
		sb.append(" WHERE EXISTS ( ");
		sb.append("               SELECT 1 ");
		sb.append("               FROM Authorities a ");
		sb.append("               WHERE a.username = :userName ");
		sb.append("               AND a.authority = f.roleId ");
		sb.append("              ) ");
		
		params.put("userName", userName);
		
		
		return doQuery(sb.toString(), params, TsmpRoleFunc.class);
	}

}
