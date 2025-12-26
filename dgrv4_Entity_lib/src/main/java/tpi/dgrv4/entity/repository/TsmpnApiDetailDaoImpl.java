package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.jpql.TsmpnApiDetail;

public class TsmpnApiDetailDaoImpl extends BaseDao {
	// add custom methods here
	
	public List<TsmpnApiDetail> query_AA0303Service_01(String apiKey, String moduleName, List<String> orgIdList) {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT D");
		sb.append(" FROM TsmpnApiDetail D");
		sb.append(" WHERE D.apiKey = :apiKey");
		sb.append(" AND EXISTS (");
		sb.append("     SELECT 1");
		sb.append("     FROM TsmpnApiModule M");
		sb.append("     WHERE M.id = D.apiModuleId");
		sb.append("     AND M.moduleName = :moduleName");
		sb.append("     AND (M.orgId IS NULL OR LENGTH(M.orgId) = 0 OR M.orgId IN (:orgIdList))");
		sb.append(" )");
		
		Map<String, Object> params = new HashMap<>();
		params.put("apiKey", apiKey);
		params.put("moduleName", moduleName);
		params.put("orgIdList", orgIdList);
		
		return doQuery(sb.toString(), params, TsmpnApiDetail.class);
	}

}