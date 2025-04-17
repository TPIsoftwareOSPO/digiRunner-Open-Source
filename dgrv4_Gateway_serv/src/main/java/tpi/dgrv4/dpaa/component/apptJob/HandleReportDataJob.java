package tpi.dgrv4.dpaa.component.apptJob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.*;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpSettingService;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CancellationException;

/**
 * 已不使用 HandleReportDataJob 報表排程, 改用即時 dashboard
 */

/**
 * 處理報表資料JOB
 * 1.ES_LOG_DISABLE =false,不管TSMP_APILOG_FORCE_WRITE_RDB的值(若為true會有紀錄但不做統計,直到執行RDB統計才做),就執行ES
 * 2.ES_LOG_DISABLE=true,而TSMP_APILOG_FORCE_WRITE_RDB=true就執行RDB
 * 3.dashboard熱冷門的計算就沒分ES/RDB, 因為來源是一樣的
 * 
 * @author tom
 *
 */
@SuppressWarnings("serial")
public class HandleReportDataJob extends ApptJob {

	private HandleReportDataByMinuteService handleReportDataByMinuteService;
	private HandleReportDataByHourService handleReportDataByHourService;
	private HandleReportDataByDayService handleReportDataByDayService;
	private HandleReportDataByMonthService handleReportDataByMonthService;
	private HandleReportDataByYearService handleReportDataByYearService;
	private HandleDashboardLogDataService handleDashboardLogDataService;
	private HandleDashboardDataService handleDashboardDataService;
	private HandleDashboardDataByYearService handleDashboardDataByYearService;
	private TsmpSettingService tsmpSettingService;
	private HandleESApiDataService handleESApiDataService;
	private TsmpDpApptJobDao tsmpDpApptJobDao;
	private HandleESExpiredDataService handleESExpiredDataService;
	
	@Autowired
	public HandleReportDataJob(TsmpDpApptJob tsmpDpApptJob, ApptJobDispatcher apptJobDispatcher,
			TsmpDpApptJobDao tsmpDpApptJobDao, HandleReportDataByMinuteService handleReportDataByMinuteService,
			HandleReportDataByHourService handleReportDataByHourService,
			HandleReportDataByDayService handleReportDataByDayService,
			HandleReportDataByMonthService handleReportDataByMonthService,
			HandleReportDataByYearService handleReportDataByYearService,
			HandleDashboardLogDataService handleDashboardLogDataService,
			HandleDashboardDataService handleDashboardDataService,
			HandleDashboardDataByYearService handleDashboardDataByYearService, TsmpSettingService tsmpSettingService,
			HandleESApiDataService handleESApiDataService,
			HandleESExpiredDataService handleESExpiredDataService) {  
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		
		this.handleReportDataByMinuteService = handleReportDataByMinuteService;
		this.handleReportDataByHourService = handleReportDataByHourService;
		this.handleReportDataByDayService = handleReportDataByDayService;
		this.handleReportDataByMonthService = handleReportDataByMonthService;
		this.handleReportDataByYearService = handleReportDataByYearService;
		this.handleDashboardLogDataService = handleDashboardLogDataService;
		this.handleDashboardDataService = handleDashboardDataService;
		this.handleDashboardDataByYearService = handleDashboardDataByYearService;
		this.tsmpSettingService = tsmpSettingService;
		this.handleESApiDataService = handleESApiDataService;
		this.handleESExpiredDataService = handleESExpiredDataService;
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
	}


	
//	 1.ES_LOG_DISABLE =false,不管TSMP_APILOG_FORCE_WRITE_RDB的值(若為true會有紀錄但不做統計,直到執行RDB統計才做),就執行ES
//	 2.ES_LOG_DISABLE=true,而TSMP_APILOG_FORCE_WRITE_RDB=true就執行RDB
//	 3.dashboard熱冷門的計算就沒分ES/RDB, 因為來源是一樣的 
	
