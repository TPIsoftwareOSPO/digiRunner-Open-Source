package tpi.dgrv4.entity.repository;


import java.util.Date;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpAuthCode;

@Repository
@Transactional //因為JPQL原先的設計只能查詢,所以不需要設定 @Transactional,但JPA 1.7以後的版本支持Query命名的方法改為 delete/remove/count, 就需要設定@Transactional
public interface TsmpAuthCodeDao extends JpaRepository<TsmpAuthCode, Long> {

	// AUTH_CODE 欄位為 UNIQUE
	public TsmpAuthCode findFirstByAuthCode(String authCode);

	public List<TsmpAuthCode> findByExpireDateTimeLessThan(Long expireDateTime);
	
	public List<TsmpAuthCode> findByStatusAndUpdateDateTimeLessThan(String status, Date updateDateTime);

	// RefreshAuthCodeJob: 找出已失效，但狀態尚未更新的授權碼
	public List<TsmpAuthCode> findByExpireDateTimeLessThanAndStatusNot(Long expireDateTime, String status);

	public List<TsmpAuthCode> findByCreateUser(String createUser);
	
}