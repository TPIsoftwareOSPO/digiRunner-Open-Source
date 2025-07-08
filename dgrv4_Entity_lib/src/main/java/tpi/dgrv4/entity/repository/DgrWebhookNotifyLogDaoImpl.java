package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.DgrWebhookNotifyLog;

public class DgrWebhookNotifyLogDaoImpl extends BaseDao {
	
	public List<DgrWebhookNotifyLog> query_dpb0285Service(Date startDate, Date endDate, DgrWebhookNotifyLog lastRecord, 
			String[] words, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();

	    StringBuilder sb = new StringBuilder();
	    sb.append(" SELECT w ");
	    sb.append(" FROM DgrWebhookNotifyLog w ");
		sb.append(" WHERE 1 = 1  ");
	    
	    if (lastRecord !=null) {
	        sb.append(" AND ");
	        sb.append(" ( ");
	        sb.append("    1 = 2 ");
	        sb.append("    OR (w.createDateTime < :lastCdt and w.webhookNotifyLogId <> :lastId)");
			sb.append("    OR (w.createDateTime = :lastCdt and w.webhookNotifyLogId < :lastId)");
	        sb.append(" ) ");
	        params.put("lastId", lastRecord.getWebhookNotifyLogId());
	        params.put("lastCdt", lastRecord.getCreateDateTime());
	    }

	    // 必要條件 (createDateTime 或 updateDateTime 在日期範圍內)
 		sb.append(" AND ( 1 = 2");
 		sb.append(" 	OR (w.updateDateTime >= :startDate AND w.updateDateTime < :endDate)");
 		sb.append(" 	OR (w.createDateTime >= :startDate AND w.createDateTime < :endDate)");
 		sb.append(" )");
 		params.put("startDate", startDate);
 		params.put("endDate", endDate); 		
 		
	    if (words != null && words.length > 0) {
	        sb.append(" AND ( 1 = 2 ");
	        for (int i = 0; i < words.length; i++) {
	        	sb.append(" OR UPPER(w.remark) like :keyworkSearch" + i);
	        	sb.append(" OR UPPER(w.clientId) like :keyworkSearch" + i);
	        	sb.append("  	OR EXISTS(");
				sb.append(" 		SELECT 1 FROM DgrWebhookNotify n");
				sb.append(" 		WHERE n.webhookNotifyId = w.webhookNotifyId");
				sb.append(" 		AND (1 = 2");
				sb.append("    		OR UPPER(n.notifyName) LIKE :keyworkSearch" + i);
				sb.append(" 		)");
				sb.append(" 	)");
				params.put("keyworkSearch" + i, "%" + words[i].toUpperCase() + "%");
	        }
	        sb.append(" ) ");
	    }
	    sb.append(" ORDER BY w.createDateTime DESC, w.webhookNotifyLogId DESC ");

	    return doQuery(sb.toString(), params, DgrWebhookNotifyLog.class, pageSize);
	}
}
