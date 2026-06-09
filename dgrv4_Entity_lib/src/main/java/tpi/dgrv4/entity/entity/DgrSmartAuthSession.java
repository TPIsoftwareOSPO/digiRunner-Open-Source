package tpi.dgrv4.entity.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;
import tpi.dgrv4.entity.converter.StringListJsonConverter;

/**
 * SMART App Launch 授權流程狀態。
 *
 * 背景：每次 SMART authorize 請求建立一筆，存放該次授權流程的 SMART 專屬上下文
 * （aud、scope、PKCE、launch context 等）。用 state 串接 GTW IdP 登入流程。
 * Token 交換完成後可清除或標記為 EXCHANGED。
 *
 * 生命週期：authorize 請求建立（PENDING）→ 使用者登入完成（AUTHENTICATED）
 * → consent 同意（APPROVED）→ token 交換（EXCHANGED）
 */
@Entity
@Table(name = "dgr_smart_auth_session")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class DgrSmartAuthSession implements DgrSequenced {

    // ==================== 主鍵與流程串接 ====================

    @Id
    @DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
    @Column(name = "session_id")
    private Long sessionId;

    /** OAuth state，串接整個授權流程的唯一鍵 */
    @Column(name = "state", unique = true, nullable = false, length = 500)
    private String state;

    /** 發起授權的 client ID */
    @Column(name = "client_id", nullable = false, length = 200)
    private String clientId;

    // ==================== SMART 授權參數 ====================

    /** 目標 FHIR Server URL（防止 token 洩漏到錯誤的 resource server） */
    @Column(name = "aud", nullable = false, length = 1000)
    private String aud;

    /** 從 aud 解析出的 proxy 名稱 */
    @Column(name = "website_name", nullable = false, length = 100)
    private String websiteName;

    /** 從 aud 解析出的 FHIR base path */
    @Column(name = "fhir_base_path", nullable = false, length = 500)
    private String fhirBasePath;

    /** 請求的 scope */
    @Column(name = "requested_scope", nullable = false, length = 4000)
    @Convert(converter = StringListJsonConverter.class)
    private List<String> requestedScope;

    /** consent 後授予的 scope */
    @Column(name = "granted_scope", length = 4000)
    @Convert(converter = StringListJsonConverter.class)
    private List<String> grantedScope;

    /** 本次請求的 redirect URI */
    @Column(name = "redirect_uri", nullable = false, length = 1000)
    private String redirectUri;

    // ==================== PKCE ====================

    /** PKCE challenge 值，token 交換時用 code_verifier 驗證 */
    @Column(name = "code_challenge", length = 500)
    private String codeChallenge;

    /** PKCE 方法，僅接受 S256 */
    @Column(name = "code_challenge_method", length = 10)
    private String codeChallengeMethod;

    // ==================== Launch Context ====================

    /** EHR Launch 時由 EHR 提供的 launch context token */
    @Column(name = "launch", length = 500)
    private String launch;

    /** 患者上下文（授予 patient/ scope 時 MUST 提供）；最終單一選定值 */
    @Column(name = "patient_id", length = 200)
    private String patientId;

    /**
     * Patient 候選清單（從 launch context 帶入）。
     * 空清單 / null 代表沒有候選，consent 時需全域搜尋。
     */
    @Column(name = "patient_candidates", length = 2000)
    @Convert(converter = StringListJsonConverter.class)
    private List<String> patientCandidates;

    /** 就醫上下文（授予 launch/encounter scope 時提供） */
    @Column(name = "encounter_id", length = 200)
    private String encounterId;

    /**
     * Provider 候選 ID 清單（從 launch context 帶入）。
     * 空清單 / null 代表沒有候選，consent 時需全域搜尋。
     */
    @Column(name = "provider_candidates", length = 2000)
    @Convert(converter = StringListJsonConverter.class)
    private List<String> providerCandidates;

    /**
     * 最終選定的 fhirUser，FHIR Reference 格式（如 Practitioner/123 或 Patient/456）。
     * 簽進 id_token 的 fhirUser claim。consent 階段寫入。
     */
    @Column(name = "fhir_user", length = 500)
    private String fhirUser;

    /**
     * fhirUser resource type：'patient' 或 'practitioner'（預設 'practitioner'）。
     * 由 launch 階段帶入，決定 Provider EHR / Patient Portal Launch。
     */
    @Column(name = "fhir_user_type", length = 20)
    private String fhirUserType;

    // ==================== 授權碼 ====================

    /** 簽發的授權碼 */
    @Column(name = "auth_code", length = 200)
    private String authCode;

    /** 授權碼到期時間（epoch millis） */
    @Column(name = "auth_code_expire")
    private Long authCodeExpire;

    // ==================== 流程狀態 ====================

    /**
     * 流程階段。
     * - PENDING：authorize 請求已建立，等待使用者登入
     * - AUTHENTICATED：使用者已通過身份驗證，等待 consent
     * - APPROVED：使用者已同意，授權碼已簽發
     * - EXCHANGED：授權碼已兌換為 token，流程結束
     */
    @Column(name = "phase", nullable = false, length = 20)
    private String phase = "PENDING";

    // ==================== 使用者身份（IdP callback 後寫入） ====================

    /** 使用者帳號（來自 DgrGtwIdpAuthCode.userName） */
    @Column(name = "user_name", length = 200)
    private String userName;

    /** 使用者顯示名稱（來自 DgrGtwIdpAuthCode.userAlias） */
    @Column(name = "user_alias", length = 500)
    private String userAlias;

    /** 使用者信箱（來自 DgrGtwIdpAuthCode.userEmail） */
    @Column(name = "user_email", length = 500)
    private String userEmail;

    // ==================== Refresh Token（token endpoint 簽發後寫入） ====================

    /** opaque refresh token 值 */
    @Column(name = "refresh_token", length = 200)
    private String refreshToken;

    /** refresh token 到期時間（epoch millis） */
    @Column(name = "refresh_token_expire")
    private Long refreshTokenExpire;

    // ==================== 錯誤模擬 ====================

    /** Launcher 測試用：模擬的錯誤代碼（auth_*、token_*、request_*） */
    @Column(name = "sim_error", length = 100)
    private String simError;

    // ==================== 共用欄位 ====================

    @Column(name = "create_date_time")
    private Date createDateTime = DateTimeUtil.now();

    @Version
    @Column(name = "version")
    private Long version = 1L;

    @Override
    public Long getPrimaryKey() {
        return sessionId;
    }
}
