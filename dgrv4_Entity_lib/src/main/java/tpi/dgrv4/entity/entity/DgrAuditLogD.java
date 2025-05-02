package tpi.dgrv4.entity.entity;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

import com.fasterxml.jackson.annotation.JsonIgnore;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;

@Entity
@Table(name = "DGR_AUDIT_LOGD")
@EntityListeners(FuzzyEntityListener.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class DgrAuditLogD {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "AUDIT_LONG_ID")
	private Long auditLongId;

	@Column(name = "TXN_UID")
	private String txnUid;

	@Column(name = "ENTITY_NAME")
	private String entityName;

	@Column(name = "CUD")
	private String cud;

	@Column(name = "OLD_ROW")
	private byte[] oldRow;

	@Transient
	private String oldRowString;

	@Column(name = "NEW_ROW")
	private byte[] newRow;

	@Transient
	private String newRowString;

	@Column(name = "PARAM1")
	private String param1;

	@Column(name = "PARAM2")
	private String param2;

	@Column(name = "PARAM3")
	private String param3;

	@Column(name = "PARAM4")
	private String param4;

	@Column(name = "PARAM5")
	private String param5;

	@Column(name = "STACK_TRACE")
	private String stackTrace;

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

	public Long getAuditLongId() {
		return auditLongId;
	}

	public void setAuditLongId(Long auditLongId) {
		this.auditLongId = auditLongId;
	}

	public String getTxnUid() {
		return txnUid;
	}

	public void setTxnUid(String txnUid) {
		this.txnUid = txnUid;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getCud() {
		return cud;
	}

	public void setCud(String cud) {
		this.cud = cud;
	}

	public byte[] getOldRow() {
		return oldRow;
	}

	@JsonIgnore
	public String getOldRowAsString() {
		if (null == oldRowString) {
			oldRowString = new String(oldRow, StandardCharsets.UTF_8);
		}

		return oldRowString;
	}

	public void setOldRow(byte[] oldRow) {
		this.oldRow = oldRow;
	}

	public byte[] getNewRow() {
		return newRow;
	}

	@JsonIgnore
	public String getNewRowAsString() {
		if (null == newRowString) {
			newRowString = new String(newRow, StandardCharsets.UTF_8);
		}

		return newRowString;
	}

	public void setNewRow(byte[] newRow) {
		this.newRow = newRow;
	}

	public String getParam1() {
		return param1;
	}

	public void setParam1(String param1) {
		this.param1 = param1;
	}

	public String getParam2() {
		return param2;
	}

	public void setParam2(String param2) {
		this.param2 = param2;
	}

	public String getParam3() {
		return param3;
	}

	public void setParam3(String param3) {
		this.param3 = param3;
	}

	public String getParam4() {
		return param4;
	}

	public void setParam4(String param4) {
		this.param4 = param4;
	}

	public String getParam5() {
		return param5;
	}

	public void setParam5(String param5) {
		this.param5 = param5;
	}

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
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

	@Override
	public String toString() {
		return "DgrAuditLogD [auditLongId=" + auditLongId + ", txnUid=" + txnUid + ", entityName=" + entityName
				+ ", cud=" + cud + ", oldRow=" + Arrays.toString(oldRow) + ", oldRowString=" + oldRowString
				+ ", newRow=" + Arrays.toString(newRow) + ", newRowString=" + newRowString + ", param1=" + param1
				+ ", param2=" + param2 + ", param3=" + param3 + ", param4=" + param4 + ", param5=" + param5
				+ ", stackTrace=" + stackTrace + ", createDateTime=" + createDateTime + ", createUser=" + createUser
				+ ", updateDateTime=" + updateDateTime + ", updateUser=" + updateUser + ", version=" + version + "]";
	}

	
}
