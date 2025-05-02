package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpDcModule;
import tpi.dgrv4.entity.entity.jpql.TsmpDcModuleId;

@Repository
public interface TsmpDcModuleDao extends JpaRepository<TsmpDcModule, TsmpDcModuleId> {

	public List<TsmpDcModule> findAllByModuleId(Long moduleId);
	
	public List<TsmpDcModule> findByDcId(Long dcId);

	public List<TsmpDcModule> queryByAA0420Service(List<Long> moduleIdList);

	public long query_AA0404Service_01(String moduleName, String dcCode);

}