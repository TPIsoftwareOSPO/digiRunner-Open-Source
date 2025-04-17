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
import tpi.dgrv4.entity.entity.jpql.TsmpReqLog;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpReportDataDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class HandleReportDataByHourService {
	private TsmpReportDataDao tsmpReportDataDao;
    private TsmpDpApptJobDao tsmpDpApptJobDao;

    @Autowired
	public HandleReportDataByHourService(TsmpReportDataDao tsmpReportDataDao, TsmpDpApptJobDao tsmpDpApptJobDao) {
		super();
		this.tsmpReportDataDao = tsmpReportDataDao;
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
	}

	@Transactional
	public void exec(Date execDate, String userName, Long jobId) {

		TPILogger.tl.debug("--- Begin HandleReportDataByHourService ---");
		
		// 現在區間的日期
		Date nowIntervalDate = this.getNowIntervalDate(execDate);
		TPILogger.tl.debug("nowIntervalDate = " + DateTimeUtil.dateTimeToString(nowIntervalDate, DateTimeFormatEnum.西元年月日時分秒).orElse(execDate.toString()));
		
		ReportUtil reportUtil = new ReportUtil();
		
		// 取得API使用次數資料
		List<TsmpReportData> minuteDataList = getTsmpReportDataDao()
				.findByReportTypeAndDateTimeRangeTypeAndStatisticsStatusAndLastRowDateTimeLessThanOrderByLastRowDateTimeAsc(
						ReportTypeEnum.API使用次數統計.value(), ReportDateTimeRangeTypeEnum.MINUTE.value(), "N",
						nowIntervalDate);
		TPILogger.tl.debug("minuteDataList1.size = " + minuteDataList.size());
		for(TsmpReportData vo : minuteDataList) {
			if(vo.getOrgid() == null) {
				vo.setOrgid("");
			}
		}
		
		// 取得API使用次數reportData
		List<TsmpReportData> hourDataList = reportUtil.getApiUseNumReportData(minuteDataList, ReportDateTimeRangeTypeEnum.HOUR, userName);
		getTsmpReportDataDao().saveAll(hourDataList);
		// 變更為已統計
		minuteDataList.forEach(vo -> {
			vo.setStatisticsStatus("Y");
			vo.setUpdateDateTime(DateTimeUtil.now());
			vo.setUpdateUser(userName);
			getTsmpReportDataDao().save(vo);
		});
		// ====================================================================================================================
		// 取得API次數-時間分析資料
		minuteDataList = getTsmpReportDataDao()
				.findByReportTypeAndDateTimeRangeTypeAndStatisticsStatusAndLastRowDateTimeLessThanOrderByLastRowDateTimeAsc(
						ReportTypeEnum.API次數_時間分析.value(), ReportDateTimeRangeTypeEnum.MINUTE.value(), "N",
						nowIntervalDate);
		TPILogger.tl.debug("minuteDataList2.size = " + minuteDataList.size());
		for(TsmpReportData vo : minuteDataList) {
			if(vo.getOrgid() == null) {
				vo.setOrgid("");
			}
		}
		
		// 取得API次數-時間分析reportData
		hourDataList = reportUtil.getApiNumTimeReportData(minuteDataList, ReportDateTimeRangeTypeEnum.HOUR, userName);
		getTsmpReportDataDao().saveAll(hourDataList);

		// 變更為已統計
		minuteDataList.forEach(vo -> {
			vo.setStatisticsStatus("Y");
			vo.setUpdateDateTime(DateTimeUtil.now());
			vo.setUpdateUser(userName);
			getTsmpReportDataDao().save(vo);
		});
		// ====================================================================================================================
		// 取得API平均時間計算分析資料
		minuteDataList = getTsmpReportDataDao()
				.findByReportTypeAndDateTimeRangeTypeAndStatisticsStatusAndLastRowDateTimeLessThanOrderByLastRowDateTimeAsc(
						ReportTypeEnum.API平均時間計算分析.value(), ReportDateTimeRangeTypeEnum.MINUTE.value(), "N",
						nowIntervalDate);

		TPILogger.tl.debug("minuteDataList3.size = " + minuteDataList.size());
		for(TsmpReportData vo : minuteDataList) {
			if(vo.getOrgid() == null) {
				vo.setOrgid("");
			}
		}
		
		// 取得API平均時間計算分析reportData
		hourDataList = reportUtil.getApiAvgTimeReportData(minuteDataList, ReportDateTimeRangeTypeEnum.HOUR, userName);
		getTsmpReportDataDao().saveAll(hourDataList);

		// 變更為已統計
		minuteDataList.forEach(vo -> {
			vo.setStatisticsStatus("Y");
			vo.setUpdateDateTime(DateTimeUtil.now());
			vo.setUpdateUser(userName);
			getTsmpReportDataDao().save(vo);
		});

		// ====================================================================================================================
		// 取得API流量分析資料
		minuteDataList = getTsmpReportDataDao()
				.findByReportTypeAndDateTimeRangeTypeAndStatisticsStatusAndLastRowDateTimeLessThanOrderByLastRowDateTimeAsc(
						ReportTypeEnum.API流量分析.value(), ReportDateTimeRangeTypeEnum.MINUTE.value(), "N",
						nowIntervalDate);
		TPILogger.tl.debug("minuteDataList4.size = " + minuteDataList.size());
		for(TsmpReportData vo : minuteDataList) {
			if(vo.getOrgid() == null) {
				vo.setOrgid("");
			}
		}
		
		// 取得API流量分析reportData
		hourDataList = reportUtil.getApiFlowReportData(minuteDataList, ReportDateTimeRangeTypeEnum.HOUR, userName);
		getTsmpReportDataDao().saveAll(hourDataList);

		// 變更為已統計
		minuteDataList.forEach(vo -> {
			vo.setStatisticsStatus("Y");
			vo.setUpdateDateTime(DateTimeUtil.now());
			vo.setUpdateUser(userName);
			getTsmpReportDataDao().save(vo);
		});
		// ====================================================================================================================
		// 取得Bad Attempt連線報告資料
		minuteDataList = getTsmpReportDataDao()
				.findByReportTypeAndDateTimeRangeTypeAndStatisticsStatusAndLastRowDateTimeLessThanOrderByLastRowDateTimeAsc(
						ReportTypeEnum.BadAttempt連線報告.value(), ReportDateTimeRangeTypeEnum.MINUTE.value(), "N",
						nowIntervalDate);
		TPILogger.tl.debug("minuteDataList5.size = " + minuteDataList.size());
		for(TsmpReportData vo : minuteDataList) {
			if(vo.getOrgid() == null) {
				vo.setOrgid("");
			}
		}
		
		// 取得Bad Attempt連線報告reportData
		hourDataList = reportUtil.getBadAttemptReportData(minuteDataList, ReportDateTimeRangeTypeEnum.HOUR, userName);
		getTsmpReportDataDao().saveAll(hourDataList);

		// 變更為已統計
		minuteDataList.forEach(vo -> {
			vo.setStatisticsStatus("Y");
			vo.setUpdateDateTime(DateTimeUtil.now());
			vo.setUpdateUser(userName);
			getTsmpReportDataDao().save(vo);
		});
		// =====================================================================================================================
		// 刪除分鐘資料
		Date deleteDate = getDeleteIntervalDate(execDate);
		TPILogger.tl.debug("deleteDate = " + DateTimeUtil.dateTimeToString(deleteDate, DateTimeFormatEnum.西元年月日時分秒).orElse(execDate.toString()));
		getTsmpReportDataDao().deleteByDateTimeRangeTypeAndLastRowDateTimeLessThan(
				ReportDateTimeRangeTypeEnum.MINUTE.value(), deleteDate);

		checkJobStatus(jobId);
		TPILogger.tl.debug("--- Finish HandleReportDataByHourService ---");
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
//		// 如果剛好整點就回上一個區間,EX:01:00,02:00,03:00.....
//		if (Integer.parseInt(mm) == 0) {
//			Calendar nowTime = Calendar.getInstance();
//			nowTime.setTime(nowDate);
//			nowTime.add(Calendar.HOUR, -1);
//			nowDate = nowTime.getTime();
//			strDate = DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日時分).get();
//		}
//
		strDate = strDate.substring(0, 14);
		strDate = strDate + "00:00.000";
		Date nowIntervalDate = DateTimeUtil.stringToDateTime(strDate, DateTimeFormatEnum.西元年月日時分秒毫秒).orElseThrow(TsmpDpAaRtnCode._1295::throwing);

		return nowIntervalDate;
	}

	private Date getDeleteIntervalDate(Date nowDate) {
		String strDate = DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日時分).orElseThrow(TsmpDpAaRtnCode._1295::throwing);
		String m = strDate.substring(15);
		// 如果剛好滿10分就回上一個區間
		if ("0".equals(m)) {
			Calendar nowTime = Calendar.getInstance();
			nowTime.setTime(nowDate);
			nowTime.add(Calendar.MINUTE, -10);
			nowDate = nowTime.getTime();
		}

		// 前三天
		Calendar nowTime = Calendar.getInstance();
		nowTime.setTime(nowDate);
		nowTime.add(Calendar.DATE, -3);
		nowDate = nowTime.getTime();
		strDate = DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日時分).orElseThrow(TsmpDpAaRtnCode._1295::throwing);

		strDate = strDate.substring(0, 15);
		strDate = strDate + "0:00.000";
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
