package tpi.dgrv4.gateway.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import tpi.dgrv4.codec.utils.ProbabilityAlgUtils;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.dpaa.constant.TsmpDpMailType;
import tpi.dgrv4.dpaa.service.PrepareMailService;
import tpi.dgrv4.dpaa.service.TsmpSettingService;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.entity.entity.DgrWebhookNotify;
import tpi.dgrv4.entity.entity.DgrWebhookNotifyField;
import tpi.dgrv4.entity.entity.DgrWebhookNotifyLog;
import tpi.dgrv4.entity.repository.DgrWebhookApiMapDao;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyDao;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyFieldDao;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyLogDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.constant.DgrHeader;
import tpi.dgrv4.gateway.constant.WebhookFieldEnum;
import tpi.dgrv4.gateway.constant.WebhookType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Service
public class WebhookService {

	private TPILogger logger = TPILogger.tl;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookNotifyDao dgrWebhookNotifyDao;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookNotifyFieldDao dgrWebhookNotifyFieldDao;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookApiMapDao dgrWebhookApiMapDao;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookNotifyLogDao dgrWebhookNotifyLogDao;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
    private TsmpTAEASKHelper tsmpTAEASKHelper;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private PrepareMailService prepareMailService;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private TsmpSettingService tsmpSettingService;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private MailHelper mailHelper;

	private static final List<String> ignoreField = Arrays.asList("url", "percent");

