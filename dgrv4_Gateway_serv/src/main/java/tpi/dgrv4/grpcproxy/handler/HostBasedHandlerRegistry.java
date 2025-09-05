package tpi.dgrv4.grpcproxy.handler;

import io.grpc.HandlerRegistry;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.ServerMethodDefinition;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.grpcproxy.marshaller.PassthroughMarshaller;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HostBasedHandlerRegistry extends HandlerRegistry {
    private final Map<String, ManagedChannel> proxyChannels;
    private static final boolean ENABLE_VERBOSE_LOGGING = false; // Logging switch

    public HostBasedHandlerRegistry(Map<String, ManagedChannel> proxyChannels) {
        // Create a new HashMap and copy all entries to ensure it won't be affected by external map changes
        this.proxyChannels = new HashMap<>(proxyChannels);

        // Add diagnostic logs
        if (ENABLE_VERBOSE_LOGGING) {
            if (!this.proxyChannels.isEmpty()) {
                TPILogger.tl.debug("Initializing HostBasedHandlerRegistry, including the following hostnames: " +
                        String.join(", ", this.proxyChannels.keySet()));
            } else {
                TPILogger.tl.warn("Initializing HostBasedHandlerRegistry, but no hostnames are included");
            }
        }
    }

    @Override
    public ServerMethodDefinition<InputStream, InputStream> lookupMethod(
            String methodName, String authority) {
        // If null or empty, fail fast
        if (authority == null || authority.isEmpty()) {
            if (ENABLE_VERBOSE_LOGGING) {
                TPILogger.tl.warn("Received null or empty authority header in lookupMethod, method: " + methodName);
            }
            return null;
        }

        // Optimize hostname extraction, avoid unnecessary string splitting
        String hostname;
        int colonIndex = authority.indexOf(':');
        if (colonIndex > 0) {
            hostname = authority.substring(0, colonIndex);
        } else {
            hostname = authority;
        }

        // Check if hostname is empty
        if (hostname.isEmpty()) {
            if (ENABLE_VERBOSE_LOGGING) {
                TPILogger.tl.warn("Empty hostname extracted from authority: " + authority + ", method: " + methodName);
            }
            return null;
        }

        if (ENABLE_VERBOSE_LOGGING) {
            TPILogger.tl.debug("Looking up handler for hostname '" + hostname + "', method: " + methodName);
            TPILogger.tl.debug("Available hostname mappings: " + String.join(", ", proxyChannels.keySet()));
        }

        // Look up the channel for this hostname
        ManagedChannel channel = proxyChannels.get(hostname);

        // If no mapping exists for this hostname, log a warning and return null
        if (channel == null) {
            if (ENABLE_VERBOSE_LOGGING) {
                TPILogger.tl.warn("No mapping found for hostname: " + hostname);
            }
            return null;
        }

        // Create and return a proxy method definition
        if (ENABLE_VERBOSE_LOGGING) {
            TPILogger.tl.debug("Creating proxy method definition for hostname '" + hostname + "', method: " + methodName);
        }
        return createProxyMethodDefinition(methodName, channel);
    }

    private ServerMethodDefinition<InputStream, InputStream> createProxyMethodDefinition(
            String methodName, ManagedChannel channel) {
        return ServerMethodDefinition.create(
                MethodDescriptor.<InputStream, InputStream>newBuilder()
                        .setType(MethodDescriptor.MethodType.UNKNOWN)
                        .setFullMethodName(methodName)
                        .setRequestMarshaller(new PassthroughMarshaller())
                        .setResponseMarshaller(new PassthroughMarshaller())
                        .build(),
                new ProxyServerCallHandler(channel, methodName));
    }

    /**
     * Get all proxy hostnames (for diagnostics)*
     * @return Set of proxy hostnames
     */
    public Set<String> getHostnames() {
        return new HashSet<>(proxyChannels.keySet());
    }
}