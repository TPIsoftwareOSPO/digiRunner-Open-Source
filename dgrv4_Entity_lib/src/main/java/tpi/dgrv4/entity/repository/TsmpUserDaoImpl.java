package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.entity.entity.TsmpUser;

public class TsmpUserDaoImpl extends BaseDao {
	// add custom methods here
	
	public List<TsmpUser> query_dpb0039Service(String userStatus, String[] words, String lastId, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpUser A");
		sb.append(" where A.userStatus = :userStatus");
		// 分頁
		if (lastId != null && !lastId.isEmpty()) {
			sb.append(" and A.userId > :userId");
			params.put("userId", lastId);
		}
		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 	or UPPER(A.userName) like :lk_keyword" + i);
				sb.append(" 	or UPPER(A.userAlias) like :lk_keyword" + i);
				sb.append(" 	or UPPER(A.userEmail) like :lk_keyword" + i);
				params.put(("lk_keyword" + i), "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" )");
		}
		sb.append(" order by A.userId asc");

		params.put("userStatus", userStatus);

		return doQuery(sb.toString(), params, TsmpUser.class, pageSize);
	}
	
	public List<TsmpUser> queryByReviewTypeAndLayer(String reviewType, Integer layer) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append("  select A from TsmpUser A where 1 = 1");
		sb.append("  and exists(");
		sb.append("  	select 1 from Authorities B where 1 = 1");
		sb.append(" 	and B.username = A.userName");
		sb.append(" 	and exists(");
		sb.append(" 		select 1 from TsmpDpChkLayer C where 1 = 1");
		sb.append(" 		and reviewType = :reviewType");
		sb.append(" 		and layer = :layer");
		sb.append(" 		and C.status = :status");
		sb.append(" 		and C.roleId = B.authority");
		sb.append(" 	)");
		sb.append(" )");
		params.put("reviewType", reviewType);
		params.put("layer", layer);
		params.put("status", TsmpDpDataStatus.ON.value());

		return doQuery(sb.toString(), params, TsmpUser.class, -1);
	}

	public List<TsmpUser> query_aa0019Service(String roleName, List<String> orgIdListFromOrgName, List<String> orgDescList, String[] words, 
			String lastId, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select U");
		sb.append(" from TsmpUser U");
		
		sb.append(" where 1 = 1");
		sb.append(" and U.orgId in (:orgDescList)");
		
		params.put("orgDescList", orgDescList);
		
		// 分頁 是否有傳入 last row id
		if (lastId != null && !lastId.isEmpty()) {
			sb.append(" and ( 1 = 2");
			sb.append(" or U.userId > :lastUserId");
			sb.append(" )");
			params.put("lastUserId", lastId);
		}
		// 如果有選org
		if(orgIdListFromOrgName != null && orgIdListFromOrgName.size() > 0) {
			sb.append(" and exists ( ");
			sb.append(" 	select 1");
			sb.append(" 	from TsmpOrganization O");
			sb.append(" 	where 1 = 1");
			sb.append(" 	and O.orgId =  U.orgId");
			sb.append(" 	and O.orgId in  (:orgIdListFromOrgName)");
			sb.append(" )");
			
			params.put("orgIdListFromOrgName", orgIdListFromOrgName);
			
		}
		
		// 如果有選role
		if(!StringUtils.isEmpty(roleName)) {
			sb.append(" and exists ( ");
			sb.append("		select A.username ");
			sb.append(" 	from Authorities A ");
			sb.append(" 	where A.authority in (");
			sb.append(" 		select R.roleId");
			sb.append(" 		from TsmpRole R");
			sb.append(" 		where 1 = 1");
			sb.append(" 		and R.roleName = :roleName");
			sb.append(" 	)");
			sb.append(" 	and A.username =  U.userName");
			sb.append(" )");
			params.put("roleName", roleName);
		}
		
		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 	or UPPER(U.userId) like :lk_keyword" + i);
				sb.append(" 	or UPPER(U.userName) like :lk_keyword" + i);
				params.put(("lk_keyword" + i), "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" )");
		}
		
		sb.append("  order by U.userId asc");
			
		return doQuery(sb.toString(), params, TsmpUser.class, pageSize);
	}

	public List<TsmpUser> queryByRoleAlert(Long alertId) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT U");
		sb.append(" FROM TsmpUser U");
		sb.append(" WHERE 1 = 1");
		// 2022.07.01; 已停用的使用者不應收到告警通知
		sb.append(" AND (U.userStatus = '1' OR U.userStatus = '3')");
		sb.append(" AND EXISTS (");
		sb.append(" 	SELECT 1");
		sb.append(" 	FROM Authorities A");
		sb.append(" 	WHERE A.username = U.userName");
		sb.append(" 	AND EXISTS (");
		sb.append(" 		SELECT 1");
		sb.append(" 		FROM TsmpRoleAlert RA");
		sb.append(" 		WHERE RA.roleId = A.authority");
		sb.append(" 		AND RA.alertId = :alertId");
		sb.append(" 	)");
		sb.append(" )");
		params.put("alertId", alertId);
		return doQuery(sb.toString(), params, TsmpUser.class);
	}

}
