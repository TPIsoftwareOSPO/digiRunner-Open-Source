package tpi.dgrv4.dpaa.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import tpi.dgrv4.common.constant.DashboardReportTypeEnum;
import tpi.dgrv4.common.constant.DashboardTimeTypeEnum;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.vo.DashboardMedianAideVo;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.jpql.DgrDashboardEsLog;
import tpi.dgrv4.entity.entity.jpql.DgrDashboardLastData;
import tpi.dgrv4.entity.entity.jpql.TsmpReqResLogHistory;
import tpi.dgrv4.entity.repository.DgrDashboardEsLogDao;
import tpi.dgrv4.entity.repository.DgrDashboardLastDataDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpReqResLogHistoryDao;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.ISysInfoService;

@Service
public class HandleDashboardDataService {

//	@Autowired(required = false)
	private ISysInfoService sysInfoService;
	
	private DgrDashboardLastDataDao dgrDashboardLastDataDao;
	private TsmpReqResLogHistoryDao tsmpReqResLogHistoryDao;
	private DgrDashboardEsLogDao dgrDashboardEsLogDao;
    private TsmpDpApptJobDao tsmpDpApptJobDao;
	private TsmpApiCacheProxy tsmpApiCacheProxy;
	private TsmpApiDao tsmpApiDao;
	private HandleDashboardDataByYearService handleDashboardDataByYearService;
	private TsmpSettingService tsmpSettingService;

	private DateFormat yyyyMMddHHmmssSSS = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
	private DateFormat yyyyMMddHHmm = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	private DateFormat yyyyMMddHH = new SimpleDateFormat("yyyy/MM/dd HH");
	private DateFormat yyyyMMdd = new SimpleDateFormat("yyyy/MM/dd");
	private DateFormat HHmm = new SimpleDateFormat("HH:mm");
	private DateFormat mm = new SimpleDateFormat("mm");
	private DateFormat MMdd = new SimpleDateFormat("MM/dd");
	
	private static final String NO_ENTERPRISE_SERVICE = "...No Enterprise Service...";
	
	@Autowired
	public HandleDashboardDataService(@Nullable ISysInfoService sysInfoService, DgrDashboardLastDataDao dgrDashboardLastDataDao,
			TsmpReqResLogHistoryDao tsmpReqResLogHistoryDao, DgrDashboardEsLogDao dgrDashboardEsLogDao,
			TsmpDpApptJobDao tsmpDpApptJobDao, TsmpApiCacheProxy tsmpApiCacheProxy, TsmpApiDao tsmpApiDao,
			HandleDashboardDataByYearService handleDashboardDataByYearService, TsmpSettingService tsmpSettingService) {
		super();
		this.sysInfoService = sysInfoService;
		this.dgrDashboardLastDataDao = dgrDashboardLastDataDao;
		this.tsmpReqResLogHistoryDao = tsmpReqResLogHistoryDao;
		this.dgrDashboardEsLogDao = dgrDashboardEsLogDao;
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
		this.tsmpApiCacheProxy = tsmpApiCacheProxy;
		this.tsmpApiDao = tsmpApiDao;
		this.handleDashboardDataByYearService = handleDashboardDataByYearService;
		this.tsmpSettingService = tsmpSettingService;
	}
	
