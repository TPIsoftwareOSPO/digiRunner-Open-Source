package tpi.dgrv4.dpaa.component.apptJob;

import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.DgrH2ConfigSyncHistory;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.repository.DgrH2ConfigSyncHistoryDao;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.H2ConfigSyncServive;
import tpi.dgrv4.gateway.vo.H2ConfigSyncReq;

import tpi.dgrv4.gateway.vo.ClientKeeper;
import tpi.dgrv4.dpaa.service.TsmpSettingService;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * H2 Config Sync Scheduled Job.
 * Orchestrates a 3-step H2 database synchronization from Primary node to multiple Replica nodes.
 * <p>
 * H2 設定同步排程工作。
 * 負責協調從主要節點 (Primary) 到多個複本節點 (Replica) 的 H2 資料庫同步流程。
 * <p>
 * Synchronization Steps / 同步步驟：
 * <ol>
 *   <li>Step 1: Compress source database / 壓縮來源資料庫</li>
 *   <li>Step 2: Transmit file to target nodes / 傳送檔案至目標節點</li>
 *   <li>Step 3: Replace database and reset connection / 替換資料庫並重置連線</li>
 * </ol>
 * <p>
 * Features / 主要功能：
 * <ul>
 *   <li>Monitors progress and updates job status / 監控進度並更新工作狀態</li>
 *   <li>Validates all Replica nodes are online before execution / 執行前檢查所有 Replica 節點是否在線</li>
 *   <li>Writes sync history to DGR_H2_CONFIG_SYNC_HISTORY for unified management / 同步記錄寫入統一歷史表</li>
 *   <li>Configurable timeout via TSMP_SETTING.H2_SYNC_TIMEOUT_MINUTES (default: 30 min) / 可設定逾時時間</li>
 * </ul>
 */
public class H2ConfigSyncReplica extends ApptJob {
    private static final String SKIPPED = "SKIPPED";
    private static final String STEP_0_INIT = "0/3: Init";
    private static final String STEP_1_INIT = "1/3: 0/1";
    private static final String STEP_1_DONE = "1/3: 1/1";
    private static final String STEP_1_ERROR = "1/3: ERROR";
    private static final String STEP_2_ERROR = "2/3: ERROR";
    private static final String STEP_3_ERROR = "3/3: ERROR";
    private static final String STEP_2_FORMAT = "2/3: %d/%d";
    private static final String STEP_3_FORMAT = "3/3: %d/%d";
    private static final int CHECK_INTERVAL_SECONDS = 5;
    private final transient H2ConfigSyncServive dbSyncService;
    @Getter(AccessLevel.PROTECTED)
    private final transient DgrH2ConfigSyncHistoryDao dgrDbSyncManualDao;
    @Getter(AccessLevel.PROTECTED)
    private final transient TsmpSettingService tsmpSettingService;
    /**
     * The total number of target replicas for the current sync job.
     * 當前同步作業的目標複本總數。
     */
    private int totalTargets = 0;

    /**
     * Schedule sync record for unified history management.
     * 排程同步記錄，用於統一歷史記錄管理。
     */
    private transient DgrH2ConfigSyncHistory scheduleRecord;

    @Autowired
    public H2ConfigSyncReplica(TsmpDpApptJob tsmpDpApptJob,
                               ApptJobDispatcher apptJobDispatcher,
                               TsmpDpApptJobDao tsmpDpApptJobDao,
                               H2ConfigSyncServive h2ConfigSyncServive,
                               DgrH2ConfigSyncHistoryDao dgrDbSyncManualDao,
                               TsmpSettingService tsmpSettingService) {
        super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
        this.dbSyncService = h2ConfigSyncServive;
        this.dgrDbSyncManualDao = dgrDbSyncManualDao;
        this.tsmpSettingService = tsmpSettingService;
    }


    private int getTimeoutMinutes() {
        return getTsmpSettingService().getVal_H2_SYNC_TIMEOUT_MINUTES();
    }


