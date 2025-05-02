package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailLog;

public class TsmpDpMailLogDaoImpl extends BaseDao {
	// add custom methods here
	
	public List<TsmpDpMailLog> queryExpiredMail(Date expDate) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT A FROM TsmpDpMailLog A");
		sb.append(" WHERE A.createDateTime < :expDate");
		
		params.put("expDate", expDate);
		
		return doQuery(sb.toString(), params, TsmpDpMailLog.class);
	}

	public List<TsmpDpMailLog> queryMailLogList(String startDate, String endDate, String result, Long id,
			String keyword, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder();

		sb.append(" select log from TsmpDpMailLog log ");
		sb.append(" where 1 = 1 ");

		// 分頁設定
		if (id != null) {
			sb.append(" and log.maillogId < :id ");
			params.put("id", id);
		}

		// 查詢條件
		sb.append(" and log.createDateTime >= :startDate ");
		sb.append(" and log.createDateTime <= :endDate ");
		if(!"-1".equals(result))
			sb.append(" and log.result = :result ");

		// 日期格式檢查
		startDate = startDate.replace('/', '-');
		endDate = endDate.replace('/', '-');
		Optional<Date> opt_startDate = DateTimeUtil.stringToDateTime(startDate, DateTimeFormatEnum.西元年月日時分秒毫秒);
		Optional<Date> opt_endDate = DateTimeUtil.stringToDateTime(endDate, DateTimeFormatEnum.西元年月日時分秒毫秒);
		if (!opt_startDate.isPresent() || !opt_endDate.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}
		params.put("startDate", opt_startDate.get());
		params.put("endDate", opt_endDate.get());
		params.put("result", result);

		// 關鍵字查詢
		if (keyword != null && !StringUtils.isBlank(keyword)) {
			String words[] = keyword.split(" ");
			int i;
			sb.append(" and ( 1 = 2 ");
			for (i = 0; i < words.length; i++) {
				sb.append(" or upper(log.templateTxt) like :keyword" + i + " ");
				sb.append(" or upper(log.recipients) like :keyword" + i + " ");
				sb.append(" or upper(log.createUser) like :keyword" + i + " ");
				params.put("keyword" + i, "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" ) ");
		}

		sb.append(" order by log.createDateTime desc ");

		return doQuery(sb.toString(), params, TsmpDpMailLog.class, pageSize);
	}
}
