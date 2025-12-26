package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpAlert;

@Repository
public interface TsmpAlertDao extends JpaRepository<TsmpAlert, Long> {

	public List<TsmpAlert> findByCreateUser(String createUser);
	public TsmpAlert findFirstByAlertName(String alertName);
	public List<TsmpAlert> queryByAA0706Service(Long lastAlertId, Boolean alertEnabled, String roleName, String[] words, Integer pageSize);
	public TsmpAlert findFirstByAlertIdAndAlertName(Long alertId, String alertName);
}
