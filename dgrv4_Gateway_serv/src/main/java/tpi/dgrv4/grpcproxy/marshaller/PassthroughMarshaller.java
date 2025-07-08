package tpi.dgrv4.grpcproxy.marshaller;

import com.google.common.io.ByteStreams;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.StatusException;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A marshaller implementation that passes through binary data without any
 * actual serialization or deserialization. This allows proxying gRPC calls
 * without knowledge of the message types.
 */
public class PassthroughMarshaller implements MethodDescriptor.Marshaller<InputStream> {
    private final TPILogger logger = TPILogger.tl;
    private static final boolean ENABLE_VERBOSE_LOGGING = false; // Logging switch

    @Override
    public InputStream parse(InputStream stream) {
        if (stream == null) {
            if (ENABLE_VERBOSE_LOGGING) {
                logger.warn("Received null input stream for parsing");
            }
            return new ByteArrayInputStream(new byte[0]);
        }

        // This is an optimization: directly return original stream, avoid unnecessary copying
        // But be careful: if original stream is closed or consumed, this might cause problems
        // Therefore only use this optimization when confirmed original stream won't be consumed
        if (stream instanceof ByteArrayInputStream) {
            try {
                // If it's ByteArrayInputStream, reset it instead of creating new one
                ByteArrayInputStream bais = (ByteArrayInputStream) stream;
                bais.reset();
                return bais;
            } catch (Exception e) {
                // If reset fails, fall back to standard method
                if (ENABLE_VERBOSE_LOGGING) {
                    logger.debug("Failed to reset ByteArrayInputStream, falling back to copy");
                }
            }
        }

        try {
            byte[] bytes = ByteStreams.toByteArray(stream);
            if (ENABLE_VERBOSE_LOGGING) {
                logger.debug("Parsed input stream, size: " + bytes.length + " bytes");
            }
            return new ByteArrayInputStream(bytes);
        } catch (IOException e) {
            logger.error("IO error parsing input stream: " + e.getMessage());
            throw new RuntimeException(
                    new StatusException(Status.INTERNAL
                            .withDescription("IO error parsing input stream: " + e.getMessage())
                            .withCause(e))
            );
        } catch (Exception e) {
            logger.error("Unexpected error parsing input stream: " + e.getMessage());
            throw new RuntimeException(
                    new StatusException(Status.UNKNOWN
                            .withDescription("Unexpected error parsing input stream: " + e.getMessage())
                            .withCause(e))
            );
        }
    }

    @Override
    public InputStream stream(InputStream value) {
        if (value == null) {
            if (ENABLE_VERBOSE_LOGGING) {
                logger.warn("Received null input stream for streaming");
            }
            return new ByteArrayInputStream(new byte[0]);
        }

        // Similar optimization as parse method
        if (value instanceof ByteArrayInputStream) {
            try {
                // If it's ByteArrayInputStream, reset it instead of creating new one
                ByteArrayInputStream bais = (ByteArrayInputStream) value;
                bais.reset();
                return bais;
            } catch (Exception e) {
                // If reset fails, fall back to standard method
                if (ENABLE_VERBOSE_LOGGING) {
                    logger.debug("Failed to reset ByteArrayInputStream in stream, falling back to copy");
                }
            }
        }

        try {
            byte[] bytes = ByteStreams.toByteArray(value);
            if (ENABLE_VERBOSE_LOGGING) {
                logger.debug("Streamed input stream, size: " + bytes.length + " bytes");
            }
            return new ByteArrayInputStream(bytes);
        } catch (IOException e) {
            logger.error("IO error streaming input stream: " + e.getMessage());
            throw new RuntimeException(
                    new StatusException(Status.INTERNAL
                            .withDescription("IO error streaming input stream: " + e.getMessage())
                            .withCause(e))
            );
        } catch (Exception e) {
            logger.error("Unexpected error streaming input stream: " + e.getMessage());
            throw new RuntimeException(
                    new StatusException(Status.UNKNOWN
                            .withDescription("Unexpected error streaming input stream: " + e.getMessage())
                            .withCause(e))
            );
        }
    }
}