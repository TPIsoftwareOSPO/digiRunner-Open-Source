package tpi.dgrv4.dpaa.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.DPB0299Req;
import tpi.dgrv4.dpaa.vo.DPB0299Resp;
import tpi.dgrv4.dpaa.vo.DPB9932Req;
import tpi.dgrv4.dpaa.vo.DPB9932Resp;
import tpi.dgrv4.escape.DPB0299Service;
import tpi.dgrv4.escape.DPB9932Service;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;
@RestController
public class DPB0299Controller {
    @Setter(onMethod_ = @Autowired)
    private DPB0299Service service;
    @Setter(onMethod_ = @Autowired)
    private ObjectMapper objectMapper;

    @PostMapping(value = "/dgrv4/11/DPB0299", //
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE, //
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TsmpBaseResp<DPB0299Resp> importWebsocketProxy(@RequestHeader HttpHeaders headers,
                                                          @RequestParam("req") String strReq, @RequestParam("file") MultipartFile mFile) {
        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        DPB0299Resp resp = null;
        TsmpBaseReq<DPB0299Req> req = null;
        try {
            req = objectMapper.readValue(strReq, new TypeReference<TsmpBaseReq<DPB0299Req>>() {
            });
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1290.throwing();
        }
        try {
            ControllerUtil.validateRequest(tsmpHttpHeader.getAuthorization(), req);
            resp = service.importWebhook(tsmpHttpHeader.getAuthorization(), mFile);
        } catch (Exception e) {
            throw new TsmpDpAaException(e, req.getReqHeader());
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), resp);
    }
}
