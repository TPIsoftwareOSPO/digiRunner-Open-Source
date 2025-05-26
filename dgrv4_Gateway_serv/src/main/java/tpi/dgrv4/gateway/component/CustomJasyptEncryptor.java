package tpi.dgrv4.gateway.component;

import static tpi.dgrv4.codec.utils.RSAUtils.PRIVATE_KEY;
import static tpi.dgrv4.codec.utils.RSAUtils.RSA_codec;
import static tpi.dgrv4.codec.utils.RSAUtils.getPrivateKey_RSA_PEMfile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Cipher;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.ulisesbocchio.jasyptspringboot.util.AsymmetricCryptography.KeyFormat;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
/**
 * 目前只支援 RSA 非對稱式解密
 * @author Kim
 *
 */
public class CustomJasyptEncryptor implements StringEncryptor {

	private static final String PRIVATE_KEY_HEADER = "-----BEGIN PRIVATE KEY-----";

	private static final String PRIVATE_KEY_FOOTER = "-----END PRIVATE KEY-----";

//	private TPILogger logger;

	private final PrivateKey privateKey;

	public CustomJasyptEncryptor(final String keyFormat, final String privateKeyLocation, //
			final String privateKeyString) {
//		this.logger = logger;
		PrivateKey pk = null;
        try {
        	// #1. 讀出 PrivateKey 內容
            byte[] keyBytes = getPrivateKeyBytes(privateKeyString, privateKeyLocation, keyFormat);
            // #2. 解開 PEM
            if (KeyFormat.PEM.name().equals(keyFormat)) {
                keyBytes = decodePem(keyBytes, PRIVATE_KEY_HEADER, PRIVATE_KEY_FOOTER);
            }
//            this.logger.debug("Read private key " + keyBytes.length + " bytes.");
            TPILogger.tl.debug("Read private key " + keyBytes.length + " bytes.");
            
            // #3. 產生 PrivateKey
            Map<String, Object> keyMap = getPrivateKey_RSA_PEMfile(keyBytes);
            pk = (PrivateKey)keyMap.get(PRIVATE_KEY);
            
//            this.logger.debug("PrivateKey is ready for decoding!");
            TPILogger.tl.debug("PrivateKey is ready for decoding!");
        } catch (Exception e) {
//        	this.logger.error("Unable to generate private key from " + keyFormat + "\n" + StackTraceUtil.logStackTrace(e));
        	TPILogger.tl.warn("Unable to generate private key from " + keyFormat + "\n" + StackTraceUtil.logStackTrace(e));
        } finally {
        	this.privateKey = pk;
        }
	}

	private byte[] getPrivateKeyBytes(String asString, String asLocation, String keyFormat) throws IOException {
		Resource rs = null;

		// 先取 privateKeyString
		if (!ObjectUtils.isEmpty(asString)) {
//			this.logger.debug("Get private key resource from STRING as '" + keyFormat + "' format.");
			TPILogger.tl.debug("Get private key resource from STRING as '" + keyFormat + "' format.");
			if (KeyFormat.DER.name().equals(keyFormat)) {
				rs = new ByteArrayResource(Base64.getDecoder().decode(asString));
			} else {
				rs = new ByteArrayResource(asString.getBytes(StandardCharsets.UTF_8));
			}
		// 沒有設定 privateKeyString 才從 privateKeyLocation 找
		} else if (!ObjectUtils.isEmpty(asLocation)) {
//			this.logger.debug("Get private key resource from LOCATION (" + asLocation + ") as '" + keyFormat + "' format.");
			TPILogger.tl.debug("Get private key resource from LOCATION (" + asLocation + ") as '" + keyFormat + "' format.");
			rs = new DefaultResourceLoader().getResource(asLocation);
		} else {
			throw new IllegalArgumentException("Unable to load key. Either resource, key as string, or resource location must be provided");
		}
		
        return FileCopyUtils.copyToByteArray(rs.getInputStream());
    }

	private byte[] decodePem(byte[] bytes, String... headers) {
        String pem = new String(bytes, StandardCharsets.UTF_8);
        // 移除檔案中的 header 跟 footer
        for (String header : headers) {
            pem = pem.replace(header, "");
        }
        byte[] decodedPem = Base64.getMimeDecoder().decode(pem);
//        this.logger.debug("PEM content is decoded.");
        TPILogger.tl.debug("PEM content is decoded.");
        return decodedPem;
    }

	@Override
	public String encrypt(String message) {
//		this.logger.error("This class is only used to decrypt.");
		TPILogger.tl.error("This class is only used to decrypt.");
		return null;
	}

	@Override
	public String decrypt(String encryptedMessage) {
		// #1. 先解開 Base64 字串
		byte[] encryptedBytes = Base64Util.base64Decode(encryptedMessage);
		// #2. 用 privateKey 解密
		byte[] decryptedBytes = decrypt(encryptedBytes);
		// #3. 轉成 UTF-8 字串
		String decryptedMessage = new String(decryptedBytes, StandardCharsets.UTF_8);
		if (StringUtils.hasLength(decryptedMessage)) {
//			this.logger.debug("Decrypt string successfully: \"" + encryptedMessage + "\"");
			TPILogger.tl.debug("Decrypt string successfully: \"" + encryptedMessage + "\"");
		}
		return decryptedMessage;
	}

	private byte[] decrypt(byte[] encrypted) {
		try {
			return RSA_codec(encrypted, privateKey, Cipher.DECRYPT_MODE);
		} catch (Exception e) {
//			this.logger.error("Decrypt error!\n" + StackTraceUtil.logStackTrace(e));
			TPILogger.tl.error("Decrypt error!\n" + StackTraceUtil.logStackTrace(e));
		}
		return new byte[0];
	}

}