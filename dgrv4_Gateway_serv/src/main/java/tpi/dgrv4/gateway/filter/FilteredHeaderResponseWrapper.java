package tpi.dgrv4.gateway.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.util.Set;

public class FilteredHeaderResponseWrapper extends HttpServletResponseWrapper {

    private final HttpServletRequest request;
    private final boolean isKibanaRequest; // Flag to indicate if the request is for Kibana

    private static final Set<String> HTTP2_FORBIDDEN_HEADERS = Set.of(
            "connection", "keep-alive", "proxy-connection", "transfer-encoding", "upgrade"
    );

    /**
     * Constructs a response adaptor wrapping the given response.
     * @param response The response to be wrapped
     * @param request The original request
     * @param isKibanaRequest True if the request is identified as a Kibana request
     */
    public FilteredHeaderResponseWrapper(HttpServletResponse response, HttpServletRequest request, boolean isKibanaRequest) {
        super(response);
        this.request = request;
        this.isKibanaRequest = isKibanaRequest;
    }

    private boolean isHttp2() {
        String protocol = request.getProtocol();
        return protocol != null && protocol.startsWith("HTTP/2");
    }
    private boolean isForbidden(String name) {
        return name != null && HTTP2_FORBIDDEN_HEADERS.contains(name.toLowerCase());
    }

    private static final Set<String> ignoreHeaders = Set.of(
            "access-control-allow-origin",
            "access-control-allow-headers",
            "access-control-allow-methods",
            "content-security-policy",
            "x-frame-options",
            "x-content-type-options",
            "strict-transport-security",
            "referrer-policy",
            "cache-control",
            "pragma"
    );

    @Override
    public void setHeader(String name, String value) {
        if (name == null) {
            return;
        }
        // Only apply the ignoreHeaders filter if it's NOT a Kibana request.
        if (!this.isKibanaRequest && ignoreHeaders.contains(name.toLowerCase())) {
            return;
        }

        // Universal rules for all requests
        if (isHttp2() && isForbidden(name))  return;
        if (name.startsWith(":")) return;

        super.setHeader(name, value);
    }
    @Override
    public void addHeader(String name, String value) {
        if (name == null) {
            return;
        }
        // Only apply the ignoreHeaders filter if it's NOT a Kibana request.
        if (!this.isKibanaRequest && ignoreHeaders.contains(name.toLowerCase())) {
            return;
        }
        
        // Universal rules for all requests
        if (isHttp2() && isForbidden(name))  return;
        if (name.startsWith(":")) return;
        super.addHeader(name, value);
    }
    public FilteredHeaderResponseWrapper injectHeader(String name, String value) {
        super.setHeader(name, value);
        return this;
    }

}
