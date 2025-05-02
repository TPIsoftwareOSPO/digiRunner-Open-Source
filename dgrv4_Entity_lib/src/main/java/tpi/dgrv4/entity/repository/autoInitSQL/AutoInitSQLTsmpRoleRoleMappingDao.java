package tpi.dgrv4.entity.repository.autoInitSQL;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.autoInitSQL.AutoInitSQLTsmpRoleRoleMapping;


@Repository
public interface AutoInitSQLTsmpRoleRoleMappingDao extends JpaRepository<AutoInitSQLTsmpRoleRoleMapping, Long> {

}
