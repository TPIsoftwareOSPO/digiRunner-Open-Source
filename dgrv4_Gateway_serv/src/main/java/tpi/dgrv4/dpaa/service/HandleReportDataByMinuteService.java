package tpi.dgrv4.dpaa.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.ReportDateTimeRangeTypeEnum;
import tpi.dgrv4.common.constant.ReportTypeEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.jpql.TsmpReportData;
import tpi.dgrv4.entity.entity.jpql.TsmpReqLog;
import tpi.dgrv4.entity.entity.jpql.TsmpResLog;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpReportDataDao;
import tpi.dgrv4.entity.repository.TsmpReqLogDao;
import tpi.dgrv4.entity.repository.TsmpResLogDao;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class HandleReportDataByMinuteService {
	private TsmpReqLogDao tsmpReqLogDao;
	private TsmpResLogDao tsmpResLogDao;
	private TsmpReportDataDao tsmpReportDataDao;
	private TsmpSettingDao tsmpSettingDao;
	private HandleApiDataAndLogService handleApiDataAndLogService;
	private HandleDashboardLogDataService handleDashboardLogDataService;
    private TsmpDpApptJobDao tsmpDpApptJobDao;

    @Autowired
	public HandleReportDataByMinuteService(TsmpReqLogDao tsmpReqLogDao, TsmpResLogDao tsmpResLogDao,
			TsmpReportDataDao tsmpReportDataDao, TsmpSettingDao tsmpSettingDao,
			HandleApiDataAndLogService handleApiDataAndLogService,
			HandleDashboardLogDataService handleDashboardLogDataService, TsmpDpApptJobDao tsmpDpApptJobDao) {
		super();
		this.tsmpReqLogDao = tsmpReqLogDao;
		this.tsmpResLogDao = tsmpResLogDao;
		this.tsmpReportDataDao = tsmpReportDataDao;
		this.tsmpSettingDao = tsmpSettingDao;
		this.handleApiDataAndLogService = handleApiDataAndLogService;
		this.handleDashboardLogDataService = handleDashboardLogDataService;
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
	}

	@Transactional()
	public void exec(Date execDate, String createUser, Long jobId) throws Exception {
		
		Map<String, Object> map = getHandleDashboardLogDataService().exec(execDate, createUser);
		List<TsmpReqLog> reqList = (List<TsmpReqLog>) map.get("req");
		List<TsmpResLog> resList = (List<TsmpResLog>) map.get("res");
		TPILogger.tl.info("--- Begin HandleReportDataByMinuteService ---");

		// 現在區間的日期
		Date nowIntervalDate = this.getNowIntervalDate(execDate);
		TPILogger.tl.info("nowIntervalDate = " + DateTimeUtil.dateTimeToString(nowIntervalDate, DateTimeFormatEnum.西元年月日時分秒).orElse(execDate.toString()));


		TPILogger.tl.info("reqList.size = " + reqList.size());
		for(TsmpReqLog vo : reqList) {
			if(vo.getOrgid() == null) {
				vo.setOrgid("");
			}
		}

		// 取得group by orgid和rtime和moduleName和txid(不包含txid為null)
		Map<String, Map<String, Map<String, Map<String, List<TsmpReqLog>>>>> reqMap = getGroupByOrgidAndRtimeAndModuleNameAndTxid(
				reqList);
		// 取得API使用次數reportData
		List<TsmpReportData> dataList = getApiUseNumReportData(reqMap, createUser);
		getTsmpReportDataDao().saveAll(dataList);

		// 取得API次數-時間分析reportData
		dataList = getApiNumTimeReportData(reqMap, createUser);
		getTsmpReportDataDao().saveAll(dataList);

		// 取得API平均時間計算分析reportData
		dataList = getApiAvgTimeReportData(reqMap, createUser);
		getTsmpReportDataDao().saveAll(dataList);

		// 取得group by orgid和rtime
		Map<String, Map<String, List<TsmpReqLog>>> reqMap2 = getGroupByOrgidAndRtime(reqList);
		// 取得API流量分析reportData
		dataList = getApiFlowReportData(reqMap2, createUser);
		getTsmpReportDataDao().saveAll(dataList);

		// 取得Bad Attempt連線報告reportData
		dataList = getBadAttemptReportData(reqMap2, createUser);
		getTsmpReportDataDao().saveAll(dataList);

		// 刪除資料 & 將次數記錄到Tsmp api
		
		getHandleApiDataAndLogService().insertTsmpApiAndDeleteLog(reqList, resList);
	
		checkJobStatus(jobId);
		TPILogger.tl.info("--- Finish HandleReportDataByMinuteService ---");
	}
	
	protected void checkJobStatus(Long id) {
		//因為在換版可能REPORT_BATCH還在執行中,啟動後會被AutoInitSQL改成取消,若這種情況就rollback
		boolean isExists = getTsmpDpApptJobDao().existsByApptJobIdAndStatus(id, "C");
		if(isExists) {
			TPILogger.tl.info("REPORT_BATCH job status changed cancel id:" + id);
			throw new CancellationException();
		}
	}

	private Date getNowIntervalDate(Date nowDate) {
		String strDate = DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日時分).orElseThrow(TsmpDpAaRtnCode._1295::throwing);

		strDate = strDate.substring(0, 15);
		strDate = strDate + "0:00.000";
		Date nowIntervalDate = DateTimeUtil.stringToDateTime(strDate, DateTimeFormatEnum.西元年月日時分秒毫秒).orElseThrow(TsmpDpAaRtnCode._1295::throwing);

		return nowIntervalDate;

	}

	private Date getHistoryDeleteDate(Date nowDate, int day) {
//		String strDate = DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日時分秒).get();

		Calendar nowTime = Calendar.getInstance();
		nowTime.setTime(nowDate);
		nowTime.add(Calendar.DATE, day);
		nowDate = nowTime.getTime();
		String strDate = DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日時分秒).orElseThrow(TsmpDpAaRtnCode._1295::throwing);
		strDate = strDate + ".000";
		Date nowIntervalDate = DateTimeUtil.stringToDateTime(strDate, DateTimeFormatEnum.西元年月日時分秒毫秒).orElseThrow(TsmpDpAaRtnCode._1295::throwing);

		return nowIntervalDate;

	}

	private Map<String, Map<String, Map<String, Map<String, List<TsmpReqLog>>>>> getGroupByOrgidAndRtimeAndModuleNameAndTxid(
			List<TsmpReqLog> reqList) {
		return reqList
				.stream().filter(
						vo -> vo.getTxid() != null)
				.collect(
						Collectors
								.groupingBy(vo -> vo.getOrgid(),
										Collectors
												.groupingBy(
														vo -> DateTimeUtil
																.dateTimeToString(vo.getRtime(),
																		DateTimeFormatEnum.西元年月日時分)
																.get().substring(0, 15),
														Collectors.groupingBy(vo -> vo.getModuleName(),
																Collectors.groupingBy(vo -> vo.getTxid())))));
	}

	private Map<String, Map<String, List<TsmpReqLog>>> getGroupByOrgidAndRtime(List<TsmpReqLog> reqList) {
		return reqList.stream()
				.collect(Collectors.groupingBy(vo -> vo.getOrgid(), Collectors.groupingBy(vo -> DateTimeUtil
						.dateTimeToString(vo.getRtime(), DateTimeFormatEnum.西元年月日時分).get().substring(0, 15))));

	}

	// API使用次數
	private List<TsmpReportData> getApiUseNumReportData(
			Map<String, Map<String, Map<String, Map<String, List<TsmpReqLog>>>>> reqMap, String createUser) {
		Map<String, List<TsmpReqLog>> rsMap = new TreeMap<>();

		// 取得group by exestatus後,並裝入新的MAP
		for (String orgidKey : new TreeSet<String>(reqMap.keySet())) {
			for (String dateKey : new TreeSet<String>(reqMap.get(orgidKey).keySet())) {
				for (String moduleNameKey : new TreeSet<String>(reqMap.get(orgidKey).get(dateKey).keySet())) {
					for (String txidKey : new TreeSet<String>(
							reqMap.get(orgidKey).get(dateKey).get(moduleNameKey).keySet())) {
						List<TsmpReqLog> dataList = reqMap.get(orgidKey).get(dateKey).get(moduleNameKey).get(txidKey);
						dataList.forEach(x -> {
							// 取得TsmpResLog
							TsmpResLog resVo = getTsmpResLogDao().findById(x.getId()).orElse(null);
							if (resVo != null) {
								String rsKey = orgidKey + "||" + dateKey + "||" + moduleNameKey + "||" + txidKey + "||"
										+ resVo.getExeStatus();
								List<TsmpReqLog> minuteApiUseNum_rsList = rsMap.get(rsKey);
								if (minuteApiUseNum_rsList == null) {
									minuteApiUseNum_rsList = new ArrayList<>();
								}
								minuteApiUseNum_rsList.add(x);
								rsMap.put(rsKey, minuteApiUseNum_rsList);
							}
						});
					}
				}
			}
		}

		// reportData
		List<TsmpReportData> reportDataList = new ArrayList<>();
		for (String minuteApiUseNum_rsKey : rsMap.keySet()) {
			List<TsmpReqLog> reqList = rsMap.get(minuteApiUseNum_rsKey);
			String[] arrKey = minuteApiUseNum_rsKey.split("\\|\\|");
			TsmpReportData vo = new TsmpReportData();
			vo.setReportType(ReportTypeEnum.API使用次數統計.value());
			vo.setDateTimeRangeType(ReportDateTimeRangeTypeEnum.MINUTE.value());
			vo.setLastRowDateTime(reqList.get(reqList.size() - 1).getRtime());
			vo.setStatisticsStatus("N");
			vo.setStringGroup1(arrKey[2]);
			vo.setIntValue1((long) reqList.size());
			vo.setStringGroup2(arrKey[3]);
			vo.setStringGroup3(arrKey[4]);
			vo.setOrgid(arrKey[0]);
			vo.setCreateUser(createUser);
			reportDataList.add(vo);
		}

		return reportDataList;
	}

	// API次數-時間分析
	private List<TsmpReportData> getApiNumTimeReportData(
			Map<String, Map<String, Map<String, Map<String, List<TsmpReqLog>>>>> reqMap, String createUser) {

		Map<String, List<TsmpReqLog>> rsMap = new TreeMap<>();

		// group by elapse區間且只取exeStatus為Y,並裝入新的MAP
		for (String orgidKey : new TreeSet<String>(reqMap.keySet())) {
			for (String dateKey : new TreeSet<String>(reqMap.get(orgidKey).keySet())) {
				for (String moduleNameKey : new TreeSet<String>(reqMap.get(orgidKey).get(dateKey).keySet())) {
					for (String txidKey : new TreeSet<String>(
							reqMap.get(orgidKey).get(dateKey).get(moduleNameKey).keySet())) {
						List<TsmpReqLog> dataList = reqMap.get(orgidKey).get(dateKey).get(moduleNameKey).get(txidKey);
						dataList.forEach(x -> {
							TsmpResLog resVo = getTsmpResLogDao().findById(x.getId()).orElse(null);
							if (resVo != null && "Y".equals(resVo.getExeStatus())) {
								String rsKey = orgidKey + "||" + dateKey + "||" + moduleNameKey + "||" + txidKey + "||"
										+ (resVo.getElapse() / 1000);
								List<TsmpReqLog> minuteApiNumTime_rsList = rsMap.get(rsKey);
								if (minuteApiNumTime_rsList == null) {
									minuteApiNumTime_rsList = new ArrayList<>();
								}
								minuteApiNumTime_rsList.add(x);
								rsMap.put(rsKey, minuteApiNumTime_rsList);
							}
						});

					}
				}
			}
		}

		// reportData
		List<TsmpReportData> reportDataList = new ArrayList<>();
		for (String rsKey : rsMap.keySet()) {
			List<TsmpReqLog> reqList = rsMap.get(rsKey);
			String[] arrKey = rsKey.split("\\|\\|");
			TsmpReportData vo = new TsmpReportData();
			vo.setReportType(ReportTypeEnum.API次數_時間分析.value());
			vo.setDateTimeRangeType(ReportDateTimeRangeTypeEnum.MINUTE.value());
			vo.setLastRowDateTime(reqList.get(reqList.size() - 1).getRtime());
			vo.setStatisticsStatus("N");
			vo.setStringGroup1((Integer.parseInt(arrKey[4]) * 1000) + "~" + (Integer.parseInt(arrKey[4]) * 1000 + 999));
			vo.setIntValue1((long) reqList.size());
			vo.setStringGroup2(arrKey[2]);
			vo.setStringGroup3(arrKey[3]);
			vo.setOrgid(arrKey[0]);
			vo.setCreateUser(createUser);
			reportDataList.add(vo);
		}

		return reportDataList;
	}

	// API平均時間計算分析
	private List<TsmpReportData> getApiAvgTimeReportData(
			Map<String, Map<String, Map<String, Map<String, List<TsmpReqLog>>>>> reqMap, String createUser) {

		Map<String, List<TsmpReqLog>> rsMap = new TreeMap<>();
		// 只取exeStatus為Y,並裝入新的MAP
		for (String orgidKey : new TreeSet<String>(reqMap.keySet())) {
			for (String dateKey : new TreeSet<String>(reqMap.get(orgidKey).keySet())) {
				for (String moduleNameKey : new TreeSet<String>(reqMap.get(orgidKey).get(dateKey).keySet())) {
					for (String txidKey : new TreeSet<String>(
							reqMap.get(orgidKey).get(dateKey).get(moduleNameKey).keySet())) {

						List<TsmpReqLog> dataList = reqMap.get(orgidKey).get(dateKey).get(moduleNameKey).get(txidKey);
						dataList.forEach(x -> {
							TsmpResLog resVo = getTsmpResLogDao().findById(x.getId()).orElse(null);
							if (resVo != null && "Y".equals(resVo.getExeStatus())) {
								String rsKey = orgidKey + "||" + dateKey + "||" + moduleNameKey + "||" + txidKey;
								List<TsmpReqLog> minuteApiAvgTime_rsList = rsMap.get(rsKey);
								if (minuteApiAvgTime_rsList == null) {
									minuteApiAvgTime_rsList = new ArrayList<>();
								}
								minuteApiAvgTime_rsList.add(x);
								rsMap.put(rsKey, minuteApiAvgTime_rsList);
							}
						});
					}
				}
			}
		}

		// reportData
		List<TsmpReportData> reportDataList = new ArrayList<>();
		for (String rsKey : rsMap.keySet()) {
			List<TsmpReqLog> reqList = rsMap.get(rsKey);
			String[] arrKey = rsKey.split("\\|\\|");
			TsmpReportData vo = new TsmpReportData();
			vo.setReportType(ReportTypeEnum.API平均時間計算分析.value());
			vo.setDateTimeRangeType(ReportDateTimeRangeTypeEnum.MINUTE.value());
			vo.setLastRowDateTime(reqList.get(reqList.size() - 1).getRtime());
			vo.setStatisticsStatus("N");
			vo.setStringGroup1(arrKey[2]);
			vo.setStringGroup2(arrKey[3]);
			// 計算intValue1的平均值
			long total = 0;
			int size = 0;
			for (TsmpReqLog x : reqList) {
				TsmpResLog resVo = getTsmpResLogDao().findById(x.getId()).orElse(null);
				if (resVo != null) {
					total += resVo.getElapse();
					size++;
				}
			}

			long intValue1 = 0;
			if (size != 0) {
				double avg = (double) total / size;
				intValue1 = BigDecimal.valueOf(avg).setScale(0, RoundingMode.HALF_UP).longValue();
			}

			vo.setIntValue1(intValue1);
			vo.setIntValue2(Long.valueOf(size));
			vo.setOrgid(arrKey[0]);
			vo.setCreateUser(createUser);
			reportDataList.add(vo);
		}

		return reportDataList;

	}

	// API流量分析
	private List<TsmpReportData> getApiFlowReportData(Map<String, Map<String, List<TsmpReqLog>>> reqMap,
			String createUser) {

		Map<String, List<TsmpReqLog>> rsMap = new TreeMap<>();
		// 取exeStatus為Y和N,並裝入新的MAP
		for (String orgidKey : new TreeSet<String>(reqMap.keySet())) {
			for (String dateKey : new TreeSet<String>(reqMap.get(orgidKey).keySet())) {
				List<TsmpReqLog> dataList = reqMap.get(orgidKey).get(dateKey);
				dataList.forEach(x -> {
					TsmpResLog resVo = getTsmpResLogDao().findById(x.getId()).orElse(null);
					if (resVo != null) {
						String rsKey = orgidKey + "||" + dateKey + "||" + resVo.getExeStatus();
						List<TsmpReqLog> minuteApiFlow_rsList = rsMap.get(rsKey);
						if (minuteApiFlow_rsList == null) {
							minuteApiFlow_rsList = new ArrayList<>();
						}
						minuteApiFlow_rsList.add(x);
						rsMap.put(rsKey, minuteApiFlow_rsList);
					}
				});

			}
		}

		// reportData
		List<TsmpReportData> reportDataList = new ArrayList<>();
		for (String minuteApiFlow_rsKey : rsMap.keySet()) {
			List<TsmpReqLog> reqList = rsMap.get(minuteApiFlow_rsKey);
			String[] arrKey = minuteApiFlow_rsKey.split("\\|\\|");
			TsmpReportData vo = new TsmpReportData();
			vo.setReportType(ReportTypeEnum.API流量分析.value());
			vo.setDateTimeRangeType(ReportDateTimeRangeTypeEnum.MINUTE.value());
			vo.setLastRowDateTime(reqList.get(reqList.size() - 1).getRtime());
			vo.setStatisticsStatus("N");
			vo.setStringGroup1(arrKey[2]);
			vo.setIntValue1((long) reqList.size());
			vo.setOrgid(arrKey[0]);
			vo.setCreateUser(createUser);
			reportDataList.add(vo);
		}

		return reportDataList;

	}

	// Bad Attempt連線報告
	private List<TsmpReportData> getBadAttemptReportData(Map<String, Map<String, List<TsmpReqLog>>> reqMap,
			String createUser) {

		Map<String, List<TsmpReqLog>> rsMap = new TreeMap<>();
		// 只取rcode不為成功,並裝入新的MAP
		for (String orgidKey : new TreeSet<String>(reqMap.keySet())) {
			for (String dateKey : new TreeSet<String>(reqMap.get(orgidKey).keySet())) {
				List<TsmpReqLog> dataList = reqMap.get(orgidKey).get(dateKey);
				dataList.forEach(x -> {
					TsmpResLog resVo = getTsmpResLogDao().findById(x.getId()).orElse(null);
					if (resVo != null) {
						int httpStatus = resVo.getHttpStatus().intValue();
						if (httpStatus <= 0 || (httpStatus >= 400 )) {
							String rsKey = orgidKey + "||" + dateKey + "||" + resVo.getHttpStatus();
							List<TsmpReqLog> minuteBadAttempt_rsList = rsMap.get(rsKey);
							if (minuteBadAttempt_rsList == null) {
								minuteBadAttempt_rsList = new ArrayList<>();
							}
							minuteBadAttempt_rsList.add(x);
							rsMap.put(rsKey, minuteBadAttempt_rsList);
						}
					}
				});

			}
		}

		// reportData
		List<TsmpReportData> reportDataList = new ArrayList<>();
		for (String rsKey : rsMap.keySet()) {
			List<TsmpReqLog> reqList = rsMap.get(rsKey);
			String[] arrKey = rsKey.split("\\|\\|");
			TsmpReportData vo = new TsmpReportData();
			vo.setReportType(ReportTypeEnum.BadAttempt連線報告.value());
			vo.setDateTimeRangeType(ReportDateTimeRangeTypeEnum.MINUTE.value());
			vo.setLastRowDateTime(reqList.get(reqList.size() - 1).getRtime());
			vo.setStatisticsStatus("N");
			vo.setStringGroup1(arrKey[2]);
			vo.setIntValue1((long) reqList.size());
			vo.setOrgid(arrKey[0]);
			vo.setCreateUser(createUser);
			reportDataList.add(vo);
		}

		return reportDataList;

	}
	protected HandleDashboardLogDataService getHandleDashboardLogDataService() {
		return handleDashboardLogDataService;
	}

	protected HandleApiDataAndLogService getHandleApiDataAndLogService() {
		return handleApiDataAndLogService;
	}

	protected TsmpReqLogDao getTsmpReqLogDao() {
		return tsmpReqLogDao;
	}

	protected TsmpResLogDao getTsmpResLogDao() {
		return tsmpResLogDao;
	}

	protected TsmpReportDataDao getTsmpReportDataDao() {
		return tsmpReportDataDao;
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return tsmpSettingDao;
	}

	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return tsmpDpApptJobDao;
	}

}
