package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpVgroupAuthoritiesMap;
import tpi.dgrv4.entity.entity.TsmpVgroupAuthoritiesMapId;


@Repository
public interface TsmpVgroupAuthoritiesMapDao extends JpaRepository<TsmpVgroupAuthoritiesMap, TsmpVgroupAuthoritiesMapId> {
	
	public List<TsmpVgroupAuthoritiesMap> findByVgroupId(String vgroupId);
	
}