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
import tpi.dgrv4.dpaa.service.DPB0292Service;
import tpi.dgrv4.dpaa.vo.DPB0292Req;
import tpi.dgrv4.dpaa.vo.DPB0292Resp;
import tpi.dgrv4.gateway.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class DPB0292Controller {
    private DPB0292Service service;

    @Autowired
    public DPB0292Controller(DPB0292Service service) {
        super();
        this.service = service;
    }

    @PostMapping(value = "/dgrv4/11/DPB0292", params = { "before" }, //
            consumes = MediaType.APPLICATION_JSON_VALUE, //
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<BeforeControllerResp> updateUpstreamService_before(@RequestHeader HttpHeaders headers //
            , @RequestBody TsmpBaseReq<BeforeControllerReq> req) {
        try {
            return ControllerUtil.getReqConstraints(req, new DPB0292Req());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }
    }

    @PostMapping(value = "/dgrv4/11/DPB0292", //
            consumes = MediaType.APPLICATION_JSON_VALUE, //
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<DPB0292Resp> updateUpstreamService(@RequestHeader HttpHeaders headers //
            , @RequestBody TsmpBaseReq<DPB0292Req> req) {
        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        DPB0292Resp resp = null;

        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
            resp = service.updateGrpcProxy(tsmpHttpHeader.getAuthorization(), req.getBody());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }
        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }
}
