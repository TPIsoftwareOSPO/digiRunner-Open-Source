package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import tpi.dgrv4.common.constant.DashboardReportTypeEnum;
import tpi.dgrv4.common.constant.DashboardTimeTypeEnum;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.AA1211ApiTrafficDistributionResp;
import tpi.dgrv4.dpaa.vo.AA1211BadAttemptResp;
import tpi.dgrv4.dpaa.vo.AA1211ClientUsagePercentageResp;
import tpi.dgrv4.dpaa.vo.AA1211ClientUsagePercentageRespItem;
import tpi.dgrv4.dpaa.vo.AA1211FailResp;
import tpi.dgrv4.dpaa.vo.AA1211LastLoginLog;
import tpi.dgrv4.dpaa.vo.AA1211MedianResp;
import tpi.dgrv4.dpaa.vo.AA1211PopularResp;
import tpi.dgrv4.dpaa.vo.AA1211Req;
import tpi.dgrv4.dpaa.vo.AA1211Resp;
import tpi.dgrv4.dpaa.vo.AA1211RespItem;
import tpi.dgrv4.dpaa.vo.AA1211SuccessResp;
import tpi.dgrv4.dpaa.vo.AA1211UnpopularResp;
import tpi.dgrv4.entity.entity.DgrAuditLogM;
import tpi.dgrv4.entity.entity.jpql.DgrDashboardLastData;
import tpi.dgrv4.entity.repository.DgrAuditLogMDao;
import tpi.dgrv4.entity.repository.DgrDashboardLastDataDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1211Service {
	private TPILogger logger = TPILogger.tl;
	
	private DgrDashboardLastDataDao dgrDashboardLastDataDao;
	private DgrAuditLogMDao dgrAuditLogMDao;
	
	private final static int GAP = 20; // 中位數的刻度，目前暫定寫死20

	@Autowired
	public AA1211Service(DgrDashboardLastDataDao dgrDashboardLastDataDao, DgrAuditLogMDao dgrAuditLogMDao) {
		super();
		this.dgrDashboardLastDataDao = dgrDashboardLastDataDao;
		this.dgrAuditLogMDao = dgrAuditLogMDao;
	}

	public AA1211Resp queryDashboardData(TsmpAuthorization authorization, AA1211Req req, ReqHeader reqHeader) {
		AA1211Resp resp = new AA1211Resp();
		int timeType = req.getTimeType();
		try {
			if (timeType == 0) {
				throw TsmpDpAaRtnCode._1350.throwing("{{timeType}}");
			}
			if (!DashboardTimeTypeEnum.checkInput(timeType)) {
				// 參數錯誤
				throw TsmpDpAaRtnCode._1290.throwing();
			}

			// 1:分, 2:天, 3:月, 4:年
			List<DgrDashboardLastData> list = getDgrDashboardLastDataDao().findByTimeType(timeType);

			if (CollectionUtils.isEmpty(list)) {
				// 查無資料
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			resp.setData(dataClassification(timeType, list, authorization.getUserName()));

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			// 執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private AA1211RespItem dataClassification(int timeType, List<DgrDashboardLastData> data, String userName) {
		AA1211RespItem item = new AA1211RespItem();
		List<AA1211ClientUsagePercentageResp> clirntResps = new ArrayList<>();
		Map<String, List<DgrDashboardLastData>> map = new HashMap<>();
		Map<String, List<AA1211ClientUsagePercentageRespItem>> respItemMap = new HashMap<>();
		List<AA1211UnpopularResp> aA1211UnpopularResps = new ArrayList<>();
		List<AA1211ApiTrafficDistributionResp> aA1211ApiTrafficDistributionResps = new ArrayList<>();
		List<AA1211PopularResp> aA1211PopularResps = new ArrayList<>();
		item.setTimeType(timeType);
		for (DgrDashboardLastData dgrDashboardLastData : data) {
			int dashboardType = dgrDashboardLastData.getDashboardType();
			// 1 資料時間
			if (dashboardType == DashboardReportTypeEnum.DATATIME.value()) {
				String datatime = dgrDashboardLastData.getStr1() + " ~ " + dgrDashboardLastData.getStr2();
				item.setDataTime(datatime);

				// 2 Request
			} else if (dashboardType == DashboardReportTypeEnum.REQUEST.value()) {
				String request = dgrDashboardLastData.getStr1();
				item.setRequest(request);

				// 3 Success
			} else if (dashboardType == DashboardReportTypeEnum.SUCCESS.value()) {
				AA1211SuccessResp resp = successResp(dgrDashboardLastData);
				item.setSuccess(resp);

				// 4 Fail
			} else if (dashboardType == DashboardReportTypeEnum.FAIL.value()) {
				AA1211FailResp resp = failResp(dgrDashboardLastData);
				item.setFail(resp);

				// 5 Bad Attempt
			} else if (dashboardType == DashboardReportTypeEnum.BAD_ATTEMPT.value()) {
				AA1211BadAttemptResp resp = badAttemptResp(dgrDashboardLastData);
				item.setBadAttempt(resp);

				// 6 平均回應時間
			} else if (dashboardType == DashboardReportTypeEnum.AVG.value()) {
				item.setAvg(dgrDashboardLastData.getNum1().intValue());

				// 7 中位數
			} else if (dashboardType == DashboardReportTypeEnum.MEDIAN.value()) {
				AA1211MedianResp resp = medianResp(dgrDashboardLastData);
				item.setMedian(resp);

				// 8 TOP5熱門
			} else if (dashboardType == DashboardReportTypeEnum.POPULAR.value()) {
				aA1211PopularResps.add(popularResp(dgrDashboardLastData));

				// 9 TOP5冷門
			} else if (dashboardType == DashboardReportTypeEnum.UNPOPULAR.value()) {
				aA1211UnpopularResps.add(unpopularResp(dgrDashboardLastData));

				// 10 API流量分佈
			} else if (dashboardType == DashboardReportTypeEnum.API_TRAFFIC_DISTRIBUTION.value()) {
				aA1211ApiTrafficDistributionResps.add(apiTrafficDistributionResp(dgrDashboardLastData));

				// 11 client使用佔比
			} else if (dashboardType == DashboardReportTypeEnum.CLIENT_USAGE_PERCENTAGE.value()) {
				clirntResps.add(clientUsagePercentageResp(dgrDashboardLastData));

				// 12 client使用次數與平均時間
			} else if (dashboardType == DashboardReportTypeEnum.CLIENT_USAGE_METRICS.value()) {

				if (map.get(dgrDashboardLastData.getStr1()) == null) {
					List<DgrDashboardLastData> list = new ArrayList<>();
					list.add(dgrDashboardLastData);
					map.put(dgrDashboardLastData.getStr1(), list);
				} else {
					map.get(dgrDashboardLastData.getStr1()).add(dgrDashboardLastData);
				}
			}
		}
		// 8 按照Rank 排列
		Collections.sort(aA1211PopularResps, Comparator.comparingInt(AA1211PopularResp::getRank));
		item.setPopular(aA1211PopularResps);
		// 9 按照Rank 排列
		Collections.sort(aA1211UnpopularResps, Comparator.comparingInt(AA1211UnpopularResp::getRank));
		item.setUnpopular(aA1211UnpopularResps);
		// 10 按照Sort 排列
		Collections.sort(aA1211ApiTrafficDistributionResps,
				Comparator.comparingInt(AA1211ApiTrafficDistributionResp::getSort));
		item.setApiTrafficDistribution(aA1211ApiTrafficDistributionResps);

		// 把12放進11
		for (Entry<String, List<DgrDashboardLastData>> entry : map.entrySet()) {
			List<DgrDashboardLastData> list = entry.getValue();
			List<AA1211ClientUsagePercentageRespItem> respItems = new ArrayList<>();
			for (DgrDashboardLastData dashboardLastData : list) {
				respItems.add(clientUsageMetricsResp(dashboardLastData));
			}
			respItemMap.put(entry.getKey(), respItems);
		}
		for (AA1211ClientUsagePercentageResp aa1211ClientUsagePercentageResp : clirntResps) {
			String client = aa1211ClientUsagePercentageResp.getClient();
			aa1211ClientUsagePercentageResp.setApiUsage(respItemMap.get(client));
		}
		clirntResps.forEach(l -> {
			if (!CollectionUtils.isEmpty(l.getApiUsage())) {
				// 按照請求總數 排列
				Collections.sort(l.getApiUsage(),
						Comparator.comparingLong((AA1211ClientUsagePercentageRespItem i) -> Long.valueOf(i.getTotal())).reversed());
			}
		});

		item.setClientUsagePercentage(clirntResps);
		
		List<DgrAuditLogM> auditLogMList = getDgrAuditLogMDao().findTop3ByUserNameAndEventNoOrderByCreateDateTimeDesc(userName, "login");
		List<AA1211LastLoginLog> lastLoginLog = auditLogMList.stream().map(alm -> {
			AA1211LastLoginLog log = new AA1211LastLoginLog();
			log.setLoginDate(DateTimeUtil.dateTimeToString(alm.getCreateDateTime(), DateTimeFormatEnum.西元年月日時分秒_2).get());
			log.setLoginIp(alm.getUserIp());
			log.setLoginStatus(alm.getParam1());
			return log;
		}).collect(Collectors.toList());		
		
		item.setLastLoginLog(lastLoginLog);

		return item;
	}

	private AA1211SuccessResp successResp(DgrDashboardLastData dgrDashboardLastData) {
		AA1211SuccessResp resp = new AA1211SuccessResp();
		resp.setPercentage(dgrDashboardLastData.getStr1() + "%");

		resp.setSuccess(String.valueOf(dgrDashboardLastData.getNum1()));
		resp.setTotal(String.valueOf(dgrDashboardLastData.getNum2()));
		return resp;

	}

	private AA1211FailResp failResp(DgrDashboardLastData dgrDashboardLastData) {
		AA1211FailResp resp = new AA1211FailResp();
		resp.setFail(String.valueOf(dgrDashboardLastData.getNum1()));
		resp.setPercentage(dgrDashboardLastData.getStr1() + "%");
		resp.setTotal(String.valueOf(dgrDashboardLastData.getNum2()));
		return resp;
	}

	private AA1211BadAttemptResp badAttemptResp(DgrDashboardLastData dgrDashboardLastData) {
		AA1211BadAttemptResp resp = new AA1211BadAttemptResp();
		resp.setTotal(String.valueOf(dgrDashboardLastData.getNum1()));
		resp.setCode_401(String.valueOf(dgrDashboardLastData.getNum2()));
		resp.setCode_403(String.valueOf(dgrDashboardLastData.getNum3()));
		resp.setOthers(String.valueOf(dgrDashboardLastData.getNum4()));

		return resp;
	}

	private AA1211MedianResp medianResp(DgrDashboardLastData dgrDashboardLastData) {
		AA1211MedianResp resp = new AA1211MedianResp();
		resp.setMin(dgrDashboardLastData.getNum1().intValue());
		resp.setMax(dgrDashboardLastData.getNum2().intValue());
		resp.setMedian(dgrDashboardLastData.getNum3().intValue());
		resp.setGap(GAP);

		return resp;
	}

	private AA1211PopularResp popularResp(DgrDashboardLastData dgrDashboardLastData) {
		AA1211PopularResp resp = new AA1211PopularResp();
		resp.setApiName(dgrDashboardLastData.getStr1());
		resp.setTotal(String.valueOf(dgrDashboardLastData.getNum1()));
		resp.setSuccess(String.valueOf(dgrDashboardLastData.getNum2()));
		resp.setFail(String.valueOf(dgrDashboardLastData.getNum3()));
		resp.setAvg(dgrDashboardLastData.getNum4().intValue());
		resp.setRank(dgrDashboardLastData.getSortNum());
		return resp;
	}

	private AA1211UnpopularResp unpopularResp(DgrDashboardLastData dgrDashboardLastData) {
		AA1211UnpopularResp resp = new AA1211UnpopularResp();
		resp.setApiName(dgrDashboardLastData.getStr1());
		resp.setFloating(dgrDashboardLastData.getStr2());
		resp.setTotal(String.valueOf(dgrDashboardLastData.getNum1()));
		resp.setRank(dgrDashboardLastData.getSortNum());
		return resp;

	}

	private AA1211ApiTrafficDistributionResp apiTrafficDistributionResp(DgrDashboardLastData dgrDashboardLastData) {
		AA1211ApiTrafficDistributionResp resp = new AA1211ApiTrafficDistributionResp();
		resp.setxLable(dgrDashboardLastData.getStr1());
		resp.setSuccess(String.valueOf(dgrDashboardLastData.getNum1()));
		resp.setFail(String.valueOf(dgrDashboardLastData.getNum2()));
		resp.setSort(dgrDashboardLastData.getSortNum());
		return resp;
	}

	private AA1211ClientUsagePercentageResp clientUsagePercentageResp(DgrDashboardLastData dgrDashboardLastData) {
		AA1211ClientUsagePercentageResp resp = new AA1211ClientUsagePercentageResp();
		resp.setClient(dgrDashboardLastData.getStr1());
		resp.setPercentage(dgrDashboardLastData.getStr2());
		resp.setRequest(String.valueOf(dgrDashboardLastData.getNum1()));
		resp.setTotal(String.valueOf(dgrDashboardLastData.getNum2()));

		return resp;
	}

	private AA1211ClientUsagePercentageRespItem clientUsageMetricsResp(DgrDashboardLastData dgrDashboardLastData) {
		AA1211ClientUsagePercentageRespItem resp = new AA1211ClientUsagePercentageRespItem();
		resp.setApiName(dgrDashboardLastData.getStr2());
		resp.setTotal(String.valueOf(dgrDashboardLastData.getNum1()));
		resp.setSuccess(String.valueOf(dgrDashboardLastData.getNum2()));
		resp.setFail(String.valueOf(dgrDashboardLastData.getNum3()));
		resp.setAvg(dgrDashboardLastData.getNum4().intValue());
		return resp;
	}


	protected DgrDashboardLastDataDao getDgrDashboardLastDataDao() {
		return dgrDashboardLastDataDao;
	}
	
	protected DgrAuditLogMDao getDgrAuditLogMDao() {
		return dgrAuditLogMDao;
	}
}
