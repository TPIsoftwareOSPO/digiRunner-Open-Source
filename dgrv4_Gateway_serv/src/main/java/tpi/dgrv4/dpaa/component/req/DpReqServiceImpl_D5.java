package tpi.dgrv4.dpaa.component.req;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpRegStatus;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType;
import tpi.dgrv4.common.constant.TsmpDpSeqStoreKey;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredOpenApiKeyJob;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.dpaa.service.SendAPIApplicationMailService;
import tpi.dgrv4.dpaa.service.SendClientRegMailService;
import tpi.dgrv4.dpaa.service.SendReviewMailService;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpDpClientext;
import tpi.dgrv4.entity.entity.TsmpOpenApiKey;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd5;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd5d;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiExtDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpChkLayerDao;
import tpi.dgrv4.entity.repository.TsmpDpChkLogDao;
import tpi.dgrv4.entity.repository.TsmpDpClientextDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd5Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd5dDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdersDao;
import tpi.dgrv4.entity.repository.TsmpDpThemeCategoryDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyDao;
import tpi.dgrv4.entity.repository.TsmpOpenApiKeyMapDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * 處理"Open Api Key"簽核流程
 * @author Mini
 *
 */
@Service(value = "dpReqServiceImpl_D5")
public class DpReqServiceImpl_D5 extends DpReqServiceAbstract implements DpReqServiceIfs {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpDpReqOrdermDao tsmpDpReqOrdermDao;
	private TsmpDpReqOrderd5Dao tsmpDpReqOrderd5Dao;
	private TsmpDpReqOrderd5dDao tsmpDpReqOrderd5dDao;
	private TsmpApiExtDao tsmpApiExtDao;
	private ApplicationContext ctx;
	private JobHelper jobHelper;
	private TsmpApiDao tsmpApiDao;
	private TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao;
	private TsmpDpClientextDao tsmpDpClientextDao;
	protected TsmpOpenApiKeyDao tsmpOpenApiKeyDao;
	protected TsmpOpenApiKeyMapDao tsmpOpenApiKeyMapDao;
	
	@Autowired
	public DpReqServiceImpl_D5(ApptJobDispatcher apptJobDispatcher, TsmpClientDao tsmpClientDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpDpReqOrdermDao tsmpDpReqOrdermDao, TsmpUserDao tsmpUserDao,
			SeqStoreService seqStoreService, TsmpDpReqOrdersDao tsmpDpReqOrdersDao, FileHelper fileHelper,
			TsmpDpFileDao tsmpDpFileDao, TsmpDpChkLayerDao tsmpDpChkLayerDao, TsmpDpChkLogDao tsmpDpChkLogDao,
			BcryptParamHelper bcryptParamHelper, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpDpApptJobDao tsmpDpApptJobDao, SendReviewMailService mailService,
			SendClientRegMailService sendClientRegMailService,
			SendAPIApplicationMailService sendAPIApplicationMailService, DgrAuditLogService dgrAuditLogService,
			DgrAcIdpUserDao dgrAcIdpUserDao,
			TsmpDpReqOrderd5Dao tsmpDpReqOrderd5Dao, TsmpDpReqOrderd5dDao tsmpDpReqOrderd5dDao,
			TsmpApiExtDao tsmpApiExtDao, ApplicationContext ctx, JobHelper jobHelper, TsmpApiDao tsmpApiDao,
			TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao, TsmpDpClientextDao tsmpDpClientextDao,
			TsmpOpenApiKeyDao tsmpOpenApiKeyDao, TsmpOpenApiKeyMapDao tsmpOpenApiKeyMapDao) {
		super(apptJobDispatcher, tsmpClientDao, tsmpDpItemsCacheProxy, tsmpDpReqOrdermDao, tsmpUserDao, seqStoreService,
				tsmpDpReqOrdersDao, fileHelper, tsmpDpFileDao, tsmpDpChkLayerDao, tsmpDpChkLogDao, bcryptParamHelper,
				tsmpOrganizationDao, tsmpDpApptJobDao, mailService, sendClientRegMailService,
				sendAPIApplicationMailService, dgrAuditLogService, dgrAcIdpUserDao);
		this.tsmpDpReqOrdermDao = tsmpDpReqOrdermDao;
		this.tsmpDpReqOrderd5Dao = tsmpDpReqOrderd5Dao;
		this.tsmpDpReqOrderd5dDao = tsmpDpReqOrderd5dDao;
		this.tsmpApiExtDao = tsmpApiExtDao;
		this.ctx = ctx;
		this.jobHelper = jobHelper;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpDpThemeCategoryDao = tsmpDpThemeCategoryDao;
		this.tsmpDpClientextDao = tsmpDpClientextDao;
		this.tsmpOpenApiKeyDao = tsmpOpenApiKeyDao;
		this.tsmpOpenApiKeyMapDao = tsmpOpenApiKeyMapDao;
	}

