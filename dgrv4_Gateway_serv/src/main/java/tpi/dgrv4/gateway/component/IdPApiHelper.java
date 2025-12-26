package tpi.dgrv4.gateway.component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoApi;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoA;
import tpi.dgrv4.gateway.constant.DgrIdPReqBodyType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

/**
 * AC/GTW IdP(API) 流程的共用程式
 * 
 * @author Mini
 */
@Component
public class IdPApiHelper {

	private TokenHelper tokenHelper;
	
	private static String AC_IdP_API_login_API_failed = "AC IdP(API) login API failed";
	private static String GTW_IdP_API_login_API_failed = "GTW API(API) IdP login API failed";

	public static class ApiUserInfoData {
		public String errMsg;
		public String apiResp;
		public String userSub;
		public String userName;
		public String userEmail;
		public String userPicture;
		public String idtLightId ;
		public String idtRoleName;
	}

	@Autowired
	public IdPApiHelper(TokenHelper tokenHelper) {
		super();
		this.tokenHelper = tokenHelper;
	}

	/**
	 * 依參數 {{$%}} 在 JSON 中找出對應的值,並取代參數
	 */
	public static String getJsonValueByParam(String respJson, String paramStr) throws Exception {
		String value = "";

		if (!StringUtils.hasLength(paramStr)) {
			return value;
		}

		// 1.paramStr 參數, 例如."{{$user.name_cn%}} - {{$user.name_en%}}"
		// 2.取得參數中的 key, 並存成 list, 例如.[user.name_cn, user.name_en]
		List<String> keyList = getKeyList(paramStr);
		if (CollectionUtils.isEmpty(keyList)) {
			return value;
		}

		// 3.依 key, 在 JSON 中找出對應的值, 例如."李OO" 和 "Mini"
		value = paramStr;
		for (String key : keyList) {
			String nodeValue = getNodeValue(respJson, key);// 例如:"user.name_cn" 或 "user.name_en"
			if (nodeValue == null) {// 替換者(replacement)不能為 null, 改為 ""
				nodeValue = "";
			}
			// 4.用值取代參數, 例如."李OO - Mini"
			value = value.replace("{{$" + key + "%}}", nodeValue);
		}

		return value;
	}

	/**
	 * 取得參數{{$%}}中的值, <br>
	 * 例如."{{$user.name_th%}} - {{$user.name_en%}}" <br>
	 * 得到 "user.name_th", "user.name_en" <br>
	 */
	public static List<String> getKeyList(String str) {
		List<String> keyList = new ArrayList<>();
		Pattern pattern = Pattern.compile("\\{\\{\\$(.*?)%\\}\\}");
		if (str==null) {str="";}
		Matcher match = pattern.matcher(str); // 不接受 null 

		while (match.find()) {
			keyList.add(match.group(1));// 例如: "user.name_th" 或 "user.name_en"
		}
		return keyList;
	}

	/**
	 * 取得 JSON 中參數的值 <br>
	 * 
	 * @param respJson
	 * @param nodeNameStr, 例如."user.name_en"
	 * @return
	 */
	public static String getNodeValue(String respJson, String nodeNameStr) throws Exception {
		ObjectMapper objMapper = new ObjectMapper();
		JsonNode rootNode = objMapper.readTree(respJson);
		if (rootNode == null) {
			return null;
		}

		if (rootNode.isArray()) {
			rootNode = rootNode.get(0);// 取第一個元素
		}

		JsonNode node = getJsonNode(rootNode, nodeNameStr);
		if (node == null) {
			return null;
		}

		if (node.isArray()) {
	        // 轉成 JSON 字串
	        String nodeJsonString = new ObjectMapper().writeValueAsString(node);
			return nodeJsonString;
		}

		if (node.isObject()) {
			return  new ObjectMapper().writeValueAsString(node);
		}

		String value = node.asText();

		return value;
	}

	/**
	 * 使用遞迴,在 JSON 中取得指定 JsonNode <br>
	 * 
	 * @param parentNode
	 * @param nodeNameStr, 例如."user.name_en"
	 * @return
	 */
	public static JsonNode getJsonNode(JsonNode parentNode, String nodeNameStr) {
		if (parentNode == null) {
			return null;
		}

		int flag = nodeNameStr.indexOf(".");
		String nodeName = "";

		if (flag == -1) {
			nodeName = nodeNameStr;
			JsonNode node = parentNode.get(nodeName);
			return node;

		} else {
			nodeName = nodeNameStr.substring(0, flag);
			String nodeName1 = nodeNameStr.substring(0, flag);
			JsonNode node1 = parentNode.get(nodeName1);

			String nodeNameStr2 = nodeNameStr.substring(flag + 1);
			return getJsonNode(node1, nodeNameStr2);// 呼叫自己
		}
	}

