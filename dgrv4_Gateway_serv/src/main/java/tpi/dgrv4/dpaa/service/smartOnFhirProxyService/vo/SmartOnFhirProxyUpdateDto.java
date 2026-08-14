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
    // SMART on FHIR 認證擴充（Inbound）
    private JsonNullable<List<String>> sofProxyAuth = JsonNullable.undefined();
    private JsonNullable<String> sofProxyInIntrospectionEndpoint = JsonNullable.undefined();
    private JsonNullable<String> sofProxyInWellKnown = JsonNullable.undefined();
    private JsonNullable<String> sofProxyInIntrospectionAuth = JsonNullable.undefined();
    private JsonNullable<String> sofProxyInIntrospectionClientId = JsonNullable.undefined();
    private JsonNullable<String> sofProxyInIntrospectionSecret = JsonNullable.undefined(); // 明文；undefined=保留, ""=清除, 有值=重新加密
    private JsonNullable<List<String>> sofProxyInAllowedIss = JsonNullable.undefined();
    private JsonNullable<String> sofProxyInExpectedAud = JsonNullable.undefined();
    // SMART on FHIR 認證擴充（Outbound）
    private JsonNullable<String> sofProxyOutRelay = JsonNullable.undefined();
    private JsonNullable<String> sofProxyOutAuthMethod = JsonNullable.undefined();
    private JsonNullable<String> sofProxyOutClientId = JsonNullable.undefined();
    private JsonNullable<String> sofProxyOutClientSecret = JsonNullable.undefined(); // 明文；undefined=保留, ""=清除, 有值=重新加密
    private JsonNullable<String> sofProxyOutWellKnown = JsonNullable.undefined();
    private JsonNullable<String> sofProxyOutTokenEndpoint = JsonNullable.undefined();
    private JsonNullable<String> sofProxyOutScope = JsonNullable.undefined();
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

    public JsonNullable<List<String>> getSofProxyAuth() {
        return sofProxyAuth;
    }

    public void setSofProxyAuth(JsonNullable<List<String>> sofProxyAuth) {
        this.sofProxyAuth = sofProxyAuth;
    }

    public JsonNullable<String> getSofProxyInIntrospectionEndpoint() {
        return sofProxyInIntrospectionEndpoint;
    }

    public void setSofProxyInIntrospectionEndpoint(JsonNullable<String> sofProxyInIntrospectionEndpoint) {
        this.sofProxyInIntrospectionEndpoint = sofProxyInIntrospectionEndpoint;
    }

    public JsonNullable<String> getSofProxyInWellKnown() {
        return sofProxyInWellKnown;
    }

    public void setSofProxyInWellKnown(JsonNullable<String> sofProxyInWellKnown) {
        this.sofProxyInWellKnown = sofProxyInWellKnown;
    }

    public JsonNullable<String> getSofProxyInIntrospectionAuth() {
        return sofProxyInIntrospectionAuth;
    }

    public void setSofProxyInIntrospectionAuth(JsonNullable<String> sofProxyInIntrospectionAuth) {
        this.sofProxyInIntrospectionAuth = sofProxyInIntrospectionAuth;
    }

    public JsonNullable<String> getSofProxyInIntrospectionClientId() {
        return sofProxyInIntrospectionClientId;
    }

    public void setSofProxyInIntrospectionClientId(JsonNullable<String> sofProxyInIntrospectionClientId) {
        this.sofProxyInIntrospectionClientId = sofProxyInIntrospectionClientId;
    }

    public JsonNullable<String> getSofProxyInIntrospectionSecret() {
        return sofProxyInIntrospectionSecret;
    }

    public void setSofProxyInIntrospectionSecret(JsonNullable<String> sofProxyInIntrospectionSecret) {
        this.sofProxyInIntrospectionSecret = sofProxyInIntrospectionSecret;
    }

    public JsonNullable<List<String>> getSofProxyInAllowedIss() {
        return sofProxyInAllowedIss;
    }

    public void setSofProxyInAllowedIss(JsonNullable<List<String>> sofProxyInAllowedIss) {
        this.sofProxyInAllowedIss = sofProxyInAllowedIss;
    }

    public JsonNullable<String> getSofProxyInExpectedAud() {
        return sofProxyInExpectedAud;
    }

    public void setSofProxyInExpectedAud(JsonNullable<String> sofProxyInExpectedAud) {
        this.sofProxyInExpectedAud = sofProxyInExpectedAud;
    }

    public JsonNullable<String> getSofProxyOutRelay() {
        return sofProxyOutRelay;
    }

    public void setSofProxyOutRelay(JsonNullable<String> sofProxyOutRelay) {
        this.sofProxyOutRelay = sofProxyOutRelay;
    }

    public JsonNullable<String> getSofProxyOutAuthMethod() {
        return sofProxyOutAuthMethod;
    }

    public void setSofProxyOutAuthMethod(JsonNullable<String> sofProxyOutAuthMethod) {
        this.sofProxyOutAuthMethod = sofProxyOutAuthMethod;
    }

    public JsonNullable<String> getSofProxyOutClientId() {
        return sofProxyOutClientId;
    }

    public void setSofProxyOutClientId(JsonNullable<String> sofProxyOutClientId) {
        this.sofProxyOutClientId = sofProxyOutClientId;
    }

    public JsonNullable<String> getSofProxyOutClientSecret() {
        return sofProxyOutClientSecret;
    }

    public void setSofProxyOutClientSecret(JsonNullable<String> sofProxyOutClientSecret) {
        this.sofProxyOutClientSecret = sofProxyOutClientSecret;
    }

    public JsonNullable<String> getSofProxyOutWellKnown() {
        return sofProxyOutWellKnown;
    }

    public void setSofProxyOutWellKnown(JsonNullable<String> sofProxyOutWellKnown) {
        this.sofProxyOutWellKnown = sofProxyOutWellKnown;
    }

    public JsonNullable<String> getSofProxyOutTokenEndpoint() {
        return sofProxyOutTokenEndpoint;
    }

    public void setSofProxyOutTokenEndpoint(JsonNullable<String> sofProxyOutTokenEndpoint) {
        this.sofProxyOutTokenEndpoint = sofProxyOutTokenEndpoint;
    }

    public JsonNullable<String> getSofProxyOutScope() {
        return sofProxyOutScope;
    }

    public void setSofProxyOutScope(JsonNullable<String> sofProxyOutScope) {
        this.sofProxyOutScope = sofProxyOutScope;
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
                + ", sofProxyAuth=" + sofProxyAuth + ", sofProxyInIntrospectionEndpoint=" + sofProxyInIntrospectionEndpoint
                + ", sofProxyInWellKnown=" + sofProxyInWellKnown + ", sofProxyInIntrospectionAuth=" + sofProxyInIntrospectionAuth
                + ", sofProxyInIntrospectionClientId=" + sofProxyInIntrospectionClientId
                + ", sofProxyInIntrospectionSecret=***" + ", sofProxyInAllowedIss=" + sofProxyInAllowedIss
                + ", sofProxyInExpectedAud=" + sofProxyInExpectedAud + ", sofProxyOutRelay=" + sofProxyOutRelay
                + ", sofProxyOutAuthMethod=" + sofProxyOutAuthMethod + ", sofProxyOutClientId=" + sofProxyOutClientId
                + ", sofProxyOutClientSecret=***" + ", sofProxyOutWellKnown=" + sofProxyOutWellKnown
                + ", sofProxyOutTokenEndpoint=" + sofProxyOutTokenEndpoint + ", sofProxyOutScope=" + sofProxyOutScope
                + ", diversionList=" + diversionList + "]";
    }
}
