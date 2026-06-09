package tpi.dgrv4.entity.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.List;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;
import tpi.dgrv4.entity.constant.CodeEnum;
import tpi.dgrv4.entity.converter.AbstractCodeEnumConverter;
import tpi.dgrv4.entity.converter.StringListJsonConverter;

@Entity
@Table(name = "dgr_smart_on_fhir_proxy")
public class DgrSmartOnFhirProxy implements DgrSequenced {

    @Id
    @DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
    @Column(name = "sof_proxy_id")
    private Long sofProxyId;

    @Column(name = "sof_proxy_name")
    private String sofProxyName;

    @Column(name = "sof_proxy_status")
    @Convert(converter = StatusConverter.class)
    private Status sofProxyStatus = Status.ENABLED;

    @Column(name = "sof_proxy_remark")
    private String sofProxyRemark;

    @Column(name = "sof_proxy_access_token")
    @Convert(converter = StatusConverter.class)
    private Status sofProxyAccessToken = Status.DISABLED;

    @Column(name = "sof_proxy_sql_injection")
    @Convert(converter = StatusConverter.class)
    private Status sofProxySqlInjection = Status.DISABLED;

    @Column(name = "sof_proxy_traffic")
    @Convert(converter = StatusConverter.class)
    private Status sofProxyTraffic = Status.DISABLED;

    @Column(name = "sof_proxy_xss")
    @Convert(converter = StatusConverter.class)
    private Status sofProxyXss = Status.DISABLED;

    @Column(name = "sof_proxy_xxe")
    @Convert(converter = StatusConverter.class)
    private Status sofProxyXxe = Status.DISABLED;

    @Column(name = "sof_proxy_tps")
    private Integer sofProxyTps = 0;

    @Column(name = "sof_proxy_ignore_api")
    private String sofProxyIgnoreApi;

    @Column(name = "sof_proxy_client_id")
    @Convert(converter = StringListJsonConverter.class)
    private List<String> sofProxyClientId;

    @Column(name = "sof_proxy_show_log")
    @Convert(converter = StatusConverter.class)
    private Status sofProxyShowLog = Status.DISABLED;

    @Column(name = "sof_proxy_sticky")
    @Convert(converter = StatusConverter.class)
    private Status sofProxySticky = Status.ENABLED;

    @Column(name = "sof_proxy_url_rewrite")
    @Convert(converter = StatusConverter.class)
    private Status sofProxyUrlRewrite = Status.ENABLED;

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
        return sofProxyId;
    }

    @Override
    public String toString() {
        return "DgrSmartOnFhirProxy [sofProxyId=" + sofProxyId + ", sofProxyName=" + sofProxyName
                + ", sofProxyStatus=" + sofProxyStatus + ", sofProxyRemark=" + sofProxyRemark
                + ", sofProxyAccessToken=" + sofProxyAccessToken + ", sofProxySqlInjection=" + sofProxySqlInjection
                + ", sofProxyTraffic=" + sofProxyTraffic + ", sofProxyXss=" + sofProxyXss + ", sofProxyXxe="
                + sofProxyXxe + ", sofProxyTps=" + sofProxyTps + ", sofProxyIgnoreApi=" + sofProxyIgnoreApi
                + ", sofProxyClientId=" + sofProxyClientId + ", sofProxyShowLog=" + sofProxyShowLog
                + ", sofProxySticky=" + sofProxySticky
                + ", sofProxyUrlRewrite=" + sofProxyUrlRewrite
                + ", createDateTime=" + createDateTime + ", createUser="
                + createUser + ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version="
                + version + "]";
    }

    // Getters and Setters

    public Status getSofProxyShowLog() {
        return sofProxyShowLog;
    }

    public void setSofProxyShowLog(Status sofProxyShowLog) {
        this.sofProxyShowLog = sofProxyShowLog;
    }

    public Status getSofProxySticky() {
        return sofProxySticky;
    }

    public void setSofProxySticky(Status sofProxySticky) {
        this.sofProxySticky = sofProxySticky;
    }

    public Status getSofProxyUrlRewrite() {
        return sofProxyUrlRewrite;
    }

    public void setSofProxyUrlRewrite(Status sofProxyUrlRewrite) {
        this.sofProxyUrlRewrite = sofProxyUrlRewrite;
    }

    public Long getSofProxyId() {
        return sofProxyId;
    }

    public void setSofProxyId(Long sofProxyId) {
        this.sofProxyId = sofProxyId;
    }

    public String getSofProxyName() {
        return sofProxyName;
    }

    public void setSofProxyName(String sofProxyName) {
        this.sofProxyName = sofProxyName;
    }

    public Status getSofProxyStatus() {
        return sofProxyStatus;
    }

    public void setSofProxyStatus(Status sofProxyStatus) {
        this.sofProxyStatus = sofProxyStatus;
    }

    public String getSofProxyRemark() {
        return sofProxyRemark;
    }

    public void setSofProxyRemark(String sofProxyRemark) {
        this.sofProxyRemark = sofProxyRemark;
    }

    public Status getSofProxyAccessToken() {
        return sofProxyAccessToken;
    }

    public void setSofProxyAccessToken(Status sofProxyAccessToken) {
        this.sofProxyAccessToken = sofProxyAccessToken;
    }

    public Status getSofProxySqlInjection() {
        return sofProxySqlInjection;
    }

    public void setSofProxySqlInjection(Status sofProxySqlInjection) {
        this.sofProxySqlInjection = sofProxySqlInjection;
    }

    public Status getSofProxyTraffic() {
        return sofProxyTraffic;
    }

    public void setSofProxyTraffic(Status sofProxyTraffic) {
        this.sofProxyTraffic = sofProxyTraffic;
    }

    public Status getSofProxyXss() {
        return sofProxyXss;
    }

    public void setSofProxyXss(Status sofProxyXss) {
        this.sofProxyXss = sofProxyXss;
    }

    public Status getSofProxyXxe() {
        return sofProxyXxe;
    }

    public void setSofProxyXxe(Status sofProxyXxe) {
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

    @Getter
    @RequiredArgsConstructor
    public static enum Status implements CodeEnum<String> {
        ENABLED("Y", "Enabled"),
        DISABLED("N", "Disabled");

        private final String code;
        private final String name;
    }

    @Converter(autoApply = false)
    public static class StatusConverter extends AbstractCodeEnumConverter<Status> {
        public StatusConverter() {
            super(Status.class);
        }
    }

}
