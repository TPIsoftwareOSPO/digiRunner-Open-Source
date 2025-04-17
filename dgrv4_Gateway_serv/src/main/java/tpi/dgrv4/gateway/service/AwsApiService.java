package tpi.dgrv4.gateway.service;

//import java.io.ByteArrayInputStream;
//import java.nio.charset.StandardCharsets;
//import java.security.PublicKey;
//import java.security.Signature;
//import java.util.Base64;
//import java.util.Optional;

//import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;

//import com.amazonaws.auth.PEM;
//import com.amazonaws.services.marketplacemetering.AWSMarketplaceMetering;
//import com.amazonaws.services.marketplacemetering.AWSMarketplaceMeteringClient;
//import com.amazonaws.services.marketplacemetering.model.RegisterUsageRequest;
//import com.amazonaws.services.marketplacemetering.model.RegisterUsageResult;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.aws.ifs.IdgRaws;
//import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

/**
 * AWS 的計量服務 只要啟動CALL一次就好了喔~~~~~~
 * 
 * @author zoele
 *
 * 2024 / 6/ 15 完成 IoC 注入, git commit:"c3338a0", "9191cff"
 * @author John Chen
 */
@Service
public class AwsApiService {
	
	// IOC 注入
//	@Autowired(required = false)
	private IdgRaws dgrAWSComponent;

	private TsmpSettingService tsmpSettingService;
	private TsmpSettingDao tsmpSettingDao;
	
	@Autowired
	public AwsApiService(@Nullable IdgRaws dgrAWSComponent, TsmpSettingService tsmpSettingService,
			TsmpSettingDao tsmpSettingDao) {
		super();
		this.dgrAWSComponent = dgrAWSComponent;
		this.tsmpSettingService = tsmpSettingService;
		this.tsmpSettingDao = tsmpSettingDao;
	}

	public Boolean awsApi(String productCode, Integer publicKeyVersion, String nonce) throws Exception {
		// 從DB取PublicKey
		String keyStr = getPublicKeyStr();
		
		// IOC 注入, 2024 / 6/ 15 完成 IoC 注入, git commit:"c3338a0", "9191cff"
		boolean b = getDgrAWSComponent().awsApi(productCode, publicKeyVersion, nonce, keyStr, TPILogger.tl);
		return b;
	}
	
	public String getRegisterUsageResult() {
		String call_reg_result = getDgrAWSComponent().getRegisterUsageResult();
		return call_reg_result;
	}

	protected String getPublicKeyStr() {
		 return getTsmpSettingService().getVal_AWS_PUBLIC_KEY();
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return tsmpSettingDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
	public IdgRaws getDgrAWSComponent() {
		return dgrAWSComponent;
	}
}
