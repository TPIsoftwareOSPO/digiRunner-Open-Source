package tpi.dgrv4.dpaa.component.ai;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tpi.dgrv4.entity.entity.DgrAiApiKey;
import tpi.dgrv4.entity.entity.DgrAiPromptTemplate;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.model.Attempt;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AiEngineDgrAiComposer Protocol
 * <br>
 * request method: POST
 * <br>
 * request body: (send to composer api)
 * <pre>
 * {
 *   "promptContent":"user input prompt content",
 *   "template":"prompt template content",
 *   "apikey":"ai apikey use for generate ai response"
 * }
 * </pre>
 * response body: (expect receive from composer api)
 * <pre>
 * {
 *   "promptResponse":
 *   [
 *       "response candidate 1",
 *       "response candidate 2"
 *   ],
 *   "promptInputTokenCount": 0L,
 *   "promptOutputTokenCount": 0L
 * }
 * </pre>
 */

@Component
public class AiEngineDgrAiComposer implements AiEngine {

    @Setter(onMethod_ = @Autowired)
    AiEngineShareComponent shareComponent;

    @Override
    public Attempt<AiEngineDTO.Output> generateContent(AiEngineDTO.Input input) {

        DgrAiPromptTemplate template = input.template();
        DgrAiApiKey apiKey = input.apiKey();
        String prompt = input.inputPrompt();

        HashMap<Object, Object> payload = new HashMap<>();
        payload.put("promptContent", prompt);
        payload.put("apikey", apiKey.getAiApikeyCode());

        if (Optional.ofNullable(template).isPresent()) {
            payload.put("template", template.getAiPromptTemplateContent());
        }

        try {
            var builder = AiEngineDTO.Output.builder();
            var rawBody = shareComponent.gson().toJson(payload);

            var resp = HttpUtil.httpReqByRawData(apiKey.getDgrAiProvider().getGenerateApi(), "POST", rawBody, Map.of("Content-Type", "application/json"), false);
            builder.respData(resp);
            var aiResponse = JsonParser.parseString(resp.respStr).getAsJsonObject();

            if (aiResponse.has("promptResponse") && aiResponse.get("promptResponse").isJsonArray()) {
                var candidates = aiResponse.get("promptResponse")
                        .getAsJsonArray()
                        .asList()
                        .stream()
                        .map(JsonElement::getAsString)
                        .toList();
                builder.candidates(candidates);
            }

            var inputTokenCount = aiResponse.get("promptInputTokenCount").getAsLong();
            var outputTokenCount = aiResponse.get("promptOutputTokenCount").getAsLong();
            builder.inputTokenCount(inputTokenCount)
                    .outputTokenCount(outputTokenCount);


            return Attempt.success(builder.build());
        } catch (Exception e) {
            return Attempt.failure(e);
        }

    }
}
