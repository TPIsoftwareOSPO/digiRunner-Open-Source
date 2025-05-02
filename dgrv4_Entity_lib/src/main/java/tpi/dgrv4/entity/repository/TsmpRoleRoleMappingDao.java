package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpRoleRoleMapping;

@Repository
public interface TsmpRoleRoleMappingDao extends JpaRepository<TsmpRoleRoleMapping, Long> {
	
	public List<TsmpRoleRoleMapping> findByRoleNameMapping(String roleNameMapping);

	public List<TsmpRoleRoleMapping> findByRoleNameAndRoleNameMapping(String roleName, String roleNameMapping);
	
	public List<TsmpRoleRoleMapping> findByRoleName(String roleName);
	
	public long deleteByRoleName(String roleName);
	
	public long deleteByRoleNameMapping(String roleNameMapping);
	
	public long deleteByRoleNameAndRoleNameMapping(String roleName, String roleNameMapping);

}
