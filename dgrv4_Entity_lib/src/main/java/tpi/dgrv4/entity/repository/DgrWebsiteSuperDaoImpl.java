package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrWebsite;

public class DgrWebsiteSuperDaoImpl extends SuperDaoImpl<DgrWebsite> implements DgrWebsiteSuperDao{

	@Override
	public Class<DgrWebsite> getEntityType() {
		return DgrWebsite.class;
	}

}
