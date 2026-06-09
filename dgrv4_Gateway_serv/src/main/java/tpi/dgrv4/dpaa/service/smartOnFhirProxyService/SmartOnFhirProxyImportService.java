package tpi.dgrv4.dpaa.service.smartOnFhirProxyService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.tool.IdCodec;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.tool.Result;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.SmartOnFhirProxyDiversionDto;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.SmartOnFhirProxyStickyImportDto;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.DPB0315Req;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.DPB0316Req;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.ImportExportProgressEvent;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.ImportValidationResultDto;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.SmartOnFhirProxyImportItemDto;
import tpi.dgrv4.entity.constant.CodeEnums;
import tpi.dgrv4.entity.constant.FhirInteraction;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxy;
import tpi.dgrv4.gateway.service.SmartOnFhirProxyRouteContext;
import tpi.dgrv4.gateway.service.SmartOnFhirProxyStickyService;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxyDiversion;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxySticky;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyDao;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyDiversionDao;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyStickyDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class SmartOnFhirProxyImportService {

    private TPILogger logger = TPILogger.tl;

    /** 預先編譯的正則表達式：sofProxyName 只能包含英文字母和數字 */
    private static final Pattern PROXY_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

    private DgrSmartOnFhirProxyDao dgrSmartOnFhirProxyDao;
    private DgrSmartOnFhirProxyDiversionDao dgrSmartOnFhirProxyDiversionDao;
    private DgrSmartOnFhirProxyStickyDao dgrSmartOnFhirProxyStickyDao;
    private SmartOnFhirProxyStickyService stickyService;
    private TsmpClientDao tsmpClientDao;
    private ObjectMapper objectMapper;
    private ExecutorService executorService;

    @Autowired
    public SmartOnFhirProxyImportService(
            DgrSmartOnFhirProxyDao dgrSmartOnFhirProxyDao,
            DgrSmartOnFhirProxyDiversionDao dgrSmartOnFhirProxyDiversionDao,
            DgrSmartOnFhirProxyStickyDao dgrSmartOnFhirProxyStickyDao,
            SmartOnFhirProxyStickyService stickyService,
            TsmpClientDao tsmpClientDao,
            ObjectMapper objectMapper) {
        this.dgrSmartOnFhirProxyDao = dgrSmartOnFhirProxyDao;
        this.dgrSmartOnFhirProxyDiversionDao = dgrSmartOnFhirProxyDiversionDao;
        this.dgrSmartOnFhirProxyStickyDao = dgrSmartOnFhirProxyStickyDao;
        this.stickyService = stickyService;
        this.tsmpClientDao = tsmpClientDao;
        this.objectMapper = objectMapper;
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
    }

    @PreDestroy
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            this.logger.info("Shutting down virtual thread executor...");
            executorService.shutdown();
        }
    }

    /**
     * 預前匯入驗證（使用 SSE 推送進度）
     * 
     * @param auth 授權資訊
     * @param req  驗證請求
     * @return SseEmitter
     */
    public SseEmitter validateImportData(TsmpAuthorization auth, DPB0315Req req) {
        // 建立 SseEmitter，設定 timeout 為 10 分鐘
        SseEmitter emitter = new SseEmitter(600000L);

        // 設定完成與逾時處理
        emitter.onCompletion(() -> {
            this.logger.debug("Validation SSE completed");
        });

        emitter.onTimeout(() -> {
            this.logger.warn("Validation SSE timeout");
            emitter.complete();
        });

        emitter.onError((ex) -> {
            this.logger.error("Validation SSE error: " + StackTraceUtil.logStackTrace(ex));
        });

        // 在背景執行緒中執行驗證作業
        executorService.execute(() -> {
            try {
                performValidation(emitter, auth, req);
            } catch (Exception e) {
                this.logger.error(StackTraceUtil.logStackTrace(e));
                try {
                    sendErrorEvent(emitter, "Validation failed: " + e.getMessage());
                } catch (IOException ioEx) {
                    this.logger.error("Failed to send error event: " + StackTraceUtil.logStackTrace(ioEx));
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 執行匯入（使用 SSE 推送進度）
     * 
     * @param auth 授權資訊
     * @param req  匯入請求
     * @return SseEmitter
     */
    public SseEmitter executeImport(TsmpAuthorization auth, DPB0316Req req) {
        // 建立 SseEmitter，設定 timeout 為 10 分鐘
        SseEmitter emitter = new SseEmitter(600000L);

        // 設定完成與逾時處理
        emitter.onCompletion(() -> {
            this.logger.debug("Import SSE completed");
        });

        emitter.onTimeout(() -> {
            this.logger.warn("Import SSE timeout");
            emitter.complete();
        });

        emitter.onError((ex) -> {
            this.logger.error("Import SSE error: " + StackTraceUtil.logStackTrace(ex));
        });

        // 在背景執行緒中執行匯入作業
        executorService.execute(() -> {
            try {
                performImport(emitter, auth, req);
            } catch (Exception e) {
                this.logger.error(StackTraceUtil.logStackTrace(e));
                try {
                    sendErrorEvent(emitter, "Import failed: " + e.getMessage());
                } catch (IOException ioEx) {
                    this.logger.error("Failed to send error event: " + StackTraceUtil.logStackTrace(ioEx));
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 執行驗證作業
     */
    private void performValidation(SseEmitter emitter, TsmpAuthorization auth, DPB0315Req req) throws IOException {

        List<SmartOnFhirProxyImportItemDto> importItems = req.getImportItems();

        if (importItems == null || importItems.isEmpty()) {
            sendCompleteEvent(emitter, Collections.emptyList(), 0, 0, "No data to validate");
            emitter.complete();
            return;
        }

        int totalCount = importItems.size();
        int successCount = 0;
        int failureCount = 0;

        List<SmartOnFhirProxyImportItemDto> validationResults = new ArrayList<>();

        // 逐筆驗證
        for (int i = 0; i < importItems.size(); i++) {
            SmartOnFhirProxyImportItemDto item = importItems.get(i);

            // 驗證單筆資料
            ImportValidationResultDto result = validateImportItem(item);

            item.setValidationResult(result);

            // 發送進度事件
            sendValidationProgressEvent(emitter, i + 1, totalCount, item);

            if (result.getSuccess()) {
                successCount++;
            } else {
                failureCount++;
            }

            validationResults.add(item);
        }

        // 發送完成事件
        sendCompleteEvent(emitter, validationResults, successCount, failureCount, "Validation completed");
        emitter.complete();
    }

    /**
     * 執行匯入作業
     */
    @Transactional
    private void performImport(SseEmitter emitter, TsmpAuthorization auth, DPB0316Req req) throws IOException {
        List<SmartOnFhirProxyImportItemDto> importItems = req.getImportItems();
        Boolean forceUpdate = req.getForceUpdate() != null ? req.getForceUpdate() : true;

        if (importItems == null || importItems.isEmpty()) {
            sendCompleteEvent(emitter, Collections.emptyList(), 0, 0, "No data to import");
            emitter.complete();
            return;
        }

        int totalCount = importItems.size();
        int successCount = 0;
        int failureCount = 0;

        List<SmartOnFhirProxyImportItemDto> importResults = new ArrayList<>();

        // 逐筆匯入
        for (int i = 0; i < importItems.size(); i++) {
            SmartOnFhirProxyImportItemDto item = importItems.get(i);

            // 匯入單筆資料
            ImportValidationResultDto result = importSingleItem(item, auth, forceUpdate);

            item.setValidationResult(result);
            // 發送進度事件
            sendImportProgressEvent(emitter, i + 1, totalCount, item);

            if (result.getSuccess()) {
                successCount++;
            } else {
                failureCount++;
            }

            importResults.add(item);
        }

        // 發送完成事件
        sendCompleteEvent(emitter, importResults, successCount, failureCount, "Import completed");
        emitter.complete();
    }

    /**
     * 驗證單筆匯入資料
     */
    private ImportValidationResultDto validateImportItem(SmartOnFhirProxyImportItemDto item) {

        ImportValidationResultDto result = new ImportValidationResultDto();
        result.setUuid(item.getUuid()); // 原樣回傳 UUID（可為 null）
        result.setSuccess(true);
        result.setErrorDetails(new ArrayList<>());
        result.setSofProxyName(item.getSofProxyName());

        try {
            // 1. 驗證必填欄位
            validateRequiredFields(item, result);

            if (!result.getSuccess()) {
                return result;
            }

            // 2. 驗證欄位格式
            validateFieldFormats(item, result);

            if (!result.getSuccess()) {
                return result;
            }

            // 3. 判斷操作類型（新增或更新）— 移到 diversion 驗證前，因為 probability 驗證需要知道操作類型
            String operationType = determineOperationType(item, result);
            result.setOperationType(operationType);

            if (!result.getSuccess()) {
                return result;
            }

            // 4. 驗證 Diversion 列表（含 Smart Merge probability 驗證）
            validateDiversionList(item.getDiversionList(), item.getSofProxyId(), operationType, result);

            if (!result.getSuccess()) {
                return result;
            }

            // 5. 驗證 Sticky 列表（如果有的話）
            if (item.getStickyList() != null && !item.getStickyList().isEmpty()) {
                validateStickyList(item.getStickyList(), item.getDiversionList(), result);
            }

            if (!result.getSuccess()) {
                return result;
            }

            // 6. 檢查名稱唯一性（新增時）或 Version 衝突（更新時）
            if ("create".equals(operationType)) {
                checkNameUniqueness(item, result);
            } else if ("update".equals(operationType)) {
                checkVersionConflict(item, result);
            }

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage("Validation error occurred: " + e.getMessage());
            result.getErrorDetails().add(e.getMessage());
        }

        return result;
    }

    /**
     * 匯入單筆資料
     */
    @Transactional
    private ImportValidationResultDto importSingleItem(SmartOnFhirProxyImportItemDto item, TsmpAuthorization auth,
            boolean forceUpdate) {

        ImportValidationResultDto result = new ImportValidationResultDto();
        result.setUuid(item.getUuid()); // 原樣回傳 UUID（可為 null）
        result.setSofProxyName(item.getSofProxyName());

        try {
            // 先驗證資料
            ImportValidationResultDto validationResult = validateImportItem(item);

            // 如果驗證失敗且不是 version 衝突，直接返回失敗
            if (!validationResult.getSuccess() && !validationResult.getHasVersionConflict()) {
                result.setSuccess(false);
                result.setErrorMessage(validationResult.getErrorMessage());
                return result;
            }

            // 如果有 version 衝突但不強制更新，返回失敗
            if (validationResult.getHasVersionConflict() != null && validationResult.getHasVersionConflict() && !forceUpdate) {
                result.setSuccess(false);
                result.setErrorMessage("Version conflict detected. Please use force update mode.");
                return result;
            }

            // 執行新增或更新
            String operationType = validationResult.getOperationType();
            result.setOperationType(operationType);

            if ("create".equals(operationType)) {
                // 新增
                DgrSmartOnFhirProxy proxy = createProxyEntity(item, auth);
                proxy = getDgrSmartOnFhirProxyDao().save(proxy);

                // 新增 Diversions（Create 時沿用匯入的 ID，內部已 saveAll）
                List<DgrSmartOnFhirProxyDiversion> diversions = createDiversionEntities(
                        item.getDiversionList(), proxy.getSofProxyId(), auth);

                // 匯入 Sticky（如果有的話）
                if (item.getStickyList() != null && !item.getStickyList().isEmpty()) {
                    importStickies(item.getStickyList(), proxy.getSofProxyId(),
                            diversions, true, auth);
                }

                result.setSofProxyId(IdCodec.toString(proxy.getSofProxyId()));
                result.setSuccess(true);

            } else if ("update".equals(operationType)) {
                // 更新
                Long sofProxyIdLong = IdCodec.parseLong(item.getSofProxyId(), "sofProxyId", logger).getOrThrow();
                DgrSmartOnFhirProxy proxy = getDgrSmartOnFhirProxyDao().findById(sofProxyIdLong).orElse(null);

                if (proxy == null) {
                    result.setSuccess(false);
                    result.setErrorMessage("Unable to find the Proxy to update");
                    return result;
                }

                // 更新 Proxy
                updateProxyEntity(proxy, item, auth);
                proxy = getDgrSmartOnFhirProxyDao().save(proxy);

                // Smart Merge Diversions（取代全刪重建）
                List<DgrSmartOnFhirProxyDiversion> mergedDiversions = mergeDiversions(
                        item.getDiversionList(), proxy.getSofProxyId(), auth);

                // 匯入 Sticky（如果有的話）
                if (item.getStickyList() != null && !item.getStickyList().isEmpty()) {
                    importStickies(item.getStickyList(), proxy.getSofProxyId(),
                            mergedDiversions, false, auth);
                }

                result.setSofProxyId(IdCodec.toString(proxy.getSofProxyId()));
                result.setSuccess(true);
            }

        } catch (Exception e) {
            this.logger.error(StackTraceUtil.logStackTrace(e));
            result.setSuccess(false);
            result.setErrorMessage("Import failed: " + e.getMessage());
        }

        return result;
    }

    /**
     * 驗證必填欄位
     */
    private void validateRequiredFields(SmartOnFhirProxyImportItemDto item, ImportValidationResultDto result) {
        // sofProxyName 必填
        if (!StringUtils.hasText(item.getSofProxyName())) {
            result.setSuccess(false);
            result.setErrorMessage("sofProxyName is required");
            result.getErrorDetails().add("sofProxyName is required");
        }

        // diversionList 必填
        if (item.getDiversionList() == null || item.getDiversionList().isEmpty()) {
            result.setSuccess(false);
            result.setErrorMessage("diversionList is required and must have at least 1 item");
            result.getErrorDetails().add("diversionList is required and must have at least 1 item");
        }
    }

    /**
     * 驗證欄位格式
     */
    private void validateFieldFormats(SmartOnFhirProxyImportItemDto item, ImportValidationResultDto result) {

        if (item.getSofProxyId() != null) {
            Result<Long> idResult = IdCodec.parseLong(item.getSofProxyId());

            if (idResult.isFailure()) {
                result.setSuccess(false);
                result.setErrorMessage(
                        "Invalid sofProxyId format: " + item.getSofProxyId());
                result.getErrorDetails().add(idResult.toString());
                return;
            }
        }

        // sofProxyName: 長度 1-100，只能英文數字
        if (item.getSofProxyName() != null) {
            if (item.getSofProxyName().length() > 100) {
                result.setSuccess(false);
                result.setErrorMessage("sofProxyName length exceeds 100");
                result.getErrorDetails().add("sofProxyName length exceeds 100");
            }

            if (!PROXY_NAME_PATTERN.matcher(item.getSofProxyName()).matches()) {
                result.setSuccess(false);
                result.setErrorMessage("sofProxyName can only contain letters and numbers");
                result.getErrorDetails().add("sofProxyName can only contain letters and numbers");
            }
        }

        // sofProxyRemark: 長度 <= 500
        if (item.getSofProxyRemark() != null && item.getSofProxyRemark().length() > 500) {
            result.setSuccess(false);
            result.setErrorMessage("sofProxyRemark length exceeds 500");
            result.getErrorDetails().add("sofProxyRemark length exceeds 500");
        }

        // sofProxyIgnoreApi: 長度 <= 4000
        if (item.getSofProxyIgnoreApi() != null && item.getSofProxyIgnoreApi().length() > 4000) {
            result.setSuccess(false);
            result.setErrorMessage("sofProxyIgnoreApi length exceeds 4000");
            result.getErrorDetails().add("sofProxyIgnoreApi length exceeds 4000");
        }

        // sofProxyTps: 範圍 0-999999
        if (item.getSofProxyTps() != null && (item.getSofProxyTps() < 0 || item.getSofProxyTps() > 999999)) {
            result.setSuccess(false);
            result.setErrorMessage("sofProxyTps must be between 0 and 999999");
            result.getErrorDetails().add("sofProxyTps must be between 0 and 999999");
        }

        // sofProxyStatus: 驗證枚舉值
        if (StringUtils.hasText(item.getSofProxyStatus())) {
            if (!CodeEnums.tryFromCode(DgrSmartOnFhirProxy.Status.class, item.getSofProxyStatus()).isPresent()) {
                result.setSuccess(false);
                result.setErrorMessage("sofProxyStatus is invalid");
                result.getErrorDetails().add("sofProxyStatus is invalid");
            }
        }

        // sofProxyClientId: 驗證 Client 是否存在（當提供時）
        if (item.getSofProxyClientId() != null && !item.getSofProxyClientId().isEmpty()) {
            for (String clientId : item.getSofProxyClientId()) {
                if (StringUtils.hasText(clientId) && !tsmpClientDao.existsById(clientId)) {
                    result.setSuccess(false);
                    result.setErrorMessage("Client ID '" + clientId + "' does not exist");
                    result.getErrorDetails().add("Client ID '" + clientId + "' does not exist");
                }
            }
        }
    }

    /**
     * 驗證 Diversion 列表（含 Smart Merge probability 驗證）
     *
     * @param diversionList 匯入的 diversion 列表
     * @param sofProxyIdStr Proxy ID（字串，update 時使用）
     * @param operationType 操作類型（create/update）
     * @param result        驗證結果
     */
    private void validateDiversionList(List<SmartOnFhirProxyDiversionDto> diversionList,
            String sofProxyIdStr, String operationType, ImportValidationResultDto result) {
        if (diversionList == null || diversionList.isEmpty()) {
            return; // 已在必填欄位驗證中處理
        }

        int importProbability = 0;
        Set<String> importedDiversionIds = new HashSet<>();

        for (int i = 0; i < diversionList.size(); i++) {
            SmartOnFhirProxyDiversionDto div = diversionList.get(i);

            if (div.getSofProxyDiversionId() != null) {
                Result<Long> idResult = IdCodec.parseLong(div.getSofProxyDiversionId());

                if (idResult.isFailure()) {
                    result.setSuccess(false);
                    result.setErrorMessage(
                            "Invalid sofProxyDiversionId format: " + div.getSofProxyDiversionId());
                    result.getErrorDetails().add(idResult.toString());
                    return;
                }
                importedDiversionIds.add(div.getSofProxyDiversionId());
            }

            // 驗證 URL 必填
            if (!StringUtils.hasText(div.getSofProxyDiversionUrl())) {
                result.setSuccess(false);
                result.setErrorMessage("Diversion[" + i + "] URL is required");
                result.getErrorDetails().add("Diversion[" + i + "] URL is required");
                continue;
            }

            // 驗證 URL 格式
            if (!isValidUrl(div.getSofProxyDiversionUrl())) {
                result.setSuccess(false);
                result.setErrorMessage("Diversion[" + i + "] URL format is invalid");
                result.getErrorDetails().add("Diversion[" + i + "] URL format is invalid");
            }

            // 驗證 probability 必填
            if (div.getSofProxyDiversionProbability() == null) {
                result.setSuccess(false);
                result.setErrorMessage("Diversion[" + i + "] probability is required");
                result.getErrorDetails().add("Diversion[" + i + "] probability is required");
                continue;
            }

            // 驗證 probability 範圍 1-100
            Integer prob = div.getSofProxyDiversionProbability();
            if (prob < 1 || prob > 100) {
                result.setSuccess(false);
                result.setErrorMessage("Diversion[" + i + "] probability must be between 1 and 100");
                result.getErrorDetails().add("Diversion[" + i + "] probability must be between 1 and 100");
            }

            importProbability += prob;
        }

        // 嚴格驗證 probability 總和 = 100
        // update 時：匯入的 + DB 中未被匯入涵蓋的 = 100
        int totalProbability = importProbability;
        if ("update".equals(operationType) && StringUtils.hasText(sofProxyIdStr)) {
            Long sofProxyIdLong = IdCodec.parseLong(sofProxyIdStr).getOrElse(() -> null);
            if (sofProxyIdLong != null) {
                List<DgrSmartOnFhirProxyDiversion> dbDiversions = getDgrSmartOnFhirProxyDiversionDao()
                        .findBySofProxyId(sofProxyIdLong);
                for (DgrSmartOnFhirProxyDiversion dbDiv : dbDiversions) {
                    String dbDivIdStr = IdCodec.toString(dbDiv.getSofProxyDiversionId());
                    // 只加上 DB 中有、但匯入沒有帶到的 diversion 的 probability
                    if (!importedDiversionIds.contains(dbDivIdStr)) {
                        totalProbability += dbDiv.getSofProxyDiversionProbability();
                    }
                }
            }
        }

        if (totalProbability != 100) {
            result.setSuccess(false);
            result.setErrorMessage("Diversion probability total must be 100, currently " + totalProbability);
            result.getErrorDetails().add("Diversion probability total must be 100, currently " + totalProbability);
        }
    }

    /**
     * 驗證 URL 格式
     */
    private boolean isValidUrl(String urlString) {
        if (urlString == null || urlString.length() > 1000) {
            return false;
        }

        try {
            URI uri = new URI(urlString);
            String scheme = uri.getScheme();
            if (scheme == null) {
                return false;
            }
            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * 判斷操作類型（新增或更新）
     */
    private String determineOperationType(SmartOnFhirProxyImportItemDto item, ImportValidationResultDto result) {
        if (item.getSofProxyId() != null) {
            Long sofProxyIdLong = IdCodec.parseLong(item.getSofProxyId(), "sofProxyId", logger).getOrElse(() -> null);

            if (sofProxyIdLong == null) {
                result.setSuccess(false);
                result.setErrorMessage("Invalid sofProxyId format");
                result.getErrorDetails().add("Invalid sofProxyId format");
                return null;
            }

            Optional<DgrSmartOnFhirProxy> existing = getDgrSmartOnFhirProxyDao().findById(sofProxyIdLong);
            if (existing.isPresent()) {
                result.setSofProxyId(item.getSofProxyId());
                return "update";
            } else {
                // 找不到時視為新增，使用提供的 ID
                result.setSofProxyId(item.getSofProxyId());
                return "create";
            }
        }

        return "create";
    }

    /**
     * 檢查名稱唯一性（新增時）
     */
    private void checkNameUniqueness(SmartOnFhirProxyImportItemDto item, ImportValidationResultDto result) {
        List<DgrSmartOnFhirProxy> existing = getDgrSmartOnFhirProxyDao().findBySofProxyName(item.getSofProxyName());

        if (!existing.isEmpty()) {
            result.setSuccess(false);
            result.setErrorMessage("Name already exists: " + item.getSofProxyName());
            result.getErrorDetails().add("Name already exists: " + item.getSofProxyName());
        }
    }

    /**
     * 檢查 Version 衝突（更新時）
     */
    private void checkVersionConflict(SmartOnFhirProxyImportItemDto item, ImportValidationResultDto result) {
        Long sofProxyIdLong = IdCodec.parseLong(item.getSofProxyId(), "sofProxyId", logger).getOrElse(0L);
        Optional<DgrSmartOnFhirProxy> existingOpt = getDgrSmartOnFhirProxyDao().findById(sofProxyIdLong);

        if (!existingOpt.isPresent()) {
            result.setSuccess(false);
            result.setErrorMessage("Proxy to update not found");
            result.getErrorDetails().add("Proxy to update not found");
            return;
        }

        DgrSmartOnFhirProxy existing = existingOpt.get();

        // 比對 version
        Long importVersionLong = IdCodec.parseLong(item.getVersion(), "version", logger).getOrElse(() -> null);
        if (importVersionLong != null && !importVersionLong.equals(existing.getVersion())) {
            result.setHasVersionConflict(true);
            result.setCurrentVersion(IdCodec.toString(existing.getVersion()));
            result.setImportVersion(item.getVersion());

            result.setSuccess(false);

            result.setErrorMessage("Version conflict: DB version is " + existing.getVersion() + ", import version is "
                    + item.getVersion());

            result.getErrorDetails()
                    .add("Version conflict");
        }
    }

    /**
     * 建立 Proxy Entity
     */
    private DgrSmartOnFhirProxy createProxyEntity(SmartOnFhirProxyImportItemDto item, TsmpAuthorization auth) {
        DgrSmartOnFhirProxy proxy = new DgrSmartOnFhirProxy();

        if (item.getSofProxyId() != null) {
            Long sofProxyId = IdCodec.parseLong(item.getSofProxyId()).getOrElse(() -> null);
            proxy.setSofProxyId(sofProxyId);
        }

        proxy.setSofProxyName(item.getSofProxyName());

        // Status: 預設 'Y'
        if (StringUtils.hasText(item.getSofProxyStatus())) {
            DgrSmartOnFhirProxy.Status status = CodeEnums
                    .tryFromCode(DgrSmartOnFhirProxy.Status.class, item.getSofProxyStatus())
                    .orElse(DgrSmartOnFhirProxy.Status.ENABLED);
            proxy.setSofProxyStatus(status);
        } else {
            proxy.setSofProxyStatus(DgrSmartOnFhirProxy.Status.ENABLED);
        }

        proxy.setSofProxyRemark(StringUtils.hasText(item.getSofProxyRemark()) ? item.getSofProxyRemark() : "");

        // 安全性設定
        proxy.setSofProxyAccessToken(getStatusOrDefault(item.getSofProxyAccessToken(),
                DgrSmartOnFhirProxy.Status.DISABLED));
        proxy.setSofProxySqlInjection(getStatusOrDefault(item.getSofProxySqlInjection(),
                DgrSmartOnFhirProxy.Status.DISABLED));
        proxy.setSofProxyTraffic(getStatusOrDefault(item.getSofProxyTraffic(),
                DgrSmartOnFhirProxy.Status.DISABLED));
        proxy.setSofProxyXss(getStatusOrDefault(item.getSofProxyXss(),
                DgrSmartOnFhirProxy.Status.DISABLED));
        proxy.setSofProxyXxe(getStatusOrDefault(item.getSofProxyXxe(),
                DgrSmartOnFhirProxy.Status.DISABLED));
		proxy.setSofProxyShowLog(getStatusOrDefault(item.getSofProxyShowLog(),
				DgrSmartOnFhirProxy.Status.DISABLED));
		proxy.setSofProxySticky(getStatusOrDefault(item.getSofProxySticky(),
				DgrSmartOnFhirProxy.Status.ENABLED));
		proxy.setSofProxyUrlRewrite(getStatusOrDefault(item.getSofProxyUrlRewrite(),
				DgrSmartOnFhirProxy.Status.ENABLED));

        proxy.setSofProxyTps(item.getSofProxyTps() != null ? item.getSofProxyTps() : 0);
        proxy.setSofProxyIgnoreApi(
                StringUtils.hasText(item.getSofProxyIgnoreApi()) ? item.getSofProxyIgnoreApi() : "");
        proxy.setSofProxyClientId(
                item.getSofProxyClientId() != null ? item.getSofProxyClientId() : new ArrayList<>());

        // 設定系統欄位（使用實際匯入時間與人員）
        proxy.setCreateDateTime(DateTimeUtil.now());
        proxy.setCreateUser(auth.getUserName());

        return proxy;
    }

    /**
     * 更新 Proxy Entity
     */
    private void updateProxyEntity(DgrSmartOnFhirProxy proxy, SmartOnFhirProxyImportItemDto item,
            TsmpAuthorization auth) {
        proxy.setSofProxyName(item.getSofProxyName());

        if (StringUtils.hasText(item.getSofProxyStatus())) {
            DgrSmartOnFhirProxy.Status status = CodeEnums
                    .tryFromCode(DgrSmartOnFhirProxy.Status.class, item.getSofProxyStatus())
                    .orElse(proxy.getSofProxyStatus());
            proxy.setSofProxyStatus(status);
        }

        proxy.setSofProxyRemark(StringUtils.hasText(item.getSofProxyRemark()) ? item.getSofProxyRemark() : "");

        // 安全性設定
        proxy.setSofProxyAccessToken(getStatusOrDefault(item.getSofProxyAccessToken(),
                proxy.getSofProxyAccessToken()));
        proxy.setSofProxySqlInjection(getStatusOrDefault(item.getSofProxySqlInjection(),
                proxy.getSofProxySqlInjection()));
        proxy.setSofProxyTraffic(getStatusOrDefault(item.getSofProxyTraffic(),
                proxy.getSofProxyTraffic()));
        proxy.setSofProxyXss(getStatusOrDefault(item.getSofProxyXss(),
                proxy.getSofProxyXss()));
        proxy.setSofProxyXxe(getStatusOrDefault(item.getSofProxyXxe(),
                proxy.getSofProxyXxe()));
        proxy.setSofProxyShowLog(getStatusOrDefault(item.getSofProxyShowLog(),
                proxy.getSofProxyShowLog()));
        proxy.setSofProxySticky(getStatusOrDefault(item.getSofProxySticky(),
                proxy.getSofProxySticky()));
        proxy.setSofProxyUrlRewrite(getStatusOrDefault(item.getSofProxyUrlRewrite(),
                proxy.getSofProxyUrlRewrite()));

        if (item.getSofProxyTps() != null) {
            proxy.setSofProxyTps(item.getSofProxyTps());
        }

        if (StringUtils.hasText(item.getSofProxyIgnoreApi())) {
            proxy.setSofProxyIgnoreApi(item.getSofProxyIgnoreApi());
        }

        if (item.getSofProxyClientId() != null) {
            proxy.setSofProxyClientId(item.getSofProxyClientId());
        }

        // 更新系統欄位（使用實際匯入時間與人員）
        proxy.setUpdateDateTime(DateTimeUtil.now());
        proxy.setUpdateUser(auth.getUserName());
    }

    private DgrSmartOnFhirProxy.Status getStatusOrDefault(String statusCode,
            DgrSmartOnFhirProxy.Status defaultStatus) {
        if (!StringUtils.hasText(statusCode)) {
            return defaultStatus;
        }
        return CodeEnums.tryFromCode(DgrSmartOnFhirProxy.Status.class, statusCode)
                .orElse(defaultStatus);
    }

    /**
     * 建立 Diversion Entity 列表（Create 時使用，支援沿用匯入的 ID）
     */
    private List<DgrSmartOnFhirProxyDiversion> createDiversionEntities(
            List<SmartOnFhirProxyDiversionDto> diversionDtoList,
            Long sofProxyId,
            TsmpAuthorization auth) {
        List<DgrSmartOnFhirProxyDiversion> diversions = new ArrayList<>();

        for (SmartOnFhirProxyDiversionDto dto : diversionDtoList) {
            SmartOnFhirProxyRouteContext.ParsedUrl parsed =
                    SmartOnFhirProxyRouteContext.parseDiversionUrl(dto.getSofProxyDiversionUrl());
            DgrSmartOnFhirProxyDiversion diversion = new DgrSmartOnFhirProxyDiversion();

            // Create 時沿用匯入的 ID（如果有的話）
            if (dto.getSofProxyDiversionId() != null) {
                Long divId = IdCodec.parseLong(dto.getSofProxyDiversionId()).getOrElse(() -> null);
                diversion.setSofProxyDiversionId(divId);
            }

            diversion.setSofProxyId(sofProxyId);
            diversion.setSofProxyDiversionProbability(dto.getSofProxyDiversionProbability());
            diversion.setSofProxyDiversionUrl(parsed.baseUrl());
            diversion.setSofProxyDiversionFhirBasePath(parsed.fhirBasePath());

            // 設定系統欄位（使用實際匯入時間與人員）
            diversion.setCreateDateTime(DateTimeUtil.now());
            diversion.setCreateUser(auth.getUserName());

            diversions.add(diversion);
        }

        return getDgrSmartOnFhirProxyDiversionDao().saveAll(diversions);
    }

    /**
     * Smart Merge Diversions（Update 時使用）
     * 有 ID 且 DB 找得到 → update in place
     * 有 ID 但 DB 找不到 → create（沿用 ID）
     * 無 ID → create（系統產生 ID）
     * DB 有但匯入沒帶 → 不動
     *
     * @return 本次匯入處理過的 diversions 列表（供 sticky 解析用）
     */
    private List<DgrSmartOnFhirProxyDiversion> mergeDiversions(
            List<SmartOnFhirProxyDiversionDto> importDiversionList,
            Long sofProxyId,
            TsmpAuthorization auth) {

        // 查詢 DB 既有 diversions
        Map<Long, DgrSmartOnFhirProxyDiversion> dbDiversionMap = new HashMap<>();
        List<DgrSmartOnFhirProxyDiversion> dbDiversions = getDgrSmartOnFhirProxyDiversionDao()
                .findBySofProxyId(sofProxyId);
        for (DgrSmartOnFhirProxyDiversion dbDiv : dbDiversions) {
            dbDiversionMap.put(dbDiv.getSofProxyDiversionId(), dbDiv);
        }

        List<DgrSmartOnFhirProxyDiversion> processedDiversions = new ArrayList<>();

        for (SmartOnFhirProxyDiversionDto dto : importDiversionList) {
            SmartOnFhirProxyRouteContext.ParsedUrl parsed =
                    SmartOnFhirProxyRouteContext.parseDiversionUrl(dto.getSofProxyDiversionUrl());

            Long diversionId = null;
            if (dto.getSofProxyDiversionId() != null) {
                diversionId = IdCodec.parseLong(dto.getSofProxyDiversionId()).getOrElse(() -> null);
            }

            if (diversionId != null && dbDiversionMap.containsKey(diversionId)) {
                // DB 有 → update in place
                DgrSmartOnFhirProxyDiversion existing = dbDiversionMap.get(diversionId);
                existing.setSofProxyDiversionProbability(dto.getSofProxyDiversionProbability());
                existing.setSofProxyDiversionUrl(parsed.baseUrl());
                existing.setSofProxyDiversionFhirBasePath(parsed.fhirBasePath());
                existing.setUpdateDateTime(DateTimeUtil.now());
                existing.setUpdateUser(auth.getUserName());
                processedDiversions.add(getDgrSmartOnFhirProxyDiversionDao().save(existing));
            } else {
                // DB 找不到或無 ID → create
                DgrSmartOnFhirProxyDiversion newDiv = new DgrSmartOnFhirProxyDiversion();
                if (diversionId != null) {
                    newDiv.setSofProxyDiversionId(diversionId); // 沿用匯入 ID
                }
                newDiv.setSofProxyId(sofProxyId);
                newDiv.setSofProxyDiversionProbability(dto.getSofProxyDiversionProbability());
                newDiv.setSofProxyDiversionUrl(parsed.baseUrl());
                newDiv.setSofProxyDiversionFhirBasePath(parsed.fhirBasePath());
                newDiv.setCreateDateTime(DateTimeUtil.now());
                newDiv.setCreateUser(auth.getUserName());
                processedDiversions.add(getDgrSmartOnFhirProxyDiversionDao().save(newDiv));
            }
        }

        return processedDiversions;
    }

    /**
     * 匯入 Sticky 資料（Create 與 Update 共用）
     *
     * @param stickyList      匯入的 sticky 列表
     * @param sofProxyId      Proxy ID
     * @param processedDiversions 本次匯入處理的 diversions（供 diversionIndex 解析）
     * @param isCreate        是否為 Create 操作
     * @param auth            授權資訊
     */
    private void importStickies(
            List<SmartOnFhirProxyStickyImportDto> stickyList,
            Long sofProxyId,
            List<DgrSmartOnFhirProxyDiversion> processedDiversions,
            boolean isCreate,
            TsmpAuthorization auth) {

        // 查詢 DB 既有 stickies（Update 時使用）
        Map<Long, DgrSmartOnFhirProxySticky> dbStickyMap = new HashMap<>();
        if (!isCreate) {
            List<DgrSmartOnFhirProxySticky> dbStickies = getDgrSmartOnFhirProxyStickyDao()
                    .findBySofProxyId(sofProxyId);
            for (DgrSmartOnFhirProxySticky dbSticky : dbStickies) {
                dbStickyMap.put(dbSticky.getSofProxyStickyId(), dbSticky);
            }
        }

        // 收集本次匯入後所有 type，用於重複檢查
        Set<String> processedTypes = new HashSet<>();

        for (int i = 0; i < stickyList.size(); i++) {
            SmartOnFhirProxyStickyImportDto stickyDto = stickyList.get(i);

            // 1. 解析 diversionId
            Long diversionId = resolveDiversionId(stickyDto, sofProxyId, processedDiversions, i);

            // 2. 驗證 type 必填
            if (!StringUtils.hasText(stickyDto.getSofProxyStickyType())) {
                throw new IllegalArgumentException(
                        "Sticky[" + i + "]: sofProxyStickyType is required.");
            }

            String stickyType = stickyDto.getSofProxyStickyType();

            // 3. 檢查匯入資料內 type 重複
            if (!processedTypes.add(stickyType)) {
                throw new IllegalArgumentException(
                        "Sticky[" + i + "]: Duplicate type '" + stickyType + "' in import data.");
            }

            // 4. 判斷是 update 還是 create
            Long stickyId = null;
            if (stickyDto.getSofProxyStickyId() != null) {
                stickyId = IdCodec.parseLong(stickyDto.getSofProxyStickyId()).getOrElse(() -> null);
            }

            if (stickyId != null && dbStickyMap.containsKey(stickyId)) {
                // Update in place
                DgrSmartOnFhirProxySticky entity = dbStickyMap.get(stickyId);
                String oldHashcode = entity.getSofProxyStickyHashcode();

                entity.setSofProxyDiversionId(diversionId);
                entity.setSofProxyStickyType(stickyType);
                entity.setSofProxyStickyTypeId(stickyDto.getSofProxyStickyTypeId());
                entity.setSofProxyStickyVerb(stickyDto.getSofProxyStickyVerb());
                entity.setSofProxyStickyPath(stickyDto.getSofProxyStickyPath());

                if (StringUtils.hasText(stickyDto.getSofProxyStickyInteraction())) {
                    entity.setSofProxyStickyInteraction(
                            FhirInteraction.valueOf(stickyDto.getSofProxyStickyInteraction()));
                } else {
                    entity.setSofProxyStickyInteraction(null);
                }

                // 重算 hashcode
                String newHashcode = SmartOnFhirProxyStickyService.calculateStickyHashcode(
                        sofProxyId, stickyType);
                entity.setSofProxyStickyHashcode(newHashcode);

                // 檢查重複（排除自己）
                Optional<DgrSmartOnFhirProxySticky> dupOpt = getDgrSmartOnFhirProxyStickyDao()
                        .findBySofProxyIdAndSofProxyStickyHashcode(sofProxyId, newHashcode);
                if (dupOpt.isPresent() && !dupOpt.get().getSofProxyStickyId().equals(stickyId)) {
                    throw new IllegalArgumentException(
                            "Sticky[" + i + "]: Duplicate binding for type '" + stickyType + "'.");
                }

                entity.setUpdateDateTime(DateTimeUtil.now());
                entity.setUpdateUser(auth.getUserName());

                DgrSmartOnFhirProxySticky saved = getDgrSmartOnFhirProxyStickyDao().save(entity);

                // 快取管理
                if (!oldHashcode.equals(newHashcode)) {
                    stickyService.evictCache(oldHashcode);
                }
                stickyService.putCache(newHashcode, saved);

            } else {
                // Create new
                DgrSmartOnFhirProxySticky entity = new DgrSmartOnFhirProxySticky();

                // 沿用匯入的 ID（如果有的話）
                if (stickyId != null) {
                    entity.setSofProxyStickyId(stickyId);
                }

                entity.setSofProxyId(sofProxyId);
                entity.setSofProxyDiversionId(diversionId);
                entity.setSofProxyStickyType(stickyType);
                entity.setSofProxyStickyTypeId(stickyDto.getSofProxyStickyTypeId());
                entity.setSofProxyStickyVerb(stickyDto.getSofProxyStickyVerb());
                entity.setSofProxyStickyPath(stickyDto.getSofProxyStickyPath());

                if (StringUtils.hasText(stickyDto.getSofProxyStickyInteraction())) {
                    entity.setSofProxyStickyInteraction(
                            FhirInteraction.valueOf(stickyDto.getSofProxyStickyInteraction()));
                }

                // 計算 hashcode
                String hashcode = SmartOnFhirProxyStickyService.calculateStickyHashcode(
                        sofProxyId, stickyType);
                entity.setSofProxyStickyHashcode(hashcode);

                // 檢查重複
                Optional<DgrSmartOnFhirProxySticky> dupOpt = getDgrSmartOnFhirProxyStickyDao()
                        .findBySofProxyIdAndSofProxyStickyHashcode(sofProxyId, hashcode);
                if (dupOpt.isPresent()) {
                    throw new IllegalArgumentException(
                            "Sticky[" + i + "]: Duplicate binding for type '" + stickyType + "'.");
                }

                entity.setCreateDateTime(DateTimeUtil.now());
                entity.setCreateUser(auth.getUserName());

                DgrSmartOnFhirProxySticky saved = getDgrSmartOnFhirProxyStickyDao().save(entity);

                // 預熱快取
                stickyService.putCache(hashcode, saved);
            }
        }
    }

    /**
     * 解析 Sticky 的 diversionId
     * 優先順序：
     * 1. sofProxyDiversionId 有值且該 diversion 存在且屬於同一 proxy → 直接使用
     * 2. diversionIndex 有值且在範圍內 → 使用 processedDiversions[index] 的 ID
     * 3. 以上皆否 → 拋出例外
     */
    private Long resolveDiversionId(
            SmartOnFhirProxyStickyImportDto stickyDto,
            Long sofProxyId,
            List<DgrSmartOnFhirProxyDiversion> processedDiversions,
            int index) {

        // 嘗試 sofProxyDiversionId
        if (StringUtils.hasText(stickyDto.getSofProxyDiversionId())) {
            Long divId = IdCodec.parseLong(stickyDto.getSofProxyDiversionId()).getOrElse(() -> null);
            if (divId != null) {
                Optional<DgrSmartOnFhirProxyDiversion> divOpt = getDgrSmartOnFhirProxyDiversionDao().findById(divId);
                if (divOpt.isPresent() && divOpt.get().getSofProxyId().equals(sofProxyId)) {
                    return divId;
                }
            }
        }

        // fallback: diversionIndex
        if (stickyDto.getDiversionIndex() != null) {
            int divIdx = stickyDto.getDiversionIndex();
            if (divIdx >= 0 && divIdx < processedDiversions.size()) {
                return processedDiversions.get(divIdx).getSofProxyDiversionId();
            }
        }

        throw new IllegalArgumentException(
                "Sticky[" + index + "]: Unable to resolve diversionId. " +
                        "Provide a valid sofProxyDiversionId or diversionIndex.");
    }

    /**
     * 驗證 Sticky 列表
     */
    private void validateStickyList(
            List<SmartOnFhirProxyStickyImportDto> stickyList,
            List<SmartOnFhirProxyDiversionDto> diversionList,
            ImportValidationResultDto result) {

        Set<String> types = new HashSet<>();

        for (int i = 0; i < stickyList.size(); i++) {
            SmartOnFhirProxyStickyImportDto sticky = stickyList.get(i);

            // sofProxyStickyType 必填
            if (!StringUtils.hasText(sticky.getSofProxyStickyType())) {
                result.setSuccess(false);
                result.setErrorMessage("Sticky[" + i + "]: sofProxyStickyType is required");
                result.getErrorDetails().add("Sticky[" + i + "]: sofProxyStickyType is required");
                continue;
            }

            // 長度檢查
            if (sticky.getSofProxyStickyType().length() > 200) {
                result.setSuccess(false);
                result.setErrorMessage("Sticky[" + i + "]: sofProxyStickyType exceeds max length 200");
                result.getErrorDetails().add("Sticky[" + i + "]: sofProxyStickyType exceeds max length 200");
            }
            if (sticky.getSofProxyStickyTypeId() != null && sticky.getSofProxyStickyTypeId().length() > 200) {
                result.setSuccess(false);
                result.setErrorMessage("Sticky[" + i + "]: sofProxyStickyTypeId exceeds max length 200");
                result.getErrorDetails().add("Sticky[" + i + "]: sofProxyStickyTypeId exceeds max length 200");
            }
            if (sticky.getSofProxyStickyVerb() != null && sticky.getSofProxyStickyVerb().length() > 50) {
                result.setSuccess(false);
                result.setErrorMessage("Sticky[" + i + "]: sofProxyStickyVerb exceeds max length 50");
                result.getErrorDetails().add("Sticky[" + i + "]: sofProxyStickyVerb exceeds max length 50");
            }
            if (sticky.getSofProxyStickyPath() != null && sticky.getSofProxyStickyPath().length() > 2000) {
                result.setSuccess(false);
                result.setErrorMessage("Sticky[" + i + "]: sofProxyStickyPath exceeds max length 2000");
                result.getErrorDetails().add("Sticky[" + i + "]: sofProxyStickyPath exceeds max length 2000");
            }

            // FhirInteraction 列舉值檢查
            if (StringUtils.hasText(sticky.getSofProxyStickyInteraction())) {
                try {
                    FhirInteraction.valueOf(sticky.getSofProxyStickyInteraction());
                } catch (IllegalArgumentException e) {
                    result.setSuccess(false);
                    result.setErrorMessage(
                            "Sticky[" + i + "]: Invalid interaction '" + sticky.getSofProxyStickyInteraction() + "'");
                    result.getErrorDetails().add(
                            "Sticky[" + i + "]: Invalid interaction '" + sticky.getSofProxyStickyInteraction() + "'");
                }
            }

            // 匯入資料內 type 重複檢查
            if (!types.add(sticky.getSofProxyStickyType())) {
                result.setSuccess(false);
                result.setErrorMessage(
                        "Sticky[" + i + "]: Duplicate type '" + sticky.getSofProxyStickyType() + "' in import data");
                result.getErrorDetails().add(
                        "Sticky[" + i + "]: Duplicate type '" + sticky.getSofProxyStickyType() + "' in import data");
            }

            // diversionId 解析可行性檢查（至少有其中一種方式）
            boolean hasDiversionId = StringUtils.hasText(sticky.getSofProxyDiversionId());
            boolean hasDiversionIndex = sticky.getDiversionIndex() != null
                    && sticky.getDiversionIndex() >= 0
                    && diversionList != null
                    && sticky.getDiversionIndex() < diversionList.size();
            if (!hasDiversionId && !hasDiversionIndex) {
                result.setSuccess(false);
                result.setErrorMessage(
                        "Sticky[" + i + "]: Must provide a valid sofProxyDiversionId or diversionIndex");
                result.getErrorDetails().add(
                        "Sticky[" + i + "]: Must provide a valid sofProxyDiversionId or diversionIndex");
            }

            // sofProxyStickyId 格式檢查
            if (sticky.getSofProxyStickyId() != null) {
                Result<Long> idResult = IdCodec.parseLong(sticky.getSofProxyStickyId());
                if (idResult.isFailure()) {
                    result.setSuccess(false);
                    result.setErrorMessage(
                            "Invalid sofProxyStickyId format: " + sticky.getSofProxyStickyId());
                    result.getErrorDetails().add(idResult.toString());
                }
            }
        }
    }

    /**
     * 發送驗證進度事件
     */
    private void sendValidationProgressEvent(SseEmitter emitter, int current, int total,
            SmartOnFhirProxyImportItemDto data) throws IOException {
        ImportExportProgressEvent event = new ImportExportProgressEvent("progress");
        event.setCurrent(current);
        event.setTotal(total);
        event.setPercentage((int) ((current * 100.0) / total));
        event.setStatus(data.getValidationResult().getSuccess() ? "success" : "failed");
        event.setMessage("Validating: " + data.getSofProxyName());
        event.setData(data);

        String jsonEvent = objectMapper.writeValueAsString(event);
        emitter.send(SseEmitter.event().name("progress").data(jsonEvent));
    }

    /**
     * 發送匯入進度事件
     */
    private void sendImportProgressEvent(SseEmitter emitter, int current, int total, SmartOnFhirProxyImportItemDto data)
            throws IOException {
        ImportExportProgressEvent event = new ImportExportProgressEvent("progress");
        event.setCurrent(current);
        event.setTotal(total);
        event.setPercentage((int) ((current * 100.0) / total));
        event.setStatus(data.getValidationResult().getSuccess() ? "success" : "failed");
        event.setMessage("Importing: " + data.getSofProxyName());
        event.setData(data);

        String jsonEvent = objectMapper.writeValueAsString(event);
        emitter.send(SseEmitter.event().name("progress").data(jsonEvent));
    }

    /**
     * 發送完成事件
     */
    private void sendCompleteEvent(SseEmitter emitter, List<?> results,
            int successCount, int failureCount, String summary)
            throws IOException {
        ImportExportProgressEvent event = new ImportExportProgressEvent("complete");
        event.setSuccessCount(successCount);
        event.setFailureCount(failureCount);
        event.setTotalCount(successCount + failureCount);
        event.setSummary(summary);
        event.setData(results);

        String jsonEvent = objectMapper.writeValueAsString(event);
        emitter.send(SseEmitter.event().name("complete").data(jsonEvent));
    }

    /**
     * 發送錯誤事件
     */
    private void sendErrorEvent(SseEmitter emitter, String message) throws IOException {
        ImportExportProgressEvent event = new ImportExportProgressEvent("error");
        event.setMessage(message);

        String jsonEvent = objectMapper.writeValueAsString(event);
        emitter.send(SseEmitter.event().name("error").data(jsonEvent));
    }

    protected DgrSmartOnFhirProxyDao getDgrSmartOnFhirProxyDao() {
        return dgrSmartOnFhirProxyDao;
    }

    protected DgrSmartOnFhirProxyDiversionDao getDgrSmartOnFhirProxyDiversionDao() {
        return dgrSmartOnFhirProxyDiversionDao;
    }

    protected DgrSmartOnFhirProxyStickyDao getDgrSmartOnFhirProxyStickyDao() {
        return dgrSmartOnFhirProxyStickyDao;
    }
}
