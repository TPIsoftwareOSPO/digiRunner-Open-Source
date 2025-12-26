package tpi.dgrv4.escape;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.ReportDateTimeRangeTypeEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA1205DataSetResp;
import tpi.dgrv4.dpaa.vo.AA1205Req;
import tpi.dgrv4.dpaa.vo.AA1205Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.jpql.TsmpReportData;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpReportDataDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
	
@Service
public class AA1205Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpApiDao tsmpApiDao;
	private BcryptParamHelper bcryptParamHelper;
	private TsmpReportDataDao tsmpReportDataDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;

	@Autowired
	public AA1205Service(TsmpApiDao tsmpApiDao, BcryptParamHelper bcryptParamHelper,
			TsmpReportDataDao tsmpReportDataDao, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.tsmpApiDao = tsmpApiDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.tsmpReportDataDao = tsmpReportDataDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	public AA1205Resp queryBadattemptConnection(TsmpAuthorization authorization, AA1205Req req, ReqHeader reqHeader) {
		AA1205Resp resp = null;
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
			
			List<TsmpReportData> data = getTsmpReportDataDao().queryBadattemptConnection(getNow(),
					req.getStartDate(), req.getEndDate(), req.getStartHour(), req.getEndHour(), orgList, reportDateTimeRangeTypeEnum);

			if (data == null || data.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			resp = convertToReportJson(req.getStartDate(),req.getEndDate(), req.getStartHour(), req.getEndHour(),reportDateTimeRangeTypeEnum,data, locale);
			// ===>查詢資料與轉換成Chart.Js格式

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private AA1205Resp convertToReportJson(String startDate,String endDate, String startHour, String endHour, ReportDateTimeRangeTypeEnum dateTimeRangeType,List<TsmpReportData> data, String locale) {

		// <=== 準備Chart.JS基本資料
		AA1205Resp resp = new AA1205Resp();

		resp.setReportName(nvl(getSbNameByItemsSbNo("REPORT_NAME", "BADATTEMPT", locale)));
		resp.setxLable(nvl(getSbNameByItemsSbNo("REPORT_LABLE_CODE", "TIME", locale)));
		resp.setyLable(nvl(getSbNameByItemsSbNo("REPORT_LABLE_CODE", "FREQUENCY", locale)));

		List<String> labels = new ArrayList<String>();

		// ===> 準備Chart.JS基本資料

		// <=== 時間資料分組
		Map<String, List<TsmpReportData>> timeMap = data.stream().collect(Collectors.groupingBy(aa1205_r -> {
			if (dateTimeRangeType == ReportDateTimeRangeTypeEnum.MINUTE) {
				if(aa1205_r.getLastRowDateTime() != null) {
					return DateTimeUtil.dateTimeToString(aa1205_r.getLastRowDateTime(), DateTimeFormatEnum.時分).orElse(null).substring(0,4) + "X";
				}else {
					return null;
				}
			}else if (dateTimeRangeType == ReportDateTimeRangeTypeEnum.DAY) {
				return DateTimeUtil.dateTimeToString(aa1205_r.getLastRowDateTime(), DateTimeFormatEnum.西元年月日_2).orElse(null);	
			}else {
				return DateTimeUtil.dateTimeToString(aa1205_r.getLastRowDateTime(), DateTimeFormatEnum.西元年月_2).orElse(null);
			}
		}));
		// ===> 時間資料分組
		
		
		// <=== 補齊缺少的日期
		HashSet<String> totalDates = new HashSet<String>();
		try {

			SimpleDateFormat aa1205_sdf = null;
			if (dateTimeRangeType == ReportDateTimeRangeTypeEnum.MINUTE) {
				aa1205_sdf = new SimpleDateFormat(DateTimeFormatEnum.時分.value());
			}else if (dateTimeRangeType == ReportDateTimeRangeTypeEnum.DAY) {
				aa1205_sdf = new SimpleDateFormat(DateTimeFormatEnum.西元年月日_2.value());
			} else {
				aa1205_sdf = new SimpleDateFormat(DateTimeFormatEnum.西元年月.value());
			}
			if (dateTimeRangeType == ReportDateTimeRangeTypeEnum.MINUTE) {
				Date start = aa1205_sdf.parse(startHour + ":00");
				Date end = aa1205_sdf.parse(endHour + ":59");
				//checkmarx, Unchecked Input for Loop Condition,所以多了maxValue和loopIndex
				final int safeMaxValue = 145;
				int loopIndex = 0;
			    while (!start.after(end) && loopIndex <= safeMaxValue) {
			    	loopIndex++;
			    	
					totalDates.add(aa1205_sdf.format(start).substring(0,4)+"X");
					Calendar aa1205_c = Calendar.getInstance();
					aa1205_c.setTime(start);
					aa1205_c.add(Calendar.MINUTE, 10);
					start = aa1205_c.getTime();
				}
			}else {
				Date start = aa1205_sdf.parse(startDate);
				Date end = aa1205_sdf.parse(endDate);
				//checkmarx, Unchecked Input for Loop Condition,所以多了maxValue和loopIndex
				int maxValue = 367;
				int loopIndex = 0;
			    while (!start.after(end) && loopIndex <= maxValue) {
			    	if(loopIndex == maxValue) {
			    		throw TsmpDpAaRtnCode._1559.throwing("Exceed " + maxValue + " day");
			    	}
			    	
					totalDates.add(aa1205_sdf.format(start));
					Calendar aa1205_c = Calendar.getInstance();
					aa1205_c.setTime(start);
					aa1205_c.add(Calendar.DATE, 1);
					start = aa1205_c.getTime();
				}
			}
		} catch (ParseException e1) {
			this.logger.debug(StackTraceUtil.logStackTrace(e1));
		}
		for (String day : totalDates) {
			if (timeMap.containsKey(day) == false) {
				timeMap.put(day, new ArrayList<TsmpReportData>());
			}
		}
		// ===> 補齊缺少的日期
				

		// <=== 進行排序，以時間(LAST_ROW_DATE_TIME)為排序條件由最舊時間往最新時間
		List<Map.Entry<String, List<TsmpReportData>>> timeList = new ArrayList<Map.Entry<String, List<TsmpReportData>>>(
				timeMap.entrySet());

		timeList.sort(new Comparator<Map.Entry<String, List<TsmpReportData>>>() {
			@Override
			public int compare(Map.Entry<String, List<TsmpReportData>> o1, Map.Entry<String, List<TsmpReportData>> o2) {

				return o1.getKey().compareTo(o2.getKey());

			}
		});
		// ===> 進行排序，以時間(LAST_ROW_DATE_TIME)為排序條件由最舊時間往最新時間

		// <=== 產生Chart.JS需要的資料格式
		HashMap<String, AA1205DataSetResp> dataSetMap = new HashMap<String, AA1205DataSetResp>();

		// <===依照「時間」的數量，先製作出datasets與datasets.data
		for (Map.Entry<String, List<TsmpReportData>> entry : timeList) {

			labels.add(entry.getKey());

			List<TsmpReportData> reportData = entry.getValue();

			for (TsmpReportData r : reportData) {

				String label = r.getStringGroup1();

				AA1205DataSetResp dataSet = new AA1205DataSetResp();
				dataSet.setLabel(label);
				dataSetMap.put(label, dataSet);
			}
		}
		List<Integer> zeroIntData = new ArrayList<Integer>();
		for (int i = 0; i < labels.size(); i++) {
			zeroIntData.add(0);
		}
		for (String label : dataSetMap.keySet()) {
			dataSetMap.get(label).setData(new ArrayList<Integer>(zeroIntData));
		}
		// ===>依照「時間」的數量，先製作出datasets與datasets.data

		int index = 0;

		for (Map.Entry<String, List<TsmpReportData>> entry : timeList) {

			List<TsmpReportData> reportData = entry.getValue();

			for (TsmpReportData r : reportData) {

				String label = r.getStringGroup1();

				if (dataSetMap.containsKey(label)) {
					int value = dataSetMap.get(label).getData().get(index);
					dataSetMap.get(label).getData().set(index, value + r.getIntValue1().intValue());
				}
			}

			index++;
		}

		ArrayList<AA1205DataSetResp> datasetsList = new ArrayList<AA1205DataSetResp>(dataSetMap.values());

		// ===> 產生Chart.JS需要的資料格式

		resp.setLabels(labels);
		resp.setDatasets(datasetsList);

		return resp;
	}

	private String getSbNameByItemsSbNo(String item, String encodeStatus, String locale) {
		String aa1205_subitemName = "";
		TsmpDpItems dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale(item, encodeStatus, locale);
		if (dpItem == null) {
			return null;
		}
		aa1205_subitemName = dpItem.getSubitemName();

		return aa1205_subitemName;
	}

	protected String getTimeTypeByBcryptParamHelper(String encodeStatus, String locale) {
		String aa1205_status = null;
		try {
			aa1205_status = getBcryptParamHelper().decode(encodeStatus, "REPORT_TIME_TYPE", locale);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return aa1205_status;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return bcryptParamHelper;
	}
	
	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}

	protected TsmpReportDataDao getTsmpReportDataDao() {
		return tsmpReportDataDao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
	
	protected Date getNow() {
		return DateTimeUtil.now();
	}

}
