package tpi.dgrv4.entity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DpUserInfoRDB;

@Repository
public interface DpUserInfoRDBDao extends JpaRepository<DpUserInfoRDB, Long>, DpUserInfoRDBSuperDao {

	public Optional<DpUserInfoRDB> findByUserName(String userName);

}
