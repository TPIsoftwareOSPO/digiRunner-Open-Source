package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpClient;

@Repository
public interface TsmpClientDao extends JpaRepository<TsmpClient, String> {

	public List<TsmpClient> queryLike(String lastId, String[] words, int pageSize);

	// DPB0095
	public List<TsmpClient> queryByRegStatusAndLike(String lastId, String[] words, String regStatus, int pageSize);

	@Deprecated
	public TsmpClient findPrivateClient(String clientId);

	public List<TsmpClient> findByClientName(String clientName);

	public List<TsmpClient> findByCreateUser(String createUser);

	public List<TsmpClient> findByClientIdAndKeywordAndGroupIdAndStatus(String clientId, String[] words, String groupID,
			String status, int pageSize);

	public List<TsmpClient> findByClientAlias(String clientAlias);

	public TsmpClient findFirstByClientIdAndClientName(String clientId, String clientName);

	public List<TsmpClient> queryByClientIdNotExists();

	public List<TsmpClient> findBySecurityLevelId(String securityLevelId);

	public Long countByClientStatus(String clientStatus);

	public List<TsmpClient> findByApiUsedGreaterThan(int apiUsed);

	public TsmpClient findFirstByClientId(String clientId);

	public List<TsmpClient> findByDpClientEntryIsNotNullAndDpClientEntryNotLike(String prefix);

	public boolean existsByClientAliasAndClientIdNot(String clientAlias, String clientId);

	public boolean existsByClientNameAndClientIdNot(String clientName, String clientId);

	public boolean existsByClientAlias(String clientAlias);

	public boolean existsByClientName(String clientName);

	long deleteByClientId(String clientId);

}
