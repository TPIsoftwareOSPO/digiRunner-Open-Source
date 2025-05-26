package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrGtwIdpInfoCus;

public class DgrGtwIdpInfoCusSuperDaoImpl extends SuperDaoImpl<DgrGtwIdpInfoCus> implements DgrGtwIdpInfoCusSuperDao {

    @Override
    public Class<DgrGtwIdpInfoCus> getEntityType() {
        return DgrGtwIdpInfoCus.class;
    }

}
