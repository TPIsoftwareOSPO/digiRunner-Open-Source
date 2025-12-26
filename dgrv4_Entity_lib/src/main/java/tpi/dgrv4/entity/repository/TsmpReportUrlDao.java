package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpReportUrl;
import tpi.dgrv4.entity.entity.jpql.TsmpReportUrlId;

@Repository
public interface TsmpReportUrlDao extends JpaRepository<TsmpReportUrl, TsmpReportUrlId> {
	
	public TsmpReportUrl findByTimeRangeAndReportId(String timeRange, String reportId);

	public boolean existsByReportId(String funcCode);

	public List<TsmpReportUrl> findByReportId(String funcCode);
	
	public List<TsmpReportUrl> findByReportIdStartsWith(String funcCode);
	
	public List<TsmpReportUrl> queryAllByReportId(List<String> reportIdList);

}
