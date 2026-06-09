package tpi.dgrv4.gateway.TCP.Packet;

import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

import java.util.concurrent.CountDownLatch;

/**
 * Packet sent from a Target node back to the Source (via the Keeper) to acknowledge
 * the final result of a file transmission. This is used to unblock the sending thread
 * after all data chunks have been sent and verified.
 * <p>
 * 從目標 (Target) 節點經由 Keeper 回傳給來源 (Source) 節點的封包，用於確認檔案傳輸的最終結果。
 * 這在所有資料區塊都已發送並驗證後，用來解除傳送執行緒的阻塞。
 */
public class H2ConfigSyncTransmitAckPacket implements Packet_i {

    /** The ID of the target node sending the acknowledgment. / 發送確認的目標節點 ID。 */
    public String targetId;
    /** The ID of the source node that sent the file. / 發送檔案的來源節點 ID。 */
    public String sourceId;
    /** Indicates if the file was received successfully and passed validation. / 指示檔案是否成功接收並通過驗證。 */
    public boolean success;
    /** A message providing details, especially in case of an error. / 提供詳細資訊的訊息，尤其是在發生錯誤時。 */
    public String message;

    public H2ConfigSyncTransmitAckPacket() {}

    /**
     * Constructs a new DbSyncTransmitAckPacket.
     */
    public H2ConfigSyncTransmitAckPacket(String targetId, String sourceId,
                                         boolean success, String message) {
        this.targetId = targetId;
        this.sourceId = sourceId;
        this.success = success;
        this.message = message;
    }

    @Override
    public void runOnServer(LinkerServer ls) {
        // This runs on the Keeper, which forwards the acknowledgment back to the original Source client.
        // 這在 Keeper 上運行，它將確認訊息轉發回原始的來源 (Source) 客戶端。
        if (success) {
            TPILogger.tl.info(String.format("Target %s confirmed successful file reception.", targetId));
        } else {
            TPILogger.tl.error(String.format("Target %s reported a file reception error: %s", targetId, message));
        }

        synchronized (CommunicationServer.cs.connClinet) {
            for (LinkerServer client : CommunicationServer.cs.connClinet) {
                if (client.userName != null && client.userName.contains(sourceId)) {
                    client.send(this);
                    return;
                }
            }
            TPILogger.tl.error(String.format("Source client %s not found to forward transmit ACK.", sourceId));
        }
    }

    @Override
    public void runOnClient(LinkerClient lc) {
        // This runs on the Source client, unblocking the thread that is waiting for transmission confirmation.
        // 這在來源 (Source) 客戶端上運行，解除等待傳輸確認的執行緒的阻塞。
        CountDownLatch latch = H2ConfigSyncTransmitRequestPacket.transmitCompleteLatches.get(targetId);
        if (latch != null) {
            latch.countDown();
        }
    }
}
