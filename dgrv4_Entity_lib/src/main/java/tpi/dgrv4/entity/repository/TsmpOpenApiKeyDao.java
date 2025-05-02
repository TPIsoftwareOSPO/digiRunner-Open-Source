package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpOpenApiKey;

@Repository
public interface TsmpOpenApiKeyDao extends JpaRepository<TsmpOpenApiKey, Long> {

	public int deleteByOpenApiKeyId(Long id);
	
	public TsmpOpenApiKey findFirstByOpenApiKey(String openApiKey);

	public List<TsmpOpenApiKey> findByCreateUser(String createUser);
	
	public List<TsmpOpenApiKey> findByClientId(String clientId);

	public List<TsmpOpenApiKey> queryExpiringOpenApiKey(Long todayLong, Long expDateLong);
	
	public List<TsmpOpenApiKey> queryExpiredOpenApiKey(Long expDateLong);
	
	//DPB0090, DPF0047
	public List<TsmpOpenApiKey> queryOpenApiKeyByClientId(TsmpOpenApiKey lastRecord, String clientId, int pageSize);
	
	public List<TsmpOpenApiKey> query_dpb0094Service(Date startDate, Date endDate, TsmpOpenApiKey lastRecord, 
			String[] words, Integer pageSize);
	
	public List<TsmpOpenApiKey> findByOpenApiKeyAlias(String openApiKeyAlias);
	
	public List<TsmpOpenApiKey> findByClientIdOrderByExpiredAtDesc(String clientId);
	
}
