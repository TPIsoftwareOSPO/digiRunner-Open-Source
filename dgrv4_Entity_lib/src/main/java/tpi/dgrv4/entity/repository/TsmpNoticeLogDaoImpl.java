package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.jpql.TsmpNoticeLog;

public class TsmpNoticeLogDaoImpl extends BaseDao {
	// add custom methods here

	public List<TsmpNoticeLog> query_noticeExpCertJob_01(String noticeSrc, String noticeMthd, String noticeKey, //
			Date lastNoticeDateTime) {
		StringBuffer sql = new StringBuffer();
		Map<String, Object> params = new HashMap<>();
		
		sql.append(" SELECT log");
		sql.append(" FROM TsmpNoticeLog log");
		sql.append(" WHERE 1 = 1");
		sql.append(" AND log.noticeSrc = :noticeSrc");
		sql.append(" AND log.noticeMthd = :noticeMthd");
		sql.append(" AND log.noticeKey = :noticeKey");
		sql.append(" AND log.lastNoticeDateTime > :lastNoticeDateTime");
		sql.append(" ORDER BY log.lastNoticeDateTime DESC");
		params.put("noticeSrc", noticeSrc);
		params.put("noticeMthd", noticeMthd);
		params.put("noticeKey", noticeKey);
		params.put("lastNoticeDateTime", lastNoticeDateTime);

		return doQuery(sql.toString(), params, TsmpNoticeLog.class);
	}
	
}