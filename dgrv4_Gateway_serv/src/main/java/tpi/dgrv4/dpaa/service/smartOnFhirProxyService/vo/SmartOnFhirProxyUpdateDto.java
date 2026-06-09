package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo;

import java.util.List;

import org.openapitools.jackson.nullable.JsonNullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Smart on FHIR Proxy 批次更新專用 DTO
 * 使用 JsonNullable 實現部分更新（區分不傳/清空/更新三種狀態）
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmartOnFhirProxyUpdateDto {

    // 必填欄位
    private String sofProxyId;
    private String version;

    // 可選更新欄位（使用 JsonNullable 包裝）
    private JsonNullable<String> sofProxyName = JsonNullable.undefined();
    private JsonNullable<String> sofProxyStatus = JsonNullable.undefined();
    private JsonNullable<String> sofProxyRemark = JsonNullable.undefined();
    private JsonNullable<String> sofProxyAccessToken = JsonNullable.undefined();
    private JsonNullable<String> sofProxySqlInjection = JsonNullable.undefined();
    private JsonNullable<String> sofProxyTraffic = JsonNullable.undefined();
    private JsonNullable<String> sofProxyXss = JsonNullable.undefined();
    private JsonNullable<String> sofProxyXxe = JsonNullable.undefined();
    private JsonNullable<Integer> sofProxyTps = JsonNullable.undefined();
    private JsonNullable<String> sofProxyIgnoreApi = JsonNullable.undefined();
    private JsonNullable<List<String>> sofProxyClientId = JsonNullable.undefined();
    private JsonNullable<String> sofProxyShowLog = JsonNullable.undefined();
    private JsonNullable<String> sofProxySticky = JsonNullable.undefined();
    private JsonNullable<String> sofProxyUrlRewrite = JsonNullable.undefined();
    // Diversion 列表（增量更新）
    private JsonNullable<List<SmartOnFhirProxyDiversionUpdateDto>> diversionList = JsonNullable.undefined();

    // Getters and Setters

    public String getSofProxyId() {
        return sofProxyId;
    }

    public void setSofProxyId(String sofProxyId) {
        this.sofProxyId = sofProxyId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public JsonNullable<String> getSofProxyName() {
        return sofProxyName;
    }

    public void setSofProxyName(JsonNullable<String> sofProxyName) {
        this.sofProxyName = sofProxyName;
    }

    public JsonNullable<String> getSofProxyStatus() {
        return sofProxyStatus;
    }

    public void setSofProxyStatus(JsonNullable<String> sofProxyStatus) {
        this.sofProxyStatus = sofProxyStatus;
    }

    public JsonNullable<String> getSofProxyRemark() {
        return sofProxyRemark;
    }

    public void setSofProxyRemark(JsonNullable<String> sofProxyRemark) {
        this.sofProxyRemark = sofProxyRemark;
    }

    public JsonNullable<String> getSofProxyAccessToken() {
        return sofProxyAccessToken;
    }

    public void setSofProxyAccessToken(JsonNullable<String> sofProxyAccessToken) {
        this.sofProxyAccessToken = sofProxyAccessToken;
    }

    public JsonNullable<String> getSofProxySqlInjection() {
        return sofProxySqlInjection;
    }

    public void setSofProxySqlInjection(JsonNullable<String> sofProxySqlInjection) {
        this.sofProxySqlInjection = sofProxySqlInjection;
    }

    public JsonNullable<String> getSofProxyTraffic() {
        return sofProxyTraffic;
    }

    public void setSofProxyTraffic(JsonNullable<String> sofProxyTraffic) {
        this.sofProxyTraffic = sofProxyTraffic;
    }

    public JsonNullable<String> getSofProxyXss() {
        return sofProxyXss;
    }

    public void setSofProxyXss(JsonNullable<String> sofProxyXss) {
        this.sofProxyXss = sofProxyXss;
    }

    public JsonNullable<String> getSofProxyXxe() {
        return sofProxyXxe;
    }

    public void setSofProxyXxe(JsonNullable<String> sofProxyXxe) {
        this.sofProxyXxe = sofProxyXxe;
    }

    public JsonNullable<Integer> getSofProxyTps() {
        return sofProxyTps;
    }

    public void setSofProxyTps(JsonNullable<Integer> sofProxyTps) {
        this.sofProxyTps = sofProxyTps;
    }

    public JsonNullable<String> getSofProxyIgnoreApi() {
        return sofProxyIgnoreApi;
    }

    public void setSofProxyIgnoreApi(JsonNullable<String> sofProxyIgnoreApi) {
        this.sofProxyIgnoreApi = sofProxyIgnoreApi;
    }

    public JsonNullable<List<String>> getSofProxyClientId() {
        return sofProxyClientId;
    }

    public void setSofProxyClientId(JsonNullable<List<String>> sofProxyClientId) {
        this.sofProxyClientId = sofProxyClientId;
    }

    public JsonNullable<List<SmartOnFhirProxyDiversionUpdateDto>> getDiversionList() {
        return diversionList;
    }

    public void setDiversionList(JsonNullable<List<SmartOnFhirProxyDiversionUpdateDto>> diversionList) {
        this.diversionList = diversionList;
    }

    public JsonNullable<String> getSofProxyShowLog() {
        return sofProxyShowLog;
    }

    public void setSofProxyShowLog(JsonNullable<String> sofProxyShowLog) {
        this.sofProxyShowLog = sofProxyShowLog;
    }

    public JsonNullable<String> getSofProxySticky() {
        return sofProxySticky;
    }

    public void setSofProxySticky(JsonNullable<String> sofProxySticky) {
        this.sofProxySticky = sofProxySticky;
    }

    public JsonNullable<String> getSofProxyUrlRewrite() {
        return sofProxyUrlRewrite;
    }

    public void setSofProxyUrlRewrite(JsonNullable<String> sofProxyUrlRewrite) {
        this.sofProxyUrlRewrite = sofProxyUrlRewrite;
    }

    @Override
    public String toString() {
        return "DgrSmartOnFhirProxyUpdateDto [sofProxyId=" + sofProxyId + ", version=" + version
                + ", sofProxyName=" + sofProxyName + ", sofProxyStatus=" + sofProxyStatus
                + ", sofProxyRemark=" + sofProxyRemark + ", sofProxyAccessToken=" + sofProxyAccessToken
                + ", sofProxySqlInjection=" + sofProxySqlInjection + ", sofProxyTraffic=" + sofProxyTraffic
                + ", sofProxyXss=" + sofProxyXss + ", sofProxyXxe=" + sofProxyXxe + ", sofProxyShowLog=" + sofProxyShowLog
                + ", sofProxySticky=" + sofProxySticky + ", sofProxyUrlRewrite=" + sofProxyUrlRewrite
                + ", sofProxyTps=" + sofProxyTps
                + ", sofProxyIgnoreApi=" + sofProxyIgnoreApi + ", sofProxyClientId=" + sofProxyClientId
                + ", diversionList=" + diversionList + "]";
    }
}
