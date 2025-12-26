package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrWebsite;

@Repository
public interface DgrWebsiteDao extends JpaRepository<DgrWebsite, Long>, DgrWebsiteSuperDao {

	List<DgrWebsite> findByWebsiteName(String websiteName);
	
	List<DgrWebsite> findByDgrWebsiteIdNotAndWebsiteName(Long dgrWebsiteId,String websiteName);
	
	List<DgrWebsite> findByDgrWebsiteIdAndWebsiteStatus(Long dgrWebsiteId, String websiteStatus);

	List<DgrWebsite> findByWebsiteNameAndWebsiteStatus(String websiteName, String websiteStatus);

	List<DgrWebsite> findByWebsiteStatus(String websiteStatus);
	
	List<DgrWebsite> findByDgrWebsiteIdAndKeyword(Long dgrWebsiteId, String websiteStatus, String[] words,
			Integer pageSize);
	
	List<DgrWebsite> findAllById(Iterable<Long> ids);
	
	DgrWebsite findFirstByWebsiteName(String websiteName);

	void deleteByWebsiteNameNotIn(List<String> websiteName);
}