	@Transactional
	public void exec(Date execDate, boolean isEs, Long jobId) {
//		System.err.println(StackTraceUtil.logStackTrace(new Exception("find-root-stack")));
		TPILogger.tl.debug("--- Begin HandleDashboardDataService ---");
		if (sysInfoService != null) {
			TPILogger.tl.info(sysInfoService.getCpuMemoryInfo());
		} else {
			TPILogger.tl.info(NO_ENTERPRISE_SERVICE);
		}
		
		sleepforColddownCPU(10, "START"); //計算完冷卻一下 CPU

		List<DgrDashboardLastData> lastList = getDgrDashboardLastDataDao()
				.findByTimeTypeAndDashboardType(DashboardTimeTypeEnum.MINUTE.value(), DashboardReportTypeEnum.UNPOPULAR.value());
		//因為年的service在同個Transactional,所以一起delete,不下deleteAll的原因是因為會將該表鎖住造成頁面無法再查詢
		List<DgrDashboardLastData> removeLastList = getDgrDashboardLastDataDao().findAll();
		
		List<DgrDashboardLastData> saveLastList = new ArrayList<>();
		//top5冷門
		execUnpopular(lastList, saveLastList);
		sleepforColddownCPU(10 , "top 5 Cold"); //計算完冷卻一下 CPU
		
		//top5熱門
		execPopular(saveLastList);
		sleepforColddownCPU(10 , "top 5 HOT"); //計算完冷卻一下 CPU
		
		// 資料日期
		Date monthStartDate = this.getStartDate(execDate, DashboardTimeTypeEnum.MONTH);
		Date endDate = this.getEndDate(execDate);
		long longEndDate = endDate.getTime();
		Date dayStartDate = this.getStartDate(execDate, DashboardTimeTypeEnum.DAY);
		long longDayStartDate = dayStartDate.getTime();
		Date minuteStartDate = this.getStartDate(execDate, DashboardTimeTypeEnum.MINUTE);
		long longMinuteStartDate = minuteStartDate.getTime();
		
		TPILogger.tl.debug("monthStartDate = " + this.dateToStringForyyyyMMddHHmmssSSS(monthStartDate));
		TPILogger.tl.debug("dayStartDate = " + this.dateToStringForyyyyMMddHHmmssSSS(dayStartDate));
		TPILogger.tl.debug("minuteStartDate = " + this.dateToStringForyyyyMMddHHmmssSSS(minuteStartDate));
		TPILogger.tl.debug("endDate = " + this.dateToStringForyyyyMMddHHmmssSSS(endDate));
		TPILogger.tl.debug("pageSize = " + this.getPageSize());
		
		Map<String, List<DgrDashboardLastData>> lastDataMap = new HashMap<>();
		Map<String, DashboardMedianAideVo> medianMap = new HashMap<>();
		List<DgrDashboardEsLog> esMonthDataList = new ArrayList<>();
		List<TsmpReqResLogHistory> rdbMonthDataList = new ArrayList<>();
		List<DgrDashboardEsLog> esDayDataList = new ArrayList<>();
		List<TsmpReqResLogHistory> rdbDayDataList = new ArrayList<>();
		List<DgrDashboardEsLog> esMinuteDataList = new ArrayList<>();
		List<TsmpReqResLogHistory> rdbMinuteDataList = new ArrayList<>();
		String lastRowId = null;
		//因為在execClientUsageMetrics的groupBy在打包會問題,所以回傳不使用父類別(List<? extends ApiDashboardFields>)方式
		//所以分成兩個dataList來接
		int loopIndex = 1;
		while(true) {
			TPILogger.tl.info("start get month,day,minute data, Loop index is " + loopIndex);
			if (sysInfoService != null) {
				TPILogger.tl.info(sysInfoService.getCpuMemoryInfo());
			} else {
				TPILogger.tl.info(NO_ENTERPRISE_SERVICE);
			}
			
			sleepforColddownCPU(10, "133"); //計算完冷卻一下 CPU
			
			if(isEs) {
				esMonthDataList = this.getDataForEs(monthStartDate, endDate, lastRowId);
				TPILogger.tl.info("esMonthDataList.size = " + esMonthDataList.size());
				//月資料給天
				if(esMonthDataList.size() > 0) {
					//第一筆就在天的範圍
					if(esMonthDataList.get(0).getRtime().getTime() >= longDayStartDate) {
						esDayDataList = esMonthDataList;
					}else if(esMonthDataList.get(esMonthDataList.size() - 1).getRtime().getTime() >= longDayStartDate) {
						//最後一筆有在天的範圍
						esDayDataList = esMonthDataList.stream().filter(f->f.getRtime().getTime() >= longDayStartDate
								&& f.getRtime().getTime() <= longEndDate).collect(Collectors.toList());
					}
				}else {
					if(!CollectionUtils.isEmpty(esDayDataList)) {
						esDayDataList = new ArrayList<>();
					}
				}
				TPILogger.tl.info("esDayDataList.size = " + esDayDataList.size());
				//天資料給分
				if(!CollectionUtils.isEmpty(esDayDataList)) {
					//第一筆就在分的範圍
					if(esDayDataList.get(0).getRtime().getTime() >= longMinuteStartDate) {
						esMinuteDataList = esDayDataList;
					}else if(esDayDataList.get(esDayDataList.size() - 1).getRtime().getTime() >= longMinuteStartDate) {
						//最後一筆有在分的範圍
						esMinuteDataList = esDayDataList.stream().filter(f->f.getRtime().getTime() >= longMinuteStartDate
								&& f.getRtime().getTime() <= longEndDate).collect(Collectors.toList());
					}
				}else {
					if(!CollectionUtils.isEmpty(esMinuteDataList)) {
						esMinuteDataList = new ArrayList<>();
					}
				}
				TPILogger.tl.info("esMinuteDataList.size = " + esMinuteDataList.size());
			}else {
				rdbMonthDataList = this.getDataForRdb(monthStartDate, endDate, lastRowId);
				TPILogger.tl.info("rdbMonthDataList.size = " + rdbMonthDataList.size());
				//月資料給天
				if(rdbMonthDataList.size() > 0) {
					//第一筆就在天的範圍
					if(rdbMonthDataList.get(0).getRtime().getTime() >= longDayStartDate) {
						rdbDayDataList = rdbMonthDataList;
					}else if(rdbMonthDataList.get(rdbMonthDataList.size() - 1).getRtime().getTime() >= longDayStartDate) {
						//最後一筆有在天的範圍
						rdbDayDataList = rdbMonthDataList.stream().filter(f->f.getRtime().getTime() >= longDayStartDate
								&& f.getRtime().getTime() <= longEndDate).collect(Collectors.toList());
					}
				}else {
					if(!CollectionUtils.isEmpty(rdbDayDataList)) {
						rdbDayDataList = new ArrayList<>();
					}
				}
				TPILogger.tl.info("rdbDayDataList.size = " + rdbDayDataList.size());
				//天資料給分
				if(!CollectionUtils.isEmpty(rdbDayDataList)) {
					//第一筆就在分的範圍
					if(rdbDayDataList.get(0).getRtime().getTime() >= longMinuteStartDate) {
						rdbMinuteDataList = rdbDayDataList;
					}else if(rdbDayDataList.get(rdbDayDataList.size() - 1).getRtime().getTime() >= longMinuteStartDate) {
						//最後一筆有在分的範圍
						rdbMinuteDataList = rdbDayDataList.stream().filter(f->f.getRtime().getTime() >= longMinuteStartDate
								&& f.getRtime().getTime() <= longEndDate).collect(Collectors.toList());
					}
				}else {
					if(!CollectionUtils.isEmpty(rdbMinuteDataList)) {
						rdbMinuteDataList = new ArrayList<>();
					}
				}
				TPILogger.tl.info("rdbMinuteDataList.size = " + rdbMinuteDataList.size());
			} 
			
			TPILogger.tl.info("end get month,day,minute data, Loop index is " + loopIndex);
			if (sysInfoService != null) {
				TPILogger.tl.info(sysInfoService.getCpuMemoryInfo());
			} else {
				TPILogger.tl.info(NO_ENTERPRISE_SERVICE);
			}
			
			sleepforColddownCPU(10, "209"); //計算完冷卻一下 CPU
			
			//月
			//5.Bad Attempt
			execBadAttempt(lastDataMap, rdbMonthDataList, esMonthDataList, isEs, DashboardTimeTypeEnum.MONTH);
			//10.API流量分佈
			execApiTrafficDistribution(lastDataMap, rdbMonthDataList, esMonthDataList, isEs, DashboardTimeTypeEnum.MONTH, monthStartDate, endDate);
			//12.客戶端使用指標
			execClientUsageMetrics(lastDataMap, medianMap, rdbMonthDataList, esMonthDataList, isEs, DashboardTimeTypeEnum.MONTH);
			
			//天
			//5.Bad Attempt
			execBadAttempt(lastDataMap, rdbDayDataList, esDayDataList, isEs, DashboardTimeTypeEnum.DAY);
			//10.API流量分佈
			execApiTrafficDistribution(lastDataMap, rdbDayDataList, esDayDataList, isEs, DashboardTimeTypeEnum.DAY, dayStartDate, endDate);
			//12.客戶端使用指標
			execClientUsageMetrics(lastDataMap, medianMap, rdbDayDataList, esDayDataList, isEs, DashboardTimeTypeEnum.DAY);
			
			//分
			//5.Bad Attempt
			execBadAttempt(lastDataMap, rdbMinuteDataList, esMinuteDataList, isEs, DashboardTimeTypeEnum.MINUTE);
			//10.API流量分佈
			execApiTrafficDistribution(lastDataMap, rdbMinuteDataList, esMinuteDataList, isEs, DashboardTimeTypeEnum.MINUTE, minuteStartDate, endDate);
			//12.客戶端使用指標
			execClientUsageMetrics(lastDataMap, medianMap, rdbMinuteDataList, esMinuteDataList, isEs, DashboardTimeTypeEnum.MINUTE);
			
			TPILogger.tl.info("data to lastDataMap, Loop index is " + loopIndex++);
			if (sysInfoService != null) {
				TPILogger.tl.info(sysInfoService.getCpuMemoryInfo());
			} else {
				TPILogger.tl.info(NO_ENTERPRISE_SERVICE);
			}
			
			sleepforColddownCPU(10, "237"); //計算完冷卻一下 CPU
			
			if(isEs) {
				if(esMonthDataList.size() < this.getPageSize()) {
					break;
				}else if(esMonthDataList.size() > 0){
					//在此循環execApiTrafficDistribution的monthStartDate只有初始化有用,所以可以變更monthStartDate
					monthStartDate = esMonthDataList.get(esMonthDataList.size() - 1).getRtime();
					lastRowId = esMonthDataList.get(esMonthDataList.size() - 1).getId();
				}
			}else {
				if(rdbMonthDataList.size() < this.getPageSize()) {
					break;
				}else if(rdbMonthDataList.size() > 0){
					//在此循環execApiTrafficDistribution的monthStartDate只有初始化有用,所以可以變更monthStartDate
					monthStartDate = rdbMonthDataList.get(rdbMonthDataList.size() - 1).getRtime();
					lastRowId = rdbMonthDataList.get(rdbMonthDataList.size() - 1).getId();
				}
			}
		}

		esMonthDataList = null;
		rdbMonthDataList = null;
		esDayDataList = null;
		rdbDayDataList = null;
		esMinuteDataList = null;
		rdbMinuteDataList = null;
		
		TPILogger.tl.info("month,day,minute data already clear, maybe garbage collection");
		if (sysInfoService != null) {
			TPILogger.tl.info(sysInfoService.getCpuMemoryInfo());
		} else {
			TPILogger.tl.info(NO_ENTERPRISE_SERVICE);
		}
		
		sleepforColddownCPU(10 , "267"); //計算完冷卻一下 CPU
		
		List<DgrDashboardLastData> monthClientLastDataList = new ArrayList<>();
		List<DgrDashboardLastData> dayClientLastDataList = new ArrayList<>();
		List<DgrDashboardLastData> minuteClientLastDataList = new ArrayList<>();
		//將以上的資料存入DB
		for(String key : lastDataMap.keySet()){
			List<DgrDashboardLastData> list =  lastDataMap.get(key);
			//CLIENT_USAGE_METRICS特別處理
			if(key.indexOf(DashboardReportTypeEnum.CLIENT_USAGE_METRICS.name()) > -1) {
				
				TPILogger.tl.info(key+" size = " + list.size());
				//各個clientId次數
				Map<String,Long> countMap = list.stream().collect(Collectors.groupingBy(vo->vo.getStr1(), () -> new TreeMap<>(), Collectors.summingLong(vo-> vo.getNum1())));
				//依最高次數排序
				List<Map.Entry<String, Long>> sortList = new ArrayList<Map.Entry<String, Long>>(countMap.entrySet());
				sortList.sort(new Comparator<Map.Entry<String, Long>>() {
					@Override
					public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
						return o2.getValue().compareTo(o1.getValue());
					}
				});
				
				//分類top5和other,cid空白改為NoAuth
				for(DgrDashboardLastData vo : list) {
					//排序值
					boolean isTop5 = false;
					for(int i=0; i<5; i++) {
						if(i>=sortList.size()) {
							break;
						}
						String sortKey = sortList.get(i).getKey();
						if(vo.getStr1().equals(sortKey)) {
							isTop5 = true;
							vo.setSortNum(i+1);
							//cid若是空的就是NoAuth
							if(vo.getStr1().isBlank()) {
								vo.setStr1("NoAuth");
							}
						}
					}
					if(!isTop5) {
						vo.setSortNum(6);
						vo.setStr1("Others");
					}
					
					//others的相同api資料加總,否則就新增
					if(key.indexOf(DashboardTimeTypeEnum.MONTH.name()) == 0) {
						sameApiSumElseAddData(monthClientLastDataList, vo);
						
					}else if(key.indexOf(DashboardTimeTypeEnum.DAY.name()) == 0) {
						sameApiSumElseAddData(dayClientLastDataList, vo);
						
					}else if(key.indexOf(DashboardTimeTypeEnum.MINUTE.name()) == 0) {
						sameApiSumElseAddData(minuteClientLastDataList, vo);
					}
				}
				//取apiName和求平均時間, 和儲存
				if(key.indexOf(DashboardTimeTypeEnum.MONTH.name()) == 0) {	
					setApiNameAndAvg(monthClientLastDataList);
					saveLastList.addAll(monthClientLastDataList);
				}else if(key.indexOf(DashboardTimeTypeEnum.DAY.name()) == 0) {
					setApiNameAndAvg(dayClientLastDataList);
					saveLastList.addAll(dayClientLastDataList);
				}else if(key.indexOf(DashboardTimeTypeEnum.MINUTE.name()) == 0) {
					setApiNameAndAvg(minuteClientLastDataList);
					saveLastList.addAll(minuteClientLastDataList);
				}
				
			}else {
				saveLastList.addAll(list);
			}
		}
		
