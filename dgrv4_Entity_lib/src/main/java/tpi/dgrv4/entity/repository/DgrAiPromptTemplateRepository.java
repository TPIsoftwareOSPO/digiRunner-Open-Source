package tpi.dgrv4.entity.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tpi.dgrv4.entity.entity.DgrAiPromptTemplate;

import java.util.List;
import java.util.Optional;

@Repository
public interface DgrAiPromptTemplateRepository extends JpaRepository<DgrAiPromptTemplate, Long> {
    Optional<DgrAiPromptTemplate> findDgrAiPromptTemplateByAiPromptTemplateName(String aiPromptTemplateName);
    Page<DgrAiPromptTemplate> findByAiPromptTemplateNameContainingIgnoreCaseAndAiPromptTemplateEnable(
            String aiPromptTemplateName,
            String aiPromptTemplateEnable,
            Pageable pageable
    );
    Page<DgrAiPromptTemplate> findByAiPromptTemplateNameContainingIgnoreCase(
            String aiPromptTemplateName,
            Pageable pageable
    );
    Page<DgrAiPromptTemplate> findByAiPromptTemplateEnable(
            String aiPromptTemplateEnable,
            Pageable pageable
    );
}
