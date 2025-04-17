package tpi.dgrv4.gateway.service;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.codec.utils.CApiKeyUtils;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.AA0317Service;
import tpi.dgrv4.dpaa.service.AA1120Service;
import tpi.dgrv4.dpaa.service.DPB9921Service;
import tpi.dgrv4.dpaa.vo.AA0317Data;
import tpi.dgrv4.dpaa.vo.AA0317Req;
import tpi.dgrv4.dpaa.vo.AA0317ReqItem;
import tpi.dgrv4.dpaa.vo.AA0317Resp;
import tpi.dgrv4.dpaa.vo.AA1120ExportData;
import tpi.dgrv4.dpaa.vo.AA1120Resp;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.entity.TsmpTokenHistory;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpTokenHistoryDao;
import tpi.dgrv4.gateway.TCP.Packet.ExternalDgrInfoPacket;
import tpi.dgrv4.gateway.TCP.Packet.ExternalRealtimeDashboardPacket;
import tpi.dgrv4.gateway.TCP.Packet.ExternalUndertowMetricsInfosPacket;
import tpi.dgrv4.gateway.TCP.Packet.ExternalUrlStatusPacket;
import tpi.dgrv4.gateway.TCP.Packet.NodeInfoPacket;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.ClientKeeper;
import tpi.dgrv4.gateway.vo.RefreshGTWReq;
import tpi.dgrv4.gateway.vo.RefreshGTWResp;
import tpi.dgrv4.tcp.utils.packets.RealtimeDashboardPacket;
import tpi.dgrv4.tcp.utils.packets.UndertowMetricsPacket;
import tpi.dgrv4.tcp.utils.packets.UrlStatusPacket;

@Service
public class RefreshGTWService {
	private AA1120Service aA1120Service;
	private CApiKeyService capiKeyService;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpApiDao tsmpApiDao;
	private AA0317Service aA0317Service;
	private DPB9921Service dpb9921Service;
	private TsmpTokenHistoryDao tsmpTokenHistoryDao;
	private TsmpClientDao tsmpClientDao;

	private final static String USERNAME = "manager";
	
	// 預編譯的正則表達式，用於匹配協定+主機名和可選的端口
	private final Pattern urlPattern = Pattern.compile("^(https?)://([^:/]+)(?::(\\d+))?");
	
	// 利用 TPILogger 把 msgLog 輸出到 Online Console, 效能問題可以利用 ThreadLocal<StringBuilder> 實作
	// StringBuffer 的高併發替代品 StringBuilder(非同步) + ThreadLocal(多執行緒安全性)
	private static final ThreadLocal<StringBuilder> sbBuilderHolder = ThreadLocal.withInitial(() -> new StringBuilder());

	@Autowired
	public RefreshGTWService(AA1120Service aA1120Service, CApiKeyService capiKeyService,
			TsmpOrganizationDao tsmpOrganizationDao, TsmpApiDao tsmpApiDao, AA0317Service aA0317Service,
			DPB9921Service dpb9921Service, TsmpTokenHistoryDao tsmpTokenHistoryDao, TsmpClientDao tsmpClientDao) {
		super();
		this.aA1120Service = aA1120Service;
		this.capiKeyService = capiKeyService;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpApiDao = tsmpApiDao;
		this.aA0317Service = aA0317Service;
		this.dpb9921Service = dpb9921Service;
		this.tsmpTokenHistoryDao = tsmpTokenHistoryDao;
		this.tsmpClientDao = tsmpClientDao;
	}

