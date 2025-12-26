package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import tpi.dgrv4.entity.entity.jpql.TsmpDcModule;

public class TsmpDcModuleDaoImpl extends BaseDao {
	// add custom methods here
	public List<TsmpDcModule> queryByAA0420Service(List<Long> moduleIdList){
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		
		sb.append(" SELECT dm");
		sb.append(" FROM TsmpDcModule dm ");
		sb.append(" WHERE dm.moduleId IN :moduleIdList ");
		params.put("moduleIdList", moduleIdList);
		
		sb.append(" AND EXISTS ( ");
		sb.append(" 	SELECT 1 ");
		sb.append(" 	FROM TsmpDc d ");
		sb.append(" 	WHERE d.dcId = dm.dcId ");
		sb.append(" 	AND d.active = 1 ");
		sb.append(" ) ");
		
		return doQuery(sb.toString(), params, TsmpDcModule.class);
	}

	public long query_AA0404Service_01(String moduleName, String dcCode) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT COUNT(1)");
		sb.append(" FROM TsmpDcModule DM");
		sb.append(" WHERE 1 = 1");
		sb.append(" AND EXISTS (");
		sb.append(" 	SELECT 1");
		sb.append(" 	FROM TsmpApiModule M");
		sb.append(" 	WHERE M.id = DM.moduleId");
		sb.append(" 	AND M.moduleName = :moduleName");
		sb.append(" 	AND M.v2Flag = :v2Flag");
		sb.append(" )");
		sb.append(" AND EXISTS (");
		sb.append(" 	SELECT 1");
		sb.append(" 	FROM TsmpDc D");
		sb.append(" 	WHERE D.dcId = DM.dcId");
		sb.append(" 	AND D.dcCode = :dcCode");
		sb.append(" 	AND D.active = :active");
		sb.append(" )");
		params.put("moduleName", moduleName);
		params.put("v2Flag", 2);
		params.put("dcCode", dcCode);
		params.put("active", Boolean.TRUE);
		List<Long> cntList = doQuery(sb.toString(), params, Long.class);
		if (CollectionUtils.isEmpty(cntList)) {
			return -1;
		} else {
			return cntList.get(0).longValue();
		}
	}
}
