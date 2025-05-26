package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0068ApiUserApply {

	/** from tsmp_client */
	private String clientId;

	/** from tsmp_client */
	private String clientName;

	/** from tsmp_client */
	private String clientAlias;

	/** 申請API明細 from TSMP_DP_REQ_ORDERD1 */
	private List<DPB0068D1> apiList;

	/** 申請文件檔名 from TSMP_DP_FILE.file_name (ref_id = req_orderm_id) */
	private String fileName;

	/** 申請文件檔名(含路徑) from TSMP_DP_FILE.file_path */
	private String filePath;

	public DPB0068ApiUserApply() {
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public String getClientId() {
		return clientId;
	}

	public String getClientName() {
		return clientName;
	}

	public String getClientAlias() {
		return clientAlias;
	}
	
	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public List<DPB0068D1> getApiList() {
		return apiList;
	}
	
	public void setClientAlias(String clientAlias) {
		this.clientAlias = clientAlias;
	}

	public void setApiList(List<DPB0068D1> apiList) {
		this.apiList = apiList;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

}
