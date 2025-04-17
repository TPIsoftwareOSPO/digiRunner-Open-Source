package tpi.dgrv4.entity.component.cipher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.util.List;

import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.keeper.ITPILogger;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.component.IFileHelper;
import tpi.dgrv4.entity.component.ITsmpCoreTokenInitializerInit;
import tpi.dgrv4.entity.component.IVersionService;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;

@Component
public class TsmpCoreTokenInitializer {

	public static final TsmpDpFileType KEY_PAIR_FILE_TYPE = TsmpDpFileType.KEY_PAIR;

	public static final Long KEY_PAIR_REF_ID = 0L;

	public static final String DEFAULT_ALGORITHM = "RSA";

	public static String KEY_PAIR_FILE_NAME;

	private static final String KEY_PATH = "digiRunner.token.key-store.path";

	private static final String KEY_NAME = "digiRunner.token.key-store.name";

	private static final String KEY_PWD = "digiRunner.token.key-store-password";

	private static final String KEY_TYPE = "digiRunner.token.keyStoreType";

	private static final String KEY_ALIAS = "digiRunner.token.keyAlias";
	
	@Autowired
	private ITPILogger logger;

	@Autowired
	private Environment env;

	@Autowired
	private IFileHelper fileHelper;

	@Autowired
	private TsmpDpFileDao tsmpDpFileDao;

	@Autowired
	private IVersionService versionService;

	@Autowired
	private ITsmpCoreTokenInitializerInit tsmpCoreTokenInitializerInit;

	public TsmpCoreTokenInitializer(ITPILogger logger) {
		this.logger = logger;
	}

	/**
	 * 在環境初始時即從 ${digiRunner.token.key-store.path} 路徑下取得 JKS 的 KeyPair<br>
	 * 並與資料庫(tsmp_dp_file)儲存的 KeyPair 比對，若資料庫無 KeyPair 或是兩者的 KeyPair hashCode
	 * 不相符<br>
	 * 則將 ${digiRunner.token.key-store.path} JKS 中的 KeyPair 新增/更新至資料庫
	 * 
	 * @param e
	 * @throws Exception
	 */
	@EventListener
	public void init(ContextRefreshedEvent e) throws Exception {
		
		StringBuffer info = new StringBuffer();
		final StringBuffer infoStr = info;
		final int delayTime = 800;
		
		// 利用時間差取得 Spring Boot 啟動完成的訊息, "Started DgrApplication in 20.436 seconds"
		new Thread() {
			public void run() {
				mySleepTsmpCoreTokenInitializer(delayTime); //單純只是為 delay 一點印出 !
				StringBuffer sb = new StringBuffer();
				if (e != null) {
					sb = tsmpCoreTokenInitializerInit.init(e, sb);
				}
				sb.append("\n=== Begin Token keyPair initialization ===");
				sb.append("\n_____________________________________________");
				sb.append("\n");
				infoStr.append(sb.toString());
				mySleepTsmpCoreTokenInitializer(delayTime); // 等待 keeper 連線
				logger.tl.info(infoStr.toString());
			}
		}.start();
		
		
		KeyPair localKeyPair = getKeyPair();
		KeyPair remoteKeyPair = loadAndExtractKeyPair();

		if (localKeyPair == null) {
			localKeyPair = remoteKeyPair;
			if (remoteKeyPair == null) {
				// 如果找不到 KeyPair 就自行產生一組
				localKeyPair = generateKeyPair(TsmpCoreTokenInitializer.DEFAULT_ALGORITHM, 2048);
			}
		} else if (remoteKeyPair != null) {
			if (!isSameHashCode(localKeyPair, remoteKeyPair)) {
				localKeyPair = remoteKeyPair;
			}
		}

		// 將 KeyPair 存到資料庫
		setKeyPair(localKeyPair);

		// 驗證:存進去跟取出來要一樣
		KeyPair kp = getKeyPair();
		if (!isSameHashCode(kp, localKeyPair)) {
			throw new Exception("KeyPair save error!");
		}

		// 留給 {@link TsmpCoreTokenHelper} 用
		TsmpCoreTokenInitializer.KEY_PAIR_FILE_NAME = getKeyStoreName();
		logger.tl.debugDelay2sec("=== Token keyPair initialize successfully! ===");
		
		final StringBuffer buf = new StringBuffer();
		new Thread() {
			public void run() {
				mySleepTsmpCoreTokenInitializer(delayTime);
				while (getProjectVersion() == null) {
					mySleepTsmpCoreTokenInitializer(delayTime);// 等到有值了才能前進
				}
				buf.append(" .....................................................\n");
				buf.append(infoStr.toString());
				buf.append(" .....................................................\n");
				buf.append(" ..." + getProjectName() + " Started OK! version:" + getProjectVersion() + "\n");
				buf.append(" .....................................................\n");
				System.out.println(buf.toString());
			}
		}.start();
	}

