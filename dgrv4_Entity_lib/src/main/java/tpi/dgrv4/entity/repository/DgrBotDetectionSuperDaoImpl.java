package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrBotDetection;

public class DgrBotDetectionSuperDaoImpl extends SuperDaoImpl<DgrBotDetection> implements DgrBotDetectionSuperDao {

	@Override
	public Class<DgrBotDetection> getEntityType() {
		return DgrBotDetection.class;
	}

}
