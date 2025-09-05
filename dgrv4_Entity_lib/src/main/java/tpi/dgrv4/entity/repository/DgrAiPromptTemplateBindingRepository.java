package tpi.dgrv4.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;
import tpi.dgrv4.entity.entity.DgrAiPromptTemplateBinding;

import java.util.Optional;

@RestController
public interface DgrAiPromptTemplateBindingRepository extends JpaRepository<DgrAiPromptTemplateBinding, Long> {

    Optional<DgrAiPromptTemplateBinding> findByAiApikeyConsumerTypeAndAiApikeyConsumerId(String aiApikeyConsumerType, String aiApikeyConsumerId);
}
