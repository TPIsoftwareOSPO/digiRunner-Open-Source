package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.TsmpRegModule;

@Repository
public interface TsmpRegModuleDao extends JpaRepository<TsmpRegModule, Long> {

	public TsmpRegModule findFirstByModuleNameAndModuleVersion(String moduleName, String moduleVersion);
	
	public List<TsmpRegModule> findByModuleNameAndLatest(String moduleName, String latest);

	public List<TsmpRegModule> findByModuleName(String moduleName);

	public int countByModuleName(String moduleName);

	public List<TsmpRegModule> findByCreateUser(String createUser);
	
}
