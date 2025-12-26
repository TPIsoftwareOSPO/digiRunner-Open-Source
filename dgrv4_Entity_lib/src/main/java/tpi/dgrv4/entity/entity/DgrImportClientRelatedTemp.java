package tpi.dgrv4.entity.entity;

import java.io.Serializable;
import java.util.Arrays;
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
@Table(name = "dgr_import_client_related_temp")
public class DgrImportClientRelatedTemp implements Serializable, DgrSequenced {

	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDDHHMMSS)
	@Column(name = "temp_id")
	private Long tempId;

	@Column(name = "import_client_related")
	private byte[] importClientRelated;

	@Column(name = "analyze_client_related")
	private byte[] analyzeClientRelated;
	
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
	private Integer version = 1;
	

	/* constructors */

	public DgrImportClientRelatedTemp() {}


	@Override
	public String toString() {
		return "DgrImportClientTemp [tempId=" + tempId + ", importClientRelated=" + Arrays.toString(importClientRelated) + ", analyzeClientRelated="
				+ Arrays.toString(analyzeClientRelated) + ", createDateTime=" + createDateTime + ", createUser=" + createUser + ", updateDateTime="
				+ updateDateTime + ", updateUser=" + updateUser + ", version=" + version + "]";
	}


	public Long getTempId() {
		return tempId;
	}


	public void setTempId(Long tempId) {
		this.tempId = tempId;
	}


	public byte[] getImportClientRelated() {
		return importClientRelated;
	}


	public void setImportClientRelated(byte[] importClientRelated) {
		this.importClientRelated = importClientRelated;
	}


	public byte[] getAnalyzeClientRelated() {
		return analyzeClientRelated;
	}


	public void setAnalyzeClientRelated(byte[] analyzeClientRelated) {
		this.analyzeClientRelated = analyzeClientRelated;
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


	public Integer getVersion() {
		return version;
	}


	public void setVersion(Integer version) {
		this.version = version;
	}
	
	@Override
	public Long getPrimaryKey() {
		return this.tempId;
	}
}
