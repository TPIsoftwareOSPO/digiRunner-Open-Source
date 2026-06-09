package tpi.dgrv4.entity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrSmartAuthSession;

/**
 * SMART 授權流程狀態的資料存取。
 */
@Repository
public interface DgrSmartAuthSessionDao extends JpaRepository<DgrSmartAuthSession, Long>, DgrSmartAuthSessionSuperDao {

    /**
     * 根據 OAuth state 查詢授權流程。
     * state 是整個授權流程的串接鍵（unique 約束）。
     */
    Optional<DgrSmartAuthSession> findByState(String state);

    /**
     * 根據授權碼查詢授權流程。
     * 用於 token endpoint 的 authorization_code 交換。
     */
    Optional<DgrSmartAuthSession> findByAuthCode(String authCode);

    /**
     * 根據 refresh token 查詢授權流程。
     * 用於 token endpoint 的 refresh_token grant 兌換。
     */
    Optional<DgrSmartAuthSession> findByRefreshToken(String refreshToken);
}
