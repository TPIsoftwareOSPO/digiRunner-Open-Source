package tpi.dgrv4.entity.entity;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;

@Entity
@Table(name = "DGR_WEBHOOK_NOTIFY")
public class DgrWebhookNotify implements DgrSequenced {

	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "WEBHOOK_NOTIFY_ID")
	private Long webhookNotifyId;

	@Column(name = "NOTIFY_NAME")
	private String notifyName;
	
	@Column(name = "NOTIFY_TYPE")
	private String notifyType;
	
	@Column(name = "ENABLE")
	private String enable;
	
	@Column(name = "MESSAGE")
	private String message;
	
	@Column(name = "PAYLOAD_FLAG")
	private String payloadFlag;
	
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
	
	@OneToMany(mappedBy = "webhookNotifyId", fetch = FetchType.LAZY)
    private List<DgrWebhookNotifyField> fieldList;
	
	
	@Override
	public Long getPrimaryKey() {
		return webhookNotifyId;
	}

	/* constructors */

	public DgrWebhookNotify() {}

	/* methods */

	@Override
	public String toString() {
		return "DgrWebhookNotify [webhookNotifyId=" + webhookNotifyId + ", notifyName=" + notifyName + ", notifyType=" + notifyType
				+ ", enable=" + enable + ", message=" + message + ", payloadFlag=" + payloadFlag + "]";
	}

	public Long getWebhookNotifyId() {
		return webhookNotifyId;
	}

	public void setWebhookNotifyId(Long webhookNotifyId) {
		this.webhookNotifyId = webhookNotifyId;
	}

	public String getNotifyName() {
		return notifyName;
	}

	public void setNotifyName(String notifyName) {
		this.notifyName = notifyName;
	}

	public String getNotifyType() {
		return notifyType;
	}

	public void setNotifyType(String notifyType) {
		this.notifyType = notifyType;
	}

	public String getEnable() {
		return enable;
	}

	public void setEnable(String enable) {
		this.enable = enable;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPayloadFlag() {
		return payloadFlag;
	}

	public void setPayloadFlag(String payloadFlag) {
		this.payloadFlag = payloadFlag;
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

	public List<DgrWebhookNotifyField> getFieldList() {
		return fieldList;
	}

	public void setFieldList(List<DgrWebhookNotifyField> fieldList) {
		this.fieldList = fieldList;
	}
}
