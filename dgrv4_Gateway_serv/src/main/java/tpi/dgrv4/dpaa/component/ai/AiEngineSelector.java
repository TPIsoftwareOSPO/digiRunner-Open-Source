package tpi.dgrv4.dpaa.component.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@AllArgsConstructor
@Component
public class AiEngineSelector {

    private AiEngineGoogleGemini googleGemini;

    private AiEngineDgrAiComposer dgrAiComposer;

    private AiEnginOpenAI openAI;

    @Accessors(fluent = true)
    @Getter
    public enum ProviderName {
        GOOGLE("Google"), COMPOSER("Composer"), OPENAI("OpenAI")
        ;
        private final String brand;
        ProviderName(String name) {
            this.brand = name;
        }
    }

    public Optional<AiEngine> select(String providerName, String aiModel) {
        return switch (providerName.toLowerCase()) {
            case "google" -> Optional.of(googleGemini);
            case "composer" -> Optional.of(dgrAiComposer);
            case "openai" -> Optional.of(openAI);
            default -> Optional.empty();
        };
    }
}
