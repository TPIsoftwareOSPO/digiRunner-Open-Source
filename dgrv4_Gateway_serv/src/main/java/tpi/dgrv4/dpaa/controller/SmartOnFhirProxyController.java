package tpi.dgrv4.dpaa.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.SmartOnFhirProxyCreateUpdateDeleteService;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.SmartOnFhirProxyExportService;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.SmartOnFhirProxyImportService;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.SmartOnFhirProxySearchService;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.DPB0310Resp;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.DPB0312Req;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.DPB0312Resp;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.DPB0313Req;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.DPB0313Resp;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.DPB0315Req;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.DPB0316Req;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.SmartOnFhirProxyDto;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.SmartOnFhirProxyImportItemDto;
import tpi.dgrv4.dpaa.service.smartOnFhirProxyService.vo.SmartOnFhirProxySearchReq;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * Smart on FHIR Proxy Controller
 * 提供 Smart on FHIR Proxy 相關的 API
 */
@RestController
public class SmartOnFhirProxyController {

    private SmartOnFhirProxySearchService smartOnFhirProxySearchService;
    private SmartOnFhirProxyCreateUpdateDeleteService smartOnFhirProxyCreateUpdateDeleteService;
    private SmartOnFhirProxyExportService smartOnFhirProxyExportService;
    private SmartOnFhirProxyImportService smartOnFhirProxyImportService;
    private ObjectMapper objectMapper;

    @Autowired
    public SmartOnFhirProxyController(
            SmartOnFhirProxySearchService smartOnFhirProxySearchService,
            SmartOnFhirProxyCreateUpdateDeleteService smartOnFhirProxyCreateUpdateDeleteService,
            SmartOnFhirProxyExportService smartOnFhirProxyExportService,
            SmartOnFhirProxyImportService smartOnFhirProxyImportService,
            ObjectMapper objectMapper) {
        this.smartOnFhirProxySearchService = smartOnFhirProxySearchService;
        this.smartOnFhirProxyCreateUpdateDeleteService = smartOnFhirProxyCreateUpdateDeleteService;
        this.smartOnFhirProxyExportService = smartOnFhirProxyExportService;
        this.smartOnFhirProxyImportService = smartOnFhirProxyImportService;
        this.objectMapper = objectMapper;
    }

