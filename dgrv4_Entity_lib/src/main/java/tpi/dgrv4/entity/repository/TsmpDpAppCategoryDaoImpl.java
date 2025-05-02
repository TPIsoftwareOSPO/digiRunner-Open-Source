package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import tpi.dgrv4.entity.entity.sql.TsmpDpAppCategory;

public class TsmpDpAppCategoryDaoImpl extends BaseDao {
	// add custom methods here

	public List<TsmpDpAppCategory> queryLikeAndSort(List<String> orgDescList, String[] words //
			, TsmpDpAppCategory lastRecord, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpDpAppCategory A where 1 = 1");
		// 分頁
		if (lastRecord != null) {
			sb.append(" and (");
			sb.append(" 	A.dataSort > :dataSort");
			if (lastRecord.getDataSort() == null) {
				sb.append(" 	or (A.dataSort is null and A.createDateTime < :createDateTime and A.appCateId <> :appCateId)");
//				sb.append(" 	or (A.dataSort is null and A.createDateTime = :createDateTime and A.appCateId > :appCateId)");
				sb.append(" 	or (A.dataSort is null and A.createDateTime = :createDateTime and A.appCateId < :appCateId)");
			} else {
				sb.append(" 	or (A.dataSort = :dataSort and A.createDateTime < :createDateTime and A.appCateId <> :appCateId)");
//				sb.append(" 	or (A.dataSort = :dataSort and A.createDateTime = :createDateTime and A.appCateId > :appCateId)");
				sb.append(" 	or (A.dataSort = :dataSort and A.createDateTime = :createDateTime and A.appCateId < :appCateId)");
			}
			sb.append(" )");
			params.put("dataSort", lastRecord.getDataSort());
			params.put("createDateTime", lastRecord.getCreateDateTime());
			params.put("appCateId", lastRecord.getAppCateId());
		}
		// 組織
		sb.append(" and (A.orgId is null or LENGTH(A.orgId) = 0");
		if (!CollectionUtils.isEmpty(orgDescList)) {
			sb.append(" or A.orgId in (:orgDescList)");
			params.put("orgDescList", orgDescList);
		}
		sb.append(" )");
		
		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 	or UPPER(A.keywordSearch) like :keyword" + i);
				params.put("keyword" + i, "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" )");
		}
//		sb.append(" order by A.dataSort asc, A.createDateTime desc, A.appCateId asc");
		sb.append(" order by A.dataSort asc, A.createDateTime desc, A.appCateId desc");
		
		return doQuery(sb.toString(), params, TsmpDpAppCategory.class, pageSize);
	}

}
