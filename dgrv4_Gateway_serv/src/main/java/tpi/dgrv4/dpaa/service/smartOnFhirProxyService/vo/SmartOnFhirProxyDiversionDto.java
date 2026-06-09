package tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo;

import java.util.Date;

/**
 * Smart on FHIR Proxy Diversion DTO
 * 通用資料傳輸物件
 */
public class SmartOnFhirProxyDiversionDto {

    private String sofProxyDiversionId;
    private String sofProxyId;
    private Integer sofProxyDiversionProbability;
    private String sofProxyDiversionUrl;
    private Date createDateTime;
    private String createUser;
    private Date updateDateTime;
    private String updateUser;
    private String version;

    public String getSofProxyDiversionId() {
        return sofProxyDiversionId;
    }

    public void setSofProxyDiversionId(String sofProxyDiversionId) {
        this.sofProxyDiversionId = sofProxyDiversionId;
    }

    public String getSofProxyId() {
        return sofProxyId;
    }

    public void setSofProxyId(String sofProxyId) {
        this.sofProxyId = sofProxyId;
    }

    public Integer getSofProxyDiversionProbability() {
        return sofProxyDiversionProbability;
    }

    public void setSofProxyDiversionProbability(Integer sofProxyDiversionProbability) {
        this.sofProxyDiversionProbability = sofProxyDiversionProbability;
    }

    public String getSofProxyDiversionUrl() {
        return sofProxyDiversionUrl;
    }

    public void setSofProxyDiversionUrl(String sofProxyDiversionUrl) {
        this.sofProxyDiversionUrl = sofProxyDiversionUrl;
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

    @Override
    public String toString() {
        return "DgrSmartOnFhirProxyDiversionDto [sofProxyDiversionId=" + sofProxyDiversionId + ", sofProxyId="
                + sofProxyId
                + ", sofProxyDiversionProbability=" + sofProxyDiversionProbability + ", sofProxyDiversionUrl="
                + sofProxyDiversionUrl
                + ", createDateTime=" + createDateTime + ", createUser=" + createUser
                + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version=" + version + "]";
    }
}