	private String getProjectVersion() {
		return getVersionService().getVersion().strVersion;
	}

	private String getProjectName() {
		return getVersionService().getProjectName();
	}

	/**
	 * 從資料庫讀取 KeyPair<br>
	 * 有時 AP 環境跟 DB 環境可能在不同系統上運行，造成目錄路徑的預設分隔符號有差異 ("/"與"\")，<br>
	 * 這會導致使用 filePath 查詢 TSMP_DP_FILE 表時查無資料<br>
	 * 因此利用 "KEY_PAIR" 檔案類型應該只會有一筆的特性，來查詢 keyPair
	 * 
	 * @return
	 */
	private KeyPair getKeyPair() {
		String tsmpDpFileName = getKeyStoreName();
		List<TsmpDpFile> tsmpDpFiles = getTsmpDpFileDao().findByRefFileCateCodeAndRefIdAndFileName( //
				TsmpCoreTokenInitializer.KEY_PAIR_FILE_TYPE.value(), TsmpCoreTokenInitializer.KEY_PAIR_REF_ID,
				tsmpDpFileName);
		if (CollectionUtils.isEmpty(tsmpDpFiles)) {
			this.logger.debugDelay2sec("No such fileName in [tsmp_dp_file]: " + tsmpDpFileName);
			return null;
		}

		TsmpDpFile tsmpDpFile = tsmpDpFiles.get(0);

		String fullTsmpDpFilePath = tsmpDpFile.getFilePath() + tsmpDpFile.getFileName();
		this.logger.debugDelay2sec("Downloading KeyPair from: " + fullTsmpDpFilePath);

		byte[] content = null;
		try {
			content = getFileHelper().download(tsmpDpFile);
		} catch (Exception e) {
			this.logger.error(String.format("Fail to read blob from db: %s\n", fullTsmpDpFilePath)
					+ StackTraceUtil.logStackTrace(e));
		}

		KeyPair keyPair = null;
		try {
			if (content != null && content.length > 0) {
				keyPair = deserializeKeyPair(content);
			}
		} catch (Exception e) {
			this.logger.error("Fail to deserialize from blob\n" + StackTraceUtil.logStackTrace(e));
		}

		if (keyPair == null) {
			this.logger.debugDelay2sec("KeyPair not exists");
		}

		return keyPair;
	}

	/**
	 * 將 KeyPair 儲存至 TsmpDpFile 中
	 * 
	 * @param keyPair
	 * @return
	 * @throws TsmpAsyCryptInitException
	 */
	private TsmpDpFile setKeyPair(KeyPair keyPair) throws Exception {
		if (keyPair == null) {
			throw new NullPointerException("KeyPair is not nullable");
		}

		byte[] content = serializeKeyPair(keyPair);
		if (content == null || content.length == 0) {
			// 移除spring-security相關
			// throw new SerializationException("Fail to serialize KeyPair");
		}

		TsmpDpFile tsmpDpFile = null;
		try {
			// 如果 KeyPair 檔案已經存在資料庫，就覆蓋 blob
			tsmpDpFile = getKeyPairFileEntry();
			if (tsmpDpFile == null) {
				tsmpDpFile = getFileHelper().upload("SYSTEM", TsmpCoreTokenInitializer.KEY_PAIR_FILE_TYPE,
						TsmpCoreTokenInitializer.KEY_PAIR_REF_ID, //
						getKeyStoreName(), content, "N");
				this.logger.debugDelay2sec("KeyPair is saved: [" + tsmpDpFile.getFileId() + "]"
						+ (tsmpDpFile.getFilePath() + tsmpDpFile.getFileName()));
			} else {
				tsmpDpFile.setBlobData(content);
				tsmpDpFile.setUpdateDateTime(DateTimeUtil.now());
				tsmpDpFile.setUpdateUser("SYSTEM");

				// 在AWS上同時啟動 8 台dgR，會出現ObjectOptimisticLocking問題。
				try {
					tsmpDpFile = getTsmpDpFileDao().save(tsmpDpFile);  // Database ReadOnly
				} catch (JpaSystemException e) {
					String msg = StackTraceUtil.logTpiShortStackTrace(e);
					if (msg.indexOf("The database is read only") == -1 ||
							msg.indexOf("read-only") == -1
							) {
						throw e;
					}
				} catch (InvalidDataAccessResourceUsageException e) {
					String msg = StackTraceUtil.logTpiShortStackTrace(e);
					if (msg.indexOf("permission denied") == -1) {
						throw e;
					}
				} catch (ObjectOptimisticLockingFailureException e) {
					this.logger.warn("KeyPair is updated: Optimistic locking occurs");
				}

				this.logger
						.debugDelay2sec("KeyPair is updated: " + (tsmpDpFile.getFilePath() + tsmpDpFile.getFileName()));
			}
		} catch (Exception e) {
			this.logger.debug("Fail to save KeyPair in TsmpDpFile\n" + StackTraceUtil.logStackTrace(e));
			throw e;
		}
		return tsmpDpFile;
	}

