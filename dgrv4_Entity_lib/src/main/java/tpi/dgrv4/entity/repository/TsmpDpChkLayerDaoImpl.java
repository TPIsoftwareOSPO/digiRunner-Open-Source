package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.entity.entity.jpql.TsmpDpChkLayer;

public class TsmpDpChkLayerDaoImpl extends BaseDao {
	// add custom methods here

	public List<Integer> queryForCreateReqOrders(String reviewType) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select A.layer");
		sb.append(" from TsmpDpChkLayer A");
		sb.append(" where A.reviewType = :reviewType");
		sb.append(" and A.status = :status");
		sb.append(" group by A.layer");
		sb.append(" order by A.layer");
		params.put("reviewType", reviewType);
		params.put("status", TsmpDpDataStatus.ON.value());
		
		return doQuery(sb.toString(), params, Integer.class);
	}

	public Boolean isUserAuthorized(String reviewType, Integer layer, String username) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		
		sb.append(" select A");
		sb.append(" from TsmpDpChkLayer A");
		sb.append(" where A.reviewType = :reviewType");
		sb.append(" and A.layer = :layer");
		sb.append(" and A.status = :status");
		sb.append(" and A.roleId in (");
		sb.append(" 	select B.authority");
		sb.append(" 	from Authorities B");
		sb.append(" 	where B.username = :username");
		sb.append(" )");
		
		params.put("reviewType", reviewType);
		params.put("layer", layer);
		params.put("status", TsmpDpDataStatus.ON.value());
		params.put("username", username);
		
		List<TsmpDpChkLayer> layers = doQuery(sb.toString(), params, TsmpDpChkLayer.class);
		if (layers == null || layers.isEmpty()) {
			return false;
		}

		return true;
	}
	
	public List<TsmpDpChkLayer> queryByReviewTypeAndRoleIdList(String reviewType, List<String> roleIdList) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpDpChkLayer A where 1 = 1");
		sb.append(" and A.reviewType = :reviewType");
		sb.append(" and A.roleId in (:roleIdList)");
		
		params.put("reviewType", reviewType);
		params.put("roleIdList", roleIdList);

		return doQuery(sb.toString(), params, TsmpDpChkLayer.class, Integer.MAX_VALUE);
	}
}
