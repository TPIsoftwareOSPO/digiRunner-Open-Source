package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrGtwIdpInfoO;

public class DgrGtwIdpInfoOSuperDaoImpl extends SuperDaoImpl<DgrGtwIdpInfoO> implements DgrGtwIdpInfoOSuperDao{

	@Override
	public Class<DgrGtwIdpInfoO> getEntityType() {
		return DgrGtwIdpInfoO.class;
	}
}