    private int getMaxRetries() {
        return getTimeoutMinutes() * 60 / CHECK_INTERVAL_SECONDS;
    }


    private String getTimeoutMessage() {
        return String.format("Timeout (%d min)", getTimeoutMinutes());
    }

    @Override
    public String runApptJob() throws Exception {
        // Prevent concurrent execution.
        // 防止重複執行。
        Long id = this.getTsmpDpApptJob().getApptJobId();
        boolean hasJobRunning = getTsmpDpApptJobDao()
                .existsByRefItemNoAndStatusAndApptJobIdNot("H2_CONFIG_SYNC", "R", id);
        boolean hasMaualJobRunning = getDgrDbSyncManualDao().existsByStatus(DgrH2ConfigSyncHistoryDao.H2ConfigSyncStatus.RUNNING.getStatus());
        if (hasJobRunning||hasMaualJobRunning) {
            step(SKIPPED);
            TPILogger.tl.warn("Another DB sync job is already running. Skipping this execution.");
            return SKIPPED;
        }

        TPILogger.tl.info("Starting DB sync scheduled job.");

        String primaryId = TPILogger.tl.getPrimaryId();
        String replicaIds = TPILogger.tl.getReplicaIds();

        // Validate configuration.
        // 驗證配置。
        if (!StringUtils.hasText(primaryId) || !StringUtils.hasText(replicaIds)) {
            String errorMsg = "DB sync configuration for primary or replica IDs is missing.";
            step("ERROR");
            TPILogger.tl.error(errorMsg);
            throw new TsmpDpAaException(errorMsg);
        }

        // Parse the list of target replica IDs.
        // 解析目標複本 ID 列表。
        List<String> targetIdList = Arrays.stream(replicaIds.split(","))
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();

        if (targetIdList.isEmpty()) {
            step(SKIPPED);
            TPILogger.tl.warn("No valid replica IDs found in configuration. Skipping sync job.");
            return SKIPPED;
        }

        // Check if all replica nodes are online before starting sync.
        // 在開始同步之前，檢查所有 replica 節點是否都在線。
        List<String> offlineNodes = getOfflineNodes(targetIdList);
        if (!offlineNodes.isEmpty()) {
            step(SKIPPED);
            TPILogger.tl.warn(String.format("Some replica nodes are offline: %s. Skipping sync job.", offlineNodes));
            return SKIPPED;
        }

        totalTargets = targetIdList.size();

        TPILogger.tl.info(String.format("DB sync source: %s, Targets: %s (%d total).",
                primaryId, targetIdList, totalTargets));

        try {
            scheduleRecord = new DgrH2ConfigSyncHistory();
            scheduleRecord.setSyncType(DgrH2ConfigSyncHistoryDao.SyncType.SCHEDULE.name());
            scheduleRecord.setScheduleId(id);
            scheduleRecord.setSourceId(primaryId);
            scheduleRecord.setTargetIds(String.join(",", targetIdList));
            scheduleRecord.setStatus(DgrH2ConfigSyncHistoryDao.H2ConfigSyncStatus.RUNNING.getStatus());
            scheduleRecord.setCurrentStep(STEP_0_INIT);
            scheduleRecord.setProgress(0);
            scheduleRecord.setStartTime(DateTimeUtil.now());
            scheduleRecord.setCreateUser("SYSTEM");
            scheduleRecord.setCreateDateTime(DateTimeUtil.now());

            scheduleRecord = getDgrDbSyncManualDao().save(scheduleRecord);

            TPILogger.tl.info(String.format("[Schedule Sync] Record created: ID=%d, JobID=%d",
                    scheduleRecord.getSyncId(), scheduleRecord.getScheduleId()));

            // Step 0: Initialization.
            // 步驟 0：初始化。
            step(STEP_0_INIT);
            updateScheduleProgress(STEP_0_INIT, 0);
            TPILogger.tl.info("Initializing 3-step auto sync request.");

            H2ConfigSyncReq req = new H2ConfigSyncReq();
            req.setSourceId(primaryId);
            req.setTargetIds(targetIdList);

            dbSyncService.syncAndResetH2Config(req);

            // Step 1: Compression.
            // 步驟 1：壓縮。
            step(STEP_1_INIT);
            updateScheduleProgress(STEP_1_INIT, 10);
            TPILogger.tl.info("Executing Step 1: Compressing database on source.");

            String errorMsg = waitForStep1();
            if (errorMsg != null) {
                step(STEP_1_ERROR);
                updateScheduleProgress(STEP_1_ERROR, 10);
                TPILogger.tl.error("Step 1 (Compression) failed: " + errorMsg);
                throw new TsmpDpAaException("Step 1 failed: " + errorMsg);
            }

            step(STEP_1_DONE);
            updateScheduleProgress(STEP_1_DONE, 33);
            TPILogger.tl.info("Step 1 (Compression) completed successfully.");

            // Step 2: Transmission (with progress tracking).
            // 步驟 2：傳送檔案（並追蹤進度）。
            String step2Init = String.format(STEP_2_FORMAT, 0, totalTargets);
            step(step2Init);
            updateScheduleProgress(step2Init, 33);
            TPILogger.tl.info(String.format("Executing Step 2: Transmitting to %d targets.", totalTargets));

            errorMsg = waitForStep2();
            if (errorMsg != null) {
                step(STEP_2_ERROR);
                updateScheduleProgress(STEP_2_ERROR, 33);
                TPILogger.tl.error("Step 2 (Transmission) failed: " + errorMsg);
                throw new TsmpDpAaException("Step 2 failed: " + errorMsg);
            }

            String step2Done = String.format(STEP_2_FORMAT, totalTargets, totalTargets);
            step(step2Done);
            updateScheduleProgress(step2Done, 66);
            TPILogger.tl.info("Step 2 (Transmission) completed for all targets.");

            // Step 3: Backup, replace DB, and reset connection (with progress tracking).
            // 步驟 3：備份替換資料庫、重製連線（並追蹤進度）。
            String step3Init = String.format(STEP_3_FORMAT, 0, totalTargets);
            step(step3Init);
            updateScheduleProgress(step3Init, 66);
            TPILogger.tl.info(String.format("Executing Step 3: Replacing database on %d targets.", totalTargets));

            errorMsg = waitForStep3();
            if (errorMsg != null) {
                step(STEP_3_ERROR);
                updateScheduleProgress(STEP_3_ERROR, 66);
                TPILogger.tl.error("Step 3 (DB Replacement) failed: " + errorMsg);
                throw new TsmpDpAaException("Step 3 failed: " + errorMsg);
            }

            String step3Done = String.format(STEP_3_FORMAT, totalTargets, totalTargets);
            step(step3Done);
            updateScheduleProgress(step3Done, 100);
            TPILogger.tl.info("Step 3 (DB Replacement) completed for all targets.");

            Date endTime = DateTimeUtil.now();
            Long duration = (endTime.getTime() - scheduleRecord.getStartTime().getTime()) / 1000;

            scheduleRecord.setStatus(DgrH2ConfigSyncHistoryDao.H2ConfigSyncStatus.DONE.getStatus());
            scheduleRecord.setEndTime(endTime);
            scheduleRecord.setDuration(duration);
            scheduleRecord.setUpdateDateTime(DateTimeUtil.now());

            getDgrDbSyncManualDao().save(scheduleRecord);

            TPILogger.tl.info(String.format("[Schedule Sync] Completed: ID=%d, Duration=%d seconds",
                    scheduleRecord.getSyncId(), duration));

            TPILogger.tl.info("DB sync job completed successfully.");

            return "SUCCESS";

        } catch (Exception e) {
            if (scheduleRecord != null) {
                Date endTime = DateTimeUtil.now();
                Long duration = (endTime.getTime() - scheduleRecord.getStartTime().getTime()) / 1000;

                scheduleRecord.setStatus(DgrH2ConfigSyncHistoryDao.H2ConfigSyncStatus.ERROR.getStatus());
                scheduleRecord.setErrorMessage(e.getMessage());
                scheduleRecord.setEndTime(endTime);
                scheduleRecord.setDuration(duration);
                scheduleRecord.setUpdateDateTime(DateTimeUtil.now());

                getDgrDbSyncManualDao().save(scheduleRecord);

                TPILogger.tl.error(String.format("[Schedule Sync] Failed: ID=%d, Error=%s",
                        scheduleRecord.getSyncId(), e.getMessage()));
            }

            TPILogger.tl.error("DB sync job failed. " + StackTraceUtil.logStackTrace(e));
            throw e;
        }
    }

