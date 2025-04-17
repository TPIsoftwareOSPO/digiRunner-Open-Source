package tpi.dgrv4.gateway.util;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.BeforeControllerReq;
import tpi.dgrv4.common.vo.BeforeControllerResp;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.entity.exceptions.DgrException;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.entity.exceptions.ICheck;
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
	
	private static TPILogger logger;
	
	private static ObjectMapper objectMapper;
	
	private static DPB0115Service dpb0115Service;
	
	public static final TsmpAuthorization parserAuthorization(String authorization) throws Exception {
		TsmpAuthorizationParser authorizationParser = new TsmpAuthorizationParserFactory() //
				.getParser(authorization);
		TsmpAuthorization cu_auth = authorizationParser.parse();
		return cu_auth;
	}
	
	public static final TsmpHttpHeader toTsmpHttpHeader(HttpHeaders headers) {
		TsmpHttpHeader tsmpHttpHeader = new TsmpHttpHeader();

		if (headers == null) {
			return tsmpHttpHeader;
		}

		// Authorization
		TsmpAuthorization cu_auth = null;
		try {
			String authorization = headers.getFirst("authorization");
			TsmpAuthorizationParser authorizationParser = new TsmpAuthorizationParserFactory() //
					.getParser(authorization);
			cu_auth = authorizationParser.parse();
		} catch (Exception e) {
			
		} finally {
			if (cu_auth == null) {
				cu_auth = new TsmpAuthorization();
			}
			tsmpHttpHeader.setAuthorization(cu_auth);
		}

		// SignCode
		try {
			String signCode = headers.getFirst("signcode");
			if (signCode != null && !signCode.isEmpty()) {
				tsmpHttpHeader.setSignCode(signCode);
			}
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
		}

		// TxToken
		try {
			String txToken = headers.getFirst("txtoken");
			if (txToken != null && !txToken.isEmpty()) {
				tsmpHttpHeader.setTxToken(txToken);
			}
		} catch (Exception e) {
		}

		return tsmpHttpHeader;
	}

	public static final <T> TsmpBaseResp<T> tsmpResponseBaseObj(ReqHeader reqHeader, T responseBody) {
		try {
			ResHeader resHeader = new ResHeader();
			resHeader.setTxSN(reqHeader.getTxSN());
			resHeader.setTxDate(reqHeader.getTxDate());
			resHeader.setTxID(reqHeader.getTxID());
			resHeader.setRtnCode(DgrRtnCode.SUCCESS.getCode());

			TsmpBaseResp<T> baseResp = new TsmpBaseResp<>();
			baseResp.setResHeader(resHeader);
			baseResp.setBody(responseBody);

			return baseResp;
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
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
			String cu_txSN = headers.getFirst("txSN");
			String txDate = headers.getFirst("txDate");
	    	String txID = headers.getFirst("txID");
			resHeader.setTxSN(cu_txSN);
			resHeader.setTxDate(txDate);
			resHeader.setTxID(txID);
			resHeader.setRtnCode(DgrRtnCode.SUCCESS.getCode());
			resHeader.setRtnMsg(DgrRtnCode.SUCCESS.getDefaultMessage());

			TsmpBaseResp<T> baseResp = new TsmpBaseResp<>();
			baseResp.setResHeader(resHeader);
			baseResp.setBody(responseBody);

			return baseResp;
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
		}

		return null;
	}

	private static ObjectMapper getObjectMapper() {
		return objectMapper;
	}

	public static void setObjectMapper(ObjectMapper objectMapper) {
		ControllerUtil.objectMapper = objectMapper;
	}
	
	/**
	 * 取得Request的限制式
	 * @param req
	 * @return
	 */
	public static <Q> TsmpBaseResp<BeforeControllerResp> getReqConstraints(// 
		TsmpBaseReq<BeforeControllerReq> req, Q reqBody
	) {
		TsmpBaseReq<Q> cu_q = new TsmpBaseReq<>();
		cu_q.setReqHeader(req.getReqHeader());
		cu_q.setBody(reqBody);
		return getReqConstraints(cu_q);
	}
	
	/**
	 * 取得Request的限制式
	 * @param req
	 * @return
	 */
	public static <Q> TsmpBaseResp<BeforeControllerResp> getReqConstraints(TsmpBaseReq<Q> req) {
		BeforeControllerResp resp = new BeforeControllerResp();
		ReqHeader reqHeader = req.getReqHeader();
		Q cu_q = req.getBody();
		if (ReqValidator.class.isAssignableFrom(cu_q.getClass())) {
			String locale = reqHeader.getLocale();
			resp.setConstraints(((ReqValidator) cu_q).constraints(locale));
		}
		return tsmpResponseBaseObj(reqHeader, resp);
	}
	
	/**
	 * 1. 檢查Token中的Role是否有權限呼叫API txID<br>
	 * 2. 執行Request的validate方法(如果有實作的話)
	 * 
	 * @param req
	 */
	public static <Q> void validateRequest(TsmpAuthorization auth, TsmpBaseReq<Q> req) {
		// #1
		if (auth != null) {
			isTxIdAvailable(auth, req.getReqHeader().getTxID());
		}
		// #2
		Q cu_q = req.getBody();
		if (ReqValidator.class.isAssignableFrom(cu_q.getClass())) {
			((ReqValidator) cu_q).validate();
		}
	}
	
	public static void isTxIdAvailable(TsmpAuthorization auth, String txId) {
		DPB0115Req req = new DPB0115Req();
		req.setTxIdList(Arrays.asList(new String[] { txId }));
		try {
			DPB0115Resp resp = getDPB0115Service().queryRTMapByUk(auth, req);
			if (resp == null)
				throw new Exception("Fail to call DPB0115Service");
			List<DPB0115Item> cu_dpb0115Items = resp.getDataList();
			if (cu_dpb0115Items == null || cu_dpb0115Items.isEmpty())
				throw new Exception("Empty DPB0115Item List");
			DPB0115Item cu_dpb0115Item = cu_dpb0115Items.get(0);
			if (cu_dpb0115Item == null)
				throw new Exception("Empty DPB0115Item");
			if (cu_dpb0115Item.getAvailable() == null)
				throw new Exception("DPB0115Item [available] flag is empty");
			if (!cu_dpb0115Item.getAvailable())
				throw DgrRtnCode._1357.throwing(txId);
		} catch (DgrException e) {
			throw e;
		} catch (Exception e) {
			throw DgrRtnCode._1297.throwing(txId);
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
			//若傳入值為空,則取得系統的 locale
			if(!StringUtils.hasLength(locale)) {
				// 不使用 Locale.getDefault(), 因為 pipelines 測試主機取出來的語系不一定存在TsmpRtnCode
				//Locale currentLocale = Locale.getDefault();
				Locale currentLocale = Locale.TRADITIONAL_CHINESE;
				locale = currentLocale.toString();
			};
 
			//修改成符合DB中的資料格式, ex.zh-TW
			if(StringUtils.hasLength(locale)) {
				locale = locale.replace("_", "-"); 
				
				String[] cu_arr = locale.split("-");
				if(StringUtils.hasLength(cu_arr[0])) { //language
					cu_arr[0] = cu_arr[0].toLowerCase(); 
				}
				
				if(StringUtils.hasLength(cu_arr[1])) { //country
					cu_arr[1] = cu_arr[1].toUpperCase();
				}
				
				locale = cu_arr[0].concat("-").concat(cu_arr[1]);
			}
			
			//tom, 20240411, 因為之後會越來越多語系,所以不再限制語系,以免每次都要修正,但目前似乎沒有人呼叫這個method
			// 若不是EN_US或ZH_TW，預設為EN_US
			//if (!LocaleType.EN_US.matches(locale)&&!LocaleType.ZH_TW.matches(locale)) {
				//locale = LocaleType.EN_US;
			//}
			
		} catch (Exception cu_e) {
			logger.debug(StackTraceUtil.logStackTrace(cu_e));
		}
		
		return locale;
	}

	
	public static DPB0115Service getDPB0115Service() {
		return dpb0115Service;
	}

	public static void setDPB0115Service(DPB0115Service dpb0115Service) {
		ControllerUtil.dpb0115Service = dpb0115Service;
	}

	public static void setLogger(TPILogger logger) {
		ControllerUtil.logger = logger;
	}
	
	/**
	 * 給 檢查器 使用
	 * @param headers
	 * @param responseBody
	 * @return
	 */
	public static final ResHeader tsmpCheckResponseBaseObj(ICheck check, String locale) {
		try {
			ResHeader resHeader = new ResHeader();
			resHeader.setRtnCode(check.getRtnCode().getCode());
			resHeader.setRtnMsg(String.format("%s - %s", check.getRtnCode().getCode(),check.getMessage(locale)));

//			TsmpCheckBaseResp baseResp = new TsmpCheckBaseResp();
//			baseResp.setResHeader(resHeader);
//			baseResp.setCode(String.format("TAA-%s", check.getRtnCode().getCode()));
//			baseResp.setMessage(check.getMessage(locale));
//			baseResp.setErrorDescription(check.getErrorDescription(locale));

			return resHeader;
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
		}

		return null;
	}
}