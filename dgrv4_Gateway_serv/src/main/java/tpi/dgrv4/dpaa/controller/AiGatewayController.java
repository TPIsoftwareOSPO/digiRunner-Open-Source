package tpi.dgrv4.dpaa.controller;


import lombok.Builder;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.component.ai.AiEngineSelector;
import tpi.dgrv4.dpaa.service.AiGatewayService;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.entity.entity.*;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

import java.util.Optional;

import static tpi.dgrv4.dpaa.vo.AiGatewayReq.*;

@RestController
public class AiGatewayController {

    @Setter(onMethod_ = @Autowired)
    AiGatewayService service;


    @Builder
    @Accessors(fluent = true)
    record AiApiKeyReqInfo(Optional<DgrAiProvider> provider, AiApiKeyReq payload) {}


    private AiApiKeyReqInfo fetchAiApiKeyReqInfo(TsmpBaseReq<? extends AiApiKeyReq> req) {

        var builder = AiApiKeyReqInfo.builder();

        var payload = req.getBody();

        builder.payload(payload)
                .provider(Optional.empty());

        if (payload.getAiProviderId() != null) {
            var provider = service.findProvider(payload.getAiProviderId());

            builder.provider(provider);
        }

        return builder.build();
    }

    private TsmpAuthorization validateHeader(HttpHeaders headers, TsmpBaseReq<?> req) {
        TsmpHttpHeader tsmpHttpHeader = ControllerUtil.toTsmpHttpHeader(headers);
        var auth = tsmpHttpHeader.getAuthorization();
        ControllerUtil.validateRequest(auth, req);
        return auth;
    }


    /**
     * 查詢 AI 供應商清單
     * @return 供應商清單
     */

