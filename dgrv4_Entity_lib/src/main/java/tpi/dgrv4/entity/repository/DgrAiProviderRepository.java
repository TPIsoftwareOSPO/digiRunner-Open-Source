package tpi.dgrv4.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tpi.dgrv4.entity.entity.DgrAiProvider;

import java.util.Optional;

@Repository
public interface DgrAiProviderRepository extends JpaRepository<DgrAiProvider, Long> {
    Optional<DgrAiProvider> findDgrAiProviderByAiProviderNameAndAiModel(String aiProviderName, String aiModel);

}