	/**
	 * 載入 KeyStore 並取出金鑰對
	 * 
	 * @return
	 */
	private KeyPair loadAndExtractKeyPair() {
		boolean isReadyToLoad = isReadyToLoadKeyStore();
		if (!isReadyToLoad) {
			this.logger.debugDelay2sec(
					"Unable to load KeyStore. Please check 'digiRunner Token Keypair Setting' in application.properties.");
			return null;
		}

		KeyStore keyStore = null;
		try {
			var keystoreType = getKeyStoreType();
			if ("bcfks".equalsIgnoreCase(keystoreType)) {
				keyStore = KeyStore.getInstance(keystoreType, BouncyCastleFipsProvider.PROVIDER_NAME);
			} else {
				keyStore = KeyStore.getInstance(keystoreType);
			}
		} catch (Exception e) {
			this.logger.debug("Fail to get KeyStore instance with type: " + getKeyStoreType());
			return null;
		}

		String ksURI = getKeyStorePath() + getKeyStoreName();
		this.logger.debugDelay2sec("Loading KeyStore from: " + ksURI);

		try (FileInputStream fis = new FileInputStream(ksURI)) {
			keyStore.load(fis, getKeyStorePassword());
		} catch (FileNotFoundException e) {
			this.logger.error("KeyStore not found!");
		} catch (Exception e) {
			this.logger.error("Load KeyStore error!\n" + StackTraceUtil.logStackTrace(e));
		}

		if (!isKeyStoreLoaded(keyStore)) {
			this.logger.debugDelay2sec("KeyStore is not loaded");
			return null;
		}

		// 取出 KeyPair

		boolean hasKeyAlias = false;
		try {
			hasKeyAlias = keyStore.containsAlias(getKeyAlias());
		} catch (Exception e) {
			this.logger.error("Unable to verify key alias: " + getKeyAlias() + "\n" + StackTraceUtil.logStackTrace(e));
		}

		if (!hasKeyAlias) {
			this.logger.debugDelay2sec("No such key alias " + getKeyAlias());
			return null;
		}

		Key key = null;
		try {
			key = keyStore.getKey(getKeyAlias(), getKeyStorePassword());
		} catch (Exception e) {
			this.logger.error("Get key error!\n" + StackTraceUtil.logStackTrace(e));
		}

		if (key == null || !(key instanceof PrivateKey)) {
			this.logger.debugDelay2sec("Fail to get key from KeyStore " + getKeyAlias());
			return null;
		}

		PublicKey publicKey = null;
		try {
			Certificate cert = keyStore.getCertificate(getKeyAlias());
			publicKey = cert.getPublicKey();
		} catch (Exception e) {
			this.logger.error("Get public key error\n" + StackTraceUtil.logStackTrace(e));
		}

		if (publicKey == null) {
			this.logger.debugDelay2sec("Fail to get public key " + getKeyAlias());
			return null;
		}

		return new KeyPair(publicKey, (PrivateKey) key);
	}

