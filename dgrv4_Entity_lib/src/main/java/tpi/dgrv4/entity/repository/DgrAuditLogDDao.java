package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrAuditLogD;

@Repository
public interface DgrAuditLogDDao extends JpaRepository<DgrAuditLogD, Long> {

	public List<DgrAuditLogD> findByTxnUid(String txnUid);

	public List<DgrAuditLogD> query_ProfileManagementReport(String entityName, List<String> eventNo, //
		Date st, Date et, DgrAuditLogD lastRecord, int pageSize);

	public List<DgrAuditLogD> findByEntityNameInAndCreateDateTimeBetween(
			List<String> entities, Date startDate, Date endDate);

	public List<DgrAuditLogD> findByTxnUidIn(List<String> txnUidList);

	public List<DgrAuditLogD> findByTxnUidOrderByAuditLongIdDesc(String txnUid);
	
	public Long deleteByTxnUid(String txnUid);

	public List<DgrAuditLogD> findByCreateDateTimeBefore(Date expDate);

}
