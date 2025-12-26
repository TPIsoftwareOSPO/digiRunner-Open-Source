package tpi.dgrv4.gateway.component;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.util.ObjectUtils;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.properties.JasyptEncryptorConfigurationProperties;
import com.ulisesbocchio.jasyptspringboot.util.Singleton;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;


public class CustomEncryptablePropertyResolver implements EncryptablePropertyResolver {

//	@Autowired
//	private TPILogger logger;
	
    private final StringEncryptor stringEncryptor;

    public CustomEncryptablePropertyResolver(StringEncryptor defaultEncryptor, //
    		Singleton<JasyptEncryptorConfigurationProperties> configProps) {
    	StringEncryptor encryptor = null;
    	try {
    		String symmetricKey = configProps.get().getPassword();
    		String keyFormat = configProps.get().getPrivateKeyFormat().name();
    		String privateKeyLocation = configProps.get().getPrivateKeyLocation();
			String privateKeyString = configProps.get().getPrivateKeyString();
			// 有設定 'jasypt.encryptor.password' 就走對稱式
			if (!ObjectUtils.isEmpty(symmetricKey)) {
//				this.logger.debug("Password Base Encryption Configuration detected!");
				TPILogger.tl.debugDelay2sec("Password Base Encryption Configuration detected!");
    			encryptor = defaultEncryptor;
    		// 有設定 'jasypt.encryptor.privateKeyFormat' 就走非對稱式
			} else if (!ObjectUtils.isEmpty(keyFormat)) {
//				this.logger.debug("Asymmetric Encryption Configuration detected!");
				TPILogger.tl.debugDelay2sec("Asymmetric Encryption Configuration detected!");
//				encryptor = new CustomJasyptEncryptor(keyFormat, privateKeyLocation, privateKeyString, logger);
				encryptor = new CustomJasyptEncryptor(keyFormat, privateKeyLocation, privateKeyString);
			} else {
//				this.logger.debug("Either 'jasypt.encryptor.password' or one of ['jasypt.encryptor.private-key-string', 'jasypt.encryptor.private-key-location'] must be provided for Password-based or Asymmetric encryption");
				TPILogger.tl.debugDelay2sec("Either 'jasypt.encryptor.password' or one of ['jasypt.encryptor.private-key-string', 'jasypt.encryptor.private-key-location'] must be provided for Password-based or Asymmetric encryption");
			}
		} catch (Exception e) {
//			logger.debug(StackTraceUtil.logStackTrace(e));
			TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
		}
    	this.stringEncryptor = encryptor;
    }

    @Override
    public String resolvePropertyValue(String value) {
        if (value != null && value.startsWith("ENC(")) {
        	String decrypted = this.stringEncryptor.decrypt(value.substring(value.indexOf("ENC(") + 4, value.lastIndexOf(")")));
        	return decrypted;
        }
        return value;
    }
}