package tpi.dgrv4.dpaa.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0229Req;
import tpi.dgrv4.dpaa.vo.DPB0229Resp;
import tpi.dgrv4.dpaa.vo.DPB0229RespItem;
import tpi.dgrv4.entity.entity.DgrMtlsClientCert;
import tpi.dgrv4.entity.repository.DgrMtlsClientCertDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DPB0229Service {

    @Setter(onMethod_ = @Autowired)
    @Getter(value = AccessLevel.PROTECTED)
    private DgrMtlsClientCertDao dgrMtlsClientCertDao;

    public DPB0229Resp queryClientCertList(DPB0229Req req) {
        DPB0229Resp resp = new DPB0229Resp();
        try {
            List<DgrMtlsClientCert> allList = getDgrMtlsClientCertDao().findAll();
            if (CollectionUtils.isEmpty(allList)) {
                throw TsmpDpAaRtnCode._1298.throwing();
            }
            List<DPB0229RespItem> infoList = new ArrayList<>();
            for (DgrMtlsClientCert dmcc : allList) {
                DPB0229RespItem item = getDpb0229RespItem(dmcc);
                infoList.add(item);
            }
            resp.setInfoList(infoList);
        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
        return resp;
    }

    private DPB0229RespItem getDpb0229RespItem(DgrMtlsClientCert dmcc) {
        DPB0229RespItem item = new DPB0229RespItem();
        item.setDgrMtlsClientCertId(dmcc.getHexId());
        item.setEnable(dmcc.getEnable());
        item.setCRTExpireDate(dmcc.getCrtExpireDate());
        item.setRootCAExpireDate(dmcc.getRootCaExpireDate());
        String hostAndPort = dmcc.getHost() + ":" + dmcc.getPort();
        item.setHostAndPort(hostAndPort);
        Date updateTime = dmcc.getUpdateDateTime() != null ? dmcc.getUpdateDateTime() : dmcc.getCreateDateTime();
        String updateUser = StringUtils.hasLength(dmcc.getUpdateUser()) ? dmcc.getUpdateUser() :
                dmcc.getCreateUser();
        item.setUpdateUser(updateUser);
        item.setUpdateDateTime(updateTime);
        return item;
    }

}
