package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.TsmpClientVgroup;

public class TsmpClientVgroupDaoImpl extends BaseDao {
	// add custom methods here
	public List<TsmpClientVgroup> queryGroupIdNotInTsmpVGroupBySecurityLV(String clientId, String securityLV) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		
		sb.append("select V from TsmpClientVgroup V ");
		sb.append("where V.clientId =:clientId ");
		sb.append("and not EXISTS ( ");
		sb.append("		select 1 from TsmpVgroup C ");
		sb.append("		where 1 = 1 ");
		sb.append("		and C.vgroupId = V.vgroupId  ");
		sb.append("		and C.securityLevelId = :securityLV ");
		sb.append(")");
		
		params.put("clientId", clientId);
		params.put("securityLV", securityLV);
		
		List<TsmpClientVgroup> tsmpClientVGroup = doQuery(sb.toString(), params, TsmpClientVgroup.class);
		
		return tsmpClientVGroup;
	}
}
