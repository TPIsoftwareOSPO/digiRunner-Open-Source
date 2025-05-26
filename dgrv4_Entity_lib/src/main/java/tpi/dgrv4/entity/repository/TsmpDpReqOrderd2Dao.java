package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd2;

@Repository
public interface TsmpDpReqOrderd2Dao extends JpaRepository<TsmpDpReqOrderd2, Long> {

	public List<TsmpDpReqOrderd2> findByRefReqOrdermId(Long refReqOrdermId);

	public List<TsmpDpReqOrderd2> findByCreateUser(String createUser);

}
