package tpi.dgrv4.entity.entity.sql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmpn_site")
public class TsmpnSite {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "site_id")
	private Long siteId;
	
	@Column(name = "site_code")
	private String siteCode;

	@Column(name = "site_memo")
	private String siteMemo;

	@Column(name = "active")
	private Boolean active;

	@Column(name = "create_user")
	private String createUser;

	@Column(name = "create_time")
	private Date createTime;

	@Column(name = "update_user")
	private String updateUser;

	@Column(name = "update_time")
	private Date updateTime;

	@Column(name = "protocol_type")
	private String protocolType = "http";

	@Column(name = "binding_ip")
	private String bindingIp;

	@Column(name = "binding_port")
	private Integer bindingPort;

	@Column(name = "app_pool")
	private String appPool;

	@Column(name = "root_path")
	private String rootPath;

	@Column(name = "clr_version")
	private String clrVersion;

	@Override
	public String toString() {
		return "TsmpnSite [siteId=" + siteId + ", siteCode=" + siteCode + ", siteMemo=" + siteMemo + ", active="
				+ active + ", createUser=" + createUser + ", createTime=" + createTime + ", updateUser=" + updateUser
				+ ", updateTime=" + updateTime + ", protocolType=" + protocolType + ", bindingIp=" + bindingIp
				+ ", bindingPort=" + bindingPort + ", appPool=" + appPool + ", rootPath=" + rootPath + ", clrVersion="
				+ clrVersion + "]";
	}

	public Long getSiteId() {
		return siteId;
	}

	public void setSiteId(Long siteId) {
		this.siteId = siteId;
	}

	public String getSiteCode() {
		return siteCode;
	}

	public void setSiteCode(String siteCode) {
		this.siteCode = siteCode;
	}

	public String getSiteMemo() {
		return siteMemo;
	}

	public void setSiteMemo(String siteMemo) {
		this.siteMemo = siteMemo;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(String protocolType) {
		this.protocolType = protocolType;
	}

	public String getBindingIp() {
		return bindingIp;
	}

	public void setBindingIp(String bindingIp) {
		this.bindingIp = bindingIp;
	}

	public Integer getBindingPort() {
		return bindingPort;
	}

	public void setBindingPort(Integer bindingPort) {
		this.bindingPort = bindingPort;
	}

	public String getAppPool() {
		return appPool;
	}

	public void setAppPool(String appPool) {
		this.appPool = appPool;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public String getClrVersion() {
		return clrVersion;
	}

	public void setClrVersion(String clrVersion) {
		this.clrVersion = clrVersion;
	}

}
