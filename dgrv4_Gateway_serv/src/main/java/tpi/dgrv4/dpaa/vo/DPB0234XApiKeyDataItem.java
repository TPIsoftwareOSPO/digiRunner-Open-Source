package tpi.dgrv4.dpaa.vo;

public class DPB0234XApiKeyDataItem {
    private String apiKeyId;
    private String apiKeyAlias;
    private String apiKeyMask;

    private String effectiveAt;

    private String expiredAt;
    public String getApiKeyId() {
        return apiKeyId;
    }

    public void setApiKeyId(String apiKeyId) {
        this.apiKeyId = apiKeyId;
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

    public String getApiKeyMask() {
        return apiKeyMask;
    }

    public void setApiKeyMask(String apiKeyMask) {
        this.apiKeyMask = apiKeyMask;
    }

    @Override
    public String toString() {
        return "DPB0234XApiKeyDataItem{" +
                "apiKeyId='" + apiKeyId + '\'' +
                ", apiKeyAlias='" + apiKeyAlias + '\'' +
                ", apiKeyMask='" + apiKeyMask + '\'' +
                ", effectiveAt='" + effectiveAt + '\'' +
                ", expiredAt='" + expiredAt + '\'' +
                '}';
    }
}
