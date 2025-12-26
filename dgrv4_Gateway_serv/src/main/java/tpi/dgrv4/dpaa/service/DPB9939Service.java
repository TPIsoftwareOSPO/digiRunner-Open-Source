package tpi.dgrv4.dpaa.service;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.Base64Util;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9939Req;
import tpi.dgrv4.dpaa.vo.DPB9939Resp;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.IKibanaService2;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.httpu.utils.HttpUtil;

@Service
public class DPB9939Service {
	private TsmpSettingService tsmpSettingService;
	private IKibanaService2 kibanaService2;

	private static final String BASIC = "basic";
	private static final String SESSION = "session";
	private static final String NO_ENTERPRISE_SERVICE = "...No Enterprise Service...";

	@Autowired
	public DPB9939Service(TsmpSettingService tsmpSettingService, @Nullable IKibanaService2 kibanaService2) {
		super();
		this.tsmpSettingService = tsmpSettingService;
		this.kibanaService2 = kibanaService2;
	}

	public DPB9939Resp testKibanaConnection(DPB9939Req req) {
		DPB9939Resp resp = new DPB9939Resp();
		try {
			String protocol = checkSetting(TsmpSettingDao.Key.KIBANA_TRANSFER_PROTOCOL);
			String host = checkSetting(TsmpSettingDao.Key.KIBANA_HOST);
			String port = getTsmpSettingService().getVal_KIBANA_PORT();
			String user = checkSetting(TsmpSettingDao.Key.KIBANA_USER);
			String mima = getTsmpSettingService().getVal_KIBANA_PWD();
			String auth = checkSetting(TsmpSettingDao.Key.KIBANA_AUTH).trim().toLowerCase();
			String statusApi = checkSetting(TsmpSettingDao.Key.KIBANA_STATUS_URL);
			// 檢查 mima
			if (!StringUtils.hasLength(mima))
				throw TsmpDpAaRtnCode._1474.throwing(TsmpSettingDao.Key.KIBANA_PWD);
			// 檢查port
			if (!StringUtils.hasLength(port)) {
				if ("https".equals(protocol))
					port = "443";
				else
					port = "80";
			}
			// base url
			String baseUrl = String.format("%s://%s:%s", protocol, host, port);
			boolean isConnection = false;
			String url = baseUrl + statusApi;
			HttpUtil.HttpRespData respData = checkConnection(url, auth, user, mima);

			if (respData.statusCode > 0 && respData.statusCode < 400) {
				isConnection = true;
			} else {
				TPILogger.tl.error(respData.getLogStr());
			}
			resp.setAuth(auth);
			resp.setStatusApiUrl(url);
			resp.setConnection(isConnection);
			resp.setResp(respData.respStr);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			// 執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	// 通一處理取得設定值和拋錯
	private String checkSetting(String id) {
		var val = getTsmpSettingService().getStringVal(id);
		if (!StringUtils.hasLength(val))
			throw TsmpDpAaRtnCode._1474.throwing(id);
		return val;
	}

	protected HttpUtil.HttpRespData checkConnection(String url, String auth, String user, String mima)
			throws Exception {
		HttpUtil.HttpRespData respData = new HttpUtil.HttpRespData();
		// 檢查連線
		if (BASIC.equals(auth)) {
			respData = callApi(url, getHeader(user, mima), false);
		} else if (SESSION.equals(auth)) {
			// 取得Kibana登入授權資料
			HttpResponse<byte[]> loginResp = null;

			if (getKibanaService2() != null) {
				loginResp = getKibanaService2().login_withUrl(user, mima);
			} else {
	            TPILogger.tl.info(NO_ENTERPRISE_SERVICE);
	            respData.statusCode = 404;
	            respData.respStr = NO_ENTERPRISE_SERVICE;
	            return respData;
			}

			if (loginResp.statusCode() < 200 || loginResp.statusCode() >= 400) {
				respData.statusCode = loginResp.statusCode();
				respData.respStr = new String(loginResp.body());
			} else {
				Map<String, List<String>> header = new HashMap<>();
                List<String> cookies = new ArrayList<>();
				// 特別處理Cookie
                loginResp.headers().map().forEach((key, valList) -> {
                    if (key != null) {
                        if (key.equalsIgnoreCase(HttpHeaders.SET_COOKIE)) {
                            valList.forEach(setCookieValue -> {
                                String cookieValue = setCookieValue.split(";")[0].trim();
                                cookies.add(cookieValue);
                            });
                        } else if (!key.startsWith(":")) {
                            header.put(key, new ArrayList<>(valList));
                        }
                    }
                });

                if (!cookies.isEmpty()) {
                    header.put(HttpHeaders.COOKIE, Arrays.asList(String.join("; ", cookies)));

                }
                header.put("Connection", Arrays.asList("close"));
                header.put("Cache-Control", Arrays.asList("no-cache, no-store, must-revalidate"));
                header.put("Pragma", Arrays.asList("no-cache"));
                header.put("User-Agent", Arrays.asList("DGRV4-StatusCheck/" + System.currentTimeMillis()));

                respData = callApi(url, header, true);
                getKibanaService2().clearCacheMap();
			}
		} else {
			throw TsmpDpAaRtnCode._1559.throwing("Error auth type. : " + auth);
		}

		return respData;
	}

	protected HttpUtil.HttpRespData callApi(String url, Map<String, List<String>> header, boolean isRedirect)
			throws IOException {
        HttpUtil.HttpRespData resp = HttpUtil.httpReqByGetList(url, header, false, isRedirect);
        TPILogger.tl.debug(resp.getLogStr());
		return resp;
	}

	public Map<String, List<String>> getHeader(String user, String mima) {
		String idMima = user + ":" + mima;
		String basicAuth = Base64Util.base64Encode(idMima.getBytes());
		Map<String, List<String>> header = new HashMap<>();
		header.put("Accept", Arrays.asList("application/json"));
		header.put("Content-Type", Arrays.asList("application/json"));
		header.put("Authorization", Arrays.asList("Basic " + basicAuth));
        header.put("Cache-Control", Arrays.asList("no-cache, no-store, must-revalidate"));
        header.put("Pragma", Arrays.asList("no-cache"));
		return header;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected IKibanaService2 getKibanaService2() {
		return kibanaService2;
	}
}
