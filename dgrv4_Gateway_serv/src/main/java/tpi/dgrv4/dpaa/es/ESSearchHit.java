package tpi.dgrv4.dpaa.es;

public class ESSearchHit <T> {
	private Integer took;
	private Boolean timed_out;
	private _shards _shards;
	private Hits hits;
	private T error;
	private int status;
	

	public Integer getTook() {
		return took;
	}

	public void setTook(Integer took) {
		this.took = took;
	}

	public Boolean getTimed_out() {
		return timed_out;
	}

	public void setTimed_out(Boolean timed_out) {
		this.timed_out = timed_out;
	}

	public _shards get_shards() {
		return _shards;
	}

	public T getError() {
		return error;
	}

	public void setError(T error) {
		this.error = error;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Hits getHits() {
		return hits;
	}

	public void setHits(Hits hits) {
		this.hits = hits;
	}

}

class _shards {
	private Integer total;
	private Integer successful;
	private Integer skipped;
	private Integer failed;
	private Object failures;

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public Integer getSuccessful() {
		return successful;
	}

	public void setSuccessful(Integer successful) {
		this.successful = successful;
	}

	public Integer getSkipped() {
		return skipped;
	}

	public void setSkipped(Integer skipped) {
		this.skipped = skipped;
	}

	public Integer getFailed() {
		return failed;
	}

	public void setFailed(Integer failed) {
		this.failed = failed;
	}

	public Object getFailures() {
		return failures;
	}

	public void setFailures(Object failures) {
		this.failures = failures;
	}
}
