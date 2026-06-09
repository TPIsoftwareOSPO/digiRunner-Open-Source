package tpi.dgrv4.gateway.TCP.Packet;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.H2ConfigSyncServive;
import tpi.dgrv4.gateway.utils.BeanUtil;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Packet sent from the Keeper to a Source node to request the start of a file transmission (Step 2) to a Target.
 * The Source node will read the specified file, break it into chunks, and send it to the Target.
 * <p>
 * 從 Keeper 發送到來源 (Source) 節點的封包，用於請求開始向目標 (Target) 進行檔案傳輸 (步驟 2)。
 * 來源節點將讀取指定的檔案，將其分解為區塊，然後發送給目標。
 */
public class H2ConfigSyncTransmitRequestPacket implements Packet_i {

    public String sourceId;               /** The ID of the source node that will send the file. / 將發送檔案的來源節點 ID。 */
    public String targetId;               /** The ID of the target node that will receive the file. / 將接收檔案的目標節點 ID。 */
    public String targetRole;             /** The role of the target node (e.g., REPLICA). / 目標節點的角色 (例如 REPLICA)。 */
    public String compressedFilePath;     /** The path to the file to be transmitted (from Step 1). / 要傳輸的檔案路徑 (來自步驟 1)。 */

    // Manages latches for flow control and completion signals.
    // 管理用於流量控制和完成信號的鎖存器。
    public static final ConcurrentHashMap<String, CountDownLatch> batchAckLatches = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, CountDownLatch> transmitCompleteLatches = new ConcurrentHashMap<>();

    // Flow control parameters.
    // 流量控制參數。
    private static final int QUEUE_WARNING_THRESHOLD = 50;
    private static final int QUEUE_CHECK_DELAY_MS = 10;

    public H2ConfigSyncTransmitRequestPacket() {
    }

