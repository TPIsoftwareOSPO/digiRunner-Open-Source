package tpi.dgrv4.entity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrWebhookNotify;

@Repository
public interface DgrWebhookNotifyDao extends JpaRepository<DgrWebhookNotify, Long>, DgrWebhookNotifySuperDao {

	List<DgrWebhookNotify> findByDgrWebhookNotifyIdAndKeyword(Long dgrWebhookNotifyId, String enable, String[] words,
			Integer pageSize);
	
	Optional<DgrWebhookNotify> findFirstByNotifyName(String notifyName);
	
	List<DgrWebhookNotify> findByCreateUser(String createUser);
}
