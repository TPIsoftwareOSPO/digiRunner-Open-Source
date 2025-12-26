package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpRoleAlert;
import tpi.dgrv4.entity.entity.jpql.TsmpRoleAlertId;

@Repository
public interface TsmpRoleAlertDao extends JpaRepository<TsmpRoleAlert, TsmpRoleAlertId> {

	List<TsmpRoleAlert> findByAlertId(Long alertId);
	Long deleteByAlertId(Long alertId);
}
