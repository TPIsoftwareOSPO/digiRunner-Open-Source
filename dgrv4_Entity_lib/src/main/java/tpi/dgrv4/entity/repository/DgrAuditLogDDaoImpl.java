package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.DgrAuditLogD;

public class DgrAuditLogDDaoImpl extends BaseDao {

	public List<DgrAuditLogD> query_ProfileManagementReport(String entityName, List<String> eventNo, //
			Date st, Date et, DgrAuditLogD lastRecord, int pageSize) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		
		sb.append(" SELECT d");
		sb.append(" FROM DgrAuditLogD d");
		sb.append(" WHERE 1 = 1");
		if (lastRecord != null) {
			sb.append(" AND ( 1 = 2");
			sb.append(" 	OR d.createDateTime > :lastCreateDateTime");
			sb.append(" 	OR (d.createDateTime = :lastCreateDateTime AND d.auditLongId > :lastId)");
			sb.append(" )");
			params.put("lastCreateDateTime", lastRecord.getCreateDateTime());
			params.put("lastId", lastRecord.getAuditLongId());
		}
		sb.append(" AND d.entityName = :entityName");
		sb.append(" AND EXISTS (");
		sb.append(" 	SELECT 1");
		sb.append(" 	FROM DgrAuditLogM m");
		sb.append(" 	WHERE d.txnUid = m.txnUid");
		sb.append(" 	AND m.eventNo IN (:eventNo)");
		sb.append(" 	AND m.createDateTime >= :st");
		sb.append(" 	AND m.createDateTime < :et");
		sb.append(" )");
		sb.append(" ORDER BY d.createDateTime ASC, d.auditLongId ASC");
		
		params.put("entityName", entityName);
		params.put("eventNo", eventNo);
		params.put("st", st);
		params.put("et", et);
		
		return doQuery(sb.toString(), params, DgrAuditLogD.class, pageSize);
	}

}
