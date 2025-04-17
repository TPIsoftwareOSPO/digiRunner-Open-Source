package tpi.dgrv4.dpaa.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
import tpi.dgrv4.dpaa.vo.DashboardAideVo;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.entity.jpql.DgrDashboardEsLog;
import tpi.dgrv4.entity.entity.jpql.DgrDashboardLastData;
import tpi.dgrv4.entity.entity.jpql.TsmpReqResLogHistory;
import tpi.dgrv4.entity.repository.DgrDashboardEsLogDao;
import tpi.dgrv4.entity.repository.DgrDashboardLastDataDao;
import tpi.dgrv4.entity.repository.TsmpReqResLogHistoryDao;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpApiCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.ISysInfoService;

@Service
public class HandleDashboardDataByYearService {
//	@Autowired(required = false)
	private ISysInfoService sysInfoService;
	
	private DgrDashboardLastDataDao dgrDashboardLastDataDao;
	private TsmpReqResLogHistoryDao tsmpReqResLogHistoryDao;
	private DgrDashboardEsLogDao dgrDashboardEsLogDao;
	private TsmpApiCacheProxy tsmpApiCacheProxy;
	
	private DateFormat yyyyMMddHHmmssSSS = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
	private DateFormat yyyyMMddHHmm = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	private DateFormat yyyyMM = new SimpleDateFormat("yyyy/MM");

	private static final String NO_ENTERPRISE_SERVICE = "...No Enterprise Service...";
	
	@Autowired
	public HandleDashboardDataByYearService(@Nullable ISysInfoService sysInfoService,
			DgrDashboardLastDataDao dgrDashboardLastDataDao, TsmpReqResLogHistoryDao tsmpReqResLogHistoryDao,
			DgrDashboardEsLogDao dgrDashboardEsLogDao, TsmpApiCacheProxy tsmpApiCacheProxy) {
		super();
		this.sysInfoService = sysInfoService;
		this.dgrDashboardLastDataDao = dgrDashboardLastDataDao;
		this.tsmpReqResLogHistoryDao = tsmpReqResLogHistoryDao;
		this.dgrDashboardEsLogDao = dgrDashboardEsLogDao;
		this.tsmpApiCacheProxy = tsmpApiCacheProxy;
	}

	@Transactional
	public void exec(Date execDate, boolean isEs) {

		TPILogger.tl.debug("--- Begin HandleDashboardDataBy\"Year\"Service ---");
		if (sysInfoService != null) {
			TPILogger.tl.info(sysInfoService.getCpuMemoryInfo());
		} else {
			TPILogger.tl.info(NO_ENTERPRISE_SERVICE);
		}
		
		List<DgrDashboardLastData> saveLastList = new ArrayList<>();
		// 資料日期
		Date startDate = this.getStartDate(execDate);
		Date endDate = this.getEndDate(execDate);
		
		TPILogger.tl.debug("year startDate = " + this.dateToStringForyyyyMMddHHmmssSSS(startDate));
		TPILogger.tl.debug("endDate = " + this.dateToStringForyyyyMMddHHmmssSSS(endDate));

		//1.資料時間
		execDataTime(startDate, endDate, saveLastList);
		//5.Bad Attempt
		execBadAttempt(isEs, saveLastList);
		//7.中位數
		execMedianAndAvg(isEs, saveLastList);
		//10.API流量分佈
		execApiTrafficDistribution(startDate, endDate, isEs, saveLastList);
		//12.客戶端使用指標
		List<DgrDashboardLastData> yearClientLastDataList = execClientUsageMetrics(isEs, saveLastList);
		TPILogger.tl.info("year.lastData.size=" + yearClientLastDataList.size());
		//3.Success
		execSuccess(yearClientLastDataList, DashboardTimeTypeEnum.YEAR, saveLastList);
		//4.Fail
		long total = execFail(yearClientLastDataList, DashboardTimeTypeEnum.YEAR, saveLastList);
		//2.Request
		execRequest(total, DashboardTimeTypeEnum.YEAR, saveLastList);
		//11.Client使用佔比
		execClientUsagePercentage(yearClientLastDataList, DashboardTimeTypeEnum.YEAR, saveLastList);
		yearClientLastDataList = null;

		getDgrDashboardLastDataDao().saveAll(saveLastList);

		TPILogger.tl.debug("--- Finish HandleDashboardDataBy\"Year\"Service ---");
		if (sysInfoService != null) {
			TPILogger.tl.info(sysInfoService.getCpuMemoryInfo());
		} else {
			TPILogger.tl.info(NO_ENTERPRISE_SERVICE);
		}
	}
	
