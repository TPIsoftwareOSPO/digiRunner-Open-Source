package tpi.dgrv4.gateway.component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ObjectUtils;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.dpaa.service.TsmpSettingService;

/**
 * 映射 application.properties 檔案內容為物件
 * @author Kim
 */
@Getter(AccessLevel.PROTECTED)
@Configuration
@ConfigurationProperties(ignoreUnknownFields = true)
public class ServiceConfig {

	public final static String KEY_PAGE_SIZE = ".page.size";
		
	private TPILogger logger = TPILogger.tl;
	
	private TsmpSettingService tsmpSettingService;

	private Map<String, String> service = new HashMap<String, String>();

    @Autowired
    public void setTsmpSettingService(TsmpSettingService tsmpSettingService) {
    	// Because using constructor injection will cause a circular dependency, use method injection instead
        this.tsmpSettingService = tsmpSettingService;
    }
	
	@PostConstruct
	public void init() {
		StringBuffer msg = new StringBuffer();
		msg.append("\n以service.為開頭的設定有: {\n");
		service.forEach((key, value) -> {
			msg.append(String.format("\t%s=%s\n", key, value));
		});
		msg.append("}");
		this.logger.debug(msg.toString());
	}

	public final String get(String key) {
		return get(key, StandardCharsets.ISO_8859_1);
	}

	public final String get(String key, Charset charset) {
		String value = service.get(key);
		if (ObjectUtils.isEmpty(value)) {
			return value;
		}
		if (charset != null && !StandardCharsets.ISO_8859_1.equals(charset)) {
			value = new String(value.getBytes(StandardCharsets.ISO_8859_1), charset);
		}
		this.logger.trace("get property: " + key + "=" + value);

		return value;
	}

	/**
	 * 取得 service.&lt;serviceName&gt;.page.size
	 * @param serviceName
	 * @return
	 * @throws NumberFormatException
	 */
	public final Integer getPageSize(String serviceName) throws NumberFormatException {
		// 改從 Setting 裡面取值。
		return getDefaultPageSize();
//		String key = serviceName + KEY_PAGE_SIZE;
//		String value = get(key);
//		if (value == null || value.isEmpty()) {
//			return getDefaultPageSize();
//		} else {
//			return Integer.valueOf(value);
//		}
	}
	
	public final Integer getDefaultPageSize() {
		return tsmpSettingService.getVal_DEFAULT_PAGE_SIZE();
	}

	public Map<String, String> getService() {
		return service;
	}

	public void setService(Map<String, String> service) {
		this.service = service;
	}
}
