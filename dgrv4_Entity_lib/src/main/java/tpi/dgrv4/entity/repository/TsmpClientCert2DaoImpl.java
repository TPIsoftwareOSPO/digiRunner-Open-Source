package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.jpql.TsmpClientCert2;

public class TsmpClientCert2DaoImpl extends BaseDao {
	// add custom methods here

	public List<TsmpClientCert2> query_dpb0088Service(Date startDate, Date endDate, //
			TsmpClientCert2 lastRecord, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select CC");
		sb.append(" from TsmpClientCert2 CC");
		sb.append(" where 1 = 1");
		// 分頁
		if (lastRecord != null) {
			sb.append(" and ( 1 = 2");
			sb.append(" 	or (CC.createDateTime < :lastCdt and CC.clientCert2Id <> :lastId)");
			sb.append(" 	or (CC.createDateTime = :lastCdt and CC.clientCert2Id > :lastId)");
			sb.append(" )");
			params.put("lastCdt", lastRecord.getCreateDateTime());
			params.put("lastId", lastRecord.getClientCert2Id());
		}
		// 必要條件
		sb.append(" and ( 1 = 2");
		sb.append(" 	or (CC.updateDateTime >= :startDate and CC.updateDateTime < :endDate)");
		sb.append(" 	or (CC.createDateTime >= :startDate and CC.createDateTime < :endDate)");
		sb.append(" )");
		sb.append(" order by CC.createDateTime desc, CC.clientCert2Id asc");
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		
		return doQuery(sb.toString(), params, TsmpClientCert2.class, pageSize);
	}
 

}
