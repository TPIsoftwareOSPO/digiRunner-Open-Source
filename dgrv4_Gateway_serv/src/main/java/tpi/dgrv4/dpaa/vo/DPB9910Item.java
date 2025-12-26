package tpi.dgrv4.dpaa.vo;

public class DPB9910Item {

	private Long cusSettingId;
	private String settingNo;
	private DPB9910Trunc settingName;
	private String subsettingNo;
	private DPB9910Trunc subsettingName;
	private Integer sortBy;
	
	
	public Long getCusSettingId() {
		return cusSettingId;
	}
	public void setCusSettingId(Long cusSettingId) {
		this.cusSettingId = cusSettingId;
	}
	public String getSettingNo() {
		return settingNo;
	}
	public void setSettingNo(String settingNo) {
		this.settingNo = settingNo;
	}
	public DPB9910Trunc getSettingName() {
		return settingName;
	}
	public void setSettingName(DPB9910Trunc settingName) {
		this.settingName = settingName;
	}
	public String getSubsettingNo() {
		return subsettingNo;
	}
	public void setSubsettingNo(String subsettingNo) {
		this.subsettingNo = subsettingNo;
	}
	public DPB9910Trunc getSubsettingName() {
		return subsettingName;
	}
	public void setSubsettingName(DPB9910Trunc subsettingName) {
		this.subsettingName = subsettingName;
	}
	public Integer getSortBy() {
		return sortBy;
	}
	public void setSortBy(Integer sortBy) {
		this.sortBy = sortBy;
	}

	
}