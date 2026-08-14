package tpi.dgrv4.dpaa.service.smartOnFhirProxyService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * sofProxyAuth 值域常數與正規化工具。
 * 值域為 none / digirunner / third-party；"dgr" 為舊值，讀取相容、寫入時正規化為 "digirunner"。
 */
final class SofProxyAuthValues {

    static final String NONE = "none";
    static final String DIGIRUNNER = "digirunner";
    /** 舊值（digirunner 的前身），僅為相容既有資料與舊 client 而保留 */
    static final String LEGACY_DGR = "dgr";
    static final String THIRD_PARTY = "third-party";

    /** auth 允許值（list 實際單值）；含舊值 dgr——update 驗證的是 entity 最終狀態，未觸及 auth 欄位的舊資料仍可能為 dgr */
    static final Set<String> ALLOWED = Set.of(NONE, DIGIRUNNER, LEGACY_DGR, THIRD_PARTY);

    private SofProxyAuthValues() {
    }

    /** 寫入前正規化：舊值 dgr（不分大小寫）改寫為 digirunner，其餘原樣；null 回 null */
    static List<String> normalize(List<String> authList) {
        if (authList == null) {
            return null;
        }
        List<String> result = new ArrayList<>(authList.size());
        for (String a : authList) {
            result.add(LEGACY_DGR.equalsIgnoreCase(a) ? DIGIRUNNER : a);
        }
        return result;
    }
}
