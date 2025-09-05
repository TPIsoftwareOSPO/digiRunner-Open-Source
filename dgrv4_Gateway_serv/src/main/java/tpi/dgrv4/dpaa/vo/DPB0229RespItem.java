package tpi.dgrv4.dpaa.vo;

import java.util.Date;

public class DPB0229RespItem {
    private String dgrMtlsClientCertId;
    private String hostAndPort;
    private Date rootCAExpireDate;
    private Date CRTExpireDate;
    private String remark;
    private Date updateDateTime;
    private String updateUser;
    private String enable;

    public String getHostAndPort() {
        return hostAndPort;
    }

    public void setHostAndPort(String hostAndPort) {
        this.hostAndPort = hostAndPort;
    }

    public Date getRootCAExpireDate() {
        return rootCAExpireDate;
    }

    public void setRootCAExpireDate(Date rootCAExpireDate) {
        this.rootCAExpireDate = rootCAExpireDate;
    }

    public Date getCRTExpireDate() {
        return CRTExpireDate;
    }

    public void setCRTExpireDate(Date CRTExpireDate) {
        this.CRTExpireDate = CRTExpireDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public String getDgrMtlsClientCertId() {
        return dgrMtlsClientCertId;
    }

    public void setDgrMtlsClientCertId(String dgrMtlsClientCertId) {
        this.dgrMtlsClientCertId = dgrMtlsClientCertId;
    }
}
