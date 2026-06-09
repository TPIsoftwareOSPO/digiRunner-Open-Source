package tpi.dgrv4.gateway.component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * SMART scope 轉人類可讀描述元件。
 *
 * 對外介面：{@link #describe(String)} 接收空格分隔的 SMART scope 字串，
 * 回傳每個 scope 對應的 {@link ScopeDescription}（scope 原文 + 中文描述）。
 *
 * 解析優先順序：
 * 1. 靜態特殊 scope 對照表（openid、fhirUser 等 7 種）
 * 2. 資源 scope 動態解析（{@code {context}/{ResourceType}.{operations}} 格式）
 * 3. 不認識的 scope → description 等於 scope 本身（避免前端顯示空白）
 */
@Component
public class SmartScopeDescriber {

    /** 靜態特殊 scope 對照表，順序決定 UI 顯示優先度。 */
    private static final Map<String, String> STATIC_SCOPE_MAP = new LinkedHashMap<>();

    static {
        STATIC_SCOPE_MAP.put("openid", "存取您的身份資訊");
        STATIC_SCOPE_MAP.put("profile", "存取您的基本個人資料");
        STATIC_SCOPE_MAP.put("fhirUser", "存取您的 FHIR 使用者資料");
        STATIC_SCOPE_MAP.put("offline_access", "在您離線時仍可存取資料");
        STATIC_SCOPE_MAP.put("online_access", "僅在您使用期間存取資料");
        STATIC_SCOPE_MAP.put("launch", "取得 EHR 啟動時的上下文資訊");
        STATIC_SCOPE_MAP.put("launch/patient", "取得您選擇的病患身份");
        STATIC_SCOPE_MAP.put("launch/encounter", "取得目前的就醫紀錄");
    }

    /**
     * 將空格分隔的 SMART scope 字串轉換為中文描述清單。
     *
     * @param spaceSeparatedScopes 空格分隔的 scope 字串，可為 null 或空白
     * @return 每個 scope 的描述清單；null 或空白輸入回傳空清單
     */
    public List<ScopeDescription> describe(String spaceSeparatedScopes) {
        if (!StringUtils.hasText(spaceSeparatedScopes)) {
            return List.of();
        }

        List<ScopeDescription> result = new ArrayList<>();
        String[] scopes = spaceSeparatedScopes.trim().split("\\s+");

        for (String scope : scopes) {
            if (scope.isBlank()) {
                continue;
            }
            result.add(new ScopeDescription(scope, describeOne(scope)));
        }

        return result;
    }

    // ==================== 內部解析 ====================

    /**
     * 解析單一 scope，回傳對應的中文描述。
     *
     * 靜態 scope 優先，接著嘗試資源 scope 格式，最後 fallback 到 scope 原文。
     */
    private String describeOne(String scope) {
        // 1. 靜態對照
        String staticDesc = STATIC_SCOPE_MAP.get(scope);
        if (staticDesc != null) {
            return staticDesc;
        }

        // 2. 資源 scope 動態解析：{context}/{ResourceType}.{operations}
        int slashIdx = scope.indexOf('/');
        int dotIdx = scope.indexOf('.');
        if (slashIdx > 0 && dotIdx > slashIdx) {
            String context = scope.substring(0, slashIdx);
            String resourceType = scope.substring(slashIdx + 1, dotIdx);
            String operations = scope.substring(dotIdx + 1);

            String contextLabel = resolveContextLabel(context);
            if (contextLabel != null && !resourceType.isBlank() && !operations.isBlank()) {
                String opsLabel = resolveOperationsLabel(operations);
                // 萬用字元不加空格：「完整存取所有資源」；一般資源名稱前加空格：「讀取、搜尋 Observation 資源」
                String resourceLabel = "*".equals(resourceType)
                        ? "所有資源"
                        : " " + resourceType + " 資源";
                return contextLabel + opsLabel + resourceLabel;
            }
        }

        // 3. 不認識的 scope，原樣回傳
        return scope;
    }

    /**
     * 將 context 字串轉換為中文層級前綴。
     *
     * @return 中文前綴（含「以」和「層級」），若 context 不在已知清單則回傳 null
     */
    private String resolveContextLabel(String context) {
        return switch (context) {
            case "patient" -> "以病患層級";
            case "user"    -> "以使用者層級";
            case "system"  -> "以系統層級";
            default        -> null;
        };
    }

    /**
     * 將 operations 字串轉換為中文動作描述。
     *
     * cruds（全集）→「完整存取」；其他依字元逐一對應後以頓號串接。
     */
    private String resolveOperationsLabel(String operations) {
        // cruds 全集 → 完整存取
        if (containsAll(operations, "cruds")) {
            return "完整存取";
        }

        List<String> labels = new ArrayList<>();
        if (operations.contains("c")) labels.add("建立");
        if (operations.contains("r")) labels.add("讀取");
        if (operations.contains("u")) labels.add("更新");
        if (operations.contains("d")) labels.add("刪除");
        if (operations.contains("s")) labels.add("搜尋");

        if (labels.isEmpty()) {
            return operations;  // 無法解析時原樣回傳
        }

        return String.join("、", labels);
    }

    /** 檢查 operations 是否包含所有指定字元（順序無關）。 */
    private boolean containsAll(String operations, String required) {
        for (char c : required.toCharArray()) {
            if (operations.indexOf(c) < 0) {
                return false;
            }
        }
        return true;
    }

    // ==================== Record 型別 ====================

    /**
     * 單一 scope 的描述結果。
     *
     * @param scope       SMART scope 字串（原文）
     * @param description 對應的中文描述
     */
    public record ScopeDescription(String scope, String description) {}
}
