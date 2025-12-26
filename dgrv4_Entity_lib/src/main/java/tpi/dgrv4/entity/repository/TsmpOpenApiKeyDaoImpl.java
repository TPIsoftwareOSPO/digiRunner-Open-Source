package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.entity.entity.TsmpOpenApiKey;

public class TsmpOpenApiKeyDaoImpl extends BaseDao {
	// add custom methods here

	//DPB0090, DPF0047
	public List<TsmpOpenApiKey> queryOpenApiKeyByClientId(TsmpOpenApiKey lastRecord, String clientId, int pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append("select A from TsmpOpenApiKey A where 1 = 1");

		// 分頁
		if (lastRecord != null) {
			sb.append(" and ( 1 = 2");
			sb.append("    or (A.createDateTime < :lastDateTime and A.openApiKeyId <> :lastId)");
			sb.append("    or (A.createDateTime = :lastDateTime and A.openApiKeyId < :lastId)");
			sb.append(" )");
			params.put("lastDateTime", lastRecord.getCreateDateTime());
			params.put("lastId", lastRecord.getOpenApiKeyId());
		}

		sb.append(" and A.clientId = :clientId");
		params.put("clientId", clientId);

		sb.append(" order by A.createDateTime desc, A.openApiKeyId desc");

		return doQuery(sb.toString(), params, TsmpOpenApiKey.class, pageSize);
	}
	
	public List<TsmpOpenApiKey> query_dpb0094Service(Date startDate, Date endDate, TsmpOpenApiKey lastRecord, 
			String[] words, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();
		
		StringBuffer sb = new StringBuffer();
		sb.append(" select A");
		sb.append(" from TsmpOpenApiKey A where 1 = 1");
		
		// 分頁
		if (lastRecord != null) {
			sb.append(" and ( 1 = 2");
			sb.append(" 	or (A.createDateTime < :lastDt and A.openApiKeyId <> :lastId)");
			sb.append(" 	or (A.createDateTime = :lastDt and A.openApiKeyId > :lastId)");
			sb.append(" )");
			params.put("lastDt", lastRecord.getCreateDateTime());
			params.put("lastId", lastRecord.getOpenApiKeyId());
		}
		
		// 必要條件 (createDateTime 或 updateDateTime 在日期範圍內)
		sb.append(" and ( 1 = 2");
		sb.append(" 	or (A.updateDateTime >= :startDate and A.updateDateTime < :endDate)");
		sb.append(" 	or (A.createDateTime >= :startDate and A.createDateTime < :endDate)");
		sb.append(" )");
		
		// 關鍵字 (CLIENT_ID、 CLIENT_NAME、 CLIENT_ALIAS)
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append("  	or exists(");
				sb.append(" 		select 1 from TsmpClient B");
				sb.append(" 		where B.clientId = A.clientId");
				sb.append(" 		and (1 = 2");
				sb.append("    		or UPPER(B.clientId) like :keyword" + i);
				sb.append("    		or UPPER(B.clientName) like :keyword" + i);
				sb.append("    		or UPPER(B.clientAlias) like :keyword" + i);
				sb.append(" 		)");
				sb.append(" 	)");
				params.put("keyword" + i, "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" )");
		}
 		
		
		sb.append(" order by A.createDateTime desc, A.openApiKeyId desc");
		params.put("startDate", startDate);
		params.put("endDate", endDate);
		
		return doQuery(sb.toString(), params, TsmpOpenApiKey.class, pageSize);
	}
	
	/**
	 * 找出過期 N 天的Open API Key
	 * 
	 * @param expDateLong
	 * @return
	 */
	public List<TsmpOpenApiKey> queryExpiredOpenApiKey(Long expDateLong) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpOpenApiKey A");
		sb.append(" where A.expiredAt < :expDateLong");
		
		params.put("expDateLong", expDateLong);
		
		return doQuery(sb.toString(), params, TsmpOpenApiKey.class);
	}
	
	/**
	 * 找出距離效期只剩 N 天 且 尚未展期,快到期的Open API Key
	 * 
	 * @param expDateLong
	 * @return
	 */
	public List<TsmpOpenApiKey> queryExpiringOpenApiKey(Long todayLong, Long expDateLong) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpOpenApiKey A");
		sb.append(" where A.expiredAt >= :todayLong and A.expiredAt < :expDateLong");
		sb.append(" and A.openApiKeyStatus = :openApiKeyStatus");
		sb.append(" and A.rolloverFlag = :rolloverFlag");
		
		params.put("todayLong", todayLong);
		params.put("expDateLong", expDateLong);
		params.put("openApiKeyStatus", TsmpDpDataStatus.ON.value());
		params.put("rolloverFlag", "N");
		
		return doQuery(sb.toString(), params, TsmpOpenApiKey.class);
	}

}
