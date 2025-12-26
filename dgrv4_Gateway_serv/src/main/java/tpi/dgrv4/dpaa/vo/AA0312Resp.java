package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.Map;

public class AA0312Resp {

	/** 表頭, ex: [ {"key1": ["a", "b"], "key2": [...]}, {...} ] */
	private List<Map<String, List<String>>> headerList;

	/** 表身 */
	private String resBody;

	/** 狀態 */
	private Integer resStatus;

	public List<Map<String, List<String>>> getHeaderList() {
		return headerList;
	}

	public void setHeaderList(List<Map<String, List<String>>> headerList) {
		this.headerList = headerList;
	}

	public String getResBody() {
		return resBody;
	}

	public void setResBody(String resBody) {
		this.resBody = resBody;
	}

	public Integer getResStatus() {
		return resStatus;
	}

	public void setResStatus(Integer resStatus) {
		this.resStatus = resStatus;
	}

}