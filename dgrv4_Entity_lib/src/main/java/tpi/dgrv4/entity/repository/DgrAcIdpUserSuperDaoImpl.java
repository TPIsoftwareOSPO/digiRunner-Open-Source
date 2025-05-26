package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrAcIdpUser;

public class DgrAcIdpUserSuperDaoImpl extends SuperDaoImpl<DgrAcIdpUser> implements DgrAcIdpUserSuperDao {

	@Override
	public Class<DgrAcIdpUser> getEntityType() {
		return DgrAcIdpUser.class;
	}
}
