package tpi.dgrv4.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;

import jakarta.annotation.PostConstruct;
import tpi.dgrv4.gateway.util.CusAcLoginStateStore;
import tpi.dgrv4.gateway.util.CusGatewayLoginStateStore;

@Configuration
public class CusAcLoginStateConfig {

    @Value("${cus.login.state.store.expiration.time:1m}")
    private String expirationTime;
    
    @PostConstruct
    public void initLoginStateStore() {
        // 使用@Value注入的值來初始化枚舉單例
        CusAcLoginStateStore.INSTANCE.setExpirationFromConfig(expirationTime);
        CusGatewayLoginStateStore.INSTANCE.setExpirationFromConfig(expirationTime);
    }
}