package tpi.dgrv4.entity.repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.TsmpGroupApi;
import tpi.dgrv4.entity.entity.TsmpGroupApiTopN;
import tpi.dgrv4.entity.vo.AA0237ReqB;

public class TsmpGroupApiDaoImpl extends BaseDao {
	// add custom methods here

	public List<TsmpGroupApi> findByDpAppClientIdWithOutVgroup(String clientId) {

		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT tgaa");
		sb.append(" FROM TsmpGroupApi tgaa ");
		sb.append(" WHERE 3 = 3 ");
		sb.append(" AND ( ");
		sb.append("     1 = 2 ");
		sb.append("     OR EXISTS ( ");
		sb.append("         SELECT 1 ");
		sb.append("         FROM TsmpGroup tsmpGroup ");
		sb.append("         WHERE tgaa.groupId = tsmpGroup.groupId ");
		sb.append("         AND (tsmpGroup.vgroupId = '' OR tsmpGroup.vgroupId IS NULL) ");
		sb.append("         AND ( ");
		sb.append("             1 = 2 ");
		sb.append("             OR EXISTS ( ");
		sb.append("                 SELECT 1 ");
		sb.append("                 FROM TsmpClientGroup tsmpClientGroup ");
		sb.append("                 WHERE tgaa.groupId = tsmpClientGroup.groupId ");
		sb.append("                 AND ( ");
		sb.append("                     1 = 2 ");
		sb.append("                     OR EXISTS ( ");
		sb.append("                         SELECT 2 ");
		sb.append("                         FROM TsmpClient tsmpClient ");
		sb.append("                         WHERE tsmpClient.clientId = tsmpClientGroup.clientId ");
		sb.append("                         AND ( ");
		sb.append("                             1 = 2 ");
		sb.append("                             OR EXISTS ( ");
		sb.append("                                 SELECT 3 ");
		sb.append("                                 FROM DpApp dpApp ");
		sb.append("                                 WHERE tsmpClient.clientId = :clientId ");
		sb.append("                             ) ");
		sb.append("                         ) ");
		sb.append("                     ) ");
		sb.append("                 ) ");
		sb.append("             ) ");
		sb.append("         ) ");
		sb.append("     ) ");
		sb.append(" ) ");
		params.put("clientId", clientId);

		return doQuery(sb.toString(), params, TsmpGroupApi.class);
	}

	public List<TsmpGroupApi> findByDpAppClientIdWithOutGroup(String clientId) {

		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT tgaa");
		sb.append(" FROM TsmpGroupApi tgaa ");
		sb.append(" WHERE 3 = 3 ");
		sb.append(" AND ( ");
		sb.append("     1 = 2 ");
		sb.append("     OR EXISTS ( ");
		sb.append("         SELECT 1 ");
		sb.append("         FROM TsmpGroup tsmpGroup ");
		sb.append("         WHERE tgaa.groupId = tsmpGroup.groupId ");
		sb.append("         AND (tsmpGroup.vgroupId <> '' OR tsmpGroup.vgroupId IS NOT NULL) ");
		sb.append("         AND ( ");
		sb.append("             1 = 2 ");
		sb.append("             OR EXISTS ( ");
		sb.append("                 SELECT 1 ");
		sb.append("                 FROM TsmpClientGroup tsmpClientGroup ");
		sb.append("                 WHERE tgaa.groupId = tsmpClientGroup.groupId ");
		sb.append("                 AND ( ");
		sb.append("                     1 = 2 ");
		sb.append("                     OR EXISTS ( ");
		sb.append("                         SELECT 2 ");
		sb.append("                         FROM TsmpClient tsmpClient ");
		sb.append("                         WHERE tsmpClient.clientId = tsmpClientGroup.clientId ");
		sb.append("                         AND ( ");
		sb.append("                             1 = 2 ");
		sb.append("                             OR EXISTS ( ");
		sb.append("                                 SELECT 3 ");
		sb.append("                                 FROM DpApp dpApp ");
		sb.append("                                 WHERE tsmpClient.clientId = :clientId ");
		sb.append("                             ) ");
		sb.append("                         ) ");
		sb.append("                     ) ");
		sb.append("                 ) ");
		sb.append("             ) ");
		sb.append("         ) ");
		sb.append("     ) ");
		sb.append(" ) ");
		params.put("clientId", clientId);

		return doQuery(sb.toString(), params, TsmpGroupApi.class);
	}

