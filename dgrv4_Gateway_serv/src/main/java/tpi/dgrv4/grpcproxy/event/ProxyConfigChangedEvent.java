package tpi.dgrv4.grpcproxy.event;

/**
 * Event class representing a proxy configuration change.
 * This event is published when proxy mappings are added, updated, or removed.
 */
public class ProxyConfigChangedEvent {

    // Event type enumeration
    public enum ChangeType {
        ADDED, UPDATED, REMOVED, REFRESHED
    }

    private final ChangeType changeType;
    private final String proxyHostname;

    /**
     * Creates a proxy configuration change event.
     *
     * @param changeType The type of change
     * @param proxyHostname The changed proxy hostname
     */
    public ProxyConfigChangedEvent(ChangeType changeType, String proxyHostname) {
        this.changeType = changeType;
        this.proxyHostname = proxyHostname;
    }

    /**
     * Creates a refresh all configurations event.
     *
     * @return Refresh event
     */
    public static ProxyConfigChangedEvent refreshEvent() {
        return new ProxyConfigChangedEvent(ChangeType.REFRESHED, null);
    }

    /**
     * Gets the change type.
     *
     * @return The change type
     */
    public ChangeType getChangeType() {
        return changeType;
    }

    /**
     * Gets the changed proxy hostname.
     * For REFRESHED type events, may return null.
     *
     * @return The proxy hostname
     */
    public String getProxyHostname() {
        return proxyHostname;
    }

    @Override
    public String toString() {
        return "ProxyConfigChangedEvent{" +
                "changeType=" + changeType +
                ", proxyHostname='" + proxyHostname + '\'' +
                '}';
    }
}