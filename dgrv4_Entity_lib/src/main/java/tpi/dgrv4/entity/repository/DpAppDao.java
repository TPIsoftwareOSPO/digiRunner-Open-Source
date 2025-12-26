package tpi.dgrv4.entity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DpApp;

@Repository
public interface DpAppDao extends JpaRepository<DpApp, Long>, DpAppSuperDao {

	public List<DpApp> findAppByDpUserNameAndIssAndAppDescAndApiNameAndApiDescAndModuleNameAndApiKey(Long appId,
			String dpUserName, String iss, String[] words, String isAdmin, String encodeClientStatus, Integer pageSize);

	List<Long> findPendingDpAppCount();

	List<String> findAppClientIdByDpUserNameAndIss(String dpUserName, String iss);

	List<DpApp> findByIdTokenJwtstr(String idTokenJwtstr);

	List<DpApp> findByDpUserNameAndIss(String dpUserName, String iss);

	List<DpApp> findByClientId(String clientId);

	public List<Long> findReviewedDpAppCountByDpUserNameAndIss(String dpUserName, String iss);

	public List<Long> findNotApprovedDpAppCountByDpUserNameAndIss(String dpUserName, String iss);
	
	public DpApp findFirstByClientId(String clientId);
}