	public RefreshGTWResp updateGTWInfo(RefreshGTWReq refreshGTWReq, HttpServletRequest request, HttpHeaders headers) {
		RefreshGTWResp resp = new RefreshGTWResp();
		String json = "";
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			json = objectMapper.writeValueAsString(refreshGTWReq);

			String cuuid = "";
			String capiKey = "";

			cuuid = headers.getFirst("cuuid");
			capiKey = headers.getFirst("capi-key");

			// 驗證 CApiKey
			boolean isValidate = CApiKeyUtils.verifyCKey(cuuid, capiKey);
			if (!isValidate) {
				StringBuffer sb = new StringBuffer();
				sb.append("\n");
				sb.append("requestURI: " + request.getRequestURI() + "\n");
				sb.append("RemoteAddr: " + request.getRemoteAddr() + "\n");
				sb.append("cuuid :" + cuuid + "\n");
				sb.append("cApikey :" + capiKey + "\n");
				sb.append("CApiKeyUtils.verifyCKey ==  false \n");
				sb.append("\n");
				TPILogger.tl.error(sb.toString());
				throw TsmpDpAaRtnCode._1522.throwing(); // 1522 - CApiKey驗證失敗
			}

			// 把 Landing 收到的 GTW 相關資料(CPU, Mem, API每秒轉發吞吐量...等),傳送到 Keeper Server
			addExternalDgrInfoToKeeper(refreshGTWReq, request);

			// 把 GTW(In-Memory) 端傳來的 token 使用量加到 Landing 端的 DB
			addTokenUsedData(refreshGTWReq);
			
			// 把 GTW(In-Memory) 端傳來的 API 使用量加到 Landing 端的 DB
			addApiUsedData(refreshGTWReq);

			// 匯出 匯出用戶端相關資料
			resp = exportClientData(refreshGTWReq, resp);

			// 匯出 API 資料
			resp = exportApiData(refreshGTWReq, resp);

			// 匯出 Setting 資料
			resp = exportSettingData(refreshGTWReq, resp);

			// 匯出 Token 資料
			resp = exportTokenData(refreshGTWReq, resp);
						
			if(refreshGTWReq.getLogMsg() != null && refreshGTWReq.getLogMsg().size() > 0) {
				StringBuilder sb = sbBuilderHolder.get();
				for(String msg : refreshGTWReq.getLogMsg()) {
					sb.append(msg);
				}
				String result = sb.toString();
				if(StringUtils.hasText(result)) {
					TPILogger.tl.info(result);
				}				
				sb.setLength(0); // 清空
			}			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(json);
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	public void unload() {
		sbBuilderHolder.remove();
	}

	/**
	 * 把 GTW(In-Memory) 端傳來的 token 使用量加到 Landing 端的 DB
	 */
	private void addTokenUsedData(RefreshGTWReq refreshGTWReq) {
		Map<String, Long> reqTokenUsedMap = refreshGTWReq.getTokenUsedMap();
		if(reqTokenUsedMap == null) {
			return;
		}
		List<TsmpTokenHistory> tsmpTokenHistorySaveList = new LinkedList<TsmpTokenHistory>();
		for (String jtiKey : reqTokenUsedMap.keySet()) {
			Long usedValue = reqTokenUsedMap.get(jtiKey);
			TsmpTokenHistory tsmpTokenHistory = getTsmpTokenHistoryDao().findFirstByTokenJti(jtiKey);
			if (tsmpTokenHistory != null) {
				Long dbTokenQuota = tsmpTokenHistory.getTokenQuota() == null ? 0 : tsmpTokenHistory.getTokenQuota();
				if (dbTokenQuota == null || dbTokenQuota == 0) {
					// 若 token_quota(可用量) 為 null 或 0, 則為不限次數, 不檢查
					// 且 token_used(使用量) 不加1
				} else {
					Long dbTokenUsed = tsmpTokenHistory.getTokenUsed() == null ? 0 : tsmpTokenHistory.getTokenUsed();
					dbTokenUsed = dbTokenUsed + usedValue;// 加上 GTW 端的使用量
					tsmpTokenHistory.setTokenUsed(dbTokenUsed);
					tsmpTokenHistorySaveList.add(tsmpTokenHistory);
				}
			}
		}

		// 若有要更新的資料,則儲存 & 更新 token 的最後更新時間
		if (!CollectionUtils.isEmpty(tsmpTokenHistorySaveList)) {
			getTsmpTokenHistoryDao().saveAllAndFlush(tsmpTokenHistorySaveList);
			TPILogger.updateTime4InMemory(DgrDataType.TOKEN.value());
		}
	}
	
	/**
	 * 把 GTW(In-Memory) 端傳來的 API 使用量加到 Landing 端的 DB
	 */
	private void addApiUsedData(RefreshGTWReq refreshGTWReq) {
		Map<String, Integer> reqApiUsedMap = refreshGTWReq.getApiUsedMap();
		if(reqApiUsedMap == null) {
			return;
		}
		List<TsmpClient> tsmpClientSaveList = new LinkedList<TsmpClient>();
		for (String clientId : reqApiUsedMap.keySet()) {
			Integer usedValue = reqApiUsedMap.get(clientId);
			TsmpClient tsmpClient = getTsmpClientDao().findFirstByClientId(clientId);
			if (tsmpClient != null) {
				Integer dbApiQuota = tsmpClient.getApiQuota() == null ? 0 : tsmpClient.getApiQuota();
				if (dbApiQuota == null || dbApiQuota == 0) {
					// 若 api_quota(可用量) 為 null 或 0, 則為不限次數, 不檢查
					// 且 api_quota(使用量) 不加1
				} else {
					Integer dbApiUsed = tsmpClient.getApiUsed() == null ? 0 : tsmpClient.getApiUsed();
					dbApiUsed = dbApiUsed + usedValue;// 加上 GTW 端的使用量
					tsmpClient.setApiUsed(dbApiUsed);
					tsmpClientSaveList.add(tsmpClient);
				}
			}
		}
		
		// 若有要更新的資料,則儲存 & 更新 token 的最後更新時間
		if (!CollectionUtils.isEmpty(tsmpClientSaveList)) {
			getTsmpClientDao().saveAllAndFlush(tsmpClientSaveList);
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());
		}
	}

