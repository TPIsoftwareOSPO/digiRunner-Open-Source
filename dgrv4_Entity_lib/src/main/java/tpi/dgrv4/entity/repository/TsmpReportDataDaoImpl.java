package tpi.dgrv4.entity.repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.util.CollectionUtils;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.ReportDateTimeRangeTypeEnum;
import tpi.dgrv4.common.constant.ReportTypeEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.entity.jpql.TsmpReportData;
import tpi.dgrv4.entity.vo.AA1201ReportDateTimeRange;
	
public class TsmpReportDataDaoImpl extends BaseDao {
	// add custom methods here

	//API使用次數統計
	public List<TsmpReportData> queryByApiUsageStatistics(Date now, String startDate, String endDate, String startHour, String endHour
			   , List<String> orgList, List<String> apiUidList, ReportDateTimeRangeTypeEnum dateTimeRangeType) {

		Optional<LocalDate> opt_startDate = DateTimeUtil.stringToLocalDate(startDate, DateTimeFormatEnum.西元年月日_2);
		if (!opt_startDate.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}

		Optional<LocalDate> opt_endDate = DateTimeUtil.stringToLocalDate(endDate, DateTimeFormatEnum.西元年月日_2);
		if (!opt_endDate.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}
		
		//分的時間條件
		Optional<Date> opt_startDateTime = Optional.empty();
		Optional<Date> opt_endDateTime = Optional.empty();
		if(dateTimeRangeType == ReportDateTimeRangeTypeEnum.MINUTE) {
			opt_startDateTime = DateTimeUtil.stringToDateTime(startDate + " " + startHour + ":00:00.000", DateTimeFormatEnum.西元年月日時分秒毫秒_2);
			if (!opt_startDateTime.isPresent()) {
				throw TsmpDpAaRtnCode._1381.throwing();
			}
			
			opt_endDateTime = DateTimeUtil.stringToDateTime(endDate + " " + endHour + ":59:59.999", DateTimeFormatEnum.西元年月日時分秒毫秒_2);
			if (!opt_endDateTime.isPresent()) {
				throw TsmpDpAaRtnCode._1383.throwing();
			}
		}

		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		//<===每張統計表的SELECT欄位與GROUP BY欄位都不一樣
		sb.append(" SELECT r.stringGroup1, ");
		sb.append(" 	   r.stringGroup2, ");
		sb.append(" 	   r.stringGroup3, ");
		sb.append(" 	   SUM(r.intValue1) ");
		
		//每張統計表都一樣
		sb.append(" FROM TsmpReportData r ");
		sb.append(" WHERE 1 = 1 ");
		sb.append(" AND r.reportType = :reportType ");
		// 組織原則
		sb.append(" AND (r.orgid IS NULL OR LENGTH(r.orgid) = 0");
		if (!CollectionUtils.isEmpty(orgList)) {
			sb.append(" OR r.orgid IN (:orgList)");
			params.put("orgList", orgList);
		}
		sb.append(" )");

		if (apiUidList != null && apiUidList.size() > 0) {
			sb.append(" AND EXISTS ( ");
			sb.append("              SELECT 1");
			sb.append("              FROM TsmpApi api");
			sb.append("              WHERE api.moduleName = r.stringGroup1 ");
			sb.append("              AND api.apiKey = r.stringGroup2 ");
			sb.append("              AND api.apiUid IN (:apiUidList) ");
			sb.append("             )");
			params.put("apiUidList", apiUidList);
		}

		sb.append(" AND ( 1=2");

		// <=== 組合天與月條件
		TsmpReportDataDaoConsumer aa1201ReportDateTimeRangeConsumer= new TsmpReportDataDaoConsumer();
		AA1201ReportDateTimeRange aa1201ReportDateTimeRange=new AA1201ReportDateTimeRange(); 
		aa1201ReportDateTimeRange.setNow(now);
		aa1201ReportDateTimeRange.setDateTimeRangeType(dateTimeRangeType);
		aa1201ReportDateTimeRange.setOpt_startDate(opt_startDate);
		aa1201ReportDateTimeRange.setOpt_endDate(opt_endDate);
		aa1201ReportDateTimeRange.setParams(params);
		aa1201ReportDateTimeRange.setSb(sb);
		aa1201ReportDateTimeRange.setOpt_startDateTime(opt_startDateTime);
		aa1201ReportDateTimeRange.setOpt_endDateTime(opt_endDateTime);
		aa1201ReportDateTimeRangeConsumer.consumer.accept(aa1201ReportDateTimeRange);
		// ===> 組合天與月條件
		
		sb.append("     ) ");
		sb.append(" GROUP BY r.stringGroup1, ");
		sb.append("          r.stringGroup2, ");
		sb.append("          r.stringGroup3 ");
		sb.append(" ORDER BY r.stringGroup1, ");
		sb.append("          r.stringGroup2, ");
		sb.append("          r.stringGroup3 ");

		params.put("reportType", ReportTypeEnum.API使用次數統計.value());

		List<Object[]> obj = doQuery(sb.toString(), params, Object[].class);

		List<TsmpReportData> data = new ArrayList<TsmpReportData>();
		for (Object[] o : obj) {
			TsmpReportData tsmpReportData = new TsmpReportData();
			tsmpReportData.setStringGroup1(o[0] == null ? "" : o[0].toString());
			tsmpReportData.setStringGroup2(o[1] == null ? "" : o[1].toString());
			tsmpReportData.setStringGroup3(o[2] == null ? "" : o[2].toString());
			tsmpReportData.setIntValue1(o[3] == null ? 0l : Long.valueOf(o[3].toString()));
			data.add(tsmpReportData);
		}

		return data;
	}
	
