package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrGtwIdpInfoA;

public class DgrGtwIdpInfoADaoImpl extends SuperDaoImpl<DgrGtwIdpInfoA> implements DgrGtwIdpInfoASuperDao {

	@Override
	public Class<DgrGtwIdpInfoA> getEntityType() {
		return DgrGtwIdpInfoA.class;
	}
}
