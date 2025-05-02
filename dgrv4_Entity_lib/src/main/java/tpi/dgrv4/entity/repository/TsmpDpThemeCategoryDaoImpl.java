package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import tpi.dgrv4.common.constant.TsmpDpDataStatus;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.entity.entity.sql.TsmpDpThemeCategory;

public class TsmpDpThemeCategoryDaoImpl extends BaseDao {
	// add custom methods here

	public List<TsmpDpThemeCategory> query_dpb0020Service(List<String> orgDescList //
			, String dataStatus, String[] words, TsmpDpThemeCategory lastRecord, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpDpThemeCategory A where 1 = 1");
		// 狀態
		if (dataStatus != null && !dataStatus.isEmpty()) {
			sb.append(" and A.dataStatus = :dataStatus");
			params.put("dataStatus", dataStatus);
		}
		// 組織
		sb.append(" and (A.orgId is null or length(A.orgId) = 0");
		if (!CollectionUtils.isEmpty(orgDescList)) {
			sb.append(" or A.orgId in (:orgDescList)");
			params.put("orgDescList", orgDescList);
		}
		sb.append(" )");
		// 分頁
		if (lastRecord != null) {
			sb.append(" and (");
			sb.append(" 	(A.createDateTime < :createDateTime and A.apiThemeId <> :apiThemeId)");
			sb.append(" 	or (A.createDateTime = :createDateTime and A.apiThemeId < :apiThemeId)");
			sb.append(" )");
			params.put("createDateTime", lastRecord.getCreateDateTime());
			params.put("apiThemeId", lastRecord.getApiThemeId());
		}
		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 	or UPPER(A.keywordSearch) like :lk_keyword" + i);
				params.put("lk_keyword" + i, "%" + words[i].toUpperCase() + "%");
				params.put("keyword" + i, words[i]);
			}
			sb.append(" 	or exists (");
			sb.append(" 		select 1 from TsmpDpApiTheme B");
			sb.append(" 		where B.refApiThemeId = A.apiThemeId");
			sb.append(" 		and exists (");
			sb.append(" 			select 1 from TsmpApi C");
			sb.append(" 			where C.apiUid = B.refApiUid");
			sb.append(" 			and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 				or C.apiKey = :keyword" + i);
				sb.append(" 				or C.apiSrc = :keyword" + i);
				sb.append(" 				or UPPER(C.moduleName) like :lk_keyword" + i);
				sb.append(" 				or UPPER(C.apiName) like :lk_keyword" + i);
				sb.append(" 				or UPPER(C.apiDesc) like :lk_keyword" + i);
			}
			/*
			sb.append(" 				or exists (");
			sb.append(" 					select 1 from TsmpApiModule D");
			sb.append(" 					where D.moduleName = C.moduleName");
			sb.append(" 					and D.active = true");
			sb.append(" 					and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 						or D.moduleVersion = :keyword" + i);
			}
			sb.append(" 					)");
			sb.append(" 				)");
			*/
			sb.append(" 			)");
			sb.append(" 		)		");
			sb.append(" 	)");
			sb.append(" )");
		}
		sb.append(" order by A.createDateTime desc, A.apiThemeId desc");

		return doQuery(sb.toString(), params, TsmpDpThemeCategory.class, pageSize);
	}

	public List<TsmpDpThemeCategory> query_dpb0055Service(List<String> orgDescList //
			, String[] words, TsmpDpThemeCategory lastRecord, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpDpThemeCategory A where 1 = 1");
		// 分頁
		if (lastRecord != null) {
			sb.append(" and A.apiThemeId > :apiThemeId");
			params.put("apiThemeId", lastRecord.getApiThemeId());
		}
		// 組織
		sb.append(" and (A.orgId is null or length(A.orgId) = 0");
		if (!CollectionUtils.isEmpty(orgDescList)) {
			sb.append(" or A.orgId in (:orgDescList)");
			params.put("orgDescList", orgDescList);
		}
		sb.append(" )");
		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 	or UPPER(A.keywordSearch) like :lk_keyword" + i);	// 主題名稱
				params.put("lk_keyword" + i, "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" 	or exists (");
			sb.append(" 		select 1 from TsmpDpFile B");
			sb.append(" 		where B.refId = A.apiThemeId");
			sb.append(" 		and B.refFileCateCode = :refFileCateCode");
			sb.append(" 		and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 			or UPPER(B.keywordSearch) like :lk_keyword" + i);
			}
			sb.append(" 		)");
			sb.append(" 	)");
			sb.append(" )");
			
			params.put("refFileCateCode", TsmpDpFileType.API_TH.value());
		}
		// 其他條件
		sb.append(" and A.dataStatus = :dataStatus");
		params.put("dataStatus", TsmpDpDataStatus.ON.value());

		sb.append(" order by A.apiThemeId asc");

		return doQuery(sb.toString(), params, TsmpDpThemeCategory.class, pageSize);
	}

	public List<TsmpDpThemeCategory> query_dpb0076Service(List<String> orgDescList, String[] words, //
			Long lastId, Integer pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpDpThemeCategory A where 1 = 1");
		// 分頁
		if (lastId != null) {
			sb.append(" and A.apiThemeId > :apiThemeId");
			params.put("apiThemeId", lastId);
		}

		// 組織
		sb.append(" and (A.orgId is null or length(A.orgId) = 0");
		if (!CollectionUtils.isEmpty(orgDescList)) {
			sb.append(" or A.orgId in (:orgDescList)");
			params.put("orgDescList", orgDescList);
		}
		sb.append(" )");
		
		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 	or UPPER(A.keywordSearch) like :lk_keyword" + i);	// 主題名稱
				params.put("lk_keyword" + i, "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" )");
			
		}

		sb.append(" order by A.apiThemeId asc");

		return doQuery(sb.toString(), params, TsmpDpThemeCategory.class, pageSize);
	}
}
