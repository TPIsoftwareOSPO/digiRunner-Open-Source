package tpi.dgrv4.entity.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import tpi.dgrv4.entity.constant.CodeEnums;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxy;

public class DgrSmartOnFhirProxySuperDaoImpl extends SuperDaoImpl<DgrSmartOnFhirProxy>
        implements DgrSmartOnFhirProxySuperDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Class<DgrSmartOnFhirProxy> getEntityType() {
        return DgrSmartOnFhirProxy.class;
    }

    @Override
    public Page<DgrSmartOnFhirProxy> findByFiltersWithPagination(
            List<String> keywords,
            String sofProxyStatus,
            Pageable pageable) {

        // 1. 取得共用的過濾條件與參數
        FilterResult filterResult = buildFilterConditions(keywords, sofProxyStatus);

        // 2. 構建並執行資料查詢 (SELECT)
        String selectJpql = "SELECT DISTINCT p FROM DgrSmartOnFhirProxy p " + 
                            filterResult.getSqlClause() + 
                            buildOrderByClause(pageable.getSort());
        
        TypedQuery<DgrSmartOnFhirProxy> query = entityManager.createQuery(selectJpql, DgrSmartOnFhirProxy.class);
        setQueryParams(query, filterResult.getParams());
        
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        
        List<DgrSmartOnFhirProxy> resultList = query.getResultList();

        // 3. 構建並執行總數查詢 (COUNT)
        String countJpql = "SELECT COUNT(DISTINCT p) FROM DgrSmartOnFhirProxy p " + filterResult.getSqlClause();
        
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        setQueryParams(countQuery, filterResult.getParams());
        
        long total = countQuery.getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }

    @Override
    public List<DgrSmartOnFhirProxy> findByFiltersForExport(
            List<String> keywords,
            String sofProxyStatus) {

        // 1. 取得共用的過濾條件與參數
        FilterResult filterResult = buildFilterConditions(keywords, sofProxyStatus);

        // 2. 構建查詢 (Export 預設按 ID 排序)
        String jpql = "SELECT DISTINCT p FROM DgrSmartOnFhirProxy p " + 
                      filterResult.getSqlClause() + 
                      " ORDER BY p.sofProxyId DESC";

        TypedQuery<DgrSmartOnFhirProxy> query = entityManager.createQuery(jpql, DgrSmartOnFhirProxy.class);
        setQueryParams(query, filterResult.getParams());

        return query.getResultList();
    }

    private FilterResult buildFilterConditions(
            List<String> keywords,
            String sofProxyStatus) {

        StringBuilder sb = new StringBuilder();
        Map<String, Object> params = new HashMap<>();

        sb.append("WHERE 1 = 1 ");

        // Keywords 篩選（OR 邏輯）
        if (!CollectionUtils.isEmpty(keywords)) {
            sb.append("AND (");
            
            for (int i = 0; i < keywords.size(); i++) {
                String keyword = keywords.get(i);
                
                if (!StringUtils.hasText(keyword)) {
                    continue;
                }
                
                if (i > 0) {
                    sb.append(" OR ");
                }
                
                String paramPrefix = "keyword" + i;
                Long keywordAsLong = tryParseLong(keyword);
                
                sb.append("(");
                
                // sofProxyId 精確匹配
                if (keywordAsLong != null) {
                    sb.append("p.sofProxyId = :").append(paramPrefix).append("Id OR ");
                    params.put(paramPrefix + "Id", keywordAsLong);
                }
                
                // sofProxyName 模糊查詢
                sb.append("UPPER(p.sofProxyName) LIKE :").append(paramPrefix).append("Name OR ");
                params.put(paramPrefix + "Name", "%" + keyword.toUpperCase() + "%");
                
                // sofProxyRemark 模糊查詢
                sb.append("UPPER(p.sofProxyRemark) LIKE :").append(paramPrefix).append("Remark OR ");
                params.put(paramPrefix + "Remark", "%" + keyword.toUpperCase() + "%");
                
                sb.append("EXISTS (")
                  .append("SELECT 1 FROM DgrSmartOnFhirProxyDiversion d ")
                  .append("WHERE d.sofProxyId = p.sofProxyId ")
                  .append("AND UPPER(d.sofProxyDiversionUrl) LIKE :").append(paramPrefix).append("Url")
                  .append(")");
                params.put(paramPrefix + "Url", "%" + keyword.toUpperCase() + "%");
                
                sb.append(")");
            }
            
            sb.append(") ");
        }

        // 狀態篩選
        if (StringUtils.hasText(sofProxyStatus)) {
            Optional<DgrSmartOnFhirProxy.Status> statusOpt = 
                CodeEnums.tryFromCode(DgrSmartOnFhirProxy.Status.class, sofProxyStatus);
            if (statusOpt.isPresent()) {
                sb.append("AND p.sofProxyStatus = :sofProxyStatus ");
                params.put("sofProxyStatus", statusOpt.get());
            }
        }

        return new FilterResult(sb.toString(), params);
    }

    /**
     * 嘗試將字串轉為 Long，失敗則返回 null
     */
    private Long tryParseLong(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void setQueryParams(Query query, Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
    }

    private String buildOrderByClause(Sort sort) {
        if (sort.isUnsorted()) {
            return " ORDER BY p.sofProxyId DESC ";
        }

        StringBuilder orderBy = new StringBuilder(" ORDER BY ");
        boolean first = true;
        for (Sort.Order order : sort) {
            if (!first) {
                orderBy.append(", ");
            }
            orderBy.append("p.").append(order.getProperty()).append(" ");
            orderBy.append(order.getDirection().name()).append(" ");
            first = false;
        }
        return orderBy.toString();
    }

    /**
     * 內部類別:用於封裝 SQL 片段與參數
     */
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
