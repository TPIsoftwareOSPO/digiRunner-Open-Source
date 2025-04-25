package tpi.dgrv4.gateway.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.copy.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.codec.utils.HexStringUtils;
import tpi.dgrv4.codec.utils.SHA256Util;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DPB0189Service;
import tpi.dgrv4.dpaa.vo.DPB0189Req;
import tpi.dgrv4.dpaa.vo.DPB0189Resp;
import tpi.dgrv4.gateway.component.IdPUserInfoHelper.UserInfoData;
import tpi.dgrv4.gateway.constant.DgrIdPAlgType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.OAuthTokenErrorResp2;

/**
 * GTW IdP(JDBC) 流程的共用程式
 * 
 * @author Mini
 */
@Component
public class IdPJdbcHelper {
	private TokenHelper tokenHelper;
	private DPB0189Service dpb0189Service;
	private ObjectMapper objectMapper;
	
	public static String The_user_was_not_found = "The user was not found. username: ";
	public static String This_user_cannot_find_the_password_data = "This user cannot find the password data.";
	public static String User_account_or_password_is_incorrect = "User account or password is incorrect.";

	@Autowired
	public IdPJdbcHelper(TokenHelper tokenHelper, DPB0189Service dpb0189Service, ObjectMapper objectMapper) {
		super();
		this.tokenHelper = tokenHelper;
		this.dpb0189Service = dpb0189Service;
		this.objectMapper = objectMapper;
	}

	public UserInfoData checkUserAuth(String connName, String sqlPtmt, String sqlParams, String reqUserName,
			String reqUserMima, String userMimaAlg, String userMimaColName, String idtSubColName, String idtNameColName,
			String idtEmailColName, String idtPictureColName, String reqUri) throws Exception {
		UserInfoData userInfoData = new UserInfoData();

		// 1.將參數轉為 ArrayList
		List<String> paramList = new ObjectMapper().readValue(sqlParams, new TypeReference<ArrayList<String>>() {
		});
		
		// 2.取代 paramList 中的參數
		paramList = replaceParam(paramList, reqUserName);
 
		// 3.設定查詢 RDB 的 request
		DPB0189Req dpb0189Req = new DPB0189Req();
		dpb0189Req.setConnName(connName);
		dpb0189Req.setStrSql(sqlPtmt);	//"SELECT * FROM users WHERE user = ? AND pass = ?";
		dpb0189Req.setParamList(paramList); //["user", "pass"]

		// 4.執行 SQL,查詢 user 資料
		DPB0189Resp dpb0189Resp = getDPB0189Service().executeSql(dpb0189Req, null, false, false);
		String retCode = dpb0189Resp.getRtnCode();
		String result = dpb0189Resp.getResult();
		
		// 檢查查詢 RDB 後的結果,可能有發生錯誤或查無資料
		ResponseEntity<?> errRespEntity = checkQueryResult(reqUserName, retCode, result);
		userInfoData.errRespEntity = errRespEntity;
		if (errRespEntity != null) {
			return userInfoData;
		}
 
		// 5.將查詢 RDB 的結果 JSON,轉成 Map,以取得 user 密碼和 user info
		Map<String, String> resultMap = null;
		try {
			List<Map<String, String>> resultList = getObjectMapper().readValue(result,
					new TypeReference<List<Map<String, String>>>() {
					});
			
			int dataSize = 0;
			if (!CollectionUtils.isEmpty(resultList)) {
				dataSize = resultList.size();
			}
 
			if (dataSize != 1) {// 查詢RDB結果筆數不為1筆,實際是N筆
				String errMsg = "The number of RDB query results is not 1, but actually " + dataSize + ".";
				TPILogger.tl.debug(errMsg);
				userInfoData.errRespEntity = getResponseEntity(errMsg);
				return userInfoData;
			} else {
				resultMap = new CaseInsensitiveMap<String, String>();
				Map<String, String> map = resultList.get(0);
				resultMap.putAll(map);// 放入可忽略key值大小寫的Map
			}
 
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			String errMsg = TokenHelper.INTERNAL_SERVER_ERROR;
			TPILogger.tl.error(errMsg);
			userInfoData.errRespEntity = getTokenHelper().getInternalServerErrorResp(reqUri, errMsg);// 500
			return userInfoData;
		}

		// 6.取得RDB中的 user 密碼
		String dbUserMima = getValueByParam(resultMap, userMimaColName);
		if (!StringUtils.hasLength(dbUserMima)) {
			// 找不到RDB中 user 密碼欄位資料
			String errMsg = This_user_cannot_find_the_password_data + " password column name '" + userMimaColName
					+ "', username: " + reqUserName + ".";
			TPILogger.tl.debug(errMsg);
			userInfoData.errRespEntity = getResponseEntity(errMsg);
			return userInfoData;
		}

		// 7.依密碼演算法做 user 密碼比對
		boolean isMatch = checkUserMima(reqUserMima, dbUserMima, userMimaAlg);
		if (!isMatch) {
			// user 帳號或密碼不對
			String errMsg = User_account_or_password_is_incorrect;
			TPILogger.tl.debug(errMsg + " username: " + reqUserName);
			userInfoData.errRespEntity = getResponseEntity(errMsg);
			return userInfoData;
		}

		// 8.印出 user 驗證成功
		TPILogger.tl.debug("(JDBC) User authentication success.");
		
		// 9.取得 user info
		userInfoData.userName = getValueByParam(resultMap, idtSubColName);
		userInfoData.userAlias = getValueByParam(resultMap, idtNameColName);
		userInfoData.userEmail = getValueByParam(resultMap, idtEmailColName);
		userInfoData.userPicture = getValueByParam(resultMap, idtPictureColName);
		
		return userInfoData;
	}
	