    /**
     * Constructs a new DbSyncTransmitRequestPacket.
     * @param sourceId           The ID of the source node.
     * @param targetId           The ID of the target node.
     * @param targetRole         The role of the target node.
     * @param compressedFilePath The path of the file to transmit.
     */
    public H2ConfigSyncTransmitRequestPacket(String sourceId, String targetId,
                                             String targetRole,
                                             String compressedFilePath) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.targetRole = targetRole;
        this.compressedFilePath = compressedFilePath;
    }

    @Override
    public void runOnServer(LinkerServer ls) {
        // This runs on the Keeper, which forwards the request to the Source client.
        // 這在 Keeper 上運行，它將請求轉發給來源 (Source) 客戶端。
        TPILogger.tl.info(String.format("Forwarding Step 2 (Transmit) request to source: %s for target: %s.", sourceId, targetId));
        TPILogger.tl.info("File to transmit: " + compressedFilePath);

        synchronized (CommunicationServer.cs.connClinet) {
            for (LinkerServer client : CommunicationServer.cs.connClinet) {
                if (client.userName != null && client.userName.contains(sourceId)) {
                    client.send(this);
                    TPILogger.tl.info(String.format("Request forwarded to client: %s.", client.userName));
                    return;
                }
            }
            TPILogger.tl.error(String.format("Source client not found: %s.", sourceId));
        }
    }

    @Override
    public void runOnClient(LinkerClient lc) {
        // This runs on the Source client, which starts a background thread to transmit the file.
        // 這在來源 (Source) 客戶端上運行，它啟動一個背景執行緒來傳輸檔案。
        new Thread(() -> {
            boolean success = false;
            String errorMessage = null;
            long transmittedBytes = 0;

            try {
                long startTime = System.currentTimeMillis();

                transmittedBytes = transmitFile(lc);

                    long duration = (System.currentTimeMillis() - startTime) / 1000;
                double avgSpeed = (transmittedBytes / 1024.0 / 1024.0) / Math.max(duration, 1);

                success = true;
                
                TPILogger.tl.info(String.format("Step 2 (Transmission) to target %s completed in %d seconds.", targetId, duration));
                TPILogger.tl.info(String.format("Total transmitted: %.2f GB, Average speed: %.2f MB/s.",
                                                transmittedBytes / 1024.0 / 1024.0 / 1024.0, avgSpeed));

            } catch (Exception e) {
                errorMessage = StackTraceUtil.logStackTrace(e);
                TPILogger.tl.error(String.format("Step 2 (Transmission) to target %s failed: %s", targetId, errorMessage));
            }

            // Send the final result back to the Keeper.
            // 將最終結果發送回 Keeper。
            H2ConfigSyncTransmitCompletePacket response = new H2ConfigSyncTransmitCompletePacket(
                    sourceId,
                    targetId,
                    success,
                    transmittedBytes,
                    errorMessage
            );

            lc.send(response);

        }, "Step2-Transmit-" + targetId).start();
    }

    /**
     * Handles the logic of reading a file, breaking it into chunks, and sending them to the target.
     * It includes flow control and waits for acknowledgments to ensure reliable delivery.
     * <p>
     * 處理讀取檔案、將其分解為區塊並發送給目標的邏輯。
     * 它包含流量控制和等待確認機制，以確保可靠的傳輸。
     * @return The total number of bytes transmitted.
     */
    private long transmitFile(LinkerClient lc) throws Exception {
        H2ConfigSyncServive h2ConfigSyncServive = BeanUtil.getBean(H2ConfigSyncServive.class);
        h2ConfigSyncServive.updateStep2Progress(targetId, 0);
        File file = new File(compressedFilePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + compressedFilePath);
        }

        int chunkSizeKb = TPILogger.tl.getFileSyncChunkSizeKb();
        if (chunkSizeKb <= 0) chunkSizeKb = 256;
        int chunkSize = chunkSizeKb * 1024;

        // Extract the timestamp from the source filename (e.g., "copy_12345.mv.db")
        // to create a corresponding target filename (e.g., "received_12345.mv.db").
        // 從來源檔案名稱 (例如 "copy_12345.mv.db") 中提取時間戳，以建立對應的目標檔案名稱 (例如 "received_12345.mv.db")。
        String sourceFileName = file.getName();
        String timestamp = extractTimestamp(sourceFileName);

        String targetFileName = "received_" + timestamp + ".mv.db";

        TPILogger.tl.info(String.format("Preparing to transmit file %s to target %s as %s.",
                                        sourceFileName, targetId, targetFileName));

        // Register a latch to wait for the final confirmation from the target.
        // 註冊一個鎖存器以等待來自目標的最終確認。
        CountDownLatch completeLatch = new CountDownLatch(1);
        transmitCompleteLatches.put(targetId, completeLatch);

        try (FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(chunkSize);
            long fileSize = channel.size();
            long bytesRead = 0;
            int chunkNumber = 0;
            int totalChunks = (int) Math.ceil((double) fileSize / chunkSize);

            TPILogger.tl.info(String.format("Starting transmission to %s. File size: %.2f MB, Chunk size: %d KB, Total chunks: %d.",
                                            targetId, fileSize / 1024.0 / 1024.0, chunkSizeKb, totalChunks));

            long lastProgressTime = System.currentTimeMillis();

            while (bytesRead < fileSize) {
                if (!lc.isConnected()) {
                    throw new IOException("Connection lost during transmission!");
                }

                buffer.clear();
                int read = channel.read(buffer);
                if (read == -1) break;

                buffer.flip();
                byte[] chunk = new byte[read];
                buffer.get(chunk);

                // Create the data packet, passing the target filename which includes the shared timestamp.
                // 建立資料封包，傳遞包含共享時間戳的目標檔案名稱。
                H2ConfigSyncTransmitDataPacket dataPacket = new H2ConfigSyncTransmitDataPacket(
                        targetId,
                        lc.userName,  // sourceId from connection
                        chunk,
                        chunkNumber,
                        totalChunks,
                        bytesRead + read >= fileSize,
                        fileSize,
                        targetFileName
                );

                // Flow control: Check the send queue size to avoid overwhelming the network buffer.
                // 流量控制：檢查發送佇列大小，以避免網路緩衝區不堪重負。
                while (true) {
                    int currentQueueSize = lc.getSnd().size();
                    if (currentQueueSize < QUEUE_WARNING_THRESHOLD) {
                        lc.send(dataPacket);
                        break;
                    } else {
                        try {
                            Thread.sleep(QUEUE_CHECK_DELAY_MS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new IOException("Transmission interrupted");
                        }
                    }
                }

                bytesRead += read;
                chunkNumber++;
                if (h2ConfigSyncServive != null && (chunkNumber % 50 == 0 || bytesRead >= fileSize)) {
                    int progress = (int) ((double) bytesRead / fileSize * 100);
                    h2ConfigSyncServive.updateStep2Progress(targetId, progress);
                }
                // Optional sleep to throttle sending speed.
                // 可選的睡眠以限制發送速度。
                try {
                    Thread.sleep(TPILogger.tl.getSleepTime());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // Periodically wait for a batch acknowledgment for flow control.
                // 定期等待批次確認以進行流量控制。
                if (chunkNumber > 0 && chunkNumber % 500 == 0) {
                    waitForBatchAck(chunkNumber);
                }

                // Log progress periodically or on the last chunk.
                // 定期或在最後一個區塊時記錄進度。
                long now = System.currentTimeMillis();
                if (now - lastProgressTime > 30000 || chunkNumber == totalChunks) {
                    int percent = (int) ((double) chunkNumber / totalChunks * 100);
                    TPILogger.tl.info(String.format("Transmission progress to %s: %d/%d chunks (%d%%).",
                                                    targetId, chunkNumber, totalChunks, percent));
                    lastProgressTime = now;
                }
            }

            TPILogger.tl.info(String.format("All %d chunks sent to target %s. Awaiting final confirmation.",
                                            totalChunks, targetId));

            // Wait for the target to confirm successful reception of the entire file.
            // 等待目標確認成功接收整個檔案。
            boolean received = completeLatch.await(30, TimeUnit.MINUTES);
            if (!received) {
                throw new IOException("Timeout waiting for final confirmation from target " + targetId);
            }

            TPILogger.tl.info(String.format("Final confirmation received from target %s.", targetId));
            h2ConfigSyncServive.updateStep2Progress(targetId, 100);
            return bytesRead;

        } finally {
            // Clean up latches for this target.
            // 清理此目標的鎖存器。
            transmitCompleteLatches.remove(targetId);
            batchAckLatches.remove(targetId);
        }
    }

    /**
     * Extracts a numeric timestamp from a filename like "copy_12345.mv.db".
     * This allows the receiver to construct a corresponding filename, ensuring consistency.
     * <p>
     * 從類似 "copy_12345.mv.db" 的檔案名稱中提取數字時間戳。
     * 這允許接收方建構對應的檔案名稱，確保一致性。
     */
    private String extractTimestamp(String fileName) {
        try {
            // 移除副檔名
            String nameWithoutExt = fileName.replace(".mv.db", "");

            // 移除前綴
            String timestamp = nameWithoutExt.replace("copy_", "");

            // 驗證是否為數字
            if (timestamp.matches("\\d+")) {
                return timestamp;
            } else {
                TPILogger.tl.warn("Could not extract a valid numeric timestamp from filename: " + fileName + ". Using current time as fallback.");
                // Fallback to current time if format is unexpected.
                // 如果格式不符預期，則退回使用當前時間。
                return String.valueOf(System.currentTimeMillis());
            }
        } catch (Exception e) {
            TPILogger.tl.error("Error extracting timestamp from filename '" + fileName + "'. Using current time as fallback. \n"+ StackTraceUtil.logStackTrace(e));
            return String.valueOf(System.currentTimeMillis());
        }
    }

    /**
     * Waits for a batch acknowledgment from the target to regulate the flow of data.
     * <p>
     * 等待來自目標的批次確認，以調節資料流。
     */
    private void waitForBatchAck(int currentChunk) {
        CountDownLatch latch = new CountDownLatch(1);
        batchAckLatches.put(targetId, latch);
        try {
            boolean received = latch.await(60, TimeUnit.SECONDS);
            if (!received) {
                TPILogger.tl.warn(String.format("Timeout waiting for batch ACK after chunk %d for target %s.",
                                                currentChunk, targetId));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            TPILogger.tl.warn("Thread interrupted while waiting for batch ACK.");
        } finally {
            batchAckLatches.remove(targetId);
        }
    }
}