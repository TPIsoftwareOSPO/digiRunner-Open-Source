package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpDc;

@Repository
public interface TsmpDcDao extends JpaRepository<TsmpDc, Long> {

	public List<TsmpDc> findByCreateUser(String createUser);
	
	public TsmpDc findFirstByDcCode(String dcCode);
	
	public TsmpDc findByDcIdAndDcCode(Long dcId, String dcCode);
	
	//AA0418
	public List<TsmpDc> queryDCList_1(TsmpDc lastRecord, String dcMappingPrefix, String[] words, Boolean isActive, int pageSize, boolean isPaging);
 
	//AA0422
	public List<TsmpDc> queryDCList_2(String moduleName);
}
