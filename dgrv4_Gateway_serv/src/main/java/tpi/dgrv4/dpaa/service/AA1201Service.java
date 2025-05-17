package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.ReportDateTimeRangeTypeEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA1201DataSetResp;
import tpi.dgrv4.dpaa.vo.AA1201Req;
import tpi.dgrv4.dpaa.vo.AA1201Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.jpql.TsmpReportData;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpReportDataDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1201Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpReportDataDao tsmpReportDataDao;
	private TsmpApiDao tsmpApiDao;
	private BcryptParamHelper bcryptParamHelper;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;

	@Autowired
	public AA1201Service(TsmpReportDataDao tsmpReportDataDao, TsmpApiDao tsmpApiDao,
			BcryptParamHelper bcryptParamHelper, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.tsmpReportDataDao = tsmpReportDataDao;
		this.tsmpApiDao = tsmpApiDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	public AA1201Resp queryApiUsageStatistics(TsmpAuthorization authorization, AA1201Req req, ReqHeader reqHeader) {
		AA1201Resp resp = null;
		try {
			
			//挑選API為必填欄位
			if (req.getApiUidList()==null||req.getApiUidList().isEmpty()) {
				throw TsmpDpAaRtnCode._1350.throwing("{{apiUidList}}");
			}
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());

			// <=== 決定時間類型
			String aa1201_timeType = getTimeTypeByBcryptParamHelper(req.getTimeType(), locale);
			ReportDateTimeRangeTypeEnum aa1201_reportDateTimeRangeTypeEnum = null;

			if ("MINUTE".equals(aa1201_timeType)) {
				aa1201_reportDateTimeRangeTypeEnum = ReportDateTimeRangeTypeEnum.MINUTE;
				if(!StringUtils.hasText(req.getStartHour())) {
					throw TsmpDpAaRtnCode._1350.throwing("{{startHour}}");
				}
				if(!StringUtils.hasText(req.getEndHour())) {
					throw TsmpDpAaRtnCode._1350.throwing("{{endHour}}");
				}
			} else if ("DAY".equals(aa1201_timeType)) {
				aa1201_reportDateTimeRangeTypeEnum = ReportDateTimeRangeTypeEnum.DAY;
			} else {
				aa1201_reportDateTimeRangeTypeEnum = ReportDateTimeRangeTypeEnum.MONTH;
			}
			// ===> 決定時間類型

			// <===查詢資料與轉換成Chart.Js格式
			
			// 取得組織原則
			List<String> orgList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(authorization.getOrgId(), Integer.MAX_VALUE); // 組織與子組織的orgId
			
			List<TsmpReportData> data = getTsmpReportDataDao().queryByApiUsageStatistics(getNow(),
					req.getStartDate(), req.getEndDate(), req.getStartHour(), req.getEndHour(), orgList, req.getApiUidList(),
					aa1201_reportDateTimeRangeTypeEnum);
			
			if (data == null || data.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			resp = convertToReportJson(data, locale);
			// ===>查詢資料與轉換成Chart.Js格式

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private AA1201Resp convertToReportJson(List<TsmpReportData> data, String locale) {

		// <=== 準備Chart.JS基本資料
		AA1201Resp resp = new AA1201Resp();
		
		resp.setReportName(nvl(getSbNameByItemsSbNo("REPORT_NAME", "API_USAGE_STATISTICS", locale)));
		resp.setxLable(nvl(getSbNameByItemsSbNo("REPORT_LABLE_CODE", "FREQUENCY", locale)));

		List<String> labels = new ArrayList<String>();

		List<AA1201DataSetResp> datasets = new ArrayList<AA1201DataSetResp>();
		AA1201DataSetResp success = new AA1201DataSetResp();
		String successLabel = nvl(getSbNameByItemsSbNo("REPORT_LABLE_CODE", "Y", locale));

		List<Integer> successData = new ArrayList<Integer>();
		success.setLabel(successLabel);
		success.setData(successData);
		datasets.add(success);

		AA1201DataSetResp fail = new AA1201DataSetResp();
		String failLabel = nvl(getSbNameByItemsSbNo("REPORT_LABLE_CODE", "N", locale));
		List<Integer> failData = new ArrayList<Integer>();
		fail.setLabel(failLabel);
		fail.setData(failData);
		datasets.add(fail);
		// ===> 準備Chart.JS基本資料

		Map<String, List<TsmpReportData>> aa1201_map = data.stream().collect(Collectors.groupingBy(r -> {
			
			TsmpApi aa1201_tsmpApi = getTsmpApiDao().findByModuleNameAndApiKey(r.getStringGroup1(), r.getStringGroup2());
			if (aa1201_tsmpApi == null && r != null) {
				return r.getStringGroup2() + "(" + r.getStringGroup1() + ")";
			}
			return aa1201_tsmpApi.getApiName()+"("+aa1201_tsmpApi.getModuleName()+")";
		}));

		// <=== 進行排序，以「成功」為排序條件由高往低
		List<Map.Entry<String, List<TsmpReportData>>> list = new ArrayList<Map.Entry<String, List<TsmpReportData>>>(
				aa1201_map.entrySet());

		list.sort(new Comparator<Map.Entry<String, List<TsmpReportData>>>() {
			@Override
			public int compare(Map.Entry<String, List<TsmpReportData>> o1, Map.Entry<String, List<TsmpReportData>> o2) {

				Integer compare1 = 0;
				Integer compare2 = 0;

				for (TsmpReportData r : o1.getValue()) {
					if ("Y".equals(r.getStringGroup2())) {
						compare1 = r.getIntValue1().intValue();
					}
				}

				for (TsmpReportData r : o2.getValue()) {
					if ("Y".equals(r.getStringGroup2())) {
						compare1 = r.getIntValue1().intValue();
					}
				}

				return compare2.compareTo(compare1);

			}
		});
		// ===> 進行排序，以「成功」為排序條件由高往低

		// <=== 產生Chart.JS需要的資料格式
		for (Map.Entry<String, List<TsmpReportData>> entry : list) {
			
			labels.add(entry.getKey());
			
			int successValue = 0;
			int failValue = 0;
			for (TsmpReportData tsmpReportData : entry.getValue()) {
				if ("Y".equals(tsmpReportData.getStringGroup3())) {
					successValue = tsmpReportData.getIntValue1().intValue();
				}
				if ("N".equals(tsmpReportData.getStringGroup3())) {
					failValue = tsmpReportData.getIntValue1().intValue();
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
		String aa1201_subitemName = "";
		TsmpDpItems dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale(item, encodeStatus, locale);
		if (dpItem == null) {
			return null;
		}
		aa1201_subitemName = dpItem.getSubitemName();

		return aa1201_subitemName;
	}

	protected String getTimeTypeByBcryptParamHelper(String encodeStatus, String locale) {
		String aa1201_status = null;
		try {
			aa1201_status = getBcryptParamHelper().decode(encodeStatus, "REPORT_TIME_TYPE", locale);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return aa1201_status;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return bcryptParamHelper;
	}

	protected TsmpReportDataDao getTsmpReportDataDao() {
		return tsmpReportDataDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}
	
	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
	
	protected Date getNow() {
		return DateTimeUtil.now();
	}
}
