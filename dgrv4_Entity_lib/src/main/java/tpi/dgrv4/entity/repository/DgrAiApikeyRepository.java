package tpi.dgrv4.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tpi.dgrv4.entity.entity.DgrAiApiKey;

import java.util.Optional;

@Repository
public interface DgrAiApikeyRepository extends JpaRepository<DgrAiApiKey, Long> {

    Optional<DgrAiApiKey> findByAiApikeyCode(String aiApikeyCode);
}
