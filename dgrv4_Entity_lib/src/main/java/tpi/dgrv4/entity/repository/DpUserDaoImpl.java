package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.DpUser;



public class DpUserDaoImpl extends BaseDao {

	// add custom methods here
	public List<DpUser> findByKeyword(Long userId, String userFlag, String[] keyword, int pageSize) {

		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder();

		sb.append(" SELECT dpUser ");
		sb.append(" FROM DpUser dpUser ");
		sb.append(" WHERE 1 = 1  ");

		if (userId != null) {
			sb.append(" AND ");
			sb.append(" ( ");
			sb.append("    1 = 2 ");
			sb.append("    OR dpUser.dpUserId > :userId ");
			sb.append(" ) ");
			params.put("userId", userId);
		}

		if (userFlag != null) {
			sb.append(" AND ");
			sb.append(" ( ");
			sb.append("    1 = 2 ");
			sb.append("    OR dpUser.userIdentity = :userFlag ");
			sb.append(" ) ");
			params.put("userFlag", userFlag);
		}

		if (keyword != null && keyword.length > 0) {
			sb.append(" AND ( 1 = 2 ");
			for (int i = 0; i < keyword.length; i++) {
				sb.append(" OR UPPER(dpUser.dpUserName) like :keyworkSearch" + i);
				sb.append(" OR UPPER(dpUser.userAlias) like :keyworkSearch" + i);
				sb.append(" OR UPPER(dpUser.iss) like :keyworkSearch" + i);
				params.put("keyworkSearch" + i, "%" + keyword[i].toUpperCase() + "%");
			}
			sb.append(" ) ");
		}

		sb.append(" ORDER BY dpUser.dpUserId ASC ");

		return doQuery(sb.toString(), params, DpUser.class, pageSize);
	}
}
