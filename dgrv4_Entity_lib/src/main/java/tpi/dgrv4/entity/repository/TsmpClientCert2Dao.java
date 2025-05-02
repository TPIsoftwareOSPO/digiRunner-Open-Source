package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpClientCert2;

@Repository
@Transactional //因為JPQL原先的設計只能查詢,所以不需要設定 @Transactional,但JPA 1.7以後的版本支持Query命名的方法改為 delete/remove/count, 就需要設定@Transactional
public interface TsmpClientCert2Dao extends JpaRepository<TsmpClientCert2, Long> {

	public List<TsmpClientCert2> query_dpb0088Service(Date startDate, Date endDate, //
			TsmpClientCert2 lastRecord, Integer pageSize);
	
	public List<TsmpClientCert2> findByCreateUser(String createUser);
	
	public List<TsmpClientCert2> findByClientId(String clientId);
	
	public List<TsmpClientCert2> findByClientId(String clientId, Sort sort);
	
	public List<TsmpClientCert2> findByClientIdAndClientCert2Id(String clientId, Long clientCert2Id);
	
	public long deleteByClientIdAndClientCert2Id(String clientId, Long clientCert2Id);
	
}

 
