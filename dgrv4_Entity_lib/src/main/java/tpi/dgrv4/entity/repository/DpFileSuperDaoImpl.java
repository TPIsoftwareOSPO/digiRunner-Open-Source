package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DpFile;

public class DpFileSuperDaoImpl extends SuperDaoImpl<DpFile> implements DpFileSuperDao {

	@Override
	public Class<DpFile> getEntityType() {
		return DpFile.class;
	}
}

