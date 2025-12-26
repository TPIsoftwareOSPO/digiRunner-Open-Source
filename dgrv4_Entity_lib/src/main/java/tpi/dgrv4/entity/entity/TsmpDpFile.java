package tpi.dgrv4.entity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import tpi.dgrv4.entity.component.fuzzy.Fuzzy;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;

@Entity
@Table(name = "tsmp_dp_file")
@EntityListeners(FuzzyEntityListener.class)
@TableGenerator(name = "seq_store", initialValue = 2_000_000_000)
public class TsmpDpFile extends BasicFields {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "seq_store")
	@Column(name = "file_id")
	private Long fileId;

	@Fuzzy
	@Column(name = "file_name")
	private String fileName;

	@Fuzzy
	@Column(name = "file_path")
	private String filePath;

	@Column(name = "ref_file_cate_code")
	private String refFileCateCode;

	@Column(name = "ref_id")
	private Long refId;
	
	@Column(name = "is_blob")
	private String isBlob = "N";
	
	@Column(name = "is_tmpfile")
	private String isTmpfile = "N";
	
	@Column(name = "blob_data")
	private byte[] blobData;

	/* constructors */

	public TsmpDpFile() {}
	
	public TsmpDpFile(Long refId, Long fileId, String fileName, Date createDateTime) {
		this.refId = refId;
		this.fileId = fileId;
		this.fileName = fileName;
		setCreateDateTime(createDateTime);
	}
	
	public TsmpDpFile(Long fileId, String fileName, String filePath, String refFileCateCode, Long refId, String isBlob, //
			String isTmpfile, Date createDateTime, String createUser, Date updateDateTime, String updateUser, Long version 
			) {
		this.fileId = fileId;
		this.fileName = fileName;
		this.filePath = filePath;
		this.refFileCateCode = refFileCateCode;
		this.refId = refId;
		this.isBlob = isBlob;
		this.isTmpfile = isTmpfile;
		super.setCreateDateTime(createDateTime);
		super.setCreateUser(createUser);
		super.setUpdateDateTime(updateDateTime);
		super.setUpdateUser(updateUser);
		super.setVersion(version);
	}

	/* methods */

	@Override
	public String toString() {
		return "TsmpDpFile [fileId=" + fileId + ", fileName=" + fileName + ", filePath=" + filePath
				+ ", refFileCateCode=" + refFileCateCode + ", refId=" + refId + ", isBlob=" + isBlob
				+ ", isTmpfile=" + isTmpfile +", getCreateDateTime()=" + getCreateDateTime()
				+ ", getCreateUser()=" + getCreateUser() + ", getUpdateDateTime()=" + getUpdateDateTime()
				+ ", getUpdateUser()=" + getUpdateUser() + ", getVersion()=" + getVersion()
				+ ", getKeywordSearch()=" + getKeywordSearch() + "]";
	}

	/* getters and setters */

	public Long getFileId() {
		return fileId;
	}

	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getRefFileCateCode() {
		return refFileCateCode;
	}

	public void setRefFileCateCode(String refFileCateCode) {
		this.refFileCateCode = refFileCateCode;
	}

	public Long getRefId() {
		return refId;
	}

	public void setRefId(Long refId) {
		this.refId = refId;
	}

	public String getIsBlob() {
		return isBlob;
	}

	public void setIsBlob(String isBlob) {
		this.isBlob = isBlob;
	}

	public String getIsTmpfile() {
		return isTmpfile;
	}

	public void setIsTmpfile(String isTmpfile) {
		this.isTmpfile = isTmpfile;
	}

	public byte[] getBlobData() {
		return blobData;
	}

	public void setBlobData(byte[] blobData) {
		this.blobData = blobData;
	}

}
