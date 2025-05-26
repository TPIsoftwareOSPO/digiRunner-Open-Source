package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.jpql.TsmpApiDetail;

public class TsmpApiDetailDaoImpl extends BaseDao {
	// add custom methods here

	public TsmpApiDetail queryFirstByApiModuleIdAndPathOfJsonLike(Long apiModuleId, String path) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		
		//select * from TSMP_API_DETAIL where api_module_id = 5144 and path_of_json like '%11/DPB0058"%'
		sb.append(" select A");
		sb.append(" from TsmpApiDetail A");
		sb.append(" where A.apiModuleId = :apiModuleId");
		sb.append(" and UPPER(A.pathOfJson) like :path");//忽略大小寫
		
		params.put("apiModuleId", apiModuleId);
		params.put("path", "%" + path.toUpperCase() + "%");//忽略大小寫
		
		List<TsmpApiDetail> list = doQuery(sb.toString(), params, TsmpApiDetail.class);
		if (list == null || list.isEmpty()) {
			return null;
		}
		
		return list.get(0);
	}
 
	public List<TsmpApiDetail> queryByAA0421Service(Long lastId, String[] keywords, Long apiModuleId, String moduleName, List<String> orgList, int pageSize){
		Map<String, Object> params = new HashMap<>();
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(" SELECT d ");
		sb.append(" FROM TsmpApiDetail d ");
		
		sb.append(" WHERE 1=1 ");
		
		// apiModuleId
		sb.append(" AND d.apiModuleId = :apiModuleId ");
		params.put("apiModuleId", apiModuleId);
		
		//分頁
		if (lastId != null) {
			sb.append(" AND ( 1 = 2");
			sb.append("    OR (d.id > :lastId)");
			sb.append(" )");			
			params.put("lastId", lastId);
		}
		
		//Join tsmp_api_module
		sb.append(" AND EXISTS ( ");
		sb.append("   SELECT 1 FROM TsmpApiModule m ");
		sb.append("   WHERE m.id = d.apiModuleId ");
		if(!StringUtils.isEmpty(moduleName)) {
			sb.append("   AND m.moduleName = :moduleName ");
			params.put("moduleName", moduleName);
		}
		sb.append("   AND (m.orgId IS NULL OR LENGTH(m.orgId) = 0");
		if (!CollectionUtils.isEmpty(orgList)) {
			sb.append(" OR m.orgId IN (:orgList)");
			params.put("orgList", orgList);
		}
		sb.append("   )");
		sb.append(" ) ");
		
		//關鍵字 
		if (keywords != null && keywords.length > 0) {

			sb.append(" AND ( ");
			sb.append("		1=2 ");
			for (int i = 0; i < keywords.length; i++) {
				sb.append(" 	OR UPPER(d.apiKey) LIKE :keyworkSearch" +i);
				sb.append(" 	OR UPPER(d.apiName) LIKE :keyworkSearch" +i);
				params.put("keyworkSearch"+i,"%" + keywords[i].toUpperCase()+ "%" );
			}
			sb.append(" 	) ");
		}
		sb.append(" ORDER BY d.id ");
		
		return doQuery(sb.toString(), params, TsmpApiDetail.class, pageSize);
	}

	public List<TsmpApiDetail> query_AA0303Service_01(String apiKey, String moduleName, List<String> orgIdList) {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT D");
		sb.append(" FROM TsmpApiDetail D");
		sb.append(" WHERE D.apiKey = :apiKey");
		sb.append(" AND EXISTS (");
		sb.append("     SELECT 1");
		sb.append("     FROM TsmpApiModule M");
		sb.append("     WHERE M.id = D.apiModuleId");
		sb.append("     AND M.moduleName = :moduleName");
		sb.append("     AND (M.orgId IS NULL OR LENGTH(M.orgId) = 0 OR M.orgId IN (:orgIdList))");
		sb.append(" )");
		// 檢查Module是否綁定
		sb.append("	AND EXISTS (");
		sb.append("		SELECT 1");
		sb.append("		FROM TsmpDcModule M1");
		sb.append("		WHERE M1.moduleId = D.apiModuleId");
		sb.append("	)");
		
		Map<String, Object> params = new HashMap<>();
		params.put("apiKey", apiKey);
		params.put("moduleName", moduleName);
		params.put("orgIdList", orgIdList);
		
		return doQuery(sb.toString(), params, TsmpApiDetail.class);
	}

}