package tpi.dgrv4.gateway.util;

import jakarta.servlet.http.HttpServletRequest;

public class ClientIpUtil {
	public static String getClientIp(HttpServletRequest request) {
	    String xff = request.getHeader("X-Forwarded-For");
	    
	    if (xff == null || xff.isEmpty() || "unknown".equalsIgnoreCase(xff)) {
	        return request.getRemoteAddr();
	    }
	    
	    // 處理多重代理，只取第一個非 unknown 的 IP
	    String[] ips = xff.split(",");
	    String clientIp = ips[0].trim();
	    
	    return clientIp;
	}
}
