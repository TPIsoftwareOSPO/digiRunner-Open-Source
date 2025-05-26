package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpnApiModule;

@Repository
public interface TsmpnApiModuleDao extends JpaRepository<TsmpnApiModule, Long> {

	public List<TsmpnApiModule> findByModuleNameAndActive(String moduleName, Boolean active);

	public List<TsmpnApiModule> queryActiveV3ModulesByModuleName(String moduleName);

	public List<TsmpnApiModule> findByUploaderName(String uploaderName);

	public List<TsmpnApiModule> findByOrgId(String orgId);
	
	//取得v2有啟動的.NET module
	public List<TsmpnApiModule> queryActiveModuleByModuleNameAndOrgId(String moduleName, String orgId);
	
	public List<TsmpnApiModule> queryByModuleNameAndOrgIdOrderByUploadTimeDesc(String moduleName, String orgId);

	public List<TsmpnApiModule> queryListByOrgId(String orgId);
}
