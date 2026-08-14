package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo;

import java.util.Date;
import java.util.List;

/**
 * Smart on FHIR Proxy 匯出資料格式
 * 包含完整的資料及審計欄位
 */
public class SmartOnFhirProxyExportDto {

    // 主表欄位
    private String sofProxyId;
    private String sofProxyName;
    private String sofProxyStatus;
    private String sofProxyRemark;
    private String sofProxyAccessToken;
    private String sofProxySqlInjection;
    private String sofProxyTraffic;
    private String sofProxyXss;
    private String sofProxyXxe;
    private String sofProxyShowLog;
    private String sofProxySticky;
    private String sofProxyUrlRewrite;
    private Integer sofProxyTps;
    private String sofProxyIgnoreApi;
    private List<String> sofProxyClientId;

    // ===== SMART on FHIR 認證擴充：Inbound =====
    private List<String> sofProxyAuth;
    private String sofProxyInIntrospectionEndpoint;
    private String sofProxyInWellKnown;
    private String sofProxyInIntrospectionAuth;
    private String sofProxyInIntrospectionClientId;
    private boolean hasIntrospectionSecret;            // 僅旗標，不匯出密文/明文
    private List<String> sofProxyInAllowedIss;
    private String sofProxyInExpectedAud;
    // ===== SMART on FHIR 認證擴充：Outbound =====
    private String sofProxyOutRelay;
    private String sofProxyOutAuthMethod;
    private String sofProxyOutClientId;
    private boolean hasOutClientSecret;                // 僅旗標，不匯出密文/明文
    private String sofProxyOutWellKnown;
    private String sofProxyOutTokenEndpoint;
    private String sofProxyOutScope;

    // 關聯資料
    private List<SmartOnFhirProxyDiversionDto> diversionList;
    private List<SmartOnFhirProxyStickyImportDto> stickyList;

    // 審計欄位（必須匯出）
    private Date createDateTime;
    private String createUser;
    private Date updateDateTime;
    private String updateUser;
    private String version;

    public String getSofProxyId() {
        return sofProxyId;
    }

    public void setSofProxyId(String sofProxyId) {
        this.sofProxyId = sofProxyId;
    }

    public String getSofProxyName() {
        return sofProxyName;
    }

    public void setSofProxyName(String sofProxyName) {
        this.sofProxyName = sofProxyName;
    }

    public String getSofProxyStatus() {
        return sofProxyStatus;
    }

    public void setSofProxyStatus(String sofProxyStatus) {
        this.sofProxyStatus = sofProxyStatus;
    }

    public String getSofProxyRemark() {
        return sofProxyRemark;
    }

    public void setSofProxyRemark(String sofProxyRemark) {
        this.sofProxyRemark = sofProxyRemark;
    }

    public String getSofProxyAccessToken() {
        return sofProxyAccessToken;
    }

    public void setSofProxyAccessToken(String sofProxyAccessToken) {
        this.sofProxyAccessToken = sofProxyAccessToken;
    }

    public String getSofProxySqlInjection() {
        return sofProxySqlInjection;
    }

    public void setSofProxySqlInjection(String sofProxySqlInjection) {
        this.sofProxySqlInjection = sofProxySqlInjection;
    }

    public String getSofProxyTraffic() {
        return sofProxyTraffic;
    }

    public void setSofProxyTraffic(String sofProxyTraffic) {
        this.sofProxyTraffic = sofProxyTraffic;
    }

    public String getSofProxyXss() {
        return sofProxyXss;
    }

    public void setSofProxyXss(String sofProxyXss) {
        this.sofProxyXss = sofProxyXss;
    }

    public String getSofProxyXxe() {
        return sofProxyXxe;
    }

    public void setSofProxyXxe(String sofProxyXxe) {
        this.sofProxyXxe = sofProxyXxe;
    }

    public Integer getSofProxyTps() {
        return sofProxyTps;
    }

    public void setSofProxyTps(Integer sofProxyTps) {
        this.sofProxyTps = sofProxyTps;
    }

