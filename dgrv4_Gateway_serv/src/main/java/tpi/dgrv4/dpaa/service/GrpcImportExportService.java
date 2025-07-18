package tpi.dgrv4.dpaa.service;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DgrGrpcProxyMapDto;
import tpi.dgrv4.entity.entity.DgrGrpcProxyMap;
import tpi.dgrv4.entity.repository.DgrGrpcProxyMapDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * Service for handling the import and export of DgrGrpcProxyMap entities.
 * <p>
 * 處理 DgrGrpcProxyMap 實體的匯入和匯出服務。
 */
@Service
public class GrpcImportExportService {

    private final DgrGrpcProxyMapDao dgrGrpcProxyMapDao;

    private final ObjectMapper objectMapper;

    private final DPB0290Service dpb0290Service;

    private final DPB0292Service dpb0292Service;

    /**
     * Constructs a new GrpcImportExportService with the specified dependencies.
     * <p>
     * 使用指定的依賴項建構一個新的 GrpcImportExportService。
     *
     * @param dgrGrpcProxyMapDao The data access object for DgrGrpcProxyMap.
     *                           / DgrGrpcProxyMap 的資料存取物件。
     * @param objectMapper       The Jackson object mapper for JSON serialization and
     *                           deserialization.
     *                           / 用於 JSON 序列化和反序列化的 Jackson 物件映射器。
     * @param dpb0290Service     The service for creating gRPC proxy entries.
     *                           / 用於建立 gRPC 代理項目的服務。
     * @param dpb0292Service     The service for updating gRPC proxy entries.
     *                           / 用於更新 gRPC 代理項目的服務。
     */
    @Autowired
    public GrpcImportExportService(DgrGrpcProxyMapDao dgrGrpcProxyMapDao, ObjectMapper objectMapper,
            DPB0290Service dpb0290Service, DPB0292Service dpb0292Service) {
        this.dgrGrpcProxyMapDao = dgrGrpcProxyMapDao;
        this.objectMapper = objectMapper;
        this.dpb0290Service = dpb0290Service;
        this.dpb0292Service = dpb0292Service;
    }

    /**
     * Exports DgrGrpcProxyMap records based on the provided IDs. If no IDs are
     * given, all records are exported.
     * <p>
     * 根據提供的 ID 匯出 DgrGrpcProxyMap 記錄。如果未提供 ID，則匯出所有記錄。
     *
     * @param tsmpHttpHeader The HTTP header containing authorization information.
     *                       / 包含授權資訊的 HTTP 標頭。
     * @param req            The request object containing a list of
     *                       DgrGrpcProxyMap IDs to export.
     *                       / 包含要匯出的 DgrGrpcProxyMap ID 列表的請求物件。
     * @return A map containing the file name and the exported data.
     *         / 包含檔案名稱和匯出資料的 Map。
     */
    public Map<String, Object> exportDgrGrpcProxyMap(TsmpHttpHeader tsmpHttpHeader, TsmpBaseReq<List<String>> req) {

        checkInput(tsmpHttpHeader, req);
        List<DgrGrpcProxyMap> dgrGrpcProxyMaps = getExportData(req);
        return getResp(dgrGrpcProxyMaps);

    }

    /**
     * Parses an uploaded file into a list of DgrGrpcProxyMapDto objects.
     * <p>
     * 將上傳的檔案解析為 DgrGrpcProxyMapDto 物件列表。
     *
     * @param file The multipart file to parse.
     *             / 要解析的 multipart 檔案。
     * @return A list of DgrGrpcProxyMapDto parsed from the file.
     *         / 從檔案中解析出的 DgrGrpcProxyMapDto 列表。
     */
    public List<DgrGrpcProxyMapDto> parseDgrGrpcProxyMap(MultipartFile file) {

        checkInput(file);
        List<DgrGrpcProxyMapDto> dgrGrpcProxyMapDtos = parseFile(file);
        return dgrGrpcProxyMapDtos;
    }

