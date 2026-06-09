package tpi.dgrv4.dpaa.controller;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.DPB0304Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0304Req;
import tpi.dgrv4.dpaa.vo.DPB0304Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;
@RequiredArgsConstructor
@RestController
@Getter(AccessLevel.PROTECTED)
public class DPB0304Controller {
    private final DPB0304Service service;

    @PostMapping(value = "/dgrv4/11/DPB0304", //
            consumes = MediaType.APPLICATION_JSON_VALUE, //
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<DPB0304Resp> dbSyncHistory(@RequestHeader HttpHeaders headers //
            , @RequestBody TsmpBaseReq<DPB0304Req> req) {
        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        DPB0304Resp resp = null;
        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
            resp = service.queryH2ConfigSyncHistory(tsmpHttpHeader.getAuthorization(),req.getBody(),req.getReqHeader());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }
}
