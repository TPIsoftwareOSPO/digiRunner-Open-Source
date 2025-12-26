package tpi.dgrv4.dpaa.vo;

public class DPB0144RespAuditItem {
	
	// 新增、刪除、修改
	private long create;
	private long delete;
	private long update;

	public long getCreate() {
		return create;
	}

	public void setCreate(long create) {
		this.create = create;
	}

	public long getDelete() {
		return delete;
	}

	public void setDelete(long delete) {
		this.delete = delete;
	}

	public long getUpdate() {
		return update;
	}

	public void setUpdate(long update) {
		this.update = update;
	}

}
