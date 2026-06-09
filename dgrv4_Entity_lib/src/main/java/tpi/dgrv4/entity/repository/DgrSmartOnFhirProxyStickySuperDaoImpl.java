package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxySticky;

public class DgrSmartOnFhirProxyStickySuperDaoImpl extends SuperDaoImpl<DgrSmartOnFhirProxySticky>
		implements DgrSmartOnFhirProxyStickySuperDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Class<DgrSmartOnFhirProxySticky> getEntityType() {
		return DgrSmartOnFhirProxySticky.class;
	}

	@Override
	public Page<DgrSmartOnFhirProxySticky> findByFiltersWithPagination(
			Long sofProxyId,
			Long sofProxyDiversionId,
			String sofProxyStickyType,
			List<String> keywords,
			Pageable pageable) {

		// 1. 建構過濾條件
		FilterResult filterResult = buildFilterConditions(sofProxyId, sofProxyDiversionId, sofProxyStickyType, keywords);

		// 2. 資料查詢
		String selectJpql = "SELECT s FROM DgrSmartOnFhirProxySticky s " +
				filterResult.getSqlClause() +
				buildOrderByClause(pageable.getSort());

		TypedQuery<DgrSmartOnFhirProxySticky> query = entityManager.createQuery(selectJpql, DgrSmartOnFhirProxySticky.class);
		setQueryParams(query, filterResult.getParams());

		query.setFirstResult((int) pageable.getOffset());
		query.setMaxResults(pageable.getPageSize());

		List<DgrSmartOnFhirProxySticky> resultList = query.getResultList();

		// 3. 總數查詢
		String countJpql = "SELECT COUNT(s) FROM DgrSmartOnFhirProxySticky s " + filterResult.getSqlClause();

		TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
		setQueryParams(countQuery, filterResult.getParams());

		long total = countQuery.getSingleResult();

		return new PageImpl<>(resultList, pageable, total);
	}

	private FilterResult buildFilterConditions(
			Long sofProxyId,
			Long sofProxyDiversionId,
			String sofProxyStickyType,
			List<String> keywords) {

		StringBuilder sb = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		sb.append("WHERE 1 = 1 ");

		// sofProxyId 精確匹配
		if (sofProxyId != null) {
			sb.append("AND s.sofProxyId = :sofProxyId ");
			params.put("sofProxyId", sofProxyId);
		}

		// sofProxyDiversionId 精確匹配
		if (sofProxyDiversionId != null) {
			sb.append("AND s.sofProxyDiversionId = :sofProxyDiversionId ");
			params.put("sofProxyDiversionId", sofProxyDiversionId);
		}

		// sofProxyStickyType 精確匹配
		if (StringUtils.hasText(sofProxyStickyType)) {
			sb.append("AND s.sofProxyStickyType = :sofProxyStickyType ");
			params.put("sofProxyStickyType", sofProxyStickyType);
		}

		// Keywords 模糊查詢（OR 邏輯）
		if (!CollectionUtils.isEmpty(keywords)) {
			sb.append("AND (");

			boolean first = true;
			for (int i = 0; i < keywords.size(); i++) {
				String keyword = keywords.get(i);

				if (!StringUtils.hasText(keyword)) {
					continue;
				}

				if (!first) {
					sb.append(" OR ");
				}

				String paramPrefix = "keyword" + i;

				sb.append("(");
				sb.append("UPPER(s.sofProxyStickyType) LIKE :").append(paramPrefix).append("Type OR ");
				params.put(paramPrefix + "Type", "%" + keyword.toUpperCase() + "%");

				sb.append("UPPER(s.sofProxyStickyTypeId) LIKE :").append(paramPrefix).append("TypeId OR ");
				params.put(paramPrefix + "TypeId", "%" + keyword.toUpperCase() + "%");

				sb.append("UPPER(s.sofProxyStickyPath) LIKE :").append(paramPrefix).append("Path");
				params.put(paramPrefix + "Path", "%" + keyword.toUpperCase() + "%");
				sb.append(")");

				first = false;
			}

			sb.append(") ");
		}

		return new FilterResult(sb.toString(), params);
	}

	private void setQueryParams(Query query, Map<String, Object> params) {
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			query.setParameter(entry.getKey(), entry.getValue());
		}
	}

	private String buildOrderByClause(Sort sort) {
		if (sort.isUnsorted()) {
			return " ORDER BY s.sofProxyStickyId DESC ";
		}

		StringBuilder orderBy = new StringBuilder(" ORDER BY ");
		boolean first = true;
		for (Sort.Order order : sort) {
			if (!first) {
				orderBy.append(", ");
			}
			orderBy.append("s.").append(order.getProperty()).append(" ");
			orderBy.append(order.getDirection().name()).append(" ");
			first = false;
		}
		return orderBy.toString();
	}

	private static class FilterResult {
		private final String sqlClause;
		private final Map<String, Object> params;

		public FilterResult(String sqlClause, Map<String, Object> params) {
			this.sqlClause = sqlClause;
			this.params = params;
		}

		public String getSqlClause() {
			return sqlClause;
		}

		public Map<String, Object> getParams() {
			return params;
		}
	}
}
