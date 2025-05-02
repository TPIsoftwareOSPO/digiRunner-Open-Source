package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrOauthApprovals;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpApiId;
 
@Repository
public interface DgrOauthApprovalsDao extends JpaRepository<DgrOauthApprovals, Long> {
	
	public List<DgrOauthApprovals> findByCreateUser(String createUser);
	
	public List<DgrOauthApprovals> findByUserNameAndClientId(String userName, String clientId);
}
