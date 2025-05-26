package tpi.dgrv4.entity.repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.DgrGtwIdpInfoCus;

public class DgrGtwIdpInfoCusDaoImpl extends BaseDao {

	public List<DgrGtwIdpInfoCus> findByGtwIdpInfoCusIdAndClientIdAndStatusOrderByUpdateDateTimeDescGtwIdpInfoCusIdDesc(
			Long gtwIdpInfoCusId, String clientId, String status, String[] keywords, Integer pageSize) {

		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT A ");
		sb.append(" FROM DgrGtwIdpInfoCus A ");
		sb.append(" WHERE 1 = 1 ");

		if (gtwIdpInfoCusId != null) {
			sb.append(" AND ( ");
			sb.append("    1 = 2 ");
			sb.append("    OR A.gtwIdpInfoCusId < :gtwIdpInfoCusId ");
			sb.append(" ) ");
			params.put("gtwIdpInfoCusId", gtwIdpInfoCusId);
		}

		if (clientId != null && !clientId.isEmpty()) {
			sb.append(" AND A.clientId = :clientId ");
			params.put("clientId", clientId);
		} else {
			return Collections.emptyList();
		}

		if (status != null && !status.isEmpty()) {
			sb.append(" AND A.status = :status ");
			params.put("status", status);
		}

		if (keywords != null && keywords.length > 0) {
			sb.append(" AND ( ");
			for (int i = 0; i < keywords.length; i++) {
				if (i > 0) {
					sb.append(" OR ");
				}
				sb.append(" (A.cusLoginUrl LIKE :keyword").append(i);
				sb.append(" OR A.cusUserDataUrl LIKE :keyword").append(i);
				sb.append(" OR A.pageTitle LIKE :keyword").append(i).append(") ");
				params.put("keyword" + i, "%" + keywords[i] + "%");
			}
			sb.append(" ) ");
		}

		sb.append(" ORDER BY A.updateDateTime DESC, A.gtwIdpInfoCusId DESC ");

		return doQuery(sb.toString(), params, DgrGtwIdpInfoCus.class, pageSize);
	}
}
