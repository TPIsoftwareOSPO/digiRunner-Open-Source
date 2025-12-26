package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpClientHost;

@Repository
public interface TsmpClientHostDao extends JpaRepository<TsmpClientHost, Long> {
	
	public List<TsmpClientHost> findByClientId(String clientId);
	
	public long deleteByClientId(String clientId);
}
