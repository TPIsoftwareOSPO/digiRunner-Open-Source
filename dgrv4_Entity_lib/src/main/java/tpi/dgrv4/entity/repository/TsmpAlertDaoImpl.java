package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.jpql.TsmpAlert;

public class TsmpAlertDaoImpl extends BaseDao {
	// add custom methods here

	public List<TsmpAlert> queryByAA0706Service(Long lastAlertId, Boolean alertEnabled, String roleName, String[] words, Integer pageSize){
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT a ");
		sb.append(" FROM TsmpAlert a ");
		sb.append(" WHERE 1 = 1 ");
		

		// 分頁
		if (lastAlertId != null) {
			sb.append(" AND ");
			sb.append(" ( ");
			sb.append(" 	1 = 2 ");
			sb.append(" 	OR a.alertId > :lastAlertId ");
			sb.append(" ) ");
			params.put("lastAlertId", lastAlertId);
		}
		
		//alertEnabled
		if(alertEnabled != null) {
			sb.append(" AND a.alertEnabled = :alertEnabled ");
			params.put("alertEnabled", alertEnabled);
		}
		
		//join TsmpRoleAlert,TsmpRole
		if (StringUtils.isEmpty(roleName) == false) {
			sb.append(" AND exists(select 1 from TsmpRoleAlert ra where a.alertId = ra.alertId "); 
			sb.append("     AND exists(select 1 from TsmpRole r where ra.roleId = r.roleId and r.roleName = :roleName ");
			sb.append(" ))");
			params.put("roleName", roleName);
		}
		
		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" AND ( 1 = 2 ");
			//TsmpAlert關鍵字
			for (int i = 0; i < words.length; i++) {
				//Id 要轉型
				Long keywordId = null;
				try {
					keywordId = Long.valueOf(words[i]);
					sb.append(" OR a.alertId = :keywordId"+i);//Long
					params.put("keywordId" + i, keywordId );//Long
				}catch (Exception e) {
				}
				
				sb.append(" OR UPPER(a.alertName) like :keyworkSearch"+i);
				sb.append(" OR UPPER(a.alertSys) like :keyworkSearch"+i);
				params.put("keyworkSearch"+i,"%" + words[i].toUpperCase()+ "%" );
			}			
			sb.append("		) ");
		}
		
		sb.append(" ORDER BY a.alertId ASC ");

		return doQuery(sb.toString(), params, TsmpAlert.class, pageSize);
	}
}
