package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import lombok.Builder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiGatewayTableInitializer {

    @Builder
    public record AiEngineProvider(
            Long id,
            String name,
            String alias,
            String model,
            String generateAPI,
            String countTokenAPI
    ) {}

    @Builder
    public record AiPromptTemplate(
            Long id,
            String name,
            String content,
            String remark
    ) {}

    public List<AiEngineProvider> defaultProviders() {
        var google = AiEngineProvider.builder()
                .name("Google");
        var gemini_2_5_FlashLite = google
                .id(1L)
                .alias("Gemini 2.5 Flash Lite")
                .model("gemini-2.5-flash-lite")
                .generateAPI("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent")
                .countTokenAPI("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:countTokens")
                .build();

        var gemini_2_5_Flash= google
                .id(2L)
                .alias("Gemini 2.5 Flash")
                .model("gemini-2.5-flash")
                .generateAPI("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent")
                .countTokenAPI("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:countTokens")
                .build();

        var openai = AiEngineProvider.builder()
                .name("OpenAI");

        var gpt_4oMini = openai
                .id(3L)
                .alias("OpenAI GPT 4o Mini")
                .model("gpt-4o-mini")
                .generateAPI("https://api.openai.com/v1/responses")
                .countTokenAPI("#")
                .build();

        var gpt_o1Mini = openai
                .id(4L)
                .alias("OpenAI GPT o1 Mini")
                .model("o1-mini")
                .generateAPI("https://api.openai.com/v1/responses")
                .countTokenAPI("#")
                .build();
        return List.of(
                gemini_2_5_FlashLite
                ,gemini_2_5_Flash
//                ,gpt_4oMini
//                ,gpt_o1Mini
        );
    }

    public List<AiPromptTemplate> defaultTemplates() {
        var defaultTemplate = AiPromptTemplate.builder()
                .id(1L)
                .name("Default")
                .content("")
                .remark("ai prompt default template")
                .build();
        return List.of(defaultTemplate);
    }
}
