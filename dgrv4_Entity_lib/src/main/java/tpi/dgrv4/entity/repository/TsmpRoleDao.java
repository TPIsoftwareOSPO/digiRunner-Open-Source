package tpi.dgrv4.entity.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpRole;

@Repository
public interface TsmpRoleDao extends JpaRepository<TsmpRole, String> {
	
	public List<TsmpRole> findByCreateUser(String createUser);
	
	public TsmpRole findFirstByRoleName(String roleName);
	
	public boolean existsByRoleName(String roleName);
	
	public List<TsmpRole> findByRoleIdAndKeyword(String roleId, String[] words, Integer pageSize);
	
	public List<TsmpRole> findByRoleName(String roleName);
	
	public List<TsmpRole> findByRoleAlias(String roleAlias);

	public List<TsmpRole> findByAA0022Service(String roleId, String[] words, Integer pageSize);
	
	public List<TsmpRole> query_aa0104Service(String roleId, String funcCode, String[] words, Integer pageSize);
	
	public List<TsmpRole> queryByAA0023Service(String roleId, String userName, String[] words, Integer pageSize);
	
}
