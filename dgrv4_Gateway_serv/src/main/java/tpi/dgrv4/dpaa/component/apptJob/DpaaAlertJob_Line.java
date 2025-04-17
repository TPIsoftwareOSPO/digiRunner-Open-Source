package tpi.dgrv4.dpaa.component.apptJob;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.alert.DpaaAlertEvent;
import tpi.dgrv4.dpaa.service.TsmpSettingService;
import tpi.dgrv4.entity.entity.TsmpDpApptJob;
import tpi.dgrv4.entity.entity.jpql.TsmpAlert;
import tpi.dgrv4.entity.repository.TsmpAlertDao;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.gateway.component.job.appt.ApptJob;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@SuppressWarnings("serial")
public class DpaaAlertJob_Line extends ApptJob {

	private TPILogger logger = TPILogger.tl;

	private TsmpAlertDao tsmpAlertDao;
	private TsmpSettingService tsmpSettingService;
	private ObjectMapper objectMapper;

	@Autowired
	public DpaaAlertJob_Line(TsmpDpApptJob tsmpDpApptJob, ApptJobDispatcher apptJobDispatcher,
			TsmpDpApptJobDao tsmpDpApptJobDao, TsmpAlertDao tsmpAlertDao, TsmpSettingService tsmpSettingService,
			ObjectMapper objectMapper) {
		super(tsmpDpApptJob, TPILogger.tl, apptJobDispatcher, tsmpDpApptJobDao);
		this.tsmpAlertDao = tsmpAlertDao;
		this.tsmpSettingService = tsmpSettingService;
		this.objectMapper = objectMapper;
	}

	@Override
	public String runApptJob() throws Exception {
		String inParams = getTsmpDpApptJob().getInParams();
		DpaaAlertEvent dpaaAlertEvent = parseInParams(inParams);

		checkDpaaAlertEvent(dpaaAlertEvent);
		
		String lineAccessToken = getLineAccessToken(dpaaAlertEvent);
		HttpRespData resp =  sendLineMessage(lineAccessToken, dpaaAlertEvent);
		return checkResp(resp);
	}

	protected DpaaAlertEvent parseInParams(String inParams) throws Exception {
		try {
			return getObjectMapper().readValue(inParams, DpaaAlertEvent.class);
		} catch (Exception e) {
			throw new Exception("參數錯誤, 無法轉型為 DpaaAlertEvent 物件");
		}
	}

	protected void checkDpaaAlertEvent(DpaaAlertEvent dpaaAlertEvent) throws Exception {
		TsmpAlert tsmpAlert = dpaaAlertEvent.getEntity();
		throwExceptionWhenNullOrEmpty(tsmpAlert, "TsmpAlert");
		throwExceptionWhenNullOrEmpty(tsmpAlert.getAlertId(), "alertId");
		throwExceptionWhenNullOrEmpty(tsmpAlert.getAlertMsg(), "alertMsg");
	}

	protected String getLineAccessToken(DpaaAlertEvent dpaaAlertEvent) throws Exception {
		TsmpAlert entity = dpaaAlertEvent.getEntity();
		Long alertId = entity.getAlertId();
		Optional<TsmpAlert> opt = getTsmpAlertDao().findById(alertId);
		if (!opt.isPresent()) {
			throw new Exception("不存在的告警項目: 告警編號=" + alertId);
		}

		String lineToken = opt.get().getImId();
		if (!StringUtils.hasLength(lineToken)) {
			throw new Exception("未設定 Line Access Token, 請檢查告警設定");
		}
		return lineToken;
	}

	protected HttpRespData sendLineMessage(String lineAccessToken, DpaaAlertEvent dpaaAlertEvent) throws Exception {
		String reqUrl = "https://notify-api.line.me/api/notify";
		HttpMethod method = HttpMethod.POST;
		Map<String, String> formData = getFormData(dpaaAlertEvent);
		Map<String, String> httpHeader = getHttpHeader(lineAccessToken);
		boolean isEnableInputStream = false;
		try {
			return submitForm(reqUrl, method, formData, httpHeader, isEnableInputStream);
		} catch (IOException e) {
			throw new Exception("傳送至 Line Notify 失敗", e);
		}
	}

