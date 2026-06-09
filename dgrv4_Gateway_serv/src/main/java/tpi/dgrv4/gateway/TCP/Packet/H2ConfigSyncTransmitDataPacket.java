package tpi.dgrv4.gateway.TCP.Packet;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Packet that carries a chunk of the database file from the Source to the Target (via the Keeper).
 * This is the primary data-carrying packet for Step 2.
 * <p>
 * 將資料庫檔案的區塊從來源 (Source) 經由 Keeper 傳輸到目標 (Target) 的封包。
 * 這是步驟 2 的主要資料承載封包。
 */
public class H2ConfigSyncTransmitDataPacket implements Packet_i {
    private static final long serialVersionUID = 1L;

    public String targetId;        /** The destination target node ID. / 目標節點 ID。 */
    public String sourceId;        /** The originating source node ID. / 來源節點 ID。 */
    public byte[] fileContent;     /** The byte content of the file chunk. / 檔案區塊的位元組內容。 */
    public int chunkNumber;        /** The sequence number of this chunk (0-based). / 此區塊的序號 (從 0 開始)。 */
    public int totalChunks;        /** The total number of chunks for the entire file. / 整個檔案的總區塊數。 */
    public boolean isLastChunk;    /** Flag indicating if this is the last chunk. / 標示這是否為最後一個區塊的旗標。 */
    public long totalFileSize;     /** The total size of the original file. / 原始檔案的總大小。 */
    public String targetFileName;  /** The name for the file on the target machine. / 在目標機器上的檔案名稱。 */

    // Tracks the next expected chunk number for each target to ensure order.
    // 追蹤每個目標的下一個預期區塊編號，以確保順序。
    private static final Map<String, Integer> chunkTracker = new ConcurrentHashMap<>();

    public H2ConfigSyncTransmitDataPacket() {}

    /**
     * Constructs a new DbSyncTransmitDataPacket.
     */
    public H2ConfigSyncTransmitDataPacket(String targetId,
                                          String sourceId,
                                          byte[] fileContent,
                                          int chunkNumber,
                                          int totalChunks,
                                          boolean isLastChunk,
                                          long totalFileSize,
                                          String targetFileName) {
        this.targetId = targetId;
        this.sourceId = sourceId;
        this.fileContent = fileContent;
        this.chunkNumber = chunkNumber;
        this.totalChunks = totalChunks;
        this.isLastChunk = isLastChunk;
        this.totalFileSize = totalFileSize;
        this.targetFileName = targetFileName;
    }

    @Override
    public void runOnServer(LinkerServer ls) {
        // This runs on the Keeper, which acts as a proxy, forwarding the data packet to the correct Target client.
        // 這在 Keeper 上運行，它作為代理，將資料封包轉發到正確的目標 (Target) 客戶端。
        synchronized (CommunicationServer.cs.connClinet) {
            for (LinkerServer client : CommunicationServer.cs.connClinet) {
                if (client.userName != null && client.userName.contains(targetId)) {
                    try {
                        client.send(this);
                    } catch (Exception e) {
                        TPILogger.tl.error("Keeper send error: " + StackTraceUtil.logStackTrace(e));
                        // Optionally, notify the source that the target is unreachable
                    }
                    return;
                }
            }
            TPILogger.tl.warn(String.format("Target client %s not found to forward data chunk %d.", targetId, chunkNumber));
        }
    }

