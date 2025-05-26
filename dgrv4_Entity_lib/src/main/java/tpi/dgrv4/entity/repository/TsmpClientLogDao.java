package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpClientLog;

@Repository
public interface TsmpClientLogDao extends JpaRepository<TsmpClientLog, String> {
	
	public List<TsmpClientLog> findByLogSeqAndEventTypeAndStartTimeAndEndTimeAndKeyword(TsmpClientLog tsmpClientLog, String eventType, String queryStartTime,String queryEndTime,String[] keyword, int pageSize);
	public List<TsmpClientLog> findByClientId(String clientId);
	public List<TsmpClientLog> findByCreateTimeBefore(Date createTime);
}