	protected Map<String, String> getHttpHeader(String lineAccessToken) {
		Map<String, String> httpHeader = new HashMap<>();
		String contentType = new MediaType(MediaType.MULTIPART_FORM_DATA, StandardCharsets.UTF_8).toString();
		httpHeader.put(HttpHeaders.CONTENT_TYPE, contentType);
		String authorization = "Bearer " + lineAccessToken;
		httpHeader.put(HttpHeaders.AUTHORIZATION, authorization);
		return httpHeader;
	}

	protected Map<String, String> getFormData(DpaaAlertEvent dpaaAlertEvent) {
		Map<String, String> formData = new HashMap<>();
		String message = getMessageContent(dpaaAlertEvent);
		formData.put("message", message);
		return formData;
	}

	protected String getMessageContent(DpaaAlertEvent dpaaAlertEvent) {
		String alertMessage = dpaaAlertEvent.getEntity().getAlertMsg();
		String sysType = getTsmpSettingService().getVal_TSMP_SYS_TYPE();
		String id = "";
		try {
			id = InetAddress.getLocalHost().toString();
		} catch (Exception e) {
			this.logger.warn("Failed to get node ip." + StackTraceUtil.logStackTrace(e));
		}
		alertMessage += "\r\n System Type:" + sysType;
		alertMessage += "\r\n Node Id:" + id;
		return alertMessage;
	}

	protected HttpRespData submitForm(String reqUrl, HttpMethod method, Map<String, String> formData, //
			Map<String, String> httpHeader, boolean isEnableInputStream) throws IOException {
		/* Library有問題
		return HttpUtil.httpReqByFormData(reqUrl, method.name(), formData, httpHeader, isEnableInputStream);
		*/
		
		HttpRespData respData = new HttpRespData();

		respData.logger("--【URL】--");
		respData.logger("\t" + reqUrl);
		
		HttpHeaders headers = new HttpHeaders();
		respData.logger("--【Http Req Header】--");
		if (!CollectionUtils.isEmpty(httpHeader)) {
			for (Entry<String, String> entry : httpHeader.entrySet()) {
				headers.add(entry.getKey(), entry.getValue());
				respData.logger("\tKey: " + entry.getKey() + ", Value: " + entry.getValue());
			}
		}
		
		respData.logger("--【Req payload / Form Data】--");
		MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
		if (!CollectionUtils.isEmpty(formData)) {
			for (Entry<String, String> entry : formData.entrySet()) {
				body.add(entry.getKey(), entry.getValue());
				respData.logger("\tKey: " + entry.getKey() + ", Value: " + entry.getValue());
			}
		}
		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String,String>>(body, headers);
		RestTemplate template = new RestTemplate();
		ResponseEntity<String> resp = template.exchange(reqUrl, method, requestEntity, String.class);
		
		respData.logger("--【Http status code】--");
		respData.logger("\t" + resp.getStatusCodeValue());
		
		respData.logger("--【Http Resp Header】--");
		HttpHeaders respHeaders = resp.getHeaders();
		for (Entry<String, List<String>> entry : respHeaders.entrySet()) {
			respData.logger("\tKey: " + entry.getKey() + ", Value: " + entry.getValue());
		}
		
		respData.logger("--【Resp payload....Return....】--");
		
		// --------- 開始塞資料 ---------
		respData.statusCode = resp.getStatusCodeValue();
		respData.respStr = "\t" + resp.getBody() + "\n";	// 為了排版才這樣, 反正之後也不會用到 respStr
		respData.respHeader = resp.getHeaders();
		return respData;
	}

	protected String checkResp(HttpRespData resp) throws Exception {
		this.logger.debug(resp.getLogStr());
		HttpStatus statusCode = HttpStatus.resolve(resp.statusCode);
		if (statusCode == null || !statusCode.is2xxSuccessful()) {
			throw new Exception(String.format("HTTP status code: [%s], msg: [%s]", resp.statusCode, resp.respStr));
		}
		return "SUCCESS";
	}

	private void throwExceptionWhenNullOrEmpty(Object input, String fieldName) throws Exception {
		if (ObjectUtils.isEmpty(input)) {
			throw new Exception("缺少必要參數: " + fieldName);
		}
	}

	protected TsmpAlertDao getTsmpAlertDao() {
		return this.tsmpAlertDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}

	protected ObjectMapper getObjectMapper() {
		return this.objectMapper;
	}

}