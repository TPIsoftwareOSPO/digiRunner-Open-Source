package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrGtwIdpInfoJdbc;

public class DgrGtwIdpInfoJdbcSuperDaoImpl extends SuperDaoImpl<DgrGtwIdpInfoJdbc>
		implements DgrGtwIdpInfoJdbcSuperDao {

	@Override
	public Class<DgrGtwIdpInfoJdbc> getEntityType() {
		return DgrGtwIdpInfoJdbc.class;
	}
}