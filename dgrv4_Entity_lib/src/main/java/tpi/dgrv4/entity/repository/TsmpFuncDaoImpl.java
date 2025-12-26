package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.TsmpFunc;

public class TsmpFuncDaoImpl extends BaseDao {
	// add custom methods here

	public List<TsmpFunc> query_aa0103Service(String funcCode, String locale, String[] words, Integer pageSize, String funcType) {
		
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select A");
		sb.append(" from TsmpFunc A");
		sb.append(" where 1 = 1 ");
		sb.append(" and A.funcType = :funcType");
		params.put("funcType", funcType);
		// 分頁
		if (!StringUtils.isEmpty(funcCode) && !StringUtils.isEmpty(locale)) {
			sb.append("	  and ( 1 = 2");
			sb.append("   or (A.funcCode > :lastFuncCode )");
			sb.append("   or (A.funcCode = :lastFuncCode and A.locale > :lastLocale)");
			sb.append("   )");		
			
			params.put("lastFuncCode", funcCode);
			params.put("lastLocale", locale);
		}
		
		//關鍵字search 
		//1.找tsmp_func的 [func_code,func_name,func_desc]
		if (words != null && words.length > 0) {
			sb.append(" 	and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append("    or UPPER(A.funcCode) = :funcCode"+i );//忽略大小寫
				sb.append("    or UPPER(A.funcName) like :word" + i);//忽略大小寫
				sb.append("    or UPPER(A.funcDesc) like :word" + i);//忽略大小寫
				params.put("funcCode" + i,  words[i].toUpperCase() );//忽略大小寫
				params.put("word" + i, "%" + words[i].toUpperCase() + "%");//忽略大小寫
			}
			sb.append(" 		or EXISTS(");
			sb.append(" 		SELECT 1 ");
			sb.append(" 		FROM  TsmpRoleFunc F ");
			sb.append(" 		where F.funcCode = A.funcCode ");
			sb.append(" 			and  EXISTS (");
			sb.append(" 				SELECT R.roleId ");
			sb.append(" 				from TsmpRole R ");
			sb.append(" 				where R.roleId = F.roleId ");
			
			for(int i = 0 ; i < words.length ; i++) {
				if(i == 0) {
					sb.append(" 			and  ( UPPER(R.roleName) like :word" + i );
					sb.append(" 			or UPPER(R.roleAlias)  like :word" + i);
					params.put("word" + i, "%" + words[i].toUpperCase() + "%");//忽略大小寫
				}else {
					
					sb.append(" 			or UPPER(R.roleName) like :word" + i );
					sb.append(" 			or UPPER(R.roleAlias)  like :word" + i);
					params.put("word" + i, "%" + words[i].toUpperCase() + "%");//忽略大小寫
				}
			}
			sb.append("					)");
			sb.append("				)");
			sb.append(" 	    )");
			sb.append(" 	)");
		} 
		
		sb.append(" ORDER BY A.funcCode, A.locale ASC ");
		
		return doQuery(sb.toString(), params, TsmpFunc.class, pageSize);

	}
	
	public List<String> queryAllFuncCode(){
		Map<String, Object> params = new HashMap<>();
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("select distinct A.funcCode from TsmpFunc A order by A.funcCode");
		
		return doQuery(sb.toString(), params, String.class);
	}

	public List<TsmpFunc> findMasterFuncList(String locale){
		Map<String, Object> params = new HashMap<>();		
		StringBuffer sb = new StringBuffer();
		
		sb.append("select f from TsmpFunc f where LENGTH(f.funcCode) = 4 and locale = :locale ");
		params.put("locale", locale);
		List<TsmpFunc> list = doQuery(sb.toString(), params, TsmpFunc.class);
		return list;
	}
}
