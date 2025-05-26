package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrGtwIdpInfoL;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoO;

@Repository
public interface DgrGtwIdpInfoLDao extends JpaRepository<DgrGtwIdpInfoL, Long>, DgrGtwIdpInfoLSuperDao {

	public List<DgrGtwIdpInfoL> findByCreateUser(String CreateUser);

	public DgrGtwIdpInfoL findFirstByClientIdAndStatusOrderByCreateDateTimeDesc(String clientId,
			String status);
	
	public List<DgrGtwIdpInfoL> findByClientIdOrderByCreateDateTimeDescGtwIdpInfoLIdDesc(String clientId);

	public List<DgrGtwIdpInfoL> findByClientId(String clientId);
	
	public long deleteByClientId(String clientId);
}
