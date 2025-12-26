package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrAcIdpInfoApi;

public class DgrAcIdpInfoApiDaoImpl extends SuperDaoImpl<DgrAcIdpInfoApi> implements DgrAcIdpInfoApiSuperDao {

	@Override
	public Class<DgrAcIdpInfoApi> getEntityType() {
		return DgrAcIdpInfoApi.class;
	}

}
