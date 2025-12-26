package tpi.dgrv4.gateway.vo;

public class TpsVo {
	private long timestampSec;
	private int number = 1;
	private int limit;
	
	
	public long getTimestampSec() {
		return timestampSec;
	}
	public void setTimestampSec(long timestampSec) {
		this.timestampSec = timestampSec;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	

}
