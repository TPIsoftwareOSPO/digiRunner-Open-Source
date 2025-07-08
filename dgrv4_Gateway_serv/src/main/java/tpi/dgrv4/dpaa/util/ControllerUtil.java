package tpi.dgrv4.dpaa.util;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.BeforeControllerReq;
import tpi.dgrv4.common.vo.BeforeControllerResp;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.DpaaStaticResourceInitializer;
import tpi.dgrv4.gateway.component.authorization.TsmpAuthorizationParser;
import tpi.dgrv4.gateway.component.authorization.TsmpAuthorizationParserFactory;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.DPB0115Service;
import tpi.dgrv4.gateway.vo.DPB0115Item;
import tpi.dgrv4.gateway.vo.DPB0115Req;
import tpi.dgrv4.gateway.vo.DPB0115Resp;
import tpi.dgrv4.gateway.vo.ResHeader;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.gateway.vo.TsmpBaseReq;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;
import tpi.dgrv4.gateway.vo.TsmpHttpHeader;

public class ControllerUtil {

	private static TPILogger logger = TPILogger.tl;


	public static boolean isLocalRoute(String ip) {
		var localIps = Set.of(
				"127.0.0.1",
				"localhost",
				"0:0:0:0:0:0:0:1"
		);
		return localIps.contains(ip);
	}
//	
//	private static final ObjectMapper om = new ObjectMapper();

	/**
	 * Injected by {@link DpaaStaticResourceInitializer}
	 */
	private static DPB0115Service dpb0115Service;

//	// 關閉序列化空物件時的例外錯誤(允許序列化空物件)
//	static {
//		om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//	}
//
//	/** Specify T as request body class type */
//	public static final <T> ReqPayload<T> toReqPayload(String jsonStr, Class<T> clazz) {
//		ReqPayload<T> reqPayload = new ReqPayload<>();
//		
//		try {
//			JsonNode root = om.readTree(jsonStr);
//
//			String keyOfHeader = ReqPayload.KEY_OF_HEADER;
//			JsonNode header_node = root.findValue(keyOfHeader);
//			if (header_node != null) {
//				ReqHeader header = om.treeToValue(header_node, ReqHeader.class);
//				reqPayload.setHeader(header);
//
//				String keyOfBody = clazz.getSimpleName();
//				JsonNode body_node = root.findValue(keyOfBody);
//				if (body_node != null && !body_node.isNull()) {
//					T body = om.treeToValue(body_node, clazz);
//					reqPayload.setBody(body);
//				} else {
//					IllegalArgumentException ex = new IllegalArgumentException(keyOfBody + " is required of this request");
//					throw new TsmpDpAaException(ex, header);
//				}
//			} else {
//				throw new IllegalArgumentException("ReqHeader is required of all requests");
//			}
//		} catch (IOException e) {
//			logger.debug("" + e);
//		}
//
//		return reqPayload;
//	}
//
//	public static final ReqHeader toReqHeader(String jsonStr) {
//		ReqHeader header = new ReqHeader();
//
//		try {
//			JsonNode root = om.readTree(jsonStr);
//
//			String keyOfHeader = ReqPayload.KEY_OF_HEADER;
//			JsonNode header_node = root.findValue(keyOfHeader);
//			if (header_node != null) {
//				header = om.treeToValue(header_node, ReqHeader.class);
//			}
//		} catch (Exception e) {
//			logger.debug("" + e);
//		}
//
//		return header;
//	}
//
//	public static final <T> String tsmpResponse(ReqHeader reqHeader, T responseBody) {
//		String jsonStr = null;
//
//		try {
//			ResHeader resHeader = new ResHeader();
//			resHeader.setTxSN(reqHeader.getTxSN());
//			resHeader.setTxDate(reqHeader.getTxDate());
//			resHeader.setTxID(reqHeader.getTxID());
//			resHeader.setRtnCode(TsmpDpAaRtnCode.SUCCESS.getCode());
//
//			ObjectNode root = om.createObjectNode();
//			
//			String keyOfHeader = ResPayload.KEY_OF_HEADER;
//			root.putPOJO(keyOfHeader, resHeader);
//			
//			if (responseBody != null) {
//				String keyOfBody = responseBody.getClass().getSimpleName();
//				root.putPOJO(keyOfBody, responseBody);
//			}
//
//			jsonStr = om.writeValueAsString(root);
//		} catch (JsonProcessingException e) {
//			logger.debug("" + e);
//		}
//
//		return jsonStr;
//	}
//
	public static final <T> TsmpBaseResp<T> tsmpResponseBaseObj(ReqHeader reqHeader, T responseBody) {
		try {
			ResHeader resHeader = new ResHeader();
			resHeader.setTxSN(reqHeader.getTxSN());
			resHeader.setTxDate(reqHeader.getTxDate());
			resHeader.setTxID(reqHeader.getTxID());
			resHeader.setRtnCode(TsmpDpAaRtnCode.SUCCESS.getCode());

			TsmpBaseResp<T> baseResp = new TsmpBaseResp<>();
			baseResp.setResHeader(resHeader);
			baseResp.setBody(responseBody);

			return baseResp;
		} catch (Exception e) {
			logger.debug("" + e);
		}

		return null;
	}
	
