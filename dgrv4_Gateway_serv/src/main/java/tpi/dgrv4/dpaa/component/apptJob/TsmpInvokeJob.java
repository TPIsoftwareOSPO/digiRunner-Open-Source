package tpi.dgrv4.dpaa.component.apptJob;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.dpaa.component.TsmpInvokeHelper;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@SuppressWarnings("serial")
public class TsmpInvokeJob extends ApptJob {

	private TPILogger logger = TPILogger.tl;

	private ObjectMapper objectMapper;
	private TsmpInvokeHelper tsmpInvokeHelper;
	private FileHelper fileHelper;
	private TsmpDpFileDao tsmpDpFileDao;

	private Long apptJobId;

	private String userName;

	private Map<String, Object> params;

	@Autowired
	public TsmpInvokeJob(TsmpDpApptJob tsmpDpApptJob, ObjectMapper objectMapper, TsmpInvokeHelper tsmpInvokeHelper,
			FileHelper fileHelper, TsmpDpFileDao tsmpDpFileDao, ApptJobDispatcher apptJobDispatcher,
			TsmpDpApptJobDao tsmpDpApptJobDao) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		this.apptJobId = tsmpDpApptJob.getApptJobId();
		this.userName = tsmpDpApptJob.getCreateUser();
		this.objectMapper = objectMapper;
		this.tsmpInvokeHelper = tsmpInvokeHelper;
		this.fileHelper = fileHelper;
		this.tsmpDpFileDao = tsmpDpFileDao;
	}

	@Override
	public String runApptJob() throws Exception {
		this.params = parseInParams();

		checkParams();
		
		// 取得 Request JSON
		Map<String, Object> reqFile = getReqFile();
		Map<String, String> cgReqHeader = getCgReqHeader(reqFile);
		Object cgReqBody = getCgReqBody(reqFile);		
		
		// 調用對接企業(一站通) API
		HttpRespData httpRespData = callCgApi(cgReqHeader, cgReqBody);
		
		// 將回傳值存檔
		saveTsmpDpFile(httpRespData);

		return "SUCCESS";
	}

	private Map<String, Object> parseInParams() {
		Map<String, Object> params = null;
		String inParams = getTsmpDpApptJob().getInParams();
		try {
			params = getObjectMapper().readValue(inParams, new TypeReference<Map<String, Object>>(){});
		} catch (Exception e) {
			this.logger.debug(String.format("%d-無法解析 in_params，停止執行: %s", this.apptJobId, inParams));
		}
		return params;
	}

	private void checkParams() throws Exception {
		if (ObjectUtils.isEmpty(getParams()) ||
			ObjectUtils.isEmpty(getParams().get("mock_id")) ||
			ObjectUtils.isEmpty(getParams().get("api_type")) ||
			ObjectUtils.isEmpty(getParams().get("ref_file_id")) ||
			ObjectUtils.isEmpty(getParams().get("http_method"))
		) {
			throw new Exception("in_params 缺少必要參數");
		}
	}

	private Map<String, Object> getReqFile() throws Exception {
		Long fileId = Long.parseLong(String.valueOf(getParams().get("ref_file_id")));
		Optional<TsmpDpFile> opt = getTsmpDpFileDao().findById(fileId);
		if (!opt.isPresent()) {
			throw new Exception("查無 Request 檔案: " + fileId);
		}

		TsmpDpFile tsmpDpFile = opt.get();
		Map<String, Object> reqFile = new HashMap<>();
		try {
			byte[] blobData = getFileHelper().download(tsmpDpFile);
			if (blobData != null && blobData.length > 0) {
				String reqFileStr = new String(blobData, StandardCharsets.UTF_8);
				reqFile = getObjectMapper().readValue(reqFileStr, new TypeReference<Map<String, Object>>(){});
			}
		} catch (Exception e) {
			throw new Exception("取得 Request 檔案(" + fileId + ")失敗: " + e.getMessage());
		}
		if (ObjectUtils.isEmpty(reqFile)) {
			throw new Exception("空白的 Request 檔案: " + fileId);
		}
		return reqFile;
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> getCgReqHeader(Map<String, Object> reqFile) throws Exception {
		Map<String, String> cgReqHeader = null;
		try {
			cgReqHeader = (Map<String, String>) reqFile.get("cgReqHeader");
		} catch (Exception e) {
			throw new Exception("從 Request 檔案取出 CgReqHeader 失敗" + e.getMessage());
		}
		if (ObjectUtils.isEmpty(cgReqHeader)) {
			throw new Exception("空白的 cgReqHeader");
		}
		return cgReqHeader;
	}

	private Object getCgReqBody(Map<String, Object> reqFile) throws Exception {
		Object cgReqBody = reqFile.get("cgReqBody");
		if (ObjectUtils.isEmpty(cgReqBody)) {
			throw new Exception("空白的 cgReqBody");
		}
		return cgReqBody;
	}

	protected HttpRespData callCgApi(Map<String, String> cgReqHeader, Object cgReqBody) throws Exception {
		try {
			String mockId = (String) getParams().get("mock_id");
			String apiType = (String) getParams().get("api_type");
			String httpMethod = (String) getParams().get("http_method");
			
			if(HttpMethod.GET.name().equalsIgnoreCase(httpMethod)) {
				return getTsmpInvokeHelper().invokeGetApi(mockId, apiType, cgReqHeader, (String)cgReqBody);
			}else {
				return getTsmpInvokeHelper().invokePostApi(mockId, apiType, cgReqHeader, cgReqBody);
			}
		} catch (Exception e) {
			throw new Exception("調用對接企業(一站通) API 失敗: " + e.getMessage());
		}
	}

	private void saveTsmpDpFile(HttpRespData httpRespData) throws Exception {
		Map<String, Object> respFile = null;
		 try {
			 respFile = new HashMap<>();
			 respFile.put("mockId", getParams().get("mock_id"));
			 respFile.put("apiType", getParams().get("api_type"));
			 respFile.put("cgRespBody", null);
			 respFile.put("httpError", null);
			 int statusCode = httpRespData.statusCode;
			 if (HttpStatus.OK.value() == statusCode) {
				 respFile.put("cgRespBody", httpRespData.respStr);
			 } else {
				 respFile.put("httpError", httpRespData.respStr);
			 }
			 String respFileStr = getObjectMapper().writeValueAsString(respFile);
			 // 建立 TSMP_DP_FILE
			 String filename = String.format("%sResp.txt", getParams().get("mock_id"));
			 String isTmpfile = "N";
			 getFileHelper().upload(this.userName, TsmpDpFileType.TSMP_DP_APPT_JOB, this.apptJobId, //
					filename, respFileStr.getBytes(StandardCharsets.UTF_8), isTmpfile);
		 } catch (Exception e) {
			 throw new Exception("建立 Response File 失敗: " + e.getMessage());
		 }
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected TsmpInvokeHelper getTsmpInvokeHelper() {
		return this.tsmpInvokeHelper;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected Map<String, Object> getParams() {
		return this.params;
	}

}