	/**
	 * 產生一組 KeyPair 並存到 TsmpDpFile 中
	 * 
	 * @param algorithm
	 * @param keySize   1024 or 2048
	 * @return
	 * @throws TsmpAsyCryptInitException
	 */
	private KeyPair generateKeyPair(String algorithm, int keySize) throws Exception {
		KeyPair keyPair = null;
		try {
			this.logger.debugDelay2sec("Generating KeyPair...");

			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
			keyPairGenerator.initialize(keySize);
			keyPair = keyPairGenerator.generateKeyPair();
		} catch (Exception e) {
			this.logger.error(String.format("Fail to generate KeyPair, algorithm=%s, keySize=%d\n", //
					algorithm, keySize) + StackTraceUtil.logStackTrace(e));
		}
		return keyPair;
	}

	/**
	 * 從資料庫取得 KeyPair 的 TsmpDpFile 檔
	 * 
	 * @return
	 */
	private TsmpDpFile getKeyPairFileEntry() {
		List<TsmpDpFile> files = getTsmpDpFileDao().findByRefFileCateCodeAndRefIdAndFileName(//
				TsmpCoreTokenInitializer.KEY_PAIR_FILE_TYPE.value(), TsmpCoreTokenInitializer.KEY_PAIR_REF_ID,
				getKeyStoreName());
		if (CollectionUtils.isEmpty(files)) {
			return null;
		}
		return files.get(0);
	}

	/**
	 * 檢查載入 KeyStore 的必要資訊是否齊全
	 */
	private boolean isReadyToLoadKeyStore() {
		if (!StringUtils.hasText(getKeyStorePath()) || !StringUtils.hasText(getKeyStoreName())
				|| !StringUtils.hasText(getKeyStoreType()) || ObjectUtils.isEmpty(getKeyStorePassword())
				|| !StringUtils.hasText(getKeyAlias())) {
			return false;
		}
		return true;
	}

	/**
	 * 確認 KeyStore 是否已載入
	 * 
	 * @param keyStore
	 * @return
	 */
	private boolean isKeyStoreLoaded(KeyStore keyStore) {
		try {
			return keyStore.size() > 0;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * KeyPair反序列化
	 * 
	 * @param content
	 * @return
	 */
	private KeyPair deserializeKeyPair(byte[] content) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(content);
		ObjectInputStream ois = new ObjectInputStream(bais);
		KeyPair keyPair = (KeyPair) ois.readObject();
		ois.close();
		bais.close();
		return keyPair;
	}

	/**
	 * KeyPair序列化
	 * 
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	private byte[] serializeKeyPair(Object obj) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(obj);
		oos.flush();
		byte[] content = baos.toByteArray();
		oos.close();
		baos.close();
		return content;
	}

	/**
	 * 檢查公、私鑰的 Hash 值是否相同
	 * 
	 * @param kp1
	 * @param kp2
	 * @return
	 */
	private boolean isSameHashCode(KeyPair kp1, KeyPair kp2) {
		if (kp1 == null || kp2 == null || (kp1.getPublic().hashCode() != kp2.getPublic().hashCode())
				|| (kp1.getPrivate().hashCode() != kp2.getPrivate().hashCode())) {
			return false;
		}
		return true;
	}

	private void mySleepTsmpCoreTokenInitializer(long t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	protected IVersionService getVersionService() {
		return versionService;
	}
	
	protected Environment getEnvironment() {
		return env;
	}

	protected IFileHelper getFileHelper() {
		return fileHelper;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return tsmpDpFileDao;
	}

	protected String getKeyStorePath() {
		String keypath = getEnvironment().getProperty(TsmpCoreTokenInitializer.KEY_PATH);
		keypath = keypath + File.separatorChar;
		return keypath;
	}

	protected String getKeyStoreName() {
		return getEnvironment().getProperty(TsmpCoreTokenInitializer.KEY_NAME);
	}

	protected char[] getKeyStorePassword() {
		String pwd = getEnvironment().getProperty(TsmpCoreTokenInitializer.KEY_PWD);
		if (StringUtils.hasLength(pwd)) {
			return pwd.toCharArray();
		}
		return null;
	}

	protected String getKeyStoreType() {
		return getEnvironment().getProperty(TsmpCoreTokenInitializer.KEY_TYPE);
	}

	protected String getKeyAlias() {
		return getEnvironment().getProperty(TsmpCoreTokenInitializer.KEY_ALIAS);
	}

}