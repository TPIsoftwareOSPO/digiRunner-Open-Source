package tpi.dgrv4.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import tpi.dgrv4.entity.entity.DgrOtp;

public interface DgrOtpDao extends JpaRepository<DgrOtp, Long>, DgrOtpSuperDao {
	public long deleteByEmail(String email);
	public DgrOtp findFirstByEmail(String email);
   
}
