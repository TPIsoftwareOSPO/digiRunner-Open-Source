package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd2d;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd2dId;

@Repository
public interface TsmpDpReqOrderd2dDao extends JpaRepository<TsmpDpReqOrderd2d, TsmpDpReqOrderd2dId> {

	public List<TsmpDpReqOrderd2d> findByReqOrderd2Id(Long reqOrderd2Id);
	
	public List<TsmpDpReqOrderd2d> findByCreateUser(String createUser);

}