package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrXApiKeyMap;

public class DgrXApiKeyMapSuperDaoImpl extends SuperDaoImpl<DgrXApiKeyMap> implements DgrXApiKeyMapSuperDao {

	@Override
	public Class<DgrXApiKeyMap> getEntityType() {
		return DgrXApiKeyMap.class;
	}
}
