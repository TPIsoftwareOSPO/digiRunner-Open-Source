package tpi.dgrv4.gateway.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import tpi.dgrv4.gateway.service.TsmpSettingService;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	
	private TsmpSettingService tsmpSettingService;
	
	@Autowired
	public WebMvcConfig(TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpSettingService = tsmpSettingService;
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
//		String url = "redirect:/dgrv4/ac4/login"; // 預設頁, 它可以跳到任何登入頁 ex: /ldap , /login2
//		String url = "redirect:/dgrv4/ac4/ldap"; // 按了登出最終要回到 /ldap
//		String url = "redirect:/dgrv4/ac4/login2"; // 測試用 預設的登入頁
		
		String url = tsmpSettingService.getVal_DGR_AC_LOGIN_PAGE();
		registry.addViewController("/dgrv4/ac4").setViewName(url);
		registry.addViewController("/dgrv4/ac4/").setViewName(url);
		
		registry.addViewController("/dgrv4/").setViewName(url);
		registry.addViewController("/dgrv4").setViewName(url);

		if (url.indexOf("redirect:/dgrv4/ac4/login") == -1) { //若是 url 沒有設首頁才會生效, 以免無窮 loop
			registry.addViewController("/dgrv4/ac4/login").setViewName(url);
			registry.addViewController("/dgrv4/ac4/login/").setViewName(url);
		}
		if (url.indexOf("redirect:/dgrv4/ac4/login2") == -1) { //若是 url 沒有設首頁才會生效, 以免無窮 loop
			registry.addViewController("/dgrv4/ac4/login2").setViewName(url);
			registry.addViewController("/dgrv4/ac4/login2/").setViewName(url);
		}
		if (url.indexOf("redirect:/dgrv4/ac4/index.html") == -1) { //若是 url 沒有設首頁才會生效, 以免無窮 loop
			registry.addViewController("/dgrv4/ac4/index.html").setViewName(url);
		}
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/**").addResourceLocations("classpath:/static/").resourceChain(true)
				.addResolver(new PathResourceResolver() {
					@Override
					protected Resource getResource(String resourcePath, Resource location) throws IOException {
						Resource requestedResource = location.createRelative(resourcePath);
						return requestedResource.exists() && requestedResource.isReadable() ? requestedResource
								: new ClassPathResource("/static/dgrv4/ac4/index.html");
					}
				});
	}
}
