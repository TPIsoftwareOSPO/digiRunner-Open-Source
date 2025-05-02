package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrGtwIdpInfoJdbc;

@Repository
public interface DgrGtwIdpInfoJdbcDao extends JpaRepository<DgrGtwIdpInfoJdbc, Long>, DgrGtwIdpInfoJdbcSuperDao {

	public List<DgrGtwIdpInfoJdbc> findByCreateUser(String CreateUser);
 
	public DgrGtwIdpInfoJdbc findFirstByClientIdAndStatusOrderByCreateDateTimeDesc(String clientId, String status);
	
	public List<DgrGtwIdpInfoJdbc> findByClientIdOrderByCreateDateTimeDescGtwIdpInfoJdbcIdDesc(String clientId);

	int deleteByClientId(String clientId);
	
	public List<DgrGtwIdpInfoJdbc> findByClientId(String clientId);

}
