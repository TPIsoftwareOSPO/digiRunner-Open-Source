package tpi.dgrv4.gateway.TCP.Packet;

import org.springframework.util.StringUtils;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.H2ConfigControlService;
import tpi.dgrv4.gateway.utils.BeanUtil;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

import java.util.HashSet;
import java.util.Set;


public class ExecutePoolSoftEvictPacket implements Packet_i {
    public String primaryId;
    public String replicaIds;

    @Override
    public void runOnServer(LinkerServer ls) {
        // When this packet is sent from a Client to the Server, this code executes on the KeeperServer.
        String sourceNode = ls.userName;
        TPILogger.tl.info(String.format("Received broadcast request for poolSoftEvict from node [%s]. Primary: [%s], Replicas: [%s]", sourceNode, primaryId, replicaIds));
        Set<String> h2ConfigNodeIds = new HashSet<>();
        if (StringUtils.hasLength(primaryId )) {
            h2ConfigNodeIds.add(primaryId);
        }
        if (StringUtils.hasLength(replicaIds)) {
            h2ConfigNodeIds.addAll(Set.of(replicaIds.split(",")));
        }
        // Create a new, clean command packet for broadcasting to avoid sending masterId/slaveId to end nodes.
        ExecutePoolSoftEvictPacket commandPacket = new ExecutePoolSoftEvictPacket();


        synchronized (CommunicationServer.cs.connClinet) {
            for (LinkerServer targetClient : CommunicationServer.cs.connClinet) {
                String targetClientName = targetClient.userName;

                // Do not send to the source node that initiated the broadcast.
                if (targetClientName != null && targetClientName.equals(sourceNode)) {
                    continue;
                }

                // Send to all nodes except the new Primary. This ensures all gateways and other replicas get the update.
                if (targetClientName != null && !h2ConfigNodeIds.contains(targetClientName)) {
                    TPILogger.tl.info(String.format("Sending poolSoftEvict command to node [%s]...", targetClientName));
                    targetClient.send(commandPacket);
                }
            }
        }
    }

    @Override
    public void runOnClient(LinkerClient lc) {
        // When this packet is sent from the Server to a Client, this code executes on the Gateway node.
        TPILogger.tl.info(lc.userName + " received command from server, preparing to perform a database connection pool soft evict...");
        try {
            H2ConfigControlService h2ConfigControlService = BeanUtil.getBean(H2ConfigControlService.class);
            h2ConfigControlService.poolSoftEvict();
            TPILogger.tl.info(lc.userName + " successfully performed poolSoftEvict.");
        } catch (Exception e) {
            TPILogger.tl.error("An error occurred while executing poolSoftEvict: \n" +  StackTraceUtil.logStackTrace(e));
        }
    }
}