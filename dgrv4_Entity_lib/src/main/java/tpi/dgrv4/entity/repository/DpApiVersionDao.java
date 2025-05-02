package tpi.dgrv4.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DpApiVersion;

@Repository
public interface DpApiVersionDao extends JpaRepository<DpApiVersion, Long>, DpApiVersionSuperDao {

	List<DpApiVersion> findByModuleNameAndApiKey(String moduleName, String apiKey);
}
