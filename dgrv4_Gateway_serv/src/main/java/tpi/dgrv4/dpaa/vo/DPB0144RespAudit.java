package tpi.dgrv4.dpaa.vo;

public class DPB0144RespAudit {
	//使用者
	private DPB0144RespAuditItem user;
	//角色
	private DPB0144RespAuditItem role;
	//用戶端
	private DPB0144RespAuditItem client;
	//群組
	private DPB0144RespAuditItem group;
	//註冊 API
	private DPB0144RespAuditItem registerApi;
	//組合 API
	private DPB0144RespAuditItem composerApi;
	//登入
	private DPB0144RespAuditItemLogin login;
	
	public DPB0144RespAuditItem getUser() {
		return user;
	}
	public void setUser(DPB0144RespAuditItem user) {
		this.user = user;
	}
	public DPB0144RespAuditItem getRole() {
		return role;
	}
	public void setRole(DPB0144RespAuditItem role) {
		this.role = role;
	}
	public DPB0144RespAuditItem getClient() {
		return client;
	}
	public void setClient(DPB0144RespAuditItem client) {
		this.client = client;
	}
	public DPB0144RespAuditItem getGroup() {
		return group;
	}
	public void setGroup(DPB0144RespAuditItem group) {
		this.group = group;
	}
	public DPB0144RespAuditItem getRegisterApi() {
		return registerApi;
	}
	public void setRegisterApi(DPB0144RespAuditItem registerApi) {
		this.registerApi = registerApi;
	}
	public DPB0144RespAuditItem getComposerApi() {
		return composerApi;
	}
	public void setComposerApi(DPB0144RespAuditItem composerApi) {
		this.composerApi = composerApi;
	}
	public DPB0144RespAuditItemLogin getLogin() {
		return login;
	}
	public void setLogin(DPB0144RespAuditItemLogin login) {
		this.login = login;
	}
}
