package tpi.dgrv4.gateway.config;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.DisallowedMethodsHandler;
import io.undertow.util.HttpString;
import jakarta.validation.Valid;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DisallowedMethodsHandlerFactory implements WebServerFactoryCustomizer<UndertowServletWebServerFactory> {

    @Value("${undertow.disallow-methods:true}")
    @Getter
    private boolean disallowMethods;

    @Override
    public void customize(UndertowServletWebServerFactory factory) {
        if (isDisallowMethods()) {
            factory.addDeploymentInfoCustomizers(deploymentInfo -> {
                deploymentInfo.addInitialHandlerChainWrapper(new HandlerWrapper() {
                    @Override
                    public HttpHandler wrap(HttpHandler handler) {
                        HttpString[] disallowedHttpMethods = {HttpString.tryFromString("TRACE"),
                                HttpString.tryFromString("TRACK")};
                        return new DisallowedMethodsHandler(handler, disallowedHttpMethods);
                    }
                });
            });
        }
    }
}
