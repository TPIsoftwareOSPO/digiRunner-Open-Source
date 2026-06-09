package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Smart on FHIR Proxy Sticky 匯出匯入 DTO
 * 包含 diversionIndex 作為 diversionId 無法解析時的 fallback
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmartOnFhirProxyStickyImportDto {

    /** Sticky ID（Create 時可為 null，由系統產生；Update 時用於比對） */
    private String sofProxyStickyId;

    /** Diversion ID（匯出時為原始 ID；匯入時優先使用此欄位解析） */
    private String sofProxyDiversionId;

    /**
     * Diversion 在 diversionList 中的索引（0-based）
     * 匯出時自動計算；匯入時作為 sofProxyDiversionId 無法解析的 fallback
     */
    private Integer diversionIndex;

    /** FHIR Resource Type（必填，如 Patient、Observation） */
    private String sofProxyStickyType;

    /** FHIR Resource ID（選填） */
    private String sofProxyStickyTypeId;

    /** HTTP Method（選填，如 GET、POST） */
    private String sofProxyStickyVerb;

    /** HTTP URL Path（選填） */
    private String sofProxyStickyPath;

    /** FHIR Interaction 類型（選填，如 READ、CREATE、UPDATE） */
    private String sofProxyStickyInteraction;

    public String getSofProxyStickyId() {
        return sofProxyStickyId;
    }

    public void setSofProxyStickyId(String sofProxyStickyId) {
        this.sofProxyStickyId = sofProxyStickyId;
    }

    public String getSofProxyDiversionId() {
        return sofProxyDiversionId;
    }

    public void setSofProxyDiversionId(String sofProxyDiversionId) {
        this.sofProxyDiversionId = sofProxyDiversionId;
    }

    public Integer getDiversionIndex() {
        return diversionIndex;
    }

    public void setDiversionIndex(Integer diversionIndex) {
        this.diversionIndex = diversionIndex;
    }

    public String getSofProxyStickyType() {
        return sofProxyStickyType;
    }

    public void setSofProxyStickyType(String sofProxyStickyType) {
        this.sofProxyStickyType = sofProxyStickyType;
    }

    public String getSofProxyStickyTypeId() {
        return sofProxyStickyTypeId;
    }

    public void setSofProxyStickyTypeId(String sofProxyStickyTypeId) {
        this.sofProxyStickyTypeId = sofProxyStickyTypeId;
    }

    public String getSofProxyStickyVerb() {
        return sofProxyStickyVerb;
    }

    public void setSofProxyStickyVerb(String sofProxyStickyVerb) {
        this.sofProxyStickyVerb = sofProxyStickyVerb;
    }

    public String getSofProxyStickyPath() {
        return sofProxyStickyPath;
    }

    public void setSofProxyStickyPath(String sofProxyStickyPath) {
        this.sofProxyStickyPath = sofProxyStickyPath;
    }

    public String getSofProxyStickyInteraction() {
        return sofProxyStickyInteraction;
    }

    public void setSofProxyStickyInteraction(String sofProxyStickyInteraction) {
        this.sofProxyStickyInteraction = sofProxyStickyInteraction;
    }

    @Override
    public String toString() {
        return "SmartOnFhirProxyStickyImportDto [sofProxyStickyId=" + sofProxyStickyId
                + ", sofProxyDiversionId=" + sofProxyDiversionId + ", diversionIndex=" + diversionIndex
                + ", sofProxyStickyType=" + sofProxyStickyType + ", sofProxyStickyTypeId=" + sofProxyStickyTypeId
                + ", sofProxyStickyVerb=" + sofProxyStickyVerb + ", sofProxyStickyPath=" + sofProxyStickyPath
                + ", sofProxyStickyInteraction=" + sofProxyStickyInteraction + "]";
    }
}
