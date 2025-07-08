package tpi.dgrv4.grpcproxy.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Configuration class for the gRPC proxy application.
 * This class is responsible for enabling the configuration properties
 * defined in {@link GrpcProxyProperties}.
 */
@Configuration
@EnableConfigurationProperties(GrpcProxyProperties.class)
public class GrpcProxyConfig {
}