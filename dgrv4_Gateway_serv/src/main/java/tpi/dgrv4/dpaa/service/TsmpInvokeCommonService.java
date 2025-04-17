package tpi.dgrv4.dpaa.service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpApptJobStatus;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.TsmpInvokeHelper;
import tpi.dgrv4.dpaa.vo.CgRespBody;
import tpi.dgrv4.dpaa.vo.TsmpInvokeCommLoopStatus;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Service
public class TsmpInvokeCommonService {

	private TPILogger logger = TPILogger.tl;

	private FileHelper fileHelper;
	private ObjectMapper objectMapper;
	private TsmpInvokeHelper tsmpInvokeHelper;

	@Autowired
	public TsmpInvokeCommonService(FileHelper fileHelper, ObjectMapper objectMapper,
			TsmpInvokeHelper tsmpInvokeHelper) {
		super();
		this.fileHelper = fileHelper;
		this.objectMapper = objectMapper;
		this.tsmpInvokeHelper = tsmpInvokeHelper;
	}

	public <R extends CgRespBody, T> R callCgApi_direct(String mockId, String apiType, Map<String, String> cgReqHeader, //
		T cgReqBody, Class<R> cgRespBodyClass, String httpMethod, String queryString) {
		HttpRespData httpRespData = callCgApi(mockId, apiType, cgReqHeader, cgReqBody, httpMethod, queryString);
		if (ObjectUtils.isEmpty(httpRespData)) {
			this.logger.debug("直接調用API時無回傳值");
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		// #1498-HTTP error
		if (HttpStatus.OK.value() != httpRespData.statusCode) {
			throw TsmpDpAaRtnCode._1498.throwing(httpRespData.respStr);
		}
		// #1497-介接規格錯誤
        R cgRespBody = null;
        try {
        	cgRespBody = getObjectMapper().readValue(httpRespData.respStr, cgRespBodyClass);
        } catch (Exception e) {
        	String statusCode = String.valueOf(httpRespData.statusCode);
        	throw TsmpDpAaRtnCode._1497.throwing(statusCode, httpRespData.respStr);
        }
        if (ObjectUtils.isEmpty(cgRespBody)) {
        	this.logger.debug(String.format("API回傳值轉換 %s 失敗", cgRespBodyClass.getName()));
			throw TsmpDpAaRtnCode._1298.throwing();
    	}
        	
        // #1499-介接邏輯錯誤
        String code = cgRespBody.getCode();
        if (!"0".equals(code)) {
        	throw TsmpDpAaRtnCode._1499.throwing(code, cgRespBody.getMessage());
        }
		
        return cgRespBody;
	}

	public <R extends CgRespBody> R queryCgRespBody(Long apptJobId, String mockId, String locale, //
			Class<R> cgRespBodyClass) {
		if (apptJobId == null) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		TsmpInvokeCommLoopStatus loopStatus = getTsmpInvokeHelper().queryApptJob(apptJobId, locale);
		String status = loopStatus.getStatus();
		if (!TsmpDpApptJobStatus.DONE.isValueEquals(status)) {
			this.logger.debug(String.format("工作尚未完成: %s-%s", status, loopStatus.getStatusName()));
			return null;
		}
		Map<String, Object> respFile = getRespFile(apptJobId, mockId);
		if (ObjectUtils.isEmpty(respFile)) {
			return null;
		}
		return getCgRespBody(respFile, cgRespBodyClass);
	}

	/**
	 * 禁止由外部呼叫此方法，都是為了單元測試而開放成 protected 的
	 * @param mockId
	 * @param apiType
	 * @param cgReqHeader
	 * @param cgReqBody
	 * @return
	 */
	protected <T> HttpRespData callCgApi(String mockId, String apiType, Map<String, String> cgReqHeader, //
		T cgReqBody, String httpMethod, String queryString) {
		if(HttpMethod.GET.name().equalsIgnoreCase(httpMethod)) {
			return getTsmpInvokeHelper().invokeGetApi(mockId, apiType, cgReqHeader, queryString);
		}else {
			return getTsmpInvokeHelper().invokePostApi(mockId, apiType, cgReqHeader, cgReqBody);
		}
	}

	protected Map<String, Object> getRespFile(Long apptJobId, String mockId) {
		String fileName = String.format("%sResp.txt", mockId);
		byte[] respContent = null;
		try {
			respContent = getFileHelper().downloadByTsmpDpFile(TsmpDpFileType.TSMP_DP_APPT_JOB, //
					apptJobId, fileName);
		} catch (Exception e) {
			this.logger.debug("讀取 Response 檔案錯誤: " + StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		if (respContent == null || respContent.length <= 0) {
			this.logger.debug(String.format("查無 Response 檔案或檔案內容為空: %s-%d-%s", //
				TsmpDpFileType.TSMP_DP_APPT_JOB.value(), apptJobId, fileName));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		try {
			String respContentStr = new String(respContent, StandardCharsets.UTF_8);
			return getObjectMapper().readValue(respContentStr, new TypeReference<Map<String, Object>>() {});
		} catch (Exception e) {
			this.logger.debug("Response 檔案轉換錯誤: " + StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	private <R extends CgRespBody> R getCgRespBody(Map<String, Object> respFile, Class<R> cgRespBodyClass) {
		// #1498-HTTP error
		String httpError = (String) respFile.get("httpError");
		if (!ObjectUtils.isEmpty(httpError)) {
			throw TsmpDpAaRtnCode._1498.throwing(httpError);
		}
		String cgRespBodyStr = (String) respFile.get("cgRespBody");
		R cgRespBody = null;
		// #1497-介接規格錯誤
		try {
			cgRespBody = getObjectMapper().readValue(cgRespBodyStr, cgRespBodyClass);
		} catch (Exception e) {
			throw TsmpDpAaRtnCode._1497.throwing(new String(), cgRespBodyStr);
		}
		// #1499-介接邏輯錯誤
        String code = cgRespBody.getCode();
        if (!"0".equals(code)) {
        	throw TsmpDpAaRtnCode._1499.throwing(code, cgRespBody.getMessage());
        }
        return cgRespBody;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected TsmpInvokeHelper getTsmpInvokeHelper() {
		return this.tsmpInvokeHelper;
	}
}