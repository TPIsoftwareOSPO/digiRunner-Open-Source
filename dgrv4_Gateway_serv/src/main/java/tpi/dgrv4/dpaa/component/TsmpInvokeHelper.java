package tpi.dgrv4.dpaa.component; 

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.constant.LocaleType;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.TsmpInvokeCommLoopStatus;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Component
public class TsmpInvokeHelper {

	private TPILogger logger = TPILogger.tl;

	private ObjectMapper objectMapper;
	private FileHelper fileHelper;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private TsmpDpApptJobDao tsmpDpApptJobDao;
	private ApptJobDispatcher apptJobDispatcher;

	@PersistenceContext
	private EntityManager em;

	@Autowired
	public TsmpInvokeHelper(ObjectMapper objectMapper, FileHelper fileHelper,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpDpApptJobDao tsmpDpApptJobDao,
			ApptJobDispatcher apptJobDispatcher) {
		super();
		this.objectMapper = objectMapper;
		this.fileHelper = fileHelper;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
		this.apptJobDispatcher = apptJobDispatcher;
	}

	public <T> Long createApptJobAndFile(String mockId, String apiType, Map<String, String> cgReqHeader, 
			T cgReqBody, String userName, String httpMethod, String queryString) {
		// 檢查參數
		if (ObjectUtils.isEmpty(mockId) || ObjectUtils.isEmpty(apiType) ||
			cgReqHeader == null) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		if(HttpMethod.POST.name().equalsIgnoreCase(httpMethod)) {
			if (ObjectUtils.isEmpty(cgReqBody)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
		}

		// 建立 TSMP_DP_APPT_JOB
		TsmpDpApptJob tsmpDpApptJob = createTsmpDpApptJob(mockId, apiType, userName);
		if (ObjectUtils.isEmpty(tsmpDpApptJob)) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		Long apptJobId = tsmpDpApptJob.getApptJobId();
		// 將 Request JSON 寫入 TSMP_DP_FILE
		TsmpDpFile tsmpDpFile = createTsmpDpFile(mockId, apiType, cgReqHeader, cgReqBody, //
			userName, apptJobId, httpMethod, queryString);
		if (ObjectUtils.isEmpty(tsmpDpFile)) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		Long fileId = tsmpDpFile.getFileId();
		// 更新 TSMP_DP_APPT_JOB
		apptJobId = updateTsmpDpApptJob(tsmpDpApptJob, mockId, apiType, fileId, httpMethod);
		if (ObjectUtils.isEmpty(apptJobId)) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return apptJobId;
	}

	public <T> HttpRespData invokePostApi(String mockId, String apiType, //
		Map<String, String> cgReqHeader, T cgReqBody) {
		// 檢查參數
		if (ObjectUtils.isEmpty(mockId) || ObjectUtils.isEmpty(apiType) ||
			cgReqHeader == null || ObjectUtils.isEmpty(cgReqBody)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		// 取得對接企業(一站通)的 API URL
		String apiUrl = getApiUrl(mockId);
		
		// 取得調用對接企業(一站通)的 Req Body (轉成 JSON)
		String reqBodyStr = null;
		try {
			reqBodyStr = getObjectMapper().writeValueAsString(cgReqBody);
		} catch (Exception e) {
			this.logger.debug("cgReqBody 轉換 JSON 失敗\n" + StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		// 調用對接企業(一站通) API
		return callApiPost(apiUrl, reqBodyStr, cgReqHeader);
	}

	public <T> HttpRespData invokeGetApi(String mockId, String apiType, //
		Map<String, String> cgReqHeader, String queryString) {
		// 檢查參數
		if (ObjectUtils.isEmpty(mockId) || ObjectUtils.isEmpty(apiType) ||
			cgReqHeader == null || StringUtils.isEmpty(queryString)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		// 取得對接企業(一站通)的 API URL
		String apiUrl = getApiUrl(mockId);
 
		// 調用對接企業(一站通) API
		return callApiGet(apiUrl, queryString, cgReqHeader);
	}
	
	private String getApiUrl(String mockId) {
		// 取得對接企業(一站通)的 API URL
		String apiUrl = getParam2ApiUrl(mockId);
		if (ObjectUtils.isEmpty(apiUrl)) {
			this.logger.debug(mockId + " 的 API URL 為空值");
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return apiUrl;
	}

	public TsmpInvokeCommLoopStatus queryApptJob(Long apptJobId, String locale) {
		// 檢查參數
		if (ObjectUtils.isEmpty(apptJobId)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		// 查詢 TSMP_DP_APPT_JOB
		Optional<TsmpDpApptJob> opt = getTsmpDpApptJobDao().findById(apptJobId);
		if (!opt.isPresent()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		TsmpDpApptJob tsmpDpApptJob = opt.get();
		String status = tsmpDpApptJob.getStatus();
		// 取得狀態名稱
		TsmpDpItems tsmpDpItems = getTsmpDpItemsCacheProxy().findById(new TsmpDpItemsId("JOB_STATUS", status, locale));
		String statusName = Optional.ofNullable(tsmpDpItems) //
			.map((i) -> i.getSubitemName()) //
			.orElseGet(() -> new String());
		// 建立 CusCommLoopStatus 物件
		TsmpInvokeCommLoopStatus cusCommLoopStatus = new TsmpInvokeCommLoopStatus();
		cusCommLoopStatus.setApptJobId(apptJobId);
		cusCommLoopStatus.setStatus(status);
		cusCommLoopStatus.setStatusName(statusName);
		cusCommLoopStatus.setStackTrace(tsmpDpApptJob.getStackTrace());
		
		// 避免 entity 被咬住，其他執行序無法更新
		getEntityManager().detach(tsmpDpApptJob);

		return cusCommLoopStatus;
	}

	protected TsmpDpApptJob createTsmpDpApptJob(String mockId, String apiType, String userName) {
		TsmpDpApptJob tsmpDpApptJob = null;
		try {
			tsmpDpApptJob = new TsmpDpApptJob();
			tsmpDpApptJob.setRefItemNo("TSMP_INVOKE");
			tsmpDpApptJob.setRefSubitemNo(null);
			String identifData = String.format("userName=%s,　mock_id=%s,　api_type=%s", userName, mockId, apiType);
			tsmpDpApptJob.setIdentifData(identifData);
			tsmpDpApptJob.setStartDateTime( delayByNow(60L) );	// 現在時間+1分鐘, 註:避免預約工作馬上執行
			tsmpDpApptJob.setCreateDateTime(DateTimeUtil.now());
			tsmpDpApptJob.setCreateUser(userName);
			tsmpDpApptJob = getTsmpDpApptJobDao().save(tsmpDpApptJob);
		} catch (Exception e) {
			this.logger.debug("建立 TSMP_DP_APPT_JOB 失敗\n" + StackTraceUtil.logStackTrace(e));
		}
		return tsmpDpApptJob;
	}

	protected <T> TsmpDpFile createTsmpDpFile(String mockId, String apiType, //
			Map<String, String> cgReqHeader, T cgReqBody, String userName, Long apptJobId, 
			String httpMethod, String queryString) {
		
		TsmpDpFile tsmpDpFile = null;
		try {
			String reqFileStr = "";
			Map<String, Object> reqFile = new HashMap<>();
			reqFile.put("mockId", mockId);
			reqFile.put("apiType",  apiType);
			reqFile.put("cgReqHeader", cgReqHeader);
			
			if(HttpMethod.GET.name().equalsIgnoreCase(httpMethod)) {
				reqFile.put("cgReqBody", queryString);
			}else {
				reqFile.put("cgReqBody", cgReqBody);
			}
			reqFileStr = getObjectMapper().writeValueAsString(reqFile);
			
			String filename = String.format("%sReq.txt", mockId);
			String isTmpfile = "N";
			byte[] content = reqFileStr.getBytes(StandardCharsets.UTF_8);
			tsmpDpFile = getFileHelper().upload(userName, //
				TsmpDpFileType.TSMP_DP_APPT_JOB, apptJobId, filename, content, isTmpfile);
			
		} catch (Exception e) {
			this.logger.debug("建立 TSMP_DP_FILE 失敗\n" + StackTraceUtil.logStackTrace(e));
		}
		return tsmpDpFile;
	}

	protected Long updateTsmpDpApptJob(TsmpDpApptJob tsmpDpApptJob, String mockId, String apiType, 
			Long fileId, String httpMethod) {
		Long apptJobId = null;
		try {
			Map<String, Object> inParamsMap = new HashMap<>();
			inParamsMap.put("mock_id", mockId);
			inParamsMap.put("api_type", apiType);
			inParamsMap.put("ref_file_id", fileId);
			inParamsMap.put("http_method", httpMethod);
			String inParams = getObjectMapper().writeValueAsString(inParamsMap);
			
			tsmpDpApptJob.setInParams(inParams);
			tsmpDpApptJob.setStartDateTime(DateTimeUtil.now());
			tsmpDpApptJob = getApptJobDispatcher().addAndRefresh(tsmpDpApptJob);
			apptJobId = tsmpDpApptJob.getApptJobId();
		} catch (Exception e) {
			this.logger.debug("更新 TSMP_DP_FILE 失敗\n" + StackTraceUtil.logStackTrace(e));
		}
		return apptJobId;
	}
 
	protected String getParam2ApiUrl(String mockId) {
		TsmpDpItemsId id = new TsmpDpItemsId("MOCK_CONFIG", mockId, LocaleType.EN_US);
		TsmpDpItems s = getTsmpDpItemsCacheProxy().findById(id);
		
		if (ObjectUtils.isEmpty(s) || StringUtils.isEmpty(s.getParam2())) {
			this.logger.debug(String.format("無法取得 %s 的 API URL 設定", mockId));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return s.getParam2();
	}

	protected HttpRespData callApiPost(String apiUrl, String reqBodyStr, Map<String, String> cgReqHeader) {
		try {
			this.logger.debug("Request Body:\t" + reqBodyStr);
			this.logger.debug("-------------------------------------------");
			HttpRespData resp = HttpUtil.httpReqByRawData(apiUrl, HttpMethod.POST.name(), reqBodyStr, cgReqHeader, false);
			this.logger.debug(resp.getLogStr());
			return resp;
		} catch (Exception e) {
			this.logger.debug("對接企業(一站通) API 調用失敗(" + apiUrl + ")\n" + StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}
	
	protected HttpRespData callApiGet(String apiUrl, String queryString, Map<String, String> cgReqHeader) {
		try {
			apiUrl += queryString;
			this.logger.debug("Query String:\t" + queryString);
			this.logger.debug("-------------------------------------------");
			HttpRespData resp = HttpUtil.httpReqByGet(apiUrl, cgReqHeader, false);
			this.logger.debug(resp.getLogStr());
			return resp;
		} catch (Exception e) {
			this.logger.debug("對接企業(一站通) API 調用失敗(" + apiUrl + ")\n" + StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}
	
	public <T> T jsonToObj(String str, Class<T> clz) throws JsonParseException, JsonMappingException, IOException {
		T bodyObj = getObjectMapper().readValue(str, clz);
		return bodyObj;
	}

	private Date delayByNow(long seconds) {
		LocalDateTime ldt = LocalDateTime.now().plusSeconds(seconds);
		return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return this.tsmpDpApptJobDao;
	}
 
	protected ApptJobDispatcher getApptJobDispatcher() {
		return this.apptJobDispatcher;
	}

	protected EntityManager getEntityManager() {
		return this.em;
	}

}