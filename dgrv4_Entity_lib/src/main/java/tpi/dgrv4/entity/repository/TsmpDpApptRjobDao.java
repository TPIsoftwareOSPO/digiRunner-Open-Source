package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpDpApptRjob;

@Repository
@Transactional //因為JPQL原先的設計只能查詢,所以不需要設定 @Transactional,但JPA 1.7以後的版本支持Query命名的方法改為 delete/remove/count, 就需要設定@Transactional
public interface TsmpDpApptRjobDao extends JpaRepository<TsmpDpApptRjob, String> {

	public List<TsmpDpApptRjob> queryAvailableRjobs();

	@Transactional
	@Modifying(clearAutomatically = true)
	@Query("update TsmpDpApptRjob R set R.nextDateTime = :newNextDateTime, R.lastDateTime = :lastDateTime, R.updateDateTime = :updateDateTime, R.updateUser = :updateUser where 1 = 1 and R.apptRjobId = :apptRjobId and R.nextDateTime = :oldNextDateTime")
	public int update_apptRjobDispatcher_01(
		@Param("newNextDateTime") Long newNextDateTime,
		@Param("lastDateTime") Long lastDateTime,
		@Param("updateDateTime") Date updateDateTime,
		@Param("updateUser") String updateUser,
		@Param("apptRjobId") String apptRjobId,
		@Param("oldNextDateTime") Long oldNextDateTime
	);

	public List<TsmpDpApptRjob> query_dpb0102Service_1(String status, TsmpDpApptRjob lastRecord, String[] keywords, String locale, Integer pageSize);

	public List<TsmpDpApptRjob> findByRemark(String remark);

	public List<TsmpDpApptRjob> findByCreateUser(String createUser);
	
	public List<TsmpDpApptRjob> findByRjobName(String rjobName);
	
}