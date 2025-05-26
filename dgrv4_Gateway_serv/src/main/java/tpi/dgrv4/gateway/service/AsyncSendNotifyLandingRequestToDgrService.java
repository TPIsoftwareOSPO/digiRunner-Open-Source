package tpi.dgrv4.gateway.service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.codec.utils.CApiKeyUtils;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.DpaaHttpUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.OAuthTokenService.OAuthTokenData;
import tpi.dgrv4.gateway.util.DigiRunnerGtwDeployProperties;
import tpi.dgrv4.gateway.vo.NotifyLandingReq;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Service
public class AsyncSendNotifyLandingRequestToDgrService {

	private DigiRunnerGtwDeployProperties digiRunnerGtwDeployProperties;

	private List<String> urls;

	// 為了單元測試而加入的暫時儲存變數
	private NotifyLandingReq reqForTest;
	private Map<String, String> reqHeaderForTest;

	@Autowired
	public AsyncSendNotifyLandingRequestToDgrService(DigiRunnerGtwDeployProperties digiRunnerGtwDeployProperties) {
		super();
		this.digiRunnerGtwDeployProperties = digiRunnerGtwDeployProperties;
	}

	/**
	 * 建立一個 Runnable 任務，用於發送登陸通知請求。
	 * 
	 * @param oauthTokenData     OAuth 令牌資料
	 * @param isHasRefreshToken  指示是否有刷新令牌
	 * @param userName           使用者名稱
	 * @param clientId           客戶端 ID
	 * @param accessTokenJti     存取令牌 JTI
	 * @param refreshTokenJti    刷新令牌 JTI
	 * @param scopeStr           範圍字串
	 * @param stime              開始時間
	 * @param accessTokenExp     存取令牌過期時間
	 * @param refreshTokenExp    刷新令牌過期時間
	 * @param grantType          授權類型
	 * @param oldAccessTokenJti  舊的存取令牌 JTI
	 * @param idPType            身份提供者類型
	 * @param idTokenJwtstr      ID 令牌 JWT 字串
	 * @param refreshTokenJwtstr 刷新令牌 JWT 字串
	 * @return 返回一個 Runnable 任務
	 */
	public Runnable getTask(OAuthTokenData oauthTokenData, boolean isHasRefreshToken, String userName, String clientId,
			String accessTokenJti, String refreshTokenJti, String scopeStr, Long stime, Long accessTokenExp,
			Long refreshTokenExp, String grantType, String oldAccessTokenJti, String idPType, String idTokenJwtstr,
			String refreshTokenJwtstr) {
		// 創建並返回一個 Runnable 任務，該任務用於向 DGR 發送通知登陸請求
		return () -> {
			sendNotifyLandingRequestToDgr(oauthTokenData, isHasRefreshToken, userName, clientId, accessTokenJti,
					refreshTokenJti, scopeStr, stime, accessTokenExp, refreshTokenExp, grantType, oldAccessTokenJti,
					oauthTokenData.idPType, idTokenJwtstr, refreshTokenJwtstr);
		};
	}

