package tpi.dgrv4.entity.component.cipher;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import java.util.function.Function;

import javax.crypto.Cipher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.CipherInstanceUtil;
import tpi.dgrv4.codec.utils.PEMUtil;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.entity.component.IFileHelper;
import tpi.dgrv4.entity.component.IFileHelperCacheProxy;
import tpi.dgrv4.entity.component.ITsmpCoreTokenHelperCacheProxy;
import tpi.dgrv4.entity.exceptions.DgrException;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.entity.ifs.IEntityTPILogger;


/**
 * [ZH] 利用 digiRunner 底層核發 Token 所使用的密碼金鑰對 (KeyPair) 實現加、解密 <br>
 * [EN] Encryption and decryption using the cryptographic key pair (KeyPair)
 * used by the underlying token issuance of digiRunner <br>
 * 
 * @author Kim
 */
@Component
public class TsmpCoreTokenEntityHelper {

	private IFileHelperCacheProxy fileHelperCacheProxy;
	private ITsmpCoreTokenHelperCacheProxy tsmpCoreTokenHelperCacheProxy;
		
	private final int MAX_ENCRYPT_BLOCK = 117;

	private int MAX_DECRYPT_BLOCK = 128;
	
	private boolean loggerFlag = true;

	@Autowired
	public TsmpCoreTokenEntityHelper(IEntityTPILogger logger, IFileHelperCacheProxy fileHelperCacheProxy,
			ITsmpCoreTokenHelperCacheProxy tsmpCoreTokenHelperCacheProxy) {
		super();
		IEntityTPILogger.Holder.setInstance((IEntityTPILogger)logger);
		this.fileHelperCacheProxy = fileHelperCacheProxy;
		this.tsmpCoreTokenHelperCacheProxy = tsmpCoreTokenHelperCacheProxy;
	}

	public String encrypt(String originalString) throws DgrException {
		PublicKey publicKey = getPublicKey();
		if (publicKey == null) {
			IEntityTPILogger.getInstance().debug("Public key is null");
			throw DgrRtnCode._1433.throwing( CipherInstanceUtil.getCipherInstance3());
		}

		byte[] originalByte = originalString.getBytes();
		byte[] encodedByte = codec(originalByte, Cipher.ENCRYPT_MODE, publicKey, this.MAX_ENCRYPT_BLOCK);
		String encodedString = Base64.getEncoder().encodeToString(encodedByte);
		return encodedString;
	}

	public String decrypt(String encodedString) throws DgrException {
		PrivateKey privateKey = getPrivateKey();
		if (privateKey == null) {
			IEntityTPILogger.getInstance().debug("Private key is null");
			throw DgrRtnCode._1434.throwing( CipherInstanceUtil.getCipherInstance3() );
		}
		
		// 調整 RSA decrypt block size
		if (privateKey != null && privateKey instanceof RSAPrivateKey) {
			RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
			int keySize = rsaPrivateKey.getModulus().bitLength();
			if (keySize >= 2048) {
				this.MAX_DECRYPT_BLOCK = 256;
			}
		}
		
		byte[] encodedByte = Base64.getDecoder().decode(encodedString);
		byte[] decodedByte = codec(encodedByte, Cipher.DECRYPT_MODE, privateKey, this.MAX_DECRYPT_BLOCK);
		String decodedString = new String(decodedByte);
		return decodedString;
	}

	private byte[] codec(byte[] data, int mode, Key key, int maxBlockLength) throws DgrException {
		try {
			Cipher cipher = Cipher.getInstance( CipherInstanceUtil.getCipherInstance3() );
			cipher.init(mode, key);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int inputLen = data.length;
			int offSet = 0;
			byte[] cache;
			int i = 0;

			while (inputLen - offSet > 0) {
				if (inputLen - offSet > maxBlockLength) {
					cache = cipher.doFinal(data, offSet, maxBlockLength);
				} else {
					cache = cipher.doFinal(data, offSet, inputLen - offSet);
				}
				out.write(cache, 0, cache.length);
				i++;
				offSet = i * maxBlockLength;
			}
			byte[] encryptedData = out.toByteArray();

			return encryptedData;

		} catch (Throwable t) {
			if (Cipher.ENCRYPT_MODE == mode) {
				throw DgrRtnCode._1433.throwing( CipherInstanceUtil.getCipherInstance3() );
			} else if (Cipher.DECRYPT_MODE == mode) {
				try {
					IEntityTPILogger.getInstance().error(new String(data, StandardCharsets.UTF_8));
				}catch(Exception e) {
					
				}
				throw DgrRtnCode._1434.throwing( CipherInstanceUtil.getCipherInstance3() );
			} else {
				throw DgrRtnCode._1297.throwing();
			}
		}
	}

