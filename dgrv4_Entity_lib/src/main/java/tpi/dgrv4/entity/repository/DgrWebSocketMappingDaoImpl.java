package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.DgrWebSocketMapping;

public class DgrWebSocketMappingDaoImpl extends BaseDao {
	
	public List<DgrWebSocketMapping> queryDPB0174(Long lastId, String[] keywords, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT S");
		sb.append(" FROM DgrWebSocketMapping S");
		sb.append(" WHERE 1 = 1");
		// 分頁
		if (lastId != null) {
			sb.append(" AND S.wsMappingId > :lastId");
			params.put("lastId", lastId);
		}
		// 關鍵字搜尋
		if (keywords != null && keywords.length > 0) {
			sb.append(" AND ( 1 = 2");
			for (int i = 0; i < keywords.length; i++) {
				sb.append("     OR UPPER(S.keywordSearch) LIKE :keyword" + i);
				params.put(("keyword" + i), "%" + keywords[i].toUpperCase() + "%");
			}
			sb.append(" )");
		}
		sb.append(" ORDER BY S.wsMappingId ASC");
		return doQuery(sb.toString(), params, DgrWebSocketMapping.class, pageSize);
	}
}
