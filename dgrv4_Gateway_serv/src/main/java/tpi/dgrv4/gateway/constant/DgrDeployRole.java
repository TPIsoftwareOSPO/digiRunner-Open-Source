package tpi.dgrv4.gateway.constant;

/**
 * for In-Memory 機制使用, <br>
 * 修改的 AC 資料類型
 */
public enum DgrDeployRole {

	LANDING("Landing"),
	MEMORY("Memory"),
	DB127("127db"),
	;
	private String value;

	private DgrDeployRole(String value) {
		this.value = value;
	}

	public String code() {
		return this.name();
	}

	public String value() {
		return this.value;
	}
}
