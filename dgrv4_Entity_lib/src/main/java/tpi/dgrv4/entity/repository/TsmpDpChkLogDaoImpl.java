package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.jpql.TsmpDpChkLog;

public class TsmpDpChkLogDaoImpl extends BaseDao {
	// add custom methods here
	
	public List<TsmpDpChkLog> queryHistoryByPk(TsmpDpChkLog lastRecord, Long reqOrdermId, int pageSize) {
		
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpDpChkLog A where 1 = 1");

		// 分頁
		// 是否有傳入 last row id
		if (lastRecord != null) {
			sb.append(" and ( 1 = 2");
			sb.append("    or (A.createDateTime > :lastCreateDateTime and A.chkLogId <> :lastChkLogId)");
			sb.append("    or (A.createDateTime = :lastCreateDateTime and A.chkLogId > :lastChkLogId)");
			sb.append(" )");			
			params.put("lastCreateDateTime", lastRecord.getCreateDateTime());
			params.put("lastChkLogId", lastRecord.getChkLogId());
		}
		
		sb.append(" and A.reqOrdermId = :reqOrdermId");
		params.put("reqOrdermId", reqOrdermId);
		
		sb.append(" order by A.createDateTime asc, A.chkLogId asc");
				
		return doQuery(sb.toString(), params, TsmpDpChkLog.class, pageSize);
	}

}
