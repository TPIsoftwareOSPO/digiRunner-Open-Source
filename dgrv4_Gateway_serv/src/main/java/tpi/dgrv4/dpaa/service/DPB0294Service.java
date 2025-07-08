package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.vo.DPB0294Req;
import tpi.dgrv4.dpaa.vo.DPB0294Resp;
import tpi.dgrv4.dpaa.vo.DPB0294RespItem;
import tpi.dgrv4.entity.entity.DgrGrpcProxyMap;
import tpi.dgrv4.entity.repository.DgrGrpcProxyMapDao;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.util.ArrayList;
import java.util.List;

@Service
public class DPB0294Service {
    private final DgrGrpcProxyMapDao dgrGrpcProxyMapDao;

    @Autowired
    public DPB0294Service(DgrGrpcProxyMapDao dgrGrpcProxyMapDao) {
        super();
        this.dgrGrpcProxyMapDao = dgrGrpcProxyMapDao;
    }

    public DPB0294Resp fetchAllUpstreamServices(TsmpAuthorization authorization, DPB0294Req req){
        List<DgrGrpcProxyMap> upstreamList = getDgrGrpcProxyMapDao().findAll();
        List<DPB0294RespItem> infoList = new ArrayList<>();

        if(upstreamList.isEmpty()){
            throw TsmpDpAaRtnCode._1298.throwing();
        }

        upstreamList.forEach(upstream -> {
            DPB0294RespItem item = new DPB0294RespItem();
            item.setgRPCProxyMapId(String.valueOf(upstream.getGrpcproxyMapId()));
            item.setServiceName(upstream.getServiceName());
            item.setProxyHostName(upstream.getProxyHostName());
            item.setTargetHostName(upstream.getTargetHostName());
            item.setTargetPort(String.valueOf(upstream.getTargetPort()));
            item.setSendTimeoutMs(String.valueOf(upstream.getSendTimeoutMs()));
            item.setConnectTimeoutMs(String.valueOf(upstream.getConnectTimeoutMs()));
            item.setReadTimeoutMs(String.valueOf(upstream.getReadTimeoutMs()));
            item.setSecureMode(upstream.getSecureMode());
            item.setServerCertContent(upstream.getServerCertContent());
            item.setServerKeyContent(upstream.getServerKeyContent());
            item.setAutoTrustUpstreamCerts(upstream.isAutoTrustUpstreamCerts());
            item.setTrustedCertsContent(upstream.getTrustedCertsContent());
            item.setCreateUser(upstream.getCreateUser());
            item.setCreateDateTime(String.valueOf(upstream.getCreateDateTime().getTime()));
            item.setUpdateUser(upstream.getUpdateUser());
            if(upstream.getUpdateDateTime() != null){
                item.setUpdateDateTime(String.valueOf(upstream.getUpdateDateTime().getTime()));
            }else {
                item.setUpdateDateTime(String.valueOf(upstream.getCreateDateTime().getTime()));
            }
            item.setEnable(upstream.getEnable());
            infoList.add(item);
        });

        DPB0294Resp resp = new DPB0294Resp();
        resp.setInfoList(infoList);
        return resp;
    }

    protected DgrGrpcProxyMapDao getDgrGrpcProxyMapDao() {
        return this.dgrGrpcProxyMapDao;
    }
}
