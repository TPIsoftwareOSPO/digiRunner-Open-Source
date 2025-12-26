package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.TsmpRtnCode;

public class TsmpRtnCodeDaoImpl extends BaseDao {
	// add custom methods here
	
	//DPB0097
	public List<TsmpRtnCode> query_dpb0097service_01(String rtnCode, String locale, String[] words, int pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder();
		sb.append("select rtnCode from TsmpRtnCode rtnCode where 1 = 1");

		// 是否有傳入 last row id
		if (StringUtils.hasLength(rtnCode) && StringUtils.hasLength(locale)) {
			sb.append(" and ( 1 = 2");
			sb.append("    or (rtnCode.tsmpRtnCode > :lastRtnCode )");
			sb.append("    or (rtnCode.tsmpRtnCode = :lastRtnCode and rtnCode.locale > :lastLocale)");
			sb.append(" )");			
			params.put("lastRtnCode", rtnCode);
			params.put("lastLocale", locale);
		}
		
		//關鍵字search [tsmpRtnCode/locale/tsmpRtnMsg/tsmpRtnDesc]
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append("    or UPPER(rtnCode.tsmpRtnCode) like :word" + i);//忽略大小寫
				sb.append("    or UPPER(rtnCode.locale) like :word" + i);//忽略大小寫
				sb.append("    or UPPER(rtnCode.tsmpRtnDesc) like :word" + i);//忽略大小寫
				params.put("word" + i, "%" + words[i].toUpperCase() + "%");//忽略大小寫
			}
			sb.append("    or UPPER(rtnCode.tsmpRtnCode) in ");
			sb.append("       (");
			sb.append("        select rtnCode.tsmpRtnCode");
			sb.append("        from TsmpRtnCode rtnCode");
			sb.append("        where 1 = 2");
			for (int j = 0; j < words.length; j++) {
				sb.append("       or UPPER(rtnCode.tsmpRtnMsg) like : word" + j);
				params.put("word" + j, "%" + words[j].toUpperCase() + "%");//忽略大小寫
			}
			sb.append("       )");
			sb.append(" )");
		} 
		
		sb.append(" order by rtnCode.tsmpRtnCode asc, rtnCode.locale asc");
		return doQuery(sb.toString(), params, TsmpRtnCode.class, pageSize);
	}

}
