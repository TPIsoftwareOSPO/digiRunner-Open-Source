package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrWebsiteDetail;

public class DgrWebsiteDetailSuperDaoImpl extends SuperDaoImpl<DgrWebsiteDetail> implements DgrWebsiteDetailSuperDao{

	@Override
	public Class<DgrWebsiteDetail> getEntityType() {
		return DgrWebsiteDetail.class;
	}

}
