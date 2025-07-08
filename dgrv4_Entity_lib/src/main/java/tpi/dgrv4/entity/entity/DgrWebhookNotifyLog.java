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
@Table(name = "DGR_WEBHOOK_NOTIFY_LOG")
public class DgrWebhookNotifyLog implements DgrSequenced {

	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "WEBHOOK_NOTIFY_LOG_ID")
	private Long webhookNotifyLogId;

	@Column(name = "WEBHOOK_NOTIFY_ID")
	private Long webhookNotifyId;
	
	@Column(name = "CLIENT_ID")
	private String clientId;
	
	@Column(name = "CONTENT")
	private String content;
	
	@Column(name = "REMARK")
	private String remark;
	
	@Column(name = "CREATE_DATE_TIME")
	private Date createDateTime = DateTimeUtil.now();

	@Column(name = "CREATE_USER")
	private String createUser = "SYSTEM";

	@Column(name = "UPDATE_DATE_TIME")
	private Date updateDateTime;

	@Column(name = "UPDATE_USER")
	private String updateUser;
	
	@Version
	@Column(name = "VERSION")
	private Long version = 1L;
	
	@Override
	public Long getPrimaryKey() {
		return webhookNotifyLogId;
	}

	/* constructors */

	public DgrWebhookNotifyLog() {}

	/* methods */

	@Override
	public String toString() {
		return "DgrWebhookNotify [webhookNotifyLogId=" + webhookNotifyLogId + ", webhookNotifyId=" + webhookNotifyId
				+ ", clientId=" + clientId + ", content=" + content + "]";
	}

	public Long getWebhookNotifyLogId() {
		return webhookNotifyLogId;
	}

	public void setWebhookNotifyLogId(Long webhookNotifyLogId) {
		this.webhookNotifyLogId = webhookNotifyLogId;
	}

	public Long getWebhookNotifyId() {
		return webhookNotifyId;
	}

	public void setWebhookNotifyId(Long webhookNotifyId) {
		this.webhookNotifyId = webhookNotifyId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
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
