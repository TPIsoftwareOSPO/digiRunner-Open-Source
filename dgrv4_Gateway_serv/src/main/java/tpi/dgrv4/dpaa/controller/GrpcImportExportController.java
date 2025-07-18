package tpi.dgrv4.dpaa.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.service.GrpcImportExportService;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DgrGrpcProxyMapDto;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class GrpcImportExportController {

    private final GrpcImportExportService grpcImportExportService;

    @Autowired
    public GrpcImportExportController(GrpcImportExportService grpcImportExportService) {
        this.grpcImportExportService = grpcImportExportService;
    }

    @PostMapping(value = "/dgrv4/11/DPB0295", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<Map<String, Object>> exportDgrApiProxy(@RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<List<String>> req) {

        Map<String, Object> resp;

        try {
            // Convert HTTP headers to TsmpHttpHeader and validate request.
            // 將 HTTP 標頭轉換為 TsmpHttpHeader 並驗證請求。
            TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);

            resp = grpcImportExportService.exportDgrGrpcProxyMap(tsmpHttpHeader, req);

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw new TsmpDpAaException(e, req.getReqHeader());
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }

    @PostMapping(value = "/dgrv4/11/DPB0296", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<List<DgrGrpcProxyMapDto>> parseDgrApiProxy(@RequestHeader HttpHeaders headers,
            @RequestParam("file") MultipartFile file) {

        List<DgrGrpcProxyMapDto> resp;
        ReqHeader reqHeader = new ReqHeader();

        try {

            // Convert HTTP headers to TsmpHttpHeader and validate request.
            // 將 HTTP 標頭轉換為 TsmpHttpHeader 並驗證請求。
            ControllerUtil.toTsmpHttpHeader(headers);
            // ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), null);

            resp = grpcImportExportService.parseDgrGrpcProxyMap(file);

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw new TsmpDpAaException(e, reqHeader);
        }

        return ControllerUtil.tsmpResponseBaseObj(reqHeader, resp);
    }

    @PostMapping(value = "/dgrv4/11/DPB0297", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<List<DgrGrpcProxyMapDto>> importDgrApiProxy(@RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<List<DgrGrpcProxyMapDto>> req) {

        List<DgrGrpcProxyMapDto> resp;
        ReqHeader reqHeader = new ReqHeader();

        try {
            // Convert HTTP headers to TsmpHttpHeader and validate request.
            // 將 HTTP 標頭轉換為 TsmpHttpHeader 並驗證請求。
            TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);

            resp = grpcImportExportService.importDgrGrpcProxyMap(tsmpHttpHeader, req);

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw new TsmpDpAaException(e, req.getReqHeader());
        }

        return ControllerUtil.tsmpResponseBaseObj(reqHeader, resp);
    }

}
