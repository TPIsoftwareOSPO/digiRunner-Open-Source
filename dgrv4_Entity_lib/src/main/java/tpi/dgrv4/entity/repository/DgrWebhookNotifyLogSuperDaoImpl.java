package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrWebhookNotifyLog;

public class DgrWebhookNotifyLogSuperDaoImpl extends SuperDaoImpl<DgrWebhookNotifyLog> implements DgrWebhookNotifyLogSuperDao{

	@Override
	public Class<DgrWebhookNotifyLog> getEntityType() {
		return DgrWebhookNotifyLog.class;
	}

}
