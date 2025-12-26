package tpi.dgrv4.entity.repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.entity.DgrAuditLogM;

public class DgrAuditLogMDaoImpl extends BaseDao {

	public List<DgrAuditLogM> query_LoginLogoutReport(Date st, Date et, DgrAuditLogM lastRecord, int pageSize) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

    	sb.append(" SELECT m");
    	sb.append(" FROM DgrAuditLogM m");
    	sb.append(" WHERE 1 = 1");
    	if (lastRecord != null) {
    		sb.append(" AND ( 1 = 2");
    		sb.append(" 	OR m.userName > :lastUserName");
    		sb.append(" 	OR (m.userName = :lastUserName AND m.createDateTime > :lastCreateDateTime)");
    		sb.append(" 	OR (m.userName = :lastUserName AND m.createDateTime = :lastCreateDateTime AND m.auditLongId > :lastId)");
    		sb.append(" )");
    		params.put("lastUserName", lastRecord.getUserName());
    		params.put("lastCreateDateTime", lastRecord.getCreateDateTime());
    		params.put("lastId", lastRecord.getAuditLongId());
    	}
    	sb.append(" AND ( 1 = 2");
    	sb.append(" 	OR (m.eventNo = 'login' AND m.param1 = 'SUCCESS')");
    	sb.append(" 	OR (m.eventNo = 'logout')");
    	sb.append(" )");
    	sb.append(" AND m.createDateTime >= :st");
    	sb.append(" AND m.createDateTime < :et");
    	sb.append(" ORDER BY m.userName ASC, m.createDateTime ASC, m.auditLongId ASC");
    	
    	params.put("st", st);
    	params.put("et", et);
    	
