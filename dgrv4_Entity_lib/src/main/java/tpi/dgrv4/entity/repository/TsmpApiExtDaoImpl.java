package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.jpql.TsmpApiExt;

public class TsmpApiExtDaoImpl extends BaseDao {
	// add custom methods here

	public TsmpApiExt queryByApiUid(String apiUid) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		
		sb.append(" select EXT");
		sb.append(" from TsmpApiExt EXT");
		sb.append(" where exists (");
		sb.append(" 	select 1");
		sb.append(" 	from TsmpApi API");
		sb.append(" 	where API.apiKey = EXT.apiKey");
		sb.append(" 	and API.moduleName = EXT.moduleName");
		sb.append(" 	and API.apiUid = :apiUid");
		sb.append(" )");
		params.put("apiUid", apiUid);
		
		List<TsmpApiExt> extList = doQuery(sb.toString(), params, TsmpApiExt.class);
		if (extList == null || extList.isEmpty()) {
			return null;
		}
		
		return extList.get(0);
	}

}
