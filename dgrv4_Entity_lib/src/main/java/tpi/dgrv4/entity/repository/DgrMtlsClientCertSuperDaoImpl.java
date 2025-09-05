package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrMtlsClientCert;

public class DgrMtlsClientCertSuperDaoImpl extends SuperDaoImpl<DgrMtlsClientCert> implements DgrMtlsClientCertSuperDao{
    @Override
    public Class<DgrMtlsClientCert> getEntityType() {
        return DgrMtlsClientCert.class;
    }
}
