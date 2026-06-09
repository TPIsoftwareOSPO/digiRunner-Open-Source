package tpi.dgrv4.dpaa.service;

import java.util.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.DgrH2ConfigSyncHistory;
import tpi.dgrv4.entity.repository.DgrH2ConfigSyncHistoryDao;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.H2ConfigSyncServive;
import tpi.dgrv4.gateway.vo.H2ConfigSyncReq;
import tpi.dgrv4.gateway.vo.DPB0303Req;
import tpi.dgrv4.gateway.vo.DPB0303Resp;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * H2 Config Sync Manual Trigger Service.
 * Provides API for manually triggering H2 database synchronization from a source node to target nodes.
 * <p>
 * H2 設定同步手動觸發服務。
 * 提供 API 讓使用者手動觸發從來源節點到目標節點的 H2 資料庫同步。
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
 *   <li>Validates source and target node IDs / 驗證來源與目標節點 ID</li>
 *   <li>Prevents concurrent execution with scheduled or other manual syncs / 防止與排程或其他手動同步同時執行</li>
 *   <li>Executes sync in background thread with progress tracking / 背景執行同步並追蹤進度</li>
 *   <li>Writes sync history to DGR_H2_CONFIG_SYNC_HISTORY / 同步記錄寫入統一歷史表</li>
 *   <li>Configurable timeout via TSMP_SETTING.H2_SYNC_TIMEOUT_MINUTES (default: 30 min) / 可設定逾時時間</li>
 * </ul>
 */
@Service
public class DPB0303Service {
    private static final int CHECK_INTERVAL_SECONDS = 5;

    @Getter(AccessLevel.PROTECTED)
    @Setter(onMethod_ = @Autowired)
    private H2ConfigSyncServive h2ConfigSyncServive;
    @Getter(AccessLevel.PROTECTED)
    @Setter(onMethod_ = @Autowired)
    private DgrH2ConfigSyncHistoryDao dgrH2ConfigSyncHistoryDao;
    @Getter(AccessLevel.PROTECTED)
    @Setter(onMethod_ = @Autowired)
    private TsmpDpApptJobDao tsmpDpApptJobDao;
    @Getter(AccessLevel.PROTECTED)
    @Setter(onMethod_ = @Autowired)
    private TsmpSettingService tsmpSettingService;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Map<Long, SyncTask> runningTasks = new ConcurrentHashMap<>();


    private int getTimeoutMinutes() {
        return getTsmpSettingService().getVal_H2_SYNC_TIMEOUT_MINUTES();
    }


    private int getMaxRetries() {
        return getTimeoutMinutes() * 60 / CHECK_INTERVAL_SECONDS;
    }


    private String getTimeoutMessage() {
        return String.format("Timeout (%d min)", getTimeoutMinutes());
    }


