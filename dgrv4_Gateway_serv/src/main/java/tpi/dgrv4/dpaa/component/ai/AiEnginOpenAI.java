package tpi.dgrv4.dpaa.component.ai;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tpi.dgrv4.entity.entity.DgrAiApiKey;
import tpi.dgrv4.entity.entity.DgrAiPromptTemplate;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.model.Attempt;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class AiEnginOpenAI implements AiEngine {

    @Setter(onMethod_ = @Autowired)
    AiEngineShareComponent shareComponent;

    @Override
    public Attempt<AiEngineDTO.Output> generateContent(AiEngineDTO.Input input) {
        DgrAiPromptTemplate template = input.template();
        DgrAiApiKey apiKey = input.apiKey();
        String prompt = input.inputPrompt();

        var body = Map.of(
                "model", apiKey.getDgrAiProvider().getAiModel(),
                "instructions", template.getAiPromptTemplateContent(),
                "input", prompt
        );

        try {
            var builder = AiEngineDTO.Output.builder();
            var rawBody = shareComponent.gson().toJson(body);

            var headers = Map.of(
                    "Authorization", "Bearer " + apiKey.getAiApikeyCode(),
                    "Content-Type", "application/json"
            );

            var resp = HttpUtil.httpReqByRawData(apiKey.getDgrAiProvider().getGenerateApi(), "POST", rawBody, headers, false);

            builder.respData(resp);

            var aiResponse = JsonParser.parseString(resp.respStr).getAsJsonObject();

            if (aiResponse.has("error")) {
                builder.candidates(List.of(aiResponse.toString()));

                return Attempt.failure(new Exception(builder.build().toString()));
            }

            if (aiResponse.has("output") && aiResponse.get("output").isJsonArray()) {
                var outputs = aiResponse.getAsJsonArray("output")
                        .get(0)
                        .getAsJsonObject()
                        .get("content")
                        .getAsJsonArray()
                        .asList()
                        .stream()
                        .map(JsonElement::getAsJsonObject)
                        .map(json -> {
                            if ("output_text".equals(json.get("type").getAsString())) {
                                return json.get("text").getAsString();
                            }
                            return json.getAsString();
                        })
                        .toList();
                builder.candidates(outputs);
            }

            var usage = aiResponse.get("usage").getAsJsonObject();

            var inputTokenCount = usage.get("input_tokens").getAsLong();
            var outputTokenCount = usage.get("output_tokens").getAsLong();

            builder.inputTokenCount(inputTokenCount)
                    .outputTokenCount(outputTokenCount);

            return Attempt.success(builder.build());
        } catch (Exception e) {
            return Attempt.failure(e);
        }
    }
}