    public String getSofProxyIgnoreApi() {
        return sofProxyIgnoreApi;
    }

    public void setSofProxyIgnoreApi(String sofProxyIgnoreApi) {
        this.sofProxyIgnoreApi = sofProxyIgnoreApi;
    }

    public List<String> getSofProxyClientId() {
        return sofProxyClientId;
    }

    public void setSofProxyClientId(List<String> sofProxyClientId) {
        this.sofProxyClientId = sofProxyClientId;
    }

    public List<SmartOnFhirProxyDiversionDto> getDiversionList() {
        return diversionList;
    }

    public void setDiversionList(List<SmartOnFhirProxyDiversionDto> diversionList) {
        this.diversionList = diversionList;
    }

    public List<SmartOnFhirProxyStickyImportDto> getStickyList() {
        return stickyList;
    }

    public void setStickyList(List<SmartOnFhirProxyStickyImportDto> stickyList) {
        this.stickyList = stickyList;
    }

    public Date getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(Date createDateTime) {
        this.createDateTime = createDateTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Date getUpdateDateTime() {
        return updateDateTime;
    }

    public void setUpdateDateTime(Date updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSofProxyShowLog() {
        return sofProxyShowLog;
    }

    public void setSofProxyShowLog(String sofProxyShowLog) {
        this.sofProxyShowLog = sofProxyShowLog;
    }

    public String getSofProxySticky() {
        return sofProxySticky;
    }

    public void setSofProxySticky(String sofProxySticky) {
        this.sofProxySticky = sofProxySticky;
    }

    public String getSofProxyUrlRewrite() {
        return sofProxyUrlRewrite;
    }

    public void setSofProxyUrlRewrite(String sofProxyUrlRewrite) {
        this.sofProxyUrlRewrite = sofProxyUrlRewrite;
    }

    public List<String> getSofProxyAuth() {
        return sofProxyAuth;
    }

    public void setSofProxyAuth(List<String> sofProxyAuth) {
        this.sofProxyAuth = sofProxyAuth;
    }

    public String getSofProxyInIntrospectionEndpoint() {
        return sofProxyInIntrospectionEndpoint;
    }

    public void setSofProxyInIntrospectionEndpoint(String sofProxyInIntrospectionEndpoint) {
        this.sofProxyInIntrospectionEndpoint = sofProxyInIntrospectionEndpoint;
    }

    public String getSofProxyInWellKnown() {
        return sofProxyInWellKnown;
    }

    public void setSofProxyInWellKnown(String sofProxyInWellKnown) {
        this.sofProxyInWellKnown = sofProxyInWellKnown;
    }

    public String getSofProxyInIntrospectionAuth() {
        return sofProxyInIntrospectionAuth;
    }

    public void setSofProxyInIntrospectionAuth(String sofProxyInIntrospectionAuth) {
        this.sofProxyInIntrospectionAuth = sofProxyInIntrospectionAuth;
    }

    public String getSofProxyInIntrospectionClientId() {
        return sofProxyInIntrospectionClientId;
    }

    public void setSofProxyInIntrospectionClientId(String sofProxyInIntrospectionClientId) {
        this.sofProxyInIntrospectionClientId = sofProxyInIntrospectionClientId;
    }

    public boolean isHasIntrospectionSecret() {
        return hasIntrospectionSecret;
    }

    public void setHasIntrospectionSecret(boolean hasIntrospectionSecret) {
        this.hasIntrospectionSecret = hasIntrospectionSecret;
    }

    public List<String> getSofProxyInAllowedIss() {
        return sofProxyInAllowedIss;
    }

    public void setSofProxyInAllowedIss(List<String> sofProxyInAllowedIss) {
        this.sofProxyInAllowedIss = sofProxyInAllowedIss;
    }

    public String getSofProxyInExpectedAud() {
        return sofProxyInExpectedAud;
    }

    public void setSofProxyInExpectedAud(String sofProxyInExpectedAud) {
        this.sofProxyInExpectedAud = sofProxyInExpectedAud;
    }

    public String getSofProxyOutRelay() {
        return sofProxyOutRelay;
    }

    public void setSofProxyOutRelay(String sofProxyOutRelay) {
        this.sofProxyOutRelay = sofProxyOutRelay;
    }

    public String getSofProxyOutAuthMethod() {
        return sofProxyOutAuthMethod;
    }

    public void setSofProxyOutAuthMethod(String sofProxyOutAuthMethod) {
        this.sofProxyOutAuthMethod = sofProxyOutAuthMethod;
    }

    public String getSofProxyOutClientId() {
        return sofProxyOutClientId;
    }

    public void setSofProxyOutClientId(String sofProxyOutClientId) {
        this.sofProxyOutClientId = sofProxyOutClientId;
    }

    public boolean isHasOutClientSecret() {
        return hasOutClientSecret;
    }

    public void setHasOutClientSecret(boolean hasOutClientSecret) {
        this.hasOutClientSecret = hasOutClientSecret;
    }

    public String getSofProxyOutWellKnown() {
        return sofProxyOutWellKnown;
    }

    public void setSofProxyOutWellKnown(String sofProxyOutWellKnown) {
        this.sofProxyOutWellKnown = sofProxyOutWellKnown;
    }

    public String getSofProxyOutTokenEndpoint() {
        return sofProxyOutTokenEndpoint;
    }

    public void setSofProxyOutTokenEndpoint(String sofProxyOutTokenEndpoint) {
        this.sofProxyOutTokenEndpoint = sofProxyOutTokenEndpoint;
    }

    public String getSofProxyOutScope() {
        return sofProxyOutScope;
    }

    public void setSofProxyOutScope(String sofProxyOutScope) {
        this.sofProxyOutScope = sofProxyOutScope;
    }

    @Override
    public String toString() {
        return "SmartOnFhirProxyExportDto [sofProxyId=" + sofProxyId + ", sofProxyName=" + sofProxyName
                + ", sofProxyStatus=" + sofProxyStatus + ", sofProxyRemark=" + sofProxyRemark
                + ", sofProxyAccessToken=" + sofProxyAccessToken + ", sofProxySqlInjection=" + sofProxySqlInjection
                + ", sofProxyTraffic=" + sofProxyTraffic + ", sofProxyXss=" + sofProxyXss + ", sofProxyXxe="
                + sofProxyXxe + ", sofProxyShowLog=" + sofProxyShowLog + ", sofProxySticky=" + sofProxySticky
                + ", sofProxyUrlRewrite=" + sofProxyUrlRewrite + ", sofProxyTps=" + sofProxyTps
                + ", sofProxyIgnoreApi=" + sofProxyIgnoreApi
                + ", sofProxyClientId=" + sofProxyClientId + ", diversionList=" + diversionList + ", createDateTime="
                + createDateTime + ", createUser=" + createUser + ", updateDateTime=" + updateDateTime
                + ", updateUser=" + updateUser + ", version=" + version
                + ", sofProxyAuth=" + sofProxyAuth + ", sofProxyInIntrospectionEndpoint=" + sofProxyInIntrospectionEndpoint
                + ", sofProxyInWellKnown=" + sofProxyInWellKnown + ", sofProxyInIntrospectionAuth=" + sofProxyInIntrospectionAuth
                + ", sofProxyInIntrospectionClientId=" + sofProxyInIntrospectionClientId
                + ", hasIntrospectionSecret=" + hasIntrospectionSecret + ", sofProxyInAllowedIss=" + sofProxyInAllowedIss
                + ", sofProxyInExpectedAud=" + sofProxyInExpectedAud + ", sofProxyOutRelay=" + sofProxyOutRelay
                + ", sofProxyOutAuthMethod=" + sofProxyOutAuthMethod + ", sofProxyOutClientId=" + sofProxyOutClientId
                + ", hasOutClientSecret=" + hasOutClientSecret + ", sofProxyOutWellKnown=" + sofProxyOutWellKnown
                + ", sofProxyOutTokenEndpoint=" + sofProxyOutTokenEndpoint + ", sofProxyOutScope=" + sofProxyOutScope + "]";
    }
}
