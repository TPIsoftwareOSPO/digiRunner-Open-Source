package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpDpCallapi;

@Repository
public interface TsmpDpCallapiDao extends JpaRepository<TsmpDpCallapi, Long> {

	public List<TsmpDpCallapi> findByCreateUser(String createUser);

}
