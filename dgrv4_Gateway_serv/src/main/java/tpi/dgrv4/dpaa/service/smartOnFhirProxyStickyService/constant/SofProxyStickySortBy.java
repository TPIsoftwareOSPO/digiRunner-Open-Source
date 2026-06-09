package tpi.dgrv4.dpaa.service.smartOnFhirProxyStickyService.constant;

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
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxySticky;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxySticky_;

public enum SofProxyStickySortBy {

	SOF_PROXY_STICKY_ID(DgrSmartOnFhirProxySticky_.SOF_PROXY_STICKY_ID, DgrSmartOnFhirProxySticky_.sofProxyStickyId),
	SOF_PROXY_STICKY_TYPE(DgrSmartOnFhirProxySticky_.SOF_PROXY_STICKY_TYPE, DgrSmartOnFhirProxySticky_.sofProxyStickyType),
	CREATE_DATE_TIME(DgrSmartOnFhirProxySticky_.CREATE_DATE_TIME, DgrSmartOnFhirProxySticky_.createDateTime),
	UPDATE_DATE_TIME(DgrSmartOnFhirProxySticky_.UPDATE_DATE_TIME, DgrSmartOnFhirProxySticky_.updateDateTime);

	private final String key;
	private final SingularAttribute<DgrSmartOnFhirProxySticky, ?> attr;

	SofProxyStickySortBy(String key, SingularAttribute<DgrSmartOnFhirProxySticky, ?> attr) {
		this.key = key;
		this.attr = attr;
	}

	public String key() {
		return key;
	}

	public SingularAttribute<DgrSmartOnFhirProxySticky, ?> attr() {
		return attr;
	}

	public static java.util.Optional<SofProxyStickySortBy> fromKey(String input) {
		if (input == null || input.isBlank())
			return java.util.Optional.empty();
		return Arrays.stream(values())
				.filter(v -> v.key.equals(input))
				.findFirst();
	}

	public static Set<String> allowedKeys() {
		return Arrays.stream(values()).map(SofProxyStickySortBy::key).collect(Collectors.toUnmodifiableSet());
	}

	public Order toOrder(CriteriaBuilder cb, Root<DgrSmartOnFhirProxySticky> root, Sort.Direction direction) {
		Path<?> path = root.get(attr);
		return (direction == Sort.Direction.ASC) ? cb.asc(path) : cb.desc(path);
	}

	public static Sort.Direction parseDirection(String sortOrder, Sort.Direction defaultDir) {
		if (sortOrder == null || sortOrder.isBlank())
			return defaultDir;
		return Sort.Direction.valueOf(sortOrder.trim().toUpperCase(Locale.ROOT));
	}
}
