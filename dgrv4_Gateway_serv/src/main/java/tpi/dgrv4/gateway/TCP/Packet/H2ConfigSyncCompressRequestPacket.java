package tpi.dgrv4.gateway.TCP.Packet;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.constant.H2ConfigSyncEnum;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.H2ConfigSyncServive;
import tpi.dgrv4.tcp.utils.communication.CommunicationServer;
import tpi.dgrv4.gateway.utils.BeanUtil;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.LinkerServer;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * Packet sent from the Keeper to a Source node to request the start of a database compression (Step 1).
 * The Source node will perform the compression in a background thread upon receiving this packet.
 * <p>
 * 從 Keeper 發送到來源 (Source) 節點的封包，用於請求開始資料庫壓縮 (步驟 1)。
 * 來源節點在收到此封包後，將在背景執行緒中執行壓縮。
 */
public class H2ConfigSyncCompressRequestPacket implements Packet_i {
    private static final long serialVersionUID = 1L;

    /**
     * The ID of the source node to perform compression on. / 將執行壓縮的來源節點 ID。
     */
    public String sourceId;
    /**
     * The role of the source node (e.g., PRIMARY). / 來源節點的角色 (例如 PRIMARY)。
     */
    public String sourceRole;

    public H2ConfigSyncCompressRequestPacket() {
    }

    /**
     * Constructs a new DbSyncCompressRequestPacket.
     *
     * @param sourceId   The ID of the source node.
     * @param sourceRole The role of the source node.
     */
    public H2ConfigSyncCompressRequestPacket(String sourceId, String sourceRole) {
        this.sourceId = sourceId;
        this.sourceRole = sourceRole;
    }

    @Override
    public void runOnServer(LinkerServer ls) {
        // This runs on the Keeper, which forwards the request to the target Source client.
        // 這在 Keeper 上運行，它將請求轉發給目標來源 (Source) 客戶端。
        TPILogger.tl.info(String.format("Forwarding Step 1 (Compress) request to source: %s.", sourceId));

        synchronized (CommunicationServer.cs.connClinet) {
            for (LinkerServer client : CommunicationServer.cs.connClinet) {
                if (client.userName != null && client.userName.contains(sourceId)) {
                    client.send(this);
                    TPILogger.tl.info(String.format("Request sent to client: %s.", client.userName));
                    return;
                }
            }
            TPILogger.tl.error(String.format("Source client not found: %s.", sourceId));
            throw new  RuntimeException(String.format("Source client not found: %s.", sourceId));
        }
    }

    @Override
    public void runOnClient(LinkerClient lc) {
        // This runs on the Source node, which performs the compression in a background thread.
        // 這在來源 (Source) 節點上運行，它在背景執行緒中執行壓縮。
        new Thread(() -> {
            try {
                TPILogger.tl.info(String.format("Starting Step 1: Compress database (Copy & Compact) for source: %s (%s).", sourceId, sourceRole));

                long startTime = System.currentTimeMillis();

                // 複製檔案後壓縮
                String compressedFilePath = copyAndCompact(sourceRole);

                long elapsed = (System.currentTimeMillis() - startTime) / 1000;

                if (compressedFilePath == null) {
                    throw new Exception("Compression failed: returned null");
                }

                // 取得壓縮後的檔案大小
                File compressedFile = new File(compressedFilePath);
                long fileSize = compressedFile.length();

                TPILogger.tl.info(String.format("Compression complete. Path: %s, Size: %.2f MB.", compressedFilePath, fileSize / 1024.0 / 1024.0));
                TPILogger.tl.info(String.format("Compression took %d seconds.", elapsed));

                // 發送完成通知
                H2ConfigSyncCompressCompletePacket completePacket = new H2ConfigSyncCompressCompletePacket(
                        sourceId,
                        true,
                        compressedFilePath,
                        fileSize,
                        fileSize,
                        null
                );

                lc.send(completePacket);

                TPILogger.tl.info("Sent Step 1 completion notification to Keeper.");

            } catch (Exception e) {
                TPILogger.tl.error("Step 1 (Compression) failed: " + StackTraceUtil.logStackTrace(e));

                // 發送失敗通知
                try {
                    H2ConfigSyncCompressCompletePacket errorPacket = new H2ConfigSyncCompressCompletePacket(
                            sourceId,
                            false,
                            null,
                            0,
                            0,
                            e.getMessage()
                    );
                    lc.send(errorPacket);
                } catch (Exception sendError) {
                    TPILogger.tl.error("Failed to send error notification to Keeper: " + sendError.getMessage());
                }
            }
        }, "Step1-Compress-" + sourceId).start();
    }

