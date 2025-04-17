package tpi.dgrv4.dpaa.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CancellationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Service
public class HandleReportDataByYearService {

	private final TsmpReportDataDao tsmpReportDataDao;
    private final TsmpDpApptJobDao tsmpDpApptJobDao;

	@Transactional
	public void exec(Date execDate, String userName, Long jobId) {
		TPILogger.tl.debug("--- Begin HandleReportDataByYearService ---");

		// 現在區間的日期
		Date nowIntervalDate = this.getNowIntervalDate(execDate);
		TPILogger.tl.debug("nowIntervalDate = " + DateTimeUtil.dateTimeToString(nowIntervalDate, DateTimeFormatEnum.西元年月日時分秒).orElse(execDate.toString()));
		
		ReportUtil reportUtil = new ReportUtil();
		
		// 取得API使用次數資料
		List<TsmpReportData> monthDataList = getTsmpReportDataDao()
				.findByReportTypeAndDateTimeRangeTypeAndStatisticsStatusAndLastRowDateTimeLessThanOrderByLastRowDateTimeAsc(
						ReportTypeEnum.API使用次數統計.value(), ReportDateTimeRangeTypeEnum.MONTH.value(), "N",
						nowIntervalDate);
		TPILogger.tl.debug("monthDataList1.size = " +  monthDataList.size());
		for(TsmpReportData vo : monthDataList) {
			if(vo.getOrgid() == null) {
				vo.setOrgid("");
			}
		}
		
		// 取得API使用次數reportData
		List<TsmpReportData> yearDataList = reportUtil.getApiUseNumReportData(monthDataList, ReportDateTimeRangeTypeEnum.YEAR, userName);
		getTsmpReportDataDao().saveAll(yearDataList);
		// 變更為已統計
		monthDataList.forEach(vo -> {
			vo.setStatisticsStatus("Y");
			vo.setUpdateDateTime(DateTimeUtil.now());
			vo.setUpdateUser(userName);
			getTsmpReportDataDao().save(vo);
		});
		// ====================================================================================================================
		// 取得API次數-時間分析資料
		monthDataList = getTsmpReportDataDao()
				.findByReportTypeAndDateTimeRangeTypeAndStatisticsStatusAndLastRowDateTimeLessThanOrderByLastRowDateTimeAsc(
						ReportTypeEnum.API次數_時間分析.value(), ReportDateTimeRangeTypeEnum.MONTH.value(), "N",
						nowIntervalDate);
		TPILogger.tl.debug("monthDataList2.size = " +  monthDataList.size());
		for(TsmpReportData vo : monthDataList) {
			if(vo.getOrgid() == null) {
				vo.setOrgid("");
			}
		}
		
		// 取得API次數-時間分析reportData
		yearDataList = reportUtil.getApiNumTimeReportData(monthDataList, ReportDateTimeRangeTypeEnum.YEAR, userName);
		getTsmpReportDataDao().saveAll(yearDataList);

		// 變更為已統計
		monthDataList.forEach(vo -> {
			vo.setStatisticsStatus("Y");
			vo.setUpdateDateTime(DateTimeUtil.now());
			vo.setUpdateUser(userName);
			getTsmpReportDataDao().save(vo);
		});
		// ====================================================================================================================
		// 取得API平均時間計算分析資料
		monthDataList = getTsmpReportDataDao()
				.findByReportTypeAndDateTimeRangeTypeAndStatisticsStatusAndLastRowDateTimeLessThanOrderByLastRowDateTimeAsc(
						ReportTypeEnum.API平均時間計算分析.value(), ReportDateTimeRangeTypeEnum.MONTH.value(), "N",
						nowIntervalDate);
		TPILogger.tl.debug("monthDataList3.size = " +  monthDataList.size());
		for(TsmpReportData vo : monthDataList) {
			if(vo.getOrgid() == null) {
				vo.setOrgid("");
			}
		}
		
		// 取得API平均時間計算分析reportData
		yearDataList = reportUtil.getApiAvgTimeReportData(monthDataList, ReportDateTimeRangeTypeEnum.YEAR, userName);
		getTsmpReportDataDao().saveAll(yearDataList);

		// 變更為已統計
		monthDataList.forEach(vo -> {
			vo.setStatisticsStatus("Y");
			vo.setUpdateDateTime(DateTimeUtil.now());
			vo.setUpdateUser(userName);
			getTsmpReportDataDao().save(vo);
		});

		// ====================================================================================================================
		// 取得API流量分析資料
		monthDataList = getTsmpReportDataDao()
				.findByReportTypeAndDateTimeRangeTypeAndStatisticsStatusAndLastRowDateTimeLessThanOrderByLastRowDateTimeAsc(
						ReportTypeEnum.API流量分析.value(), ReportDateTimeRangeTypeEnum.MONTH.value(), "N",
						nowIntervalDate);
		TPILogger.tl.debug("monthDataList4.size = " +  monthDataList.size());
		for(TsmpReportData vo : monthDataList) {
			if(vo.getOrgid() == null) {
				vo.setOrgid("");
			}
		}
		
		// 取得API流量分析reportData
		yearDataList = reportUtil.getApiFlowReportData(monthDataList, ReportDateTimeRangeTypeEnum.YEAR, userName);
		getTsmpReportDataDao().saveAll(yearDataList);

		// 變更為已統計
		monthDataList.forEach(vo -> {
			vo.setStatisticsStatus("Y");
			vo.setUpdateDateTime(DateTimeUtil.now());
			vo.setUpdateUser(userName);
			getTsmpReportDataDao().save(vo);
		});
		// ====================================================================================================================
		// 取得Bad Attempt連線報告資料
		monthDataList = getTsmpReportDataDao()
				.findByReportTypeAndDateTimeRangeTypeAndStatisticsStatusAndLastRowDateTimeLessThanOrderByLastRowDateTimeAsc(
						ReportTypeEnum.BadAttempt連線報告.value(), ReportDateTimeRangeTypeEnum.MONTH.value(), "N",
						nowIntervalDate);
		TPILogger.tl.debug("monthDataList5.size = " +  monthDataList.size());
		for(TsmpReportData vo : monthDataList) {
			if(vo.getOrgid() == null) {
				vo.setOrgid("");
			}
		}
		
		// 取得Bad Attempt連線報告reportData
		yearDataList = reportUtil.getBadAttemptReportData(monthDataList, ReportDateTimeRangeTypeEnum.YEAR, userName);
		getTsmpReportDataDao().saveAll(yearDataList);

		// 變更為已統計
		monthDataList.forEach(vo -> {
			vo.setStatisticsStatus("Y");
			vo.setUpdateDateTime(DateTimeUtil.now());
			vo.setUpdateUser(userName);
			getTsmpReportDataDao().save(vo);
		});
		// =====================================================================================================================
		// 刪除月資料
		Date deleteDate = getDeleteIntervalDate(execDate);
		TPILogger.tl.debug("deleteDate = " + DateTimeUtil.dateTimeToString(deleteDate, DateTimeFormatEnum.西元年月日時分秒).orElse(execDate.toString()));
		getTsmpReportDataDao().deleteByDateTimeRangeTypeAndLastRowDateTimeLessThan(
				ReportDateTimeRangeTypeEnum.MONTH.value(), deleteDate);

		checkJobStatus(jobId);
		TPILogger.tl.debug("--- Finish HandleReportDataByYearService ---");
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
		String yearNow_strDate = DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日時分).orElseThrow(TsmpDpAaRtnCode._1295::throwing);
//		String mm = yearNow_strDate.substring(14);
//		String HH = yearNow_strDate.substring(11,13);
//		String dd = yearNow_strDate.substring(8,10);
//		String month = yearNow_strDate.substring(5,7);
//		// 如果在1月1號0點0分就回上一個區間
//		if (Integer.parseInt(month) == 1 && Integer.parseInt(dd) == 1 && Integer.parseInt(HH) == 0 && Integer.parseInt(mm) == 0) {
//			Calendar nowTime = Calendar.getInstance();
//			nowTime.setTime(nowDate);
//			nowTime.add(Calendar.YEAR, -1);
//			nowDate = nowTime.getTime();
//			yearNow_strDate = DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日時分).get();
//		}
//
		yearNow_strDate = yearNow_strDate.substring(0, 5);
		yearNow_strDate = yearNow_strDate + "01-01 00:00:00.000";
		Date nowIntervalDate = DateTimeUtil.stringToDateTime(yearNow_strDate, DateTimeFormatEnum.西元年月日時分秒毫秒).orElseThrow(TsmpDpAaRtnCode._1295::throwing);

