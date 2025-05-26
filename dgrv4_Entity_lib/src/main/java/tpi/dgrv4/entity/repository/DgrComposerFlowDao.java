package tpi.dgrv4.entity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.jpql.DgrComposerFlow;

@Repository
public interface DgrComposerFlowDao extends JpaRepository<DgrComposerFlow, Long> {

	public Optional<DgrComposerFlow> findByModuleNameAndApiId(String moduleName, String apiId);

}
