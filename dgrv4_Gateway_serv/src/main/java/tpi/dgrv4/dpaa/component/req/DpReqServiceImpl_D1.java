package tpi.dgrv4.dpaa.component.req;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpApplyStatus;
import tpi.dgrv4.common.constant.TsmpDpRegStatus;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.dpaa.service.SendAPIApplicationMailService;
import tpi.dgrv4.dpaa.service.SendClientRegMailService;
import tpi.dgrv4.dpaa.service.SendReviewMailService;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpDpClientext;
import tpi.dgrv4.entity.entity.jpql.TsmpApiExt;
import tpi.dgrv4.entity.entity.jpql.TsmpApiExtId;
import tpi.dgrv4.entity.entity.jpql.TsmpDpApiAuth2;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd1;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrders;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiExtDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpApiAuth2Dao;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpChkLayerDao;
import tpi.dgrv4.entity.repository.TsmpDpChkLogDao;
import tpi.dgrv4.entity.repository.TsmpDpClientextDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd1Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdersDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * 處理"用戶申請API"簽核流程
 * @author Kim
 *
 */
@Service(value = "dpReqServiceImpl_D1")
public class DpReqServiceImpl_D1 extends DpReqServiceAbstract implements DpReqServiceIfs {

	private TPILogger logger = TPILogger.tl;

	private TsmpApiDao tsmpApiDao;
	private TsmpDpClientextDao tsmpDpClientextDao;
	private TsmpApiExtDao tsmpApiExtDao;
	private TsmpDpReqOrderd1Dao tsmpDpReqOrderd1Dao;
	private TsmpDpApiAuth2Dao tsmpDpApiAuth2Dao;

	@Autowired
	public DpReqServiceImpl_D1(ApptJobDispatcher apptJobDispatcher, TsmpClientDao tsmpClientDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpDpReqOrdermDao tsmpDpReqOrdermDao, TsmpUserDao tsmpUserDao,
			SeqStoreService seqStoreService, TsmpDpReqOrdersDao tsmpDpReqOrdersDao, FileHelper fileHelper,
			TsmpDpFileDao tsmpDpFileDao, TsmpDpChkLayerDao tsmpDpChkLayerDao, TsmpDpChkLogDao tsmpDpChkLogDao,
			BcryptParamHelper bcryptParamHelper, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpDpApptJobDao tsmpDpApptJobDao, SendReviewMailService mailService,
			SendClientRegMailService sendClientRegMailService,
			SendAPIApplicationMailService sendAPIApplicationMailService, DgrAuditLogService dgrAuditLogService,
			DgrAcIdpUserDao dgrAcIdpUserDao, TsmpApiDao tsmpApiDao, TsmpDpClientextDao tsmpDpClientextDao,
			TsmpApiExtDao tsmpApiExtDao, TsmpDpReqOrderd1Dao tsmpDpReqOrderd1Dao, TsmpDpApiAuth2Dao tsmpDpApiAuth2Dao) {
		super(apptJobDispatcher, tsmpClientDao, tsmpDpItemsCacheProxy, tsmpDpReqOrdermDao, tsmpUserDao, seqStoreService,
				tsmpDpReqOrdersDao, fileHelper, tsmpDpFileDao, tsmpDpChkLayerDao, tsmpDpChkLogDao, bcryptParamHelper,
				tsmpOrganizationDao, tsmpDpApptJobDao, mailService, sendClientRegMailService,
				sendAPIApplicationMailService, dgrAuditLogService, dgrAcIdpUserDao);
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpDpClientextDao = tsmpDpClientextDao;
		this.tsmpApiExtDao = tsmpApiExtDao;
		this.tsmpDpReqOrderd1Dao = tsmpDpReqOrderd1Dao;
		this.tsmpDpApiAuth2Dao = tsmpDpApiAuth2Dao;
	}

