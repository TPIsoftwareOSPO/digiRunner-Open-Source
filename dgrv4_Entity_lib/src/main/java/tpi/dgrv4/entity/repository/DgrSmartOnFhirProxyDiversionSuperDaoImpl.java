package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrSmartOnFhirProxyDiversion;

public class DgrSmartOnFhirProxyDiversionSuperDaoImpl extends SuperDaoImpl<DgrSmartOnFhirProxyDiversion>
        implements DgrSmartOnFhirProxyDiversionSuperDao {

    @Override
    public Class<DgrSmartOnFhirProxyDiversion> getEntityType() {
        return DgrSmartOnFhirProxyDiversion.class;
    }

}