	@Override
	protected <Q extends DpReqServiceSaveDraftReq> void checkDetailReq(Q q, String locale) throws TsmpDpAaException {
		// 一定要先轉型
		DpReqServiceSaveDraftReq_D5 req = castSaveDraftReq(q, DpReqServiceSaveDraftReq_D5.class);
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
		
		final String _clientId = req.get_clientId();
		if (StringUtils.isEmpty(_clientId)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		// clientId不存在
		if (!getTsmpClientDao().findById(_clientId).isPresent()) {
			throw TsmpDpAaRtnCode.NO_MEMBER_INFO.throwing();
		}
		
		// 取出 clientExt
		TsmpDpClientext clientExt = getClientExt(_clientId);
		if (clientExt == null) {
			throw TsmpDpAaRtnCode.NO_MEMBER_INFO.throwing();
		}
		
		// client必須已放行
		if (!TsmpDpRegStatus.PASS.value().equals(clientExt.getRegStatus())) {
			throw TsmpDpAaRtnCode._1228.throwing();
		}
		
		List<String> apiUids = req.getApiUids();
		if (apiUids == null || apiUids.isEmpty()) {
			this.logger.error("沒有傳入參數: apiUids");
			throw TsmpDpAaRtnCode.NO_APPLIED_API.throwing();
		}

		// 詳細檢查每筆API
		List<TsmpApi> apiList = null;
		for(String apiUid : apiUids) {
			apiList = getTsmpApiDao().findByApiUid(apiUid);
			// 檢查API是否存在
			if (apiList == null || apiList.isEmpty()) {
				this.logger.error("找不到API: " + apiUid);
				throw TsmpDpAaRtnCode.NO_APPLIED_API.throwing();
			}
		}
		
		String reqSubtype = req.getReqSubtype();
 
		// 依照簽核類型做不同檢查
		if (TsmpDpReqReviewType.OPEN_API_KEY.OPEN_API_KEY_UPDATE.isValueEquals(reqSubtype)
				|| TsmpDpReqReviewType.OPEN_API_KEY.OPEN_API_KEY_REVOKE.isValueEquals(reqSubtype)) {
			Long openApiKeyId = req.getOpenApiKeyId();
			String openApiKey = req.getOpenApiKey();
			String secretKey = req.getSecretKey();
			
			if (StringUtils.isEmpty(openApiKeyId)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			if (StringUtils.isEmpty(openApiKey)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			if (StringUtils.isEmpty(secretKey)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
		}
 
		String openApiKeyAlias = req.getOpenApiKeyAlias();
		Integer timesThreshold = req.getTimesThreshold();
		String expiredAt = req.getExpiredAt();
		if (StringUtils.isEmpty(openApiKeyAlias)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		if (StringUtils.isEmpty(timesThreshold)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		if (StringUtils.isEmpty(expiredAt)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	@Override
	protected <R extends DpReqServiceResp, Q extends DpReqServiceSaveDraftReq> void saveDetail( //
			TsmpDpReqOrderm m, Q q, R r, InnerInvokeParam iip) {
		// 一定要先轉型
		DpReqServiceSaveDraftReq_D5 req = castSaveDraftReq(q, DpReqServiceSaveDraftReq_D5.class);
		DpReqServiceResp_D5 resp = castResp(r, DpReqServiceResp_D5.class);
		
		List<String> apiUids = req.getApiUids();
		
		// 儲存D5	
		TsmpDpReqOrderd5 d5 = saveD5(req.get_clientId(), m.getReqOrdermId(), req.getOpenApiKeyId(), req.getOpenApiKey(), 
				req.getOpenApiKeyAlias(), req.getTimesThreshold(), req.getExpiredAt(), m.getCreateUser());
		
		// 儲存D5D, 存入 mapping table		
		List<Long> reqOrderd5dIds = new ArrayList<>();
		for (String apiUid : apiUids) {
			TsmpDpReqOrderd5d d5d = new TsmpDpReqOrderd5d();
			d5d.setRefReqOrderd5Id(d5.getReqOrderd5Id());
			d5d.setRefApiUid(apiUid);
			Long reqOrderd5dId = getSeqStoreService().nextSequence(TsmpDpSeqStoreKey.TSMP_DP_REQ_ORDERD5D);
			d5d.setReqOrderd5dId(reqOrderd5dId);
			d5d.setCreateDateTime(DateTimeUtil.now());
			d5d.setCreateUser(m.getCreateUser());
			d5d = getTsmpDpReqOrderd5dDao().save(d5d);
			reqOrderd5dIds.add(d5d.getReqOrderd5dId());
		}
		resp.setReqOrderd5Id(d5.getReqOrderd5Id());
		resp.setReqOrderd5dIds(reqOrderd5dIds);
	}

	@Override
	protected void deleteDraftDetail(TsmpDpReqOrderm m) throws TsmpDpAaException {
		Long refReqOrdermId = m.getReqOrdermId();
		List<TsmpDpReqOrderd5> d5List = getTsmpDpReqOrderd5Dao().findByRefReqOrdermId(refReqOrdermId);
		if (!CollectionUtils.isEmpty(d5List)) {
			d5List.forEach((d5) -> {
				// 刪除 TSMP_DP_REQ_ORDERD5D
				Long refReqOrderd5Id = d5.getReqOrderd5Id();
				List<TsmpDpReqOrderd5d> d5dList = getTsmpDpReqOrderd5dDao().findByRefReqOrderd5Id(refReqOrderd5Id);
				if (!CollectionUtils.isEmpty(d5dList)) {
					getTsmpDpReqOrderd5dDao().deleteAll(d5dList);
				}
				// 刪除 TSMP_DP_REQ_ORDERD5
				getTsmpDpReqOrderd5Dao().delete(d5);
			});
		}
	}
  
	@Override
	protected <Q extends DpReqServiceUpdateReq> void checkDetailUpdateReq(Q q, String locale) throws TsmpDpAaException {
		DpReqServiceUpdateReq_D5 req = castUpdateReq(q, DpReqServiceUpdateReq_D5.class);
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
		
		final String clientId = req.get_clientId();
		if (StringUtils.isEmpty(clientId)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		// clientId不存在
		if (!getTsmpClientDao().findById(clientId).isPresent()) {
			throw TsmpDpAaRtnCode.NO_MEMBER_INFO.throwing();
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
		
		List<String> apiUids = req.getApiUids();
		if (apiUids == null || apiUids.isEmpty()) {
			this.logger.error("沒有傳入參數: apiUids");
			throw TsmpDpAaRtnCode.NO_APPLIED_API.throwing();
		}

		// 詳細檢查每筆API
		List<TsmpApi> apiList = null;
		for(String apiUid : apiUids) {
			apiList = getTsmpApiDao().findByApiUid(apiUid);
			// 檢查API是否存在
			if (apiList == null || apiList.isEmpty()) {
				this.logger.error("找不到API: " + apiUid);
				throw TsmpDpAaRtnCode.NO_APPLIED_API.throwing();
			}
		}
		
		Long reqOrdermId = req.getReqOrdermId();
		TsmpDpReqOrderm m = getTsmpDpReqOrderm(reqOrdermId);
		String reqSubtype = "";
		if(m != null) {
			reqSubtype = m.getReqSubtype();
		}
 
		// 依照簽核類型做不同檢查
		if (TsmpDpReqReviewType.OPEN_API_KEY.OPEN_API_KEY_UPDATE.isValueEquals(reqSubtype)
				|| TsmpDpReqReviewType.OPEN_API_KEY.OPEN_API_KEY_REVOKE.isValueEquals(reqSubtype)) {
			Long openApiKeyId = req.getOpenApiKeyId();
			String openApiKey = req.getOpenApiKey();
			String secretKey = req.getSecretKey();
			
			if (StringUtils.isEmpty(openApiKeyId)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			if (StringUtils.isEmpty(openApiKey)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			if (StringUtils.isEmpty(secretKey)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
		}
		
		String openApiKeyAlias = req.getOpenApiKeyAlias();
		Integer timesThreshold = req.getTimesThreshold();
		String expiredAt = req.getExpiredAt();
		if (StringUtils.isEmpty(openApiKeyAlias)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		if (StringUtils.isEmpty(timesThreshold)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		if (StringUtils.isEmpty(expiredAt)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	@Override
	protected <R extends DpReqServiceResp, Q extends DpReqServiceUpdateReq> void updateDetail(TsmpDpReqOrderm m, Q q,
			R r, InnerInvokeParam iip) {
		DpReqServiceUpdateReq_D5 req = castUpdateReq(q, DpReqServiceUpdateReq_D5.class);
		DpReqServiceResp_D5 resp = castResp(r, DpReqServiceResp_D5.class);
		
		// 重建新資料		
		List<String> apiUids = req.getApiUids();
		
		// 舊的D5資料
		List<TsmpDpReqOrderd5> oldD5List = getTsmpDpReqOrderd5Dao().findByRefReqOrdermId(m.getReqOrdermId());
		
		// 儲存D5	
		TsmpDpReqOrderd5 d5 = saveD5(req.get_clientId(), m.getReqOrdermId(), req.getOpenApiKeyId(), req.getOpenApiKey(), 
				req.getOpenApiKeyAlias(), req.getTimesThreshold(), req.getExpiredAt(), m.getCreateUser());
		
		// 儲存D5D, 存入 mapping table		
		List<Long> reqOrderd5dIds = new ArrayList<>();
		for (String apiUid : apiUids) {
			TsmpDpReqOrderd5d d5d = new TsmpDpReqOrderd5d();
			d5d.setRefReqOrderd5Id(d5.getReqOrderd5Id());
			d5d.setRefApiUid(apiUid);
			Long reqOrderd5dId = getSeqStoreService().nextSequence(TsmpDpSeqStoreKey.TSMP_DP_REQ_ORDERD5D);
			d5d.setReqOrderd5dId(reqOrderd5dId);
			d5d.setCreateDateTime(DateTimeUtil.now());
			d5d.setCreateUser(m.getCreateUser());
			d5d = getTsmpDpReqOrderd5dDao().save(d5d);
			reqOrderd5dIds.add(d5d.getReqOrderd5dId());
		}
		resp.setReqOrderd5Id(d5.getReqOrderd5Id());
		resp.setReqOrderd5dIds(reqOrderd5dIds);
		
		// 清空舊明細資料
		removeOldD5(oldD5List);
	}

	@Override
	protected <R extends DpReqServiceResp> void postDoAccept(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo, String locale, InnerInvokeParam iip) {
		super.postDoAccept(q, vo, locale, iip);

		// 寄發簽核Mail通知
		TsmpAuthorization auth = new TsmpAuthorization();
		auth.setUserName(q.getSignUserName());
		getSendReviewMailService().sendEmail(auth, vo.getM().getReqType(), vo.getM().getReqOrdermId(), vo.getM().getReqOrderNo(), locale);
	}

	/**
	 * D5 不像 D1、D3 在 postDoAccept() 方法中組成 identif_data，<br>
	 * 是因為它們都需要在"同意"後更新其它明細表(ex: tsmp_dp_api_auth2)<br>
	 * 所以才"順便"組資料, 減少資料庫存取次數
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected <R extends DpReqServiceResp> void postDoAllAccept(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo) {
		StringBuffer sb = new StringBuffer();
		sb.append("reqOrderNo=" + vo.getM().getReqOrderNo());

		final Long reqOrdermId = vo.getM().getReqOrdermId();
		List<TsmpDpReqOrderd5> d5List = getTsmpDpReqOrderd5Dao().findByRefReqOrdermId(reqOrdermId);
		if (d5List != null && !d5List.isEmpty()) {
			List<String> innerData = new ArrayList<>();
			
			for(TsmpDpReqOrderd5 d5 : d5List) {
				List<TsmpDpReqOrderd5d> d5dList = getTsmpDpReqOrderd5dDao().findByRefReqOrderd5Id(d5.getReqOrderd5Id());
				if (d5dList != null && !d5dList.isEmpty()) {
					for (TsmpDpReqOrderd5d d5d : d5dList) {
						TsmpApi api = getTsmpApi(d5d.getRefApiUid());
						if (api != null) {
							innerData.add(api.getApiName());
						}
					}
				}
			}
			
			sb.append(", apiName=" + innerData);
		}
		
		vo.setIndentifData(sb.toString());
		
		// 刪除過期的 Open API Key
		deleteExpiredOpenApiKey();
		
		super.postDoAllAccept(q, vo);
	}

	@Override
	protected <R extends DpReqServiceResp> void postDoDenied(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo, String locale, InnerInvokeParam iip) {
		super.postDoDenied(q, vo, locale, iip);

		// 寄發簽核Mail通知
		TsmpAuthorization auth = new TsmpAuthorization();
		auth.setUserName(q.getSignUserName());
		getSendReviewMailService().sendEmail(auth, vo.getM().getReqType(), vo.getM().getReqOrdermId(), vo.getM().getReqOrderNo(), locale);
		
		// 刪除過期的 Open API Key
		deleteExpiredOpenApiKey();
	}

	@Override
	protected <R extends DpReqServiceResp> void postDoReturn(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo, String locale, InnerInvokeParam iip) {
		super.postDoReturn(q, vo, locale, iip);

		// 寄發簽核Mail通知
		TsmpAuthorization auth = new TsmpAuthorization();
		auth.setUserName(q.getSignUserName());
		getSendReviewMailService().sendEmail(auth, vo.getM().getReqType(), vo.getM().getReqOrdermId(), vo.getM().getReqOrderNo(), locale);
	}

	@Override
	protected <R extends DpReqServiceResp> void postDoEnd(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo, String locale, InnerInvokeParam iip) {
		super.postDoEnd(q, vo, locale, iip);

		// 寄發簽核Mail通知
		TsmpAuthorization auth = new TsmpAuthorization();
		auth.setUserName(q.getSignUserName());
		getSendReviewMailService().sendEmail(auth, vo.getM().getReqType(), vo.getM().getReqOrdermId(), vo.getM().getReqOrderNo(), locale);
	}
 
	private TsmpDpReqOrderd5 saveD5(String clientId, Long reqOrdermId, Long openApiKeyId, String openApiKey, 
			String openApiKeyAlias, int timesThreshold, String expiredAt, String createUser) {
		Optional<Date> opt_date = DateTimeUtil.stringToDateTime(expiredAt, DateTimeFormatEnum.西元年月日_2);// yyyy/MM/dd
		if (!opt_date.isPresent()) {
			throw TsmpDpAaRtnCode._1295.throwing();
		}		
		
		/*
		 *	申請單上的 Secret Key 已被中間隱藏加星號(*),要重新由Table取得
		 */
		String secretKey = "";
		if(openApiKeyId != null) {
			Optional<TsmpOpenApiKey> opt_oak = getTsmpOpenApiKeyDao().findById(openApiKeyId);
			if (!opt_oak.isPresent()) {
				throw TsmpDpAaRtnCode._1297.throwing();
			}
			TsmpOpenApiKey oak = opt_oak.get();
			secretKey = oak.getSecretKey();
		}
		
		TsmpDpReqOrderd5 d5 = new TsmpDpReqOrderd5();
		d5.setClientId(clientId);
		d5.setRefReqOrdermId(reqOrdermId);
		d5.setRefOpenApiKeyId(openApiKeyId);
		d5.setOpenApiKey(openApiKey);
		d5.setSecretKey(secretKey);
		d5.setOpenApiKeyAlias(openApiKeyAlias);
		d5.setTimesThreshold(timesThreshold);
		d5.setExpiredAt(getExpiredDateLong(opt_date.get().getTime()));
		d5.setCreateDateTime(DateTimeUtil.now());
		d5.setCreateUser(createUser);
		d5 = getTsmpDpReqOrderd5Dao().save(d5);
		return d5;
	}

	/**
	 * 存入的效期 = 畫面的效期日+1Day-1ns; 
	 * ex: 效期為2020/9/7, EXPIRED_AT = 2020/9/8 減1納秒
	 * 
	 * @param value
	 */
	private static long getExpiredDateLong(long value) {
		Date date = new Date(value);
		Date date2 = plusDay(date, 1);//加1天
		Date date3 = minusNanos(date2, 1);//減1納秒(10的負9次方秒)
		return date3.getTime();
	}

	/*
	 * 加1天
	 */
	private static Date plusDay(Date dt, long days) {
		LocalDateTime ldt = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		ldt = ldt.plusDays(days);
		return (Date) Date.from( ldt.atZone(ZoneId.systemDefault()).toInstant() );
	}
	
	/*
	 * 減1納秒(10的負9次方秒)
	 */
	private static Date minusNanos(Date dt, long ns) {
		LocalDateTime ldt = dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		ldt = ldt.minusNanos(ns);
		return (Date) Date.from( ldt.atZone(ZoneId.systemDefault()).toInstant() );
	}
	
	private void removeOldD5(List<TsmpDpReqOrderd5> oldD5List) {
		if (oldD5List == null || oldD5List.isEmpty()) {
			return;
		}

		Long oldD5Id = null;
		for(TsmpDpReqOrderd5 oldD5 : oldD5List) {
			oldD5Id = oldD5.getReqOrderd5Id();

			// #1. 刪除舊的D5d
			List<TsmpDpReqOrderd5d> oldD5dList = getTsmpDpReqOrderd5dDao().findByRefReqOrderd5Id(oldD5Id);
			if (oldD5dList != null) {
				getTsmpDpReqOrderd5dDao().deleteAll(oldD5dList);
			}
			// #2. 刪除舊的D5
			getTsmpDpReqOrderd5Dao().delete(oldD5);
		}
	}
	
	private TsmpDpReqOrderm getTsmpDpReqOrderm(Long reqOrdermId) {
		if (!StringUtils.isEmpty(reqOrdermId)) {
			Optional<TsmpDpReqOrderm> opt = getTsmpDpReqOrdermDao().findById(reqOrdermId);
			if (opt.isPresent()) {
				return opt.get();
			}
		}
		return null;
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
	
	public DeleteExpiredOpenApiKeyJob deleteExpiredOpenApiKey() {
		DeleteExpiredOpenApiKeyJob job = (DeleteExpiredOpenApiKeyJob) getCtx().getBean("deleteExpiredOpenApiKeyJob");
		getJobHelper().add(job);
		return job;
	}
	
	protected TsmpDpReqOrdermDao getTsmpDpReqOrdermDao() {
		return this.tsmpDpReqOrdermDao;
	}
	
	protected TsmpDpReqOrderd5Dao getTsmpDpReqOrderd5Dao() {
		return this.tsmpDpReqOrderd5Dao;
	}

	protected TsmpDpReqOrderd5dDao getTsmpDpReqOrderd5dDao() {
		return this.tsmpDpReqOrderd5dDao;
	}

	protected TsmpApiExtDao getTsmpApiExtDao() {
		return this.tsmpApiExtDao;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}
 
	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpDpThemeCategoryDao getTsmpDpThemeCategoryDao() {
		return this.tsmpDpThemeCategoryDao;
	}

	private TsmpDpClientext getClientExt(String clientId) {
		Optional<TsmpDpClientext> opt = getTsmpDpClientextDao().findById(clientId);
		return opt.orElse(null);
	}

	protected TsmpDpClientextDao getTsmpDpClientextDao() {
		return this.tsmpDpClientextDao;
	}
	
	protected TsmpOpenApiKeyDao getTsmpOpenApiKeyDao() {
		return this.tsmpOpenApiKeyDao;
	}
}
