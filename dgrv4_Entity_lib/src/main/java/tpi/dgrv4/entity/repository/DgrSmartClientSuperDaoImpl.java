package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrSmartClient;

public class DgrSmartClientSuperDaoImpl extends SuperDaoImpl<DgrSmartClient> implements DgrSmartClientSuperDao {

    @Override
    public Class<DgrSmartClient> getEntityType() {
        return DgrSmartClient.class;
    }
}
