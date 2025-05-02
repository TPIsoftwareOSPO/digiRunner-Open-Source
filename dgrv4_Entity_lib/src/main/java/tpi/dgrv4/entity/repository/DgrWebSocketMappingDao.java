package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrWebSocketMapping;

@Repository
public interface DgrWebSocketMappingDao extends JpaRepository<DgrWebSocketMapping, Long>, DgrWebSocketMappingSuperDao {

	public List<DgrWebSocketMapping> findByCreateUser(String createUser);
	
	public DgrWebSocketMapping findFirstBySiteName(String siteName);
	
	public List<DgrWebSocketMapping> queryDPB0174(Long lastId, String[] keywords, Integer pageSize);
	
	public void deleteBySiteNameNotIn(List<String> siteNameList);
	
}
