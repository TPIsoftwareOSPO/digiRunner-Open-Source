package tpi.dgrv4.gateway.controller;

import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.*;

import tpi.dgrv4.dpaa.service.AiGatewayService;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.entity.entity.DgrAiPromptTemplate;

import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.gateway.constant.DgrHeader;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.GtwIdPUserInfoV2Service;

import java.util.List;
import java.util.Optional;

@RestController
public class AiGatewayPromptController {


    @Setter(onMethod_ = @Autowired)
    TsmpTAEASKHelper tsmpTAEASKHelper;

    @Setter(onMethod_ = @Autowired)
    GtwIdPUserInfoV2Service gtwIdPUserInfoV2Service;

    @Setter(onMethod_ = @Autowired)
    AiGatewayService service;

    @Builder
    @Accessors(fluent = true)
    record UsageIdentity(String clientId, String userId, String userEmail, String userName) { }

    @Builder
    @Accessors(fluent = true)
    record CredentialInfo(String consumerType, String consumerId) { }

    @Builder
    @Accessors(fluent = true)
    record AiPromptTemplateInfo(String consumerType, String consumerId, DgrAiPromptTemplate template) {}

    @Builder
    public record AiPromptResponse(List<String> promptResponse) {}

    private UsageIdentity getIdentity(HttpServletRequest rawReq) {
        var builder = UsageIdentity.builder()
                .userEmail("")
                .clientId("")
                .userEmail("")
                .userId("");

        var encClientId = Optional.ofNullable(rawReq.getHeader(DgrHeader.CLIENT_ID)).map(Object::toString).orElse("");
        var encUserName = Optional.ofNullable(rawReq.getHeader(DgrHeader.USER_NAME)).map(Object::toString).orElse("");

        var clientId = encClientId.isEmpty() ? "" :tsmpTAEASKHelper.decrypt(encClientId);
        var userName = encUserName.isEmpty() ? "" : tsmpTAEASKHelper.decrypt(encUserName);

        builder.clientId(clientId).userId(userName);

        var originAuthorization = Optional.ofNullable(rawReq.getHeader(DgrHeader.ORIGIN_AUTHORIZATION)).map(Object::toString).orElse("");

        if (!originAuthorization.isEmpty()) {
            var userInfoResult = gtwIdPUserInfoV2Service.getUserInfoByAuthorization(originAuthorization, rawReq.getRequestURI());

            userInfoResult.success().ifPresent(userInfoEntity -> {
                if (userInfoEntity.getStatusCode().equals(HttpStatusCode.valueOf(200))) {

                    Optional.ofNullable(userInfoEntity.getBody()).ifPresent(body -> {
                        try {
                            var userInfo = JsonParser.parseString(body.toString()).getAsJsonObject();
                            if (userInfo.has("email")) {
                                builder.userEmail(userInfo.get("email").getAsString());
                            }
                            if (userInfo.has("name")) {
                                builder.userName(userInfo.get("name").getAsString());
                            }
                        } catch (Exception e) {
                            TPILogger.tl.error("parse userInfo error:"+e.getMessage());
                        }
                    });
                }
            });
        }


        return  builder.build();
    }

