package tpi.dgrv4.common.constant;

public enum TableAct {
	C("C"), // 新增
	U("U"), // 更新
	D("D"), // 刪除
	;

	private String value;

	private TableAct(String value) {
		this.value = value;
	}

	public String code() {
		return this.name();
	}

	public String value() {
		return this.value;
	}
}
