package tpi.dgrv4.entity.repository;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import jakarta.persistence.Query;
import tpi.dgrv4.common.constant.TsmpDpPublicFlag;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
import tpi.dgrv4.entity.vo.AA0301SearchCriteria;
import tpi.dgrv4.entity.vo.DPB0018SearchCriteria;

public class TsmpApiDaoImpl extends BaseDao {

	public int deleteNonSpecifiedContent(List<AbstractMap.SimpleEntry<String, String>> list) {

		if (list == null) {
			return 0;
		}

		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();

		sb.append(" DELETE FROM TsmpApi a ");
		sb.append(" WHERE NOT ( a.apiKey = '' AND  a.moduleName ='' ");

		for (int i = 0; i < list.size(); i++) {

			SimpleEntry<String, String> entry = list.get(i);
			String apiKey = entry.getKey();
			String moduleName = entry.getValue();

			String paramsApiKey = "apiKey" + i;
			String paramsModuleName = "moduleName" + i;

			sb.append(" OR ( a.apiKey =:" + paramsApiKey);
			sb.append(" AND a.moduleName =:" + paramsModuleName + " )");

			params.put(paramsApiKey, apiKey);
			params.put(paramsModuleName, moduleName);
		}

		sb.append(" )");

		Query query = getEntityManager().createQuery(sb.toString());

		for (Map.Entry<String, Object> param : params.entrySet()) {
			query.setParameter(param.getKey(), param.getValue());
		}

		return query.executeUpdate();
	}

	// add custom methods here

	public List<TsmpApi> query_dpb0018Service(DPB0018SearchCriteria cri) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpApi A");
		sb.append(" where A.apiStatus = :apiStatus");

		// 分頁
		if (cri.getLastId() != null) {
			sb.append(" and (");
			sb.append(" 	A.apiKey > :apiKey");
			sb.append(" 	or (A.apiKey = :apiKey and A.moduleName > :moduleName)");
			sb.append(" )");
			params.put("apiKey", cri.getLastId().getApiKey());
			params.put("moduleName", cri.getLastId().getModuleName());
		}

		// 公開/私有
		/*
		 * if (TsmpDpPublicFlag.PRIVATE.value().equals(cri.getPublicFlag())) {
		 * sb.append(" and (A.publicFlag is null or A.publicFlag = :publicFlag)"); }
		 * else { sb.append(" and A.publicFlag = :publicFlag"); }
		 */
		sb.append(" and exists (");
		sb.append(" 	select 1");
		sb.append(" 	from TsmpDpItems IT");
		sb.append(" 	where IT.itemNo = :itemNo");
		sb.append(" 	and IT.subitemNo = :publicFlag");
		sb.append(" 	and ( 1 = 2");
		// API有設定public_flag
		sb.append(" 		or (");
		sb.append(" 			A.publicFlag is not null and ( 1 = 2");
		sb.append(" 				or IT.param1 = A.publicFlag");
		sb.append(" 				or IT.param2 = A.publicFlag");
		sb.append(" 				or IT.param3 = A.publicFlag");
		sb.append(" 				or IT.param4 = A.publicFlag");
		sb.append(" 				or IT.param5 = A.publicFlag");
		sb.append(" 			)");
		sb.append(" 		)");
		// API未設定public_flag
		sb.append(" 		or (");
		/*
		 * 20200804; Kim; v3.8 增加'-1'代表對內
		 * sb.append(" 			A.publicFlag is null and :publicFlag in (:availableFlags)"
		 * );
		 */
		sb.append(" 			A.publicFlag is null and ( 1 = 2");
		sb.append(" 				or IT.param1 = :emptyFlag");
		sb.append(" 				or IT.param2 = :emptyFlag");
		sb.append(" 				or IT.param3 = :emptyFlag");
		sb.append(" 				or IT.param4 = :emptyFlag");
		sb.append(" 				or IT.param5 = :emptyFlag");
		sb.append(" 			)");
		sb.append(" 		)");
		sb.append(" 	)");
		sb.append(" )");

		// 組織
		sb.append(" and (A.orgId is null or LENGTH(A.orgId) = 0");
		List<String> orgDescList = cri.getOrgDescList();
		if (!CollectionUtils.isEmpty(orgDescList)) {
			sb.append(" or A.orgId in (:orgDescList)");
			params.put("orgDescList", orgDescList);
		}
		sb.append(" )");

		// 關鍵字
		if (cri.getWords() != null && cri.getWords().length > 0) {
			sb.append(" and ( 1 = 2");
			for (int i = 0; i < cri.getWords().length; i++) {
				sb.append(" 	or UPPER(A.apiKey) like :lk_keyword" + i);
				sb.append(" 	or A.apiSrc = :keyword" + i);
				sb.append(" 	or UPPER(A.moduleName) like :lk_keyword" + i);
				sb.append(" 	or UPPER(A.apiName) like :lk_keyword" + i);
				sb.append(" 	or UPPER(A.apiDesc) like :lk_keyword" + i);
				sb.append(" 	or UPPER(A.apiUid) like :lk_keyword" + i);
				params.put("lk_keyword" + i, "%" + cri.getWords()[i].toUpperCase() + "%");
				params.put("keyword" + i, cri.getWords()[i]);
			}
			/*
			 * sb.append(" 	or exists ("); sb.append(" 		select 1 from TsmpApiModule B");
			 * sb.append(" 		where B.moduleName = A.moduleName");
			 * sb.append(" 		and (1 = 2"); sb.append(" 			or (B.active = true)");
			 * sb.append(" 			or exists ("); sb.append(" 				select 1");
			 * sb.append(" 				from TsmpDcModule D");
			 * sb.append(" 				where D.moduleId = B.id");
			 * sb.append(" 				and exists (");
			 * sb.append(" 					select 1 from TsmpDc E");
			 * sb.append(" 					where E.dcId = D.dcId");
			 * sb.append(" 					and E.active = true");
			 * sb.append(" 				)"); sb.append(" 			)");
			 * sb.append(" 		)"); sb.append(" 		and ( 1 = 2"); for(int i = 0; i <
			 * cri.getWords().length; i++) {
			 * sb.append(" 			or B.moduleVersion = :keyword" + i); }
			 * sb.append(" 		)"); sb.append(" 	)"); // apiSrc = 'N'
			 * sb.append(" 	or exists (");
			 * sb.append(" 		select 1 from TsmpnApiModule C");
			 * sb.append(" 		where C.moduleName = A.moduleName and C.active = true");
			 * sb.append(" 		and ( 1 = 2"); for(int i = 0; i < cri.getWords().length;
			 * i++) { sb.append(" 			or C.moduleVersion = :keyword" + i); }
			 * sb.append(" 		)"); sb.append(" 	)");
			 */
			sb.append(" )");
		}

