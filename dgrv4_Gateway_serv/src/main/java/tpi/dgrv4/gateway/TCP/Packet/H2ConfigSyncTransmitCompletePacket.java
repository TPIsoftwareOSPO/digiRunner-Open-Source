package tpi.dgrv4.gateway.TCP.Packet;

import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.H2ConfigSyncServive;
import tpi.dgrv4.gateway.utils.BeanUtil;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

/**
 * Packet sent from a Source node to the Keeper to report the result of a file transmission (Step 2) to a specific target.
 * This is sent after the Source has finished sending all file chunks and has received a final ACK from the Target.
 * <p>
 * 從來源 (Source) 節點發送到 Keeper 的封包，用於回報對特定目標的檔案傳輸 (步驟 2) 的結果。
 * 這在來源節點完成發送所有檔案區塊並從目標節點收到最終 ACK 後發送。
 */
public class H2ConfigSyncTransmitCompletePacket implements Packet_i {

    /** The ID of the source node that sent the file. / 發送檔案的來源節點 ID。 */
    public String sourceId;
    /** The ID of the target node that received the file. / 接收檔案的目標節點 ID。 */
    public String targetId;
    /** Indicates if the transmission was successful. / 指示傳輸是否成功。 */
    public boolean success;
    /** The total number of bytes transmitted. / 傳輸的總位元組數。 */
    public long transmittedBytes;
    /** The error message if transmission failed. / 如果傳輸失敗，則為錯誤訊息。 */
    public String errorMessage;

    public H2ConfigSyncTransmitCompletePacket() {}

    /**
     * Constructs a new DbSyncTransmitCompletePacket.
     * @param sourceId         The ID of the source node.
     * @param targetId         The ID of the target node.
     * @param success          True if transmission was successful.
     * @param transmittedBytes Total bytes transmitted.
     * @param errorMessage     The error message, if any.
     */
    public H2ConfigSyncTransmitCompletePacket(String sourceId, String targetId,
                                              boolean success, long transmittedBytes, String errorMessage) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.success = success;
        this.transmittedBytes = transmittedBytes;
        this.errorMessage = errorMessage;
    }

    @Override
    public void runOnServer(LinkerServer ls) {
        // This runs on the Keeper, which receives the transmission completion status from the Source node.
        // 這在 Keeper 上運行，它從來源 (Source) 節點接收傳輸完成狀態。
        if (success) {
            TPILogger.tl.info(String.format("Step 2 (Transmission) complete. Source: %s, Target: %s.", sourceId, targetId));
            TPILogger.tl.info(String.format("Total transmitted: %.2f GB.", transmittedBytes / 1024.0 / 1024.0 / 1024.0));

            try {
                TPILogger.tl.info(String.format("Notifying H2ConfigSyncService of Step 2 completion for target: %s.", targetId));
                H2ConfigSyncServive service = BeanUtil.getBean(H2ConfigSyncServive.class);
                service.onStep2Complete(targetId);
            } catch (Exception e) {
                TPILogger.tl.error("Failed to notify H2ConfigSyncService about Step 2 completion: " + e.getMessage());
            }
        } else {
            TPILogger.tl.error(String.format("Step 2 (Transmission) failed. Source: %s, Target: %s.", sourceId, targetId));
            TPILogger.tl.error("Error: " + errorMessage);
            try {
                H2ConfigSyncServive service = BeanUtil.getBean(H2ConfigSyncServive.class);
                service.onStepError(2, "Source " + sourceId + " failed to transmit to " + targetId + ": " + errorMessage);
            } catch (Exception e) {
                TPILogger.tl.error("Failed to notify H2ConfigSyncService of the error: " + e.getMessage());
            }
        }
    }

    @Override
    public void runOnClient(LinkerClient lc) {

    }
}