	@Override
	protected <Q extends DpReqServiceSaveDraftReq> void checkDetailReq(Q q, String locale) throws TsmpDpAaException {
		DpReqServiceSaveDraftReq_D1 req = castSaveDraftReq(q, DpReqServiceSaveDraftReq_D1.class);
		if (req == null) {
			throw TsmpDpAaRtnCode._1213.throwing();
		}

		// 如果是後端申請, 就一定要帶入生效日期; 前端可以不指定生效日期, 代表審核通過立即生效
		checkEffectiveDate(req.getEffectiveDate(), () -> {
			if (!StringUtils.isEmpty(req.getOrgId())) {
				return TsmpDpAaRtnCode._1296.throwing();
			}
			return null;
		}, false);

		final String clientId = req.get_clientId();
		if (StringUtils.isEmpty(clientId)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		// clientId不存在
		if (!getTsmpClientDao().findById(clientId).isPresent()) {
			throw TsmpDpAaRtnCode.NO_MEMBER_INFO.throwing();
		}
		
		List<String> apiUids = req.getApiUids();
		if (apiUids == null || apiUids.isEmpty()) {
			this.logger.error("沒有傳入參數: apiUids");
			throw TsmpDpAaRtnCode.NO_APPLIED_API.throwing();
		}
		
		// 取出 clientExt
		TsmpDpClientext clientExt = getClientExt(clientId);
		if (clientExt == null) {
			throw TsmpDpAaRtnCode.NO_MEMBER_INFO.throwing();
		}

		// client必須已放行
		if (!TsmpDpRegStatus.PASS.value().equals(clientExt.getRegStatus())) {
			throw TsmpDpAaRtnCode._1228.throwing();
		}

		// 詳細檢查每筆API
		List<TsmpApi> apiList = null;
		TsmpApiExt apiExt = null;
		for(String apiUid : apiUids) {
			apiList = getTsmpApiDao().findByApiUid(apiUid);
			// 檢查API是否存在
			if (apiList == null || apiList.isEmpty()) {
				this.logger.error("找不到API: " + apiUid);
				throw TsmpDpAaRtnCode.NO_APPLIED_API.throwing();
			}
			// 檢查是否有其他正在申請相同API的單還沒結束
			checkHasInProgress(null, clientId, apiUid);
			// 檢查是否已申請過
			checkPrevAuth(clientId, apiUid);

			for(TsmpApi api : apiList) {
				// 檢查API的publicFlag是否與申請者(client)的publicFlag相符
				if (!isQualified(api, clientExt)) {
					throw TsmpDpAaRtnCode.NO_AUTH_TO_APPLY.throwing();
				}
				// 檢查API是否已上架
				Optional<TsmpApiExt> opt_apiExt = getTsmpApiExtDao().findById(new TsmpApiExtId(api.getApiKey(), 
						api.getModuleName()));
				if (!opt_apiExt.isPresent()) {
					throw TsmpDpAaRtnCode.NO_APPLIED_API.throwing();
				}
				apiExt = opt_apiExt.get();
				if (!"1".equals(apiExt.getDpStatus())) {
					throw TsmpDpAaRtnCode.NO_AUTH_TO_APPLY.throwing();
				}
			}
		}
	}

	@Override
	protected <R extends DpReqServiceResp, Q extends DpReqServiceSaveDraftReq> void saveDetail(TsmpDpReqOrderm m, Q q,
			R r, InnerInvokeParam iip) {
		DpReqServiceSaveDraftReq_D1 req = castSaveDraftReq(q, DpReqServiceSaveDraftReq_D1.class);
		DpReqServiceResp_D1 resp = castResp(r, DpReqServiceResp_D1.class);
		
		resp.setReqOrderd1Ids(new ArrayList<>());
		
		final String clientId = req.get_clientId();
		TsmpDpReqOrderd1 d1 = null;
		for(String apiUid : req.getApiUids()) {
			d1 = new TsmpDpReqOrderd1();
			d1.setRefReqOrdermId(m.getReqOrdermId());
			d1.setClientId(clientId);
			d1.setApiUid(apiUid);
			d1.setCreateDateTime(DateTimeUtil.now());
			d1.setCreateUser(m.getCreateUser());
			d1 = getTsmpDpReqOrderd1Dao().saveAndFlush(d1);
			resp.getReqOrderd1Ids().add(d1.getReqOrderd1Id());
		}
	}

	@Override
	protected void deleteDraftDetail(TsmpDpReqOrderm m) throws TsmpDpAaException {
		Long reqOrdermId = m.getReqOrdermId();
		List<TsmpDpReqOrderd1> d1List = getTsmpDpReqOrderd1Dao().findByRefReqOrdermId(reqOrdermId);
		if (!CollectionUtils.isEmpty(d1List)) {
			getTsmpDpReqOrderd1Dao().deleteAll(d1List);
		}
	}

	@Override
	protected <Q extends DpReqServiceUpdateReq> void checkDetailUpdateReq(Q q, String locale) throws TsmpDpAaException {
		DpReqServiceUpdateReq_D1 req = castUpdateReq(q, DpReqServiceUpdateReq_D1.class);
		if (req == null) {
			throw TsmpDpAaRtnCode._1223.throwing();
		}

		// 如果是後端申請, 就一定要帶入生效日期; 前端可以不指定生效日期, 代表審核通過立即生效
		checkEffectiveDate(req.getEffectiveDate(), () -> {
			if (!StringUtils.isEmpty(req.getOrgId())) {
				return TsmpDpAaRtnCode._1296.throwing();
			}
			return null;
		}, false);
		
		String clientId = req.get_clientId();
		List<String> apiUids = req.getApiUids();
		if (StringUtils.isEmpty(clientId) ||
			apiUids == null || apiUids.isEmpty()) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		// 取出Clientext
		TsmpDpClientext clientExt = getClientExt(clientId);
		if (clientExt == null) {
			throw TsmpDpAaRtnCode.NO_MEMBER_INFO.throwing();
		}

		// client必須已放行
		if (!TsmpDpRegStatus.PASS.value().equals(clientExt.getRegStatus())) {
			throw TsmpDpAaRtnCode._1228.throwing();
		}
		
		// 詳細檢查每筆API
		List<TsmpApi> apiList = null;
		TsmpApiExt apiExt = null;
		HashSet<String> apiUidSet = new HashSet<>();
		for(String apiUid : apiUids) {
			// 同一張申請單不能有兩筆相同的API
			if (apiUidSet.contains(apiUid)) {
				throw TsmpDpAaRtnCode._1290.throwing();	// 參數錯誤
			} else {
				apiUidSet.add(apiUid);
			}
			apiList = getTsmpApiDao().findByApiUid(apiUid);
			// 檢查API是否存在
			if (apiList == null || apiList.isEmpty()) {
				this.logger.error("找不到API: " + apiUid);
				throw TsmpDpAaRtnCode.NO_APPLIED_API.throwing();
			}
			// 檢查是否有其他正在申請相同API的單還沒結束
			checkHasInProgress(req.getReqOrdermId(), clientId, apiUid);
			// 檢查是否已申請過
			checkPrevAuth(clientId, apiUid);

			for(TsmpApi api : apiList) {
				// 檢查API的publicFlag是否與申請者(client)的publicFlag相符
				if (!isQualified(api, clientExt)) {
					throw TsmpDpAaRtnCode.NO_AUTH_TO_APPLY.throwing();
				}
				// 檢查API是否已上架
				Optional<TsmpApiExt> opt_apiExt = getTsmpApiExtDao().findById(new TsmpApiExtId(api.getApiKey(), api.getModuleName()));
				if (!opt_apiExt.isPresent()) {
					throw TsmpDpAaRtnCode.NO_APPLIED_API.throwing();
				}
				apiExt = opt_apiExt.get();
				if (!"1".equals(apiExt.getDpStatus())) {
					throw TsmpDpAaRtnCode.NO_AUTH_TO_APPLY.throwing();
				}
			}
		}
	}

	@Override
	protected <R extends DpReqServiceResp, Q extends DpReqServiceUpdateReq> void updateDetail(TsmpDpReqOrderm m, Q q,
			R r, InnerInvokeParam iip) {
		DpReqServiceUpdateReq_D1 req = castUpdateReq(q, DpReqServiceUpdateReq_D1.class);
		DpReqServiceResp_D1 resp = castResp(r, DpReqServiceResp_D1.class);
		
		// 清除舊的D1
		final Long reqOrdermId = m.getReqOrdermId();
		List<TsmpDpReqOrderd1> oldD1List = getTsmpDpReqOrderd1Dao().findByRefReqOrdermId(reqOrdermId);
		if (oldD1List != null && !oldD1List.isEmpty()) {
			for(TsmpDpReqOrderd1 oldD1 : oldD1List) {
				getTsmpDpReqOrderd1Dao().delete(oldD1);
			}
		}
		
		getTsmpDpReqOrderd1Dao().flush();
		
		// 重建新的D1
		resp.setReqOrderd1Ids(new ArrayList<>());
		
		final String clientId = req.get_clientId();
		TsmpDpReqOrderd1 d1 = null;
		for(String apiUid : req.getApiUids()) {
			d1 = new TsmpDpReqOrderd1();
			d1.setRefReqOrdermId(m.getReqOrdermId());
			d1.setClientId(clientId);
			d1.setApiUid(apiUid);
			d1.setCreateDateTime(DateTimeUtil.now());
			d1.setCreateUser(m.getUpdateUser());
			d1 = getTsmpDpReqOrderd1Dao().saveAndFlush(d1);
			resp.getReqOrderd1Ids().add(d1.getReqOrderd1Id());
		}
	}

	@Override
	protected <R extends DpReqServiceResp> void postSubmit(TsmpDpReqOrderm m, DpReqServiceUpdateReq q, R r, String locale, InnerInvokeParam iip) {
		DpReqServiceResp_D1 d1Resp = castResp(r, DpReqServiceResp_D1.class);
		
		// 寫入 tsmp_dp_api_auth2
		List<TsmpDpReqOrderd1> d1List = getTsmpDpReqOrderd1Dao().findByRefReqOrdermId(m.getReqOrdermId());
		if (d1List == null || d1List.isEmpty()) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		d1Resp.setApiAuthIds(new ArrayList<>());
		TsmpDpApiAuth2 auth = null;
		for(TsmpDpReqOrderd1 d1 : d1List) {
			auth = new TsmpDpApiAuth2();
			auth.setRefClientId(d1.getClientId());
			auth.setRefApiUid(d1.getApiUid());
			auth.setApplyStatus(TsmpDpApplyStatus.REVIEW.value());
			auth.setApplyPurpose(m.getReqDesc());
			auth.setCreateDateTime(DateTimeUtil.now());
			auth.setCreateUser(q.getUpdateUser());
			auth = getTsmpDpApiAuth2Dao().save(auth);
			d1Resp.getApiAuthIds().add(auth.getApiAuthId());
		}
		
		super.postSubmit(m, q, r, locale, iip);
	}

	@Override
	protected <R extends DpReqServiceResp> void postResubmit(TsmpDpReqOrderm m, DpReqServiceUpdateReq q, R r, String locale, InnerInvokeParam iip) {
		DpReqServiceResp_D1 d1Resp = castResp(r, DpReqServiceResp_D1.class);
		
		/**
		 * 因為只有退回後才可重送, 且退回時會將 tsmp_dp_api_auth2 的 apply_status 改為 FAIL
		 * 所以在重送時, 只需寫入新資料即可
		 */
		List<TsmpDpReqOrderd1> newD1List = getTsmpDpReqOrderd1Dao().findByRefReqOrdermId(m.getReqOrdermId());
		if (newD1List == null || newD1List.isEmpty()) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		d1Resp.setApiAuthIds(new ArrayList<>());
		TsmpDpApiAuth2 auth = null;
		for(TsmpDpReqOrderd1 newD1 : newD1List) {
			auth = new TsmpDpApiAuth2();
			auth.setRefClientId(newD1.getClientId());
			auth.setRefApiUid(newD1.getApiUid());
			auth.setApplyStatus(TsmpDpApplyStatus.REVIEW.value());
			auth.setApplyPurpose(m.getReqDesc());
			auth.setCreateDateTime(DateTimeUtil.now());
			auth.setCreateUser(q.getUpdateUser());
			auth = getTsmpDpApiAuth2Dao().save(auth);
			d1Resp.getApiAuthIds().add(auth.getApiAuthId());
		}
		
		super.postResubmit(m, q, r, locale, iip);
	}

	@Override
	protected <R extends DpReqServiceResp> void postDoAccept(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo, String locale, InnerInvokeParam iip) {
		super.postDoAccept(q, vo, locale, iip);
		
		updateTsmpDpApiAuth2AfterSign(vo, null, q.getSignUserName());
		
		// 寄發簽核Mail通知
		TsmpAuthorization authorization = new TsmpAuthorization();
		authorization.setUserName(q.getSignUserName());
		getSendReviewMailService().sendEmail(authorization, vo.getM().getReqType(), vo.getM().getReqOrdermId(), vo.getM().getReqOrderNo(), locale);
	}

	@Override
	protected <R extends DpReqServiceResp> void postDoDenied(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo, String locale, InnerInvokeParam iip) {
		super.postDoDenied(q, vo, locale, iip);
		
		sendAPIApplicationResultMail(q, vo, TsmpDpApplyStatus.FAIL.value());
		
		updateTsmpDpApiAuth2AfterSign(vo, TsmpDpApplyStatus.FAIL, q.getSignUserName());
		
		// 寄發簽核Mail通知
		TsmpAuthorization authorization = new TsmpAuthorization();
		authorization.setUserName(q.getSignUserName());
		getSendReviewMailService().sendEmail(authorization, vo.getM().getReqType(), vo.getM().getReqOrdermId(), vo.getM().getReqOrderNo(), locale);
	}

	@Override
	protected <R extends DpReqServiceResp> void postDoEnd(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo, String locale, InnerInvokeParam iip) {
		super.postDoEnd(q, vo, locale, iip);

		updateTsmpDpApiAuth2AfterSign(vo, TsmpDpApplyStatus.FAIL, q.getSignUserName());
		
		// 寄發簽核Mail通知
		TsmpAuthorization authorization = new TsmpAuthorization();
		authorization.setUserName(q.getSignUserName());
		getSendReviewMailService().sendEmail(authorization, vo.getM().getReqType(), vo.getM().getReqOrdermId(), vo.getM().getReqOrderNo(), locale);
	}

	@Override
	protected <R extends DpReqServiceResp> void postDoReturn(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo, String locale, InnerInvokeParam iip) {
		super.postDoReturn(q, vo, locale, iip);
		
		updateTsmpDpApiAuth2AfterSign(vo, TsmpDpApplyStatus.FAIL, q.getSignUserName());
		
		// 寄發簽核Mail通知
		TsmpAuthorization authorization = new TsmpAuthorization();
		authorization.setUserName(q.getSignUserName());
		getSendReviewMailService().sendEmail(authorization, vo.getM().getReqType(), vo.getM().getReqOrdermId(), vo.getM().getReqOrderNo(), locale);
	}

	private TsmpDpClientext getClientExt(String clientId) {
		Optional<TsmpDpClientext> opt = getTsmpDpClientextDao().findById(clientId);
		return opt.orElse(null);
	}

	private boolean isQualified(TsmpApi api, TsmpDpClientext ext) {
		final String clientId = ext.getClientId();
		final String apiUid = api.getApiUid();
		
		Boolean hasApiAuthority = getTsmpDpClientextDao().hasApiAuthority(clientId, apiUid);
		return hasApiAuthority;
	}

	private void checkHasInProgress(Long currentMid, String clientId, String apiUid) {
		List<TsmpDpReqOrderd1> d1List = getTsmpDpReqOrderd1Dao().findByClientIdAndApiUid(clientId, apiUid);
		if (d1List != null && !d1List.isEmpty()) {
			Long reqOrdermId = null;
			TsmpDpReqOrders nextChkPoint = null;
			for (TsmpDpReqOrderd1 d1 : d1List) {
				reqOrdermId = d1.getRefReqOrdermId();
				// 不用比對當前的單
				if (currentMid != null && currentMid.equals(reqOrdermId)) {
					continue;
				}

				nextChkPoint = getTsmpDpReqOrdersDao().queryNextCheckPoint(reqOrdermId);
				if (nextChkPoint != null) {
					this.logger.debug("還有其他正在申請相同API的簽核單: " + reqOrdermId);
					throw TsmpDpAaRtnCode.ERROR_API_APPLIED.throwing();
				}
			}
		}
	}

	private void checkPrevAuth(String refClientId, String refApiUid) {
		List<TsmpDpApiAuth2> prevAuths = getTsmpDpApiAuth2Dao().findByRefClientIdAndRefApiUid(refClientId, refApiUid);
		if (prevAuths == null || prevAuths.isEmpty()) {
			return;
		}
		
		String applyStatus = null;
		for (TsmpDpApiAuth2 prevAuth : prevAuths) {
			if (prevAuth == null) continue;

			applyStatus = prevAuth.getApplyStatus();
			if (TsmpDpApplyStatus.PASS.value().equals(applyStatus)) {
				throw TsmpDpAaRtnCode.ERROR_API_AUTHORIZED.throwing();
			}
			if (TsmpDpApplyStatus.REVIEW.value().equals(applyStatus)) {
				throw TsmpDpAaRtnCode.ERROR_API_APPLIED.throwing();
			}
		}
	}

	// 更新 tsmp_dp_api_auth2 檔, 並組出 tsmp_dp_appt_job.identif_data
	private <R extends DpReqServiceResp> void updateTsmpDpApiAuth2AfterSign(DpReqServiceSignVo<R> vo, //
			TsmpDpApplyStatus applyStatus, String signUserName) {
		StringBuffer sb = new StringBuffer();
		sb.append("reqOrderNo=" + vo.getM().getReqOrderNo());
		
		List<TsmpDpReqOrderd1> d1List = getTsmpDpReqOrderd1Dao().findByRefReqOrdermId(vo.getM().getReqOrdermId());
		if (d1List != null && !d1List.isEmpty()) {
			List<String> innerData = new ArrayList<>();
			
			for(TsmpDpReqOrderd1 d1 : d1List) {
				List<TsmpDpApiAuth2> auth2List = getTsmpDpApiAuth2Dao() //
					.findByRefClientIdAndRefApiUidAndApplyStatus(
						d1.getClientId(), d1.getApiUid(), TsmpDpApplyStatus.REVIEW.value()
					);
				if (auth2List != null && !auth2List.isEmpty()) {
					TsmpApi api = null;
					String data = null;
					for (TsmpDpApiAuth2 auth2 : auth2List) {
						auth2.setRefReviewUser(signUserName);
						auth2.setReviewRemark(vo.getCurrentS().getReqComment());
						if (applyStatus != null) {
							auth2.setApplyStatus(applyStatus.value());
						}
						auth2.setUpdateDateTime(DateTimeUtil.now());
						auth2.setUpdateUser(signUserName);
						auth2 = getTsmpDpApiAuth2Dao().save(auth2);

						data = "{clientId=" + auth2.getRefClientId() + ", apiName=";
						api = getTsmpApi(auth2.getRefApiUid());
						if (api != null) {
							data += api.getApiName();
						}
						data += "}";
						innerData.add(data);
					}
				}
			}
			
			sb.append(", " + innerData);
		}
		
		vo.setIndentifData(sb.toString());
	}

	private TsmpApi getTsmpApi(String apiUid) {
		if (!StringUtils.isEmpty(apiUid)) {
			List<TsmpApi> apiList = getTsmpApiDao().findByApiUid(apiUid);
			if (apiList != null && !apiList.isEmpty()) {
				return apiList.get(0);
			}
		}
		return null;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpDpClientextDao getTsmpDpClientextDao() {
		return this.tsmpDpClientextDao;
	}

	protected TsmpApiExtDao getTsmpApiExtDao() {
		return this.tsmpApiExtDao;
	}

	protected TsmpDpReqOrderd1Dao getTsmpDpReqOrderd1Dao() {
		return this.tsmpDpReqOrderd1Dao;
	}

	protected TsmpDpApiAuth2Dao getTsmpDpApiAuth2Dao() {
		return this.tsmpDpApiAuth2Dao;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected <R extends DpReqServiceResp> void postDoAllAccept(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo) {
		super.postDoAllAccept(q, vo);
		sendAPIApplicationResultMail(q, vo, TsmpDpApplyStatus.PASS.value());
	}

	private <R extends DpReqServiceResp> void sendAPIApplicationResultMail(DpReqServiceSignReq q,
			DpReqServiceSignVo<R> vo, String applyStatus) {
		
		final Long reqOrdermId = vo.getM().getReqOrdermId();
		List<TsmpDpReqOrderd1> d1List = getTsmpDpReqOrderd1Dao().findByRefReqOrdermId(reqOrdermId);
		
		if (d1List != null && !d1List.isEmpty()) {
			
			HashMap<String, List<TsmpDpApiAuth2>> clientAuthMap= new HashMap<String, List<TsmpDpApiAuth2>>();
						
			for (TsmpDpReqOrderd1 d1 : d1List) {
				
				List<TsmpDpApiAuth2> auth2List = getTsmpDpApiAuth2Dao() //
						.findByRefClientIdAndRefApiUid(
							d1.getClientId(), d1.getApiUid());
				
				if (auth2List == null || auth2List.isEmpty()) {
					continue;
				}
				
				List<TsmpDpApiAuth2> authList = null;
				if (clientAuthMap.containsKey(d1.getClientId())) {
					authList = clientAuthMap.get(d1.getClientId());
				}else {
					authList = new ArrayList<TsmpDpApiAuth2>();
					clientAuthMap.put(d1.getClientId(), authList);
				}
				
				for (TsmpDpApiAuth2 auth2 : auth2List) {
					if (TsmpDpApplyStatus.REVIEW.value().equals(auth2.getApplyStatus())) {
						authList.add(auth2);	
					}
				}
			}
			
			for (String clientId : clientAuthMap.keySet()) {
				Optional<TsmpClient> opt_tsmpClient = getTsmpClientDao().findById(clientId);
				if (opt_tsmpClient.isPresent()) {
					
					TsmpClient client = opt_tsmpClient.get();
					List<TsmpDpApiAuth2> authList = clientAuthMap.get(clientId);
					if (authList != null && !authList.isEmpty()) {
						TsmpAuthorization auth = new TsmpAuthorization();
						auth.setUserName(q.getSignUserName());
						
						// 寄發審核通過Mail通知
						getSendAPIApplicationMailService().sendEmail(client,authList,auth,applyStatus, vo.getM().getReqOrdermId());
					}
				}
			}
		}
	}
}