		return nowIntervalDate;
	}

	private Date getDeleteIntervalDate(Date nowDate) {
		String yearDel_strDate = DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日時分).orElseThrow(TsmpDpAaRtnCode._1295::throwing);
		String mm = yearDel_strDate.substring(14);
		String HH = yearDel_strDate.substring(11,13);
		String dd = yearDel_strDate.substring(8,10);
		String month = yearDel_strDate.substring(5,7);
		// 如果在1月1號0點0分就回上一個區間
		if (Integer.parseInt(month) == 1 && Integer.parseInt(dd) == 1 && Integer.parseInt(HH) == 0 && Integer.parseInt(mm) == 0) {
			Calendar nowTime = Calendar.getInstance();
			nowTime.setTime(nowDate);
			nowTime.add(Calendar.YEAR, -1);
			nowDate = nowTime.getTime();
		}

		// 去年
		Calendar nowTime = Calendar.getInstance();
		nowTime.setTime(nowDate);
		nowTime.add(Calendar.YEAR, -1);
		nowDate = nowTime.getTime();
		yearDel_strDate = DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日時分).orElseThrow(TsmpDpAaRtnCode._1295::throwing);

		yearDel_strDate = yearDel_strDate.substring(0, 5);
		yearDel_strDate = yearDel_strDate + "01-01 00:00:00.000";
		Date nowIntervalDate = DateTimeUtil.stringToDateTime(yearDel_strDate, DateTimeFormatEnum.西元年月日時分秒毫秒).orElseThrow(TsmpDpAaRtnCode._1295::throwing);

		return nowIntervalDate;

	}

	protected TsmpReportDataDao getTsmpReportDataDao() {
		return tsmpReportDataDao;
	}
	
	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return tsmpDpApptJobDao;
	}

}
