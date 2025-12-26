package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class AA1211RespItem {
	private Integer timeType;
	private String dataTime;
	private String request;
	private AA1211SuccessResp success;
	private AA1211FailResp fail;
	private AA1211BadAttemptResp badAttempt;
	private Integer avg;
	private AA1211MedianResp median;
	private List<AA1211PopularResp> popular;
	private List<AA1211UnpopularResp> unpopular;
	private List<AA1211ApiTrafficDistributionResp> apiTrafficDistribution;
	private List<AA1211ClientUsagePercentageResp> clientUsagePercentage;
	private List<AA1211LastLoginLog> lastLoginLog;

	public Integer getTimeType() {
		return timeType;
	}

	public void setTimeType(Integer timeType) {
		this.timeType = timeType;
	}

	public String getDataTime() {
		return dataTime;
	}

	public void setDataTime(String dataTime) {
		this.dataTime = dataTime;
	}



	public String getRequest() {
		return request;
	}

	public void setRequest(String request) {
		this.request = request;
	}

	public AA1211SuccessResp getSuccess() {
		return success;
	}

	public void setSuccess(AA1211SuccessResp success) {
		this.success = success;
	}

	public AA1211FailResp getFail() {
		return fail;
	}

	public void setFail(AA1211FailResp fail) {
		this.fail = fail;
	}

	public AA1211BadAttemptResp getBadAttempt() {
		return badAttempt;
	}

	public void setBadAttempt(AA1211BadAttemptResp badAttempt) {
		this.badAttempt = badAttempt;
	}

	public Integer getAvg() {
		return avg;
	}

	public void setAvg(Integer avg) {
		this.avg = avg;
	}

	public AA1211MedianResp getMedian() {
		return median;
	}

	public void setMedian(AA1211MedianResp median) {
		this.median = median;
	}

	public List<AA1211PopularResp> getPopular() {
		return popular;
	}

	public void setPopular(List<AA1211PopularResp> popular) {
		this.popular = popular;
	}

	public List<AA1211UnpopularResp> getUnpopular() {
		return unpopular;
	}

	public void setUnpopular(List<AA1211UnpopularResp> unpopular) {
		this.unpopular = unpopular;
	}

	public List<AA1211ApiTrafficDistributionResp> getApiTrafficDistribution() {
		return apiTrafficDistribution;
	}

	public void setApiTrafficDistribution(List<AA1211ApiTrafficDistributionResp> apiTrafficDistribution) {
		this.apiTrafficDistribution = apiTrafficDistribution;
	}

	public List<AA1211ClientUsagePercentageResp> getClientUsagePercentage() {
		return clientUsagePercentage;
	}

	public void setClientUsagePercentage(List<AA1211ClientUsagePercentageResp> clientUsagePercentage) {
		this.clientUsagePercentage = clientUsagePercentage;
	}

	public List<AA1211LastLoginLog> getLastLoginLog() {
		return lastLoginLog;
	}

	public void setLastLoginLog(List<AA1211LastLoginLog> lastLoginLog) {
		this.lastLoginLog = lastLoginLog;
	}
}
