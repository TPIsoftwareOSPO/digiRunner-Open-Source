package tpi.dgrv4.entity.component.cipher;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.AESUtils;
import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.HexStringUtils;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.keeper.ITPILogger;
import tpi.dgrv4.common.utils.StackTraceUtil;

/**
 * 對稱式加解密工具
 */
@Component
public class TsmpTAEASKHelper {

	private final int keySize = 128;

	private final Charset defaultCharset = StandardCharsets.UTF_8;

	private final String algorithm = "AES";

	private String secureKey;

	private Cipher encryptCipher;

	private Cipher decryptCipher;
	
	@PostConstruct
	public void init() throws Exception {
		ITPILogger.tl.debugDelay2sec("=== Begin TAEASK initialization ===");

		loadKey();

		/*
		 * 2022.05.10 改用 AESUtils 就不用初始化 Cipher 了 KeyGenerator kgen =
		 * KeyGenerator.getInstance(this.algorithm); SecureRandom secureRandom =
		 * SecureRandom.getInstance("SHA1PRNG");
		 * secureRandom.setSeed(getSecureKey().getBytes()); kgen.init(this.keySize,
		 * secureRandom);
		 * 
		 * SecretKey secretKey = kgen.generateKey(); byte[] enCodeFormat =
		 * secretKey.getEncoded(); SecretKeySpec key = new SecretKeySpec(enCodeFormat,
		 * this.algorithm);
		 * 
		 * this.encryptCipher = Cipher.getInstance(this.algorithm);
		 * this.encryptCipher.init(Cipher.ENCRYPT_MODE, key);
		 * 
		 * this.decryptCipher = Cipher.getInstance(this.algorithm);
		 * this.decryptCipher.init(Cipher.DECRYPT_MODE, key);
		 */

		ITPILogger.tl.debugDelay2sec("=== TAEASK initialize successfully! ===");
	}

	private void loadKey() throws Exception {
		String key = null;

		// 需要交付組在啟動v3的shell或bat中設定"TAEASK"的值，這邊才會抓得到
		try {
			key = System.getenv("TAEASK");
			if (!StringUtils.hasText(key)) {
				String errMsg = "\n\t...Could not find TAEASK Key\n";
				errMsg += "\t...Please add Eclipse 'Run as / Configurations... / Enviroment / Variable:TAEASK'\n";
				ITPILogger.tl.error(errMsg);
				System.err.println("=============================================");
				System.err.println(errMsg);
				System.err.println("=============================================");
			}
		} catch (Exception e) {
			ITPILogger.tl.error("Unable to load TAEASK Key\n" + StackTraceUtil.logStackTrace(e));
		}

		// 如果找不到 secureKey 就自己產生一組
		if (!StringUtils.hasText(key)) {
			try {
				key = genKey();
				ITPILogger.tl.warn("Generating TAEASK Key: " + key);
				System.err.println("=============================================");
				System.err.println("Generating TAEASK Key: " + key);
				System.err.println("=============================================");
			} catch (Exception e) {
				ITPILogger.tl.error("Error generating TAEASK Key\n" + StackTraceUtil.logStackTrace(e));
			}
		}

		if (!StringUtils.hasText(key)) {
			throw new Exception("Error loading TAEASK Key");
		}

		this.secureKey = key;

		ITPILogger.tl.debugDelay2sec("TAEASK Key is loaded");
	}

	private String genKey() {
		long receptor = Instant.now().getEpochSecond();
		int radix = Character.MAX_RADIX;
		return Long.toString(receptor, radix);
	}

	public String encrypt(String content) {
		String code = encryptByAESUtils(content);
		return code;
	}

	public String decrypt(String content) {
		String decryptContent = decryptByAESUtils(content);
		return decryptContent;
	}

	public String encryptByAESUtils(String content) {
		try {
			byte[] seed = getSecureKey().getBytes();

			String enc = AESUtils.AesCipher(content, Cipher.ENCRYPT_MODE, this.keySize, this.algorithm, null, seed);

			// Base64 to Hex
			enc = HexStringUtils.toString(Base64Util.base64Decode(enc));

			return enc;
		} catch (Exception e) {
			ITPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	public String decryptByAESUtils(String content) {
		try {
			// Hex to Base64
			content = Base64Util.base64Encode(HexStringUtils.toBytes(content));

			byte[] seed = getSecureKey().getBytes();

			String dec = AESUtils.AesCipher(content, Cipher.DECRYPT_MODE, this.keySize, this.algorithm, null, seed);

			return dec;
		} catch (Exception e) {// AES 解密錯誤
			StringBuilder sb = new StringBuilder();
			sb.append("...AES decryption error. \n");
			sb.append(StackTraceUtil.logStackTrace(e));
			ITPILogger.tl.error(sb.toString());
			throw TsmpDpAaRtnCode._1562.throwing("AES");
		}
	}

	protected String getSecureKey() {
		return this.secureKey;
	}

	protected Cipher getEncryptCipher() {
		return this.encryptCipher;
	}

	protected Cipher getDecryptCipher() {
		return this.decryptCipher;
	}

}