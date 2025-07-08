package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrWebhookNotify;
import tpi.dgrv4.entity.entity.DgrWebhookNotifyField;

@Repository
public interface DgrWebhookNotifyFieldDao extends JpaRepository<DgrWebhookNotifyField, Long>, DgrWebhookNotifyFieldSuperDao {

	List<DgrWebhookNotifyField> findByWebhookNotifyId(Long webhookNotifyId);
	
	List<DgrWebhookNotifyField> findByCreateUser(String createUser);
}
