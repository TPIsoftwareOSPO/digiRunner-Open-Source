package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0121Resp {

	private List<String> refreshMemListUrls;

	private List<String> redoUrls;

	public List<String> getRefreshMemListUrls() {
		return refreshMemListUrls;
	}

	public void setRefreshMemListUrls(List<String> refreshMemListUrls) {
		this.refreshMemListUrls = refreshMemListUrls;
	}

	public List<String> getRedoUrls() {
		return redoUrls;
	}

	public void setRedoUrls(List<String> redoUrls) {
		this.redoUrls = redoUrls;
	}

}