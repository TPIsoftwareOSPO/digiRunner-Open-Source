package tpi.dgrv4.gateway.TCP.Packet;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.constant.H2ConfigSyncEnum;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.H2ConfigControlService;
import tpi.dgrv4.gateway.service.H2ConfigSyncServive;
import tpi.dgrv4.gateway.utils.BeanUtil;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Packet sent from the Keeper to a Target node to request the start of a database replacement (Step 3).
 * The Target node will back up its current DB, replace it with the new one, and reset its connection pool.
 * <p>
 * 從 Keeper 發送到目標 (Target) 節點的封包，用於請求開始資料庫替換 (步驟 3)。
 * 目標節點將備份其當前資料庫，用新資料庫替換它，並重置其連線池。
 */
public class H2ConfigSyncReplaceRequestPacket implements Packet_i {

    /**
     * The ID of the target node to perform the replacement on. / 將執行替換的目標節點 ID。
     */
    public String targetId;
    /**
     * The role of the target node (e.g., REPLICA). / 目標節點的角色 (例如 REPLICA)。
     */
    public String targetRole;

    public H2ConfigSyncReplaceRequestPacket() {
    }

    /**
     * Constructs a new DbSyncReplaceRequestPacket.
     *
     * @param targetId   The ID of the target node.
     * @param targetRole The role of the target node.
     */
    public H2ConfigSyncReplaceRequestPacket(String targetId, String targetRole) {
        this.targetId = targetId;
        this.targetRole = targetRole;
    }

    @Override
    public void runOnServer(LinkerServer ls) {
        // This runs on the Keeper, which forwards the request to the target client.
        // 這在 Keeper 上運行，它將請求轉發給目標客戶端。
        TPILogger.tl.info(String.format("Forwarding Step 3 (H2Config Replacement) request to target: %s.", targetId));
        synchronized (CommunicationServer.cs.connClinet) {
            for (LinkerServer client : CommunicationServer.cs.connClinet) {
                if (client.userName != null && client.userName.contains(targetId)) {
                    try {
                        client.send(this);
                        TPILogger.tl.info(String.format("Request sent to client: %s.", client.userName));
                    } catch (Exception e) {
                        TPILogger.tl.error("Failed to send Step 3 request: " + StackTraceUtil.logStackTrace(e));
                    }
                    return;
                }
            }
            TPILogger.tl.error(String.format("Target client not found: %s.", targetId));
        }
    }

    @Override
    public void runOnClient(LinkerClient lc) {

        // This runs on the Target node, which performs the DB replacement in a background thread.
        // 這在目標 (Target) 節點上運行，它在背景執行緒中執行資料庫替換。
        new Thread(() -> {
            try {
                TPILogger.tl.info(String.format("Starting Step 3: Replace database for target: %s (%s).", targetId, targetRole));

                long startTime = System.currentTimeMillis();

                replaceDatabase(targetRole);

                long elapsed = (System.currentTimeMillis() - startTime) / 1000;

                TPILogger.tl.info(String.format("Database replacement took %d seconds.", elapsed));

                // 發送完成通知
                H2ConfigSyncReplaceCompletePacket completePacket = new H2ConfigSyncReplaceCompletePacket(
                        targetId,
                        true,
                        null
                );

                lc.send(completePacket);

                TPILogger.tl.info("Sent Step 3 completion notification to Keeper.");

            } catch (Exception e) {
                String errMsg = StackTraceUtil.logStackTrace(e);
                TPILogger.tl.error("Step 3 (H2Config Replacement) failed: " + errMsg);

                // 發送失敗通知
                try {
                    H2ConfigSyncReplaceCompletePacket errorPacket = new H2ConfigSyncReplaceCompletePacket(
                            targetId,
                            false,
                            errMsg
                    );
                    lc.send(errorPacket);
                } catch (Exception sendError) {
                    TPILogger.tl.error("Failed to send error notification to Keeper: " + sendError.getMessage());
                }
            }
        }, "Step3-Replace-" + targetId).start();
    }

