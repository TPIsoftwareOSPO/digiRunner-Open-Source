package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpReqLog;

@Repository
public interface TsmpReqLogDao extends JpaRepository<TsmpReqLog, String> {
	
	List<TsmpReqLog> findByRtimeLessThanOrderByRtimeAsc(Date rtime);
	List<TsmpReqLog> findByRtimeLessThanOrderByRtimeDesc(Date rtime);
}
