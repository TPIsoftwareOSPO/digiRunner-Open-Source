package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.TsmpGroup;

public class TsmpGroupDaoImpl extends BaseDao {
	// add custom methods here
	public List<TsmpGroup> query_aa0228Service(String groupName, String groupId, String securityLevelID,
			String clientID, String vGroupFlag, String[] words, Integer pageSize) {

		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select G");
		sb.append(" from TsmpGroup G");
		sb.append(" where 1 = 1 ");
		sb.append(" and G.securityLevelId = :securityLevelID ");
		sb.append(" and G.vgroupFlag =:vGroupFlag ");

		params.put("securityLevelID", securityLevelID);
		params.put("vGroupFlag", vGroupFlag);

		// 分頁
		if (!StringUtils.isEmpty(groupId)) {
			sb.append("	and ( 1 = 2");
			sb.append(" or (G.groupName > :groupName )");
			sb.append(" or (G.groupName > :groupName and G.groupId > :lastGroupId )");
			sb.append(" )");

			params.put("lastGroupId", groupId);
			params.put("groupName", groupName);
		}

		sb.append("	and not EXISTS (");
		sb.append("		select 1 ");
		sb.append("		from TsmpClientGroup CG ");
		sb.append("	  	where 1 = 1 ");
		sb.append("	  	and G.groupId =CG.groupId ");
		sb.append("	  	and CG.clientId = :clientID ");
		sb.append("	 ) ");
		params.put("clientID", clientID);

		// 關鍵字search
		if (words != null && words.length > 0) {
			sb.append(" 	and ( 1 = 2");
			for (int i = 0; i < words.length; i++) {
				sb.append("   	 or UPPER(G.groupName) like :word" + i);// 忽略大小寫
				sb.append("  	 or UPPER(G.groupAlias) like :word" + i);// 忽略大小寫
				sb.append("   	 or UPPER(G.groupDesc) like :word" + i);// 忽略大小寫
				params.put("word" + i, "%" + words[i].toUpperCase() + "%");// 忽略大小寫
			}

			sb.append("	  	 or EXISTS(");
			sb.append(" 			select 1 ");
			sb.append(" 			from  TsmpApi A ");
			sb.append(" 			where EXISTS ( ");
			sb.append(" 				select 1 ");
			sb.append(" 				from TsmpGroupApi tga ");
			sb.append(" 				where 1 = 1 ");
			sb.append(" 					and tga.groupId =G.groupId ");
			sb.append(" 					and A.apiKey = tga.apiKey  ");
			sb.append(" 					and A.moduleName =tga.moduleName  ");
			sb.append(" 					and (  ");
			sb.append(" 						1 = 2 ");
			for (int i = 0; i < words.length; i++) {
				sb.append(" 						or UPPER(A.apiKey) like :word" + i); // 忽略大小寫
				sb.append(" 						or  UPPER(A.moduleName) like :word" + i); // 忽略大小寫
				sb.append(" 						or  UPPER(A.apiName) like :word" + i); // 忽略大小寫
				params.put("word" + i, "%" + words[i].toUpperCase() + "%"); // 忽略大小寫
			}
			sb.append(" 					)  ");
			sb.append(" 				)  ");

			sb.append("		)");
			sb.append(" )");
		}

		sb.append(" ORDER BY G.groupName, G.groupId asc");

		return doQuery(sb.toString(), params, TsmpGroup.class, pageSize);
	}

	public List<TsmpGroup> query_SyncTsmpdpapiToDpClient(String moduleName, String regStatus) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select G");
		sb.append(" from TsmpGroup G");
		sb.append(" where 1 = 1");
		sb.append(" and exists (");
		sb.append(" 	select 1");
		sb.append(" 	from TsmpGroupApi GA");
		sb.append(" 	where GA.groupId = G.groupId");
		sb.append(" 	and GA.moduleName = :moduleName");
		sb.append(" )");
		sb.append(" and exists (");
		sb.append(" 	select 1");
		sb.append(" 	from TsmpClient C");
		sb.append(" 	where C.clientName = G.groupName");
		sb.append(" 	and exists (");
		sb.append(" 		select 1");
		sb.append(" 		from TsmpDpClientext EXT");
		sb.append(" 		where EXT.clientId = C.clientId");
		sb.append(" 		and EXT.regStatus = :regStatus");
		sb.append(" 	)");
		sb.append(" )");
		params.put("moduleName", moduleName);
		params.put("regStatus", regStatus);

