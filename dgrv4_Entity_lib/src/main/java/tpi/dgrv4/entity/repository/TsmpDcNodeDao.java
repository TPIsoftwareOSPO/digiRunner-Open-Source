package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpDcNode;
import tpi.dgrv4.entity.entity.jpql.TsmpDcNodeId;

@Repository
public interface TsmpDcNodeDao extends JpaRepository<TsmpDcNode, TsmpDcNodeId> {
	
	public List<TsmpDcNode> findByDcId(Long dcId);
	
	public List<TsmpDcNode> findByNodeLike(String node);

}
