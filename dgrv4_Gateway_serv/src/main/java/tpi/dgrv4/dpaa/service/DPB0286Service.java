package tpi.dgrv4.dpaa.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import tpi.dgrv4.codec.utils.ProbabilityAlgUtils;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.dpaa.constant.TsmpDpMailType;
import tpi.dgrv4.dpaa.vo.DPB0286Req;
import tpi.dgrv4.dpaa.vo.DPB0286Resp;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.entity.DgrWebhookNotify;
import tpi.dgrv4.entity.entity.DgrWebhookNotifyField;
import tpi.dgrv4.entity.entity.DgrWebhookNotifyLog;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyDao;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyFieldDao;
import tpi.dgrv4.entity.repository.DgrWebhookNotifyLogDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.constant.WebhookFieldEnum;
import tpi.dgrv4.gateway.constant.WebhookType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.httpu.utils.HttpUtil;
import tpi.dgrv4.httpu.utils.HttpUtil.HttpRespData;

@Service
public class DPB0286Service {

	private TPILogger logger = TPILogger.tl;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookNotifyDao dgrWebhookNotifyDao;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookNotifyFieldDao dgrWebhookNotifyFieldDao;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private DgrWebhookNotifyLogDao dgrWebhookNotifyLogDao;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private PrepareMailService prepareMailService;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private TsmpSettingService tsmpSettingService;
	
	@Setter(onMethod_ = @Autowired)
	@Getter(AccessLevel.PROTECTED)
	private MailHelper mailHelper;

