package tpi.dgrv4.entity.repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.entity.jpql.TsmpEvents;

public class TsmpEventsDaoImpl extends BaseDao {
		
	public List<TsmpEvents> findByEventIdAndStartDateAndEndDateAndKeyword(TsmpEvents lastTsmpEvents, String querystartDate,String queryendDate,String[] keyword, String locale, int pageSize){
		Map<String, Object> params = new HashMap<>();

		Optional<LocalDate> opt_startDate = DateTimeUtil.stringToLocalDate(querystartDate, DateTimeFormatEnum.西元年月日_2);
		if(!opt_startDate.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}
		
		Optional<LocalDate> opt_endDate = DateTimeUtil.stringToLocalDate(queryendDate, DateTimeFormatEnum.西元年月日_2);
		if(!opt_endDate.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}
		
		LocalDate ld_startDate = opt_startDate.get();
		LocalDate ld_endDate = opt_endDate.get().plusDays(1L);//加一天
		Date startDate = Date.from(ld_startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		Date endDate = Date.from(ld_endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
		
		
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT tsmpEvents ");
		sb.append(" FROM TsmpEvents tsmpEvents ");
		sb.append(" WHERE 1 = 1 ");
		
		//日期查詢
		sb.append(" AND tsmpEvents.createDateTime >= :startDate ");
		sb.append(" AND tsmpEvents.createDateTime < :endDate   ");
		
		if(startDate!=null && endDate!=null) {
			params.put("startDate", startDate);
			params.put("endDate", endDate);	
		}
		
		// 分頁
		if (lastTsmpEvents != null) {
			sb.append(" AND ");
			sb.append(" ( ");
			sb.append(" 	1 = 2 ");
			sb.append(" 	OR (tsmpEvents.createDateTime < :lastCreateDateTime AND tsmpEvents.eventId <> :lastEventId) ");
			sb.append(" 	OR (tsmpEvents.createDateTime = :lastCreateDateTime AND tsmpEvents.eventId < :lastEventId) ");
			sb.append(" ) ");
			params.put("lastCreateDateTime", lastTsmpEvents.getCreateDateTime());
			params.put("lastEventId", lastTsmpEvents.getEventId());
		}
		
		// 關鍵字
		if (keyword != null && keyword.length > 0) {
			sb.append(" AND ( 1 = 2 ");
			
			for (int i = 0; i < keyword.length; i++) {
				try {
					Long keywordEventId = Long.valueOf(keyword[i]);
					sb.append(" OR tsmpEvents.eventId = :keywordEventId"+ i);
					params.put("keywordEventId"+ i, keywordEventId );
				}catch (Exception e) {
					
				}
				sb.append(" OR UPPER(tsmpEvents.moduleName) like :keyworkSearch"+i);
				sb.append(" OR UPPER(tsmpEvents.infoMsg) like :keyworkSearch"+i);
				params.put("keyworkSearch"+i,"%" + keyword[i].toUpperCase()+ "%" );
			}	
				//事件類型的中文
				sb.append(" OR EXISTS (");
				sb.append("				SELECT 1");
				sb.append("				FROM TsmpDpItems tsmpDpItems ");
				sb.append("				WHERE tsmpDpItems.itemNo = :itemNo1 ");
				sb.append("					AND tsmpDpItems.subitemNo = tsmpEvents.eventTypeId ");
				sb.append("					AND tsmpDpItems.locale = :locale ");
				sb.append("				 	AND (");
				sb.append("				 		  1=2");
				for (int i = 0; i < keyword.length; i++) {
					sb.append("					  OR UPPER(tsmpDpItems.subitemName) like :keyworkSearch" +i);
				}
				sb.append("				 	    )");
				sb.append(" 		  )");
				params.put("itemNo1", "EVENT_TYPE");
				params.put("locale", locale);
				
				//事件名稱的中文
				sb.append(" OR EXISTS (");
				sb.append("				SELECT 1");
				sb.append("				FROM TsmpDpItems tsmpDpItems ");
				sb.append("				WHERE tsmpDpItems.itemNo = :itemNo2 ");
				sb.append("					AND tsmpDpItems.subitemNo = tsmpEvents.eventNameId ");
				sb.append("					AND tsmpDpItems.locale = :locale ");
				sb.append("				 	AND (");
				sb.append("				 		  1=2");
				for (int i = 0; i < keyword.length; i++) {
					sb.append("				 	  OR UPPER(tsmpDpItems.subitemName) like :keyworkSearch" +i);
				}
				sb.append("				 	    )");
				sb.append(" 		  )");
				params.put("itemNo2", "EVENT_NAME");
				
			sb.append("		) ");
		}
		
		sb.append(" ORDER BY tsmpEvents.createDateTime DESC, tsmpEvents.eventId DESC ");
		
		return doQuery(sb.toString(), params, TsmpEvents.class, pageSize);
	}
}
