package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.SsoAuthResult;

@Repository
public interface SsoAuthResultDao extends JpaRepository<SsoAuthResult, Long> {

	public List<SsoAuthResult> findByCreateUser(String createUser);

	public List<SsoAuthResult> findByUserName(String userName);

	public List<SsoAuthResult> findByUserNameAndCodeChallengeAndCreateDateTimeAfter(String userName,
			String codeChallenge, Date createDateTime);

	public List<SsoAuthResult> findByUserNameAndCreateDateTimeAfterOrderByCreateDateTimeDesc(String userName, 
			Date createDateTime);
	
}