package tpi.dgrv4.gateway.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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

        return new HikariDataSource(config);
    }
}
