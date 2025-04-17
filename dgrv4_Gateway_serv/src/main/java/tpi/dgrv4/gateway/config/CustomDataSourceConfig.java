package tpi.dgrv4.gateway.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import tpi.dgrv4.codec.utils.CApiKeyUtils;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.DpaaHttpUtil;
import tpi.dgrv4.gateway.constant.DbConnectionMode;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.JsonNodeUtil;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 
 * @author zoe 用於啟動時設定DataSource
 */
@Configuration
@ConditionalOnProperty(name = "db.connection.mode", havingValue = "api")
public class CustomDataSourceConfig {
	private TPILogger logger = TPILogger.tl;

	@Value(value = "${cus.ip.port}")
	private String cusIpPort;

	@Value(value = "${cus.api.getDbMima}")
	private String getDbMima;

	@Value(value = "${db.connection.mode}")
	private String dbconnectionMode;

	@Value(value = "${dbConnectInit}")
	private String dbConnectInit;

	@Value("${spring.datasource.hikari.maximum-pool-size:10}")
	private int hikariMaximumPoolSize;
	@Value("${spring.datasource.hikari.connection-timeout:30000}")
	private long hikariConnectionTimeout;
	@Value("${spring.datasource.hikari.idle-timeout:600000}")
	private long hikariIdleTimeout;
	@Value("${spring.datasource.hikari.max-lifetime:1800000}")
	private long hikariMaxLifeTime;
	private static String tpiSalt = "TPIdigiRunner";
	
	private DataSourceProperties dataSourceProperties;
	private ObjectMapper om;
	
	@Autowired
	public CustomDataSourceConfig(DataSourceProperties dataSourceProperties, ObjectMapper om) {
		super();
		this.dataSourceProperties = dataSourceProperties;
		this.om = om;
	}

	@Bean
	public DataSource getDataSource() {
		HikariConfig hikariConfig = new HikariConfig();
		HikariDataSource hikaridataSource = null;
		try {
			TPILogger.tl.info("============Start setting DB============");
			// API 獲取資料庫密碼
			if (DbConnectionMode.API.equalsIgnoreCase(dbconnectionMode)) {
				TPILogger.tl.debug("Db Connection Mode :  " + dbconnectionMode);
				TPILogger.dbConnByApi = true;
				// 沒有設定ip port號
				if (!StringUtils.hasLength(getCusIpPort())) {
					throw new TsmpDpAaException("Ip And port are required when in API mode");
				}
				// 沒有設定uri
				if (!StringUtils.hasLength(getCusDbInfoUri())) {
					throw new TsmpDpAaException("API URi is required when in API mode");
				}
				String url = getApiUrl();
				TPILogger.tl.debug("API URL :  " + url);
				Map<String, Object> info = getDBInfo(url);

				JsonNode dbInfoRespJson = (JsonNode) info.get(TPILogger.DBINFO);
				String username = JsonNodeUtil.getNodeAsText(dbInfoRespJson, "dbUsername1");
				String password = JsonNodeUtil.getNodeAsText(dbInfoRespJson, "dbMima1");
				String username2 = JsonNodeUtil.getNodeAsText(dbInfoRespJson, "dbUsername2");
				String password2 = JsonNodeUtil.getNodeAsText(dbInfoRespJson, "dbMima2");
				// 回應的訊息必須包含 username1, password1, username2, password2
				if (!StringUtils.hasLength(username) || !StringUtils.hasLength(password)
						|| !StringUtils.hasLength(username2) || !StringUtils.hasLength(password2)) {
					throw new TsmpDpAaException(
							"The data must have dbUsername1, dbMima1, dbUsername1, dbMima2.  ");
				}
				// 依 dbConnectInit, 設定 DB連線使用帳號1 或 2
				if ("1".equals(dbConnectInit)) {
					hikariConfig.setUsername(username);
					hikariConfig.setPassword(password);
				}
				if ("2".equals(dbConnectInit)) {
					hikariConfig.setUsername(username2);
					hikariConfig.setPassword(password2);
				}
				// 更新至客製包 的 Memory
				TPILogger.dbInfoMap = info;

				// 正常來說其實不需要寫這兩行，但是以防他有時候失靈還是從properties取出來重新設定
				hikariConfig.setJdbcUrl(getDataSourceProperties().getUrl());
				hikariConfig.setDriverClassName(getDataSourceProperties().getDriverClassName());
				hikariConfig.setMaximumPoolSize(hikariMaximumPoolSize);
				hikariConfig.setConnectionTimeout(hikariConnectionTimeout);
				hikariConfig.setIdleTimeout(hikariIdleTimeout);
				hikariConfig.setMaxLifetime(hikariMaxLifeTime);
				// 使用自訂義設定檔設定HikariDataSource
				hikaridataSource = new HikariDataSource(hikariConfig);
			}



		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw new TsmpDpAaException(e.getMessage());
		}
		TPILogger.tl.info("============End setting DB============");
		return hikaridataSource;
	}

