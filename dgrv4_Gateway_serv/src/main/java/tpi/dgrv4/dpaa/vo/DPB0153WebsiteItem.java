package tpi.dgrv4.dpaa.vo;

public class DPB0153WebsiteItem {
	private String id; // 鍵
	private String name; // 名稱
	private String status; // 狀態
	private DPB0153Trunc remark; // 備註(長度超過20，就截斷)
	private String statusName; // 狀態名稱

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public DPB0153Trunc getRemark() {
		return remark;
	}

	public void setRemark(DPB0153Trunc remark) {
		this.remark = remark;
	}

	public String getStatusName() {
		return statusName;
	}

	public void setStatusName(String statusName) {
		this.statusName = statusName;
	}

	
}
