package tpi.dgrv4.dpaa.service.smartClientService.constant;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import tpi.dgrv4.entity.entity.DgrSmartClient_;

/**
 * SMART Client 查詢的合法排序欄位。
 *
 * 欄位名稱取自 JPA Metamodel（{@link DgrSmartClient_}），
 * Entity 欄位更名時會觸發編譯錯誤，避免 runtime property not found。
 */
public enum SmartClientSortBy {

    SMART_CLIENT_ID(DgrSmartClient_.SMART_CLIENT_ID),
    CLIENT_ID(DgrSmartClient_.CLIENT_ID),
    CREATE_DATE_TIME(DgrSmartClient_.CREATE_DATE_TIME);

    private final String key;

    SmartClientSortBy(String key) {
        this.key = key;
    }

    public String key() {
        return key;
    }

    /**
     * 從輸入字串比對排序欄位（忽略大小寫）。
     *
     * @param input 前端傳入的排序欄位名稱
     * @return 比對成功回傳對應的 enum 值，否則回傳 empty
     */
    public static Optional<SmartClientSortBy> fromKey(String input) {
        if (input == null) {
            return Optional.empty();
        }
        String trimmed = input.trim();
        for (SmartClientSortBy value : values()) {
            if (value.key.equalsIgnoreCase(trimmed)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    /**
     * 回傳所有合法的排序欄位名稱，用於錯誤訊息提示。
     */
    public static Set<String> allowedKeys() {
        return Arrays.stream(values())
            .map(SmartClientSortBy::key)
            .collect(Collectors.toSet());
    }
}
