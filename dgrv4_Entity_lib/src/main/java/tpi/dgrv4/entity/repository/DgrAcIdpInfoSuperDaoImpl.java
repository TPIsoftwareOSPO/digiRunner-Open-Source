package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrAcIdpInfo;

public class DgrAcIdpInfoSuperDaoImpl extends SuperDaoImpl<DgrAcIdpInfo> implements DgrAcIdpInfoSuperDao {

	@Override
	public Class<DgrAcIdpInfo> getEntityType() {
		return DgrAcIdpInfo.class;
	}

}
