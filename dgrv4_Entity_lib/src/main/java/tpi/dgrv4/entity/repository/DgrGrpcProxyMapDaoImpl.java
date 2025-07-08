package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrGrpcProxyMap;

public class DgrGrpcProxyMapDaoImpl extends SuperDaoImpl<DgrGrpcProxyMap> implements DgrGrpcProxyMapSuperDao{
    @Override
    public Class<DgrGrpcProxyMap> getEntityType() {
        return DgrGrpcProxyMap.class;
    }
}
