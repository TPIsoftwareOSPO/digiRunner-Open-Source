package tpi.dgrv4.gateway.vo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class AutoCacheRespVo implements Serializable{
	
	private String id;
	private String respStr;
	private byte[] httpRespArray;
	private Map<String, List<String>> respHeader;
	private int statusCode;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getRespStr() {
		return respStr;
	}
	public void setRespStr(String respStr) {
		this.respStr = respStr;
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
	public byte[] getHttpRespArray() {
		return httpRespArray;
	}
	public void setHttpRespArray(byte[] httpRespArray) {
		this.httpRespArray = httpRespArray;
	}
	
	

}