	//API次數-時間分析
	public List<TsmpReportData> queryAPITimesAndTime(Date now, String startDate, String endDate, String startHour, String endHour, 
			List<String> orgList, List<String> apiUidList, ReportDateTimeRangeTypeEnum dateTimeRangeType) {

		Optional<LocalDate> opt_startDate = DateTimeUtil.stringToLocalDate(startDate, DateTimeFormatEnum.西元年月日_2);
		if (!opt_startDate.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}

		Optional<LocalDate> opt_endDate = DateTimeUtil.stringToLocalDate(endDate, DateTimeFormatEnum.西元年月日_2);
		if (!opt_endDate.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}
		
		//分的時間條件
		Optional<Date> opt_startDateTime = Optional.empty();
		Optional<Date> opt_endDateTime = Optional.empty();
		if(dateTimeRangeType == ReportDateTimeRangeTypeEnum.MINUTE) {
			opt_startDateTime = DateTimeUtil.stringToDateTime(startDate + " " + startHour + ":00:00.000", DateTimeFormatEnum.西元年月日時分秒毫秒_2);
			if (!opt_startDateTime.isPresent()) {
				throw TsmpDpAaRtnCode._1381.throwing();
			}
			
			opt_endDateTime = DateTimeUtil.stringToDateTime(endDate + " " + endHour + ":59:59.999", DateTimeFormatEnum.西元年月日時分秒毫秒_2);
			if (!opt_endDateTime.isPresent()) {
				throw TsmpDpAaRtnCode._1383.throwing();
			}
		}

		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		//<===每張統計表的SELECT欄位與GROUP BY欄位都不一樣
		sb.append(" SELECT r.stringGroup1, ");
		sb.append(" 	   r.stringGroup2, ");
		sb.append(" 	   r.stringGroup3, ");
		sb.append(" 	   SUM(r.intValue1) ");
		
		//每張統計表都一樣
		sb.append(" FROM TsmpReportData r ");
		sb.append(" WHERE 1 = 1 ");
		sb.append(" AND r.reportType = :reportType ");
		// 組織原則
		sb.append(" AND (r.orgid IS NULL OR LENGTH(r.orgid) = 0");
		if (!CollectionUtils.isEmpty(orgList)) {
			sb.append(" OR r.orgid IN (:orgList)");
			params.put("orgList", orgList);
		}
		sb.append(" )");

		if (apiUidList != null && apiUidList.size() > 0) {
			sb.append(" AND EXISTS ( ");
			sb.append("              SELECT 1");
			sb.append("              FROM TsmpApi api");
			sb.append("              WHERE api.moduleName = r.stringGroup2 ");
			sb.append("              AND api.apiKey = r.stringGroup3 ");
			sb.append("              AND api.apiUid IN (:apiUidList) ");
			sb.append("             )");
			params.put("apiUidList", apiUidList);
		}

		sb.append(" AND ( 1=2");

		// <=== 組合天與月條件
		TsmpReportDataDaoConsumer aa1201ReportDateTimeRangeConsumer= new TsmpReportDataDaoConsumer();
		AA1201ReportDateTimeRange aa1201ReportDateTimeRange=new AA1201ReportDateTimeRange(); 
		aa1201ReportDateTimeRange.setNow(now);
		aa1201ReportDateTimeRange.setDateTimeRangeType(dateTimeRangeType);
		aa1201ReportDateTimeRange.setOpt_startDate(opt_startDate);
		aa1201ReportDateTimeRange.setOpt_endDate(opt_endDate);
		aa1201ReportDateTimeRange.setParams(params);
		aa1201ReportDateTimeRange.setSb(sb);
		aa1201ReportDateTimeRange.setOpt_startDateTime(opt_startDateTime);
		aa1201ReportDateTimeRange.setOpt_endDateTime(opt_endDateTime);
		aa1201ReportDateTimeRangeConsumer.consumer.accept(aa1201ReportDateTimeRange);
		// ===> 組合天與月條件
		
		sb.append("     ) ");
		sb.append(" GROUP BY r.stringGroup1, ");
		sb.append("          r.stringGroup2, ");
		sb.append("          r.stringGroup3 ");
		sb.append(" ORDER BY r.stringGroup1, ");
		sb.append("          r.stringGroup2, ");
		sb.append("          r.stringGroup3 ");

		params.put("reportType", ReportTypeEnum.API次數_時間分析.value());

		List<Object[]> obj = doQuery(sb.toString(), params, Object[].class);

		List<TsmpReportData> data = new ArrayList<TsmpReportData>();
		for (Object[] o : obj) {
			TsmpReportData tsmpReportData = new TsmpReportData();
			tsmpReportData.setStringGroup1(o[0] == null ? "" : o[0].toString());
			tsmpReportData.setStringGroup2(o[1] == null ? "" : o[1].toString());
			tsmpReportData.setStringGroup3(o[2] == null ? "" : o[2].toString());
			tsmpReportData.setIntValue1(o[3] == null ? 0l : Long.valueOf(o[3].toString()));
			data.add(tsmpReportData);
		}

		return data;
	}
	
