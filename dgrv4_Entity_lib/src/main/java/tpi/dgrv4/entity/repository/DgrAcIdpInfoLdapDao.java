package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrAcIdpInfoLdap;

@Repository
public interface DgrAcIdpInfoLdapDao extends JpaRepository<DgrAcIdpInfoLdap, Long>, DgrAcIdpInfoLdapSuperDao {

	public List<DgrAcIdpInfoLdap> findByCreateUser(String createUser);
	
	public DgrAcIdpInfoLdap findFirstByLdapStatusOrderByCreateDateTimeDesc(String ldapStatus);
	
	public List<DgrAcIdpInfoLdap> findAllByOrderByCreateDateTimeDescAcIdpInfoLdapIdDesc();
 
}
