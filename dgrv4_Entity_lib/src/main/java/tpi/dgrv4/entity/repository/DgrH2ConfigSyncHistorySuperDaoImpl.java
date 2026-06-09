package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrH2ConfigSyncHistory;

public class DgrH2ConfigSyncHistorySuperDaoImpl extends SuperDaoImpl<DgrH2ConfigSyncHistory> implements DgrH2ConfigSyncHistorySuperDao {
    @Override
    public Class<DgrH2ConfigSyncHistory> getEntityType() {
        return DgrH2ConfigSyncHistory.class;
    }
}