		lastDataMap = null;
		TPILogger.tl.info("lastDataMap already clear, maybe garbage collection");
		if (sysInfoService != null) {
			TPILogger.tl.info(sysInfoService.getCpuMemoryInfo());
		} else {
			TPILogger.tl.info(NO_ENTERPRISE_SERVICE);
		}
		
		sleepforColddownCPU(10, "344"); //計算完冷卻一下 CPU
		//月
		//3.Success
		execSuccess(monthClientLastDataList, DashboardTimeTypeEnum.MONTH, saveLastList);
		//4.Fail
		long total = execFail(monthClientLastDataList, DashboardTimeTypeEnum.MONTH, saveLastList);
		//2.Request
		execRequest(total, DashboardTimeTypeEnum.MONTH, saveLastList);
		//11.Client使用佔比
		execClientUsagePercentage(monthClientLastDataList, DashboardTimeTypeEnum.MONTH, saveLastList);
		monthClientLastDataList = null;
		//因為可能被改變了再取一次
		monthStartDate = this.getStartDate(execDate, DashboardTimeTypeEnum.MONTH);
		//7.中位數
		execMedian(medianMap, monthStartDate, endDate, isEs, DashboardTimeTypeEnum.MONTH, saveLastList);
		//6.平均回應時間
		execAVG(medianMap, DashboardTimeTypeEnum.MONTH, saveLastList);
		//1.資料時間
		execDataTime(monthStartDate, endDate, DashboardTimeTypeEnum.MONTH, saveLastList);
		
		//天
		//3.Success
		execSuccess(dayClientLastDataList, DashboardTimeTypeEnum.DAY, saveLastList);
		//4.Fail
		total = execFail(dayClientLastDataList, DashboardTimeTypeEnum.DAY, saveLastList);
		//2.Request
		execRequest(total, DashboardTimeTypeEnum.DAY, saveLastList);
		//11.Client使用佔比
		execClientUsagePercentage(dayClientLastDataList, DashboardTimeTypeEnum.DAY, saveLastList);
		dayClientLastDataList = null;
		//7.中位數
		execMedian(medianMap, dayStartDate, endDate, isEs, DashboardTimeTypeEnum.DAY, saveLastList);
		//6.平均回應時間
		execAVG(medianMap, DashboardTimeTypeEnum.DAY, saveLastList);
		//1.資料時間
		execDataTime(dayStartDate, endDate, DashboardTimeTypeEnum.DAY, saveLastList);
		
		//分
		//3.Success
		execSuccess(minuteClientLastDataList, DashboardTimeTypeEnum.MINUTE, saveLastList);
		//4.Fail
		total = execFail(minuteClientLastDataList, DashboardTimeTypeEnum.MINUTE, saveLastList);
		//2.Request
		execRequest(total, DashboardTimeTypeEnum.MINUTE, saveLastList);
		//11.Client使用佔比
		execClientUsagePercentage(minuteClientLastDataList, DashboardTimeTypeEnum.MINUTE, saveLastList);
		minuteClientLastDataList = null;
		//7.中位數
		execMedian(medianMap, minuteStartDate, endDate, isEs, DashboardTimeTypeEnum.MINUTE, saveLastList);
		//6.平均回應時間
		execAVG(medianMap, DashboardTimeTypeEnum.MINUTE, saveLastList);
		//1.資料時間
		execDataTime(minuteStartDate, endDate, DashboardTimeTypeEnum.MINUTE, saveLastList);
		
		execHandleDashboardDataByYearService(execDate, isEs);
		
		checkJobStatus(jobId);
		
		getDgrDashboardLastDataDao().deleteAll(removeLastList);
		getDgrDashboardLastDataDao().saveAll(saveLastList);
		