	/**
	 * 給 GET API 使用
	 * @param headers
	 * @param responseBody
	 * @return
	 */
	public static final <T> TsmpBaseResp<T> tsmpResponseBaseObj(HttpHeaders headers, T responseBody) {
		try {
			ResHeader resHeader = new ResHeader();
			String txSN = headers.getFirst("txSN");
			String txDate = headers.getFirst("txDate");
	    	String txID = headers.getFirst("txID");
			resHeader.setTxSN(txSN);
			resHeader.setTxDate(txDate);
			resHeader.setTxID(txID);
			resHeader.setRtnCode(TsmpDpAaRtnCode.SUCCESS.getCode());
			resHeader.setRtnMsg(TsmpDpAaRtnCode.SUCCESS.getDefaultMessage());

			TsmpBaseResp<T> baseResp = new TsmpBaseResp<>();
			baseResp.setResHeader(resHeader);
			baseResp.setBody(responseBody);

			return baseResp;
		} catch (Exception e) {
			logger.debug("" + e);
		}

		return null;
	}
	
	public static final TsmpAuthorization parserAuthorization(String authorization) throws Exception {
		TsmpAuthorizationParser authorizationParser = new TsmpAuthorizationParserFactory() //
				.getParser(authorization);
		TsmpAuthorization auth = authorizationParser.parse();
		return auth;
	}

	public static final TsmpHttpHeader toTsmpHttpHeader(HttpHeaders headers) {
		TsmpHttpHeader tsmpHttpHeader = new TsmpHttpHeader();

		if (headers == null) {
			return tsmpHttpHeader;
		}

		// Authorization
		TsmpAuthorization auth = null;
		try {
			String authorization = headers.getFirst("authorization");
			TsmpAuthorizationParser authorizationParser = new TsmpAuthorizationParserFactory() //
				.getParser(authorization);
			auth = authorizationParser.parse();
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
		} finally {
			if (auth == null) {
				auth = new TsmpAuthorization();
			}
			tsmpHttpHeader.setAuthorization(auth);
		}

		// SignCode
		try {
			String signCode = headers.getFirst("signcode");
			if (signCode != null && !signCode.isEmpty()) {
				tsmpHttpHeader.setSignCode(signCode);
			}
		} catch (Exception e) {
			logger.debug("" + e);
		}
		// TxToken
		try {
			String txToken = headers.getFirst("txtoken");
			if (txToken != null && !txToken.isEmpty()) {
				tsmpHttpHeader.setTxToken(txToken);
			}
		} catch (Exception e) {}

		return tsmpHttpHeader;
	}

	/**
	 * 取得Request的限制式
	 * @param req
	 * @return
	 */
	public static <Q> TsmpBaseResp<BeforeControllerResp> getReqConstraints(// 
		TsmpBaseReq<BeforeControllerReq> req, Q reqBody
	) {
		TsmpBaseReq<Q> q = new TsmpBaseReq<>();
		q.setReqHeader(req.getReqHeader());
		q.setBody(reqBody);
		return getReqConstraints(q);
	}

