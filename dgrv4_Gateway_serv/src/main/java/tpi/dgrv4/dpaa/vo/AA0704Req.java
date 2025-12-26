package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;
import tpi.dgrv4.dpaa.constant.RegexpConstant;

public class AA0704Req extends ReqValidator{

	/** 告警編號*/
	private String alertId;
	
	/** 告警說明*/
	private String alertDesc;
	
	/** 狀態*/
	private String alertEnabled;
	
	/** Alert Interval 告警間隔(sec)*/
	private Integer alertInterval;
	
	/** 告警訊息*/
	private String alertMsg;
	
	/** 告警名稱*/
	private String alertName;
	
	/** 告警系統*/
	private String alertSys;
	
	/** 告警類型*/
	private String alertType;
	
	/** 客製化告警*/
	private String cFlag;
	
	/** Duration 問題持續時間(sec)*/
	private Integer duration;
	
	/** 例外日期*/
	private String exDays;
	
	/** 開始時間 + 結束時間*/
	private String exTime;
	
	/** 例外類型*/
	private String exType;
	
	/** Line 告警*/
	private String imFlag;
	
	/** Line Token*/
	private String imId;

	private String imType;
	
	/** 告警角色清單*/
	private List<String> roleIDList;
	
	/** Keyword與API共用*/
	private String searchMapString;
	
	/** Threshold 門檻 (次數或%)*/
	private Integer threshold;
	
	public String getAlertId() {
		return alertId;
	}

	public void setAlertId(String alertId) {
		this.alertId = alertId;
	}

	public String getAlertDesc() {
		return alertDesc;
	}

	public void setAlertDesc(String alertDesc) {
		this.alertDesc = alertDesc;
	}

	public String getAlertEnabled() {
		return alertEnabled;
	}

	public void setAlertEnabled(String alertEnabled) {
		this.alertEnabled = alertEnabled;
	}

	public Integer getAlertInterval() {
		return alertInterval;
	}

	public void setAlertInterval(Integer alertInterval) {
		this.alertInterval = alertInterval;
	}

	public String getAlertMsg() {
		return alertMsg;
	}

	public void setAlertMsg(String alertMsg) {
		this.alertMsg = alertMsg;
	}

	public String getAlertName() {
		return alertName;
	}

	public void setAlertName(String alertName) {
		this.alertName = alertName;
	}

	public void setAlertSys(String alertSys) {
		this.alertSys = alertSys;
	}
	
	public String getAlertSys() {
		return alertSys;
	}

	public String getAlertType() {
		return alertType;
	}

	public void setAlertType(String alertType) {
		this.alertType = alertType;
	}

	public String getcFlag() {
		return cFlag;
	}

	public void setcFlag(String cFlag) {
		this.cFlag = cFlag;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public String getExDays() {
		return exDays;
	}

	public void setExDays(String exDays) {
		this.exDays = exDays;
	}

	public String getExTime() {
		return exTime;
	}

	public void setExTime(String exTime) {
		this.exTime = exTime;
	}

	public String getExType() {
		return exType;
	}

	public void setExType(String exType) {
		this.exType = exType;
	}

	public String getImFlag() {
		return imFlag;
	}

	public void setImFlag(String imFlag) {
		this.imFlag = imFlag;
	}

	public String getImId() {
		return imId;
	}

	public void setImId(String imId) {
		this.imId = imId;
	}

	public String getImType() {
		return imType;
	}

	public void setImType(String imType) {
		this.imType = imType;
	}

	public List<String> getRoleIDList() {
		return roleIDList;
	}

	public void setRoleIDList(List<String> roleIDList) {
		this.roleIDList = roleIDList;
	}

	public String getSearchMapString() {
		return searchMapString;
	}

	public void setSearchMapString(String searchMapString) {
		this.searchMapString = searchMapString;
	}

	public Integer getThreshold() {
		return threshold;
	}

	public void setThreshold(Integer threshold) {
		this.threshold = threshold;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("alertId")
					.isRequired()
					.build()
			    ,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("alertDesc")
					.maxLength(200)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("alertEnabled")
					.isRequired()
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildInt(locale)
					.field("alertInterval")
					.isRequired()
					.min(1)
					.max(3000000)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("alertMsg")
					.isRequired()
					.maxLength(150)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("alertName")
					.isRequired()
					.maxLength(30)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("alertSys")
					.maxLength(20)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("alertType")
					.isRequired()
					.maxLength(20)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildInt(locale)
					.field("duration")
					.isRequired()
					.min(1)
					.max(3000000)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("exDays")
					.maxLength(100)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("exTime")
					.maxLength(100)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("exType")
					.minLength(1)
					.maxLength(1)
					.pattern(RegexpConstant.EX_TYPE, TsmpDpAaRtnCode._1427.getCode(), null)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("imId")
					.maxLength(100)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("imType")
					.maxLength(20)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildCollection(locale)
					.field("roleIDList")
					.isRequired()
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("searchMapString")
					.maxLength(255)
					.build()
				,
				new BeforeControllerRespItemBuilderSelector()
					.buildInt(locale)
					.field("threshold")
					.isRequired()
					.min(0)
					.max(3000000)
					.build()
			});
	}


}
