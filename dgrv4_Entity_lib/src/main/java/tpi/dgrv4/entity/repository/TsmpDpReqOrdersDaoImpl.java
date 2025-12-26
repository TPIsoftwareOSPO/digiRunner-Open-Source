package tpi.dgrv4.entity.repository;

import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.constant.TsmpDpReqReviewStatus;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TsmpDpReqOrdersDaoImpl extends BaseDao {
	// add custom methods here

	public TsmpDpReqOrders queryCurrentStatus(Long reqOrdermId) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select S");
		sb.append(" from TsmpDpReqOrders S");
		sb.append(" where S.reqOrdermId = :reqOrdermId");
		sb.append(" and S.status = :status");
		sb.append(" and S.procFlag is not null");
		params.put("reqOrdermId", reqOrdermId);
		params.put("status", TsmpDpDataStatus.ON.value());
		List<TsmpDpReqOrders> sList = doQuery(sb.toString(), params, TsmpDpReqOrders.class);
		if (sList == null || sList.isEmpty()) {
			return null;
		}
		
		return sList.get(0);
	}

	public TsmpDpReqOrders queryNextCheckPoint(Long reqOrdermId) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select S");
		sb.append(" from TsmpDpReqOrders S");
		sb.append(" where S.reqOrdermId = :reqOrdermId");
		sb.append(" and S.status = :status");
		sb.append(" and S.procFlag = :procFlag");
		params.put("reqOrdermId", reqOrdermId);
		params.put("status", TsmpDpDataStatus.ON.value());
		params.put("procFlag", 1);
		List<TsmpDpReqOrders> sList = doQuery(sb.toString(), params, TsmpDpReqOrders.class);
		if (sList == null || sList.isEmpty()) {
			return null;
		}
		
		return sList.get(0);
	}

	// 未有任何簽核存在簽核清單中, 退回後未有任何新簽核仍可結案
	public Boolean isEndable(Long reqOrdermId) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		// 確認是否被退回
		sb.append(" select S");
		sb.append(" from TsmpDpReqOrders S");
		sb.append(" where S.reqOrdermId = :reqOrdermId");
		sb.append(" and S.status = :status");
		sb.append(" and S.procFlag = :procFlag");
		sb.append(" and S.reviewStatus = :rs_return");
		params.put("reqOrdermId", reqOrdermId);
		params.put("status", TsmpDpDataStatus.ON.value());
		params.put("procFlag", Integer.valueOf(0));
		params.put("rs_return", TsmpDpReqReviewStatus.RETURN.value());
		List<TsmpDpReqOrders> sList = doQuery(sb.toString(), params, TsmpDpReqOrders.class);
		if (sList != null && !sList.isEmpty()) {
			return true;
		}
		
		params.clear();
		sb.setLength(0);
		
		// 確認是否被簽核過
		sb.append(" select S");
		sb.append(" from TsmpDpReqOrders S");
		sb.append(" where S.reqOrdermId = :reqOrdermId");
		sb.append(" and S.status = :status");
		sb.append(" and (");
		sb.append(" 	1 = 2");
		// 除申請者以外有其他人簽過的狀態
		sb.append(" 	or (S.layer <> :applierLayer and S.reviewStatus not in (:waitingStatus))");
		// 申請者自己是最後一關，且已結束
		sb.append(" 	or (S.layer = :applierLayer and S.procFlag = :procFlag)");
		sb.append(" )");
		params.put("reqOrdermId", reqOrdermId);
		params.put("status", TsmpDpDataStatus.ON.value());
		params.put("applierLayer", Integer.valueOf(0));
		params.put("waitingStatus", Arrays.asList(new String[] {
			TsmpDpReqReviewStatus.WAIT1.value(),
			TsmpDpReqReviewStatus.WAIT2.value()
		}));
		params.put("procFlag", Integer.valueOf(0));
		sList = doQuery(sb.toString(), params, TsmpDpReqOrders.class);
		if (sList == null || sList.isEmpty()) {
			return true;
		}

		return false;
	}

	/**
	 * 判斷使用者是否"曾經"可簽此單
	 * @param reqOrdermId
	 * @param userName
	 * @return
	 */
	public Boolean wasAbleToSign(Long reqOrdermId, String userName) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select S");
		sb.append(" from TsmpDpReqOrders S");
		sb.append(" where 1 = 1");
		sb.append(" and S.reqOrdermId = :reqOrdermId");
		sb.append(" and ( 1 = 2");
		sb.append(" 	or S.reqOrdersId < (");
		sb.append(" 		select MAX(S1.reqOrdersId)");
		sb.append(" 		from TsmpDpReqOrders S1");
		sb.append(" 		where 1 = 1");
		sb.append(" 		and S1.reqOrdermId = S.reqOrdermId");
		sb.append(" 		and S1.procFlag = 1");
		sb.append(" 	)");
		sb.append(" 	or S.reqOrdersId <= (");
		sb.append(" 		select MAX(S1.reqOrdersId)");
		sb.append(" 		from TsmpDpReqOrders S1");
		sb.append(" 		where 1 = 1");
		sb.append(" 		and S1.reqOrdermId = S.reqOrdermId");
		sb.append(" 		and S1.procFlag = 0");
		sb.append(" 	)");
		sb.append(" )");
		sb.append(" and exists (");
		sb.append(" 	select 1");
		sb.append(" 	from TsmpDpChkLayer L");
		sb.append(" 	where 1 = 1");
		sb.append(" 	and L.layer = S.layer");
		sb.append(" 	and L.status = :status");
		sb.append(" 	and L.reviewType = (");
		sb.append(" 		select M.reqType");
		sb.append(" 		from TsmpDpReqOrderm M");
		sb.append(" 		where M.reqOrdermId = :reqOrdermId");
		sb.append(" 	)");
		sb.append(" 	and L.roleId in (");
		sb.append(" 		select A.authority");
		sb.append(" 		from Authorities A");
		sb.append(" 		where A.username = :userName");
		sb.append(" 	)");
		sb.append(" )");
		params.put("reqOrdermId", reqOrdermId);
		params.put("status", TsmpDpDataStatus.ON.value());
		params.put("userName", userName);
		
		List<TsmpDpReqOrders> sList = doQuery(sb.toString(), params, TsmpDpReqOrders.class);
		return !(sList == null || sList.isEmpty());
	}

	public List<TsmpDpReqOrders> query_AA0303Service_01(String apiUid) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		
		sb.append(" SELECT S");
		sb.append(" FROM TsmpDpReqOrders S");
		sb.append(" WHERE S.status = '1'");	// 啟用
		sb.append(" AND S.procFlag = 1");	// 流程尚未結束
		sb.append(" AND ( 1 = 2");
		// 用戶授權API
		sb.append("     OR EXISTS (");
		sb.append("         SELECT 1");
		sb.append("         FROM TsmpDpReqOrderd1 D1");
		sb.append("         WHERE D1.refReqOrdermId = S.reqOrdermId");
		sb.append("         AND D1.apiUid = :apiUid");
		sb.append("     )");
		// API上下架
		sb.append("     OR EXISTS (");
		sb.append("         SELECT 1");
		sb.append("         FROM TsmpDpReqOrderd2 D2");
		sb.append("         WHERE D2.refReqOrdermId = S.reqOrdermId");
		sb.append("         AND D2.apiUid = :apiUid");
		sb.append("     )");
		// OpenAPI Key申請
		sb.append("     OR EXISTS (");
		sb.append("         SELECT 1");
		sb.append("         FROM TsmpDpReqOrderd5 D5");
		sb.append("         WHERE D5.refReqOrdermId = S.reqOrdermId");
		sb.append("         AND EXISTS (");
		sb.append("             SELECT 1");
		sb.append("             FROM TsmpDpReqOrderd5d D5D");
		sb.append("             WHERE D5D.refReqOrderd5Id = D5.reqOrderd5Id");
		sb.append("             AND D5D.refApiUid = :apiUid");
		sb.append("         )");
		sb.append("     )");
		sb.append(" )");

		params.put("apiUid", apiUid);
		
		return doQuery(sb.toString(), params, TsmpDpReqOrders.class);
	}

}
