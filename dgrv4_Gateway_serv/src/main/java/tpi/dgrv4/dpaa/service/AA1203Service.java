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
import tpi.dgrv4.dpaa.vo.AA1203DataSetResp;
import tpi.dgrv4.dpaa.vo.AA1203Req;
import tpi.dgrv4.dpaa.vo.AA1203Resp;
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
public class AA1203Service {

	private TPILogger logger = TPILogger.tl;

	private BcryptParamHelper bcryptParamHelper;
	private TsmpReportDataDao tsmpReportDataDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private TsmpApiDao tsmpApiDao;
	private TsmpOrganizationDao tsmpOrganizationDao;

	@Autowired
	public AA1203Service(BcryptParamHelper bcryptParamHelper, TsmpReportDataDao tsmpReportDataDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpApiDao tsmpApiDao,
			TsmpOrganizationDao tsmpOrganizationDao) {
		super();
		this.bcryptParamHelper = bcryptParamHelper;
		this.tsmpReportDataDao = tsmpReportDataDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
	}

	public AA1203Resp queryAPIAverageTime(TsmpAuthorization authorization, AA1203Req req, ReqHeader reqHeader) {
		AA1203Resp resp = null;
		try {
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());

			//挑選API為必填欄位
			if (req.getApiUidList()==null||req.getApiUidList().isEmpty()) {
				throw TsmpDpAaRtnCode._1350.throwing("{{apiUidList}}");
			}
			
			// <=== 決定時間類型
			String aa1203_timeType = getTimeTypeByBcryptParamHelper(req.getTimeType(), locale);
			ReportDateTimeRangeTypeEnum aa1203_reportDateTimeRangeTypeEnum = null;

			if ("MINUTE".equals(aa1203_timeType)) {
				aa1203_reportDateTimeRangeTypeEnum = ReportDateTimeRangeTypeEnum.MINUTE;
				if(!StringUtils.hasText(req.getStartHour())) {
					throw TsmpDpAaRtnCode._1350.throwing("{{startHour}}");
				}
				if(!StringUtils.hasText(req.getEndHour())) {
					throw TsmpDpAaRtnCode._1350.throwing("{{endHour}}");
				}
			}else if ("DAY".equals(aa1203_timeType)) {
				aa1203_reportDateTimeRangeTypeEnum = ReportDateTimeRangeTypeEnum.DAY;
			} else {
				aa1203_reportDateTimeRangeTypeEnum = ReportDateTimeRangeTypeEnum.MONTH;
			}
			// ===> 決定時間類型

			// <===查詢資料與轉換成Chart.Js格式
			
			// 取得組織原則
			List<String> orgList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(authorization.getOrgId(), Integer.MAX_VALUE); // 組織與子組織的orgId
						
			List<TsmpReportData> data = getTsmpReportDataDao().queryAPIAverageTime(getNow(),
					req.getStartDate(), req.getEndDate(), req.getStartHour(), req.getEndHour(), orgList, req.getApiUidList(),
					aa1203_reportDateTimeRangeTypeEnum);

			if (data == null || data.size() == 0) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			resp = convertToReportJson(data, reqHeader.getLocale());
			// ===>查詢資料與轉換成Chart.Js格式

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private String getSbNameByItemsSbNo(String item, String encodeStatus, String locale) {
		String subitemName = "";
		TsmpDpItems dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale(item, encodeStatus, locale);
		if (dpItem == null) {
			return null;
		}
		subitemName = dpItem.getSubitemName();

		return subitemName;
	}
	
	private AA1203Resp convertToReportJson(List<TsmpReportData> data, String locale) {

		// <=== 準備Chart.JS基本資料
		AA1203Resp resp = new AA1203Resp();

		resp.setReportName(nvl(getSbNameByItemsSbNo("REPORT_NAME", "API_AVERAGETIME", locale)));
		resp.setxLable(nvl(getSbNameByItemsSbNo("REPORT_LABLE_CODE", "MILLISECOND", locale)));

		List<String> labels = new ArrayList<String>();

		List<AA1203DataSetResp> datasets = new ArrayList<AA1203DataSetResp>();
		AA1203DataSetResp success = new AA1203DataSetResp();
		String successLabel = nvl(getSbNameByItemsSbNo("REPORT_LABLE_CODE", "Y", locale));

		List<Integer> successData = new ArrayList<Integer>();
		success.setLabel(successLabel);
		success.setData(successData);
		datasets.add(success);

		// ===> 準備Chart.JS基本資料

		// <=== 'API_NAME(MODULENAME)'資料分組
		Map<String, List<TsmpReportData>> aa1203_map = data.stream().collect(Collectors.groupingBy(r -> {

			TsmpApi aa1203_tsmpApi = getTsmpApiDao().findByModuleNameAndApiKey(r.getStringGroup1(), r.getStringGroup2());
			if (aa1203_tsmpApi == null && r != null) {
				return r.getStringGroup2() + "(" + r.getStringGroup1() + ")";
			}
			return aa1203_tsmpApi.getApiName() + "(" + aa1203_tsmpApi.getModuleName() + ")";
		}));
		// ===> 'API_NAME(MODULENAME)'資料分組

		// <=== 進行排序，以「成功」為排序條件由高往低
		List<Map.Entry<String, List<TsmpReportData>>> list = new ArrayList<Map.Entry<String, List<TsmpReportData>>>(
				aa1203_map.entrySet());

		list.sort(new Comparator<Map.Entry<String, List<TsmpReportData>>>() {
			@Override
			public int compare(Map.Entry<String, List<TsmpReportData>> o1, Map.Entry<String, List<TsmpReportData>> o2) {

				if (o1.getValue().size() == 1 && o2.getValue().size() == 1) {
					return o2.getValue().get(0).getIntValue1().compareTo(o1.getValue().get(0).getIntValue1());
				}

				return 0;

			}
		});
		// ===> 進行排序，以「成功」為排序條件由高往低

		// <=== 產生Chart.JS需要的資料格式
		for (Map.Entry<String, List<TsmpReportData>> entry : list) {

			labels.add(entry.getKey());

			int successValue = 0;
			if (entry.getValue().size() == 1) {
				successValue = entry.getValue().get(0).getIntValue1().intValue();
			}

			datasets.get(0).getData().add(successValue);
		}
		// ===> 產生Chart.JS需要的資料格式

		resp.setLabels(labels);
		resp.setDatasets(datasets);

		return resp;
	}


	protected String getTimeTypeByBcryptParamHelper(String encodeStatus, String locale) {
		String status = null;
		try {
			status = getBcryptParamHelper().decode(encodeStatus, "REPORT_TIME_TYPE", locale);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return status;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}

	protected TsmpReportDataDao getTsmpReportDataDao() {
		return tsmpReportDataDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return bcryptParamHelper;
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected Date getNow() {
		return DateTimeUtil.now();
	}

}
