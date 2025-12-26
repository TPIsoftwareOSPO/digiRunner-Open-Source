package tpi.dgrv4.dpaa.component.ai;

import com.google.gson.*;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tpi.dgrv4.entity.entity.DgrAiApiKey;
import tpi.dgrv4.entity.entity.DgrAiPromptTemplate;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.model.Attempt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AiEnginOpenAI implements AiEngine {

    @Override
    public Attempt<AiEngineDTO.Output> generateContent(AiEngineDTO.Input input) {
        DgrAiPromptTemplate template = input.template();
        DgrAiApiKey apiKey = input.apiKey();
        String prompt = input.inputPrompt();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        var body = Map.of(
                "model", apiKey.getDgrAiProvider().getAiModel(),
                "instructions", template.getAiPromptTemplateContent(),
                "input", prompt
        );

        try {
            var builder = AiEngineDTO.Output.builder();
            var rawBody = gson.toJson(body);

            var headers = Map.of(
                    "Authorization", "Bearer " + apiKey.getAiApikeyCode(),
                    "Content-Type", "application/json"
            );

            var resp = HttpUtil.httpReqByRawData(apiKey.getDgrAiProvider().getGenerateApi(), "POST", rawBody, headers, false);

            builder.respData(resp);

            var aiResponse = JsonParser.parseString(resp.respStr).getAsJsonObject();

            if (aiResponse.has("error") && !aiResponse.get("error").isJsonNull()) {
                builder.candidates(List.of(aiResponse.toString()));

                return Attempt.failure(new Exception(builder.build().toString()));
            }

            if (aiResponse.has("output") && aiResponse.get("output").isJsonArray()) {
                var outputs = aiResponse.getAsJsonArray("output")
                        .asList()
                        .stream()
                        .map(JsonElement::getAsJsonObject)
                        .filter(ele -> ele.has("content"))
                        .filter(json -> json.get("content").isJsonArray())
                        .map( json -> json.getAsJsonArray("content"))
                        .map(contents -> {
                            var candidates = new ArrayList<String>();
                            contents.forEach(content -> {
                                var json =  content.getAsJsonObject();
                                if (json.has("type")
                                        && "output_text".equalsIgnoreCase(json.get("type").getAsString())
                                        && json.has("text")
                                ) {
                                    candidates.add(json.get("text").getAsString());
                                }
                            });
                            return candidates;
                        })
                        .flatMap(List::stream)
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
