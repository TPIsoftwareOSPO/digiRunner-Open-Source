package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpUser;

@Repository
public interface TsmpUserDao extends JpaRepository<TsmpUser, String> {

	public List<TsmpUser> findByCreateUser(String createUser);
	
	public TsmpUser findFirstByUserName(String userName);

	public Page<TsmpUser> findByUserStatusIn(List<String> userStatusList, Pageable pageable);
	
	public List<TsmpUser> findByLogonDateBefore(Date logonDate);
	
	public List<TsmpUser> queryByReviewTypeAndLayer(String reviewType, Integer layer);
	
	public List<TsmpUser> query_dpb0039Service(String userStatus, String[] words, String lastId, Integer pageSize);

	public List<TsmpUser> query_aa0019Service(String roleName, List<String> orgIdListFromOrgName, List<String> orgDescList, String[] words, 
			String lastId, Integer pageSize);
	
	public TsmpUser findByUserIdAndUserName(String userId,String userName);
	
	public List<TsmpUser> findByOrgId(String orgId);

	public List<TsmpUser> queryByRoleAlert(Long alertId);
	
	public Long countByUserStatus(String userStatus);

}
