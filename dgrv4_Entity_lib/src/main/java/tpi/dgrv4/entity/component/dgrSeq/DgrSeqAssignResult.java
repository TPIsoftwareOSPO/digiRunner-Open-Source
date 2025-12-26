package tpi.dgrv4.entity.component.dgrSeq;

public class DgrSeqAssignResult<T> {

	// 在取號前, 是否就已經有填入 primary key 了
	private boolean hasDgrSeqAlready = Boolean.FALSE;

	// 預先填入的序號, 或是新取得的序號
	private Long dgrSeq;

	// entity
	private T entity;

	public DgrSeqAssignResult(T entity) {
		setEntity(entity);
	}

	public boolean hasDgrSeqAlready() {
		return hasDgrSeqAlready;
	}

	public void setHasDgrSeqAlready(boolean hasDgrSeqAlready) {
		this.hasDgrSeqAlready = hasDgrSeqAlready;
	}

	public Long getDgrSeq() {
		return dgrSeq;
	}

	public void setDgrSeq(Long dgrSeq) {
		this.dgrSeq = dgrSeq;
	}

	public T getEntity() {
		return entity;
	}

	public void setEntity(T entity) {
		this.entity = entity;
	}

}