    /**
     * The core logic for replacing the database file on a Target node.
     * It involves finding the new file, backing up the old one, suspending the data source,
     * performing the file swap, and resuming the data source.
     * <p>
     * 在目標節點上替換資料庫檔案的核心邏輯。
     * 該過程包括：尋找新檔案、備份舊檔案、暫停資料來源、執行檔案交換以及恢復資料來源。
     */
    private void replaceDatabase(String role) throws Exception {
        H2ConfigSyncServive h2ConfigSyncServive = BeanUtil.getBean(H2ConfigSyncServive.class);
        TPILogger.tl.info("Starting database replacement process.");

        // 1. 取得目標 DB 路徑
        String h2ConfigPathStr = H2ConfigSyncEnum.REPLICA.name().equalsIgnoreCase(role)
                ? TPILogger.tl.getReplicaH2ConfigPath()
                : TPILogger.tl.getPrimaryH2ConfigPath();

        Path currentH2ConfigPath = Paths.get(h2ConfigPathStr).toAbsolutePath().normalize();
        Path parentDir = currentH2ConfigPath.getParent();
        Path snapshotDir = parentDir.resolve("dgr_snapshots");

        TPILogger.tl.info("Current database path: " + currentH2ConfigPath);
        TPILogger.tl.info("Snapshot directory: " + snapshotDir);

        // 2. 尋找接收到的 .mv.db 檔案
        TPILogger.tl.info("Step 1/5: Finding received database file.");

        File receivedFile = findReceivedMvH2Config(snapshotDir);
        if (receivedFile == null || !receivedFile.exists()) {
            throw new Exception("Received .mv.db file not found in: " + snapshotDir);
        }

        long receivedSize = receivedFile.length();
        TPILogger.tl.info(String.format("Found received file: %s (Size: %.2f MB).", receivedFile.getName(), receivedSize / 1024.0 / 1024.0));
        h2ConfigSyncServive.updateStep3Progress(targetId, 10);
        // 3. 備份現有資料庫
        TPILogger.tl.info("Step 2/5: Backing up current database.");

        if (Files.exists(currentH2ConfigPath)) {
            String timestamp = DateTimeUtil.dateTimeToString(
                    new Date(), DateTimeFormatEnum.西元年月日時分_4).orElse("");


            String backupDirConfig = TPILogger.tl.getH2ConfigBackupDir();
            Path backupDir = Paths.get(backupDirConfig).toAbsolutePath();

            // 確保備份目錄存在
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }

            // 清理過期備份檔案（依設定保留天數）
            int retentionDays = TPILogger.tl.getH2ConfigBackupRetentionDays();
            cleanupExpiredBackups(backupDir, retentionDays);

            // 備份檔案名稱：dgrdb.mv.db.bak.20251229_020000
            String backupFileName = currentH2ConfigPath.getFileName() + ".bak." + timestamp;
            Path backupPath = backupDir.resolve(backupFileName);

            Files.copy(currentH2ConfigPath, backupPath, StandardCopyOption.REPLACE_EXISTING);

            long backupSize = Files.size(backupPath);
            TPILogger.tl.info(String.format("Backup created: %s (Size: %.2f MB).", backupPath.getFileName(), backupSize / 1024.0 / 1024.0));
            h2ConfigSyncServive.updateStep3Progress(targetId, 40);
        } else {
            TPILogger.tl.info("No existing database found to back up.");
        }

        // 4. 暫停 DataSource
        TPILogger.tl.info("Step 3/5: Suspending DataSource.");

        H2ConfigControlService h2ConfigControlService = BeanUtil.getBean(H2ConfigControlService.class);
        h2ConfigControlService.suspendDataSource();

        TPILogger.tl.info("DataSource suspended successfully.");

        // 等待所有連線關閉
        Thread.sleep(2000);
        h2ConfigSyncServive.updateStep3Progress(targetId, 60);
        try {
            // 5. 替換資料庫檔案
            TPILogger.tl.info("Step 4/5: Replacing database file.");

            Files.copy(
                    receivedFile.toPath(),
                    currentH2ConfigPath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            long newSize = Files.size(currentH2ConfigPath);
            TPILogger.tl.info(String.format("Database file replaced successfully. New size: %.2f MB.", newSize / 1024.0 / 1024.0));
            h2ConfigSyncServive.updateStep3Progress(targetId, 80);
            // 6. 重啟 DataSource
            TPILogger.tl.info("Step 5/5: Resuming DataSource.");

            h2ConfigControlService.resumeDataSource();

            // 等待完全啟動
            Thread.sleep(2000);

            TPILogger.tl.info("DataSource resumed successfully.");

        } catch (Exception e) {
            // 發生錯誤，嘗試恢復 DataSource
            TPILogger.tl.error("Database replacement failed. Attempting to resume DataSource to restore service. \n" + StackTraceUtil.logStackTrace(e));
            try {
                h2ConfigControlService.resumeDataSource();
                TPILogger.tl.info("DataSource resumed after error.");
            } catch (Exception resumeError) {
                TPILogger.tl.error("CRITICAL: Failed to resume DataSource after a replacement error. Manual intervention may be required. \n" + StackTraceUtil.logStackTrace(resumeError));
            }
            throw e;
        }

        // 7. 清理接收到的臨時檔案
        TPILogger.tl.info("Cleaning up temporary received file.");

        try {
            Files.deleteIfExists(receivedFile.toPath());
            TPILogger.tl.info("Temporary file deleted successfully.");
        } catch (Exception e) {
            TPILogger.tl.warn("Failed to delete temporary file: " + e.getMessage());
        }

        TPILogger.tl.info("Database replacement process completed.");
        h2ConfigSyncServive.updateStep3Progress(targetId, 100);
    }

