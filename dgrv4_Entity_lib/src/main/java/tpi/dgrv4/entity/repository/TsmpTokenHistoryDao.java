package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpTokenHistory;

@Repository
public interface TsmpTokenHistoryDao extends JpaRepository<TsmpTokenHistory, Long> {
	
	public TsmpTokenHistory findFirstByTokenJti(String tokenJti);
	
	public TsmpTokenHistory findFirstByTokenJtiAndIdpType(String tokenJti, String idpType);
	
	public TsmpTokenHistory findFirstByTokenJtiAndRetokenJti(String tokenJti, String retokenJti);

	public TsmpTokenHistory findFirstByTokenJtiAndRetokenJtiAndIdpType(String tokenJti, String retokenJti,
			String idpType);

	public TsmpTokenHistory findFirstByRetokenJtiOrderByCreateAtDesc(String retokenJti);
	
	public TsmpTokenHistory findFirstByIdTokenJwtstr(String idTokenJwtstr);
	
	public List<TsmpTokenHistory> findByClientId(String clientId);
	
	public List<TsmpTokenHistory> findByRetokenJti(String retokenJti);
	
	public List<TsmpTokenHistory> findByClientIdAndUserName(String clientId, String userName);
	
	public List<TsmpTokenHistory> findByClientIdAndUserNameOrderBySeqNo(String clientId, String userName);
	
	public List<TsmpTokenHistory> findByReexpiredAtBefore(Date expDate);
	
	public List<TsmpTokenHistory> findByUserName(String userName);
	
	public List<TsmpTokenHistory> findByReexpiredAtAfterOrExpiredAtAfter(Date expDate,Date expDate2);

	public void deleteByTokenJti(String tokenJti);
}
