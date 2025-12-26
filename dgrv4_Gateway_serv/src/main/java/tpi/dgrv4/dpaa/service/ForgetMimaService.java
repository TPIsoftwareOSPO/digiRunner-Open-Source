package tpi.dgrv4.dpaa.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.Setter;
import tpi.dgrv4.codec.utils.ExpireKeyUtil;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.dpaa.component.job.AA0550Job;
import tpi.dgrv4.dpaa.component.job.AA0552Job;
import tpi.dgrv4.dpaa.util.OAuthUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0550Req;
import tpi.dgrv4.dpaa.vo.AA0550Resp;
import tpi.dgrv4.dpaa.vo.AA0551Req;
import tpi.dgrv4.dpaa.vo.AA0551Resp;
import tpi.dgrv4.dpaa.vo.AA0552Req;
import tpi.dgrv4.dpaa.vo.EmptyBodyResp;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.entity.DgrOtp;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.Users;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailTplt;
import tpi.dgrv4.entity.repository.DgrOtpDao;
import tpi.dgrv4.entity.repository.TsmpDpMailTpltDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.entity.repository.UsersDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;


@Service
public class ForgetMimaService {
	public static final String AUTH_CLIENT_ID="forgetMimaId";
	public static final String AUTH_USER_NAME="forgetMimaName";
	private final int expiredKeyTimeMinutes = 15;
	private final String verifiedOtpCode = "verified";
	public final int errorLimit = 3;
	private SecureRandom random = new SecureRandom();
	
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private TsmpUserDao tsmpUserDao;
	
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private UsersDao usersDao;
	
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private DgrOtpDao dgrOtpDao;
	
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private TsmpDpMailTpltDao tsmpDpMailTpltDao;
	
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private DgrAuditLogService dgrAuditLogService;
	
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private TsmpSettingService tsmpSettingService;
	
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private ApplicationContext ctx;
	
	@Setter(value = AccessLevel.PROTECTED, onMethod_ = @Autowired)
	private JobHelper jobHelper;


