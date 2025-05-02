package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DpApp;

public class DpAppSuperDaoImpl extends SuperDaoImpl<DpApp> implements DpAppSuperDao {

	@Override
	public Class<DpApp> getEntityType() {
		return DpApp.class;
	}
}