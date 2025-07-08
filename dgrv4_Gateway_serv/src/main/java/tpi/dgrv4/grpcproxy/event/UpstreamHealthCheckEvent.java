package tpi.dgrv4.grpcproxy.event;

/**
 * Event class for upstream service health check results
 */
public class UpstreamHealthCheckEvent {
    private final String proxyHostname;
    private final String targetHostname;
    private final int targetPort;
    private final boolean healthy;
    private final String message;

    public UpstreamHealthCheckEvent(String proxyHostname, String targetHostname, int targetPort,
                                    boolean healthy, String message) {
        this.proxyHostname = proxyHostname;
        this.targetHostname = targetHostname;
        this.targetPort = targetPort;
        this.healthy = healthy;
        this.message = message;
    }

    public String getProxyHostname() {
        return proxyHostname;
    }

    public String getTargetHostname() {
        return targetHostname;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "UpstreamHealthCheckEvent{" +
                "proxyHostname='" + proxyHostname + '\'' +
                ", targetHostname='" + targetHostname + '\'' +
                ", targetPort=" + targetPort +
                ", healthy=" + healthy +
                ", message='" + message + '\'' +
                '}';
    }
}