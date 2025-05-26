package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpRegStatus;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.dpaa.component.job.DPB0005Job;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredMailJob;
import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpModule;
import tpi.dgrv4.dpaa.vo.DPB0005Req;
import tpi.dgrv4.dpaa.vo.DPB0005Resp;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpDpClientext;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.jpql.TsmpDpMailTplt;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpClientextDao;
import tpi.dgrv4.entity.repository.TsmpDpMailTpltDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0005Service {

	private TPILogger logger = TPILogger.tl;
	
	private String sendTime;

	private TsmpDpClientextDao tsmpDpClientextDao;
	private TsmpClientDao tsmpClientDao;
	private TsmpDpMailTpltDao tsmpDpMailTpltDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private JobHelper jobHelper;
	private ApplicationContext ctx;
	private DgrAuditLogService dgrAuditLogService;
	private TsmpSettingService tsmpSettingService;
	
	@Autowired
	public DPB0005Service(TsmpDpClientextDao tsmpDpClientextDao, TsmpClientDao tsmpClientDao,
			TsmpDpMailTpltDao tsmpDpMailTpltDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, JobHelper jobHelper,
			ApplicationContext ctx, DgrAuditLogService dgrAuditLogService, TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpDpClientextDao = tsmpDpClientextDao;
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpDpMailTpltDao = tsmpDpMailTpltDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.jobHelper = jobHelper;
		this.ctx = ctx;
		this.dgrAuditLogService = dgrAuditLogService;
		this.tsmpSettingService = tsmpSettingService;
	}

	@PostConstruct
	public void init() {
	}
	
	@Transactional
	public DPB0005Resp updateMemberStatus(TsmpAuthorization authorization, DPB0005Req req, ReqHeader reqHeader, InnerInvokeParam iip) {
		
		String regStatus = req.getRegStatus();
		if (isInvalidRegStatus(regStatus)) {
			throw TsmpDpAaRtnCode.FAIL_MEMBER_QUALIFICATION.throwing();
		}

		// "放行"則"公開/私有"為必填
		String publicFlag = req.getPublicFlag();
		if (TsmpDpRegStatus.PASS.value().equals(regStatus) &&
			isInvalidPublicFlag(publicFlag, reqHeader.getLocale())) {
			throw TsmpDpAaRtnCode.FAIL_MEMBER_QUALIFICATION.throwing();
		}

		// 0=寄送;1=不寄送
		String mailFlag = getMailFlag(req.getMailFlag());
		if (mailFlag == null) {
			throw TsmpDpAaRtnCode.FAIL_MEMBER_QUALIFICATION.throwing();
		}

		// "不通過"則審核備註為必填
		String reviewRemark = req.getReviewRemark();
		if (regStatus.equals(TsmpDpRegStatus.RETURN.value()) &&
			(reviewRemark == null || reviewRemark.isEmpty())) {
			throw TsmpDpAaRtnCode.FAIL_MEMBER_QUALIFICATION.throwing();
		}

		List<String> extClientIds = req.getClientIds();
		List<TsmpDpClientext> extList = getExtList(extClientIds);
		if (extList == null || extList.isEmpty()) {
			throw TsmpDpAaRtnCode.FAIL_MEMBER_QUALIFICATION.throwing();
		}

		DPB0005Resp resp = new DPB0005Resp();
		List<TsmpDpClientext> failList = doUpdate(extList, regStatus, reviewRemark, publicFlag, authorization, iip);
		resp.setClientIds(failList.stream().map((ext) -> {
			return ext.getClientId();
		}).collect(Collectors.toList()));

		// 更新成功後, 確認MailFlag設定是否發送Email
		this.logger.debug("Mail Flag = " + mailFlag);
		if ("0".equals(mailFlag)) {
			sendEmail(extList, authorization, regStatus);
		}
		
		// 刪除過期的 Mail log
		deleteExpiredMail();

		return resp;
	}

	private boolean isInvalidPublicFlag(String publicFlag, String locale) {
		if (!StringUtils.isEmpty(publicFlag)) {
			TsmpDpItemsId id = new TsmpDpItemsId("API_AUTHORITY", publicFlag, locale);
			TsmpDpItems vo = getTsmpDpItemsCacheProxy().findById(id);
			return vo == null;
		}
		return true;
	}

	private List<TsmpDpClientext> getExtList(List<String> clientIds) {
		List<TsmpDpClientext> extList = null;

		if (clientIds != null && !clientIds.isEmpty()) {
			extList = new ArrayList<>();
			try {
				extList = getTsmpDpClientextDao().findAllById(clientIds);
				if (extList != null && extList.size() != clientIds.size()) {
					throw new TsmpDpAaException("Result size should be (" + //
							clientIds.size() + ") but (" + extList.size() + ")!");
				}
			} catch (Exception e) {
				this.logger.error(StackTraceUtil.logStackTrace(e));
			}
		}

		return extList;
	}

	private boolean isInvalidRegStatus(String value) {
		boolean isValid = (
			TsmpDpRegStatus.PASS.value().equals(value) ||
			TsmpDpRegStatus.RETURN.value().equals(value)
		);
		return !isValid;
	}

	private String getMailFlag(String mailFlag) {
		if (mailFlag != null && ("0".equals(mailFlag) || "1".equals(mailFlag))) {
			return mailFlag;
		}
		return null;
	}

	private List<TsmpDpClientext> doUpdate(List<TsmpDpClientext> extList, String regStatus//
			, String reviewRemark, String publicFlag, TsmpAuthorization authorization, InnerInvokeParam iip) {
		List<TsmpDpClientext> failList = new ArrayList<>();
		List<TsmpDpClientext> successList = new ArrayList<>();

		int uuidIndex = 1;
		String uuid = iip != null ? iip.getTxnUid() : null;
		for(TsmpDpClientext ext : extList) {
			try {
				if(iip != null) {
					iip.setTxnUid(uuid + "_" + uuidIndex);
					uuidIndex++;
				}
				//寫入 Audit Log M
				String lineNumber = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_CLIENT.value());
				
				String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, ext); //舊資料統一轉成 String
				
				ext.setRegStatus(regStatus);
				ext.setReviewRemark(reviewRemark);
				ext.setRefReviewUser(authorization.getUserName());
				ext.setPublicFlag(publicFlag);
				ext.setUpdateDateTime(DateTimeUtil.now());
				ext.setUpdateUser(authorization.getUserName());
				ext = getTsmpDpClientextDao().saveAndFlush(ext);
				
				//寫入 Audit Log D
				lineNumber = StackTraceUtil.getLineNumber();
				getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
						TsmpDpClientext.class.getSimpleName(), TableAct.U.value(), oldRowStr, ext);

				successList.add(ext);
			} catch (Exception e) {
				this.logger.error(StackTraceUtil.logStackTrace(e));
				failList.add(ext);
			}
		}

		extList.clear();
		extList.addAll(successList);
		return failList;
	}

	public DPB0005Job sendEmail(List<TsmpDpClientext> extList, TsmpAuthorization authorization, String regStatus) {
		List<TsmpMailEvent> mailEvents = new ArrayList<>();
		TsmpMailEvent mailEvent = null;
		for(TsmpDpClientext ext : extList) {
			mailEvent = getTsmpMailEvent(ext, authorization, regStatus);
			if (mailEvent != null) {
				mailEvents.add(mailEvent);
			}
		}
		
		//使用 Job 寫入 APPT_JOB Table & 建立 Mail 檔案, 由排程來寄信
		DPB0005Job job = getDPB0005Job(authorization, mailEvents);
		
		return job;
	}
	
	protected DPB0005Job getDPB0005Job(TsmpAuthorization authorization, List<TsmpMailEvent> mailEvents) {
		DPB0005Job job = (DPB0005Job) getCtx().getBean("dpb0005Job", authorization, mailEvents, getSendTime());
		getJobHelper().add(job);
		return job;
	}

	public DeleteExpiredMailJob deleteExpiredMail() {
		DeleteExpiredMailJob job = (DeleteExpiredMailJob) getCtx().getBean("deleteExpiredMailJob");
		getJobHelper().add(job);
		return job;
	}

	private TsmpMailEvent getTsmpMailEvent(TsmpDpClientext ext, TsmpAuthorization authorization //
			, String regStatus) {
		TsmpClient dpb0005_client = getClient(ext.getClientId());
		if (dpb0005_client == null) {
			this.logger.debug(String.format("TsmpDpClientext: clientId=%s missing client!", ext.getClientId()));
			return null;
		}

		String dpb0005_recipients = dpb0005_client.getEmails();
		if (dpb0005_recipients == null || dpb0005_recipients.isEmpty()) {
			this.logger.debug(String.format("TsmpDpClientext: clientId=%s empty emails!", ext.getClientId()));
			return null;
		}

		Map<String, String> dpb0005_subjectParams = getSubjectParams();
		if (dpb0005_subjectParams == null || dpb0005_subjectParams.isEmpty()) {
			this.logger.debug(String.format("TsmpDpClientext: clientId=%s empty subject params!", ext.getClientId()));
			return null;
		}

		Map<String, String> dpb0005_bodyParams = getBodyParams(ext, dpb0005_client, regStatus);
		if (dpb0005_bodyParams == null || dpb0005_bodyParams.isEmpty()) {
			this.logger.debug(String.format("TsmpDpClientext: clientId=%s empty body params!", ext.getClientId()));
			return null;
		}

		String dpb0005_templateKey = "subject.member-pass";
		String dpb0005_subject = getTemplate(dpb0005_templateKey);
		if (StringUtils.isEmpty(dpb0005_subject == null)) {
			this.logger.error(String.format("Missing template \"%s\", didn't send email.", dpb0005_templateKey));
			return null;
		}

		if (TsmpDpRegStatus.PASS.value().equals(regStatus)) {
			dpb0005_templateKey = "body.member-pass";
		} else if (TsmpDpRegStatus.RETURN.value().equals(regStatus)) {
			dpb0005_templateKey = "body.member-fail";
		}
		String template = getTemplate(dpb0005_templateKey);
		if (StringUtils.isEmpty(template)) {
			this.logger.error(String.format("Missing template \"%s\", didn't send email.", dpb0005_templateKey));
			return null;
		}

		final String title = MailHelper.buildContent(dpb0005_subject, dpb0005_subjectParams);
		final String content = MailHelper.buildContent(template, dpb0005_bodyParams);
		this.logger.debug("Email title = " + title);
		this.logger.debug("Email content = " + content);
		return new TsmpMailEventBuilder() //
		.setSubject(title)
		.setContent(content)
		.setRecipients(dpb0005_recipients)
		.setCreateUser(authorization.getUserName())
		.setRefCode(dpb0005_templateKey)
		.build();
	}

	private TsmpClient getClient(String clientId) {
		Optional<TsmpClient> dpb0005_opt = getTsmpClientDao().findById(clientId);
		if (dpb0005_opt.isPresent()) {
			return dpb0005_opt.get();
		}
		return null;
	}

	private Map<String, String> getSubjectParams() {
		Map<String, String> dpb0005_params = new HashMap<>();
		dpb0005_params.put("projectName", TsmpDpModule.DP.getChiDesc());
		return dpb0005_params;
	}

	private Map<String, String> getBodyParams(TsmpDpClientext ext, TsmpClient client //
			, String regStatus) {
		Map<String, String> dpb0005_params = new HashMap<>();

		// 通過
		if (TsmpDpRegStatus.PASS.value().equals(regStatus)) {
			String clientName = client.getClientName();
			if (clientName == null || clientName.isEmpty()) {
				return null;
			}
			dpb0005_params.put("projectName", TsmpDpModule.DP.getChiDesc());
			dpb0005_params.put("clientId", client.getClientId());
			dpb0005_params.put("clientName", clientName);
			dpb0005_params.put("clientSd", convertObjectToDateString(client.getStartDate(),client.getTimeZone()));
			dpb0005_params.put("clientEd", convertObjectToDateString(client.getEndDate(),client.getTimeZone()));
			dpb0005_params.put("svcSt", convertObjectToTimeString(client.getStartTimePerDay(),client.getTimeZone()));
			dpb0005_params.put("svcEt", convertObjectToTimeString(client.getEndTimePerDay(),client.getTimeZone()));
			dpb0005_params.put("timeZone", nvl(client.getTimeZone()));
			dpb0005_params.put("apiQuota", nvl(client.getApiQuota()));
			dpb0005_params.put("tps", nvl(client.getTps()));
			dpb0005_params.put("cPriority", nvl(client.getcPriority()));
		// 退回
		} else if (TsmpDpRegStatus.RETURN.value().equals(regStatus)) {
			dpb0005_params.put("reviewRemark", ext.getReviewRemark());
			dpb0005_params.put("refReviewUser", ext.getRefReviewUser());
			dpb0005_params.put("updateDateTime", DateTimeUtil.dateTimeToString(ext.getUpdateDateTime(), DateTimeFormatEnum.西元年月日時分_2).orElse(""));
			dpb0005_params.put("projectName", TsmpDpModule.DP.getChiDesc());
		}

		return dpb0005_params;
	}

	private String convertObjectToDateString(Long datetime, String timezone) {
		if (datetime == null || !StringUtils.hasText(timezone)) {
			return "";
		}
		Date dpb0005_date = new Date(datetime);
		return DateTimeUtil.dateTimeToString(dpb0005_date, DateTimeFormatEnum.西元年月日_2, timezone).orElse("");
	}
	
	private String convertObjectToTimeString(Long datetime, String timezone) {
		if (datetime == null || !StringUtils.hasText(timezone)) {
			return "";
		}
		Date dpb0005_date = new Date(datetime);
		return DateTimeUtil.dateTimeToString(dpb0005_date, DateTimeFormatEnum.時分秒, timezone).orElse("");
	}

	private String getTemplate(String code) {
		if (!StringUtils.isEmpty(code)) {
			List<TsmpDpMailTplt> list = getTsmpDpMailTpltDao().findByCode(code);
			if (list != null && !list.isEmpty()) {
				return list.get(0).getTemplateTxt();
			}
		}
		return null;
	}

	private String nvl(Object obj) {
		return nvl(obj, null);
	}

	private String nvl(Object obj, Function<Object, String> func) {
		if (obj == null) {
			return new String();
		}
		if (func == null) {
			return String.valueOf(obj);
		}
		return func.apply(obj);
	}

	protected TsmpDpClientextDao getTsmpDpClientextDao() {
		return this.tsmpDpClientextDao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}

	protected TsmpDpMailTpltDao getTsmpDpMailTpltDao() {
		return this.tsmpDpMailTpltDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}
	
	protected String getSendTime() {
		this.sendTime = this.getTsmpSettingService().getVal_MAIL_SEND_TIME();//多久後寄發Email(ms)
		return this.sendTime;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}
}
