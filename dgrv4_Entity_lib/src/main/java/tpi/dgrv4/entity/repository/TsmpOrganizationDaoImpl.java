package tpi.dgrv4.entity.repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.TsmpOrganization;

public class TsmpOrganizationDaoImpl extends BaseDao {
	// add custom methods here

	public List<String> queryOrgDescendingByOrgId_rtn_id(String orgId, Integer pageSize) {
		String select = "A.orgId";
		return queryOrgDescendingByOrgId(select, orgId, String.class, pageSize);
	}

	public List<TsmpOrganization> queryOrgDescendingByOrgId_rtn_entity(String orgId, Integer pageSize) {
		String select = "A";
		return queryOrgDescendingByOrgId(select, orgId, TsmpOrganization.class, pageSize);
	}

	private <T> List<T> queryOrgDescendingByOrgId(String selectClause, String orgId, Class<T> clazz, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append("select A from TsmpOrganization A where A.orgId = :orgId");
		params.put("orgId", orgId);
		List<TsmpOrganization> orgList = doQuery(sb.toString(), params, TsmpOrganization.class);
		if (orgList == null || orgList.size() != 1) {
			return Collections.emptyList();
		}
		sb.setLength(0);
		params.clear();

		String orgPath = orgList.get(0).getOrgPath();

		// 用 '%::id::%' 避免 id=100003 向下找到 id=1000030
		sb.append(" select " + selectClause + " from TsmpOrganization A");
		sb.append(" where ( 1 = 2");
		sb.append(" 	or A.orgId = :orgId");
		sb.append(" 	or A.parentId = :orgId");
		if (orgPath == null || orgPath.isEmpty()) {
			sb.append(" 	or A.orgPath like :orgIdLike_1");
			sb.append(" 	or A.orgPath like :orgIdLike_2");
			params.put("orgIdLike_1", orgId + "::%");
			params.put("orgIdLike_2", "%::" + orgId + "::%");
		} else {
			sb.append(" 	or A.orgPath like :orgPathLike");
			params.put("orgPathLike", orgPath + "::%");
		}
		sb.append(" )");
		sb.append(" order by A.orgPath, A.parentId, A.orgId");

		params.put("orgId", orgId);
		
		return doQuery(sb.toString(), params, clazz, pageSize);
	}
	
	public List<String> findByOrgName(String orgName, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append("select A.orgId from TsmpOrganization A where A.orgName = :orgName");
		sb.append(" order by A.orgPath, A.parentId, A.orgId");

		params.put("orgName", orgName);
		
		return doQuery(sb.toString(), params, String.class, pageSize);
	}

}