	/**
	 * 取得Request的限制式
	 * @param req
	 * @return
	 */
	public static <Q> TsmpBaseResp<BeforeControllerResp> getReqConstraints(TsmpBaseReq<Q> req) {
		BeforeControllerResp resp = new BeforeControllerResp();
		ReqHeader reqHeader = req.getReqHeader();
		Q q = req.getBody();
		if (ReqValidator.class.isAssignableFrom(q.getClass())) {
			String locale = reqHeader.getLocale();
			resp.setConstraints(((ReqValidator) q).constraints(locale));
		}
		return tsmpResponseBaseObj(reqHeader, resp);
	}

	/**
	 * 1. 檢查Token中的Role是否有權限呼叫API txID<br>
	 * 2. 執行Request的validate方法(如果有實作的話)
	 * @param req
	 */
	public static <Q> void validateRequest(TsmpAuthorization auth, TsmpBaseReq<Q> req) {
		// #1
		if (auth != null) {
			isTxIdAvailable(auth, req.getReqHeader().getTxID());
		}
		// #2
		Q q = req.getBody();
		if (ReqValidator.class.isAssignableFrom(q.getClass())) {
			((ReqValidator) q).validate();
		}
	}

	public static void isTxIdAvailable(TsmpAuthorization auth, String txId) {
		DPB0115Req req = new DPB0115Req();
		req.setTxIdList(Arrays.asList(new String[] {txId}));
		try {
			DPB0115Resp resp = getDPB0115Service().queryRTMapByUk(auth, req);
			if (resp == null) throw new Exception("Fail to call DPB0115Service");
			List<DPB0115Item> dpb0115Items = resp.getDataList();
			if (dpb0115Items == null || dpb0115Items.isEmpty()) throw new Exception("Empty DPB0115Item List");
			DPB0115Item dpb0115Item = dpb0115Items.get(0);
			if (dpb0115Item == null) throw new Exception("Empty DPB0115Item");
			if (dpb0115Item.getAvailable() == null) throw new Exception("DPB0115Item [available] flag is empty");
			if (!dpb0115Item.getAvailable()) throw TsmpDpAaRtnCode._1357.throwing(txId);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing(txId);
		}
	}

	/**
	 * Locale 修改成符合資料表中的資料格式, ex. zh-TW
	 * 
	 * @param locale
	 * @return
	 */
	public static String getLocale(String locale) {
		try {
			//若傳入值為空,則預設為英文
			if(!StringUtils.hasLength(locale)) {
				// 不使用 Locale.getDefault(), 因為 pipelines 測試主機取出來的語系不一定存在TsmpRtnCode
				//Locale currentLocale = Locale.getDefault();
				locale = "en-US";
			};
 
			//修改成符合DB中的資料格式, ex.zh-TW
			if(StringUtils.hasLength(locale)) {
				locale = locale.replace("_", "-"); 
			}
			
			String[] arr = locale.split("-");
			if(StringUtils.hasLength(arr[0])) { //language
				arr[0] = arr[0].toLowerCase();
			}
			
			if(StringUtils.hasLength(arr[1])) { //country
				arr[1] = arr[1].toUpperCase();
			}
			
			locale = arr[0].concat("-").concat(arr[1]);
			
			//tom, 20240411, 因為之後會越來越多語系,所以不再限制語系,以免每次都要修正
			// 若不是EN_US或ZH_TW，預設為EN_US
			//if (!LocaleType.EN_US.matches(locale)&&!LocaleType.ZH_TW.matches(locale)) {
				//locale = LocaleType.EN_US;
			//}
			
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
		}
		
		return locale;
	}

	public static DPB0115Service getDPB0115Service() {
		return dpb0115Service;
	}

	public static void setDPB0115Service(DPB0115Service dpb0115Service) {
		ControllerUtil.dpb0115Service = dpb0115Service;
	}

}
