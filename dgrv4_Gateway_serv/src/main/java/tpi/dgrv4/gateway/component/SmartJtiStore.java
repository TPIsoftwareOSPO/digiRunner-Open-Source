package tpi.dgrv4.gateway.component;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * JWT ID（jti）重放防護 Store。
 *
 * 背景：Backend Services 的 private_key_jwt client assertion 必須包含唯一的 jti，
 * 伺服器必須拒絕已出現過的 jti，防止 replay attack（SMART v2 §7.1）。
 *
 * 設計：
 * - ConcurrentHashMap&lt;String, Long&gt;（key=jti, value=過期時間戳 ms）
 * - markUsed：寫入 map
 * - isUsed：查 map
 * - @Scheduled cleanup：每分鐘掃描移除已過期的 entry，避免記憶體無限成長
 */
@Component
public class SmartJtiStore {

    /** key=jti, value=過期時間戳（毫秒） */
    private final ConcurrentHashMap<String, Long> usedJtis = new ConcurrentHashMap<>();

    /**
     * 標記 jti 已使用。
     *
     * @param jti        JWT ID
     * @param expiration jti 的過期時間（到期後 cleanup 可移除）
     */
    public void markUsed(String jti, Date expiration) {
        usedJtis.put(jti, expiration.getTime());
    }

    /**
     * 檢查 jti 是否已被使用。
     *
     * @param jti JWT ID
     * @return true 表示已使用（應拒絕），false 表示未使用
     */
    public boolean isUsed(String jti) {
        return usedJtis.containsKey(jti);
    }

    /**
     * 原子性地標記 jti 為已使用（若尚未使用）。
     * 使用 ConcurrentHashMap.putIfAbsent 避免 check-then-act 競態條件。
     *
     * @param jti        JWT ID
     * @param expiration jti 的過期時間
     * @return true 表示標記成功（jti 未被使用過），false 表示 jti 已存在（應拒絕）
     */
    public boolean markIfUnused(String jti, Date expiration) {
        return usedJtis.putIfAbsent(jti, expiration.getTime()) == null;
    }

    /**
     * 清除已過期的 jti entry。
     *
     * 由 @Scheduled 每分鐘觸發，也可在 unit test 中直接呼叫驗證行為。
     */
    @Scheduled(fixedRate = 60_000L)
    public void cleanup() {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> it = usedJtis.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> entry = it.next();
            if (entry.getValue() <= now) {
                it.remove();
            }
        }
    }
}