    @Override
    public void runOnClient(LinkerClient lc) {
        // This runs on the Target client, which receives the data chunk and writes it to a temporary file.
        // 這在目標 (Target) 客戶端上運行，它接收資料區塊並將其寫入暫存檔案。
        int chunkSizeKb = TPILogger.tl.getFileSyncChunkSizeKb();
        if (chunkSizeKb <= 0) chunkSizeKb = 256;
        long chunkSizeByte = chunkSizeKb * 1024L;

        try {
            // 組合完整的檔案路徑
            String tempFilePath = getTargetFilePath(targetFileName);

            Path targetPath = Paths.get(tempFilePath);
            if (targetPath.getParent() != null) {
                Files.createDirectories(targetPath.getParent());
            }

            // 第一個 chunk
            if (chunkNumber == 0) {
                chunkTracker.put(targetId, 0);
                TPILogger.tl.info(String.format("Starting to receive file: %s.", tempFilePath));
                TPILogger.tl.info(String.format("Total size: %.2f GB, Total chunks: %d.", totalFileSize / 1024.0 / 1024.0 / 1024.0, totalChunks));
                Files.deleteIfExists(targetPath);
                try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }

            int expectedChunk = chunkTracker.getOrDefault(targetId, 0);

            // Discard duplicate packets
            // 丟棄重複的封包
            if (this.chunkNumber < expectedChunk) {
                TPILogger.tl.warn(String.format("Discarding duplicate chunk %d for target %s.", this.chunkNumber, targetId));
                return;
            }

            if (this.chunkNumber > expectedChunk) {
                String errorMsg = String.format("Packet loss detected. Expected chunk %d, but received %d.", expectedChunk, this.chunkNumber);
                TPILogger.tl.error(errorMsg);
                sendFileReceivedAck(lc, false, errorMsg);
                return;
            }

            // 寫入檔案
            File tempFile = new File(tempFilePath);
            try (RandomAccessFile raf = new RandomAccessFile(tempFile, "rw")) {
                long writeOffset = (long) chunkNumber * chunkSizeByte;
                raf.seek(writeOffset);
                raf.write(fileContent);
                if (isLastChunk) {
                    raf.getFD().sync();
                }
            }

            chunkTracker.put(targetId, this.chunkNumber + 1);

            // Send a batch ACK for flow control.
            // 發送批次 ACK 以進行流量控制。
            if (chunkNumber > 0 && chunkNumber % 500 == 0) {
                H2ConfigSyncTransmitBatchAckPacket ack = new H2ConfigSyncTransmitBatchAckPacket(
                        targetId, sourceId, chunkNumber, true);
                lc.send(ack);
            }

            // 最後一個 chunk
            if (isLastChunk) {
                chunkTracker.remove(targetId);

                long currentSize = tempFile.length();

                // Final file size validation.
                // 最終檔案大小驗證。
                if (currentSize != this.totalFileSize) {
                    boolean match = false;
                    for (int i = 0; i < 3; i++) {
                        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                        if (tempFile.length() == this.totalFileSize) {
                            match = true;
                            break;
                        }
                    }
                    if (!match) {
                        String err = String.format("Final file size mismatch. Expected: %d, Actual: %d.", this.totalFileSize, tempFile.length());
                        TPILogger.tl.error(err);
                        sendFileReceivedAck(lc, false, err);
                        return;
                    }
                }

                TPILogger.tl.info(String.format("File reception complete. Final size: %.2f GB.", currentSize / 1024.0 / 1024.0 / 1024.0));

                // 確認收到完整檔案
                sendFileReceivedAck(lc, true, "File received successfully");
            }

        } catch (Exception e) {
            TPILogger.tl.error(String.format(
                    "Error processing chunk %d for target %s: %s", chunkNumber, targetId, StackTraceUtil.logStackTrace(e)));
            sendFileReceivedAck(lc, false, StackTraceUtil.logStackTrace(e));
        }
    }

    /**
     * Constructs the full path for the temporary file on the target machine.
     * The file is placed in a 'dgr_snapshots' subdirectory relative to the main database path.
     * <p>
     * 在目標機器上建構暫存檔案的完整路徑。
     * 該檔案被放置在相對於主資料庫路徑的 'dgr_snapshots' 子目錄中。
     */
    private String getTargetFilePath(String fileName) throws Exception {
        // 根據 Target 角色取得資料庫路徑
        String h2ConfigPath = TPILogger.tl.getReplicaH2ConfigPath();
        if (h2ConfigPath == null || h2ConfigPath.isEmpty()) {
            h2ConfigPath = TPILogger.tl.getPrimaryH2ConfigPath();
        }
        if (h2ConfigPath == null || h2ConfigPath.isEmpty()) {
            throw new Exception("Cannot determine database path");
        }

        // 取得資料庫的父目錄
        Path H2ConfigFilePath = Paths.get(h2ConfigPath).toAbsolutePath().normalize();
        Path parentDir = H2ConfigFilePath.getParent();
        if (parentDir == null) {
            parentDir = Paths.get(".").toAbsolutePath().normalize();
        }

        // 快照目錄
        Path snapshotDir = parentDir.resolve("dgr_snapshots");
        Files.createDirectories(snapshotDir);

        // 組合完整路徑
        return snapshotDir.resolve(fileName).toString();
    }

    /**
     * Sends a final acknowledgment packet to the source (via the Keeper) to confirm
     * the result of the file reception.
     * <p>
     * 向來源 (經由 Keeper) 發送最終確認封包，以確認檔案接收的結果。
     */
    private void sendFileReceivedAck(LinkerClient lc, boolean success, String message) {
        H2ConfigSyncTransmitAckPacket ack = new H2ConfigSyncTransmitAckPacket(
                targetId, sourceId, success, message);
        lc.send(ack);
    }
}