	public List<String> findGroupIdByDpAppClientIdWithOutVgroup(String clientId) {

		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder();

//		sb.append(" SELECT DISTINCT tga.groupId ");
//		sb.append(" FROM TsmpGroupApi tga ");
//		sb.append(" WHERE 2 = 2 ");
//		sb.append(" AND ( ");
//		sb.append("     1 = 2 ");
//		sb.append("     OR EXISTS ( ");
		sb.append("         SELECT tccc.groupId ");
		sb.append("         FROM TsmpClientGroup tccc ");
		sb.append("         WHERE 3 = 3 ");
		sb.append("         AND ( ");
		sb.append("             1 = 2 ");
		sb.append("             OR EXISTS ( ");
		sb.append("                 SELECT 1 ");
		sb.append("                 FROM TsmpGroup tsmpGroup ");
		sb.append("                 WHERE tsmpGroup.groupId = tccc.groupId ");
		sb.append("                 AND (tsmpGroup.vgroupId = '' OR tsmpGroup.vgroupId IS NULL) ");
		sb.append("                 AND ( ");
		sb.append("                     1 = 2 ");
		sb.append("                     OR EXISTS ( ");
		sb.append("                         SELECT 1 ");
		sb.append("                         FROM TsmpClient tc ");
		sb.append("                         WHERE tc.clientId = tccc.clientId ");
		sb.append("                         AND ( ");
		sb.append("                             1 = 2 ");
		sb.append("                             OR EXISTS ( ");
		sb.append("                                 SELECT 1 ");
		sb.append("                                 FROM DpApp dpApp ");
		sb.append("                                 WHERE tc.clientId = :clientId ");
		sb.append("                             ) ");
		sb.append("                         ) ");
		sb.append("                     ) ");
		sb.append("                 ) ");
		sb.append("             ) ");
		sb.append("         ) ");
		params.put("clientId", clientId);

		return doQuery(sb.toString(), params, String.class);
	}

	public List<TsmpGroupApiTopN> findReferApiBindDpApp(String apiKey, String moduleNmae, String clientId,
			Integer pageSize) {

		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder();

		sb.append(
				" SELECT new tpi.dgrv4.entity.entity.TsmpGroupApiTopN(tga.moduleName, tga.apiKey, COUNT(DISTINCT da.dpApplicationId)) ");
		sb.append(" FROM TsmpGroupApi tga, TsmpClientGroup tcg, TsmpClient tc, DpApp da , TsmpApi ta");
		sb.append(" WHERE 3 = 3 ");

		if (StringUtils.hasText(apiKey) && StringUtils.hasText(moduleNmae)) {
			sb.append(" AND ");
			sb.append(" tga.apiKey = :apiKey AND tga.moduleName = :moduleName");
			params.put("apiKey", apiKey);
			params.put("moduleName", moduleNmae);
		}

		if (StringUtils.hasText(clientId)) {
			sb.append(" AND :clientId = tcg.clientId ");
			sb.append(" AND da.clientId = :clientId ");
			params.put("clientId", clientId);
		} else {
			sb.append(" AND tc.clientId = tcg.clientId ");
			sb.append(" AND da.clientId = tc.clientId ");
		}

		sb.append(" AND tga.groupId = tcg.groupId ");
		sb.append(" GROUP BY tga.moduleName, tga.apiKey ");
		sb.append(" ORDER BY COUNT(DISTINCT da.dpApplicationId) DESC");

		return doQuery(sb.toString(), params, TsmpGroupApiTopN.class, pageSize);
	}

