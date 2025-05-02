package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.TsmpVgroup;

public class TsmpVgroupDaoImpl extends BaseDao {
	// add custom methods here
	/**
	 * 1.將TSMP_VGROUP、TSMP_VGROUP_GROUP與TSMP_GROUP_API與TSMP_API進行查詢並ORDER BY TSMP_VGROUP.VGROUP_NAME, 
	 * TSMP_VGROUP.VGROUP_ID，條件 TSMP_VGROUP.SECURITY_LEVEL_ID =AA0229Req.securityLevelID 
	 * + TSMP_VGROUP.VGROUP_ID NOT IN (select VGROUP_ID TSMP_CLIENT_VGROUP.CLIENT_ID = AA0229Req.clientID) 
	 * +分頁 + 關鍵字搜尋，資料不可以重複
	 * */
	public List<TsmpVgroup> findByAA0229Service(TsmpVgroup lastTsmpVgroup, String securityLevelId, String clientId, String[] words, Integer pageSize){
		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT v ");
		sb.append(" FROM TsmpVgroup v ");
		sb.append(" WHERE 1 = 1 ");
		

		// 分頁
		if (lastTsmpVgroup != null) {
			sb.append(" AND ");
			sb.append(" ( ");
			sb.append("    1 = 2 ");
			sb.append("    OR v.vgroupName > :lastVgroupName ");
			sb.append("    OR (v.vgroupName = :lastVgroupName AND v.vgroupId > :lastVgroupId) ");
			sb.append(" ) ");
			
			params.put("lastVgroupName", lastTsmpVgroup.getVgroupName());
			params.put("lastVgroupId", lastTsmpVgroup.getVgroupId());
			
		}
		
		//securityLevelId條件
		if (StringUtils.hasText(securityLevelId)) {
			sb.append(" AND v.securityLevelId = :securityLevelId ");
			params.put("securityLevelId", securityLevelId);
		}
		
		//不包含tsmp_client_vgroup的資料
		sb.append(" AND not exists (select cv.vgroupId from TsmpClientVgroup cv where 1 = 1 ");
		if(StringUtils.hasText(clientId)) {
			sb.append(" AND cv.clientId = :clientId ");
			params.put("clientId", clientId);
		}
		sb.append(" AND v.vgroupId = cv.vgroupId ) ");
		
		//TsmpVgroup的關鍵字
		if (words != null && words.length > 0) {
			sb.append(" AND (  1 = 2 ");
			for (int i = 0; i < words.length; i++) {
				sb.append(" OR UPPER(v.vgroupName) like :keyworkSearch"+i);
				sb.append(" OR UPPER(v.vgroupAlias) like :keyworkSearch"+i);
				sb.append(" OR UPPER(v.vgroupDesc) like :keyworkSearch"+i);
			}
			//tsmpAPi的關鍵字
			sb.append(" OR exists(select 1 from TsmpVgroupGroup vg where v.vgroupId = vg.vgroupId ");
			sb.append(" AND exists(select 1 from TsmpGroupApi ga where vg.groupId = ga.groupId  ");
			sb.append(" AND exists(select 1 from TsmpApi a where ga.apiKey = a.apiKey and ga.moduleName = a.moduleName ");
			sb.append(" AND ( 1 = 2  ");
			for (int i = 0; i < words.length; i++) {
				sb.append(" OR UPPER(a.apiKey) like :keyworkSearch"+i);
				sb.append(" OR UPPER(a.moduleName) like :keyworkSearch"+i);
				sb.append(" OR UPPER(a.apiName) like :keyworkSearch"+i);
				params.put("keyworkSearch"+i,"%" + words[i].toUpperCase()+ "%" );
			}
			sb.append(" ) ");
			sb.append(" ))) ");
			sb.append(" ) ");
		}
		
		sb.append(" ORDER BY v.vgroupName ASC, v.vgroupId ASC ");

		return doQuery(sb.toString(), params, TsmpVgroup.class, pageSize);
	}
	
	
	public List<TsmpVgroup> query_aa0222Service(TsmpVgroup lastTsmpVgroup, String securityLevelId, List<String> vgroupAuthoritiesIds, String[] words, Integer pageSize){
		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT V ");
		sb.append(" FROM TsmpVgroup V ");
		sb.append(" WHERE 1 = 1 ");
		
		// 分頁
		if (lastTsmpVgroup != null) {
			sb.append(" AND ");
			sb.append(" ( ");
			sb.append("    1 = 2 ");
			sb.append("    OR V.vgroupName > :lastVgroupName ");
			sb.append("    OR (V.vgroupName = :lastVgroupName AND V.vgroupId > :lastVgroupId) ");
			sb.append(" ) ");
			
			params.put("lastVgroupName", lastTsmpVgroup.getVgroupName());
			params.put("lastVgroupId", lastTsmpVgroup.getVgroupId());
			
		}
		
		//securityLevelId條件
		if (!StringUtils.isEmpty(securityLevelId)) {
			sb.append(" AND V.securityLevelId = :securityLevelId ");
			params.put("securityLevelId", securityLevelId);
		}
		
		//vgroupAuthoritiesIds條件
		if (vgroupAuthoritiesIds != null && !vgroupAuthoritiesIds.isEmpty()) {
			sb.append(" AND EXISTS( ");
			sb.append("     select 1");
			sb.append("     from TsmpVgroupAuthoritiesMap A");
			sb.append("     where 1 = 1");
			sb.append("     and A.vgroupId =  V.vgroupId");
			sb.append("     and A.vgroupAuthoritieId in  (:vgroupAuthoritiesIds)");
			
			sb.append(" ) ");
			
			params.put("vgroupAuthoritiesIds", vgroupAuthoritiesIds);
		}
		
		//TsmpVgroup的關鍵字
		if (words != null && words.length > 0) {
			sb.append(" AND ( 1 = 2 ");
			for (int i = 0; i < words.length; i++) {
				sb.append(" OR UPPER(V.vgroupName) like :keyworkSearch"+i);
				sb.append(" OR UPPER(V.vgroupAlias) like :keyworkSearch"+i);
				
				params.put("keyworkSearch"+i,"%" + words[i].toUpperCase()+ "%" );
			}
			
			sb.append(" ) ");
		}
		
		sb.append(" ORDER BY V.vgroupName ASC, V.vgroupId ASC ");
		
		return doQuery(sb.toString(), params, TsmpVgroup.class, pageSize);
	}
	
}
