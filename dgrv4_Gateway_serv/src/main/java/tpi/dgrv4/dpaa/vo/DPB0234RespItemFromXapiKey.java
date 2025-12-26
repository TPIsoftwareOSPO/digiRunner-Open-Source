package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0234RespItemFromXapiKey {
    private String apiKeyId;
    private String apiKeyMask;
    private String apiKeyAlias;
    private String groupId;
    private String groupAlias;
    private String groupName;
    private String clientId;
    private String clientName;
    private String effectiveAt;
    private String expiredAt;
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupAlias() {
        return groupAlias;
    }

    public void setGroupAlias(String groupAlias) {
        this.groupAlias = groupAlias;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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

    public List<DPB0234ApiDataItem> getApiDataList() {
        return apiDataList;
    }

    public void setApiDataList(List<DPB0234ApiDataItem> apiDataList) {
        this.apiDataList = apiDataList;
    }

    @Override
    public String toString() {
        return "DPB0234RespItemFromXapiKey{" +
                "apiKeyId='" + apiKeyId + '\'' +
                ", apiKeyMask='" + apiKeyMask + '\'' +
                ", apiKeyAlias='" + apiKeyAlias + '\'' +
                ", groupId='" + groupId + '\'' +
                ", groupAlias='" + groupAlias + '\'' +
                ", groupName='" + groupName + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientName='" + clientName + '\'' +
                ", effectiveAt='" + effectiveAt + '\'' +
                ", expiredAt='" + expiredAt + '\'' +
                ", apiDataList=" + apiDataList +
                '}';
    }
}
