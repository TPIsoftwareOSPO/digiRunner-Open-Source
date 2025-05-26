package tpi.dgrv4.dpaa.vo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

/**
 * DPB0155Req VO object
 */
public class DPB0155Req extends ReqValidator implements Serializable {

	// DGR_WEBSITE的主鍵 (DGR_WEBSITE primary key)
    private Long dgrWebsiteId;

    // 狀態 (Status)
    private String websiteStatus;

    // 名稱 (Name)
    private String websiteName;

    // 轉導網站清單 (Redirect website list)
    private List<DPB0155ItemReq> webSiteList;

    // 備註 (Remark)
    private String remark;
    
    private String auth;
	private String sqlInjection;
	private String traffic;
	private String xss;
	private String xxe;
	private Integer tps;
	private String ignoreApi;
	private String showLog;

	public Long getDgrWebsiteId() {
		return dgrWebsiteId;
	}

	public void setDgrWebsiteId(Long dgrWebsiteId) {
		this.dgrWebsiteId = dgrWebsiteId;
	}

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

	public List<DPB0155ItemReq> getWebSiteList() {
		return webSiteList;
	}

	public void setWebSiteList(List<DPB0155ItemReq> webSiteList) {
		this.webSiteList = webSiteList;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

    public boolean checkdate() {
        return isWebsiteStatusValid() && isWebsiteNameValid() && isWebSiteListValid();
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

	@JsonIgnore
	// 檢查 websiteStatus 是否不為空值
    public boolean isWebsiteStatusValid() {
        return websiteStatus != null && !websiteStatus.trim().isEmpty();
    }

	@JsonIgnore
    // 檢查 websiteName 是否符合限制條件：不為空值、最大長度為 50
    public boolean isWebsiteNameValid() {
        return websiteName != null && !websiteName.trim().isEmpty() && websiteName.length() <= 50;
    }

	@JsonIgnore
    // 檢查 webSiteList 是否不為空值，並且其中的元素都符合限制條件
    public boolean isWebSiteListValid() {
		if (webSiteList == null || webSiteList.isEmpty()) {
			return false;
		}
		for (DPB0155ItemReq item : webSiteList) {
			if (!item.isProbabilityValid()) {
				throw TsmpDpAaRtnCode._1527.throwing(item.getProbability() + "");
			}
			if (!item.isUrlValid()) {
				throw TsmpDpAaRtnCode._1405.throwing();
			}
		}
		return true;
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
