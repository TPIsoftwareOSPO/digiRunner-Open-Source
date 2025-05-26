package tpi.dgrv4.entity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrGtwIdpInfoO;

@Repository
public interface DgrGtwIdpInfoODao extends JpaRepository<DgrGtwIdpInfoO, Long>, DgrGtwIdpInfoOSuperDao {

	public List<DgrGtwIdpInfoO> findByCreateUser(String createUser);

	public DgrGtwIdpInfoO findFirstByClientIdAndIdpTypeAndStatusOrderByCreateDateTimeDesc(String clientId,
			String idpType, String status);

	public List<DgrGtwIdpInfoO> findByClientIdOrderByCreateDateTimeDescGtwIdpInfoOIdDesc(String clientId);

	public List<DgrGtwIdpInfoO> findByClientId(String clientId);
	
	public long deleteByClientId(String clientId);
	
	public List<DgrGtwIdpInfoO> findByClientIdAndIdpTypeIn(String clientId, List<String> idpTypeList);
}
