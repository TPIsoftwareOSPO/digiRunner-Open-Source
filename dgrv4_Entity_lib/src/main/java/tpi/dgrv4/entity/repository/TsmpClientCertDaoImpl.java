package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.jpql.TsmpClientCert;

public class TsmpClientCertDaoImpl extends BaseDao {
	// add custom methods here

	public List<TsmpClientCert> query_dpb0088Service(Date startDate, Date endDate, //
			TsmpClientCert lastRecord, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select CC");
		sb.append(" from TsmpClientCert CC");
		sb.append(" where 1 = 1");
		// 分頁
		if (lastRecord != null) {
			sb.append(" and ( 1 = 2");
			sb.append(" 	or (CC.createDateTime < :lastCdt and CC.clientCertId <> :lastId)");
			sb.append(" 	or (CC.createDateTime = :lastCdt and CC.clientCertId > :lastId)");
			sb.append(" )");
			params.put("lastCdt", lastRecord.getCreateDateTime());
			params.put("lastId", lastRecord.getClientCertId());
		}
		// 必要條件
		sb.append(" and ( 1 = 2");
		sb.append(" 	or (CC.updateDateTime >= :startDate and CC.updateDateTime < :endDate)");
		sb.append(" 	or (CC.createDateTime >= :startDate and CC.createDateTime < :endDate)");
		sb.append(" )");
		sb.append(" order by CC.createDateTime desc, CC.clientCertId asc");
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		
		return doQuery(sb.toString(), params, TsmpClientCert.class, pageSize);
	}

	/* 我覺得之後會改成這樣, 先備用
	public List<TsmpClientCert> query_dpb0088Service(Date startDate, Date endDate, //
			TsmpClientCert lastRecord, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select CC");
		sb.append(" from TsmpClientCert CC");
		sb.append(" where 1 = 1");
		// 分頁
		if (lastRecord != null) {
			// 上一筆有UpdateDateTime
			if (lastRecord.getUpdateDateTime() != null) {
				sb.append(" and ( 1 = 2");
				sb.append(" 	or (");
				sb.append(" 		CC.updateDateTime is not null and ( 1 = 2");
				sb.append(" 			or (CC.updateDateTime < :lastUdt and CC.clientCertId <> :lastId)");
				sb.append(" 			or (CC.updateDateTime = :lastUdt and CC.createDateTime < :lastCdt and CC.clientCertId <> :lastId)");
				sb.append(" 			or (CC.updateDateTime = :lastUdt and CC.createDateTime = :lastCdt and CC.clientCertId > :lastId)");
				sb.append(" 		)");
				sb.append(" 	)");
				sb.append(" 	or (");
				sb.append(" 		CC.updateDateTime is null and ( 1 = 2");
				sb.append(" 			or (CC.createDateTime < :lastUdt and CC.clientCertId <> :lastId)");
				sb.append(" 			or (CC.createDateTime = :lastUdt and CC.clientCertId > :lastId)");
				sb.append(" 		)");
				sb.append(" 	)");
				sb.append(" )");
				params.put("lastUdt", lastRecord.getUpdateDateTime());
			// 上一筆無UpdateDateTime
			} else {
				sb.append(" and ( 1 = 2");
				sb.append(" 	or (");
				sb.append(" 		CC.updateDateTime is not null and ( 1 = 2");
				sb.append(" 			or (CC.updateDateTime < :lastCdt and CC.clientCertId <> :lastId)");
				sb.append(" 			or (CC.updateDateTime = :lastCdt and CC.createDateTime < :lastCdt and CC.clientCertId <> :lastId)");
				sb.append(" 			or (CC.updateDateTime = :lastCdt and CC.createDateTime = :lastCdt and CC.clientCertId > :lastId)");
				sb.append(" 		)");
				sb.append(" 	)");
				sb.append(" 	or (");
				sb.append(" 		CC.updateDateTime is null and ( 1 = 2");
				sb.append(" 			or (CC.createDateTime < :lastCdt and CC.clientCertId <> :lastId)");
				sb.append(" 			or (CC.createDateTime = :lastCdt and CC.clientCertId > :lastId)");
				sb.append(" 		)");
				sb.append(" 	)");
				sb.append(" )");
			}
			params.put("lastCdt", lastRecord.getCreateDateTime());
			params.put("lastId", lastRecord.getClientCertId());
		}
		// 必要條件
		sb.append(" and ( 1 = 2");
		sb.append(" 	or (CC.updateDateTime >= :startDate and CC.updateDateTime < :endDate)");
		sb.append(" 	or (CC.createDateTime >= :startDate and CC.createDateTime < :endDate)");
		sb.append(" )");
		sb.append(" order by CC.updateDateTime desc, CC.createDateTime desc, CC.clientCertId asc");
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		
		return doQuery(sb.toString(), params, TsmpClientCert.class, pageSize);
	}
	*/

}
