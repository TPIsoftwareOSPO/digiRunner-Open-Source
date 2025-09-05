package tpi.dgrv4.dpaa.vo;

import java.util.Date;

public class DPB0228Resp {
    private String dgrMtlsClientCertId;
    private String host;
    private int port;
    private String rootCa;
    private String clientCert;
    private String clientKey;
    private String keyMima;
    private String remark;
    private String enable;
    private Date createDateTime;
    private String createUser;
    private Date updateDateTime;
    private String updateUser;

    public String getDgrMtlsClientCertId() {
        return dgrMtlsClientCertId;
    }

    public void setDgrMtlsClientCertId(String dgrMtlsClientCertId) {
        this.dgrMtlsClientCertId = dgrMtlsClientCertId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getRootCa() {
        return rootCa;
    }

    public void setRootCa(String rootCa) {
        this.rootCa = rootCa;
    }

    public String getClientCert() {
        return clientCert;
    }

    public void setClientCert(String clientCert) {
        this.clientCert = clientCert;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public String getKeyMima() {
        return keyMima;
    }

    public void setKeyMima(String keyMima) {
        this.keyMima = keyMima;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
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
}
