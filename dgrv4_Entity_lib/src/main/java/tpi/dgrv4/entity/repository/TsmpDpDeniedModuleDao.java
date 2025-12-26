package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpDpDeniedModule;


@Repository
public interface TsmpDpDeniedModuleDao extends JpaRepository<TsmpDpDeniedModule, String> {
	
	public List<TsmpDpDeniedModule> findByCreateUser(String createUser);
	
}
