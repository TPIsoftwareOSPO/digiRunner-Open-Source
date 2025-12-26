package tpi.dgrv4.entity.repository.autoInitSQL;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.TsmpRoleRoleMapping;
import tpi.dgrv4.entity.entity.autoInitSQL.AutoInitSQLTsmpRoleRoleMapping;


@Repository
public interface AutoInitSQLTsmpRoleRoleMappingDao extends JpaRepository<AutoInitSQLTsmpRoleRoleMapping, Long> {

	public List<AutoInitSQLTsmpRoleRoleMapping> findByRoleNameAndRoleNameMapping(String roleName, String roleNameMapping);
}
