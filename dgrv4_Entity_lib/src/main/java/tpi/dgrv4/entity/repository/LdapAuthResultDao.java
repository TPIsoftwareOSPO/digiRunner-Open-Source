package tpi.dgrv4.entity.repository;


import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.LdapAuthResult;

@Repository
public interface LdapAuthResultDao extends JpaRepository<LdapAuthResult, Long> {
	
	public List<LdapAuthResult> findByCreateUser(String createUser);
	
	public List<LdapAuthResult> findByUserNameAndCodeChallengeAndCreateDateTimeAfter(String userName,
			String codeChallenge, Date createDateTime);
	
	public List<LdapAuthResult> findByUserNameAndCodeChallengeAndUserIpAndCreateDateTimeAfter(String userName,
			String codeChallenge, String userIp, Date createDateTime);
	
	public List<LdapAuthResult> findByUserNameAndCreateDateTimeAfterOrderByCreateDateTimeDesc(String userName, Date createDateTime);
	
	public List<LdapAuthResult> findByUserNameAndUserIpAndCreateDateTimeAfterOrderByCreateDateTimeDesc(String userName, String userIp, 
			Date queryStartDate);
}