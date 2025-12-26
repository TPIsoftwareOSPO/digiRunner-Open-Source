package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrWebSocketMapping;

public class DgrWebSocketMappingSuperDaoImpl extends SuperDaoImpl<DgrWebSocketMapping> implements DgrWebSocketMappingSuperDao {

	@Override
	public Class<DgrWebSocketMapping> getEntityType() {
		return DgrWebSocketMapping.class;
	}
}
