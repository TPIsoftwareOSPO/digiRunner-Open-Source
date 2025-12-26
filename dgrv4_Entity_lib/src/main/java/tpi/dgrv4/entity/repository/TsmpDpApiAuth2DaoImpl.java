package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import tpi.dgrv4.entity.entity.jpql.TsmpDpApiAuth2;

public class TsmpDpApiAuth2DaoImpl extends BaseDao {
	// add custom methods here

	public List<TsmpDpApiAuth2> query_dpb0001Service_01(//
			List<String> orgDescList, String applyStatus, String[] words, Long lastId, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append("select A from TsmpDpApiAuth2 A");
		sb.append(" where 1 = 1");
		sb.append(" and A.applyStatus = :applyStatus");
		// 分頁
		if (lastId != null) {
			sb.append(" and A.apiAuthId > :apiAuthId");
			params.put("apiAuthId", lastId);
		}
		// 組織
		sb.append(" and exists (");
		sb.append("     select 1");
		sb.append("     from TsmpApi D");
		sb.append("     where D.apiUid = A.refApiUid");
		sb.append("     and ( 1 = 2");
		sb.append("         or D.orgId is null");
		sb.append("         or length(D.orgId) = 0");
		if (!CollectionUtils.isEmpty(orgDescList)) {
			sb.append("         or D.orgId in (:orgDescList)");
			params.put("orgDescList", orgDescList);
		}
		sb.append("     )");
		sb.append(" )");
		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for (int i = 0; i < words.length; i++) {
				sb.append(" 	or UPPER(A.applyPurpose) like :keyword" + i);
				params.put("keyword" + i, "%" + words[i].toUpperCase() + "%");
			}
			// 申請人
			sb.append(" 	or exists (");
			sb.append(" 		select 1 from TsmpClient B");
			sb.append(" 		where B.clientId = A.refClientId");
			sb.append(" 		and ( 1 = 2");
			for (int i = 0; i < words.length; i++) {
				sb.append(" 			or UPPER(B.clientName) like :keyword" + i);
			}
			sb.append(" 		)");
			sb.append(" 	)");
			// API名稱, 模組名稱
			sb.append(" 	or exists (");
			sb.append(" 		select 1 from TsmpApi C");
			sb.append(" 		where C.apiUid = A.refApiUid");
			// 組織
			sb.append("         and (C.orgId is null or length(C.orgId) = 0");
			if (!CollectionUtils.isEmpty(orgDescList)) {
				sb.append("         or C.orgId in (:orgDescList)");
			}
			sb.append("         )");
			sb.append(" 		and ( 1 = 2");
			for (int i = 0; i < words.length; i++) {
				sb.append(" 			or UPPER(C.apiName) like :keyword" + i);
				sb.append(" 			or UPPER(C.moduleName) like :keyword" + i);
			}
			sb.append(" 		)");
			sb.append(" 	)");
			sb.append(" )");
		}
		sb.append(" order by A.apiAuthId asc");

		params.put("applyStatus", applyStatus);
		
		return doQuery(sb.toString(), params, TsmpDpApiAuth2.class, pageSize);
	}

	public List<TsmpDpApiAuth2> query_dpb0003Service_01(//
			List<String> orgDescList, List<String> applyStatus, String[] words, TsmpDpApiAuth2 lastRecord, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append("select A from TsmpDpApiAuth2 A");
		sb.append(" where 1 = 1");
		sb.append(" and A.applyStatus in (:applyStatus)");

		// 分頁
		if (lastRecord != null) {
			sb.append(" and (");
			sb.append(" 	A.updateDateTime < :updateDateTime");
			sb.append(" 	or (A.updateDateTime = :updateDateTime and A.apiAuthId > :apiAuthId)");
			sb.append(" )");
			params.put("updateDateTime", lastRecord.getUpdateDateTime());
			params.put("apiAuthId", lastRecord.getApiAuthId());
		}

		// 組織
		sb.append(" and exists (");
		sb.append("     select 1");
		sb.append("     from TsmpApi E");
		sb.append("     where E.apiUid = A.refApiUid");
		sb.append("     and ( 1 = 2");
		sb.append("         or E.orgId is null");
		sb.append("         or length(E.orgId) = 0");
		if (!CollectionUtils.isEmpty(orgDescList)) {
			sb.append("         or E.orgId in (:orgDescList)");
			params.put("orgDescList", orgDescList);
		}
		sb.append("     )");
		sb.append(" )");

		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for (int i = 0; i < words.length; i++) {
				sb.append(" 	or UPPER(A.keywordSearch) like :lk_keyword" + i);	// 申請用途說明、審核備註
				sb.append(" 	or A.applyStatus = :keyword" + i);		// 申請狀態
				sb.append(" 	or A.refReviewUser = :keyword" + i);	// 審核人員
				params.put("lk_keyword" + i, "%" + words[i].toUpperCase() + "%");
				params.put("keyword" + i, words[i]);
			}
			sb.append(" 	or exists (");
			sb.append(" 		select 1 from TsmpClient B");
			sb.append(" 		where B.clientId = A.refClientId");
			sb.append(" 		and ( 1 = 2");
			for (int i = 0; i < words.length; i++) {
				sb.append(" 			or UPPER(B.clientName) like :lk_keyword" + i);	// 申請人
			}
			sb.append(" 		)");
			sb.append(" 	)");
			sb.append(" 	or exists (");
			sb.append(" 		select 1 from TsmpApi C");
			sb.append(" 		where C.apiUid = A.refApiUid");
			// 組織
			sb.append("         and (C.orgId is null or length(C.orgId) = 0");
			if (!CollectionUtils.isEmpty(orgDescList)) {
				sb.append("         or C.orgId in (:orgDescList)");
			}
			sb.append("         )");
			sb.append(" 		and ( 1 = 2");
			for (int i = 0; i < words.length; i++) {
				sb.append(" 			or UPPER(C.apiName) like :lk_keyword" + i);		// api名稱
				sb.append(" 			or UPPER(C.moduleName) like :lk_keyword" + i);	// 模組名稱
			}
			sb.append(" 		)");
			sb.append(" 	)");
			sb.append(" )");
		}

		sb.append(" order by A.updateDateTime desc, A.apiAuthId asc");

		params.put("applyStatus", applyStatus);
		
		return doQuery(sb.toString(), params, TsmpDpApiAuth2.class, pageSize);
	}

}
