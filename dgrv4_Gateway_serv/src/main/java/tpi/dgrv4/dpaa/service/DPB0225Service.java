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
import tpi.dgrv4.dpaa.vo.DPB0225Req;
import tpi.dgrv4.dpaa.vo.DPB0225Resp;
import tpi.dgrv4.entity.entity.DgrMtlsClientCert;
import tpi.dgrv4.entity.repository.DgrMtlsClientCertDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.util.Date;

@Service

public class DPB0225Service {

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private DgrMtlsClientCertDao dgrMtlsClientCertDao;

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private CommService commService;

    public DPB0225Resp createClientCert(TsmpAuthorization authorization, DPB0225Req req) {
        DPB0225Resp resp = new DPB0225Resp();
        try {
            String host = req.getHost();
            int port = req.getPort();
            boolean isExists = getDgrMtlsClientCertDao().existsByHostAndPort(host, port);
            if (isExists) {
                throw TsmpDpAaRtnCode._1284.throwing("Host and Port");
            }

            DgrMtlsClientCert dmcc = new DgrMtlsClientCert();
            String rootCa = req.getRootCa();
            String clientCert = req.getClientCert();
            Date rootCaExpireDate = getCommService().checkCertAndGetExpire("Root Ca", rootCa);
            Date crtExpireDate = getCommService().checkCertAndGetExpire("Client Cert", clientCert);
            getCommService().checkPrivateKey(req.getClientKey());
            dmcc.setHost(host);
            dmcc.setPort(port);
            dmcc.setRootCa(rootCa);
            dmcc.setClientCert(clientCert);
            dmcc.setCrtExpireDate(crtExpireDate);
            dmcc.setRootCaExpireDate(rootCaExpireDate);
            String keyMima = req.getKeyMima();
            //若有金鑰密碼，且非ENC開頭，則要做ENC
            if (StringUtils.hasLength(keyMima) && !(keyMima.startsWith("ENC(") && keyMima.endsWith(")"))) {
                dmcc.setKeyMima(getCommService().getENCEncode(keyMima));

            } else {
                dmcc.setKeyMima(keyMima);
            }
            dmcc.setEnable("N");
            dmcc.setClientKey(req.getClientKey());
            dmcc.setRemark(req.getRemark());
            dmcc.setCreateUser(authorization.getUserName());
            dmcc.setCreateDateTime(DateTimeUtil.now());
            getDgrMtlsClientCertDao().save(dmcc);

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1288.throwing();
        }
        return resp;
    }


}
