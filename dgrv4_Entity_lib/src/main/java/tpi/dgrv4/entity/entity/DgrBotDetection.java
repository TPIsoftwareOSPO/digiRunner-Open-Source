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
@Table(name = "dgr_bot_detection")
public class DgrBotDetection implements DgrSequenced {

	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "bot_detection_id")
	private Long botDetectionId;

	@Column(name = "bot_detection_rule")
	private String botDetectionRule;

	@Column(name = "type")
	private String type;

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
		return botDetectionId;
	}

	@Override
	public String toString() {
		return "DgrBotDetection [botDetectionId=" + botDetectionId + ", botDetectionRule=" + botDetectionRule
				+ ", type=" + type + ", createDateTime=" + createDateTime + ", createUser=" + createUser
				+ ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version=" + version + "]";
	}

	// Getters and Setters

	public Long getBotDetectionId() {
		return botDetectionId;
	}

	public void setBotDetectionId(Long botDetectionId) {
		this.botDetectionId = botDetectionId;
	}

	public String getBotDetectionRule() {
		return botDetectionRule;
	}

	public void setBotDetectionRule(String botDetectionRule) {
		this.botDetectionRule = botDetectionRule;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