    /**
     * Performs the database compression by creating a copy and running SHUTDOWN COMPACT on it.
     * This avoids locking the live database.
     * <p>
     * 透過建立複本並對其執行 SHUTDOWN COMPACT 來執行資料庫壓縮。
     * 這避免了鎖定正在運行的資料庫。
     */
    private String copyAndCompact(String role) throws Exception {
        H2ConfigSyncServive h2ConfigSyncServive = BeanUtil.getBean(H2ConfigSyncServive.class);
        TPILogger.tl.info("Starting copy and compact process.");

        // 1. 取得原始 DB 路徑
        String h2ConfigPathStr = H2ConfigSyncEnum.PRIMARY.name().equalsIgnoreCase(role)
                ? TPILogger.tl.getPrimaryH2ConfigPath() : TPILogger.tl.getReplicaH2ConfigPath();

        Path originalH2ConfigPath = Paths.get(h2ConfigPathStr).toAbsolutePath().normalize();
        Path parentDir = originalH2ConfigPath.getParent();
        Path snapshotDir = parentDir.resolve("dgr_snapshots");
        Files.createDirectories(snapshotDir);
        //清理
        TPILogger.tl.info("Step 0/4: Cleaning up old copy files...");
        cleanupOldFiles(snapshotDir, "copy_*.mv.db");
        h2ConfigSyncServive.updateStep1Progress(5);

        String timestamp = String.valueOf(System.currentTimeMillis());
        String copyFileName = "copy_" + timestamp + ".mv.db";
        Path copyH2ConfigPath = snapshotDir.resolve(copyFileName);

        // 移除 .mv.db 後綴，取得資料庫名稱
        String originalH2ConfigName = originalH2ConfigPath.toString().replace(".mv.db", "");
        String copyH2ConfigName = copyH2ConfigPath.toString().replace(".mv.db", "");

        TPILogger.tl.info("Original database path: " + originalH2ConfigPath);
        TPILogger.tl.info("Temporary copy path: " + copyH2ConfigPath);

        // 2. 對原資料庫執行 CHECKPOINT（確保資料完整）
        TPILogger.tl.info("Step 1/4: Executing CHECKPOINT on original database.");

        // 從應用程式上下文取得 DataSource，以避免硬編碼連線資訊
        // Get the DataSource from the application context to avoid hardcoding connection details.
        DataSource dataSource = BeanUtil.getBean(DataSource.class);
        //CHECKPOINT
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CHECKPOINT SYNC");
            TPILogger.tl.info("CHECKPOINT SYNC complete.");
            h2ConfigSyncServive.updateStep1Progress(15);
        }

        // 等待寫入完成
        Thread.sleep(1000);

        // 3. 記錄原始檔案大小
        long beforeSize = Files.size(originalH2ConfigPath);
        TPILogger.tl.info(String.format("Original file size: %.2f MB.", beforeSize / 1024.0 / 1024.0));
        // 4. 複製檔案到子資料夾
        TPILogger.tl.info("Step 2/4: Copying database file.");

        Files.copy(originalH2ConfigPath, copyH2ConfigPath, StandardCopyOption.REPLACE_EXISTING);

        long copySize = Files.size(copyH2ConfigPath);
        TPILogger.tl.info(String.format("File copy complete. Size: %.2f MB.", copySize / 1024.0 / 1024.0));
        h2ConfigSyncServive.updateStep1Progress(30);
        // 5. 對複製的檔案執行 SHUTDOWN COMPACT
        TPILogger.tl.info("Step 3/4: Executing SHUTDOWN COMPACT on copied database.");

        String fileUrl = String.format(
                "jdbc:h2:%s;NON_KEYWORDS=VALUE;Mode=MySQL",
                copyH2ConfigName
        );

        TPILogger.tl.info("Connecting to copied database via File Mode: " + fileUrl);

