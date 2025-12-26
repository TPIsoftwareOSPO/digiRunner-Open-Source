package tpi.dgrv4.dpaa.vo;

import java.util.Map;

public class DPB0144RespSummary {

	private DPB0144RespSummaryItem users;
	private DPB0144RespSummaryItem clients;
	private long roles;
	private long groups;
	private DPB0144RespSummaryItemApis registerApis;
	private DPB0144RespSummaryItemApis composerApis;
	
	public DPB0144RespSummaryItem getUsers() {
		return users;
	}
	public void setUsers(DPB0144RespSummaryItem users) {
		this.users = users;
	}
	public DPB0144RespSummaryItem getClients() {
		return clients;
	}
	public void setClients(DPB0144RespSummaryItem clients) {
		this.clients = clients;
	}
	public long getRoles() {
		return roles;
	}
	public void setRoles(long roles) {
		this.roles = roles;
	}
	public long getGroups() {
		return groups;
	}
	public void setGroups(long groups) {
		this.groups = groups;
	}
	public DPB0144RespSummaryItemApis getRegisterApis() {
		return registerApis;
	}
	public void setRegisterApis(DPB0144RespSummaryItemApis registerApis) {
		this.registerApis = registerApis;
	}
	public DPB0144RespSummaryItemApis getComposerApis() {
		return composerApis;
	}
	public void setComposerApis(DPB0144RespSummaryItemApis composerApis) {
		this.composerApis = composerApis;
	}
}
