package tpi.dgrv4.entity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "seq_store")
public class SeqStore {

	@Id
	@Column(name = "sequence_name")
	private String sequenceName;

	@Column(name = "next_val")
	private Long nextVal;

	/* constructors */

	public SeqStore() {}

	/* methods */

	@Override
	public String toString() {
		return "SeqStore [sequenceName=" + sequenceName + ", nextVal=" + nextVal + "]";
	}

	/* getters and setters */

	public String getSequenceName() {
		return sequenceName;
	}

	public void setSequenceName(String sequenceName) {
		this.sequenceName = sequenceName;
	}

	public Long getNextVal() {
		return nextVal;
	}

	public void setNextVal(Long nextVal) {
		this.nextVal = nextVal;
	}
	
}
