package tpi.dgrv4.dpaa.vo;

public class DPB0234ApiDataItem {
    private String apiStatus;
    private String apiName;
    private String apiPath;
    private String moduleName;

    public String getApiStatus() {
        return apiStatus;
    }

    public void setApiStatus(String apiStatus) {
        this.apiStatus = apiStatus;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiPath() {
        return apiPath;
    }

    public void setApiPath(String apiPath) {
        this.apiPath = apiPath;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public String toString() {
        return "DPB0234ApiDataItem{" +
                "apiStatus='" + apiStatus + '\'' +
                ", apiName='" + apiName + '\'' +
                ", apiPath='" + apiPath + '\'' +
                ", moduleName='" + moduleName + '\'' +
                '}';
    }
}
