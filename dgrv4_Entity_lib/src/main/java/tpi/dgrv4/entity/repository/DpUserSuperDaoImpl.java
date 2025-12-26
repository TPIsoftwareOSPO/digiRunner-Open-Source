package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DpUser;

public class DpUserSuperDaoImpl extends SuperDaoImpl<DpUser> implements DpUserSuperDao {

	@Override
	public Class<DpUser> getEntityType() {
		return DpUser.class;
	}
}
