package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;	

public class AA0204Req extends ReqValidator{

	/**用戶端帳號*/
	private String clientID;
	
	/**原用戶優先權*/
	private String cPriority;
	
	/**用戶優先權*/
	private String newCPriority;
	
	/**原API 配額*/
	private String apiQuota;
	
	/**API 配額*/
	private String newApiQuota;
	
	/**原用戶端名稱*/
	private String clientAlias;
	
	/**用戶端名稱*/
	private String newClientAlias;
		
	/**原用戶端代號*/
	private String clientName;
	
	/**用戶端代號*/
	private String newClientName;
	
	/**原電子郵件帳號*/
	private String emails;
	
	/**電子郵件帳號*/
	private String newEmails;
	
	/**原擁有者*/
	private String owner;
	
	/**擁有者*/
	private String newOwner;
	
	/**原簽呈編號*/
	private String signupNum;
	
	/**簽呈編號*/
	private String newSignupNum;
		
	/**原TPS*/
	private String tps;
	
	/**TPS*/
	private String newTps;
	
	/**原開放狀態*/
	private String encodePublicFlag;
	
	/**開放狀態*/
	private String newEncodePublicFlag;
	
	/**原主機清單*/
	private List<AA0204HostReq> hostList;
	
	/**主機清單*/
	private List<AA0204HostReq> newHostList;

	private List<String> groupIDList;

	private List<String> newGroupIDList;
	
	/** 原備註*/
	private String remark;
	
	/** 備註*/
	private String newRemark;
	
	/**原開始日期**/
	private String clientStartDate;
	
	/**開始日期**/
	private String newClientStartDate;
	
	/**原到期日期**/
	private String clientEndDate;
	
	/**到期日期**/
	private String newClientEndDate;
	
	/**原開始服務時間**/
	private String clientStartTimePerDay;
	
	/**開始服務時間**/
	private String newClientStartTimePerDay;
	
	/**原結束服務時間**/
	private String clientEndTimePerDay;
	
	/**結束服務時間**/
	private String newClientEndTimePerDay;
	
	/**原時區**/
	private String timeZone;
	
	/**時區**/
	private String newTimeZone;


	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getcPriority() {
		return cPriority;
	}

	public void setcPriority(String cPriority) {
		this.cPriority = cPriority;
	}

	public String getNewCPriority() {
		return newCPriority;
	}

	public void setNewCPriority(String newCPriority) {
		this.newCPriority = newCPriority;
	}

	public String getApiQuota() {
		return apiQuota;
	}

	public void setApiQuota(String apiQuota) {
		this.apiQuota = apiQuota;
	}

	public String getNewApiQuota() {
		return newApiQuota;
	}

	public void setNewApiQuota(String newApiQuota) {
		this.newApiQuota = newApiQuota;
	}

	public String getClientAlias() {
		return clientAlias;
	}

	public void setClientAlias(String clientAlias) {
		this.clientAlias = clientAlias;
	}

	public String getNewClientAlias() {
		return newClientAlias;
	}

	public void setNewClientAlias(String newClientAlias) {
		this.newClientAlias = newClientAlias;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getNewClientName() {
		return newClientName;
	}

	public void setNewClientName(String newClientName) {
		this.newClientName = newClientName;
	}

	public String getEmails() {
		return emails;
	}

	public void setEmails(String emails) {
		this.emails = emails;
	}

	public String getNewEmails() {
		return newEmails;
	}

	public void setNewEmails(String newEmails) {
		this.newEmails = newEmails;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getNewOwner() {
		return newOwner;
	}

	public void setNewOwner(String newOwner) {
		this.newOwner = newOwner;
	}

	public String getSignupNum() {
		return signupNum;
	}

	public void setSignupNum(String signupNum) {
		this.signupNum = signupNum;
	}

	public String getNewSignupNum() {
		return newSignupNum;
	}

	public void setNewSignupNum(String newSignupNum) {
		this.newSignupNum = newSignupNum;
	}

	public String getTps() {
		return tps;
	}

	public void setTps(String tps) {
		this.tps = tps;
	}

	public String getNewTps() {
		return newTps;
	}

	public void setNewTps(String newTps) {
		this.newTps = newTps;
	}

	public String getEncodePublicFlag() {
		return encodePublicFlag;
	}

	public void setEncodePublicFlag(String encodePublicFlag) {
		this.encodePublicFlag = encodePublicFlag;
	}

	public String getNewEncodePublicFlag() {
		return newEncodePublicFlag;
	}

	public void setNewEncodePublicFlag(String newEncodePublicFlag) {
		this.newEncodePublicFlag = newEncodePublicFlag;
	}

	public List<AA0204HostReq> getHostList() {
		return hostList;
	}

	public void setHostList(List<AA0204HostReq> hostList) {
		this.hostList = hostList;
	}

	public List<AA0204HostReq> getNewHostList() {
		return newHostList;
	}

	public void setNewHostList(List<AA0204HostReq> newHostList) {
		this.newHostList = newHostList;
	}

	public List<String> getGroupIDList() {
		return groupIDList;
	}

	public void setGroupIDList(List<String> groupIDList) {
		this.groupIDList = groupIDList;
	}

	public List<String> getNewGroupIDList() {
		return newGroupIDList;
	}

	public void setNewGroupIDList(List<String> newGroupIDList) {
		this.newGroupIDList = newGroupIDList;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getNewRemark() {
		return newRemark;
	}

	public void setNewRemark(String newRemark) {
		this.newRemark = newRemark;
	}
	
	public String getClientStartDate() {
		return clientStartDate;
	}

	public void setClientStartDate(String clientStartDate) {
		this.clientStartDate = clientStartDate;
	}

	public String getNewClientStartDate() {
		return newClientStartDate;
	}

	public void setNewClientStartDate(String newClientStartDate) {
		this.newClientStartDate = newClientStartDate;
	}

	public String getClientEndDate() {
		return clientEndDate;
	}

	public void setClientEndDate(String clientEndDate) {
		this.clientEndDate = clientEndDate;
	}

	public String getNewClientEndDate() {
		return newClientEndDate;
	}

	public void setNewClientEndDate(String newClientEndDate) {
		this.newClientEndDate = newClientEndDate;
	}

	public String getClientStartTimePerDay() {
		return clientStartTimePerDay;
	}

	public void setClientStartTimePerDay(String clientStartTimePerDay) {
		this.clientStartTimePerDay = clientStartTimePerDay;
	}

	public String getNewClientStartTimePerDay() {
		return newClientStartTimePerDay;
	}

	public void setNewClientStartTimePerDay(String newClientStartTimePerDay) {
		this.newClientStartTimePerDay = newClientStartTimePerDay;
	}

	public String getClientEndTimePerDay() {
		return clientEndTimePerDay;
	}

	public void setClientEndTimePerDay(String clientEndTimePerDay) {
		this.clientEndTimePerDay = clientEndTimePerDay;
	}

	public String getNewClientEndTimePerDay() {
		return newClientEndTimePerDay;
	}

	public void setNewClientEndTimePerDay(String newClientEndTimePerDay) {
		this.newClientEndTimePerDay = newClientEndTimePerDay;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getNewTimeZone() {
		return newTimeZone;
	}

	public void setNewTimeZone(String newTimeZone) {
		this.newTimeZone = newTimeZone;
	}
	
	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("newRemark")
				.maxLength(300)
				.build()
			});
	}
	
	
}
