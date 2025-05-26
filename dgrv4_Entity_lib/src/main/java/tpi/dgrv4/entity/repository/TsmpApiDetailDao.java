package tpi.dgrv4.entity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpApiDetail;

@Repository
public interface TsmpApiDetailDao extends JpaRepository<TsmpApiDetail, Long> {
							   
	public Optional<TsmpApiDetail> findFirstByApiModuleIdAndApiKey(Long apiModuleId, String apiKey);
	
	public TsmpApiDetail queryFirstByApiModuleIdAndPathOfJsonLike(Long apiModuleId, String path);

	public List<TsmpApiDetail> query_AA0303Service_01(String apiKey, String moduleName, List<String> orgIdList);

	public List<TsmpApiDetail> queryByAA0421Service(Long lastId, String[] keywords, Long apiModuleId, String moduleName, List<String> orgList, int pageSize);

	public long deleteByApiModuleId(Long apiModuleId);

}
