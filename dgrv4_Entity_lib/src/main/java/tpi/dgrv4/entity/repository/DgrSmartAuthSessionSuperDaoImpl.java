package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrSmartAuthSession;

public class DgrSmartAuthSessionSuperDaoImpl extends SuperDaoImpl<DgrSmartAuthSession> implements DgrSmartAuthSessionSuperDao {

    @Override
    public Class<DgrSmartAuthSession> getEntityType() {
        return DgrSmartAuthSession.class;
    }
}
