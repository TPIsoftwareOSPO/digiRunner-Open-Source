package tpi.dgrv4.dpaa.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.ReportDateTimeRangeTypeEnum;
import tpi.dgrv4.common.constant.ReportTypeEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.entity.jpql.TsmpReportData;
import tpi.dgrv4.gateway.keeper.TPILogger;

public class ReportUtil {

	private TPILogger logger = TPILogger.tl;

	/** API使用次數 */
	public List<TsmpReportData> getApiUseNumReportData(List<TsmpReportData> oriDataList,
			ReportDateTimeRangeTypeEnum dateTimeType, String createUser) {

		// group by orgid和lastRowDateTime和StringGroup1和StringGroup2和StringGroup3
		Map<String, Map<String, Map<String, Map<String, Map<String, List<TsmpReportData>>>>>> dataMap = getGroupByOrgidAndLastRowDateTimeAndStringGroup1AndStringGroup2AndStringGroup3(
				dateTimeType, oriDataList);

		// reportData
		List<TsmpReportData> reportDataList = new ArrayList<>();
		for (String orgidKey : new TreeSet<String>(dataMap.keySet())) {
			for (String dateKey : new TreeSet<String>(dataMap.get(orgidKey).keySet())) {
				for (String stringGroup1Key : new TreeSet<String>(dataMap.get(orgidKey).get(dateKey).keySet())) {
					for (String stringGroup2Key : new TreeSet<String>(
							dataMap.get(orgidKey).get(dateKey).get(stringGroup1Key).keySet())) {
						for (String stringGroup3Key : new TreeSet<String>(dataMap.get(orgidKey).get(dateKey)
								.get(stringGroup1Key).get(stringGroup2Key).keySet())) {

							List<TsmpReportData> dataList = dataMap.get(orgidKey).get(dateKey).get(stringGroup1Key)
									.get(stringGroup2Key).get(stringGroup3Key);
							TsmpReportData apiUseNum_vo = new TsmpReportData();
							apiUseNum_vo.setReportType(ReportTypeEnum.API使用次數統計.value());
							apiUseNum_vo.setDateTimeRangeType(dateTimeType.value());
							apiUseNum_vo.setLastRowDateTime(dataList.get(dataList.size() - 1).getLastRowDateTime());
							apiUseNum_vo.setStatisticsStatus("N");
							apiUseNum_vo.setStringGroup1(stringGroup1Key);
							apiUseNum_vo.setIntValue1(dataList.stream().mapToLong(d -> d.getIntValue1()).sum());
							apiUseNum_vo.setStringGroup2(stringGroup2Key);
							apiUseNum_vo.setStringGroup3(stringGroup3Key);
							apiUseNum_vo.setOrgid(orgidKey);
							apiUseNum_vo.setCreateUser(createUser);
							reportDataList.add(apiUseNum_vo);
						}
					}
				}
			}
		}
		return reportDataList;

	}

	/** API次數-時間分析 */
	public List<TsmpReportData> getApiNumTimeReportData(List<TsmpReportData> oriDataList,
			ReportDateTimeRangeTypeEnum dateTimeType, String createUser) {

		// group by orgid和lastRowDateTime和StringGroup1和StringGroup2和StringGroup3
		Map<String, Map<String, Map<String, Map<String, Map<String, List<TsmpReportData>>>>>> dataMap = getGroupByOrgidAndLastRowDateTimeAndStringGroup1AndStringGroup2AndStringGroup3(
				dateTimeType, oriDataList);

		// reportData
		List<TsmpReportData> reportDataList = new ArrayList<>();
		for (String orgidKey : new TreeSet<String>(dataMap.keySet())) {
			for (String dateKey : new TreeSet<String>(dataMap.get(orgidKey).keySet())) {
				for (String stringGroup1Key : new TreeSet<String>(dataMap.get(orgidKey).get(dateKey).keySet())) {
					for (String stringGroup2Key : new TreeSet<String>(
							dataMap.get(orgidKey).get(dateKey).get(stringGroup1Key).keySet())) {
						for (String stringGroup3Key : new TreeSet<String>(dataMap.get(orgidKey).get(dateKey)
								.get(stringGroup1Key).get(stringGroup2Key).keySet())) {
							List<TsmpReportData> dataList = dataMap.get(orgidKey).get(dateKey).get(stringGroup1Key)
									.get(stringGroup2Key).get(stringGroup3Key);
							TsmpReportData apiNumTime_vo = new TsmpReportData();
							apiNumTime_vo.setReportType(ReportTypeEnum.API次數_時間分析.value());
							apiNumTime_vo.setDateTimeRangeType(dateTimeType.value());
							apiNumTime_vo.setLastRowDateTime(dataList.get(dataList.size() - 1).getLastRowDateTime());
							apiNumTime_vo.setStatisticsStatus("N");
							apiNumTime_vo.setStringGroup1(stringGroup1Key);
							apiNumTime_vo.setIntValue1(dataList.stream().mapToLong(d -> d.getIntValue1()).sum());
							apiNumTime_vo.setStringGroup2(stringGroup2Key);
							apiNumTime_vo.setStringGroup3(stringGroup3Key);
							apiNumTime_vo.setOrgid(orgidKey);
							apiNumTime_vo.setCreateUser(createUser);
							reportDataList.add(apiNumTime_vo);
						}

					}
				}
			}
		}
		return reportDataList;
	}