    /**
     * 提問給 AI
     * create API at frontend -> hard coded url: 127.0.0.1:{port}/dgrv4/11/DPB0261?ai_apikey=<selected ai apikey id>
     * @return AI 回答
     */
    @PostMapping("/dgrv4/ai/prompt/{apikeyId}")
    public AiPromptResponse promptToAI(
            @RequestBody String promptInput,
            @PathVariable(name = "apikeyId") Long aiApikeyId,
            HttpServletRequest rawReq
    ) {

        if (!service.isAcceptableRemoteAddress(rawReq.getRemoteAddr())) {
            throw DgrRtnCode._1352.throwing(String.format("[%s] ip not allowed", rawReq.getRemoteAddr()));
        }

        var identity = getIdentity(rawReq);

        if (identity.userEmail.isEmpty() && identity.clientId.isEmpty()) {
            throw DgrRtnCode._1352.throwing("credential not found");
        }

        var apiKeyCheck = service.findApiKey(aiApikeyId);

        if (apiKeyCheck.isEmpty()) {
            throw DgrRtnCode._1352.throwing("ai apikey not found");
        }

        var apiKey = apiKeyCheck.get();

        if (apiKey.getAiApikeyEnable().equals("N")) {
            throw DgrRtnCode._1352.throwing(apiKey.getAiApikeyName() + " disabled");
        }

        var provider = apiKey.getDgrAiProvider();

        if (provider.getAiProviderEnable().equals("N")) {
            throw DgrRtnCode._1352.throwing("provider name:%s, model:%s, id:%s disabled".formatted(provider.getAiProviderName(), provider.getAiModel(), provider.getId()));
        }

        // check api key usage limit
        if (
                (apiKey.getUsageLimitInputToken() > 0 && apiKey.getUsageInputTokenCount() > apiKey.getUsageLimitInputToken()) ||
                        (apiKey.getUsageLimitOutputToken() > 0 && apiKey.getUsageOutputTokenCount() > apiKey.getUsageLimitOutputToken())
        ) {
            var message = """
                    apikey usage limit restrict: %s
                    input token : %d/%d
                    output token : %d/%d
                    """.formatted(
                            apiKey.getAiApikeyName(),
                            apiKey.getUsageInputTokenCount(),
                            apiKey.getUsageLimitInputToken(),
                            apiKey.getUsageOutputTokenCount(),
                            apiKey.getUsageLimitOutputToken());
            if (apiKey.getUsageLimitPolicy().equals("REJECT")) {
                throw DgrRtnCode._1352.throwing(apiKey.getAiApikeyName() + " " + message);
            }
        }

        var promptCredential = StringUtils.isEmpty(identity.userEmail) ?
                CredentialInfo.builder().consumerType("client").consumerId(identity.clientId).build() :
                CredentialInfo.builder().consumerType("user").consumerId(identity.userEmail).build();


        // find prompt template setting
        // search priority: user -> client -> default
        var templateBinding = service.findPromptTemplateBindingByConsumerTypeAndConsumerId("user", identity.userEmail)
                .filter(setting -> setting.getDgrAiPromptTemplate().getAiPromptTemplateEnable().equals("Y"))
                .map(setting -> AiPromptTemplateInfo.builder()
                        .consumerType("user")
                        .consumerId(identity.userEmail)
                        .template(setting.getDgrAiPromptTemplate())
                        .build())
                .or(() -> service.findPromptTemplateBindingByConsumerTypeAndConsumerId("client", identity.clientId)
                        .map(setting-> AiPromptTemplateInfo.builder()
                                .consumerType("client")
                                .consumerId(identity.clientId)
                                .template(setting.getDgrAiPromptTemplate())
                                .build()))
                .orElse(AiPromptTemplateInfo.builder()
                        .consumerId(promptCredential.consumerId)
                        .consumerType(promptCredential.consumerType)
                        .template(service.defaultTemplate())  // user 和 client 都沒有設定樣板則使用預設樣板
                        .build());

        var template = service.defaultTemplate();

        if (templateBinding.template.getAiPromptTemplateEnable().equals("Y")) {
            template = templateBinding.template;
        }

        var aiRequest = AiGatewayService.PromptToAiRequest.builder()
                .apiKey(apiKey)
                .template(template)
                .consumerType(promptCredential.consumerType)
                .consumerId(promptCredential.consumerId)
                .promptContent(promptInput)
                .build();
        var builder = AiPromptResponse.builder();
        try {
            var aiResponse = service.promptToAI(aiRequest);

            if (aiResponse.success().isPresent()) {
                builder.promptResponse(aiResponse.success().get());
            }

            if (aiResponse.failure().isPresent()) {
                throw aiResponse.failure().get();
            }

            return builder.build();

        } catch (Throwable e) {
            builder.promptResponse(List.of(e.getMessage()));
            return builder.build();
        }
    }
}
