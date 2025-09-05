package tpi.dgrv4.dpaa.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0228Req;
import tpi.dgrv4.dpaa.vo.DPB0228Resp;
import tpi.dgrv4.entity.entity.DgrMtlsClientCert;
import tpi.dgrv4.entity.repository.DgrMtlsClientCertDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.util.Date;
import java.util.Optional;

@Service
public class DPB0228Service {

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private DgrMtlsClientCertDao dgrMtlsClientCertDao;

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private CommService commService;

    public DPB0228Resp queryClientCertDetail(DPB0228Req req) {
        DPB0228Resp resp = new DPB0228Resp();
        try {

            Long longId = getCommService().getLongId(req.getDgrMtlsClientCertId(), "dgrMtlsClientCertId");
            DgrMtlsClientCert dmcc = getDgrMtlsClientCertDao().findById(longId).orElse(null);
            if (dmcc == null)
                throw TsmpDpAaRtnCode._1298.throwing();

            resp.setClientCert(dmcc.getClientCert());
            resp.setClientKey(dmcc.getClientKey());
            resp.setEnable(dmcc.getEnable());
            resp.setHost(dmcc.getHost());
            resp.setPort(dmcc.getPort());
            resp.setRemark(dmcc.getRemark());
            resp.setKeyMima(dmcc.getKeyMima());
            resp.setDgrMtlsClientCertId(dmcc.getHexId());
            resp.setRootCa(dmcc.getRootCa());
            resp.setCreateDateTime(dmcc.getCreateDateTime());
            resp.setCreateUser(dmcc.getCreateUser());
            resp.setUpdateUser( dmcc.getUpdateUser());
            resp.setUpdateDateTime(dmcc.getUpdateDateTime());

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }

        return resp;

    }


}