		TPILogger.tl.debug("--- Finish HandleDashboardDataService ---");
		if (sysInfoService != null) {
			TPILogger.tl.info(sysInfoService.getCpuMemoryInfo());
		} else {
			TPILogger.tl.info(NO_ENTERPRISE_SERVICE);
		}
	}
	
	protected void checkJobStatus(Long id) {
		//因為在換版可能REPORT_BATCH還在執行中,啟動後會被被AutoInitSQL改成取消,若這種情況就rollback
		boolean isExists = getTsmpDpApptJobDao().existsByApptJobIdAndStatus(id, "C");
		if(isExists) {
			TPILogger.tl.info("REPORT_BATCH job status changed cancel id:" + id);
			throw new CancellationException();
		}
	}
	
	protected void execHandleDashboardDataByYearService(Date execDate, boolean isEs) {
		getHandleDashboardDataByYearService().exec(execDate, isEs);
	}
	
	private void sameApiSumElseAddData(List<DgrDashboardLastData> list, DgrDashboardLastData originVo) {
		if("Others".equals(originVo.getStr1())) {
			DgrDashboardLastData findVo = list.stream().filter(f->f.getStr1().equals(originVo.getStr1()) 
												&& f.getStr2().equals(originVo.getStr2())).findAny().orElse(null);
			if(findVo != null) {
				findVo.setNum1(findVo.getNum1() + originVo.getNum1());
				findVo.setNum2(findVo.getNum2() + originVo.getNum2());
				findVo.setNum3(findVo.getNum3() + originVo.getNum3());
				findVo.setNum4(findVo.getNum4() + originVo.getNum4());
			}else {
				list.add(originVo);
			}
		}else {
			list.add(originVo);
		}
	}
	
	private void setApiNameAndAvg(List<DgrDashboardLastData> list) {
		for(DgrDashboardLastData vo : list) {
			//轉apiName
			String[] arrStr2 = vo.getStr2().split(",");
			if(arrStr2.length == 2) {
				vo.setStr2(this.getApiName(arrStr2[0], arrStr2[1]));
			}
			
			//求平均回應時間,四捨五入
			BigDecimal bdElapse = new BigDecimal(vo.getNum4().toString());
			BigDecimal bdFrequency = new BigDecimal(vo.getNum2().toString());
			if(vo.getNum4() > 0 && vo.getNum2() > 0) {
				long avgTime = bdElapse.divide(bdFrequency, 0, RoundingMode.HALF_UP).longValue();
				vo.setNum4(avgTime);
			}else {
				vo.setNum4(0L);
			}
		}
	}
	
	private void execPopular(List<DgrDashboardLastData> saveLastList) {
		Pageable pageable = PageRequest.of(0, 5, Sort.by("total").descending().and(Sort.by("createTime").descending()));
		Page<TsmpApi> pageResult = getTsmpApiDao().findAll(pageable);
		List<TsmpApi> list =  pageResult.getContent();
		for(int i=0; i < list.size(); i++){
			TsmpApi apiVo = list.get(i);
			
			long avg = 0L;
			if(apiVo.getElapse().longValue() > 0 && apiVo.getSuccess().longValue() > 0) {
				BigDecimal bdElapse = new BigDecimal(apiVo.getElapse().toString());
				avg = bdElapse.divide(new BigDecimal(apiVo.getSuccess().toString()), 0, RoundingMode.HALF_UP).longValue();
			}
			
			for(DashboardTimeTypeEnum timeTypeVo : DashboardTimeTypeEnum.values()) {
				DgrDashboardLastData vo = new DgrDashboardLastData();
				vo.setTimeType(timeTypeVo.value());
				vo.setDashboardType(DashboardReportTypeEnum.POPULAR.value());
				vo.setStr1(apiVo.getApiName());
				vo.setNum1(apiVo.getTotal());
				vo.setNum2(apiVo.getSuccess());
				vo.setNum3(apiVo.getFail());
				vo.setNum4(avg);
				vo.setSortNum(i+1);
				
				saveLastList.add(vo);
			}
			
		}
		
	}
	
	private void execUnpopular(List<DgrDashboardLastData> lastList, List<DgrDashboardLastData> saveLastList) {
		Pageable pageable = PageRequest.of(0, 5, Sort.by("total").ascending().and(Sort.by("createTime").ascending()));
		Page<TsmpApi> pageResult = getTsmpApiDao().findAll(pageable);
		List<TsmpApi> list =  pageResult.getContent();
		for(int i=0; i < list.size(); i++){
			TsmpApi apiVo = list.get(i);
			String status = "U";
			for(int j=0; j < lastList.size(); j++) {
				DgrDashboardLastData lastVo = lastList.get(j);
				if(apiVo.getApiName().equals(lastVo.getStr1())) {
					if((i+1) == lastVo.getSortNum().intValue()) {
						status = lastVo.getStr2();
					}else if((i+1) > lastVo.getSortNum().intValue()) {
						status = "D";
					}
				}
			}

			for(DashboardTimeTypeEnum timeTypeVo : DashboardTimeTypeEnum.values()) {
				DgrDashboardLastData vo = new DgrDashboardLastData();
				vo.setTimeType(timeTypeVo.value());
				vo.setDashboardType(DashboardReportTypeEnum.UNPOPULAR.value());
				vo.setStr1(apiVo.getApiName());
				vo.setStr2(status);
				vo.setNum1(apiVo.getTotal());
				vo.setSortNum(i+1);
				
				saveLastList.add(vo);
			}
			
		}
		
	}
	
	private List<DgrDashboardEsLog> getDataForEs(Date startDate, Date endDate, String id) {
		//因為在execClientUsageMetrics的groupBy在打包會有問題,所以回傳不使用父類別(List<? extends ApiDashboardFields>)方式
		//因為分頁效能的關係,所以用nativeSQL方式
		List<DgrDashboardEsLog> list = getDgrDashboardEsLogDao().queryByDashboard(startDate, endDate, id, this.getPageSize());
		for(DgrDashboardEsLog vo : list) {
			if(vo.getCid() == null) {
				vo.setCid("");
			}
		}
		return list;
		
	}
	
	private List<TsmpReqResLogHistory> getDataForRdb(Date startDate, Date endDate, String id) {
		//因為在execClientUsageMetrics的groupBy在打包會有問題,所以回傳不使用父類別(List<? extends ApiDashboardFields>)方式
		//因為分頁效能的關係,所以用nativeSQL方式
		List<TsmpReqResLogHistory> list = getTsmpReqResLogHistoryDao().queryByDashboard(startDate, endDate, id, this.getPageSize());
		for(TsmpReqResLogHistory vo : list) {
			if(vo.getCid() == null) {
				vo.setCid("");
			}
		}
		return list;
		
	}
	
	private void execDataTime(Date startDate, Date endDate, DashboardTimeTypeEnum timeType, List<DgrDashboardLastData> saveLastList) {
		String strStartDate = this.dateToStringForyyyyMMddHHmm(startDate);
		String strEndDate = this.dateToStringForyyyyMMddHHmm(endDate);
		
		DgrDashboardLastData vo = new DgrDashboardLastData();
		vo.setTimeType(timeType.value());
		vo.setDashboardType(DashboardReportTypeEnum.DATATIME.value());
		vo.setStr1(strStartDate);
		vo.setStr2(strEndDate);
		
		saveLastList.add(vo);

	}
	
	private long execSuccess(List<DgrDashboardLastData> list, DashboardTimeTypeEnum timeType, List<DgrDashboardLastData> saveLastList) {
		long success = 0;
		long total = 0;
		for(DgrDashboardLastData vo : list) {
			total += vo.getNum1();
			success += vo.getNum2();
		}
		
		BigDecimal bdTotal = new BigDecimal(Long.toString(total));
		BigDecimal bdSuccess = new BigDecimal(Long.toString(success));
		String percentage = "0.00";
		if(total > 0 && success > 0) {
			percentage = String.format("%.2f", bdSuccess.divide(bdTotal, 4, RoundingMode.HALF_UP).doubleValue() * 100) ;
		}
		
		
		DgrDashboardLastData vo = new DgrDashboardLastData();
		vo.setTimeType(timeType.value());
		vo.setDashboardType(DashboardReportTypeEnum.SUCCESS.value());
		vo.setStr1(percentage);
		vo.setNum1(success);
		vo.setNum2(total);
		
		saveLastList.add(vo);
		
		return total;
	}
	
	private long execFail(List<DgrDashboardLastData> list, DashboardTimeTypeEnum timeType, List<DgrDashboardLastData> saveLastList) {
		long fail = 0;
		long total = 0;
		for(DgrDashboardLastData vo : list) {
			total += vo.getNum1();
			fail += vo.getNum3();
		}
		
		BigDecimal bdTotal = new BigDecimal(Long.toString(total));
		BigDecimal bdFail = new BigDecimal(Long.toString(fail));
		String percentage = "0.00";
		if(total > 0 && fail > 0) {
			percentage = String.format("%.2f", bdFail.divide(bdTotal, 4, RoundingMode.HALF_UP).doubleValue() * 100) ;
		}
		
		DgrDashboardLastData vo = new DgrDashboardLastData();
		vo.setTimeType(timeType.value());
		vo.setDashboardType(DashboardReportTypeEnum.FAIL.value());
		vo.setStr1(percentage);
		vo.setNum1(fail);
		vo.setNum2(total);
		
		saveLastList.add(vo);
		
		return total;
	}
	
	private void execRequest(long total, DashboardTimeTypeEnum timeType, List<DgrDashboardLastData> saveLastList) {
		
		String str1 = null;
		if(total > 1000) {
			BigDecimal bdTotal = new BigDecimal(Long.toString(total));
			String k = String.format("%,.1f",bdTotal.divide(new BigDecimal(Integer.toString(1000)), 1, RoundingMode.HALF_UP).doubleValue()) ;
			str1 = k + "K";
		}else {
			str1 = Long.toString(total);
		}

		DgrDashboardLastData vo = new DgrDashboardLastData();
		vo.setTimeType(timeType.value());
		vo.setDashboardType(DashboardReportTypeEnum.REQUEST.value());
		vo.setStr1(str1);
		
		saveLastList.add(vo);

	}
	
	private void execBadAttempt(Map<String, List<DgrDashboardLastData>> lastDataMap, List<TsmpReqResLogHistory> rdbDataList
			, List<DgrDashboardEsLog> esDataList, boolean isEs, DashboardTimeTypeEnum timeType ) {
		Map<String,Long> map = null;
		//過濾非bad attempt的資料
		if(isEs) {
			map = esDataList.stream().filter(f->f.getHttpStatus() >= 400 || f.getHttpStatus() <= 0)
	                .collect(Collectors.groupingBy(vo->vo.getHttpStatus().toString(), Collectors.counting()));
		}else {
			map = rdbDataList.stream().filter(f->f.getHttpStatus() >= 400 || f.getHttpStatus() <= 0)
	                .collect(Collectors.groupingBy(vo->vo.getHttpStatus().toString(), Collectors.counting()));
		}
		
		long total = 0;
		long num401 = 0;
		long num403 = 0;
		long other = 0;
		
		for(String key : map.keySet()) {
			long val = map.get(key);
			if("401".equals(key)) {
				num401 += val;
			}else if("403".equals(key)) {
				num403 += val;
			}else {
				other += val;
			}
			total += val;
		}
		
		String lastDataKey = timeType.name() + "_" + DashboardReportTypeEnum.BAD_ATTEMPT.name();
		if(lastDataMap.get(lastDataKey) != null) {
			DgrDashboardLastData vo  = lastDataMap.get(lastDataKey).get(0);
			vo.setNum1(vo.getNum1() + total);
			vo.setNum2(vo.getNum2() + num401);
			vo.setNum3(vo.getNum3() + num403);
			vo.setNum4(vo.getNum4() + other);
		}else {
			DgrDashboardLastData vo = new DgrDashboardLastData();
			vo.setTimeType(timeType.value());
			vo.setDashboardType(DashboardReportTypeEnum.BAD_ATTEMPT.value());
			vo.setNum1(total);
			vo.setNum2(num401);
			vo.setNum3(num403);
			vo.setNum4(other);
			
			List<DgrDashboardLastData> lastDataList = new ArrayList<>();
			lastDataList.add(vo);
			lastDataMap.put(lastDataKey, lastDataList);
		}
	}
	
	private void execMedian(Map<String, DashboardMedianAideVo> medianMap, Date startDate, Date endDate, boolean isEs, DashboardTimeTypeEnum timeType
			, List<DgrDashboardLastData> saveLastList) {
		DashboardMedianAideVo medianVo = medianMap.get(timeType.name());
		long min = 0;
		long max = 0;
		long median = 0;
		if(medianVo != null && medianVo.getFrequency() > 0) {
			min = medianVo.getMinElapse();
			max = medianVo.getMaxElapse();
			int remainder = (int)(medianVo.getFrequency() % 2);
			int pageSize = 1;
			int firstResult = (int)(medianVo.getFrequency() / 2);
			if(remainder == 0) {
				pageSize = 2;
				firstResult = firstResult - 1;
			}
			if(isEs) {
				List<DgrDashboardEsLog> list = getDgrDashboardEsLogDao().queryByMedian(startDate, endDate, pageSize, firstResult);
				if(list.size() == 1) {
					median = list.get(0).getElapse();
				}else if(list.size() == 2){
					int elapse1 = list.get(0).getElapse();
					int elapse2 = list.get(1).getElapse();
					BigDecimal bdElapse = new BigDecimal(String.valueOf( elapse1 + elapse2));
					median = bdElapse.divide(new BigDecimal(String.valueOf(2)), 0, RoundingMode.HALF_UP).longValue();
				}
			}else {
				List<TsmpReqResLogHistory> list = getTsmpReqResLogHistoryDao().queryByMedian(startDate, endDate, pageSize, firstResult);
				if(list.size() == 1) {
					median = list.get(0).getElapse();
				}else if(list.size() == 2){
					int elapse1 = list.get(0).getElapse();
					int elapse2 = list.get(1).getElapse();
					BigDecimal bdElapse = new BigDecimal(String.valueOf( elapse1 + elapse2));
					median = bdElapse.divide(new BigDecimal(String.valueOf(2)), 0, RoundingMode.HALF_UP).longValue();
				}
			}
		}
		
		
		DgrDashboardLastData vo = new DgrDashboardLastData();
		vo.setTimeType(timeType.value());
		vo.setDashboardType(DashboardReportTypeEnum.MEDIAN.value());
		vo.setNum1(min);
		vo.setNum2(max);
		vo.setNum3(median);
		
		saveLastList.add(vo);
	}
	
	private void execAVG(Map<String, DashboardMedianAideVo> medianMap, DashboardTimeTypeEnum timeType, List<DgrDashboardLastData> saveLastList) {
		long avg = 0;
		DashboardMedianAideVo medianVo = medianMap.get(timeType.name());
		if(medianVo != null && medianVo.getFrequency() > 0) {
			long total = medianVo.getTotalElapse();
			BigDecimal bdTotal = new BigDecimal(String.valueOf(total));
			avg = bdTotal.divide(new BigDecimal(String.valueOf(medianVo.getFrequency())), 0, RoundingMode.HALF_UP).longValue();
		}
		
		DgrDashboardLastData vo = new DgrDashboardLastData();
		vo.setTimeType(timeType.value());
		vo.setDashboardType(DashboardReportTypeEnum.AVG.value());
		vo.setNum1(avg);
		
		saveLastList.add(vo);
		
	}
	
	private void execApiTrafficDistribution(Map<String, List<DgrDashboardLastData>> lastDataMap, List<TsmpReqResLogHistory> rdbDataList
			, List<DgrDashboardEsLog> esDataList, boolean isEs, DashboardTimeTypeEnum timeType, Date startDate, Date endDate) 
	{
		String lastDataKey = timeType.name() + "_" + DashboardReportTypeEnum.API_TRAFFIC_DISTRIBUTION.name();
		List<DgrDashboardLastData> lastDataList = null;
		Map<String,Map<String,Long>> map = null;
		if(timeType == DashboardTimeTypeEnum.MINUTE) {
			//預設值,每分都給0
			if(lastDataMap.get(lastDataKey) == null) {
				lastDataList = new ArrayList<>();
				Date rtime = null;
				for(int i=0 ; i<10; i++) {
					if(i == 0) {
						rtime = startDate;
					}else {
						rtime = getApiTrafficTime(timeType, rtime);
					}
					
					String rtimeKey = this.dateToStringForHHmm(rtime);
					
					DgrDashboardLastData vo = new DgrDashboardLastData();
					vo.setTimeType(timeType.value());
					vo.setDashboardType(DashboardReportTypeEnum.API_TRAFFIC_DISTRIBUTION.value());
					vo.setStr1(rtimeKey);
					vo.setNum1(0L);
					vo.setNum2(0L);
					vo.setSortNum(i+1);
					
					lastDataList.add(vo);
				}
				lastDataMap.put(lastDataKey, lastDataList);
				
				
			}else {
				lastDataList = lastDataMap.get(lastDataKey);
			}
			
			//計算資料
			if(isEs) {
				map = esDataList.stream().collect(Collectors.groupingBy(vo->this.dateToStringForHHmm(vo.getRtime()) 
					     , Collectors.groupingBy(vo -> vo.getExeStatus(), Collectors.counting())));
			}else {
				map = rdbDataList.stream().collect(Collectors.groupingBy(vo->this.dateToStringForHHmm(vo.getRtime()) 
					     , Collectors.groupingBy(vo -> vo.getExeStatus(), Collectors.counting())));
			}
			
			//儲存計算資料
			for(String rtimeKey : map.keySet()) {
				try {
					Map<String, Long> statusMap = map.get(rtimeKey);

					lastDataList.stream().filter(f->f.getStr1().equals(rtimeKey)).findAny().ifPresent(vo -> {
						if(statusMap.get("Y") != null) {
							vo.setNum1(vo.getNum1() + statusMap.get("Y"));
						}
						if(statusMap.get("N") != null) {
							vo.setNum2(vo.getNum2() + statusMap.get("N"));
						}
					});


				}catch(Exception e) {
					TPILogger.tl.error("rtimeKey="+rtimeKey);
					throw e;
				}
			}
			
		}else if(timeType == DashboardTimeTypeEnum.DAY) {
			//預設值,每時都給0
			if(lastDataMap.get(lastDataKey) == null) {
				lastDataList = new ArrayList<>();
				int endIndex = 25;
				if("00".equals(this.dateToStringFormm(startDate))) {
					endIndex = 24;
				}
				Date rtime = null;
				for(int i=0 ; i<endIndex; i++) {
					if(i == 0) {
						rtime = startDate;
					}else {
						rtime = getApiTrafficTime(timeType, rtime);
					}
					
					String rtimeKey = Optional.ofNullable(this.dateToStringForyyyyMMddHH(rtime)).orElseThrow(TsmpDpAaRtnCode._1295::throwing);
					
					DgrDashboardLastData vo = new DgrDashboardLastData();
					vo.setTimeType(timeType.value());
					vo.setDashboardType(DashboardReportTypeEnum.API_TRAFFIC_DISTRIBUTION.value());
					if(i==0 && endIndex == 25) {
						vo.setStr1(this.dateToStringForHHmm(rtime));
					}else {
						vo.setStr1(rtimeKey.substring(11) + ":00");
					}
					vo.setNum1(0L);
					vo.setNum2(0L);
					vo.setSortNum(i+1);
					
					lastDataList.add(vo);
				}
				
				lastDataMap.put(lastDataKey, lastDataList);
				
			}else {
				lastDataList = lastDataMap.get(lastDataKey);
			}
			
			//計算資料
			if(isEs) {
				map = esDataList.stream().collect(Collectors.groupingBy(vo->this.dateToStringForyyyyMMddHH(vo.getRtime()) 
					     , Collectors.groupingBy(vo -> vo.getExeStatus(), Collectors.counting())));
			}else {
				map = rdbDataList.stream().collect(Collectors.groupingBy(vo->this.dateToStringForyyyyMMddHH(vo.getRtime()) 
					     , Collectors.groupingBy(vo -> vo.getExeStatus(), Collectors.counting())));
			}
			
			String strStartDate = this.dateToStringForyyyyMMddHH(startDate); 
			//儲存計算資料
			for(String rtimeKey : map.keySet()) {
				try {
					DgrDashboardLastData vo = null;
					//非整點第1個的取法,因為它的分不是00
					if(lastDataList.size() == 25 && rtimeKey.equals(strStartDate)) {
						vo = lastDataList.get(0);
					}else {
						String key = rtimeKey.substring(11) +":00";
						vo =  lastDataList.stream().filter(f->f.getStr1().equals(key)).findAny().orElseThrow(TsmpDpAaRtnCode._1298::throwing);
					}
					
					Map<String, Long> statusMap = map.get(rtimeKey);
					if(statusMap.get("Y") != null) {
						vo.setNum1(vo.getNum1() + statusMap.get("Y"));
					}
					if(statusMap.get("N") != null) {
						vo.setNum2(vo.getNum2() + statusMap.get("N"));
					}
				}catch(Exception e) {
					TPILogger.tl.error("rtimeKey="+rtimeKey);
					throw e;
				}
			}
			
			
		}else {//月
			//預設值,每日都給0
			Date rtime = null;
			String strEndDate = this.dateToStringForyyyyMMdd(endDate);
			if(lastDataMap.get(lastDataKey) == null) {
				lastDataList = new ArrayList<>();
				for(int i=0 ; i<32; i++) {
					if(i == 0) {
						rtime = startDate;
					}else {
						rtime = getApiTrafficTime(timeType, rtime);
					}
					
					String strRtime = this.dateToStringForyyyyMMdd(rtime);
					if(strRtime.compareTo(strEndDate) > 0) {
						break;
					}
					
					String rtimeKey = this.dateToStringForMMdd(rtime);
					DgrDashboardLastData vo = new DgrDashboardLastData();
					vo.setTimeType(timeType.value());
					vo.setDashboardType(DashboardReportTypeEnum.API_TRAFFIC_DISTRIBUTION.value());
					vo.setStr1(rtimeKey);
					vo.setNum1(0L);
					vo.setNum2(0L);
					vo.setSortNum(i+1);
					
					lastDataList.add(vo);
				}
				
				lastDataMap.put(lastDataKey, lastDataList);
				
			}else {
				lastDataList = lastDataMap.get(lastDataKey);
			}
			
			//計算資料
			if(isEs) {
				map = esDataList.stream().collect(Collectors.groupingBy(vo->this.dateToStringForMMdd(vo.getRtime()) 
					     , Collectors.groupingBy(vo -> vo.getExeStatus(), Collectors.counting())));
			}else {
				map = rdbDataList.stream().collect(Collectors.groupingBy(vo->this.dateToStringForMMdd(vo.getRtime()) 
					     , Collectors.groupingBy(vo -> vo.getExeStatus(), Collectors.counting())));
			}
			
			//儲存計算資料
			for(String rtimeKey : map.keySet()) {
				try {
					DgrDashboardLastData vo =  lastDataList.stream().filter(f->f.getStr1().equals(rtimeKey)).findAny().get();
					Map<String, Long> statusMap = map.get(rtimeKey);
					if(statusMap.get("Y") != null) {
						vo.setNum1(vo.getNum1() + statusMap.get("Y"));
					}
					if(statusMap.get("N") != null) {
						vo.setNum2(vo.getNum2() + statusMap.get("N"));
					}
				}catch(Exception e) {
					TPILogger.tl.error("rtimeKey="+rtimeKey);
					throw e;
				}
			}
		}
				
	}
	
	private Date getApiTrafficTime(DashboardTimeTypeEnum timeType, Date startDate) {
		Calendar c = Calendar.getInstance();
		c.setTime(startDate);
		if(timeType == DashboardTimeTypeEnum.DAY) {
			c.add(Calendar.HOUR_OF_DAY, 1);
		}else if(timeType == DashboardTimeTypeEnum.MONTH) {
			c.add(Calendar.DATE, 1);
		}else {//分
			c.add(Calendar.MINUTE, 1);
		}
		
		return c.getTime();
	}
	
	private void execClientUsagePercentage(List<DgrDashboardLastData> list, DashboardTimeTypeEnum timeType, List<DgrDashboardLastData> saveLastList) {
		if(CollectionUtils.isEmpty(list)) {
			return;
		}
		
		//加總
		Map<Integer, Long> map = list.stream().collect(Collectors.groupingBy(vo->vo.getSortNum(), () -> new TreeMap<>(), Collectors.summingLong(vo-> vo.getNum1())));
		long total = list.stream().mapToLong(vo -> vo.getNum1()).sum();
		BigDecimal bdTotal = new BigDecimal(Long.toString(total));
		map.forEach((k,v) ->{
			BigDecimal bdNum1 = new BigDecimal(String.valueOf(v * 100));
			String percentage = bdNum1.divide(bdTotal, 0, RoundingMode.HALF_UP).toString();
			
			DgrDashboardLastData vo = new DgrDashboardLastData();
			vo.setTimeType(timeType.value());
			vo.setDashboardType(DashboardReportTypeEnum.CLIENT_USAGE_PERCENTAGE.value());
			DgrDashboardLastData findVo = list.stream().filter(f->f.getSortNum().equals(k)).findAny().orElse(null);
			vo.setStr1(findVo.getStr1());
			vo.setStr2(percentage);
			vo.setNum1(v);
			vo.setNum2(total);
			vo.setSortNum(k);
			
			saveLastList.add(vo);
		});
		
	}
	
	private void execClientUsageMetrics(Map<String, List<DgrDashboardLastData>> lastDataMap, Map<String, DashboardMedianAideVo> medianMap
			,List<TsmpReqResLogHistory> rdbDataList, List<DgrDashboardEsLog> esDataList, boolean isEs, DashboardTimeTypeEnum timeType) {
		
		if(isEs) {
			if(CollectionUtils.isEmpty(esDataList)) {
				return;
			}
		}else {
			if(CollectionUtils.isEmpty(rdbDataList)) {
				return;
			}
		}
		
		String lastDataKey = timeType.name() + "_" + DashboardReportTypeEnum.CLIENT_USAGE_METRICS.name();
		List<DgrDashboardLastData> lastDataList = lastDataMap.get(lastDataKey);
		if(lastDataList == null) {
			lastDataList = new ArrayList<>();
			lastDataMap.put(lastDataKey, lastDataList);
		}
		
		DashboardMedianAideVo medianVo = medianMap.get(timeType.name());
		if(medianVo == null) {
			medianVo = new DashboardMedianAideVo();
			medianMap.put(timeType.name(), medianVo);
		}

		//以cid為gorup,再以moduleName+txid為group
		Map<String, Map<String,List<TsmpReqResLogHistory>>> rdbCidMap = null;
		Map<String, Map<String,List<DgrDashboardEsLog>>> esCidMap = null;
		if(isEs) {
			esCidMap = esDataList.stream().collect(Collectors.groupingBy(vo->vo.getCid(),Collectors.groupingBy(vo->vo.getModuleName()+","+vo.getTxid())));	
		}else {
			rdbCidMap = rdbDataList.stream().collect(Collectors.groupingBy(vo->vo.getCid(),Collectors.groupingBy(vo->vo.getModuleName()+","+vo.getTxid())));
		}
				
		if(isEs) {
			for(String cidKey : esCidMap.keySet()) {
				Map<String,List<DgrDashboardEsLog>> apiMap = esCidMap.get(cidKey);
				for(String apiKey : apiMap.keySet()) {
					List<DgrDashboardEsLog> apiList = apiMap.get(apiKey); 
					
					long total = 0;
					long success = 0;
					long fail = 0;
					long elapse = 0;
					long maxElapse = 0;
					long minElapse = 0;
					
					//計算總數,成功數,失敗數,回應時間
					for(DgrDashboardEsLog apiVo : apiList){
						if("Y".equals(apiVo.getExeStatus())) {
							if(success == 0) {
								maxElapse = apiVo.getElapse().longValue();
								minElapse = apiVo.getElapse().longValue();
								//medianVo的max和min初始化
								if(medianVo.getFrequency() == 0) {
									medianVo.setMaxElapse(maxElapse);
									medianVo.setMinElapse(minElapse);
								}
								
							}else {
								if(apiVo.getElapse().longValue() > maxElapse) {
									maxElapse = apiVo.getElapse().longValue();
								}
								if(apiVo.getElapse().longValue() < minElapse) {
									minElapse = apiVo.getElapse().longValue();
								}
							}
							success++;
							elapse += apiVo.getElapse().longValue();
						}else if("N".equals(apiVo.getExeStatus())) {
							fail++;
						}
						total++;
					}
					
					//設定medianVo
					medianVo.setFrequency(medianVo.getFrequency() + success);
					medianVo.setTotalElapse(medianVo.getTotalElapse() + elapse);
					
					if(success > 0) {
						if(maxElapse > medianVo.getMaxElapse()) {
							medianVo.setMaxElapse(maxElapse);
						}
						if(minElapse < medianVo.getMinElapse()) {
							medianVo.setMinElapse(minElapse);
						}
						
					}
					
					
					DgrDashboardLastData vo =  lastDataList.stream().filter(f->cidKey.equals(f.getStr1()) && apiKey.equals(f.getStr2())).findAny().orElse(null);
					if(vo == null) {
						vo = new DgrDashboardLastData();
						vo.setTimeType(timeType.value());
						vo.setDashboardType(DashboardReportTypeEnum.CLIENT_USAGE_METRICS.value());
						vo.setStr1(cidKey);
						vo.setStr2(apiKey);//暫存,資料處理完再取apiName
						vo.setNum1(total);
						vo.setNum2(success);
						vo.setNum3(fail);
						vo.setNum4(elapse);//暫存,資料處理完再平均
						lastDataList.add(vo);
					}else {
						vo.setNum1(vo.getNum1() + total);
						vo.setNum2(vo.getNum2() + success);
						vo.setNum3(vo.getNum3() + fail);
						vo.setNum4(vo.getNum4() + elapse);//暫存,資料處理完再平均
					}
				}
			}
		}else {
			for(String cidKey : rdbCidMap.keySet()) {
				Map<String,List<TsmpReqResLogHistory>> apiMap = rdbCidMap.get(cidKey);
				for(String apiKey : apiMap.keySet()) {
					List<TsmpReqResLogHistory> apiList = apiMap.get(apiKey);
					long total = 0;
					long success = 0;
					long fail = 0;
					long elapse = 0;
					long maxElapse = 0;
					long minElapse = 0;
					//計算總數,成功數,失敗數,回應時間
					for(TsmpReqResLogHistory apiVo : apiList){
						if("Y".equals(apiVo.getExeStatus())) {
							if(success == 0) {
								maxElapse = apiVo.getElapse().longValue();
								minElapse = apiVo.getElapse().longValue();
								//medianVo的max和min初始化
								if(medianVo.getFrequency() == 0) {
									medianVo.setMaxElapse(maxElapse);
									medianVo.setMinElapse(minElapse);
								}
								
							}else {
								if(apiVo.getElapse().longValue() > maxElapse) {
									maxElapse = apiVo.getElapse().longValue();
								}
								if(apiVo.getElapse().longValue() < minElapse) {
									minElapse = apiVo.getElapse().longValue();
								}
							}
							success++;
							elapse += apiVo.getElapse().longValue();
						}else if("N".equals(apiVo.getExeStatus())) {
							fail++;
						}
						total++;
					}
					
					//設定medianVo
					medianVo.setFrequency(medianVo.getFrequency() + success);
					medianVo.setTotalElapse(medianVo.getTotalElapse() + elapse);
					if(success > 0) {
						if(maxElapse > medianVo.getMaxElapse()) {
							medianVo.setMaxElapse(maxElapse);
						}
						if(minElapse < medianVo.getMinElapse()) {
							medianVo.setMinElapse(minElapse);
						}
					}

					DgrDashboardLastData vo =  lastDataList.stream().filter(f->cidKey.equals(f.getStr1()) && apiKey.equals(f.getStr2())).findAny().orElse(null);
					if(vo == null) {
						vo = new DgrDashboardLastData();
						vo.setTimeType(timeType.value());
						vo.setDashboardType(DashboardReportTypeEnum.CLIENT_USAGE_METRICS.value());
						vo.setStr1(cidKey);
						vo.setStr2(apiKey);//暫存,資料處理完再取apiName
						vo.setNum1(total);
						vo.setNum2(success);
						vo.setNum3(fail);
						vo.setNum4(elapse);//暫存,資料處理完再平均
						lastDataList.add(vo);
					}else {
						vo.setNum1(vo.getNum1() + total);
						vo.setNum2(vo.getNum2() + success);
						vo.setNum3(vo.getNum3() + fail);
						vo.setNum4(vo.getNum4() + elapse);//暫存,資料處理完再平均
					}
				}
			}
		}
		
	}
	
	private String getApiName(String moduleName, String apiKey) {
		TsmpApiId apiIdVo = new TsmpApiId();
		apiIdVo.setModuleName(moduleName);
		apiIdVo.setApiKey(apiKey);
		TsmpApi apiVo = getTsmpApiCacheProxy().findById(apiIdVo).orElse(null);
		if(apiVo != null) {
			return apiVo.getApiName();
		}else {
			return apiKey + "(" + moduleName + ")";
		}
	}

	private Date getStartDate(Date nowDate, DashboardTimeTypeEnum timeType) {
		
		if (nowDate == null) {
			nowDate = DateTimeUtil.now();
		}
		
		String strDate = null;
		Date startDate = nowDate;
		Calendar c = Calendar.getInstance();
		c.setTime(nowDate);
		if(timeType == DashboardTimeTypeEnum.DAY) {
			c.add(Calendar.DATE, -1);
		}else if(timeType == DashboardTimeTypeEnum.MONTH) {
			c.add(Calendar.MONTH, -1);
			
		}else if(timeType == DashboardTimeTypeEnum.YEAR) {
			c.add(Calendar.YEAR, -1);
			
		}else {//分
			c.add(Calendar.MINUTE, -10);
		}
		nowDate = c.getTime();
		strDate = this.dateToStringForyyyyMMddHHmm(nowDate);
		
		strDate = StringUtils.hasText(strDate) ? strDate.substring(0, 15) : null;
		strDate = strDate + "0:00.000";
		Optional<Date> startDateOpt = DateTimeUtil.stringToDateTime(strDate, DateTimeFormatEnum.西元年月日時分秒毫秒_2);
		if (startDateOpt.isPresent()) {
			startDate = startDateOpt.get();
		}
		
		return startDate;

	}
	
	private Date getEndDate(Date nowDate) {
				
		if (nowDate == null) {
			nowDate = DateTimeUtil.now();
		}
		
		Calendar c = Calendar.getInstance();
		c.setTime(nowDate);
		c.add(Calendar.MINUTE, -10);
		nowDate = c.getTime();
		String strDate = this.dateToStringForyyyyMMddHHmm(nowDate);
		Date endDate = nowDate;
		strDate = StringUtils.hasText(strDate) ? strDate.substring(0, 15) : null;
		strDate = strDate + "9:59.999";
		Optional<Date> endDateOpt = DateTimeUtil.stringToDateTime(strDate, DateTimeFormatEnum.西元年月日時分秒毫秒_2);
		if (endDateOpt.isPresent()) {
			endDate = endDateOpt.get();
		}
		return endDate;

	}
	
	private String dateToStringForyyyyMMddHHmmssSSS(Date date) {
		if(date == null) {
			return null;
		}
		synchronized (yyyyMMddHHmmssSSS) {
			return yyyyMMddHHmmssSSS.format(date);
		}
	}
	
	private String dateToStringForyyyyMMddHHmm(Date date) {
		synchronized (yyyyMMddHHmm) {
			return yyyyMMddHHmm.format(date);
		}
	}
	
	private String dateToStringForyyyyMMddHH(Date date) {
		if(date == null) {
			return null;
		}
		synchronized (yyyyMMddHH) {
			return yyyyMMddHH.format(date);
		}
	}
	
	private String dateToStringForyyyyMMdd(Date date) {
		if(date == null) {
			return null;
		}
		synchronized (yyyyMMdd) {
			return yyyyMMdd.format(date);
		}
	}
	
	private String dateToStringForHHmm(Date date) {
		if(date == null) {
			return null;
		}
		synchronized (HHmm) {
			return HHmm.format(date);
		}
	}
	
	private String dateToStringFormm(Date date) {
		if(date == null) {
			return null;
		}
		synchronized (mm) {
			return mm.format(date);
		}
	}
	
	private String dateToStringForMMdd(Date date) {
		if(date == null) {
			return null;
		}
		synchronized (MMdd) {
			return MMdd.format(date);
		}
	}
	
	private void sleepforColddownCPU(long ms, String msg) {
//		TPILogger.tl.info(ms + " -- " + msg);
//		try {
//			Thread.sleep(ms);
//		} catch (InterruptedException e) {
//			Thread.currentThread().interrupt();
//		}
	}
	
	protected Integer getPageSize() {
		return this.getTsmpSettingService().getVal_API_DASHBOARD_BATCH_QUANTITY();
	}

	protected DgrDashboardLastDataDao getDgrDashboardLastDataDao() {
		return dgrDashboardLastDataDao;
	}

	protected TsmpReqResLogHistoryDao getTsmpReqResLogHistoryDao() {
		return tsmpReqResLogHistoryDao;
	}

	protected TsmpApiCacheProxy getTsmpApiCacheProxy() {
		return tsmpApiCacheProxy;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}

	protected HandleDashboardDataByYearService getHandleDashboardDataByYearService() {
		return handleDashboardDataByYearService;
	}

	protected DgrDashboardEsLogDao getDgrDashboardEsLogDao() {
		return dgrDashboardEsLogDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return tsmpDpApptJobDao;
	}

	
}
