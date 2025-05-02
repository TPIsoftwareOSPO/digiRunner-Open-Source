package tpi.dgrv4.gateway.vo;

public class OCInMetadata {
	
	private String id;
	private String name;
	private String ver;
	private String livenessDetectionAPI;
	private String keeperServerApi;
	
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
	public String getVer() {
		return ver;
	}
	public void setVer(String ver) {
		this.ver = ver;
	}
	public String getLivenessDetectionAPI() {
		return livenessDetectionAPI;
	}
	public void setLivenessDetectionAPI(String livenessDetectionAPI) {
		this.livenessDetectionAPI = livenessDetectionAPI;
	}
	public String getKeeperServerApi() {
		return keeperServerApi;
	}
	public void setKeeperServerApi(String keeperServerApi) {
		this.keeperServerApi = keeperServerApi;
	}
}
