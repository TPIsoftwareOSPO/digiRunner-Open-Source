package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.vo.BeforeControllerReq;
import tpi.dgrv4.common.vo.BeforeControllerResp;
import tpi.dgrv4.dpaa.service.DPB0225Service;
import tpi.dgrv4.dpaa.vo.*;
import tpi.dgrv4.gateway.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class DPB0225Controller {

    private final  DPB0225Service service;

    @Autowired
    public DPB0225Controller(DPB0225Service service) {
        super();
        this.service = service;
    }

    @PostMapping(value = "/dgrv4/11/DPB0225", params = { "before" }, //
            consumes = MediaType.APPLICATION_JSON_VALUE, //
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<BeforeControllerResp> createClientCert_before(@RequestHeader HttpHeaders headers //
            , @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
        try {
            return ControllerUtil.getReqConstraints(req, new DPB0225Req());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }
    }
    @PostMapping(value = "/dgrv4/11/DPB0225", //
            consumes = MediaType.APPLICATION_JSON_VALUE, //
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<DPB0225Resp> createClientCert(@RequestHeader HttpHeaders headers //
            , @RequestBody TsmpBaseReq<DPB0225Req> req) {
        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        DPB0225Resp resp = null;
        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
            resp = service.createClientCert(tsmpHttpHeader.getAuthorization(), req.getBody());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }
}
