package tpi.dgrv4.dpaa.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.TsmpInvokeHelper;
import tpi.dgrv4.dpaa.vo.DPB0123CgReqBody;
import tpi.dgrv4.dpaa.vo.DPB0123CgRespBody;
import tpi.dgrv4.dpaa.vo.DPB0123Resp;
import tpi.dgrv4.dpaa.vo.DPB0123Result;
import tpi.dgrv4.dpaa.vo.DPB0123ShowUI;
import tpi.dgrv4.dpaa.vo.TsmpInvokeCommLoopStatus;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class DPB0123Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpInvokeHelper tsmpInvokeHelper;
	private TsmpInvokeCommonService tsmpInvokeCommonService;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;

	@Autowired
	public DPB0123Service(TsmpInvokeHelper tsmpInvokeHelper, TsmpInvokeCommonService tsmpInvokeCommonService,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy) {
		super();
		this.tsmpInvokeHelper = tsmpInvokeHelper;
		this.tsmpInvokeCommonService = tsmpInvokeCommonService;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
	}

	/**
	 * for mockId = "DPB0123"
	 */
	public DPB0123Resp queryInvoke(String httpMethod, String userName, String codeChallenge,
			String mockId, String apiType, String locale) {
		String userIp = null;
		return queryInvoke(httpMethod, userName, codeChallenge, mockId, apiType, userIp, locale);
	}
	
	public DPB0123Resp queryInvoke(String httpMethod, String userName, String codeChallenge,
			String mockId, String apiType, String userIp, String locale) {
		checkInvokeParams(httpMethod, userName, codeChallenge, mockId, apiType, locale);
		
		DPB0123Resp resp = null;
		try {
			// 準備要呼叫對接企業API的參數
			Map<String, String> cgReqHeader = getCgReqHeader();
			String queryString = null;
			if(mockId.equals("DPB0123Udp")) {
				queryString = getQueryString(userName, codeChallenge, userIp, locale);
			}else {
				queryString = getQueryString(userName, codeChallenge, locale);
			}
			
			// 呼叫對接企業API(可能是Mock或直接調用)
			resp = callCgApi(mockId, apiType, cgReqHeader, null, userName, httpMethod, queryString);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//1297:執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private String getQueryString(String userName, String codeChallenge, String locale) {
		String queryString = "?username=" + userName + "&codeChallenge=" + codeChallenge + "&locale=" + locale;
		return queryString;
	}
	
	private String getQueryString(String userName, String codeChallenge, String userIp, String locale) {
		String queryString = "?username=" + userName + "&codeChallenge=" + codeChallenge + "&userIp=" + userIp + "&locale=" + locale;
		return queryString;
	}
	
	private void checkInvokeParams(String httpMethod, String userName, String codeChallenge,
			String mockId, String apiType, String locale) {
		if (
			!StringUtils.hasText(httpMethod) ||
			!StringUtils.hasText(userName) ||
			!StringUtils.hasText(codeChallenge) ||
			!StringUtils.hasText(mockId) ||
			!StringUtils.hasText(apiType) ||
			!StringUtils.hasText(locale)
		) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	private Map<String, String> getCgReqHeader() {
		Map<String, String> cgReqHeader = new HashMap<>();
		cgReqHeader.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
		return cgReqHeader;
	}

	private DPB0123CgReqBody getCgReqBody(String username, String codeChallenge) {
		DPB0123CgReqBody cgReqBody = new DPB0123CgReqBody();
		cgReqBody.setUsername(username);
		cgReqBody.setCodeChallenge(codeChallenge);
		return cgReqBody;
	}

	private String getTsmpDpItemsParam1(String itemNo, String subitemNo) {
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, LocaleType.EN_US);
		TsmpDpItems s = getTsmpDpItemsCacheProxy().findById(id);
		if (ObjectUtils.isEmpty(s) || !StringUtils.hasText(s.getParam1())) {
			this.logger.debug(String.format("無法取得 TSMP_DP_ITEMS: %s-%s", itemNo, subitemNo));
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		return s.getParam1();
	}
 
	protected DPB0123Resp callCgApi(String mockId, String apiType, Map<String, String> cgReqHeader, //
			DPB0123CgReqBody cgReqBody, String userName, String httpMethod, String queryString) {
		
		String isMock = getTsmpDpItemsParam1("MOCK_CONFIG", mockId);
		DPB0123Resp resp = new DPB0123Resp();
		// Mock調用
		if ("Y".equals(isMock)) {
			Long apptJobId = getTsmpInvokeHelper().createApptJobAndFile(mockId, apiType, cgReqHeader, cgReqBody, 
					userName, httpMethod, queryString);
			resp.setApptJobId(apptJobId);
		// 直接調用
		} else {
			DPB0123CgRespBody cgRespBody = getTsmpInvokeCommonService().callCgApi_direct(//
				mockId, apiType, cgReqHeader, cgReqBody, DPB0123CgRespBody.class, httpMethod, queryString);
			DPB0123ShowUI showUI = new DPB0123ShowUI();
	        showUI.setCgRespBody(cgRespBody);
	        resp.setShowUI(showUI);
		}
		return resp;
	}

	public DPB0123Resp queryLoopStatus(Long apptJobId, String locale) {
		checkLoopStatusParams(apptJobId, locale);
		
		DPB0123Resp resp = null;
		try {
			TsmpInvokeCommLoopStatus loopStatus = getTsmpInvokeHelper().queryApptJob(apptJobId, locale);
			resp = new DPB0123Resp();
			resp.setCommLoopStatus(loopStatus);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//1297:執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private void checkLoopStatusParams(Long apptJobId, String locale) {
		if (apptJobId == null || !StringUtils.hasText(locale)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}
	
	public DPB0123Resp queryResult(Long apptJobId, String locale, String mockId) {
		checkResultParams(apptJobId, locale, mockId);

		DPB0123Resp resp = null;
		try {
			Class<DPB0123CgRespBody> cgRespBodyClass = DPB0123CgRespBody.class;
			DPB0123CgRespBody cgRespBody = getTsmpInvokeCommonService().queryCgRespBody(apptJobId, mockId, locale, cgRespBodyClass);
			
			DPB0123ShowUI showUI = new DPB0123ShowUI();
			showUI.setCgRespBody(cgRespBody);
			
			DPB0123Result result = new DPB0123Result();
			// 雖然 TsmpInvokeCommonService.queryCgRespBody 有呼叫了 TsmpInvokeHelper.queryApptJob
			// 但沒有回傳出來，所以只好再查一次
			result.setCommLoopStatus( getTsmpInvokeHelper().queryApptJob(apptJobId, locale) );
			result.setShowUI(showUI);
			
			resp = new DPB0123Resp();
			resp.setResult(result);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//1297:執行錯誤
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private void checkResultParams(Long apptJobId, String locale, String mockId) {
		if (apptJobId == null) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		if (!StringUtils.hasText(locale)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		if (!StringUtils.hasText(mockId)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	protected TsmpInvokeHelper getTsmpInvokeHelper() {
		return this.tsmpInvokeHelper;
	}
 
	protected TsmpInvokeCommonService getTsmpInvokeCommonService() {
		return this.tsmpInvokeCommonService;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return tsmpDpItemsCacheProxy;
	}
}