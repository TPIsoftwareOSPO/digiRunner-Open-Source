package tpi.dgrv4.dpaa.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AA0203Resp {

	/**API 配額*/
	private String apiQuota;
	

	private String apiUsed;
	
	/**用戶優先權*/
	private String cPriority;
	
	/**用戶端名稱*/
	private String clientAlias;
		
	/**用戶端帳號*/
	private String clientID;
	
	/**用戶端代號*/
	private String clientName;
		
	/**建立日期*/
	private String createDate;
	
	/**建立人員*/
	private String createUser;
	
	/**電子郵件帳號*/
	private String emails;
	
	/**允許密碼錯誤上限*/
	private String failTreshhold;
	
	/**擁有者*/
	private String owner;
	
	/**密碼錯誤次數*/
	//checkmarx, Excessive Data Exposure
	@JsonProperty("pwdFailTimes")
	private String mimaFailTimes;
	
	/**簽呈編號*/
	private String signupNum;
	
	/**狀態*/
	private String status;
	
	/**TPS (預設 10)*/
	private String tps;
	
	/**更新日期*/
	private String updateDate;
	
	/**更新人員*/
	private String updateUser;
	
	/**主機清單*/
	private List<AA0203Host> hostList;
	
	/**群組*/
	private List<AA0203GroupInfo> groupInfoList;
	
	/**安全等級*/
	private AA0203SecurityLV securityLV;
	
	/**虛擬群組*/
	private List<AA0203VgroupInfo> vgroupInfoList;
	
	/**開放狀態*/
	private String publicFlag;

	/** 備註*/
	private String remark;

	/**開始日期**/
	private String clientStartDate;
	
	/**到期日期**/
	private String clientEndDate;
	
	/**開始服務時間**/
	private String clientStartTimePerDay;
	
	/**結束服務時間**/
	private String clientEndTimePerDay;
	
	/**時區**/
	private String timeZone;
	
	public String getApiQuota() {
		return apiQuota;
	}

	public void setApiQuota(String apiQuota) {
		this.apiQuota = apiQuota;
	}

	public String getApiUsed() {
		return apiUsed;
	}

	public void setApiUsed(String apiUsed) {
		this.apiUsed = apiUsed;
	}

	public String getcPriority() {
		return cPriority;
	}

	public void setcPriority(String cPriority) {
		this.cPriority = cPriority;
	}

	public String getClientAlias() {
		return clientAlias;
	}

	public void setClientAlias(String clientAlias) {
		this.clientAlias = clientAlias;
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public String getEmails() {
		return emails;
	}

	public void setEmails(String emails) {
		this.emails = emails;
	}

	public String getFailTreshhold() {
		return failTreshhold;
	}

	public void setFailTreshhold(String failTreshhold) {
		this.failTreshhold = failTreshhold;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getMimaFailTimes() {
		return mimaFailTimes;
	}

	public void setMimaFailTimes(String mimaFailTimes) {
		this.mimaFailTimes = mimaFailTimes;
	}

	public String getSignupNum() {
		return signupNum;
	}

	public void setSignupNum(String signupNum) {
		this.signupNum = signupNum;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTps() {
		return tps;
	}

	public void setTps(String tps) {
		this.tps = tps;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public List<AA0203Host> getHostList() {
		return hostList;
	}

	public void setHostList(List<AA0203Host> hostList) {
		this.hostList = hostList;
	}

	public List<AA0203GroupInfo> getGroupInfoList() {
		return groupInfoList;
	}

	public void setGroupInfoList(List<AA0203GroupInfo> groupInfoList) {
		this.groupInfoList = groupInfoList;
	}

	public AA0203SecurityLV getSecurityLV() {
		return securityLV;
	}

	public void setSecurityLV(AA0203SecurityLV securityLV) {
		this.securityLV = securityLV;
	}

	public List<AA0203VgroupInfo> getVgroupInfoList() {
		return vgroupInfoList;
	}

	public void setVgroupInfoList(List<AA0203VgroupInfo> vgroupInfoList) {
		this.vgroupInfoList = vgroupInfoList;
	}

	public String getPublicFlag() {
		return publicFlag;
	}

	public void setPublicFlag(String publicFlag) {
		this.publicFlag = publicFlag;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public String getClientStartDate() {
		return clientStartDate;
	}

	public void setClientStartDate(String clientStartDate) {
		this.clientStartDate = clientStartDate;
	}

	public String getClientEndDate() {
		return clientEndDate;
	}

	public void setClientEndDate(String clientEndDate) {
		this.clientEndDate = clientEndDate;
	}

	public String getClientStartTimePerDay() {
		return clientStartTimePerDay;
	}

	public void setClientStartTimePerDay(String clientStartTimePerDay) {
		this.clientStartTimePerDay = clientStartTimePerDay;
	}

	public String getClientEndTimePerDay() {
		return clientEndTimePerDay;
	}

	public void setClientEndTimePerDay(String clientEndTimePerDay) {
		this.clientEndTimePerDay = clientEndTimePerDay;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	
}
