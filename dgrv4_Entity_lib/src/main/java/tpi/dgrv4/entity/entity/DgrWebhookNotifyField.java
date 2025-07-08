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
@Table(name = "DGR_WEBHOOK_NOTIFY_FIELD")
public class DgrWebhookNotifyField implements DgrSequenced {

	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "WEBHOOK_NOTIFY_FIELD_ID")
	private Long webhookNotifyFieldId;

	@Column(name = "WEBHOOK_NOTIFY_ID")
	private Long webhookNotifyId;
	
	@Column(name = "FIELD_KEY")
	private String fieldKey;
	
	@Column(name = "FIELD_VALUE")
	private String fieldValue;
	
	@Column(name = "FIELD_TYPE")
	private String fieldType;
	
	@Column(name = "MAPPING_URL")
	private String mappingUrl;
	
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
		return webhookNotifyFieldId;
	}

	/* constructors */

	public DgrWebhookNotifyField() {}

	/* methods */

	@Override
	public String toString() {
		return "DgrWebhookNotifyField [webhookNotifyFieldId=" + webhookNotifyFieldId + ", webhookNotifyId=" + webhookNotifyId
				+ ", fieldKey=" + fieldKey + ", fieldValue=" + fieldValue + ", mappingUrl=" + mappingUrl + "]";
	}

	public Long getWebhookNotifyFieldId() {
		return webhookNotifyFieldId;
	}

	public void setWebhookNotifyFieldId(Long webhookNotifyFieldId) {
		this.webhookNotifyFieldId = webhookNotifyFieldId;
	}

	public Long getWebhookNotifyId() {
		return webhookNotifyId;
	}

	public void setWebhookNotifyId(Long webhookNotifyId) {
		this.webhookNotifyId = webhookNotifyId;
	}

	public String getFieldKey() {
		return fieldKey;
	}

	public void setFieldKey(String fieldKey) {
		this.fieldKey = fieldKey;
	}

	public String getFieldValue() {
		return fieldValue;
	}

	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	public String getMappingUrl() {
		return mappingUrl;
	}

	public void setMappingUrl(String mappingUrl) {
		this.mappingUrl = mappingUrl;
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
