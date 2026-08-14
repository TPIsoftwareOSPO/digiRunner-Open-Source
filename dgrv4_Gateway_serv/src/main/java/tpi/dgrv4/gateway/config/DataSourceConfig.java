package tpi.dgrv4.gateway.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Embedded 模式的 DataSource 配置
 * DataSource configuration for embedded mode
 */
@Configuration
@ConditionalOnProperty(name = "db.connection.mode", havingValue = "embedded", matchIfMissing = true)
public class DataSourceConfig {

    @Value("${spring.datasource.hikari.maximum-pool-size:10}")
    private int hikariMaximumPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:10}")
    private int hikariMinimumIdle;

    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private long hikariConnectionTimeout;

    @Value("${spring.datasource.hikari.idle-timeout:600000}")
    private long hikariIdleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private long hikariMaxLifeTime;
    
    @Value("${spring.datasource.hikari.keepalive-time:120000}")
    private long hikariKeepaliveTime;

    /**
     * 創建 raw DataSource / Create raw DataSource
     */
    @Bean(name = "rawEmbeddedDataSource")
    public DataSource rawEmbeddedDataSource(DataSourceProperties properties) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getUrl());
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        config.setDriverClassName(properties.getDriverClassName());
        config.addDataSourceProperty("shutdown", "true");
        
        config.setMaximumPoolSize(hikariMaximumPoolSize);
        config.setMinimumIdle(hikariMinimumIdle);
        config.setConnectionTimeout(hikariConnectionTimeout);
        config.setIdleTimeout(hikariIdleTimeout);
        config.setMaxLifetime(hikariMaxLifeTime);
        config.setKeepaliveTime(hikariKeepaliveTime);

        return new HikariDataSource(config);
    }
}
