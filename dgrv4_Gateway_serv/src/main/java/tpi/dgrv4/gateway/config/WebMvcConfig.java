package tpi.dgrv4.gateway.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
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
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(-1);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        String url = tsmpSettingService.getVal_DGR_AC_LOGIN_PAGE();
    
        // 首頁 / 根目錄：跳轉到設定的登入頁 (例如 redirect:/dgrv4/ac4/login)
        registry.addViewController("/dgrv4/ac4").setViewName(url);
        registry.addViewController("/dgrv4/ac4/").setViewName(url);
        registry.addViewController("/dgrv4/").setViewName(url);
        registry.addViewController("/dgrv4").setViewName(url);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 核心設定：專門針對 /dgrv4/ac4/ 及其子路徑進行 SPA Fallback 處理
        registry.addResourceHandler("/dgrv4/ac4/**")
                .addResourceLocations("classpath:/static/dgrv4/ac4/", "classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        
                        // 1. 若該請求能找到實體靜態檔案 (例如 main.js, styles.css, logo.png) 直接回應
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        
                        // 2. 若找不到檔案，且請求不帶有副檔名（表示是前端 JS 路由如 /dashboard, /user/setting）
                        // 才回傳 index.html 交給 SPA 前端處理
                        if (!resourcePath.contains(".")) {
                            return new ClassPathResource("/static/dgrv4/ac4/index.html");
                        }

                        // 3. 如果請求帶有副檔名（如 /dgrv4/ac4/test.php 或不存在的 /dgrv4/ac4/missing.js）
                        // 代表這原本應該要是個檔案卻不存在，回傳 null 讓 Spring 正確輸出 HTTP 404！
                        return null;
                    }
                });

        // 其餘非 /dgrv4/ac4/ 的靜態資源路徑，走預設處理（找不到檔案直接 404，不進行 fallback）
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}