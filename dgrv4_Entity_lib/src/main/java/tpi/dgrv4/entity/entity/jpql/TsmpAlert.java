package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_alert")
public class TsmpAlert {

	@Id
	@Column(name = "alert_id")
	private Long alertId;
	
	@Column(name = "alert_name")
	private String alertName;
	
	@Column(name = "alert_type")
	private String alertType;
	
	@Column(name = "alert_enabled")
	private Boolean alertEnabled;
	
	@Column(name = "threshold")
	private Integer threshold;
	
	@Column(name = "duration")
	private Integer duration;
	
	@Column(name = "alert_interval")
	private Integer alertInterval;
	
	@Column(name = "c_flag")
	private Boolean cFlag;
	
	@Column(name = "im_flag")
	private Boolean imFlag;
	
	@Column(name = "im_type")
	private String imType;
	
	@Column(name = "im_id")
	private String imId;
	
	@Column(name = "ex_type")
	private String exType;
	
	@Column(name = "ex_days")
	private String exDays;
	
	@Column(name = "ex_time")
	private String exTime;
	
	@Column(name = "alert_desc")
	private String alertDesc;
	
	@Column(name = "alert_sys")
	private String alertSys;
	
	@Column(name = "alert_msg")
	private String alertMsg;
	
	@Column(name = "modulename")
	private String modulename;
	
	@Column(name = "responsetime")
	private String responsetime;
	
	@Column(name = "es_search_payload")
	private String esSearchPayload;
	
	@Column(name = "create_time")
	private Date createTime;

	@Column(name = "create_user")
	private String createUser;

	@Column(name = "update_time")
	private Date updateTime;

	@Column(name = "update_user")
	private String updateUser;
	
	/* constructors */

	public TsmpAlert() {}

	/* methods */
	@Override
	public String toString() {
		return "TsmpAlert [alertId=" + alertId + ", alertName=" + alertName + ", alertType=" + alertType
				+ ", alertEnabled=" + alertEnabled + ", threshold=" + threshold + ", duration=" + duration
				+ ", alertInterval=" + alertInterval + ", cFlag=" + cFlag + ", imFlag=" + imFlag + ", imType=" + imType
				+ ", imId=" + imId + ", exType=" + exType + ", exDays=" + exDays + ", exTime=" + exTime + ", alertDesc="
				+ alertDesc + ", alertSys=" + alertSys + ", alertMsg=" + alertMsg + ", modulename=" + modulename
				+ ", responsetime=" + responsetime + ", esSearchPayload=" + esSearchPayload + ", createTime="
				+ createTime + ", createUser=" + createUser + ", updateTime=" + updateTime + ", updateUser="
				+ updateUser + "]";
	}
	
	/* getters and setters */

	public Long getAlertId() {
		return alertId;
	}

	public void setAlertId(Long alertId) {
		this.alertId = alertId;
	}
	
	public String getAlertName() {
		return alertName;
	}
	
	public void setAlertName(String alertName) {
		this.alertName = alertName;
	}

	public String getAlertType() {
		return alertType;
	}

	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}

	public Boolean getAlertEnabled() {
		return alertEnabled;
	}

	public void setAlertEnabled(Boolean alertEnabled) {
		this.alertEnabled = alertEnabled;
	}

	public Integer getThreshold() {
		return threshold;
	}

	public void setThreshold(Integer threshold) {
		this.threshold = threshold;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public Integer getAlertInterval() {
		return alertInterval;
	}

	public void setAlertInterval(Integer alertInterval) {
		this.alertInterval = alertInterval;
	}

	public Boolean getcFlag() {
		return cFlag;
	}

	public void setcFlag(Boolean cFlag) {
		this.cFlag = cFlag;
	}

	public Boolean getImFlag() {
		return imFlag;
	}

	public void setImFlag(Boolean imFlag) {
		this.imFlag = imFlag;
	}

	public String getImType() {
		return imType;
	}

	public void setImType(String imType) {
		this.imType = imType;
	}

	public String getImId() {
		return imId;
	}

	public void setImId(String imId) {
		this.imId = imId;
	}

	public String getExType() {
		return exType;
	}

	public void setExType(String exType) {
		this.exType = exType;
	}

	public String getExDays() {
		return exDays;
	}

	public void setExDays(String exDays) {
		this.exDays = exDays;
	}

	public String getExTime() {
		return exTime;
	}

	public void setExTime(String exTime) {
		this.exTime = exTime;
	}

	public String getAlertDesc() {
		return alertDesc;
	}

	public void setAlertDesc(String alertDesc) {
		this.alertDesc = alertDesc;
	}

	public String getAlertSys() {
		return alertSys;
	}

	public void setAlertSys(String alertSys) {
		this.alertSys = alertSys;
	}

	public String getAlertMsg() {
		return alertMsg;
	}

	public void setAlertMsg(String alertMsg) {
		this.alertMsg = alertMsg;
	}

	public String getModulename() {
		return modulename;
	}

	public void setModulename(String modulename) {
		this.modulename = modulename;
	}

	public String getResponsetime() {
		return responsetime;
	}

	public void setResponsetime(String responsetime) {
		this.responsetime = responsetime;
	}

	public String getEsSearchPayload() {
		return esSearchPayload;
	}

	public void setEsSearchPayload(String esSearchPayload) {
		this.esSearchPayload = esSearchPayload;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}
	
	
}
