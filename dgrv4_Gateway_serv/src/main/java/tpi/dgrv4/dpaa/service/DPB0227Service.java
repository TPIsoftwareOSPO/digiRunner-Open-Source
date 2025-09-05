package tpi.dgrv4.dpaa.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0227Req;
import tpi.dgrv4.dpaa.vo.DPB0227Resp;
import tpi.dgrv4.entity.entity.DgrMtlsClientCert;
import tpi.dgrv4.entity.repository.DgrMtlsClientCertDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.util.Optional;

@Service
public class DPB0227Service {

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private DgrMtlsClientCertDao dgrMtlsClientCertDao;

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private  CommService commService;
    public DPB0227Resp deleteClientCert(DPB0227Req req) {
        DPB0227Resp resp = new DPB0227Resp();
        try {
            Long longId = getCommService().getLongId(req.getDgrMtlsClientCertId(),"dgrMtlsClientCertId");
            Optional<DgrMtlsClientCert> mtlsOpt = getDgrMtlsClientCertDao().findById(longId);
            if (mtlsOpt.isEmpty())
                throw TsmpDpAaRtnCode._1298.throwing();
            else{
                DgrMtlsClientCert mtls = mtlsOpt.get();
                if (mtls.getEnable().equals("Y"))
                    throw TsmpDpAaRtnCode._2039.throwing();
            }

            getDgrMtlsClientCertDao().deleteById(longId);
        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1287.throwing();
        }
        return resp;

    }

}
