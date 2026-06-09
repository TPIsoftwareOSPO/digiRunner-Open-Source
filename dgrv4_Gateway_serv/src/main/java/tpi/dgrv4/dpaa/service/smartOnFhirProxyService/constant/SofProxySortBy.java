package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.constant;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.SingularAttribute;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxy;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxy_;

public enum SofProxySortBy {

    SOF_PROXY_ID(DgrSmartOnFhirProxy_.SOF_PROXY_ID, DgrSmartOnFhirProxy_.sofProxyId),
    CREATE_DATE_TIME(DgrSmartOnFhirProxy_.CREATE_DATE_TIME, DgrSmartOnFhirProxy_.createDateTime),
    UPDATE_DATE_TIME(DgrSmartOnFhirProxy_.UPDATE_DATE_TIME, DgrSmartOnFhirProxy_.updateDateTime);

    private final String key; // DTO 傳入的 sortBy 值（對外 key）
    private final SingularAttribute<DgrSmartOnFhirProxy, ?> attr; // 對應 entity metamodel

    SofProxySortBy(String key, SingularAttribute<DgrSmartOnFhirProxy, ?> attr) {
        this.key = key;
        this.attr = attr;
    }

    public String key() {
        return key;
    }

    public SingularAttribute<DgrSmartOnFhirProxy, ?> attr() {
        return attr;
    }

    /** DTO sortBy 解析成 enum（不合法回 empty） */
    public static java.util.Optional<SofProxySortBy> fromKey(String input) {
        if (input == null || input.isBlank())
            return java.util.Optional.empty();
        return Arrays.stream(values())
                .filter(v -> v.key.equals(input))
                .findFirst();
    }

    public static Set<String> allowedKeys() {
        return Arrays.stream(values()).map(SofProxySortBy::key).collect(Collectors.toUnmodifiableSet());
    }

    /** 產生 Criteria Order（只會用白名單欄位） */
    public Order toOrder(CriteriaBuilder cb, Root<DgrSmartOnFhirProxy> root, Sort.Direction direction) {
        Path<?> path = root.get(attr);
        return (direction == Sort.Direction.ASC) ? cb.asc(path) : cb.desc(path);
    }

    /** sortOrder 解析（可接受 asc/desc/ASC/DESC） */
    public static Sort.Direction parseDirection(String sortOrder, Sort.Direction defaultDir) {
        if (sortOrder == null || sortOrder.isBlank())
            return defaultDir;
        return Sort.Direction.valueOf(sortOrder.trim().toUpperCase(Locale.ROOT));
    }
}
