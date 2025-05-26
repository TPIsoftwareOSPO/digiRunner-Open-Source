package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd3;

@Repository
public interface TsmpDpReqOrderd3Dao extends JpaRepository<TsmpDpReqOrderd3, Long> {

	public TsmpDpReqOrderd3 findFirstByRefReqOrdermId(Long reqOrdermId);

	public List<TsmpDpReqOrderd3> findByClientId(String ClientId);

	public List<TsmpDpReqOrderd3> findByCreateUser(String createUser);

}