	// API平均時間計算分析
	public List<TsmpReportData> getApiAvgTimeReportData(List<TsmpReportData> oriDataList,
			ReportDateTimeRangeTypeEnum dateTimeType, String createUser) {

		// 取得group by orgid和lastRowDateTime和StringGroup1和StringGroup2
		Map<String, Map<String, Map<String, Map<String, List<TsmpReportData>>>>> dataMap = getGroupByOrgidAndLastRowDateTimeAndStringGroup1AndStringGroup2(
				dateTimeType, oriDataList);

		// reportData
		List<TsmpReportData> reportDataList = new ArrayList<>();
		for (String orgidKey : new TreeSet<String>(dataMap.keySet())) {
			for (String dateKey : new TreeSet<String>(dataMap.get(orgidKey).keySet())) {
				for (String stringGroup1Key : new TreeSet<String>(dataMap.get(orgidKey).get(dateKey).keySet())) {
					for (String stringGroup2Key : new TreeSet<String>(
							dataMap.get(orgidKey).get(dateKey).get(stringGroup1Key).keySet())) {

						List<TsmpReportData> dataList = dataMap.get(orgidKey).get(dateKey).get(stringGroup1Key)
								.get(stringGroup2Key);
						TsmpReportData vo = new TsmpReportData();
						vo.setReportType(ReportTypeEnum.API平均時間計算分析.value());
						vo.setDateTimeRangeType(dateTimeType.value());
						vo.setLastRowDateTime(dataList.get(dataList.size() - 1).getLastRowDateTime());
						vo.setStatisticsStatus("N");
						vo.setStringGroup1(stringGroup1Key);
						vo.setStringGroup2(stringGroup2Key);

						// 計算intValue1的平均值
						long total = dataList.stream()
								.mapToLong(d -> (d.getIntValue1() * (d.getIntValue2() == null ? 1 : d.getIntValue2())))
								.sum();
						long size = dataList.stream().mapToLong(d -> d.getIntValue2() == null ? 1 : d.getIntValue2()).sum();
//						double avg = (double) total / dataList.size();
						BigDecimal bdTotal = new BigDecimal(Long.toString(total));
						BigDecimal bdSize = new BigDecimal(Long.toString(size));
						long avg = bdTotal.divide(bdSize, 0, RoundingMode.HALF_UP).longValue();
//						double avg = (double) total / size;
//						long intValue1 = BigDecimal.valueOf(avg).setScale(0, RoundingMode.HALF_UP).longValue();

						vo.setIntValue1(avg);
						vo.setIntValue2(size);
						vo.setOrgid(orgidKey);
						vo.setCreateUser(createUser);
						reportDataList.add(vo);
					}

				}
			}
		}

		return reportDataList;

	}

	/** API流量分析 */
	public List<TsmpReportData> getApiFlowReportData(List<TsmpReportData> oriDataList,
			ReportDateTimeRangeTypeEnum dateTimeType, String createUser) {

		// 取得group by orgid和lastRowDateTime和StringGroup1
		Map<String, Map<String, Map<String, List<TsmpReportData>>>> dataMap = getGroupByOrgidAndLastRowDateTimeAndStringGroup1(
				dateTimeType, oriDataList);

		// reportData
		List<TsmpReportData> reportDataList = new ArrayList<>();
		for (String orgidKey : new TreeSet<String>(dataMap.keySet())) {
			for (String dateKey : new TreeSet<String>(dataMap.get(orgidKey).keySet())) {
				for (String stringGroup1Key : new TreeSet<String>(dataMap.get(orgidKey).get(dateKey).keySet())) {

					List<TsmpReportData> dataList = dataMap.get(orgidKey).get(dateKey).get(stringGroup1Key);
					TsmpReportData vo = new TsmpReportData();
					vo.setReportType(ReportTypeEnum.API流量分析.value());
					vo.setDateTimeRangeType(dateTimeType.value());
					vo.setLastRowDateTime(dataList.get(dataList.size() - 1).getLastRowDateTime());
					vo.setStatisticsStatus("N");
					vo.setStringGroup1(stringGroup1Key);
					vo.setIntValue1(dataList.stream().mapToLong(d -> d.getIntValue1()).sum());
					vo.setOrgid(orgidKey);
					vo.setCreateUser(createUser);
					reportDataList.add(vo);

				}
			}
		}

		return reportDataList;

	}

