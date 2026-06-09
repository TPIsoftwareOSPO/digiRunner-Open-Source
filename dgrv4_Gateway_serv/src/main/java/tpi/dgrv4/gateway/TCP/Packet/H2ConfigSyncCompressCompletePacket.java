package tpi.dgrv4.gateway.TCP.Packet;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.H2ConfigSyncServive;
import tpi.dgrv4.gateway.utils.BeanUtil;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

/**
 * Packet sent from a Source node to the Keeper to report the result of a database compression (Step 1).
 * It contains the status (success/failure) and details like the path to the compressed file.
 * <p>
 * 從來源 (Source) 節點發送到 Keeper 的封包，用於回報資料庫壓縮 (步驟 1) 的結果。
 * 它包含成功或失敗的狀態，以及壓縮檔案路徑等詳細資訊。
 */
public class H2ConfigSyncCompressCompletePacket implements Packet_i {
    private static final long serialVersionUID = 1L;

    /** The ID of the source node where compression was performed. / 執行壓縮的來源節點 ID。 */
    public String sourceId;
    /** Indicates if the compression was successful. / 指示壓縮是否成功。 */
    public boolean success;
    /** The file path of the resulting compressed database file. / 產生的壓縮資料庫檔案路徑。 */
    public String compressedFilePath;
    /** The original size of the database file in bytes. / 原始資料庫檔案的大小 (位元組)。 */
    public long originalSize;
    /** The size of the compressed file in bytes. / 壓縮後檔案的大小 (位元組)。 */
    public long compressedSize;
    /** The error message if compression failed. / 如果壓縮失敗，則為錯誤訊息。 */
    public String errorMessage;

    public H2ConfigSyncCompressCompletePacket() {}

    /**
     * Constructs a new DbSyncCompressCompletePacket.
     *
     * @param sourceId           The ID of the source node.
     * @param success            True if compression was successful.
     * @param compressedFilePath The path to the compressed file.
     * @param originalSize       The original file size.
     * @param compressedSize     The compressed file size.
     * @param errorMessage       The error message, if any.
     */
    public H2ConfigSyncCompressCompletePacket(String sourceId, boolean success,
                                              String compressedFilePath, long originalSize,
                                              long compressedSize, String errorMessage) {
        this.sourceId = sourceId;
//        this.targetIds = targetIds;
        this.success = success;
        this.compressedFilePath = compressedFilePath;
        this.originalSize = originalSize;
        this.compressedSize = compressedSize;
        this.errorMessage = errorMessage;
    }

    @Override
    public void runOnServer(LinkerServer ls) {
        // This runs on the Keeper, which receives the completion status from the Source node.
        // 這在 Keeper 上運行，它從來源 (Source) 節點接收完成狀態。
        if (success) {
            double ratio = (1.0 - (double)compressedSize / originalSize) * 100;

            TPILogger.tl.info(String.format("Step 1 (Compression) complete. Received from source: %s.", sourceId));
            TPILogger.tl.info(String.format("File: %s, Original: %.2f GB, Compressed: %.2f GB (Space saved: %.1f%%).",
                                            compressedFilePath,
                                            originalSize / 1024.0 / 1024.0 / 1024.0,
                                            compressedSize / 1024.0 / 1024.0 / 1024.0,
                                            ratio));

            // Key step: Notify the service to automatically trigger Step 2.
            // 關鍵步驟：通知服務以自動觸發步驟 2。
            try {
                TPILogger.tl.info("Notifying H2ConfigSyncService to trigger Step 2.");
                H2ConfigSyncServive service = BeanUtil.getBean(H2ConfigSyncServive.class);
                service.onStep1Complete(compressedFilePath);
                TPILogger.tl.info("H2ConfigSyncService notified successfully.");
            } catch (Exception e) {
                TPILogger.tl.error("Failed to notify H2ConfigSyncService: " + StackTraceUtil.logStackTrace(e));
            }

        } else {
            TPILogger.tl.error(String.format("Step 1 (Compression) failed. Received from source: %s.", sourceId));
            TPILogger.tl.error("Error: " + errorMessage);
            try {
                H2ConfigSyncServive service = BeanUtil.getBean(H2ConfigSyncServive.class);
                service.onStepError(1, errorMessage);
            } catch (Exception e) {
                TPILogger.tl.error("Failed to notify H2ConfigSyncService of the error: " + StackTraceUtil.logStackTrace(e));
            }
        }
    }

    @Override
    public void runOnClient(LinkerClient lc) {
    }
}
