package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpVgroupGroup;
import tpi.dgrv4.entity.entity.TsmpVgroupGroupId;

@Repository
public interface TsmpVgroupGroupDao extends JpaRepository<TsmpVgroupGroup, TsmpVgroupGroupId> {

	public List<TsmpVgroupGroup> findByVgroupId(String vgroupId);
	
	public List<TsmpVgroupGroup> findByGroupId(String groupId);

	public long deleteByVgroupId(String vgroupId);
}
