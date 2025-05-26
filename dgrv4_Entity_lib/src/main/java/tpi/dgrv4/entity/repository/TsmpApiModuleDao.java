package tpi.dgrv4.entity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpApiModule;
import tpi.dgrv4.entity.vo.AA0419SearchCriteria;

@Repository
public interface TsmpApiModuleDao extends JpaRepository<TsmpApiModule, Long> {

	public List<TsmpApiModule> findByModuleNameAndActive(String moduleName, Boolean active);

	public List<TsmpApiModule> queryActiveV3ModulesByModuleName(String moduleName);

	public List<TsmpApiModule> findByUploaderName(String uploaderName);
	
	//JPQL
	public List<TsmpApiModule> queryApiDocsListLike(String[] words, String moduleName //
			, String moduleVersion, Integer pageSize);

	public boolean isExistsByModuleName(String moduleName);
	
	public List<TsmpApiModule> findByOrgId(String orgId);

	public TsmpApiModule queryById(Long id);

	public List<TsmpApiModule> findByModuleNameAndActiveAndOrgId(String moduleName, Boolean active, String orgId);
	
	public Optional<TsmpApiModule> findFirstByModuleNameAndOrgIdOrderByUploadTimeDesc(String moduleName, String orgId);

	// 找出V2有啟動的 Java module：
	public List<TsmpApiModule> queryActiveV2ByModuleNameAndOrgId(String moduleName, String orgId);
	
	// 找出V3有啟動的 Java module：
	public List<TsmpApiModule> queryActiveV3ByModuleNameAndOrgId(String moduleName, String orgId);

	public List<TsmpApiModule> queryByModuleNameAndOrgIdOrderByUploadTimeDesc(String moduleName, String orgId);
	
	public TsmpApiModule queryFirstByModuleName(String moduleName);
	
	public List<TsmpApiModule> queryByAA0420Service(Long lastId, String[] keywords, Long dcId, String moduleName, List<String> orgList, int pageSize);

	public List<TsmpApiModule> query_AA0419Service_01(AA0419SearchCriteria cri);

	public List<TsmpApiModule> query_AA0419Service_02(AA0419SearchCriteria cri, Long dcId);

	public TsmpApiModule query_AA0403Service_01(Long id, String moduleName, boolean isV2, List<String> orgIdList);

	public TsmpApiModule query_AA0404Service_01(Long id, String moduleName, List<String> orgIdList);

	public List<TsmpApiModule> query_AA0404Service_02(String moduleName, Long dcId);

	public List<TsmpApiModule> queryByModuleName(String moduleName);
	
	public List<TsmpApiModule> queryModuleVersion();

	public List<TsmpApiModule> query_AA0404Service_03(String moduleName, Integer v2Flag);

	public List<TsmpApiModule> queryListByOrgId(String orgId);
}