    /**
     * Update schedule sync progress to database.
     * 更新排程同步進度到資料庫。
     *
     * @param step Current step description
     * @param progress Progress percentage (0-100)
     */
    private void updateScheduleProgress(String step, int progress) {
        if (scheduleRecord != null) {
            scheduleRecord.setCurrentStep(step);
            scheduleRecord.setProgress(progress);
            scheduleRecord.setUpdateDateTime(DateTimeUtil.now());
            getDgrDbSyncManualDao().save(scheduleRecord);

            TPILogger.tl.debug(String.format("[Schedule Sync] Progress: ID=%d, %d%% - %s",
                    scheduleRecord.getSyncId(), progress, step));
        }
    }

    /**
     * Waits for step 1 (compression) to complete by polling the status from DbSyncService.
     * 透過輪詢 DbSyncService 的狀態來等待步驟 1 (壓縮) 完成。
     *
     * @return null if successful, or an error message if failed or timed out.
     * @throws InterruptedException if the thread is interrupted.
     */
    private String waitForStep1() throws InterruptedException {
        int maxRetries = getMaxRetries();
        int retries = 0;

        while (retries < maxRetries) {
            String errorMsg = dbSyncService.getErrorMessage();
            if (errorMsg != null) {
                return errorMsg;
            }

            if (dbSyncService.isStep1Complete()) {
                return null;
            }

            Thread.sleep(CHECK_INTERVAL_SECONDS * 1000L);
            retries++;

            if (retries % 12 == 0) {
                TPILogger.tl.info(String.format("Waiting for step 1 to complete. Elapsed time: %d/%d min.",
                        retries / 12, maxRetries / 12));
            }
        }

        return getTimeoutMessage();
    }

