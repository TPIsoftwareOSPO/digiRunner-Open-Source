package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpClientCert;

@Repository
@Transactional //因為JPQL原先的設計只能查詢,所以不需要設定 @Transactional,但JPA 1.7以後的版本支持Query命名的方法改為 delete/remove/count, 就需要設定@Transactional
public interface TsmpClientCertDao extends JpaRepository<TsmpClientCert, Long> {

	public List<TsmpClientCert> query_dpb0088Service(Date startDate, Date endDate, //
			TsmpClientCert lastRecord, Integer pageSize);
	
	public List<TsmpClientCert> findByCreateUser(String createUser);
	
	public List<TsmpClientCert> findByClientId(String clientId);
	
	public List<TsmpClientCert> findByClientId(String clientId, Sort sort);
	
	public List<TsmpClientCert> findByClientIdAndClientCertId(String clientId, Long clientCertId);
	
	public long deleteByClientIdAndClientCertId(String clientId, Long clientCertId);
	
	public List<TsmpClientCert> findByClientIdAndExpiredAtAfterOrderByCreateDateTime(String clientId, Long expiredAt);

	public void deleteByClientIdAndCertFileName(String clientId, String certFileName);
}

 
