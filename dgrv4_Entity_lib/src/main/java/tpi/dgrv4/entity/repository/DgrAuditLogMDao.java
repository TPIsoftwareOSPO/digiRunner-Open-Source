package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrAuditLogM;
import tpi.dgrv4.entity.entity.DgrAuditLogMId;

@Repository
public interface DgrAuditLogMDao extends JpaRepository<DgrAuditLogM, DgrAuditLogMId> {

	public DgrAuditLogM findByTxnUid(String txnUid);

	public Page<DgrAuditLogM> findByEventNoAndCreateDateTimeGreaterThanEqualAndCreateDateTimeLessThanOrderByCreateDateTime(String eventNo, Date st, Date et, Pageable pageable);

	public DgrAuditLogM findFirstByUserNameAndCreateDateTimeLessThanAndEventNoAndParam1OrderByCreateDateTimeDesc(
		String userName, Date createDateTime, String eventNo, String param1
	);

	public List<DgrAuditLogM> query_LoginLogoutReport(Date st, Date et, DgrAuditLogM lastRecord, int pageSize);

	public List<DgrAuditLogM> query_IDManagementReport(Date st, Date et, DgrAuditLogM lastRecord, int pageSize);

	public DgrAuditLogM findFirstByUserNameOrderByCreateDateTimeDesc(String userName);

	public List<DgrAuditLogM> findByUserNameAndEventNoOrderByCreateDateTimeDesc(String userName, String eventNo, //
		Pageable pageable);

	public List<DgrAuditLogM> findByCreateDateTimeBetweenAndEventNoAndParam1OrderByCreateDateTime(Date startDate,
			Date endDate, String eventNo, String param1);

	public List<DgrAuditLogM> findByCreateDateTimeBetweenAndEventNoInAndParam1OrderByCreateDateTime(Date startDate,
			Date endDate, List<String> eventNo, String param1);

	public List<DgrAuditLogM> queryByAuditLogIdAndStartDateAndEndDateAndKeyword(DgrAuditLogM lastDgrAuditLogM,
			String startDate, String endDate, String[] keyword, String locale, int pageSize);

	public Long countByEventNoAndCreateDateTimeBetween(String eventNo, Date start, Date end);
	
	public Long countByEventNoAndParam1AndCreateDateTimeBetween(String eventNo, String param, Date start, Date end);
	
	public Long deleteByTxnUid(String txnUid);
	
	public List<DgrAuditLogM> findByUserNameAndEventNoOrderByCreateDateTimeDesc(String userName, String eventNo);
	
	public List<DgrAuditLogM> findTop3ByUserNameAndEventNoOrderByCreateDateTimeDesc(String userName, String eventNo);

	public List<DgrAuditLogM> findByCreateDateTimeBefore(Date expDate);
}
