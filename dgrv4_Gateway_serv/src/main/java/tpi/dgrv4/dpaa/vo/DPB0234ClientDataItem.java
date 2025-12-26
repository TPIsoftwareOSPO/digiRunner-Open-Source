package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0234ClientDataItem {
    private String apiKeyId;
    private String apiKeyMask;
    private String apiKeyAlias;
    private String clientId;
    private String clientName;
    private List<DPB0234ApiDataItem> apiDataList;

    public String getApiKeyId() {
        return apiKeyId;
    }

    public void setApiKeyId(String apiKeyId) {
        this.apiKeyId = apiKeyId;
    }

    public String getApiKeyMask() {
        return apiKeyMask;
    }

    public void setApiKeyMask(String apiKeyMask) {
        this.apiKeyMask = apiKeyMask;
    }

    public String getApiKeyAlias() {
        return apiKeyAlias;
    }

    public void setApiKeyAlias(String apiKeyAlias) {
        this.apiKeyAlias = apiKeyAlias;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public List<DPB0234ApiDataItem> getApiDataList() {
        return apiDataList;
    }

    public void setApiDataList(List<DPB0234ApiDataItem> apiDataList) {
        this.apiDataList = apiDataList;
    }

    @Override
    public String toString() {
        return "DPB0234ClientDataItem{" +
                "apiKeyId='" + apiKeyId + '\'' +
                ", apiKeyMask='" + apiKeyMask + '\'' +
                ", apiKeyAlias='" + apiKeyAlias + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientName='" + clientName + '\'' +
                ", apiDataList=" + apiDataList +
                '}';
    }
}