    /**
     * Finds the most recently received database file in the snapshot directory.
     * It looks for files matching the pattern "received_*.mv.db".
     * <p>
     * 在快照目錄中尋找最新接收到的資料庫檔案。
     * 它會尋找符合 "received_*.mv.db" 模式的檔案。
     */
    private File findReceivedMvH2Config(Path snapshotDir) {
        if (!Files.exists(snapshotDir)) {
            TPILogger.tl.warn("Snapshot directory not found: " + snapshotDir);
            return null;
        }

        File dir = snapshotDir.toFile();
        File[] mvH2ConfigFiles = dir.listFiles((d, name) ->
                name.startsWith("received_") && name.endsWith(".mv.db")
        );

        if (mvH2ConfigFiles == null || mvH2ConfigFiles.length == 0) {
            TPILogger.tl.warn("No received_*.mv.db files found in snapshot directory.");
            return null;
        }

        // 如果有多個，選最新的
        File latest = mvH2ConfigFiles[0];
        for (File file : mvH2ConfigFiles) {
            if (file.lastModified() > latest.lastModified()) {
                latest = file;
            }
        }

        return latest;
    }

    /**
     * Cleans up expired backup files in the specified directory.
     * Files older than the retention period will be deleted.
     * <p>
     * 清理指定目錄中過期的備份檔案。
     * 超過保留期限的檔案將被刪除。
     *
     * @param backupDir     The backup directory path / 備份目錄路徑
     * @param retentionDays Number of days to retain backups / 備份保留天數
     */
    private void cleanupExpiredBackups(Path backupDir, int retentionDays) {
        try {
            File directory = backupDir.toFile();
            File[] files = directory.listFiles();

            if (files == null || files.length == 0) {
                return;
            }

            int deletedCount = 0;
            for (File file : files) {
                // 只處理備份檔案 (*.bak.*)
                if (file.isFile() && file.getName().contains(".bak.")) {
                    if (isFileOlderThanDays(file, retentionDays)) {
                        if (file.delete()) {
                            deletedCount++;
                            TPILogger.tl.info("Deleted expired backup: " + file.getName());
                        } else {
                            TPILogger.tl.warn("Failed to delete expired backup: " + file.getName());
                        }
                    }
                }
            }

            if (deletedCount > 0) {
                TPILogger.tl.info(String.format("Backup cleanup complete. Deleted %d expired file(s).", deletedCount));
            }

        } catch (Exception e) {
            TPILogger.tl.warn("Error during backup cleanup: " + e.getMessage());
            // 不中斷主流程，清理失敗只記錄警告
        }
    }

    /**
     * Checks if a file is older than the specified number of days.
     * <p>
     * 檢查檔案是否超過指定的天數。
     *
     * @param file The file to check / 要檢查的檔案
     * @param days Number of days / 天數
     * @return true if the file is older than the specified days / 如果檔案超過指定天數則返回 true
     */
    private boolean isFileOlderThanDays(File file, int days) {
        try {
            Path filePath = file.toPath();
            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
            Instant fileTime = attrs.lastModifiedTime().toInstant();
            LocalDateTime fileDateTime = LocalDateTime.ofInstant(fileTime, ZoneId.systemDefault());
            LocalDateTime cutoffDateTime = LocalDateTime.now().minus(days, ChronoUnit.DAYS);
            return fileDateTime.isBefore(cutoffDateTime);
        } catch (Exception e) {
            TPILogger.tl.warn("Error checking file age: " + e.getMessage());
            return false;
        }
    }
}
