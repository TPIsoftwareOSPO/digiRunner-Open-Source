package tpi.dgrv4.dpaa.vo;

import java.util.List;
import java.util.Set;

public class AA0229VgroupInfo {
	/**安全等級*/
	private String securityLevelName;
	
	/**群組名稱*/
	private String vgroupAlias;
	
	/**群組描述*/
	private String vgroupDesc;
	
	/**群組流水號*/
	private String vgroupID;
	
	/**群組代號*/
	private String vgroupName;
	
	private List<AA0229ModuleAPIKey> moduleAPIKeyList;

	public String getSecurityLevelName() {
		return securityLevelName;
	}

	public void setSecurityLevelName(String securityLevelName) {
		this.securityLevelName = securityLevelName;
	}

	public String getVgroupAlias() {
		return vgroupAlias;
	}

	public void setVgroupAlias(String vgroupAlias) {
		this.vgroupAlias = vgroupAlias;
	}

	public String getVgroupDesc() {
		return vgroupDesc;
	}

	public void setVgroupDesc(String vgroupDesc) {
		this.vgroupDesc = vgroupDesc;
	}

	public String getVgroupID() {
		return vgroupID;
	}

	public void setVgroupID(String vgroupID) {
		this.vgroupID = vgroupID;
	}

	public String getVgroupName() {
		return vgroupName;
	}

	public void setVgroupName(String vgroupName) {
		this.vgroupName = vgroupName;
	}

	public List<AA0229ModuleAPIKey> getModuleAPIKeyList() {
		return moduleAPIKeyList;
	}

	public void setModuleAPIKeyList(List<AA0229ModuleAPIKey> moduleAPIKeyList) {
		this.moduleAPIKeyList = moduleAPIKeyList;
	}

	
	
}
