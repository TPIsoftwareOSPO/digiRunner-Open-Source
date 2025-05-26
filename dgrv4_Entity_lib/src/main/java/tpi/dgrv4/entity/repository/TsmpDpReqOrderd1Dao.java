package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd1;

@Repository
public interface TsmpDpReqOrderd1Dao extends JpaRepository<TsmpDpReqOrderd1, Long> {

	public List<TsmpDpReqOrderd1> findByRefReqOrdermId(Long reqOrdermId);

	public List<TsmpDpReqOrderd1> findByClientIdAndApiUid(String clientId, String apiUid);

	public List<TsmpDpReqOrderd1> findByClientId(String clientId);

	public List<TsmpDpReqOrderd1> findByApiUid(String apiUid);

	public List<TsmpDpReqOrderd1> findByCreateUser(String createUser);

}
