package tpi.dgrv4.gateway.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Configuration
public class DisallowedMethodsHandlerFactory {

    @Value("${undertow.disallow-methods:true}")
    @Getter
    private boolean disallowMethods;

    @Bean
    public FilterRegistrationBean<DisallowedMethodsFilter> disallowedMethodsFilter() {
        FilterRegistrationBean<DisallowedMethodsFilter> registrationBean = new FilterRegistrationBean<>();
        
        if (isDisallowMethods()) {
            registrationBean.setFilter(new DisallowedMethodsFilter());
            registrationBean.addUrlPatterns("/*");
            registrationBean.setOrder(1); // 設定過濾器優先順序
        }
        
        return registrationBean;
    }

    public static class DisallowedMethodsFilter implements Filter {
        
        private static final List<String> DISALLOWED_METHODS = Arrays.asList("TRACE", "TRACK");

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            String method = httpRequest.getMethod();
            
            if (DISALLOWED_METHODS.contains(method.toUpperCase())) {
                httpResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                httpResponse.getWriter().write("Method Not Allowed");
                return;
            }
            
            chain.doFilter(request, response);
        }

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        @Override
        public void destroy() {
        }
    }
}
