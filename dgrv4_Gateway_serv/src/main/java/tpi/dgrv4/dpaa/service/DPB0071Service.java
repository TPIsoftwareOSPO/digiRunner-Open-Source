package tpi.dgrv4.dpaa.service;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType.ItemContainer;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredMailJob;
import tpi.dgrv4.dpaa.component.req.DpReqServiceFactory;
import tpi.dgrv4.dpaa.component.req.DpReqServiceIfs;
import tpi.dgrv4.dpaa.component.req.DpReqServiceResp;
import tpi.dgrv4.dpaa.component.req.DpReqServiceResp_D1;
import tpi.dgrv4.dpaa.component.req.DpReqServiceResp_D2;
import tpi.dgrv4.dpaa.component.req.DpReqServiceResp_D3;
import tpi.dgrv4.dpaa.component.req.DpReqServiceResp_D5;
import tpi.dgrv4.dpaa.component.req.DpReqServiceSignReq;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0071Req;
import tpi.dgrv4.dpaa.vo.DPB0071Resp;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0071Service {

	private TPILogger logger = TPILogger.tl;
	
	private DpReqServiceFactory dpReqServiceFactory;
	private JobHelper jobHelper;
	private ApplicationContext ctx;
	private DgrAcIdpUserDao dgrAcIdpUserDao;
	private TsmpUserDao tsmpUserDao;
	
	@Autowired
	public DPB0071Service(DpReqServiceFactory dpReqServiceFactory, JobHelper jobHelper, ApplicationContext ctx,
			DgrAcIdpUserDao dgrAcIdpUserDao, TsmpUserDao tsmpUserDao) {
		super();
		this.dpReqServiceFactory = dpReqServiceFactory;
		this.jobHelper = jobHelper;
		this.ctx = ctx;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
		this.tsmpUserDao = tsmpUserDao;
	}

	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}
	
	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}

	public String getUserName(TsmpAuthorization auth) {

		if (StringUtils.hasLength(auth.getIdpType())) {
			return auth.getIdpType() + "." + auth.getUserNameForQuery();
		} else {// 以 AC 登入
		
			return auth.getUserName();
		}
	}
	
	@Transactional
	public DPB0071Resp signReq(TsmpAuthorization auth, DPB0071Req req, ReqHeader reqHeader, InnerInvokeParam iip) {
		Long reqOrdermId = checkCommonParams(auth, req);
		
		DpReqServiceIfs dpReqService = getDpReqServiceFactory().getDpReqService(reqOrdermId, () -> {
			return TsmpDpAaRtnCode._1218.throwing();
		});
		
		DpReqServiceSignReq signReq = new DpReqServiceSignReq();
		signReq.setReqOrdermId(req.getReqOrdermId());
		String userName = getUserName(auth);
		signReq.setSignUserName(auth.getUserNameForQuery());
		signReq.setIdPType(auth.getIdpType());
		signReq.setOrgId(auth.getOrgId());
		signReq.setCurrentReviewStatus(req.getChkStatus());
		signReq.setEncNextReviewStatus(req.getEncodeSubItemNo());
		signReq.setReqComment(req.getReqComment());
		String locale = ServiceUtil.getLocale(reqHeader.getLocale());

		// 初始化回傳參數
		DPB0071Resp resp = null;
		if (isRtEquals(TsmpDpReqReviewType.API_APPLICATION, dpReqService)) {
			resp = doSign(() -> {
				return dpReqService.doSign(signReq, DpReqServiceResp_D1.class, locale, iip);
			});
		} else if (isRtEquals(TsmpDpReqReviewType.API_ON_OFF, dpReqService)) {
			resp = doSign(() -> {
				return dpReqService.doSign(signReq, DpReqServiceResp_D2.class, locale, iip);
			});
		} else if (isRtEquals(TsmpDpReqReviewType.CLIENT_REG, dpReqService)) {
			resp = doSign(() -> {
				return dpReqService.doSign(signReq, DpReqServiceResp_D3.class, locale, iip);
			});
		} else if (isRtEquals(TsmpDpReqReviewType.OPEN_API_KEY, dpReqService)) {
			resp = doSign(() -> {
				return dpReqService.doSign(signReq, DpReqServiceResp_D5.class, locale, iip);
			});
		}
		// 刪除過期的 Mail log
		deleteExpiredMail();
		return resp;
	}

	private Long checkCommonParams(TsmpAuthorization auth, DPB0071Req req) {
		if (StringUtils.isEmpty(auth.getOrgId()) ||
			req.getReqOrdermId() == null) {
			this.logger.error(String.format("orgId=%s, reqOrdermId=%d", auth.getOrgId(), req.getReqOrdermId()));
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		return req.getReqOrdermId();
	}

	private <R extends DpReqServiceResp> DPB0071Resp doSign(Supplier<R> sup) {
		R r = sup.get();
		if (r != null) {
			DPB0071Resp resp = new DPB0071Resp();
			if (r.getsIds() != null && !r.getsIds().isEmpty()) {
				resp.setReqOrdersId(r.getsIds().get(0));
			}
			resp.setLv(r.getLv());
			resp.setChkLogId(r.getChkLogId());
			return resp;
		}
		return null;
	}

	/** 檢查簽核類型是否相符 */
	protected boolean isRtEquals(ItemContainer rt, DpReqServiceIfs ifs) {
		return getDpReqServiceFactory().isReviewTypeEquals(rt, ifs);
	}

	public DeleteExpiredMailJob deleteExpiredMail() {
		DeleteExpiredMailJob job = (DeleteExpiredMailJob) getCtx().getBean("deleteExpiredMailJob");
		getJobHelper().add(job);
		return job;
	}

	protected DpReqServiceFactory getDpReqServiceFactory() {
		return this.dpReqServiceFactory;
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}

}
