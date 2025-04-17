package tpi.dgrv4.dpaa.service.composer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import tpi.dgrv4.codec.utils.CApiKeyUtils;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Service
public class ComposerServiceImpl implements ComposerService {

	private TPILogger logger = TPILogger.tl;

	private ComposerURLService composerURLService;
	private ObjectMapper objectMapper;

	@Autowired
	public ComposerServiceImpl(ComposerURLService composerURLService, ObjectMapper objectMapper) {
		super();
		this.composerURLService = composerURLService;
		this.objectMapper = objectMapper;
	}

	@Override
	public void confirmAllNodes(String apiUUID) {
		List<String> restartPaths = getComposerURLService().getRestartPaths(apiUUID);
		if (CollectionUtils.isEmpty(restartPaths)) {
			this.logger.debug("Confirm to all nodes error! Missing restart paths.");
			return;
		}
		
		logger.debug("1.原始的 restartPaths:\n" + restartPaths.toString().replaceAll(",", ",\n"));
		restartPaths = removeLocalhostPath(restartPaths);
		logger.debug("2.移除後的 restartPaths:\n" + restartPaths.toString().replaceAll(",", ",\n"));
		
		for (String restartPath : restartPaths) {
			try {

				notifyNodeV4(restartPath,"PATCH", null);
			} catch (Exception e) {
				this.logger.debug("Confirm to node error: " + restartPath + "\n" + StackTraceUtil.logStackTrace(e));
			}
		}
	}
	
	/**
	 * 廣播時,判斷若URL為本機,則略過不執行,以避免同一台server重複執行2次 
	 */
	protected static List<String> removeLocalhostPath(List<String> restartPaths) {
		List<String> newRestartPaths = new ArrayList<String>();
		
		for (int i = 0; i < restartPaths.size(); i++) {
			String restartPath = restartPaths.get(i);
			if(!restartPath.toLowerCase().contains("127.0.0.1") 
					&& !restartPath.toLowerCase().contains("localhost")) {
				newRestartPaths.add(restartPath);
			}
		}
		return newRestartPaths;
	}

	@Override
	public void deleteAndStopAllNodes(String apiUUID) {
		List<String> deleteAndStopPaths = getComposerURLService().getDeleteAndStopPaths(apiUUID);
		
		if (CollectionUtils.isEmpty(deleteAndStopPaths)) {
			this.logger.debug("Delete/Stop to all nodes error! Missing restart paths.");
		}
		
		String path = null;
		for (int i = 0; i < deleteAndStopPaths.size(); i++) {
			path = deleteAndStopPaths.get(i);
			try {
				if (i == 0) {

					String response = notifyNodeV4(path,"DELETE", null);
					this.logger.info("Delete API: host=" + path);
					this.logger.debug(response);
				} else {

					notifyNodeV4(path,"PATCH", null);

					this.logger.info("Stop API: host=" + path);
				}

			} catch (Exception e) {
				this.logger.debug("Delete / Stop to node error: " + path + "\n" + StackTraceUtil.logStackTrace(e));
			}
		}
	}

	@Override
	public List<String> getComposerIPs() {
		return getComposerURLService().getComposerIPs();
	}
	
