package tpi.dgrv4.entity.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.converter.StringListJsonConverter;

/**
 * EHR Launch 啟動上下文暫存記錄。
 *
 * 背景：EHR 系統呼叫 Launch Registration API，建立一次性的 launch context，
 * 並取得不透明的 launch token。SMART App 啟動時帶上此 token，
 * 授權流程結束後即標記為已消費（consumed=true）。
 *
 * 設計決策：
 * - PK 為 UUID 字串（launch_token），不使用 DgrSeq 序號
 * - 短命記錄（5 分鐘效期），不需要樂觀鎖（無 @Version）
 * - 不實作 DgrSequenced（PK 非 Long）
 *
 * 生命週期：EHR 呼叫 /smart/launch 建立 → SMART App 啟動時消費 → expire_at 後可清除
 */
@Entity
@Table(name = "dgr_smart_launch_context")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class DgrSmartLaunchContext {

    // ==================== 主鍵 ====================

    /** 不透明的 launch token（UUID 格式），由 Launch Registration API 產生 */
    @Id
    @Column(name = "launch_token", length = 100, nullable = false)
    private String launchToken;

    // ==================== Launch Context ====================

    /** 發起 EHR Launch 的 SMART client ID */
    @Column(name = "client_id", nullable = false, length = 200)
    private String clientId;

    /** 患者上下文 ID（patient/ scope 消費時提供）；單一 ID（向下相容欄位） */
    @Column(name = "patient_id", length = 200)
    private String patientId;

    /**
     * Patient 候選 ID 清單（多選用）。
     * 0 個→流程中全域選；1 個→自動帶入；多個→流程中限定候選挑選。
     */
    @Column(name = "patient_ids", length = 2000)
    @Convert(converter = StringListJsonConverter.class)
    private List<String> patientIds;

    /** 就醫上下文 ID（launch/encounter scope 消費時提供） */
    @Column(name = "encounter_id", length = 200)
    private String encounterId;

    /**
     * Provider 候選 ID 清單。
     * fhirUser scope 啟用時的候選來源：0 個→流程中全域選；1 個→自動帶入；多個→流程中限定候選挑選。
     */
    @Column(name = "provider_ids", length = 2000)
    @Convert(converter = StringListJsonConverter.class)
    private List<String> providerIds;

    /**
     * fhirUser resource type：'patient' 或 'practitioner'（預設 'practitioner'）。
     * 區分 Provider EHR Launch 與 Patient Portal Launch。
     * scope `launch` 對兩者相同，無法靠 scope 區分，故由 launch 註冊時明確指定。
     */
    @Column(name = "fhir_user_type", length = 20)
    private String fhirUserType;

    // ==================== 狀態與效期 ====================

    /** 是否已被消費（false=尚未使用，true=已消費） */
    @Column(name = "consumed", nullable = false)
    private Boolean consumed = Boolean.FALSE;

    /** 到期時間（epoch millis），預設 now + 5 分鐘 */
    @Column(name = "expire_at", nullable = false)
    private Long expireAt;

    // ==================== 共用欄位 ====================

    @Column(name = "create_date_time")
    private Date createDateTime = DateTimeUtil.now();
}
