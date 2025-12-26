package tpi.dgrv4.escape;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.ReportDateTimeRangeTypeEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA1204DataSetResp;
import tpi.dgrv4.dpaa.vo.AA1204Req;
import tpi.dgrv4.dpaa.vo.AA1204Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.jpql.TsmpReportData;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpReportDataDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1204Service {

	private TPILogger logger = TPILogger.tl;

	private BcryptParamHelper bcryptParamHelper;
	private TsmpReportDataDao tsmpReportDataDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private TsmpOrganizationDao tsmpOrganizationDao;

	@Autowired
	public AA1204Service(BcryptParamHelper bcryptParamHelper, TsmpReportDataDao tsmpReportDataDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpOrganizationDao tsmpOrganizationDao) {
		super();
		this.bcryptParamHelper = bcryptParamHelper;
		this.tsmpReportDataDao = tsmpReportDataDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
	}

	public AA1204Resp queryApiTraffic(TsmpAuthorization authorization, AA1204Req req, ReqHeader reqHeader) {
		AA1204Resp resp = null;
		try {
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());

			// <=== 決定時間類型
			String timeType = getTimeTypeByBcryptParamHelper(req.getTimeType(), locale);
			ReportDateTimeRangeTypeEnum reportDateTimeRangeTypeEnum = null;

			if ("MINUTE".equals(timeType)) {
				reportDateTimeRangeTypeEnum = ReportDateTimeRangeTypeEnum.MINUTE;
				if(!StringUtils.hasText(req.getStartHour())) {
					throw TsmpDpAaRtnCode._1350.throwing("{{startHour}}");
				}
				if(!StringUtils.hasText(req.getEndHour())) {
					throw TsmpDpAaRtnCode._1350.throwing("{{endHour}}");
				}
			}else if ("DAY".equals(timeType)) {
				reportDateTimeRangeTypeEnum = ReportDateTimeRangeTypeEnum.DAY;
			} else {
				reportDateTimeRangeTypeEnum = ReportDateTimeRangeTypeEnum.MONTH;
			}
			// ===> 決定時間類型

			// <===查詢資料與轉換成Chart.Js格式
			
			// 取得組織原則
			List<String> orgList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(authorization.getOrgId(), Integer.MAX_VALUE); // 組織與子組織的orgId
						
			List<TsmpReportData> data = getTsmpReportDataDao().queryApiTraffic(getNow(), req.getStartDate(),
					req.getEndDate(), req.getStartHour(), req.getEndHour(), orgList, reportDateTimeRangeTypeEnum);

			if (data == null || data.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			resp = convertToReportJson(req.getStartDate(),req.getEndDate(), req.getStartHour(), req.getEndHour(), reportDateTimeRangeTypeEnum,data, locale);
			// ===>查詢資料與轉換成Chart.Js格式

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private AA1204Resp convertToReportJson(String startDate,String endDate, String startHour, String endHour, ReportDateTimeRangeTypeEnum dateTimeRangeType,List<TsmpReportData> data, String locale) {

		// <=== 準備Chart.JS基本資料
		AA1204Resp resp = new AA1204Resp();

		resp.setReportName(nvl(getSbNameByItemsSbNo("REPORT_NAME", "API_TRAFFIC", locale)));
		resp.setxLable(nvl(getSbNameByItemsSbNo("REPORT_LABLE_CODE", "TIME", locale)));
		resp.setyLable(nvl(getSbNameByItemsSbNo("REPORT_LABLE_CODE", "FREQUENCY", locale)));

		List<String> labels = new ArrayList<String>();

		List<AA1204DataSetResp> datasets = new ArrayList<AA1204DataSetResp>();
		AA1204DataSetResp success = new AA1204DataSetResp();
		String successLabel = nvl(getSbNameByItemsSbNo("REPORT_LABLE_CODE", "Y", locale));

		List<Integer> successData = new ArrayList<Integer>();
		success.setLabel(successLabel);
		success.setData(successData);
		datasets.add(success);
		
		AA1204DataSetResp fail = new AA1204DataSetResp();
		String failLabel = nvl(getSbNameByItemsSbNo("REPORT_LABLE_CODE", "N", locale));
		List<Integer> failData = new ArrayList<Integer>();
		fail.setLabel(failLabel);
		fail.setData(failData);
		datasets.add(fail);

		// ===> 準備Chart.JS基本資料

		
		
		// <=== '時間(LAST_ROW_DATE_TIME)'資料分組
		Map<String, List<TsmpReportData>> map = data.stream().collect(Collectors.groupingBy(aa1204_r -> {
			if (dateTimeRangeType == ReportDateTimeRangeTypeEnum.MINUTE) {
				if(aa1204_r.getLastRowDateTime() != null) {
					return DateTimeUtil.dateTimeToString(aa1204_r.getLastRowDateTime(), DateTimeFormatEnum.時分).orElse(null).substring(0,4) + "0";
				}else {
					return null;
				}
			}else if (dateTimeRangeType == ReportDateTimeRangeTypeEnum.DAY) {
				return DateTimeUtil.dateTimeToString(aa1204_r.getLastRowDateTime(), DateTimeFormatEnum.西元年月日_2).orElse(null);	
			}else {
				return DateTimeUtil.dateTimeToString(aa1204_r.getLastRowDateTime(), DateTimeFormatEnum.西元年月_2).orElse(null);
			}
			
		}));
		// ===> '時間(LAST_ROW_DATE_TIME)'資料分組
		
		
		// <=== 補齊缺少的日期
		HashSet<String> totalDates = new HashSet<String>();
		try {
			
			SimpleDateFormat aa1204_sdf = null;
			if (dateTimeRangeType == ReportDateTimeRangeTypeEnum.MINUTE) {
				aa1204_sdf = new SimpleDateFormat(DateTimeFormatEnum.時分.value());
			}else if (dateTimeRangeType == ReportDateTimeRangeTypeEnum.DAY) {
				aa1204_sdf = new SimpleDateFormat(DateTimeFormatEnum.西元年月日_2.value());
			}else {
				aa1204_sdf = new SimpleDateFormat(DateTimeFormatEnum.西元年月.value());
			}
			
			if (dateTimeRangeType == ReportDateTimeRangeTypeEnum.MINUTE) {
				Date start = aa1204_sdf.parse(startHour + ":00");
				Date end = aa1204_sdf.parse(endHour + ":59");
				//checkmarx, Unchecked Input for Loop Condition,所以多了maxValue和loopIndex
				int maxValue = 145;
				int loopIndex = 0;
			    while (!start.after(end) && loopIndex <= maxValue) {
			    	loopIndex++;
					totalDates.add(aa1204_sdf.format(start));
					Calendar aa1204_c = Calendar.getInstance();
					aa1204_c.setTime(start);
					aa1204_c.add(Calendar.MINUTE, 10);
					start = aa1204_c.getTime();
				}
			}else {
				Date start = aa1204_sdf.parse(startDate);
				Date end = aa1204_sdf.parse(endDate);
				//checkmarx, Unchecked Input for Loop Condition,所以多了maxValue和loopIndex
				int maxValue = 367;
				int loopIndex = 0;
			    while (!start.after(end) && loopIndex <= maxValue) {
			    	if(loopIndex == maxValue) {
			    		throw TsmpDpAaRtnCode._1559.throwing("Exceed " + maxValue + " day");
			    	}
			    	loopIndex++;
					totalDates.add(aa1204_sdf.format(start));
					Calendar aa1204_c = Calendar.getInstance();
					aa1204_c.setTime(start);
					aa1204_c.add(Calendar.DATE, 1);
					start = aa1204_c.getTime();
			    }
			}
			
			
		} catch (ParseException e1) {
			this.logger.error(StackTraceUtil.logStackTrace(e1));
		}
		for (String day : totalDates) {
			if (map.containsKey(day)==false) {
				map.put(day, new ArrayList<TsmpReportData>());
			}
		}
		// ===> 補齊缺少的日期
		

		// <=== 進行排序，以時間(LAST_ROW_DATE_TIME)為排序條件由最舊時間往最新時間
		List<Map.Entry<String, List<TsmpReportData>>> list = new ArrayList<Map.Entry<String, List<TsmpReportData>>>(
				map.entrySet());

		list.sort(new Comparator<Map.Entry<String, List<TsmpReportData>>>() {
			@Override
			public int compare(Map.Entry<String, List<TsmpReportData>> o1, Map.Entry<String, List<TsmpReportData>> o2) {

				return o1.getKey().compareTo(o2.getKey());

			}
		});
		// ===> 進行排序，以時間(LAST_ROW_DATE_TIME)為排序條件由最舊時間往最新時間

		
		
		
		// <=== 產生Chart.JS需要的資料格式
		for (Map.Entry<String, List<TsmpReportData>> entry : list) {

			labels.add(entry.getKey());

			int successValue = 0;
			int failValue = 0;
			for (TsmpReportData r : entry.getValue()) {
				if ("Y".equals(r.getStringGroup1())) {
					successValue = successValue + r.getIntValue1().intValue();
				}
				if ("N".equals(r.getStringGroup1())) {
					failValue = failValue + r.getIntValue1().intValue();
				}
			}

			datasets.get(0).getData().add(successValue);
			datasets.get(1).getData().add(failValue);
		}
		// ===> 產生Chart.JS需要的資料格式

		resp.setLabels(labels);
		resp.setDatasets(datasets);

		return resp;
	}

	private String getSbNameByItemsSbNo(String item, String encodeStatus, String locale) {
		String aa1204_subitemName = "";
		TsmpDpItems dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale(item, encodeStatus, locale);
		if (dpItem == null) {
			return null;
		}
		aa1204_subitemName = dpItem.getSubitemName();

		return aa1204_subitemName;
	}

	protected String getTimeTypeByBcryptParamHelper(String encodeStatus, String locale) {
		String aa1204_status = null;
		try {
			aa1204_status = getBcryptParamHelper().decode(encodeStatus, "REPORT_TIME_TYPE", locale);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return aa1204_status;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return bcryptParamHelper;
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected TsmpReportDataDao getTsmpReportDataDao() {
		return tsmpReportDataDao;
	}
	
	protected Date getNow() {
		return DateTimeUtil.now();
	}
}
