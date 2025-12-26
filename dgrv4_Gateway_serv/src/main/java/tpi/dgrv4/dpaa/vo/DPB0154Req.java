package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0154Req extends ReqValidator{

	private String websiteStatus; // 狀態
	private String websiteName; // 名稱
	private List<DPB0154ItemReq> webSiteList; // 轉導網站清單
	private String remark; // 備註
	private String auth;
	private String sqlInjection;
	private String traffic;
	private String xss;
	private String xxe;
	private Integer tps;
	private String ignoreApi;
	private String showLog;

	public String getWebsiteStatus() {
		return websiteStatus;
	}

	public void setWebsiteStatus(String websiteStatus) {
		this.websiteStatus = websiteStatus;
	}

	public String getWebsiteName() {
		return websiteName;
	}

	public void setWebsiteName(String websiteName) {
		this.websiteName = websiteName;
	}

	public List<DPB0154ItemReq> getWebSiteList() {
		return webSiteList;
	}

	public void setWebSiteList(List<DPB0154ItemReq> webSiteList) {
		this.webSiteList = webSiteList;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public String getSqlInjection() {
		return sqlInjection;
	}

	public void setSqlInjection(String sqlInjection) {
		this.sqlInjection = sqlInjection;
	}

	public String getTraffic() {
		return traffic;
	}

	public void setTraffic(String traffic) {
		this.traffic = traffic;
	}

	public String getXss() {
		return xss;
	}

	public void setXss(String xss) {
		this.xss = xss;
	}

	public String getXxe() {
		return xxe;
	}

	public void setXxe(String xxe) {
		this.xxe = xxe;
	}

	public Integer getTps() {
		return tps;
	}

	public void setTps(Integer tps) {
		this.tps = tps;
	}

	public String getIgnoreApi() {
		return ignoreApi;
	}

	public void setIgnoreApi(String ignoreApi) {
		this.ignoreApi = ignoreApi;
	}

	public String getShowLog() {
		return showLog;
	}

	public void setShowLog(String showLog) {
		this.showLog = showLog;
	}

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		
		return Arrays.asList(
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("websiteStatus")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("websiteName")
					.isRequired()
					.maxLength(50)
					.build(),				
				new BeforeControllerRespItemBuilderSelector()
					.buildCollection(locale)
					.field("webSiteList")
					.isRequired(TsmpDpAaRtnCode._1531.getCode(), null)
					.build(),				
				new BeforeControllerRespItemBuilderSelector()
					.buildInt(locale)
					.field("tps")
					.isRequired()
					.min(0)
					.max(Integer.MAX_VALUE)
					.build());
		
	}

}
