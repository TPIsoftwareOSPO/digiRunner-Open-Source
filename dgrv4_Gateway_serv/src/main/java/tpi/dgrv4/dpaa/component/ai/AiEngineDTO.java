package tpi.dgrv4.dpaa.component.ai;

import lombok.Builder;
import tpi.dgrv4.entity.entity.DgrAiApiKey;
import tpi.dgrv4.entity.entity.DgrAiPromptTemplate;
import tpi.dgrv4.httpu.utils.HttpUtil;

import java.util.List;

public sealed interface AiEngineDTO permits AiEngineDTO.Input, AiEngineDTO.Output {

    @Builder
    record Input(DgrAiApiKey apiKey, DgrAiPromptTemplate template, String inputPrompt) implements AiEngineDTO {}

    @Builder
    record Output(HttpUtil.HttpRespData respData, List<String> candidates, Long inputTokenCount, Long outputTokenCount) implements AiEngineDTO {}
}
