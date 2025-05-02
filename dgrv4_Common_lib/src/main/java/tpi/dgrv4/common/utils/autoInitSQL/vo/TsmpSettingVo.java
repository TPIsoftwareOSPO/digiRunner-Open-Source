package tpi.dgrv4.common.utils.autoInitSQL.vo;

public class TsmpSettingVo {

	private String id;

	private String value;

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
