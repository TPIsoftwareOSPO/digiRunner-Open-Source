package tpi.dgrv4.dpaa.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0293Req;
import tpi.dgrv4.dpaa.vo.DPB0293Resp;
import tpi.dgrv4.entity.entity.DgrGrpcProxyMap;
import tpi.dgrv4.entity.repository.DgrGrpcProxyMapDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.grpcproxy.manager.DynamicGrpcProxyManager;

import java.util.Optional;

@Service
public class DPB0293Service {
    private final DgrGrpcProxyMapDao dgrGrpcProxyMapDao;
    private final DynamicGrpcProxyManager dynamicGrpcProxyManager;

    @Autowired
    public DPB0293Service(DgrGrpcProxyMapDao dgrGrpcProxyMapDao,
                          DynamicGrpcProxyManager dynamicGrpcProxyManager) {
        super();
        this.dgrGrpcProxyMapDao = dgrGrpcProxyMapDao;
        this.dynamicGrpcProxyManager = dynamicGrpcProxyManager;
    }

    @Transactional
    public DPB0293Resp deleteGrpcProxy(TsmpAuthorization authorization, DPB0293Req req) {

        try{
            String gRPCProxyMapId  = req.getgRPCProxyMapId();
            Long id = getId(gRPCProxyMapId);

            if(gRPCProxyMapId == null || gRPCProxyMapId.isEmpty()){
                throw TsmpDpAaRtnCode._1350.throwing("gRPCProxyMapId");
            }

            Optional<DgrGrpcProxyMap> opt = getDgrGrpcProxyMapDao().findById(id);
            if(opt.isEmpty()){
                throw TsmpDpAaRtnCode._1298.throwing();
            }else {
                DgrGrpcProxyMap dgrGrpcProxyMap = opt.get();
                getDgrGrpcProxyMapDao().delete(dgrGrpcProxyMap);
                dynamicGrpcProxyManager.deleteProxyMapping(dgrGrpcProxyMap);
            }

        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing();
        }
        return new DPB0293Resp();
    }

    private Long getId(String id) {

        if (!StringUtils.hasText(id)) {
            throw TsmpDpAaRtnCode._2025.throwing("{{id}}");
        }

        return isValidLong(id) ? Long.parseLong(id)
                : RandomSeqLongUtil.toLongValue(id);
    }

    private boolean isValidLong(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    protected DgrGrpcProxyMapDao getDgrGrpcProxyMapDao() {
        return this.dgrGrpcProxyMapDao;
    }
}