    /**
     * Waits for step 2 (transmission) to complete for all targets.
     * It polls the status and updates the job progress when changes are detected.
     * 等待所有目標的步驟 2 (傳輸) 完成。此方法會輪詢狀態，並在偵測到變更時更新工作進度。
     *
     * @return null if successful, or an error message if failed or timed out.
     * @throws InterruptedException if the thread is interrupted.
     */
    private String waitForStep2() throws InterruptedException {
        int maxRetries = getMaxRetries();
        int retries = 0;
        int lastCompletedCount = 0;

        while (retries < maxRetries) {
            // Check for a global error message from the service.
            // 檢查服務中是否有全域錯誤訊息。
            String errorMsg = dbSyncService.getErrorMessage();
            if (errorMsg != null) {
                return errorMsg;
            }

            // Get the current completion status for each target.
            // 獲取每個目標的當前完成狀態。
            Map<String, Boolean> status = dbSyncService.getStep2TargetStatus();
            int completedCount = (int) status.values().stream()
                    .filter(Boolean::booleanValue).count();

            // Update progress and log if the number of completed targets has changed.
            // 如果已完成的目標數量發生變化，則更新進度並記錄日誌。
            if (completedCount != lastCompletedCount) {
                String step2Progress = String.format(STEP_2_FORMAT, completedCount, totalTargets);
                step(step2Progress);

                int progress = 33 + (completedCount * 32 / totalTargets);
                updateScheduleProgress(step2Progress, progress);

                TPILogger.tl.info(String.format("Step 2 progress: %d/%d targets completed.", completedCount, totalTargets));
                lastCompletedCount = completedCount;
            }

            if (dbSyncService.isStep2Complete()) {
                return null;
            }

            Thread.sleep(CHECK_INTERVAL_SECONDS * 1000L);
            retries++;

            if (retries % 12 == 0) {
                TPILogger.tl.info(String.format("Waiting for step 2 to complete. Elapsed time: %d/%d min. Progress: %d/%d.",
                        retries / 12, maxRetries / 12, completedCount, totalTargets));
            }
        }

        return getTimeoutMessage();
    }

