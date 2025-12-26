package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.jpql.TsmpnNodeTask;

public class TsmpnNodeTaskDaoImpl extends BaseDao {
	// add custom methods here
	
	public List<TsmpnNodeTask> queryTaskList_1(TsmpnNodeTask lastRecord, String[] words, Date startDate, Date endDate, int pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append("select A from TsmpnNodeTask A where 1 = 1");

		// 是否有傳入 last row id
		// 分頁
		if (lastRecord != null) {
			sb.append(" and ( 1 = 2");
			sb.append("    or (A.executeTime < :lastTime and A.id <> :lastId)");
			sb.append("    or (A.executeTime = :lastTime and A.id > :lastId)");
			sb.append(" )");			
			params.put("lastTime", lastRecord.getExecuteTime());
			params.put("lastId", lastRecord.getId());
		}

		// 必要條件
		sb.append(" and A.executeTime >= :queryStartDate");//時間 >= queryStartDate
		sb.append(" and A.executeTime <= :queryEndDate");//時間< queryEndDate
		params.put("queryStartDate", startDate);
		params.put("queryEndDate", endDate);
 
		//關鍵字search [task_id / task_signature / coordination / notice_node]
		if (words != null && words.length > 0) {
			sb.append(" 	and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append("    or UPPER(A.taskId) like :word" + i);//忽略大小寫
				sb.append("    or UPPER(A.taskSignature) like :word" + i);//忽略大小寫
				sb.append("    or UPPER(A.coordination) like :word" + i);//忽略大小寫
				sb.append("    or UPPER(A.noticeNode) like :word" + i);//忽略大小寫
				params.put("word" + i, "%" + words[i].toUpperCase() + "%");//忽略大小寫
			}
			sb.append(" )");
		} 
		sb.append(" order by A.executeTime desc, A.id asc");

		
		return doQuery(sb.toString(), params, TsmpnNodeTask.class, pageSize);
	}

}
