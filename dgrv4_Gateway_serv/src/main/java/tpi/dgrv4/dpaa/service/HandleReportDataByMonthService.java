package tpi.dgrv4.dpaa.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CancellationException;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.ReportDateTimeRangeTypeEnum;
import tpi.dgrv4.common.constant.ReportTypeEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.util.ReportUtil;
import tpi.dgrv4.entity.entity.jpql.TsmpReportData;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpReportDataDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class HandleReportDataByMonthService {
	private TsmpReportDataDao tsmpReportDataDao;
    private TsmpDpApptJobDao tsmpDpApptJobDao;

    @Autowired
	public HandleReportDataByMonthService(TsmpReportDataDao tsmpReportDataDao, TsmpDpApptJobDao tsmpDpApptJobDao) {
		super();
		this.tsmpReportDataDao = tsmpReportDataDao;
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
	}

	@Transactional
	public void exec(Date execDate, String userName, Long jobId) {
		TPILogger.tl.debug("--- Begin HandleReportDataByMonthService ---");

		// 現在區間的日期
		Date nowIntervalDate = this.getNowIntervalDate(execDate);
		TPILogger.tl.debug("nowIntervalDate = " + DateTimeUtil.dateTimeToString(nowIntervalDate, DateTimeFormatEnum.西元年月日時分秒).orElse(execDate.toString()));
		
		ReportUtil reportUtil = new ReportUtil();
		
		// 取得API使用次數資料
		List<TsmpReportData> dayDataList = getTsmpReportDataDao()
				.findByReportTypeAndDateTimeRangeTypeAndStatisticsStatusAndLastRowDateTimeLessThanOrderByLastRowDateTimeAsc(
						ReportTypeEnum.API使用次數統計.value(), ReportDateTimeRangeTypeEnum.DAY.value(), "N",
						nowIntervalDate);
		TPILogger.tl.debug("dayDataList1.size = " +  dayDataList.size());
		for(TsmpReportData vo : dayDataList) {
			if(vo.getOrgid() == null) {
				vo.setOrgid("");
			}
		}
		
		// 取得API使用次數reportData
		List<TsmpReportData> monthDataList = reportUtil.getApiUseNumReportData(dayDataList, ReportDateTimeRangeTypeEnum.MONTH, userName);
		getTsmpReportDataDao().saveAll(monthDataList);
		// 變更為已統計
		dayDataList.forEach(vo -> {
			vo.setStatisticsStatus("Y");
			vo.setUpdateDateTime(DateTimeUtil.now());
			vo.setUpdateUser(userName);
			getTsmpReportDataDao().save(vo);
		});
		// ====================================================================================================================
		// 取得API次數-時間分析資料
		dayDataList = getTsmpReportDataDao()
				.findByReportTypeAndDateTimeRangeTypeAndStatisticsStatusAndLastRowDateTimeLessThanOrderByLastRowDateTimeAsc(
						ReportTypeEnum.API次數_時間分析.value(), ReportDateTimeRangeTypeEnum.DAY.value(), "N",
						nowIntervalDate);
		TPILogger.tl.debug("dayDataList2.size = " +  dayDataList.size());
		for(TsmpReportData vo : dayDataList) {
			if(vo.getOrgid() == null) {
				vo.setOrgid("");
			}
		}
		
		// 取得API次數-時間分析reportData
		monthDataList = reportUtil.getApiNumTimeReportData(dayDataList, ReportDateTimeRangeTypeEnum.MONTH, userName);
		getTsmpReportDataDao().saveAll(monthDataList);

		// 變更為已統計
		dayDataList.forEach(vo -> {
			vo.setStatisticsStatus("Y");
			vo.setUpdateDateTime(DateTimeUtil.now());
			vo.setUpdateUser(userName);
			getTsmpReportDataDao().save(vo);
		});
		// ====================================================================================================================
		// 取得API平均時間計算分析資料
		dayDataList = getTsmpReportDataDao()
				.findByReportTypeAndDateTimeRangeTypeAndStatisticsStatusAndLastRowDateTimeLessThanOrderByLastRowDateTimeAsc(
						ReportTypeEnum.API平均時間計算分析.value(), ReportDateTimeRangeTypeEnum.DAY.value(), "N",
						nowIntervalDate);
		TPILogger.tl.debug("dayDataList3.size = " +  dayDataList.size());
		for(TsmpReportData vo : dayDataList) {
			if(vo.getOrgid() == null) {
				vo.setOrgid("");
			}
		}
		
		// 取得API平均時間計算分析reportData
		monthDataList = reportUtil.getApiAvgTimeReportData(dayDataList, ReportDateTimeRangeTypeEnum.MONTH, userName);
		getTsmpReportDataDao().saveAll(monthDataList);

		// 變更為已統計
		dayDataList.forEach(vo -> {
			vo.setStatisticsStatus("Y");
			vo.setUpdateDateTime(DateTimeUtil.now());
			vo.setUpdateUser(userName);
			getTsmpReportDataDao().save(vo);
		});

		// ====================================================================================================================
		// 取得API流量分析資料
		dayDataList = getTsmpReportDataDao()
				.findByReportTypeAndDateTimeRangeTypeAndStatisticsStatusAndLastRowDateTimeLessThanOrderByLastRowDateTimeAsc(
						ReportTypeEnum.API流量分析.value(), ReportDateTimeRangeTypeEnum.DAY.value(), "N",
						nowIntervalDate);
		TPILogger.tl.debug("dayDataList4.size = " +  dayDataList.size());
		for(TsmpReportData vo : dayDataList) {
			if(vo.getOrgid() == null) {
				vo.setOrgid("");
			}
		}
		
		// 取得API流量分析reportData
		monthDataList = reportUtil.getApiFlowReportData(dayDataList, ReportDateTimeRangeTypeEnum.MONTH, userName);
		getTsmpReportDataDao().saveAll(monthDataList);

		// 變更為已統計
		dayDataList.forEach(vo -> {
			vo.setStatisticsStatus("Y");
			vo.setUpdateDateTime(DateTimeUtil.now());
			vo.setUpdateUser(userName);
			getTsmpReportDataDao().save(vo);
		});
		// ====================================================================================================================
		// 取得Bad Attempt連線報告資料
		dayDataList = getTsmpReportDataDao()
				.findByReportTypeAndDateTimeRangeTypeAndStatisticsStatusAndLastRowDateTimeLessThanOrderByLastRowDateTimeAsc(
						ReportTypeEnum.BadAttempt連線報告.value(), ReportDateTimeRangeTypeEnum.DAY.value(), "N",
						nowIntervalDate);
		TPILogger.tl.debug("dayDataList5.size = " +  dayDataList.size());
		for(TsmpReportData vo : dayDataList) {
			if(vo.getOrgid() == null) {
				vo.setOrgid("");
			}
		}
		
		// 取得Bad Attempt連線報告reportData
		monthDataList = reportUtil.getBadAttemptReportData(dayDataList, ReportDateTimeRangeTypeEnum.MONTH, userName);
		getTsmpReportDataDao().saveAll(monthDataList);

		// 變更為已統計
		dayDataList.forEach(vo -> {
			vo.setStatisticsStatus("Y");
			vo.setUpdateDateTime(DateTimeUtil.now());
			vo.setUpdateUser(userName);
			getTsmpReportDataDao().save(vo);
		});
		// =====================================================================================================================
		// 刪除天資料
		Date deleteDate = getDeleteIntervalDate(execDate);
		TPILogger.tl.debug("deleteDate = " + DateTimeUtil.dateTimeToString(deleteDate, DateTimeFormatEnum.西元年月日時分秒).orElse(execDate.toString()));
		getTsmpReportDataDao().deleteByDateTimeRangeTypeAndLastRowDateTimeLessThan(
				ReportDateTimeRangeTypeEnum.DAY.value(), deleteDate);

		checkJobStatus(jobId);
		TPILogger.tl.debug("--- Finish HandleReportDataByMonthService ---");
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
//		String mm = strDate.substring(14);
//		String HH = strDate.substring(11,13);
//		String dd = strDate.substring(8,10);
//		// 如果在1號0點0分就回上一個區間
//		if (Integer.parseInt(dd) == 1 && Integer.parseInt(HH) == 0 && Integer.parseInt(mm) == 0) {
//			Calendar nowTime = Calendar.getInstance();
//			nowTime.setTime(nowDate);
//			nowTime.add(Calendar.MONTH, -1);
//			nowDate = nowTime.getTime();
//			strDate = DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日時分).get();
//		}
//
		strDate = strDate.substring(0, 8);
		strDate = strDate + "01 00:00:00.000";
		Date month_nowIntervalDate = DateTimeUtil.stringToDateTime(strDate, DateTimeFormatEnum.西元年月日時分秒毫秒).orElseThrow(TsmpDpAaRtnCode._1295::throwing);

		return month_nowIntervalDate;
	}

	private Date getDeleteIntervalDate(Date nowDate) {
		String strDate = DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日時分).orElseThrow(TsmpDpAaRtnCode._1295::throwing);
		String mm = strDate.substring(14);
		String HH = strDate.substring(11,13);
		String dd = strDate.substring(8,10);
		// 如果在1號0點0分就回上一個區間
		if (Integer.parseInt(dd) == 1 && Integer.parseInt(HH) == 0 && Integer.parseInt(mm) == 0) {
			Calendar nowTime = Calendar.getInstance();
			nowTime.setTime(nowDate);
			nowTime.add(Calendar.MONTH, -1);
			nowDate = nowTime.getTime();
		}

		// 前三個月
		Calendar nowTime = Calendar.getInstance();
		nowTime.setTime(nowDate);
		nowTime.add(Calendar.MONTH, -3);
		nowDate = nowTime.getTime();
		strDate = DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日時分).orElseThrow(TsmpDpAaRtnCode._1295::throwing);

		strDate = strDate.substring(0, 8);
		strDate = strDate + "01 00:00:00.000";
		Date nowIntervalDate = DateTimeUtil.stringToDateTime(strDate, DateTimeFormatEnum.西元年月日時分秒毫秒).orElseThrow(TsmpDpAaRtnCode._1295::throwing);

		return nowIntervalDate;

	}

	protected TsmpReportDataDao getTsmpReportDataDao() {
		return tsmpReportDataDao;
	}

	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return tsmpDpApptJobDao;
	}
	
	

}
