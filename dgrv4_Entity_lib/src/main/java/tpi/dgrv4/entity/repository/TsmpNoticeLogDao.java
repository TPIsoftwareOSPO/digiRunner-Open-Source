package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpNoticeLog;

@Repository
public interface TsmpNoticeLogDao extends JpaRepository<TsmpNoticeLog, Long> {

	// HouseKeeping 用
	public List<TsmpNoticeLog> findByLastNoticeDateTimeLessThan(Date LastNoticeDateTime);

	public List<TsmpNoticeLog> findByNoticeSrcAndNoticeKey(String noticeSrc, String noticeKey);

	public List<TsmpNoticeLog> findByNoticeSrcAndNoticeMthdAndNoticeKeyOrderByLastNoticeDateTime(String noticeSrc, String noticeMthd, String noticeKey);

	public List<TsmpNoticeLog> query_noticeExpCertJob_01(String noticeSrc, String noticeMthd, String noticeKey, //
		Date lastNoticeDateTime);
	
}