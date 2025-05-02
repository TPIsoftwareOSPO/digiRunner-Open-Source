package tpi.dgrv4.dpaa.vo;

public class DashboardMedianAideVo {
	private long totalElapse;
	private long maxElapse;
	private long minElapse;
	private long frequency;
	
	public long getTotalElapse() {
		return totalElapse;
	}
	public void setTotalElapse(long totalElapse) {
		this.totalElapse = totalElapse;
	}
	public long getMaxElapse() {
		return maxElapse;
	}
	public void setMaxElapse(long maxElapse) {
		this.maxElapse = maxElapse;
	}
	public long getMinElapse() {
		return minElapse;
	}
	public void setMinElapse(long minElapse) {
		this.minElapse = minElapse;
	}
	public long getFrequency() {
		return frequency;
	}
	public void setFrequency(long frequency) {
		this.frequency = frequency;
	}
	@Override
	public String toString() {
		return "DashboardMedianAideVo [totalElapse=" + totalElapse + ", maxElapse=" + maxElapse + ", minElapse="
				+ minElapse + ", frequency=" + frequency + "]";
	}
	
	
	
}