        // 從主 DataSource 獲取憑證，避免硬編碼
        // Get credentials from the main DataSource to avoid hardcoding.
        HikariDataSource mainDataSource = BeanUtil.getBean(HikariDataSource.class);
        String username = mainDataSource.getUsername();
        String password = mainDataSource.getPassword();

        try (Connection copyConn = DriverManager.getConnection(fileUrl, username, password);
             Statement copyStmt = copyConn.createStatement()) {

            TPILogger.tl.info("Connected to copied database");

            // 清理複製資料庫中的「執行中」狀態記錄，避免備援資料庫啟動時卡住
            // Clear "RUNNING" status records in the copied database to prevent
            // the replica from being stuck in sync-running state after failover.
            try {
                // 1. 清理同步歷史表 DGR_H2_CONFIG_SYNC_HISTORY
                int updatedRows = copyStmt.executeUpdate(
                    "UPDATE DGR_H2_CONFIG_SYNC_HISTORY SET STATUS = 'E', " +
                    "ERROR_MESSAGE = 'Interrupted: cleared during sync snapshot' " +
                    "WHERE STATUS = 'R'"
                );
                if (updatedRows > 0) {
                    TPILogger.tl.info(String.format(
                        "Cleared %d running sync history record(s) in copied database.", updatedRows));
                }

                // 2. 清理排程表 TSMP_DP_APPT_JOB 中 H2_CONFIG_SYNC 的執行中記錄
                int updatedJobRows = copyStmt.executeUpdate(
                    "UPDATE TSMP_DP_APPT_JOB SET STATUS = 'E', " +
                    "EXEC_RESULT = 'Interrupted: cleared during sync snapshot' " +
                    "WHERE REF_ITEM_NO = 'H2_CONFIG_SYNC' AND STATUS = 'R'"
                );
                if (updatedJobRows > 0) {
                    TPILogger.tl.info(String.format(
                        "Cleared %d running H2_CONFIG_SYNC job record(s) in copied database.", updatedJobRows));
                }
            } catch (Exception e) {
                // 如果表不存在或其他錯誤，記錄警告但繼續執行
                TPILogger.tl.warn("Warning while clearing sync status: " + e.getMessage());
            }

            // 執行 CHECKPOINT
            try {
                copyStmt.execute("CHECKPOINT");
                TPILogger.tl.info("CHECKPOINT on copy complete.");
            } catch (Exception e) {
                TPILogger.tl.warn("CHECKPOINT warning: " + e.getMessage());
            }

            // 執行 SHUTDOWN COMPACT（只關閉複製的資料庫）
            TPILogger.tl.info("Executing SHUTDOWN COMPACT on copy.");
            copyStmt.execute("SHUTDOWN COMPACT");
            TPILogger.tl.info("SHUTDOWN COMPACT on copy executed.");
        }

        // 6. 等待壓縮完成
        TPILogger.tl.info("Step 4/4: Waiting for compression to finalize.");
        h2ConfigSyncServive.updateStep1Progress(60);
        Thread.sleep(3000);

        // 7. 驗證壓縮結果
        if (!Files.exists(copyH2ConfigPath)) {
            throw new Exception("Compressed file not found: " + copyH2ConfigPath);
        }

        long afterSize = Files.size(copyH2ConfigPath);
        TPILogger.tl.info(String.format("Final compressed size: %.2f MB.", afterSize / 1024.0 / 1024.0));

        if (beforeSize > 0 && afterSize < beforeSize) {
            double ratio = (1.0 - (double) afterSize / beforeSize) * 100;
            TPILogger.tl.info(String.format("Space saved: %.1f%%.", ratio));
        }

        TPILogger.tl.info("Copy and compact process completed successfully.");
        TPILogger.tl.info("Original database remains online and unaffected.");
        h2ConfigSyncServive.updateStep1Progress(100);
        return copyH2ConfigPath.toString();
    }

    private void cleanupOldFiles(Path dir, String pattern) {
//        long cutoffTime = System.currentTimeMillis() - (60 * 60 * 1000); // 1小時

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, pattern)) {
            for (Path file : stream) {
                Files.delete(file);
                TPILogger.tl.info("Deleted: " + file.getFileName());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}