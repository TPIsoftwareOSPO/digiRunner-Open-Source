package tpi.dgrv4.dpaa.controller;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.service.DPB0303Service;
import tpi.dgrv4.gateway.vo.*;

@RequiredArgsConstructor
@RestController
@Getter(AccessLevel.PROTECTED)
public class DPB0303Controller {

    private final DPB0303Service service;

    @PostMapping(value = "/dgrv4/11/DPB0303", //
            consumes = MediaType.APPLICATION_JSON_VALUE, //
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<DPB0303Resp> dbSyncTrigger(@RequestHeader HttpHeaders headers //
            , @RequestBody TsmpBaseReq<DPB0303Req> req) {
        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        DPB0303Resp resp = null;
        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
            resp = service.triggerManualSync(tsmpHttpHeader.getAuthorization(),req.getBody());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }

}
