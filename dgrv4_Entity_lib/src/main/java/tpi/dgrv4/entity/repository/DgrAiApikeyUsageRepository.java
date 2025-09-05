package tpi.dgrv4.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tpi.dgrv4.entity.entity.DgrAiApiKey;
import tpi.dgrv4.entity.entity.DgrAiApiKeyUsage;

import java.util.List;

public interface DgrAiApikeyUsageRepository extends JpaRepository<DgrAiApiKeyUsage, Long> {

    List<DgrAiApiKeyUsage> findAllByDgrAiApiKey(DgrAiApiKey dgrAiApiKey);
}
