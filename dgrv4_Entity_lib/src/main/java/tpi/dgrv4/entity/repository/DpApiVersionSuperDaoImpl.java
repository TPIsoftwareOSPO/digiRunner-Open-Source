package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DpApiVersion;

public class DpApiVersionSuperDaoImpl extends SuperDaoImpl<DpApiVersion> implements DpApiVersionSuperDao {

	@Override
	public Class<DpApiVersion> getEntityType() {
		return DpApiVersion.class;
	}
}