	private RefreshGTWResp exportTokenData(RefreshGTWReq refreshGTWReq, RefreshGTWResp resp) {
		// 比對時間大小,若 GTW 的 Token 更新時間小於 Landing,則匯出相關資料
		Long lastUpdateTimeToken = refreshGTWReq.getGtwLastUpdateTimeToken();
		Long loggerLastUpdateTimeToken = TPILogger.lastUpdateTimeToken.get();
		if (lastUpdateTimeToken < loggerLastUpdateTimeToken) {
			// 查詢 TSMP_TOKEN_HISTORY 未過期的所有資料, 即 expired_at > 現在時間 或 reexpired_at > 現在時間
			Date now = getNow();
			List<TsmpTokenHistory> tsmpTokenHistoryList = getTsmpTokenHistoryDao()
					.findByReexpiredAtAfterOrExpiredAtAfter(now, now);
			resp.setTokenData(tsmpTokenHistoryList);
			resp.setLandLastUpdateTimeToken(loggerLastUpdateTimeToken);

		} else {
			resp.setLandLastUpdateTimeToken(-1L);// 表示 GTW 不用更新
		}

		return resp;
	}

	private RefreshGTWResp exportSettingData(RefreshGTWReq refreshGTWReq, RefreshGTWResp resp) {
		// 比對時間大小,若 GTW 的 Setting 更新時間小於 Landing,則匯出相關資料
		Long lastUpdateTimeSetting = refreshGTWReq.getGtwLastUpdateTimeSetting();
		Long loggerLastUpdateTimeSetting = TPILogger.lastUpdateTimeSetting.get();
		if (lastUpdateTimeSetting < loggerLastUpdateTimeSetting) {
			// DPB9921(匯出TSMP_SETTING)
			ByteArrayOutputStream byteArrOs = new ByteArrayOutputStream();
			getDPB9921Service().exportTsmpSetting(byteArrOs);
			byte[] dataByte = byteArrOs.toByteArray();// 得到 Excel 檔案
			String settingData = Base64Util.base64Encode(dataByte);// 將 Excel 檔案由 byte 轉成 String
			resp.setSettingData(settingData);
			resp.setLastUpdateTimeSetting(loggerLastUpdateTimeSetting);

		} else {
			resp.setLastUpdateTimeSetting(-1L);// 表示 GTW 不用更新
		}
		return resp;
	}

