package tpi.dgrv4.entity.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.DpApp;

public class DpAppDaoImpl extends BaseDao {

	public List<Long> findNotApprovedDpAppCountByDpUserNameAndIss(String dpUserName, String iss) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT COUNT(1) ");
		sb.append(" FROM DpApp A ");
		sb.append(" WHERE 1 = 1  ");
		sb.append("   AND ( 1 = 2 ");
		sb.append("   OR Exists (");
		sb.append("       SELECT tsmpClient");
		sb.append("       FROM TsmpClient tsmpClient");
		sb.append("       WHERE A.clientId = tsmpClient.clientId");
		sb.append("       AND ( tsmpClient.clientStatus = '2' OR tsmpClient.clientStatus = '3')");
		sb.append("  )");
		sb.append(" )");
		sb.append("   AND ( 1 = 2 ");
		sb.append("   OR Exists (");
		sb.append("       SELECT dpUser");
		sb.append("       FROM DpUser dpUser");
		sb.append("       WHERE A.dpUserName = :dpUserName And A.iss = :iss");
		sb.append("  )");
		sb.append(" )");
		params.put("dpUserName", dpUserName);
		params.put("iss", iss);

		return doQuery(sb.toString(), params, Long.class);
	}

	public List<Long> findReviewedDpAppCountByDpUserNameAndIss(String dpUserName, String iss) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT COUNT(1) ");
		sb.append(" FROM DpApp A ");
		sb.append(" WHERE 1 = 1  ");
		sb.append("   AND ( 1 = 2 ");
		sb.append("   OR Exists (");
		sb.append("       SELECT tsmpClient");
		sb.append("       FROM TsmpClient tsmpClient");
		sb.append("       WHERE A.clientId = tsmpClient.clientId");
		sb.append("       AND ( tsmpClient.clientStatus = '1')");
		sb.append("  )");
		sb.append(" )");
		sb.append("   AND ( 1 = 2 ");
		sb.append("   OR Exists (");
		sb.append("       SELECT dpUser");
		sb.append("       FROM DpUser dpUser");
		sb.append("       WHERE A.dpUserName = :dpUserName And A.iss = :iss");
		sb.append("  )");
		sb.append(" )");
		params.put("dpUserName", dpUserName);
		params.put("iss", iss);

		return doQuery(sb.toString(), params, Long.class);
	}

	public List<String> findAppClientIdByDpUserNameAndIss(String dpUserName, String iss) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT A.clientId ");
		sb.append(" FROM DpApp A ");
		sb.append(" WHERE dpUserName = :dpUserName And iss = :iss  ");
		params.put("dpUserName", dpUserName);
		params.put("iss", iss);

		return doQuery(sb.toString(), params, String.class);

	}

	public List<Long> findPendingDpAppCount() {

		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT COUNT(1) ");
		sb.append(" FROM DpApp A ");
		sb.append(" WHERE 1 = 1  ");
		sb.append("   AND ( 1 = 2 ");
		sb.append("   OR Exists (");
		sb.append("       SELECT tsmpClient");
		sb.append("       FROM TsmpClient tsmpClient");
		sb.append("       WHERE A.clientId = tsmpClient.clientId");
		sb.append("       AND ( tsmpClient.clientStatus = '2' OR tsmpClient.clientStatus = '3' )");
		sb.append("  )");
		sb.append(" )");

		return doQuery(sb.toString(), params, Long.class);
	}

	public List<DpApp> findAppByDpUserNameAndIssAndAppDescAndApiNameAndApiDescAndModuleNameAndApiKey(Long appId,
			String dpUserName, String iss, String[] words, String isAdmin, String encodeClientStatus, Integer pageSize) {

		Map<String, Object> params = new HashMap<>();

		boolean isAdminBoolean = Boolean.valueOf(isAdmin);

		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT A ");
		sb.append(" FROM DpApp A ");
		sb.append(" WHERE 1 = 1  ");

		if (!isAdminBoolean && StringUtils.hasText(dpUserName)) {
			sb.append(" AND ");
			sb.append(" ( ");
			sb.append("    1 = 2 ");
			sb.append("    OR A.dpUserName = :dpUserName And A.iss = :iss");
			sb.append(" ) ");
			params.put("dpUserName", dpUserName);
			params.put("iss", iss);
		}

		if (appId != null) {
			sb.append(" AND ");
			sb.append(" ( ");
			sb.append("    1 = 2 ");
			sb.append("    OR A.dpApplicationId > :appId ");
			sb.append(" ) ");
			params.put("appId", appId);
		}

		if (StringUtils.hasText(encodeClientStatus) && !encodeClientStatus.equals("-1")) {

			sb.append(" AND ");
			sb.append(" ( 1 = 2 ");
			sb.append(" OR Exists (");
			sb.append("   SELECT tsmpClient.clientId");
			sb.append("   FROM TsmpClient tsmpClient");
			sb.append("   WHERE 1 = 1");
			sb.append("   AND ( A.clientId = tsmpClient.clientId )");
			sb.append("   AND ( tsmpClient.clientStatus = :encodeClientStatus )");
			sb.append("  ) ");
			sb.append(" ) ");
			params.put("encodeClientStatus", encodeClientStatus);

		}

		if (words != null && words.length > 0) {
			sb.append(" AND ( 1 = 2 ");
			for (int i = 0; i < words.length; i++) {
				sb.append(" OR UPPER(A.applicationName) like :keyworkSearch" + i);
				sb.append(" OR UPPER(A.applicationDesc) like :keyworkSearch" + i);
				sb.append(" OR UPPER(A.iss) like :keyworkSearch" + i);
				sb.append(" OR UPPER(A.dpUserName) like :keyworkSearch" + i);
				sb.append(" OR Exists (");
				sb.append("   SELECT tsmpClient.clientId");
				sb.append("   FROM TsmpClient tsmpClient");
				sb.append("   WHERE 1 = 1");
				sb.append("   AND ( A.clientId = tsmpClient.clientId ");
				sb.append("   AND ( 1 = 2 ");
				sb.append("   OR Exists (");
				sb.append("       SELECT tsmpClientGroup.clientId");
				sb.append("       FROM TsmpClientGroup tsmpClientGroup");
				sb.append("       WHERE 1 = 1");
				sb.append("       AND ( tsmpClient.clientId = tsmpClientGroup.clientId ");
				sb.append("       AND ( 1 = 2 ");
				sb.append("       OR Exists (");
				sb.append("           SELECT tsmpGroupApi.groupId");
				sb.append("           FROM TsmpGroupApi tsmpGroupApi");
				sb.append("           WHERE 1 = 1");
				sb.append("           AND ( tsmpClientGroup.groupId = tsmpGroupApi.groupId ");
				sb.append("           AND ( 1 = 2 ");
				sb.append("           OR Exists (");
				sb.append("              SELECT tsmpApi.apiKey");
				sb.append("              FROM TsmpApi tsmpApi");
				sb.append("              WHERE 1 = 1");
				sb.append("              AND ( tsmpGroupApi.apiKey = tsmpApi.apiKey ");
				sb.append("              AND ( 1 = 2 ");
				sb.append(
						"               OR tsmpApi.apiKey = tsmpGroupApi.apiKey AND tsmpApi.moduleName = tsmpGroupApi.moduleName");
				sb.append("               AND ( 1 = 2 ");
				sb.append("                OR UPPER(tsmpApi.apiDesc) like :keyworkSearch" + i);
				sb.append("                OR UPPER(tsmpApi.apiName) like :keyworkSearch" + i);
				sb.append("                OR UPPER(tsmpApi.apiKey) like :keyworkSearch" + i);
				sb.append("                OR UPPER(tsmpApi.moduleName) like :keyworkSearch" + i);
				sb.append("                 )");
				sb.append("                )");
				sb.append("               )");
				sb.append("              )");
				sb.append("             )");
				sb.append("            )");
				sb.append("           )");
				sb.append("          )");
				sb.append("         )");
				sb.append("       )");
				sb.append("     )");
				sb.append("   )");
				sb.append(" )");
				sb.append("  OR Exists (");
				sb.append("     SELECT du.dpUserName");
				sb.append("     FROM DpUser du");
				sb.append("     WHERE 1 = 1");
				sb.append("     AND ( A.dpUserName = du.dpUserName ");
				sb.append("     AND ( 1 = 2 ");
				sb.append("     OR UPPER(du.userAlias) like :keyworkSearch" + i);
				sb.append("  )");
				sb.append(" )");
				sb.append(")");
				params.put("keyworkSearch" + i, "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" ) ");
		}

		sb.append(" ORDER BY A.dpApplicationId ASC ");

		return doQuery(sb.toString(), params, DpApp.class, pageSize);
	}

}
