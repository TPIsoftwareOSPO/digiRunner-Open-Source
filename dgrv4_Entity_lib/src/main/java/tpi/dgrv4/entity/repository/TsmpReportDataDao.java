package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.common.constant.ReportDateTimeRangeTypeEnum;
import tpi.dgrv4.entity.entity.jpql.TsmpReportData;

@Repository
public interface TsmpReportDataDao extends JpaRepository<TsmpReportData, Long> {

	List<TsmpReportData> findByCreateUser(String createUser);
	
	List<TsmpReportData> findByDateTimeRangeTypeAndCreateUser(int dateTimeRangeType, String createUser);

	List<TsmpReportData> findByReportTypeAndDateTimeRangeType(int reportType, int dateTimeRangeType);

	List<TsmpReportData> findByReportTypeAndDateTimeRangeTypeAndStatisticsStatusAndLastRowDateTimeLessThanOrderByLastRowDateTimeAsc(
			int reportType, int dateTimeRangeType, String statisticsStatus, Date lastRowDateTimeLess);
	
	Long deleteByDateTimeRangeTypeAndLastRowDateTimeLessThan(int dateTimeRangeType, Date lastRowDateTime);
	
	List<TsmpReportData> queryByApiUsageStatistics(Date now, String startDate, String endDate, String startHour, String endHour, List<String> orgList,List<String> apiUidList, ReportDateTimeRangeTypeEnum dateTimeRangeType);

	List<TsmpReportData> queryAPITimesAndTime(Date now, String startDate, String endDate, String startHour, String endHour, List<String> orgList,List<String> apiUidList, ReportDateTimeRangeTypeEnum dateTimeRangeType);
	
	List<TsmpReportData> queryAPIAverageTime(Date now, String startDate, String endDate, String startHour, String endHour, List<String> orgList,List<String> apiUidList, ReportDateTimeRangeTypeEnum dateTimeRangeType);
	
	List<TsmpReportData> queryApiTraffic(Date now, String startDate, String endDate, String startHour, String endHour, List<String> orgList , ReportDateTimeRangeTypeEnum dateTimeRangeType);

	List<TsmpReportData> queryBadattemptConnection(Date now, String startDate, String endDate, String startHour, String endHour, List<String> orgList, ReportDateTimeRangeTypeEnum dateTimeRangeType);
}
