package tpi.dgrv4.dpaa.service;

import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.component.job.SendReviewMailJob;
import tpi.dgrv4.dpaa.component.req.DpReqQueryFactory;
import tpi.dgrv4.dpaa.component.req.DpReqQueryIfs;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class SendReviewMailService {

	private TPILogger logger = TPILogger.tl;

	private ApplicationContext ctx;
	private JobHelper jobHelper;
	private TsmpUserDao tsmpUserDao;
	private DpReqQueryFactory dpReqQueryFactory;
	private TsmpSettingService tsmpSettingService;
	
	private String sendTime;
 
	@Autowired
	public SendReviewMailService(ApplicationContext ctx, JobHelper jobHelper, TsmpUserDao tsmpUserDao,
			@Lazy DpReqQueryFactory dpReqQueryFactory, TsmpSettingService tsmpSettingService) {
		super();
		this.ctx = ctx;
		this.jobHelper = jobHelper;
		this.tsmpUserDao = tsmpUserDao;
		this.dpReqQueryFactory = dpReqQueryFactory;
		this.tsmpSettingService = tsmpSettingService;
	}

	@PostConstruct
	public void init() {
	}
	
	public SendReviewMailJob sendEmail(TsmpAuthorization authorization, String reviewType, Long reqOrdermId,
			String reqOrderNo, String locale) {
		// 使用 Job 寫入 APPT_JOB Table & 建立 Mail 檔案, 由排程來寄信
		SendReviewMailJob job = (SendReviewMailJob) getCtx().getBean("sendReviewMailJob", authorization, reqOrdermId,
				reviewType, getSendTime(), reqOrderNo, locale);
		getJobHelper().add(job);
		return job;
	}
	
	public List<TsmpMailEvent> getTsmpMailEventList(TsmpAuthorization authorization, Long reqOrdermId, String reviewType, String locale) {
		DpReqQueryIfs<?> ifs = getDpReqQueryFactory().getDpReqQuery(reqOrdermId, () -> {
			throw TsmpDpAaRtnCode._1297.throwing();
		});
		return ifs.getTsmpMailEvents(authorization, reqOrdermId, locale);
	}
	
	protected ApplicationContext getCtx() {
		return this.ctx;
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}

	protected String getSendTime() {
		this.sendTime = this.getTsmpSettingService().getVal_MAIL_SEND_TIME();//多久後寄發Email(ms)
		return this.sendTime;
	}

	protected DpReqQueryFactory getDpReqQueryFactory() {
		return this.dpReqQueryFactory;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}

}
