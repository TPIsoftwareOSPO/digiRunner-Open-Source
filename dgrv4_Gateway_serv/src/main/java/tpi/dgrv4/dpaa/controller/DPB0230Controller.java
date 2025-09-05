package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.DPB0229Service;
import tpi.dgrv4.dpaa.service.DPB0230Service;
import tpi.dgrv4.dpaa.vo.DPB0229Req;
import tpi.dgrv4.dpaa.vo.DPB0229Resp;
import tpi.dgrv4.dpaa.vo.DPB0230Req;
import tpi.dgrv4.dpaa.vo.DPB0230Resp;
import tpi.dgrv4.gateway.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class DPB0230Controller {

    private final DPB0230Service service;

    @Autowired
    public DPB0230Controller(DPB0230Service service) {
        super();
        this.service = service;
    }

    @PostMapping(value = "/dgrv4/11/DPB0230", //
            consumes = MediaType.APPLICATION_JSON_VALUE, //
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<DPB0230Resp> enableClientCert(@RequestHeader HttpHeaders headers //
            , @RequestBody TsmpBaseReq<DPB0230Req> req) {
        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        DPB0230Resp resp = null;
        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
            resp = service.enableClientCert(req.getBody());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }
}