	/**
	 * 向 DGR 發送登陸通知請求。
	 * 
	 * @param oauthTokenData     OAuth 令牌資料
	 * @param isHasRefreshToken  是否擁有刷新令牌
	 * @param userName           使用者名稱
	 * @param clientId           客戶端 ID
	 * @param accessTokenJti     存取令牌 JTI
	 * @param refreshTokenJti    刷新令牌 JTI
	 * @param scopeStr           授權範圍
	 * @param stime              開始時間
	 * @param accessTokenExp     存取令牌到期時間
	 * @param refreshTokenExp    刷新令牌到期時間
	 * @param grantType          授權類型
	 * @param oldAccessTokenJti  舊的存取令牌 JTI
	 * @param idPType            身份提供者類型
	 * @param idTokenJwtstr      ID 令牌 JWT 字串
	 * @param refreshTokenJwtstr 刷新令牌 JWT 字串
	 */
	private void sendNotifyLandingRequestToDgr(OAuthTokenData oauthTokenData, boolean isHasRefreshToken,
			String userName, String clientId, String accessTokenJti, String refreshTokenJti, String scopeStr,
			Long stime, Long accessTokenExp, Long refreshTokenExp, String grantType, String oldAccessTokenJti,
			String idPType, String idTokenJwtstr, String refreshTokenJwtstr) {

		// 獲取 URL 清單
		List<String> urls = getUrls();
		// 建立通知登陸請求的頭部資訊
		Map<String, String> header = makeNotifyLandingHeader();
		// 建立通知登陸請求的主體內容
		String reqJson = makeNotifyLandingBody(oauthTokenData, isHasRefreshToken, userName, clientId, accessTokenJti,
				refreshTokenJti, scopeStr, stime, accessTokenExp, refreshTokenExp, grantType, oldAccessTokenJti,
				oauthTokenData.idPType, idTokenJwtstr, refreshTokenJwtstr);

		String reqUrl = null;
		try {
			int dataSize = urls.size();
			
			// 遍歷 URL 清單，發送 HTTP 請求
			// 若打第一個 Landing API URL 失敗,就打下一個,直到成功則跳出
			int dataIndex = 1;// 為第 N 個 Landing URL
			for (String url : urls) {
				reqUrl = url;
				// 發送 HTTP POST 請求
				HttpRespData resp = HttpUtil.httpReqByRawData(url, "POST", reqJson, header, false);
				// 如果狀態碼為 200，則中斷循環
				if (resp.statusCode == 200) {
					break;
				} else {
					// 記錄錯誤日誌
					if (dataIndex == dataSize) {// 當調用最後一個 Landing API 錯誤,才印出錯誤訊息
						TPILogger.tl.error(resp.getLogStr());
					}
				}
				
				dataIndex++;
			}

		} catch (IOException e) {
			// 記錄異常日誌
			TPILogger.tl.error("Error sending notify landing request: ");
			TPILogger.tl.error("reqUrl = " + reqUrl);
			TPILogger.tl.error("reqJson = " + reqJson);
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
		}
	}

	/**
	 * 建立通知登陸請求的 JSON 主體。
	 * 
	 * @param oauthTokenData     OAuth 令牌資料
	 * @param isHasRefreshToken  是否擁有刷新令牌
	 * @param userName           使用者名稱
	 * @param clientId           客戶端 ID
	 * @param accessTokenJti     存取令牌 JTI
	 * @param refreshTokenJti    刷新令牌 JTI
	 * @param scopeStr           授權範圍字串
	 * @param stime              開始時間
	 * @param accessTokenExp     存取令牌過期時間
	 * @param refreshTokenExp    刷新令牌過期時間
	 * @param grantType          授權類型
	 * @param oldAccessTokenJti  舊的存取令牌 JTI
	 * @param idPType            身份提供者類型
	 * @param idTokenJwtstr      ID 令牌的 JWT 字串
	 * @param refreshTokenJwtstr 刷新令牌的 JWT 字串
	 * @return 返回通知登陸請求的 JSON 字串
	 */
	private String makeNotifyLandingBody(OAuthTokenData oauthTokenData, boolean isHasRefreshToken, String userName,
			String clientId, String accessTokenJti, String refreshTokenJti, String scopeStr, Long stime,
			Long accessTokenExp, Long refreshTokenExp, String grantType, String oldAccessTokenJti, String idPType,
			String idTokenJwtstr, String refreshTokenJwtstr) {

		// 創建一個通知登陸請求物件
		NotifyLandingReq req = new NotifyLandingReq();

		// 設置 OAuth 令牌資料
		req.setOauthTokenData(oauthTokenData);
		// 設置是否擁有刷新令牌
		req.setHasRefreshToken(isHasRefreshToken);
		// 設置使用者名稱
		req.setUserName(userName);
		// 設置客戶端 ID
		req.setClientId(clientId);
		// 設置存取令牌 JTI
		req.setAccessTokenJti(accessTokenJti);
		// 設置刷新令牌過期時間
		req.setRefreshTokenJti(refreshTokenJti);
		// 設置授權範圍字串
		req.setScopeStr(scopeStr);
		// 設置開始時間
		req.setStime(stime);
		// 設置存取令牌過期時間
		req.setAccessTokenExp(accessTokenExp);
		// 再次設置刷新令牌過期時間
		req.setRefreshTokenExp(refreshTokenExp);
		// 設置授權類型
		req.setGrantType(grantType);
		// 設置舊的存取令牌 JTI
		req.setOldAccessTokenJti(oldAccessTokenJti);
		// 設置身份提供者類型
		req.setIdPType(idPType);
		// 設置 ID 令牌的 JWT 字串
		req.setIdTokenJwt(idTokenJwtstr);
		// 設置刷新令牌的 JWT 字串
		req.setRefreshTokenJwtstr(refreshTokenJwtstr);

		this.reqForTest = req;

		// 將請求物件轉換為 JSON 字串並返回
		return DpaaHttpUtil.toReqPayloadJson(req, "inMemory");// 因為不會驗証,所以 cid 不是重點
	}

