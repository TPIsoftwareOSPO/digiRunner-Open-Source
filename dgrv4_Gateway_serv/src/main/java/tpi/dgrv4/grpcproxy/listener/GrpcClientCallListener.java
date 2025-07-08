package tpi.dgrv4.grpcproxy.listener;

import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.Status;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.io.InputStream;

/**
 * A ClientCall.Listener that receives responses from the target service
 * and forwards them back to the original client through the ServerCall.
 */
public class GrpcClientCallListener extends ClientCall.Listener<InputStream> {
    /**
     * Logger instance for logging operations.
     */
    private final TPILogger logger = TPILogger.tl;

    /**
     * The server call to send responses back to the original client.
     */
    private final ServerCall<InputStream, InputStream> serverCall;

    /**
     * Flag indicating whether headers have been sent to the original client.
     */
    private boolean headersSent = false;

    /**
     * Constructs a new GrpcClientCallListener that will forward responses to the given server call.
     * @param serverCall The server call to forward responses to
     */
    public GrpcClientCallListener(ServerCall<InputStream, InputStream> serverCall) {
        this.serverCall = serverCall;
        logger.debug("Created new GrpcClientCallListener");
    }

    /**
     * Called when a message is received from the target service.
     * Forwards the message to the original client.
     *
     * @param message The message received from the target service
     */
    @Override
    public void onMessage(InputStream message) {
        logger.debug("GrpcClientCallListener received message");
        try {
            // Ensure headers are sent before sending any messages
            if (!headersSent) {
                logger.debug("Sending initial headers");
                serverCall.sendHeaders(new Metadata());
                headersSent = true;
            }

            // Forward the message to the original client
            serverCall.sendMessage(message);

            // Request the next message from the target service
            serverCall.request(1);
            logger.debug("Message forwarded to server call and requested next message");
        } catch (Exception e) {
            logger.error("Error forwarding message to server call");
            throw e;
        }
    }

    /**
     * Called when the call to the target service is closed.
     * Closes the call to the original client with the same status and trailers.
     * @param status The status of the call
     * @param trailers The trailing metadata
     */
    @Override
    public void onClose(Status status, Metadata trailers) {
        logger.debug("GrpcClientCallListener closing with status: " + status);
        try {
            // Ensure headers are sent before closing
            if (!headersSent) {
                logger.debug("Sending headers before close");
                serverCall.sendHeaders(new Metadata());
                headersSent = true;
            }

            // Close the call to the original client
            serverCall.close(status, trailers);
            logger.debug("Server call closed");
        } catch (Exception e) {
            logger.error("Error closing server call");
            throw e;
        }
    }

    /**
     * Called when the client call is ready to send more messages.
     */
    @Override
    public void onReady() {
        logger.debug("GrpcClientCallListener ready");
    }
}