    /**
     * DPB0310: 查詢 Smart on FHIR Proxy 列表（分頁）
     * 
     * @param headers HTTP Headers
     * @param req     請求參數
     * @return 分頁查詢結果
     */
    @PostMapping(value = "/dgrv4/11/DPB0310", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<DPB0310Resp> querySmartOnFhirProxyList(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<SmartOnFhirProxySearchReq> req) {

        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        DPB0310Resp resp = null;

        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
            resp = smartOnFhirProxySearchService.querySmartOnFhirProxyList(tsmpHttpHeader.getAuthorization(),
                    req.getBody());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }

    /**
     * DPB0311: 新增 Smart on FHIR Proxy
     * 
     * @param headers HTTP Headers
     * @param req     請求參數（包含 Proxy 基本資料及 Diversion 列表）
     * @return 新建的 Proxy 完整資料
     */
    @PostMapping(value = "/dgrv4/11/DPB0311", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<SmartOnFhirProxyDto> addSmartOnFhirProxy(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<SmartOnFhirProxyDto> req) {

        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        SmartOnFhirProxyDto resp = null;

        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
            resp = smartOnFhirProxyCreateUpdateDeleteService.addSmartOnFhirProxy(tsmpHttpHeader.getAuthorization(),
                    req.getBody());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }

    /**
     * DPB0312: 批次更新 Smart on FHIR Proxy
     * 
     * @param headers HTTP Headers
     * @param req     請求參數（包含多個 Proxy 的更新資料）
     * @return 批次更新結果（成功數量及更新後的 Proxy 列表）
     */
    @PostMapping(value = "/dgrv4/11/DPB0312", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<DPB0312Resp> batchUpdateSmartOnFhirProxy(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<DPB0312Req> req) {

        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        DPB0312Resp resp = null;

        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
            resp = smartOnFhirProxyCreateUpdateDeleteService
                    .batchUpdateSmartOnFhirProxy(tsmpHttpHeader.getAuthorization(), req.getBody());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }

    /**
     * DPB0313: 刪除 Smart on FHIR Proxy（支援單一和批次刪除）
     * 
     * @param headers HTTP Headers
     * @param req     請求參數（包含要刪除的 Proxy 列表）
     * @return 刪除結果（成功數量及已刪除的 Proxy ID 列表）
     */
    @PostMapping(value = "/dgrv4/11/DPB0313", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<DPB0313Resp> deleteSmartOnFhirProxy(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<DPB0313Req> req) {

        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        DPB0313Resp resp = null;

        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
            resp = smartOnFhirProxyCreateUpdateDeleteService.deleteSmartOnFhirProxy(tsmpHttpHeader.getAuthorization(),
                    req.getBody());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }

    /**
     * DPB0314: 匯出 Smart on FHIR Proxy（使用 SSE 推送進度）
     * 
     * @param headers HTTP Headers
     * @param req     匯出請求（篩選條件）
     * @return SseEmitter（推送進度與匯出資料）
     */
    @PostMapping(value = "/dgrv4/11/DPB0314", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter exportSmartOnFhirProxy(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<SmartOnFhirProxySearchReq> req) {

        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
            return smartOnFhirProxyExportService.exportSmartOnFhirProxyData(tsmpHttpHeader.getAuthorization(),
                    req.getBody());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }
    }

    /**
     * DPB0315: 預前匯入驗證（使用 SSE 推送進度）
     * 支援 JSON 字串或檔案上傳
     * 每筆資料可選擇性地包含 uuid 欄位，用於追蹤處理結果
     * 
     * @param headers  HTTP Headers
     * @param file     上傳的 JSON 檔案（可選）
     * @param jsonData JSON 字串（可選）
     * @return SseEmitter（推送驗證進度與結果）
     */
    @PostMapping(value = "/dgrv4/11/DPB0315", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter preValidateImport(
            @RequestHeader HttpHeaders headers,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "jsonData", required = false) String jsonData) {

        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

        try {
            // 解析匯入資料（從檔案或 JSON 字串）
            List<SmartOnFhirProxyImportItemDto> importItems = parseImportData(file, jsonData);

            // 建立請求物件
            DPB0315Req req = new DPB0315Req();
            req.setImportItems(importItems);

            // 執行驗證
            return smartOnFhirProxyImportService.validateImportData(tsmpHttpHeader.getAuthorization(), req);
        } catch (Exception e) {
            throw new TsmpDpAaException(e, null);
        }
    }

    /**
     * DPB0316: 匯入 Smart on FHIR Proxy（使用 SSE 推送進度）
     * 支援 JSON 字串或檔案上傳
     * 每筆資料可選擇性地包含 uuid 欄位，用於追蹤處理結果
     * 
     * @param headers     HTTP Headers
     * @param file        上傳的 JSON 檔案（可選）
     * @param jsonData    JSON 字串（可選）
     * @param forceUpdate 是否強制更新（忽略 version 衝突），預設 true
     * @return SseEmitter（推送匯入進度與結果）
     */
    @PostMapping(value = "/dgrv4/11/DPB0316", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter importSmartOnFhirProxy(
            @RequestHeader HttpHeaders headers,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "jsonData", required = false) String jsonData,
            @RequestParam(name = "forceUpdate",defaultValue = "true") boolean forceUpdate) {

        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);

        try {
            // 解析匯入資料（從檔案或 JSON 字串）
            List<SmartOnFhirProxyImportItemDto> importItems = parseImportData(file, jsonData);

            // 建立請求物件
            DPB0316Req req = new DPB0316Req();
            req.setImportItems(importItems);
            req.setForceUpdate(forceUpdate);

            // 執行匯入
            return smartOnFhirProxyImportService.executeImport(tsmpHttpHeader.getAuthorization(), req);
        } catch (Exception e) {
            throw new TsmpDpAaException(e, null);
        }
    }

    /**
     * 解析匯入資料（從檔案或 JSON 字串）
     * 優先使用檔案，如果沒有檔案則使用 JSON 字串
     */
    private List<SmartOnFhirProxyImportItemDto> parseImportData(MultipartFile file, String jsonData) {
        try {
            String jsonContent;

            if (file != null && !file.isEmpty()) {
                // 從檔案讀取
                jsonContent = new String(file.getBytes(), "UTF-8");
            } else if (jsonData != null && !jsonData.isEmpty()) {
                // 使用 JSON 字串
                jsonContent = jsonData;
            } else {
                throw TsmpDpAaRtnCode._1296.throwing("{{field}}", "file or jsonData");
            }

            // 解析 JSON
            return objectMapper.readValue(jsonContent,
                    objectMapper.getTypeFactory().constructCollectionType(List.class,
                            SmartOnFhirProxyImportItemDto.class));

        } catch (Exception e) {
            throw TsmpDpAaRtnCode._1297.throwing("{{message}}", "Failed to parse import data: " + e.getMessage());
        }
    }
}
