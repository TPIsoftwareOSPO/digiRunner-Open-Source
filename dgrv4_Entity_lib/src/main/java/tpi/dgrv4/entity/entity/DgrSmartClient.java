package tpi.dgrv4.entity.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
 * SMART App Launch client 設定。
 *
 * 背景：SMART client 的靜態註冊資料獨立於 tsmp_client，
 * 避免 SMART 專屬欄位（allowed_scopes、redirect_uris、jwks 等）污染既有的 client 表。
 * clientId 對應 tsmp_client.client_id，DgrSmartClient 是 tsmp_client 的 SMART 擴充設定。
 *
 * 規格依據：HL7 SMART App Launch STU2.2
 */
@Entity
@Table(name = "dgr_smart_client")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class DgrSmartClient implements DgrSequenced {

    // ==================== 主鍵 ====================

    @Id
    @DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
    @Column(name = "smart_client_id")
    private Long smartClientId;

    // ==================== Client 關聯與認證 ====================

    /** Client ID，對應 tsmp_client.client_id */
    @Column(name = "client_id", unique = true, nullable = false)
    private String clientId;

    /**
     * 認證方式。
     * - public：無密鑰（瀏覽器 SPA、原生應用）
     * - confidential-symmetric：client_secret（HTTP Basic Auth）
     * - confidential-asymmetric：JWT assertion + 公鑰（Backend Services 必須用此方式）
     */
    @Column(name = "client_type", nullable = false)
    private String clientType;

    /**
     * 身份驗證方式，決定 SMART authorize 流程中使用者如何登入。
     * 對應 GTW IdP 的 idPType：GOOGLE / MS / OIDC / LDAP / JDBC / API / CUS
     */
    @Column(name = "idp_type", nullable = false)
    private String idpType;

    /** 用來做 ssotoken 第二段驗證的 client ID，對應 tsmp_client.client_id */
    @Column(name = "idp_client_id", nullable = false)
    private String idpClientId;

    /**
     * Token endpoint 的認證方式。
     * client_secret_basic / client_secret_post / private_key_jwt
     */
    @Column(name = "token_endpoint_auth_method")
    private String tokenEndpointAuthMethod;

    // ==================== SMART scope 與授權 ====================

    /** 該 client 被授權的 SMART scope */
    @Column(name = "allowed_scopes", nullable = false, length = 4000)
    @Convert(converter = StringListJsonConverter.class)
    private List<String> allowedScopes;

    /** SMART 專用 redirect URI */
    @Column(name = "redirect_uris", length = 4000)
    @Convert(converter = StringListJsonConverter.class)
    private List<String> redirectUris;

    /**
     * 啟動模式。
     * - standalone：App 從 EHR 外部啟動
     * - ehr：App 在 EHR 內啟動（EHR 提供 launch context）
     * - both：兩者都支援
     */
    @Column(name = "launch_mode")
    private String launchMode;

    /** 是否跳過 consent 畫面（Y/N）。EHR Launch 或受信任 App 可設為 Y */
    @Column(name = "auto_approve", nullable = false)
    private String autoApprove = "N";

    // ==================== 非對稱認證（JWK） ====================

    /**
     * Client 的 JWK Set URL。
     * Server 從此 URL 動態抓取 client 公鑰，支援 client 自行輪換金鑰。
     * 規範 SHALL support（SMART Asymmetric Auth §Registration）。
     */
    @Column(name = "jwks_uri", length = 1000)
    private String jwksUri;

    /**
     * Client 的 JWK Set JSON（直接存放）。
     * 用於離線環境或無法提供 JWKS URL 的 client。
     * 規範 SHALL support（SMART Asymmetric Auth §Registration），但 strongly discouraged。
     */
    @Lob
    @Column(name = "jwks")
    private String jwks;

    // ==================== 共用欄位 ====================

    @Column(name = "create_date_time")
    private Date createDateTime = DateTimeUtil.now();

    @Column(name = "create_user")
    private String createUser = "SYSTEM";

    @Column(name = "update_date_time")
    private Date updateDateTime;

    @Column(name = "update_user")
    private String updateUser;

    @Version
    @Column(name = "version")
    private Long version = 1L;

    @Override
    public Long getPrimaryKey() {
        return smartClientId;
    }
}
