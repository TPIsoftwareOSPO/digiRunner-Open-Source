package tpi.dgrv4.gateway.component.cache.proxy;

import com.esotericsoftware.kryo.Kryo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tpi.dgrv4.common.component.cache.core.DaoGenericCache;
import tpi.dgrv4.common.component.cache.proxy.DaoCacheProxy;
import tpi.dgrv4.entity.entity.DgrMtlsClientCert;
import tpi.dgrv4.entity.repository.DgrMtlsClientCertDao;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
public class DgrMtlsClientCertCacheProxy extends DaoCacheProxy {


    private DgrMtlsClientCertDao dgrMtlsClientCertDao;
    @Autowired
    public DgrMtlsClientCertCacheProxy(ObjectMapper objectMapper, DaoGenericCache cache ,DgrMtlsClientCertDao dgrMtlsClientCertDao) {
        super(objectMapper, cache);
        this.dgrMtlsClientCertDao = dgrMtlsClientCertDao;
    }

    public DgrMtlsClientCert findByHostAndPort(String host, int port) {
        Supplier<DgrMtlsClientCert> supplier = () -> {
            return getDgrMtlsClientCertDao().findByHostAndPort(host, port);
        };
        return getOne("findByHostAndPort", supplier, DgrMtlsClientCert.class, host, port).orElse(null);
    }

    public DgrMtlsClientCert findByHostAndPortAndEnable(String host, int port, String enable) {
        Supplier<DgrMtlsClientCert> supplier = () -> {
            DgrMtlsClientCert vo = getDgrMtlsClientCertDao().findByHostAndPortAndEnable(host, port, enable);
            return vo;
        };
        return getOne("findByHostAndPortAndEnable", supplier, DgrMtlsClientCert.class, host, port, enable).orElse(null);
    }

    public Boolean existsByHostAndPort(String host, int port) {
        Supplier<Boolean> supplier = () -> {
            return getDgrMtlsClientCertDao().existsByHostAndPort(host, port);
        };
        return    getOne("existsByHostAndPort", supplier, Boolean.class, host, port).orElse(null);
    }

    protected DgrMtlsClientCertDao getDgrMtlsClientCertDao() {
        return this.dgrMtlsClientCertDao;
    }

    @Override
    protected Class<?> getDaoClass() {
        return DgrMtlsClientCertDao.class;
    }

    @Override
    protected Consumer<String> getTraceLogger() {
        return (String log) -> {

        };
    }

    @Override
    protected void kryoRegistration(Kryo kryo) {
        kryo.register(DgrMtlsClientCert.class);
    }
}
