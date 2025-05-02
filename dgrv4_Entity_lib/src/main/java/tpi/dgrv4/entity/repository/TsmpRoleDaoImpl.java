package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.TsmpRole;

public class TsmpRoleDaoImpl extends BaseDao {
	// add custom methods here

	private final String roleIdStr = "roleId";
	private final String keyworkSearchStr = "keyworkSearch";
	
	public List<TsmpRole> findByRoleIdAndKeyword(String roleId, String[] words, Integer pageSize){
	
		
		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT r ");
		sb.append(" FROM TsmpRole r ");
		sb.append(" WHERE 1 = 1  ");
		

		// 分頁
		if (StringUtils.hasText(roleId)) {
			sb.append(" AND  ");
			sb.append(" ( ");
			sb.append("    1 = 2 ");
			sb.append("    OR r.roleId > :roleId ");
			sb.append(" ) ");
			params.put(roleIdStr, roleId);
		}
		
		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" AND ( 1 = 2   ");
			for (int i = 0; i < words.length; i++) {
				sb.append(" OR UPPER(r.roleName) like :keyworkSearch"+i);
				sb.append(" OR UPPER(r.roleAlias) like :keyworkSearch"+i);
				params.put(keyworkSearchStr+i,"%" + words[i].toUpperCase()+ "%" );
			}
			sb.append(" ) ");
		}
		
		sb.append(" ORDER BY r.roleId ASC ");
		
		return doQuery(sb.toString(), params, TsmpRole.class, pageSize);
	}
	
	public List<TsmpRole> findByAA0022Service(String roleId, String[] words, Integer pageSize){
		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT r ");
		sb.append(" FROM TsmpRole r ");
		sb.append(" WHERE 1 = 1 ");
		

		// 分頁
		if (StringUtils.hasText(roleId)) {
			sb.append(" AND ");
			sb.append(" ( ");
			sb.append("   1 = 2    ");
			sb.append("   OR r.roleId > :roleId ");
			sb.append(" ) ");
			params.put("roleId", roleId);
		}
		
		//join TsmpRoleRoleMapping
		sb.append(" AND exists(select 1 from TsmpRoleRoleMapping m where r.roleName = m.roleName) ");
		
		
		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" AND ( 1 = 2 ");
			//TsmpRole關鍵字(角色代號) (角色名稱)
			for (int i = 0; i < words.length; i++) {
				sb.append(" OR UPPER(r.roleName) like :keyworkSearch"+i);
				sb.append(" OR UPPER(r.roleAlias) like :keyworkSearch"+i);
				params.put("keyworkSearch"+i,"%" + words[i].toUpperCase()+ "%" );
			}
			
			//TsmpUser關鍵字(使用者名稱) (使用者帳號)
			sb.append("     OR (exists(select 1 from Authorities a where a.authority =r.roleId ");
			sb.append("     AND exists(select 1 from TsmpUser u where a.username = u.userName");
			sb.append("            AND ( 1 = 2 ");
			for (int i = 0; i < words.length; i++) {
				sb.append("            OR UPPER(u.userName) like :keyworkSearch"+i);
				sb.append("            OR UPPER(u.userAlias) like :keyworkSearch"+i);
			}
			sb.append("                ) ");
			sb.append("           ))) ");
			
			sb.append(" ) ");
		}
		
		sb.append(" ORDER BY r.roleId ASC ");

		return doQuery(sb.toString(), params, TsmpRole.class, pageSize);
	}
	
	public List<TsmpRole> query_aa0104Service(String roleId, String funcCode, String[] words, Integer pageSize) {

		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder();
		sb.append(" select R");
		sb.append(" from TsmpRole R ");
		sb.append(" where 1 = 1 ");

		// 分頁
		if (!StringUtils.isEmpty(roleId)) {
			sb.append("   and  ( 1 = 2 ");
			sb.append("   or (R.roleId > :lastRoleID )");
			sb.append("   )");

			params.put("lastRoleID", roleId);
		}

		// 關鍵字search
		if (words != null && words.length > 0) {
			sb.append("   and  ( ");

			for (int i = 0; i < words.length; i++) {
				if (i == 0) {
					sb.append("     UPPER(R.roleName) like :word" + i);// 忽略大小寫
					sb.append("    or UPPER(R.roleAlias) like :word" + i);// 忽略大小寫

					params.put("word" + i, "%" + words[i].toUpperCase() + "%");// 忽略大小寫
				} else {
					sb.append("    or UPPER(R.roleName) like :word" + i);// 忽略大小寫
					sb.append("    or UPPER(R.roleAlias) like :word" + i);// 忽略大小寫

					params.put("word" + i, "%" + words[i].toUpperCase() + "%");// 忽略大小寫

				}
			}
			sb.append(" 	)");

		}

		sb.append("             and EXISTS (");
		sb.append("             SELECT 1 ");
		sb.append("             FROM  TsmpRoleFunc F ");
		sb.append("             where R.roleId = F.roleId ");
		sb.append("             and F.funcCode = :funcCode");// 忽略大小寫
		params.put("funcCode", funcCode);// 忽略大小寫
		sb.append("         )");

		sb.append(" ORDER BY R.roleId ASC ");

		return doQuery(sb.toString(), params, TsmpRole.class, pageSize);

	}
	
	public List<TsmpRole> queryByAA0023Service(String roleId, String userName, String[] words, Integer pageSize){
		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT r1 ");
		sb.append(" FROM TsmpRole r1 ");
		sb.append(" WHERE 1 = 1 ");
		

		// 分頁
		if (StringUtils.hasText(roleId)) {
			sb.append(" AND ");
			sb.append(" ( ");
			sb.append("     1 = 2     ");
			sb.append("     OR r1.roleId > :roleId ");
			sb.append(" ) ");
			params.put("roleId", roleId);
		}
		
		//join TsmpRoleRoleMapping,authorities
		sb.append(" AND exists(select 1 from TsmpRoleRoleMapping m where r1.roleName = m.roleNameMapping "); 
		sb.append("     AND exists(select 1 from TsmpRole r2 where m.roleName = r2.roleName "); 
		sb.append("         AND exists(select 1 from Authorities a where r2.roleId = a.authority and a.username = :userName ");
		sb.append(" )))");
		params.put("userName", userName);
		
		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" AND ( 1 = 2 ");
			//TsmpRole關鍵字(角色代號) (角色名稱)
			for (int i = 0; i < words.length; i++) {
				sb.append(" OR UPPER(r1.roleId) like :keyworkSearch"+i);
				sb.append(" OR UPPER(r1.roleName) like :keyworkSearch"+i);
				sb.append(" OR UPPER(r1.roleAlias) like :keyworkSearch"+i);
				params.put("keyworkSearch"+i,"%" + words[i].toUpperCase()+ "%" );
			}			
			sb.append("     ) ");
		}
		
		sb.append(" ORDER BY r1.roleId ASC ");

		return doQuery(sb.toString(), params, TsmpRole.class, pageSize);
	}

}
