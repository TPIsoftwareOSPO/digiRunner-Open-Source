package tpi.dgrv4.dpaa.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import oshi.util.tuples.Pair;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0231Req;
import tpi.dgrv4.dpaa.vo.DPB0231Resp;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.CertificateInfo;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil2;

import java.util.HashMap;

@Service
public class DPB0231Service {

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private CommService commService;

    public DPB0231Resp checkMtlsConnection(DPB0231Req req) {
        DPB0231Resp resp = new DPB0231Resp();
        String msg = "";
        boolean success = false;
        try {
            checkParams(req);

            getCommService().checkPrivateKey(req.getClientKey());
            getCommService().checkCertAndGetExpire("Client Cert", req.getClientCert());
            getCommService().checkCertAndGetExpire("Root Ca", req.getRootCa());
            String keyMima = null;
            try {
                keyMima = getCommService().getENCPlainVal(req.getKeyMima());
            } catch (Exception e) {
                throw TsmpDpAaRtnCode._1434.throwing("{{keyMima}}");
            }
            var certBuilder = CertificateInfo.builder();
            certBuilder
                    .rootCA(req.getRootCa())
                    .clientCert(req.getClientCert())
                    .clientKey(req.getClientKey())
                    .keyMima(keyMima);
            try {
                var pair = checkConnection(req.getHost(), req.getPort(), certBuilder.build());
                success = pair.getA();
                msg = pair.getB();
            } catch (Exception e) {
                success = false;
                msg = e.getMessage();
                TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            }
            resp.setMsg(msg);
            resp.setSuccess(success);

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }


        return resp;
    }

    protected Pair<Boolean, String> checkConnection(String host, int port, CertificateInfo mtlsMap) throws Exception {
        String msg;
        boolean success;
        String requrl = "https://" + host + ":" + port;
        HttpUtil.HttpRespData resp = HttpUtil2.callApiByHttp2(requrl, "HEAD", new HashMap<String, String>(), null, false, true, mtlsMap,true ,null,false);
        resp.fetchByte();
        if (resp.statusCode > 0 && resp.statusCode < 495) {
            success = true;

        } else {
            success = false;
        }
        TPILogger.tl.debug(resp.getLogStr());
        return new Pair<>(success, resp.respStr);
    }

    private void checkParams(DPB0231Req req) {
        checkHasLength(req.getHost(), "host");
        checkHasLength(String.valueOf(req.getPort()), "port");
        checkHasLength(req.getRootCa(), "rootCa");
        checkHasLength(req.getClientCert(), "clientCert");
        checkHasLength(req.getClientKey(), "clientKey");
    }

    private void checkHasLength(String value, String name) {
        if (!StringUtils.hasLength(value))
            throw TsmpDpAaRtnCode._2025.throwing(name);
    }

}
