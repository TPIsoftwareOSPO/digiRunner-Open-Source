package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpModule;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.dpaa.component.job.AA0231Job;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredMailJob;
import tpi.dgrv4.dpaa.util.OAuthUtil;
import tpi.dgrv4.dpaa.util.RandomUtils;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0231Req;
import tpi.dgrv4.dpaa.vo.AA0231Resp;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.component.cipher.TsmpTAEASKHelper;
import tpi.dgrv4.entity.entity.OauthClientDetails;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailTplt;
import tpi.dgrv4.entity.repository.OauthClientDetailsDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpMailTpltDao;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0231Service {

	private TPILogger logger = TPILogger.tl;
	private String sendTime;
	
	private TsmpClientDao tsmpClientDao;
	private OauthClientDetailsDao oauthClientDetailsDao;
	private ApplicationContext ctx;
	private JobHelper jobHelper;
	private TsmpDpMailTpltDao tsmpDpMailTpltDao;
	private ServiceConfig serviceConfig;
	private DgrAuditLogService dgrAuditLogService;
	private TsmpSettingDao tsmpSettingDao;
	private TsmpTAEASKHelper tsmpTAEASKHelper;
    private DaoGenericCacheService daoGenericCacheService;
    @Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private TsmpSettingService tsmpSettingService;
    
    @Autowired
	public AA0231Service(TsmpClientDao tsmpClientDao, OauthClientDetailsDao oauthClientDetailsDao,
			ApplicationContext ctx, JobHelper jobHelper, TsmpDpMailTpltDao tsmpDpMailTpltDao,
			ServiceConfig serviceConfig, DgrAuditLogService dgrAuditLogService, TsmpSettingDao tsmpSettingDao,
			TsmpTAEASKHelper tsmpTAEASKHelper, DaoGenericCacheService daoGenericCacheService) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.ctx = ctx;
		this.jobHelper = jobHelper;
		this.tsmpDpMailTpltDao = tsmpDpMailTpltDao;
		this.serviceConfig = serviceConfig;
		this.dgrAuditLogService = dgrAuditLogService;
		this.tsmpSettingDao = tsmpSettingDao;
		this.tsmpTAEASKHelper = tsmpTAEASKHelper;
		this.daoGenericCacheService = daoGenericCacheService;
	}

	@PostConstruct
	public void init() {
		this.sendTime = "0"; //設為0,不等待,因重置密碼,必須馬上寄發Email
	}
	
	@Transactional
	public AA0231Resp updatePasswordSettingByClient(TsmpAuthorization authorization, AA0231Req req, InnerInvokeParam iip) {
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		String iipTxnUid_1 = iip.getTxnUid() + "_1";
		String iipTxnUid_2 = iip.getTxnUid() + "_2";
		iip.setTxnUid(iipTxnUid_1);
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_CLIENT.value());
		
		AA0231Resp resp = new AA0231Resp();

		try {
			String clientID = req.getClientID();
			String clientBlock = req.getClientBlock();
			String newClientBlock = req.getNewClientBlock();
			String confirmNewClientBlock = req.getConfirmNewClientBlock();
			String resetBlock = req.getResetBlock();
			String adminConsolePwd = "";

			//2.1查詢TSMP_CLIENT資料表，條件CLIENT_ID = AA0231Req.clientID，若查無資料則throw RTN Code  1344。
			TsmpClient tsmpClientVo = getTsmpClientDao().findById(clientID).orElse(null);
			if(tsmpClientVo == null) {
				//1344:用戶端不存在
				throw TsmpDpAaRtnCode._1344.throwing();
			}
			
			//1.若AA0231Req.resetBlock=true，則進行步驟2系列，反之進行步驟3系列。
			if("true".equalsIgnoreCase(resetBlock)) {
				//2.2若步驟2.1查詢到的TSMP_CLIENT.EMAIL(kim.huang@thinkpower-info.com,carl.jiang@thinkpower-info.com)有值則進行步驟2.3。
				if(!StringUtils.isEmpty(tsmpClientVo.getEmails())) {
					//2.3 亂數產生10位數的新密碼，再用Base64進行編碼。(Base64.getEncoder().encodeToString(新密碼.getBytes()))。
					String pwd = this.getRandom(10);
					adminConsolePwd = pwd;
					String base64Pwd = ServiceUtil.base64Encode(pwd.getBytes());
					
					//2.4 更新oauth_client_details資料表，欄位 oauth_client_details.CLIENT_SECRET = Bcrypt(Base64進行編碼後的新密碼)，條件CLIENT_ID = AA0231Req.clientID。
					String bcryptPwd = OAuthUtil.bCryptEncode(base64Pwd);
					OauthClientDetails oauthVo = getOauthClientDetailsDao().findById(clientID).orElse(null);
					if(oauthVo != null) {
						String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, oauthVo); //舊資料統一轉成 String
						
						oauthVo.setClientSecret(bcryptPwd);
						oauthVo = getOauthClientDetailsDao().save(oauthVo);
						
						//寫入 Audit Log D
						lineNumber = StackTraceUtil.getLineNumber();
						getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
								OauthClientDetails.class.getSimpleName(), TableAct.U.value(), oldRowStr, oauthVo);
						
						/*
						 * 只要有異動到 Client pwd (OAUTH_CLIENT_DETAILS.client_secret) 這個欄位,	
						 * 都要將 Client pwd 經過 SHA512URL base64 hash 的值,更新至 TSMP_CLIENT.client_secret	
						 */
						oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpClientVo); //舊資料統一轉成 String
						
						String sha512Pwd = ServiceUtil.getSHA512ToBase64(pwd);
						tsmpClientVo.setClientSecret(sha512Pwd);
						tsmpClientVo = getTsmpClientDao().save(tsmpClientVo);
						
						//寫入 Audit Log D
						lineNumber = StackTraceUtil.getLineNumber();
						getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
								TsmpClient.class.getSimpleName(), TableAct.U.value(), oldRowStr, tsmpClientVo);
						
						// 更新成功後, 發送Email
						sendEmail(authorization, tsmpClientVo, pwd, base64Pwd);
						
						// 刪除過期的 Mail log
						deleteExpiredMail();
					}
				}
				
			}else {
				//情境1：若下列三個欄位都是空或是NULL，則throw RTN Code 1440
				if(StringUtils.isEmpty(clientBlock) && StringUtils.isEmpty(newClientBlock) && StringUtils.isEmpty(confirmNewClientBlock)) {
					//請填寫密碼或是重置密碼
					throw TsmpDpAaRtnCode._1440.throwing();
				}
				
				//情境2：若以下欄位有符合，則throw RTN Code 1441
				if(!StringUtils.isEmpty(clientBlock) && StringUtils.isEmpty(newClientBlock) && StringUtils.isEmpty(confirmNewClientBlock)) {
					//請填寫密碼
					throw TsmpDpAaRtnCode._1441.throwing();
				}
				
				//情境3：若以下欄位有符合，則throw RTN Code 1441
				if(StringUtils.isEmpty(clientBlock) && StringUtils.isEmpty(newClientBlock) && !StringUtils.isEmpty(confirmNewClientBlock)) {
					//請填寫密碼
					throw TsmpDpAaRtnCode._1441.throwing();
				}
				
				//3.1若AA0231Req.newClientBlock不為空，進行步驟3.2。
				if(!StringUtils.isEmpty(newClientBlock)) {
					//3.2檢查AA0231Req.newClientBlock與AA0231Req.confirmNewClientBlock是否一致，若不一致則 throw RTN Code 1408。
					if(!newClientBlock.equals(confirmNewClientBlock)) {
						//1408:新密碼與再次確認密碼不一致
						throw TsmpDpAaRtnCode._1408.throwing();
					}
					
					//3.3查詢oauth_client_details資料表，條件 client_id = AA0231Req.clientID，若查無資料則throw RTN Code 1344。
					OauthClientDetails oauthVo = getOauthClientDetailsDao().findById(clientID).orElse(null);
					if(oauthVo == null) {
						//1344:用戶端不存在
						throw TsmpDpAaRtnCode._1344.throwing();
					}
					
					//3.4比對新舊密碼是否一致，oauth_client_details.client_secret與AA0231Req.clientBlock比對，若不一致throw RTN Code 1238。
					if(!OAuthUtil.bCryptPasswordCheck(clientBlock, oauthVo.getClientSecret())) {
						//1238:原密碼不正確
						throw TsmpDpAaRtnCode._1238.throwing();
					}
					
					String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, oauthVo); //舊資料統一轉成 String
					//3.5更新oauth_client_details資料表，欄位client_secret = Bcrypt(新密碼)，條件 client_id = AA0231Req.clientID。
					String bcryptPwd = OAuthUtil.bCryptEncode(newClientBlock);
					oauthVo.setClientSecret(bcryptPwd);
					oauthVo = getOauthClientDetailsDao().save(oauthVo);
					
					//寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
							OauthClientDetails.class.getSimpleName(), TableAct.U.value(), oldRowStr, oauthVo);
					
					/*
					 * 只要有異動到 Client pwd (OAUTH_CLIENT_DETAILS.client_secret) 這個欄位,	
					 * 都要將 Client pwd 經過 SHA512URL base64 hash 的值,更新至 TSMP_CLIENT.client_secret	
					 */
					oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpClientVo); //舊資料統一轉成 String
					
					String newClientPwd = new String(ServiceUtil.base64Decode(newClientBlock));//Client PW,經過 Base64 Decode 的值
					if(!newClientPwd.matches(this.getTsmpSettingService().getVal_CLIENT_PWD_STRENGTH())) {
						throw TsmpDpAaRtnCode._1352.throwing("{{newClientBlock}}");
					}
					adminConsolePwd = newClientPwd;
					String sha512Pwd = ServiceUtil.getSHA512ToBase64(newClientPwd);
					tsmpClientVo.setClientSecret(sha512Pwd);
					tsmpClientVo = getTsmpClientDao().save(tsmpClientVo);
					
					//寫入 Audit Log D
					lineNumber = StackTraceUtil.getLineNumber();
					getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
							TsmpClient.class.getSimpleName(), TableAct.U.value(), oldRowStr, tsmpClientVo);
				}
			}
			
			/*
			 * 4.如果 Client ID = YWRtaW5Db25zb2xl, 就必須把密碼更新到 TsmpSetting 這張表內, 
			 * 鍵為 TSMP_AC_CLIENT_PW 的這個列的值上面。
			 * 
			 */
			// 檢查 Client Id
			if ("YWRtaW5Db25zb2xl".equals(clientID)) {

				// 檢查是否有 TSMP_AC_CLIENT_PW
				Optional<TsmpSetting> opt = getTsmpSettingDao().findById("TSMP_AC_CLIENT_PW");
				if (opt.isEmpty()) {
					throw TsmpDpAaRtnCode._1298.throwing();
				}

				// 寫入 Audit Log M
				iip.setTxnUid(iipTxnUid_2);
				lineNumber = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogM(iip, lineNumber,
						AuditLogEvent.UPDATE_TSMP_SETTING.value());

				// 準備修改密碼與寫入 Audit Log D
				TsmpSetting setting = opt.get();
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, setting); // 舊資料統一轉成 String

				// 將明文密碼做 Base64 編碼後以環境變數為依據，
				// 決定是否做 TAEASK 加密後，存入 TsmpSetting 裡面。
				String base64ClientPwd = ServiceUtil.base64Encode(adminConsolePwd.getBytes());
				String taeask = System.getenv("TAEASK");
				String encode;
				if (StringUtils.hasText(taeask)) {
					encode = getTAEASKEncode(base64ClientPwd);
				} else {
					encode = base64ClientPwd;
				}
				setting.setValue(encode);
				getTsmpSettingDao().saveAndFlush(setting);

				// 寫入 Audit Log D
				lineNumber = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogD(iip, lineNumber, //
						TsmpSetting.class.getSimpleName(), TableAct.U.value(), oldRowStr, setting);// U

				getDaoGenericCacheService().clearAndNotify();
			}

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//1286:更新失敗
			throw TsmpDpAaRtnCode._1286.throwing();
		}
		
		return resp;
	}
	
	private String getRandom(int len) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ; i < len ; i++) {
			String number = RandomUtils.randomString(1, false, true);//取得亂數
			if(i == 0 && "0".equals(number)) {
				number = "1";
			}
			sb.append(number);
		}
		return sb.toString();
	}
	
	public AA0231Job sendEmail(TsmpAuthorization auth, TsmpClient tsmpClientVo, String pwd, String base64Pwd) {
		List<TsmpMailEvent> mailEvents = new ArrayList<>();
			TsmpMailEvent mailEvent = getTsmpMailEvent(auth, tsmpClientVo, pwd, base64Pwd);
			if (mailEvent != null) {
				mailEvents.add(mailEvent);
			}
			
		AA0231Job job = getAA0231Job(auth, mailEvents, getSendTime());
		getJobHelper().add(job);
			
		return job;
	}
	
	protected DeleteExpiredMailJob deleteExpiredMail() {
		DeleteExpiredMailJob job = (DeleteExpiredMailJob) getCtx().getBean("deleteExpiredMailJob");
		getJobHelper().add(job);
		return job;
	}
	
	private String getTAEASKEncode(String value) {
        String encoded = null;
        encoded = getTsmpTAEASKHelper().encrypt(value);
        logger.debug("TAEASK encode   " + encoded);
        return encoded;
	}
	
	
	private TsmpMailEvent getTsmpMailEvent(TsmpAuthorization authorization, TsmpClient tsmpClientVo, String pwd, String base64Pwd) {
		//String clientId = authorization.getClientId();
		String clientId = tsmpClientVo.getClientId();
		String recipients = tsmpClientVo.getEmails();
		
		if (recipients == null || recipients.isEmpty()) {
			this.logger.debug(String.format("Client %s has empty emails!", clientId));
			return null;
		}

		String subject = null;
		String body = null;
		subject = getTemplate("subject.updClie-pwd");
		body = getTemplate("body.updClie-pwd");
		
		if (subject == null || body == null) {
			return null;
		}

		Map<String, String> subjectParams = getSubjectParams();
		if (subjectParams == null || subjectParams.isEmpty()) {
			this.logger.debug(String.format("Client %s has empty subject params!", clientId));
			return null;
		}

		Map<String, String> bodyParams = getBodyParams(tsmpClientVo, pwd, base64Pwd);
		if (bodyParams == null || bodyParams.isEmpty()) {
			this.logger.debug(String.format("Client %s has empty body params!", clientId));
			return null;
		}

		final String title = MailHelper.buildContent(subject, subjectParams);
		final String content = MailHelper.buildContent(body, bodyParams);
		//this.logger.debug("Email title = " + title);
		//this.logger.debug("Email content = " + content);
		
		return new TsmpMailEventBuilder() //
		.setSubject(title)
		.setContent(content)
		.setRecipients(recipients)
		.setCreateUser(authorization.getUserName())
		.setRefCode("body.updClie-pwd")
		.build();
	}

	private String getTemplate(String code) {
		List<TsmpDpMailTplt> list = getTsmpDpMailTpltDao().findByCode(code);
		if (list != null && !list.isEmpty()) {
			return list.get(0).getTemplateTxt();
		}
		return null;
	}
	
	private Map<String, String> getSubjectParams() {
		Map<String, String> emailParams = new HashMap<>();
		emailParams.put("projectName", TsmpDpModule.DP.getChiDesc());
		return emailParams;
	}
	
	private Map<String, String> getBodyParams(TsmpClient tsmpClientVo, String pwd, String base64Pwd) {

		Map<String, String> emailParams = new HashMap<>();
		
		String now = "";
		Optional<String> opt = DateTimeUtil.dateTimeToString(DateTimeUtil.now(), DateTimeFormatEnum.西元年月日_2);
		if (opt.isPresent()) {
			now = opt.get();
		}
		
		String clientID = tsmpClientVo.getClientId();
		String client = tsmpClientVo.getClientName();
		String newBlock = pwd;
		String base64Block = base64Pwd;
		String date = now;
		
		emailParams.put("clientID", clientID);
		emailParams.put("client", client);
		emailParams.put("newBlock", newBlock);
		emailParams.put("base64Block", base64Block);
		emailParams.put("date", date);
		
		return emailParams;
	}
	
	

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}
	
	protected OauthClientDetailsDao getOauthClientDetailsDao() {
		return oauthClientDetailsDao;
	}
	
	protected ApplicationContext getCtx() {
		return ctx;
	}

	protected JobHelper getJobHelper() {
		return jobHelper;
	}
	
	protected TsmpDpMailTpltDao getTsmpDpMailTpltDao() {
		return this.tsmpDpMailTpltDao;
	}

	protected String getSendTime() {
		return this.sendTime;
	}
	
	protected AA0231Job getAA0231Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime) {
		return (AA0231Job) getCtx().getBean("aa0231Job", auth, mailEvents, getSendTime());
	}
	
	protected ServiceConfig getServiceConfig() {
		return serviceConfig;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected TsmpSettingDao getTsmpSettingDao() {
		return tsmpSettingDao;
	}

    protected TsmpTAEASKHelper getTsmpTAEASKHelper() {
        return tsmpTAEASKHelper;
    }

	protected DaoGenericCacheService getDaoGenericCacheService() {
		return daoGenericCacheService;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
    
}