	public DPB0286Resp resendNotification(TsmpAuthorization auth, DPB0286Req req) {
		DPB0286Resp resp = new DPB0286Resp();
		try {
			checkParam(req);
			Long id = Long.parseLong(req.getWebhookNotifyLogId());
			
			Optional<DgrWebhookNotifyLog> oNotifyLog = getDgrWebhookNotifyLogDao().findById(id);
			if(oNotifyLog.isPresent()) {
				DgrWebhookNotifyLog notifyLog = oNotifyLog.get();
				Optional<DgrWebhookNotify> oNotify= getDgrWebhookNotifyDao().findById(notifyLog.getWebhookNotifyId());
				if(oNotify.isPresent()) {
					List<DgrWebhookNotifyField> usedFields = null;
					if(WebhookType.HTTP.equals(oNotify.get().getNotifyType().toUpperCase())) {
						//Confirm which URL and field to use.
						LinkedList<String[]> dataList = new LinkedList<String[]>();
						for(DgrWebhookNotifyField field: oNotify.get().getFieldList()) {
							if(WebhookFieldEnum.PERCENT.getCode().equals(field.getFieldKey())) {
								dataList.add(new String[] {field.getFieldValue(), field.getMappingUrl()});
							}
						}
						// select used URL
						String targetURL = ProbabilityAlgUtils.getProbabilityAns(dataList);
						System.out.println(targetURL);
						// filter field
						usedFields = oNotify.get().getFieldList().stream().filter(field -> field.getMappingUrl().equals(targetURL)).collect(Collectors.toList());
					} else {
						usedFields = oNotify.get().getFieldList();
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
					
					DgrWebhookNotifyLog log;				
					switch(oNotify.get().getNotifyType().toUpperCase()) {
						case "LINE":
							log = processLine(notifyLog.getContent(), oNotify.get(), fieldMap, headerMap);
							break;
						case "EMAIL":
							log = processEmail(notifyLog.getContent(), oNotify.get(), fieldMap, notifyLog.getClientId());
							break;
						case "SLACK":
							log = processSlack(notifyLog.getContent(), oNotify.get(), fieldMap);
							break;
						case "DISCORD":
							log = processDiscord(notifyLog.getContent(), oNotify.get(), fieldMap);
							break;
						default:
							log = processDefault(notifyLog.getContent(), oNotify.get(), fieldMap, headerMap);
							break;
					}
					log.setClientId(notifyLog.getClientId());
					log.setWebhookNotifyId(oNotify.get().getWebhookNotifyId());
					getDgrWebhookNotifyLogDao().save(log);
				}			
			} else {
				throw TsmpDpAaRtnCode._1298.throwing();
			}				
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}
	
	private void checkParam(DPB0286Req req) {
		if(!StringUtils.hasText(req.getWebhookNotifyLogId())) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}
	
	private DgrWebhookNotifyLog processLine(String content, DgrWebhookNotify wh, Map<String, String> fieldMap, Map<String, String> headerMap) {		
		DgrWebhookNotifyLog log = new DgrWebhookNotifyLog();
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
			// to 有值使用單一UserID or GroupID 發送
			// If 'to' has a value, send to a specific UserID or GroupID; otherwise, send as a broadcast
			if(StringUtils.hasText(to)) {
				reqUrl = "https://api.line.me/v2/bot/message/push";
			}
			
	        //merge req rawData and custom fields to new rawData
	        ObjectMapper om = new ObjectMapper();	        			
	        log.setContent(content);
			resp = HttpUtil.httpReqByRawData(reqUrl, "POST", content, httpHeader, false);
			
			// if HttpCode is not HttpStatus.OK then set message to remark			
			if(HttpStatus.OK.value() != resp.statusCode) {
				JsonNode jsonNode = om.readTree(resp.respStr);				
	            String errorMsg = jsonNode.get("message").asText();
	            log.setRemark(errorMsg);				
			}
		} catch (TsmpDpAaException e) {
			log.setRemark(e.getMessage());
		} catch (Exception e) {
			log.setRemark(e.getMessage());
		}
		return log;
	}
	
	private DgrWebhookNotifyLog processEmail(String content, DgrWebhookNotify wh, Map<String, String> fieldMap,
			String clientId) {
		DgrWebhookNotifyLog log = new DgrWebhookNotifyLog();
		try {
			//check must parameter
			String subject = fieldMap.get(WebhookFieldEnum.SUBJECT.getCode());
			String recipients = fieldMap.get(WebhookFieldEnum.RECIPIENTS.getCode());
			if(!StringUtils.hasText(subject) || !StringUtils.hasText(recipients)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			log.setContent(content);						
			
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
			log.setRemark(e.getMessage());
		} catch (Exception e) {
			log.setRemark(e.getMessage());
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}		
		return log;
	}
	
	private DgrWebhookNotifyLog processSlack(String content, DgrWebhookNotify wh, Map<String, String> fieldMap) {
		DgrWebhookNotifyLog log = new DgrWebhookNotifyLog();
		try {								
			//check must parameter
			String reqUrl = fieldMap.get(WebhookFieldEnum.URL.getCode());
			if(!StringUtils.hasText(reqUrl)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			// ready http request header
			Map<String, String> httpHeader = new HashMap<>();
			
			log.setContent(content);
			HttpRespData resp = HttpUtil.httpReqByRawData(reqUrl, "POST", content, httpHeader, false);
			
			// if HttpCode is not HttpStatus.OK then set message to remark
			if(HttpStatus.OK.value() != resp.statusCode) {
				ObjectMapper om = new ObjectMapper();
				JsonNode jsonNode = om.readTree(resp.respStr);				
	            String message = jsonNode.get("message").asText();
	            log.setRemark(message);				
			}
		} catch (TsmpDpAaException e) {
			log.setRemark(e.getMessage());
		} catch (Exception e) {
			log.setRemark(e.getMessage());
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}		
		return log;
	}
	
	private DgrWebhookNotifyLog processDiscord(String content, DgrWebhookNotify wh, Map<String, String> fieldMap) {
		DgrWebhookNotifyLog log = new DgrWebhookNotifyLog();
		try {								
			//check must parameter
			String reqUrl = fieldMap.get(WebhookFieldEnum.URL.getCode());
			if(!StringUtils.hasText(reqUrl)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			// ready http request header
			Map<String, String> httpHeader = new HashMap<>();
			httpHeader.put("Content-Type", "application/json");
			log.setContent(content);
			HttpRespData resp = HttpUtil.httpReqByRawData(reqUrl, "POST", content, httpHeader, false);
			
			// if HttpCode is not HttpStatus.OK then set message to remark
			if(HttpStatus.OK.value() != resp.statusCode && HttpStatus.NO_CONTENT.value() != resp.statusCode) {
				ObjectMapper om = new ObjectMapper();
				JsonNode jsonNode = om.readTree(resp.respStr);				
	            String message = jsonNode.get("_misc").asText();
	            log.setRemark(message);				
			}
		} catch (TsmpDpAaException e) {
			log.setRemark(e.getMessage());
		} catch (Exception e) {
			log.setRemark(e.getMessage());
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}		
		return log;
	}
	
	private DgrWebhookNotifyLog processDefault(String content, DgrWebhookNotify wh, Map<String, String> fieldMap, Map<String, String> headerMap) {
		DgrWebhookNotifyLog log = new DgrWebhookNotifyLog();
		try {
			//check must parameter
			String reqUrl = fieldMap.get("url");			
			if(!StringUtils.hasText(reqUrl)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}			
			
			// ready http request header
			Map<String, String> httpHeader = new HashMap<>();
			httpHeader.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);
			HttpRespData resp;
			
			headerMap.forEach((key, value) -> {
				httpHeader.put(key, value);
			});
			
			// 0:form-data 1:raw 2:x-www-form-urlencoded;
			if("1".equals(wh.getPayloadFlag())) {				
		        log.setContent(content);
				resp = HttpUtil.httpReqByRawData(reqUrl, "POST", content, httpHeader, false);
			} else if("2".equals(wh.getPayloadFlag())) {				
				log.setContent(content);
				resp = HttpUtil.httpReqByX_www_form_urlencoded_UTF8(reqUrl, "POST", fieldMap, httpHeader, false);
			} else {				
				log.setContent(content);
				resp = HttpUtil.httpReqByFormData(reqUrl, "POST", fieldMap, httpHeader, false);
			}
			
			// if HttpCode is not HttpStatus.OK then set message to remark
			if(HttpStatus.OK.value() != resp.statusCode) {				
	            log.setRemark(resp.respStr);				
			}
		} catch (TsmpDpAaException e) {
			log.setRemark(e.getMessage());
		} catch (Exception e) {
			log.setRemark(e.getMessage());
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}
		return log;
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
}
