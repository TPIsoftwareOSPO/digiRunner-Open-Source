package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.TsmpDpItems;

public class TsmpDpItemsDaoImpl extends BaseDao {
	// add custom methods here

	public List<TsmpDpItems> queryBcryptParam(String itemNo, String locale) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select A ");
		sb.append(" from TsmpDpItems A ");
		sb.append(" where A.itemNo = :itemNo ");
		sb.append(" and A.locale = :locale ");
		sb.append(" order by A.sortBy asc, A.itemId asc ");
		params.put("itemNo", itemNo);
		params.put("locale", locale);
		return doQuery(sb.toString(), params, TsmpDpItems.class);
	}

	//DPB0047, DPF0029
	public List<TsmpDpItems> queryLike(Long lastId, String[] words, String itemNo, 
			String isDefault, int pageSize, String locale){
		
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append("select item from TsmpDpItems item where 1 = 1");

		// 是否有傳入 last row id
		// 分頁
		if (lastId != null) {
			sb.append(" and ( 1 = 2");
			sb.append("    or (item.itemId > :lastItemId)");
			sb.append(" )");			
			params.put("lastItemId", lastId);
		}
 
		// 分類
		sb.append(" and item.itemNo = :itemNo");
		params.put("itemNo", itemNo);
		
		// 語系
		sb.append(" and item.locale = :locale");
		params.put("locale", locale);
 
		if(isDefault.equalsIgnoreCase("Y")) {//表示要取預設值,此時與keyword無關
			// 預設值
			sb.append(" and UPPER(item.isDefault) = :default");//忽略大小寫
			params.put("default", "V");//忽略大小寫
			
		}else {
			// 關鍵字
			if (words != null && words.length > 0) {
				sb.append(" 	and ( 1 = 2");
				for(int i = 0; i < words.length; i++) {
					sb.append("    or UPPER(item.keywordSearch) like :keywordSearch" + i);//忽略大小寫
					params.put("keywordSearch" + i, "%" + words[i].toUpperCase() + "%");//忽略大小寫
				}
				sb.append(" )");
			} 
		}
		sb.append(" order by item.itemId asc");
		
		return doQuery(sb.toString(), params, TsmpDpItems.class, pageSize);
	}
}