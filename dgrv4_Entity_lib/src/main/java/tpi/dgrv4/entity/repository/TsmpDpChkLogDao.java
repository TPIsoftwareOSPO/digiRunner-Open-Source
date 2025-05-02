package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpDpChkLog;

@Repository
public interface TsmpDpChkLogDao extends JpaRepository<TsmpDpChkLog, Long> {
	
	public List<TsmpDpChkLog> queryHistoryByPk(TsmpDpChkLog lastRecord, Long reqOrdermId, int pageSize);
	
	public List<TsmpDpChkLog> findByReqOrdermIdOrderByCreateDateTime(Long reqOrdermId);

	public List<TsmpDpChkLog> findByReqOrdermIdAndCreateUser(Long reqOrdermId, String userName);

	public List<TsmpDpChkLog> findByCreateUser(String createUser);

}