    /**
     * Waits for step 3 (DB replacement and connection reset) to complete for all targets.
     * It polls the status and updates the job progress when changes are detected.
     * 等待所有目標的步驟 3 (資料庫替換與連線重製) 完成。此方法會輪詢狀態，並在偵測到變更時更新工作進度。
     *
     * @return null if successful, or an error message if failed or timed out.
     * @throws InterruptedException if the thread is interrupted.
     */
    private String waitForStep3() throws InterruptedException {
        int maxRetries = getMaxRetries();
        int retries = 0;
        int lastCompletedCount = 0;

        while (retries < maxRetries) {
            // Check for a global error message from the service.
            // 檢查服務中是否有全域錯誤訊息。
            String errorMsg = dbSyncService.getErrorMessage();
            if (errorMsg != null) {
                return errorMsg;
            }

            // Get the current completion status for each target.
            // 獲取每個目標的當前完成狀態。
            Map<String, Boolean> status = dbSyncService.getStep3TargetStatus();
            int completedCount = (int) status.values().stream()
                    .filter(Boolean::booleanValue).count();

            // Update progress and log if the number of completed targets has changed.
            // 如果已完成的目標數量發生變化，則更新進度並記錄日誌。
            if (completedCount != lastCompletedCount) {
                String step3Progress = String.format(STEP_3_FORMAT, completedCount, totalTargets);
                step(step3Progress);

                int progress = 66 + (completedCount * 34 / totalTargets);
                updateScheduleProgress(step3Progress, progress);

                TPILogger.tl.info(String.format("Step 3 progress: %d/%d targets completed.", completedCount, totalTargets));
                lastCompletedCount = completedCount;
            }

            if (dbSyncService.isStep3Complete()) {
                return null;
            }

            Thread.sleep(CHECK_INTERVAL_SECONDS * 1000L);
            retries++;

            if (retries % 12 == 0) {
                TPILogger.tl.info(String.format("Waiting for step 3 to complete. Elapsed time: %d/%d min. Progress: %d/%d.",
                        retries / 12, maxRetries / 12, completedCount, totalTargets));
            }
        }

        return getTimeoutMessage();
    }

    /**
     * Get the list of offline nodes from the target list.
     * 從目標列表中取得離線節點列表。
     *
     * @param targetIds List of target node IDs to check.
     * @return List of offline node IDs, empty if all nodes are online.
     */
    @SuppressWarnings("unchecked")
    private List<String> getOfflineNodes(List<String> targetIds) {
        List<ClientKeeper> allClientList = (List<ClientKeeper>) TPILogger.lc.paramObj.get("allClientList");

        Set<String> onlineClients = (allClientList != null)
                ? allClientList.stream().map(ClientKeeper::getUsername).collect(Collectors.toSet())
                : Set.of();

        return targetIds.stream()
                .filter(targetId -> !isNodeOnline(targetId, onlineClients)).toList();

    }

    /**
     * Check if a node is online.
     * 檢查節點是否在線。
     *
     * @param nodeId The node ID to check.
     * @param onlineClients Set of online client usernames.
     * @return true if the node is online, false otherwise.
     */
    private boolean isNodeOnline(String nodeId, Set<String> onlineClients) {
        return onlineClients.stream().anyMatch(client -> client.contains(nodeId));
    }
}