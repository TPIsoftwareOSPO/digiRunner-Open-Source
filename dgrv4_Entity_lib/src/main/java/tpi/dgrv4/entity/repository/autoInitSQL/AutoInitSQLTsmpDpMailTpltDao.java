package tpi.dgrv4.entity.repository.autoInitSQL;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.autoInitSQL.AutoInitSQLTsmpDpMailTplt;

@Repository
public interface AutoInitSQLTsmpDpMailTpltDao extends JpaRepository<AutoInitSQLTsmpDpMailTplt, Long> {

}
