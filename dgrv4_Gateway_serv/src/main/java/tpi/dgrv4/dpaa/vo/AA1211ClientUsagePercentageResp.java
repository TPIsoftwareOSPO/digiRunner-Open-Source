package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA1211ClientUsagePercentageResp {
	private String total;
	private String client;
	private String percentage;
	private String request;
	private List<AA1211ClientUsagePercentageRespItem> apiUsage;

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getPercentage() {
		return percentage;
	}

	public void setPercentage(String percentage) {
		this.percentage = percentage;
	}

	public List<AA1211ClientUsagePercentageRespItem> getApiUsage() {
		return apiUsage;
	}

	public void setApiUsage(List<AA1211ClientUsagePercentageRespItem> apiUsage) {
		this.apiUsage = apiUsage;
	}

	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

}
