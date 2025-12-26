package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrXApiKey;

public class DgrXApiKeySuperDaoImpl extends SuperDaoImpl<DgrXApiKey> implements DgrXApiKeySuperDao {

	@Override
	public Class<DgrXApiKey> getEntityType() {
		return DgrXApiKey.class;
	}
}
