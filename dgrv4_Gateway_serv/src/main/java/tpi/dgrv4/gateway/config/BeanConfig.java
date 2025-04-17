package tpi.dgrv4.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.encryptor.DefaultLazyEncryptor;
import com.ulisesbocchio.jasyptspringboot.properties.JasyptEncryptorConfigurationProperties;
import com.ulisesbocchio.jasyptspringboot.util.Singleton;

import tpi.dgrv4.gateway.component.CustomEncryptablePropertyResolver;

@Configuration
@ComponentScan({"tpi.dgrv4.common.utils"})	// Online Console
public class BeanConfig {

	private DefaultLazyEncryptor defaultEncryptor;

	/**
	 * 關於 jasypt 的設定值 (from application-*.properties)
	 */
	private Singleton<JasyptEncryptorConfigurationProperties> configPropsSingleton;

	@Autowired
	public BeanConfig(DefaultLazyEncryptor defaultEncryptor,
			Singleton<JasyptEncryptorConfigurationProperties> configPropsSingleton) {
		super();
		this.defaultEncryptor = defaultEncryptor;
		this.configPropsSingleton = configPropsSingleton;
	}

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		// 關閉序列化空物件時的例外錯誤(允許序列化空物件)
		return objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	/**
	 * 一旦註冊了這個名稱的 Bean，則不論對稱式或非對稱式，一律都交由自訂的 Bean 處理加解密
	 * @return
	 */
	@Bean(name="encryptablePropertyResolver")
    public EncryptablePropertyResolver encryptablePropertyResolver() {
        return new CustomEncryptablePropertyResolver(defaultEncryptor, configPropsSingleton);
    }

}
