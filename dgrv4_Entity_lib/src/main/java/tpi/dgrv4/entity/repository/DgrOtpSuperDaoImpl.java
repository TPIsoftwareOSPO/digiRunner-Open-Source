package tpi.dgrv4.entity.repository;

import tpi.dgrv4.entity.entity.DgrOtp;

public class DgrOtpSuperDaoImpl extends SuperDaoImpl<DgrOtp> implements DgrOtpSuperDao {

	@Override
	public Class<DgrOtp> getEntityType() {
		return DgrOtp.class;
	}
}
