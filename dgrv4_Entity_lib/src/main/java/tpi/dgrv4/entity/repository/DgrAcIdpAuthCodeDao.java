package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrAcIdpAuthCode;

@Repository
public interface DgrAcIdpAuthCodeDao extends JpaRepository<DgrAcIdpAuthCode, Long> {
	
	// AUTH_CODE 欄位為 UNIQUE
	public DgrAcIdpAuthCode findFirstByAuthCode(String authCode);
	
	public List<DgrAcIdpAuthCode> findByCreateUser(String createUser);
	
	public List<DgrAcIdpAuthCode> findByExpireDateTimeBefore(long expireDateTime);
}
