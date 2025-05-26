package tpi.dgrv4.dpaa.vo;

public class AA0315Req {
	
	/** 主機位址(host) 若文件沒有提供hotst主機名稱，可由前端傳入再取得。*/
	private String optionHost;
	
	/** 上傳文件的暫存檔名  由 DPB0082 回傳 */
	private String tempFileName;

	/**
	 * 模式	
	 * 
	 * 0:tsmpc,1:dgrc
	 * 
	 */
	private Integer type;

	@Override
	public String toString() {
		return "AA0315Req [optionHost=" + optionHost + ", tempFileName=" + tempFileName + "]";
	}

	public String getTempFileName() {
		return tempFileName;
	}

	public void setTempFileName(String tempFileName) {
		this.tempFileName = tempFileName;
	}

	public String getOptionHost() {
		return optionHost;
	}

	public void setOptionHost(String optionHost) {
		this.optionHost = optionHost;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
	
}
