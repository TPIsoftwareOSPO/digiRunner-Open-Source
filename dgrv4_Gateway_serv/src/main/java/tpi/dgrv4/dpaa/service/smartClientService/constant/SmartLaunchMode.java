package tpi.dgrv4.dpaa.service.smartClientService.constant;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SMART App Launch 啟動模式。
 *
 * - standalone：App 從 EHR 外部啟動
 * - ehr：App 在 EHR 內啟動（EHR 提供 launch context）
 * - both：兩者都支援
 */
public enum SmartLaunchMode {

    STANDALONE("standalone"),
    EHR("ehr"),
    BOTH("both");

    private final String key;

    SmartLaunchMode(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    public static Optional<SmartLaunchMode> fromKey(String input) {
        if (input == null) {
            return Optional.empty();
        }
        String trimmed = input.trim();
        for (SmartLaunchMode value : values()) {
            if (value.key.equalsIgnoreCase(trimmed)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    public static Set<String> allowedKeys() {
        return Arrays.stream(values())
            .map(SmartLaunchMode::key)
            .collect(Collectors.toSet());
    }
}
