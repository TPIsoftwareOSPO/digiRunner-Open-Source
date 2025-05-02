package tpi.dgrv4.entity.vo;

import java.util.Date;
import java.util.List;

import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;

public class DPB0067SearchCriteria {

	/** TSMP_DP_REQ_ORDERM.REQ_ORDERM_ID */
	private TsmpDpReqOrderm lastRecord;

	private String[] words;

	private Integer pageSize;

	private Date startDate;

	private Date endDate;

	private String reqType;

	private String reqSubtype;

	private String clientId;

	private String userId;

	private String userName;

	private List<String> orgDescList;

	private List<String> roleIdList;
	
	public DPB0067SearchCriteria() {}

	public TsmpDpReqOrderm getLastRecord() {
		return lastRecord;
	}

	public void setLastRecord(TsmpDpReqOrderm lastRecord) {
		this.lastRecord = lastRecord;
	}

	public String[] getWords() {
		return words;
	}

	public void setWords(String[] words) {
		this.words = words;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getReqType() {
		return reqType;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
	}

	public void setReqSubtype(String reqSubtype) {
		this.reqSubtype = reqSubtype;
	}
	
	public String getReqSubtype() {
		return reqSubtype;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public List<String> getOrgDescList() {
		return orgDescList;
	}

	public void setOrgDescList(List<String> orgDescList) {
		this.orgDescList = orgDescList;
	}

	public List<String> getRoleIdList() {
		return roleIdList;
	}

	public void setRoleIdList(List<String> roleIdList) {
		this.roleIdList = roleIdList;
	}

}
