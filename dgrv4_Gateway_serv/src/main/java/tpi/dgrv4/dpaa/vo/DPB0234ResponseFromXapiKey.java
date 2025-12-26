package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0234ResponseFromXapiKey {
    private String apiKeyId;
    private String apiKeyMask;
    private String apiKeyAlias;
    private String effectiveAt;
    private String expiredAt;
    private String clientId;
    private String clientName;
    private String totalApi;
    private List<DPB0234GroupInfo> groupList;

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

    public String getEffectiveAt() {
        return effectiveAt;
    }

    public void setEffectiveAt(String effectiveAt) {
        this.effectiveAt = effectiveAt;
    }

    public String getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(String expiredAt) {
        this.expiredAt = expiredAt;
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

    public String getTotalApi() {
        return totalApi;
    }

    public void setTotalApi(String totalApi) {
        this.totalApi = totalApi;
    }

    public List<DPB0234GroupInfo> getGroupList() {
        return groupList;
    }

    public void setGroupList(List<DPB0234GroupInfo> groupList) {
        this.groupList = groupList;
    }
}
