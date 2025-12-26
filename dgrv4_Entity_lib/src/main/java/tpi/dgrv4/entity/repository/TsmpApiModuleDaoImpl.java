package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.jpql.TsmpApiModule;
import tpi.dgrv4.entity.vo.AA0419SearchCriteria;

public class TsmpApiModuleDaoImpl extends BaseDao {
	// add custom methods here
	public List<TsmpApiModule> queryApiDocsListLike(String[] words, String moduleName //
			, String moduleVersion, Integer pageSize){
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		sb.append(" select T.src, T.id, T.module_name, T.module_version, T.active");
		sb.append(" from (");
		sb.append(" 	select 'M' as src, A.id, A.module_name, A.module_version, A.active");
		sb.append(" 	from tsmp_api_module A");
		sb.append(" 	where ( 1 = 2");
		sb.append(" 		or (A.active = 1)");
		sb.append(" 		or exists (");
		sb.append(" 			select 1");
		sb.append(" 			from tsmp_dc_module AA");
		sb.append(" 			where AA.module_id = A.id");
		sb.append(" 			and exists (");
		sb.append(" 				select 1");
		sb.append(" 				from tsmp_dc dc");
		sb.append(" 				where dc.dc_id = AA.dc_id");
		sb.append(" 				and dc.active = 1");
		sb.append(" 			)");
		sb.append(" 		)");
		sb.append(" 	)");
		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" 	and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 		or UPPER(A.module_name) like :keyword" + i);
				params.put(("keyword" + i), "%" + words[i].toUpperCase() + "%");
			}
			sb.append(" 	)");
		}
		sb.append(" 	union all");
		sb.append(" 	select 'N' as src, B.id, B.module_name, B.module_version, B.active");
		sb.append(" 	from tsmpn_api_module B");
		sb.append(" 	where ( 1 = 2");
		sb.append(" 		or (B.active = 1)");
		sb.append(" 		or exists (");
		sb.append(" 			select 1");
		sb.append(" 			from tsmp_dc_module BB");
		sb.append(" 			where BB.module_id = B.id");
		sb.append(" 			and exists (");
		sb.append(" 				select 1");
		sb.append(" 				from tsmp_dc dc");
		sb.append(" 				where dc.dc_id = BB.dc_id");
		sb.append(" 				and dc.active = 1");
		sb.append(" 			)");
		sb.append(" 		)");
		sb.append(" 	)");
		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" 	and ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 		or UPPER(B.module_name) like :keyword" + i);
			}
			sb.append(" 	)");
		}
		sb.append(" ) T");
		// 分頁
		if (moduleName != null && !moduleName.isEmpty() && moduleVersion != null && !moduleVersion.isEmpty()) {
			sb.append(" where (");
			sb.append(" 	(T.module_name > :moduleName)");
			sb.append(" 	OR");
			sb.append(" 	(T.module_name = :moduleName AND T.module_version > :moduleVersion)");
			sb.append(" )");
			params.put("moduleName", moduleName);
			params.put("moduleVersion", moduleVersion);
		}
		// 分頁 order by
		sb.append(" order by T.module_name ASC, T.module_version ASC");
		
		List<TsmpApiModule> modules = doNativeQuery(sb.toString(), "TsmpApiModuleMapping", params, pageSize, TsmpApiModule.class);
		if (modules != null && !modules.isEmpty()) {
			return modules;
		}

