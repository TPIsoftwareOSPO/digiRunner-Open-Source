package tpi.dgrv4.entity.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_rtn_code")
@IdClass(value = TsmpRtnCodeId.class)
public class TsmpRtnCode implements Serializable, ITsmpRtnCode {

	@Id
	@Column(name = "tsmp_rtn_code")
	private String tsmpRtnCode;
	
	@Id
	@Column(name = "locale")
	private String locale;
	
	@Column(name = "tsmp_rtn_msg")
	private String tsmpRtnMsg;
	
	@Column(name = "tsmp_rtn_desc")
	private String tsmpRtnDesc;

	public String getTsmpRtnCode() {
		return tsmpRtnCode;
	}

	public void setTsmpRtnCode(String tsmpRtnCode) {
		this.tsmpRtnCode = tsmpRtnCode;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getTsmpRtnMsg() {
		return tsmpRtnMsg;
	}

	public void setTsmpRtnMsg(String tsmpRtnMsg) {
		this.tsmpRtnMsg = tsmpRtnMsg;
	}

	public String getTsmpRtnDesc() {
		return tsmpRtnDesc;
	}

	public void setTsmpRtnDesc(String tsmpRtnDesc) {
		this.tsmpRtnDesc = tsmpRtnDesc;
	}
	
	@Override
	public String toString() {
		return "TsmpRtnCode [tsmpRtnCode=" + tsmpRtnCode + ", locale=" + locale + ", tsmpRtnMsg=" + tsmpRtnMsg
				+ ", tsmpRtnDesc=" + tsmpRtnDesc + "]";
	}

}
