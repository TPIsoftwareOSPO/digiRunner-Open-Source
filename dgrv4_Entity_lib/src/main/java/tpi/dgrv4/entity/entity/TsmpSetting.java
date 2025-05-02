package tpi.dgrv4.entity.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_setting")
public class TsmpSetting implements Serializable {

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "value")
	private String value;

	@Column(name = "memo")
	private String memo;

	@Override
	public String toString() {
		return "TsmpSetting [id=" + id + ", value=" + value + ", memo=" + memo + "]\n";
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	/**
	 * 1.取得的值為DB原值,不做解密,<br>
	 * 不同於 TsmpSettingService.getVal()有做ENC解密,
	 * 2.為避免 setting 畫面出現明文, <br>
	 * 取得的值來自DB原值,不做解密<br>
	 */
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

}
