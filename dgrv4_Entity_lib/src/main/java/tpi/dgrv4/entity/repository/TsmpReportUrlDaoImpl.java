package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.jpql.TsmpReportUrl;

public class TsmpReportUrlDaoImpl extends BaseDao {
	
	public List<TsmpReportUrl> queryAllByReportId(List<String> reportIdList) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select A ");
		sb.append(" from TsmpReportUrl A ");
		sb.append(" where 1 = 1 ");
		sb.append(" and A.reportId in (:reportIdList) ");
		sb.append(" order by A.reportId asc");
		params.put("reportIdList", reportIdList);
		return doQuery(sb.toString(), params, TsmpReportUrl.class);
	}
}