	//AA0550
	@Transactional
	public AA0550Resp sendOtp(TsmpAuthorization auth, AA0550Req req) {
		AA0550Resp resp = new AA0550Resp();
		try {
			TsmpUser tsmpUserVo = this.getTsmpUserDao().findFirstByUserEmail(req.getEmail());
			if(tsmpUserVo == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			//delete email, Because it is a one-time and safety
			getDgrOtpDao().deleteByEmail(req.getEmail());


			//otp Code
            String otpCode =  getVerificationCode();
            //expire Key
            String expireKey = getExpireKey();
			//save db
            DgrOtp otpVo = new DgrOtp();
            otpVo.setEmail(req.getEmail());
            otpVo.setExpireKey(expireKey);
            otpVo.setOtpCode(otpCode);
            this.getDgrOtpDao().save(otpVo);
			
            //send email
			this.sendEmailByOtp(req, auth, otpCode);
			
			//resp
			resp.setExpireKey(expireKey);
			return resp;
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}
	
	//AA0551
	public AA0551Resp validationOtp(AA0551Req req) {
		AA0551Resp resp = new AA0551Resp();
		DgrOtp otpVo = null;
		try {
			otpVo = this.getDgrOtpDao().findFirstByEmail(req.getEmail());
			//Check if the error has reached the upper limit
			//檢查錯誤是否達到上限
			handleErrorLimitNumber(otpVo);
			//Check whether expireKey is valid and legal
			//檢查expireKey是否有效合法
			checkExpireKey(otpVo, req.getEmail(), req.getExpireKey());
			
			//verifiedOtpCode represents a verified value, so req should not pass such a value in
			//verifiedOtpCode代表已驗證過值了,所以req不應該會傳這樣的值進來
			if(!otpVo.getOtpCode().equals(req.getOtpCode()) || verifiedOtpCode.equalsIgnoreCase(req.getOtpCode())) {
				throw TsmpDpAaRtnCode._1567.throwing("OTP");
			}
			
			List<TsmpUser> tsmpUserList = this.getTsmpUserDao().findByUserEmail(req.getEmail());
			if(tsmpUserList.isEmpty()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			List<String> userList = tsmpUserList.stream().map(vo->vo.getUserName()).collect(Collectors.toList());
			
			resp.setUserList(userList);
			
			//Because it is one-time but expireKey is still needed
			otpVo.setOtpCode(verifiedOtpCode);
			this.getDgrOtpDao().save(otpVo);

			return resp;
		} catch (TsmpDpAaException e) {
			handleErrorLimit(otpVo, e);
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}
	
	private void handleErrorLimit(DgrOtp otpVo, TsmpDpAaException e) {
		if(otpVo != null) {
			if(otpVo.getErrorLimit() >= errorLimit) {
				TPILogger.tl.info("errorCode="+e.getError().getCode());
				throw TsmpDpAaRtnCode._1569.throwing("" + errorLimit);
			}else {
				try {
					otpVo.setErrorLimit(otpVo.getErrorLimit() + 1);
					this.getDgrOtpDao().save(otpVo);
				} catch (Exception e2) {
					TPILogger.tl.info("errorCode="+e.getError().getCode());
					TPILogger.tl.error(StackTraceUtil.logStackTrace(e2));
					throw TsmpDpAaRtnCode._1297.throwing();
				}
			}
			
		}
	}
	
	private void handleErrorLimitNumber(DgrOtp otpVo) {
		if(otpVo != null) {
			if(otpVo.getErrorLimit() >= errorLimit) {
				throw TsmpDpAaRtnCode._1569.throwing("" + errorLimit);
			}
		}
	}
	
	//AA0552
	@Transactional
	public EmptyBodyResp changeMima(AA0552Req req, InnerInvokeParam iip) {
		EmptyBodyResp resp = new EmptyBodyResp();
		DgrOtp otpVo = null;
		try {
			checkParam(req);
			otpVo = this.getDgrOtpDao().findFirstByEmail(req.getEmail());
			//Check if the error has reached the upper limit
			//檢查錯誤是否達到上限
			handleErrorLimitNumber(otpVo);
			//Check whether expireKey is valid and legal
			//檢查expireKey是否有效合法
			this.checkExpireKey(otpVo, req.getEmail(), req.getExpireKey());
			List<Users> usersList = this.getUsersDao().findByUserNameIn(req.getUserList());
			if(usersList.isEmpty()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			//write Audit Log M
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_USER_PROFILE.value());
			
			//change mima and send email
			TsmpAuthorization auth = this.getTsmpAuthorization(iip);
			for(Users usersVo : usersList) {
				updateUsers(usersVo, req, iip);
				sendEmailByChangeMima(req, auth, usersVo.getUserName());
			}
			
			return resp;
		} catch (TsmpDpAaException e) {
			handleErrorLimit(otpVo, e);
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}
	
	private void checkParam(AA0552Req req) throws Exception {
		if(!StringUtils.hasText(req.getNewMima())) {
			throw TsmpDpAaRtnCode._1234.throwing(); 
		}
		
		String decodeMima = base64Decode(req.getNewMima());
		//Check password strength
		//檢查密碼強度
		if(!decodeMima.matches(this.getTsmpSettingService().getVal_PWD_STRENGTH())) {
			throw TsmpDpAaRtnCode._1352.throwing("{{newMima}}");
		}
		String aa0552_msg = "";
		if(decodeMima.length() > 128) {
			int aa0552_length = decodeMima.length();
			aa0552_msg = String.valueOf(aa0552_length);
			throw TsmpDpAaRtnCode._1248.throwing("128", aa0552_msg);
		}
	}
	
	protected String base64Decode(String userBlock) throws Exception {
		return new String(ServiceUtil.base64Decode(userBlock));
			
	}
	
	protected TsmpAuthorization getTsmpAuthorization(InnerInvokeParam iip) {
		return iip.getTsmpAuthorization();
	}
	
	private void updateUsers(Users usersVo, AA0552Req req, InnerInvokeParam iip) throws Exception {

		//Convert old data into String
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, usersVo);
		
		//update users
		String encodeMima = this.getEncodeMima(req.getNewMima());
		usersVo.setPassword(encodeMima);
		usersVo = this.getUsersDao().saveAndFlush(usersVo);
		
		//write Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
				Users.class.getSimpleName(), TableAct.U.value(), oldRowStr, usersVo);// U
	}
	
	protected String getEncodeMima(String mima) throws Exception {
		return OAuthUtil.bCryptEncode(mima);
	}
	
	private void checkExpireKey(DgrOtp otpVo, String email, String expireKey) throws Exception {
		
		if(otpVo == null) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}
		//ExpireKey and db data are different
		if(!otpVo.getExpireKey().equals(expireKey)) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		String strRs = this.verifyExpireKey(expireKey);
		if("expired".equals(strRs)) {
			throw TsmpDpAaRtnCode._1568.throwing(expiredKeyTimeMinutes + "");
		}
		
		if(!"success".equals(strRs)) {
			throw TsmpDpAaRtnCode._1567.throwing("Key");
		}
	}
	
	protected String verifyExpireKey(String expireKey) throws Exception {
		return ExpireKeyUtil.verifyExpireKeyRtnString(expireKey);
	}
	
	protected String getExpireKey() throws Exception {
		return ExpireKeyUtil.getExpireKey(expiredKeyTimeMinutes * 60 * 1000L);
	}
	
	public AA0550Job sendEmailByOtp(AA0550Req req, TsmpAuthorization auth, String code) {
		List<TsmpMailEvent> mailEvents = new ArrayList<>();
			TsmpMailEvent mailEvent = getTsmpMailEventByOtp(req, auth, code);
			if (mailEvent != null) {
				mailEvents.add(mailEvent);
			}
			
		AA0550Job job = getAA0550Job(auth, mailEvents, getSendTime());
		getJobHelper().add(job);
			
		return job;
	}
	
	private TsmpMailEvent getTsmpMailEventByOtp( AA0550Req req , TsmpAuthorization authorization, String code) {
		String aa0550_clientId = authorization.getClientId();
		String aa0550_recipients = req.getEmail();
		
		if (aa0550_recipients == null || aa0550_recipients.isEmpty()) {
			TPILogger.tl.debug(String.format("Client %s has empty emails!", aa0550_clientId));
			return null;
		}

		String aa0550_subject = null;
		String aa0550_body = null;
		aa0550_subject = getTemplate("subject.otp");
		aa0550_body = getTemplate("body.otp");
		
		if (aa0550_subject == null || aa0550_body == null) {
			TPILogger.tl.error("subject.otp is null or body.otp is null");
			return null;
		}

		Map<String, String> bodyParams = getBodyParamsByOtp(code);
		
		final String title = aa0550_subject;
		final String content = MailHelper.buildContent(aa0550_body, bodyParams);
		
		return new TsmpMailEventBuilder() //
		.setSubject(title)
		.setContent(content)
		.setRecipients(aa0550_recipients)
		.setCreateUser(authorization.getUserName())
		.setRefCode("body.otp")
		.build();
	}
	
	private Map<String, String> getBodyParamsByOtp(String code) {

		Map<String, String> emailParams = new HashMap<>();
		String minutes = String.valueOf(expiredKeyTimeMinutes);
		
		emailParams.put("code", code);
		emailParams.put("minutes", minutes);
		return emailParams;
	}
	
	private String getTemplate(String code) {
		List<TsmpDpMailTplt> aa0550_list = getTsmpDpMailTpltDao().findByCode(code);
		if (aa0550_list != null && !aa0550_list.isEmpty()) {
			return aa0550_list.get(0).getTemplateTxt();
		}
		return null;
	}
	
	public AA0552Job sendEmailByChangeMima(AA0552Req req, TsmpAuthorization auth, String tUser) {
		List<TsmpMailEvent> mailEvents = new ArrayList<>();
			TsmpMailEvent mailEvent = getTsmpMailEventByChangeMima(req, auth, tUser);
			if (mailEvent != null) {
				mailEvents.add(mailEvent);
			}
			
		AA0552Job job = getAA0552Job(auth, mailEvents, getSendTime());
		getJobHelper().add(job);
			
		return job;
	}
	
	private TsmpMailEvent getTsmpMailEventByChangeMima( AA0552Req req , TsmpAuthorization authorization, String tUser) {
		String aa0552_clientId = authorization.getClientId();
		String aa0552_recipients = req.getEmail();
		
		if (aa0552_recipients == null || aa0552_recipients.isEmpty()) {
			TPILogger.tl.debug(String.format("Client %s has empty emails!", aa0552_clientId));
			return null;
		}

		String aa0552_subject = null;
		String aa0552_body = null;
		aa0552_subject = getTemplate("subject.changeMima");
		aa0552_body = getTemplate("body.changeMima");
		
		if (aa0552_subject == null || aa0552_body == null) {
			TPILogger.tl.error("subject.changeMima is null or body.changeMima is null");
			return null;
		}

		Map<String, String> bodyParams = getBodyParamsByChangeMima(tUser);
		
		final String title = aa0552_subject;
		final String content = MailHelper.buildContent(aa0552_body, bodyParams);
		
		return new TsmpMailEventBuilder() //
		.setSubject(title)
		.setContent(content)
		.setRecipients(aa0552_recipients)
		.setCreateUser(authorization.getUserName())
		.setRefCode("body.changeMima")
		.build();
	}
	
	private Map<String, String> getBodyParamsByChangeMima(String tUser) {

		Map<String, String> emailParams = new HashMap<>();
		String nowDate = DateTimeUtil.dateTimeToString(DateTimeUtil.now(), DateTimeFormatEnum.西元年月日時分).orElse(null); 
		
		emailParams.put("tUser", tUser);
		emailParams.put("date", nowDate);
		return emailParams;
	}
	
	protected String getSendTime() {
		//return this.getTsmpSettingService().getVal_MAIL_SEND_TIME();//How long does it take to send the email? (ms)
		return "0";
	}
	
	protected String getVerificationCode() {
        int number = random.nextInt(1_000_000); // 0 ~ 999999
        String strNumber = String.format("%06d", number);
        return strNumber;
	}
	
	protected AA0550Job getAA0550Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime) {
		return (AA0550Job) getCtx().getBean("aa0550Job", auth, mailEvents, getSendTime());
	}
	
	protected AA0552Job getAA0552Job(TsmpAuthorization auth, List<TsmpMailEvent> mailEvents, String sendTime) {
		return (AA0552Job) getCtx().getBean("aa0552Job", auth, mailEvents, getSendTime());
	}

	protected TsmpUserDao getTsmpUserDao() {
		return tsmpUserDao;
	}

	protected UsersDao getUsersDao() {
		return usersDao;
	}

	protected DgrOtpDao getDgrOtpDao() {
		return dgrOtpDao;
	}

	protected TsmpDpMailTpltDao getTsmpDpMailTpltDao() {
		return tsmpDpMailTpltDao;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}

	protected ApplicationContext getCtx() {
		return ctx;
	}

	protected JobHelper getJobHelper() {
		return jobHelper;
	}

	protected String getVerifiedOtpCode() {
		return verifiedOtpCode;
	}
	
	
}
