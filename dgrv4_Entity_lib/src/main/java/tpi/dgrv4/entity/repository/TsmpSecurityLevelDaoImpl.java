package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;

public class TsmpSecurityLevelDaoImpl extends BaseDao {
	// add custom methods here
	
	//AA1116
	public List<TsmpSecurityLevel> querySecurityLevelList(TsmpSecurityLevel lastRecord, String[] words, int pageSize) {
		Map<String, Object> params = new HashMap<>();
		StringBuilder sb = new StringBuilder();
		sb.append("select A from TsmpSecurityLevel A where 1 = 1");
		
		//是否有傳入 last row id
		//分頁
		if(lastRecord != null) {
			sb.append(" and ( 1 = 2");
			sb.append("      or (A.securityLevelId > :lastSecurityLevelId)");			
			sb.append(" )");
			params.put("lastSecurityLevelId", lastRecord.getSecurityLevelId());
		}		
		
		//關鍵字search [security_level_id/security_level_name/security_level_desc]
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for (int i = 0 ; i < words.length ; i++) {
				sb.append("   or UPPER(A.securityLevelId) like :word" + i);//忽略大小寫
				sb.append("   or UPPER(A.securityLevelName) like :word" + i);//忽略大小寫
				sb.append("   or UPPER(A.securityLevelDesc) like :word" + i);//忽略大小寫
				params.put("word" + i, "%" + words[i].toUpperCase() + "%");//忽略大小寫
			}
			sb.append(" )");
		}
		sb.append(" order by A.securityLevelId asc");
				
		return doQuery(sb.toString(), params, TsmpSecurityLevel.class, pageSize);
	}
}
