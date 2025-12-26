package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DpUserInfoRDB;

public class DpUserInfoRDBDaoImpl extends SuperDaoImpl<DpUserInfoRDB> implements DpUserInfoRDBSuperDao {
	@Override
	public Class<DpUserInfoRDB> getEntityType() {
		return DpUserInfoRDB.class;
	}
}
