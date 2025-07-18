package tpi.dgrv4.gateway.keeper;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Setter;
import org.apache.catalina.Context;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import tpi.dgrv4.codec.utils.UUID64Util;
import tpi.dgrv4.common.component.cache.core.DaoGenericCache;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.LoggerLevelConstant;
import tpi.dgrv4.common.keeper.ITPILogger;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.LicenseType;
import tpi.dgrv4.common.utils.LicenseUtilBase;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.DpaaSystemInfoHelper;
import tpi.dgrv4.dpaa.constant.LicenseEnvType;
import tpi.dgrv4.dpaa.es.DiskSpaceMonitor;
import tpi.dgrv4.dpaa.es.ESLogBuffer;
import tpi.dgrv4.dpaa.es.EsHttpClient;
import tpi.dgrv4.dpaa.service.ChangeDbConnInfoService;
import tpi.dgrv4.dpaa.service.RealtimeDashboardService;
import tpi.dgrv4.dpaa.vo.DpaaSystemInfo;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.ifs.IEntityTPILogger;
import tpi.dgrv4.entity.repository.DgrDashboardEsLogDao;
import tpi.dgrv4.entity.repository.DgrNodeLostContactDao;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.TCP.Packet.NodeInfoPacket;
import tpi.dgrv4.gateway.TCP.Packet.RequireAllClientListPacket;
import tpi.dgrv4.gateway.TCP.Packet.TPILogInfoPacket;
import tpi.dgrv4.gateway.TCP.Packet.WebsiteTargetThroughputPacket;
import tpi.dgrv4.gateway.component.BotDetectionRuleValidator;
import tpi.dgrv4.gateway.component.ServerConfigProperties;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.cache.core.GenericCache;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpSettingCacheProxy;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.constant.DgrDeployRole;
import tpi.dgrv4.gateway.filter.GatewayFilter;
import tpi.dgrv4.gateway.keeper.server.CommunicationServerConfig;
import tpi.dgrv4.gateway.service.AwsApiService;
import tpi.dgrv4.gateway.service.CommForwardProcService;
import tpi.dgrv4.gateway.service.DPB0059Service;
import tpi.dgrv4.gateway.service.DgrApiLog2ESQueue;
import tpi.dgrv4.gateway.service.DgrApiLog2RdbQueue;
import tpi.dgrv4.gateway.service.IUndertowMetricsService;
import tpi.dgrv4.gateway.service.InMemoryGtwRefresh2LandingService;
import tpi.dgrv4.gateway.service.MonitorHostService;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.service.WebsiteService;
import tpi.dgrv4.gateway.vo.ClientKeeper;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.tcp.utils.communication.ClinetNotifier;
import tpi.dgrv4.tcp.utils.communication.LinkerClient;
import tpi.dgrv4.tcp.utils.communication.Role;
import tpi.dgrv4.tcp.utils.packets.DoSetUserName;
import tpi.dgrv4.tcp.utils.packets.RealtimeDashboardPacket;
import tpi.dgrv4.tcp.utils.packets.UndertowMetricsPacket;
import tpi.dgrv4.tcp.utils.packets.UrlStatusPacket;

@Getter(AccessLevel.PROTECTED)
@Component
public class TPILogger extends ITPILogger implements IEntityTPILogger {
	// ALL < TRACE < DEBUG < INFO < WARN < ERROR < FATAL < OFF
	public static boolean trace_flag = false; // 可由 LoggerFlagController 啟用/停用
	public static boolean debug_flag = false; // 可由 LoggerFlagController 啟用/停用
	public static boolean info_flag = true; // 可由 LoggerFlagController 啟用/停用
	public static boolean warn_flag = true; // 可由 LoggerFlagController 啟用/停用
	public static boolean error_flag = true; // 可由 LoggerFlagController 啟用/停用

	public final static String nodeInfo = "nodeInfo";
	
	private static Logger logger = LoggerFactory.getLogger(TPILogger.class);
	public static TPILogger tl;
	
	private static boolean isFirstConnection = true; // 第一次連完後要變為 false, 因為第一次是連 127.0.0.1
	public static boolean hasSecondConnectionStarting = false; // 第一次連接後, 要改連 RDB 為加速需要變更為 true

	public static LinkerClient lc;
	public static LinkedList<String> logStartingMsg = new LinkedList<String>();

	private ClinetNotifier lcNofify;

	public String loggerLevel;
	
	public static String lcUserName;

	private String prifixUserName = "gateway";

	public static String uuid = UUID64Util.UUID64(UUID.randomUUID()).substring(0, 4);

	@Value("${digi.instance.id}")
	private String instanceId;
	
	@Value("${es.apilog.disk.free.threshold:0.2f}")
	public Float diskFreeThreshHold; 
	
	@Value("${es.apilog.disk.deletePercert:80}")
	public int deletePercent;
	
	@Value("${es.apilog.allow.write.elastic:false}")
	public boolean allowWriteElastic;

	public static final String dgrNodeLostContactDaoStr = "dgrNodeLostContactDaoStr";
	private Thread scheduler_t_refresh = null;
	private Thread scheduler_t_check = null;

	private StringBuffer debugMsg = new StringBuffer();
	private String delayDEBUGLineNumberStr = "";

	private StringBuffer traceMsg = new StringBuffer();
	private String delayTRACELineNumberStr = "";

	public ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread thread = new Thread(r);
		thread.setName("TPILogger-Thread");
		return thread;
	});

	private HashMap<String, String> paramBefore;

	private long startTime = System.currentTimeMillis();
	
	private NodeInfoPacket nodeInfoPacket;

	// 儲存客戶端資料最後更新的時間戳，初始值為 -1 表示尚未更新
	public static final AtomicLong lastUpdateTimeClient = new AtomicLong(-1);
	// 儲存 API 資料最後更新的時間戳，初始值為 -1 表示尚未更新
	public static final AtomicLong lastUpdateTimeAPI = new AtomicLong(-1);
	// 儲存設定資料最後更新的時間戳，初始值為 -1 表示尚未更新
	public static final AtomicLong lastUpdateTimeSetting = new AtomicLong(-1);
	// 儲存 Token 最後更新的時間戳，初始值為 -1 表示尚未更新
	public static final AtomicLong lastUpdateTimeToken = new AtomicLong(-1);

	public static Map<String, Long> tokenUsedMap = new HashMap<>();// 用來記錄 token 的使用量
	public static Map<String, Integer> apiUsedMap = new HashMap<>();// 用來記錄 API 的使用量

	@Value("${digiRunner.gtw.deploy.role}")
	private String deployRole;
	public static String tlDeployRole;

	@Value("${digiRunner.gtw.deploy.interval.ms:1000}")
	private Long deployIntervalMs;

	@Value("${es.api.log.store.dir.max.size:5GB}")
	private String esApiLogDirMaxSize;
	
	@Value("${es.api.log.store.dir.max.files:50000}")
	private int esApiLogDirMaxFiles;
	
	// 從配置中獲取最大目錄大小
    private long getConfiguredMaxDirSize() {
        // 這裡可以從系統屬性、配置文件或環境變量讀取配置
        return parseSize(esApiLogDirMaxSize);
    }
    
    // 從配置中獲取最大文件數量
    private int getConfiguredMaxFiles() {
        return esApiLogDirMaxFiles;
    }
    
    // 解析大小字符串為字節數
    private static long parseSize(String sizeStr) {
        sizeStr = sizeStr.toUpperCase();
        long multiplier = 1;
        
        if (sizeStr.endsWith("KB")) {
            multiplier = 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("MB")) {
            multiplier = 1024L * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("GB")) {
            multiplier = 1024L * 1024 * 1024;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("TB")) {
            multiplier = 1024L * 1024L * 1024L * 1024L;
            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
        } else if (sizeStr.endsWith("B")) {
            sizeStr = sizeStr.substring(0, sizeStr.length() - 1);
        }
        
        return (long)(Double.parseDouble(sizeStr) * multiplier);
    }

	public String getDeployRole() {
		return deployRole;
	}
	@Setter
	public static Map<String, Object> dbInfoMap = new HashMap<>();
	public static final String DBINFOMAP_TITLE = "dbInfoMap";
	public static final String DBINFO = "dbInfo";

	public static int PORT = 0;
	@Value("${dbInfo.mask.keys}")
	private String maskKeys;
	@Value(value = "${cus.ip.port}")
	private String cusIpPort;
	@Value(value = "${cus.scheme}")
	private String cusScheme;
	public static String cusSchemeForBroadcast;
	public static String cusIpPortForBroadcast;
	public static List<String> maskKeysArr;
	private static long threadCreateTiem;
	public static boolean dbConnByApi;
	
	@Value(value = "${tomcat.Graceful}")
	private Boolean tomcatGraceful;
	
	@Value("${check.sensitive.info.enable:false}")
	public boolean sensitiveEnable;
	@Value("${check.sensitive.info.keyword:pwd,password,mima}")
	public String sensitiveKeyword;
	
	
