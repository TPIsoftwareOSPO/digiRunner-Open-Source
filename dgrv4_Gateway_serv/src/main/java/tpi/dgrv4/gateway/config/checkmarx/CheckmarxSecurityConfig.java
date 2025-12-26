package tpi.dgrv4.gateway.config.checkmarx;

public class CheckmarxSecurityConfig {
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.headers().httpStrictTransportSecurity();
        return new SecurityFilterChain();
    }
}
