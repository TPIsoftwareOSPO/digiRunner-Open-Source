package tpi.dgrv4.entity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrAcIdpAuthCode;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;

@Repository
public interface DgrAcIdpUserDao extends JpaRepository<DgrAcIdpUser, Long>, DgrAcIdpUserSuperDao {
	
	public List<DgrAcIdpUser> findByCreateUser(String createUser);
	
    public List<DgrAcIdpUser> findByUserEmail(String userEmail);

    public DgrAcIdpUser findByAcIdpUserIdAndUserNameAndIdpType(Long acIdpUserId, String userEmail, String idpType);
    
	// user_name + idp_type 欄位為 UNIQUE
    public DgrAcIdpUser findFirstByUserNameAndIdpType(String userName, String idpType);

	public List<DgrAcIdpUser> findByUserName(String userName);

	public  Optional<DgrAcIdpUser> findFirstByUserName(String createUser);
	
	public List<DgrAcIdpUser> queryAllByOrgList(List<String> orgDescList);
	
	public DgrAcIdpUser findFirstByUserNameOrderByCreateDateTime(String userName);
	
}
