package tpi.dgrv4.dpaa.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.RegexpConstant;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0230Req;
import tpi.dgrv4.dpaa.vo.DPB0230Resp;
import tpi.dgrv4.entity.entity.DgrMtlsClientCert;
import tpi.dgrv4.entity.repository.DgrMtlsClientCertDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.util.ArrayList;
import java.util.List;

@Service
public class DPB0230Service {

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private DgrMtlsClientCertDao dgrMtlsClientCertDao;

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private CommService commService;

    public DPB0230Resp enableClientCert(DPB0230Req req) {
        DPB0230Resp resp = new DPB0230Resp();
        try {
            checkParam(req);
            List<String> idList = req.getIdList();
            List<Long> longIdList = new ArrayList<>();
            idList.forEach(e -> {
                longIdList.add(getCommService().getLongId(e, "dgrMtlsClientCertId"));
            });

            List<DgrMtlsClientCert> list = getDgrMtlsClientCertDao().findAllById(longIdList);
            if (list == null || list.size() == 0)
                throw TsmpDpAaRtnCode._1298.throwing();
            for (DgrMtlsClientCert dmcc : list) {
                if (dmcc == null)
                    throw TsmpDpAaRtnCode._1298.throwing();
                dmcc.setEnable(req.getEnable());
                getDgrMtlsClientCertDao().save(dmcc);
            }
        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
        return resp;
    }

    private void checkParam(DPB0230Req req) {
        String enable = req.getEnable();
        if (!StringUtils.hasLength(enable)) {
            throw TsmpDpAaRtnCode._2025.throwing("{{enable}}");
        }
        if (!enable.matches(RegexpConstant.Y_OR_N)) {
            throw TsmpDpAaRtnCode._1352.throwing("{{enable}}");
        }

        List<String> idList = req.getIdList();
        if (idList == null || idList.size() == 0)
            throw TsmpDpAaRtnCode._2009.throwing("1");

    }


}
