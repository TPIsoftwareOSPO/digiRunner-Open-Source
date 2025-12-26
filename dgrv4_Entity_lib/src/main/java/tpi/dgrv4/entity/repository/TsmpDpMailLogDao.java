package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailLog;

@Repository
public interface TsmpDpMailLogDao extends JpaRepository<TsmpDpMailLog, Long> {
	
	public List<TsmpDpMailLog> queryExpiredMail(Date expDate);
	
	public List<TsmpDpMailLog> findByCreateUser(String createUser);
	
	public List<TsmpDpMailLog> findByMaillogIdAndRecipients(Long maillogId, String recipients);
	
	public List<TsmpDpMailLog> queryMailLogList(String startDate, String endDate, String result, Long id,
			String keyword, Integer pageSize);

}
