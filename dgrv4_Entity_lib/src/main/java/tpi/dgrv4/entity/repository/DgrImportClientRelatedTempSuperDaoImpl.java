package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrImportClientRelatedTemp;

public class DgrImportClientRelatedTempSuperDaoImpl extends SuperDaoImpl<DgrImportClientRelatedTemp> implements DgrImportClientRelatedTempSuperDao {

	@Override
	public Class<DgrImportClientRelatedTemp> getEntityType() {
		return DgrImportClientRelatedTemp.class;
	}
}
