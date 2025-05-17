package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrGtwIdpAuthCode;

@Repository
public interface DgrGtwIdpAuthCodeDao extends JpaRepository<DgrGtwIdpAuthCode, Long> {

	public List<DgrGtwIdpAuthCode> findByCreateUser(String createUser);

	// AUTH_CODE 欄位為 UNIQUE
	public DgrGtwIdpAuthCode findFirstByAuthCode(String authCode);

	// AUTH_CODE 欄位為 UNIQUE
	public DgrGtwIdpAuthCode findFirstByAuthCodeAndPhase(String authCode, String phase);
}
