package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.TsmpDpApptRjobD;

public class TsmpDpApptRjobDDaoImpl extends BaseDao {
	// add custom methods here

	public TsmpDpApptRjobD queryNextRjobD(String apptRjobId, Long apptRjobDId) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select D1");
		sb.append(" from TsmpDpApptRjobD D1");
		sb.append(" where 1 = 1");
		sb.append(" and D1.apptRjobId = :apptRjobId");
		if (apptRjobDId != null) {
			sb.append(" and D1.sortBy > (");
			sb.append(" 	select D2.sortBy");
			sb.append(" 	from TsmpDpApptRjobD D2");
			sb.append(" 	where D2.apptRjobDId = :apptRjobDId");
			sb.append(" )");
			params.put("apptRjobDId", apptRjobDId);
		}
		sb.append(" order by D1.sortBy asc");
		params.put("apptRjobId", apptRjobId);
		
		List<TsmpDpApptRjobD> rjobDList = doQuery(sb.toString(), params, TsmpDpApptRjobD.class);
		if (rjobDList == null || rjobDList.isEmpty()) {
			return null;
		}
		return rjobDList.get(0);
	}

}