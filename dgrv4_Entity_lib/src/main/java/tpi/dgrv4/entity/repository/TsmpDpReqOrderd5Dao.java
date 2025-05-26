package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd5;

@Repository
public interface TsmpDpReqOrderd5Dao extends JpaRepository<TsmpDpReqOrderd5, Long> {

	public List<TsmpDpReqOrderd5> findByRefReqOrdermId(Long refReqOrdermId);
	
	public List<TsmpDpReqOrderd5> findByCreateUser(String createUser);
}
