package tpi.dgrv4.dpaa.config;

import java.io.IOException;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Configuration
public class AllowCorsFilterConfig implements Filter {

	/* [static] field */

	/* [static] */

	/* [static] method */

	/* [instance] field */

	private TPILogger logger = TPILogger.tl;

	/* [instance] constructor */

	/* [instance] method */

	@Bean
	public FilterRegistrationBean<Filter> allowCorsFilterRegistrationBean() {
		FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
		// 目前先 幫 dpaa 的 API 做 CORS 處理
		registrationBean.addUrlPatterns("/11/*", "/17/*");
		registrationBean.setFilter(this);
		/*
		 * 20210118 registrationBean.setOrder(Integer.MIN_VALUE + 1); //
		 * 排在apimEarliestFilter之後
		 */
		registrationBean.setOrder(Integer.MIN_VALUE); // 跟 ApimEarliesrFilter 相同順序

		return registrationBean;
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;

		allowCORS(httpServletRequest, httpServletResponse);

		String httpMethod = httpServletRequest.getMethod();
		this.logger.trace(String.format("- %s -", httpMethod));
		if (HttpMethod.OPTIONS.matches(httpMethod.toUpperCase())) {
			httpServletResponse.setStatus(HttpServletResponse.SC_OK);
		} else {
			chain.doFilter(request, response);
		}
	}

	public void allowCORS(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

		// 取得 CORS 的值, 從 GatewayFilter.java 放入
		String corsAllowOriginVal = (String) httpServletRequest.getServletContext().getAttribute("Access-Control-Allow-Origin");
		String corsAllowHeadersVal = (String) httpServletRequest.getServletContext().getAttribute("Access-Control-Allow-Headers");
		String corsAllowMethodsVal = (String) httpServletRequest.getServletContext().getAttribute("Access-Control-Allow-Methods");
		
		// 取得 "Content-Security-Policy" 的值, 從 GatewayFilter.java 放入
		String dgrCspVal = (String) httpServletRequest.getServletContext().getAttribute("Content-Security-Policy");

		// 當值為空時，使用預設值 "*"
		if (!StringUtils.hasText(corsAllowOriginVal)) {
			corsAllowOriginVal = "*";
		}
		
		// 當值為空時，使用預設值 "*"
		if (StringUtils.hasText(dgrCspVal) && !"*".equals(dgrCspVal.trim())) {
			httpServletResponse.setHeader("Content-Security-Policy", dgrCspVal);
		}
//		String cspVal = String.format(GatewayFilter.cspDefaultVal, dgrCspVal);

		httpServletResponse.setHeader("Access-Control-Allow-Origin", corsAllowOriginVal);
		httpServletResponse.setHeader("Access-Control-Allow-Methods", corsAllowMethodsVal);
		httpServletResponse.setHeader("Access-Control-Max-Age", "3600");
		//httpServletResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, SignCode, Language");
		httpServletResponse.setHeader("Access-Control-Allow-Headers", corsAllowHeadersVal); //"1. corsAllowHeaders = " + corsAllowHeaders

		httpServletResponse.setHeader("X-Frame-Options", "sameorigin");
		httpServletResponse.setHeader("X-Content-Type-Options", "nosniff");
		httpServletResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
		httpServletResponse.setHeader("Cache-Control", "no-cache");
		httpServletResponse.setHeader("Pragma", "no-cache");
		
		//checkmarx, Missing HSTS Header,從preload; includeSubDomains改為includeSubDomains; preload, 已通過中風險
		httpServletResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
	}

	@Override
	public void destroy() {
	}

	/* [instance] getter/setter */

}
