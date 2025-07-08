package tpi.dgrv4.dpaa.vo;

public class AA0434DTO {
    
    private String apiKey;
    private String moduleName;

	/** 組織單位ID */
	private String orgID;
	
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getModuleName() {
        return moduleName;
    }
    
    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }
    
    public String getOrgID() {
        return orgID;
    }

    public void setOrgID(String orgID) {
        this.orgID = orgID;
    }

}
