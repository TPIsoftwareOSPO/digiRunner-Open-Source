package tpi.dgrv4.gateway.constant;

import tpi.dgrv4.common.constant.DashboardReportTypeEnum;

/**
 * for In-Memory 機制使用, <br>
 * 設備的角色類型
 */
public enum DgrDataType {

	CLIENT("Client"),
	API("API"),
	SETTING("Setting"),
	TOKEN("Token"),
	;

	public static DashboardReportTypeEnum Setting;
	private String value;

	private DgrDataType(String value) {
		this.value = value;
	}

	public String code() {
		return this.name();
	}

	public String value() {
		return this.value;
	}
}
