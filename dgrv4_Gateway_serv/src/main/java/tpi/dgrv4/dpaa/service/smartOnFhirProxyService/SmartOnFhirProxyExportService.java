package tpi.dgrv4.dpaa.service.smartOnFhirProxyService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PreDestroy;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.service.SmartOnFhirProxyRouteContext;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.tool.IdCodec;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.ImportExportProgressEvent;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.SmartOnFhirProxyDiversionDto;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.SmartOnFhirProxyExportDto;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.SmartOnFhirProxySearchReq;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.SmartOnFhirProxyStickyImportDto;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxy;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxyDiversion;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxySticky;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyDao;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyDiversionDao;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyStickyDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class SmartOnFhirProxyExportService {

    private TPILogger logger = TPILogger.tl;

    private DgrSmartOnFhirProxyDao dgrSmartOnFhirProxyDao;
    private DgrSmartOnFhirProxyDiversionDao dgrSmartOnFhirProxyDiversionDao;
    private DgrSmartOnFhirProxyStickyDao dgrSmartOnFhirProxyStickyDao;
    private ObjectMapper objectMapper;
    private ExecutorService executorService;

    @Autowired
    public SmartOnFhirProxyExportService(
            DgrSmartOnFhirProxyDao dgrSmartOnFhirProxyDao,
            DgrSmartOnFhirProxyDiversionDao dgrSmartOnFhirProxyDiversionDao,
            DgrSmartOnFhirProxyStickyDao dgrSmartOnFhirProxyStickyDao,
            ObjectMapper objectMapper) {
        this.dgrSmartOnFhirProxyDao = dgrSmartOnFhirProxyDao;
        this.dgrSmartOnFhirProxyDiversionDao = dgrSmartOnFhirProxyDiversionDao;
        this.dgrSmartOnFhirProxyStickyDao = dgrSmartOnFhirProxyStickyDao;
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
     * 匯出 Smart on FHIR Proxy 資料（使用 SSE 推送進度）
     * 
     * @param auth 授權資訊
     * @param req  匯出請求（篩選條件）
     * @return SseEmitter
     */
    public SseEmitter exportSmartOnFhirProxyData(TsmpAuthorization auth, SmartOnFhirProxySearchReq req) {
        // 建立 SseEmitter，設定 timeout 為 10 分鐘
        SseEmitter emitter = new SseEmitter(600000L);

        // 設定完成與逾時處理
        emitter.onCompletion(() -> {
            this.logger.debug("Export SSE completed");
        });

        emitter.onTimeout(() -> {
            this.logger.warn("Export SSE timeout");
            emitter.complete();
        });

        emitter.onError((ex) -> {
            this.logger.error("Export SSE error: " + StackTraceUtil.logStackTrace(ex));
        });

        // 在背景執行緒中執行匯出作業
        executorService.execute(() -> {
            try {
                performExport(emitter, auth, req);
            } catch (Exception e) {
                this.logger.error(StackTraceUtil.logStackTrace(e));
                try {
                    sendErrorEvent(emitter, "匯出失敗：" + e.getMessage());
                } catch (IOException ioEx) {
                    this.logger.error("Failed to send error event: " + StackTraceUtil.logStackTrace(ioEx));
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * 執行匯出作業
     */
    private void performExport(SseEmitter emitter, TsmpAuthorization auth, SmartOnFhirProxySearchReq req) throws IOException {
        
        // 1. 查詢符合條件的資料（不分頁）
        List<DgrSmartOnFhirProxy> proxyList = getDgrSmartOnFhirProxyDao().findByFiltersForExport(
                req.getKeywords(),
                req.getSofProxyStatus());

        int totalCount = proxyList.size();

        if (totalCount == 0) {
            // 沒有資料
            sendCompleteEvent(emitter, 0, 0, "No data matched the criteria");
            emitter.complete();
            return;
        }

        // 2. 批次查詢關聯的 Diversion 資料
        Map<Long, List<SmartOnFhirProxyDiversionDto>> diversionMap = queryDiversionMap(proxyList);

        // 2.5 批次查詢關聯的 Sticky 資料
        Map<Long, List<DgrSmartOnFhirProxySticky>> stickyMap = queryStickyMap(proxyList);

        // 3. 逐筆轉換並發送進度
        List<SmartOnFhirProxyExportDto> exportDataList = new ArrayList<>();

        for (int i = 0; i < proxyList.size(); i++) {
            DgrSmartOnFhirProxy proxy = proxyList.get(i);

            // 轉換為匯出 DTO
            SmartOnFhirProxyExportDto exportDto = convertToExportDto(proxy, diversionMap, stickyMap);
            exportDataList.add(exportDto);

            // 發送進度事件
            sendProgressEvent(emitter, i + 1, totalCount, exportDto);
        }

        // 4. 發送完成事件（包含完整的 JSON 資料）
        sendCompleteEventWithData(emitter, exportDataList, totalCount);

        // 5. 完成 SSE
        emitter.complete();
    }

    /**
     * 批次查詢 Diversion 資料
     */
    private Map<Long, List<SmartOnFhirProxyDiversionDto>> queryDiversionMap(List<DgrSmartOnFhirProxy> proxyList) {
        if (proxyList == null || proxyList.isEmpty()) {
            return new HashMap<>();
        }

        // 收集所有 sofProxyId
        List<Long> proxyIds = proxyList.stream()
                .map(DgrSmartOnFhirProxy::getSofProxyId)
                .collect(Collectors.toList());

        // 批次查詢 Diversion
        List<DgrSmartOnFhirProxyDiversion> diversionList = getDgrSmartOnFhirProxyDiversionDao()
                .findBySofProxyIdIn(proxyIds);

        // 轉換為 DTO 並按 sofProxyId 分組
        Map<Long, List<SmartOnFhirProxyDiversionDto>> diversionMap = new HashMap<>();
        for (DgrSmartOnFhirProxyDiversion diversion : diversionList) {
            Long proxyId = diversion.getSofProxyId();
            if (!diversionMap.containsKey(proxyId)) {
                diversionMap.put(proxyId, new ArrayList<>());
            }
            diversionMap.get(proxyId).add(convertDiversionToDto(diversion));
        }

        return diversionMap;
    }

    /**
     * 批次查詢 Sticky 資料
     */
    private Map<Long, List<DgrSmartOnFhirProxySticky>> queryStickyMap(List<DgrSmartOnFhirProxy> proxyList) {
        if (proxyList == null || proxyList.isEmpty()) {
            return new HashMap<>();
        }

        List<Long> proxyIds = proxyList.stream()
                .map(DgrSmartOnFhirProxy::getSofProxyId)
                .collect(Collectors.toList());

        List<DgrSmartOnFhirProxySticky> stickyList = getDgrSmartOnFhirProxyStickyDao()
                .findBySofProxyIdIn(proxyIds);

        Map<Long, List<DgrSmartOnFhirProxySticky>> stickyMap = new HashMap<>();
        for (DgrSmartOnFhirProxySticky sticky : stickyList) {
            stickyMap.computeIfAbsent(sticky.getSofProxyId(), k -> new ArrayList<>()).add(sticky);
        }

        return stickyMap;
    }

    /**
     * 轉換為匯出 DTO（包含審計欄位）
     */
    private SmartOnFhirProxyExportDto convertToExportDto(
            DgrSmartOnFhirProxy entity,
            Map<Long, List<SmartOnFhirProxyDiversionDto>> diversionMap,
            Map<Long, List<DgrSmartOnFhirProxySticky>> stickyMap) {

        SmartOnFhirProxyExportDto dto = new SmartOnFhirProxyExportDto();

        // 主表欄位
        dto.setSofProxyId(IdCodec.toString(entity.getSofProxyId()));
        dto.setSofProxyName(entity.getSofProxyName());

        // 設置狀態（轉換為 code）
        if (entity.getSofProxyStatus() != null) {
            dto.setSofProxyStatus(entity.getSofProxyStatus().getCode());
        }

        dto.setSofProxyRemark(entity.getSofProxyRemark());

        // 設置其他安全性設定（轉換為 code）
        if (entity.getSofProxyAccessToken() != null) {
            dto.setSofProxyAccessToken(entity.getSofProxyAccessToken().getCode());
        }
        if (entity.getSofProxySqlInjection() != null) {
            dto.setSofProxySqlInjection(entity.getSofProxySqlInjection().getCode());
        }
        if (entity.getSofProxyTraffic() != null) {
            dto.setSofProxyTraffic(entity.getSofProxyTraffic().getCode());
        }
        if (entity.getSofProxyXss() != null) {
            dto.setSofProxyXss(entity.getSofProxyXss().getCode());
        }
        if (entity.getSofProxyXxe() != null) {
            dto.setSofProxyXxe(entity.getSofProxyXxe().getCode());
        }
        if (entity.getSofProxyShowLog() != null) {
            dto.setSofProxyShowLog(entity.getSofProxyShowLog().getCode());
        }
        if (entity.getSofProxySticky() != null) {
            dto.setSofProxySticky(entity.getSofProxySticky().getCode());
        }
        if (entity.getSofProxyUrlRewrite() != null) {
            dto.setSofProxyUrlRewrite(entity.getSofProxyUrlRewrite().getCode());
        }
        dto.setSofProxyTps(entity.getSofProxyTps());
        dto.setSofProxyIgnoreApi(entity.getSofProxyIgnoreApi());
        dto.setSofProxyClientId(entity.getSofProxyClientId());

        // 設置關聯的 Diversion 列表
        List<SmartOnFhirProxyDiversionDto> diversionList = diversionMap.get(entity.getSofProxyId());
        dto.setDiversionList(diversionList != null ? diversionList : new ArrayList<>());

        // 設置關聯的 Sticky 列表（含 diversionIndex 計算）
        List<DgrSmartOnFhirProxySticky> stickies = stickyMap.get(entity.getSofProxyId());
        if (stickies != null && !stickies.isEmpty()) {
            // 建立 diversionId → index 映射表
            List<SmartOnFhirProxyDiversionDto> finalDiversionList = dto.getDiversionList();
            Map<String, Integer> diversionIdToIndex = new HashMap<>();
            for (int i = 0; i < finalDiversionList.size(); i++) {
                diversionIdToIndex.put(finalDiversionList.get(i).getSofProxyDiversionId(), i);
            }

            List<SmartOnFhirProxyStickyImportDto> stickyDtoList = new ArrayList<>();
            for (DgrSmartOnFhirProxySticky sticky : stickies) {
                SmartOnFhirProxyStickyImportDto stickyDto = convertStickyToImportDto(sticky);
                // 計算 diversionIndex
                String divIdStr = IdCodec.toString(sticky.getSofProxyDiversionId());
                stickyDto.setDiversionIndex(diversionIdToIndex.get(divIdStr));
                stickyDtoList.add(stickyDto);
            }
            dto.setStickyList(stickyDtoList);
        }

        // 審計欄位（必須匯出）
        dto.setCreateDateTime(entity.getCreateDateTime());
        dto.setCreateUser(entity.getCreateUser());
        dto.setUpdateDateTime(entity.getUpdateDateTime());
        dto.setUpdateUser(entity.getUpdateUser());
        dto.setVersion(IdCodec.toString(entity.getVersion()));

        return dto;
    }

    /**
     * 轉換 Diversion Entity 為 DTO
     */
    private SmartOnFhirProxyDiversionDto convertDiversionToDto(DgrSmartOnFhirProxyDiversion entity) {
        SmartOnFhirProxyDiversionDto dto = new SmartOnFhirProxyDiversionDto();

        dto.setSofProxyDiversionId(IdCodec.toString(entity.getSofProxyDiversionId()));
        dto.setSofProxyId(IdCodec.toString(entity.getSofProxyId()));
        dto.setSofProxyDiversionProbability(entity.getSofProxyDiversionProbability());
        dto.setSofProxyDiversionUrl(
                new SmartOnFhirProxyRouteContext.ParsedUrl(
                        entity.getSofProxyDiversionUrl(),
                        entity.getSofProxyDiversionFhirBasePath()).toFullUrl());
        dto.setCreateDateTime(entity.getCreateDateTime());
        dto.setCreateUser(entity.getCreateUser());
        dto.setUpdateDateTime(entity.getUpdateDateTime());
        dto.setUpdateUser(entity.getUpdateUser());
        dto.setVersion(IdCodec.toString(entity.getVersion()));

        return dto;
    }

    /**
     * 發送進度事件
     */
    private void sendProgressEvent(SseEmitter emitter, int current, int total, SmartOnFhirProxyExportDto data)
            throws IOException {
        ImportExportProgressEvent event = new ImportExportProgressEvent("progress");
        event.setCurrent(current);
        event.setTotal(total);
        event.setPercentage((int) ((current * 100.0) / total));
        event.setStatus("processing");
        event.setMessage("Exporting: " + data.getSofProxyName());
        event.setData(data);

        String jsonEvent = objectMapper.writeValueAsString(event);
        emitter.send(SseEmitter.event().name("progress").data(jsonEvent));
    }

    /**
     * 發送完成事件（包含完整資料）
     */
    private void sendCompleteEventWithData(SseEmitter emitter, List<SmartOnFhirProxyExportDto> dataList, int totalCount)
            throws IOException {
        ImportExportProgressEvent event = new ImportExportProgressEvent("complete");
        event.setSuccessCount(totalCount);
        event.setFailureCount(0);
        event.setTotalCount(totalCount);
        event.setSummary("Export completed, total " + totalCount + " records");    
        event.setData(dataList);
        event.setFileName(getFileName());

        String jsonEvent = objectMapper.writeValueAsString(event);
        emitter.send(SseEmitter.event().name("complete").data(jsonEvent));
    }
    
    /**
     * Generates a file name for the export file based on the current timestamp.
     * <p>
     * 根據當前時間戳為匯出檔案生成檔案名。
     *
     * @return A formatted file name string, e.g., "exportAPI_2023-10-27
     *         10-30-00.json".
     *         格式化的檔案名字串，例如 "exportAPI_2023-10-27 10-30-00.json"。
     */
    private String getFileName() {
        Date now = DateTimeUtil.now();
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(now);
        return String.format("exportSmartOnFhirProxy_%s.json", dateTime);
    }

    /**
     * 發送完成事件（無資料）
     */
    private void sendCompleteEvent(SseEmitter emitter, int successCount, int failureCount, String summary)
            throws IOException {
        ImportExportProgressEvent event = new ImportExportProgressEvent("complete");
        event.setSuccessCount(successCount);
        event.setFailureCount(failureCount);
        event.setTotalCount(successCount + failureCount);
        event.setSummary(summary);

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

    /**
     * 轉換 Sticky Entity 為 ImportDto（匯出用）
     */
    private SmartOnFhirProxyStickyImportDto convertStickyToImportDto(DgrSmartOnFhirProxySticky entity) {
        SmartOnFhirProxyStickyImportDto dto = new SmartOnFhirProxyStickyImportDto();
        dto.setSofProxyStickyId(IdCodec.toString(entity.getSofProxyStickyId()));
        dto.setSofProxyDiversionId(IdCodec.toString(entity.getSofProxyDiversionId()));
        dto.setSofProxyStickyType(entity.getSofProxyStickyType());
        dto.setSofProxyStickyTypeId(entity.getSofProxyStickyTypeId());
        dto.setSofProxyStickyVerb(entity.getSofProxyStickyVerb());
        dto.setSofProxyStickyPath(entity.getSofProxyStickyPath());
        if (entity.getSofProxyStickyInteraction() != null) {
            dto.setSofProxyStickyInteraction(entity.getSofProxyStickyInteraction().name());
        }
        return dto;
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
