package tpi.dgrv4.dpaa.component.ai;

import tpi.dgrv4.model.Attempt;


public interface AiEngine {

    Attempt<AiEngineDTO.Output> generateContent(AiEngineDTO.Input input);

}
