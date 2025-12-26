package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpNode;

@Repository
public interface TsmpNodeDao extends JpaRepository<TsmpNode, String> {
	
	public List<TsmpNode> findByUpdateTimeAfter(Date updateTime);
	
	public TsmpNode findFirstByNode(String node);
	
	//AA0417
	public List<String> queryGreenTsmpNode(String lastNode, String[] words, Date queryStartDate,
			List<String> excludeNode, int pageSize);

	public TsmpNode query_AA0404Service_01(String moduleName, Integer v2Flag);
 
}