	public ResponseEntity<?> handleNotifications(HttpServletRequest httpReq, HttpServletResponse httpResp, HttpHeaders headers) {
		String encClientId = headers.getFirst(DgrHeader.CLIENT_ID);
		String notifyIds = headers.getFirst(DgrHeader.NOTIFY);
		
		if(!StringUtils.hasText(encClientId)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		String clientId = getTsmpTAEASKHelper().decrypt(encClientId);
		if(StringUtils.hasText(notifyIds)) {
			for(String notifyId : notifyIds.split(";")) {
				if(StringUtils.hasText(notifyId)) {
					Long id = Long.parseLong(notifyId);
					DgrWebhookNotify wh = getDgrWebhookNotifyDao().findById(id).orElse(null);
					
					//null or not Enable then continue;					
					if(wh == null || "N".equals(wh.getEnable()))continue;
										
					List<DgrWebhookNotifyField> usedFields = null;
					if(WebhookType.HTTP.equals(wh.getNotifyType().toUpperCase())) {
						//Confirm which URL and field to use.
						LinkedList<String[]> dataList = new LinkedList<String[]>();
						for(DgrWebhookNotifyField field: wh.getFieldList()) {
							if(WebhookFieldEnum.PERCENT.getCode().equals(field.getFieldKey())) {
								dataList.add(new String[] {field.getFieldValue(), field.getMappingUrl()});
							}
						}
						// select used URL
						String targetURL = ProbabilityAlgUtils.getProbabilityAns(dataList);
						System.out.println(targetURL);
						// filter field
						usedFields = wh.getFieldList().stream().filter(field -> field.getMappingUrl().equals(targetURL)).collect(Collectors.toList());
					} else {
						usedFields = wh.getFieldList();
					}
					
					Map<String, String> fieldMap = new HashMap<>();
					Map<String, String> headerMap = new HashMap<>();
					Optional.ofNullable(usedFields).ifPresent(lst -> {
					    for (DgrWebhookNotifyField f : lst) {
					    	if("1".equals(f.getFieldType())) {
					    		headerMap.put(f.getFieldKey(), f.getFieldValue());
					    	} else {
					    		fieldMap.put(f.getFieldKey(), f.getFieldValue());
					    	}					    					    	
					    }
					});
					DgrWebhookNotifyLog log = new DgrWebhookNotifyLog();
					log.setClientId(clientId);
					log.setWebhookNotifyId(id);
					switch(wh.getNotifyType().toUpperCase()) {
						case WebhookType.LINE:
							processLine(httpReq, wh, fieldMap, headerMap, log);
							break;
						case WebhookType.EMAIL:
							processEmail(httpReq, wh, fieldMap, clientId, log);
							break;
						case WebhookType.SLACK:
							processSlack(httpReq, wh, fieldMap, log);
							break;
						case WebhookType.DISCORD:
							processDiscord(httpReq, wh, fieldMap, log);
							break;
						default:
							HttpRespData resp = processDefault(httpReq, httpResp, wh, fieldMap, headerMap, log);
							
							if(notifyIds.split(";").length == 1 && resp!=null) {
								try {
									// 將回覆header取出來放在httpRes header
									resp.respHeader.forEach((key, valList) -> valList.forEach((val) -> {
										if (key != null) {
											httpResp.addHeader(key, val);
										}
									}));
									//checkmarx, Missing HSTS Header
									httpResp.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");

									boolean isStreaming = MediaType.TEXT_EVENT_STREAM_VALUE.equalsIgnoreCase(httpResp.getContentType()) && resp.statusCode < 400;
									resp.fetchByte(new HashMap<String, String>(), isStreaming); // because Enable inputStream																		
                                    httpResp.setStatus(resp.statusCode);
									// content output
									if(!isStreaming) {
										ByteArrayInputStream bi = new ByteArrayInputStream(resp.httpRespArray);
										IOUtils.copy(bi, httpResp.getOutputStream());
									}									
								} catch(Exception e) {
									this.logger.error(StackTraceUtil.logStackTrace(e));
								}
							}							
							break;
					}
					getDgrWebhookNotifyLogDao().save(log);
				}
			}
		}
		return null;
	}
	
	private void processLine(HttpServletRequest httpReq, DgrWebhookNotify wh, Map<String, String> fieldMap, Map<String, String> headerMap, DgrWebhookNotifyLog log) {		
		try {
			//check must parameter				
			String authorization = headerMap.get(WebhookFieldEnum.AUTHORIZATION.getCode());
			if(!StringUtils.hasText(authorization)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			//預設廣播模式
			// Default to broadcast mode
			String reqUrl = "https://api.line.me/v2/bot/message/broadcast";
			
			// ready http request header
			Map<String, String> httpHeader = new HashMap<>();
			httpHeader.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
			httpHeader.put("Authorization", authorization);
			HttpRespData resp;
			
			String to = fieldMap.get(WebhookFieldEnum.TO.getCode());
			String notificationDisabled = fieldMap.get(WebhookFieldEnum.NOTIFICATION_DISABLED.getCode());

			Map<String, Object> payload = new LinkedHashMap<>();
			// to 有值使用單一UserID or GroupID 發送
			// If 'to' has a value, send to a specific UserID or GroupID; otherwise, send as a broadcast
			if(StringUtils.hasText(to)) {
				reqUrl = "https://api.line.me/v2/bot/message/push";
				payload.put(WebhookFieldEnum.TO.getCode(), to);		        
		        payload.put(WebhookFieldEnum.NOTIFICATION_DISABLED.getCode(), "Y".equals(notificationDisabled)?true:false);
			}
			
			// 訊息內容
			// Payload of the message
	        Map<String, String> message = Map.of(
	            "type", "text",
	            "text", wh.getMessage()
	        );		        
	        List<Map<String, String>> messageMapList = List.of(message);
	        payload.put("messages", messageMapList);
			
	        //merge req rawData and custom fields to new rawData
	        ObjectMapper om = new ObjectMapper();
	        String rawData = om.writeValueAsString(payload);				
	        log.setContent(calculateMsgAndMask(rawData.toString()));
			resp = HttpUtil.httpReqByRawData(reqUrl, "POST", rawData.toString(), httpHeader, false);			
			
			// if HttpCode is not HttpStatus.OK then set message to remark
			if(HttpStatus.OK.value() != resp.statusCode) {
				JsonNode jsonNode = om.readTree(resp.respStr);				
	            String errorMsg = jsonNode.get("message").asText();
	            log.setRemark(calculateMsgAndMask(errorMsg));
			}
		} catch (TsmpDpAaException e) {
			log.setRemark(calculateMsgAndMask(e.getMessage()));
		} catch (Exception e) {
            log.setRemark(calculateMsgAndMask(e.getMessage()));
		}
		getDgrWebhookNotifyLogDao().save(log);
	}
	
	private void processEmail(HttpServletRequest httpReq, DgrWebhookNotify wh, Map<String, String> fieldMap,
			String clientId, DgrWebhookNotifyLog log) {
		try {
			//check must parameter
			String subject = fieldMap.get(WebhookFieldEnum.SUBJECT.getCode());
			String recipients = fieldMap.get(WebhookFieldEnum.RECIPIENTS.getCode());
			if(!StringUtils.hasText(subject) || !StringUtils.hasText(recipients)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}	
			
			String content = wh.getMessage();
			log.setContent(calculateMsgAndMask(wh.getMessage()));
			
			String identif = String.format(//
					"clientId=%s" //
					+ ",　mailType=%s" //
					, //
					clientId, //
					"webhook" //
			);
			
			TsmpMailEvent event = getMail(subject, recipients, content);			
			getPrepareMailService().createMailSchedule(Arrays.asList(event), identif, TsmpDpMailType.SAME.text(), "0");
		} catch (TsmpDpAaException e) {
            log.setRemark(calculateMsgAndMask(e.getMessage()));
		} catch (Exception e) {
            log.setRemark(calculateMsgAndMask(e.getMessage()));
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}		
		getDgrWebhookNotifyLogDao().save(log);
	}
	
	private void processSlack(HttpServletRequest httpReq, DgrWebhookNotify wh, Map<String, String> fieldMap, DgrWebhookNotifyLog log) {
		try {								
			//check must parameter
			String reqUrl = fieldMap.get(WebhookFieldEnum.URL.getCode());
			if(!StringUtils.hasText(reqUrl)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			// ready http request header
			Map<String, String> httpHeader = new HashMap<>();
			HttpRespData resp;
			
			//set text							
			fieldMap.put("text", wh.getMessage());					
			ObjectMapper objectMapper = new ObjectMapper();
	        String rawData = objectMapper.writeValueAsString(fieldMap);
			log.setContent(calculateMsgAndMask(rawData));
			resp = HttpUtil.httpReqByRawData(reqUrl, "POST", rawData, httpHeader, false);				
			
			// if HttpCode is not HttpStatus.OK then set message to remark
			if(HttpStatus.OK.value() != resp.statusCode) {
				ObjectMapper om = new ObjectMapper();
				JsonNode jsonNode = om.readTree(resp.respStr);				
	            String message = jsonNode.get("message").asText();
	            log.setRemark(calculateMsgAndMask(message));
			}
		} catch (TsmpDpAaException e) {
            log.setRemark(calculateMsgAndMask(e.getMessage()));
		} catch (Exception e) {
            log.setRemark(calculateMsgAndMask(e.getMessage()));
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}		
		getDgrWebhookNotifyLogDao().save(log);
	}
	
	private void processDiscord(HttpServletRequest httpReq, DgrWebhookNotify wh, Map<String, String> fieldMap, DgrWebhookNotifyLog log) {
		try {								
			//check must parameter
			String reqUrl = fieldMap.get(WebhookFieldEnum.URL.getCode());
			if(!StringUtils.hasText(reqUrl)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			// ready http request header
			Map<String, String> httpHeader = new HashMap<>();
			httpHeader.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
			HttpRespData resp;
			
			//set content
			fieldMap.put("content", wh.getMessage());		
			
			ObjectMapper objectMapper = new ObjectMapper();
	        String rawData = objectMapper.writeValueAsString(fieldMap);
			log.setContent(rawData);
			resp = HttpUtil.httpReqByRawData(reqUrl, "POST", rawData, httpHeader, false);
			
			// if HttpCode is not HttpStatus.OK then set message to remark
			if(HttpStatus.OK.value() != resp.statusCode && HttpStatus.NO_CONTENT.value() != resp.statusCode) {
				ObjectMapper om = new ObjectMapper();
				JsonNode jsonNode = om.readTree(resp.respStr);				
	            String message = jsonNode.get("_misc").asText();
	            log.setRemark(calculateMsgAndMask(message));
			}
		} catch (TsmpDpAaException e) {
            log.setRemark(calculateMsgAndMask(e.getMessage()));
		} catch (Exception e) {
            log.setRemark(calculateMsgAndMask(e.getMessage()));
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}		
		getDgrWebhookNotifyLogDao().save(log);
	}
	
	private HttpRespData processDefault(HttpServletRequest httpReq, HttpServletResponse httpResp, DgrWebhookNotify wh, Map<String, String> fieldMap, Map<String, String> headerMap, DgrWebhookNotifyLog log) {
		HttpRespData resp = null;
		try {			
			//check must parameter
			String reqUrl = fieldMap.get(WebhookFieldEnum.URL.getCode());			
			if(!StringUtils.hasText(reqUrl)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			// ready http request header
			Map<String, String> httpHeader = new HashMap<>();
			httpHeader.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
			
			//set original header
			Enumeration<String> headerNames = httpReq.getHeaderNames();
	        if (headerNames != null) {
	            while (headerNames.hasMoreElements()) {
	                String headerName = headerNames.nextElement();
	                String headerValue = httpReq.getHeader(headerName);
	                httpHeader.put(headerName, headerValue);
	            }
	        }
			
			headerMap.forEach((key, value) -> {
				httpHeader.put(key, value);
			});						
			
			// 0:form-data 1:raw 2:x-www-form-urlencoded;
			if("1".equals(wh.getPayloadFlag())) {
				StringBuilder rawData = new StringBuilder();
		        String line;
		        try (BufferedReader reader = httpReq.getReader()) {
		            while ((line = reader.readLine()) != null) {
		                rawData.append(line);
		            }
		        }
		        
		        //merge req rawData and custom fields to new rawData
		        ObjectMapper om = new ObjectMapper();
		        String rawDataStr = (rawData == null || rawData.toString().trim().isEmpty()) ? "{}" : rawData.toString();
		        Map<String, Object> rawDataMap = om.readValue(rawDataStr, Map.class);
		        rawDataMap.putAll(fieldMap);
				ignoreField.forEach(rawDataMap::remove);
		        String reqRawData = om.writeValueAsString(rawDataMap);		        
		        log.setContent(calculateMsgAndMask(reqRawData.toString()));
				resp = HttpUtil.httpReqByRawDataList(reqUrl, "POST", reqRawData.toString(), tranferOne2List(httpHeader), true, false, httpResp.getOutputStream());
			} else {
				log.setContent(calculateMsgAndMask(wh.getMessage()));
				// parse request form-data
				Map<String, String[]> parameterMap = httpReq.getParameterMap();
				for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
					fieldMap.put(entry.getKey(), entry.getValue()[0]); // only get first
			    }
				
				// if PayloadFlag = 2 then x-www-form-urlencoded else form-data
				if("2".equals(wh.getPayloadFlag())) {
					resp = HttpUtil.httpReqByX_www_form_urlencoded_UTF8(reqUrl, "POST", fieldMap, httpHeader, true);
				} else {
					resp = HttpUtil.httpReqByFormData(reqUrl, "POST", fieldMap, httpHeader, true);
				}
			}			
			// if HttpCode is not HttpStatus.OK then set message to remark
			if(HttpStatus.OK.value() != resp.statusCode) {
				log.setRemark(calculateMsgAndMask(resp.respStr));
			}
		} catch (TsmpDpAaException e) {
            log.setRemark(calculateMsgAndMask(e.getMessage()));
		} catch (Exception e) {
            log.setRemark(calculateMsgAndMask(e.getMessage()));
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}
		getDgrWebhookNotifyLogDao().save(log);
		return resp;
	}
	
	private Map<String, List<String>> tranferOne2List(Map<String, String> map) {
		Map<String, List<String>> newMap = new HashMap<>();
		if (!CollectionUtils.isEmpty(map)) {
			map.forEach((k, v) -> {
				newMap.put(k, Arrays.asList(v));
			});
		}
		return newMap;
	}
	
	/**
	 * 寄信給 client 的內容
	 */
	private TsmpMailEvent getMail(String subject, String recipients, String message) throws Exception {
		String bodyTpltCode = "body.webhook";				
		// 內文		
		Map<String, Object> bodyParams = new HashMap<>();
		bodyParams.put("message", message);		

		String content = getMailHelper().buildNestedContent(bodyTpltCode, bodyParams);

		return new TsmpMailEventBuilder() //
				.setSubject(subject).setContent(content).setRecipients(recipients).setCreateUser("SYS")
				.setRefCode("body.webhook").build();
	}
	
	/**
	 * 取得多久後寄發Email時間
	 */
	protected String getSendTime() {
		String sendTime = this.getTsmpSettingService().getVal_MAIL_SEND_TIME();// 多久後寄發Email(ms)
		return sendTime;
	}

    private String calculateMsgAndMask(String msg){
        if (!StringUtils.hasLength(msg)) {
            return "";
        }
        int keepLength = 998;
        int totalLength = msg.length();
        if (totalLength<=2000)
            return msg;
        else{
            String firstPart = msg.substring(0, keepLength);
            String lastPart = msg.substring(totalLength - keepLength);
            return firstPart + "..." + lastPart;
        }
    }
}
