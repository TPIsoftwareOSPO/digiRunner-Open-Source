package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrGtwIdpAuthCode;
import tpi.dgrv4.entity.entity.DgrGtwIdpAuthM;

@Repository
public interface DgrGtwIdpAuthMDao extends JpaRepository<DgrGtwIdpAuthM, Long> {
	
	public List<DgrGtwIdpAuthM> findByCreateUser(String createUser);
	
	public DgrGtwIdpAuthM findFirstByAuthCodeAndClientId(String authCode, String clientId);
	
	// STATE 欄位為 UNIQUE
	public DgrGtwIdpAuthM findFirstByState(String state);
}