	/**
	 * 建立通知登陸請求的頭部資訊。
	 * 
	 * @return 返回包含請求頭部資訊的 Map 物件。
	 */
	private Map<String, String> makeNotifyLandingHeader() {
		// 生成一個隨機 UUID 作為 CAPI 的 key
		String uuidForCapiKey = UUID.randomUUID().toString();
		// 將 UUID 轉換成大寫
		String cuuid = uuidForCapiKey.toUpperCase();
		// 使用 CAPIKeyUtils 的 signCKey 方法對 cuuid 進行簽名
		String capiKey = CApiKeyUtils.signCKey(cuuid);
		// 創建一個 HashMap 來存放頭部資訊
		Map<String, String> header = new HashMap<>();
		// 設置接受的內容類型為 JSON
		header.put("Accept", "application/json");
		// 設置請求內容類型為 JSON
		header.put("Content-Type", "application/json");
		// 添加 cuuid 到頭部資訊
		header.put("cuuid", cuuid);
		// 添加簽名後的 capi-key 到頭部資訊
		header.put("capi-key", capiKey);

		reqHeaderForTest = header;

		// 返回包含頭部資訊的 Map 物件
		return header;
	}

	/**
	 * 獲取 URL 清單。
	 * 
	 * @return 返回 URL 字串的清單。
	 */
	private List<String> getUrls() {
		// 如果 urls 尚未初始化
		if (urls == null) {
			// 從配置中獲取登陸 IP 和端口資訊
			String landingIpPort = getDigiRunnerGtwDeployProperties().getDeployLandingIpPort();
			// 將獲取的 IP 和端口資訊分割成陣列
			String[] landingIpPorts = splitString(landingIpPort);
			// 根據分割後的 IP 和端口生成 URL 清單
			urls = generateUrls(landingIpPorts);
		}
		// 返回 URL 清單
		return urls;
	}

	/**
	 * 根據輸入的 IP 和端口資訊生成 URL 清單。
	 * 
	 * @param input 包含 IP 和端口資訊的字串陣列
	 * @return 返回生成的 URL 清單
	 */
	private List<String> generateUrls(String[] input) {
		// 檢查輸入陣列是否為空或無元素，若是則返回一個空的清單
		if (input == null || input.length == 0) {
			return Collections.emptyList();
		}

		List<String> urlList = new ArrayList<>();
		String schema  = getDigiRunnerGtwDeployProperties().getDeployLandingScheme();

		// 遍歷輸入的 IP 和端口資訊
		for (String item : input) {
			// 建立 URI，使用 HTTPS 協議並附加特定路徑
			URI uri = URI.create(schema + "://" + item + "/dgrv4/ImGTW/notifyLanding");
			// 將 URI 轉換為字串並添加到 URL 清單中
			urlList.add(uri.toString());
		}

		// 返回生成的 URL 清單
		return urlList;
	}

	/**
	 * 根據逗號分割字串。
	 * 
	 * @param input 待分割的字串
	 * @return 分割後的字串陣列，如果輸入為 null 或空白字串，則返回一個空陣列。
	 */
	private String[] splitString(String input) {
		// 檢查輸入字串是否為 null 或只包含空白字符
		if (input == null || input.trim().isEmpty()) {
			// 如果是，返回一個空的字串陣列
			return new String[0];
		}
		// 使用逗號作為分隔符號，將輸入字串分割成多個部分，並返回結果
		return input.split(",");
	}
	
	public Map<String, String> getReqHeaderForTest() {
		return reqHeaderForTest;
	}

	public NotifyLandingReq getReqForTest() {
		return reqForTest;
	}

	protected DigiRunnerGtwDeployProperties getDigiRunnerGtwDeployProperties() {
		return digiRunnerGtwDeployProperties;
	}
}
