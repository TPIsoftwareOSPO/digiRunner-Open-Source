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

@Entity
@Table(name = "dgr_gtw_idp_info_cus")
public class DgrGtwIdpInfoCus implements DgrSequenced {

    @Id
    @DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
    @Column(name = "gtw_idp_info_cus_id")
    private Long gtwIdpInfoCusId;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "status")
    private String status;

    @Column(name = "cus_login_url")
    private String cusLoginUrl;

    @Column(name = "cus_user_data_url")
    private String cusUserDataUrl;

    @Column(name = "icon_file")
    private String iconFile;

    @Column(name = "page_title")
    private String pageTitle;

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
        return gtwIdpInfoCusId;
    }

    @Override
    public String toString() {
        return "DgrGtwIdpInfoCus [gtwIdpInfoCusId=" + gtwIdpInfoCusId + ", clientId=" + clientId
                + ", status=" + status + ", cusLoginUrl=" + cusLoginUrl + ", cusUserDataUrl="
                + cusUserDataUrl + ", iconFile=" + iconFile + ", pageTitle=" + pageTitle
                + ", createDateTime=" + createDateTime + ", createUser=" + createUser
                + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser
                + ", version=" + version + "]";
    }

    // Getters and Setters

    public Long getGtwIdpInfoCusId() {
        return gtwIdpInfoCusId;
    }

    public void setGtwIdpInfoCusId(Long gtwIdpInfoCusId) {
        this.gtwIdpInfoCusId = gtwIdpInfoCusId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCusLoginUrl() {
        return cusLoginUrl;
    }

    public void setCusLoginUrl(String cusLoginUrl) {
        this.cusLoginUrl = cusLoginUrl;
    }

    public String getCusUserDataUrl() {
        return cusUserDataUrl;
    }

    public void setCusUserDataUrl(String cusUserDataUrl) {
        this.cusUserDataUrl = cusUserDataUrl;
    }

    public String getIconFile() {
        return iconFile;
    }

    public void setIconFile(String iconFile) {
        this.iconFile = iconFile;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
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
