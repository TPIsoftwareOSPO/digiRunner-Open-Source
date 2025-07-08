package tpi.dgrv4.entity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;

@Entity
@Table(name = "DGR_WEBHOOK_API_MAP")
public class DgrWebhookApiMap implements DgrSequenced {

	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "WEBHOOK_API_MAP_ID")
	private Long webhookApiMapId;

	@Column(name = "API_KEY")
	private String apiKey;
	
	@Column(name = "MODULE_NAME")
	private String moduleName;
	
	@Column(name = "WEBHOOK_NOTIFY_ID")
	private Long webhookNotifyId;
	
	@ManyToOne
    @JoinColumn(name = "WEBHOOK_NOTIFY_ID", insertable = false, updatable = false)
    private DgrWebhookNotify dgrWebhookNotify;
	
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
		return webhookApiMapId;
	}

	/* constructors */

	public DgrWebhookApiMap() {}

	/* methods */

	@Override
	public String toString() {
		return "DgrApiWebhookMap [webhookApiMapId=" + webhookApiMapId + ", apiKey=" + apiKey
				+ ", moduleName=" + moduleName + ", webhookNotifyId=" + webhookNotifyId + "]";
	}

	public Long getWebhookApiMapId() {
		return webhookApiMapId;
	}

	public void setWebhookApiMapId(Long webhookApiMapId) {
		this.webhookApiMapId = webhookApiMapId;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public Long getWebhookNotifyId() {
		return webhookNotifyId;
	}

	public void setWebhookNotifyId(Long webhookNotifyId) {
		this.webhookNotifyId = webhookNotifyId;
	}

	public DgrWebhookNotify getDgrWebhookNotify() {
		return dgrWebhookNotify;
	}

	public void setDgrWebhookNotify(DgrWebhookNotify dgrWebhookNotify) {
		this.dgrWebhookNotify = dgrWebhookNotify;
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
