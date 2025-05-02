package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpHeartbeat;
import tpi.dgrv4.entity.entity.jpql.TsmpNode;

@Repository
public interface TsmpHeartbeatDao extends JpaRepository<TsmpHeartbeat, String> {
	
	public List<TsmpHeartbeat> findByUpdateTimeAfter(Date updateTime);
}
