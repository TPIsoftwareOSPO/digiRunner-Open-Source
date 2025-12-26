package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpRegHost;

@Repository
public interface TsmpRegHostDao extends JpaRepository<TsmpRegHost, String> {
	public List<TsmpRegHost> findByReghost(String regHost);

	public TsmpRegHost findByReghostId(String regHostId);
	
	public TsmpRegHost findFirstByReghostIdAndReghost(String regHostId, String regHost);
	
	public Long deleteByReghostIdAndReghost(String regHostId, String regHost);
	
	public List<TsmpRegHost> query_aa0806Service(String roleId, String enableHeartbeat, String status, boolean isNeedCheck, String[] words, String userName,Integer pageSize);
}
