package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.jpql.TsmpRegHost;

public class TsmpRegHostDaoImpl extends BaseDao {
	// add custom methods here
	public List<TsmpRegHost> query_aa0806Service(String regHostId, String enableHeartbeat, String hostStatus, boolean isNeedCheck, 
			String[] words, String userName, Integer pageSize){
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT R ");
		sb.append(" FROM TsmpRegHost R ");
		sb.append(" WHERE 1 = 1 ");
		
		//心跳(資料庫:enable)
		if(!StringUtils.isEmpty(enableHeartbeat)) {
			sb.append(" AND R.enabled = :enableHeartbeat ");
			params.put("enableHeartbeat", enableHeartbeat);
		}
		//主機狀態(資料庫:reghostStatus)
		if(!StringUtils.isEmpty(hostStatus)) {
			sb.append(" AND R.reghostStatus = :reghostStatus ");
			params.put("reghostStatus", hostStatus);
		}
		// 分頁
		if (!StringUtils.isEmpty(regHostId)) {
			sb.append(" AND ");
			sb.append(" ( ");
			sb.append(" 	1 = 2 ");
			sb.append(" 	OR R.reghostId > :regHostId ");
			sb.append(" ) ");
			params.put("regHostId", regHostId);
		}
		
		if(isNeedCheck) {
			sb.append(" AND  ( ");
			sb.append(" 	 R.createUser = :username ");
			sb.append(" ) ");
			params.put("username", userName);
		}

		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" AND ( 1 = 2 ");
			for (int i = 0; i < words.length; i++) {
				sb.append(" OR UPPER(R.reghostId) like :keyworkSearch"+i);
				sb.append(" OR UPPER(R.reghost) like :keyworkSearch"+i);
				sb.append(" OR UPPER(R.clientid) like :keyworkSearch"+i);
				sb.append(" OR UPPER(R.memo) like :keyworkSearch"+i);
				params.put("keyworkSearch"+i,"%" + words[i].toUpperCase()+ "%" );
			}
			sb.append("		) ");
		}
		sb.append(" ORDER BY R.reghostId ASC ");
		return doQuery(sb.toString(), params, TsmpRegHost.class, pageSize);
	}
}
