package tpi.dgrv4.grpcproxy.listener;

import io.grpc.ClientCall;
import io.grpc.ServerCall;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.io.InputStream;

/**
 * A ServerCall.Listener that receives requests from the original client
 * and forwards them to the target service through the ClientCall.
 *
 */
public class GrpcServerCallListener extends ServerCall.Listener<InputStream> {
    /**
     * Logger instance for logging operations.
     *
     */
    private final TPILogger logger = TPILogger.tl;

    /**
     * The client call to the target service.
     *
     */
    private final ClientCall<InputStream, InputStream> clientCall;

    /**
     * Constructs a new GrpcServerCallListener that will forward requests to the given client call.
     *
     * @param clientCall The client call to forward requests to
     */
    public GrpcServerCallListener(ClientCall<InputStream, InputStream> clientCall) {
        this.clientCall = clientCall;
        logger.debug("Created new GrpcServerCallListener");
    }

    /**
     * Called when a message is received from the original client.
     * Forwards the message to the target service.
     *
     * @param message The message received from the original client
     */
    @Override
    public void onMessage(InputStream message) {
        logger.debug("GrpcServerCallListener received message");
        try {
            // Forward the message to the target service
            // Forward the message to the target service
            clientCall.sendMessage(message);
            logger.debug("Message forwarded to client call");
        } catch (Exception e) {
            logger.error("Error forwarding message to client call");
            throw e;
        }
    }

    /**
     * Called when the original client has finished sending messages (half-close).
     * Half-closes the call to the target service.
     */
    @Override
    public void onHalfClose() {
        logger.debug("GrpcServerCallListener half-closing");
        try {
            // Half-close the call to the target service
            // Half-close the call to the target service
            clientCall.halfClose();
            logger.debug("Client call half-closed");
        } catch (Exception e) {
            logger.error("Error during half-close");
            throw e;
        }
    }

    /**
     * Called when the original client cancels the call.
     * Cancels the call to the target service.
     */
    @Override
    public void onCancel() {
        logger.debug("GrpcServerCallListener cancelled");
        try {
            // Cancel the call to the target service
            // Cancel the call to the target service
            clientCall.cancel("Client cancelled the call", null);
            logger.debug("Client call cancelled");
        } catch (Exception e) {
            logger.error("Error cancelling client call");
            throw e;
        }
    }

    /**
     * Called when the call from the original client is completed.
     */
    @Override
    public void onComplete() {
        logger.debug("GrpcServerCallListener completed");
    }

    /**
     * Called when the server call is ready to receive more messages.
     */
    @Override
    public void onReady() {
        logger.debug("GrpcServerCallListener ready");
    }
}