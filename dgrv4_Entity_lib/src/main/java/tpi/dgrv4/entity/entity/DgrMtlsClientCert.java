package tpi.dgrv4.entity.entity;

import jakarta.persistence.*;
import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;

import java.util.Date;

@Entity
@Table(name = "dgr_mtls_client_cert")
//@EntityListeners(EntityCrudPostEventListener.class)
public class DgrMtlsClientCert implements DgrSequenced {
    @Id
    @DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
    @Column(name = "dgr_mtls_client_cert_id")
    private Long dgrMtlsClientCertId;
    @Column(name = "host")
    private String host;
    @Column(name = "port")
    private int port;
    @Column(name = "root_ca")
    private String rootCa;
    @Column(name = "client_cert")
    private String clientCert;
    @Column(name = "client_key")
    private String clientKey;
    @Column(name = "key_mima")
    private String keyMima;
    @Column(name = "remark")
    private String remark;
    @Column(name = "enable")
    private String enable = "N";

    @Column(name = "root_ca_expire_date")
    private Date rootCaExpireDate;

    @Column(name = "crt_expire_date")
    private Date crtExpireDate;

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


    public Long getDgrMtlsClientCertId() {
        return dgrMtlsClientCertId;
    }

    public void setDgrMtlsClientCertId(Long dgrMtlsClientCertId) {
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Date getRootCaExpireDate() {
        return rootCaExpireDate;
    }

    public void setRootCaExpireDate(Date rootCaExpireDate) {
        this.rootCaExpireDate = rootCaExpireDate;
    }

    public Date getCrtExpireDate() {
        return crtExpireDate;
    }

    public void setCrtExpireDate(Date crtExpireDate) {
        this.crtExpireDate = crtExpireDate;
    }

    @Override
    public String toString() {
        return "DgrMtlsClientCert{" +
                "dgrMtlsClientCertId=" + dgrMtlsClientCertId +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", rootCa='" + rootCa + '\'' +
                ", clientCert='" + clientCert + '\'' +
                ", clientKey='" + clientKey + '\'' +
                ", keyMima='" + keyMima + '\'' +
                ", remark='" + remark + '\'' +
                ", enable='" + enable + '\'' +
                ", rootCaExpireDate=" + rootCaExpireDate +
                ", crtExpireDate=" + crtExpireDate +
                ", createDateTime=" + createDateTime +
                ", createUser='" + createUser + '\'' +
                ", updateDateTime=" + updateDateTime +
                ", updateUser='" + updateUser + '\'' +
                ", version=" + version +
                '}';
    }

    @Override
    public Long getPrimaryKey() {
        return dgrMtlsClientCertId;
    }
}
