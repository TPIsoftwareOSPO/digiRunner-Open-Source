package tpi.dgrv4.gateway.vo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FixedCacheVo {

	private byte[] data;
	private Long dataTimestamp;
	private String respStr;
//	private String fileSha256;
	private boolean isFile;
	private Map<String, List<String>> respHeader;
	private int statusCode;
	
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
	public Long getDataTimestamp() {
		return dataTimestamp;
	}
	public void setDataTimestamp(Long dataTimestamp) {
		this.dataTimestamp = dataTimestamp;
	}
	public String getRespStr() {
		return respStr;
	}
	public void setRespStr(String respStr) {
		this.respStr = respStr;
	}
	public String getFileSha256() {
//		return fileSha256;
		return this.respStr;
	}
	public void setFileSha256(String fileSha256) {
		this.setRespStr(fileSha256);
	}
	public boolean isFile() {
		return isFile;
	}
	public void setFile(boolean isFile) {
		this.isFile = isFile;
	}
	public Map<String, List<String>> getRespHeader() {
		return respHeader;
	}
	public void setRespHeader(Map<String, List<String>> respHeader) {
		this.respHeader = respHeader;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	@Override
	public String toString() {
		return "FixedCacheVo [data=" + Arrays.toString(data) + ", dataTimestamp=" + dataTimestamp + ", respStr=" + respStr + ", isFile=" + isFile
				+ ", respHeader=" + respHeader + ", statusCode=" + statusCode + "]";
	}
	
	
	
	
	
	
}
