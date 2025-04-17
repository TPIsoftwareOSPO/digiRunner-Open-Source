package tpi.dgrv4.dpaa.vo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tpi.dgrv4.dpaa.record.UrlStatusRecord;

public class RealtimeDashboardAllNodeVo {
	private String nodeName = "All Node";
	private long totalRequest = 0;
	private long success = 0;
	private long fail = 0;
	private long code401 = 0;
	private long code403 = 0;
	private long others = 0;
	private Set<UrlStatusRecord> above400StatusSet = new HashSet<>();
	private int reqTps = 0;
	private int respTps = 0;
	private List<UrlStatusRecord> urlStatusRecordList = new ArrayList<>();
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	public long getTotalRequest() {
		return totalRequest;
	}
	public void setTotalRequest(long totalRequest) {
		this.totalRequest = totalRequest;
	}
	public long getSuccess() {
		return success;
	}
	public void setSuccess(long success) {
		this.success = success;
	}
	public long getFail() {
		return fail;
	}
	public void setFail(long fail) {
		this.fail = fail;
	}
	public long getCode401() {
		return code401;
	}
	public void setCode401(long code401) {
		this.code401 = code401;
	}
	public long getCode403() {
		return code403;
	}
	public void setCode403(long code403) {
		this.code403 = code403;
	}
	public long getOthers() {
		return others;
	}
	public void setOthers(long others) {
		this.others = others;
	}
	public Set<UrlStatusRecord> getAbove400StatusSet() {
		return above400StatusSet;
	}
	public void setAbove400StatusSet(Set<UrlStatusRecord> above400StatusSet) {
		this.above400StatusSet = above400StatusSet;
	}
	public int getReqTps() {
		return reqTps;
	}
	public void setReqTps(int reqTps) {
		this.reqTps = reqTps;
	}
	public int getRespTps() {
		return respTps;
	}
	public void setRespTps(int respTps) {
		this.respTps = respTps;
	}
	public List<UrlStatusRecord> getUrlStatusRecordList() {
		return urlStatusRecordList;
	}
	public void setUrlStatusRecordList(List<UrlStatusRecord> urlStatusRecordList) {
		this.urlStatusRecordList = urlStatusRecordList;
	}
	
	
}
