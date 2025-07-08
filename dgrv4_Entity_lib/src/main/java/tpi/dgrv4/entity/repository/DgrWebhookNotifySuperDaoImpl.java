package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrWebhookNotify;

public class DgrWebhookNotifySuperDaoImpl extends SuperDaoImpl<DgrWebhookNotify> implements DgrWebhookNotifySuperDao{

	@Override
	public Class<DgrWebhookNotify> getEntityType() {
		return DgrWebhookNotify.class;
	}

}
