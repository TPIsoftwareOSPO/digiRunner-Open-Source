package tpi.dgrv4.dpaa.vo;

public class AA0320Item {
	
	/** 群組編號	*/
	private String gId;
	
	/** 群組代碼	*/
	private String name;
	
	/** 群組名稱	*/
	private String alias;
	
	/** 群組描述	*/
	private String desc;
	
	/** 是否為虛擬群組*/
	private String v;
	
	/** 虛擬群組編號	*/
	private String vId;

	public String getgId() {
		return gId;
	}

	public void setgId(String gId) {
		this.gId = gId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getV() {
		return v;
	}

	public void setV(String v) {
		this.v = v;
	}

	public String getvId() {
		return vId;
	}

	public void setvId(String vId) {
		this.vId = vId;
	}

	@Override
	public String toString() {
		return "AA0320Item [gId=" + gId + ", name=" + name + ", alias=" + alias + ", desc=" + desc + ", v=" + v
				+ ", vId=" + vId + "]";
	}
	
}