    @PostMapping("/dgrv4/11/DPB0250")
    public TsmpBaseResp<Page<DgrAiProvider>> listAllProvider(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<DPB0250Req> req
    ) {
        validateHeader(headers, req);

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), service.providers(req.getBody().getPagination()));
    }

    @PostMapping("/dgrv4/11/DPB0251")
    public TsmpBaseResp<DgrAiProvider> createProvider(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<DPB0251Req> req
    ) {
        var auth = validateHeader(headers, req);
        var providerName = req.getBody().getAiProviderName();

        if (!AiEngineSelector.ProviderName.COMPOSER.brand().equals(providerName)) {
            throw TsmpDpAaRtnCode._1559.throwing(String.format("[%s] provider not support", providerName));
        }

        if (Optional.ofNullable(req.getBody().getAiProviderAlias()).isEmpty()) {
            throw TsmpDpAaRtnCode._1559.throwing(String.format("[%s] alias is required", providerName));
        }

        var modelName = req.getBody().getAiModel();
        var provider = service.findProvider(providerName, modelName);

        // check if provider/model already exist
        if (provider.isPresent()) {
            throw TsmpDpAaRtnCode._1559.throwing("%s/%s already exist".formatted(providerName, modelName));
        }

        var entity = DgrAiProvider.builder()
                .id(RandomSeqLongUtil.getRandomLongByDefault())
                .aiProviderAlias(req.getBody().getAiProviderAlias())
                .aiProviderName(providerName)
                .aiModel(modelName)
                .countTokenApi(req.getBody().getCountTokenAPI())
                .generateApi(req.getBody().getGenerateAPI())
                .createUser(auth.getUserName())
                .updateDateTime(DateTimeUtil.now())
                .updateUser(auth.getUserName())
                .aiProviderEnable(req.getBody().getAiProviderEnabled())
                .build();

        var record = service.upsert(entity);
        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), record);
    }

    @PostMapping("/dgrv4/11/DPB0252/{id}")
    public TsmpBaseResp<DgrAiProvider> getProvider    (
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<?> req,
            @PathVariable("id") Long id
    ) {
        validateHeader(headers, req);

        var provider = service.findProvider(id);

        if (provider.isEmpty()) {
            throw TsmpDpAaRtnCode._1354.throwing("ai provider", String.valueOf(id));
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), provider.get());
    }

    @PostMapping("/dgrv4/11/DPB0253/{id}")
    public TsmpBaseResp<DgrAiProvider> updateProvider (
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<DPB0253Req> req,
            @PathVariable("id") Long id
    ) {
        validateHeader(headers, req);

        var providerCheck = service.findProvider(id);

        if (providerCheck.isEmpty()) {
            throw TsmpDpAaRtnCode._1354.throwing("ai provider", String.valueOf(id));
        }

        var provider = providerCheck.get();

        Optional.ofNullable(req.getBody().getAiProviderName()).ifPresent(provider::setAiProviderName);
        Optional.ofNullable(req.getBody().getAiModel()).ifPresent(provider::setAiModel);
        Optional.ofNullable(req.getBody().getAiProviderEnabled()).ifPresent(provider::setAiProviderEnable);
        Optional.ofNullable(req.getBody().getGenerateAPI()).ifPresent(provider::setGenerateApi);
        Optional.ofNullable(req.getBody().getCountTokenAPI()).ifPresent(provider::setCountTokenApi);
        Optional.ofNullable(req.getBody().getAiProviderAlias()).ifPresent(provider::setAiProviderAlias);


        var record = service.upsert(provider);

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), record);
    }

    @PostMapping("/dgrv4/11/DPB0254/{id}")
    public TsmpBaseResp<DgrAiPromptTemplate> deleteProvider(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<?> req,
            @PathVariable("id") Long id) {
        validateHeader(headers, req);

        var provider = service.findProvider(id);

        if (provider.isEmpty()) {
            throw TsmpDpAaRtnCode._1354.throwing("ai provider", String.valueOf(id));
        }
        if (!"Composer".equals(provider.get().getAiProviderName())) {
            throw TsmpDpAaRtnCode._1559.throwing("The system provider does not support delete operations.");
        }


        service.delete(provider.get());

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), null);
    }

    /**
     * 查詢 AI APIKEY 清單
     * @return APIKEY 清單
     */
    @PostMapping("/dgrv4/11/DPB0255")
    public TsmpBaseResp<Page<DgrAiApiKey>> listApiKeys(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<DPB0255Req> req
    ) {
        validateHeader(headers, req);

        var apiKeys = service.apiKeys(req.getBody().getPagination());
        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), apiKeys);
    }

    /**
     * 新增 AI APIKEY
     * @return 新增後的 APIKEY
     */
    @PostMapping("/dgrv4/11/DPB0256")
    public TsmpBaseResp<DgrAiApiKey> registerApiKey(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<DPB0256Req> req
    ) {
        var auth = validateHeader(headers, req);

        var checkInfo = fetchAiApiKeyReqInfo(req);

        if (checkInfo.provider.isEmpty()) {
            throw TsmpDpAaRtnCode._1354.throwing("ai provider", "");
        }

        // 尋找 provider 是否已存在該 apikey
        // 已存在則拋出異常
        if (checkInfo.provider.get().getDgrAiApiKeys().stream().anyMatch(
                key -> key.getAiApikeyCode().equals(checkInfo.payload.getAiApiKeyCode())
        )) {
            throw TsmpDpAaRtnCode._1353.throwing(checkInfo.provider.get().getAiProviderName(), checkInfo.payload.getAiApiKeyCode());
        }

        var payload = checkInfo.payload;

        var entity = DgrAiApiKey.builder()
                .id(RandomSeqLongUtil.getRandomLongByDefault())
                .aiApikeyCode(payload.getAiApiKeyCode())
                .aiApikeyName(payload.getAiApiKeyName())
                .dgrAiProvider(checkInfo.provider.get())
                .usageLimitInputToken(payload.getUsageLimitInputToken())
                .usageLimitOutputToken(payload.getUsageLimitOutputToken())
                .usageLimitPolicy(payload.getUsageLimitPolicy())
//                .backupKeyId(payload.getBackupKeyId())
                .aiApikeyEnable(payload.getAiApiKeyEnable())
                .createUser(auth.getUserId())
                .build();

        var record = service.upsert(entity);

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), record);
    }

    /**
     * 查詢 api key 詳細
     * @param id api key id
     * @return api key
     */
    @PostMapping("/dgrv4/11/DPB0257/{id}")
    public TsmpBaseResp<DgrAiApiKey> getAIAPIKEY(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<?> req,
            @PathVariable("id") Long id
    ) {
        validateHeader(headers, req);

        var apiKey = service.findApiKey(id);

        if (apiKey.isEmpty()) {
            throw TsmpDpAaRtnCode._1354.throwing("ai apikey", String.valueOf(id));
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), apiKey.get());
    }


    /**
     * 更新 api key
     * @param id  api key id
     * @return api key
     */
    @PostMapping("/dgrv4/11/DPB0258/{id}")
    public TsmpBaseResp<DgrAiApiKey> updateAIAPIKEY(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<DPB0258Req> req,
            @PathVariable("id") Long id
    ) {
        var auth = validateHeader(headers, req);

        var checkInfo = fetchAiApiKeyReqInfo(req);

        if (req.getBody().getAiProviderId() != null && checkInfo.provider.isEmpty()) {
            throw TsmpDpAaRtnCode._1354.throwing("ai provider", "");
        }

        var checkKey = service.findApiKey(id);

        if (checkKey.isEmpty()) {
            throw TsmpDpAaRtnCode._1354.throwing("ai apikey", String.valueOf(id));
        }

        var apiKey = checkKey.get();

//        Optional.ofNullable(checkInfo.payload.getBackupKeyId()).ifPresent(apiKey::setBackupKeyId);
        Optional.ofNullable(checkInfo.payload.getUsageLimitInputToken()).ifPresent(apiKey::setUsageLimitInputToken);
        Optional.ofNullable(checkInfo.payload.getUsageLimitOutputToken()).ifPresent(apiKey::setUsageLimitOutputToken);
        Optional.ofNullable(checkInfo.payload.getUsageLimitPolicy()).ifPresent(apiKey::setUsageLimitPolicy);
        Optional.ofNullable(checkInfo.payload.getAiApiKeyEnable()).ifPresent(apiKey::setAiApikeyEnable);
        Optional.ofNullable(checkInfo.payload.getAiApiKeyCode()).ifPresent(apiKey::setAiApikeyCode);
        Optional.ofNullable(checkInfo.payload.getAiApiKeyName()).ifPresent(apiKey::setAiApikeyName);
        checkInfo.provider.ifPresent(apiKey::setDgrAiProvider);

        apiKey.setUpdateDateTime(DateTimeUtil.now())
                .setUpdateUser(auth.getUserId())
        ;

        var record = service.upsert(apiKey);

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), record);
    }

    /**
     * 刪除 api key
     * @param id api key id
     * @return api key
     */
    @PostMapping("/dgrv4/11/DPB0259/{id}")
    public TsmpBaseResp<DgrAiApiKey> deleteAIAPIKEY(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<?> req,
            @PathVariable("id") Long id
    ) {
        validateHeader(headers, req);
        var apiKey = service.findApiKey(id);

        if (apiKey.isEmpty()) {
            throw TsmpDpAaRtnCode._1354.throwing("ai apikey", String.valueOf(id));
        }

        service.delete(apiKey.get());
        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), null);
    }


    /**
     * 查詢 api key 使用紀錄
     * @param req {"pageNum": 分頁數(int),"pageSize": 每頁筆數(int)}
     * @return api key 使用清單
     */
    @PostMapping("/dgrv4/11/DPB0260")
    public TsmpBaseResp<Page<DgrAiApiKeyUsage>>listApiUsage(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<DPB0260Req> req
    ) {
        validateHeader(headers, req);

        var page = service.usages(req.getBody().getPagination());
        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), page);
    }

    /**
     * 查詢 api key 使用詳細
     * @param id api key id
     * @return api key 使用詳細
     */
    @PostMapping("/dgrv4/11/DPB02601/{id}")
    public TsmpBaseResp<DgrAiApiKeyUsage>getAIUsageDetail(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<?> req,
            @PathVariable("id") Long id
    ) {
        validateHeader(headers, req);

        var usage = service.findUsage(id);

        if (usage.isEmpty()) {
            throw TsmpDpAaRtnCode._1354.throwing("ai apikey usage", String.valueOf(id));
        }

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), usage.get());
    }






    /**
     * 查詢樣板清單
     * @return 樣板清單
     */
    @PostMapping("/dgrv4/11/DPB0262")
    public TsmpBaseResp<Page<DgrAiPromptTemplate>> listPromptTemplate(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<DPB0262Req> req
    ) {
        validateHeader(headers, req);

        var templateName = req.getBody().getAiPromptTemplateName();
        var templateEnable = req.getBody().getAiPromptTemplateEnable();
        var page = req.getBody().getPagination();
        var templates = service.templates(templateName, templateEnable, page);
        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), templates);
    }

    /**
     * 新增樣板
     * @return 新增後的樣板資訊
     */
    @PostMapping("/dgrv4/11/DPB0263")
    public TsmpBaseResp<DgrAiPromptTemplate> createPromptTemplate(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<DPB0263Req> req) {
        //        validateHeader(headers, req);

        var payload = req.getBody();

        if (service.findPromptTemplate(payload.getAiPromptTemplateName()).isPresent()) {
            throw TsmpDpAaRtnCode._1353.throwing("ai template", payload.getAiPromptTemplateName());
        }

        var entity = DgrAiPromptTemplate.builder()
                .id(RandomSeqLongUtil.getRandomLongByDefault())
                .aiPromptTemplateName(payload.getAiPromptTemplateName())
                .aiPromptTemplateContent(payload.getAiPromptTemplateContent())
                .aiPromptTemplateRemark(payload.getAiPromptTemplateRemark())
                .aiPromptTemplateEnable(payload.getAiPromptTemplateEnable())
                .build();

        var template = service.upsert(entity);
        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), template);
    }

    /**
     * 查詢樣板詳細
     * @return 樣板詳細
     */
    @PostMapping("/dgrv4/11/DPB0264/{id}")
    public TsmpBaseResp<DgrAiPromptTemplate> getAIPromptTemplate(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<?> req,
            @PathVariable("id") Long id) {
                validateHeader(headers, req);

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), service.findPromptTemplate(id).orElse(null));
    }

    /**
     * 更新樣板
     * @return 更新後的樣板資訊
     */
    @PostMapping("/dgrv4/11/DPB0265/{id}")
    public TsmpBaseResp<DgrAiPromptTemplate> updateAIPromptTemplate(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<DPB0265Req> req,
            @PathVariable("id") Long id) {
        validateHeader(headers, req);

        var templateCheck = service.findPromptTemplate(id);

        if (templateCheck.isEmpty()) {
            throw TsmpDpAaRtnCode._1354.throwing("ai template", String.valueOf(id));
        }

        var template = templateCheck.get();

        Optional.ofNullable(req.getBody().getAiPromptTemplateName()).ifPresent(template::setAiPromptTemplateName);
        Optional.ofNullable(req.getBody().getAiPromptTemplateContent()).ifPresent(template::setAiPromptTemplateContent);
        Optional.ofNullable(req.getBody().getAiPromptTemplateRemark()).ifPresent(template::setAiPromptTemplateRemark);
        Optional.ofNullable(req.getBody().getAiPromptTemplateEnable()).ifPresent(template::setAiPromptTemplateEnable);

        var record = service.upsert(template);

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), record);
    }

    /**
     * 刪除樣板
     * @return 刪除後的樣板資訊
     */
    @PostMapping("/dgrv4/11/DPB0266/{id}")
    public TsmpBaseResp<DgrAiPromptTemplate> deleteAIPromptTemplate(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<?> req,
            @PathVariable("id") Long id) {
        validateHeader(headers, req);

        var template = service.findPromptTemplate(id);

        if (template.isEmpty()) {
            throw TsmpDpAaRtnCode._1354.throwing("ai template", String.valueOf(id));
        }

        service.delete(template.get());

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), null);
    }

    /**
     * bind template
     * @return template detail
     */
    @PostMapping("/dgrv4/11/DPB0267")
    public TsmpBaseResp<DgrAiPromptTemplateBinding> updateConsumerAIPromptTemplateSetting(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<DPB0267Req> req
    ) {
        validateHeader(headers, req);
        var payload = req.getBody();

        var templateCheck = service.findPromptTemplate(payload.getAiPromptTemplateId());


        if (templateCheck.isEmpty()) {
            throw TsmpDpAaRtnCode._1354.throwing("ai template", String.valueOf(payload.getAiPromptTemplateId()));
        }

        var template = templateCheck.get();

        // 如果找到設定就更新，如果沒找到就創建
        var entity = service.findPromptTemplateBindingByConsumerTypeAndConsumerId(payload.getAiApiKeyConsumerType(), payload.getAiApiKeyConsumerId())
                .orElse(DgrAiPromptTemplateBinding.builder()
                        .id(RandomSeqLongUtil.getRandomLongByDefault())
                        .dgrAiPromptTemplate(template)
                        .aiApikeyConsumerId(payload.getAiApiKeyConsumerId())
                        .aiApikeyConsumerType(payload.getAiApiKeyConsumerType())
                        .build());

        entity.setDgrAiPromptTemplate(template)
                .setAiApikeyConsumerId(payload.getAiApiKeyConsumerId())
                .setAiApikeyConsumerType(payload.getAiApiKeyConsumerType());

        var record = service.upsert(entity);

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), record);
    }

    /**
     * 查詢使用者樣板設定
     * @return 使用者樣板設定清單
     */
    @PostMapping("/dgrv4/11/DPB02671")
    public TsmpBaseResp<Page<DgrAiPromptTemplateBinding>> listConsumerAIPromptTemplateSetting(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<DPB02671> req
    ) {
        validateHeader(headers, req);

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), service.templateBindings(req.getBody().getPagination()));
    }

    /**
     * 刪除使用者樣板設定
     * @return 刪除後的樣板資訊
     */
    @PostMapping("/dgrv4/11/DPB0268/{id}")
    public TsmpBaseResp<?> deleteConsumerAIPromptTemplateSetting(
            @RequestHeader HttpHeaders headers,
            @RequestBody TsmpBaseReq<?> req,
            @PathVariable("id") Long id
    ) {
        validateHeader(headers, req);

        var templateSetting = service.findPromptTemplateSetting(id);

        if (templateSetting.isEmpty()) {
            throw TsmpDpAaRtnCode._1354.throwing("ai template setting", String.valueOf(id));
        }

        service.delete(templateSetting.get());

        return ControllerUtil.tsmpResponseBaseObj(req.getReqHeader(), null);
    }
}
