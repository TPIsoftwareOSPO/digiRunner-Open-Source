package tpi.dgrv4.common.constant;

public enum MultilingualSheetNameEnum {
	/**setting*/
	SETTING("setting"),
	/**items*/
	ITEMS("items"),
	/**mail*/
	MAIL_TPLT("mailTplt"),
	/**func*/
	FUNC("func"),
	/**func_reportUrl*/
	FUNC_REPORT_URL("func_reportUrl"),
	/**embeddedFunc*/
	EMBEDDED_FUNC("embeddedFunc"),
	/**embeddedFunc_reportUrl*/
	EMBEDDED_FUNC_REPORT_URL("embeddedFunc_reportUrl"),
	/**rtnCode*/
	RTNCODE("rtnCode")
	;

	private String value;

	private MultilingualSheetNameEnum(String value) {
		this.value = value;
	}

	public String value() {
		return this.value;
	}

}