    /**
     * Imports a list of DgrGrpcProxyMapDto objects, creating new records or
     * updating existing ones.
     * Handles each item individually and returns a list of results, including any
     * errors.
     * <p>
     * 匯入 DgrGrpcProxyMapDto 物件列表，建立新記錄或更新現有記錄。
     * 單獨處理每個項目並返回結果列表，包括任何錯誤。
     *
     * @param tsmpHttpHeader The HTTP header containing authorization information.
     *                       / 包含授權資訊的 HTTP 標頭。
     * @param req            The request object containing the list of
     *                       DgrGrpcProxyMapDto to import.
     *                       / 包含要匯入的 DgrGrpcProxyMapDto 列表的請求物件。
     * @return A list of DgrGrpcProxyMapDto with import status (success, error
     *         message) for each item.
     *         / 包含每個項目匯入狀態（成功、錯誤訊息）的 DgrGrpcProxyMapDto 列表。
     */
    public List<DgrGrpcProxyMapDto> importDgrGrpcProxyMap(TsmpHttpHeader tsmpHttpHeader,
            TsmpBaseReq<List<DgrGrpcProxyMapDto>> req) {

        checkInput(tsmpHttpHeader, req);
        List<DgrGrpcProxyMapDto> dgrGrpcProxyMapDtos = req.getBody();
        
        // Batch validation
        validateBatchInput(dgrGrpcProxyMapDtos);
        
        // Batch query existing records to avoid N+1 query problem
        Map<Long, DgrGrpcProxyMap> existingRecords = getExistingRecords(dgrGrpcProxyMapDtos);

        List<DgrGrpcProxyMapDto> resp = new ArrayList<>();

        for (DgrGrpcProxyMapDto dgrGrpcProxyMapDto : dgrGrpcProxyMapDtos) {
            try {
                DgrGrpcProxyMapDto result = processIndividualItem(tsmpHttpHeader, dgrGrpcProxyMapDto, existingRecords);
                resp.add(result);
            } catch (TsmpDpAaException e) {
                // Business logic exception
                String errorMessage = ServiceUtil.buildContent(e.getMessage(), e.getParams());
                resp.add(createErrorResponse(dgrGrpcProxyMapDto, errorMessage));

                TPILogger.tl.error("Business logic exception: " + errorMessage);
                TPILogger.tl.error(StackTraceUtil.logStackTrace(e));

            } catch (Exception e) {
                // System exception
                TPILogger.tl.error("Unexpected error processing item: " + dgrGrpcProxyMapDto.toString());
                TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
                resp.add(createErrorResponse(dgrGrpcProxyMapDto, "System Error: " + e.getMessage()));
            }
        }

        return resp;
    }

    /**
     * Parses the content of a MultipartFile into a list of DgrGrpcProxyMapDto
     * objects.
     * <p>
     * 將 MultipartFile 的內容解析為 DgrGrpcProxyMapDto 物件列表。
     *
     * @param file The multipart file to be parsed.
     *             / 要解析的 multipart 檔案。
     * @return A list of DgrGrpcProxyMapDto.
     *         / DgrGrpcProxyMapDto 列表。
     * @throws TsmpDpAaException if parsing fails. / 如果解析失敗。
     */
    private List<DgrGrpcProxyMapDto> parseFile(MultipartFile file) {

        if (file == null) {
            return Collections.emptyList();
        }

        try (InputStream inputStream = file.getInputStream()) {

            JsonNode rootNode = objectMapper.readTree(inputStream);

            List<DgrGrpcProxyMapDto> dgrGrpcProxyMapDtos = objectMapper.readValue(rootNode.traverse(),
                    new TypeReference<List<DgrGrpcProxyMapDto>>() {
                    });

            return checkExist(dgrGrpcProxyMapDtos);

        } catch (IOException e) {
            TPILogger.tl.error("parse file to dgrGrpcProxyMapDto list failed");
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1559.throwing("parse file to dgrGrpcProxyMapDto list failed");
        }
    }

    /**
     * Checks each DgrGrpcProxyMapDto in the list to determine if it should be a new
     * creation or an update based on whether the ID exists in the database.
     * <p>
     * 檢查列表中的每個 DgrGrpcProxyMapDto，根據 ID 是否存在於資料庫中來決定是應該新增還是更新。
     *
     * @param dgrGrpcProxyMapDtos The list of DTOs to check.
     *                            / 要檢查的 DTO 列表。
     * @return The list of DTOs with their operation type (create/update) set.
     *         / 已設定其操作類型（新增/更新）的 DTO 列表。
     */
    private List<DgrGrpcProxyMapDto> checkExist(List<DgrGrpcProxyMapDto> dgrGrpcProxyMapDtos) {
        if (dgrGrpcProxyMapDtos == null) {
            return Collections.emptyList();
        }

        return dgrGrpcProxyMapDtos.stream()
                .map(this::determineOperationType)
                .toList();
    }

