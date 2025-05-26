package tpi.dgrv4.dpaa.config;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * <strong>2022-09-07</strong><br/>
 * 暫時將 @Configuration 註解，是因為如果打開，會造成 {@link tpi.dgrv4.gateway.config.BeanConfig} 無法 @Autowired
 * {@link com.ulisesbocchio.jasyptspringboot.properties.JasyptEncryptorConfigurationProperties}
 * @author Kim
 */
//@Configuration
public class SwaggerConfig {

	@Bean
	GroupedOpenApi api() {
		return GroupedOpenApi.builder() //
			.packagesToScan("tpi.dgrv4.dpaa.controller") //
			.group("dgrv4") //
			.build();
	}

	@Bean
	public OpenAPI springShopOpenAPI() {
		return new OpenAPI()
			.info( //
				new Info() //
				.title("dgR Developer Portal Admin API") //
				.description("dgR Developer Portal Admin API") //
				.version("v4")
				.license( //
					new License() //
					.name("Apache 2.0") //
					.url("https://www.apache.org/licenses/LICENSE-2.0.txt") //
				) //
			) //
			;
	}
	
}