	private String notifyNodeV4(String url ,String method, String body) {
		String responseBody = null;

		this.logger.debug("Composer Request URL: " + url);
		
		HttpRespData response = null;
		try {
			String uuidForCapiKey = UUID.randomUUID().toString();
			String cuuid = uuidForCapiKey.toUpperCase();
			String capikey = CApiKeyUtils.signCKey(cuuid);
						
			response = notifyNodeV4(url, method, body, null, cuuid, capikey);

			if (response != null) {
				int code = response.statusCode;
				
				
				// 未授權401
				if (HttpStatus.UNAUTHORIZED.value() == code) {
					throw TsmpDpAaRtnCode._1522.throwing();
				}
				if (HttpStatus.FORBIDDEN.value() == code) {
					throw TsmpDpAaRtnCode._1522.throwing();
				}
				
				if(-1 == code) {
					throw TsmpDpAaRtnCode._1498.throwing("connection to composer server failed");	
				}
			}
			
			responseBody = response.respStr;
			
		} catch (TsmpDpAaException e) {
			this.logger.debug("Error handling http response: " + StackTraceUtil.logStackTrace(e));
			throw e;
		} catch (Exception e) {
			this.logger.debug("Error handling http response: " + StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		} 
//		catch (IOException e) {
//			this.logger.debug("Error handling http response: " + StackTraceUtil.logStackTrace(e));
//			throw TsmpDpAaRtnCode._1297.throwing();
//		} 
//		finally {
//			try {
//				if (req != null) {
//					req.releaseConnection();
//				} 
//			} catch (Exception e) {
//				this.logger.debug("Error closing http connection: " + StackTraceUtil.logStackTrace(e));
//			}
//		}
		
		return responseBody;
	}
	
	@Override
	public String saveToCollectionsV4(List<List<String>> request) {
		List<String> saveCollectionsPaths = getComposerURLService().getSaveCollectionsPathsV4();
		if (CollectionUtils.isEmpty(saveCollectionsPaths)) {
			this.logger.debug("Missing saveToCollections paths.");
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		String body = null;
		String resp = null;
		for (String saveToCollectionsPath : saveCollectionsPaths) {
			
			logger.debug("saveToCollectionsPath=");
			logger.debug(saveToCollectionsPath);
			try {
			body = getObjectMapper().writeValueAsString(request);
			} catch (Exception e) {
				this.logger.debug("saveCollectionsV4 error: " + saveToCollectionsPath + "\n" + StackTraceUtil.logStackTrace(e));
				throw TsmpDpAaRtnCode._1297.throwing();
			}
			this.logger.debug("========== [HTTP Body] ==========");
			this.logger.debug(body);
			body = notifyNodeV4(saveToCollectionsPath,"POST",body);
			if (StringUtils.hasText(body)) {					
				resp = body;
				if (resp != null) {
					break;
				}
			}
			// 只要沒有出錯就結束
			if(true) {
				break;
			}
			
			
		}
		return resp;
	}

	private HttpRespData notifyNode(String reqUrl, String method, String rawData, Map<String, String> httpHeader,
			String authorization) //
			throws  IOException {
		if(httpHeader == null) {
			httpHeader = new HashMap<String, String>();
		}
		httpHeader.put("charset", StandardCharsets.UTF_8.name());
		httpHeader.put("Accept", MediaType.APPLICATION_JSON_VALUE);
		httpHeader.put("Content-type", MediaType.APPLICATION_JSON_VALUE);
		httpHeader.put("Authorization", authorization);
		
		HttpRespData response = HttpUtil.httpReqByRawData(reqUrl, method, rawData, httpHeader, false);
		this.logger.debug("Response Str : " + response.respStr);
		return response;
	}
	
	private HttpRespData notifyNodeV4(String reqUrl, String method, String rawData, Map<String, String> httpHeader,
			String cuuid, String capikey) //
			throws IOException {
		if(httpHeader == null) {
			httpHeader = new HashMap<String, String>();
		}
		httpHeader.put("charset", StandardCharsets.UTF_8.name());
		httpHeader.put("Accept", MediaType.APPLICATION_JSON_VALUE);
		httpHeader.put("Content-type", MediaType.APPLICATION_JSON_VALUE);
		httpHeader.put("cuuid", cuuid);
		httpHeader.put("capi-key", capikey);
		
		HttpRespData response = HttpUtil.httpReqByRawData(reqUrl, method, rawData, httpHeader, false);
		this.logger.debug("Response Str : " + response.respStr);
		return response;
	}
	
	@Override
	public String findAllNodesByModuleNameAndApiName(List<List<String>> request) {
		List<String> findNodesPaths = getComposerURLService().getFindAllNodesPaths();
		if (CollectionUtils.isEmpty(findNodesPaths)) {
			this.logger.debug("Missing findApplicationsByModuleNameAndApiName paths.");
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		String resp = null;
		
		String body = null;
		for (String findNodesPath : findNodesPaths) {
			
			try {	
				body = getObjectMapper().writeValueAsString(request);
				
			} catch (Exception e) {
				this.logger.debug("Json error: " + request + "\n" + StackTraceUtil.logStackTrace(e));
				throw TsmpDpAaRtnCode._1297.throwing();
			}	
			body = notifyNodeV4(findNodesPath,"POST", body);
			if (StringUtils.hasText(body)) {					
				resp = body;
				if (resp != null) {
					break;
				}
			}
			
		}
		return resp;
	}

	protected ComposerURLService getComposerURLService() {
		return this.composerURLService;
	}

 
	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}
}