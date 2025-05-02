package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrAcIdpInfoCus;

public class DgrAcIdpInfoCusSuperDaoImpl extends SuperDaoImpl<DgrAcIdpInfoCus> implements DgrAcIdpInfoCusSuperDao {

	@Override
	public Class<DgrAcIdpInfoCus> getEntityType() {
		return DgrAcIdpInfoCus.class;
	}

}
