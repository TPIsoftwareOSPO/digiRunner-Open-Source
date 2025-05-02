package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrGtwIdpInfoA;
 
@Repository
public interface DgrGtwIdpInfoADao extends JpaRepository<DgrGtwIdpInfoA, Long>, DgrGtwIdpInfoASuperDao {

	public List<DgrGtwIdpInfoA> findByCreateUser(String CreateUser);
 
	public DgrGtwIdpInfoA findFirstByClientIdAndStatusOrderByCreateDateTimeDesc(String clientId, String status);
	
	public List<DgrGtwIdpInfoA> findByClientIdOrderByCreateDateTimeDescGtwIdpInfoAIdDesc(String clientId);
	
	int deleteByClientId(String clientId);
	
	public List<DgrGtwIdpInfoA> findByClientId(String clientId);
}
