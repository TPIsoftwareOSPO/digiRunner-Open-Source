package tpi.dgrv4.dpaa.service;

import java.util.*;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tpi.dgrv4.entity.entity.DgrH2ConfigSyncHistory;
import tpi.dgrv4.entity.repository.DgrH2ConfigSyncHistoryDao;
import tpi.dgrv4.gateway.constant.H2ConfigSyncEnum;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.H2ConfigSyncServive;
import tpi.dgrv4.gateway.vo.ClientKeeper;
import tpi.dgrv4.gateway.vo.DPB0302Resp;
import tpi.dgrv4.gateway.vo.DPB0302RespCurrentSyncInfo;
import tpi.dgrv4.gateway.vo.DPB0302RespItem;




@Service

public class DPB0302Service {

    @Setter(onMethod_ = @Autowired)
    private DgrH2ConfigSyncHistoryDao dgrH2ConfigSyncHistoryDao;

    @Setter(onMethod_ = @Autowired)
    private H2ConfigSyncServive h2ConfigSyncServive;

    public DPB0302Resp syncH2ConfigNodeInfo() {
        DPB0302Resp resp = new DPB0302Resp();
        List<DPB0302RespItem> nodeInfoList = new ArrayList<>();
        processNodeList(nodeInfoList);
        resp.setNodeInfoList(nodeInfoList);

        DPB0302RespCurrentSyncInfo currentSync = getCurrentSync();
        resp.setCurrentSync(currentSync);
        boolean canTrigger = (currentSync == null);
        resp.setBtnStatus(canTrigger);
        return resp;
    }

    private void processNodeList(List<DPB0302RespItem> nodeInfoList) {
        LinkedList<ClientKeeper> clientList = (LinkedList<ClientKeeper>) TPILogger.lc.paramObj.get("allClientList");
        if (CollectionUtils.isEmpty(clientList)) {
            return;
        }
        String primaryId = TPILogger.tl.getPrimaryId();
        String replicaIds = TPILogger.tl.getReplicaIds();
        List<String> replicaIdList = Arrays.stream(replicaIds.split(",")).toList();
        for (ClientKeeper client : clientList) {


            if (client.getUsername().contains(primaryId)) {
                DPB0302RespItem item = new DPB0302RespItem();
                item.setRole(H2ConfigSyncEnum.PRIMARY.name());
                item.setNodeName(client.getUsername());
                item.setNodeId(primaryId);
                nodeInfoList.add(item);
            } else {
                Optional<String> matchingReplicaId = replicaIdList.stream()
                        .filter(id -> client.getUsername().contains(id))
                        .findFirst();

                if (matchingReplicaId.isPresent()) {
                    DPB0302RespItem item = new DPB0302RespItem();
                    item.setRole(H2ConfigSyncEnum.REPLICA.name());
                    item.setNodeName(client.getUsername());
                    item.setNodeId(matchingReplicaId.get());
                    nodeInfoList.add(item);
                }
            }


        }
    }

    /**
     * Gets the currently running sync job or recently completed job by querying the unified history table.
     * If no running job, returns a job completed within the last 10 seconds so frontend can capture final status.
     *
     * 取得目前正在執行的同步工作，或最近完成的工作。
     * 如果沒有正在執行的工作，會返回 10 秒內完成的工作，讓前端可以取得最終狀態。
     *
     * @return A DTO with the current sync info, or null if no job is running/recently completed.
     */
    private DPB0302RespCurrentSyncInfo getCurrentSync() {
        // Query for any running sync job (both MANUAL and SCHEDULE) from the history table.
        List<DgrH2ConfigSyncHistory> runningSyncs = getDgrH2ConfigSyncHistoryDao().findByStatus("R");

        if (!runningSyncs.isEmpty()) {
            // Build the response from the first running record found.
            return buildSyncInfoFromHistory(runningSyncs.get(0));
        }

        // No running job found. Check for recently completed jobs (within 10 seconds).
        // 沒有正在執行的工作。檢查是否有最近完成的工作（10 秒內）。
        Optional<DgrH2ConfigSyncHistory> recentlyCompleted = getDgrH2ConfigSyncHistoryDao()
                .findTopByStatusInOrderByEndTimeDesc(List.of("D", "E"));

        if (recentlyCompleted.isPresent()) {
            DgrH2ConfigSyncHistory historyRecord = recentlyCompleted.get();
            if (historyRecord.getEndTime() != null) {
                long elapsedSinceEnd = (new Date().getTime() - historyRecord.getEndTime().getTime()) / 1000;
                if (elapsedSinceEnd <= 10) {
                    // Return recently completed job so frontend can capture final status.
                    return buildSyncInfoFromHistory(historyRecord);
                }
            }
        }

        return null; // No running or recently completed sync job found.
    }

