package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.sql.TsmpDpSiteMap;

@Repository
public interface TsmpDpSiteMapDao extends JpaRepository<TsmpDpSiteMap, Long> {

	public List<TsmpDpSiteMap> findBySiteParentId(Long siteParentId, Sort sort);

	public List<TsmpDpSiteMap> findByCreateUser(String createUser);

}
