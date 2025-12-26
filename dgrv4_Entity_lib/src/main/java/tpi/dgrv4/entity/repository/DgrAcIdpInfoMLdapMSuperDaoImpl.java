package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrAcIdpInfoMLdapM;

public class DgrAcIdpInfoMLdapMSuperDaoImpl extends SuperDaoImpl<DgrAcIdpInfoMLdapM>
		implements DgrAcIdpInfoMLdapMSuperDao {

	@Override
	public Class<DgrAcIdpInfoMLdapM> getEntityType() {
		return DgrAcIdpInfoMLdapM.class;
	}
}