	private RefreshGTWResp exportApiData(RefreshGTWReq refreshGTWReq, RefreshGTWResp resp) {
		// 比對時間大小,若 GTW 的 API 更新時間小於 Landing,則匯出相關資料
		Long lastUpdateTimeAPI = refreshGTWReq.getGtwLastUpdateTimeAPI();
		Long loggerLastUpdateTimeAPI = TPILogger.lastUpdateTimeAPI.get();
		// 若 RefreshGTWReq.gtwLastUpdateTimeAPI < Landing 的 TPILogger.lastUpdateTimeAPI
		if (lastUpdateTimeAPI < loggerLastUpdateTimeAPI) {
			// 取得根組織的 org_id 查詢 TSMP_ORGANIZATION, parent_id = '' 或 parent_id = null
			TsmpOrganization org = getTsmpOrganizationDao().findFirstByParentIdIsNullOrParentIdIs("");
			String orgId = "";
			if (org != null)
				orgId = org.getOrgId();
			else {
				// 若查無資料,印出錯誤訊息,
				TPILogger.tl.error("In-Memory flow(Landing), root id not found");
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			// 查詢 TSMP_API 所有資料的 api_key 和 module_name 放到 List<AA0317ReqItem>
			List<TsmpApi> apis = getTsmpApiDao().findAll();
			if (CollectionUtils.isEmpty(apis)) {// 如果 Landing 端,查無資料
				// Landing 端,查無資料,印出訊息
				TPILogger.tl.error("In-Memory flow(Landing), Table [TSMP_API] can't find data");
				resp.setOrgId(orgId);
				resp.setLastUpdateTimeAPI(loggerLastUpdateTimeAPI);
				return resp;
			}

			List<AA0317ReqItem> AA0317ReqItemList = new ArrayList<>();
			apis.forEach(a -> {
				AA0317ReqItem item = new AA0317ReqItem();
				item.setApiKey(a.getApiKey());
				item.setModuleName(a.getModuleName());
				AA0317ReqItemList.add(item);
			});

			// AA0317(匯出註冊/組合API)
			AA0317Req aa0317Req = new AA0317Req();
			aa0317Req.setApiList(AA0317ReqItemList);
			AA0317Resp aa0317Resp = getAA0317Service().exportRegCompAPIs(USERNAME, orgId, aa0317Req);
			AA0317Data aa0317Data = aa0317Resp.getData();
			resp.setOrgId(orgId);
			resp.setAa0317ReqItemList(AA0317ReqItemList);
			resp.setApiData(aa0317Data);
			resp.setLastUpdateTimeAPI(loggerLastUpdateTimeAPI);

		} else {
			resp.setLastUpdateTimeAPI(-1L);// 表示 GTW 不用更新
		}
		return resp;
	}

	private RefreshGTWResp exportClientData(RefreshGTWReq refreshGTWReq, RefreshGTWResp resp) {
		// 比對時間大小,若 GTW 的 Client 更新時間小於 Landing,則匯出相關資料
		Long lastUpdateTimeClient = refreshGTWReq.getGtwLastUpdateTimeClient();
		Long loggerLastUpdateTimeClient = TPILogger.lastUpdateTimeClient.get();
		// 若 RefreshGTWReq.gtwLastUpdateTimeClient < Landing 的
		// TPILogger.lastUpdateTimeClient
		if (lastUpdateTimeClient < loggerLastUpdateTimeClient) {
			// AA1120(匯出用戶端相關資料)
			AA1120Resp aa1120Resp = getAA1120Service().exportClientRelated(null, null);
			AA1120ExportData aa1120ExportData = aa1120Resp.getData();
			resp.setClientData(aa1120ExportData);
			// Landing 的 lastUpdateTimeClient
			resp.setLastUpdateTimeClient(loggerLastUpdateTimeClient);

		} else {// 表示 GTW 不用更新
			resp.setLastUpdateTimeClient(-1L);
		}
		return resp;

	}

	/**
	 * 將外部 dgr 資訊添加到 Keeper 中。
	 * 
	 * @param refreshGTWReq 請求物件，包含 dgr 的節點資訊和識別資料。
	 * @param request       HTTP 請求物件，用於獲取請求來源的 IP 和端口。
	 */
	private void addExternalDgrInfoToKeeper(RefreshGTWReq refreshGTWReq, HttpServletRequest request) {
		// 從請求物件中獲取節點資訊包
		NodeInfoPacket nodeInfoPacket = refreshGTWReq.getNodeInfoPacket();
		UndertowMetricsPacket undertowMetricsPacket = refreshGTWReq.getUndertowMetricsPacket();
		UrlStatusPacket urlStatusPacket = refreshGTWReq.getUrlStatusPacket();
		RealtimeDashboardPacket realtimeDashboardPacket = refreshGTWReq.getRealtimeDashboardPacket();
		// 從請求物件中獲取 dgr 的 ID
		String gtwID = refreshGTWReq.getGtwID();
		// 從請求物件中獲取 dgr 的名稱，名稱格式為 "gateway-xxxx"
		String gtwName = refreshGTWReq.getGtwName(); // GTW 名稱，"gateway-" 開頭，例如：gateway-zE8I

		// 從請求物件中獲取 Keeper API 的 URL
		String keeperApi = refreshGTWReq.getKeeperApi();

		// 透過 HTTP 請求物件獲取請求來源的 IP 地址
		String gtwIp = request.getRemoteAddr();
		// 透過 HTTP 請求物件獲取請求來源的端口號
		int gtwPort = request.getRemotePort();

		// 將 dgr 名稱和 ID 組合成新的名稱格式
		gtwName = gtwName + " (" + gtwID + ")";

		// 使用獲取的資訊創建一個 ClientKeeper 物件
		ClientKeeper clientKeeper = createClientKeeper(gtwName, gtwIp, gtwPort, keeperApi, nodeInfoPacket);

		// 如果日誌記錄器的 lc 屬性非空，則發送一個包含 ClientKeeper 資訊的 ExternalDgrInfoPacket
		if (TPILogger.lc != null) {
			TPILogger.lc.send(new ExternalDgrInfoPacket(clientKeeper));
			TPILogger.lc.send(new ExternalUndertowMetricsInfosPacket(undertowMetricsPacket));
			TPILogger.lc.send(new ExternalUrlStatusPacket(urlStatusPacket));
			TPILogger.lc.send(new ExternalRealtimeDashboardPacket(realtimeDashboardPacket));
		} else {
			// 若日誌記錄器的 lc 屬性為空，則記錄錯誤信息，表示無法將外部 dgr 資料發送到 Keeper 伺服器
			TPILogger.tl.warn("The program lc is null and cannot send external dgr data to the Keeper server\n");
		}
	}

	/**
	 * 解析 URL 並提取協議、主機地址和端口號。
	 * 
	 * @param keeperApi 需要解析的 URL 字串。
	 * @return 字串陣列，包含協議加主機地址和端口號，如果無法解析或端口號不存在，端口號位置將為空字串。
	 */
	private String[] parseURL(String keeperApi) {
		// 使用預定義的 URL_PATTERN 來匹配輸入的 URL 字串
		Matcher matcher = urlPattern.matcher(keeperApi);
		// 初始化協議加主機地址和端口號為空字串
		String protocolAndHost = "";
		String port = "";
		if (matcher.find()) {
			// 如果找到匹配項，則從匹配結果中提取協議加主機地址和端口號
			protocolAndHost = matcher.group(1) + "://" + matcher.group(2);
			port = matcher.group(3); // 端口號可能不存在，此時為空字串
		} else {
			// 如果 URL 格式不符合預期，記錄錯誤日誌
			TPILogger.tl.error("Invalid URL format: " + keeperApi);
		}

		// 返回協議加主機地址和端口號的字串陣列
		return new String[] { protocolAndHost, port };
	}

	/**
	 * 根據傳入的 dgr 名稱、ID、端口和節點資訊包，創建並配置一個 ClientKeeper 物件。
	 * 
	 * @param gtwName        dgr 名稱
	 * @param gtwIp          dgr IP 地址
	 * @param gtwPort        dgr 端口號
	 * @param keeperApi      Keeper API 的 URL
	 * @param nodeInfoPacket 節點資訊包
	 * @return 配置好的 ClientKeeper 物件
	 */
	private ClientKeeper createClientKeeper(String gtwName, String gtwIp, int gtwPort, String keeperApi,
			NodeInfoPacket nodeInfoPacket) {

		// 創建 ClientKeeper 物件的實例
		ClientKeeper clientKeeper = new ClientKeeper();

		// 解析 URL 獲取 Keeper 服務的 IP 地址和端口號
		String[] urlString = parseURL(keeperApi);

		clientKeeper.setUsername(gtwName);
		clientKeeper.setIp(gtwIp);
		clientKeeper.setPort(gtwPort);

		clientKeeper.setMain(nodeInfoPacket.main);
		clientKeeper.setDeferrable(nodeInfoPacket.deferrable);
		clientKeeper.setRefresh(nodeInfoPacket.refresh);
		clientKeeper.setVersion(nodeInfoPacket.version);
		clientKeeper.setUpdateTime(nodeInfoPacket.updateTime);

		clientKeeper.setCpu(nodeInfoPacket.cpu);
		clientKeeper.setMem(nodeInfoPacket.mem);
		clientKeeper.setH_used(nodeInfoPacket.h_used);
		clientKeeper.setH_free(nodeInfoPacket.h_free);
		clientKeeper.setH_total(nodeInfoPacket.h_total);

		clientKeeper.setApi_ReqThroughputSize(nodeInfoPacket.api_ReqThroughputSize);
		clientKeeper.setApi_RespThroughputSize(nodeInfoPacket.api_RespThroughputSize);

		clientKeeper.setUpTime(nodeInfoPacket.upTime);
		clientKeeper.setStartTime(nodeInfoPacket.startTime);
		clientKeeper.setServerPort(nodeInfoPacket.serverPort);
		clientKeeper.setServerSslEnalbed(nodeInfoPacket.serverSslEnalbed);
		clientKeeper.setSpringProfilesActive(nodeInfoPacket.springProfilesActive);
		clientKeeper.setServerServletContextPath(nodeInfoPacket.serverServletContextPath);
		clientKeeper.setKeeperServerIp(urlString[0]);
		clientKeeper.setKeeperServerPort(urlString[1]);
		clientKeeper.setRcdCacheSize(nodeInfoPacket.rcdCacheSize);
		clientKeeper.setDaoCacheSize(nodeInfoPacket.daoCacheSize);
		clientKeeper.setFixedCacheSize(nodeInfoPacket.fixedCacheSize);
		clientKeeper.setWebLocalIP(nodeInfoPacket.webLocalIP);
		clientKeeper.setFqdn(nodeInfoPacket.fqdn);
		clientKeeper.setEsQueue(nodeInfoPacket.ES_Queue);
		clientKeeper.setRdbQueue(nodeInfoPacket.RDB_Queue);

		clientKeeper.setLastUpdateTimeAPI(nodeInfoPacket.lastUpdateTimeAPI);
		clientKeeper.setLastUpdateTimeClient(nodeInfoPacket.lastUpdateTimeClient);
		clientKeeper.setLastUpdateTimeSetting(nodeInfoPacket.lastUpdateTimeSetting);
		clientKeeper.setLastUpdateTimeToken(nodeInfoPacket.lastUpdateTimeToken);

		return clientKeeper;
	}

	protected Date getNow() {
		return DateTimeUtil.now();
	}

	protected TsmpTokenHistoryDao getTsmpTokenHistoryDao() {
		return tsmpTokenHistoryDao;
	}

	protected DPB9921Service getDPB9921Service() {
		return dpb9921Service;
	}

	protected AA0317Service getAA0317Service() {
		return aA0317Service;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return tsmpApiDao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return tsmpOrganizationDao;
	}

	protected AA1120Service getAA1120Service() {
		return aA1120Service;
	}

	protected CApiKeyService getCapiKeyService() {
		return capiKeyService;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}
}