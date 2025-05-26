package tpi.dgrv4.gateway.component;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class SessionTimeoutHelper {

    public int getCurrentSessionTimeout() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpSession session = ((ServletRequestAttributes) requestAttributes).getRequest().getSession(false);
            if (session != null) {
                return session.getMaxInactiveInterval();
            }
        }
        return 1800; // 預設值,單位為秒
    }

}