	//API平均時間計算分析
	public List<TsmpReportData> queryAPIAverageTime(Date now, String startDate, String endDate, String startHour, String endHour,
			   List<String> orgList,List<String> apiUidList, ReportDateTimeRangeTypeEnum dateTimeRangeType){

		Optional<LocalDate> opt_startDate = DateTimeUtil.stringToLocalDate(startDate, DateTimeFormatEnum.西元年月日_2);
		if (!opt_startDate.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}

		Optional<LocalDate> opt_endDate = DateTimeUtil.stringToLocalDate(endDate, DateTimeFormatEnum.西元年月日_2);
		if (!opt_endDate.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}
		
		//分的時間條件
		Optional<Date> opt_startDateTime = Optional.empty();
		Optional<Date> opt_endDateTime = Optional.empty();
		if(dateTimeRangeType == ReportDateTimeRangeTypeEnum.MINUTE) {
			opt_startDateTime = DateTimeUtil.stringToDateTime(startDate + " " + startHour + ":00:00.000", DateTimeFormatEnum.西元年月日時分秒毫秒_2);
			if (!opt_startDateTime.isPresent()) {
				throw TsmpDpAaRtnCode._1381.throwing();
			}
			
			opt_endDateTime = DateTimeUtil.stringToDateTime(endDate + " " + endHour + ":59:59.999", DateTimeFormatEnum.西元年月日時分秒毫秒_2);
			if (!opt_endDateTime.isPresent()) {
				throw TsmpDpAaRtnCode._1383.throwing();
			}
		}

		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		//<===每張統計表的SELECT欄位與GROUP BY欄位都不一樣
		sb.append(" SELECT r.stringGroup1, ");
		sb.append(" 	   r.stringGroup2, ");
//		sb.append(" 	   AVG(r.intValue1) ");
		sb.append(" 	  SUM(r.intValue1 * r.intValue2) , ");
		sb.append(" 	  SUM(r.intValue2) ");
		sb.append(" 	  ");
		
		
		//每張統計表都一樣
		sb.append(" FROM TsmpReportData r ");
		sb.append(" WHERE 1 = 1 ");
		sb.append(" AND r.reportType = :reportType ");
		// 組織原則
		sb.append(" AND (r.orgid IS NULL OR LENGTH(r.orgid) = 0");
		if (!CollectionUtils.isEmpty(orgList)) {
			sb.append(" OR r.orgid IN (:orgList)");
			params.put("orgList", orgList);
		}
		sb.append(" )");

		if (apiUidList != null && apiUidList.size() > 0) {
			sb.append(" AND EXISTS ( ");
			sb.append("              SELECT 1");
			sb.append("              FROM TsmpApi api");
			sb.append("              WHERE api.moduleName = r.stringGroup1 ");
			sb.append("              AND api.apiKey = r.stringGroup2 ");
			sb.append("              AND api.apiUid IN (:apiUidList) ");
			sb.append("             )");
			params.put("apiUidList", apiUidList);
		}

		sb.append(" AND ( 1=2");

		// <=== 組合天與月條件
		TsmpReportDataDaoConsumer aa1201ReportDateTimeRangeConsumer= new TsmpReportDataDaoConsumer();
		AA1201ReportDateTimeRange aa1201ReportDateTimeRange=new AA1201ReportDateTimeRange(); 
		aa1201ReportDateTimeRange.setNow(now);
		aa1201ReportDateTimeRange.setDateTimeRangeType(dateTimeRangeType);
		aa1201ReportDateTimeRange.setOpt_startDate(opt_startDate);
		aa1201ReportDateTimeRange.setOpt_endDate(opt_endDate);
		aa1201ReportDateTimeRange.setParams(params);
		aa1201ReportDateTimeRange.setSb(sb);
		aa1201ReportDateTimeRange.setOpt_startDateTime(opt_startDateTime);
		aa1201ReportDateTimeRange.setOpt_endDateTime(opt_endDateTime);
		aa1201ReportDateTimeRangeConsumer.consumer.accept(aa1201ReportDateTimeRange);
		// ===> 組合天與月條件
		
		sb.append("     ) ");
		sb.append(" GROUP BY r.stringGroup1, ");
		sb.append("          r.stringGroup2 ");
		sb.append(" ORDER BY r.stringGroup1, ");
		sb.append("          r.stringGroup2 ");

		params.put("reportType", ReportTypeEnum.API平均時間計算分析.value());

		List<Object[]> obj = doQuery(sb.toString(), params, Object[].class);

		List<TsmpReportData> data = new ArrayList<TsmpReportData>();
		for (Object[] o : obj) {
			TsmpReportData tsmpReportData = new TsmpReportData();
			tsmpReportData.setStringGroup1(o[0] == null ? "" : o[0].toString());
			tsmpReportData.setStringGroup2(o[1] == null ? "" : o[1].toString());
			BigDecimal v1 = new BigDecimal(o[2].toString());
			BigDecimal v2 = new BigDecimal(o[3] == null ? "1" :  o[3].toString());
			long avgTime = v1.divide(v2, 0, RoundingMode.HALF_UP).longValue();
//			tsmpReportData.setIntValue1(o[2] == null ? 0l : new BigDecimal( o[2].toString()).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
			tsmpReportData.setIntValue1(avgTime);
			tsmpReportData.setIntValue2(v2.longValue());
			data.add(tsmpReportData);
		}

		return data;
	}

	
	//API流量分析
	public List<TsmpReportData> queryApiTraffic(Date now, String startDate, String endDate, String startHour, String endHour,
			   List<String> orgList, ReportDateTimeRangeTypeEnum dateTimeRangeType){

		Optional<LocalDate> opt_startDate = DateTimeUtil.stringToLocalDate(startDate, DateTimeFormatEnum.西元年月日_2);
		if (!opt_startDate.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}

		Optional<LocalDate> opt_endDate = DateTimeUtil.stringToLocalDate(endDate, DateTimeFormatEnum.西元年月日_2);
		if (!opt_endDate.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}
		
		//分的時間條件
		Optional<Date> opt_startDateTime = Optional.empty();
		Optional<Date> opt_endDateTime = Optional.empty();
		if(dateTimeRangeType == ReportDateTimeRangeTypeEnum.MINUTE) {
			opt_startDateTime = DateTimeUtil.stringToDateTime(startDate + " " + startHour + ":00:00.000", DateTimeFormatEnum.西元年月日時分秒毫秒_2);
			if (!opt_startDateTime.isPresent()) {
				throw TsmpDpAaRtnCode._1381.throwing();
			}
			
			opt_endDateTime = DateTimeUtil.stringToDateTime(endDate + " " + endHour + ":59:59.999", DateTimeFormatEnum.西元年月日時分秒毫秒_2);
			if (!opt_endDateTime.isPresent()) {
				throw TsmpDpAaRtnCode._1383.throwing();
			}
		}

		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		//<===每張統計表的SELECT欄位與GROUP BY欄位都不一樣
		sb.append(" SELECT r.lastRowDateTime, ");
		sb.append(" 	   r.intValue1, ");
		sb.append(" 	   r.stringGroup1 ");
		
		//每張統計表都一樣
		sb.append(" FROM TsmpReportData r ");
		sb.append(" WHERE 1 = 1 ");
		sb.append(" AND r.reportType = :reportType ");
		// 組織原則
		sb.append(" AND (r.orgid IS NULL OR LENGTH(r.orgid) = 0");
		if (!CollectionUtils.isEmpty(orgList)) {
			sb.append(" OR r.orgid IN (:orgList)");
			params.put("orgList", orgList);
		}
		sb.append(" )");

		sb.append(" AND ( 1=2");

		// <=== 組合天與月條件
		TsmpReportDataDaoConsumer aa1201ReportDateTimeRangeConsumer= new TsmpReportDataDaoConsumer();
		AA1201ReportDateTimeRange aa1201ReportDateTimeRange=new AA1201ReportDateTimeRange(); 
		aa1201ReportDateTimeRange.setNow(now);
		aa1201ReportDateTimeRange.setDateTimeRangeType(dateTimeRangeType);
		aa1201ReportDateTimeRange.setOpt_startDate(opt_startDate);
		aa1201ReportDateTimeRange.setOpt_endDate(opt_endDate);
		aa1201ReportDateTimeRange.setParams(params);
		aa1201ReportDateTimeRange.setSb(sb);
		aa1201ReportDateTimeRange.setOpt_startDateTime(opt_startDateTime);
		aa1201ReportDateTimeRange.setOpt_endDateTime(opt_endDateTime);
		aa1201ReportDateTimeRangeConsumer.consumer.accept(aa1201ReportDateTimeRange);
		// ===> 組合天與月條件
		
		sb.append("     ) ");

		params.put("reportType", ReportTypeEnum.API流量分析.value());

		List<Object[]> obj = doQuery(sb.toString(), params, Object[].class);

		List<TsmpReportData> data = new ArrayList<TsmpReportData>();
		for (Object[] o : obj) {
			TsmpReportData tsmpReportData = new TsmpReportData();
			
			tsmpReportData.setLastRowDateTime((Timestamp)o[0]);
			tsmpReportData.setIntValue1(o[1] == null ? 0l : Long.valueOf(o[1].toString()));
			tsmpReportData.setStringGroup1(o[2] == null ? "" : o[2].toString());
			data.add(tsmpReportData);
		}

		return data;
	}
	
	
	//Bad Attempt連線報告
	public List<TsmpReportData> queryBadattemptConnection(Date now, String startDate, String endDate, String startHour, String endHour, List<String> orgList, ReportDateTimeRangeTypeEnum dateTimeRangeType){


		Optional<LocalDate> opt_startDate = DateTimeUtil.stringToLocalDate(startDate, DateTimeFormatEnum.西元年月日_2);
		if (!opt_startDate.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}

		Optional<LocalDate> opt_endDate = DateTimeUtil.stringToLocalDate(endDate, DateTimeFormatEnum.西元年月日_2);
		if (!opt_endDate.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}
		
		//分的時間條件
		Optional<Date> opt_startDateTime = Optional.empty();
		Optional<Date> opt_endDateTime = Optional.empty();
		if(dateTimeRangeType == ReportDateTimeRangeTypeEnum.MINUTE) {
			opt_startDateTime = DateTimeUtil.stringToDateTime(startDate + " " + startHour + ":00:00.000", DateTimeFormatEnum.西元年月日時分秒毫秒_2);
			if (!opt_startDateTime.isPresent()) {
				throw TsmpDpAaRtnCode._1381.throwing();
			}
			
			opt_endDateTime = DateTimeUtil.stringToDateTime(endDate + " " + endHour + ":59:59.999", DateTimeFormatEnum.西元年月日時分秒毫秒_2);
			if (!opt_endDateTime.isPresent()) {
				throw TsmpDpAaRtnCode._1383.throwing();
			}
		}

		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		//<===每張統計表的SELECT欄位與GROUP BY欄位都不一樣
		sb.append(" SELECT r.lastRowDateTime, ");
		sb.append(" 	   r.stringGroup1, ");
		sb.append(" 	   r.intValue1 ");
		
		//每張統計表都一樣
		sb.append(" FROM TsmpReportData r ");
		sb.append(" WHERE 1 = 1 ");
		sb.append(" AND r.reportType = :reportType ");
		// 組織原則
		sb.append(" AND (r.orgid IS NULL OR LENGTH(r.orgid) = 0");
		if (!CollectionUtils.isEmpty(orgList)) {
			sb.append(" OR r.orgid IN (:orgList)");
			params.put("orgList", orgList);
		}
		sb.append(" )");

		sb.append(" AND ( 1=2");

		// <=== 組合天與月條件
		TsmpReportDataDaoConsumer aa1201ReportDateTimeRangeConsumer= new TsmpReportDataDaoConsumer();
		AA1201ReportDateTimeRange aa1201ReportDateTimeRange=new AA1201ReportDateTimeRange(); 
		aa1201ReportDateTimeRange.setNow(now);
		aa1201ReportDateTimeRange.setDateTimeRangeType(dateTimeRangeType);
		aa1201ReportDateTimeRange.setOpt_startDate(opt_startDate);
		aa1201ReportDateTimeRange.setOpt_endDate(opt_endDate);
		aa1201ReportDateTimeRange.setParams(params);
		aa1201ReportDateTimeRange.setSb(sb);
		aa1201ReportDateTimeRange.setOpt_startDateTime(opt_startDateTime);
		aa1201ReportDateTimeRange.setOpt_endDateTime(opt_endDateTime);
		aa1201ReportDateTimeRangeConsumer.consumer.accept(aa1201ReportDateTimeRange);
		// ===> 組合天與月條件
		
		sb.append("     ) ");

		params.put("reportType", ReportTypeEnum.BadAttempt連線報告.value());

		List<Object[]> obj = doQuery(sb.toString(), params, Object[].class);

		List<TsmpReportData> data = new ArrayList<TsmpReportData>();
		for (Object[] o : obj) {
			TsmpReportData tsmpReportData = new TsmpReportData();
			
			tsmpReportData.setLastRowDateTime((Timestamp)o[0]);
			tsmpReportData.setStringGroup1(o[1] == null ? "" : o[1].toString());
			tsmpReportData.setIntValue1(o[2] == null ? 0l : Long.valueOf(o[2].toString()));
			data.add(tsmpReportData);
		}

		return data;
	}
}