		sb.append(" order by A.apiKey asc, A.moduleName asc");

		params.put("apiStatus", cri.getApiStatus());
		params.put("itemNo", "API_AUTHORITY");
		params.put("publicFlag", cri.getPublicFlag());
		/*
		 * 20200804; Kim; v3.8 增加'-1'代表對內 params.put("availableFlags", Arrays.asList(new
		 * String[] { TsmpDpPublicFlag.ALL.value(), TsmpDpPublicFlag.PRIVATE.value()
		 * }));
		 */
		params.put("emptyFlag", TsmpDpPublicFlag.EMPTY.value());

		return doQuery(sb.toString(), params, TsmpApi.class, cri.getPageSize());
	}

	public List<TsmpApi> query_dpb0075Service(TsmpApiId lastId, List<String> orgDescList, String dpStatus,
			String[] words, int pageSize) {

		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpApi A");
		sb.append(" where 1 = 1");

		// 分頁
		if (lastId != null) {
			sb.append(" and (");
			sb.append(" 	A.apiKey > :apiKey");
			sb.append(" 	or (A.apiKey = :apiKey and A.moduleName > :moduleName)");
			sb.append(" )");
			params.put("apiKey", lastId.getApiKey());
			params.put("moduleName", lastId.getModuleName());
		}

		// 組織
		sb.append(" and (A.orgId is null or LENGTH(A.orgId) = 0");
		if (!CollectionUtils.isEmpty(orgDescList)) {
			sb.append(" or A.orgId in (:orgDescList)");
			params.put("orgDescList", orgDescList);
		}
		sb.append(" )");

		// 上架或下架
		if ("1".contentEquals(dpStatus)) {// 上架
			sb.append(" and exists(");
			sb.append(" 	select 1 from TsmpApiExt B");
			sb.append(" 	where B.dpStatus = :dpStatus");
			sb.append(" 	and B.apiKey = A.apiKey");
			sb.append(" 	and B.moduleName = A.moduleName");
			sb.append(" )");
			params.put("dpStatus", dpStatus);

		} else if ("0".contentEquals(dpStatus)) {// 下架
			sb.append(" and (1 = 2 ");
			sb.append("		or exists(");
			sb.append(" 		select 1 from TsmpApiExt B");
			sb.append(" 		where B.dpStatus = :dpStatus");
			sb.append(" 		and B.apiKey = A.apiKey");
			sb.append(" 		and B.moduleName = A.moduleName");
			sb.append(" 	) or not exists(");
			sb.append(" 		select 1 from TsmpApiExt B");
			sb.append(" 		where B.apiKey = A.apiKey");
			sb.append(" 		and B.moduleName = A.moduleName");
			sb.append(" 	)");
			sb.append(" )");
			params.put("dpStatus", dpStatus);
		}

		// 關鍵字 (API_KEY/API_NAME/API_DESC , THEME_NAME , ORG_NAME/ORG_CODE)
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for (int i = 0; i < words.length; i++) {
				sb.append(" 	or UPPER(A.apiKey) like :keyword" + i);
				sb.append(" 	or UPPER(A.apiName) like :keyword" + i);
				sb.append(" 	or UPPER(A.apiDesc) like :keyword" + i);
				sb.append("  	or exists(");
				sb.append(" 		select 1 from TsmpDpApiTheme C");
				sb.append(" 		where C.refApiUid = A.apiUid");
				sb.append(" 		and exists(");
				sb.append(" 			select 1 from TsmpDpThemeCategory D");
				sb.append(" 			where D.apiThemeId = C.refApiThemeId");
				sb.append(" 			and (1 = 2");
				sb.append(" 				or UPPER(D.apiThemeName) like :keyword" + i);
				sb.append(" 			)");
				sb.append(" 		)");
				sb.append(" 	)");
				sb.append(" 	or exists(");
				sb.append(" 		select 1 from TsmpOrganization E");
				sb.append(" 		where E.orgId = A.orgId");
				sb.append(" 		and (1 = 2");
				sb.append(" 			or UPPER(E.orgName) like :keyword" + i);
				sb.append(" 			or UPPER(E.orgCode) like :keyword" + i);
				sb.append(" 		)");
				sb.append(" 	)");
				params.put("keyword" + i, "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" )");
		}

		sb.append(" order by A.apiKey asc, A.moduleName asc");

		return doQuery(sb.toString(), params, TsmpApi.class, pageSize);
	}

	public List<TsmpApi> query_dpb0072Service(Date sdt, Date edt, String[] words, TsmpApi lastRecord, Integer pageSize,
			List<String> orgDescList, String orgFlag) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select API");
		sb.append(" from TsmpApi API");
		sb.append(" where 1 = 1");
		// 分頁
		if (lastRecord != null) {
			sb.append(" and ( 1 = 2");
			sb.append(" 	or API.apiName > :lastApiName");
			sb.append(" 	or (API.apiName = :lastApiName and API.moduleName > :lastModuleName)");
			sb.append(
					" 	or (API.apiName = :lastApiName and API.moduleName = :lastModuleName and API.apiKey > :lastApiKey)");
			sb.append(" )");
			params.put("lastApiName", lastRecord.getApiName());
			params.put("lastModuleName", lastRecord.getModuleName());
			params.put("lastApiKey", lastRecord.getApiKey());
		}
		// 組織
		if ("0".equals(orgFlag)) {// 本組織向下
			sb.append(" and (API.orgId is null or LENGTH(API.orgId) = 0");
			if (!CollectionUtils.isEmpty(orgDescList)) {
				sb.append(" or API.orgId in (:orgDescList)");
				params.put("orgDescList", orgDescList);
			}
			sb.append(" )");
		}
		// 必要條件 (已上架、日期區間)
		sb.append(" and exists (");
		sb.append(" 	select 1");
		sb.append(" 	from TsmpApiExt EXT");
		sb.append(" 	where API.apiKey = EXT.apiKey");
		sb.append(" 	and API.moduleName = EXT.moduleName");
		sb.append(" 	and EXT.dpStatus = :dpStatus");
		sb.append(" 	and (EXT.dpStuDateTime is not null and EXT.dpStuDateTime between :sdt and :edt)");
		sb.append(" )");
		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for (int i = 0; i < words.length; i++) {
				sb.append(" 	or UPPER(API.apiKey) like :keyword" + i); // API Key
				sb.append(" 	or UPPER(API.apiName) like :keyword" + i); // API 名稱
				sb.append(" 	or UPPER(API.apiDesc) like :keyword" + i); // API 描述
				params.put(("keyword" + i), "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" 	or exists (");
			sb.append(" 		select 1");
			sb.append(" 		from TsmpDpApiTheme AT");
			sb.append(" 		where AT.refApiUid = API.apiUid");
			sb.append(" 		and exists (");
			sb.append(" 			select 1");
			sb.append(" 			from TsmpDpThemeCategory TH");
			sb.append(" 			where TH.apiThemeId = AT.refApiThemeId");
			sb.append(" 			and ( 1 = 2");
			for (int i = 0; i < words.length; i++) {
				sb.append(" 				or UPPER(TH.apiThemeName) like :keyword" + i); // 主題名稱
			}
			sb.append(" 			)");
			sb.append(" 		)");
			sb.append(" 	)");
			sb.append(" 	or exists (");
			sb.append(" 		select 1");
			sb.append(" 		from TsmpOrganization ORG");
			sb.append(" 		where ORG.orgId = API.orgId");
			sb.append(" 		and ( 1 = 2");
			for (int i = 0; i < words.length; i++) {
				sb.append(" 			or UPPER(ORG.orgName) like :keyword" + i); // 業務單位名稱
			}
			sb.append(" 		)");
			sb.append(" 	)");
			sb.append(" )");
		}
		sb.append(" order by API.apiName asc, API.moduleName asc, API.apiKey asc");

		params.put("dpStatus", "1"); // 上架
		params.put("sdt", sdt);
		params.put("edt", edt);

		return doQuery(sb.toString(), params, TsmpApi.class, pageSize);
	}

	// DPB0091, DPF0048 & DPB0092, DPF0049
	public List<TsmpApi> queryByOpenApiKeyId(TsmpApi lastRecord, Integer pageSize, Long openApiKeyId) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		sb.append(" select API");
		sb.append(" from TsmpApi API");
		sb.append(" where 1 = 1");
		// 分頁
		if (lastRecord != null) {
			sb.append(" and ( 1 = 2");
			sb.append(" 	or API.apiName > :lastApiName");
			sb.append(" 	or (API.apiName = :lastApiName and API.moduleName > :lastModuleName)");
			sb.append(
					" 	or (API.apiName = :lastApiName and API.moduleName = :lastModuleName and API.apiKey > :lastApiKey)");
			sb.append(" )");
			params.put("lastApiName", lastRecord.getApiName());
			params.put("lastModuleName", lastRecord.getModuleName());
			params.put("lastApiKey", lastRecord.getApiKey());
		}

		// 必要條件
		sb.append(" and exists (");
		sb.append(" 	select 1");
		sb.append(" 	from TsmpOpenApiKeyMap B");
		sb.append(" 	where API.apiUid = B.refApiUid");
		sb.append(" 	and B.refOpenApiKeyId = :openApiKeyId");
		sb.append(" )");
		params.put("openApiKeyId", openApiKeyId);

		sb.append(" order by API.apiName asc, API.moduleName asc, API.apiKey asc");

		return doQuery(sb.toString(), params, TsmpApi.class, pageSize);
	}

	// DPB0093, DPF0046
	public List<TsmpApi> queryApiLikeList(TsmpApiId lastId, String[] words, int pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpApi A");
		sb.append(" where 1 = 1");

		// 分頁
		if (lastId != null) {
			sb.append(" and (");
			sb.append(" 	A.apiKey > :apiKey");
			sb.append(" 	or (A.apiKey = :apiKey and A.moduleName > :moduleName)");
			sb.append(" )");
			params.put("apiKey", lastId.getApiKey());
			params.put("moduleName", lastId.getModuleName());
		}

		// 關鍵字 (API_KEY/API_NAME/API_DESC , THEME_NAME , ORG_NAME/ORG_CODE)
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for (int i = 0; i < words.length; i++) {
				sb.append(" 	or UPPER(A.apiKey) like :keyword" + i);
				sb.append(" 	or UPPER(A.apiName) like :keyword" + i);
				sb.append(" 	or UPPER(A.apiDesc) like :keyword" + i);
				sb.append("  	or exists(");
				sb.append(" 		select 1 from TsmpDpApiTheme C");
				sb.append(" 		where C.refApiUid = A.apiUid");
				sb.append(" 		and exists(");
				sb.append(" 			select 1 from TsmpDpThemeCategory D");
				sb.append(" 			where D.apiThemeId = C.refApiThemeId");
				sb.append(" 			and (1 = 2");
				sb.append(" 				or UPPER(D.apiThemeName) like :keyword" + i);
				sb.append(" 			)");
				sb.append(" 		)");
				sb.append(" 	)");
				sb.append(" 	or exists(");
				sb.append(" 		select 1 from TsmpOrganization E");
				sb.append(" 		where E.orgId = A.orgId");
				sb.append(" 		and (1 = 2");
				sb.append(" 			or UPPER(E.orgName) like :keyword" + i);
				sb.append(" 			or UPPER(E.orgCode) like :keyword" + i);
				sb.append(" 		)");
				sb.append(" 	)");
				params.put("keyword" + i, "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" )");
		}

		sb.append(" order by A.apiKey asc, A.moduleName asc");

		return doQuery(sb.toString(), params, TsmpApi.class, pageSize);
	}

	// AA0228
	public List<TsmpApi> queryByTsmpGroupAPiGroupId(String groupId) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpApi A ");
		sb.append(" where 1 = 1");
		sb.append(" and EXISTS (");
		sb.append(" 	select 1 ");
		sb.append(" 	from TsmpGroupApi G ");
		sb.append(" 	where G.groupId = :groupId");
		sb.append(" 	and G.apiKey = A.apiKey");
		sb.append(" 	and G.moduleName  = A.moduleName ");
		sb.append(" ) ");
		sb.append(" order by A.moduleName asc, A.apiName asc, A.apiKey asc");
		params.put("groupId", groupId);

		return doQuery(sb.toString(), params, TsmpApi.class);
	}

	/**
	 * 3.查詢TSMP_API、TSMP_GROUP_API與TSMP_VGROUP_GROUP資料表( TSMP_GROUP_API.GROUP_ID=
	 * TSMP_VGROUP_GROUP.GROUP_ID AND TSMP_API.API_KEY=tsmp_group_api.API_KEY AND
	 * TSMP_API.MODULE_NAME = tsmp_group_api.MODULE_NAME) ，條件
	 * TSMP_VGROUP_GROUP.GROUP_ID = AA0228Resp.vgroupId每一筆資料並ORDER BY
	 * TSMP_API.MODULE_NAME , TSMP_API.API_NAME
	 * ，TSMP_API為主要查詢資料表，將查詢到資料放進AA0229Resp.moduleAPIKeyList。
	 */
	public List<TsmpApi> findByAA0229Service(String vgroupId) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT a from TsmpApi a WHERE exists( ");
		sb.append(" SELECT 1 FROM TsmpGroupApi ga ");
		sb.append(" WHERE a.apiKey =ga.apiKey and a.moduleName = ga.moduleName  ");
		sb.append("       AND exists(SELECT 1 FROM TsmpVgroupGroup vg ");
		sb.append("                  WHERE vg.vgroupId = :vgroupId AND ga.groupId =vg.groupId )");
		sb.append(" ) ORDER BY a.moduleName ASC, a.apiName ASC");

		params.put("vgroupId", vgroupId);

		return doQuery(sb.toString(), params, TsmpApi.class);
	}

	// AA0233
	public List<String> queryModuleByClinetOrgId(String lastModuleNameRecord, String[] keywords, List<String> orgList,
			List<String> selectedModuleNameList, int pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();

		sb.append(" SELECT DISTINCT tsmpApi.moduleName ");
		sb.append(" FROM TsmpApi tsmpApi ");

		sb.append(" WHERE 1=1 ");

		// 'M': Module(Default); 'R': Registerd; 'C': Composed; 'N': .Net;
		sb.append(" AND tsmpApi.apiSrc IN ('M', 'R', 'C', 'N') ");

		// 將已經選擇的Module忽略
		if (selectedModuleNameList != null && selectedModuleNameList.size() > 0) {
			sb.append(" AND tsmpApi.moduleName NOT IN :selectedModuleNameList ");
			params.put("selectedModuleNameList", selectedModuleNameList);
		}

		// client的組織與子組織，找出關聯的TsmpApi資料
		sb.append(" AND (tsmpApi.orgId IS NULL OR LENGTH(tsmpApi.orgId) = 0");
		if (!CollectionUtils.isEmpty(orgList)) {
			sb.append(" OR tsmpApi.orgId IN (:orgList)");
			params.put("orgList", orgList);
		}
		sb.append(" )");

		// 分頁
		if (lastModuleNameRecord != null) {
			sb.append(" AND ( ");
			sb.append(" 	1=2");
			sb.append(" 	OR (tsmpApi.moduleName > :moduleName ) ");
			sb.append(" 	) ");
			params.put("moduleName", lastModuleNameRecord);
		}

		// 關鍵字
		if (keywords != null && keywords.length > 0) {

			sb.append(" AND ( ");
			sb.append("		1=2 ");
			for (int i = 0; i < keywords.length; i++) {
				sb.append(" 	OR UPPER(tsmpApi.moduleName) LIKE :keyworkSearch" + i);
				params.put("keyworkSearch" + i, "%" + keywords[i].toUpperCase() + "%");
			}
			sb.append(" 	) ");
			sb.append(" ");
		}
		sb.append(" ORDER BY tsmpApi.moduleName ASC ");

		return doQuery(sb.toString(), params, String.class, pageSize);
	}

	public List<TsmpApi> queryByAA0234Service(TsmpApi lastTsmpApi, String[] keywords, String moduleName,
			List<String> orgList, List<String> selectedApiKeyList, int pageSize) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();

		sb.append(" SELECT a ");
		sb.append(" FROM TsmpApi a ");

		sb.append(" WHERE 1=1 ");

		// moduleName
		sb.append(" AND a.moduleName = :moduleName ");
		params.put("moduleName", moduleName);

		// 'M': Module(Default); 'R': Registerd; 'C': Composed; 'N': .Net;
		sb.append(" AND a.apiSrc IN ('M', 'R', 'C', 'N') ");

		// 將已經選擇的Api Key忽略
		if (selectedApiKeyList != null && selectedApiKeyList.size() > 0) {
			sb.append(" AND a.apiKey NOT IN :selectedApiKeyList ");
			params.put("selectedApiKeyList", selectedApiKeyList);
		}

		// client的組織與子組織，找出關聯的TsmpApi資料
		sb.append(" AND (a.orgId IS NULL OR LENGTH(a.orgId) = 0");
		if (!CollectionUtils.isEmpty(orgList)) {
			sb.append(" OR a.orgId IN (:orgList)");
			params.put("orgList", orgList);
		}
		sb.append(" )");

		// 分頁
		if (lastTsmpApi != null) {
			sb.append(" AND ( ");
			sb.append(" 	1=2");
			sb.append(" 	OR (a.apiKey > :lastApiKey ) ");
			sb.append(" 	OR (a.apiKey = :lastApiKey AND a.apiName > :lastApiName) ");
			sb.append(" 	) ");
			params.put("lastApiKey", lastTsmpApi.getApiKey());
			params.put("lastApiName", lastTsmpApi.getApiName());
		}

		// 關鍵字
		if (keywords != null && keywords.length > 0) {

			sb.append(" AND ( ");
			sb.append("		1=2 ");
			for (int i = 0; i < keywords.length; i++) {
				sb.append(" 	OR UPPER(a.apiKey) LIKE :keyworkSearch" + i);
				sb.append(" 	OR UPPER(a.apiName) LIKE :keyworkSearch" + i);
				params.put("keyworkSearch" + i, "%" + keywords[i].toUpperCase() + "%");
			}
			sb.append(" 	) ");
		}
		sb.append(" ORDER BY a.apiKey ASC, a.apiName ASC ");

		return doQuery(sb.toString(), params, TsmpApi.class, pageSize);
	}

	public List<TsmpApi> query_AA0301Service(AA0301SearchCriteria cri) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		StringBuffer orderBySb = new StringBuffer();

		sb.append(" SELECT A ");
		sb.append(" FROM TsmpApi A ");

		sb.append(" WHERE 1=1 ");

		// client的組織與子組織，找出關聯的TsmpApi資料
		if (cri.getOrgList() != null && cri.getOrgList().size() > 0) {
			sb.append(" AND (A.orgId IN :orgList or A.orgId is Null or LENGTH(A.orgId) = 0)");
			params.put("orgList", cri.getOrgList());
		}

		// 分頁
		if ("Y".equals(cri.getPaging())) {
			if (cri.getLastTsmpApi() != null) {
				orderBySb = getAA0301PagingSQL(cri, sb, params);
			}
		}

		// API來源
		if (cri.getApiSrc() != null && cri.getApiSrc().size() > 0) {
			sb.append(" AND ( ");
			sb.append(" 	A.apiSrc in :apiSrc");
			sb.append(" 	) ");
			params.put("apiSrc", cri.getApiSrc());
		}

		// API狀態
		if (!StringUtils.isEmpty(cri.getApiStatus())) {
			sb.append(" AND ( ");
			sb.append(" 	A.apiStatus = :apiStatus");
			sb.append(" 	) ");
			params.put("apiStatus", cri.getApiStatus());
		}

		// 開放權限
		if (!StringUtils.isEmpty(cri.getPublicFlag())) {
			sb.append(" AND EXISTS (");
			sb.append(" 	SELECT 1");
			sb.append(" 	FROM TsmpDpItems IT");
			sb.append(" 	WHERE IT.itemNo = :itemNo");
			sb.append(" 	AND IT.subitemNo = :publicFlag");
			sb.append(" 	AND ( 1 = 2");
			// API有設定public_flag
			sb.append(" 		OR (");
			sb.append(" 			A.publicFlag IS NOT NULL AND ( 1 = 2");
			sb.append(" 				OR IT.param1 = A.publicFlag");
			sb.append(" 				OR IT.param2 = A.publicFlag");
			sb.append(" 				OR IT.param3 = A.publicFlag");
			sb.append(" 				OR IT.param4 = A.publicFlag");
			sb.append(" 				OR IT.param5 = A.publicFlag");
			sb.append(" 			)");
			sb.append(" 		)");
			// API未設定public_flag
			sb.append(" 		OR (");
			sb.append(" 			A.publicFlag IS NULL AND ( 1 = 2");
			sb.append(" 				OR IT.param1 = :emptyFlag");
			sb.append(" 				OR IT.param2 = :emptyFlag");
			sb.append(" 				OR IT.param3 = :emptyFlag");
			sb.append(" 				OR IT.param4 = :emptyFlag");
			sb.append(" 				OR IT.param5 = :emptyFlag");
			sb.append(" 			)");
			sb.append(" 		)");
			sb.append(" 	)");
			sb.append(" )");
			params.put("itemNo", "API_AUTHORITY");
			params.put("publicFlag", cri.getPublicFlag());
			params.put("emptyFlag", TsmpDpPublicFlag.EMPTY.value());
		}

		// JWT設定((Req)
		if (!StringUtils.isEmpty(cri.getJweFlag())) {
			sb.append(" AND ( ");
			sb.append(" 	A.jewFlag = :jewFlag");
			sb.append(" 	) ");
			params.put("jewFlag", cri.getJweFlag());
		}

		// JWT設定((Resp)
		if (!StringUtils.isEmpty(cri.getJweFlagResp())) {
			sb.append(" AND ( ");
			sb.append(" 	A.jewFlagResp = :jweFlagResp");
			sb.append(" 	) ");
			params.put("jweFlagResp", cri.getJweFlagResp());
		}

		// 關鍵字
		if (cri.getKeywords() != null && cri.getKeywords().length > 0) {
			sb.append(" AND ( ");
			sb.append("		1=2 ");
			for (int i = 0; i < cri.getKeywords().length; i++) {
				sb.append(" 	OR UPPER(A.apiKey) LIKE :keyworkSearch" + i);
				sb.append(" 	OR UPPER(A.moduleName) LIKE :keyworkSearch" + i);
				sb.append(" 	OR UPPER(A.apiName) LIKE :keyworkSearch" + i);
				sb.append(" 	OR UPPER(A.apiDesc) LIKE :keyworkSearch" + i);
				params.put("keyworkSearch" + i, "%" + cri.getKeywords()[i].toUpperCase() + "%");
			}
			sb.append(" 	) ");
		}

		if (orderBySb == null || "".equals(orderBySb.toString())) {
			orderBySb = getAA0301OrderBy(cri);
			sb.append(orderBySb.toString());
		} else {
			sb.append(orderBySb.toString());
		}

		if ("Y".equals(cri.getPaging())) {
			return doQuery(sb.toString(), params, TsmpApi.class, cri.getPageSize());
		} else {
			return doQuery(sb.toString(), params, TsmpApi.class);

		}
	}

	public StringBuffer getAA0301PagingSQL(AA0301SearchCriteria cri, StringBuffer sb, Map<String, Object> params) {
		StringBuffer orderBySb = new StringBuffer(); // orderBy SQL
		sb.append(" 	AND ( ");
		sb.append(" 			1=2");
		String sortColumn = cri.getSortColumn();

		switch (sortColumn) {
		case "apiKey":
			if ("asc".equals(cri.getSort())) {
				sb.append(" 	OR (A.apiKey > :lastApiKey ) ");
				orderBySb.append("ORDER BY A.apiKey ASC, A.moduleName ASC "); // orderBy SQL
			} else {
				sb.append(" 	OR (A.apiKey < :lastApiKey ) ");
				orderBySb.append("ORDER BY A.apiKey DESC, A.moduleName ASC "); // orderBy SQL
			}
			sb.append(" 		OR (A.apiKey = :lastApiKey AND A.moduleName > :lastModuleName) ");

			break;
		case "apiSrc":
			if ("asc".equals(cri.getSort())) {
				sb.append(" 	OR (A.apiSrc > :apiSrc ) ");
				orderBySb.append("ORDER BY A.apiSrc ASC, A.apiKey ASC, A.moduleName ASC "); // orderBy SQL
			} else {
				sb.append(" 	OR (A.apiSrc < :apiSrc ) ");
				orderBySb.append("ORDER BY A.apiSrc DESC, A.apiKey ASC, A.moduleName ASC "); // orderBy SQL
			}
			sb.append(" 		OR (A.apiSrc = :apiSrc AND A.apiKey > :lastApiKey ) ");
			sb.append(
					" 		OR (A.apiSrc = :apiSrc AND A.apiKey = :lastApiKey AND A.moduleName > :lastModuleName) ");

			params.put("apiSrc", cri.getLastTsmpApi().getApiSrc());
			break;
		case "moduleName":
			if ("asc".equals(cri.getSort())) {
				sb.append(" 	OR (A.moduleName > :lastModuleName ) ");
				orderBySb.append("ORDER BY A.moduleName ASC, A.apiKey ASC "); // orderBy SQL
			} else {
				sb.append(" 	OR (A.moduleName < :lastModuleName ) ");
				orderBySb.append("ORDER BY A.moduleName DESC, A.apiKey ASC "); // orderBy SQL
			}
			sb.append(" 		OR (A.moduleName = lastModuleName: AND A.apiKey > :lastApiKey) ");

			break;
		case "apiName":
			if ("asc".equals(cri.getSort())) {
				sb.append(" 	OR (A.apiName > :apiName ) ");
				orderBySb.append("ORDER BY A.apiName ASC, A.apiKey ASC, A.moduleName ASC"); // orderBy SQL
			} else {
				sb.append(" 	OR (A.apiName < :apiName ) ");
				orderBySb.append("ORDER BY A.apiName DESC, A.apiKey ASC, A.moduleName ASC"); // orderBy SQL
			}
			sb.append(" 		OR (A.apiName = :apiName AND A.apiKey > :lastApiKey ) ");
			sb.append(
					" 		OR (A.apiName = :apiName AND A.apiKey = :lastApiKey AND A.moduleName > :lastModuleName) ");

			params.put("apiName", cri.getLastTsmpApi().getApiName());
			break;
		default:
			sb.append(" 		OR (A.apiKey > :lastApiKey ) ");
			sb.append(" 		OR (A.apiKey = :lastApiKey AND A.moduleName > :lastModuleName) ");
			break;
		}

		sb.append(" 	) ");
		params.put("lastApiKey", cri.getLastTsmpApi().getApiKey());
		params.put("lastModuleName", cri.getLastTsmpApi().getModuleName());

		return orderBySb;
	}

	public StringBuffer getAA0301OrderBy(AA0301SearchCriteria cri) {
		StringBuffer orderBySb = new StringBuffer();
		String sort = cri.getSort();
		String sortColumn = cri.getSortColumn();

		if (!"".equals(sort) && !"".equals(sortColumn)) {
			switch (sortColumn) {
			case "apiKey":
				if ("asc".equals(sort)) {
					orderBySb.append("ORDER BY A.apiKey ASC, A.moduleName ASC ");
				} else {
					orderBySb.append("ORDER BY A.apiKey DESC, A.moduleName ASC ");
				}
				break;
			case "apiSrc":
				if ("asc".equals(sort)) {
					orderBySb.append("ORDER BY A.apiSrc ASC, A.apiKey ASC, A.moduleName ASC ");
				} else {
					orderBySb.append("ORDER BY A.apiSrc DESC, A.apiKey ASC, A.moduleName ASC ");
				}
				break;
			case "moduleName":
				if ("asc".equals(sort)) {
					orderBySb.append("ORDER BY A.moduleName ASC, A.apiKey ASC ");
				} else {
					orderBySb.append("ORDER BY A.moduleName DESC, A.apiKey ASC ");
				}
				break;
			case "apiName":
				if ("asc".equals(sort)) {
					orderBySb.append("ORDER BY A.apiName ASC, A.apiKey ASC, A.moduleName ASC");
				} else {
					orderBySb.append("ORDER BY A.apiName DESC, A.apiKey ASC, A.moduleName ASC");
				}
				break;
			default:
				orderBySb.append("ORDER BY A.apiKey ASC, A.moduleName ASC ");
				break;
			}
		} else {
			orderBySb.append("ORDER BY A.apiKey ASC, A.moduleName ASC ");
		}

		return orderBySb;
	}

	public List<TsmpApi> query_aa0321Service(TsmpApiId lastId, List<String> orgDescList, String[] words,
			List<String> apiUidList, int pageSize) {

		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select A from TsmpApi A");
		sb.append(" where 1 = 1");

		// 已經挑選API清單需要過濾掉
		if (apiUidList != null && apiUidList.isEmpty() == false) {
			sb.append(" AND A.apiUid NOT IN (:apiUidList)");
			params.put("apiUidList", apiUidList);
		}

		// 分頁
		if (lastId != null) {
			sb.append(" and (");
			sb.append(" 	A.apiKey > :apiKey");
			sb.append(" 	or (A.apiKey = :apiKey and A.moduleName > :moduleName)");
			sb.append(" )");
			params.put("apiKey", lastId.getApiKey());
			params.put("moduleName", lastId.getModuleName());
		}

		// 組織
		sb.append(" AND (A.orgId IS NULL OR LENGTH(A.orgId) = 0");
		if (!CollectionUtils.isEmpty(orgDescList)) {
			sb.append(" OR A.orgId IN (:orgDescList)");
			params.put("orgDescList", orgDescList);
		}
		sb.append(" )");

		// 關鍵字 (API_KEY/API_NAME/API_DESC , ORG_NAME/ORG_CODE)
		if (words != null && words.length > 0) {
			sb.append(" and ( 1 = 2");
			for (int i = 0; i < words.length; i++) {
				sb.append(" 	or UPPER(A.apiKey) like :keyword" + i);
				sb.append(" 	or UPPER(A.apiName) like :keyword" + i);
				sb.append(" 	or UPPER(A.apiDesc) like :keyword" + i);
				sb.append(" 	or UPPER(A.moduleName) like :keyword" + i);
				sb.append(" 	or exists(");
				sb.append(" 		select 1 from TsmpOrganization E");
				sb.append(" 		where E.orgId = A.orgId");
				sb.append(" 		and (1 = 2");
				sb.append(" 			or UPPER(E.orgName) like :keyword" + i);
				sb.append(" 			or UPPER(E.orgCode) like :keyword" + i);
				sb.append(" 		)");
				sb.append(" 	)");
				params.put("keyword" + i, "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" )");
		}

		sb.append(" order by A.apiKey asc, A.moduleName asc");

		return doQuery(sb.toString(), params, TsmpApi.class, pageSize);
	}

	public List<TsmpApi> query_AA0405Service(String moduleName, List<String> orgList) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();

		sb.append(" SELECT A ");
		sb.append(" FROM TsmpApi A ");

		sb.append(" WHERE 1=1 ");
		sb.append(" AND A.moduleName = :moduleName");
		params.put("moduleName", moduleName);

		// client的組織與子組織，找出關聯的TsmpApi資料
		if (orgList != null && orgList.size() > 0) {
			sb.append(" AND (A.orgId IN :orgList or A.orgId is Null or LENGTH(A.orgId) = 0)");
			params.put("orgList", orgList);
		}

		return doQuery(sb.toString(), params, TsmpApi.class);
	}

	public List<TsmpApi> findTop5ByReleaseTimeDesc() {
		Map<String, Object> params = new HashMap<>();

		params.put("apiStatus", "1");
		params.put("publicFlag", "E");

		StringBuffer sb = new StringBuffer();

		sb.append(" SELECT A ");
		sb.append(" FROM TsmpApi A ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND A.apiStatus = :apiStatus");
		sb.append(" AND A.publicFlag = :publicFlag");
		sb.append(" ORDER BY A.apiReleaseTime DESC , A.apiName ");

		return doQuery(sb.toString(), params, TsmpApi.class, 5);
	}

	public List<TsmpApi> findByModuleNameAndApiKeyAndApiNameAndApiDescAndApiStatusAndPublicFlag(String moduleName,
			String apiKey, String apiStatus, String publicFlag, String[] words, Integer pageSize) {

		Map<String, Object> params = new HashMap<>();

		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT A ");
		sb.append(" FROM TsmpApi A ");
		sb.append(" WHERE 1 = 1  ");

		if (StringUtils.hasText(moduleName) && StringUtils.hasText(apiKey)) {
			sb.append(" AND ");
			sb.append(" ( ");
			sb.append("    1 = 2 ");
			sb.append("    OR A.apiKey > :apiKey ");
			sb.append("    OR ( A.apiKey = :apiKey AND A.moduleName > :moduleName)");
			sb.append(" ) ");
			params.put("apiKey", apiKey);
			params.put("moduleName", moduleName);
		}

		if (StringUtils.hasText(apiStatus)) {
			sb.append(" AND ");
			sb.append(" ( ");
			sb.append("    1 = 2 ");
			sb.append("    OR A.apiStatus = :apiStatus ");
			sb.append(" ) ");
			params.put("apiStatus", apiStatus);
		}

		if (StringUtils.hasText(publicFlag)) {
			if ("E".equals(publicFlag)) {
				sb.append(" AND ");
				sb.append(" ( ");
				sb.append("    1 = 2 ");
				sb.append("    OR A.publicFlag = :publicFlag ");
				sb.append(" ) ");
				params.put("publicFlag", publicFlag);
			} else {
				sb.append(" AND ");
				sb.append(" ( ");
				sb.append("    1 = 2 ");
				sb.append("    OR A.publicFlag <> :publicFlag ");
				sb.append(" ) ");
				params.put("publicFlag", "E");
			}
		}

		if (words != null && words.length > 0) {
			sb.append(" AND ( 1 = 2 ");
			for (int i = 0; i < words.length; i++) {
				sb.append(" OR UPPER(A.apiKey) like :keyworkSearch" + i);
				sb.append(" OR UPPER(A.moduleName) like :keyworkSearch" + i);
				sb.append(" OR UPPER(A.apiName) like :keyworkSearch" + i);
				sb.append(" OR UPPER(A.apiDesc) like :keyworkSearch" + i);
				params.put("keyworkSearch" + i, "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" ) ");
		}
		sb.append(" ORDER BY A.apiKey, A.moduleName ASC ");

		return doQuery(sb.toString(), params, TsmpApi.class, pageSize);

	}

	public List<String> query_AA0427Lable1(List<String> apiSrc) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT DISTINCT LOWER(tsmpApi.label1) ");
		sb.append(" FROM TsmpApi tsmpApi ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND tsmpApi.label1 IS NOT NULL");
//		sb.append(" AND tsmpApi.label1 <> ''");
		if (apiSrc != null && apiSrc.size() > 0) {
			sb.append(" AND tsmpApi.apiSrc IN :apiSrc ");
			params.put("apiSrc", apiSrc);
		}
		return doQuery(sb.toString(), params, String.class);
	}

	public List<String> query_AA0427Lable2(List<String> apiSrc) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT DISTINCT LOWER(tsmpApi.label2)");
		sb.append(" FROM TsmpApi tsmpApi ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND tsmpApi.label2 IS NOT NULL");
