package tpi.dgrv4.dpaa.service.tools;

import org.springframework.stereotype.Component;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class DgrProtocol {

    private final String rex = "^dgr(\\+[-a-zA-Z]*)*://.*";
    private final Pattern pattern = Pattern.compile(rex);

    public boolean isValidScheme(String url) {
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

    public Set<String> getExtensions(String url) {
        if (isValidScheme(url)) {
            var protocolEnd = url.indexOf("://");
            var extensions = url.substring(3, protocolEnd);
            return Arrays.stream(extensions.split("\\+")).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
        }
        throw TsmpDpAaRtnCode._1559.throwing("not dgr protocol");
    }
}