	/**
	 * 調用 login API <br>
	 */
	public ApiUserInfoData callLoginApi(String reqUserName, String userMima, String userIp,
			Object infoData, String reqUri) throws IOException {

		String clientId = null;
		String loginUrl = null;
		String apiMethod = null;
		String reqHeader = null;
		String reqBodyType = null;
		String reqBody = null;
		String sucByType = null;
		String sucByField = null;
		String sucByValue = null;

		String idtName = null;
		String idtEmail = null;
		String idtPicture = null;

		String idtLightId = null;
		String idtRoleName = null;

		boolean isAc = true;
		String idPApiLoginApiFailed = null;
		if (infoData instanceof DgrGtwIdpInfoA gtwIdpInfoA) { // GTW
			isAc = false;
			idPApiLoginApiFailed = GTW_IdP_API_login_API_failed;
			
			clientId = gtwIdpInfoA.getClientId();
			loginUrl = gtwIdpInfoA.getApiUrl();
			apiMethod = gtwIdpInfoA.getApiMethod();
			reqHeader = gtwIdpInfoA.getReqHeader();
			reqBodyType = gtwIdpInfoA.getReqBodyType();
			reqBody = gtwIdpInfoA.getReqBody();
			sucByType = gtwIdpInfoA.getSucByType();
			sucByField = gtwIdpInfoA.getSucByField();
			sucByValue = gtwIdpInfoA.getSucByValue();

			idtName = gtwIdpInfoA.getIdtName();
			idtEmail = gtwIdpInfoA.getIdtEmail();
			idtPicture = gtwIdpInfoA.getIdtPicture();
			idtLightId = gtwIdpInfoA.getIdtLightId();
			idtRoleName = gtwIdpInfoA.getIdtRoleName();
			
		}else if (infoData instanceof DgrAcIdpInfoApi acIdpInfoApi) { // AC
			isAc = true;
			idPApiLoginApiFailed = AC_IdP_API_login_API_failed;
			
			loginUrl = acIdpInfoApi.getApiUrl();
			apiMethod = acIdpInfoApi.getApiMethod();
			reqHeader = acIdpInfoApi.getReqHeader();
			reqBodyType = acIdpInfoApi.getReqBodyType();
			reqBody = acIdpInfoApi.getReqBody();
			sucByType = acIdpInfoApi.getSucByType();
			sucByField = acIdpInfoApi.getSucByField();
			sucByValue = acIdpInfoApi.getSucByValue();

			idtName = acIdpInfoApi.getIdtName();
			idtEmail = acIdpInfoApi.getIdtEmail();
			idtPicture = acIdpInfoApi.getIdtPicture();
		}
		
		ApiUserInfoData apiUserInfoData = new ApiUserInfoData();

		try {
			// 將 header(JSON) 轉成 Map
			Map<String, List<String>> reqHeaderMap = convertJsonToMap(reqHeader);
			// 取代 API URL 中的參數
			loginUrl = replaceParam(reqUserName, userMima, userIp, loginUrl, true);

			// 取代 body 中的參數
			reqBody = replaceParam(reqUserName, userMima, userIp, reqBody, false);

			HttpRespData respData = null;
			if (HttpMethod.POST.name().equalsIgnoreCase(apiMethod)) {
				String msg = String.format("...Login API reqBodyType: %s(%s)", reqBodyType,
						DgrIdPReqBodyType.getText(reqBodyType));
				TPILogger.tl.debug(msg);
				
				// 依 Request Body 類型, 調用 API
				if (DgrIdPReqBodyType.FORM_DATA.isValueEquals(reqBodyType)) {// F：form-data
					// 將 body(JSON) 轉成 Map
					Map<String, List<String>> formDataMap = convertJsonToMap(reqBody);
					respData = callLoginApiByFormDataList(reqUserName, userMima, userIp, loginUrl, reqHeaderMap,
							formDataMap);

				} else if (DgrIdPReqBodyType.X_WWW_FORM_URLENCODED.isValueEquals(reqBodyType)) {// X：x-www-form-urlencoded
					// 將 body(JSON) 轉成 Map
					Map<String, List<String>> formDataMap = convertJsonToMap(reqBody);
					respData = callLoginApiByX_www_form_urlencoded_UTF8List(reqUserName, userMima, userIp, loginUrl,
							reqHeaderMap, formDataMap);

				} else if (DgrIdPReqBodyType.RAW.isValueEquals(reqBodyType)) {// R：raw
					respData = callLoginApiByRawDataList(reqUserName, userMima, userIp, loginUrl, reqHeaderMap,
							reqBody);

				} else {
					String errMsg = "";
					if (isAc) {
						errMsg = String.format("%s. DGR_AC_IDP_INFO_A.req_body_type: %s", 
								idPApiLoginApiFailed,
								reqBodyType);
					} else {
						 errMsg = String.format("%s. DGR_GTW_IDP_INFO_A.req_body_type: %s, client_id: %s",
								idPApiLoginApiFailed,
								reqBodyType, clientId);
					}
					
					TPILogger.tl.debug(errMsg);
					apiUserInfoData.errMsg = errMsg;
					return apiUserInfoData;

				}
				
			} else if (HttpMethod.GET.name().equalsIgnoreCase(apiMethod)) {
				respData = callLoginApiByGetList(reqUserName, userMima, userIp, loginUrl, reqHeaderMap, reqBody);
				
			} else {
				String errMsg = "";

				if (isAc) {
					errMsg = String.format("%s. DGR_AC_IDP_INFO_A.api_method: %s", 
							idPApiLoginApiFailed,
							apiMethod);
				} else {

					errMsg = String.format("%s. DGR_GTW_IDP_INFO_A.api_method: %s, client_id: %s",
						idPApiLoginApiFailed,
						apiMethod, 
						clientId);
				}
				TPILogger.tl.debug(errMsg);
				apiUserInfoData.errMsg = errMsg;
				return apiUserInfoData;
			}

			int statusCode = respData.statusCode;
			TPILogger.tl.debug("HTTP status code: " + statusCode);
			if (statusCode >= 300) {
				String errMsg = "";
				errMsg = String.format("%s. URL: %s, HTTP Status Code '%s' : %s", 
						idPApiLoginApiFailed,
						loginUrl,
						statusCode + "", respData.respStr);
				
				TPILogger.tl.debug(errMsg);
				
				int maxLength = 2000;
				if (errMsg.length() > maxLength) {// 如果訊息長度超過2000,則截斷,以免存入DB失敗
					errMsg = errMsg.substring(0, maxLength);
				}
				
				apiUserInfoData.errMsg = errMsg;
				return apiUserInfoData;
			}

			// Response JSON
			String respJson = respData.respStr;
			apiUserInfoData.apiResp = respJson;
			
			// [ZH] response 錯誤, 沒有值 
			// [EN] response is wrong, no value
			if (!StringUtils.hasLength(respJson)) { 
				String errMsg = String.format("%s. URL: %s, HTTP Status Code '%s' : %s. The API response is wrong, no value.", 
						idPApiLoginApiFailed,
						loginUrl,
						statusCode + "", 
						respData.respStr);

				TPILogger.tl.debug(errMsg);
				apiUserInfoData.errMsg = errMsg;
				return apiUserInfoData;
			}			
			
			if ("R".equals(sucByType)) {// 判定登入成功的類型為 Http status + return code
				boolean isSuccess = false;
				String respJsonValue = IdPApiHelper.getJsonValueByParam(respJson, sucByField);
				String[] sucByValueArr = sucByValue.split(",");
				for (String sucByValueTemp : sucByValueArr) {
					if (respJsonValue.equals(sucByValueTemp)) {
						isSuccess = true;
						break;
					}
				}
				
				if (!isSuccess) {
					/* 
					Login API failed. Response field value '200' mismatch '100,101'. 
					client_id: miniclient, suc_by_field: {{$status%}}, suc_by_value: 100,101
					*/
					String errMsg = "";
					if (isAc) {
						errMsg = String.format("%s. Response field value '%s' mismatch '%s'. "
								+ " suc_by_field: %s"
								+ ", suc_by_value: %s",
								idPApiLoginApiFailed,
								respJsonValue,
								sucByValue,
								sucByField,
								sucByValue);
						
					} else {
						errMsg = String.format("%s. Response field value '%s' mismatch '%s'. "
								+ "client_id: %s"
								+ ", suc_by_field: %s"
								+ ", suc_by_value: %s",
								idPApiLoginApiFailed,
								respJsonValue,
								sucByValue,
								clientId,
								sucByField,
								sucByValue);
					}
					
					TPILogger.tl.debug("api_url: " + loginUrl + "\n\t" + errMsg);
					apiUserInfoData.errMsg = errMsg;
					return apiUserInfoData;
				}
			}
			
			apiUserInfoData.userName = IdPApiHelper.getJsonValueByParam(respJson, idtName);
			apiUserInfoData.userEmail = IdPApiHelper.getJsonValueByParam(respJson, idtEmail);
			apiUserInfoData.userPicture = IdPApiHelper.getJsonValueByParam(respJson, idtPicture);
			apiUserInfoData.idtLightId = IdPApiHelper.getJsonValueByParam(respJson, idtLightId);
			apiUserInfoData.idtRoleName = IdPApiHelper.getJsonValueByParam(respJson, idtRoleName);
		} catch (Exception e) {
			TPILogger.tl.debug(StackTraceUtil.logStackTrace(e));
			String errMsg = String.format("%s. URL: %s", 
					idPApiLoginApiFailed,
					loginUrl);
			
			TPILogger.tl.debug(errMsg);
			apiUserInfoData.errMsg = errMsg;
		}

		return apiUserInfoData;
	}

