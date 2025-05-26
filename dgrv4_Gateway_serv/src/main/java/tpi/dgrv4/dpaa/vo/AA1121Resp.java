package tpi.dgrv4.dpaa.vo;

import java.io.Serializable;
import java.util.List;

public class AA1121Resp implements Serializable{
	private List<AA1121LackApi> lackApiList;
	private List<AA1121Client> clientList;
	private List<AA1121Group> groupList;
	private List<AA1121Vgroup> vgroupList;
	private List<AA1121GroupAuth> groupAuthList;
	private List<AA1121SecurityLevel> securityLevelList;
	private List<AA1121RdbConnection> rdbConnectionList;
	private String longId;
	
	public List<AA1121LackApi> getLackApiList() {
		return lackApiList;
	}
	public void setLackApiList(List<AA1121LackApi> lackApiList) {
		this.lackApiList = lackApiList;
	}
	public List<AA1121Client> getClientList() {
		return clientList;
	}
	public void setClientList(List<AA1121Client> clientList) {
		this.clientList = clientList;
	}
	public List<AA1121Group> getGroupList() {
		return groupList;
	}
	public void setGroupList(List<AA1121Group> groupList) {
		this.groupList = groupList;
	}
	public List<AA1121Vgroup> getVgroupList() {
		return vgroupList;
	}
	public void setVgroupList(List<AA1121Vgroup> vgroupList) {
		this.vgroupList = vgroupList;
	}
	public List<AA1121GroupAuth> getGroupAuthList() {
		return groupAuthList;
	}
	public void setGroupAuthList(List<AA1121GroupAuth> groupAuthList) {
		this.groupAuthList = groupAuthList;
	}
	public List<AA1121SecurityLevel> getSecurityLevelList() {
		return securityLevelList;
	}
	public void setSecurityLevelList(List<AA1121SecurityLevel> securityLevelList) {
		this.securityLevelList = securityLevelList;
	}
	public String getLongId() {
		return longId;
	}
	public void setLongId(String longId) {
		this.longId = longId;
	}
	public List<AA1121RdbConnection> getRdbConnectionList() {
		return rdbConnectionList;
	}
	public void setRdbConnectionList(List<AA1121RdbConnection> rdbConnectionList) {
		this.rdbConnectionList = rdbConnectionList;
	}
	
	
}
