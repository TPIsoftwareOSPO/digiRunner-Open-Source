package tpi.dgrv4.grpcproxy.config;

import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tpi.dgrv4.gateway.keeper.TPILogger;
import org.springframework.core.io.ClassPathResource;
import javax.net.ssl.KeyManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

@Configuration
public class GrpcTlsConfig {

    private final TPILogger logger = TPILogger.tl;

    @Value("${grpc.proxy.tls.key-store:#{null}}")
    private String keyStorePath;

    @Value("${grpc.proxy.tls.key-store-password:#{null}}")
    private String keyStorePassword;

    @Value("${grpc.proxy.tls.key-store-type:JKS}")
    private String keyStoreType;

    @Bean
    public SslContext grpcSslContext() {
        try {
            // Check if SSL properties are configured
            if (keyStorePath == null || keyStorePassword == null) {
                logger.info("SSL properties not configured, skipping gRPC SSL context creation");
                return null;
            }

            // Try multiple methods to find the keystore file
            File keyStoreFile = null;

            // Try as absolute path
            File absolute = new File(keyStorePath);
            if (absolute.exists()) {
                keyStoreFile = absolute;
                logger.debug("Found absolute path keystore: " + keyStorePath);
            }

            // Try as relative path
            if (keyStoreFile == null) {
                File relative = new File(System.getProperty("user.dir"), keyStorePath);
                if (relative.exists()) {
                    keyStoreFile = relative;
                    logger.debug("Found relative path keystore: " + keyStorePath);
                }
            }

            // If still not found, try to read from classpath
            if (keyStoreFile == null) {
                keyStoreFile = tryLoadFromClasspath(keyStorePath);
            }

            if (keyStoreFile == null) {
                logger.error("Unable to find keystore file: " + keyStorePath);
                return null;
            }

            return createSslContextFromKeyStore(keyStoreFile);

        } catch (Exception e) {
            logger.error("Unable to create gRPC TLS context: " + e.getMessage());
            return null;
        }
    }

    /**
     * Try to load keystore file from classpath
     *
     * @param keyStorePath the path to the keystore
     * @return File object if found, null otherwise
     */
    private File tryLoadFromClasspath(String keyStorePath) {
        try {
            ClassPathResource resource = new ClassPathResource(keyStorePath.replace("classpath:", ""));
            if (resource.exists()) {
                logger.debug("Found keystore from classpath: " + keyStorePath);
                return resource.getFile();
            }
        } catch (Exception e) {
            logger.debug("Unable to load keystore from classpath: " + e.getMessage());
        }
        return null;
    }

    /**
     * Create SSL context from keystore file
     */
    private SslContext createSslContextFromKeyStore(File keyStoreFile) throws Exception {
        // Load KeyStore
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        try (FileInputStream fis = new FileInputStream(keyStoreFile)) {
            keyStore.load(fis, keyStorePassword.toCharArray());
        }

        // Create KeyManagerFactory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyStorePassword.toCharArray());

        // Create SslContext
        return GrpcSslContexts.configure(
                SslContextBuilder.forServer(kmf)
                        .clientAuth(ClientAuth.NONE)
        ).build();
    }
}