package tpi.dgrv4.gateway.filter;

import java.util.Arrays;

import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpServletResponseWrapper;

import org.springframework.web.util.ContentCachingResponseWrapper;

// extends HttpServletResponseWrapper
public class CusContentCachingResponseWrapper extends ContentCachingResponseWrapper {
	public CusContentCachingResponseWrapper(HttpServletResponse response) {
		super(response);
	}
 
	private String ignoreHeaders[] = { 
			"Access-Control-Allow-Origin", 
			"Access-Control-Allow-Headers",
			"Access-Control-Allow-Methods",		
			
			"Content-Security-Policy",
			"X-Frame-Options",
			"X-Content-Type-Options",
			"Strict-Transport-Security",
			"Referrer-Policy",
			"Cache-Control",
			"Pragma"
	};

	@Override
	public void setHeader(String name, String value) {
		// 遇到特殊 Header , 不讓它設定
		boolean hasIgnore = Arrays.asList(ignoreHeaders).contains(name);
		if (! hasIgnore) {
			super.setHeader(name, value);
		}
	}
	
	public void setHeaderByForce(String name, String value) {
		super.setHeader(name, value);
	}
}
