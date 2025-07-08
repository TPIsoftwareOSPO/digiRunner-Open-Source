package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrWebhookApiMap;

public class DgrWebhookApiMapSuperDaoImpl extends SuperDaoImpl<DgrWebhookApiMap> implements DgrWebhookApiMapSuperDao{

	@Override
	public Class<DgrWebhookApiMap> getEntityType() {
		return DgrWebhookApiMap.class;
	}

}
