package tpi.dgrv4.dpaa.vo;

public class CgRespBody {

	/** 錯誤代碼，0 表示成功，其餘請參考錯誤代碼表 */
	private String code;

	/** 錯誤訊息 */
	private String message;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}