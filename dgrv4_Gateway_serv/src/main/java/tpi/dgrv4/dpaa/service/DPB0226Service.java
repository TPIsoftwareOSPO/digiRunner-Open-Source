package tpi.dgrv4.dpaa.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0226Req;
import tpi.dgrv4.dpaa.vo.DPB0226Resp;
import tpi.dgrv4.entity.entity.DgrMtlsClientCert;
import tpi.dgrv4.entity.repository.DgrMtlsClientCertDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.util.Date;

@Service
public class DPB0226Service {


    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private DgrMtlsClientCertDao dgrMtlsClientCertDao;

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private CommService commService;

    public DPB0226Resp updateClientCert(TsmpAuthorization authorization, DPB0226Req req) {
        DPB0226Resp resp = new DPB0226Resp();

        try {
            String id = req.getDgrMtlsClientCertId();
            Long longId = getCommService().getLongId(id, "DgrMtlsClientCertId");

            DgrMtlsClientCert dmcc = getDgrMtlsClientCertDao().findById(longId).orElse(null);
            if (dmcc == null) {
                throw TsmpDpAaRtnCode._1298.throwing();
            }
            String host = req.getHost();
            int port = req.getPort();
            DgrMtlsClientCert existHostAndPort = getDgrMtlsClientCertDao().findByHostAndPort(host, port);
            if (existHostAndPort != null && !longId.equals(existHostAndPort.getDgrMtlsClientCertId())) {
                throw TsmpDpAaRtnCode._1284.throwing("Host and Port");
            }
            String rootCa = req.getRootCa();
            String clientCert = req.getClientCert();
            //檢查 Root Ca 與 Client Cert 與 privateKey 格式是否正確
            Date rootCaExpireDate = getCommService().checkCertAndGetExpire("Root Ca", rootCa);
            Date crtExpireDate = getCommService().checkCertAndGetExpire("Client Cert", clientCert);
            dmcc.setHost(host);
            dmcc.setPort(port);
            dmcc.setCrtExpireDate(crtExpireDate);
            dmcc.setRootCaExpireDate(rootCaExpireDate);
            getCommService().checkPrivateKey(req.getClientKey());
            dmcc.setRootCa(rootCa);
            dmcc.setClientCert(clientCert);
            String keyMima = req.getKeyMima();
            //若有金鑰密碼，且非ENC開頭，則要做ENC
            if (StringUtils.hasLength(keyMima) && !(keyMima.startsWith("ENC(") && keyMima.endsWith(")"))) {
                dmcc.setKeyMima(getCommService().getENCEncode(keyMima));
            } else {
                dmcc.setKeyMima(keyMima);
            }
            dmcc.setEnable(dmcc.getEnable());
            dmcc.setClientKey(req.getClientKey());
            dmcc.setRemark(req.getRemark());
            dmcc.setUpdateUser(authorization.getUserName());
            dmcc.setUpdateDateTime(DateTimeUtil.now());
            getDgrMtlsClientCertDao().save(dmcc);

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1286.throwing();
        }
        return resp;
    }
}