	/**
	 * 依參數 {{$%}} 在 RDB 中找出對應的值,並取代參數
	 */
	public static String getValueByParam(Map<String, String> resultMap, String paramStr) throws Exception {
		String value = null;

		if (!StringUtils.hasLength(paramStr)) {
			return value;
		}

		// 1.paramStr 參數, 例如."{{$user_name_cn%}} - {{$user_name_en%}}"
		// 2.取得參數中的 key, 並存成 list, 例如.[user_name_cn, user_name_en]
		List<String> keyList = IdPApiHelper.getKeyList(paramStr);
		if (CollectionUtils.isEmpty(keyList)) {
			return value;
		}

		// 3.依 key, 在 JSON 中找出對應的值, 例如."李OO" 和 "Mini"
		value = paramStr;
		for (String key : keyList) {
			String dbValue = resultMap.get(key);// 例如:"user.name_cn" 或 "user.name_en"
			if (dbValue == null) {// 替換者(replacement)不能為 null, 改為 ""
				dbValue = "";
			}
			// 4.用值取代參數, 例如."李OO - Mini"
			value = value.replace("{{$" + key + "%}}", dbValue);
		}

		return value;
	}
	
	/**
	 * 取代 paramList 中的參數,
	 * 將 "{{$username%}}" 取代為 reqUserName
	 */
	private List<String> replaceParam(List<String> paramList, String reqUserName) throws Exception {
		List<String> paramListNew = new ArrayList<String>();
		for (String value : paramList) {
			if ("{{$username%}}".equals(value)) {// 若值是 "{{$username%}}",將值取代為 reqUserName
				paramListNew.add(reqUserName);

			} else {
				paramListNew.add(value);
			}
		}

		return paramListNew;
	}
 
	/**
	 * 檢查查詢RDB後的結果, <br>
	 * 可能會有發生錯誤,例如.查詢RDB的連線錯誤、找不到connName、找不到table, <br>
	 * 或查無資料
	 */
	private ResponseEntity<?> checkQueryResult(String reqUserName, String retCode, String result) {
		// 發生錯誤,例如.查詢RDB的連線錯誤、找不到connName、找不到table
		if ("1000".equals(retCode)) {
			TPILogger.tl.debug("Query error result: " + result);
			return getResponseEntity(result);
		}

		// 找不到 username 資料
		if (!"0000".equals(retCode)) {
			String errMsg = The_user_was_not_found + reqUserName;
			TPILogger.tl.debug("Query error result: " + result);
			return getResponseEntity(errMsg);
		}

		// 找不到 username 資料
		if (!StringUtils.hasLength(result) || "[]".equals(result)) {
			String errMsg = The_user_was_not_found + reqUserName;
			TPILogger.tl.debug(errMsg);
			return getResponseEntity(errMsg);
		}

		return null;
	}
	
	/**
	 * 依密碼演算法做密碼比對
	 */
	protected boolean checkUserMima(String reqUserMima, String dbUserMima, String userMimaAlg) throws Exception {
		boolean isMatch = false;

		// 依密碼演算法做密碼比對
		if (DgrIdPAlgType.PLAIN.equalsIgnoreCase(userMimaAlg)) {
			if (reqUserMima.equals(dbUserMima)) {// 比對密碼,區分大小寫
				isMatch = true;
			}
 
		} else if (DgrIdPAlgType.BCRYPT.equalsIgnoreCase(userMimaAlg)) {
			// 比對 user 密碼是否正確
			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			isMatch = passwordEncoder.matches(reqUserMima, dbUserMima);// 比對密碼
			
		} else if (DgrIdPAlgType.SHA256.equalsIgnoreCase(userMimaAlg)) {// 做 SHA256 轉 16進制
			byte[] byteArr = SHA256Util.getSHA256(reqUserMima.getBytes());
			reqUserMima = HexStringUtils.toString(byteArr);// 大寫
			if (reqUserMima.equalsIgnoreCase(dbUserMima)) {// 比對密碼,16進制不區分大小寫
				isMatch = true;
			}

		} else if (DgrIdPAlgType.SHA512.equalsIgnoreCase(userMimaAlg)) {// 做 SHA512 轉 16進制
			byte[] byteArr = SHA256Util.getSHA512(reqUserMima.getBytes());
			reqUserMima = HexStringUtils.toString(byteArr);// 大寫
			if (reqUserMima.equalsIgnoreCase(dbUserMima)) {// 比對密碼,16進制不區分大小寫
				isMatch = true;
			}
		
		} else {
			String errMsg = "Invalid userMimaAlg: " + userMimaAlg;
			TPILogger.tl.debug(errMsg);
			isMatch = false;
		}

		return isMatch;
	}

	private ResponseEntity<?> getResponseEntity(String errMsg) {
		ResponseEntity<?> errRespEntity = new ResponseEntity<OAuthTokenErrorResp2>(
				getTokenHelper().getOAuthTokenErrorResp2(TokenHelper.INVALID_USER, errMsg), HttpStatus.UNAUTHORIZED);// 401
		return errRespEntity;
	}

	protected TokenHelper getTokenHelper() {
		return tokenHelper;
	}

	protected DPB0189Service getDPB0189Service() {
		return dpb0189Service;
	}

	protected ObjectMapper getObjectMapper() {
		return objectMapper;
	}
}
