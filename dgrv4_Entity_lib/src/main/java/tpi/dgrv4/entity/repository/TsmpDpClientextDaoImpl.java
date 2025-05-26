package tpi.dgrv4.entity.repository;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.constant.TsmpDpPublicFlag;
import tpi.dgrv4.common.constant.TsmpDpRegStatus;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpDpClientext;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.vo.DPB0006SearchCriteria;

public class TsmpDpClientextDaoImpl extends BaseDao {
	// add custom methods here

	public List<TsmpDpClientext> queryLikeRegStatus(List<String> regStatusList, //
			String[] keywords, String lastId, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpDpClientext A");
		sb.append(" where A.regStatus in (:regStatusList)");
		// 分頁
		if (lastId != null) {
			sb.append(" and A.clientId > :clientId");
			params.put("clientId", lastId);
		}
		// 關鍵字
		if (keywords != null && keywords.length > 0) {
			sb.append(" and ( 1 = 2");
			for(int i = 0; i < keywords.length; i++) {
				sb.append(" 	or UPPER(A.keywordSearch) like :lk_keyword" + i);	// 申請會員說明、會員資格審核備註
				sb.append(" 	or UPPER(A.clientId) like :lk_keyword" + i);	// Client ID
				sb.append(" 	or UPPER(A.refReviewUser) like :lk_keyword" + i);	// 審核人員(userName)
				params.put("lk_keyword" + i, ("%" + keywords[i] + "%").toUpperCase());
			}
			sb.append(" 	or exists (");
			sb.append(" 		select 1 from TsmpDpFile C");
			sb.append(" 		where C.refFileCateCode = :refFileCateCode");
			sb.append(" 		and C.refId = A.clientSeqId");
			sb.append(" 		and ( 1 = 2");
			for(int i = 0; i < keywords.length; i++) {
				sb.append(" 			or UPPER(C.fileName) like :lk_keyword" + i);	// 附件清單
			}
			sb.append(" 		)");
			sb.append(" 	)");
			sb.append(" )");
		}
		sb.append(" order by A.clientId asc");

		params.put("regStatusList", regStatusList);
		params.put("refFileCateCode", TsmpDpFileType.MEMBER_APPLY.value());

		return doQuery(sb.toString(), params, TsmpDpClientext.class, pageSize);
	}

	public List<TsmpDpClientext> queryLikeRegStatusBetween(DPB0006SearchCriteria cri) {

		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpDpClientext A");
		sb.append(" where A.regStatus in (:regStatusList)");
		/* 2020/07/03 允許不使用日期區間查詢
		sb.append(" and A.updateDateTime between :startDate and :endDate");
		*/
		if (cri.getStartDate() != null) {
			sb.append(" and A.updateDateTime >= :startDate");
			params.put("startDate", cri.getStartDate());
		}
		if (cri.getEndDate() != null) {
			sb.append(" and A.updateDateTime <= :endDate");
			params.put("endDate", cri.getEndDate());
		}
		// 分頁
		/* 20200902; Kim; 修正: 以關鍵字搜尋時, 按下"顯示更多"會重複出現相同資料
		if (cri.getLastClientId() != null && cri.getLastUpdateDateTime() != null) {
			sb.append(" and (");
			sb.append(" 	A.updateDateTime < :lastRecordUpdateDateTime and A.clientId <> :lastRecordClientId");
			sb.append(" 	or");
			sb.append(" 	(A.updateDateTime = :lastRecordUpdateDateTime and A.clientId > :lastRecordClientId)");
			sb.append(" )");
			params.put("lastRecordUpdateDateTime", cri.getLastUpdateDateTime());
			params.put("lastRecordClientId", cri.getLastClientId());
		}
		*/
		if (!StringUtils.isEmpty(cri.getLastClientId())) {
				sb.append(" and (");
				sb.append(" 	(A.clientId > :lastRecordClientId)");
				sb.append(" )");
				params.put("lastRecordClientId", cri.getLastClientId());
		}
		// 關鍵字
		String[] keywords = cri.getWords();
		if (keywords != null && keywords.length > 0) {
			sb.append(" and ( 1 = 2");
			for(int i = 0; i < keywords.length; i++) {
				sb.append(" 	or UPPER(A.keywordSearch) like :lk_keyword" + i);	// 申請會員說明、會員資格審核備註
				sb.append(" 	or UPPER(A.clientId) like :lk_keyword" + i);	// Client ID
				sb.append(" 	or UPPER(A.refReviewUser) like :lk_keyword" + i);	// 審核人員
				params.put("lk_keyword" + i, ("%" + keywords[i] + "%").toUpperCase());
			}
			sb.append(" 	or exists (");
			sb.append(" 		select 1 from TsmpDpFile C");
			sb.append(" 		where C.refFileCateCode = :refFileCateCode");
			sb.append(" 		and C.refId = A.clientSeqId");
			sb.append(" 		and ( 1 = 2");
			for(int i = 0; i < keywords.length; i++) {
				sb.append(" 			or UPPER(C.fileName) like :lk_keyword" + i);	// 附件清單
			}
			sb.append(" 		)");
			sb.append(" 	)");
			sb.append(" )");
			params.put("refFileCateCode", TsmpDpFileType.MEMBER_APPLY.value());
		}
		sb.append(" order by A.clientId asc");

		params.put("regStatusList", cri.getRegStatusList());

		return doQuery(sb.toString(), params, TsmpDpClientext.class, cri.getPageSize());
	}

