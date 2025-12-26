package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrAcIdpInfoMLdapM;

@Repository
public interface DgrAcIdpInfoMLdapMDao extends JpaRepository<DgrAcIdpInfoMLdapM, Long>, DgrAcIdpInfoMLdapMSuperDao {

	public List<DgrAcIdpInfoMLdapM> findByCreateUser(String createUser);
	
	public DgrAcIdpInfoMLdapM findFirstByStatusOrderByCreateDateTimeDesc(String ldapStatus); 
	
	public List<DgrAcIdpInfoMLdapM> findAllByOrderByCreateDateTimeDescAcIdpInfoMLdapMIdDesc();
}
