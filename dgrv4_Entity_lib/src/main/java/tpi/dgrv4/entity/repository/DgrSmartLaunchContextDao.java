package tpi.dgrv4.entity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import tpi.dgrv4.entity.entity.DgrSmartLaunchContext;

/**
 * EHR Launch 啟動上下文的資料存取。
 */
@Repository
public interface DgrSmartLaunchContextDao extends JpaRepository<DgrSmartLaunchContext, String> {

    /**
     * 根據 launch token 查詢啟動上下文。
     * launch_token 是 PK，此查詢語意等同 findById，但命名更清晰。
     *
     * @param launchToken EHR Launch Registration API 產生的不透明 token
     * @return 啟動上下文（Optional）
     */
    Optional<DgrSmartLaunchContext> findByLaunchToken(String launchToken);

    /**
     * 原子消費 launch token：僅在 consumed = false 時才更新為 true。
     *
     * 設計目的：防止 TOCTOU race condition。兩個並行請求都通過
     * consumed = false 的讀取檢查後，只有一個能成功執行此更新（
     * affected rows = 1），另一個會得到 0，系統據此拋出錯誤。
     *
     * 呼叫端必須搭配 {@code @Transactional}，否則 {@code @Modifying}
     * 查詢不會生效。
     *
     * @param launchToken 目標 launch token
     * @return 受影響列數（1 = 成功消費；0 = 已被搶先消費）
     */
    @Transactional
    @Modifying
    @Query("UPDATE DgrSmartLaunchContext c SET c.consumed = true WHERE c.launchToken = :token AND c.consumed = false")
    int consumeByLaunchToken(@Param("token") String launchToken);
}
