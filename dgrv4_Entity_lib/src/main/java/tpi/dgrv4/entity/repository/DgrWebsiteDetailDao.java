package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrWebsiteDetail;

@Repository
public interface DgrWebsiteDetailDao extends JpaRepository<DgrWebsiteDetail, Long>, DgrWebsiteDetailSuperDao {

	List<DgrWebsiteDetail> findByDgrWebsiteId(Long dgrWebsiteId);

	List<DgrWebsiteDetail> findByDgrWebsiteIdAndKeyword(Long dgrWebsiteId, String[] words, Integer pageSize);

	void deleteByDgrWebsiteId(Long dgrWebsiteId);

	void deleteByDgrWebsiteIdNotIn(List<Long> wsIdList);
}
