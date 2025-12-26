package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.DgrDashboardLastData;

@Repository
public interface DgrDashboardLastDataDao extends JpaRepository<DgrDashboardLastData, Long> {


	public List<DgrDashboardLastData> findByTimeTypeAndDashboardType(int timeType, int dashboardType);
	
	public List<DgrDashboardLastData> findByTimeTypeAndDashboardTypeOrderBySortNum(int timeType, int dashboardType);

	public List<DgrDashboardLastData> findByTimeType(int timeType);
	
	public long deleteByTimeType(int timeType);

}
