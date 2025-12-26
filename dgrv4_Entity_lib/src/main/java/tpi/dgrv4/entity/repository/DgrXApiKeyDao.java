package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrGtwIdpInfoJdbc;
import tpi.dgrv4.entity.entity.DgrXApiKey;

@Repository
public interface DgrXApiKeyDao extends JpaRepository<DgrXApiKey, Long>, DgrXApiKeySuperDao {

	public List<DgrXApiKey> findByCreateUser(String createUser);
	
	public List<DgrXApiKey> findByClientIdOrderByCreateDateTimeDescApiKeyIdDesc(String clientId);

	public DgrXApiKey findFirstByApiKeyEn(String apiKeyEn);
	
	public List<DgrXApiKey> findByClientId(String clientId);

	public int deleteByApiKeyId(Long apiKeyId);

	
}