    /**
     * Determines the operation type (create or update) for a given DTO.
     * It checks if an ID is provided and if a record with that ID already exists in
     * the database.
     * <p>
     * 判斷給定 DTO 的操作類型（新增或更新）。
     * 它會檢查是否提供了 ID，以及資料庫中是否已存在具有該 ID 的記錄。
     * 
     * @param dto The DTO to check.
     *            / 要檢查的 DTO。
     * @return The DTO with its 'create' and 'update' flags set accordingly.
     *         / 已相應設定 'create' 和 'update' 標記的 DTO。
     */
    private DgrGrpcProxyMapDto determineOperationType(DgrGrpcProxyMapDto dto) {
        // Sets the default state: ready to create.
        // 設定預設狀態：準備新增
        dto.setCreate(true);
        dto.setUpdate(false);

        String idStr = dto.getGrpcproxyMapId();
        if (!StringUtils.hasText(idStr)) {
            return dto;
        }

        Long id = getLong(idStr);
        if (id == null) {
            return dto;
        }

        boolean existsInDatabase = dgrGrpcProxyMapDao.existsById(id);
        if (existsInDatabase) {
            // Data already exists, switch to update mode.
            // 資料已存在，改為更新模式
            dto.setCreate(false);
            dto.setUpdate(true);
        }

        dto.setSuccess(null);
        dto.setErrorMessage(null);

        return dto;
    }

    /**
     * Validates the input for the export/import operation, checking for non-null
     * headers and request body.
     * <p>
     * 驗證匯出/匯入操作的輸入，檢查標頭和請求主體是否為 null。
     *
     * @param tsmpHttpHeader The HTTP header. HTTP 標頭。
     * @param req            The request object. 請求物件。
     * @throws TsmpDpAaException if validation fails. 如果驗證失敗。
     */
    private void checkInput(TsmpHttpHeader tsmpHttpHeader, TsmpBaseReq<?> req) {

        if (tsmpHttpHeader == null || tsmpHttpHeader.getAuthorization() == null) {
            throw TsmpDpAaRtnCode._1559
                    .throwing("tsmphttpheader or authorization cannot be null for dgrapiproxy export");
        }

        if (req == null || req.getBody() == null) {
            throw TsmpDpAaRtnCode._1559.throwing("grpcproxy export req or body cannot be null");
        }
    }

    /**
     * Validates the uploaded file, ensuring it is not null, empty, and within the
     * size limit.
     * <p>
     * 驗證上傳的檔案，確保其不為 null、不為空且在大小限制內。
     *
     * @param file The multipart file to validate.
     *             要驗證的 multipart 檔案。
     * @throws TsmpDpAaException if the file is invalid. 如果檔案無效。
     */
    private void checkInput(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw TsmpDpAaRtnCode._1559.throwing("file cannot be null or empty");
        }