	public List<TsmpGroupApi> query_aa0237Service(TsmpGroupApi lastTsmpGroupApi, String vgroupId, AA0237ReqB reqB,
			String[] words, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select GA");
		sb.append(" from TsmpGroupApi GA");
		sb.append(" where 1 = 1 ");
		sb.append(" and exists 	( ");
		sb.append(" 	select 1 ");
		sb.append("  	from TsmpVgroupGroup VGG");
		sb.append(" 	where 1 = 1 ");
		sb.append(" 	and exists ( ");
		sb.append("  		select 1 ");
		sb.append(" 		from TsmpVgroup VG ");
		sb.append(" 		where VG.vgroupId = VGG.vgroupId ");
		sb.append(" 		and VG.vgroupId = :vgroupId ");
		sb.append(" 	) ");
		sb.append(" 	and VGG.groupId = GA.groupId ");
		sb.append(" ) ");
		params.put("vgroupId", vgroupId);

		if (reqB != null) {
			// 模組名稱
			if (!StringUtils.isEmpty(reqB.getModuleName())) {
				sb.append(" and GA.moduleName = :moduleName");
				params.put("moduleName", reqB.getModuleName());

			}

			if (reqB.getP()) {
				// 分頁
				if (lastTsmpGroupApi != null) {
					sb.append("	and ( 1 = 2");
					sb.append(" or (GA.moduleName > :lastmoduleName )");
					sb.append(" or (GA.moduleName = :lastmoduleName and GA.apiKey > :lastApiKey)");
					sb.append(
							" or (GA.moduleName = :lastmoduleName and GA.apiKey = :lastApiKey and GA.groupId > :lastGroupId ) ");
					sb.append(" )");

					params.put("lastmoduleName", lastTsmpGroupApi.getModuleName());
					params.put("lastApiKey", lastTsmpGroupApi.getApiKey());
					params.put("lastGroupId", lastTsmpGroupApi.getGroupId());
				}

				// 關鍵字search(api_key, api_name)
				if (words != null && words.length > 0) {

					sb.append(" and ( 1 = 2 ");
					for (int i = 0; i < words.length; i++) {
						sb.append(" or upper(GA.apiKey) like :word" + i);
						params.put("word" + i, "%" + words[i].toUpperCase() + "%");// 忽略大小寫
					}
					sb.append("	   or exists (");
					sb.append("    		select  1 ");
					sb.append(" 		from TsmpApi A ");
					sb.append(" 		where 1 = 1 ");
					sb.append(" 		and A.moduleName = GA.moduleName");
					sb.append(" 		and A.apiKey = GA.apiKey ");
					sb.append(" 		and (  1 = 2");
					for (int i = 0; i < words.length; i++) {
						sb.append("  			or UPPER(A.apiName) like :_word" + i);
						params.put("_word" + i, "%" + words[i].toUpperCase() + "%");// 忽略大小寫
					}
					sb.append("			)");
					sb.append(" 	)");
					sb.append(" ) ");
				}
			}
		}

		sb.append(" order by GA.moduleName asc, GA.apiKey asc, GA.groupId asc ");

		return doQuery(sb.toString(), params, TsmpGroupApi.class, pageSize);

	}

	public List<String> findUniqueModuleByGroupIdOrderByModuleName(String groupId) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT DISTINCT tsmpGroupApi.moduleName ");
		sb.append(" FROM TsmpGroupApi tsmpGroupApi ");
		sb.append(" WHERE 1 = 1 ");
		sb.append(" AND tsmpGroupApi.groupId = :groupId ");
		sb.append(" ORDER BY tsmpGroupApi.moduleName ASC ");

		params.put("groupId", groupId);

