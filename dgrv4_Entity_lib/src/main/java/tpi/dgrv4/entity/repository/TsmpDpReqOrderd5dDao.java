package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd5d;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd5dId;

@Repository
public interface TsmpDpReqOrderd5dDao extends JpaRepository<TsmpDpReqOrderd5d, TsmpDpReqOrderd5dId> {
	
	public List<TsmpDpReqOrderd5d> findByReqOrderd5dId(Long reqOrderd5dId);
	
	public List<TsmpDpReqOrderd5d> findByRefReqOrderd5Id(Long refReqOrderd5Id);
	
	public List<TsmpDpReqOrderd5d> findByCreateUser(String createUser);
}
