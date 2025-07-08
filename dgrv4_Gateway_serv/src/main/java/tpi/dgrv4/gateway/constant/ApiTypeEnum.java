package tpi.dgrv4.gateway.constant;

import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Optional;

@Getter
public enum ApiTypeEnum {
	AI_GATEWAY("ai-gateway"),
	WEBHOOK("webhook"),
	;

	private final String dgrProtocolExtensionId;

	ApiTypeEnum(String extensionId) {
		this.dgrProtocolExtensionId = extensionId;
	}

	private static final HashMap<String, ApiTypeEnum> map = new HashMap<>();

	static {
		for (var value:ApiTypeEnum.values()) {
			map.put(value.name().toUpperCase(), value);
		}
	}

	public static Optional<ApiTypeEnum> search(String apiType) {
		if (!StringUtils.hasLength(apiType)) return Optional.empty();
		return Optional.ofNullable(map.getOrDefault(apiType.toUpperCase(), null));
	}
}
