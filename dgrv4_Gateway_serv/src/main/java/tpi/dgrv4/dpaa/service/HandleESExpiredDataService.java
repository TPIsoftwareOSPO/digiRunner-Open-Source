package tpi.dgrv4.dpaa.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.entity.jpql.DgrDashboardEsLog;
import tpi.dgrv4.entity.repository.DgrDashboardEsLogDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class HandleESExpiredDataService {
	
	private DgrDashboardEsLogDao dgrDashboardEsLogDao;

	@Autowired
	public HandleESExpiredDataService(DgrDashboardEsLogDao dgrDashboardEsLogDao) {
		super();
		this.dgrDashboardEsLogDao = dgrDashboardEsLogDao;
	}

	@Transactional
	public void exec(Date execDate, Long jobId) throws Exception {
		// 刪除年前的資料
		Date oneYearAgo = this.getOneYearAgo(execDate);
		List<DgrDashboardEsLog> ovewOneYearData = getDgrDashboardEsLogDao().findByRtimeLessThan(oneYearAgo);

		if (ovewOneYearData.size() > 0) {
			ovewOneYearData.forEach(dvo -> {
				getDgrDashboardEsLogDao().delete(dvo);
			});
			TPILogger.tl.info("DgrDashboardEsLog 已刪除 " + ovewOneYearData.size() + " 筆超過一年的紀錄");
		}
	}

	// 取得一年前的時間
	public Date getOneYearAgo(Date nowDate) {
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

	protected DgrDashboardEsLogDao getDgrDashboardEsLogDao() {
		return dgrDashboardEsLogDao;
	}
}
