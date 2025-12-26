package tpi.dgrv4.dpaa.vo;

import java.io.Serializable;
import java.util.List;

public class DPB0158Resp implements Serializable{

	// 狀態
    private String websiteStatus;

    // 名稱
    private String websiteName;

    // 轉導網站清單
    private List<DPB0158ItemReq> webSiteList;

    // 備註
    private String remark;
    
    private String auth;
	private String sqlInjection;
	private String traffic;
	private String xss;
	private String xxe;
	private Integer tps;
	private String ignoreApi;
	private String showLog;
	
    // Getters and Setters
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

    public List<DPB0158ItemReq> getWebSiteList() {
        return webSiteList;
    }

    public void setWebSiteList(List<DPB0158ItemReq> webSiteList) {
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
	
	
}
