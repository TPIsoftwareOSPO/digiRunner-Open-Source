package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.DgrAcIdpUser;

public class DgrAcIdpUserDaoImpl extends BaseDao {

	public List<DgrAcIdpUser> queryAllByOrgList(List<String> orgDescList) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select U");
		sb.append(" from DgrAcIdpUser U");
		sb.append(" where 1 = 1");
		sb.append(" and U.orgId in (:orgDescList)");
		sb.append(" order by U.acIdpUserId asc");
		params.put("orgDescList", orgDescList);
		return doQuery(sb.toString(), params, DgrAcIdpUser.class);
	}
}