	private Map<String, Object> getDBInfo(String url) {
		// 因為Spring 還沒有實例化 所以手動 init 鹽
		CApiKeyUtils.init(new Object(), tpiSalt);
		Map<String, String> header = new HashMap<>();
		String uuidForCapiKey = UUID.randomUUID().toString();
		String cuuid = uuidForCapiKey.toUpperCase();
		String capikey = CApiKeyUtils.signCKey(cuuid);
		header.put("Accept", "application/json");
		header.put("Content-Type", "application/json");
		header.put("cuuid", cuuid);
		header.put("capi-key", capikey);
		// 從客製包取得DB Info
		HttpRespData dbInfoResp = null;
		try {

			dbInfoResp = HttpUtil.httpReqByRawData(url, "POST", DpaaHttpUtil.toReqPayloadJson(new Object(), null),
					header, false);
			
		} catch (IOException e) {
			TPILogger.tl.info(StackTraceUtil.logTpiShortStackTrace(e));
			throw new TsmpDpAaException(e.getMessage());

		}
		if (dbInfoResp.statusCode <= 0 || dbInfoResp.statusCode >= 400) {
			TPILogger.tl.info("Get Db Info from Cus error : \n  " + dbInfoResp.getLogStr());
			throw new TsmpDpAaException("Error Status Code :" + dbInfoResp.statusCode);
		}

		if (!StringUtils.hasLength(dbInfoResp.respStr)) {
			throw new TsmpDpAaException("The API did not rssponse any data.");
		}

		JsonNode dbInfoRespJson = null;
		try {
			TPILogger.tl.info("Get Db Info from Cus success : \n   " + url);
			dbInfoRespJson = getObjectMapper().readTree(dbInfoResp.respStr);
			
			String dgRRtnCode = dbInfoRespJson.get("ResHeader").get("rtnCode").asText();
			if (!"1100".equals(dgRRtnCode)) {
				TPILogger.tl.info("Get Db Info from Cus error : \n  " + dbInfoResp.getLogStr());
				throw new TsmpDpAaException(" Get Db Info from Cus error " + dbInfoResp.respStr);
			}
			
			dbInfoRespJson = dbInfoRespJson.get("RespBody");
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logTpiShortStackTrace(e));
			throw new TsmpDpAaException(
					"The API response format is incorrect." + "\n resp data : \n" + dbInfoResp.respStr);
		}

		Map<String, Object> infoData = new HashMap<>();
		infoData.put(TPILogger.DBINFO, dbInfoRespJson);

		return infoData;

	}

	private String getApiUrl() {
		return "https://" + getCusIpPort() + getCusDbInfoUri();

	}

	public String getCusIpPort() {
		return cusIpPort;
	}

	public void setCusIpPort(String cusIpPort) {
		this.cusIpPort = cusIpPort;
	}

	public String getCusDbInfoUri() {
		return getDbMima;
	}

	public void setCusDbInfoUri(String cusDbInfoUri) {
		this.getDbMima = cusDbInfoUri;
	}

	public String getDbconnectionMode() {
		return dbconnectionMode;
	}

	public void setDbconnectionMode(String dbconnectionMode) {
		this.dbconnectionMode = dbconnectionMode;
	}

	protected DataSourceProperties getDataSourceProperties() {
		return dataSourceProperties;
	}

	protected ObjectMapper getObjectMapper() {
		return om;
	}

}