    public DPB0303Resp triggerManualSync(TsmpAuthorization auth, DPB0303Req request) {
        try {

            if (isScheduleRunning()) {
                return DPB0303Resp.fail("A scheduled sync is running.");
            }
            List<DgrH2ConfigSyncHistory> runningManual = getDgrH2ConfigSyncHistoryDao().findByStatus(DgrH2ConfigSyncHistoryDao.H2ConfigSyncStatus.RUNNING.getStatus());
            if (!runningManual.isEmpty()) {
                return DPB0303Resp.fail("A manual sync is running.");
            }

            validateRequest(request);
            DgrH2ConfigSyncHistory history =writeDb(request, auth);

            Long syncId = history.getSyncId();

            SyncTask task = new SyncTask();
            task.syncId = syncId;
            task.history = history;
            task.targetIds = new ArrayList<>(request.getTargetIds());
            task.totalTargets = request.getTargetIds().size();
            runningTasks.put(syncId, task);

            TPILogger.tl.info("Sync ID: " + syncId);
            TPILogger.tl.info("Source: " + request.getSourceId());
            TPILogger.tl.info("Targets: " + request.getTargetIds());


            excute(task);
            return DPB0303Resp.success(syncId);

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }

    protected void excute(SyncTask task) {
        executor.submit(() -> executeSyncTask(task));
    }


    private DgrH2ConfigSyncHistory writeDb(DPB0303Req request, TsmpAuthorization auth) {
        DgrH2ConfigSyncHistory history =new DgrH2ConfigSyncHistory();
        history.setSourceId(request.getSourceId());
        history.setSyncType(DgrH2ConfigSyncHistoryDao.SyncType.MANUAL.name());
        history.setTargetIds(String.join(",", request.getTargetIds()));
        history.setStatus(DgrH2ConfigSyncHistoryDao.H2ConfigSyncStatus.RUNNING.getStatus());
        history.setCurrentStep("0/3: Init");  //
        history.setProgress(0);
        history.setStartTime(new Date());
        history.setCreateUser(auth.getUserName());

        history = getDgrH2ConfigSyncHistoryDao().save(history);
        return history;
    }

    /**
     * Executes the sync task in background thread.
     * Optimized to reduce DB writes by only updating at key checkpoints.
     * <p>
     * 背景執行同步任務。
     * 優化：減少 DB 寫入，只在關鍵節點更新。
     *
     * @param task The sync task to execute / 要執行的同步任務
     */
    private void executeSyncTask(SyncTask task) {
        DgrH2ConfigSyncHistory history =task.history;

        try {
            TPILogger.tl.info(String.format("[%d] Starting synchronization...", task.syncId));

            H2ConfigSyncReq req = new H2ConfigSyncReq();
            req.setSourceId(history.getSourceId());
            req.setTargetIds(task.targetIds);

            // Call sync service / 呼叫同步服務
            getH2ConfigSyncServive().syncAndResetH2Config(req);

            // ========== Step 1: Compress database / 壓縮資料庫 ==========
            updateProgress(history, "1/3: 0/1", 10);
            TPILogger.tl.info(String.format("[%d] Step 1 started", task.syncId));

            String error = waitForStep1(task);
            if (error != null) throw new TsmpDpAaException("Step 1 failed: " + error);

            updateProgress(history, "1/3: 1/1", 33);
            TPILogger.tl.info(String.format("[%d] Step 1 complete", task.syncId));

            // ========== Step 2: Transmit file / 傳送檔案 ==========
            updateProgress(history, String.format("2/3: 0/%d", task.totalTargets), 33);
            TPILogger.tl.info(String.format("[%d] Step 2 started", task.syncId));

            error = waitForStep2(task);
            if (error != null) throw new TsmpDpAaException("Step 2 failed: " + error);

            TPILogger.tl.info(String.format("[%d] Step 2 complete", task.syncId));

            // ========== Step 3: Replace database / 替換資料庫 ==========
            updateProgress(history, String.format("3/3: 0/%d", task.totalTargets), 66);
            TPILogger.tl.info(String.format("[%d] Step 3 started", task.syncId));

            error = waitForStep3(task);
            if (error != null) throw new TsmpDpAaException("Step 3 failed: " + error);

            updateProgress(history, String.format("3/3: %d/%d", task.totalTargets, task.totalTargets), 100);
            TPILogger.tl.info(String.format("[%d] Step 3 complete", task.syncId));

            // ========== Complete / 完成 ==========
            Date endTime = DateTimeUtil.now();
            Long duration = ((endTime.getTime() - history.getStartTime().getTime()) / 1000);

            history.setStatus(DgrH2ConfigSyncHistoryDao.H2ConfigSyncStatus.DONE.getStatus());
            history.setEndTime(endTime);
            history.setDuration(duration);
            history.setUpdateDateTime(DateTimeUtil.now());

            getDgrH2ConfigSyncHistoryDao().save(history);

            TPILogger.tl.info(String.format("[%d] Sync completed (Duration: %d seconds)", task.syncId, duration));

        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            Date endTime = DateTimeUtil.now();
            Long duration = ((endTime.getTime() - history.getStartTime().getTime()) / 1000);

            history.setStatus(DgrH2ConfigSyncHistoryDao.H2ConfigSyncStatus.ERROR.getStatus());
            history.setErrorMessage(e.getMessage());
            history.setEndTime(endTime);
            history.setDuration(duration);
            history.setUpdateDateTime(DateTimeUtil.now());

            getDgrH2ConfigSyncHistoryDao().save(history);

            TPILogger.tl.error(String.format("[%d] Sync failed: %s", task.syncId, StackTraceUtil.logStackTrace(e)));

            // 重設中斷旗標，讓上層呼叫者知道執行緒已被中斷
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

        } finally {
            runningTasks.remove(task.syncId);
            TPILogger.tl.info("===========================================");
        }
    }

    /**
     * Updates sync progress to database.
     * Note: Only called at key checkpoints to minimize DB writes.
     * <p>
     * 更新同步進度到資料庫。
     * 注意：只在關鍵節點呼叫此方法以減少 DB 寫入。
     *
     * @param history  The sync history record / 同步歷史記錄
     * @param step     Current step description / 當前步驟描述
     * @param progress Progress percentage (0-100) / 進度百分比
     */
    private void updateProgress(DgrH2ConfigSyncHistory history, String step, int progress) {
        history.setCurrentStep(step);
        history.setProgress(progress);
        history.setUpdateDateTime(DateTimeUtil.now());
        getDgrH2ConfigSyncHistoryDao().save(history);

        TPILogger.tl.debug(String.format("[%d] Progress: %d%% - %s",
                history.getSyncId(), progress, step));
    }

    /**
     * Waits for Step 1 (compression) to complete by polling.
     * Does not write to DB, only polls for status.
     * <p>
     * 等待步驟 1（壓縮）完成。
     * 只輪詢狀態，不寫入 DB。
     *
     * @param task The sync task / 同步任務
     * @return null if successful, error message if failed or timed out / 成功返回 null，失敗或逾時返回錯誤訊息
     * @throws InterruptedException if thread is interrupted / 執行緒被中斷時拋出
     */
    private String waitForStep1(SyncTask task) throws InterruptedException {
        int maxRetries = getMaxRetries();

        for (int i = 0; i < maxRetries; i++) {
            String error = getH2ConfigSyncServive().getErrorMessage();
            if (error != null) return error;

            if (getH2ConfigSyncServive().isStep1Complete()) {
                return null;
            }

            if (i % 12 == 0) {
                TPILogger.tl.info(String.format("[%d] Step 1 in progress... (%d/%d min)",
                        task.syncId, i / 12, maxRetries / 12));
            }

            Thread.sleep(CHECK_INTERVAL_SECONDS * 1000L);
        }

        return getTimeoutMessage();
    }

    /**
     * Waits for Step 2 (transmission) to complete for all targets.
     * Updates DB only when target completion count changes.
     * <p>
     * 等待步驟 2（傳輸）完成。
     * 只在目標完成數變化時寫入 DB。
     *
     * @param task The sync task / 同步任務
     * @return null if successful, error message if failed or timed out / 成功返回 null，失敗或逾時返回錯誤訊息
     * @throws InterruptedException if thread is interrupted / 執行緒被中斷時拋出
     */
    private String waitForStep2(SyncTask task) throws InterruptedException {
        int maxRetries = getMaxRetries();
        int lastCompleted = 0;

        for (int i = 0; i < maxRetries; i++) {
            String error = getH2ConfigSyncServive().getErrorMessage();
            if (error != null) return error;

            Map<String, Boolean> status = getH2ConfigSyncServive().getStep2TargetStatus();
            int completed = (int) status.values().stream()
                    .filter(Boolean::booleanValue).count();

            // Update DB only when completion count changes / 只在完成數變化時寫入 DB
            if (completed != lastCompleted) {
                String step = String.format("2/3: %d/%d", completed, task.totalTargets);
                int progress = 33 + (completed * 32 / task.totalTargets);

                updateProgress(task.history, step, progress);

                TPILogger.tl.info(String.format("[%d] Step 2: %d/%d targets completed",
                        task.syncId, completed, task.totalTargets));

                lastCompleted = completed;
            }

            if (getH2ConfigSyncServive().isStep2Complete()) {
                return null;
            }

            Thread.sleep(CHECK_INTERVAL_SECONDS * 1000L);
        }

        return getTimeoutMessage();
    }

    /**
     * Waits for Step 3 (DB replacement) to complete for all targets.
     * Updates DB only when target completion count changes.
     * <p>
     * 等待步驟 3（資料庫替換）完成。
     * 只在目標完成數變化時寫入 DB。
     *
     * @param task The sync task / 同步任務
     * @return null if successful, error message if failed or timed out / 成功返回 null，失敗或逾時返回錯誤訊息
     * @throws InterruptedException if thread is interrupted / 執行緒被中斷時拋出
     */
    private String waitForStep3(SyncTask task) throws InterruptedException {
        int maxRetries = getMaxRetries();
        int lastCompleted = 0;

        for (int i = 0; i < maxRetries; i++) {
            String error = getH2ConfigSyncServive().getErrorMessage();
            if (error != null) return error;

            Map<String, Boolean> status = getH2ConfigSyncServive().getStep3TargetStatus();
            int completed = (int) status.values().stream()
                    .filter(Boolean::booleanValue).count();

            // Update DB only when completion count changes / 只在完成數變化時寫入 DB
            if (completed != lastCompleted) {
                String step = String.format("3/3: %d/%d", completed, task.totalTargets);
                int progress = 66 + (completed * 34 / task.totalTargets);

                updateProgress(task.history, step, progress);

                TPILogger.tl.info(String.format("[%d] Step 3: %d/%d targets completed",
                        task.syncId, completed, task.totalTargets));

                lastCompleted = completed;
            }

            if (getH2ConfigSyncServive().isStep3Complete()) {
                return null;
            }

            Thread.sleep(CHECK_INTERVAL_SECONDS * 1000L);
        }

        return getTimeoutMessage();
    }

    private boolean isScheduleRunning() {
        return !getTsmpDpApptJobDao().findByRefItemNoAndStatus("DB_SYNC_REPLICA", DgrH2ConfigSyncHistoryDao.H2ConfigSyncStatus.RUNNING.getStatus()).isEmpty();
    }

    private void validateRequest(DPB0303Req request) {
        if (request.getSourceId() == null || request.getSourceId().isEmpty()) {
            throw TsmpDpAaRtnCode._1350.throwing("sourceId");
        }
        if (request.getTargetIds() == null || request.getTargetIds().isEmpty()) {
            throw TsmpDpAaRtnCode._1406.throwing("targetIds", "1", "0");
        }
        if (request.getTargetIds().contains(request.getSourceId())) {
            throw TsmpDpAaRtnCode._1559.throwing("Target ID cannot be the same as Source ID.");
        }

        String primaryId = TPILogger.tl.getPrimaryId();
        String replicaIds = TPILogger.tl.getReplicaIds();
        Set<String> validIds = new HashSet<>();

        if (primaryId != null && !primaryId.isEmpty()) {
            validIds.add(primaryId);
        }
        if (replicaIds != null && !replicaIds.isEmpty()) {
            validIds.addAll(Arrays.asList(replicaIds.split(",")));
        }

        if (!validIds.contains(request.getSourceId())) {
            throw TsmpDpAaRtnCode._1559.throwing("Invalid source node: " + request.getSourceId());
        }
        for (String targetId : request.getTargetIds()) {
            if (!validIds.contains(targetId)) {
                throw TsmpDpAaRtnCode._1559.throwing("Invalid target node: " + targetId);
            }
        }
    }

    static class SyncTask {
        Long syncId;
        DgrH2ConfigSyncHistory history;
        List<String> targetIds;
        int totalTargets;
    }
}