		return doQuery(sb.toString(), params, String.class);
	}

	public List<TsmpGroupApi> findByTsmpGroupApiIdAndKeyword(TsmpGroupApi lastTsmpGroupApi, String groupId,
			String moduleName, String[] keyword, int pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT tsmpGroupApi ");
		sb.append(" FROM TsmpGroupApi tsmpGroupApi ");
		sb.append(" WHERE 1 = 1 ");

		// 條件 group Id
		sb.append(" AND tsmpGroupApi.groupId = :groupId ");
		params.put("groupId", groupId);

		// 條件 moduleName名稱
		sb.append(" AND tsmpGroupApi.moduleName = :moduleName ");
		params.put("moduleName", moduleName);

		// 分頁
		if (lastTsmpGroupApi != null) {
			sb.append(" AND ( ");
			sb.append("  	  1 = 2");
			sb.append(" 	  OR ( tsmpGroupApi.groupId > :lastGroupId ) ");
			sb.append(
					" 	  OR ( tsmpGroupApi.groupId = :lastGroupId AND tsmpGroupApi.moduleName > :lastModuleName ) ");
			sb.append(
					" 	  OR ( tsmpGroupApi.groupId = :lastGroupId AND tsmpGroupApi.moduleName = :lastModuleName AND tsmpGroupApi.apiKey > :lastApiKey ) ");
			sb.append(" 	) ");
			params.put("lastGroupId", lastTsmpGroupApi.getGroupId());
			params.put("lastModuleName", lastTsmpGroupApi.getModuleName());
			params.put("lastApiKey", lastTsmpGroupApi.getApiKey());
		}

		// 關鍵字
		if (keyword != null && keyword.length > 0) {
			sb.append(" AND ( ");
			sb.append("  	  1 = 2");
			for (int i = 0; i < keyword.length; i++) {
				sb.append("  	  OR UPPER(tsmpGroupApi.apiKey) LIKE :searchkeyword" + i);
				sb.append("  	  OR EXISTS( ");
				sb.append("  	  			SELECT 1 ");
				sb.append("  	  			FROM TsmpApi tsmpApi ");
				sb.append("  	  			WHERE tsmpApi.moduleName = tsmpGroupApi.moduleName ");
				sb.append("  	  			AND tsmpApi.apiKey = tsmpGroupApi.apiKey ");
				sb.append("  	  			AND UPPER(tsmpApi.apiName) LIKE :searchkeyword" + i);
				sb.append("  	  			) ");
				params.put("searchkeyword" + i, "%" + keyword[i].toUpperCase() + "%");
			}
			sb.append("  	) ");
		}
		sb.append(" ORDER BY tsmpGroupApi.groupId ASC, tsmpGroupApi.moduleName ASC, tsmpGroupApi.apiKey ASC ");

		return doQuery(sb.toString(), params, TsmpGroupApi.class, pageSize);
	}

	public List<TsmpGroupApi> query_aa0320Service(String gId, String moduleName, String apiKey, String[] keywords,
			int pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();

		sb.append(" SELECT GA ");
		sb.append(" FROM TsmpGroupApi GA ");
		sb.append(" WHERE GA.apiKey = :apiKey AND GA.moduleName = :moduleName");
		params.put("apiKey", apiKey);
		params.put("moduleName", moduleName);

		// 分頁
		if (!StringUtils.isEmpty(gId)) {
			sb.append(" AND (");
			sb.append(" 	GA.groupId > :gId");
			sb.append(" )");
			params.put("gId", gId);
		}

		// 關鍵字
		if (keywords != null && keywords.length > 0) {
			sb.append(" AND EXISTS( ");
			sb.append(" 	SELECT 1 ");
			sb.append(" 	FROM TsmpGroup G ");
			sb.append(" 	WHERE G.groupId = GA.groupId ");

			sb.append(" AND( ");
			sb.append(" 	1 = 2");
			for (int i = 0; i < keywords.length; i++) {
				sb.append(" OR UPPER(G.groupName) LIKE :keyworkSearch" + i);
				sb.append(" OR UPPER(G.groupAlias) LIKE :keyworkSearch" + i);
				sb.append(" OR UPPER(G.groupDesc) LIKE :keyworkSearch" + i);
				params.put("keyworkSearch" + i, "%" + keywords[i].toUpperCase() + "%");
			}
//		}

			sb.append(" 		OR ( G.vgroupFlag = '1' ");
			sb.append("  			AND EXISTS(");
			sb.append("  				SELECT 1 ");
			sb.append("  				FROM TsmpVgroupGroup VGG ");
			sb.append("  				WHERE VGG.groupId = G.groupId ");
			sb.append("  				AND EXISTS(");
			sb.append("  					SELECT 1 ");
			sb.append("  					FROM TsmpVgroup VG ");
			sb.append("  					WHERE VG.vgroupId = VGG.vgroupId");
			// 關鍵字
//		if (keywords != null && keywords.length > 0) {
			sb.append("  				AND ( 1 = 2");
			for (int i = 0; i < keywords.length; i++) {
				sb.append(" 			OR UPPER(VG.vgroupName) LIKE :keyworkSearch" + i);
				sb.append(" 			OR UPPER(VG.vgroupAlias) LIKE :keyworkSearch" + i);
				sb.append("				OR UPPER(VG.vgroupDesc) LIKE :keyworkSearch" + i);
				params.put("keyworkSearch" + i, "%" + keywords[i].toUpperCase() + "%");
			}
			sb.append("  				)");
			sb.append("  				)");
			sb.append("  			)");
			sb.append("  		)");
			sb.append(" 	) ");
			sb.append(" ) ");
		}

		sb.append(" ORDER BY GA.groupId ASC ");
		return doQuery(sb.toString(), params, TsmpGroupApi.class, pageSize);

	}
}
