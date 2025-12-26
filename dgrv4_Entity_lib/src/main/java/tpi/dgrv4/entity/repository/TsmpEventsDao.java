package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpEvents;
	
@Repository
@Transactional // 因為JPQL原先的設計只能查詢,所以不需要設定 @Transactional,但JPA 1.7以後的版本支持Query命名的方法改為delete/remove/count, 就需要設定@Transactional
public interface TsmpEventsDao extends JpaRepository<TsmpEvents, Long> {

	public List<TsmpEvents> findByEventIdAndStartDateAndEndDateAndKeyword(TsmpEvents lastTsmpEvents, String startDate,String endDate,String[] keyword, String locale, int pageSize);
	
	public List<TsmpEvents> findByCreateUser(String createUser);
	
	public List<TsmpEvents> findByKeepFlagAndCreateDateTimeBefore(String keepFlag, Date createDateTime);
}