	@Override
	public String runApptJob() throws Exception {
		Long id = this.getTsmpDpApptJob().getApptJobId();

		// print info , remember delete that !
		String temp = "\nId::" + getTsmpDpApptJob().getApptJobId() + "\n";
		temp += "Status::" + getTsmpDpApptJob().getStatus() + "\n";
		temp += "Job Type::" + getTsmpDpApptJob().getRefItemNo() + "\n";
		TPILogger.tl.info(temp);
		
		
		// RefItemNo - 參照項目編號要等於 "REPORT_BATCH"
		// Status - 狀態要等於 "R"
		// ApptJobIdNot - 工作 ID 不等於傳入的 id 參數
		boolean hasReportRunning = getTsmpDpApptJobDao().
				existsByRefItemNoAndStatusAndApptJobIdNot("REPORT_BATCH", "R", id);
		TPILogger.tl.info("...Has Repor Job Running....::" + hasReportRunning );
		if (hasReportRunning) {
			String msg = TPILogger.lcUserName + " :: REPORT_BATCH job status running id:" + id;
			TPILogger.tl.info(msg);
			TPILogger.tl.info(StackTraceUtil.logStackTrace(new Exception(msg)));
			throw new CancellationException(msg);
		} else {
			boolean isEs = false;
			String rdbVal = "false";
			try {
				rdbVal = getTsmpSettingService().getVal_TSMP_APILOG_FORCE_WRITE_RDB();
				isEs = !getTsmpSettingService().getVal_ES_LOG_DISABLE(); // 給予 反向 值
				//RDB-log 未啟用 && ES-log 未啟用, 則 '取消執行'
				if("false".equalsIgnoreCase(rdbVal) && !isEs) {
					throw new CancellationException();
				}
				
				isEs = false; // 由於 ES-log 會造成 memory leak (10W) 筆 , 故先強制轉為 RDB-log 

				String strParam = this.getTsmpDpApptJob().getInParams();
				if(isEs) {
					TPILogger.tl.info("--- Begin ES HandleReportDataJob ---");
					
					Date execDate = getExecuteDate();
					TPILogger.tl.info(
							"execute time : " + DateTimeUtil.dateTimeToString(execDate, DateTimeFormatEnum.西元年月日時分秒).orElse(String.valueOf(TsmpDpAaRtnCode._1295)));

					if(StringUtils.hasText(strParam)) {
						execDate = DateTimeUtil.stringToDateTime(strParam, DateTimeFormatEnum.西元年月日時分秒_2).orElse(null);
						TPILogger.tl.info(
								"inParams execute time : " + DateTimeUtil.dateTimeToString(execDate, DateTimeFormatEnum.西元年月日時分秒).orElse(String.valueOf(TsmpDpAaRtnCode._1295)));
						getHandleESExpiredDataService().exec(execDate, id);
						step("2/2P");
						getHandleDashboardDataService().exec(execDate, isEs, id);
					}else {
						String stepMax = "/3";
						TPILogger.tl.info("Report Data Job:: 1" + stepMax + " (ES Expired Data)");
						step(1 + stepMax);
						getHandleESExpiredDataService().exec(execDate, id);
						
						TPILogger.tl.info("Report Data Job:: 2" + stepMax + " (ES Api Data)");
						step(2 + stepMax);
						getHandleESApiDataService().exec(execDate, id);
						
						TPILogger.tl.info("Report Data Job:: 3" + stepMax + " (Dashboard Data)");
						step(3 + stepMax);
						getHandleDashboardDataService().exec(execDate, isEs, id);
					}
	
					

				}else {
					TPILogger.tl.info("--- Begin RDB HandleReportDataJob ---");
	
					Date execDate = getExecuteDate();
					TPILogger.tl.info(
							"execute time : " + DateTimeUtil.dateTimeToString(execDate, DateTimeFormatEnum.西元年月日時分秒).orElse(String.valueOf(TsmpDpAaRtnCode._1295)));
					String createUser = getTsmpDpApptJob().getCreateUser();
	
					String stepMax = "/7";
	
					if(StringUtils.hasText(strParam)) {
						execDate = DateTimeUtil.stringToDateTime(strParam, DateTimeFormatEnum.西元年月日時分秒_2).orElse(null);
						TPILogger.tl.info(
								"inParams execute time : " + DateTimeUtil.dateTimeToString(execDate, DateTimeFormatEnum.西元年月日時分秒).orElse(String.valueOf(TsmpDpAaRtnCode._1295)));
						step("6/6P");
						getHandleDashboardDataService().exec(execDate, isEs, id);
					}else {
						step(1 + stepMax);
						getHandleReportDataByMinuteService().exec(execDate, createUser, id);
		
						step(2 + stepMax);
						getHandleReportDataByHourService().exec(execDate, createUser, id);
		
						step(3 + stepMax);
						getHandleReportDataByDayService().exec(execDate, createUser, id);
		
						step(4 + stepMax);
						getHandleReportDataByMonthService().exec(execDate, createUser, id);
		
						step(5 + stepMax);
						getHandleReportDataByYearService().exec(execDate, createUser, id);
		
						step(6 + stepMax); // 這一步跑很久
						getHandleDashboardDataService().exec(execDate, isEs, id);
						step(7 + stepMax); // finish 
					}
					
				}

			} catch(CancellationException e) {
				throw e;
			} catch(ObjectOptimisticLockingFailureException e) {
				TPILogger.tl.warn(StackTraceUtil.logTpiShortStackTrace(e));
				throw e;
			} catch (Exception e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
				throw e;
			} finally {
				if(isEs) {
					TPILogger.tl.info("--- Finish ES HandleReportDataJob ---");
				}else if(!"false".equalsIgnoreCase(rdbVal)){
					TPILogger.tl.info("--- Finish RDB HandleReportDataJob ---");
				}
				
			}
			return "SUCCESS";
		}

	}

	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return this.tsmpDpApptJobDao;
	}

	protected HandleDashboardLogDataService getHandleDashboardLogDataService() {
		return handleDashboardLogDataService;
	}

	protected Date getExecuteDate() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c.getTime();
	}

	protected HandleReportDataByMinuteService getHandleReportDataByMinuteService() {
		return handleReportDataByMinuteService;
	}

	protected HandleReportDataByHourService getHandleReportDataByHourService() {
		return handleReportDataByHourService;
	}

	protected HandleReportDataByDayService getHandleReportDataByDayService() {
		return handleReportDataByDayService;
	}

	protected HandleReportDataByMonthService getHandleReportDataByMonthService() {
		return handleReportDataByMonthService;
	}

	protected HandleReportDataByYearService getHandleReportDataByYearService() {
		return handleReportDataByYearService;
	}

	protected HandleDashboardDataService getHandleDashboardDataService() {
		return handleDashboardDataService;
	}

	protected HandleDashboardDataByYearService getHandleDashboardDataByYearService() {
		return handleDashboardDataByYearService;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected HandleESApiDataService getHandleESApiDataService() {
		return handleESApiDataService;
	}

	protected HandleESExpiredDataService getHandleESExpiredDataService() {
		return handleESExpiredDataService;
	}

	
}
