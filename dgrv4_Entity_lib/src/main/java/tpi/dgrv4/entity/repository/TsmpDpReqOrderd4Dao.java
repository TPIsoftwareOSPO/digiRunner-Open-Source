package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd4;

@Repository
public interface TsmpDpReqOrderd4Dao extends JpaRepository<TsmpDpReqOrderd4, Long> {

	public List<TsmpDpReqOrderd4> findByRefReqOrdermId(Long refReqOrdermId);

	public TsmpDpReqOrderd4 findFirstByRefReqOrdermIdAndUserId(Long refReqOrdermId, String userId);

	public List<TsmpDpReqOrderd4> findByCreateUser(String createUser);

}
