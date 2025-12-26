package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.jpql.CusSetting;

public class CusSettingDaoImpl extends BaseDao {
	public List<CusSetting> queryDPB9910Service(Integer lastSortBy, String[] keywords, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT c");
		sb.append(" FROM CusSetting c");
		sb.append(" WHERE 1 = 1");
		// 分頁
		if (lastSortBy != null) {
			sb.append(" AND c.sortBy > :lastSortBy");
			params.put("lastSortBy", lastSortBy);
		}
		// 關鍵字搜尋
		if (keywords != null && keywords.length > 0) {
			sb.append(" AND ( 1 = 2");
			for (int i = 0; i < keywords.length; i++) {
				sb.append(" 	OR UPPER(c.keywordSearch) LIKE :keyword" + i);
				params.put(("keyword" + i), "%" + keywords[i].toUpperCase() + "%");
			}
			sb.append(" )");
		}
		sb.append(" ORDER BY c.sortBy ASC");
		return doQuery(sb.toString(), params, CusSetting.class, pageSize);
	}
}
