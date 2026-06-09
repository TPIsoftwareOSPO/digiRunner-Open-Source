package tpi.dgrv4.dpaa.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.record.PageResp;
import tpi.dgrv4.dpaa.service.smartClientService.SmartClientCreateService;
import tpi.dgrv4.dpaa.service.smartClientService.SmartClientDeleteService;
import tpi.dgrv4.dpaa.service.smartClientService.SmartClientSearchService;
import tpi.dgrv4.dpaa.service.smartClientService.SmartClientUpdateService;
import tpi.dgrv4.dpaa.service.smartClientService.vo.DPB0330Req;
import tpi.dgrv4.dpaa.service.smartClientService.vo.DPB0331Req;
import tpi.dgrv4.dpaa.service.smartClientService.vo.DPB0332Req;
import tpi.dgrv4.dpaa.service.smartClientService.vo.DPB0333Req;
import tpi.dgrv4.dpaa.service.smartClientService.vo.DPB0333Resp;
import tpi.dgrv4.dpaa.service.smartClientService.vo.SmartClientDto;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

/**
 * SMART Client 管理 API。
 */
@RestController
@RequiredArgsConstructor
public class SmartClientController {

    private final SmartClientSearchService smartClientSearchService;
    private final SmartClientCreateService smartClientCreateService;
    private final SmartClientUpdateService smartClientUpdateService;
    private final SmartClientDeleteService smartClientDeleteService;

    /**
     * DPB0330：查詢 SMART Client 列表（分頁）。
     */
    @PostMapping(value = "/dgrv4/11/DPB0330",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<PageResp<SmartClientDto>> querySmartClientList(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<DPB0330Req> req) {

        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        PageResp<SmartClientDto> resp = null;

        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
            resp = smartClientSearchService.querySmartClientList(
                tsmpHttpHeader.getAuthorization(), req.getBody());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }

    /**
     * DPB0331：新增 SMART Client 設定。
     */
    @PostMapping(value = "/dgrv4/11/DPB0331",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<SmartClientDto> createSmartClient(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<DPB0331Req> req) {

        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        SmartClientDto resp = null;

        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
            resp = smartClientCreateService.createSmartClient(
                tsmpHttpHeader.getAuthorization(), req.getBody());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }

    /**
     * DPB0332：批次更新 SMART Client 設定。
     */
    @PostMapping(value = "/dgrv4/11/DPB0332",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<List<SmartClientDto>> batchUpdateSmartClient(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<DPB0332Req> req) {

        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        List<SmartClientDto> resp = null;

        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
            resp = smartClientUpdateService.batchUpdateSmartClient(
                tsmpHttpHeader.getAuthorization(), req.getBody());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }

    /**
     * DPB0333：批次刪除 SMART Client 設定。
     */
    @PostMapping(value = "/dgrv4/11/DPB0333",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<DPB0333Resp> batchDeleteSmartClient(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<DPB0333Req> req) {

        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        DPB0333Resp resp = null;

        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
            resp = smartClientDeleteService.batchDeleteSmartClient(
                tsmpHttpHeader.getAuthorization(), req.getBody());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }
}
