package tpi.dgrv4.dpaa.component;

import lombok.Builder;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("java:S5998")
@Builder
public record DgrProtocol(
        Boolean valid,
        String scheme,
        String host,
        String path,
        String origin,
        Integer port,
        Set<String> extensions,
        boolean mtls
) {

	private static final String rex = "^dgr(\\+[-a-zA-Z]+)+##(.+)";
    private static final Pattern pattern = Pattern.compile(rex);

    public static DgrProtocol parse(String url) {
        var validation = validation(url);
        var builder = DgrProtocol.builder()
                .valid(validation.valid)
                .origin(validation.origin)
                .mtls(false);
        if (validation.valid) {
            var path = validation.origin;
            var extensions = extensions(url);
            if (extensions.contains("mtls")) {
                var uri = URI.create(validation.origin);
                var port = uri.getPort() == -1 ? 443 : uri.getPort();
                path = uri.getPath();
                builder.host(uri.getHost())
                        .port(port)
                        .mtls(true);
            }
            builder
                    .path(path)
                    .extensions(extensions(url))
                    .scheme(scheme(url))
                    .build();
        }
        return builder.build();
    }

    @Builder
    private record Validation(boolean valid, String origin, Set<String> extensions) {}

    private static Validation validation(String url) {
        Matcher matcher = pattern.matcher(url);

        var builder = Validation.builder()
                .valid(matcher.matches());

        if (matcher.matches()) {
            builder.origin(matcher.group(2))
                    .extensions(extensions(url));
        }

        return builder.build();
    }

    private static Set<String> extensions(String url) {
        var protocolEnd = url.indexOf("##");
        var extensions = url.substring(3, protocolEnd);
        return Arrays.stream(extensions.split("\\+")).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }

    private static String scheme(String url) {
        var protocolEnd = url.indexOf("##");
        return url.substring(0, protocolEnd);
    }
}
