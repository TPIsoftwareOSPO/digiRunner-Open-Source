package tpi.dgrv4.dpaa.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.service.DPB0298Service;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0298Req;
import tpi.dgrv4.dpaa.vo.DPB0298Resp;
import tpi.dgrv4.dpaa.vo.DPB9929Req;
import tpi.dgrv4.dpaa.vo.DPB9929Resp;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

@RestController
public class DPB0298Controller {
    @Setter(onMethod_ = @Autowired)
    private DPB0298Service service;

    @PostMapping(value = "/dgrv4/11/DPB0298", //
            consumes = MediaType.APPLICATION_JSON_VALUE, //
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<DPB0298Resp> exportWebhook(@RequestHeader HttpHeaders headers //
            , @RequestBody TsmpBaseReq<DPB0298Req> req, HttpServletResponse response) {
        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        DPB0298Resp resp = null;
        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);

            resp = service.exportWebhook(req.getBody(), response);


        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }
        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);

    }
}
