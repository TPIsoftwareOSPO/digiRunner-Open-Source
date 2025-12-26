package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
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
import tpi.dgrv4.dpaa.vo.AA1202DataSetResp;
import tpi.dgrv4.dpaa.vo.AA1202Req;
import tpi.dgrv4.dpaa.vo.AA1202Resp;
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
public class AA1202Service {

	private TPILogger logger = TPILogger.tl;

	private BcryptParamHelper bcryptParamHelper;
	private TsmpApiDao tsmpApiDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private TsmpReportDataDao tsmpReportDataDao;
	private TsmpOrganizationDao tsmpOrganizationDao;

	@Autowired
	public AA1202Service(BcryptParamHelper bcryptParamHelper, TsmpApiDao tsmpApiDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpReportDataDao tsmpReportDataDao,
			TsmpOrganizationDao tsmpOrganizationDao) {
		super();
		this.bcryptParamHelper = bcryptParamHelper;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.tsmpReportDataDao = tsmpReportDataDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
	}

	public AA1202Resp queryAPITimesAndTime(TsmpAuthorization authorization, AA1202Req req, ReqHeader reqHeader) {
		AA1202Resp resp = null;
		try {
			String locale = ServiceUtil.getLocale(reqHeader.getLocale());

			//挑選API為必填欄位
			if (req.getApiUidList()==null||req.getApiUidList().isEmpty()) {
				throw TsmpDpAaRtnCode._1350.throwing("{{apiUidList}}");
			}

			// <=== 決定時間類型
			String aa1202_timeType = getTimeTypeByBcryptParamHelper(req.getTimeType(), locale);
			ReportDateTimeRangeTypeEnum aa1202_reportDateTimeRangeTypeEnum = null;

			if ("MINUTE".equals(aa1202_timeType)) {
				aa1202_reportDateTimeRangeTypeEnum = ReportDateTimeRangeTypeEnum.MINUTE;
				if(!StringUtils.hasText(req.getStartHour())) {
					throw TsmpDpAaRtnCode._1350.throwing("{{startHour}}");
				}
				if(!StringUtils.hasText(req.getEndHour())) {
					throw TsmpDpAaRtnCode._1350.throwing("{{endHour}}");
				}
			}else if ("DAY".equals(aa1202_timeType)) {
				aa1202_reportDateTimeRangeTypeEnum = ReportDateTimeRangeTypeEnum.DAY;
			} else {
				aa1202_reportDateTimeRangeTypeEnum = ReportDateTimeRangeTypeEnum.MONTH;
			}
			// ===> 決定時間類型

			// <===查詢資料與轉換成Chart.Js格式
			
			// 取得組織原則
			List<String> orgList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(authorization.getOrgId(), Integer.MAX_VALUE); // 組織與子組織的orgId
						
			List<TsmpReportData> data = getTsmpReportDataDao().queryAPITimesAndTime(getNow(),
					req.getStartDate(), req.getEndDate(), req.getStartHour(), req.getEndHour(), orgList, req.getApiUidList(),
					aa1202_reportDateTimeRangeTypeEnum);

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

	private AA1202Resp convertToReportJson(List<TsmpReportData> data, String locale) {

		// <=== 準備Chart.JS基本資料
		AA1202Resp resp = new AA1202Resp();

		resp.setReportName(nvl(getSbNameByItemsSbNo("REPORT_NAME", "API_TIMESANDTIME", locale)));
		resp.setxLable(nvl(getSbNameByItemsSbNo("REPORT_LABLE_CODE", "MILLISECOND", locale)));
		resp.setyLable(nvl(getSbNameByItemsSbNo("REPORT_LABLE_CODE", "FREQUENCY", locale)));

		List<String> labels = new ArrayList<String>();

		List<AA1202DataSetResp> datasets = new ArrayList<AA1202DataSetResp>();
		// ===> 準備Chart.JS基本資料

		// <=== '0~999', '1000~1999', '2000~2999'毫秒資料分組
		Map<String, List<TsmpReportData>> millisecondMap = data.stream().collect(Collectors.groupingBy(r -> {
			return r.getStringGroup1();
		}));
		// ===> '0~999', '1000~1999', '2000~2999'毫秒資料分組

		// <=== 進行排序，排序'0~999', '1000~1999', '2000~2999'毫秒由高往低
		List<Map.Entry<String, List<TsmpReportData>>> millisecondMapLlist = new ArrayList<Map.Entry<String, List<TsmpReportData>>>(
				millisecondMap.entrySet());

		millisecondMapLlist.sort(new Comparator<Map.Entry<String, List<TsmpReportData>>>() {
			@Override
			public int compare(Map.Entry<String, List<TsmpReportData>> o1, Map.Entry<String, List<TsmpReportData>> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		// ===> 進行排序，排序'0~999', '1000~1999', '2000~2999'毫秒由高往低

		// <=== 產生Chart.JS需要的資料格式
		HashMap<String, AA1202DataSetResp> dataSetMap = new HashMap<String, AA1202DataSetResp>();

		// <===依照「毫秒」的數量，先製作出datasets與datasets.data
		for (Map.Entry<String, List<TsmpReportData>> entry : millisecondMapLlist) {
			labels.add(entry.getKey());

			List<TsmpReportData> reportData = entry.getValue();

			for (TsmpReportData r : reportData) {
				TsmpApi tsmpApi = getTsmpApiDao().findByModuleNameAndApiKey(r.getStringGroup2(), r.getStringGroup3());
				String label = "";
				if (tsmpApi == null && r != null) {
					label = r.getStringGroup2() + "(" + r.getStringGroup1() + ")";
				}else {
					label = tsmpApi.getApiName() + "(" + tsmpApi.getModuleName() + ")";					
				}
				AA1202DataSetResp dataSet = new AA1202DataSetResp();
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
		// ===>依照「毫秒」的數量，先製作出datasets與datasets.data

		int index = 0;

		for (Map.Entry<String, List<TsmpReportData>> entry : millisecondMapLlist) {

			List<TsmpReportData> reportData = entry.getValue();

			for (TsmpReportData r : reportData) {

				TsmpApi tsmpApi = getTsmpApiDao().findByModuleNameAndApiKey(r.getStringGroup2(), r.getStringGroup3());
				String label = "";
				if (tsmpApi != null) {
					label = tsmpApi.getApiName() + "(" + tsmpApi.getModuleName() + ")";
					if (dataSetMap.containsKey(label)) {
						dataSetMap.get(label).getData().set(index, r.getIntValue1().intValue());
					}
				}
			}

			index++;
		}

		// ===> 產生Chart.JS需要的資料格式

		ArrayList<AA1202DataSetResp> datasetsList = new ArrayList<AA1202DataSetResp>(dataSetMap.values());



		resp.setLabels(labels);
		resp.setDatasets(datasetsList);

		return resp;
	}

	private String getSbNameByItemsSbNo(String item, String encodeStatus, String locale) {
		String aa1202_subitemName = "";
		TsmpDpItems dpItem = getTsmpDpItemsCacheProxy().findByItemNoAndSubitemNoAndLocale(item, encodeStatus, locale);
		if (dpItem == null) {
			return null;
		}
		aa1202_subitemName = dpItem.getSubitemName();

		return aa1202_subitemName;
	}

	protected String getTimeTypeByBcryptParamHelper(String encodeStatus, String locale) {
		String aa1202_status = null;
		try {
			aa1202_status = getBcryptParamHelper().decode(encodeStatus, "REPORT_TIME_TYPE", locale);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return aa1202_status;
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
	
	protected Date getNow() {
		return DateTimeUtil.now();
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return bcryptParamHelper;
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	
}
