package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.jpql.TsmpDc;

public class TsmpDcDaoImpl extends BaseDao {
	// add custom methods here

	//AA0418
	public List<TsmpDc> queryDCList_1(TsmpDc lastRecord, String dcMappingPrefix, String[] words, //
			Boolean isActive, int pageSize, boolean isPaging){
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpDc A where 1 = 1");
 
		if(isPaging) {//是否分頁
			// 分頁
			if (lastRecord != null) {
				sb.append(" and ( 1 = 2");
				sb.append("    or (A.dcId > :dcId)");
				sb.append(" )");	
				params.put("dcId", lastRecord.getDcId());
			}
		}
		 
		// 部署容器狀態(active)
		if (isActive != null) {
			sb.append(" and ( 1 = 2");
			sb.append("    or (A.active = :active)");
			sb.append(" )");			
			params.put("active", isActive);
		}
		
		//關鍵字search [ 部署容器代碼(TSMP_DC.dc_code)/部署容器備註(TSMP_DC.dc_memo)/節點名稱(TSMP_DC_NODE.node)/模組名稱(TSMP_API_MODULE.module_name) ]
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 	or UPPER( CONCAT('" + dcMappingPrefix + "', A.dcCode) ) like :word" + i);
				sb.append(" 	or UPPER(A.dcMemo) like :word" + i);
				sb.append(" 	or exists (");
				sb.append(" 		select 1 from TsmpDcNode B");
				sb.append(" 		where A.dcId = B.dcId");
				sb.append(" 		and UPPER(B.node) like :word" + i);
				sb.append(" 	)");
				sb.append(" 	or exists (");
				sb.append(" 		select 1 from TsmpDcModule C");
				sb.append(" 		where A.dcId = C.dcId");
				sb.append(" 		and exists (");
				sb.append(" 			select 1 from TsmpApiModule D");
				sb.append(" 			where C.moduleId = D.id");
				sb.append(" 			and UPPER(D.moduleName) like :word" + i);
				sb.append(" 		)");
				sb.append(" 	)");
				params.put("word" + i, "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" )");
		}
 
		sb.append(" order by A.dcId asc");

		if(isPaging) {
			return doQuery(sb.toString(), params, TsmpDc.class, pageSize);
			
		}else {
			return doQuery(sb.toString(), params, TsmpDc.class, Integer.MAX_VALUE);
			
		}
	}
	
	//AA0422
	public List<TsmpDc> queryDCList_2(String moduleName){
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append("SELECT tsmpDc ");
		sb.append("FROM TsmpDc tsmpDc ");
		sb.append("WHERE EXISTS (SELECT 1 ");
		sb.append("              FROM TsmpDcModule TsmpDcModule ");
		sb.append("              WHERE tsmpDc.dcId = TsmpDcModule.dcId ");
		sb.append("              AND   EXISTS (SELECT 1 ");
		sb.append("                            FROM TsmpApiModule tsmpApiModule ");
		sb.append("                            WHERE TsmpDcModule.moduleId = tsmpApiModule.id ");
		sb.append("                            AND   tsmpApiModule.moduleName = :moduleName )) ");
		
		params.put("moduleName", moduleName);
		
		return doQuery(sb.toString(), params, TsmpDc.class, Integer.MAX_VALUE);			
	}
}
