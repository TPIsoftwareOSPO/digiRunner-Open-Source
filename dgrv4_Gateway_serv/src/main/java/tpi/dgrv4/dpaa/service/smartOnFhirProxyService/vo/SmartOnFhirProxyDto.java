package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Smart on FHIR Proxy DTO
 * 通用資料傳輸物件
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SmartOnFhirProxyDto {

    private String sofProxyId;
    private String sofProxyName;
    private String sofProxyStatus;
    private String sofProxyStatusName; // 狀態名稱（用於顯示）
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
    private List<SmartOnFhirProxyDiversionDto> diversionList;
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

    public String getSofProxyStatusName() {
        return sofProxyStatusName;
    }

    public void setSofProxyStatusName(String sofProxyStatusName) {
        this.sofProxyStatusName = sofProxyStatusName;
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

    @Override
    public String toString() {
        return "DgrSmartOnFhirProxyDto [sofProxyId=" + sofProxyId + ", sofProxyName=" + sofProxyName
                + ", sofProxyStatus=" + sofProxyStatus + ", sofProxyStatusName=" + sofProxyStatusName
                + ", sofProxyRemark=" + sofProxyRemark + ", sofProxyAccessToken=" + sofProxyAccessToken
                + ", sofProxySqlInjection=" + sofProxySqlInjection + ", sofProxyTraffic=" + sofProxyTraffic
                + ", sofProxyXss=" + sofProxyXss + ", sofProxyXxe=" + sofProxyXxe + ", sofProxyShowLog=" + sofProxyShowLog
                + ", sofProxySticky=" + sofProxySticky + ", sofProxyUrlRewrite=" + sofProxyUrlRewrite
                + ", sofProxyTps=" + sofProxyTps
                + ", sofProxyIgnoreApi=" + sofProxyIgnoreApi + ", sofProxyClientId=" + sofProxyClientId
                + ", diversionList=" + diversionList + ", createDateTime=" + createDateTime + ", createUser="
                + createUser + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version="
                + version + "]";
    }
}