		return doQuery(sb.toString(), params, TsmpGroup.class);
	}

	public List<TsmpGroup> findByVgroupFlagAndAliasAndCreateNameFromDpApp(String vgroupFlag, String groupAlias,
			String createUser, boolean isBoundApp, Long appId) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append("SELECT G ");
		sb.append("FROM TsmpGroup G ");
		sb.append("WHERE 1 = 1 ");

		if (StringUtils.hasText(vgroupFlag)) {
			sb.append("AND G.vgroupFlag = :vgroupFlag ");
			params.put("vgroupFlag", vgroupFlag);
		}

		if (StringUtils.hasText(groupAlias)) {
			sb.append("AND G.groupAlias = :groupAlias ");
			params.put("groupAlias", groupAlias);
		}

		if (StringUtils.hasText(createUser)) {
			sb.append("AND G.createUser = :createUser ");
			params.put("createUser", createUser);
		}

		if (isBoundApp) {
			sb.append("AND EXISTS ( ");
			sb.append("    SELECT 1 ");
			sb.append("    FROM TsmpClientGroup tcg ");
			sb.append("    WHERE tcg.groupId = G.groupId ");
			sb.append("    AND EXISTS ( ");
			sb.append("        SELECT 1 ");
			sb.append("        FROM DpApp d ");
			sb.append("        WHERE d.clientId = tcg.clientId ");

			if (appId != null) {
				sb.append("AND d.dpApplicationId = :dpApplicationId");
				params.put("dpApplicationId", appId);
			}

			sb.append("    )");
			sb.append(")");
		}

		return doQuery(sb.toString(), params, TsmpGroup.class);
	}

	public List<TsmpGroup> queryByAA0238Service(String groupId, List<String> groupAuthoritieIdList,
			String securityLevelId, String[] words, Integer pageSize) {

		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select G");
		sb.append(" from TsmpGroup G");
		sb.append(" where 1 = 1 ");

		// 查詢一般群組，不包含虛擬群組
		sb.append(" and G.vgroupFlag ='0' ");

		// securityLevelId
		if (!StringUtils.isEmpty(securityLevelId)) {
			sb.append(" and G.securityLevelId = :securityLevelId ");
			params.put("securityLevelId", securityLevelId);
		}

		// 分頁
		if (!StringUtils.isEmpty(groupId)) {
			sb.append("	and ( 1 = 2");
			sb.append(" or (G.groupId > :groupId )");
			sb.append(" )");

			params.put("groupId", groupId);
		}

		if (groupAuthoritieIdList != null && groupAuthoritieIdList.size() > 0) {
			sb.append(" AND EXISTS( ");
			sb.append(" 	select 1");
			sb.append(" 	from TsmpGroupAuthoritiesMap A");
			sb.append(" 	where 1 = 1");
			sb.append(" 	and A.groupId = G.groupId");
			sb.append(" 	and A.groupAuthoritieId in (:groupAuthoritieIdList)");

			sb.append(" ) ");

			params.put("groupAuthoritieIdList", groupAuthoritieIdList);
		}

		// 關鍵字search
		if (words != null && words.length > 0) {
			sb.append(" 	and ( 1 = 2");
			for (int i = 0; i < words.length; i++) {
				sb.append("   	 or UPPER(G.groupName) like :word" + i);// 忽略大小寫
				sb.append("  	 or UPPER(G.groupAlias) like :word" + i);// 忽略大小寫
				params.put("word" + i, "%" + words[i].toUpperCase() + "%");// 忽略大小寫
			}

			sb.append(" )");
		}

		sb.append(" ORDER BY G.groupId asc");

		return doQuery(sb.toString(), params, TsmpGroup.class, pageSize);
	}

	public List<TsmpGroup> query_AA0303Service_01(String apiKey, String moduleName) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		sb.append(" SELECT G");
		sb.append(" FROM TsmpGroup G");
		sb.append(" WHERE G.vgroupFlag = :vgroupFlag");
		sb.append(" AND (G.vgroupId IS NOT NULL AND LENGTH(G.vgroupId) > 0)");
		sb.append(" AND EXISTS (");
		sb.append("     SELECT 1");
		sb.append("     FROM TsmpGroupApi GA");
		sb.append("     WHERE GA.groupId = G.groupId");
		sb.append("     AND GA.apiKey = :apiKey");
		sb.append("     AND GA.moduleName = :moduleName");
		sb.append(" )");

		params.put("vgroupFlag", "1"); // 由虛擬群組所建立
		params.put("apiKey", apiKey);
		params.put("moduleName", moduleName);

		return doQuery(sb.toString(), params, TsmpGroup.class);
	}

}