package tpi.dgrv4.dpaa.component.ai;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Getter
@Accessors(fluent = true)
@Component
public class AiEngineShareComponent {

    private Gson gson;

    @PostConstruct
    public void init() {
        var builder = new GsonBuilder();
        builder.setPrettyPrinting();
        gson = builder.create();
    }
}
