package tpi.dgrv4.entity.entity.jpql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import tpi.dgrv4.entity.component.fuzzy.Fuzzy;
import tpi.dgrv4.entity.component.fuzzy.FuzzyEntityListener;
import tpi.dgrv4.entity.entity.BasicFields;

@Entity
@Table(name = "cus_setting")
@EntityListeners(FuzzyEntityListener.class)
@IdClass(CusSettingId.class)
public class CusSetting extends BasicFields {
	
	/** 存入前請自行呼叫 SeqStoreService 產生編號 */
	@Column(name = "cus_setting_id")
	private Long cusSettingId;

	@Fuzzy
	@Column(name = "setting_name")
	private String settingName;

	@Id
	@Column(name = "subsetting_no")
	private String subsettingNo;

	@Fuzzy
	@Column(name = "subsetting_name")
	private String subsettingName;

	@Column(name = "sort_by")
	private Integer sortBy = 0; // 預設0

	@Column(name = "is_default")
	private String isDefault;

	@Column(name = "param1")
	private String param1;
	
	@Id
	@Column(name = "setting_no")
	private String settingNo;

	@Column(name = "param2")
	private String param2;

	@Column(name = "param5")
	private String param5;
	
	@Column(name = "param3")
	private String param3;

	@Column(name = "param4")
	private String param4;
	

	/* constructors */
	public CusSetting() {}

	/* methods */
	
	@Override
	public String toString() {
		return "CusSetting [cusSettingId=" + cusSettingId + ", settingNo=" + settingNo + ", settingName=" + settingName + ", subsettingNo="
				+ subsettingNo + ", subsettingName=" + subsettingName + ", sortBy=" + sortBy + ", isDefault=" + isDefault
				+ ", param1=" + param1 + ", param2=" + param2 + ", param3=" + param3 + ", param4=" + param4
				+ ", param5=" + param5 + "]";
	}

	/* getters and setters */

	public Long getCusSettingId() {
		return cusSettingId;
	}

	public void setCusSettingId(Long cusSettingId) {
		this.cusSettingId = cusSettingId;
	}

	public String getSettingNo() {
		return settingNo;
	}	

	public String getSettingName() {
		return settingName;
	}

	public void setSettingName(String settingName) {
		this.settingName = settingName;
	}

	public String getSubsettingNo() {
		return subsettingNo;
	}

	public void setSubsettingNo(String subsettingNo) {
		this.subsettingNo = subsettingNo;
	}

	public String getSubsettingName() {
		return subsettingName;
	}

	public void setSubsettingName(String subsettingName) {
		this.subsettingName = subsettingName;
	}
	
	public void setSettingNo(String settingNo) {
		this.settingNo = settingNo;
	}

	public int getSortBy() {
		return sortBy;
	}

	public void setSortBy(Integer sortBy) {
		this.sortBy = sortBy;
	}

	public String getParam1() {
		return param1;
	}
	
	public String getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(String isDefault) {
		this.isDefault = isDefault;
	}

	public String getParam3() {
		return param3;
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

	public void setParam3(String param3) {
		this.param3 = param3;
	}

	public String getParam4() {
		return param4;
	}

	public void setParam4(String param4) {
		this.param4 = param4;
	}

	public void setParam5(String param5) {
		this.param5 = param5;
	}
	
	public String getParam5() {
		return param5;
	}
}
