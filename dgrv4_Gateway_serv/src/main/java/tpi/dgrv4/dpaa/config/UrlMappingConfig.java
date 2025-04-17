package tpi.dgrv4.dpaa.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import tpi.dgrv4.gateway.keeper.TPILogger;

@Configuration
public class UrlMappingConfig {

	private TPILogger logger = TPILogger.tl;

	@Bean
	public SimpleUrlHandlerMapping dynamicUrlMapping() {
		logger.info("Initializing dynamicUrlMapping bean");
		return createNewUrlMapping(new HashMap<>());
	}

	public SimpleUrlHandlerMapping createNewUrlMapping(Map<String, Object> urlMap) {
		SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
		int order = Integer.MAX_VALUE - 2;
		mapping.setOrder(order);
		mapping.setUrlMap(urlMap);
		logger.debug(
				String.format("Created new SimpleUrlHandlerMapping with order: %d and URL map: %s", order, urlMap));
		return mapping;
	}
}
