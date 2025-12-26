package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.TsmpRoleTxidMap;

public class TsmpRoleTxidMapDaoImpl extends BaseDao {
	// add custom methods here

	public List<TsmpRoleTxidMap> query_dpb0111Service_01(String p_roleId, String p_listType, //
			String listType, String[] keywords) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		
		sb.append(" select M");
		sb.append(" from TsmpRoleTxidMap M");
		sb.append(" where 1 = 1");
		// 分頁
		if (!StringUtils.isEmpty(p_roleId) && !StringUtils.isEmpty(p_listType)) {
			sb.append(" and (1 = 2");
			sb.append(" 	or M.roleId > :p_roleId");
			sb.append(" 	or (M.roleId = :p_roleId and M.listType > :p_listType)");
			sb.append(" )");
			params.put("p_roleId", p_roleId);
			params.put("p_listType", p_listType);
		}
		// 名單類型
		if (!StringUtils.isEmpty(listType)) {
			sb.append(" and M.listType = :listType");
			params.put("listType", listType);
		}
		// 關鍵字
		if (keywords != null && keywords.length > 0) {
			sb.append(" and (1 = 2");
			for (int i = 0; i < keywords.length; i++) {
				sb.append(" 	or UPPER(M.txid) like :keyword" + i);
				params.put("keyword" + i, "%" + keywords[i].toUpperCase() + "%");
			}
			sb.append(" 	or exists (");
			sb.append(" 		select 1");
			sb.append(" 		from TsmpRole R");
			sb.append(" 		where R.roleId = M.roleId");
			sb.append(" 		and (1 = 2");
			for (int i = 0; i < keywords.length; i++) {
				sb.append(" 			or UPPER(R.roleName) like :keyword" + i);
				sb.append(" 			or UPPER(R.roleAlias) like :keyword" + i);
			}
			sb.append(" 		)");
			sb.append(" 	)");
			sb.append(" )");
		}
		// 分頁排序: roleId asc, listType asc
		sb.append(" order by M.roleId asc, M.listType asc, M.roleTxidMapId");
		
		return doQuery(sb.toString(), params, TsmpRoleTxidMap.class);
	}

}