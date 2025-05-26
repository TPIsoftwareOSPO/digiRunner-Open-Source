package tpi.dgrv4.common.constant;

public enum TsmpDpFileType {
	API("API"),							// 未使用
	API_TH("API_TH"),					// 主題圖示
	API_ATTACHMENT("API_ATTACHMENT"),	// API說明文件
	APP_IMAGE("APP_IMG"),				// 實例圖示
	APP_ATTACHMENT("APP_ATTACHMENT"),	// 實例附件
	DOC("DOC"),							// 未使用
	DOC_API("DOC_API"),					// 未使用
	DOC_GUIDELINE("DOC_GUIDELINE"),		// 未使用(API開發標準作業手冊)
	FAQ_ATTACHMENT("FAQ_ATTACHMENT"),	// 問答附件
	MEMBER_APPLY("MEMBER_APPLY"),		// 會員申請上傳檔案
	D2_ATTACHMENT("D2_ATTACHMENT"),		// 審查明細(TSMP_DP_REQ_ORDERD2)附件
	M_ATTACHMENT("TSMP_DP_REQ_ORDERM"),	// 審查單(TSMP_DP_REQ_ORDERM)附件
	MAIL_CONTENT("MAIL_CONTENT"),		// Mail內容檔案
	TEMP("TEMP"),                       // 暫存檔
	KEY_PAIR("KEY_PAIR"),				// 公、私鑰
	REG_MODULE_DOC("REG_MODULE_DOC"),	// 註冊模組介接規格文件
	DPB0082("DPB0082"),                 // 暫存檔(controller unit test)
	REG_COMP_API("REG_COMP_API"),		// 註冊/組合API的匯出檔案
	TSMP_DP_APPT_JOB("TSMP_DP_APPT_JOB"),// 排程作業相關檔案
	DASHBOARD_TEMP_DATA("DASHBOARD_TEMP_DATA"), //DASHBOARD未匹配紀錄
	API_MODIFY_BATCH("API_MODIFY_BATCH"), //API批量修改的暫存檔
	HTTP_UTIL_JOB_API("HTTP_UTIL_JOB_API")// PASM 產生報表
	;

	private String value;

	private TsmpDpFileType(String value) {
		this.value = value;
	}

	public String code() {
		return this.name();
	}

	public String value() {
		return this.value;
	}

}
