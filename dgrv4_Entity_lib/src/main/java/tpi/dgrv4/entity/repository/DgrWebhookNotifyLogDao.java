package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrWebhookNotifyLog;

@Repository
public interface DgrWebhookNotifyLogDao extends JpaRepository<DgrWebhookNotifyLog, Long>, DgrWebhookNotifyLogSuperDao {
	
	public List<DgrWebhookNotifyLog> query_dpb0285Service(Date startDate, Date endDate, DgrWebhookNotifyLog lastRecord, 
			String[] words, Integer pageSize);
	
	List<DgrWebhookNotifyLog> findByCreateUser(String createUser);
	
	List<DgrWebhookNotifyLog> findByWebhookNotifyId(Long webhookNotifyId);
}
