package tpi.dgrv4.entity.repository;

import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.vo.DPB0067SearchCriteria;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TsmpDpReqOrdermDaoImpl extends BaseDao {
	// add custom methods here

	public List<TsmpDpReqOrderm> query_dpb0067_queryReviewWork(DPB0067SearchCriteria sc) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select M");
		sb.append(" from TsmpDpReqOrderm M");
		sb.append(" where 1 = 1");
		// 分頁
		TsmpDpReqOrderm lastRecord = sc.getLastRecord();
		if (lastRecord != null) {
			sb.append(" and (");
			sb.append(" 	1 = 2");
			sb.append(" 	or (M.createDateTime < :p_createDateTime and M.reqOrdermId <> :p_reqOrdermId)");
			sb.append(" 	or (M.createDateTime = :p_createDateTime and M.reqOrderNo > :p_reqOrderNo)");
			sb.append(" 	or (M.createDateTime = :p_createDateTime and M.reqOrderNo = :p_reqOrderNo and M.reqOrdermId > :p_reqOrdermId)");
			sb.append(" )");
			params.put("p_createDateTime", lastRecord.getCreateDateTime());
			params.put("p_reqOrderNo", lastRecord.getReqOrderNo());
			params.put("p_reqOrdermId", lastRecord.getReqOrdermId());
		}
		// 必要條件 (組織)
		// 前台輸入的申請單沒有 orgId, 所以資料為 null, 一律視為可被 search 的資料
		sb.append(" and (M.orgId is null or length(M.orgId) = 0 or M.orgId in (:orgDescList))");
		// 必要條件 (申請日期)
		sb.append(" and (M.createDateTime between :startDateTime and :endDateTime)");
		// 必要條件 (簽核角色)
		sb.append(" and exists (");
		/* 2020.05.07; Kim; MariaDB不支援子查詢內參考一層以上的物件名稱
		sb.append(" 	select 1");
		sb.append(" 	from TsmpDpChkLayer L");
		sb.append(" 	where L.reviewType = M.reqType");
		sb.append(" 	and L.layer = (");
		sb.append(" 		select MIN(S.layer)");
		sb.append(" 		from TsmpDpReqOrders S");
		sb.append(" 		where S.status = :status");
		sb.append(" 		and S.reqOrdermId = M.reqOrdermId");
		sb.append(" 		and S.procFlag = :procFlag");
		sb.append(" 	)");
		sb.append(" 	and L.status = :status");
		sb.append(" 	and L.roleId in (:roleIdList)");
		*/
		sb.append(" 	select 1");
		sb.append(" 	from TsmpDpChkLayer L");
		sb.append(" 	join TsmpDpReqOrders S");
		sb.append(" 	on L.layer = S.layer");
		sb.append(" 	where 1 = 1");
		sb.append(" 	and S.reqOrdermId = M.reqOrdermId");
		sb.append(" 	and S.status = :status");
		sb.append(" 	and S.procFlag = :procFlag");
		sb.append(" 	and L.reviewType = M.reqType");
		sb.append(" 	and L.status = :status");
		sb.append(" 	and L.roleId in (:roleIdList)");
		sb.append(" )");
		// 可選條件 (申請類型)
		if (!StringUtils.isEmpty(sc.getReqType())) {
			sb.append(" and M.reqType = :reqType");
			params.put("reqType", sc.getReqType());
			if (!StringUtils.isEmpty(sc.getReqSubtype())) {
				sb.append(" and M.reqSubtype = :reqSubtype");
				params.put("reqSubtype", sc.getReqSubtype());
			}
		}
		// 關鍵字
		String[] words = sc.getWords();
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 	or UPPER(M.keywordSearch) like :keyword" + i);	// 案件編號,申請說明
				params.put(("keyword" + i), "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" 	or exists (");
			sb.append(" 		select 1");
			sb.append(" 		from TsmpClient C");
			sb.append(" 		where C.clientId = M.clientId");
			sb.append(" 		and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 			or UPPER(C.clientName) like :keyword" + i);	// 申請者
			}
			sb.append(" 		)");
			sb.append(" 	)");
			sb.append(" 	or exists (");
			sb.append(" 		select 1");
			sb.append(" 		from TsmpUser U");
			sb.append(" 		where M.reqUserId is not null");
			sb.append(" 		and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 			or UPPER(U.userName) like :keyword" + i);	// 申請者
			}
			sb.append(" 		)");
			sb.append(" 	)");
			sb.append(" )");
		}
		sb.append(" order by M.createDateTime desc, M.reqOrderNo asc, M.reqOrdermId asc");
		
		params.put("orgDescList", sc.getOrgDescList());
		params.put("startDateTime", sc.getStartDate());
		params.put("endDateTime", sc.getEndDate());
		params.put("status", TsmpDpDataStatus.ON.value());
		params.put("procFlag", 1);
		params.put("roleIdList", sc.getRoleIdList());
		
		return doQuery(sb.toString(), params, TsmpDpReqOrderm.class, sc.getPageSize());
	}
	
	public List<TsmpDpReqOrderm> query_dpb0067_queryPersonal(DPB0067SearchCriteria sc) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select M");
		sb.append(" from TsmpDpReqOrderm M");
		sb.append(" where 1 = 1");
		// 分頁
		TsmpDpReqOrderm lastRecord = sc.getLastRecord();
		if (lastRecord != null) {
			sb.append(" and (");
			sb.append(" 	1 = 2");
			sb.append(" 	or (M.createDateTime < :p_createDateTime and M.reqOrdermId <> :p_reqOrdermId)");
			sb.append(" 	or (M.createDateTime = :p_createDateTime and M.reqOrderNo > :p_reqOrderNo)");
			sb.append(" 	or (M.createDateTime = :p_createDateTime and M.reqOrderNo = :p_reqOrderNo and M.reqOrdermId > :p_reqOrdermId)");
			sb.append(" )");
			params.put("p_createDateTime", lastRecord.getCreateDateTime());
			params.put("p_reqOrderNo", lastRecord.getReqOrderNo());
			params.put("p_reqOrdermId", lastRecord.getReqOrdermId());
		}
		// 必要條件 (申請者ID)
		// 後端登入者查詢個人案件時, 只需比對 reqUserId 即可, 前台所申請的案件, 應該是由審查者身分才能查到
		sb.append(" and (");
		/*
		sb.append(" 	1 = 2");
		sb.append(" 	or M.clientId = :clientId");
		if (!StringUtils.isEmpty(sc.getUserId())) {
			sb.append(" 	or M.reqUserId = :userId");
			params.put("userId", sc.getUserId());
		}
		*/
		sb.append(" 	M.reqUserId = :userId");
		sb.append(" )");
		// 必要條件 (申請日期)
		sb.append(" and (M.createDateTime between :startDateTime and :endDateTime)");
		// 可選條件 (申請類型)
		if (!StringUtils.isEmpty(sc.getReqType())) {
			sb.append(" and M.reqType = :reqType");
			params.put("reqType", sc.getReqType());
			if (!StringUtils.isEmpty(sc.getReqSubtype())) {
				sb.append(" and M.reqSubtype = :reqSubtype");
				params.put("reqSubtype", sc.getReqSubtype());
			}
		}
		// 關鍵字
		String[] words = sc.getWords();
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 	or UPPER(M.keywordSearch) like :keyword" + i);	// 案件編號,申請說明
				params.put(("keyword" + i), "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" )");
		}
		sb.append(" order by M.createDateTime desc, M.reqOrderNo asc, M.reqOrdermId asc");
		
		// params.put("clientId", sc.getClientId());
		params.put("userId", sc.getUserId());
		params.put("startDateTime", sc.getStartDate());
		params.put("endDateTime", sc.getEndDate());
		
		return doQuery(sb.toString(), params, TsmpDpReqOrderm.class, sc.getPageSize());
	}
	
	public List<TsmpDpReqOrderm> query_dpb0067_expired(Date queryStartDate) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select M");
		sb.append(" from TsmpDpReqOrderm M");
		sb.append(" where 1 = 1");
		sb.append(" and ( 1=2 ");
		sb.append("       or (M.updateDateTime IS NULL and M.createDateTime < :queryStartDate) ");
		sb.append("       or (M.updateDateTime IS NOT NULL and M.updateDateTime < :queryStartDate) ");
		sb.append("     ) ");
		
		
		params.put("queryStartDate", queryStartDate);
		
		return doQuery(sb.toString(), params, TsmpDpReqOrderm.class);
	}
	
	public List<TsmpDpReqOrderm> query_dpb0067_queryReviewHistory(DPB0067SearchCriteria sc) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select M");
		sb.append(" from TsmpDpReqOrderm M");
		sb.append(" where 1 = 1");
		// 分頁
		TsmpDpReqOrderm lastRecord = sc.getLastRecord();
		if (lastRecord != null) {
			sb.append(" and (");
			sb.append(" 	1 = 2");
			sb.append(" 	or (M.createDateTime < :p_createDateTime and M.reqOrdermId <> :p_reqOrdermId)");
			sb.append(" 	or (M.createDateTime = :p_createDateTime and M.reqOrderNo > :p_reqOrderNo)");
			sb.append(" 	or (M.createDateTime = :p_createDateTime and M.reqOrderNo = :p_reqOrderNo and M.reqOrdermId > :p_reqOrdermId)");
			sb.append(" )");
			params.put("p_createDateTime", lastRecord.getCreateDateTime());
			params.put("p_reqOrderNo", lastRecord.getReqOrderNo());
			params.put("p_reqOrdermId", lastRecord.getReqOrdermId());
		}
		// 必要條件 (組織)
		// 前台輸入的申請單沒有 orgId, 所以資料為 null, 一律視為可被 search 的資料
		sb.append(" and (M.orgId is null or length(M.orgId) = 0 or M.orgId in (:orgDescList))");
		// 必要條件 (申請日期)
		sb.append(" and (M.createDateTime between :startDateTime and :endDateTime)");
		// 必要條件 (當前關卡非登入者有權限簽核的關卡)
		sb.append(" and not exists (");
		sb.append(" 	select 1");
		sb.append(" 	from TsmpDpReqOrders S");
		sb.append(" 	inner join TsmpDpChkLayer L on S.layer = L.layer");
		sb.append(" 	where 1 = 1");
		sb.append(" 	and S.reqOrdermId = M.reqOrdermId");
		sb.append(" 	and S.status = :status");
		sb.append(" 	and S.procFlag = 1");
		sb.append(" 	and L.reviewType = M.reqType");
		sb.append(" 	and L.status = :status");
		sb.append(" 	and L.roleId in (:roleIdList)");
		sb.append(" )");
		sb.append(" and ( 1 = 2");
		// 必要條件 (曾經可以簽此單)
		sb.append(" 	or exists (");
		sb.append(" 		select 1");
		sb.append(" 		from TsmpDpReqOrders S1");
		sb.append(" 		inner join TsmpDpChkLayer L on S1.layer = L.layer");
		sb.append(" 		where 1 = 1");
		sb.append(" 		and S1.reqOrdermId = M.reqOrdermId");
		sb.append(" 		and L.reviewType = M.reqType");
		sb.append(" 		and L.status = :status");
		sb.append(" 		and (");
		sb.append(" 			S1.reqOrdersId < (");
		sb.append(" 				select MAX(S2.reqOrdersId)");
		sb.append(" 				from TsmpDpReqOrders S2");
		sb.append(" 				where 1 = 1");
		sb.append(" 				and S2.reqOrdermId = S1.reqOrdermId");
		sb.append(" 				and S2.procFlag = 1");
		sb.append(" 			) or");
		sb.append(" 			S1.reqOrdersId <= (");
		sb.append(" 				select MAX(S2.reqOrdersId)");
		sb.append(" 				from TsmpDpReqOrders S2");
		sb.append(" 				where 1 = 1");
		sb.append(" 				and S2.reqOrdermId = S1.reqOrdermId");
		sb.append(" 				and S2.procFlag = 0");
		sb.append(" 			)");
		sb.append(" 		)");
		sb.append(" 		and L.roleId in (:roleIdList)");
		sb.append(" 	)");
		// 必要條件 (之前曾經簽過)
		sb.append(" 	or exists (");
		sb.append(" 		select 1");
		sb.append(" 		from TsmpDpChkLog LOG");
		sb.append(" 		where 1 = 1");
		sb.append(" 		and LOG.reqOrdermId = M.reqOrdermId");
		sb.append(" 		and LOG.createUser = :userName");
		sb.append(" 	)");
		sb.append(" )");
		// 可選條件 (申請類型)
		if (!StringUtils.isEmpty(sc.getReqType())) {
			sb.append(" and M.reqType = :reqType");
			params.put("reqType", sc.getReqType());
			if (!StringUtils.isEmpty(sc.getReqSubtype())) {
				sb.append(" and M.reqSubtype = :reqSubtype");
				params.put("reqSubtype", sc.getReqSubtype());
			}
		}
		// 關鍵字
		String[] words = sc.getWords();
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 	or UPPER(M.keywordSearch) like :keyword" + i);	// 案件編號,申請說明
				params.put(("keyword" + i), "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" 	or exists (");
			sb.append(" 		select 1");
			sb.append(" 		from TsmpClient C");
			sb.append(" 		where C.clientId = M.clientId");
			sb.append(" 		and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 			or UPPER(C.clientName) like :keyword" + i);	// 申請者
			}
			sb.append(" 		)");
			sb.append(" 	)");
			sb.append(" 	or exists (");
			sb.append(" 		select 1");
			sb.append(" 		from TsmpUser U");
			sb.append(" 		where M.reqUserId is not null");
			sb.append(" 		and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 			or UPPER(U.userName) like :keyword" + i);	// 申請者
			}
			sb.append(" 		)");
			sb.append(" 	)");
			sb.append(" )");
		}
		sb.append(" order by M.createDateTime desc, M.reqOrderNo asc, M.reqOrdermId asc");
		
		params.put("orgDescList", sc.getOrgDescList());
		params.put("startDateTime", sc.getStartDate());
		params.put("endDateTime", sc.getEndDate());
		params.put("status", TsmpDpDataStatus.ON.value());
		params.put("roleIdList", sc.getRoleIdList());
		params.put("userName", sc.getUserName());
		
		return doQuery(sb.toString(), params, TsmpDpReqOrderm.class, sc.getPageSize());
	}

}
