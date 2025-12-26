package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.jpql.TsmpReqResLogHistory;

public class TsmpReqResLogHistoryDaoImpl extends BaseDao {
	public List<TsmpReqResLogHistory> queryByDashboard(Date startDate, Date endDate, String id, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		sb.append(" SELECT new tpi.dgrv4.entity.entity.jpql.TsmpReqResLogHistory(");
		sb.append("	id, rtime, moduleName, txid, cid, exeStatus, elapse, httpStatus");
		sb.append(" ) FROM TsmpReqResLogHistory");
		sb.append(" WHERE 1=1 ");
		if(StringUtils.hasText(id)) {
			sb.append(" AND ( 1 = 2");
			sb.append(" OR (rtime > :startDate AND rtime <= :endDate)");
			sb.append(" OR (rtime = :startDate AND id > :id)");
			sb.append(" )");
			
			params.put("startDate", startDate);
			params.put("endDate", endDate);
			params.put("id", id);
		}else {
			sb.append(" AND rtime >= :startDate");
			sb.append(" AND rtime <= :endDate");
			
			params.put("startDate", startDate);
			params.put("endDate", endDate);
		}
		

		sb.append(" ORDER BY rtime ASC, id ASC");

		
		return doQuery(sb.toString(), params, TsmpReqResLogHistory.class, pageSize);
	}
	
	public List<TsmpReqResLogHistory> queryByDashboard2(Date startDate, Date endDate, String id, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		sb.append(" SELECT *");
		//sb.append("	id, rtime, module_Name, txid, cid, exe_Status, elapse, http_Status");//testcase會過,但在dev環境會錯,似乎要全mapping欄位
		sb.append("  FROM Tsmp_Req_Res_Log_History");
		sb.append(" WHERE 1=1 ");
		if(StringUtils.hasText(id)) {
			sb.append(" AND ( 1 = 2");
			sb.append(" OR (rtime > :startDate AND rtime <= :endDate)");
			sb.append(" OR (rtime = :startDate AND id > :id)");
			sb.append(" )");
			
			params.put("startDate", startDate);
			params.put("endDate", endDate);
			params.put("id", id);
		}else {
			sb.append(" AND rtime >= :startDate");
			sb.append(" AND rtime <= :endDate");
			
			params.put("startDate", startDate);
			params.put("endDate", endDate);
		}
		

		sb.append(" ORDER BY rtime ASC, id ASC");

		
		return doNativeQuery(sb.toString(), null, params, pageSize, TsmpReqResLogHistory.class);
	}
	
	public List<Map> queryByClientUsageMetrics(){
		StringBuffer sb = new StringBuffer();

		sb.append(" SELECT cid, ");
		sb.append(" 	   module_name, ");
		sb.append(" 	   txid, ");
		sb.append(" 	   exe_status, ");
		sb.append(" 	   sum(elapse) elapse, ");
		sb.append(" 	   count(1) frequency ");
		sb.append(" FROM tsmp_req_res_log_history ");
		sb.append(" GROUP BY cid, module_name, txid, exe_status ");
		sb.append(" ORDER BY cid, module_name, txid, exe_status ");
		
		return doNativeQuery(sb.toString(), null, Map.class);
	}
	
	public List<Map> queryByBadAttempt(){
		StringBuffer sb = new StringBuffer();

		sb.append(" SELECT http_status, ");
		sb.append(" 	   count(1) frequency ");
		sb.append(" FROM tsmp_req_res_log_history ");
		sb.append(" WHERE http_status >= 400");
		sb.append(" OR http_status = 0");
		sb.append(" GROUP BY http_status ");
		sb.append(" ORDER BY http_status ");
		
		return doNativeQuery(sb.toString(), null, Map.class);

	}
	
	public List<Map> queryByMedian(){
		StringBuffer sb = new StringBuffer();

		sb.append(" SELECT max(elapse) max_val, ");
		sb.append("        min(elapse) min_val, ");
		sb.append(" 	   count(1) frequency, ");
		sb.append(" 	   sum(elapse) total ");
		sb.append(" FROM tsmp_req_res_log_history ");
		sb.append(" WHERE exe_status = 'Y'");
		
		return doNativeQuery(sb.toString(), null, Map.class);

	}
	
	public List<TsmpReqResLogHistory> queryByMedian(Date startDate, Date endDate, Integer pageSize, Integer firstResult){
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		sb.append(" SELECT h ");
		sb.append(" FROM TsmpReqResLogHistory h");
		sb.append(" WHERE 1=1 ");
		if(startDate != null) {
			sb.append(" AND rtime >= :startDate");
			params.put("startDate", startDate);
		}
		if(endDate != null) {
			sb.append(" AND rtime <= :endDate");
			params.put("endDate", endDate);
		}
		sb.append(" AND exeStatus = 'Y'");
		
		sb.append(" ORDER BY elapse ASC");

		return doQuery(sb.toString(), params, TsmpReqResLogHistory.class, pageSize, firstResult);

	}
	
	public List<Map> queryByApiTrafficDistribution(){
		StringBuffer sb = new StringBuffer();

		sb.append(" SELECT rtime_year_month, ");
		sb.append("        exe_status, ");
		sb.append(" 	   count(1) frequency ");
		sb.append(" FROM tsmp_req_res_log_history ");
		sb.append(" GROUP BY rtime_year_month, exe_status ");
		sb.append(" ORDER BY rtime_year_month, exe_status ");
		
		return doNativeQuery(sb.toString(), null, Map.class);

	}
}
