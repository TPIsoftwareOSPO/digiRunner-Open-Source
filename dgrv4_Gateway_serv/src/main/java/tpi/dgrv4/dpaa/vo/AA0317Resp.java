package tpi.dgrv4.dpaa.vo;

public class AA0317Resp {

	/** 檔名, "exportAPI_" + 現在時間(yyyy-MM-dd HH-mm-ss) + ".json", ex："exportAPI_2020-11-11 10-04-31.json" */
	private String fileName;

	/** 匯出資料 */
	private AA0317Data data;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public AA0317Data getData() {
		return data;
	}

	public void setData(AA0317Data data) {
		this.data = data;
	}

}