package tpi.dgrv4.gateway.TCP.Packet;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.H2ConfigSyncServive;
import tpi.dgrv4.gateway.utils.BeanUtil;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

/**
 * Packet sent from a Target node to the Keeper to report the result of a database replacement (Step 3).
 * <p>
 * 從目標 (Target) 節點發送到 Keeper 的封包，用於回報資料庫替換 (步驟 3) 的結果。
 */
public class H2ConfigSyncReplaceCompletePacket implements Packet_i {
    private static final long serialVersionUID = 1L;

    /** The ID of the target node where the replacement was performed. / 執行替換的目標節點 ID。 */
    public String targetId;
    /** Indicates if the replacement was successful. / 指示替換是否成功。 */
    public boolean success;
    /** The error message if replacement failed. / 如果替換失敗，則為錯誤訊息。 */
    public String errorMessage;

    public H2ConfigSyncReplaceCompletePacket() {}

    /**
     * Constructs a new DbSyncReplaceCompletePacket.
     * @param targetId     The ID of the target node.
     * @param success      True if replacement was successful.
     * @param errorMessage The error message, if any.
     */
    public H2ConfigSyncReplaceCompletePacket(String targetId, boolean success, String errorMessage) {
        this.targetId = targetId;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public void runOnServer(LinkerServer ls) {
        // This runs on the Keeper, which receives the completion status from the Target node.
        // 這在 Keeper 上運行，它從目標 (Target) 節點接收完成狀態。
        if (success) {
            TPILogger.tl.info(String.format("Step 3 (H2Config Replacement) complete for target: %s.", targetId));

            // Notify the service to update the overall progress.
            // 通知服務以更新整體進度。
            try {
                H2ConfigSyncServive service = BeanUtil.getBean(H2ConfigSyncServive.class);
                if (service != null) {
                    service.onStep3Complete(targetId);
                } else {
                    TPILogger.tl.error("Unable to notify service: H2ConfigSyncServive bean not found.");
                }
            } catch (Exception e) {
                TPILogger.tl.error("Failed to notify H2ConfigSyncService about Step 3 completion: " + StackTraceUtil.logStackTrace(e));
            }

        } else {
            TPILogger.tl.error(String.format("Step 3 (H2Config Replacement) failed for target: %s.", targetId));
            TPILogger.tl.error("Error: " + errorMessage);
            try {
                H2ConfigSyncServive service = BeanUtil.getBean(H2ConfigSyncServive.class);
                service.onStepError(3, "Target " + targetId + " failed: " + errorMessage);
            } catch (Exception e) {
                TPILogger.tl.error("Failed to notify H2ConfigSyncService of the error: " + StackTraceUtil.logStackTrace(e));
            }
        }
    }

    @Override
    public void runOnClient(LinkerClient lc) {
    }
}
