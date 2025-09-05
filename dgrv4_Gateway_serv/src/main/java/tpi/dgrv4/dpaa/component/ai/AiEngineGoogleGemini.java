package tpi.dgrv4.dpaa.component.ai;

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

@Component
public class AiEngineGoogleGemini implements AiEngine {


    @Setter(onMethod_ = @Autowired)
    AiEngineShareComponent shareComponent;



    @Override
    public Attempt<AiEngineDTO.Output> generateContent(AiEngineDTO.Input input) {

        DgrAiPromptTemplate template = input.template();
        DgrAiApiKey apiKey = input.apiKey();
        String prompt = input.inputPrompt();

        var url = "%s?key=%s".formatted(
                apiKey.getDgrAiProvider().getGenerateApi(),
                apiKey.getAiApikeyCode()
        );

        var body = new HashMap<>();

        var textPrompt = Map.of("text", prompt);

        if (Optional.ofNullable(template).isPresent()) {
            var instructionParts = Map.of("parts", Map.of("text",template.getAiPromptTemplateContent()));
            var contentsParts = Map.of("parts", textPrompt);
            body.put("system_instruction", instructionParts);
            body.put("contents", contentsParts);
        } else {
            var contentsParts = List.of(Map.of("parts", List.of(textPrompt)));
            body.put("contents", contentsParts);
        }

        try {
            var builder = AiEngineDTO.Output.builder();
            var rawBody = shareComponent.gson().toJson(body);
            var resp = HttpUtil.httpReqByRawData(url, "POST", rawBody, Map.of("Content-Type", "application/json"), false);
            builder.respData(resp);
            var aiResponse = JsonParser.parseString(resp.respStr).getAsJsonObject();

            if (aiResponse.has("error") && aiResponse.get("error").isJsonObject()) {
                var error = aiResponse.getAsJsonObject("error");
                return Attempt.failure(new Exception(error.toString()));
            }

            if (aiResponse.has("candidates") && aiResponse.get("candidates").isJsonArray()) {
                var candidates = aiResponse.getAsJsonArray("candidates")
                        .get(0)
                        .getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .asList()
                        .stream()
                        .map(part -> part.getAsJsonObject().get("text").getAsString())
                        .toList();
                builder.candidates(candidates);
            }

            var usageMetadata = aiResponse.get("usageMetadata")
                    .getAsJsonObject();

            var inputTokenCount = usageMetadata.get("promptTokenCount").getAsLong();
            var outputTokenCount = usageMetadata.get("candidatesTokenCount").getAsLong();
            builder.inputTokenCount(inputTokenCount)
                    .outputTokenCount(outputTokenCount);

            return Attempt.success(builder.build());
        } catch (Exception e) {
            return Attempt.failure(e);
        }


    }

}
