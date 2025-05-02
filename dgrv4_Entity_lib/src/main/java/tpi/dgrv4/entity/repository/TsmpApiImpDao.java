package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpApiImp;
import tpi.dgrv4.entity.entity.jpql.TsmpApiImpId;

@Repository
public interface TsmpApiImpDao extends JpaRepository<TsmpApiImp, TsmpApiImpId> {

	public Integer queryMaxBatchNo();

	public long countByBatchNo(Integer batchNo);

	public List<TsmpApiImp> findByCreateUser(String createUser);

}
