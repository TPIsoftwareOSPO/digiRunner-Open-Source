package tpi.dgrv4.entity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;

/**
 * Smart on FHIR Proxy Diversion Entity
 */
@Entity
@Table(name = "dgr_smart_on_fhir_proxy_diversion")
public class DgrSmartOnFhirProxyDiversion implements DgrSequenced {
	
    @Id
    @DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
    @Column(name = "sof_proxy_diversion_id")
    private Long sofProxyDiversionId;

    @Column(name = "sof_proxy_id")
    private Long sofProxyId;

    @Column(name = "sof_proxy_diversion_probability")
    private Integer sofProxyDiversionProbability;

    @Column(name = "sof_proxy_diversion_url")
    private String sofProxyDiversionUrl;

    @Column(name = "sof_proxy_diversion_fhir_base_path")
    private String sofProxyDiversionFhirBasePath;

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
        return sofProxyDiversionId;
    }

    @Override
    public String toString() {
        return "DgrSmartOnFhirProxyDiversion [sofProxyId=" + sofProxyId + ", sofProxyDiversionId=" + sofProxyDiversionId
                + ", sofProxyDiversionProbability=" + sofProxyDiversionProbability + ", sofProxyDiversionUrl="
                + sofProxyDiversionUrl + ", sofProxyDiversionFhirBasePath=" + sofProxyDiversionFhirBasePath
                + ", createDateTime=" + createDateTime + ", createUser=" + createUser
                + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version=" + version + "]";
    }

    // Getters and Setters

    public Long getSofProxyId() {
        return sofProxyId;
    }

    public void setSofProxyId(Long sofProxyId) {
        this.sofProxyId = sofProxyId;
    }

    public Long getSofProxyDiversionId() {
        return sofProxyDiversionId;
    }

    public void setSofProxyDiversionId(Long sofProxyDiversionId) {
        this.sofProxyDiversionId = sofProxyDiversionId;
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

    public String getSofProxyDiversionFhirBasePath() {
        return sofProxyDiversionFhirBasePath;
    }

    public void setSofProxyDiversionFhirBasePath(String sofProxyDiversionFhirBasePath) {
        this.sofProxyDiversionFhirBasePath = sofProxyDiversionFhirBasePath;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
