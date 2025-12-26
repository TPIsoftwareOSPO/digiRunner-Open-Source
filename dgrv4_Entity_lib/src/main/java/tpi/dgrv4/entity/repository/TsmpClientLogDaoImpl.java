package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.entity.jpql.TsmpClientLog;
	
public class TsmpClientLogDaoImpl extends BaseDao {
	// add custom methods here

	public List<TsmpClientLog> findByLogSeqAndEventTypeAndStartTimeAndEndTimeAndKeyword(TsmpClientLog tsmpClientLog,String eventType, String queryStartTime,String queryEndTime,String[] keyword, int pageSize){
		Map<String, Object> params = new HashMap<>();

		Optional<Date> opt_startDate = DateTimeUtil.stringToDateTime(queryStartTime, DateTimeFormatEnum.西元年月日時分);
		if(!opt_startDate.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}
		
		Optional<Date> opt_endDate = DateTimeUtil.stringToDateTime(queryEndTime, DateTimeFormatEnum.西元年月日時分);
		if(!opt_endDate.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}
		
		
		
		Date startDate = opt_startDate.get();
		Date endDate = opt_endDate.get();
		
		
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT tsmpClientLog ");
		sb.append(" FROM TsmpClientLog tsmpClientLog ");
		sb.append(" WHERE 1 = 1 ");
		
		// 分頁
		if (tsmpClientLog != null) {
			sb.append(" AND ");
			sb.append(" ( ");
			sb.append(" 	1 = 2 ");
			sb.append(" 	OR (tsmpClientLog.eventTime < :lastEventTime AND tsmpClientLog.logSeq <> :lastLogSeq) ");
			sb.append(" 	OR (tsmpClientLog.eventTime = :lastEventTime AND tsmpClientLog.logSeq < :lastLogSeq) ");
			sb.append(" ) ");
			params.put("lastEventTime", tsmpClientLog.getEventTime());
			params.put("lastLogSeq", tsmpClientLog.getLogSeq());
		}
		
		//事件類型
		if (StringUtils.isEmpty(eventType)==false) {			
			sb.append(" AND tsmpClientLog.eventType = :eventType ");
			params.put("eventType", eventType);	
		}

		//事件日期查詢
		if(startDate!=null && endDate!=null) {

			sb.append(" AND tsmpClientLog.eventTime >= :startDate ");
			sb.append(" AND tsmpClientLog.eventTime <= :endDate   ");
			
			params.put("startDate", startDate);
			params.put("endDate", endDate);	
		}
		
		// 關鍵字
		if (keyword != null && keyword.length > 0) {
			sb.append(" AND ( 1 = 2 ");
			
			for (int i = 0; i < keyword.length; i++) {
				
				//Log序號
				sb.append(" OR tsmpClientLog.logSeq = :keyworkSearchLogSeq"+i);
				params.put("keyworkSearchLogSeq"+ i, keyword[i] );
				
				//事件訊息、用戶端帳號、用戶端IP、使用者帳號
				sb.append(" OR UPPER(tsmpClientLog.eventMsg) like :keyworkSearch"+i);
				sb.append(" OR UPPER(tsmpClientLog.clientId) like :keyworkSearch"+i);
				sb.append(" OR UPPER(tsmpClientLog.clientIp) like :keyworkSearch"+i);
				sb.append(" OR UPPER(tsmpClientLog.userName) like :keyworkSearch"+i);
				params.put("keyworkSearch"+i,"%" + keyword[i].toUpperCase()+ "%" );
				
				//前端交易序號
				sb.append(" OR UPPER(tsmpClientLog.txsn) like :keyworkSearchTxsn"+i); 
				params.put("keyworkSearchTxsn"+ i, keyword[i] );
			}	
			sb.append("		) ");
		}
		
		sb.append(" ORDER BY tsmpClientLog.eventTime DESC, tsmpClientLog.logSeq DESC ");
		
		return doQuery(sb.toString(), params, TsmpClientLog.class, pageSize);
	}
}
