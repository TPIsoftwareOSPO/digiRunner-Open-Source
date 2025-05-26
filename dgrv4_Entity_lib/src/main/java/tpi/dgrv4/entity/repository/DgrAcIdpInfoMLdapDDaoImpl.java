package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrAcIdpInfoMLdapD;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoMLdapM;

public class DgrAcIdpInfoMLdapDDaoImpl extends SuperDaoImpl<DgrAcIdpInfoMLdapD> implements DgrAcIdpInfoMLdapDSuperDao {

	@Override
	public Class<DgrAcIdpInfoMLdapD> getEntityType() {
		return DgrAcIdpInfoMLdapD.class;
	}
}