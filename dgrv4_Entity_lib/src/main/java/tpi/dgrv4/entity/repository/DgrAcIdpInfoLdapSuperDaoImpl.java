package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrAcIdpInfoLdap;

public class DgrAcIdpInfoLdapSuperDaoImpl extends SuperDaoImpl<DgrAcIdpInfoLdap> implements DgrAcIdpInfoLdapSuperDao {

	@Override
	public Class<DgrAcIdpInfoLdap> getEntityType() {
		return DgrAcIdpInfoLdap.class;
	}
}