//		sb.append(" AND tsmpApi.label2 <> ''");
		if (apiSrc != null && apiSrc.size() > 0) {
			sb.append(" AND tsmpApi.apiSrc IN :apiSrc ");
			params.put("apiSrc", apiSrc);
		}
		return doQuery(sb.toString(), params, String.class);
	}

	public List<String> query_AA0427Lable3(List<String> apiSrc) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT DISTINCT LOWER(tsmpApi.label3) ");
		sb.append(" FROM TsmpApi tsmpApi ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND tsmpApi.label3 IS NOT NULL");
//		sb.append(" AND tsmpApi.label3 <> ''");
		if (apiSrc != null && apiSrc.size() > 0) {
			sb.append(" AND tsmpApi.apiSrc IN :apiSrc ");
			params.put("apiSrc", apiSrc);
		}
		return doQuery(sb.toString(), params, String.class);
	}

	public List<String> query_AA0427Lable4(List<String> apiSrc) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT DISTINCT LOWER(tsmpApi.label4) ");
		sb.append(" FROM TsmpApi tsmpApi ");
		sb.append(" WHERE 1=1 ");
		sb.append(" AND tsmpApi.label4 IS NOT NULL");
//		sb.append(" AND tsmpApi.label4 <> ''");
		if (apiSrc != null && apiSrc.size() > 0) {
			sb.append(" AND tsmpApi.apiSrc IN :apiSrc ");
			params.put("apiSrc", apiSrc);
		}
		return doQuery(sb.toString(), params, String.class);
	}

	public List<String> query_AA0427Lable5(List<String> apiSrc) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT DISTINCT LOWER(tsmpApi.label5) ");
		sb.append(" FROM TsmpApi tsmpApi ");
		sb.append(" WHERE 1=1 ");

		sb.append(" AND tsmpApi.label5 IS NOT NULL");
