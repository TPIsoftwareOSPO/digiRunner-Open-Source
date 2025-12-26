package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import jakarta.persistence.Column;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class AA0201Req extends ReqValidator{

	/**用戶端帳號*/
	private String clientID;
	
	/**用戶端代號*/
	private String clientName;
	
	/**用戶端名稱*/
	private String clientAlias;
	
	/**簽呈編號*/
	private String signupNum;
	
	/**密碼*/
	private String clientBlock;
	
	/**電子郵件帳號*/
	private String emails;
	
	/**TPS 若tps == null，則tps=10*/
	private String tps;
	
	/**狀態 使用BcryptParam,
	 * ITEM_NO='ENABLE_FLAG' , DB儲存值對應代碼如下:
	 * DB值 (PARAM1) = 中文說明;  
	 * 1=啟用, 2=停用 
     */
	private String encodeStatus;
	
	/**開放狀態
	 * 使用BcryptParam, 
	 * ITEM_NO='API_AUTHORITY' , DB儲存值對應代碼如下: 
	 * DB值 (SUBITEM_NO) = 中文說明; 
	 * 0=對內及對外, 1=對外, 2=對內
	 * */
	private String publicFlag;
	
	/**主機清單*/
	private List<AA0201HostReq> hostList;

	/**API 配額 若apiQuota == null，則apiQuota=0*/
	private String apiQuota;
	
	/**擁有者*/
	private String owner;
	
	/**用戶優先權 若cPriority == null，則cPriority=5*/
	private String cPriority;
	
	/**台新客製*/
	private List<String> groupIDList;
	
	/**備註 */
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

	public String getClientAlias() {
		return clientAlias;
	}

	public void setClientAlias(String clientAlias) {
		this.clientAlias = clientAlias;
	}

	public String getSignupNum() {
		return signupNum;
	}

	public void setSignupNum(String signupNum) {
		this.signupNum = signupNum;
	}

	public String getClientBlock() {
		return clientBlock;
	}

	public void setClientBlock(String clientBlock) {
		this.clientBlock = clientBlock;
	}

	public String getEmails() {
		return emails;
	}

	public void setEmails(String emails) {
		this.emails = emails;
	}

	public String getTps() {
		return tps;
	}

	public void setTps(String tps) {
		this.tps = tps;
	}

	public String getEncodeStatus() {
		return encodeStatus;
	}

	public void setEncodeStatus(String encodeStatus) {
		this.encodeStatus = encodeStatus;
	}

	public String getPublicFlag() {
		return publicFlag;
	}

	public void setPublicFlag(String publicFlag) {
		this.publicFlag = publicFlag;
	}

	public List<AA0201HostReq> getHostList() {
		return hostList;
	}

	public void setHostList(List<AA0201HostReq> hostList) {
		this.hostList = hostList;
	}

	public String getApiQuota() {
		return apiQuota;
	}

	public void setApiQuota(String apiQuota) {
		this.apiQuota = apiQuota;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getcPriority() {
		return cPriority;
	}

	public void setcPriority(String cPriority) {
		this.cPriority = cPriority;
	}

	public List<String> getGroupIDList() {
		return groupIDList;
	}

	public void setGroupIDList(List<String> groupIDList) {
		this.groupIDList = groupIDList;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public void setClientStartDate(String clientStartDate) {
		this.clientStartDate = clientStartDate;
	}
	
	public String getClientStartDate() {
		return clientStartDate;
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

	public String getClientEndTimePerDay() {
		return clientEndTimePerDay;
	}

	public void setClientEndTimePerDay(String clientEndTimePerDay) {
		this.clientEndTimePerDay = clientEndTimePerDay;
	}

	public String getTimeZone() {
		return timeZone;
	}
	
	public void setClientStartTimePerDay(String clientStartTimePerDay) {
		this.clientStartTimePerDay = clientStartTimePerDay;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("clientID")
				.pattern(RegexpConstant.ENGLISH_NUMBER_EQUALSIGN, TsmpDpAaRtnCode._2008.getCode(), null)
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("remark")
				.maxLength(300)
				.build()
			});
	}
}
