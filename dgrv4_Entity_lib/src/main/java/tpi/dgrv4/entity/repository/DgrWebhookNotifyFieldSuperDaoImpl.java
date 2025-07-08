package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrWebhookNotifyField;

public class DgrWebhookNotifyFieldSuperDaoImpl extends SuperDaoImpl<DgrWebhookNotifyField> implements DgrWebhookNotifyFieldSuperDao{

	@Override
	public Class<DgrWebhookNotifyField> getEntityType() {
		return DgrWebhookNotifyField.class;
	}

}