	public List<TsmpDpClientext> query_dpb0006Job(Date expDate) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT A FROM TsmpDpClientext A");
		sb.append(" WHERE A.regStatus = :regStatus");
		sb.append(" AND A.updateDateTime < :expDate");
		
		params.put("regStatus", TsmpDpRegStatus.RETURN.value());
		params.put("expDate", expDate);
		
		return doQuery(sb.toString(), params, TsmpDpClientext.class);
	}

	public List<TsmpDpClientext> query_dpb0006Job_inconsistentExt() {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT A FROM TsmpDpClientext A");
		sb.append(" WHERE NOT EXISTS (");
		sb.append(" 	SELECT 1");
		sb.append(" 	FROM TsmpClient B");
		sb.append(" 	WHERE B.clientId = A.clientId");
		sb.append(" )");

		return doQuery(sb.toString(), Collections.emptyMap(), TsmpDpClientext.class);
	}

	public Boolean hasApiAuthority(String clientId, String apiUid) {
		/* 2020.07.16; Kim; for v3.6.1 hotfix 先修一版, 待 v3.8 再調一次
		// 找出此API允許被哪些publicFlags查到
		List<String> apiPublicFlags = getApiPublicFlags(apiUid);

		// 找出client允許查到的api publicFlag有哪些
		List<String> clientPublicFlags = getClientPublicFlags(clientId);
		
		// 確認 API 的 publicFlag 與 items 的 params (client publicFlags) 是否符合
		for(String clientPublicFlag : clientPublicFlags) {
			for(String apiPublicFlag : apiPublicFlags) {
				if (clientPublicFlag.equals(apiPublicFlag)) {
					return true;
				}
			}
		}
		return false;
		*/

		// 找出此API所設定的權限
		String apiPublicFlag = getApiPublicFlag(apiUid);
		
		// 找出Client所擁有的權限
		String cliPublicFlag = getCliPublicFlag(clientId);
		
		/* 20200803; Kim; v3.8 增加'-1'代表對內
		// 找出client允許查到的api publicFlag有哪些
		List<String> clientPublicFlags = getClientPublicFlags(clientId);
		
		for(String clientPublicFlag : clientPublicFlags) {
			if (clientPublicFlag.equals(apiPublicFlag)) {
				return true;
			}
		}
		
		return false;
		*/
		
		// 以Client擁有的權限去對應Api所設定的權限
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select ITM");
		sb.append(" from TsmpDpItems ITM");
		sb.append(" where ITM.itemNo = :itemNo");
		sb.append(" and ITM.subitemNo = :cliPublicFlag");
		sb.append(" and ( 1 = 2");
		sb.append(" 	or param1 = :apiPublicFlag");
		sb.append(" 	or param2 = :apiPublicFlag");
		sb.append(" 	or param3 = :apiPublicFlag");
		sb.append(" 	or param4 = :apiPublicFlag");
		sb.append(" 	or param5 = :apiPublicFlag");
		sb.append(" )");
		params.put("itemNo", "API_AUTHORITY");	// API露出權限
		params.put("cliPublicFlag", cliPublicFlag);
		params.put("apiPublicFlag", apiPublicFlag);
		List<TsmpDpItems> itemList = doQuery(sb.toString(), params, TsmpDpItems.class);
		return !(itemList == null || itemList.isEmpty());
	}

	/* 2020.07.16; Kim; for v3.6.1 hotfix 先修一版, 待 v3.8 再調一次
	private List<String> getApiPublicFlags(String apiUid) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select API from TsmpApi API where API.apiUid = :apiUid");
		params.put("apiUid", apiUid);
		List<TsmpApi> apiList = doQuery(sb.toString(), params, TsmpApi.class);
		
		List<String> apiPublicFlags = new ArrayList<>();
		if (apiList != null && !apiList.isEmpty()) {
			TsmpApi api = apiList.get(0);
			String apiPublicFlag = api.getPublicFlag();
			if (!StringUtils.isEmpty(apiPublicFlag)) {
				apiPublicFlags.add(apiPublicFlag);
				return apiPublicFlags;
			}
		}
		// 查無API或未設定publicFlag則一律當作"對內"的API, 只有權限為"0"或"2"才可看到
		apiPublicFlags.add(TsmpDpPublicFlag.ALL.value());
		apiPublicFlags.add(TsmpDpPublicFlag.PRIVATE.value());
		return apiPublicFlags;
	}

	private List<String> getClientPublicFlags(String clientId) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select ITM");
		sb.append(" from TsmpDpItems ITM");
		sb.append(" where ITM.itemNo = :itemNo");
		sb.append(" and ITM.subitemNo = (");
		sb.append(" 	select EXT.publicFlag");
		sb.append(" 	from TsmpDpClientext EXT");
		sb.append(" 	where EXT.clientId = :clientId");
		sb.append(" )");
		params.put("itemNo", "API_AUTHORITY");	// API露出權限
		params.put("clientId", clientId);
		List<TsmpDpItems> itemList = doQuery(sb.toString(), params, TsmpDpItems.class);
		
		List<String> publicFlags = new ArrayList<>();
		if (itemList == null || itemList.isEmpty()) {
			// 如果 client 沒有設定 publicFlag, 則預設只能查到"對外"的API
			publicFlags.add(TsmpDpPublicFlag.PUBLIC.value());
		} else {
			TsmpDpItems items = itemList.get(0);
			setPublicFlags(publicFlags, items.getParam1(), items.getParam2(), items.getParam3());
		}
		return publicFlags;
	}
	*/

	private String getApiPublicFlag(String apiUid) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select API from TsmpApi API where API.apiUid = :apiUid");
		params.put("apiUid", apiUid);
		List<TsmpApi> apiList = doQuery(sb.toString(), params, TsmpApi.class);
		
		// 找不到API或是未設定publicFlag就預設權限為2
		if (apiList == null || apiList.isEmpty() || StringUtils.isEmpty(apiList.get(0).getPublicFlag())) {
			/* 20200803; Kim; v3.8 增加'-1'代表對內
			return TsmpDpPublicFlag.PRIVATE.value();
			*/
			return TsmpDpPublicFlag.EMPTY.value();
		}
		
		return apiList.get(0).getPublicFlag();
	}

	private String getCliPublicFlag(String clientId) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select EXT");
		sb.append(" from TsmpDpClientext EXT");
		sb.append(" where EXT.clientId = :clientId");
		params.put("clientId", clientId);
		List<TsmpDpClientext> extList = doQuery(sb.toString(), params, TsmpDpClientext.class);
		// 找不到Client就預設權限為"對外", 沒有設定publicFlag時才預設為"-1"
		if (extList == null || extList.isEmpty()) {
			return TsmpDpPublicFlag.PUBLIC.value();
		} else if (StringUtils.isEmpty(extList.get(0).getPublicFlag())) {
			return TsmpDpPublicFlag.EMPTY.value();
		} else {
			return extList.get(0).getPublicFlag();
		}
	}

	/* 20200803; Kim; v3.8 增加'-1'代表對內
	private List<String> getClientPublicFlags(String clientId) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select ITM");
		sb.append(" from TsmpDpItems ITM");
		sb.append(" where ITM.itemNo = :itemNo");
		sb.append(" and ITM.subitemNo = (");
		sb.append(" 	select EXT.publicFlag");
		sb.append(" 	from TsmpDpClientext EXT");
		sb.append(" 	where EXT.clientId = :clientId");
		sb.append(" )");
		params.put("itemNo", "API_AUTHORITY");	// API露出權限
		params.put("clientId", clientId);
		List<TsmpDpItems> itemList = doQuery(sb.toString(), params, TsmpDpItems.class);
		
		List<String> publicFlags = new ArrayList<>();
		if (itemList == null || itemList.isEmpty()) {
			// 如果 client 沒有設定 publicFlag, 則預設權限為2, 再查一次
			params.clear();
			sb.setLength(0);
			sb.append(" select ITM");
			sb.append(" from TsmpDpItems ITM");
			sb.append(" where ITM.itemNo = :itemNo");
			sb.append(" and ITM.subitemNo = :flag");
			params.put("itemNo", "API_AUTHORITY");	// API露出權限
			params.put("flag", TsmpDpPublicFlag.PRIVATE.value());
			itemList = doQuery(sb.toString(), params, TsmpDpItems.class);
			if (itemList == null || itemList.isEmpty()) {
				// 再真的查不到, 就預設只能看權限為2的API
				publicFlags.add(TsmpDpPublicFlag.PRIVATE.value());
			} else {
				TsmpDpItems items = itemList.get(0);
				setPublicFlags(publicFlags, items.getParam1(), items.getParam2(), items.getParam3());
			}
		} else {
			TsmpDpItems items = itemList.get(0);
			setPublicFlags(publicFlags, items.getParam1(), items.getParam2(), items.getParam3());
		}
		return publicFlags;
	}

	private void setPublicFlags(List<String> publicFlags, String ... params) {
		if (params != null) {
			for(String param : params) {
				if (!StringUtils.isEmpty(param)) {
					publicFlags.add(param);
				}
			}
		}
	}
	*/

}
