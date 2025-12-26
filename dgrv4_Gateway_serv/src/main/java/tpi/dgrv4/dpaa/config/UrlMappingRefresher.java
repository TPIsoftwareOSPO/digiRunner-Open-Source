package tpi.dgrv4.dpaa.config;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpSettingService;

@Service
public class UrlMappingRefresher {

	private final TsmpSettingService tsmpSettingService;
	private final SimpleUrlHandlerMapping dynamicUrlMapping;
	private final ReentrantLock lock = new ReentrantLock();

	public UrlMappingRefresher(TsmpSettingService tsmpSettingService,
			@Qualifier("dynamicUrlMapping") SimpleUrlHandlerMapping dynamicUrlMapping) {
		this.tsmpSettingService = tsmpSettingService;
		this.dynamicUrlMapping = dynamicUrlMapping;
		TPILogger.tl.debug(
				String.format("UrlMappingRefresher initialized with tsmpSettingService: %s and dynamicUrlMapping: %s",
						tsmpSettingService, dynamicUrlMapping));
	}

	public void refreshUrlMappings() {
		TPILogger.tl.info("Starting to refresh URL mappings");
		lock.lock();
		TPILogger.tl.debug("Lock acquired for URL mapping refresh");

		try {
			String loginUrl = tsmpSettingService.getVal_DGR_AC_LOGIN_PAGE();
			TPILogger.tl.debug("Retrieved login URL: " + loginUrl);

			Map<String, Object> urlMap = createUrlMappings(loginUrl);
			TPILogger.tl.debug("Created URL mappings: " + urlMap);

			Class<?> abstractUrlHandlerMappingClass = Class
					.forName("org.springframework.web.servlet.handler.AbstractUrlHandlerMapping");

			Field handlerMapField = abstractUrlHandlerMappingClass.getDeclaredField("handlerMap");
			Field pathPatternHandlerMapField = abstractUrlHandlerMappingClass.getDeclaredField("pathPatternHandlerMap");

			// 設置字段可訪問
			handlerMapField.setAccessible(true);
			pathPatternHandlerMapField.setAccessible(true);
			// 將 handlerMap 設置為空 Map
			handlerMapField.set(dynamicUrlMapping, new LinkedHashMap<>());
			pathPatternHandlerMapField.set(dynamicUrlMapping, new LinkedHashMap<>());

			dynamicUrlMapping.getUrlMap().clear();
			dynamicUrlMapping.setUrlMap(urlMap);
			dynamicUrlMapping.initApplicationContext();

			TPILogger.tl.info("URL mappings refreshed successfully. New login URL: " + loginUrl);
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		} finally {
			lock.unlock();
			TPILogger.tl.debug("Lock released after URL mapping refresh");
		}
	}

	private Map<String, Object> createUrlMappings(String url) {
		TPILogger.tl.debug("Creating URL mappings for login URL: " + url);
		Map<String, Object> urlMap = new HashMap<>();

		addViewController(urlMap, "/dgrv4/ac4", url);
		addViewController(urlMap, "/dgrv4/ac4/", url);
		addViewController(urlMap, "/dgrv4/", url);
		addViewController(urlMap, "/dgrv4", url);

		if (url.indexOf("redirect:/dgrv4/ac4/login") == -1) {
			TPILogger.tl.debug("Adding /dgrv4/ac4/login mappings");
			addViewController(urlMap, "/dgrv4/ac4/login", url);
			addViewController(urlMap, "/dgrv4/ac4/login/", url);
		}
		if (url.indexOf("redirect:/dgrv4/ac4/login2") == -1) {
			TPILogger.tl.debug("Adding /dgrv4/ac4/login2 mappings");
			addViewController(urlMap, "/dgrv4/ac4/login2", url);
			addViewController(urlMap, "/dgrv4/ac4/login2/", url);
		}
		if (url.indexOf("redirect:/dgrv4/ac4/index.html") == -1) {
			TPILogger.tl.debug("Adding /dgrv4/ac4/index.html mapping");
			addViewController(urlMap, "/dgrv4/ac4/index.html", url);
		}

		TPILogger.tl.debug("Finished creating URL mappings: " + urlMap);
		return urlMap;
	}

	private void addViewController(Map<String, Object> urlMap, String path, String viewName) {
		TPILogger.tl.trace(String.format("Adding view controller for path: %s with view name: %s", path, viewName));
		ParameterizableViewController controller = new ParameterizableViewController();
		controller.setViewName(viewName);
		urlMap.put(path, controller);
	}
}
