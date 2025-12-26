package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpReqResLogHistory;

@Repository
public interface TsmpReqResLogHistoryDao extends JpaRepository<TsmpReqResLogHistory, String>{
	
	public List<TsmpReqResLogHistory> queryByDashboard(Date startDate, Date endDate, String id, Integer pageSize);
	
	public List<TsmpReqResLogHistory> queryByDashboard2(Date startDate, Date endDate, String id, Integer pageSize);

	List<TsmpReqResLogHistory> findByRtimeLessThan(Date oneYearAgo);
	
	public List<Map> queryByClientUsageMetrics();
	
	public List<Map> queryByBadAttempt();
	
	public List<Map> queryByMedian();
	
	public Page<TsmpReqResLogHistory> findByExeStatus(String exeStatus, Pageable pageable);
	
	public List<Map> queryByApiTrafficDistribution();
	
	public List<TsmpReqResLogHistory> queryByMedian(Date startDate, Date endDate, Integer pageSize, Integer firstResult);
	
	public List<TsmpReqResLogHistory> findTop20By();
	

}
