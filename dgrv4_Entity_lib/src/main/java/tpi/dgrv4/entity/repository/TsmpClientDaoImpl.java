package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpPublicFlag;
import tpi.dgrv4.entity.entity.TsmpClient;

public class TsmpClientDaoImpl extends BaseDao {
	// add custom methods here
	
	@Deprecated
	public TsmpClient findPrivateClient(String clientId) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append("select C from TsmpClient C");
		sb.append(" where C.clientId = :clientId");
		sb.append(" and C.clientStatus = :clientStatus");	// 啟用
		sb.append(" and exists (");
		sb.append(" 	select 1 from TsmpDpClientext E");
		sb.append(" 	where C.clientId  = E.clientId");
		sb.append(" 	and (E.publicFlag is null or E.publicFlag = :publicFlag)");	// 私有
		sb.append(" )");
		
		params.put("clientId", clientId);
		params.put("clientStatus", "1");
		params.put("publicFlag", TsmpDpPublicFlag.PRIVATE.value());

		List<TsmpClient> clientList = doQuery(sb.toString(), params, TsmpClient.class);
		if (clientList != null && !clientList.isEmpty()) {
			return clientList.get(0);
		}
		return null;
	}
	
	public List<TsmpClient> queryLike(String lastId, String[] words, int pageSize) {
		
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append("select c from TsmpClient c where 1 = 1");

		// 分頁
		if (lastId != null) {
			sb.append(" and ( 1 = 2");
			sb.append("    or (c.clientId > :lastId)");
			sb.append(" )");			
			params.put("lastId", lastId);
		}
		
		// 關鍵字search [CLIENT_ID / CLIENT_NAME / CLIENT_ALIAS]
		if (words != null && words.length > 0) {
			sb.append(" 	and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append("    or UPPER(c.clientId) like :keywordSearch" + i);
				sb.append("    or UPPER(c.clientName) like :keywordSearch" + i);
				sb.append("    or UPPER(c.clientAlias) like :keywordSearch" + i);
				params.put("keywordSearch" + i, "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" )");
		} 
		sb.append(" order by c.clientId asc");

		
		return doQuery(sb.toString(), params, TsmpClient.class, pageSize);
	}
	
	public List<TsmpClient> findByClientIdAndKeywordAndGroupIdAndStatus(String clientId, String[] words, String groupID, String status, int pageSize){
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT tsmpClient ");
		sb.append(" FROM TsmpClient tsmpClient ");
		sb.append(" WHERE 1 = 1 ");
		
		if (StringUtils.isEmpty(status) == false) {
			sb.append(" AND tsmpClient.clientStatus = :status ");
			params.put("status", status);
		}
		
		// JOIN
		if (StringUtils.isEmpty(groupID) == false) {
			sb.append("	AND Exists ( ");
			sb.append(" 			SELECT 1 ");
			sb.append(" 			FROM TsmpClientGroup tsmpClientGroup ");
			sb.append(" 			WHERE tsmpClientGroup.clientId = tsmpClient.clientId ");
			sb.append(" 				AND Exists (  ");
			sb.append(" 							SELECT 1 ");
			sb.append(" 			 				FROM TsmpGroup tsmpGroup ");
			sb.append(" 			 				WHERE tsmpGroup.groupId = :groupID ");
			sb.append(" 			 				AND tsmpGroup.groupId = tsmpClientGroup.groupId");
			sb.append(" 			 				) ");
			sb.append(" 			) ");
			params.put("groupID", groupID);
		}
		
		// 分頁
		if (StringUtils.isEmpty(clientId) == false) {
			sb.append(" AND ");
			sb.append(" ( ");
			sb.append(" 	1 = 2 ");
			sb.append(" 	OR tsmpClient.clientId > :clientId ");
			sb.append(" ) ");
			params.put("clientId", clientId);
		}
		
		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" AND ( 1 = 2 ");
			for (int i = 0; i < words.length; i++) {
				sb.append(" OR UPPER(tsmpClient.clientId) like :keyworkSearch"+i);
				sb.append(" OR UPPER(tsmpClient.clientName) like :keyworkSearch"+i);
				sb.append(" OR UPPER(tsmpClient.clientAlias) like :keyworkSearch"+i);
				params.put("keyworkSearch"+i,"%" + words[i].toUpperCase()+ "%" );
			}
			sb.append("		) ");
		}
		
		sb.append(" ORDER BY tsmpClient.clientId ASC ");
		
		return doQuery(sb.toString(), params, TsmpClient.class, pageSize);
	}
	
	//DPB0095
	public List<TsmpClient> queryByRegStatusAndLike(String lastId, String[] words, String regStatus, int pageSize) {
		
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append("select A from TsmpClient A where 1 = 1");

		// 分頁
		if (lastId != null) {
			sb.append(" and ( 1 = 2");
			sb.append("    or (A.clientId > :lastId)");
			sb.append(" )");			
			params.put("lastId", lastId);
		}
		
		// 必要條件
		// 會員資格狀態(reqStatus)
		sb.append(" and exists (");
		sb.append("		select 1 from TsmpDpClientext B");
		sb.append("		where B.clientId = A.clientId");
		sb.append("		and B.regStatus = :regStatus");
		sb.append(" )");			
		params.put("regStatus", regStatus);
		
		// 關鍵字search [CLIENT_ID / CLIENT_NAME / CLIENT_ALIAS]
		if (words != null && words.length > 0) {
			sb.append(" 	and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append("    or UPPER(A.clientId) like :keywordSearch" + i);
				sb.append("    or UPPER(A.clientName) like :keywordSearch" + i);
				sb.append("    or UPPER(A.clientAlias) like :keywordSearch" + i);
				params.put("keywordSearch" + i, "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" )");
		} 
		sb.append(" order by A.clientId asc");
		
		return doQuery(sb.toString(), params, TsmpClient.class, pageSize);
	}
	
	public List<TsmpClient> queryByClientIdNotExists() {
		Map<String, Object> params = null;
		
		StringBuffer sb = new StringBuffer();
		sb.append("select a from TsmpClient a ");
		sb.append(" where not exists ");
		sb.append(" (select 1 from TsmpDpClientext b ");
		sb.append("   where a.clientId = b.clientId ");
		sb.append(" )");
		
		return doQuery(sb.toString(), params, TsmpClient.class);
	}

}