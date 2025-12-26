package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import tpi.dgrv4.entity.entity.jpql.TsmpNode;

public class TsmpNodeDaoImpl extends BaseDao {
	// add custom methods here
 
	public List<String> queryGreenTsmpNode(String lastNode, String[] words, Date queryStartDate,
			List<String> excludeNode, int pageSize){
		
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		
		sb.append(" select distinct A.node from TsmpNode A where 1 = 1");
 
		// 分頁
		if (lastNode != null) {
			sb.append(" and ( 1 = 2");
			sb.append("    or (A.node > :lastNode)");//找不重複的node,所以只需篩選 node,不需id
			sb.append(" )");	
			params.put("lastNode", lastNode);
		}
		
		// 必要條件
		// 排除 node 欄位值包含 "proxy" 或 "TSMP.NET" 的資料
		sb.append(" and UPPER(A.node) not like :notLikeWord1 ");
		sb.append(" and UPPER(A.node) not like :notLikeWord2 ");
		params.put("notLikeWord1", "%" + "proxy".toUpperCase() + "%");
		params.put("notLikeWord2", "%" + "TSMP.NET".toUpperCase() + "%");
		//updateTime > ( now - 1分鐘)
		sb.append(" and (A.updateTime > :updateTime)");
		params.put("updateTime", queryStartDate);
 
		// 欲排除的節點名稱
		if (excludeNode != null && !excludeNode.isEmpty()) {
			sb.append(" and (A.node not in (:excludeNode))");
			params.put("excludeNode", excludeNode);
		}
		
		// 關鍵字search [ node ]
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 	or UPPER(A.node) like :word" + i);
				params.put("word" + i, "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" )");
		}
 
		sb.append(" order by A.node asc");

		return doQuery(sb.toString(), params, String.class, pageSize);
	}

	public TsmpNode query_AA0404Service_01(String moduleName, Integer v2Flag) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select N");
		sb.append(" from TsmpNode N");
		sb.append(" where 1 = 1");
		sb.append(" and exists (");
		sb.append("   select 1");
		sb.append("   from TsmpDcNode DN");
		sb.append("   where DN.node = N.node");
		sb.append("   and exists (");
		sb.append("     select 1");
		sb.append("     from TsmpDc D");
		sb.append("     where D.dcId = DN.dcId");
		sb.append("     and D.active = true");
		sb.append("     and exists (");
		sb.append("       select 1");
		sb.append("       from TsmpDcModule DM");
		sb.append("       where DM.dcId = D.dcId");
		sb.append("       and exists (");
		sb.append("         select 1");
		sb.append("         from TsmpApiModule M");
		sb.append("         where M.id = DM.moduleId");
		sb.append("         and M.moduleName = :moduleName");
		if (v2Flag == null || v2Flag.equals(1)) {
			sb.append("         and (M.v2Flag is null or M.v2Flag = 1)");
		} else {
			sb.append("         and M.v2Flag = :v2Flag");
			params.put("v2Flag", v2Flag);
		}
		sb.append("       )");
		sb.append("     )");
		sb.append("   )");
		sb.append(" )");
		sb.append(" order by N.updateTime desc");
		params.put("moduleName", moduleName);
		List<TsmpNode> nList = doQuery(sb.toString(), params, TsmpNode.class);
		if (CollectionUtils.isEmpty(nList)) {
			return null;
		} else {
			return nList.get(0);
		}
	}

}