//		sb.append(" AND tsmpApi.label5 <> ''");
		if (apiSrc != null && apiSrc.size() > 0) {
			sb.append(" AND tsmpApi.apiSrc IN :apiSrc ");
			params.put("apiSrc", apiSrc);
		}

		return doQuery(sb.toString(), params, String.class);
	}

	public List<TsmpApi> query_AA0428Service(AA0301SearchCriteria cri) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		StringBuffer orderBySb = new StringBuffer();
		sb.append(" SELECT A ");
		sb.append(" FROM TsmpApi A ");

		sb.append(" WHERE 1=1 ");

		// client的組織與子組織，找出關聯的TsmpApi資料
		if (cri.getOrgList() != null && cri.getOrgList().size() > 0) {
			sb.append(" AND (A.orgId IN :orgList or A.orgId is Null or LENGTH(A.orgId) = 0)");
			params.put("orgList", cri.getOrgList());
		}

		// 分頁
		if ("Y".equals(cri.getPaging())) {
			if (cri.getLastTsmpApi() != null) {
				orderBySb = getAA0301PagingSQL(cri, sb, params);
			}
		}
		List<String> labelList = cri.getLabeList();
		if (labelList != null && labelList.size() > 0) {
			sb.append(" AND ( ");
			sb.append("		1=2 ");

			sb.append(" 	OR LOWER(A.label1) IN :labelList");
			sb.append(" 	OR LOWER(A.label2) IN :labelList");
			sb.append(" 	OR LOWER(A.label3) IN :labelList");
			sb.append(" 	OR LOWER(A.label4) IN :labelList");
			sb.append(" 	OR LOWER(A.label5) IN :labelList");
			params.put("labelList", labelList);

			sb.append(" 	) ");
		}

		if (orderBySb == null || "".equals(orderBySb.toString())) {
			orderBySb = getAA0301OrderBy(cri);
			sb.append(orderBySb.toString());
		} else {
			sb.append(orderBySb.toString());
		}

		if ("Y".equals(cri.getPaging())) {
			return doQuery(sb.toString(), params, TsmpApi.class, cri.getPageSize());
		} else {
			return doQuery(sb.toString(), params, TsmpApi.class);

		}

	}

	public List<TsmpApi> query_AA0423Service(List<String> labelList) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT A ");
		sb.append(" FROM TsmpApi A ");

		sb.append(" WHERE 1=1 ");
		// 只搜尋註冊API
		sb.append(" AND ( ");
		sb.append("		A.apiSrc = 'R' ");
		sb.append(" 	) ");
		if (labelList != null && labelList.size() > 0) {
			sb.append(" AND ( ");
			sb.append("		1=2 ");

			sb.append(" 	OR LOWER(A.label1) IN :labelList");
			sb.append(" 	OR LOWER(A.label2) IN :labelList");
			sb.append(" 	OR LOWER(A.label3) IN :labelList");
			sb.append(" 	OR LOWER(A.label4) IN :labelList");
			sb.append(" 	OR LOWER(A.label5) IN :labelList");
			params.put("labelList", labelList);

			sb.append(" 	) ");
		}

		return doQuery(sb.toString(), params, TsmpApi.class);

	}
}
