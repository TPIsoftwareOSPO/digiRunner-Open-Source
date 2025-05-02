package tpi.dgrv4.dpaa.vo;

public class DPB0201RespItem{

	// 狀態
    private String targetUrl;

    // 名稱
    private Integer req = 0;

    // 轉導網站清單
    private Integer resp = 0;

	public String getTargetUrl() {
		return targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	public Integer getReq() {
		return req;
	}

	public void setReq(Integer req) {
		this.req = req;
	}

	public Integer getResp() {
		return resp;
	}

	public void setResp(Integer resp) {
		this.resp = resp;
	}

    
	
}
