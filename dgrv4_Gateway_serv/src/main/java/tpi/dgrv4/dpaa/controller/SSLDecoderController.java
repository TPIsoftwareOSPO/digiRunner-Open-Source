package tpi.dgrv4.dpaa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.service.SSLDecoderService;
import tpi.dgrv4.dpaa.vo.SSLDecoderReq;
import tpi.dgrv4.dpaa.vo.SslDetailsResp;
import tpi.dgrv4.gateway.vo.*;

@RestController
public class SSLDecoderController {

    private final SSLDecoderService service;
    @Autowired
    public SSLDecoderController(SSLDecoderService service) {
        super();
        this.service = service;
    }

    @PostMapping(value = "/dgrv4/SSLDecoder", //
            consumes = MediaType.APPLICATION_JSON_VALUE, //
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<SslDetailsResp> sendMail(@RequestHeader HttpHeaders headers //
            , @RequestBody TsmpBaseReq<SSLDecoderReq> req) {
        SslDetailsResp resp;
        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);

            resp= service.getSSLInfo( req.getBody());
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());

        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }
}