    	return doQuery(sb.toString(), params, DgrAuditLogM.class, pageSize);
	}

	public List<DgrAuditLogM> query_IDManagementReport(Date st, Date et, DgrAuditLogM lastRecord, int pageSize) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		
		sb.append(" SELECT m");
		sb.append(" FROM DgrAuditLogM m");
		sb.append(" WHERE 1 = 1");
		if (lastRecord != null) {
			sb.append(" AND ( 1 = 2");
			sb.append(" 	OR m.createDateTime > :lastCreateDateTime");
			sb.append(" 	OR (m.createDateTime = :lastCreateDateTime AND m.auditLongId > :lastAuditLongId)");
			sb.append(" )");
			params.put("lastCreateDateTime", lastRecord.getCreateDateTime());
			params.put("lastAuditLongId", lastRecord.getAuditLongId());
		}
		sb.append(" AND m.createDateTime >= :st");
		sb.append(" AND m.createDateTime < :et");
		sb.append(" AND ( 1 = 2");
		sb.append(" 	OR (");
		sb.append(" 		(m.eventNo = 'addUser' OR m.eventNo = 'deleteUser')");
		sb.append(" 		AND EXISTS (");
		sb.append(" 			SELECT 1");
		sb.append(" 			FROM DgrAuditLogD d");
		sb.append(" 			WHERE m.txnUid = d.txnUid");
		sb.append(" 			AND d.entityName = 'TsmpUser'");
		sb.append(" 			AND (d.cud = 'C' OR d.cud = 'D')");
		sb.append(" 		)");
		sb.append(" 	)");
		sb.append(" 	OR (");
		sb.append(" 		(m.eventNo = 'updateUser' OR m.eventNo = 'updateUserProfile')");
		sb.append(" 		AND EXISTS (");
		sb.append(" 			SELECT 1");
		sb.append(" 			FROM DgrAuditLogD d");
		sb.append(" 			WHERE m.txnUid = d.txnUid");
		sb.append(" 			AND (d.entityName = 'TsmpUser' OR d.entityName = 'Users' OR d.entityName = 'Authorities')");
		sb.append(" 			AND (d.cud = 'C' OR d.cud = 'U')");
		sb.append(" 		)");	
		sb.append(" 	)"); 
		sb.append(" )");
		sb.append(" ORDER BY m.createDateTime ASC, m.auditLongId ASC");
		
		params.put("st", st);
		params.put("et", et);
		
		return doQuery(sb.toString(), params, DgrAuditLogM.class, pageSize);
	}

	public List<DgrAuditLogM> queryByAuditLogIdAndStartDateAndEndDateAndKeyword(DgrAuditLogM lastDgrAuditLogM,
			String queryStartDate, String queryEndDate, String[] keyword, String locale, int pageSize) {
		Map<String, Object> params = new HashMap<>();

		Optional<LocalDate> opt_startDate = DateTimeUtil.stringToLocalDate(queryStartDate, DateTimeFormatEnum.西元年月日_2);
		Optional<LocalDate> opt_endDate = DateTimeUtil.stringToLocalDate(queryEndDate, DateTimeFormatEnum.西元年月日_2);
		if (!(opt_startDate.isPresent() && opt_endDate.isPresent())) {
			String actual = opt_startDate.isPresent() ? queryEndDate : queryStartDate;
			logger.error("Expected query date format as " + DateTimeFormatEnum.西元年月日_2 + " but was " + actual + ".");
			return Collections.emptyList();
		}
		LocalDate ld_startDate = opt_startDate.get();
		LocalDate ld_endDate = opt_endDate.get().plusDays(1L);
		Date startDate = Date.from(ld_startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		Date endDate = Date.from(ld_endDate.atStartOfDay().plus(-1,ChronoUnit.SECONDS).atZone(ZoneId.systemDefault()).toInstant());
		
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT dgrAuditLogM ");
		sb.append(" FROM DgrAuditLogM dgrAuditLogM ");
		sb.append(" WHERE 1 = 1 ");

		// 日期查詢
		sb.append(" AND dgrAuditLogM.createDateTime >= :startDate ");
		sb.append(" AND dgrAuditLogM.createDateTime <= :endDate   ");

		params.put("startDate", startDate);
		params.put("endDate", endDate);

		// 分頁
		if (lastDgrAuditLogM != null) {
			sb.append(" AND ");
			sb.append(" ( ");
			sb.append(" 	1 = 2 ");
			sb.append(" 	OR (dgrAuditLogM.createDateTime < :lastDateTime and (dgrAuditLogM.auditExtId <> :lastAuditExtId or dgrAuditLogM.auditLongId <> :lastAuditLongId)) ");
			sb.append(" 	OR (dgrAuditLogM.createDateTime = :lastDateTime and dgrAuditLogM.auditExtId < :lastAuditExtId) ");
			sb.append(" 	OR (dgrAuditLogM.createDateTime = :lastDateTime and dgrAuditLogM.auditExtId = :lastAuditExtId and dgrAuditLogM.auditLongId < :lastAuditLongId) ");
			sb.append(" ) ");
			params.put("lastDateTime", lastDgrAuditLogM.getCreateDateTime());
			params.put("lastAuditExtId", lastDgrAuditLogM.getAuditExtId());
			params.put("lastAuditLongId", lastDgrAuditLogM.getAuditLongId());
		}

		// 關鍵字
		if (keyword != null && keyword.length > 0) {
			sb.append(" AND ( 1 = 2 ");

			for (int i = 0; i < keyword.length; i++) {
				try {
					Long keywordAuditLongId = Long.valueOf(keyword[i]);
					sb.append(" OR dgrAuditLogM.auditLongId = :keywordAuditLongId"+ i);
					params.put("keywordAuditLongId"+ i, keywordAuditLongId );
				}catch (Exception e) {
					
				}				
				sb.append(" OR UPPER(dgrAuditLogM.userName) like :keyworkSearch" + i);
				sb.append(" OR UPPER(dgrAuditLogM.clientId) like :keyworkSearch" + i);
				sb.append(" OR UPPER(dgrAuditLogM.apiUrl) like :keyworkSearch" + i);
				sb.append(" OR UPPER(dgrAuditLogM.userIp) like :keyworkSearch" + i);
				sb.append(" OR UPPER(dgrAuditLogM.userHostname) like :keyworkSearch" + i);
				sb.append(" OR UPPER(dgrAuditLogM.userRole) like :keyworkSearch" + i);
				sb.append(" OR UPPER(dgrAuditLogM.txnUid) like :keyworkSearch" + i);
				sb.append(" OR UPPER(dgrAuditLogM.origApiUrl) like :keyworkSearch" + i);
				sb.append(" OR UPPER(dgrAuditLogM.param1) like :keyworkSearch" + i);
				sb.append(" OR UPPER(dgrAuditLogM.param2) like :keyworkSearch" + i);
				sb.append(" OR UPPER(dgrAuditLogM.param3) like :keyworkSearch" + i);
				sb.append(" OR UPPER(dgrAuditLogM.param4) like :keyworkSearch" + i);
				sb.append(" OR UPPER(dgrAuditLogM.param5) like :keyworkSearch" + i);
				sb.append(" OR UPPER(dgrAuditLogM.stackTrace) like :keyworkSearch" + i);
				params.put("keyworkSearch" + i, "%" + keyword[i].toUpperCase() + "%");
			}
			// API事件編號的中文
			sb.append(" OR EXISTS (");
			sb.append("				SELECT 1");
			sb.append("				FROM TsmpDpItems tsmpDpItems ");
			sb.append("				WHERE tsmpDpItems.itemNo = :itemNo1 ");
			sb.append("					AND tsmpDpItems.subitemNo = dgrAuditLogM.eventNo ");
			sb.append("					AND tsmpDpItems.locale = :locale ");
			sb.append("				 	AND (");
			sb.append("				 		  1=2");
			for (int i = 0; i < keyword.length; i++) {
				sb.append("					  OR UPPER(tsmpDpItems.subitemNo) like :keyworkSearch" + i);
				sb.append("					  OR UPPER(tsmpDpItems.subitemName) like :keyworkSearch" + i);
			}
			sb.append("				 	    )");
			sb.append(" 		  )");
			params.put("itemNo1", "AUDIT_LOG_EVENT");
			params.put("locale", locale);

			sb.append("		) ");
		}

		sb.append(" ORDER BY dgrAuditLogM.createDateTime DESC, dgrAuditLogM.auditExtId DESC, dgrAuditLogM.auditLongId DESC");

		return doQuery(sb.toString(), params, DgrAuditLogM.class, pageSize);
	}
    
}
