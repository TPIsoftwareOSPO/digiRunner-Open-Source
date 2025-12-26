package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import tpi.dgrv4.entity.entity.sql.TsmpDpApp;

public class TsmpDpAppDaoImpl extends BaseDao {
	// add custom methods here

	public List<TsmpDpApp> queryLikeByDataStatus(List<String> orgDescList //
			, List<String> dataStatusList, String[] words, Long lastId, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpDpApp A");
		sb.append(" where 1 = 1");
		sb.append(" and A.dataStatus in (:dataStatusList)");
		// 分頁
		if (lastId != null) {
			sb.append(" and A.appId > :appId");
			params.put("appId", lastId);
		}
		// 組織
		sb.append(" and (A.orgId is null or length(A.orgId) = 0");
		if (!CollectionUtils.isEmpty(orgDescList)) {
			sb.append(" or A.orgId in (:orgDescList)");
			params.put("orgDescList", orgDescList);
		}
		sb.append(" )");

		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 	or UPPER(A.name) like :lk_keyword" + i);
				params.put("lk_keyword" + i, "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" 	or exists (");
			sb.append(" 		select 1 from TsmpDpAppCategory B");
			sb.append(" 		where B.appCateId = A.refAppCateId");
			sb.append(" 		and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 			or UPPER(B.appCateName) like :lk_keyword" + i);
			}
			sb.append(" 		)");
			sb.append(" 	)");
			sb.append(" )");
		}
		sb.append(" order by A.appId asc");
		
		params.put("dataStatusList", dataStatusList);

		return doQuery(sb.toString(), params, TsmpDpApp.class, pageSize);
	}
}
