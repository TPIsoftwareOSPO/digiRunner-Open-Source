package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.sql.TsmpDpFaqQuestion;

public class TsmpDpFaqQuestionDaoImpl extends BaseDao {
	// add custom methods here

	public List<TsmpDpFaqQuestion> query_dpb0027Service(List<String> dataStatusList //
			, String[] words, TsmpDpFaqQuestion lastRecord, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpDpFaqQuestion A");
		sb.append(" where A.dataStatus in (:dataStatusList)");
		// 分頁
		if (lastRecord != null) {
			sb.append(" and (");
			sb.append(" 	A.dataSort > :dataSort");
			if (lastRecord.getDataSort() == null) {
				sb.append(" 	or (A.dataSort is null and A.questionId > :questionId)");
			} else {
				sb.append(" 	or (A.dataSort = :dataSort and A.questionId > :questionId)");
			}
			sb.append(" )");
			params.put("dataSort", lastRecord.getDataSort());
			params.put("questionId", lastRecord.getQuestionId());
		}
		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 	or UPPER(A.keywordSearch) like :lk_keyword" + i);
				params.put("lk_keyword" + i, "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" 	or exists (");
			sb.append(" 		select 1 from TsmpDpFaqAnswer B");
			sb.append(" 		where B.refQuestionId = A.questionId");
			sb.append(" 		and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 			or UPPER(B.keywordSearch) like :lk_keyword" + i);	
			}
			sb.append(" 		)");
			sb.append(" 	)");
			sb.append(" )");
		}
		sb.append(" order by A.dataSort asc, A.questionId asc");

		params.put("dataStatusList", dataStatusList);

		return doQuery(sb.toString(), params, TsmpDpFaqQuestion.class, pageSize);
	}

}