	private String replaceParam(String reqUserName, String userMima, String userIp, String str, boolean isUrl)
			throws Exception {
		if (isUrl) {
			reqUserName = URLEncoder.encode(reqUserName, StandardCharsets.UTF_8.toString());
			userMima = URLEncoder.encode(userMima, StandardCharsets.UTF_8.toString());
			userIp = URLEncoder.encode(userIp, StandardCharsets.UTF_8.toString());
		}

		if (StringUtils.hasLength(str)) {
			str = str.replace("{{$username%}}", reqUserName);
			str = str.replace("{{$password%}}", userMima);
			str = str.replace("{{$ip%}}", userIp);
		}
		
		return str;
	}

	/**
	 * 調用 login API, <br>
	 * GET
	 */
	public HttpRespData callLoginApiByGetList(String reqUserName, String userMima, String userIp, String loginUrl,
			Map<String, List<String>> reqHeaderMap, String reqBody) throws Exception {
		HttpRespData respData = HttpUtil.httpReqByGetList(loginUrl, reqHeaderMap, false, false);
		TPILogger.tl.debug(respData.getLogStr());
		
		return respData;
	}

	/**
	 * 調用 login API, <br>
	 * POST "Content-Type":"multipart/form-data"
	 */
	public HttpRespData callLoginApiByFormDataList(String reqUserName, String userMima, String userIp, String loginUrl,
			Map<String, List<String>> reqHeaderMap, Map<String, List<String>> formDataMap) throws Exception {
		HttpRespData respData = HttpUtil.httpReqByFormDataList(loginUrl, "POST", formDataMap, reqHeaderMap, false,
				false);
		TPILogger.tl.debug(respData.getLogStr());

		return respData;
	}

