package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.TsmpClientGroup;

public class TsmpClientGroupDaoImpl extends BaseDao {
	// add custom methods here
	public List<TsmpClientGroup> queryGroupIdNotInTsmpGroupBySecurityLV(String clientId, String securityLV) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		
		sb.append("select C from TsmpClientGroup C ");
		sb.append("where C.clientId = :clientId ");
		sb.append("and not EXISTS (");
		sb.append("		SELECT 1 FROM TsmpGroup G ");
		sb.append("		where 1 = 1 ");
		sb.append("		and C.groupId = G.groupId ");
		sb.append("		and G.securityLevelId = :securityLV ");
		sb.append(")");
		
		params.put("clientId", clientId);
		params.put("securityLV", securityLV);
		
		List<TsmpClientGroup> tsmpClientGroup = doQuery(sb.toString(), params, TsmpClientGroup.class);
		
		return tsmpClientGroup;
	}
}
