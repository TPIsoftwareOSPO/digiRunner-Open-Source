package tpi.dgrv4.entity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpnApiDetail;

@Repository
public interface TsmpnApiDetailDao extends JpaRepository<TsmpnApiDetail, Long> {

	public List<TsmpnApiDetail> query_AA0303Service_01(String apiKey, String moduleName, List<String> orgIdList);

	public List<TsmpnApiDetail> findByApiModuleIdAndApiKey(Long apiModuleId, String apiKey);
	
	public Optional<TsmpnApiDetail> findFirstByApiModuleIdAndApiKey(Long apiModuleId, String apiKey);

}
