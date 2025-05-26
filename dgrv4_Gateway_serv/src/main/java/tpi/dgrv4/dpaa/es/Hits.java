package tpi.dgrv4.dpaa.es;

import java.util.List;

public class Hits {
	private Total total;
	private float max_score;
	private List<ResponseHit> hits;

	public Total getTotal() {
		return total;
	}

	public void setTotal(Total total) {
		this.total = total;
	}

	public float getMax_score() {
		return max_score;
	}

	public void setMax_score(float max_score) {
		this.max_score = max_score;
	}

	public List<ResponseHit> getHits() {
		return hits;
	}

	public void setHits(List<ResponseHit> hits) {
		this.hits = hits;
	}
}
