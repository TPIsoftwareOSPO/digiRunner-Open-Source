package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrWebhookApiMap;
import tpi.dgrv4.entity.entity.DgrWebhookNotifyField;

@Repository
public interface DgrWebhookApiMapDao extends JpaRepository<DgrWebhookApiMap, Long>, DgrWebhookApiMapSuperDao {
	
	List<DgrWebhookApiMap> findByApiKeyAndModuleName(String apiKey, String moduleName);
	
	List<DgrWebhookApiMap> findByWebhookNotifyId(Long webhookNotifyId);
	
	List<DgrWebhookApiMap> findByCreateUser(String createUser);
}
