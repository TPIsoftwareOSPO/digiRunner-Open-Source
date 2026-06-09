package tpi.dgrv4.gateway.config;

import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * DataSource Proxy 配置（無條件載入）
 * DataSource Proxy configuration (always loaded)
 */
@Configuration
public class DataSourceProxyConfig {

    /**
     * 創建 HotSwappableTargetSource / Create HotSwappableTargetSource
     */
    @Bean
    public HotSwappableTargetSource hotSwappableTargetSource(
            @Autowired(required = false) @Qualifier("rawApiDataSource") DataSource rawApiDataSource,
            @Autowired(required = false) @Qualifier("rawEmbeddedDataSource") DataSource rawEmbeddedDataSource) {

        DataSource initialDataSource;

        if (rawApiDataSource != null) {
            // API 模式：使用 CustomDataSourceConfig 創建的 DataSource
            initialDataSource = rawApiDataSource;
        } else if (rawEmbeddedDataSource != null) {
            // Embedded 模式：使用 DataSourceConfig 創建的 DataSource
            initialDataSource = rawEmbeddedDataSource;
        } else {
            throw new IllegalStateException(
                    "No DataSource available. Please check db.connection.mode configuration.");
        }

        return new HotSwappableTargetSource(initialDataSource);
    }

    /**
     * 創建 Proxy DataSource / Create Proxy DataSource
     */
    @Bean
    @Primary
    public DataSource dataSource(HotSwappableTargetSource targetSource) {
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTargetSource(targetSource);
        proxyFactoryBean.setProxyTargetClass(true);
        return (DataSource) proxyFactoryBean.getObject();
    }
}
