package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import lombok.Builder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AiGatewayTableInitializer {

    private AtomicLong idGenerator = new AtomicLong(1);

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
        var gemini_2_5_flash_lite = google
                .id(idGenerator.getAndIncrement())
                .alias("Gemini 2.5 Flash Lite")
                .model("gemini-2.5-flash-lite")
                .generateAPI("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent")
                .countTokenAPI("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:countTokens")
                .build();

        var gemini_2_5_flash= google
                .id(idGenerator.getAndIncrement())
                .alias("Gemini 2.5 Flash")
                .model("gemini-2.5-flash")
                .generateAPI("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent")
                .countTokenAPI("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:countTokens")
                .build();

        var gemini_2_5_pro= google
                .id(idGenerator.getAndIncrement())
                .alias("Gemini 2.5 Pro")
                .model("gemini-2.5-pro")
                .generateAPI("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent")
                .countTokenAPI("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:countTokens")
                .build();

        var openai = AiEngineProvider.builder()
                .name("OpenAI");

        var gpt_4_1 = openai
                .id(idGenerator.getAndIncrement())
                .alias("OpenAI GPT-4.1")
                .model("gpt-4.1")
                .generateAPI("https://api.openai.com/v1/responses")
                .countTokenAPI("#")
                .build();

        var gpt_5 = openai
                .id(idGenerator.getAndIncrement())
                .alias("OpenAI GPT-5")
                .model("gpt-5")
                .generateAPI("https://api.openai.com/v1/responses")
                .countTokenAPI("#")
                .build();

        var gpt_5_pro = openai
                .id(idGenerator.getAndIncrement())
                .alias("OpenAI GPT-5 pro")
                .model("gpt-5-pro")
                .generateAPI("https://api.openai.com/v1/responses")
                .countTokenAPI("#")
                .build();

        var gpt_5_mini = openai
                .id(idGenerator.getAndIncrement())
                .alias("OpenAI GPT-5 mini")
                .model("gpt-5-mini")
                .generateAPI("https://api.openai.com/v1/responses")
                .countTokenAPI("#")
                .build();

        var gpt_5_nano = openai
                .id(idGenerator.getAndIncrement())
                .alias("OpenAI GPT-5 nano")
                .model("gpt-5-nano")
                .generateAPI("https://api.openai.com/v1/responses")
                .countTokenAPI("#")
                .build();

        return List.of(
                gemini_2_5_flash_lite
                ,gemini_2_5_flash
                ,gemini_2_5_pro
                ,gpt_4_1
                ,gpt_5
                ,gpt_5_pro
                ,gpt_5_mini
                ,gpt_5_nano
        );
    }

    public List<AiPromptTemplate> defaultTemplates() {
        var defaultTemplate = AiPromptTemplate.builder()
                .id(1L)
                .name("Default")
                .content(" ")
                .remark("ai prompt default template")
                .build();
        return List.of(defaultTemplate);
    }
}