	/**
	 * 調用 login API, <br>
	 * POST Content-Type":"application/x-www-form-urlencoded"
	 */
	public HttpRespData callLoginApiByX_www_form_urlencoded_UTF8List(String reqUserName, String userMima, String userIp,
			String loginUrl, Map<String, List<String>> reqHeaderMap, Map<String, List<String>> formDataMap)
			throws Exception {
		HttpRespData respData = HttpUtil.httpReqByX_www_form_urlencoded_UTF8List(loginUrl, "POST", formDataMap,
				reqHeaderMap, false, false);
		TPILogger.tl.debug(respData.getLogStr());
		
		return respData;
	}

	/**
	 * 調用 login API, <br>
	 * POST "Content-Type":"application/json"
	 */
	public HttpRespData callLoginApiByRawDataList(String reqUserName, String userMima, String userIp, String loginUrl,
			Map<String, List<String>> reqHeaderMap, String reqBody) throws Exception {
		HttpRespData respData = HttpUtil.httpReqByRawDataList(loginUrl, "POST", reqBody, reqHeaderMap, false, false);
		TPILogger.tl.debug(respData.getLogStr());
		
		return respData;
	}

	/**
	 * 1.將 req_header 或 req_body 的 JSON 格式 <br>
	 * 轉成 List<Map<String, String>> <br>
	 * 2.再轉成 Map<String, List<String>> <br>
	 * 
	 * @param reqJson : JSON 格式的字串 <br>
	 * 例如1. req_header<br>
	 * [{"Authorization":"bearer N9HzPSjTCU1zlYCeo1HJZk0XKQlm7t25"},{"Dgr-mock-test":"true"}]
	 * 例如2. req_body (form data 或 x-www-form-urlencoded) <br>
	 * [{"username":"{{$username%}}"},{"password":"{{$password%}}"},{"uid":"{{$ip%}}"},{"check":"Y"}]
	 * 例如3. req_body (Raw) <br>
	 * {"username":"{{$username%}}","password":"{{$password%}}","uid":"{{$ip%}}","check":"Y"}
	 */
	public static Map<String, List<String>> convertJsonToMap(String reqJson) throws Exception {
		Map<String, List<String>> newMap = new HashedMap<>();
		if (!StringUtils.hasLength(reqJson)) {
			return newMap;
		}
		
		// 1.將 JSON 轉成 List<Map<String, String>>
		ObjectMapper objectMapper = new ObjectMapper();
		List<Map<String, String>> list = objectMapper.readValue(reqJson,
				new TypeReference<List<Map<String, String>>>() {
				});

		// 2.再轉成 Map<String, List<String>>
		for (Map<String, String> origMap : list) {
			for (String key : origMap.keySet()) {
				String value = origMap.get(key);
				List<String> newList = newMap.get(key);
				if (newList == null) {
					newList = new LinkedList<>();
					newMap.put(key, newList);
				}
				newList.add(value);
			}
		}

		return newMap;
	}

	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}
}