	/** Bad Attempt連線報告 */
	public List<TsmpReportData> getBadAttemptReportData(List<TsmpReportData> oriDataList,
			ReportDateTimeRangeTypeEnum dateTimeType, String createUser) {
		
		// 取得group by orgid和lastRowDateTime和StringGroup1
		Map<String, Map<String, Map<String, List<TsmpReportData>>>> dataMap = getGroupByOrgidAndLastRowDateTimeAndStringGroup1(
				dateTimeType, oriDataList);

		// reportData
		List<TsmpReportData> reportDataList = new ArrayList<>();
		for (String orgidKey : new TreeSet<String>(dataMap.keySet())) {
			for (String dateKey : new TreeSet<String>(dataMap.get(orgidKey).keySet())) {
				for (String stringGroup1Key : new TreeSet<String>(dataMap.get(orgidKey).get(dateKey).keySet())) {

					List<TsmpReportData> dataList = dataMap.get(orgidKey).get(dateKey).get(stringGroup1Key);
					TsmpReportData vo = new TsmpReportData();
					vo.setReportType(ReportTypeEnum.BadAttempt連線報告.value());
					vo.setDateTimeRangeType(dateTimeType.value());
					vo.setLastRowDateTime(dataList.get(dataList.size() - 1).getLastRowDateTime());
					vo.setStatisticsStatus("N");
					vo.setStringGroup1(stringGroup1Key);
					vo.setIntValue1(dataList.stream().mapToLong(d -> d.getIntValue1()).sum());
					vo.setOrgid(orgidKey);
					vo.setCreateUser(createUser);
					reportDataList.add(vo);

				}
			}
		}

		return reportDataList;

	}

	private Map<String, Map<String, Map<String, Map<String, Map<String, List<TsmpReportData>>>>>> getGroupByOrgidAndLastRowDateTimeAndStringGroup1AndStringGroup2AndStringGroup3(
			ReportDateTimeRangeTypeEnum dateTimeType, List<TsmpReportData> dataList) {
		return dataList.stream().collect(Collectors.groupingBy(vo -> vo.getOrgid(),
				Collectors.groupingBy(vo -> getGroupByLastRowDateTimeFormat(dateTimeType, vo.getLastRowDateTime()),
						Collectors.groupingBy(vo -> vo.getStringGroup1(), Collectors.groupingBy(
								vo -> vo.getStringGroup2(), Collectors.groupingBy(vo -> vo.getStringGroup3()))))));

	}

	private Map<String, Map<String, Map<String, Map<String, List<TsmpReportData>>>>> getGroupByOrgidAndLastRowDateTimeAndStringGroup1AndStringGroup2(
			ReportDateTimeRangeTypeEnum dateTimeType, List<TsmpReportData> dataList) {
		return dataList.stream().collect(Collectors.groupingBy(vo -> vo.getOrgid(), Collectors.groupingBy(
				vo -> getGroupByLastRowDateTimeFormat(dateTimeType, vo.getLastRowDateTime()),
				Collectors.groupingBy(vo -> vo.getStringGroup1(), Collectors.groupingBy(vo -> vo.getStringGroup2())))));
	}

	private Map<String, Map<String, Map<String, List<TsmpReportData>>>> getGroupByOrgidAndLastRowDateTimeAndStringGroup1(
			ReportDateTimeRangeTypeEnum dateTimeType, List<TsmpReportData> dataList) {
		return dataList.stream()
				.collect(Collectors.groupingBy(vo -> vo.getOrgid(),
						Collectors.groupingBy(
								vo -> getGroupByLastRowDateTimeFormat(dateTimeType, vo.getLastRowDateTime()),
								Collectors.groupingBy(vo -> vo.getStringGroup1()))));

	}

	private String getGroupByLastRowDateTimeFormat(ReportDateTimeRangeTypeEnum dateTimeType, Date lastRowDateTime) {
		if (dateTimeType == ReportDateTimeRangeTypeEnum.HOUR) {
			return DateTimeUtil.dateTimeToString(lastRowDateTime, DateTimeFormatEnum.西元年月日時).orElse(null);
		} else if (dateTimeType == ReportDateTimeRangeTypeEnum.DAY) {
			return DateTimeUtil.dateTimeToString(lastRowDateTime, DateTimeFormatEnum.西元年月日).orElse(null);
		} else if (dateTimeType == ReportDateTimeRangeTypeEnum.MONTH) {
			return DateTimeUtil.dateTimeToString(lastRowDateTime, DateTimeFormatEnum.西元年月_2).orElse(null);
		} else if (dateTimeType == ReportDateTimeRangeTypeEnum.YEAR) {
			return DateTimeUtil.dateTimeToString(lastRowDateTime, DateTimeFormatEnum.西元年).orElse(null);
		} else {
			logger.error("ReportDateTimeRangeTypeEnum not found : " + dateTimeType);
			return null;
		}
	}

}
