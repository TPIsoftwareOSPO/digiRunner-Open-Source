package tpi.dgrv4.gateway.constant;

public enum H2ConfigSyncEnum {

    PRIMARY("Primary"),
    REPLICA("Replica");

    private final String displayName;

    H2ConfigSyncEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
