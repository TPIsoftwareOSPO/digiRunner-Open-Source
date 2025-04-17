package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.entity.jpql.TsmpReqLog;
import tpi.dgrv4.entity.entity.jpql.TsmpReqResLogHistory;
import tpi.dgrv4.entity.entity.jpql.TsmpResLog;
import tpi.dgrv4.entity.repository.TsmpReqLogDao;
import tpi.dgrv4.entity.repository.TsmpReqResLogHistoryDao;
import tpi.dgrv4.entity.repository.TsmpResLogDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class HandleDashboardLogDataService {
	private TPILogger logger = TPILogger.tl;
	
	private TsmpReqLogDao tsmpReqLogDao;
	private TsmpResLogDao tsmpResLogDao;
	private TsmpReqResLogHistoryDao tsmpReqResLogHistoryDao;

	@Autowired
	public HandleDashboardLogDataService(TsmpReqLogDao tsmpReqLogDao, TsmpResLogDao tsmpResLogDao,
			TsmpReqResLogHistoryDao tsmpReqResLogHistoryDao) {
		super();
		this.tsmpReqLogDao = tsmpReqLogDao;
		this.tsmpResLogDao = tsmpResLogDao;
		this.tsmpReqResLogHistoryDao = tsmpReqResLogHistoryDao;
	}

	@Transactional
	public Map<String, Object> exec(Date execDate, String createUser) {
		logger = TPILogger.tl;

		this.logger.info("--- Begin HandleDashboardLogDataService ---");

		// 現在區間的日期
		Date nowIntervalDate = this.getNowIntervalDate(execDate);
		this.logger.info("nowIntervalDate = "
				+ DateTimeUtil.dateTimeToString(nowIntervalDate, DateTimeFormatEnum.西元年月日時分秒).orElseThrow(TsmpDpAaRtnCode._1295::throwing));

		// 先找出要刪除的 一年前的紀錄
		Date oneYearAgo = this.getOneYearAgo(execDate);
		this.logger
				.info("oneYearAgo = " + DateTimeUtil.dateTimeToString(oneYearAgo, DateTimeFormatEnum.西元年月日時分秒).orElseThrow(TsmpDpAaRtnCode._1295::throwing));

		List<TsmpReqResLogHistory> deleteList = getTsmpReqResLogHistoryDao().findByRtimeLessThan(oneYearAgo);
		this.logger.info("TsmpReqResLogHistory will delete count: " + deleteList.size());
		if (deleteList.size() > 0) {
			//該行太多筆資料會報java.lang.StackOverflowError
			//Too many pieces of information in this line will report java.lang.StackOverflowError
//			getTsmpReqResLogHistoryDao().deleteAllInBatch(deleteList); // 這裡刪除太久了
			deleteList.forEach(dvo -> {
				getTsmpReqResLogHistoryDao().delete(dvo);
			});
			this.logger.info("TsmpReqResLogHistory 已刪除 " + deleteList.size() + " 筆超過一年的紀錄");
		}
		List<TsmpReqLog> reqList = getTsmpReqLogDao().findByRtimeLessThanOrderByRtimeAsc(nowIntervalDate);
		this.logger.info("TsmpReqLog count: " + reqList.size());
		List<TsmpReqLog> deleteReqList = new ArrayList<>();
		List<String> resIdList = new ArrayList<>();
		List<String> reqIdList = new ArrayList<>();

		List<TsmpResLog> resList = getTsmpResLogDao().findAll();
		this.logger.info("TsmpResLog count: " + resList.size());
		List<TsmpResLog> deleteResList = new ArrayList<>();
		// 假設ID req 1 2 3 4 res 2 3 4 5
		resList.forEach(r -> {
			resIdList.add(r.getId()); // 2 3 4 5
		});

		reqList.forEach(r -> {
			if (!resIdList.contains(r.getId()))

				deleteReqList.add(r); // 1

		});

		reqList.removeAll(deleteReqList);

		reqList.forEach(r -> {
			reqIdList.add(r.getId()); // 2 3 4
		});

		resList.forEach(r -> { // 2 3 4 5
			if (!reqIdList.contains(r.getId()))
				deleteResList.add(r); // 5

		});
		resList.removeAll(deleteResList); // 2 3 4

		Map<String, TsmpResLog> resMap = new HashMap<>();
		resList.forEach(x -> resMap.put(x.getId(), x));

		// 寫入資料
		List<TsmpReqResLogHistory> insertList = new ArrayList();
		reqList.forEach(vo -> {
			if (resMap.containsKey(vo.getId())) {
				TsmpResLog resVo = resMap.get(vo.getId());
				TsmpReqResLogHistory trrlh = new TsmpReqResLogHistory();
				trrlh.setId(vo.getId());
				trrlh.setAtype(vo.getAtype());
				trrlh.setCid(vo.getCid());
				trrlh.setCip(vo.getCip());
				trrlh.setElapse(resVo.getElapse());
				trrlh.setEntry(vo.getEntry());
				trrlh.setErrMsg(resVo.getErrMsg());
				trrlh.setExeStatus(resVo.getExeStatus());
				trrlh.setHttpStatus(resVo.getHttpStatus());
				trrlh.setJti(vo.getJti());
				trrlh.setModuleName(vo.getModuleName());
				trrlh.setModuleVersion(vo.getModuleVersion());
				trrlh.setNodeAlias(vo.getNodeAlias());
				trrlh.setNodeId(vo.getNodeId());
				trrlh.setOrgid(vo.getOrgid());
				trrlh.setRcode(resVo.getRcode());
				trrlh.setRtime(vo.getRtime());
				trrlh.setTuser(vo.getTuser());
				trrlh.setTxid(vo.getTxid());
				trrlh.setUrl(vo.getUrl());
				String yearMonth = DateTimeUtil.dateTimeToString(vo.getRtime(), DateTimeFormatEnum.西元年月_2).get();
				trrlh.setRtimeYearMonth(yearMonth);
				insertList.add(trrlh);
			}

		});
		List<TsmpReqLog> removeFromDeleteReqList = new ArrayList<>();
		for (TsmpReqLog d : deleteReqList) {
			long oneHourInMillis = 60 * 60 * 1000L;
			long timeDifference = nowIntervalDate.getTime() - d.getRtime().getTime();
			if (timeDifference >= 0 && timeDifference <= oneHourInMillis) { // 如果小於一小時內，要把它移出刪除的list
				removeFromDeleteReqList.add(d);
			}
		}
		deleteReqList.removeAll(removeFromDeleteReqList);

		this.logger.info("TsmpReqResLogHistory prepare to write " + insertList.size() + " records");
		getTsmpReqResLogHistoryDao().saveAll(insertList);  //-- 寫入太久
		this.logger.info("TsmpReqResLogHistory 已寫入 " + insertList.size() + " 筆紀錄");
		if (deleteReqList.size() > 0) {
			deleteReqList.forEach(rm -> {
				getTsmpReqLogDao().delete(rm);
			});
			this.logger.error("TsmpReqLog 已刪除 " + deleteReqList.size() + " 筆高過一小時仍不匹配的紀錄");

		}
		if (deleteResList.size() > 0) {
			deleteResList.forEach(d -> {
				TsmpReqLog req = getTsmpReqLogDao().findById(d.getId()).orElse(null);
				if (req == null) {
					getTsmpResLogDao().delete(d);
				}
			});

		}

		Map<String, Object> map = new HashMap<>();
		map.put("req", reqList);
		map.put("res", resList);
		this.logger.info("--- Finish HandleDashboardLogDataService ---");
		return map;
	}

	private Date getNowIntervalDate(Date nowDate) {
		String strDate = DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日時分).orElseThrow(TsmpDpAaRtnCode._1295::throwing);
		strDate = strDate.substring(0, 15);
		strDate = strDate + "0:00.000";
		Date nowIntervalDate = DateTimeUtil.stringToDateTime(strDate, DateTimeFormatEnum.西元年月日時分秒毫秒).orElseThrow(TsmpDpAaRtnCode._1295::throwing);

		return nowIntervalDate;

	}

	private Date getOneYearAgo(Date nowDate) {
		Calendar nowTime = Calendar.getInstance();
		nowTime.setTime(nowDate);
		nowTime.add(Calendar.YEAR, -1);
		nowDate = nowTime.getTime();
		String strDate = DateTimeUtil.dateTimeToString(nowDate, DateTimeFormatEnum.西元年月日時分).orElseThrow(TsmpDpAaRtnCode._1295::throwing);
		strDate = strDate.substring(0, 15);
		strDate = strDate + "0";
		Date nowIntervalDate = DateTimeUtil.stringToDateTime(strDate, DateTimeFormatEnum.西元年月日時分).orElseThrow(TsmpDpAaRtnCode._1295::throwing);

		return nowIntervalDate;

	}

	protected TsmpReqResLogHistoryDao getTsmpReqResLogHistoryDao() {
		return tsmpReqResLogHistoryDao;
	}

	protected TsmpReqLogDao getTsmpReqLogDao() {
		return tsmpReqLogDao;
	}

	protected TsmpResLogDao getTsmpResLogDao() {
		return tsmpResLogDao;
	}
}
