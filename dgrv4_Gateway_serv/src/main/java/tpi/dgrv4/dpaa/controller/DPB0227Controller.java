package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.DPB0227Service;
import tpi.dgrv4.dpaa.vo.DPB0226Req;
import tpi.dgrv4.dpaa.vo.DPB0226Resp;
import tpi.dgrv4.dpaa.vo.DPB0227Req;
import tpi.dgrv4.dpaa.vo.DPB0227Resp;
import tpi.dgrv4.gateway.util.ControllerUtil;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class DPB0227Controller {

    private final DPB0227Service service;
    @Autowired
    public DPB0227Controller(DPB0227Service service) {
        super();
        this.service = service;
    }

    @PostMapping(value = "/dgrv4/11/DPB0227", //
            consumes = MediaType.APPLICATION_JSON_VALUE, //
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<DPB0227Resp> updateClientCert(@RequestHeader HttpHeaders headers //
            , @RequestBody TsmpBaseReq<DPB0227Req> req) {
        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        DPB0227Resp resp = null;
        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
            resp = service.deleteClientCert(req.getBody());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }
}
