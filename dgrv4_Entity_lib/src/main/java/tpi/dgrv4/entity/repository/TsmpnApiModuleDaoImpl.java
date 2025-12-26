package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.jpql.TsmpnApiModule;

public class TsmpnApiModuleDaoImpl extends BaseDao {
	// add custom methods here

	public List<TsmpnApiModule> queryActiveV3ModulesByModuleName(String moduleName) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		/* .NET模組不會部署在DC上, 而是部署在SITE上
		sb.append(" SELECT A FROM TsmpnApiModule A");
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
		*/
		sb.append(" select");
		sb.append(" new tpi.dgrv4.entity.entity.jpql.TsmpnApiModule(");
		sb.append("		A.id, A.moduleName, A.moduleVersion, A.active");
		sb.append(" )");
		sb.append(" from TsmpnApiModule A");
		sb.append(" where 1 = 1");
		sb.append(" and A.moduleName = :moduleName");
		sb.append(" and A.active = true");
		sb.append(" and exists (");
		sb.append(" 	select 1");
		sb.append(" 	from TsmpnSiteModule B");
		sb.append(" 	where B.moduleId = A.id");
		sb.append(" 	and exists (");
		sb.append(" 		select 1");
		sb.append(" 		from TsmpnSite C");
		sb.append(" 		where C.siteId = B.siteId");
		sb.append(" 		and C.active = true");
		sb.append(" 	)");
		sb.append(" )");

		params.put("moduleName", moduleName);
		return doQuery(sb.toString(), params, TsmpnApiModule.class);
	}
	
	public List<TsmpnApiModule> queryActiveModuleByModuleNameAndOrgId(String moduleName, String orgId) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		sb.append(" select");
		sb.append(" new tpi.dgrv4.entity.entity.jpql.TsmpnApiModule(");
		sb.append("		A.id, A.moduleName, A.moduleVersion, A.active");
		sb.append(" )");
		sb.append(" from TsmpnApiModule A");
		sb.append(" where 1 = 1");
		sb.append(" and A.moduleName = :moduleName");
		sb.append(" and (A.orgId = :orgId or A.orgId is Null or LENGTH(A.orgId) = 0)");
		/* 20210122; 修正邏輯
		sb.append(" and A.active = true");
		*/
		sb.append(" and exists (");
		sb.append(" 	select 1");
		sb.append(" 	from TsmpnSiteModule B");
		sb.append(" 	where B.moduleId = A.id");
		sb.append(" 	and exists (");
		sb.append(" 		select 1");
		sb.append(" 		from TsmpnSite C");
		sb.append(" 		where C.siteId = B.siteId");
		sb.append(" 		and C.active = true");
		sb.append(" 	)");
		sb.append(" )");

		params.put("moduleName", moduleName);
		params.put("orgId", orgId);
		return doQuery(sb.toString(), params, TsmpnApiModule.class);
	}
	
	public List<TsmpnApiModule> queryByModuleNameAndOrgIdOrderByUploadTimeDesc(String moduleName, String orgId) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		sb.append(" select");
		sb.append(" new tpi.dgrv4.entity.entity.jpql.TsmpnApiModule(");
		sb.append("		A.id, A.moduleName, A.moduleVersion, A.active");
		sb.append(" )");
		sb.append(" from TsmpnApiModule A");
		sb.append(" where 1 = 1");
		sb.append(" and A.moduleName = :moduleName");
		sb.append(" and (A.orgId = :orgId or A.orgId is Null or LENGTH(A.orgId) = 0)");
		sb.append(" order by A.uploadTime desc");

		params.put("moduleName", moduleName);
		params.put("orgId", orgId);
		return doQuery(sb.toString(), params, TsmpnApiModule.class);
	}

	public List<TsmpnApiModule> queryListByOrgId(String orgId) {
		Map<String, Object> params = new HashMap<>();
		StringBuffer sb = new StringBuffer();

		sb.append(" select");
		sb.append(" new tpi.dgrv4.entity.entity.jpql.TsmpnApiModule(");
		sb.append("		A.id, A.moduleName, A.moduleVersion, A.active");
		sb.append(" )");
		sb.append(" from TsmpnApiModule A");
		sb.append(" where 1 = 1");
		sb.append(" and (A.orgId = :orgId or A.orgId is Null or LENGTH(A.orgId) = 0)");
		params.put("orgId", orgId);
		return doQuery(sb.toString(), params, TsmpnApiModule.class);
	}

}
