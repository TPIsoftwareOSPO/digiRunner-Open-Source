package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Smart on FHIR Proxy Diversion 批次更新專用 DTO
 * 用於增量更新 Diversion（新增、修改、刪除）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmartOnFhirProxyDiversionUpdateDto {

    /**
     * Diversion ID（選填）
     * - 有值 + version: 更新現有 Diversion
     * - 無值: 新增 Diversion
     * - 現有 ID 不在新列表中: 刪除該 Diversion
     */
    private Long sofProxyDiversionId;

    /**
     * 版本號（更新現有 Diversion 時必填，用於樂觀鎖）
     */
    private Long version;

    /**
     * Diversion URL（必填）
     */
    private String sofProxyDiversionUrl;

    /**
     * 權重（必填，範圍 1-100）
     */
    private Integer sofProxyDiversionProbability;

    // Getters and Setters

    public Long getSofProxyDiversionId() {
        return sofProxyDiversionId;
    }

    public void setSofProxyDiversionId(Long sofProxyDiversionId) {
        this.sofProxyDiversionId = sofProxyDiversionId;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getSofProxyDiversionUrl() {
        return sofProxyDiversionUrl;
    }

    public void setSofProxyDiversionUrl(String sofProxyDiversionUrl) {
        this.sofProxyDiversionUrl = sofProxyDiversionUrl;
    }

    public Integer getSofProxyDiversionProbability() {
        return sofProxyDiversionProbability;
    }

    public void setSofProxyDiversionProbability(Integer sofProxyDiversionProbability) {
        this.sofProxyDiversionProbability = sofProxyDiversionProbability;
    }

    @Override
    public String toString() {
        return "DgrSmartOnFhirProxyDiversionUpdateDto [sofProxyDiversionId=" + sofProxyDiversionId
                + ", version=" + version + ", sofProxyDiversionUrl=" + sofProxyDiversionUrl
                + ", sofProxyDiversionProbability=" + sofProxyDiversionProbability + "]";
    }
}
