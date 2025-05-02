package tpi.dgrv4.entity.repository;

import jakarta.persistence.*;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.sql.internal.NativeQueryImpl;
import org.hibernate.transform.ResultTransformer;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.keeper.ITPILogger;
import tpi.dgrv4.common.utils.StackTraceUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.function.Function;

public abstract class BaseDao {

	protected static ITPILogger logger;

	@PersistenceContext
	private EntityManager entityManager;

	protected final <EntityType> List<EntityType> doQuery(String sql, Vector<Object> params, Class<EntityType> clazz) {
		return doQuery(sql, params, clazz, -1);
	}

	@SuppressWarnings({ "unchecked" })
	protected final <EntityType> List<EntityType> doQuery(String sql, Vector<Object> params, Class<EntityType> clazz,
			Integer pageSize) {
		return doQuery(sql, clazz, pageSize, (query) -> {
			for (int pos = 0; pos < params.size(); pos++) {
				query.setParameter((pos + 1), params.get(pos));
			}
			return query.getResultList();
		});
	}

	protected final <EntityType> List<EntityType> doQuery(String sql, Map<String, Object> params,
			Class<EntityType> clazz) {
		return doQuery(sql, params, clazz, -1);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected final <EntityType> List<EntityType> doQuery(String sql, Map<String, Object> params,
			Class<EntityType> clazz, Integer pageSize) {
		return doQuery(sql, clazz, pageSize, (query) -> {
			for (Parameter p : query.getParameters()) {
				query.setParameter(p, params.get(p.getName()));
			}
			return query.getResultList();
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected final <EntityType> List<EntityType> doQuery(String sql, Map<String, Object> params,
			Class<EntityType> clazz, Integer pageSize, Integer firstResult) {
		return doQuery(sql, clazz, pageSize, firstResult, (query) -> {
			for (Parameter p : query.getParameters()) {
				query.setParameter(p, params.get(p.getName()));
			}
			return query.getResultList();
		});
	}

	@SuppressWarnings("unchecked")
	private <EntityType> List<EntityType> doQuery(String sql, Class<EntityType> clazz, Integer pageSize,
			Function<Query, List<EntityType>> function) {
		Query query;

		try {
			query = entityManager.createQuery(sql, clazz);

			if (pageSize != null && pageSize > 0) {
				query.setMaxResults(pageSize);
			}

			return function.apply(query);
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
			return Collections.EMPTY_LIST;
		}
	}

	@SuppressWarnings("unchecked")
	private <EntityType> List<EntityType> doQuery(String sql, Class<EntityType> clazz, Integer pageSize,
			Integer firstResult, Function<Query, List<EntityType>> function) {
		Query query;

		try {
			query = entityManager.createQuery(sql, clazz);

			if (pageSize != null && pageSize > 0) {
				query.setMaxResults(pageSize);
			}

			if (firstResult != null) {
				query.setFirstResult(firstResult);
			}

			return function.apply(query);
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
			return Collections.EMPTY_LIST;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected int doNativeUpdate(String nativeSql, Map<String, Object> params) {
		Query query = entityManager.createNativeQuery(nativeSql);
		if (params != null && !params.isEmpty()) {
			for (Parameter p : query.getParameters()) {
				query.setParameter(p, params.get(p.getName()));
			}
		}
		return query.executeUpdate();
	}

	protected <EntityType> List<EntityType> doNativeQuery(String nativeSql, String resultSetMapping //
			, Class<EntityType> clazz) {
		return doNativeQuery(nativeSql, resultSetMapping, null, null, clazz);
	}

	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	protected <EntityType> List<EntityType> doNativeQuery(String nativeSql, String resultSetMapping //
			, Map<String, Object> params, Integer pageSize, Class<EntityType> clazz) {
		Query query = null;
		if (!StringUtils.hasLength(resultSetMapping) && clazz == null) {
			query = entityManager.createNativeQuery(nativeSql);
		} else if (StringUtils.hasLength(resultSetMapping)) {
			query = entityManager.createNativeQuery(nativeSql, resultSetMapping);
		} else {
			if("java.util.Map".equals(clazz.getName())) {
				 query = entityManager.createNativeQuery(nativeSql);
				//注意:回傳的key一律為小寫,會有CustomTransformers是因為根據DB造成KEY大小寫不同,如postgreSQL是小寫,其他為大寫,所以將它統一為小寫
				 try {
					 query.unwrap(NativeQueryImpl.class).setResultTransformer((ResultTransformer) CustomTransformers.ALIAS_TO_ENTITY_MAP);
				 }catch(Exception e) {
					 //mariadb使用NativeQueryImpl會出錯,所以用NativeQuery
					 query.unwrap(NativeQuery.class).setResultTransformer((ResultTransformer) CustomTransformers.ALIAS_TO_ENTITY_MAP);
				 }
			}else {
				query = entityManager.createNativeQuery(nativeSql, clazz);
			}

		}
		if (pageSize != null && pageSize > 0) {
			query.setMaxResults(pageSize);
		}
		if (params != null && !params.isEmpty()) {
			for (Parameter p : query.getParameters()) {
				query.setParameter(p, params.get(p.getName()));
			}
		}
		return query.getResultList();
	}

	protected EntityManager getEntityManager() {
		return entityManager;
	}

	public static void setLogger(ITPILogger logger) {
		BaseDao.logger = logger;
	}
}
