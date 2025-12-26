package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.jpql.TsmpNodeTaskWork;

public class TsmpNodeTaskWorkDaoImpl extends BaseDao {
	// add custom methods here
	
	public List<TsmpNodeTaskWork> queryTaskStatus(TsmpNodeTaskWork lastRecord, String[] words, Long nodeTaskId, 
			Boolean isSuccess, int pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append("select A from TsmpNodeTaskWork A where 1 = 1");

		// 是否有傳入 last row id
		// 分頁
		if (lastRecord != null) {
			sb.append(" and ( 1 = 2");
			sb.append("    or (A.competitiveId > :lastCompetitiveId)");
			sb.append("    or (A.competitiveId = :lastCompetitiveId and A.id > :lastId)");
			sb.append(" )");			
			params.put("lastCompetitiveId", lastRecord.getCompetitiveId());
			params.put("lastId", lastRecord.getId());
		}

		// 必要條件
		sb.append(" and A.nodeTaskId = :nodeTaskId");//任務序號
		params.put("nodeTaskId", nodeTaskId);

		// 任務成功與否
		if (isSuccess != null) {
			sb.append(" and ( 1 = 2");
			sb.append("    or (A.success = :isSuccess)");
			sb.append(" )");			
			params.put("isSuccess", isSuccess);
		}
 
		// 關鍵字search [ competitive_id / node / error_msg ]
		if (words != null && words.length > 0) {
			sb.append(" 	and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append("    or UPPER(A.competitiveId) like :word" + i);//忽略大小寫
				sb.append("    or UPPER(A.node) like :word" + i);//忽略大小寫
				sb.append("    or UPPER(A.errorMsg) like :word" + i);//忽略大小寫
				params.put("word" + i, "%" + words[i].toUpperCase() + "%");//忽略大小寫
			}
			sb.append(" )");
		} 
		sb.append(" order by A.competitiveId asc, A.id asc");

		
		return doQuery(sb.toString(), params, TsmpNodeTaskWork.class, pageSize);
	}

}
