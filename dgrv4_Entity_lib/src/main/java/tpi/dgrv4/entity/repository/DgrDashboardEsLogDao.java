package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import tpi.dgrv4.entity.entity.jpql.DgrDashboardEsLog;

public interface DgrDashboardEsLogDao extends JpaRepository<DgrDashboardEsLog, String> {

	public List<DgrDashboardEsLog> queryByDashboard(Date startDate, Date endDate, String id, Integer pageSize);
	
	public List<DgrDashboardEsLog> queryByDashboard2(Date startDate, Date endDate, String id, Integer pageSize);
	
	public List<Map> queryByClientUsageMetrics();
	
	public List<Map> queryByBadAttempt();
	
	public List<Map> queryByMedian();
	
	public Page<DgrDashboardEsLog> findByExeStatus(String exeStatus, Pageable pageable);
	
	public List<Map> queryByApiTrafficDistribution();
	
	public Optional<DgrDashboardEsLog> findTopByOrderByRtimeDesc();
	
	public List<DgrDashboardEsLog> queryByMedian(Date startDate, Date endDate, Integer pageSize, Integer firstResult);
	
	public List<DgrDashboardEsLog> findTop20By();

	public List<DgrDashboardEsLog> findByRtimeLessThan(Date oneYearAgo);
}
