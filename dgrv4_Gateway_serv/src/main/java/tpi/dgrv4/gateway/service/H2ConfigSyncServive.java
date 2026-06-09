package tpi.dgrv4.gateway.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.gateway.TCP.Packet.H2ConfigSyncCompressRequestPacket;
import tpi.dgrv4.gateway.TCP.Packet.H2ConfigSyncTransmitRequestPacket;
import tpi.dgrv4.gateway.TCP.Packet.H2ConfigSyncReplaceRequestPacket;
import tpi.dgrv4.gateway.constant.H2ConfigSyncEnum;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.H2ConfigSyncReq;
import tpi.dgrv4.gateway.vo.H2ConfigSyncResp;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class H2ConfigSyncServive {

    @Setter
    private String primaryId;
    @Setter
    private String replicaIds;

    /**
     * The file path of the compressed database archive from step 1.
     * 步驟 1 產生的壓縮資料庫封存檔案路徑。
     */
    @Getter
    private volatile String currentCompressedFilePath = null;

    /**
     * The source instance ID for the current synchronization process.
     * 當前同步流程的來源實例 ID。
     */
    private volatile String currentSourceId = null;

    /**
     * A thread-safe list of target instance IDs for the current synchronization.
     * 當前同步流程的目標實例 ID 列表 (執行緒安全)。
     */
    private final List<String> currentTargetIds = new java.util.concurrent.CopyOnWriteArrayList<>();

    /**
     * Flags to track the completion status of each step in the sync process.
     * 用於追蹤同步流程中每個步驟完成狀態的旗標。
     */
    @Getter
    private volatile boolean step1Complete = false;
    @Getter
    private volatile boolean step2Complete = false;
    @Getter
    private volatile boolean step3Complete = false;

    /**
     * A thread-safe map to track the completion status of step 2 for each target.
     * 用於追蹤每個目標在步驟 2 的完成狀態 (執行緒安全)。
     */
    private final Map<String, Boolean> step2TargetStatus = new ConcurrentHashMap<>();
    /**
     * A thread-safe map to track the completion status of step 3 for each target.
     * 用於追蹤每個目標在步驟 3 的完成狀態 (執行緒安全)。
     */
    private final Map<String, Boolean> step3TargetStatus = new ConcurrentHashMap<>();

    /**
     * Stores the error message if any step fails.
     * 如果任何步驟失敗，儲存錯誤訊息。
     */
    @Getter
    private volatile String errorMessage = null;

    /**
     * Initiates a full, automated 3-step database synchronization process.
     * This method resets the state, validates the request, and triggers step 1.
     * Subsequent steps are triggered automatically via callbacks.
     *
     * 啟動一個完整的、自動化的三步驟資料庫同步流程。
     * 此方法會重置狀態、驗證請求，並觸發步驟 1。後續步驟將透過回呼 (callback) 自動觸發。
     */
    public H2ConfigSyncResp syncAndResetH2Config(H2ConfigSyncReq req) {
        H2ConfigSyncResp resp = new H2ConfigSyncResp();

        // Reset state for a new synchronization process.
        // 為新的同步流程重置狀態。
        step1Complete = false;
        step2Complete = false;
        step3Complete = false;
        errorMessage = null;
        currentCompressedFilePath = null;
        step2TargetStatus.clear();
        step3TargetStatus.clear();
        currentTargetIds.clear();

        String sourceId = req.getSourceId();
        List<String> targetIds = req.getTargetIds();

        validateRequest(sourceId, targetIds);

        H2ConfigSyncEnum sourceRole = determineRole(sourceId);
        if (sourceRole == null) {
            throw TsmpDpAaRtnCode._1354.throwing("Unknown Source Role", sourceId);
        }

        TPILogger.tl.info(String.format("Auto 3-step sync initiated. Source: %s (%s), Targets: %s.",
                                        sourceId, sourceRole, targetIds));

        // Store context for the current sync process.
        // 儲存當前同步流程的上下文資訊。
        currentSourceId = sourceId;
        currentTargetIds.addAll(targetIds);

        // Trigger Step 1: Request database compression on the source instance.
        // 觸發步驟 1：請求來源實例壓縮資料庫。
        H2ConfigSyncCompressRequestPacket step1Packet = new H2ConfigSyncCompressRequestPacket(
                sourceId, sourceRole.name()
        );
        TPILogger.lc.send(step1Packet);

        TPILogger.tl.info("Step 1 (Compress) request sent. Awaiting completion to auto-trigger Step 2.");

        return resp;
    }

    /**
     * Manually triggers Step 1: Compressing the database on the source instance.
     * This is typically used for manual, step-by-step synchronization.
     *
     * 手動觸發步驟 1：在來源實例上壓縮資料庫。
     * 這通常用於手動、分步執行的同步操作。
     */
    public H2ConfigSyncResp step1_compress(H2ConfigSyncReq req) {
        H2ConfigSyncResp resp = new H2ConfigSyncResp();

        String sourceId = req.getSourceId();
        List<String> targetIds = req.getTargetIds();

        validateRequest(sourceId, targetIds);

        H2ConfigSyncEnum sourceRole = determineRole(sourceId);
        if (sourceRole == null) {
            throw TsmpDpAaRtnCode._1354.throwing("Unknown Source Role", sourceId);
        }

        TPILogger.tl.info(String.format("Manual Step 1: Compressing database. Source: %s, Targets: %s.",
                                        sourceId, targetIds));

        // Store context for the current sync process.
        // 儲存當前同步流程的上下文資訊。
        currentSourceId = sourceId;
        currentTargetIds.clear();
        currentTargetIds.addAll(targetIds);

        // Send the compression request packet.
        // 發送壓縮請求封包。
        H2ConfigSyncCompressRequestPacket packet = new H2ConfigSyncCompressRequestPacket(
                sourceId, sourceRole.name()
        );
        TPILogger.lc.send(packet);

        TPILogger.tl.info("Manual Step 1 request sent to source instance.");

        return resp;
    }

    /**
     * Manually triggers Step 2: Transmitting the compressed file to all target instances.
     *
     * 手動觸發步驟 2：將壓縮檔案傳輸到所有目標實例。
     */
    public H2ConfigSyncResp step2_transmit(H2ConfigSyncReq req) {
        H2ConfigSyncResp resp = new H2ConfigSyncResp();

        String sourceId = req.getSourceId();
        List<String> targetIds = req.getTargetIds();
        String compressedFilePath = req.getFilePath();

        validateRequest(sourceId, targetIds);

        // If file path is not provided, use the one from the last completed step 1.
        // 如果未提供檔案路徑，則使用上一次完成的步驟 1 所產生的路徑。
        if (!StringUtils.hasText(compressedFilePath)) {
            compressedFilePath = currentCompressedFilePath;
        }

        if (!StringUtils.hasText(compressedFilePath)) {
            throw TsmpDpAaRtnCode._1559.throwing("Compressed file path is required (result from Step 1).");
        }

        TPILogger.tl.info(String.format("Manual Step 2: Transmitting file. Source: %s, Targets: %s, File: %s.",
                                        sourceId, targetIds, compressedFilePath));

        // Send a transmission request to each target.
        // 向每個目標發送傳輸請求。
        for (String targetId : targetIds) {
            if (sourceId.equals(targetId)) continue;

            H2ConfigSyncEnum targetRole = determineRole(targetId);
            if (targetRole == null) {
                TPILogger.tl.error("Skipping target [" + targetId + "]: Unknown role");
                continue;
            }

            TPILogger.tl.info(String.format("Sending transmission request to target: %s.", targetId));

            H2ConfigSyncTransmitRequestPacket packet = new H2ConfigSyncTransmitRequestPacket(
                    sourceId, targetId, targetRole.name(), compressedFilePath
            );
            TPILogger.lc.send(packet);
        }

        return resp;
    }

    /**
     * Manually triggers Step 3: Decompressing the file and resetting the database on all target instances.
     *
     * 手動觸發步驟 3：在所有目標實例上解壓縮檔案並重置資料庫。
     */
    public H2ConfigSyncResp step3_decompress(H2ConfigSyncReq req) {
        H2ConfigSyncResp resp = new H2ConfigSyncResp();

        List<String> targetIds = req.getTargetIds();

        if (CollectionUtils.isEmpty(targetIds)) {
            throw TsmpDpAaRtnCode._2025.throwing("targetIds");
        }
        if (TPILogger.lc == null || !TPILogger.lc.isConnected()) {
            TPILogger.tl.error("Cannot send request: Keeper disconnected");
            throw TsmpDpAaRtnCode._1297.throwing();
        }

        TPILogger.tl.info(String.format("Manual Step 3: Decompressing and resetting DB on targets: %s.", targetIds));

        // Send a decompression request to each target.
        // 向每個目標發送解壓縮請求。
        for (String targetId : targetIds) {
            H2ConfigSyncEnum targetRole = determineRole(targetId);
            if (targetRole == null) {
                TPILogger.tl.error("Skipping target [" + targetId + "]: Unknown role");
                continue;
            }

            TPILogger.tl.info(String.format("Sending decompression request to target: %s.", targetId));

            H2ConfigSyncReplaceRequestPacket packet = new H2ConfigSyncReplaceRequestPacket(
                    targetId, targetRole.name()
            );
            TPILogger.lc.send(packet);
        }

        return resp;
    }

    /**
     * Callback method invoked when Step 1 (compression) is complete.
     * It saves the state and automatically triggers Step 2 for all targets.
     *
     * 步驟 1 (壓縮) 完成時的回呼方法。
     * 此方法會儲存狀態，並自動為所有目標觸發步驟 2。
     */
    public void onStep1Complete(String compressedFilePath) {
        currentCompressedFilePath = compressedFilePath;
        step1Complete = true;

        TPILogger.tl.info(String.format("Step 1 (Compression) is complete. Compressed file path: %s.", compressedFilePath));

        // Automatically trigger Step 2 if the sync process is still active.
        // 如果同步流程仍在進行中，則自動觸發步驟 2。
        if (currentSourceId != null && currentTargetIds != null) {
            TPILogger.tl.info("Auto-triggering Step 2 (Transmission) for all targets.");

            for (String targetId : currentTargetIds) {
                if (currentSourceId.equals(targetId)) continue;

                H2ConfigSyncEnum targetRole = determineRole(targetId);
                if (targetRole != null) {
                    H2ConfigSyncTransmitRequestPacket packet = new H2ConfigSyncTransmitRequestPacket(
                            currentSourceId, targetId, targetRole.name(), compressedFilePath
                    );
                    TPILogger.lc.send(packet);
                }
            }
        }
    }

    /**
     * Callback method invoked when Step 2 (transmission) is complete for a specific target.
     * It updates the target's status and, if all targets are complete, automatically triggers Step 3.
     *
     * 針對特定目標的步驟 2 (傳輸) 完成時的回呼方法。
     * 此方法會更新目標的狀態，並在所有目標都完成後，自動觸發步驟 3。
     */
    public void onStep2Complete(String targetId) {
        TPILogger.tl.info(String.format("Step 2 (Transmission) completed for target: %s.", targetId));

        // Record completion for this target.
        // 記錄此目標的完成狀態。
        step2TargetStatus.put(targetId, true);

        // Check if all targets have completed this step.
        // 檢查是否所有目標都已完成此步驟。
        if (currentTargetIds != null) {
            boolean allComplete = currentTargetIds.stream()
                    .allMatch(tid -> step2TargetStatus.getOrDefault(tid, false));

            // If all targets are done and Step 3 hasn't been triggered yet, trigger it now.
            // The !step2Complete check prevents multiple triggers.
            // 如果所有目標都已完成且步驟 3 尚未觸發，則立即觸發。!step2Complete 檢查可防止重複觸發。
            if (allComplete && !step2Complete) {
                step2Complete = true;

                TPILogger.tl.info(String.format("All targets have completed Step 2. Completed targets: %s.", currentTargetIds));

                TPILogger.tl.info("Auto-triggering Step 3 (Decompression) for all targets.");

                for (String tid : currentTargetIds) {
                    H2ConfigSyncEnum targetRole = determineRole(tid);
                    if (targetRole != null) {
                        H2ConfigSyncReplaceRequestPacket packet = new H2ConfigSyncReplaceRequestPacket(
                                tid, targetRole.name()
                        );
                        TPILogger.lc.send(packet);
                        TPILogger.tl.info(String.format("Step 3 request sent to target: %s.", tid));
                    }
                }
            } else if (!allComplete) {
                // Log progress if some targets are still pending.
                // 如果仍有目標待處理，則記錄進度。
                long completedCount = currentTargetIds.stream()
                        .filter(tid -> step2TargetStatus.getOrDefault(tid, false))
                        .count();
                TPILogger.tl.info(String.format("Step 2 progress: %d/%d targets completed.", completedCount, currentTargetIds.size()));
            }
        }
    }

    /**
     * Callback method invoked when Step 3 (decompression) is complete for a specific target.
     * It updates the target's status and logs the overall completion when all targets are done.
     *
     * 針對特定目標的步驟 3 (解壓縮) 完成時的回呼方法。
     * 此方法會更新目標的狀態，並在所有目標都完成後記錄整體完成情況。
     */
    public void onStep3Complete(String targetId) {
        TPILogger.tl.info(String.format("Step 3 (Decompression) completed for target: %s.", targetId));

        // Record completion for this target.
        // 記錄此目標的完成狀態。
        step3TargetStatus.put(targetId, true);

        // Check if all targets have completed this step.
        // 檢查是否所有目標都已完成此步驟。
        if (currentTargetIds != null) {
            boolean allComplete = currentTargetIds.stream()
                    .allMatch(tid -> step3TargetStatus.getOrDefault(tid, false));

            // If all targets are done and the final status hasn't been set, set it now.
            // The !step3Complete check prevents multiple log entries.
            // 如果所有目標都已完成且最終狀態尚未設定，則立即設定。!step3Complete 檢查可防止重複記錄日誌。
            if (allComplete && !step3Complete) {
                step3Complete = true;

                TPILogger.tl.info(String.format("All steps completed for all targets. Completed targets: %s.", currentTargetIds));

                // Optional: Clean up state variables after completion.
                // 可選：完成後清理狀態變數。
                // currentSourceId = null;
                // currentTargetIds = null;
                // currentCompressedFilePath = null;
            } else if (!allComplete) {
                // Log progress if some targets are still pending.
                // 如果仍有目標待處理，則記錄進度。
                long completedCount = currentTargetIds.stream()
                        .filter(tid -> step3TargetStatus.getOrDefault(tid, false))
                        .count();
                TPILogger.tl.info(String.format("Step 3 progress: %d/%d targets completed.", completedCount, currentTargetIds.size()));
            }
        }
    }

    /**
     * Callback method invoked when an error occurs during any step.
     * It records the error message to halt the polling process in DbSyncReplica.
     *
     * 在任何步驟中發生錯誤時的回呼方法。
     * 它會記錄錯誤訊息，以中止 DbSyncReplica 中的輪詢過程。
     */
    public void onStepError(int stepNumber, String error) {
        errorMessage = String.format("Step %d error: %s", stepNumber, error);
        TPILogger.tl.error(String.format("An error occurred in Step %d: %s", stepNumber, error));
    }

    /**
     * Returns a copy of the completion status map for Step 2.
     *
     * 返回步驟 2 完成狀態圖的副本。
     */
    public Map<String, Boolean> getStep2TargetStatus() {
        return new java.util.HashMap<>(step2TargetStatus);
    }

    /**
     * Returns a copy of the completion status map for Step 3.
     *
     * 返回步驟 3 完成狀態圖的副本。
     */
    public Map<String, Boolean> getStep3TargetStatus() {
        return new java.util.HashMap<>(step3TargetStatus);
    }

    /**
     * Validates the essential parameters for a sync request.
     *
     * 驗證同步請求的基本參數。
     */
    private void validateRequest(String sourceId, List<String> targetIds) {
        if (!StringUtils.hasText(sourceId)) {
            throw TsmpDpAaRtnCode._2025.throwing("sourceId");
        }
        if (CollectionUtils.isEmpty(targetIds)) {
            throw TsmpDpAaRtnCode._2025.throwing("targetIds");
        }
        if (TPILogger.lc == null || !TPILogger.lc.isConnected()) {
            TPILogger.tl.error("Cannot send sync request: Keeper disconnected");
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }

    /**
     * Determines the role (PRIMARY or REPLICA) of a given instance ID based on service configuration.
     *
     * 根據服務配置，確定給定實例 ID 的角色 (PRIMARY 或 REPLICA)。
     * @param instanceId The ID of the instance to check.
     */
    private H2ConfigSyncEnum determineRole(String instanceId) {
        if (instanceId == null) return null;
        if (instanceId.equals(primaryId)) return H2ConfigSyncEnum.PRIMARY;

        Set<String> replicaIdSet = (replicaIds == null || replicaIds.isEmpty())
                ? Collections.emptySet()
                : Arrays.stream(replicaIds.split(",")).map(String::trim).collect(Collectors.toSet());

        if (replicaIdSet.contains(instanceId)) return H2ConfigSyncEnum.REPLICA;

        return null;
    }
    /**
     * Step 1（壓縮）進度：0-100
     */
    private AtomicInteger step1Progress = new AtomicInteger(0);

    /**
     * Step 2（傳輸）進度：每個節點的進度 0-100
     * Key: 節點 ID (例如 "dgr-4")
     * Value: 進度百分比 (0-100)
     */
    private Map<String, AtomicInteger> step2Progress = new ConcurrentHashMap<>();

    /**
     * Step 3（替換）進度：每個節點的進度 0-100
     * Key: 節點 ID (例如 "dgr-4")
     * Value: 進度百分比 (0-100)
     */
    private Map<String, AtomicInteger> step3Progress = new ConcurrentHashMap<>();

    // ========== Step 1（壓縮）進度方法 ==========

    /**
     * 取得 Step 1 壓縮進度
     * @return 進度百分比 (0-100)
     */
    public int getStep1Progress() {
        return step1Progress.get();
    }

    /**
     * 更新 Step 1 壓縮進度
     * @param progress 進度百分比 (0-100)
     */
    public void updateStep1Progress(int progress) {
        this.step1Progress.set(Math.clamp(progress, 0, 100));
    }

    // ========== Step 2（傳輸）進度方法 ==========

    /**
     * 取得 Step 2 平均傳輸進度
     * @return 平均進度百分比 (0-100)
     */
    public int getStep2AverageProgress() {
        if (step2Progress.isEmpty()) {
            return 0;
        }
        int total = step2Progress.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum();
        return total / step2Progress.size();
    }

    /**
     * 取得 Step 2 每個節點的傳輸進度
     * @return Map<節點ID, 進度百分比>
     */
    public Map<String, Integer> getStep2NodeProgress() {
        Map<String, Integer> result = new HashMap<>();
        step2Progress.forEach((nodeId, progress) ->
                result.put(nodeId, progress.get())
        );
        return result;
    }

    /**
     * 更新 Step 2 某節點的傳輸進度
     * @param nodeId 節點 ID
     * @param progress 進度百分比 (0-100)
     */
    public void updateStep2Progress(String nodeId, int progress) {
        step2Progress.computeIfAbsent(nodeId, k -> new AtomicInteger(0))
                .set(Math.clamp(progress, 0, 100));
    }

    /**
     * 初始化 Step 2 節點（設定為 0%）
     * @param nodeIds 節點 ID 列表
     */
    public void initStep2Nodes(List<String> nodeIds) {
        for (String nodeId : nodeIds) {
            step2Progress.put(nodeId, new AtomicInteger(0));
        }
    }

    // ========== Step 3（替換）進度方法 ==========

    /**
     * 取得 Step 3 平均替換進度
     * @return 平均進度百分比 (0-100)
     */
    public int getStep3AverageProgress() {
        if (step3Progress.isEmpty()) {
            return 0;
        }
        int total = step3Progress.values().stream()
                .mapToInt(AtomicInteger::get)
                .sum();
        return total / step3Progress.size();
    }

    /**
     * 取得 Step 3 每個節點的替換進度
     * @return Map<節點ID, 進度百分比>
     */
    public Map<String, Integer> getStep3NodeProgress() {
        Map<String, Integer> result = new HashMap<>();
        step3Progress.forEach((nodeId, progress) ->
                result.put(nodeId, progress.get())
        );
        return result;
    }

    /**
     * 更新 Step 3 某節點的替換進度
     * @param nodeId 節點 ID
     * @param progress 進度百分比 (0-100)
     */
    public void updateStep3Progress(String nodeId, int progress) {
        step3Progress.computeIfAbsent(nodeId, k -> new AtomicInteger(0))
                .set(Math.clamp(progress, 0, 100));
    }

    /**
     * 初始化 Step 3 節點（設定為 0%）
     * @param nodeIds 節點 ID 列表
     */
    public void initStep3Nodes(List<String> nodeIds) {
        for (String nodeId : nodeIds) {
            step3Progress.put(nodeId, new AtomicInteger(0));
        }
    }
    /**
     * 重置所有進度（開始新同步時呼叫）
     */
    public void resetProgress() {
        step1Progress.set(0);
        step2Progress.clear();
        step3Progress.clear();
    }

    /**
     * 重置 Step 2 進度
     */
    public void resetStep2Progress() {
        step2Progress.clear();
    }

    /**
     * 重置 Step 3 進度
     */
    public void resetStep3Progress() {
        step3Progress.clear();
    }
    public Map<String, Object> getAllProgress() {
        Map<String, Object> result = new HashMap<>();
        result.put("step1Progress", getStep1Progress());
        result.put("step2AverageProgress", getStep2AverageProgress());
        result.put("step2NodeProgress", getStep2NodeProgress());
        result.put("step3AverageProgress", getStep3AverageProgress());
        result.put("step3NodeProgress", getStep3NodeProgress());
        return result;
    }
}