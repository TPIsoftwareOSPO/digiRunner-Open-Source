package tpi.dgrv4.entity.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrSmartClient;

/**
 * SMART client 設定的資料存取。
 */
@Repository
public interface DgrSmartClientDao extends JpaRepository<DgrSmartClient, Long>, DgrSmartClientSuperDao {

    /**
     * 根據 OAuth client ID 查詢 SMART 設定。
     * 一個 client ID 最多一筆 SMART 設定（unique 約束）。
     */
    Optional<DgrSmartClient> findByClientId(String clientId);

    /**
     * 根據 client ID 模糊查詢（分頁）。
     * 用於管理介面的列表篩選。
     */
    Page<DgrSmartClient> findByClientIdContainingIgnoreCase(String keyword, Pageable pageable);
}
