package tpi.dgrv4.dpaa.service;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.component.ai.AiEngineDTO;
import tpi.dgrv4.dpaa.component.ai.AiEngineSelector;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.entity.entity.*;
import tpi.dgrv4.entity.repository.*;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.model.Attempt;

import java.text.MessageFormat;
import java.util.*;

@Service
public class AiGatewayService {

    public record Pagination(Integer pageNum, Integer pageSize, String[] sortBy, String sortOrder) {

        public Pagination() {
            this(0, 20);
        }

        public Pagination(Integer pageNum, Integer pageSize) {
            this(pageNum, pageSize, new String[]{"id"}, "desc");
        }

        public Sort.Direction sortDirection() {
            if (this.sortOrder.equalsIgnoreCase("desc")) {
                return Sort.Direction.DESC;
            }
            return Sort.Direction.ASC;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            Pagination that = (Pagination) o;
            return Objects.equals(pageNum, that.pageNum) && Objects.deepEquals(sortBy, that.sortBy) && Objects.equals(pageSize, that.pageSize) && Objects.equals(sortOrder, that.sortOrder);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pageNum, pageSize, Arrays.hashCode(sortBy), sortOrder);
        }

        @Override
        public String toString() {
            return "Pagination{" +
                    "pageNum=" + pageNum +
                    ", pageSize=" + pageSize +
                    ", sortBy=" + Arrays.toString(sortBy) +
                    ", sortOrder='" + sortOrder + '\'' +
                    '}';
        }
    }

    @Setter(onMethod_ = @Autowired)
    DgrAiProviderRepository providers;

    @Setter(onMethod_ = @Autowired)
    DgrAiApikeyRepository apiKeys;

    @Setter(onMethod_ = @Autowired)
    DgrAiApikeyUsageRepository usages;

    @Setter(onMethod_ = @Autowired)
    DgrAiPromptTemplateRepository templates;

    @Setter(onMethod_ = @Autowired)
    DgrAiPromptTemplateBindingRepository templateBindings;

    @Setter(onMethod_ = @Autowired)
    AiEngineSelector aiEngineSelector;

    public boolean isAcceptableRemoteAddress(String remoteAddress) {
        return ControllerUtil.isLocalRoute(remoteAddress);
    }

    public Page<DgrAiProvider> providers(Pagination pagination) {
        Pageable page = PageRequest.of(pagination.pageNum(), pagination.pageSize(), Sort.by(pagination.sortDirection(), pagination.sortBy()));
        return providers.findAll(page);
    }


    public Page<DgrAiApiKey> apiKeys(Pagination pagination) {
        Pageable page = PageRequest.of(pagination.pageNum(), pagination.pageSize(), Sort.by(pagination.sortDirection(), pagination.sortBy()));
        return apiKeys.findAll(page);
    }

    public Page<DgrAiApiKeyUsage> usages(Pagination pagination) {
        Pageable page = PageRequest.of(pagination.pageNum(), pagination.pageSize(), Sort.by(pagination.sortDirection(), pagination.sortBy()));
        return usages.findAll(page);
    }

    public Page<DgrAiPromptTemplate> templates(Pagination pagination) {
        Pageable page = PageRequest.of(pagination.pageNum(), pagination.pageSize(), Sort.by(pagination.sortDirection(), pagination.sortBy()));
        return templates.findAll(page);
    }

    public Page<DgrAiPromptTemplate> templates(String templateName, String templateEnabled, Pagination pagination) {
        Pageable page = PageRequest.of(pagination.pageNum(), pagination.pageSize(), Sort.by(pagination.sortDirection(), pagination.sortBy()));
        if (!StringUtils.isEmpty(templateEnabled) && !StringUtils.isEmpty(templateName)) {
            return templates.findByAiPromptTemplateNameContainingIgnoreCaseAndAiPromptTemplateEnable(templateName, templateEnabled, page);
        } else if (!StringUtils.isEmpty(templateName)) {
            return templates.findByAiPromptTemplateNameContainingIgnoreCase(templateName, page);
        } else if (!StringUtils.isEmpty(templateEnabled)) {
            return templates.findByAiPromptTemplateEnable(templateEnabled, page);
        }

        return templates.findAll(page);
    }

    public Page<DgrAiPromptTemplateBinding> templateBindings(Pagination pagination) {
        Pageable page = PageRequest.of(pagination.pageNum(), pagination.pageSize(), Sort.by(pagination.sortDirection(), pagination.sortBy()));
        return templateBindings.findAll(page);
    }

    public Optional<DgrAiPromptTemplateBinding> findPromptTemplateBindingByConsumerTypeAndConsumerId(String type, String id) {
        return templateBindings.findByAiApikeyConsumerTypeAndAiApikeyConsumerId(type, id);
    }

    public DgrAiPromptTemplate defaultTemplate() {

        return templates.findById(1L).orElseThrow(() -> new TsmpDpAaException("default template not found"));
    }


    @Builder
    @Accessors(fluent = true)
    public record PromptToAiRequest(DgrAiPromptTemplate template, DgrAiApiKey apiKey, String promptContent, String consumerId, String consumerType) {}

    @Transactional
    public Attempt<List<String>> promptToAI(
            PromptToAiRequest request) {

        var apiKey = request.apiKey;

        var providerName = apiKey.getDgrAiProvider().getAiProviderName();
        var aiModel = apiKey.getDgrAiProvider().getAiModel();


        var engine = aiEngineSelector.select(providerName, aiModel);
        if (engine.isEmpty()) return Attempt.failure(new TsmpDpAaException("AI engine not found"));

        var now = System.currentTimeMillis();
        Attempt<AiEngineDTO.Output> result = engine.get().generateContent(new AiEngineDTO.Input(apiKey, request.template, request.promptContent));

        var failure = result.failure();

        if (failure.isPresent()) {
            return Attempt.failure(failure.get());
        }

        var success = result.success();

        if (success.isEmpty()) {
            return Attempt.failure(new Exception("ai empty response"));
        }

        var duration = System.currentTimeMillis() - now;

        var usage = DgrAiApiKeyUsage.builder()
                .id(RandomSeqLongUtil.getRandomLongByDefault())
                .dgrAiApiKey(apiKey)
//                .aiUsagePromptInput(promptContent)
//                .aiUsagePromptOutput(promptOutput)
                .httpTransactionStatus(String.valueOf(success.get().respData().statusCode))
                .aiApikeyConsumerId(request.consumerId)
                .aiApikeyConsumerType(request.consumerType)
                .inputTokenCount(success.get().inputTokenCount())
                .outputTokenCount(success.get().outputTokenCount())
                .requstTs(duration)
                .dgrAiPromptTemplate(request.template)
                .build();

        this.upsert(usage);

        // sum apikey usage

        var apiKeyUsageTotalInputTokenCount = apiKey.getUsageInputTokenCount() + success.get().inputTokenCount();
        var apiKeyUsageTotalOutputTokenCount = apiKey.getUsageOutputTokenCount() + success.get().outputTokenCount();

        apiKey
                .setUsageInputTokenCount(apiKeyUsageTotalInputTokenCount)
                .setUsageOutputTokenCount(apiKeyUsageTotalOutputTokenCount);

        this.upsert(apiKey);
        String uuid = UUID.randomUUID().toString();

        String logTemplate = """
                
                --【LOGUUID】【{0}】【Start AiGateway-to-AiProvider】--
                --【LOGUUID】【{0}】【End AiGateway-to-AiProvider】--
                {1}
                """;

        if (success.get().respData().statusCode > 300) {
            var log = MessageFormat.format(logTemplate, uuid, success.get().respData().getLogStr());
            TPILogger.tl.error(log);
        } else {
            var log = MessageFormat.format(logTemplate, uuid, apiKey.getDgrAiProvider().getGenerateApi());
            TPILogger.tl.debug(log);
        }

        return Attempt.success(success.get().candidates());
    }

    @Transactional
    public DgrAiProvider upsert(DgrAiProvider provider) {
        return providers.save(provider);
    }

    @Transactional
    public DgrAiApiKey upsert(DgrAiApiKey apikey) {
        return apiKeys.save(apikey);
    }

    @Transactional
    public void upsert(DgrAiApiKeyUsage usage) {
        usages.save(usage);
    }

    @Transactional
    public DgrAiPromptTemplate upsert(DgrAiPromptTemplate template) {
        return templates.save(template);
    }

    @Transactional
    public DgrAiPromptTemplateBinding upsert(DgrAiPromptTemplateBinding config) {
        return templateBindings.save(config);
    }


    public Optional<DgrAiProvider> findProvider(Long id) {
        return providers.findById(id);
    }

    public Optional<DgrAiProvider> findProvider(String providerName, String modelName) {
        return providers.findDgrAiProviderByAiProviderNameAndAiModel(providerName, modelName);
    }

    public Optional<DgrAiApiKey> findApiKey(Long id) {
        return apiKeys.findById(id);
    }


    public Optional<DgrAiPromptTemplate> findPromptTemplate(Long id) {
        return templates.findById(id);
    }

    public Optional<DgrAiPromptTemplate> findPromptTemplate(String templateName) {
        return templates.findDgrAiPromptTemplateByAiPromptTemplateName(templateName);
    }

    public Optional<DgrAiPromptTemplateBinding> findPromptTemplateSetting(Long id) {
        return templateBindings.findById(id);
    }

    public Optional<DgrAiApiKeyUsage> findUsage(Long id) {
        return usages.findById(id);
    }


    @Transactional
    public void delete(DgrAiApiKey apikey) {
        apiKeys.delete(apikey);
    }

    @Transactional
    public void delete(DgrAiProvider provider) {
        providers.delete(provider);
    }


    @Transactional
    public void delete(DgrAiPromptTemplate template) {
        templates.delete(template);
    }

    @Transactional
    public void delete(DgrAiPromptTemplateBinding config) {
        templateBindings.delete(config);
    }

}