    /**
     * Builds the current sync information DTO from a DgrH2ConfigSyncHistory record.
     * This method handles both MANUAL and SCHEDULE sync types, and both running and completed jobs.
     *
     * @param history The history record of the sync job.
     * @return A DTO containing detailed information about the sync job.
     */
    private DPB0302RespCurrentSyncInfo buildSyncInfoFromHistory(DgrH2ConfigSyncHistory history) {
        DPB0302RespCurrentSyncInfo info = new DPB0302RespCurrentSyncInfo();

        String syncType = history.getSyncType();
        String historyStatus = history.getStatus();

        info.setSyncType(syncType);
        info.setSyncId(String.valueOf(history.getSyncId()));
        info.setScheduleId(history.getScheduleId());
        info.setSourceId(history.getSourceId());
        info.setTargetIds(Arrays.asList(history.getTargetIds().split(",")));
        info.setStartTime(history.getStartTime());

        // Set actual status from history (RUNNING, DONE, or ERROR).
        // 設置記錄的實際狀態（RUNNING、DONE 或 ERROR）。
        if ("D".equals(historyStatus)) {
            info.setStatus(DgrH2ConfigSyncHistoryDao.H2ConfigSyncStatus.DONE.name());
        } else if ("E".equals(historyStatus)) {
            info.setStatus(DgrH2ConfigSyncHistoryDao.H2ConfigSyncStatus.ERROR.name());
            info.setErrorMessage(history.getErrorMessage());
        } else {
            info.setStatus(DgrH2ConfigSyncHistoryDao.H2ConfigSyncStatus.RUNNING.name());
        }

        // Calculate elapsed time (use duration for completed jobs, calculate for running jobs).
        // 計算經過時間（已完成的工作使用 duration，執行中的工作即時計算）。
        if (history.getDuration() != null && !"R".equals(historyStatus)) {
            info.setElapsed(history.getDuration());
        } else if (history.getStartTime() != null) {
            long elapsed = (new Date().getTime() - history.getStartTime().getTime()) / 1000;
            info.setElapsed(elapsed);
        }

        // Progress and step details handling.
        // 進度和步驟詳情處理。
        if (!"R".equals(historyStatus)) {
            // For completed jobs, use stored values.
            // 對於已完成的工作，使用儲存的值。
            info.setProgress("D".equals(historyStatus) ? 100 : history.getProgress());
            info.setCurrentStep("D".equals(historyStatus) ? "Sync complete" : history.getCurrentStep());
        } else if (DgrH2ConfigSyncHistoryDao.SyncType.SCHEDULE.name().equals(syncType)) {
            // For running scheduled syncs, calculate dynamically.
            info.setProgress(calculateScheduleProgress());
            info.setCurrentStep(getScheduleCurrentStep());
        } else {
            // For running manual syncs, read directly from the history.
            info.setProgress(history.getProgress());
            info.setCurrentStep(history.getCurrentStep());
        }

        return info;
    }


    /**
     * Calculates the progress percentage for a scheduled sync, dynamically adapting to the number of nodes.
     * The progress is divided into three main steps:
     * - Step 1 (Compression): Fixed at 5%.
     * - Step 2 (Transmission): Ranges from 10% to 55%.
     * - Step 3 (Replacement): Ranges from 55% to 100%.
     *
     * @return The calculated progress percentage (0-100).
     */
    private int calculateScheduleProgress() {
        // ========== Step 3（55-100%）==========
        if (getH2ConfigSyncServive().isStep2Complete()) {
            Map<String, Boolean> status = getH2ConfigSyncServive().getStep3TargetStatus();

            if (getH2ConfigSyncServive().isStep3Complete()) {
                return 100;  // All complete
            }

            if (status.isEmpty()) {
                return 55;  // Step 3 just started
            }

            int total = status.size();
            int completed = (int) status.values().stream()
                    .filter(Boolean::booleanValue)
                    .count();

            if (completed == 0) {
                return 55;  // No nodes started yet
            }

            double progressPerNode = 45.0 / total;
            int progress = 55 + (int) Math.round(completed * progressPerNode);
            return Math.clamp(progress, 55, 100);
        }

        // ========== Step 2（10-55%）==========
        if (getH2ConfigSyncServive().isStep1Complete()) {
            Map<String, Boolean> status = getH2ConfigSyncServive().getStep2TargetStatus();

            if (status.isEmpty()) {
                return 10;  // Step 2 just started
            }

            int total = status.size();
            int completed = (int) status.values().stream()
                    .filter(Boolean::booleanValue)
                    .count();

            if (completed == 0) {
                return 10;  // No nodes started yet
            }

            double progressPerNode = 45.0 / total;
            int progress = 10 + (int) Math.round(completed * progressPerNode);
            return Math.clamp(progress, 10, 55);
        }

        // ========== Step 1（0-10%）==========
        return 5;
    }

    /**
     * Gets the current step description for a scheduled sync.
     *
     * @return A formatted string describing the current step and its progress.
     */
    private String getScheduleCurrentStep() {
        if (getH2ConfigSyncServive().isStep3Complete()) {
            return "Sync complete";
        }

        if (getH2ConfigSyncServive().isStep2Complete()) {
            Map<String, Boolean> status = getH2ConfigSyncServive().getStep3TargetStatus();
            int total = status.size();
            int completed = (int) status.values().stream().filter(Boolean::booleanValue).count();
            return String.format("Step 3/3 - Replacing database (%d/%d)", completed, total);
        }

        if (getH2ConfigSyncServive().isStep1Complete()) {
            Map<String, Boolean> status = getH2ConfigSyncServive().getStep2TargetStatus();
            int total = status.size();
            int completed = (int) status.values().stream().filter(Boolean::booleanValue).count();
            return String.format("Step 2/3 - Transmitting file (%d/%d)", completed, total);
        }

        return "Step 1/3 - Compressing database";
    }

    protected DgrH2ConfigSyncHistoryDao getDgrH2ConfigSyncHistoryDao() {
        return dgrH2ConfigSyncHistoryDao;
    }
    protected H2ConfigSyncServive getH2ConfigSyncServive() {
        return h2ConfigSyncServive;
    }
}
