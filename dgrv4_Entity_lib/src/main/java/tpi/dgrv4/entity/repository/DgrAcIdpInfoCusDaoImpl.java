package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.DgrAcIdpInfoCus;

public class DgrAcIdpInfoCusDaoImpl extends BaseDao {

	public List<DgrAcIdpInfoCus> findByAcIdpInfoCusIdAndAcIdpInfoCusNameAndCusStatusOrderByUpdateDateTimeDescAcIdpInfoCusIdDesc(
			Long acIdpInfoCusId,  String cusStatus, String[] words, Integer pageSize) {

		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT A ");
		sb.append(" FROM DgrAcIdpInfoCus A ");
		sb.append(" WHERE 1 = 1 ");

		if (acIdpInfoCusId != null) {
			sb.append(" AND ( ");
			sb.append("    1 = 2 ");
			sb.append("    OR A.acIdpInfoCusId < :acIdpInfoCusId ");
			sb.append(" ) ");
			params.put("acIdpInfoCusId", acIdpInfoCusId);
		}

		if (cusStatus != null && !cusStatus.isEmpty()) {
			sb.append(" AND A.cusStatus = :cusStatus ");
			params.put("cusStatus", cusStatus);
		}

		if (words != null && words.length > 0) {
			sb.append(" AND ( ");
			for (int i = 0; i < words.length; i++) {
				if (i > 0) {
					sb.append(" OR ");
				}
				sb.append(" (A.acIdpInfoCusName LIKE :word").append(i);
				sb.append(" OR A.cusLoginUrl LIKE :word").append(i);
				sb.append(" OR A.cusBackendLoginUrl LIKE :word").append(i);
				sb.append(" OR A.cusUserDataUrl LIKE :word").append(i).append(") ");
				params.put("word" + i, "%" + words[i] + "%");
			}
			sb.append(" ) ");
		}

		sb.append(" ORDER BY A.updateDateTime DESC, A.acIdpInfoCusId DESC ");

		return doQuery(sb.toString(), params, DgrAcIdpInfoCus.class, pageSize);
	}
}
