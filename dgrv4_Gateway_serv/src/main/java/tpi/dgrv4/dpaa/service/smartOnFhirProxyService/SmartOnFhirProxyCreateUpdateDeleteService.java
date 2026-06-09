package tpi.dgrv4.dpaa.service.smartOnFhirProxyService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.tool.IdCodec;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.DPB0312Req;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.DPB0312Resp;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.SmartOnFhirProxyDiversionDto;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.SmartOnFhirProxyDiversionUpdateDto;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.SmartOnFhirProxyDto;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.SmartOnFhirProxyUpdateDto;
import tpi.dgrv4.entity.constant.CodeEnums;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxy;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxyDiversion;
import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxy_;
import tpi.dgrv4.gateway.service.SmartOnFhirProxyRouteContext;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyDao;
import tpi.dgrv4.entity.repository.DgrSmartOnFhirProxyDiversionDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class SmartOnFhirProxyCreateUpdateDeleteService {

    private TPILogger logger = TPILogger.tl;

    /** 預先編譯的正則表達式：sofProxyName 只能包含英文字母和數字 */
    private static final Pattern PROXY_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

    private DgrSmartOnFhirProxyDao dgrSmartOnFhirProxyDao;
    private DgrSmartOnFhirProxyDiversionDao dgrSmartOnFhirProxyDiversionDao;
    private TsmpClientDao tsmpClientDao;
    private tpi.dgrv4.gateway.service.SmartOnFhirProxyStickyService stickyService;

    @Autowired
    public SmartOnFhirProxyCreateUpdateDeleteService(
            DgrSmartOnFhirProxyDao dgrSmartOnFhirProxyDao,
            DgrSmartOnFhirProxyDiversionDao dgrSmartOnFhirProxyDiversionDao,
            TsmpClientDao tsmpClientDao,
            tpi.dgrv4.gateway.service.SmartOnFhirProxyStickyService stickyService) {
        this.dgrSmartOnFhirProxyDao = dgrSmartOnFhirProxyDao;
        this.dgrSmartOnFhirProxyDiversionDao = dgrSmartOnFhirProxyDiversionDao;
        this.tsmpClientDao = tsmpClientDao;
        this.stickyService = stickyService;
    }

    @Transactional
    public SmartOnFhirProxyDto addSmartOnFhirProxy(TsmpAuthorization auth, SmartOnFhirProxyDto reqDto) {
        try {
        	
            // 1. 驗證必填欄位
            validateRequiredFieldsForCreate(reqDto);

            // 2. 驗證欄位格式與範圍（使用 -1 表示非批次操作）
            validateProxyFieldFormats(reqDto.getSofProxyName(), reqDto.getSofProxyRemark(),
                    reqDto.getSofProxyIgnoreApi(), reqDto.getSofProxyTps(), reqDto.getSofProxyStatus(), -1);

            // 3. 驗證 Diversion（使用 -1 表示非批次操作）
            validateDiversionList(reqDto.getDiversionList(), -1);

            // 4. 檢查名稱唯一性（使用 -1 表示非批次操作，null 表示不排除任何 ID）
            checkProxyNameUniqueness(reqDto.getSofProxyName(), null, -1);

            // 5. 驗證 Client IDs（當提供時）
            validateClientIds(reqDto.getSofProxyClientId(), "sofProxyClientId");

            // 6. 建立並儲存 Proxy
            DgrSmartOnFhirProxy proxy = createProxyEntity(reqDto, auth);
            proxy = getDgrSmartOnFhirProxyDao().save(proxy);

            // 7. 建立並儲存 Diversions
            List<DgrSmartOnFhirProxyDiversion> diversions = createDiversionEntities(
                    reqDto.getDiversionList(), proxy.getSofProxyId(), auth);
            diversions = saveAllDgrSmartOnFhirProxyDiversion(diversions);

            // 8. 組裝回應
            return convertToDto(proxy, diversions);

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            this.logger.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }

    protected List<DgrSmartOnFhirProxyDiversion> saveAllDgrSmartOnFhirProxyDiversion(
            List<DgrSmartOnFhirProxyDiversion> diversions) {
        return getDgrSmartOnFhirProxyDiversionDao().saveAll(diversions);
    }

    /**
     * 驗證必填欄位（Create 專用）
     */
    private void validateRequiredFieldsForCreate(SmartOnFhirProxyDto reqDto) {

        // sofProxyName 必填
        if (!StringUtils.hasText(reqDto.getSofProxyName())) {
            String msg = "Field 'sofProxyName' is required. Please provide a value for this field.";
            this.logger.error(msg);
            throw TsmpDpAaRtnCode._1559.throwing(msg);
        }

        // diversionList 必填
        if (reqDto.getDiversionList() == null || reqDto.getDiversionList().isEmpty()) {
            String msg = "Field 'diversionList' is required and must have at least 1 item. Please provide at least one diversion.";
            this.logger.error(msg);
            throw TsmpDpAaRtnCode._1559.throwing(msg);
        }
    }

    /**
     * 驗證 Proxy 欄位格式與範圍（通用方法）
     * 
     * @param sofProxyName      Proxy 名稱
     * @param sofProxyRemark    Proxy 備註
     * @param sofProxyIgnoreApi Proxy 忽略 API
     * @param sofProxyTps       Proxy TPS
     * @param sofProxyStatus    Proxy 狀態
     * @param index             索引（-1 表示非批次操作，>= 0 表示批次操作中的位置）
     */
    private void validateProxyFieldFormats(String sofProxyName, String sofProxyRemark,
            String sofProxyIgnoreApi, Integer sofProxyTps, String sofProxyStatus, int index) {

        String fieldPrefix = index >= 0 ? "Proxy[" + index + "]." : "";

        // sofProxyName: 長度 1-100，只能英文數字
        if (sofProxyName != null) {
            if (sofProxyName.length() > 100) {
                String msg = fieldPrefix + "sofProxyName length exceeds 100: " + sofProxyName.length();
                this.logger.error(msg);
                throw TsmpDpAaRtnCode._1559.throwing(msg);
            }

            // 使用預先編譯的 Pattern 進行驗證
            if (!PROXY_NAME_PATTERN.matcher(sofProxyName).matches()) {
                String msg = fieldPrefix + "sofProxyName contains invalid characters: " + sofProxyName;
                this.logger.error(msg);
                throw TsmpDpAaRtnCode._1559.throwing(msg);
            }
        }

        // sofProxyRemark: 長度 <= 500
        if (sofProxyRemark != null && sofProxyRemark.length() > 500) {
            String msg = fieldPrefix + "sofProxyRemark length exceeds 500: " + sofProxyRemark.length();
            this.logger.error(msg);
            throw TsmpDpAaRtnCode._1559.throwing(msg);
        }

        // sofProxyIgnoreApi: 長度 <= 4000
        if (sofProxyIgnoreApi != null && sofProxyIgnoreApi.length() > 4000) {
            String msg = fieldPrefix + "sofProxyIgnoreApi length exceeds 4000: " + sofProxyIgnoreApi.length();
            this.logger.error(msg);
            throw TsmpDpAaRtnCode._1559.throwing(msg);
        }

        // sofProxyTps: 範圍 0-999999
        if (sofProxyTps != null && (sofProxyTps < 0 || sofProxyTps > 999999)) {
            String msg = fieldPrefix + "sofProxyTps out of range: " + sofProxyTps;
            this.logger.error(msg);
            throw TsmpDpAaRtnCode._1559.throwing(msg);
        }

        // sofProxyStatus: 驗證枚舉值（如果有提供）
        if (StringUtils.hasText(sofProxyStatus)) {
            if (!CodeEnums.tryFromCode(DgrSmartOnFhirProxy.Status.class, sofProxyStatus).isPresent()) {
                String msg = fieldPrefix + "Invalid sofProxyStatus: " + sofProxyStatus;
                this.logger.error(msg);
                throw TsmpDpAaRtnCode._1559.throwing(msg);
            }
        }
    }

    /**
     * 驗證 Diversion 列表（通用方法）
     * 
     * @param diversionList Diversion 列表
     * @param proxyIndex    Proxy 索引（-1 表示非批次操作，>= 0 表示批次操作中的位置）
     */
    private void validateDiversionList(List<SmartOnFhirProxyDiversionDto> diversionList, int proxyIndex) {
        String proxyPrefix = proxyIndex >= 0 ? "Proxy[" + proxyIndex + "]." : "";
        int totalProbability = 0;

        for (int i = 0; i < diversionList.size(); i++) {
            SmartOnFhirProxyDiversionDto div = diversionList.get(i);

            // 驗證 URL 必填
            if (!StringUtils.hasText(div.getSofProxyDiversionUrl())) {
                String msg = proxyPrefix + "Diversion[" + i
                        + "].sofProxyDiversionUrl is required. Please provide a URL.";
                this.logger.error(msg);
                throw TsmpDpAaRtnCode._1559.throwing(msg);
            }

            // 驗證 URL 格式
            String urlString = div.getSofProxyDiversionUrl();
            if (!isValidUrl(urlString)) {
                String msg = proxyPrefix + "Diversion[" + i + "] has invalid URL format: '" + urlString
                        + "'. Please provide a valid HTTP/HTTPS URL.";
                this.logger.error(msg);
                throw TsmpDpAaRtnCode._1559.throwing(msg);
            }

            // 驗證 probability 必填
            if (div.getSofProxyDiversionProbability() == null) {
                String msg = proxyPrefix + "Diversion[" + i
                        + "].sofProxyDiversionProbability is required. Please provide a probability value.";
                this.logger.error(msg);
                throw TsmpDpAaRtnCode._1559.throwing(msg);
            }

            // 驗證 probability 範圍 1-100
            Integer prob = div.getSofProxyDiversionProbability();
            if (prob < 1 || prob > 100) {
                String msg = proxyPrefix + "Diversion[" + i + "].sofProxyDiversionProbability out of range: " + prob
                        + ". Must be between 1-100.";
                this.logger.error(msg);
                throw TsmpDpAaRtnCode._1559.throwing(msg);
            }

            totalProbability += prob;
        }

        // 驗證權重總和 = 100
        if (totalProbability != 100) {
            String msg = proxyPrefix + "Diversion probability sum must equal 100 (current: " + totalProbability
                    + "). Please adjust the probability values.";
            this.logger.error(msg);
            throw TsmpDpAaRtnCode._1559.throwing(msg);
        }
    }

    /**
     * 驗證 URL 格式
     */
    private boolean isValidUrl(String urlString) {
        // 檢查長度限制
        if (urlString == null || urlString.length() > 1000) {
            return false;
        }

        try {
            URI uri = new URI(urlString);
            String scheme = uri.getScheme();
            // 只允許 http 和 https，且必須是絕對 URI
            if (scheme == null) {
                return false;
            }
            return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * 檢查 Proxy 名稱唯一性（通用方法）
     * 
     * @param sofProxyName   Proxy 名稱
     * @param excludeProxyId 要排除的 Proxy ID（更新時使用，null 表示不排除）
     * @param index          索引（-1 表示非批次操作，>= 0 表示批次操作中的位置）
     */
    private void checkProxyNameUniqueness(String sofProxyName, Long excludeProxyId, int index) {
        List<DgrSmartOnFhirProxy> existing = getDgrSmartOnFhirProxyDao().findBySofProxyName(sofProxyName);

        // 如果是更新操作，排除自己
        if (excludeProxyId != null) {
            existing = existing.stream()
                    .filter(p -> !p.getSofProxyId().equals(excludeProxyId))
                    .collect(Collectors.toList());
        }

        if (!existing.isEmpty()) {
            String prefix = (index >= 0 ? "Proxy[" + index + "] " : "");
            String msg = prefix + "Proxy name '" + sofProxyName + "' already exists. Please use a different name.";
            this.logger.error(msg);
            throw TsmpDpAaRtnCode._1559.throwing(msg);
        }
    }

    /**
     * 建立 Proxy Entity
     */
    private DgrSmartOnFhirProxy createProxyEntity(SmartOnFhirProxyDto reqDto, TsmpAuthorization auth) {
        DgrSmartOnFhirProxy proxy = new DgrSmartOnFhirProxy();

        // 必填欄位
        proxy.setSofProxyName(reqDto.getSofProxyName());

        // 選填欄位（使用預設值）
        // Status: 預設 'Y'
        if (StringUtils.hasText(reqDto.getSofProxyStatus())) {
            DgrSmartOnFhirProxy.Status status = CodeEnums
                    .tryFromCode(DgrSmartOnFhirProxy.Status.class, reqDto.getSofProxyStatus())
                    .orElse(DgrSmartOnFhirProxy.Status.ENABLED);
            proxy.setSofProxyStatus(status);
        } else {
            proxy.setSofProxyStatus(DgrSmartOnFhirProxy.Status.ENABLED);
        }

        // Remark: 預設 ""
        proxy.setSofProxyRemark(
                StringUtils.hasText(reqDto.getSofProxyRemark()) ? reqDto.getSofProxyRemark() : "");

        // 安全性設定：預設 'N' (DISABLED)
        proxy.setSofProxyAccessToken(getStatusOrDefault(reqDto.getSofProxyAccessToken(),
                DgrSmartOnFhirProxy.Status.DISABLED));
        proxy.setSofProxySqlInjection(getStatusOrDefault(reqDto.getSofProxySqlInjection(),
                DgrSmartOnFhirProxy.Status.DISABLED));
        proxy.setSofProxyTraffic(getStatusOrDefault(reqDto.getSofProxyTraffic(),
                DgrSmartOnFhirProxy.Status.DISABLED));
        proxy.setSofProxyXss(getStatusOrDefault(reqDto.getSofProxyXss(),
                DgrSmartOnFhirProxy.Status.DISABLED));
        proxy.setSofProxyXxe(getStatusOrDefault(reqDto.getSofProxyXxe(),
                DgrSmartOnFhirProxy.Status.DISABLED));
        proxy.setSofProxyShowLog(getStatusOrDefault(reqDto.getSofProxyShowLog(),
                DgrSmartOnFhirProxy.Status.DISABLED));
        proxy.setSofProxySticky(getStatusOrDefault(reqDto.getSofProxySticky(),
                DgrSmartOnFhirProxy.Status.ENABLED));
        proxy.setSofProxyUrlRewrite(getStatusOrDefault(reqDto.getSofProxyUrlRewrite(),
                DgrSmartOnFhirProxy.Status.ENABLED));

        // TPS: 預設 0
        proxy.setSofProxyTps(reqDto.getSofProxyTps() != null ? reqDto.getSofProxyTps() : 0);

        // IgnoreApi: 預設 ""
        proxy.setSofProxyIgnoreApi(
                StringUtils.hasText(reqDto.getSofProxyIgnoreApi()) ? reqDto.getSofProxyIgnoreApi() : "");

        // ClientId: 預設 []
        proxy.setSofProxyClientId(
                reqDto.getSofProxyClientId() != null ? reqDto.getSofProxyClientId() : new ArrayList<>());

        // 設定系統欄位
        proxy.setCreateDateTime(DateTimeUtil.now());
        proxy.setCreateUser(auth.getUserName());

        return proxy;
    }

    /**
     * 取得狀態枚舉或預設值
     */
    private DgrSmartOnFhirProxy.Status getStatusOrDefault(String statusCode,
            DgrSmartOnFhirProxy.Status defaultStatus) {
        if (!StringUtils.hasText(statusCode)) {
            return defaultStatus;
        }
        return CodeEnums.tryFromCode(DgrSmartOnFhirProxy.Status.class, statusCode)
                .orElse(defaultStatus);
    }

    /**
     * 建立 Diversion Entity 列表
     */
    private List<DgrSmartOnFhirProxyDiversion> createDiversionEntities(
            List<SmartOnFhirProxyDiversionDto> diversionDtos,
            Long sofProxyId,
            TsmpAuthorization auth) {

        return diversionDtos.stream()
                .map(divDto -> {
                    SmartOnFhirProxyRouteContext.ParsedUrl parsed =
                            SmartOnFhirProxyRouteContext.parseDiversionUrl(divDto.getSofProxyDiversionUrl());
                    DgrSmartOnFhirProxyDiversion div = new DgrSmartOnFhirProxyDiversion();
                    div.setSofProxyId(sofProxyId);
                    div.setSofProxyDiversionProbability(divDto.getSofProxyDiversionProbability());
                    div.setSofProxyDiversionUrl(parsed.baseUrl());
                    div.setSofProxyDiversionFhirBasePath(parsed.fhirBasePath());
                    div.setCreateDateTime(DateTimeUtil.now());
                    div.setCreateUser(auth.getUserName());
                    return div;
                })
                .collect(Collectors.toList());
    }

    /**
     * 轉換 Entity 為 DTO
     */
    private SmartOnFhirProxyDto convertToDto(
            DgrSmartOnFhirProxy entity,
            List<DgrSmartOnFhirProxyDiversion> diversionList) {

        SmartOnFhirProxyDto dto = new SmartOnFhirProxyDto();

        dto.setSofProxyId(IdCodec.toString(entity.getSofProxyId()));
        dto.setSofProxyName(entity.getSofProxyName());

        // 設置狀態
        if (entity.getSofProxyStatus() != null) {
            dto.setSofProxyStatus(entity.getSofProxyStatus().getCode());
            dto.setSofProxyStatusName(entity.getSofProxyStatus().getName());
        }

        dto.setSofProxyRemark(entity.getSofProxyRemark());

        // 設置其他安全性設定
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

        // 設置 Diversion 列表
        List<SmartOnFhirProxyDiversionDto> diversionDtos = diversionList.stream()
                .map(this::convertDiversionToDto)
                .collect(Collectors.toList());
        dto.setDiversionList(diversionDtos);

        dto.setCreateDateTime(entity.getCreateDateTime());
        dto.setCreateUser(entity.getCreateUser());
        dto.setUpdateDateTime(entity.getUpdateDateTime());
        dto.setUpdateUser(entity.getUpdateUser());
        dto.setVersion((entity.getVersion().toString()));

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
        dto.setVersion((entity.getVersion().toString()));

        return dto;
    }

    protected DgrSmartOnFhirProxyDao getDgrSmartOnFhirProxyDao() {
        return dgrSmartOnFhirProxyDao;
    }

    protected DgrSmartOnFhirProxyDiversionDao getDgrSmartOnFhirProxyDiversionDao() {
        return dgrSmartOnFhirProxyDiversionDao;
    }
    
	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}


    /**
     * DPB0312: 批次更新 Smart on FHIR Proxy
     * 
     * @param auth    授權資訊
     * @param reqBody 批次更新請求
     * @return 批次更新回應
     */
    @Transactional
    public DPB0312Resp batchUpdateSmartOnFhirProxy(TsmpAuthorization auth, DPB0312Req reqBody) {
        try {
            List<SmartOnFhirProxyUpdateDto> proxyList = reqBody.getProxyList();

            // 1. 前置驗證
            validateBatchUpdateRequest(proxyList);

            // 2. 批次內唯一性檢查
            checkDuplicateNamesInBatch(proxyList);

            // 3. 逐筆處理並更新
            List<SmartOnFhirProxyDto> updatedProxies = new ArrayList<>();
            for (int i = 0; i < proxyList.size(); i++) {
                SmartOnFhirProxyUpdateDto updateDto = proxyList.get(i);
                SmartOnFhirProxyDto updatedProxy = updateSingleProxy(auth, updateDto, i);
                updatedProxies.add(updatedProxy);
            }

            // 4. 組裝回應
            DPB0312Resp resp = new DPB0312Resp();
            resp.setSuccessCount(updatedProxies.size());
            resp.setUpdatedProxies(updatedProxies);

            return resp;

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            this.logger.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }

    /**
     * 驗證批次更新請求
     */
    private void validateBatchUpdateRequest(List<SmartOnFhirProxyUpdateDto> proxyList) {
        // 檢查列表不為空
        if (proxyList == null || proxyList.isEmpty()) {
            this.logger.error("Batch update list is empty");
            throw TsmpDpAaRtnCode._1559.throwing("Batch update list cannot be empty");
        }

        // 檢查每個項目的必填欄位
        for (int i = 0; i < proxyList.size(); i++) {
            SmartOnFhirProxyUpdateDto dto = proxyList.get(i);

            if (dto.getSofProxyId() == null) {
                this.logger.error("Proxy[" + i + "] sofProxyId is required");
                throw TsmpDpAaRtnCode._1559.throwing("Proxy[" + i + "].sofProxyId is required");
            }

            if (dto.getVersion() == null) {
                this.logger.error("Proxy[" + i + "] version is required");
                throw TsmpDpAaRtnCode._1559.throwing("Proxy[" + i + "].version is required");
            }
        }
    }

    /**
     * 檢查批次內 sofProxyName 是否重複
     */
    private void checkDuplicateNamesInBatch(List<SmartOnFhirProxyUpdateDto> proxyList) {
        Map<String, Integer> nameIndexMap = new HashMap<>();

        for (int i = 0; i < proxyList.size(); i++) {
            SmartOnFhirProxyUpdateDto dto = proxyList.get(i);

            // 只檢查有更新 name 的項目
            if (dto.getSofProxyName() != null && dto.getSofProxyName().isPresent()) {
                String newName = dto.getSofProxyName().get();

                if (newName != null && !newName.isEmpty()) {
                    if (nameIndexMap.containsKey(newName)) {
                        this.logger.error("Duplicate sofProxyName in batch: " + newName);
                        throw TsmpDpAaRtnCode._1559.throwing(
                                "Duplicate sofProxyName in batch at Proxy[" + nameIndexMap.get(newName) +
                                        "] and Proxy[" + i + "]: " + newName);
                    }
                    nameIndexMap.put(newName, i);
                }
            }
        }
    }

    /**
     * 更新單一 Proxy
     */
    private SmartOnFhirProxyDto updateSingleProxy(TsmpAuthorization auth,
            SmartOnFhirProxyUpdateDto updateDto, int index) {

        // 1. 轉換 String ID 為 Long
        Long sofProxyIdLong = IdCodec.parseLong(updateDto.getSofProxyId(), "Proxy[" + index + "].sofProxyId",
                logger).getOrThrow();

        Long versionLong = IdCodec.parseLong(updateDto.getVersion(), "Proxy[" + index + "].version",
                logger).getOrThrow();

        // 2. 查詢現有 Proxy
        Optional<DgrSmartOnFhirProxy> proxyOpt = getDgrSmartOnFhirProxyDao()
                .findById(sofProxyIdLong);

        if (!proxyOpt.isPresent()) {
            this.logger.error("Proxy[" + index + "] not found: " + updateDto.getSofProxyId());
            throw TsmpDpAaRtnCode._1559.throwing(
                    "Proxy[" + index + "] not found: sofProxyId=" + updateDto.getSofProxyId());
        }

        DgrSmartOnFhirProxy proxy = proxyOpt.get();

        // 3. 樂觀鎖檢查
        if (!proxy.getVersion().equals(versionLong)) {
            this.logger.error("Proxy[" + index + "] version mismatch: expected " +
                    proxy.getVersion() + ", got " + updateDto.getVersion());
            throw TsmpDpAaRtnCode._1559.throwing(
                    "Version mismatch for Proxy[" + index + "]: expected " +
                            proxy.getVersion() + ", found " + updateDto.getVersion());
        }

        // 4. 套用 JsonNullable 欄位更新
        applyJsonNullableUpdates(proxy, updateDto, index);

        // 5. 驗證更新後的欄位
        validateUpdatedProxy(proxy, updateDto, index);

        // 6. 檢查 sofProxyName 唯一性（排除自己，使用通用方法）
        if (updateDto.getSofProxyName() != null && updateDto.getSofProxyName().isPresent()) {
            String newName = updateDto.getSofProxyName().get();
            if (newName != null && !newName.isEmpty()) {
                checkProxyNameUniqueness(newName, proxy.getSofProxyId(), index);
            }
        }

        // 7. 驗證 Client IDs（如果有更新）
        if (updateDto.getSofProxyClientId() != null && updateDto.getSofProxyClientId().isPresent()) {
            validateClientIds(updateDto.getSofProxyClientId().get(), "Proxy[" + index + "].sofProxyClientId");
        }

        // 8. 處理 Diversion 增量更新
        List<DgrSmartOnFhirProxyDiversion> updatedDiversions = updateDiversions(proxy.getSofProxyId(), updateDto,
                index, auth);

        // 9. 設定系統欄位
        proxy.setUpdateDateTime(DateTimeUtil.now());
        proxy.setUpdateUser(auth.getUserName());

        // 10. 儲存 Proxy
        proxy = getDgrSmartOnFhirProxyDao().saveAndFlush(proxy);

        // 11. 轉換為 DTO
        return convertToDto(proxy, updatedDiversions);
    }

    /**
     * 套用 JsonNullable 欄位更新
     */
    private void applyJsonNullableUpdates(DgrSmartOnFhirProxy proxy,
            SmartOnFhirProxyUpdateDto updateDto, int index) {

        // sofProxyName
        if (updateDto.getSofProxyName() != null && updateDto.getSofProxyName().isPresent()) {
            proxy.setSofProxyName(updateDto.getSofProxyName().get());
        }

        // sofProxyStatus
        if (updateDto.getSofProxyStatus() != null && updateDto.getSofProxyStatus().isPresent()) {
            String statusCode = updateDto.getSofProxyStatus().get();
            if (statusCode != null) {
                proxy.setSofProxyStatus(getStatusOrDefault(statusCode,
                        DgrSmartOnFhirProxy.Status.ENABLED));
            }
        }

        // sofProxyRemark
        if (updateDto.getSofProxyRemark() != null && updateDto.getSofProxyRemark().isPresent()) {
            proxy.setSofProxyRemark(
                    updateDto.getSofProxyRemark().get() != null ? updateDto.getSofProxyRemark().get() : "");
        }

        // 安全性設定
        if (updateDto.getSofProxyAccessToken() != null && updateDto.getSofProxyAccessToken().isPresent()) {
            proxy.setSofProxyAccessToken(getStatusOrDefault(updateDto.getSofProxyAccessToken().get(),
                    DgrSmartOnFhirProxy.Status.DISABLED));
        }

        if (updateDto.getSofProxySqlInjection() != null && updateDto.getSofProxySqlInjection().isPresent()) {
            proxy.setSofProxySqlInjection(getStatusOrDefault(updateDto.getSofProxySqlInjection().get(),
                    DgrSmartOnFhirProxy.Status.DISABLED));
        }

        if (updateDto.getSofProxyTraffic() != null && updateDto.getSofProxyTraffic().isPresent()) {
            proxy.setSofProxyTraffic(getStatusOrDefault(updateDto.getSofProxyTraffic().get(),
                    DgrSmartOnFhirProxy.Status.DISABLED));
        }

        if (updateDto.getSofProxyXss() != null && updateDto.getSofProxyXss().isPresent()) {
            proxy.setSofProxyXss(getStatusOrDefault(updateDto.getSofProxyXss().get(),
                    DgrSmartOnFhirProxy.Status.DISABLED));
        }

        if (updateDto.getSofProxyXxe() != null && updateDto.getSofProxyXxe().isPresent()) {
            proxy.setSofProxyXxe(getStatusOrDefault(updateDto.getSofProxyXxe().get(),
                    DgrSmartOnFhirProxy.Status.DISABLED));
        }
        
        if (updateDto.getSofProxyShowLog() != null && updateDto.getSofProxyShowLog().isPresent()) {
            proxy.setSofProxyShowLog(getStatusOrDefault(updateDto.getSofProxyShowLog().get(),
                    DgrSmartOnFhirProxy.Status.DISABLED));
        }

        if (updateDto.getSofProxySticky() != null && updateDto.getSofProxySticky().isPresent()) {
            proxy.setSofProxySticky(getStatusOrDefault(updateDto.getSofProxySticky().get(),
                    DgrSmartOnFhirProxy.Status.ENABLED));
        }

        if (updateDto.getSofProxyUrlRewrite() != null && updateDto.getSofProxyUrlRewrite().isPresent()) {
            proxy.setSofProxyUrlRewrite(getStatusOrDefault(updateDto.getSofProxyUrlRewrite().get(),
                    DgrSmartOnFhirProxy.Status.DISABLED));
        }

        // sofProxyTps
        if (updateDto.getSofProxyTps() != null && updateDto.getSofProxyTps().isPresent()) {
            proxy.setSofProxyTps(updateDto.getSofProxyTps().get() != null ? updateDto.getSofProxyTps().get() : 0);
        }

        // sofProxyIgnoreApi
        if (updateDto.getSofProxyIgnoreApi() != null && updateDto.getSofProxyIgnoreApi().isPresent()) {
            proxy.setSofProxyIgnoreApi(
                    updateDto.getSofProxyIgnoreApi().get() != null ? updateDto.getSofProxyIgnoreApi().get() : "");
        }

        // sofProxyClientId
        if (updateDto.getSofProxyClientId() != null && updateDto.getSofProxyClientId().isPresent()) {
            proxy.setSofProxyClientId(
                    updateDto.getSofProxyClientId().get() != null ? updateDto.getSofProxyClientId().get()
                            : new ArrayList<>());
        }
    }

    /**
     * 驗證更新後的 Proxy（使用通用驗證方法）
     */
    private void validateUpdatedProxy(DgrSmartOnFhirProxy proxy,
            SmartOnFhirProxyUpdateDto updateDto, int index) {

        // 使用通用驗證方法
        validateProxyFieldFormats(
                proxy.getSofProxyName(),
                proxy.getSofProxyRemark(),
                proxy.getSofProxyIgnoreApi(),
                proxy.getSofProxyTps(),
                proxy.getSofProxyStatus() != null ? proxy.getSofProxyStatus().getCode() : null,
                index);
    }

    /**
     * 增量更新 Diversion 列表
     */
    private List<DgrSmartOnFhirProxyDiversion> updateDiversions(Long sofProxyId,
            SmartOnFhirProxyUpdateDto updateDto, int proxyIndex ,TsmpAuthorization  auth) {

        // 如果 diversionList 是 undefined，不處理
        if (updateDto.getDiversionList() == null || !updateDto.getDiversionList().isPresent()) {
            // 返回現有的 Diversions
            return getDgrSmartOnFhirProxyDiversionDao().findBySofProxyId(sofProxyId);
        }

        List<SmartOnFhirProxyDiversionUpdateDto> newDiversionList = updateDto.getDiversionList().get();

        // 如果傳入 null，表示要刪除所有 Diversion（但至少需要保留一個）
        if (newDiversionList == null || newDiversionList.isEmpty()) {
            this.logger.error("Proxy[" + proxyIndex + "] must have at least one diversion");
            throw TsmpDpAaRtnCode._1559.throwing(
                    "Proxy[" + proxyIndex + "] must have at least one diversion");
        }

        // 查詢現有的 Diversions
        List<DgrSmartOnFhirProxyDiversion> existingDiversions = getDgrSmartOnFhirProxyDiversionDao()
                .findBySofProxyId(sofProxyId);

        Map<Long, DgrSmartOnFhirProxyDiversion> existingMap = existingDiversions.stream()
                .collect(Collectors.toMap(DgrSmartOnFhirProxyDiversion::getSofProxyDiversionId, d -> d));

        Set<Long> processedIds = new HashSet<>();
        List<DgrSmartOnFhirProxyDiversion> resultDiversions = new ArrayList<>();

        // 處理新傳入的 Diversion（新增或更新）
        for (int i = 0; i < newDiversionList.size(); i++) {
            SmartOnFhirProxyDiversionUpdateDto divDto = newDiversionList.get(i);

            // 驗證 Diversion 基本欄位
            validateDiversionUpdateDto(divDto, proxyIndex, i);

            if (divDto.getSofProxyDiversionId() != null) {
                // 更新現有 Diversion
                DgrSmartOnFhirProxyDiversion existing = existingMap.get(divDto.getSofProxyDiversionId());

                if (existing == null) {
                    this.logger.error("Proxy[" + proxyIndex + "].Diversion[" + i + "] not found");
                    throw TsmpDpAaRtnCode._1559.throwing(
                            "Proxy[" + proxyIndex + "].Diversion[" + i + "] not found: diversionId=" +
                                    divDto.getSofProxyDiversionId());
                }

                // 樂觀鎖檢查
                if (divDto.getVersion() == null) {
                    this.logger.error("Proxy[" + proxyIndex + "].Diversion[" + i + "] version is required");
                    throw TsmpDpAaRtnCode._1559.throwing(
                            "Proxy[" + proxyIndex + "].Diversion[" + i + "].version is required for update");
                }

                if (!existing.getVersion().equals(divDto.getVersion())) {
                    this.logger.error("Proxy[" + proxyIndex + "].Diversion[" + i + "] version mismatch");
                    throw TsmpDpAaRtnCode._1559.throwing(
                            "Version mismatch for Proxy[" + proxyIndex + "].Diversion[" + i + "]: expected " +
                                    existing.getVersion() + ", found " + divDto.getVersion());
                }

                // 更新欄位
                SmartOnFhirProxyRouteContext.ParsedUrl parsedUpdate =
                        SmartOnFhirProxyRouteContext.parseDiversionUrl(divDto.getSofProxyDiversionUrl());
                existing.setSofProxyDiversionUrl(parsedUpdate.baseUrl());
                existing.setSofProxyDiversionProbability(divDto.getSofProxyDiversionProbability());
                existing.setSofProxyDiversionFhirBasePath(parsedUpdate.fhirBasePath());
                existing.setUpdateDateTime(DateTimeUtil.now());
                existing.setUpdateUser(auth.getUserName());

                resultDiversions.add(getDgrSmartOnFhirProxyDiversionDao().save(existing));
                processedIds.add(divDto.getSofProxyDiversionId());

            } else {
                // 新增 Diversion
                SmartOnFhirProxyRouteContext.ParsedUrl parsedNew =
                        SmartOnFhirProxyRouteContext.parseDiversionUrl(divDto.getSofProxyDiversionUrl());
                DgrSmartOnFhirProxyDiversion newDiversion = new DgrSmartOnFhirProxyDiversion();
                newDiversion.setSofProxyId(sofProxyId);
                newDiversion.setSofProxyDiversionUrl(parsedNew.baseUrl());
                newDiversion.setSofProxyDiversionProbability(divDto.getSofProxyDiversionProbability());
                newDiversion.setSofProxyDiversionFhirBasePath(parsedNew.fhirBasePath());
                newDiversion.setCreateDateTime(DateTimeUtil.now());
                newDiversion.setCreateUser(auth.getUserName());

                resultDiversions.add(getDgrSmartOnFhirProxyDiversionDao().save(newDiversion));
            }
        }

        // 刪除未處理的 Diversion（現有 ID 不在新列表中）
        for (DgrSmartOnFhirProxyDiversion existing : existingDiversions) {
            if (!processedIds.contains(existing.getSofProxyDiversionId())) {
                // 清理被移除 Diversion 的 Sticky 紀錄和快取
                stickyService.cleanupDiversion(existing.getSofProxyDiversionId());
                getDgrSmartOnFhirProxyDiversionDao().delete(existing);
            }
        }

        // 驗證權重總和 = 100
        int totalProbability = resultDiversions.stream()
                .mapToInt(DgrSmartOnFhirProxyDiversion::getSofProxyDiversionProbability)
                .sum();

        if (totalProbability != 100) {
            this.logger.error("Proxy[" + proxyIndex + "] diversion probability sum is not 100");
            throw TsmpDpAaRtnCode._1559.throwing(
                    "Proxy[" + proxyIndex + "] diversion probability sum must be 100, got " + totalProbability);
        }

        return resultDiversions;
    }

    /**
     * 驗證單一 Diversion 更新 DTO（使用通用驗證邏輯的輔助方法）
     */
    private void validateDiversionUpdateDto(SmartOnFhirProxyDiversionUpdateDto divDto,
            int proxyIndex, int divIndex) {

        String proxyPrefix = "Proxy[" + proxyIndex + "].";

        // 驗證 URL 必填
        if (!StringUtils.hasText(divDto.getSofProxyDiversionUrl())) {
            String msg = proxyPrefix + "Diversion[" + divIndex
                    + "].sofProxyDiversionUrl is required. Please provide a URL.";
            this.logger.error(msg);
            throw TsmpDpAaRtnCode._1559.throwing(msg);
        }

        // 驗證 URL 格式
        if (!isValidUrl(divDto.getSofProxyDiversionUrl())) {
            String msg = proxyPrefix + "Diversion[" + divIndex + "] has invalid URL format: '"
                    + divDto.getSofProxyDiversionUrl() + "'. Please provide a valid HTTP/HTTPS URL.";
            this.logger.error(msg);
            throw TsmpDpAaRtnCode._1559.throwing(msg);
        }

        // 驗證 probability 必填
        if (divDto.getSofProxyDiversionProbability() == null) {
            String msg = proxyPrefix + "Diversion[" + divIndex
                    + "].sofProxyDiversionProbability is required. Please provide a probability value.";
            this.logger.error(msg);
            throw TsmpDpAaRtnCode._1559.throwing(msg);
        }

        // 驗證 probability 範圍 1-100
        Integer prob = divDto.getSofProxyDiversionProbability();
        if (prob < 1 || prob > 100) {
            String msg = proxyPrefix + "Diversion[" + divIndex + "].sofProxyDiversionProbability out of range: " + prob
                    + ". Must be between 1-100.";
            this.logger.error(msg);
            throw TsmpDpAaRtnCode._1559.throwing(msg);
        }
    }

    /**
     * DPB0313: 刪除 Smart on FHIR Proxy（支援單一和批次刪除）
     * 
     * @param auth 使用者授權資訊
     * @param req  刪除請求（包含要刪除的 Proxy 列表）
     * @return 刪除結果（成功數量及已刪除的 ID 列表）
     */
    @Transactional
    public tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.DPB0313Resp deleteSmartOnFhirProxy(TsmpAuthorization auth,
            tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.DPB0313Req req) {
        try {
            // 1. 輸入驗證
            validateDeleteRequest(req);

            // 2. 批次處理刪除
            List<String> deletedProxyIds = new ArrayList<>();
            int index = 0;

            for (SmartOnFhirProxyUpdateDto updateDto : req.getProxyList()) {
                // 驗證必填欄位
                validateDeleteItem(updateDto, index);

                // 轉換 String ID 為 Long
                Long sofProxyIdLong = IdCodec.parseLong(updateDto.getSofProxyId(),
                        "Proxy[" + index + "].sofProxyId",
                        logger).getOrThrow();

                Long versionLong = IdCodec.parseLong(updateDto.getVersion(),
                        "Proxy[" + index + "].version",
                        logger).getOrThrow();

                // 查詢 Proxy
                Optional<DgrSmartOnFhirProxy> proxyOpt = getDgrSmartOnFhirProxyDao()
                        .findById(sofProxyIdLong);

                if (!proxyOpt.isPresent()) {
                    this.logger.error("Proxy[" + index + "] not found: " + updateDto.getSofProxyId());
                    throw TsmpDpAaRtnCode._1559.throwing(
                            "Proxy with ID '" + updateDto.getSofProxyId()
                                    + "' not found. Please verify the ID or use a valid proxy ID.");
                }

                DgrSmartOnFhirProxy proxy = proxyOpt.get();

                // 驗證 version（樂觀鎖定）
                if (!proxy.getVersion().equals(versionLong)) {
                    this.logger.error("Proxy[" + index + "] version mismatch. Expected: " +
                            proxy.getVersion() + ", Actual: " + updateDto.getVersion());
                    throw TsmpDpAaRtnCode._1559.throwing(
                            "Version mismatch for Proxy[" + index + "]: expected " +
                                    proxy.getVersion() + ", found " + updateDto.getVersion() +
                                    ". Please refresh the data and try again.");
                }

                // 先刪除關聯的 Diversion（級聯刪除）
                List<DgrSmartOnFhirProxyDiversion> diversions = getDgrSmartOnFhirProxyDiversionDao()
                        .findBySofProxyId(proxy.getSofProxyId());

                if (!diversions.isEmpty()) {
                    // 清理每個 Diversion 的 Sticky 紀錄和快取
                    for (DgrSmartOnFhirProxyDiversion diversion : diversions) {
                        stickyService.cleanupDiversion(diversion.getSofProxyDiversionId());
                    }
                    getDgrSmartOnFhirProxyDiversionDao().deleteAll(diversions);
                    this.logger.debug("Deleted " + diversions.size() + " diversion(s) for proxy: " +
                            proxy.getSofProxyId());
                }

                // 再刪除 Proxy
                getDgrSmartOnFhirProxyDao().delete(proxy);
                deletedProxyIds.add(IdCodec.toString(proxy.getSofProxyId()));

                this.logger.debug("Successfully deleted proxy: " + proxy.getSofProxyId());
                index++;
            }

            // 3. 組裝回應
            tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.DPB0313Resp resp = new tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.DPB0313Resp();
            resp.setSuccessCount(deletedProxyIds.size());
            resp.setDeletedProxyIds(deletedProxyIds);

            return resp;

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            this.logger.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
    }

    /**
     * 驗證刪除請求
     */
    private void validateDeleteRequest(tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.DPB0313Req req) {
        if (req.getProxyList() == null || req.getProxyList().isEmpty()) {
            this.logger.error("Delete proxy list is empty");
            throw TsmpDpAaRtnCode._1559.throwing("Delete proxy list cannot be empty");
        }
    }

    /**
     * 驗證刪除項目
     */
    private void validateDeleteItem(SmartOnFhirProxyUpdateDto updateDto, int index) {
        if (updateDto.getSofProxyId() == null) {
            this.logger.error("Proxy[" + index + "] sofProxyId is required for delete");
            throw TsmpDpAaRtnCode._1559.throwing("Proxy[" + index + "].sofProxyId is required for delete");
        }

        if (updateDto.getVersion() == null) {
            this.logger.error("Proxy[" + index + "] version is required for delete");
            throw TsmpDpAaRtnCode._1559.throwing("Proxy[" + index + "].version is required for delete");
        }
    }

    /**
     * 驗證 Client ID 列表（當提供時）
     */
    private void validateClientIds(List<String> clientIds, String fieldContext) {
        if (clientIds == null || clientIds.isEmpty()) {
            return; // 空列表或 null 跳過驗證
        }

        for (String clientId : clientIds) {
            if (StringUtils.hasText(clientId) && !getTsmpClientDao().existsById(clientId)) {
                String msg = fieldContext + ": Client ID '" + clientId
                        + "' does not exist. Please verify the client ID or create the client first.";
                this.logger.error(msg);
                throw TsmpDpAaRtnCode._1559.throwing(msg);
            }
        }
    }

}
