package tpi.dgrv4.gateway.filter;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.util.Set;

public class FilteredHeaderResponseWrapper extends HttpServletResponseWrapper {
    public FilteredHeaderResponseWrapper(HttpServletResponse response) {
        super(response);
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
        if (ignoreHeaders.contains(name.toLowerCase())) return;
        if (name.startsWith(":")) return;

        super.setHeader(name, value);
    }

    public FilteredHeaderResponseWrapper injectHeader(String name, String value) {
        super.setHeader(name, value);
        return this;
    }

}
