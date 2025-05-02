package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import tpi.dgrv4.entity.entity.TsmpGroupAuthoritiesMap;
import tpi.dgrv4.entity.entity.TsmpGroupAuthoritiesMapId;

public interface TsmpGroupAuthoritiesMapDao extends JpaRepository<TsmpGroupAuthoritiesMap, TsmpGroupAuthoritiesMapId> {

	public List<TsmpGroupAuthoritiesMap> findByGroupId(String groupId);
	public List<TsmpGroupAuthoritiesMap> findByGroupAuthoritieId(String groupAuthoritieId);
	public long deleteByGroupId(String groupId);
}
