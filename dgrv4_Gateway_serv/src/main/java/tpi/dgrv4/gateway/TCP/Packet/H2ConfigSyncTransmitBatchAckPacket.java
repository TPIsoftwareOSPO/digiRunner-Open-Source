package tpi.dgrv4.gateway.TCP.Packet;

import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

import java.util.concurrent.CountDownLatch;

/**
 * Packet sent from a Target node back to the Source (via the Keeper) to acknowledge
 * the reception of a batch of data chunks. This is used for flow control to prevent
 * the sender from overwhelming the receiver.
 * <p>
 * 從目標 (Target) 節點經由 Keeper 回傳給來源 (Source) 節點的封包，用於確認收到一批資料區塊。
 * 這被用於流量控制，以防止發送方壓垮接收方。
 */
public class H2ConfigSyncTransmitBatchAckPacket implements Packet_i {

    /** The ID of the target node sending the acknowledgment. / 發送確認的目標節點 ID。 */
    public String targetId;
    /** The ID of the source node that sent the data. / 發送資料的來源節點 ID。 */
    public String sourceId;
    /** The chunk number being acknowledged. / 被確認的區塊編號。 */
    public int chunkNumber;
    /** Indicates if the batch was received successfully. / 指示該批次是否成功接收。 */
    public boolean success;

    public H2ConfigSyncTransmitBatchAckPacket() {}

    /**
     * Constructs a new DbSyncTransmitBatchAckPacket.
     */
    public H2ConfigSyncTransmitBatchAckPacket(String targetId, String sourceId, int chunkNumber, boolean success) {
        this.targetId = targetId;
        this.sourceId = sourceId;
        this.chunkNumber = chunkNumber;
        this.success = success;
    }

    @Override
    public void runOnServer(LinkerServer ls) {
        // This runs on the Keeper, which forwards the batch acknowledgment back to the original Source client.
        // 這在 Keeper 上運行，它將批次確認訊息轉發回原始的來源 (Source) 客戶端。
        synchronized (CommunicationServer.cs.connClinet) {
            for (LinkerServer client : CommunicationServer.cs.connClinet) {
                if (client.userName != null && client.userName.contains(sourceId)) {
                    client.send(this);
                    return;
                }
            }
            TPILogger.tl.warn(String.format("Source client %s not found to forward batch ACK.", sourceId));
        }
    }

    @Override
    public void runOnClient(LinkerClient lc) {
        // This runs on the Source client, unblocking the thread waiting for a batch acknowledgment.
        // 這在來源 (Source) 客戶端上運行，解除等待批次確認的執行緒的阻塞。
        CountDownLatch latch = H2ConfigSyncTransmitRequestPacket.batchAckLatches.get(targetId);
        if (latch != null) {
            latch.countDown();
        }
    }
}
