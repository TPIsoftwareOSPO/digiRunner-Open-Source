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
import tpi.dgrv4.dpaa.service.DPB0226Service;
import tpi.dgrv4.dpaa.vo.DPB0225Req;
import tpi.dgrv4.dpaa.vo.DPB0225Resp;
import tpi.dgrv4.dpaa.vo.DPB0226Req;
import tpi.dgrv4.dpaa.vo.DPB0226Resp;
import tpi.dgrv4.gateway.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class DPB0226Controller {

    private final DPB0226Service service;
    @Autowired
    public DPB0226Controller(DPB0226Service service) {
        super();
        this.service = service;
    }

    @PostMapping(value = "/dgrv4/11/DPB0226", params = { "before" }, //
            consumes = MediaType.APPLICATION_JSON_VALUE, //
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<BeforeControllerResp> updateClientCert_before(@RequestHeader HttpHeaders headers //
            , @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
        try {
            return ControllerUtil.getReqConstraints(req, new DPB0225Req());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }
    }
    @PostMapping(value = "/dgrv4/11/DPB0226", //
            consumes = MediaType.APPLICATION_JSON_VALUE, //
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<DPB0226Resp> updateClientCert(@RequestHeader HttpHeaders headers //
            , @RequestBody TsmpBaseReq<DPB0226Req> req) {
        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        DPB0226Resp resp = null;
        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
            resp = service.updateClientCert(tsmpHttpHeader.getAuthorization(), req.getBody());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }
}
