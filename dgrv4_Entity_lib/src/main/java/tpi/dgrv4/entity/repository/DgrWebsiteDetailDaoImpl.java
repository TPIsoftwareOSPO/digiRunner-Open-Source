package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.DgrWebsiteDetail;

public class DgrWebsiteDetailDaoImpl extends BaseDao {

	private final String dgrWebsiteIdStr = "dgrWebsiteId";
	private final String keyworkSearchStr = "keyworkSearch";

	public List<DgrWebsiteDetail> findByDgrWebsiteIdAndKeyword(Long dgrWebsiteId, String[] words, Integer pageSize) {

		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT w ");
		sb.append(" FROM DgrWebsiteDetail w ");
		sb.append(" WHERE 1 = 1  ");

		if (dgrWebsiteId != null) {
			sb.append(" AND ");
			sb.append(" ( ");
			sb.append("    1 = 2 ");
			sb.append("    OR w.dgrWebsiteId > :dgrWebsiteId ");
			sb.append(" ) ");
			params.put(dgrWebsiteIdStr, dgrWebsiteId);
		}

		if (words != null && words.length > 0) {
			sb.append(" AND ( 1 = 2 ");
			for (int i = 0; i < words.length; i++) {
				sb.append(" OR UPPER(w.contentPath) like :keyworkSearch" + i);
				sb.append(" OR UPPER(w.url) like :keyworkSearch" + i);
				params.put(keyworkSearchStr + i, "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" ) ");
		}

		sb.append(" ORDER BY w.dgrWebsiteId ASC ");

		return doQuery(sb.toString(), params, DgrWebsiteDetail.class, pageSize);
	}

}
