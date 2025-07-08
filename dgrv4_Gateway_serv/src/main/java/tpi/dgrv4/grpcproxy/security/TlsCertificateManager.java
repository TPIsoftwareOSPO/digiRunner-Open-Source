package tpi.dgrv4.grpcproxy.security;

import io.grpc.netty.GrpcSslContexts;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.stereotype.Component;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.*;
import java.util.Collection;

/**
 * Utility class for managing TLS certificates and SSL contexts.
 * Supports building SSL contexts directly from string content instead of reading from files.
 */
@Component
public class TlsCertificateManager {
    private final TPILogger logger = TPILogger.tl;
    /**
     * Creates a client SSL context that automatically trusts all upstream certificates.
     *
     * @return A client SSL context configured to trust all certificates automatically.
     * @throws IOException If an error occurs while processing the certificates.
     */
    public SslContext createInsecureClientSslContext() throws IOException {
        logger.debug("Creating a client SSL context that trusts all certificates automatically.");

        try {
            SslContext context = GrpcSslContexts.configure(SslContextBuilder.forClient()
                            .trustManager(InsecureTrustManagerFactory.INSTANCE))
                    .build();
            logger.debug("Successfully created an SSL context that trusts all certificates.");
            return context;
        } catch (Exception e) {
            logger.error("Error creating client SSL context: " + e.getMessage() + ", possibly an SSL library configuration issue.");
            throw new IOException("Failed to create client SSL context", e);
        }
    }

    /**
     * Creates a client SSL context from a PEM-formatted trust certificate string.
     *
     * @param trustCertsContent The content of the trust certificates in PEM format.
     * @return A client SSL context configured with the specified trust certificates.
     * @throws IOException If an error occurs while processing the certificates.
     */
    public SslContext createClientSslContext(String trustCertsContent) throws IOException {
        if (trustCertsContent == null || trustCertsContent.isEmpty()) {
            logger.error("Trust certificates content is empty, cannot create SSL context.");
            throw new IllegalArgumentException("Trust certificates content cannot be empty.");
        }

        logger.debug("Creating a client SSL context from string content using custom trust certificates.");

        try {
            // Analyze certificate count and basic information
            int certCount = 0;
            StringBuilder certInfo = new StringBuilder("Trust certificates content analysis:");

            String beginMarker = "-----BEGIN CERTIFICATE-----";
            String endMarker = "-----END CERTIFICATE-----";
            int beginIndex = trustCertsContent.indexOf(beginMarker);

            while (beginIndex != -1) {
                certCount++;
                int endIndex = trustCertsContent.indexOf(endMarker, beginIndex);

                if (endIndex != -1) {
                    try {
                        String certPem = trustCertsContent.substring(beginIndex, endIndex + endMarker.length());
                        ByteArrayInputStream certStream = new ByteArrayInputStream(
                                certPem.getBytes(StandardCharsets.UTF_8));
                        CertificateFactory cf = CertificateFactory.getInstance("X.509");
                        X509Certificate cert = (X509Certificate) cf.generateCertificate(certStream);

                        certInfo.append("\n  Certificate #").append(certCount)
                                .append(": Subject=").append(cert.getSubjectX500Principal().getName())
                                .append(", Issuer=").append(cert.getIssuerX500Principal().getName())
                                .append(", Valid from ").append(cert.getNotBefore())
                                .append(" to ").append(cert.getNotAfter());
                    } catch (Exception e) {
                        certInfo.append("\n  Certificate #").append(certCount).append(": Unable to parse: ").append(e.getMessage());
                    }
                }

                beginIndex = trustCertsContent.indexOf(beginMarker, beginIndex + beginMarker.length());
            }

            logger.debug(certInfo.toString());

            if (certCount == 0) {
                logger.warn("No valid X.509 certificates found in the provided PEM content.");
            } else {
                logger.debug("Found " + certCount + " X.509 certificates in the provided PEM content.");
            }

            // Convert trust certificate content to InputStream
            ByteArrayInputStream trustCertsStream = new ByteArrayInputStream(
                    trustCertsContent.getBytes(StandardCharsets.UTF_8));

            // Build SSL context
            SslContext context = GrpcSslContexts.configure(SslContextBuilder.forClient()
                            .trustManager(trustCertsStream))
                    .build();

            logger.debug("Successfully created SSL context with custom trust certificates.");
            return context;
        } catch (Exception e) {
            logger.error("Error creating client SSL context: " + e.getMessage());
            throw new IOException("Failed to create client SSL context", e);
        }
    }

    /**
     * Validates the effectiveness of an X.509 certificate.
     *
     * @param certContent The content of the X.509 certificate in PEM format.
     * @return True if the certificate is valid, otherwise false.
     */
    public boolean validateCertificate(String certContent) {
        if (certContent == null || certContent.isEmpty()) {
            logger.error("Certificate content is empty, cannot validate.");
            return false;
        }

        try {
            ByteArrayInputStream certStream = new ByteArrayInputStream(
                    certContent.getBytes(StandardCharsets.UTF_8));
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Collection<? extends X509Certificate> certs = (Collection<? extends X509Certificate>)
                    cf.generateCertificates(certStream);

            if (certs.isEmpty()) {
                logger.error("No valid X.509 certificates found.");
                return false;
            }

            // Log the number of certificates found
            logger.debug("Found " + certs.size() + " X.509 certificates in the provided content.");

            // Check the first certificate
            X509Certificate cert = certs.iterator().next();

            // Log basic certificate information
            logger.debug("Certificate information: Subject=" + cert.getSubjectX500Principal().getName() +
                    ", Issuer=" + cert.getIssuerX500Principal().getName() +
                    ", Valid from " + cert.getNotBefore() + " to " + cert.getNotAfter());

            try {
                // Check if the certificate is expired
                cert.checkValidity();
                logger.debug("Certificate validation successful, the certificate is within its validity period.");
            } catch (CertificateExpiredException e) {
                logger.error("Certificate has expired, expiration date: " + cert.getNotAfter());
                return false;
            } catch (CertificateNotYetValidException e) {
                logger.error("Certificate is not yet valid, effective date: " + cert.getNotBefore());
                return false;
            }

            return true;
        } catch (CertificateException e) {
            logger.error("Certificate validation failed: " + e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("An unexpected error occurred during certificate validation: " + e.getMessage());
            return false;
        }
    }
}