//	@Autowired(required = false)
	private IUndertowMetricsService undertowMetricsService;
//	@Autowired(required = false)
	private LicenseUtilBase licenseUtilBase;
	
	private CommunicationServerConfig communicationServerConfig; // 用來載入 @PostConstruct init()
	
	private TsmpSettingService tsmpSettingService;
	private DgrDashboardEsLogDao dgrDashboardEsLogDao;
	private RealtimeDashboardService realtimeDashboardService;
	private DPB0059Service dPB0059Service;
	private JobHelper jobHelper;
	private MonitorHostService monitorHostService;
	private InMemoryGtwRefresh2LandingService inMemoryGtwRefresh2LandingService;
	private GenericCache genericCache;
	private DaoGenericCache daoGenericCache;
	private ApptJobDispatcher apptJobDispatcher;
	private DgrNodeLostContactDao dgrNodeLostContactDao;
	private ServiceConfig serviceConfig;
	private TsmpSettingCacheProxy tsmpSettingCacheProxy;
	private WebsiteService websiteService;
	private BotDetectionRuleValidator botDetectionRuleValidator;
	private ObjectMapper objectMapper;
	private AwsApiService awsApiService;
	private HikariDataSource dataSource;
	private ChangeDbConnInfoService changeDbConnInfoService;
	private ServerConfigProperties serverConfigProperties;
	
	@Autowired
	public void setTPILogger(@Nullable IUndertowMetricsService undertowMetricsService,
			@Nullable LicenseUtilBase licenseUtilBase, CommunicationServerConfig communicationServerConfig,
			TsmpSettingService tsmpSettingService, DgrDashboardEsLogDao dgrDashboardEsLogDao,
			RealtimeDashboardService realtimeDashboardService, DPB0059Service dPB0059Service, JobHelper jobHelper,
			MonitorHostService monitorHostService, InMemoryGtwRefresh2LandingService inMemoryGtwRefresh2LandingService,
			GenericCache genericCache, DaoGenericCache daoGenericCache, ApptJobDispatcher apptJobDispatcher,
			DgrNodeLostContactDao dgrNodeLostContactDao, ServiceConfig serviceConfig,
			TsmpSettingCacheProxy tsmpSettingCacheProxy, WebsiteService websiteService,
			BotDetectionRuleValidator botDetectionRuleValidator, ObjectMapper objectMapper, AwsApiService awsApiService,
			HikariDataSource dataSource, ChangeDbConnInfoService changeDbConnInfoService,
			ServerConfigProperties serverConfigProperties) {
		this.undertowMetricsService = undertowMetricsService;
		this.licenseUtilBase = licenseUtilBase;
		this.communicationServerConfig = communicationServerConfig;
		this.tsmpSettingService = tsmpSettingService;
		this.dgrDashboardEsLogDao = dgrDashboardEsLogDao;
		this.realtimeDashboardService = realtimeDashboardService;
		this.dPB0059Service = dPB0059Service;
		this.jobHelper = jobHelper;
		this.monitorHostService = monitorHostService;
		this.inMemoryGtwRefresh2LandingService = inMemoryGtwRefresh2LandingService;
		this.genericCache = genericCache;
		this.daoGenericCache = daoGenericCache;
		this.apptJobDispatcher = apptJobDispatcher;
		this.dgrNodeLostContactDao = dgrNodeLostContactDao;
		this.serviceConfig = serviceConfig;
		this.tsmpSettingCacheProxy = tsmpSettingCacheProxy;
		this.websiteService = websiteService;
		this.botDetectionRuleValidator = botDetectionRuleValidator;
		this.objectMapper = objectMapper;
		this.awsApiService = awsApiService;
		this.dataSource = dataSource;
		this.changeDbConnInfoService = changeDbConnInfoService;
		this.serverConfigProperties = serverConfigProperties;
	}


	static {
		tl = new TPILogger();
		ITPILogger.tl = tl;
		IEntityTPILogger.Holder.setInstance(tl);
		TPIFileLoggerQueue.startThread();
	}

	@PostConstruct
	public void init() {
		tl = this;
		ITPILogger.tl = tl;
		IEntityTPILogger.Holder.setInstance(tl);
		initKeeper();

		// init logger LEVEL
		initLoggerLevel(null);
		maskKeysArr = Arrays.asList(maskKeys.split(","));
		cusIpPortForBroadcast = cusIpPort;
		cusSchemeForBroadcast = cusScheme;
		PORT = serverConfigProperties.getPort();
	}

	public static void initLoggerLevel(String loggerLevel) {
		if (loggerLevel == null) {
			loggerLevel = TPILogger.tl.currentLoggerLevel();
		}
		TPILogger.tl.loggerLevel = loggerLevel;

		// onlineConsole 切換到 trace 才要啟用, 但它無法適用 HA, 一次只能切換一台
		TPILogger.trace_flag = "TRACE".equalsIgnoreCase(loggerLevel);
		TPILogger.debug_flag = "TRACE".equalsIgnoreCase(loggerLevel) || "DEBUG".equalsIgnoreCase(loggerLevel)
				|| "LOGUUID".equalsIgnoreCase(loggerLevel);
		TPILogger.info_flag = "TRACE".equalsIgnoreCase(loggerLevel) || "DEBUG".equalsIgnoreCase(loggerLevel)
				|| "INFO".equalsIgnoreCase(loggerLevel) || "LOGUUID".equalsIgnoreCase(loggerLevel);
		TPILogger.warn_flag = "TRACE".equalsIgnoreCase(loggerLevel) || "DEBUG".equalsIgnoreCase(loggerLevel)
				|| "INFO".equalsIgnoreCase(loggerLevel) || "WARN".equalsIgnoreCase(loggerLevel)
				|| "LOGUUID".equalsIgnoreCase(loggerLevel);

		TPILogger.tl.error("\n<font size=18>logger level: " + loggerLevel + " </font>\n");
	}

	public String currentLoggerLevel() {
		Optional<TsmpSetting> opt_tsmpSetting = getTsmpSettingCacheProxy().findById(TsmpSettingDao.Key.LOGGER_LEVEL);
		if (opt_tsmpSetting.isPresent()) {
			TsmpSetting tsmpSetting = opt_tsmpSetting.get();
			return tsmpSetting.getValue();
		}
		return "";
	}

	public void initKeeper() {
		// keeper client connect to server with [Notifier]
		lcNofify = new ClinetNotifier() {
			@Override
			public void runDisconnect(LinkerClient conn) {
				// 表示至少連線過一次, 只是 keeper server 有斷線
				if (lc != null) {
					paramBefore = (HashMap<String, String>) TPILogger.lc.param.clone();
				}

				lc = null;
				TPILogger.tl.info("detected dgr-keeper is Disconnect....");

				// Re-connection
				new Thread() {
					public void run() {
						// connect to server each 10 sec
						conn.close();
						connect();
					}
				}.start();
			}

			@Override
			public void runConnection(LinkerClient conn) {
				StringBuffer msgbuf = new StringBuffer();
				String s = "\r\n" + " __  ___ .______       ______  __       __  .___________.\r\n"
						+ "|  |/  / |   _  \\     /      ||  |     |  | |           |\r\n"
						+ "|  '  /  |  |_)  |   |  ,----'|  |     |  | `---|  |----`\r\n"
						+ "|    <   |   ___/    |  |     |  |     |  |     |  |     \r\n"
						+ "|  .  \\  |  |        |  `----.|  `----.|  |     |  |     \r\n"
						+ "|__|\\__\\ | _|         \\______||_______||__|     |__|     \r\n"
						+ "                                                         \r\n" + "";
				msgbuf.append(s);
				msgbuf.append("\n...This Client Connect to dgr-keeper server [Socket Connected OK !]");
				msgbuf.append("\n______________________________________________________");
				msgbuf.append("\n");
				TPILogger.tl.info(msgbuf.toString());
			}
		};

		// connect to server each 10 sec
		connect();
	}

	public void info(String logMsg) {
		if (!info_flag) {
			return;
		} // 不寫檔 + 不送 keeper
		TPILogInfo log = new TPILogInfo();
		try {
			log.setLevel("INFO");
			log.getLogMsg().append(Thread.currentThread().getName() + "::" + logMsg);
			String msg = getLotMsg(log);
			TPIFileLoggerQueue.put(TPIFileLoggerQueue.INFO, msg);
		} catch (InterruptedException e) {
			// 重新設置中斷狀態
			Thread.currentThread().interrupt();
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(StackTraceUtil.logTpiShortStackTrace(e));
		}

		if (getOnlineFlagByCache()) {
			sendLogPacket(log);
		}
	}

	public void debug(String logMsg) {
		if (!debug_flag) {
			return;
		} // 不寫檔 + 不送 keeper

		// Level 設定不是 API Log 就離開
		if (isNotAPILogInDebugMsg(logMsg)) {
			return;
		}

		TPILogInfo log = new TPILogInfo();
		try {
			log.setLevel("DEBUG");
			log.getLogMsg().append(Thread.currentThread().getName() + "::" + logMsg);
			String msg = getLotMsg(log);
			TPIFileLoggerQueue.put(TPIFileLoggerQueue.DEBUG, msg);
		} catch (InterruptedException e) {
			// 重新設置中斷狀態
			Thread.currentThread().interrupt();
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(StackTraceUtil.logTpiShortStackTrace(e));
		}

		if (getOnlineFlagByCache()) {
			sendLogPacket(log);
		}
	}

	public void debugDelay2sec(String logMsg) {
		if (!debug_flag) {
			return;
		} // 不寫檔 + 不送 keeper

		// Level 設定不是 API Log 就離開
		if (isNotAPILogInDebugMsg(logMsg)) {
			return;
		}

		if (!StringUtils.hasText(debugMsg) || traceMsg.length() >= 1024) {
			TPILogger.tl.executorService.schedule(() -> debug(), 2, TimeUnit.SECONDS); // 延遲 2 秒後
		}
		String lineNumberStr = TPILogInfo.getLineNumber2();
		if (delayDEBUGLineNumberStr.equals(lineNumberStr)) {
			debugMsg.append("\t..." + logMsg + "\n");
		} else {
			delayDEBUGLineNumberStr = lineNumberStr;
			debugMsg.append("\n\t...[ " + lineNumberStr + "] => \n\t..." + logMsg + "\n");
		}
	}

	public void debug() {
		if (!debug_flag) {
			return;
		} // 不寫檔 + 不送 keeper
		if (!StringUtils.hasText(debugMsg)) {
			return;
		}
		TPILogInfo log = new TPILogInfo();
		try {
			log.setLevel("DEBUG");
			log.getLogMsg().append(debugMsg.toString()); // 載入 buffer
			debugMsg.delete(0, debugMsg.length()); // 清空 buffer
			delayDEBUGLineNumberStr = "";
			TPIFileLoggerQueue.put(TPIFileLoggerQueue.DEBUG, "[ " + log.getLine() + "]\n" + log.getLogMsg().toString());
		} catch (InterruptedException e) {
			// 重新設置中斷狀態
			Thread.currentThread().interrupt();
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(StackTraceUtil.logTpiShortStackTrace(e));
		}

		if (getOnlineFlagByCache()) {
			sendLogPacket(log);
		}
	}

	public void error(String logMsg) {
		if (!error_flag) {
			return;
		} // 不寫檔 + 不送 keeper

		TPILogInfo log = new TPILogInfo();
		try {
			log.setLevel("ERROR");
			log.getLogMsg().append(Thread.currentThread().getName() + "::" + logMsg);
			String msg = getLotMsg(log);
			TPIFileLoggerQueue.put(TPIFileLoggerQueue.ERROR, msg);
		} catch (InterruptedException e) {
			// 重新設置中斷狀態
			Thread.currentThread().interrupt();
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(StackTraceUtil.logTpiShortStackTrace(e));
		}

		if (getOnlineFlagByCache()) {
			sendLogPacket(log);
		}
	}

	public void trace(String logMsg) {
		if (!trace_flag) {
			return;
		} // 不寫檔 + 不送 keeper
		if (traceMsg.isEmpty() || traceMsg.length() >= 1) { // 原6000
			TPILogger.tl.executorService.schedule(() -> trace(), 4, TimeUnit.SECONDS); // 延遲 4 秒後
		}

		traceMsg.append(Thread.currentThread().getName() + "::");
		String lineNumberStr = TPILogInfo.getLineNumber2();
		if (delayTRACELineNumberStr.equals(lineNumberStr)) {
			traceMsg.append("\t..." + logMsg + "\n");
		} else {
			delayTRACELineNumberStr = lineNumberStr;
			traceMsg.append("\n\t...[ " + lineNumberStr + "] => \n\t..." + logMsg + "\n");
		}

		if (traceMsg.length() > 1000) {
			trace(); // 大於字數就可以直印了
		}
	}

	public void trace() {
		if (!trace_flag) {
			return;
		} // 不寫檔 + 不送 keeper
		if (!StringUtils.hasLength(traceMsg)) {
			return;
		}
		TPILogInfo log = new TPILogInfo();
		try {
			log.setLevel("TRACE");
			log.getLogMsg().append(traceMsg.toString()); // 載入 buffer
			traceMsg.delete(0, traceMsg.length()); // 清空 buffer
			delayTRACELineNumberStr = "";
			String msg = getLotMsg(log);
			TPIFileLoggerQueue.put(TPIFileLoggerQueue.TRACE, msg);
		} catch (InterruptedException e) {
			// 重新設置中斷狀態
			Thread.currentThread().interrupt();
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(StackTraceUtil.logTpiShortStackTrace(e));
		}

		if (getOnlineFlagByCache()) {
			sendLogPacket(log);
		}
	}

	public void warn(String logMsg) {
		if (!warn_flag) {
			return;
		} // 不寫檔 + 不送 keeper
		TPILogInfo log = new TPILogInfo();
		try {
			log.setLevel("WARN");
			log.getLogMsg().append(Thread.currentThread().getName() + "::" + logMsg);
			String msg = getLotMsg(log);
			TPIFileLoggerQueue.put(TPIFileLoggerQueue.WARN, msg);
		} catch (InterruptedException e) {
			// 重新設置中斷狀態
			Thread.currentThread().interrupt();
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(StackTraceUtil.logTpiShortStackTrace(e));
		}

		if (getOnlineFlagByCache()) {
			sendLogPacket(log);
		}
	}
	
	// 取得訊息內容
	private String getLotMsg(TPILogInfo log) {
		String userName = TPILogger.lcUserName;
		if(DgrDeployRole.MEMORY.value().equals(getDeployRole())) {
			userName = "["+TPILogger.lcUserName+"]";
		}
		
		return  "\n\t" + userName + "::" +
				"\n\t[" + log.getLine() + "]\n\t" + log.getLogMsg().toString() + "\n";
	}

	private void sendLogPacket(TPILogInfo log) {
		if (lc != null && getTsmpSettingService() != null) {

			// 若loggerLevel沒有初始值就從資料庫取得。
			if (loggerLevel == null) {
				Optional<TsmpSetting> opt_tsmpSetting = getTsmpSettingCacheProxy()
						.findById(TsmpSettingDao.Key.LOGGER_LEVEL);
				if (opt_tsmpSetting.isPresent()) {
					loggerLevel = opt_tsmpSetting.get().getValue();
				}
			}

			TPILogInfoPacket packet = new TPILogInfoPacket(log);
			log.userName = lc.userName;
			lc.send(packet);
		}
	}

	private void connect() {
		tl.info(".............TPILogger.connection()...............");
		while (true) {
			try {
				// wait 10 sec and print msg.
				wait10Sec2ConnectKPServ();

				try {
					// 網路斷掉, SQL 也會連不上
					createLinkerClient();
				} catch (Exception e) {
					// 未連線前無法使用 TPILogger
					LoggerFactory.getLogger(TPILogger.class).error(StackTraceUtil.logStackTrace(e));
					mySleepByCount(20);
					continue;
				}

				TPILogger.lc.paramObj.put(dgrNodeLostContactDaoStr, dgrNodeLostContactDao);

				// 重覆使用先前的 connection 資訊
				if (paramBefore != null) {
					System.out.println("重覆使用先前的 connection 資訊...paramBefore.size(): " + paramBefore.size());
					TPILogger.lc.param = paramBefore;
					paramBefore = null;
				}

				break;

			} catch (InterruptedException ex) {
				// 未連線前無法使用 TPILogger
				LoggerFactory.getLogger(TPILogger.class).error(StackTraceUtil.logStackTrace(ex));

				// Restore interrupted state...
				Thread.currentThread().interrupt();
			} catch (Exception e) {
				// 未連線前無法使用 TPILogger
				LoggerFactory.getLogger(TPILogger.class).error(StackTraceUtil.logStackTrace(e));
			}
		}

		TPILogger.tlDeployRole = deployRole;
		TPILogger.lcUserName = prifixUserName + "(" + instanceId + ")-" + uuid;
		lc.setUserName(TPILogger.lcUserName);

		// wait()
		myWait_DoSetUserName();

		// 傳送 node Info 給 Keeper Server, 這樣才能取得 version
		// sendNodeInfo(); 之後的程序會再做一次, 故這裡不用做了 2024/12/31 (John) 

		// 取得所有的 client monitor info
		lc.send(new RequireAllClientListPacket());

		myWait_RequireAllClientListPacket();

		LinkedList<ClientKeeper> allClientList = (LinkedList<ClientKeeper>) TPILogger.lc.paramObj.get("allClientList");

		// 更新自己的 clientInfo
		if (allClientList != null) {
			for (ClientKeeper clientKeeper : allClientList) {
				if (clientKeeper.getUsername().equals(lc.userName)) {
					TPILogger.lc.param.put(TPILogger.nodeInfo, String.format("%s / IP：%s / PORT：%s",
							clientKeeper.getUsername(), clientKeeper.getIp(), clientKeeper.getPort()));
				}
			}
		}

		StringBuffer msgbuf = new StringBuffer();
		String s = "\r\n" + " __  ___ .______       ______  __       __  .___________.\r\n"
				+ "|  |/  / |   _  \\     /      ||  |     |  | |           |\r\n"
				+ "|  '  /  |  |_)  |   |  ,----'|  |     |  | `---|  |----`\r\n"
				+ "|    <   |   ___/    |  |     |  |     |  |     |  |     \r\n"
				+ "|  .  \\  |  |        |  `----.|  `----.|  |     |  |     \r\n"
				+ "|__|\\__\\ | _|         \\______||_______||__|     |__|     \r\n"
				+ "                                                         \r\n" + "";

		msgbuf.append(s);
		msgbuf.append("\n...This Client Connect to dgr-keeper server [Set UserName SUCCESS]");
		msgbuf.append("\n...Keeper Server IP = " + lc.serverIP);
		msgbuf.append("\n...Keeper Server port = " + lc.port);
		msgbuf.append("\n...Name = " + prifixUserName + "(" + instanceId + ")-" + uuid);
		msgbuf.append("\n...NodeInfo = " + lc.param.get(TPILogger.nodeInfo));
		msgbuf.append("\n______________________________________________________");
		msgbuf.append("\n");
		TPILogger.tl.info(msgbuf.toString());
		TPILogger.lc.paramObj.put("RefreshMem", getApptJobDispatcher());
		lc.paramObj.put("GenericCache", genericCache);
		lc.paramObj.put("DaoGenericCache", daoGenericCache);
		TPILogger.lc.paramObj.put("changeDbInfo", getChangeDbConnInfoService());
		TPILogger.lc.paramObj.put(DBINFOMAP_TITLE, dbInfoMap);

		createThreadStarter();
		
		// 控制後續的非首次連線
		TPILogger.isFirstConnection = false;
				
	}

	private void createThreadStarter() {
		TPILogger.threadCreateTiem = System.currentTimeMillis();
		// 定期跟 KP Server回報主機的資訊。
		// Memory Role 不啟動
		report2Keeper(TPILogger.threadCreateTiem);
		
		// tsmp_monitor_log 定期寫入 ES.
		// Memory Role 不啟動
		report2ESLog(TPILogger.threadCreateTiem);
		
		// 定期跟 KP Server回報website流量。
		// Memory Role 不啟動
		report2KeeperByWebsiteThroughput(TPILogger.threadCreateTiem);
		
		// 排程器啟動程式
		startRunScheduler();
		
		// 判斷aws環境 呼叫aws 的計量API.
		// Memory Role 不啟動
		callAwsRegisterUsage();
		
		if (tomcatGraceful) {
			startTomcat();
		}
		
		// In-Memory, 系統啟動時,初始化 Landing 的最後更新時間為現在時間, 以使 GTW(In-Memory) 做同步
		initialLandingUpdateTime();
		
		// In-Memory, 加上定期呼叫 Landing API 內容
		// 定期呼叫 Landing API,以更新 GTW(In-Memory)的資料
		// Landing Role 不啟動
		if (TPILogger.isFirstConnection == false) {
			// 首次連 keeper 時不啟動
			inMemoryGtwRefresh2Landing(TPILogger.threadCreateTiem);
		}
		
		if (allowWriteElastic) {
			// 產生 CloseableHttpClient 並啟動 ESLogBuffer
			EsHttpClient.getInstance().getESLogBuffer(diskFreeThreshHold, deletePercent, allowWriteElastic);
			
			// 啟動 disk 監控器, 考慮只在消費者(master)中啟動監控器，而生產者(slave)只讀取狀態（這樣只有一個實例在執行檢查）
			long maxDirSize = getConfiguredMaxDirSize(); // 例如 5GB, 10MB, 2TB
			int maxFiles = getConfiguredMaxFiles(); // 例如 50,000
			DiskSpaceMonitor.getInstance(maxDirSize, maxFiles).start();
		}

		updateESLogFlag();
		
		initHttpUtil();
	}
	
	public void initHttpUtil() {
		if (TPILogger.isFirstConnection) {
			HttpUtil.SENSITIVE_ENABLE = this.sensitiveEnable;
			HttpUtil.SENSITIVE_KEYWORD = this.sensitiveKeyword;
		}
	}
	
	public void updateESLogFlag() {
		try {
			DgrApiLog2ESQueue.esUrlCheckConnection = getTsmpSettingService().getVal_ES_CHECK_CONNECTION();
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logTpiShortStackTrace(e));
		}
		
		try {
			ESLogBuffer.enableRetry = getTsmpSettingService().getVal_ES_LOGFILE_FAIL_RETRY();
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logTpiShortStackTrace(e));
		}
		
		
		TPILogger.tl.info(String.format("\n...ES_CHECK_CONNECTION=[%b] ,ES_LOGFILE_FAIL_RETRY=[%b]", 
				DgrApiLog2ESQueue.esUrlCheckConnection, 
				ESLogBuffer.enableRetry ));
	}

	private void createLinkerClient() throws UnknownHostException, IOException {
		String dgrKeeper_ip = getTsmpSettingCacheProxy().findById(TsmpSettingDao.Key.DGRKEEPER_IP)
				.map(TsmpSetting::getValue).orElse("default_ip"); // 提供一個預設值或拋出一個自定義異常

		String dgrKeeper_portStr = getTsmpSettingCacheProxy().findById(TsmpSettingDao.Key.DGRKEEPER_PORT)
				.map(TsmpSetting::getValue).orElse("default_port"); // 提供一個預設值或拋出一個自定義異常

		int dgrKeeper_port = Integer.parseInt(dgrKeeper_portStr);

		// role = Memory 採用 127.0.0.1
		if (DgrDeployRole.MEMORY.value().equalsIgnoreCase(deployRole)) {
			TPILogger.tl.info("\n...I am [Memory] Role, DGR Keeper IP = [127.0.0.1]\n");
			dgrKeeper_ip = "127.0.0.1";
		}
		
		// role = 127db (取代客戶自建RDB,以 DGR 做為 RDB), 採用 127.0.0.1 
//		if (DgrDeployRole.DB127.value().equalsIgnoreCase(deployRole)) {
//			TPILogger.tl.info("I am [127db] Role, DGR Keeper IP = [127.0.0.1] ");
//			dgrKeeper_ip = "127.0.0.1";
//		}

		// role = Memory 另外使用一個 +10 的 port
		if (DgrDeployRole.MEMORY.value().equalsIgnoreCase(deployRole)) {
			TPILogger.tl.info("\n...I am [Memory] Role, DGR Keeper Port + [10]\n");
			dgrKeeper_port = dgrKeeper_port + 10;
		}
		
		// 第一次啟動也是採用 127.0.0.1
		if (isFirstConnection == true) {
			TPILogger.tl.info("\n\n...isFirstConnection == true, DGR Keeper IP = [127.0.0.1]\n\n");
			dgrKeeper_ip = "127.0.0.1";
		} 

		TPILogger.lc = new LinkerClient(dgrKeeper_ip, dgrKeeper_port, Role.admin, lcNofify);
	}

	private void wait10Sec2ConnectKPServ() throws InterruptedException {
		for (int i = 0; i < 10; i++) {
			if (paramBefore == null || hasSecondConnectionStarting == true)
				break;
			Thread.sleep(1000);
			System.out.println("wait SQL_RDB or KeeperServer connection...." + i);
		}
		
		hasSecondConnectionStarting = false;
	}

	private Object inMemoryGtwRefresh2LandingLock = new Object();

	private void inMemoryGtwRefresh2Landing(long threadCreateTiem01) {

		// role 不是 Memory 就不跑 Thread
		if (DgrDeployRole.MEMORY.value().equalsIgnoreCase(deployRole) == false) {
			TPILogger.tl.info("\n...I am not a [Memory] Role rather than [" + deployRole + "]\n");
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				
				// I'm Memory Role
				TPILogger.tl.info("\n...My role is [" + deployRole + "]....inMemoryGtwRefresh2Landing()... \n");
				
				long threadCreateTiem = threadCreateTiem01 + 2500;
				while (lc != null) {
					if (TPILogger.threadCreateTiem > threadCreateTiem) {
						TPILogger.tl.info("\n...inMemoryGtwRefresh2Landing()=" + Thread.currentThread().getName() + "...EXIT...");
						return; //防止 keeper re-connection 造成之前的 Thread 仍然沒有消減
					}
					try {
//						Thread.sleep(1000);
						synchronized (inMemoryGtwRefresh2LandingLock) {
							inMemoryGtwRefresh2LandingLock.wait(deployIntervalMs); // 取代原來的 sleep(1000), 以免阻塞;
						}
						
						// 啟動第一次連線 client 後, lc 有存到資料, 才開始調用
						if (TPILogger.lc == null) {
							continue;
						}
						String port = TPILogger.lc.param.get("server.port"); // TsmpCoreTokenInitializerInit.init() 已有 put
						if (port == null) {
							continue;
						}

						// In-Memory 調用 Landing 的 API
						nodeInfoPacket = sendNodeInfo();
						
						String threadStatus = "...No Enterprise Service...";
						if (undertowMetricsService != null) {
							threadStatus = undertowMetricsService.webserverProperties();
						}
						UndertowMetricsPacket undertowMetricsPacket = new UndertowMetricsPacket(lc.userName, threadStatus);
						
						String uriStatus = GatewayFilter.fetchUriHistoryList();
						UrlStatusPacket urlStatusPacket = new UrlStatusPacket(lc.userName, uriStatus);
						
						RealtimeDashboardPacket realtimeDashboardPacket = realtimeDashboardService.getPacket(lc.userName);
						
						inMemoryGtwRefresh2LandingService.landingGtw(nodeInfoPacket, undertowMetricsPacket, urlStatusPacket, realtimeDashboardPacket);
					} catch (InterruptedException e) {
						TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
						Thread.currentThread().interrupt();
						return;
					} catch (Exception e) {
						TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
						return;
					}
				}
			}

		}).start();
	}

	/**
	 * for In-Memory 流程, <br>
	 * 系統啟動時,初始化 Landing 的 lastUpdateTimeXXX = 現在時間(long型態), <br>
	 * 以使 GTW(In-Memory) 執行同步資料 <br>
	 */
	private void initialLandingUpdateTime() {
		// role 不是 Landing 就不執行
		if (!DgrDeployRole.LANDING.value().equalsIgnoreCase(deployRole)) {
			return;
		}

		Long nowTime = System.currentTimeMillis();
		TPILogger.updateTime4InMemory(DgrDataType.API.value(), nowTime);
		TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value(), nowTime);
		TPILogger.updateTime4InMemory(DgrDataType.SETTING.value(), nowTime);
		TPILogger.updateTime4InMemory(DgrDataType.TOKEN.value(), nowTime);
	}

	private static Tomcat tomcat;

	private void startTomcat() {

		if (TPILogger.isFirstConnection == false) {
			// 非首次連線
			TPILogger.tl.info("\n...startTomcat()=" + Thread.currentThread().getName() + "...No re-start...");
			return; //防止 keeper re-connection 產生新的 Thread
		}

		new Thread(() -> {
			tomcat = new Tomcat();
			tomcat.setPort(8081);

			// HTTPS
			Connector httpsConnector = createSslConnector();
			tomcat.getService().addConnector(httpsConnector);

			// 創建 Context
			Context ctx = tomcat.addContext("", null);

			// proxy
			Tomcat.addServlet(ctx, "proxyServlet", new ProxyServlet());
			ctx.addServletMappingDecoded("/", "proxyServlet");

			// 啟動 Tomcat
			try {
				tomcat.start();
			} catch (Exception e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
				return;
			}
			System.out.printf("Tomcat started on ports  %s (HTTPS)\r\n", httpsConnector.getPort());

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					stopTomcatGracefully();
				} catch (Exception e) {
					TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
				}
			}));

			tomcat.getServer().await();
		}).start();

	}

	private static void stopTomcatGracefully() throws Exception {
		if (tomcat != null) {
			System.out.println("Stopping Tomcat gracefully...");

			// 禁止新請求
			for (Service service : tomcat.getServer().findServices()) {
				for (Connector connector : service.findConnectors()) {
					connector.pause();
					connector.getProtocolHandler().closeServerSocketGraceful();
				}
			}
			
			awaitTomcatStop();
			
			System.out.println("Tomcat stopped.");
		}
	}

	private static void awaitTomcatStop() throws InterruptedException {
		try (ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
		    Thread thread = new Thread(r);
		    thread.setName("graceful-shutdown-Thread");
		    return thread;
		})) {
			// 執行 executor 內容
		    executor.submit(() -> {
		        tomcat.getServer().await();
		    });

		    executor.shutdown();
		    if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
		        executor.shutdownNow();
		    }
		    //當 try 區塊結束時，ExecutorService 會自動被關閉(自動執行shutdown)，即使發生異常也能確保資源被釋放
		}
	}

	private Connector createSslConnector() {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		connector.setScheme("https");
		connector.setSecure(true);
		int sslport = PORT + 10;
		connector.setPort(sslport);

		Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
		protocol.setSSLEnabled(serverConfigProperties.getSsl().isEnabled());
		SSLHostConfig sslconfig = new SSLHostConfig();
		SSLHostConfigCertificate shc = new SSLHostConfigCertificate(sslconfig, SSLHostConfigCertificate.Type.RSA);

		String filepath = "../" + serverConfigProperties.getSsl().getKeyStore();
		shc.setCertificateKeystoreFile(filepath);
		shc.setCertificateKeystorePassword(serverConfigProperties.getSsl().getKeyStorePassword());
		shc.setCertificateKeystoreType(serverConfigProperties.getSsl().getKeyStoreType());
		shc.setCertificateKeyAlias(serverConfigProperties.getSsl().getKeyAlias());
		sslconfig.addCertificate(shc);

		connector.addSslHostConfig(sslconfig);
		return connector;
	}

	private void callAwsRegisterUsage() {
		if (TPILogger.isFirstConnection == false) {
			// 非首次連線
			TPILogger.tl.info("\n...callAwsRegisterUsage()=" + Thread.currentThread().getName() + "...No re-start...");
			return; //防止 keeper re-connection 產生新的 Thread
		}

		Boolean result = false;
		StringBuffer msgbuf = new StringBuffer();
		msgbuf.append("\n==================== [START]  call AWS RegisterUsage ====================");

		// 確認 Injection 狀態
		msgbuf.append("\n ... * AWS Lib IoC Object Status: " + getAwsApiService().getDgrAWSComponent() + " * ... \n");

		String productCode = null;
		// 判斷LICENSE中的 env 是否AWS 是才呼叫 AWS的計量服務
		String license = getTsmpSettingService().getVal_TSMP_LICENSE_KEY();
		if (StringUtils.hasLength(license)) {
			licenseUtilBase.initLicenseUtil(license, null);
			String env = licenseUtilBase.getValue(license, LicenseType.env);
			if (StringUtils.hasLength(env) && env.equals(LicenseEnvType.AWS.name())) {
				msgbuf.append("\nenv : " + env);
				try {
					// 取得系統變數內的PRODUCT_CODE
					productCode = System.getenv("PRODUCT_CODE");
					if (!StringUtils.hasText(productCode)) {
						TPILogger.tl.error("Could not find PRODUCT_CODE");
					}
					msgbuf.append("\nproductCode = " + productCode);
					Integer publicKeyVersion = null;
					// 取得系統變數內的publicKeyVersion
					String publicKeyVersionStr = System.getenv("publicKeyVersion");

					if (!StringUtils.hasText(publicKeyVersionStr)) {
						TPILogger.tl.error("Could not find publicKeyVersion");
					}
					msgbuf.append("\npublicKeyVersion = " + publicKeyVersionStr);
					try {
						publicKeyVersion = Integer.valueOf(publicKeyVersionStr);
					} catch (Exception e) {
						TPILogger.tl.error("Could not convert publicKeyVersion to integer");
					}

					// 取得系統變數內的nonce (可以沒有)
					String nonce = null;

					nonce = System.getenv("nonce");
					msgbuf.append("\nnonce = " + nonce);

					// 2024 / 6/ 15 完成 IoC 注入, git commit:"c3338a0", "9191cff"
					result = getAwsApiService().awsApi(productCode, publicKeyVersion, nonce);
					String call_reg_result = getAwsApiService().getRegisterUsageResult();
					msgbuf.append("\n call RegisterUsage result: " + call_reg_result);
					// msgbuf.append("\n call RegisterUsage result: " +
					// AwsApiService.registerUsageResult.toString());

				} catch (Exception e) {
					result = false;
					TPILogger.tl.error("Unable to call RegisterUsage\n" + StackTraceUtil.logStackTrace(e));

				} finally {
					msgbuf.append("\n call RegisterUsage valid result: " + result);
				}
			} else {
				msgbuf.append("\n ... * No AWS * ...");
			}
		} else {
			msgbuf.append("\n ... # No AWS # ...");
		}
		msgbuf.append("\n==================== [END]  call AWS RegisterUsage ======================\n\n");
		TPILogger.tl.info(msgbuf.toString());
	}

	/**
	 * 排程器啟動程式
	 */
	private void startRunScheduler() {
		if (TPILogger.isFirstConnection == false) {
			// 非首次連線
			TPILogger.tl.info("\n...startRunScheduler()=" + Thread.currentThread().getName() + "...No re-start...");
			return; //防止 keeper re-connection 產生新的 Thread
		}
		
		boolean isSchedulerEnabled = isSchedulerEnabled();
		if (isSchedulerEnabled
				&& (this.scheduler_t_refresh == null
						|| (this.scheduler_t_refresh != null && this.scheduler_t_refresh.isInterrupted()))
				&& (this.scheduler_t_check == null
						|| (this.scheduler_t_check != null && this.scheduler_t_check.isInterrupted()))) {

			// [排程]每 30 分撈 DB 排程到 MemList
			refreshDB2MemList();

			// [排程]每 1 秒檢查 MemList (原 ApptJobDispatcher)
			checkMemList();
		} else {
			StringBuffer sbf = new StringBuffer();
			// info("未啟用排程器, 請設定 service.scheduler.appt-job.enable=true");
			sbf.append("Scheduler is not enabled, please set 'service.scheduler.appt-job.enable=true'");
			sbf.append("\n");
			sbf.append("isSchedulerEnabled: " + isSchedulerEnabled);
			sbf.append("\n");
			sbf.append("this.scheduler_t_refresh: " + this.scheduler_t_refresh);
			sbf.append("\n");
			if (this.scheduler_t_refresh != null) {
				sbf.append("this.scheduler_t_refresh: " + this.scheduler_t_refresh.isInterrupted());
				sbf.append("\n");
			}
			info(sbf.toString());
		}
	}

	/**
	 * '排程器' 是否有啟動
	 */
	private boolean isSchedulerEnabled() {
		boolean isSchedulerEnabled = false;

		String strVal = getServiceConfig().get("scheduler.appt-job.enable");
		try {
			isSchedulerEnabled = Boolean.valueOf(strVal);
		} catch (Exception e) {
			warn("Error value of " + strVal + ", set to default " + isSchedulerEnabled);
		}

		return isSchedulerEnabled;
	}

	/**
	 * [排程]每 30 分撈 DB 排程到 MemList
	 */
	private Object refreshDB2MemListLock = new Object();

	private void refreshDB2MemList() {
		class PeriodGetter {
			long get() {
				long period = 1800000L; // 30分
				String strVal = getServiceConfig().get("job-dispatcher.period.ms");
				try {
					period = Long.valueOf(strVal);
				} catch (Exception e) {
					warn("Error value of " + strVal + ", set to default " + period);
				}
				return period;
			}
		}

		final long period = new PeriodGetter().get();

		this.scheduler_t_refresh = new Thread(new Runnable() {
			@Override
			public void run() {
				// i這一段是為了sonarQube而修改,但要保證永遠跑迴圈
				int i = 0;
				while (i < Integer.MAX_VALUE) {
					try {
						i++;
						if (i > 1000) {
							i = 0;
						}
						getApptJobDispatcher().refreshJobCache();

//						Thread.sleep(period);
						synchronized (refreshDB2MemListLock) {
							refreshDB2MemListLock.wait(period); // default to 30 min(s)
						}
					} catch (InterruptedException e) {
						error(StackTraceUtil.logStackTrace(e));
						// Restore interrupted state...
						Thread.currentThread().interrupt();
					} catch (Exception e) {
						error(StackTraceUtil.logStackTrace(e));
					}
				}
			}
		}, "ApptJobDispatcher");
		this.scheduler_t_refresh.start();
	}

	/**
	 * [排程]每 1 秒檢查 MemList (原 ApptJobDispatcher)
	 */
	private Object checkMemListLock = new Object();

	private void checkMemList() {
		this.scheduler_t_check = new Thread(new Runnable() {
			@Override
			public void run() {
				// i這一段是為了sonarQube而修改,但要保證永遠跑迴圈
				int i = 0;
				while (i < Integer.MAX_VALUE) {
					try {
						i++;
						if (i > 1000) {
							i = 0;
						}
						getApptJobDispatcher().run();
//						Thread.sleep(1000); // 1 sec
						// 檢查是否因為 Sleep 導致Blocking, 目前測試有效
						synchronized (checkMemListLock) {
							checkMemListLock.wait(1000); // 1 sec
						}
					} catch (InterruptedException e) {
						error(StackTraceUtil.logStackTrace(e));
						// Restore interrupted state...
						Thread.currentThread().interrupt();
					} catch (Exception e) {
						error(StackTraceUtil.logStackTrace(e));
					}
				}
			}
		}, "ApptJobDispatcher");
		this.scheduler_t_check.start();
	}

	private Object report2ESLogLock = new Object();

	private void report2ESLog(long threadCreateTiem01) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				long threadCreateTiem = threadCreateTiem01;
				while (lc != null) {
					if (TPILogger.threadCreateTiem > threadCreateTiem) {
						TPILogger.tl.info("\n...report2ESLog()=" + Thread.currentThread().getName() + "...EXIT...");
						return;  //防止 keeper re-connection 造成之前的 Thread 仍然沒有消減
					}
					try {
//						Thread.sleep(1000);
						synchronized (report2ESLogLock) {
							report2ESLogLock.wait(1000); // 取代原來的 sleep(1000), 以免阻塞;
						}
						
						// 啟動第一次連線 client 後, lc 有存到資料, 才開始調用
						if (TPILogger.lc == null) {
							continue;
						}
						String port = TPILogger.lc.param.get("server.port"); // TsmpCoreTokenInitializerInit.init() 已有 put
						if (port == null) {
							continue;
						}
						
						// 監控Host,寫入ES
						getMonitorHostService().execMonitor();
					} catch (InterruptedException e) {
						TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
						// Restore interrupted state...
						Thread.currentThread().interrupt();
						return;
					} catch (Exception e) {
						TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
						return;
					}
				}
			}
		}).start();
	}

	/**
	 * 定期跟Server回報主機的資訊。
	 */
	private Object report2KeeperLock = new Object();

	private void report2Keeper(long threadCreateTiem01) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				long threadCreateTiem = threadCreateTiem01;
				while (lc != null) {
					if (TPILogger.threadCreateTiem > threadCreateTiem) {
						TPILogger.tl.info("\n...report2Keeper()=" + Thread.currentThread().getName() + "...EXIT...");
						return; //防止 keeper re-connection 造成之前的 Thread 仍然沒有消減
					}
//					TPILogger.tl.info("\nreport2Keeper: serverIP = " + lc.serverIP + "LC Ref: " + lc + "\n");
					try {
//						Thread.sleep(1000);
						synchronized (report2KeeperLock) {
							report2KeeperLock.wait(1000); // 取代原來的 sleep(1000), 以免阻塞;
						}
						// 傳送 Node Info 給 Keeper server
						nodeInfoPacket = sendNodeInfo();
						lc.send(nodeInfoPacket);
						lc.send(new RequireAllClientListPacket());
						String threadStatus = "...No Enterprise Service...";
						if (undertowMetricsService != null) {
							// online console 'Thread Status' data
							threadStatus = undertowMetricsService.webserverProperties();
						}
						lc.send(new UndertowMetricsPacket(lc.userName, threadStatus));
						
						// online console 'URI Status' data
						String uriStatus = GatewayFilter.fetchUriHistoryList();
						lc.send(new UrlStatusPacket(lc.userName, uriStatus));
						RealtimeDashboardPacket realtimeDashboardPacket = realtimeDashboardService.getPacket(lc.userName);
						if(realtimeDashboardPacket != null) {
							lc.send(realtimeDashboardPacket);
						}
					} catch (InterruptedException e) {
						TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
						// Restore interrupted state...
						Thread.currentThread().interrupt();
						return;
					} catch (Exception e) {
						TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
						return;
					}
				}
			}

		}).start();
	}

	private NodeInfoPacket sendNodeInfo() {
		// 監控Host CPU
		DpaaSystemInfo cpuInfo = getMetrics();

		// [一級快取]的工作數量
		long main = jobHelper.getJobQueueSize(1);

		// [二級快取]的工作數量
		long deferrable = jobHelper.getJobQueueSize(2);

		// [二級快取]的工作數量
		long refresh = jobHelper.getJobQueueSize(3);

		NodeInfoPacket nodeInfoPacket = new NodeInfoPacket();
		nodeInfoPacket.cpu = cpuInfo.getCpu() + "";
		nodeInfoPacket.mem = cpuInfo.getMem() + "";
		nodeInfoPacket.metaSpace = cpuInfo.getMetaspacePercent();
		nodeInfoPacket.h_used = cpuInfo.getHused() + "";
		nodeInfoPacket.h_free = cpuInfo.getHfree() + "";
		nodeInfoPacket.h_total = cpuInfo.getHtotal() + "";

		Map<String, Object> map = dbInfoMap;
		String dbConnect = dataSource.getUsername();
		nodeInfoPacket.dbConnect = dbConnect + "";
		if (map != null) {
			if (map.get("dbInfo") != null) {
				nodeInfoPacket.dbInfo = ((JsonNode) map.get("dbInfo")).toPrettyString();
			}
		}

		// 獲取API每秒轉發吞吐量，發送到keeper server。
		// 獲取目前秒數，因為有可能在GatewayFilter設定Api Req
		// Throughput的秒數為28，在TPILogger取出的秒數是27會有秒差問題，
		// 所以TPILogger取出秒數要減2秒。
		getTps(nodeInfoPacket);

		nodeInfoPacket.main = main + "";
		nodeInfoPacket.deferrable = deferrable + "";
//		nodeInfoPacket.refresh = refresh + ", (buff2ndWaitCount=" + DeferrableJobManager.buff2ndWaitCount.get() +")";
		nodeInfoPacket.refresh = refresh + "";
		nodeInfoPacket.updateTime = DateTimeUtil.dateTimeToString(DateTimeUtil.now(), DateTimeFormatEnum.西元年月日時分秒)
				.orElse("");
		nodeInfoPacket.startTime = startTime + "";
		nodeInfoPacket.serverPort = TPILogger.lc.param.get("server.port");
		nodeInfoPacket.serverServletContextPath = TPILogger.lc.param.get("server.servlet.context-path");
		nodeInfoPacket.serverSslEnalbed = TPILogger.lc.param.get("server.ssl.enabled");
		nodeInfoPacket.springProfilesActive = TPILogger.lc.param.get("spring.profiles.active");
		nodeInfoPacket.keeperServerIp = lc.serverIP;
		nodeInfoPacket.keeperServerPort = lc.port + "";
		nodeInfoPacket.rcdCacheSize = genericCache.getCacheMap().size() + "";
		nodeInfoPacket.daoCacheSize = daoGenericCache.getCacheMap().size() + "";
		nodeInfoPacket.fixedCacheSize = CommForwardProcService.fixedCacheMap.size() + "";
		nodeInfoPacket.webLocalIP = lc.getLocalIpAdress();
		nodeInfoPacket.fqdn = lc.getLocalIpFQDN();
		nodeInfoPacket.ES_Queue = DgrApiLog2ESQueue.ES_LoggerQueue.size() + " (-" + DgrApiLog2ESQueue.abortNum + ")(-"+ ESLogBuffer.abortNum + ")";
		nodeInfoPacket.RDB_Queue = DgrApiLog2RdbQueue.rdb_LoggerQueue.size() + " (-"+ DgrApiLog2RdbQueue.abortNum +")";

		nodeInfoPacket.lastUpdateTimeAPI = String.valueOf(lastUpdateTimeAPI.get());
		nodeInfoPacket.lastUpdateTimeClient = String.valueOf(lastUpdateTimeClient.get());
		nodeInfoPacket.lastUpdateTimeSetting = String.valueOf(lastUpdateTimeSetting.get());
		nodeInfoPacket.lastUpdateTimeToken = String.valueOf(lastUpdateTimeToken.get());

		// 取得目前執行的version資訊
		nodeInfoPacket.version = "0.0.0";
		for (String str : logStartingMsg) {
			if (str.indexOf("dgrv4-gateway-") != -1 && str.indexOf(".jar") != -1) {
				String version = str.substring(str.indexOf("dgrv4-gateway-"), str.indexOf(".jar") + 4);
				nodeInfoPacket.version = version;
			}
		}

		long endTime = System.currentTimeMillis();
		nodeInfoPacket.upTime = DateTimeUtil.secondsToDaysHoursMinutesSeconds(endTime - startTime);

		return nodeInfoPacket;
//		lc.send(nodeInfoPacket);
	}
	
	private void getTps(NodeInfoPacket nodeInfoPacket) {
		
		long currentTimeMillis = System.currentTimeMillis();
		int currentSeconds = ((int) (currentTimeMillis / 1000)) - 2;

		synchronized (GatewayFilter.throughputObjLock) {
			// 重置API發吞吐量
			nodeInfoPacket.api_ReqThroughputSize = 0 + "";
			nodeInfoPacket.api_RespThroughputSize = 0 + "";
			// 取得API發吞吐量
			if (GatewayFilter.apiReqThroughput.containsKey(currentSeconds)) {
				nodeInfoPacket.api_ReqThroughputSize = GatewayFilter.apiReqThroughput.get(currentSeconds) + "";
			}
			if (GatewayFilter.apiRespThroughput.containsKey(currentSeconds)) {
				nodeInfoPacket.api_RespThroughputSize = GatewayFilter.apiRespThroughput.get(currentSeconds) + "";
			}
		}
	}
	
	public String getReqTps() {
		return nodeInfoPacket.api_ReqThroughputSize;
	}
	
	public String getRespTps() {
		return nodeInfoPacket.api_RespThroughputSize;
	}

	/**
	 * 定期跟Server回報website的流量。
	 */
	private Object report2KeeperByWebsiteThroughputLock = new Object();

	private void report2KeeperByWebsiteThroughput(long threadCreateTiem01) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				long threadCreateTiem = threadCreateTiem01;
				while (lc != null) {
					if (TPILogger.threadCreateTiem > threadCreateTiem) {
						TPILogger.tl.info("\n...report2KeeperByWebsiteThroughput()=" + Thread.currentThread().getName() + "...EXIT...");
						return; //防止 keeper re-connection 造成之前的 Thread 仍然沒有消減
					}
					try {
//						Thread.sleep(1000);
						synchronized (report2KeeperByWebsiteThroughputLock) {
							report2KeeperByWebsiteThroughputLock.wait(1000);
						}

						// 傳送 WebsiteThroughput 給 Keeper server
						sendWebsiteThroughput();
					} catch (InterruptedException e) {
						TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
						// Restore interrupted state...
						Thread.currentThread().interrupt();
						return;
					} catch (Exception e) {
						TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
						return;
					}
				}
			}
		}).start();
	}

	private void sendWebsiteThroughput() throws JsonProcessingException {
		if (this.websiteService.targetThroughputMap.size() > 0) {
			Map<Long, Map<String, Map<String, Map<String, Integer>>>> sendDataMap = null;
			try {
				long nowTimestampSec = System.currentTimeMillis() / 1000;
				// 將N秒內將targetUrl最新的資料來傳送,目前只抓前1秒資料,所以迴圈只跑一次
				for (int i = 1; i < 2; i++) {
					nowTimestampSec = nowTimestampSec - i;
					Map<String, Map<String, Map<String, Integer>>> clientDataMap = this.websiteService.targetThroughputMap
							.get(nowTimestampSec);
					if (clientDataMap != null) {
						if (sendDataMap == null) {
							sendDataMap = new HashMap<>();
							sendDataMap.put(nowTimestampSec, clientDataMap);
						} else {
							// 印象中在跑迴圈時,若對該物件有增刪動作(WebsiteService)會發生錯誤,所以copy給新的物件
							Map<String, Map<String, Map<String, Integer>>> websiteNameMap = new HashMap<>(
									clientDataMap);
							for (String websiteName : websiteNameMap.keySet()) {
								Map<String, Map<String, Integer>> targetUrlMap = websiteNameMap.get(websiteName);
								for (String targetUrl : targetUrlMap.keySet()) {
									Map<String, Integer> typeMap = targetUrlMap.get(targetUrl);

									Map<String, Map<String, Map<String, Integer>>> sendWebsiteNameMap = sendDataMap
											.get(nowTimestampSec + i);
									if (sendWebsiteNameMap != null) {// 不應該會發生==null的情況,所以沒else
										Map<String, Map<String, Integer>> sendTargetUrlMap = sendWebsiteNameMap
												.get(websiteName);
										if (sendTargetUrlMap != null) {
											Map<String, Integer> sendTypeMap = sendTargetUrlMap.get(targetUrl);
											if (sendTypeMap == null) {
												sendTargetUrlMap.put(targetUrl, typeMap);
											}
										} else {
											sendWebsiteNameMap.put(websiteName, targetUrlMap);
										}
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			}
			WebsiteTargetThroughputPacket packet = new WebsiteTargetThroughputPacket();
			String strJson = null;
			if (sendDataMap != null) {
				strJson = objectMapper.writeValueAsString(sendDataMap);
			}
			packet.targetThroughputJson = strJson;
			lc.send(packet);
		}
	}

	/**
	 * Get CPU / Mem info
	 * 使用單例模式（Singleton Pattern）確保 DpaaSystemInfoHelper 只會被創建一次
	 * @return
	 */
	protected DpaaSystemInfo getMetrics() {
		return DpaaSystemInfoHelper.getInstance().getCachedSystemInfo();
	}

	/* 由 cache 取 onlineConsole 值 */
	public boolean getOnlineFlagByCache() {
		boolean onlineFlagVal = false;
		if (getTsmpSettingService() == null) {
			return false; // 啟動階段還不能使用
		}

		// 為了避免 GatewayController 發生 504 中斷了流程, 所以強制接收後傳出去 Keeper
		try {
			onlineFlagVal = getTsmpSettingService().getVal_TSMP_ONLINE_CONSOLE();
		} catch (CannotCreateTransactionException e) {
			onlineFlagVal = true; // 強制傳輸到 OnlineConsole
			logger.error(StackTraceUtil.logStackTrace(e));
		}

		return onlineFlagVal;
	}

	// 設定值 450 < Msg 400, [450] ,500
	private boolean isNotAPILogInDebugMsg(String logMsg) {

		if (loggerLevel == null) {
			loggerLevel = TPILogger.tl.currentLoggerLevel();
		}

		int settingLevel = LoggerLevelConstant.getValue(loggerLevel.toUpperCase());
		int debugLevel = LoggerLevelConstant.getValue(LoggerLevelConstant.DEBUG.name());
		if (settingLevel < debugLevel) {
			String msg = logMsg.toUpperCase();
			if (msg.contains(LoggerLevelConstant.APILOG.text())) {
				return false;
			} else {
				return true; // Setting=APILog, but MsgLevel=Debug
			}
		}
		return false;
	}

	private void mySleep(long t) {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	private void mySleepByCount(int t) {
		try {
			for (int i = 0; i < t; i++) {
				Thread.sleep(1000);
				logger.debug(".");
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		logger.debug(".\n\n");
	}

	private void myWait_DoSetUserName() {
		synchronized (DoSetUserName.waitKey) {
			try {
				DoSetUserName.waitKey.wait();
			} catch (InterruptedException e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
				Thread.currentThread().interrupt();
			}
		}
	}

	private void myWait_RequireAllClientListPacket() {
		synchronized (RequireAllClientListPacket.waitKey) {
			try {
				RequireAllClientListPacket.waitKey.wait();
			} catch (InterruptedException e) {
				TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
				Thread.currentThread().interrupt();
			}
		}
	}

	/**
	 * for In-Memory 流程, <br>
	 * 記錄 Landing 端,AC 資料的最後異動時間, <br>
	 * 當以下 table 做異動(CUD)時,則更改對應的 lastUpdateTimeXXX = 現在時間 (long型態), <br>
	 * 1.Client 相關資料 <br>
	 * 2.API 相關資料 <br>
	 * 3.Setting <br>
	 * 
	 * @param dataType API / Client / Setting / Token,使用列舉值方式傳入
	 */
	public static void updateTime4InMemory(String dataType) {
		Long nowTime = System.currentTimeMillis();
		updateTime4InMemory(dataType, nowTime);
	}

	private static void updateTime4InMemory(String dataType, Long nowTime) {
		// 檢查 deployRole 是否有值，且是否為 LANDING 角色
		String deployRole = TPILogger.tl.getDeployRole();
		if (DgrDeployRole.LANDING.value().equalsIgnoreCase(deployRole)) {
			// 如果資料類型為 CLIENT，更新客戶端資料的最後更新時間
			if (DgrDataType.CLIENT.value().equals(dataType)) {
				updateTimestamp(lastUpdateTimeClient, nowTime);
			}
			// 如果資料類型為 API，更新 API 資料的最後更新時間
			else if (DgrDataType.API.value().equals(dataType)) {
				updateTimestamp(lastUpdateTimeAPI, nowTime);
			}
			// 如果資料類型為 SETTING，更新設定資料的最後更新時間
			else if (DgrDataType.SETTING.value().equals(dataType)) {
				updateTimestamp(lastUpdateTimeSetting, nowTime);
			}
			// 如果資料類型為 TOKEN，更新設定資料的最後更新時間
			else if (DgrDataType.TOKEN.value().equals(dataType)) {
				updateTimestamp(lastUpdateTimeToken, nowTime);
			}
			// 如果資料類型未知，則輸出日誌
			else {
				TPILogger.tl.error("In-Memory flow, dataType is invalid: " + dataType);
			}
		}
	}

	/**
	 * 更新時間戳記。
	 * 
	 * @param timestamp 原子性的長整數時間戳記，確保在多線程環境下的安全性。
	 * @param time      新的時間值，將與現有值比較後更新。
	 */
	public static void updateTimestamp(AtomicLong timestamp, long time) {
		// 使用 updateAndGet 方法更新 timestamp，確保新的時間值不會小於當前值
		timestamp.updateAndGet(currentValue -> Math.max(currentValue, time));
	}

	protected ChangeDbConnInfoService getChangeDbConnInfoService() {
		return this.changeDbConnInfoService;
	}

	protected DPB0059Service getDPB0059Service() {
		return this.dPB0059Service;
	}

	protected ApptJobDispatcher getApptJobDispatcher() {
		return this.apptJobDispatcher;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected MonitorHostService getMonitorHostService() {
		return monitorHostService;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected TsmpSettingCacheProxy getTsmpSettingCacheProxy() {
		return this.tsmpSettingCacheProxy;
	}

	protected AwsApiService getAwsApiService() {
		return awsApiService;
	}

	public BotDetectionRuleValidator getBotDetectionRuleValidator() {
		return botDetectionRuleValidator;
	}

	public void setBotDetectionRuleValidator(BotDetectionRuleValidator botDetectionRuleValidator) {
		this.botDetectionRuleValidator = botDetectionRuleValidator;
	}



}