        if (file.getSize() > 1024 * 1024 * 100) {
            throw TsmpDpAaRtnCode._1559.throwing("file size cannot be greater than 100MB");
        }
    }

    /**
     * Retrieves DgrGrpcProxyMap records from the database.
     * If a list of IDs is provided, it fetches only those records; otherwise, it
     * fetches all records.
     * <p>
     * 從資料庫中檢索 DgrGrpcProxyMap 記錄。
     * 如果提供了 ID 列表，則僅獲取這些記錄；否則，獲取所有記錄。
     *
     * @param req The request containing the list of IDs.
     *           包含 ID 列表的請求。
     * @return A list of DgrGrpcProxyMap entities.
     *         DgrGrpcProxyMap 實體列表。
     */
    private List<DgrGrpcProxyMap> getExportData(TsmpBaseReq<List<String>> req) {

        List<String> ids = req.getBody();

        if (ids == null || ids.isEmpty()) {
            return dgrGrpcProxyMapDao.findAll();
        }

        // Batch fetch: first convert all IDs, then query once.
        // 批次撈取：先轉換所有ID，然後一次查詢
        List<Long> longIds = ids.stream()
                .map(this::getLongWithException)
                .toList();

        return dgrGrpcProxyMapDao.findAllById(longIds);
    }

    /**
     * Converts a String ID to a Long, throwing a TsmpDpAaException if the
     * conversion fails.
     * <p>
     * 將字串 ID 轉換為 Long，如果轉換失敗則拋出 TsmpDpAaException。
     *
     * @param id The string ID to convert.
     *           要轉換的字串 ID。
     * @return The converted Long ID. 轉換後的 Long ID。
     * @throws TsmpDpAaException if the ID is not a valid number.
     *                           如果 ID 不是有效的數字。
     */
    private Long getLongWithException(String id) {
        try {
            return Long.parseLong(id.trim());
        } catch (NumberFormatException e) {
            TPILogger.tl.error("GrpcProxy ID must be a valid number. The id is " + id);
            throw TsmpDpAaRtnCode._1400.throwing(id);
        }
    }

    /**
     * Converts a String ID to a Long, returning null if the string is blank or the
     * conversion fails.
     * <p>
     * 將字串 ID 轉換為 Long，如果字串為空或轉換失敗則返回 null。
     *
     * @param id The string ID to convert.
     *           要轉換的字串 ID。
     * @return The converted Long ID, or null if conversion is not possible.
     *         轉換後的 Long ID，如果無法轉換則為 null。
     */
    private Long getLong(String id) {

        if (!StringUtils.hasText(id)) {
            return null;
        }

        try {
            return Long.parseLong(id.trim());
        } catch (NumberFormatException e) {
            TPILogger.tl.error("GrpcProxy ID must be a valid number. The id is " + id);
            return null;
        }
    }

    /**
     * Creates the response map for the export operation, including the file name
     * and the data.
     * <p>
     * 為匯出操作創建回應對應，包括檔案名稱和資料。
     *
     * @param dgrGrpcProxyMaps The list of entities to be included in the response.
     *                         要包含在回應中的實體列表。
     * @return A map containing the file name and a list of DTOs.
     *         包含檔案名稱和 DTO 列表的 Map。
     */
    private Map<String, Object> getResp(List<DgrGrpcProxyMap> dgrGrpcProxyMaps) {

        List<DgrGrpcProxyMapDto> dgrGrpcProxyMapDtos = dgrGrpcProxyMaps.stream()
                .map(DgrGrpcProxyMapDto::new)
                .toList();

        Map<String, Object> resp = new HashMap<>();

        resp.put("fileName", getFileName());
        resp.put("data", dgrGrpcProxyMapDtos);

        return resp;
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
        return String.format("exportGrpcProxy_%s.json", dateTime);
    }

    /**
     * Validates the batch input data. Currently checks for duplicate IDs in the
     * provided list of DTOs.
     * <p>
     * 批次驗證輸入資料。目前檢查所提供的 DTO 列表中是否有重複的 ID。
     *
     * @param dtos The list of DgrGrpcProxyMapDto to validate.
     *             要驗證的 DgrGrpcProxyMapDto 列表。
     * @throws TsmpDpAaException if duplicate IDs are found. 如果發現重複的 ID。
     */
    private void validateBatchInput(List<DgrGrpcProxyMapDto> dtos) {
        
        // Check for duplicate IDs.
        // 檢查是否有重複的 ID
        Set<String> ids = new HashSet<>();
        for (DgrGrpcProxyMapDto dto : dtos) {
            String id = dto.getGrpcproxyMapId();
            if (StringUtils.hasText(id) && !ids.add(id)) {
                throw TsmpDpAaRtnCode._1559.throwing("Duplicate ID found in batch data: " + id);
            }
        }
    }

    /**
     * Fetches existing DgrGrpcProxyMap records from the database in a single batch
     * based on the IDs from the DTO list.
     * This avoids the N+1 query problem.
     * <p>
     * 根據 DTO 列表中的 ID，從資料庫中批次查詢現有的 DgrGrpcProxyMap 記錄。
     * 這樣可以避免 N+1 查詢問題。
     *
     * @param dtos The list of DTOs containing the IDs to look up.
     *             包含要查詢的 ID 的 DTO 列表。
     * @return A map of existing records, with the entity ID as the key and the
     *         entity itself as the value.
     *         現有記錄的 Map，以實體 ID 為鍵，實體本身為值。
     */
    private Map<Long, DgrGrpcProxyMap> getExistingRecords(List<DgrGrpcProxyMapDto> dtos) {
        List<Long> ids = dtos.stream()
            .map(dto -> getLong(dto.getGrpcproxyMapId()))
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        
        return dgrGrpcProxyMapDao.findAllById(ids).stream()
            .collect(Collectors.toMap(DgrGrpcProxyMap::getGrpcproxyMapId, Function.identity()));
    }

    /**
     * Processes a single DgrGrpcProxyMapDto, either updating an existing record or
     * creating a new one based on whether the ID exists in the pre-fetched map of
     * existing records.
     * <p>
     * 處理單個 DgrGrpcProxyMapDto，根據 ID 是否存在於預先擷取的現有記錄 Map 中，來更新現有記錄或創建新記錄。
     *
     * @param tsmpHttpHeader    The HTTP header for authorization.
     *                         用於授權的 HTTP 標頭。
     * @param dto               The DTO to process. 要處理的 DTO。
     * @param existingRecords   A map of existing records to check against.
     *                         用於核對的現有記錄 Map。
     * @return A DgrGrpcProxyMapDto representing the result of the operation.
     *         代表操作結果的 DgrGrpcProxyMapDto。
     */
    private DgrGrpcProxyMapDto processIndividualItem(TsmpHttpHeader tsmpHttpHeader, 
            DgrGrpcProxyMapDto dto, Map<Long, DgrGrpcProxyMap> existingRecords) {
        
        Long id = getLong(dto.getGrpcproxyMapId());
        boolean isUpdate = id != null && existingRecords.containsKey(id);
        
        DgrGrpcProxyMap savedEntity;
        if (isUpdate) {
            savedEntity = dpb0292Service.updateGrpcProxyWithImport(
                tsmpHttpHeader.getAuthorization(), dto.toDPB0292Req());
        } else {
            savedEntity = dpb0290Service.createGrpcProxyWithImport(
                tsmpHttpHeader.getAuthorization(), dto.toDPB0290Req());
        }
        
        return createSuccessResponse(savedEntity, dto, isUpdate);
    }

    /**
     * Creates a success response DTO from a saved entity, preserving note and uuid fields from the original DTO.
     * <p>
     * 從已儲存的實體創建成功的 DTO 回應，保留原始 DTO 中的 note 和 uuid 欄位。
     *
     * @param entity      The saved DgrGrpcProxyMap entity.
     *                    已儲存的 DgrGrpcProxyMap 實體。
     * @param originalDto The original DTO containing note and uuid fields to preserve.
     *                    包含要保留的 note 和 uuid 欄位的原始 DTO。
     * @param isUpdate    A boolean indicating if the operation was an update.
     *                    一個布林值，指示操作是否為更新。
     * @return A DgrGrpcProxyMapDto marked as successful with preserved note and uuid.
     *         標記為成功且保留 note 和 uuid 的 DgrGrpcProxyMapDto。
     */
    private DgrGrpcProxyMapDto createSuccessResponse(DgrGrpcProxyMap entity, DgrGrpcProxyMapDto originalDto, boolean isUpdate) {
        DgrGrpcProxyMapDto result = new DgrGrpcProxyMapDto(entity);
        result.setSuccess(true);
        result.setCreate(!isUpdate);
        result.setUpdate(isUpdate);
        
        // Preserve note and uuid fields from the original DTO
        // 保留原始 DTO 中的 note 和 uuid 欄位
        result.setNote(originalDto.getNote());
        result.setUuid(originalDto.getUuid());
        
        return result;
    }

    /**
     * Creates an error response DTO from the original DTO and an error message.
     * <p>
     * 根據原始 DTO 和錯誤訊息創建錯誤的 DTO 回應。
     *
     * @param originalDto  The original DTO that caused the error.
     *                     導致錯誤的原始 DTO。
     * @param errorMessage The error message to include in the response.
     *                     要包含在回應中的錯誤訊息。
     * @return The original DTO, now marked as failed and with an error message.
     *         原始 DTO，現在標記為失敗並帶有錯誤訊息。
     */
    private DgrGrpcProxyMapDto createErrorResponse(DgrGrpcProxyMapDto originalDto, String errorMessage) {
        originalDto.setSuccess(false);
        originalDto.setErrorMessage(errorMessage);
        return originalDto;
    }
}