/*
		// 增加查詢速度
		//sb.append("SELECT A FROM TsmpApiModule A ");
		sb.append(" SELECT new tpi.dgrv4.dpaa.entity.jpql.TsmpApiModule(");
		sb.append("A.id, A.moduleName, A.moduleVersion, A.active");
		sb.append(") FROM TsmpApiModule A");
		
		//一般條件
		sb.append(" WHERE A.active=true ");
		
		// 分頁
		if (lastId != null) {
			sb.append(" AND A.id > :id");
			params.put("id", lastId);
		}
		
		//一般條件
//		sb.append(" AND NOT EXISTS (");
//		sb.append(" SELECT 1 FROM TsmpDpDeniedModule B WHERE A.moduleName = B.refModuleName");
//		sb.append(" ) ");
		
		// 關鍵字
		if (words != null && words.length > 0) {
			sb.append(" AND ( 1 = 2");
			for(int i = 0; i < words.length; i++) {
				sb.append(" 	OR A.moduleName LIKE :keyword" + i);
				params.put(("keyword" + i), "%" + words[i] + "%");
			}
			sb.append(" ) ");
		}

		// 分頁 order by
		sb.append(" ORDER BY A.id ASC");

		
		List<TsmpApiModule> dataList = doQuery(sb.toString(), params, TsmpApiModule.class, pageSize);
		if (dataList != null && !dataList.isEmpty()) {
			return dataList;
		}
*/
		return null;
	}

	public boolean isExistsByModuleName(String moduleName) {
		Map<String, Object> params = new HashMap<>();

		StringBuffer sb = new StringBuffer();
		sb.append(" select T.src, T.id, T.module_name, T.module_version, T.active");
		sb.append(" from (");
		sb.append(" 	select 'M' as src, A.id, A.module_name, A.module_version, A.active");
		sb.append(" 	from tsmp_api_module A");
		sb.append(" 	where A.module_name = :moduleName");
		sb.append(" 	union all");
		sb.append(" 	select 'N' as src, B.id, B.module_name, B.module_version, B.active");
		sb.append(" 	from tsmpn_api_module B");
		sb.append(" 	where B.module_name = :moduleName");
		sb.append(" ) T");
		
		params.put("moduleName", moduleName);
		List<TsmpApiModule> modules = doNativeQuery(sb.toString(), "TsmpApiModuleMapping", params //
				, Integer.MAX_VALUE, TsmpApiModule.class);
		return !(modules == null || modules.isEmpty());
	}

	public List<TsmpApiModule> queryActiveV3ModulesByModuleName(String moduleName) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		sb.append(" SELECT new tpi.dgrv4.entity.entity.jpql.TsmpApiModule(");
		sb.append("A.id, A.moduleName, A.moduleVersion, A.active");
		sb.append(") FROM TsmpApiModule A");
		sb.append(" WHERE A.moduleName = :moduleName");
		sb.append(" AND (1 = 2");
		sb.append(" 	OR (A.active = true)");
		sb.append(" 	OR EXISTS (");
		sb.append(" 		SELECT 1");
		sb.append(" 		FROM TsmpDcModule B");
		sb.append(" 		WHERE B.moduleId = A.id");
		sb.append(" 		AND EXISTS (");
		sb.append(" 			SELECT 1");
		sb.append(" 			FROM TsmpDc C");
		sb.append(" 			WHERE C.dcId = B.dcId");
		sb.append(" 			AND C.active = true");
		sb.append(" 		)");
		sb.append(" 	)");
		sb.append(" )");
		sb.append(" ORDER BY A.moduleVersion ASC, A.id ASC");

		params.put("moduleName", moduleName);
		return doQuery(sb.toString(), params, TsmpApiModule.class);
	}

	public TsmpApiModule queryById(Long id) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		sb.append(" SELECT new tpi.dgrv4.entity.entity.jpql.TsmpApiModule(");
		sb.append("     M.id, M.moduleName, M.moduleVersion,");
		sb.append("     M.moduleAppClass, M.moduleMd5, M.moduleType,");
		sb.append("     M.uploadTime, M.uploaderName,");
		sb.append("     M.statusTime, M.statusUser,");
		sb.append("     M.active, M.nodeTaskId, M.v2Flag, M.orgId");
		sb.append(" ) FROM TsmpApiModule M");
		sb.append(" WHERE M.id = :id");

		params.put("id", id);
		List<TsmpApiModule> entries = doQuery(sb.toString(), params, TsmpApiModule.class);
		if (entries != null && !entries.isEmpty()) {
			return entries.get(0);
		}
		
		return null;
	}
	
	public List<TsmpApiModule> queryActiveV2ByModuleNameAndOrgId(String moduleName, String orgId) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		sb.append(" SELECT new tpi.dgrv4.entity.entity.jpql.TsmpApiModule(");
		sb.append("A.id, A.moduleName, A.moduleVersion, A.active");
		sb.append(") FROM TsmpApiModule A");
		sb.append(" WHERE A.moduleName = :moduleName");
		sb.append(" AND (A.orgId = :orgId or A.orgId is Null or LENGTH(A.orgId) = 0) ");
		sb.append(" AND A.active = true ");
		sb.append(" AND (1 = 2");
		sb.append(" 	OR A.v2Flag = 1 ");
		sb.append(" 	OR A.v2Flag is null ");
		sb.append(" )");
		sb.append(" ORDER BY A.moduleVersion ASC, A.id ASC");

		params.put("moduleName", moduleName);
		params.put("orgId", orgId);
		return doQuery(sb.toString(), params, TsmpApiModule.class);
	}
	
	public List<TsmpApiModule> queryActiveV3ByModuleNameAndOrgId(String moduleName, String orgId) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		sb.append(" SELECT new tpi.dgrv4.entity.entity.jpql.TsmpApiModule(");
		sb.append("A.id, A.moduleName, A.moduleVersion, A.active");
		sb.append(") FROM TsmpApiModule A");
		sb.append(" WHERE A.moduleName = :moduleName");
		sb.append(" AND (A.orgId = :orgId or A.orgId is Null or LENGTH(A.orgId) = 0) ");
		sb.append(" AND A.v2Flag = 2 ");
		sb.append(" AND EXISTS (");
		sb.append(" 	SELECT 1");
		sb.append(" 	FROM TsmpDcModule B");
		sb.append(" 	WHERE B.moduleId = A.id");
		sb.append(" 	AND EXISTS (");
		sb.append(" 		SELECT 1");
		sb.append(" 		FROM TsmpDc C");
		sb.append(" 		WHERE C.dcId = B.dcId");
		sb.append(" 		AND C.active = true");
		sb.append(" 	)");
		sb.append(" )");
		sb.append(" ORDER BY A.moduleVersion ASC, A.id ASC");

		params.put("moduleName", moduleName);
		params.put("orgId", orgId);
		return doQuery(sb.toString(), params, TsmpApiModule.class);
	}
	
	public List<TsmpApiModule> queryByModuleNameAndOrgIdOrderByUploadTimeDesc(String moduleName, String orgId) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		sb.append(" SELECT new tpi.dgrv4.entity.entity.jpql.TsmpApiModule(");
		sb.append("A.id, A.moduleName, A.moduleVersion, A.active");
		sb.append(") FROM TsmpApiModule A");
		sb.append(" WHERE A.moduleName = :moduleName");
		sb.append(" AND (A.orgId = :orgId or A.orgId is Null or LENGTH(A.orgId) = 0) ");
		sb.append(" ORDER BY A.uploadTime desc");

		params.put("moduleName", moduleName);
		params.put("orgId", orgId);
		return doQuery(sb.toString(), params, TsmpApiModule.class);
	}
	
	public TsmpApiModule queryFirstByModuleName(String moduleName) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		
		sb.append(" select new tpi.dgrv4.entity.entity.jpql.TsmpApiModule(");
		sb.append("A.id, A.moduleName, A.moduleVersion, A.active");
		sb.append(") from TsmpApiModule A");
		sb.append(" where A.moduleName = :moduleName");
		sb.append(" and exists (select 1 from TsmpDcModule B where B.moduleId = A.id) ");
		sb.append(" order by A.id desc");
		
		params.put("moduleName", moduleName);
		
		List<TsmpApiModule> list = doQuery(sb.toString(), params, TsmpApiModule.class);
		if (list == null || list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}
	
	public List<TsmpApiModule> queryByAA0420Service(Long lastId, String[] keywords, Long dcId, String moduleName, List<String> orgList, int pageSize){
		Map<String, Object> params = new HashMap<>();
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(" SELECT new tpi.dgrv4.entity.entity.jpql.TsmpApiModule(");
		sb.append("   m.id, m.moduleName, m.moduleVersion, m.uploadTime, m.active, m.v2Flag");
		sb.append(" ) FROM TsmpApiModule m ");
		
		sb.append(" WHERE 1=1 ");
		
		//分頁
		if (lastId != null) {
			sb.append(" AND ( 1 = 2");
			sb.append("    OR (m.id < :lastId)");
			sb.append(" )");			
			params.put("lastId", lastId);
		}
		
		//組織原則
		sb.append(" AND (m.orgId IS NULL OR LENGTH(m.orgId) = 0");
		if (!CollectionUtils.isEmpty(orgList)) {
			sb.append(" OR m.orgId IN (:orgList)");
			params.put("orgList", orgList);
		}
		sb.append(" )");
		
		//模組名稱
		sb.append(" AND m.moduleName = :moduleName ");
		params.put("moduleName", moduleName);

		// dcId=0,則v2_flag = 1 or is null,否則v2_flag=2
		if(dcId.longValue() == 0L) {
			sb.append(" AND (m.v2Flag = 1 OR m.v2Flag is null) ");
		}else {
			sb.append(" AND m.v2Flag = 2 ");
		}
		
		//關鍵字 
		if (keywords != null && keywords.length > 0) {

			sb.append(" AND ( ");
			sb.append("		1 = 2 ");
			for (int i = 0; i < keywords.length; i++) {
				//版本
				sb.append(" OR UPPER(m.moduleVersion) LIKE :keyworkSearch" +i);
				params.put("keyworkSearch"+i,"%" + keywords[i].toUpperCase()+ "%" );
			}
			
			//dcId=0則不用組成以下條件
			if(dcId.longValue() != 0L) {
				sb.append(" OR EXISTS( ");
				sb.append(" 	SELECT 1 ");
				sb.append(" 	FROM TsmpDcModule dm ");
				sb.append(" 	WHERE dm.moduleId = m.id ");
				sb.append(" 	AND EXISTS ( ");
				sb.append(" 		SELECT 1 ");
				sb.append(" 		FROM TsmpDc d ");
				sb.append(" 		WHERE dm.dcId = d.dcId ");
				sb.append(" 		AND d.active = 1 ");
				sb.append(" 		AND ( 1 = 2 ");
				for (int i = 0; i < keywords.length; i++) {
					//部署容器代碼
					sb.append(" 	OR UPPER(d.dcCode) LIKE :keyworkSearch" +i);
					params.put("keyworkSearch"+i,"%" + keywords[i].toUpperCase()+ "%" );
				}
				sb.append(" 		) ");
				sb.append(" 	) ");
				sb.append(" ) ");
			}
			
			sb.append(" ) ");
		}
		sb.append(" ORDER BY m.id DESC");
		
		return doQuery(sb.toString(), params, TsmpApiModule.class, pageSize);
	}

	public List<TsmpApiModule> query_AA0419Service_01(AA0419SearchCriteria cri) {
		String lastModuleName = cri.getLastModuleName();
		boolean isV2 = cri.isV2();
		String[] keyword = cri.getKeyword();
		List<String> orgIdList = cri.getOrgIdList();
		return query_AA0419Service_0(lastModuleName, isV2, null, keyword, orgIdList);
	}

	public List<TsmpApiModule> query_AA0419Service_02(AA0419SearchCriteria cri, Long dcId) {
		String lastModuleName = cri.getLastModuleName();
		boolean isV2 = cri.isV2();
		String[] keyword = cri.getKeyword();
		List<String> orgIdList = cri.getOrgIdList();
		return query_AA0419Service_0(lastModuleName, isV2, dcId, keyword, orgIdList);
	}

	private List<TsmpApiModule> query_AA0419Service_0(String lastModuleName, boolean isV2, //
		Long dcId, String[] keyword, List<String> orgIdList) {
		
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		
		sb.append(" SELECT new tpi.dgrv4.entity.entity.jpql.TsmpApiModule(");
		sb.append("     M.id, M.moduleName, M.moduleVersion,");
		sb.append("     M.moduleAppClass, M.moduleMd5, M.moduleType,");
		sb.append("     M.uploadTime, M.uploaderName,");
		sb.append("     M.statusTime, M.statusUser,");
		sb.append("     M.active, M.nodeTaskId, M.v2Flag, M.orgId");
		sb.append(" )");
		sb.append(" FROM TsmpApiModule M");
		sb.append(" WHERE 1 = 1");
		
		// 分頁
		if (!StringUtils.isEmpty(lastModuleName)) {
			sb.append(" AND M.moduleName > :lastModuleName");
			params.put("lastModuleName", lastModuleName);
		}

		// 組織原則
		sb.append(" AND (M.orgId IS NULL OR LENGTH(M.orgId) = 0");
		if (!CollectionUtils.isEmpty(orgIdList)) {
			sb.append(" OR M.orgId IN (:orgIdList)");
			params.put("orgIdList", orgIdList);
		}
		sb.append(" )");
		
		// 模組架構
		if (isV2) {
			sb.append(" AND (M.v2Flag IS NULL OR M.v2Flag = 1)");
		} else {
			sb.append(" AND M.v2Flag = 2");
		}

		// 非 V2 模組是否啟用
		if (dcId != null) {
			sb.append(" AND EXISTS (");
			sb.append("     SELECT 1");
			sb.append("     FROM TsmpDcModule DM");
			sb.append("     WHERE DM.moduleId = M.id");
			sb.append("     AND DM.dcId = :dcId");
			sb.append("     AND EXISTS (");
			sb.append("         SELECT 1");
			sb.append("         FROM TsmpDc D");
			sb.append("         WHERE D.dcId = DM.dcId");
			sb.append("         AND D.active = :active");
			sb.append("     )");
			sb.append(" )");
			params.put("dcId", dcId);
			params.put("active", Boolean.TRUE);
		}
		
		// 關鍵字
		if (keyword != null && keyword.length > 0) {
			sb.append(" AND ( 1 = 2");
			for (int i = 0; i < keyword.length; i++) {
				sb.append("     OR UPPER(M.moduleName) LIKE :keyword" + i);
				params.put("keyword" + i, "%" + keyword[i].toUpperCase() + "%");
			}
			sb.append(" )");
		}
		
		sb.append(" ORDER BY M.moduleName asc, M.id asc");
		
		return doQuery(sb.toString(), params, TsmpApiModule.class);
	}

	public TsmpApiModule query_AA0403Service_01(Long id, String moduleName, boolean isV2, List<String> orgIdList) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		
		sb.append(" SELECT new tpi.dgrv4.entity.entity.jpql.TsmpApiModule(");
		sb.append("     M.id, M.moduleName, M.moduleVersion,");
		sb.append("     M.moduleAppClass, M.moduleMd5, M.moduleType,");
		sb.append("     M.uploadTime, M.uploaderName,");
		sb.append("     M.statusTime, M.statusUser,");
		sb.append("     M.active, M.nodeTaskId, M.v2Flag, M.orgId");
		sb.append(" )");
		sb.append(" FROM TsmpApiModule M");
		sb.append(" WHERE M.id = :id");
		sb.append(" AND M.moduleName = :moduleName");
		
		// 模組架構
		if (isV2) {
			sb.append(" AND (M.v2Flag IS NULL OR M.v2Flag = :v2Flag)");
			params.put("v2Flag", 1);
		} else {
			sb.append(" AND M.v2Flag = :v2Flag");
			params.put("v2Flag", 2);
		}
		
		// 組織原則
		sb.append(" AND (M.orgId IS NULL OR LENGTH(M.orgId) = 0");
		if (!CollectionUtils.isEmpty(orgIdList)) {
			sb.append(" OR M.orgId IN (:orgIdList)");
			params.put("orgIdList", orgIdList);
		}
		sb.append(" )");
		
		params.put("id", id);
		params.put("moduleName", moduleName);
		
		List<TsmpApiModule> mList = doQuery(sb.toString(), params, TsmpApiModule.class);
		if (mList != null && mList.size() > 0) {
			return mList.get(0);
		}
		return null;
	}

	public TsmpApiModule query_AA0404Service_01(Long id, String moduleName, List<String> orgIdList) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		
		sb.append(" SELECT new tpi.dgrv4.entity.entity.jpql.TsmpApiModule(");
		sb.append("     M.id, M.moduleName, M.moduleVersion,");
		sb.append("     M.moduleAppClass, M.moduleMd5, M.moduleType,");
		sb.append("     M.uploadTime, M.uploaderName,");
		sb.append("     M.statusTime, M.statusUser,");
		sb.append("     M.active, M.nodeTaskId, M.v2Flag, M.orgId");
		sb.append(" )");
		sb.append(" FROM TsmpApiModule M");
		sb.append(" WHERE M.id = :id");
		sb.append(" AND M.moduleName = :moduleName");
		
		// 組織原則
		sb.append(" AND (M.orgId IS NULL OR LENGTH(M.orgId) = 0");
		if (!CollectionUtils.isEmpty(orgIdList)) {
			sb.append(" OR M.orgId IN (:orgIdList)");
			params.put("orgIdList", orgIdList);
		}
		sb.append(" )");
		
		params.put("id", id);
		params.put("moduleName", moduleName);
		
		List<TsmpApiModule> mList = doQuery(sb.toString(), params, TsmpApiModule.class);
		if (mList != null && mList.size() > 0) {
			return mList.get(0);
		}
		return null;
	}

	public List<TsmpApiModule> query_AA0404Service_02(String moduleName, Long dcId) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		
		sb.append(" SELECT new tpi.dgrv4.entity.entity.jpql.TsmpApiModule(");
		sb.append("     M.id, M.moduleName, M.moduleVersion,");
		sb.append("     M.moduleAppClass, M.moduleMd5, M.moduleType,");
		sb.append("     M.uploadTime, M.uploaderName,");
		sb.append("     M.statusTime, M.statusUser,");
		sb.append("     M.active, M.nodeTaskId, M.v2Flag, M.orgId");
		sb.append(" )");
		sb.append(" FROM TsmpApiModule M");
		sb.append(" WHERE M.moduleName = :moduleName");
		sb.append(" AND EXISTS (");
		sb.append("     SELECT 1");
		sb.append("     FROM TsmpDcModule DM");
		sb.append("     WHERE DM.moduleId = M.id");
		sb.append("     AND DM.dcId = :dcId");
		sb.append(" )");

		params.put("moduleName", moduleName);
		params.put("dcId", dcId);
		
		return doQuery(sb.toString(), params, TsmpApiModule.class);
	}
	
	public List<TsmpApiModule> queryModuleVersion(){
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
 
		sb.append(" SELECT new tpi.dgrv4.entity.entity.jpql.TsmpApiModule(");
		sb.append("     M.id, M.moduleName, M.moduleVersion,");
		sb.append("     M.moduleAppClass, M.moduleMd5, M.moduleType,");
		sb.append("     M.uploadTime, M.uploaderName,");
		sb.append("     M.statusTime, M.statusUser,");
		sb.append("     M.active, M.nodeTaskId, M.v2Flag, M.orgId");
		sb.append(" )");
		sb.append(" FROM TsmpApiModule M ");
		sb.append(" WHERE (M.active = 1 and M.moduleName like 'tsmp%') or ");
		sb.append(" EXISTS ( SELECT 1 FROM TsmpDcModule tdm ");
		sb.append(" 	WHERE tdm.moduleId = M.id ");
		sb.append(" 	AND M.moduleName like 'tsmp%'");
		sb.append(" 	AND EXISTS ( SELECT 1 FROM TsmpDc tdc ");
		sb.append(" 		WHERE tdc.dcId = tdm.dcId ");
		sb.append(" 		AND tdc.dcCode = 'default') ");
		sb.append(" ) ORDER BY M.moduleName"); 
		
		return doQuery(sb.toString(), params, TsmpApiModule.class);
	}
	
	public List<TsmpApiModule> queryByModuleName(String moduleName) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		
		sb.append(" SELECT new tpi.dgrv4.entity.entity.jpql.TsmpApiModule(");
		sb.append("     M.id, M.moduleName, M.moduleVersion,");
		sb.append("     M.moduleAppClass, M.moduleMd5, M.moduleType,");
		sb.append("     M.uploadTime, M.uploaderName,");
		sb.append("     M.statusTime, M.statusUser,");
		sb.append("     M.active, M.nodeTaskId, M.v2Flag, M.orgId");
		sb.append(" )");
		sb.append(" FROM TsmpApiModule M");
		sb.append(" WHERE M.moduleName = :moduleName");

		params.put("moduleName", moduleName);
		
		return doQuery(sb.toString(), params, TsmpApiModule.class);
	}

	public List<TsmpApiModule> query_AA0404Service_03(String moduleName, Integer v2Flag) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		
		sb.append(" SELECT new tpi.dgrv4.entity.entity.jpql.TsmpApiModule(");
		sb.append("     M.id, M.moduleName, M.moduleVersion,");
		sb.append("     M.moduleAppClass, M.moduleMd5, M.moduleType,");
		sb.append("     M.uploadTime, M.uploaderName,");
		sb.append("     M.statusTime, M.statusUser,");
		sb.append("     M.active, M.nodeTaskId, M.v2Flag, M.orgId");
		sb.append(" )");
		sb.append(" FROM TsmpApiModule M");
		sb.append(" WHERE M.moduleName = :moduleName");
		if (v2Flag == null || v2Flag.equals(1)) {
			sb.append(" AND (M.v2Flag IS NULL or M.v2Flag = 1)");
		} else {
			sb.append(" AND M.v2Flag = :v2Flag");
			params.put("v2Flag", v2Flag);
		}
		params.put("moduleName", moduleName);
		return doQuery(sb.toString(), params, TsmpApiModule.class);
	}
	
	public List<TsmpApiModule> queryListByOrgId(String orgId) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();
		
		sb.append(" SELECT new tpi.dgrv4.entity.entity.jpql.TsmpApiModule(");
		sb.append("A.id, A.moduleName, A.moduleVersion, A.active");
		sb.append(") FROM TsmpApiModule A");
		sb.append(" WHERE 1 = 1");
		sb.append(" AND (A.orgId = :orgId or A.orgId is Null or LENGTH(A.orgId) = 0) ");
		sb.append(" ORDER BY A.moduleVersion ASC, A.id ASC");
		params.put("orgId", orgId);
		return doQuery(sb.toString(), params, TsmpApiModule.class);
	}
}
