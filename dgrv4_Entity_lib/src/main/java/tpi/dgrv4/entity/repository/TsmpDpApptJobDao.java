package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpDpApptJob;

@Repository
public interface TsmpDpApptJobDao extends JpaRepository<TsmpDpApptJob, Long> {

	public TsmpDpApptJob findFirstByPeriodUidAndPeriodItemsIdAndPeriodNexttime(String periodUid, Long periodItemsId, Long periodNexttime);

	public List<TsmpDpApptJob> findByPeriodUidOrderByPeriodNexttimeAscPeriodItemsIdAsc(String periodUid);

	public List<TsmpDpApptJob> findByPeriodUidAndPeriodNexttime(String periodUid, Long periodNexttime);

	public TsmpDpApptJob findFirstByPeriodUidAndPeriodNexttimeGreaterThan(String periodUid, Long peruidNexttime);

	public List<TsmpDpApptJob> queryExecutableJobs(Date startDateTime);

	public List<TsmpDpApptJob> findByCreateUser(String createUser);
	
	public List<TsmpDpApptJob> queryByRefItemNoAndCreateDateTime(String refItemNo,String date);
	
	public List<TsmpDpApptJob> query_dpb0058Service(Date sdt, Date edt, String status, String[] words //
			, TsmpDpApptJob lastRecord, String locale, Integer pageSize);

	public List<TsmpDpApptJob> query_dpb0104Service_1(String apptRjobId, TsmpDpApptJob lastRecord, Integer pageSize);

	public List<TsmpDpApptJob> findByUpdateDateTimeBefore(Date updateDateTime);

	public List<TsmpDpApptJob> queryRunLoopJobByFileName(String refSubitemNo, String fileName, List<String> statusList);
	
	public void deleteByCreateUser(String createUser);

	public boolean existsByRefItemNoAndStatusAndApptJobIdNot(String refItemNo, String status, Long apptJobId);
	
	public List<TsmpDpApptJob> findByRefItemNoAndStatus(String refItemNo, String status);
	
	public boolean existsByApptJobIdAndStatus(Long apptJobId, String status);
}