	private void execDataTime(Date startDate, Date endDate, List<DgrDashboardLastData> saveLastList) {
		String strStartDate = this.dateToStringForyyyyMMddHHmm(startDate);
		String strEndDate = this.dateToStringForyyyyMMddHHmm(endDate);
		
		DgrDashboardLastData vo = new DgrDashboardLastData();
		vo.setTimeType(DashboardTimeTypeEnum.YEAR.value());
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
			String k = String.format("%,.1f",bdTotal.divide(new BigDecimal(Integer.toString(1000)), 1, RoundingMode.HALF_UP).doubleValue());
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
	
	private void execBadAttempt(boolean isEs, List<DgrDashboardLastData> saveLastList) {
		
		List<Map> list = null;
		if(isEs) {
			list = getDgrDashboardEsLogDao().queryByBadAttempt();
		}else {
			list = getTsmpReqResLogHistoryDao().queryByBadAttempt();
		}
		
		long total = 0;
		long num401 = 0;
		long num403 = 0;
		long other = 0;
		
		for(Map map : list) {
			String httpStatus = map.get("http_status").toString();
			if("401".equals(httpStatus)) {
				num401 = Long.parseLong(map.get("frequency").toString());
			}else if("403".equals(httpStatus)) {
				num403 = Long.parseLong(map.get("frequency").toString());
			}else {
				other += Long.parseLong(map.get("frequency").toString());
			}
			total += Long.parseLong(map.get("frequency").toString());
		}
		
		DgrDashboardLastData vo = new DgrDashboardLastData();
		vo.setTimeType(DashboardTimeTypeEnum.YEAR.value());
		vo.setDashboardType(DashboardReportTypeEnum.BAD_ATTEMPT.value());
		vo.setNum1(total);
		vo.setNum2(num401);
		vo.setNum3(num403);
		vo.setNum4(other);
		
		saveLastList.add(vo);
	}
	
	private void execMedianAndAvg(boolean isEs, List<DgrDashboardLastData> saveLastList) {
		long median = 0;
		long min = 0;
		long max = 0;
		long total = 0;
		long avg = 0;
		
		List<Map> medianList = null;
		if(isEs) {
			medianList = getDgrDashboardEsLogDao().queryByMedian();
		}else {
			medianList = getTsmpReqResLogHistoryDao().queryByMedian();
		}
		Map map = medianList.get(0);
		long frequency = Long.parseLong(map.get("frequency").toString());
		if(frequency > 0) {
			int remainder = (int)(frequency % 2);
			int number = (int)(frequency / 2);
			if(remainder == 1) {//奇數
				Pageable pageable = PageRequest.of(number, 1, Sort.by("elapse").ascending());
				if(isEs) {
					Page<DgrDashboardEsLog> pageResult = getDgrDashboardEsLogDao().findByExeStatus("Y", pageable);
					List<DgrDashboardEsLog> list =  pageResult.getContent();
					median = list.get(0).getElapse().longValue();
				}else {
					Page<TsmpReqResLogHistory> pageResult = getTsmpReqResLogHistoryDao().findByExeStatus("Y", pageable);
					List<TsmpReqResLogHistory> list =  pageResult.getContent();
					median = list.get(0).getElapse().longValue();
				}
				
			}else {//偶數
				Pageable pageable = PageRequest.of(number, 1, Sort.by("elapse").ascending());
				int elapse1 = 0;
				int elapse2 = 0;
				if(isEs) {
					Page<DgrDashboardEsLog> pageResult = getDgrDashboardEsLogDao().findByExeStatus("Y", pageable);
					List<DgrDashboardEsLog> list =  pageResult.getContent();
					elapse1 = list.get(0).getElapse().intValue();
					pageable = pageResult.previousPageable();
					pageResult = getDgrDashboardEsLogDao().findByExeStatus("Y", pageable);
					list =  pageResult.getContent();
					elapse2 = list.get(0).getElapse().intValue();
				}else {
					Page<TsmpReqResLogHistory> pageResult = getTsmpReqResLogHistoryDao().findByExeStatus("Y", pageable);
					List<TsmpReqResLogHistory> list =  pageResult.getContent();
					elapse1 = list.get(0).getElapse().intValue();
					
					pageable = pageResult.previousPageable();
					pageResult = getTsmpReqResLogHistoryDao().findByExeStatus("Y", pageable);
					list =  pageResult.getContent();
					elapse2 = list.get(0).getElapse().intValue();
				}
				
				
				BigDecimal bdElapse = new BigDecimal(String.valueOf(elapse1 + elapse2));
				median = bdElapse.divide(new BigDecimal(String.valueOf(2)), 0, RoundingMode.HALF_UP).longValue();
				
			}
			
			min = Long.parseLong(map.get("min_val").toString());
			max = Long.parseLong(map.get("max_val").toString());		
			total = Long.parseLong(map.get("total").toString());
			BigDecimal bdTotal = new BigDecimal(String.valueOf(total));
			avg = bdTotal.divide(new BigDecimal(String.valueOf(frequency)), 0, RoundingMode.HALF_UP).longValue();

		}
		
		//中位數
		DgrDashboardLastData vo = new DgrDashboardLastData();
		vo.setTimeType(DashboardTimeTypeEnum.YEAR.value());
		vo.setDashboardType(DashboardReportTypeEnum.MEDIAN.value());
		vo.setNum1(min);
		vo.setNum2(max);
		vo.setNum3(median);
		saveLastList.add(vo);
		
		//平均數
		vo = new DgrDashboardLastData();
		vo.setTimeType(DashboardTimeTypeEnum.YEAR.value());
		vo.setDashboardType(DashboardReportTypeEnum.AVG.value());
		vo.setNum1(avg);
		saveLastList.add(vo);
		
	}
	
	private void execApiTrafficDistribution(Date startDate, Date endDate, boolean isEs, List<DgrDashboardLastData> saveLastList) 
	{
		
		List<Map> list = null;
		if(isEs) {
			list = getDgrDashboardEsLogDao().queryByApiTrafficDistribution();
		}else {
			list = getTsmpReqResLogHistoryDao().queryByApiTrafficDistribution();
		}
		Map<String,List<Map>> dataMap = list.stream().collect(Collectors.groupingBy(map->map.get("rtime_year_month").toString()));
		Date rtime = null;
		String strEndDate = this.dateToStringForyyyyMM(endDate);
		for(int i=0 ; i<13; i++) {
			if(i == 0) {
				rtime = startDate;
			}else {
				rtime = getApiTrafficTime(rtime);
			}
			
			String strRtime = this.dateToStringForyyyyMM(rtime);
			if(strRtime.compareTo(strEndDate) > 0) {
				break;
			}
			
			long success = 0;
			long fail = 0;
			if(dataMap.get(strRtime) != null) {
				list = dataMap.get(strRtime);
				for(Map map : list) {
					if("Y".equals(map.get("exe_status").toString())) {
						success = Long.parseLong(map.get("frequency").toString());
					}else if("N".equals(map.get("exe_status").toString())) {
						fail = Long.parseLong(map.get("frequency").toString());
					}
				}
			}
			
			DgrDashboardLastData vo = new DgrDashboardLastData();
			vo.setTimeType(DashboardTimeTypeEnum.YEAR.value());
			vo.setDashboardType(DashboardReportTypeEnum.API_TRAFFIC_DISTRIBUTION.value());
			vo.setStr1(strRtime);
			vo.setNum1(success);
			vo.setNum2(fail);
			vo.setSortNum(i+1);
			
			saveLastList.add(vo);
		}
				
	}
	
	private Date getApiTrafficTime(Date startDate) {
		Calendar c = Calendar.getInstance();
		c.setTime(startDate);
		c.add(Calendar.MONTH, 1);
		
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
	
	private List<DgrDashboardLastData> execClientUsageMetrics(boolean isEs, List<DgrDashboardLastData> saveLastList) {
		
		List<Map> list = null;
		if(isEs) {
			list = getDgrDashboardEsLogDao().queryByClientUsageMetrics();
		}else {
			list = getTsmpReqResLogHistoryDao().queryByClientUsageMetrics();
		}
		
		for(Map map : list) {
			if(map.get("cid") == null) {
				map.put("cid", "");
			}
		}
		
		if(CollectionUtils.isEmpty(list)) {
			return new ArrayList<DgrDashboardLastData>();
		}
		
		//各個clientId次數
		Map<String,Long> countMap = list.stream().collect(Collectors.groupingBy(map->map.get("cid").toString(), 
				() -> new TreeMap<>(), Collectors.summingLong(map->Long.parseLong(map.get("frequency").toString()))));
		//依最高次數排序
		List<Map.Entry<String, Long>> sortList = new ArrayList<Map.Entry<String, Long>>(countMap.entrySet());
		sortList.sort(new Comparator<Map.Entry<String, Long>>() {
			@Override
			public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		
		//回傳的結果的List,提供給其他圖表計算運用
		List<DgrDashboardLastData> lastDataList = new ArrayList<>();
		
		//以cid為gorup,再以moduleName+txid為group, 20240814在v3版的txid有可能是null,在SCB遇到
		Map<String, Map<String,List<Map>>> cidMap = list.stream().collect(Collectors.groupingBy(map->map.get("cid").toString(),
				           () -> new TreeMap<>(), Collectors.groupingBy(map->map.get("module_name").toString()+","+(String)map.get("txid"))));
		Map<String, DashboardAideVo> otherMap = new LinkedHashMap<>();
		for(String cidKey : cidMap.keySet()) {
			Map<String,List<Map>> apiMap = cidMap.get(cidKey);
			boolean isTop5 = false;
			for(int i=0; i<5; i++) {
				if(i>=sortList.size()) {
					break;
				}
				String sortKey = sortList.get(i).getKey();
				//前5名
				if(cidKey.equals(sortKey)) {
					isTop5 = true;
					for(String apiKey : apiMap.keySet()) {
						List<Map> apiList = apiMap.get(apiKey);
						long total = 0;
						long success = 0;
						long fail = 0;
						long avgTime = 0;
						long elapse = 0;
						//計算前5名的總數,成功數,失敗數,平均時間
						for(Map map : apiList){
							if("Y".equals(map.get("exe_status").toString())) {
								success = Long.parseLong(map.get("frequency").toString());
								elapse = Long.parseLong(map.get("elapse").toString());
							}else if("N".equals(map.get("exe_status").toString())) {
								fail = Long.parseLong(map.get("frequency").toString());
							}
							total += Long.parseLong(map.get("frequency").toString());
						}

						//求平均回應時間,四捨五入
						BigDecimal bdElapse = new BigDecimal(String.valueOf(elapse));
						BigDecimal bdFrequency = new BigDecimal(String.valueOf(success));
						if(elapse > 0 && success > 0) {
							avgTime = bdElapse.divide(bdFrequency, 0, RoundingMode.HALF_UP).longValue();
						}
						
						//前5名insert data
						DgrDashboardLastData vo = new DgrDashboardLastData();
						vo.setTimeType(DashboardTimeTypeEnum.YEAR.value());
						vo.setDashboardType(DashboardReportTypeEnum.CLIENT_USAGE_METRICS.value());
						vo.setStr1(StringUtils.hasText(cidKey) ? cidKey : "NoAuth");
						//20240814在v3版的txid有可能是null,在SCB遇到
						vo.setStr2(this.getApiName(apiList.get(0).get("module_name").toString(), (String)apiList.get(0).get("txid")));
						vo.setNum1(total);
						vo.setNum2(success);
						vo.setNum3(fail);
						vo.setNum4(avgTime);
						vo.setSortNum(i+1);
						
						saveLastList.add(vo);
						lastDataList.add(vo);
					}
					break;
				}
			}
			//others
			if(!isTop5) {
				for(String apiKey : apiMap.keySet()) {
					List<Map> apiList = apiMap.get(apiKey);
					//計算others的總數,成功數,失敗數,平均時間
					for(Map map : apiList) {
						DashboardAideVo daVo = null;
						if(otherMap.get(apiKey) == null) {
							daVo = new DashboardAideVo();
							daVo.setMoudleName(map.get("module_name").toString());
							//20240814在v3版的txid有可能是null,在SCB遇到
							daVo.setApiKey((String)map.get("txid"));
							otherMap.put(apiKey, daVo);
						}else {
							daVo = otherMap.get(apiKey);
						}
						
						if("Y".equals(map.get("exe_status").toString())) {
							daVo.setSuccess(daVo.getSuccess() + Long.parseLong(map.get("frequency").toString()));
							daVo.setElapse(daVo.getElapse() + Long.parseLong(map.get("elapse").toString()));
						}else if("N".equals(map.get("exe_status").toString())) {
							daVo.setFail(daVo.getFail() + Long.parseLong(map.get("frequency").toString()));
						}
						daVo.setTotal(daVo.getTotal() + Long.parseLong(map.get("frequency").toString()));
					}
				}
			}
		}
		//others的insert data
		if(!otherMap.isEmpty()) {
			otherMap.forEach((k, v) ->{
				DgrDashboardLastData vo = new DgrDashboardLastData();
				vo.setTimeType(DashboardTimeTypeEnum.YEAR.value());
				vo.setDashboardType(DashboardReportTypeEnum.CLIENT_USAGE_METRICS.value());
				vo.setStr1("Others");
				vo.setStr2(this.getApiName(v.getMoudleName(), v.getApiKey()));
				vo.setNum1(v.getTotal());
				vo.setNum2(v.getSuccess());
				vo.setNum3(v.getFail());
				//四捨五入
				BigDecimal bdElapse = new BigDecimal(Long.toString(v.getElapse()));
				BigDecimal bdSuccess = new BigDecimal(Long.toString(v.getSuccess()));
				long avgTime = 0;
				if(v.getElapse() > 0 && v.getSuccess() > 0) {
					avgTime = bdElapse.divide(bdSuccess, 0, RoundingMode.HALF_UP).longValue();
				}
				vo.setNum4(avgTime);
				vo.setSortNum(6);
				
				saveLastList.add(vo);
				lastDataList.add(vo);
			});
		}
		
		return lastDataList;
		
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

	private Date getStartDate(Date nowDate) {
		String strDate = null;
		Date startDate = null;
		Calendar c = Calendar.getInstance();
		c.setTime(nowDate);
		c.add(Calendar.YEAR, -1);
		
		nowDate = c.getTime();
		strDate = this.dateToStringForyyyyMMddHHmm(nowDate);
		
		strDate = strDate.substring(0, 15);
		strDate = strDate + "0:00.000";
		startDate = DateTimeUtil.stringToDateTime(strDate, DateTimeFormatEnum.西元年月日時分秒毫秒_2).orElseThrow(TsmpDpAaRtnCode._1295::throwing);
		
		return startDate;

	}
	
	private Date getEndDate(Date nowDate) {
		Calendar c = Calendar.getInstance();
		c.setTime(nowDate);
		c.add(Calendar.MINUTE, -10);
		nowDate = c.getTime();
		String strDate = this.dateToStringForyyyyMMddHHmm(nowDate);
		
		strDate = strDate.substring(0, 15);
		strDate = strDate + "9:59.999";
		Date endDate = DateTimeUtil.stringToDateTime(strDate, DateTimeFormatEnum.西元年月日時分秒毫秒_2).orElseThrow(TsmpDpAaRtnCode._1295::throwing);
		
		return endDate;

	}
	
	private String dateToStringForyyyyMMddHHmmssSSS(Date date) {
		synchronized (yyyyMMddHHmmssSSS) {
			return yyyyMMddHHmmssSSS.format(date);
		}
	}
	private String dateToStringForyyyyMMddHHmm(Date date) {
		synchronized (yyyyMMddHHmm) {
			return yyyyMMddHHmm.format(date);
		}
	}
	private String dateToStringForyyyyMM(Date date) {
		synchronized (yyyyMM) {
			return yyyyMM.format(date);
		}
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
	
	protected DgrDashboardEsLogDao getDgrDashboardEsLogDao() {
		return dgrDashboardEsLogDao;
	}

}
