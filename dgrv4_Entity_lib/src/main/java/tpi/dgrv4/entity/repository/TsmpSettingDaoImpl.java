package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.TsmpSetting;

public class TsmpSettingDaoImpl extends BaseDao {

	public List<TsmpSetting> query_DPB9900Service_01(String lastId, String[] keywords, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();
		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT S");
		sb.append(" FROM TsmpSetting S");
		sb.append(" WHERE 1 = 1");
		// 分頁
		if (StringUtils.hasText(lastId)) {
			sb.append(" AND S.id > :lastId");
			params.put("lastId", lastId);
		}
		// 關鍵字搜尋
		if (keywords != null && keywords.length > 0) {
			sb.append(" AND ( 1 = 2");
			for (int i = 0; i < keywords.length; i++) {
				sb.append("     OR UPPER(S.id) LIKE :keyword" + i);
				sb.append("     OR UPPER(S.value) LIKE :keyword" + i);
				sb.append("     OR UPPER(S.memo) LIKE :keyword" + i);
				params.put(("keyword" + i), "%" + keywords[i].toUpperCase() + "%");
			}
			sb.append(" )");
		}
		sb.append(" ORDER BY S.id ASC");
		return doQuery(sb.toString(), params, TsmpSetting.class, pageSize);
	}

}