	/**
	 * 取得 PublicKey
	 * 
	 * @return Nullable
	 */
	private PublicKey getPublicKey() {
		return getKey((keyPair) -> {
			return keyPair.getPublic();
		});
	}

	/**
	 * 取得 PrivateKey
	 * 
	 * @return Nullable
	 */
	private PrivateKey getPrivateKey() {
		return getKey((keyPair) -> {
			return keyPair.getPrivate();
		});
	}

	private <R> R getKey(Function<KeyPair, R> func) {
		KeyPair keyPair = getKeyPair();
		if (keyPair == null) {
			return null;
		}
		return func.apply(keyPair);
	}

	/**
	 * 從資料庫讀取 KeyPair
	 * 
	 * @return
	 */
	public KeyPair getKeyPair() {
		byte[] content = getKeyPairContent();
		return deserializeKeyPair(content);
	}

	private byte[] getKeyPairContent() {
		TsmpDpFileType keyPairFileType = TsmpCoreTokenInitializer.KEY_PAIR_FILE_TYPE;
		Long keyPairRefId = TsmpCoreTokenInitializer.KEY_PAIR_REF_ID; // ex: 0
		String keyPairFileName = TsmpCoreTokenInitializer.KEY_PAIR_FILE_NAME; // ex: express-dgr-token.jks or tsmp-core-token.jks
		
		if (!StringUtils.hasText(keyPairFileName)) {
			IEntityTPILogger.getInstance().error("File name of KeyPair is empty! Was TsmpCoreTokenInitializer initialized successfully?");
			return null;
		}
		
		String tsmpDpFilePath = IFileHelper.getTsmpDpFilePath(keyPairFileType, keyPairRefId); // ex: KEY_PAIR\0\
		//if (this.loggerFlag == true) {
			// 為了不要 RunLoopJob / ES 每 call 一次要解密時就 debugDelay2sec 一次, 導致整個畫面都是 "...Downloading KeyPair"
			//this.logger.debugDelay2sec("Downloading KeyPair from: " + tsmpDpFilePath + keyPairFileName);
		//}
		
		byte[] blobData = getFileHelperCacheProxy().downloadByPathAndName(tsmpDpFilePath, keyPairFileName);
		if (blobData == null || blobData.length < 1) {
			// 為了不要 RunLoopJob / ES 每 call 一次要解密時就 debugDelay2sec 一次, 導致整個畫面都是 "...Downloading KeyPair"
			this.loggerFlag = true;

			//例如在執行本機但DB連DEV
			if(tsmpDpFilePath != null && tsmpDpFilePath.indexOf("/") > -1) {
				String tsmpDir = tsmpDpFilePath.replaceAll("/", "\\\\");
				blobData = getFileHelperCacheProxy().downloadByPathAndName(tsmpDir, keyPairFileName);
				//this.logger.debugDelay2sec("change Downloading KeyPair from: " + tsmpDir + keyPairFileName);
			}else if(tsmpDpFilePath != null){
				String tsmpDir = tsmpDpFilePath.replaceAll("\\\\", "/");
				blobData = getFileHelperCacheProxy().downloadByPathAndName(tsmpDir, keyPairFileName);
				//this.logger.debugDelay2sec("change Downloading KeyPair from: " + tsmpDir + keyPairFileName);
			}
			
			if (blobData == null || blobData.length < 1) {
				IEntityTPILogger.getInstance().error(String.format("Fail to read blob from db: %s%s", tsmpDpFilePath, keyPairFileName));
			}
		} else {
			// 為了不要 RunLoopJob / ES 每 call 一次要解密時就 debugDelay2sec 一次, 導致整個畫面都是 "...Downloading KeyPair"
			this.loggerFlag = false;
		}
		return blobData;
	}

	/**
	 * KeyPair反序列化
	 * @param content
	 * @return
	 */
	public KeyPair deserializeKeyPair(byte[] content) {
		if (content == null || content.length == 0) {
			return null;
		}
		
		return getTsmpCoreTokenHelperCacheProxy().deserializeKeyPair(content);
	}
	
	/**
	 * [ZH] 取得 dgR 核發 Token & ENC 加密, 所使用的金鑰對的公鑰憑證內容 <br>
	 * [EN] Get the public key certificate content of the key pair used for dgR
	 * issued Token & ENC encryption <br>
	 */
	public String getPublicKeyPem() {
		PublicKey publicKey = getKeyPair().getPublic();
		return PEMUtil.converKeyByteToPem(publicKey.getEncoded(), "PUBLIC KEY");
	}
	
	protected IFileHelperCacheProxy getFileHelperCacheProxy() {
		return this.fileHelperCacheProxy;
	}

	protected ITsmpCoreTokenHelperCacheProxy getTsmpCoreTokenHelperCacheProxy() {
		return this.tsmpCoreTokenHelperCacheProxy;
	}

}