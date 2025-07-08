package tpi.dgrv4.dpaa.component.req;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.*;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.dpaa.service.SendAPIApplicationMailService;
import tpi.dgrv4.dpaa.service.SendClientRegMailService;
import tpi.dgrv4.dpaa.service.SendReviewMailService;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.*;
import tpi.dgrv4.entity.entity.jpql.TsmpDpChkLog;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrders;
import tpi.dgrv4.entity.repository.*;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class DpReqServiceAbstract implements DpReqServiceIfs {

	private TPILogger logger = TPILogger.tl;

//	@Autowired(required=false)
	private ApptJobDispatcher apptJobDispatcher;
	
	private TsmpClientDao tsmpClientDao;
	protected TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private TsmpDpReqOrdermDao tsmpDpReqOrdermDao;
	private TsmpUserDao tsmpUserDao;
	private SeqStoreService seqStoreService;
	private TsmpDpReqOrdersDao tsmpDpReqOrdersDao;
	private FileHelper fileHelper;
	private TsmpDpFileDao tsmpDpFileDao;
	private TsmpDpChkLayerDao tsmpDpChkLayerDao;
	private TsmpDpChkLogDao tsmpDpChkLogDao;
	private BcryptParamHelper bcryptParamHelper;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpDpApptJobDao tsmpDpApptJobDao;
	private SendReviewMailService mailService;
	private SendClientRegMailService sendClientRegMailService;
	private SendAPIApplicationMailService sendAPIApplicationMailService;
	private DgrAuditLogService dgrAuditLogService;
	private DgrAcIdpUserDao dgrAcIdpUserDao;

	@Autowired
	public DpReqServiceAbstract(@Nullable ApptJobDispatcher apptJobDispatcher, TsmpClientDao tsmpClientDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpDpReqOrdermDao tsmpDpReqOrdermDao, TsmpUserDao tsmpUserDao,
			SeqStoreService seqStoreService, TsmpDpReqOrdersDao tsmpDpReqOrdersDao, FileHelper fileHelper,
			TsmpDpFileDao tsmpDpFileDao, TsmpDpChkLayerDao tsmpDpChkLayerDao, TsmpDpChkLogDao tsmpDpChkLogDao,
			BcryptParamHelper bcryptParamHelper, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpDpApptJobDao tsmpDpApptJobDao, SendReviewMailService mailService,
			SendClientRegMailService sendClientRegMailService,
			SendAPIApplicationMailService sendAPIApplicationMailService, DgrAuditLogService dgrAuditLogService,
			DgrAcIdpUserDao dgrAcIdpUserDao) {
		super();
		this.apptJobDispatcher = apptJobDispatcher;
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.tsmpDpReqOrdermDao = tsmpDpReqOrdermDao;
		this.tsmpUserDao = tsmpUserDao;
		this.seqStoreService = seqStoreService;
		this.tsmpDpReqOrdersDao = tsmpDpReqOrdersDao;
		this.fileHelper = fileHelper;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.tsmpDpChkLayerDao = tsmpDpChkLayerDao;
		this.tsmpDpChkLogDao = tsmpDpChkLogDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpDpApptJobDao = tsmpDpApptJobDao;
		this.mailService = mailService;
		this.sendClientRegMailService = sendClientRegMailService;
		this.sendAPIApplicationMailService = sendAPIApplicationMailService;
		this.dgrAuditLogService = dgrAuditLogService;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
	}

	@Override
	public <R extends DpReqServiceResp, Q extends DpReqServiceSaveDraftReq> R saveDraft(Q req, //
			Class<R> rClass, String locale, InnerInvokeParam iip) throws TsmpDpAaException {
		// 檢查共同參數
		checkSaveDraftReq(req, locale);
		
		// 檢查明細參數
		checkDetailReq(req, locale);
		
		// 初始化回傳參數
		R resp = initResponse(rClass);
		
		// 建立申請單主檔
		TsmpDpReqOrderm m = saveM(req, locale);
		resp.setReqOrdermId(m.getReqOrdermId());
		resp.setLv(m.getVersion());
		
		// 建立申請單明細檔
		saveDetail(m, req, resp, iip);

		// 紀錄審核狀態
		List<TsmpDpReqOrders> sList = saveS(m);
		resp.setsIds(sList.stream().map((s) -> {return s.getReqOrdersId();}).collect(Collectors.toList()));

		// 儲存申請單附件
		saveAttachments(m, req, resp);

		// 後續處理
		postSaveDraft(m, req, resp, locale);
		
		return resp;
	}

	@Override
	public void deleteDraft(final Long reqOrdermId) throws TsmpDpAaException {
		if (reqOrdermId == null) {
			this.logger.debug("reqOrdermId is required!");
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		// 確認是否為可刪除的草稿
		TsmpDpReqOrderm m = checkDeleteDraft(reqOrdermId);
		
		// 刪除明細檔 (D)
		deleteDraftDetail(m);

		// 刪除審核歷程檔 (LOG) (如果是草稿，歷程檔應該不會有資料)
		List<TsmpDpChkLog> logList = getTsmpDpChkLogDao().findByReqOrdermIdOrderByCreateDateTime(reqOrdermId);
		if (!CollectionUtils.isEmpty(logList)) {
			logList.forEach((log) -> {
				getTsmpDpChkLogDao().delete(log);
			});
		}

		// 刪除審核狀態檔 (S)
		getTsmpDpReqOrdersDao().deleteByReqOrdermId(reqOrdermId);
		
		// 刪除申請單附件
		deleteAttachments(m);
		
		// 刪除主檔 (M)
		getTsmpDpReqOrdermDao().delete(m);
	}

	@Override
	public <R extends DpReqServiceResp, Q extends DpReqServiceUpdateReq> R update(Q req, Class<R> rClass, String locale, InnerInvokeParam iip)
			throws TsmpDpAaException {
		
		if(DpReqServiceResp_D3.class.equals(rClass)) {
			//寫入 Audit Log M
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_CLIENT.value());
		}
		
		// 檢查共同參數
		checkUpdateReq(req);
		
		// 確認申請單主檔是否存在
		final Long reqOrdermId = req.getReqOrdermId();
		TsmpDpReqOrderm m = getTsmpDpReqOrdermDao().findById(reqOrdermId).orElse(null);
		if (m == null) {
			throw TsmpDpAaRtnCode._1217.throwing();
		}

		// 檢查是否可更新
		TsmpDpReqOrders applierS = checkUpdatable(reqOrdermId);
		
		// 檢查明細參數
		checkDetailUpdateReq(req, locale);
		
		// 初始化回傳參數
		R resp = initResponse(rClass);
		
		// 更新主檔
		m = updateM(m, req, resp);
		resp.setReqOrdermId(m.getReqOrdermId());
		resp.setLv(m.getVersion());
		
		// 更新明細檔
		updateDetail(m, req, resp, iip);

		// 更新審核狀態
		applierS = updateS(m, applierS, resp);
		resp.setsIds(Arrays.asList(new Long[] {applierS.getReqOrdersId()}));
		
		// 更新申請單附件
		updateAttachments(m, req, resp);

		// 後續處理
		postUpdate(m, req, resp);
		
		return resp;
	}

	@Override
	public boolean isUpdatable(Long reqOrdermId) {
		try {
			checkUpdatable(reqOrdermId);
			return true;
		} catch (TsmpDpAaException e) {}
		return false;
	}

	@Override
	public <R extends DpReqServiceResp> R submit(DpReqServiceUpdateReq req, Class<R> rClass, String locale, InnerInvokeParam iip) throws TsmpDpAaException {
		// 檢查共同參數
		checkSubmitReq(req);
		
		// 確認申請單主檔是否存在
		final Long reqOrdermId = req.getReqOrdermId();
		TsmpDpReqOrderm m = getTsmpDpReqOrdermDao().findById(reqOrdermId).orElse(null);
		if (m == null) {
			throw TsmpDpAaRtnCode._1217.throwing();
		}
		
		// 檢查是否可送審
		TsmpDpReqOrders applierS = checkSubmittable(reqOrdermId);
		
		// 初始化回傳參數
		R resp = initResponse(rClass);
		resp.setReqOrdermId(m.getReqOrdermId());
		resp.setLv(m.getVersion());

		// 更新審核狀態檔
		applierS.setReviewStatus(TsmpDpReqReviewStatus.ACCEPT.value());
		applierS.setProcFlag(null);
		applierS.setUpdateDateTime(DateTimeUtil.now());
		applierS.setUpdateUser(req.getUpdateUser());
		applierS = getTsmpDpReqOrdersDao().save(applierS);
		List<TsmpDpReqOrders> sList = createS(m, applierS, //
				TsmpDpReqReviewStatus.WAIT1, req.getUpdateUser(), false, false);
		resp.setsIds(
			sList.stream().map((orders) -> {
				return orders.getReqOrdersId();
			}).collect(Collectors.toList())
		);
		
		// 新增審核歷程
		saveChkLog(sList, resp);

		// 後續處理
		postSubmit(m, req, resp, locale, iip);

		return resp;
	}

	@Override
	public boolean isSubmittable(Long reqOrdermId) {
		try {
			checkSubmittable(reqOrdermId);
			return true;
		} catch (TsmpDpAaException e) {}
		return false;
	}

	@Override
	public <R extends DpReqServiceResp, Q extends DpReqServiceUpdateReq> R resubmit(Q req, Class<R> rClass, String locale, InnerInvokeParam iip)
			throws TsmpDpAaException {
		
		if(DpReqServiceResp_D3.class.equals(rClass)) {
			//寫入 Audit Log M
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_CLIENT.value());
		}
		
		// 檢查共同參數
		checkResubmitReq(req);

		// 確認申請單主檔是否存在
		final Long reqOrdermId = req.getReqOrdermId();
		TsmpDpReqOrderm m = getTsmpDpReqOrdermDao().findById(reqOrdermId).orElse(null);
		if (m == null) {
			throw TsmpDpAaRtnCode._1217.throwing();
		}

		// 檢查是否可重送
		checkResubmittable(reqOrdermId);

		// 檢查明細參數
		checkDetailResubmitReq(req, locale);

		// 初始化回傳參數
		R resp = initResponse(rClass);

		// 更新主檔
		m = updateM(m, req, resp);
		resp.setReqOrdermId(m.getReqOrdermId());
		resp.setLv(m.getVersion());

		// 更新明細檔
		updateDetail(m, req, resp, iip);

		// 更新申請單附件
		updateAttachments(m, req, resp);

		// 更新審核狀態
		List<TsmpDpReqOrders> sList = createS(m, null, //
				TsmpDpReqReviewStatus.WAIT2, req.getUpdateUser(), true, true);
		resp.setsIds(
			sList.stream().map((orders) -> {
				return orders.getReqOrdersId();
			}).collect(Collectors.toList())
		);

		// 新增審核歷程
		saveChkLog(sList, resp);

		// 後續處理 (寄發郵件)
		postResubmit(m, req, resp, locale, iip);
		
		return resp;
	}

	@Override
	public boolean isResubmittable(Long reqOrdermId) {
		try {
			checkResubmittable(reqOrdermId);
			return true;
		} catch (TsmpDpAaException e) {}
		return false;
	}

	@Override
	public <R extends DpReqServiceResp> R doSign(DpReqServiceSignReq q, Class<R> rClass, String locale, InnerInvokeParam iip) throws TsmpDpAaException {
		if(DpReqServiceResp_D3.class.equals(rClass)) {
			//寫入 Audit Log M
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_CLIENT.value());
		}
		
		// 檢查參數、申請單狀態、使用者權限...
		DpReqServiceSignVo<R> vo = checkSignReq(q, rClass, locale);
		// 依照簽核動作執行不同的pre/post處理
		final String nextReviewStatus = q.getEncNextReviewStatus();	// 經過checkSignReq之後, 此欄位已被解密
		if (TsmpDpReqReviewStatus.ACCEPT.value().equals(nextReviewStatus)) {
			preDoAccept(q, vo);
			doSign(q, vo, rClass);	// 更新簽核狀態、歷程
			postDoAccept(q, vo, locale, iip);
			if (vo.getNextChkPoint() == null) {	// 如果現在通過的這一關是最後一關
				postDoAllAccept(q, vo);
			}
		} else if (TsmpDpReqReviewStatus.DENIED.value().equals(nextReviewStatus)) {
			preDoDenied(q, vo);
			doSign(q, vo, rClass);	// 更新簽核狀態、歷程
			postDoDenied(q, vo, locale, iip);
		} else if (TsmpDpReqReviewStatus.RETURN.value().equals(nextReviewStatus)) {
			preDoReturn(q, vo);
			doSign(q, vo, rClass);	// 更新簽核狀態、歷程
			postDoReturn(q, vo, locale, iip);
		} else if (TsmpDpReqReviewStatus.END.value().equals(nextReviewStatus)) {
			preDoEnd(q, vo);
			doSign(q, vo, rClass);	// 更新簽核狀態、歷程
			postDoEnd(q, vo, locale, iip);
		} else {
			this.logger.error("錯誤的簽核操作: " + nextReviewStatus);
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return vo.getResp();
	}

	@Override
	public boolean isEndable(Long reqOrdermId) {
		return getTsmpDpReqOrdersDao().isEndable(reqOrdermId);
	}

	@Override
	public boolean isSignable(Long reqOrdermId) {
		TsmpDpReqOrders nextChkPoint = getTsmpDpReqOrdersDao().queryNextCheckPoint(reqOrdermId);
		return (nextChkPoint != null);
	}

	private <Q extends DpReqServiceSaveDraftReq> void checkSaveDraftReq(Q req, String locale) {
		final String clientId = req.getClientId();
		final String reqType = req.getReqType();	// 簽核類別
		final String reqDesc = req.getReqDesc();
		if (
			StringUtils.isEmpty(clientId) ||
			StringUtils.isEmpty(reqType)
		) {
			throw TsmpDpAaRtnCode._1213.throwing();
		}
		
		if (StringUtils.isEmpty(reqDesc)){
			throw TsmpDpAaRtnCode.REQUIRED_APPLY_DESC.throwing();
		}
		
		TsmpDpItems reviewType = getItemsById(TsmpDpReqReviewType.ITEM_NO, reqType, false, locale);
		if (reviewType == null) {
			throw TsmpDpAaRtnCode.NO_ITEMS_DATA.throwing();
		}
	}

	private <R extends DpReqServiceResp> R initResponse(Class<R> clazz) {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			this.logger.error("初始化回傳參數失敗!");
			throw TsmpDpAaRtnCode._1213.throwing();
		}
	}

	private <Q extends DpReqServiceSaveDraftReq> TsmpDpReqOrderm saveM(Q req, String locale) {
		final String reqType = req.getReqType();
		final String reqSubtype = req.getReqSubtype();
		final String clientId = req.getClientId();
		final String orgId = req.getOrgId();
		final String reqDesc = req.getReqDesc();
		final String userName = req.getCreateUser();
		final String reqUserId = getUserId(req.getReqUserId(), userName);
		final String effectiveDateStr = req.getEffectiveDate();
		final Date effectiveDate = getEffectiveDate(effectiveDateStr);
		
		TsmpDpReqOrderm m = new TsmpDpReqOrderm();
		setReqOrderNo(m, reqType, reqSubtype, locale);
		m.setReqType(reqType);
		m.setReqSubtype(reqSubtype);
		m.setClientId(clientId);
		m.setOrgId(orgId);
		m.setReqDesc(reqDesc);
		m.setReqUserId(reqUserId);
		m.setEffectiveDate(effectiveDate);
		m.setCreateUser(userName);
		m = getTsmpDpReqOrdermDao().save(m);
		return m;
	}

	private List<TsmpDpReqOrders> saveS(TsmpDpReqOrderm m) {
		List<TsmpDpReqOrders> sList = new ArrayList<>();
		TsmpDpReqOrders s = new TsmpDpReqOrders();
		s.setReqOrdermId(m.getReqOrdermId());
		s.setLayer(0);
		s.setReqComment(new String());
		s.setReviewStatus(TsmpDpReqReviewStatus.WAIT1.value());
		s.setProcFlag(1);
		s.setCreateUser( m.getReqUserId());
		s = getTsmpDpReqOrdersDao().save(s);
		sList.add(s);
		return sList;
	}

	private <R extends DpReqServiceResp, Q extends DpReqServiceSaveDraftReq> void saveAttachments( //
			TsmpDpReqOrderm m, Q q, R r) {
		List<String> tempFileNames = q.getTempFileNames();
		if (tempFileNames == null || tempFileNames.isEmpty()) {
			return;
		}

		uploadAttachments(m.getReqOrdermId(), m.getCreateUser(), tempFileNames, r);
	}

	private <R extends DpReqServiceResp> void uploadAttachments(Long mId, String uploader, //
			List<String> tempFileNames, R r) {
		List<Long> dpFileIds = new ArrayList<>();
		List<String> failToUploads = new ArrayList<>();

		// 如果有上傳附件, 則refId參考reqOrdermId
		//String tsmpDpFilePath = FileHelper.getTsmpDpFilePath(TsmpDpFileType.M_ATTACHMENT, mId);
		//Path file = null;
		TsmpDpFile dpFile = null;
		for(String tempFileName : tempFileNames) {
			try {
				dpFile = getFileHelper().moveTemp(uploader, TsmpDpFileType.M_ATTACHMENT, mId, tempFileName, true, false, true);
				if (dpFile == null) {
					this.logger.error(String.format("上傳申請單附件失敗: %d-%s", mId, tempFileName));
					failToUploads.add(tempFileName);
					continue;
				}
				dpFileIds.add(dpFile.getFileId());
				/*file = getFileHelper().moveTemp(tsmpDpFilePath, tempFileName);
				if (file == null) {
					this.logger.error("上傳申請單附件失敗: {}-{}", mId, tempFileName);
					failToUploads.add(tempFileName);
					continue;
				}
				
				dpFile = new TsmpDpFile();
				dpFile.setFileName(file.getFileName().toString());	// 若檔名有重複, 上傳後會自動更名, 需儲存修改後的檔名
				dpFile.setFilePath(tsmpDpFilePath);
				dpFile.setRefFileCateCode(TsmpDpFileType.M_ATTACHMENT.value());
				dpFile.setRefId(mId);
				dpFile.setCreateUser(uploader);
				dpFile = getTsmpDpFileDao().saveAndFlush(dpFile);

				dpFileIds.add(dpFile.getFileId());*/
			} catch (Exception e) {
				logger.debug("" + e);
				failToUploads.add(tempFileName);
			}
		}
		
		r.setFileIds(dpFileIds);
		r.setFailToUploads(failToUploads);
	}

	private Date getEffectiveDate(String dateString) {
		return DateTimeUtil.stringToDateTime(dateString, DateTimeFormatEnum.西元年月日_2).orElse(null);
	}

	private TsmpDpItems getItemsById(String itemNo, String subitemNo, boolean errorWhenNotExists, String locale) {
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, locale);
		TsmpDpItems vo = getTsmpDpItemsCacheProxy().findById(id);
		if (errorWhenNotExists) {
			if(vo != null) {
				return vo;
			}else {
				throw TsmpDpAaRtnCode._1213.throwing();
			}
			
		} else {
			return vo;
		}
	}

	private String getUserId(String userId, String userName) {
		if (!StringUtils.isEmpty(userId)) {
			return userId;
		}
		if (StringUtils.isEmpty(userName)) {
			return new String();
		}
		TsmpUser user = getTsmpUserDao().findFirstByUserName(userName);
		if (user == null) {
			return new String();
		}
		return user.getUserId();
	}


	/** 取申請單號 */
	private void setReqOrderNo(TsmpDpReqOrderm m, String reviewType, String reviewSubtype, String locale) {
		TsmpDpItems items = null;
		if (StringUtils.isEmpty(reviewSubtype)) {
			items = getItemsById(TsmpDpReqReviewType.ITEM_NO, reviewType, true, locale);
		} else {
			items = getItemsById(reviewType, reviewSubtype, true, locale);
		}
		final String reqOrderNo_type = items.getParam1();
		if (StringUtils.isEmpty(reqOrderNo_type)) {
			throw TsmpDpAaRtnCode._1213.throwing();
		}
		final String reqOrderNo_date = DateTimeUtil.dateTimeToString(DateTimeUtil.now(), DateTimeFormatEnum.西元年月日_3).orElse(String.valueOf(TsmpDpAaRtnCode._1295));
		final String seqName = reqOrderNo_type.concat("-").concat(reqOrderNo_date);
		final Long seq = getSeqStoreService().nextSequence(seqName);
		if (seq == null) {
			throw TsmpDpAaRtnCode._1213.throwing();
		}
		
		m.ensureReqOrderNo(reqOrderNo_type, reqOrderNo_date, seq);
	}

	private <Q extends DpReqServiceUpdateReq> void checkUpdateReq(Q req) {
		final Long reqOrdermId = req.getReqOrdermId();
		final Long lv = req.getLv();
		final String reqDesc = req.getReqDesc();
		final String clientId = req.getClientId();
		if (
			reqOrdermId == null || lv == null ||
			StringUtils.isEmpty(reqDesc) ||
			StringUtils.isEmpty(clientId)
		) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	/**
	 * 未送審才可更新(草稿階段), 且不會將此單送到下一簽核關卡, 更新後仍停留在"申請者"關卡
	 * @param reqOrdermId
	 * @return 申請者關卡
	 */
	private TsmpDpReqOrders checkUpdatable(Long reqOrdermId) {
		TsmpDpReqOrders s = getTsmpDpReqOrdersDao().queryNextCheckPoint(reqOrdermId);
		// 沒有可進行的關卡就不能更新
		if (s == null) {
			throw TsmpDpAaRtnCode._1223.throwing();
		}
		String reviewStatus = s.getReviewStatus();
		if (!( s.getLayer() == 0 && TsmpDpReqReviewStatus.WAIT1.value().equals(reviewStatus)) ) {
			throw TsmpDpAaRtnCode._1223.throwing();
		}
		return s;
	}

	private <R extends DpReqServiceResp, Q extends DpReqServiceUpdateReq> TsmpDpReqOrderm updateM( //
			TsmpDpReqOrderm m, Q req, R resp) {
		final String clientId = req.getClientId();
		final String orgId = req.getOrgId();
		final String reqDesc = req.getReqDesc();
		final String userName = req.getUpdateUser();
		final String reqUserId = getUserId(m.getReqUserId(), userName);
		final String effectiveDateStr = req.getEffectiveDate();
		final Date effectiveDate = getEffectiveDate(effectiveDateStr);
		final Long lv = req.getLv();

		TsmpDpReqOrderm orderM = ServiceUtil.deepCopy(m, TsmpDpReqOrderm.class);
		orderM.setClientId(clientId);
		orderM.setOrgId(orgId);
		orderM.setReqDesc(reqDesc);
		orderM.setReqUserId(reqUserId);
		orderM.setEffectiveDate(effectiveDate);
		orderM.setUpdateDateTime(DateTimeUtil.now());
		orderM.setUpdateUser(userName);
		orderM.setVersion(lv);
		try {
			orderM = getTsmpDpReqOrdermDao().saveAndFlush(orderM);
		} catch (ObjectOptimisticLockingFailureException e) {
			throw TsmpDpAaRtnCode.ERROR_DATA_EDITED.throwing();
		}

		return orderM;
	}

	private <R extends DpReqServiceResp> TsmpDpReqOrders updateS(TsmpDpReqOrderm m, TsmpDpReqOrders applierS, R resp) {
		applierS.setUpdateDateTime(DateTimeUtil.now());
		applierS.setUpdateUser(m.getUpdateUser());
		applierS = getTsmpDpReqOrdersDao().save(applierS);
		return applierS;
	}

	private <R extends DpReqServiceResp, Q extends DpReqServiceUpdateReq> void updateAttachments( //
			TsmpDpReqOrderm m, Q q, R r) {
		// 新增檔案
		List<String> newTempFileNames = q.getNewTempFileNames();
		if (newTempFileNames != null && !newTempFileNames.isEmpty()) {
			uploadAttachments(m.getReqOrdermId(), m.getCreateUser(), newTempFileNames, r);
		}
		
		// 異動檔案
		Map<String, String> oldFileMapping = q.getOldFileMapping();
		if (oldFileMapping == null || oldFileMapping.isEmpty()) {
			return;
		}
		
		List<Long> dpFileIds = r.getFileIds() == null ? new ArrayList<>() : r.getFileIds();
		List<String> failToUploads = r.getFailToUploads() == null ? new ArrayList<>() : r.getFailToUploads();

		String oldFileName = null;
		String newFileName = null;
		for(Map.Entry<String, String> entry : oldFileMapping.entrySet()) {
			oldFileName = entry.getKey();
			newFileName = entry.getValue();
			
			try {
				// 先把舊檔案砍掉
				List<TsmpDpFile> oldDpFiles = getTsmpDpFileDao() //
						.findByRefFileCateCodeAndRefIdAndFileName(TsmpDpFileType.M_ATTACHMENT.value(), m.getReqOrdermId(), oldFileName);
				if (oldDpFiles != null && !oldDpFiles.isEmpty()) {
					oldDpFiles.forEach((oldDpFile) -> {
						try {
							/* 20210205; Kim; 無論檔案是否為空，資料都應刪除
							if("Y".equals(oldDpFile.getIsBlob()) && oldDpFile.getBlobData() != null) {
							*/
							if("Y".equals(oldDpFile.getIsBlob())) {
								getTsmpDpFileDao().delete(oldDpFile);
							}else {
								getFileHelper().remove01(oldDpFile.getFilePath(), oldDpFile.getFileName(), (filename) -> {
									getTsmpDpFileDao().delete(oldDpFile);
								});
							}
							
						} catch (Exception e) {
							logger.debug("" + e);
						}
					});
				}
				// 再上傳新檔案(暫存)
				if (!StringUtils.isEmpty(newFileName) && getFileHelper().isTempFile(newFileName)) {
					TsmpDpFile dpFile = getFileHelper().moveTemp(m.getUpdateUser(), TsmpDpFileType.M_ATTACHMENT, m.getReqOrdermId(), newFileName, true, false,true);
					if (dpFile != null) {
						dpFileIds.add(dpFile.getFileId());
					}
					/*Path newFile = getFileHelper().moveTemp(tsmpDpFilePath, newFileName);
					if (newFile != null) {
						TsmpDpFile dpFile = new TsmpDpFile();
						dpFile.setFileName(newFile.getFileName().toString());	// 若檔名有重複, 上傳後會自動更名, 需儲存修改後的檔名
						dpFile.setFilePath(tsmpDpFilePath);
						dpFile.setRefFileCateCode(TsmpDpFileType.M_ATTACHMENT.value());
						dpFile.setRefId(m.getReqOrdermId());
						dpFile.setCreateUser(m.getUpdateUser());
						dpFile = getTsmpDpFileDao().saveAndFlush(dpFile);
						dpFileIds.add(dpFile.getFileId());
					}*/
				}
			} catch (Exception e) {
				logger.debug("" + e);
				failToUploads.add(newFileName);
			}
		}
		
		r.setFileIds(dpFileIds);
		r.setFailToUploads(failToUploads);
	}

	private void checkSubmitReq(DpReqServiceUpdateReq req) {
		final Long reqOrdermId = req.getReqOrdermId();
		final Long lv = req.getLv();
		final String clientId = req.getClientId();
		final String userName = req.getUpdateUser();
		if (
			reqOrdermId == null || lv == null ||
			StringUtils.isEmpty(clientId) ||
			StringUtils.isEmpty(userName)
		) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	/**
	 * @param m
	 * @param applierS 如果 createAppliers = false 才需要帶入
	 * @param nextStatus
	 * @param userName 操作者
	 * @param disableAll 是否要先停用現在所有的S
	 * @param createAppliers 是否要自動生成申請者關卡(已通過)
	 * @return
	 */
	private List<TsmpDpReqOrders> createS(TsmpDpReqOrderm m, TsmpDpReqOrders applierS, //
			TsmpDpReqReviewStatus nextStatus, String userName, boolean disableAll, boolean createAppliers
	) {
		final Long reqOrdermId = m.getReqOrdermId();
		final String reviewType = m.getReqType();
		
		if (disableAll) {
			// 停用舊的審核狀態檔
			List<TsmpDpReqOrders> oldSList = getTsmpDpReqOrdersDao().findByReqOrdermIdAndStatusOrderByLayerAscReqOrdersIdAsc(reqOrdermId //
					, TsmpDpDataStatus.ON.value());
			if (oldSList != null && !oldSList.isEmpty()) {
				oldSList.forEach((s) -> {
					s.setStatus(TsmpDpDataStatus.OFF.value());
					s.setProcFlag(null);
					s.setUpdateDateTime(DateTimeUtil.now());
					s.setUpdateUser(userName);
					s = getTsmpDpReqOrdersDao().save(s);
				});
			}
		}
		
		// 加入新的審核狀態
		// 查出此種簽核類型的所有關卡
		List<Integer> layers = getTsmpDpChkLayerDao().queryForCreateReqOrders(reviewType);
		if (layers == null || layers.isEmpty()) {
			this.logger.error("查無簽核關卡設定: reviewType=" + reviewType);
			throw TsmpDpAaRtnCode._1565.throwing();
		}

		List<TsmpDpReqOrders> ordersList = new ArrayList<>();

		TsmpDpReqOrders s = null;
		Integer layer = null;
		for(int i = 0; i < layers.size(); i++) {
			layer = layers.get(i);
			
			if (i == 0) {
				if (createAppliers) {
					s = new TsmpDpReqOrders();
					s.setReqOrdermId(reqOrdermId);
					s.setLayer(0);
					s.setReqComment(new String());
					s.setReviewStatus(TsmpDpReqReviewStatus.ACCEPT.value());
					s.setCreateUser(userName);
					s = getTsmpDpReqOrdersDao().save(s);
					ordersList.add(s);
				} else {
					ordersList.add(applierS);
				}
			}

			s = new TsmpDpReqOrders();
			s.setReqOrdermId(reqOrdermId);
			s.setLayer(layer);
			s.setReqComment(new String());
			s.setReviewStatus(nextStatus.value());
			if (i == 0) {
				s.setProcFlag(1);
			}
			s.setCreateUser(userName);
			s = getTsmpDpReqOrdersDao().save(s);
			ordersList.add(s);
		}
		
		return ordersList;
	}

	private <R extends DpReqServiceResp>void saveChkLog(List<TsmpDpReqOrders> sList, R r) {
		TsmpDpChkLog log = null;
		Integer procFlag = null;
		for(TsmpDpReqOrders s : sList) {
			procFlag = s.getProcFlag();
			if (procFlag != null && procFlag.equals(1)) {
				break;
			}
			
			log = new TsmpDpChkLog();
			log.setReqOrdermId(s.getReqOrdermId());
			log.setReqOrdersId(s.getReqOrdersId());
			log.setLayer(s.getLayer());
			log.setReqComment(s.getReqComment()); // NOT NULL
			log.setReviewStatus(s.getReviewStatus());
			log.setCreateDateTime(DateTimeUtil.now());
			log.setCreateUser(getUserAliasBySub(s.getCreateUser()) );
			log = getTsmpDpChkLogDao().save(log);

			r.setChkLogId(log.getChkLogId());
		}
	}

	/**
	 * 關卡停在申請者身上時才可以送審(layer = 0 and proc_flag = 1)
	 * @param reqOrdermId
	 */
	private TsmpDpReqOrders checkSubmittable(Long reqOrdermId) {
		return checkUpdatable(reqOrdermId);
	}

	private <Q extends DpReqServiceUpdateReq> void checkResubmitReq(Q q) {
		final Long reqOrdermId = q.getReqOrdermId();
		final Long lv = q.getLv();
		final String clientId = q.getClientId();
		final String userName = q.getUpdateUser();
		final String reqDesc = q.getReqDesc();
		if (
			reqOrdermId == null || lv == null
			|| StringUtils.isEmpty(clientId)
			|| StringUtils.isEmpty(userName)
		) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		if (StringUtils.isEmpty(reqDesc)){
			throw TsmpDpAaRtnCode.REQUIRED_APPLY_DESC.throwing();
		}
		
		// clientId不存在
		if (!getTsmpClientDao().findById(clientId).isPresent()) {
			throw TsmpDpAaRtnCode.NO_MEMBER_INFO.throwing();
		}
	}

	/**
	 * 申請單填寫者在最後一個關卡為 RETURN 時, 可以執行 WAIT2, 表示重送
	 * @param reqOrdermId
	 */
	private void checkResubmittable(Long reqOrdermId) {
		TsmpDpReqOrders currentS = getTsmpDpReqOrdersDao().queryCurrentStatus(reqOrdermId);
		if (currentS == null) {
			throw TsmpDpAaRtnCode._1214.throwing();
		}
		final String reviewStatus = currentS.getReviewStatus();
		final Integer procFlag = currentS.getProcFlag();
		if (!TsmpDpReqReviewStatus.RETURN.value().equals(reviewStatus) || !procFlag.equals(0)) {
			throw TsmpDpAaRtnCode._1214.throwing();
		}
	}

	private <R extends DpReqServiceResp> DpReqServiceSignVo<R> checkSignReq(DpReqServiceSignReq q, Class<R> rClass, String locale) {
		DpReqServiceSignVo<R> vo = new DpReqServiceSignVo<>();
		
		// 檢查申請單是否存在
		TsmpDpReqOrderm m = checkReqOrdermId(q.getReqOrdermId());
		// 檢查必要參數
		checkSignReq(q, locale);
		// 檢查審核狀態
		TsmpDpReqOrders currentS = getNextCheckPoint( //
			m.getReqOrdermId(), q.getCurrentReviewStatus(), q.getEncNextReviewStatus());
		// 簽核者是否為申請者本人
		boolean isApplier = checkIsApplier(q.getSignUserName(), q.getIdPType(), m);
		// 檢查使用者審核權限
		checkUserAuthority(m, q, currentS, isApplier);
		// 檢查審核狀態合法性
		checkSignStatus(m, q.getEncNextReviewStatus(), isApplier);
		
		vo.setM(m);
		vo.setCurrentS(currentS);
		vo.setResp( initResponse(rClass) );
		return vo;
	}

	private TsmpDpReqOrderm checkReqOrdermId(Long reqOrdermId) {
		if (reqOrdermId == null) {
			this.logger.debug("未傳入申請單ID");
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		Optional<TsmpDpReqOrderm> opt_m = getTsmpDpReqOrdermDao().findById(reqOrdermId);
		if (!opt_m.isPresent()) {
			this.logger.debug("查無申請單: " + reqOrdermId);
			throw TsmpDpAaRtnCode._1217.throwing();
		}
		return opt_m.get();
	}

	private void checkSignReq(DpReqServiceSignReq req, String locale) {
		final String signUserName = req.getSignUserName();
		final String chkStatus = req.getCurrentReviewStatus();
		String bcryptParamString = req.getEncNextReviewStatus();
		if (StringUtils.isEmpty(signUserName) ||
			StringUtils.isEmpty(chkStatus) ||
			StringUtils.isEmpty(bcryptParamString)
		) {
			this.logger.debug("基本共同欄位有誤");
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		// 檢查 currentReviewStatus 是否存在
		TsmpDpItems currentRs = getItemsById("REVIEW_STATUS", chkStatus, false, locale);
		if (currentRs == null) {
			throw TsmpDpAaRtnCode._1290.throwing();
		}
		
		bcryptParamString = decodeParamString(bcryptParamString, locale);
		req.setEncNextReviewStatus(bcryptParamString);
 
	}
	
	protected String decodeParamString(String encodeParamString, String locale) {
		String paramString = null;
		try {
			paramString = getBcryptParamHelper().decode(encodeParamString, "REVIEW_STATUS", locale);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return paramString;
	}

	/**
	 * 判斷簽核者是否為申請者本人
	 * @param signUserName
	 * @param m
	 * @return
	 */
	private boolean checkIsApplier(String signUserName, String idPType, TsmpDpReqOrderm m) {
		// 找出簽核者資訊
		DgrAcIdpUser dgrAcIdpUser = null;
		TsmpUser user = null;
		if (StringUtils.hasLength(idPType)) {// 以 SSO AC IdP 登入
			dgrAcIdpUser = getDgrAcIdpUserDao().findFirstByUserNameAndIdpType(signUserName, idPType);
		}else {// 以 AC 登入
			user = getTsmpUserDao().findFirstByUserName(signUserName);
		}
		
		List<TsmpClient> clients = getTsmpClientDao().findByClientName(signUserName);
		TsmpClient client = (clients == null || clients.isEmpty()) ? null : clients.get(0);
		// 使用者/用戶是否存在
		if (dgrAcIdpUser == null && user == null && client == null) {
			this.logger.debug("查無使用者/用戶: " + signUserName);
			throw TsmpDpAaRtnCode._1219.throwing();
		}

		final String reqUserId = m.getReqUserId();
		final String orgId = m.getOrgId();
		final String clientId = m.getClientId();
		final String createUser = m.getCreateUser();
		// 後台建立的案件
		if (StringUtils.hasLength(reqUserId) && StringUtils.hasLength(orgId)) {
			// 簽核者從後台登入
			if (dgrAcIdpUser != null) {
				return reqUserId.equals(signUserName);
			}else if (user != null) {
				return reqUserId.equals(user.getUserId());
			// 簽核者從前台登入 (正常不應該跑到這)
			} else {
				return false;
			}
		// 前台建立的案件
		// 即使是從後台登入, 也會有預設的 adminConsole 用戶帳號
		} else if (client != null) {
			return client.getClientId().equals(clientId) && client.getClientName().equals(createUser);
		}
		return false;
	}
 
	// 此案是否有下一關待簽(若是退回後結案, 則找不到下一關是正常的)
	private TsmpDpReqOrders getNextCheckPoint(Long reqOrdermId, String chkStatus, String nextRs) {
		TsmpDpReqOrders nextChkPoint = getTsmpDpReqOrdersDao().queryNextCheckPoint(reqOrdermId);
		if ((nextChkPoint != null && nextChkPoint.getReviewStatus().equals(chkStatus)) || //
			(nextChkPoint == null && TsmpDpReqReviewStatus.END.value().equals(nextRs))) {
			return ServiceUtil.deepCopy(nextChkPoint, TsmpDpReqOrders.class);
		}
		this.logger.debug("此案無法審核: " + reqOrdermId);
		throw TsmpDpAaRtnCode._1218.throwing();
	}

	// update前仍要檢查 roleId及orgId 是否符合關卡設定檔
	private void checkUserAuthority(TsmpDpReqOrderm m, DpReqServiceSignReq req, TsmpDpReqOrders currentS, //
			boolean isApplier) {
		// 如果是申請者本人的單要結案, 則可以直接先通過審核
		if (isApplier && TsmpDpReqReviewStatus.END.value().equals(req.getEncNextReviewStatus())) {
			return;
		}
		
		// 組織是否符合
		final String reqOrgId = m.getOrgId();
		final String usrOrgId = req.getOrgId();
		if (StringUtils.hasLength(reqOrgId)) {
			List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(usrOrgId, Integer.MAX_VALUE);
			if (!orgDescList.contains(reqOrgId)) {
				this.logger.debug(String.format("非所屬組織權限: %s not in %s", reqOrgId, orgDescList.toString()));
				throw TsmpDpAaRtnCode._1219.throwing();
			}
		}

		// 角色是否符合當前關卡
		if (currentS != null) {
			final String reviewType = m.getReqType();
			final Integer layer = currentS.getLayer();

			Boolean isUserAuthorized = getTsmpDpChkLayerDao().isUserAuthorized(reviewType, layer, req.getSignUserName());
			if (!isUserAuthorized) {
				this.logger.debug(String.format("無權審核當前關卡: %s-%d", reviewType, layer));
				throw TsmpDpAaRtnCode._1218.throwing();
			}
		} else if (!isApplier) {
			this.logger.debug("當前狀態僅允許申請者操作");
			throw TsmpDpAaRtnCode._1218.throwing();
		}
	}

	/* 申請者可以在草稿、未受審以及退回狀態下執行 END */
	private void checkSignStatus(TsmpDpReqOrderm m, String nextReviewStatus, boolean isApplier) {
		final Long reqOrdermId = m.getReqOrdermId();
		if (isApplier) {
			if (TsmpDpReqReviewStatus.END.value().equals(nextReviewStatus)) {
				Boolean isEndable = isEndable(reqOrdermId);
				if (!isEndable) {
					this.logger.debug("不可結案的狀態");
					throw TsmpDpAaRtnCode._1218.throwing();
				}
			}
		// 當前關卡審核者可以執行 ACCEPT / DENIED / RETURN
		} else if (
			!(TsmpDpReqReviewStatus.ACCEPT.value().equals(nextReviewStatus) ||
			TsmpDpReqReviewStatus.DENIED.value().equals(nextReviewStatus) ||
			TsmpDpReqReviewStatus.RETURN.value().equals(nextReviewStatus))
		) {
			this.logger.debug("不可執行的狀態");
			throw TsmpDpAaRtnCode._1218.throwing();
		}
	}

	private <R extends DpReqServiceResp> void doSign(DpReqServiceSignReq req, DpReqServiceSignVo<R> vo, //
			Class<R> rClass) {
		// tsmp_dp_req_orders
		TsmpDpReqOrders s = signS(vo, req);
		// tsmp_dp_chk_log
		TsmpDpChkLog log = new TsmpDpChkLog();
		log.setReqOrdermId(s.getReqOrdermId());
		log.setReqOrdersId(s.getReqOrdersId());
		log.setLayer(s.getLayer());
		log.setReqComment(s.getReqComment()); // NOT NULL
		log.setReviewStatus(s.getReviewStatus());
		log.setCreateDateTime(DateTimeUtil.now());
		log.setCreateUser(s.getUpdateUser());
		log = getTsmpDpChkLogDao().save(log);
		vo.getResp().setChkLogId(log.getChkLogId());
	}

	private <R extends DpReqServiceResp> TsmpDpReqOrders signS(DpReqServiceSignVo<R> vo, DpReqServiceSignReq req) {
		String reqComment = (req.getReqComment() == null ? "" : req.getReqComment());
		String nextRs = req.getEncNextReviewStatus();
		String chkStatus = req.getCurrentReviewStatus();	// 原始狀態
		TsmpDpReqOrders s = vo.getCurrentS();
		String signUserName = getUserAlias(req.getSignUserName());
		List<TsmpDpReqOrders> sList = getTsmpDpReqOrdersDao().findByReqOrdermIdAndStatusOrderByLayerAscReqOrdersIdAsc(req.getReqOrdermId() //
				, TsmpDpDataStatus.ON.value());
		
		if (sList == null || sList.isEmpty()) {
			this.logger.debug("審核狀態檔錯誤");
			throw TsmpDpAaRtnCode._1218.throwing();
		}

		TsmpDpReqOrders rtn = new TsmpDpReqOrders();

		// 結案需特別處理
		if (TsmpDpReqReviewStatus.END.value().equals(nextRs)) {
			// 退回後結案, 需要Disable原本的簽核清單
			if (TsmpDpReqReviewStatus.RETURN.value().equals(chkStatus)) {
				sList.forEach((origS) -> {
					origS.setStatus(TsmpDpDataStatus.OFF.value());
					origS.setProcFlag(null);
					origS.setUpdateDateTime(DateTimeUtil.now());
					
					origS.setUpdateUser(signUserName);
					getTsmpDpReqOrdersDao().saveAndFlush(origS);
				});
				sList = createS(vo.getM(), null, TsmpDpReqReviewStatus.END, req.getSignUserName(), false, true);
			}
			
			TsmpDpReqOrders orders = null;
			Integer layer = null;
			for(int i = 0; i < sList.size(); i++) {
				orders = sList.get(i);
				layer = orders.getLayer();
				if (layer == 0) {
					orders.setReqComment(reqComment);
					orders.setProcFlag(0);
					vo.getResp().setsIds(Arrays.asList(new Long[] {orders.getReqOrdersId()}));
					vo.getResp().setLv(orders.getVersion());
					rtn = orders;
				} else {
					orders.setProcFlag(null);
				}
				orders.setReviewStatus(nextRs);
				orders.setUpdateDateTime(DateTimeUtil.now());
				orders.setUpdateUser(signUserName);
				getTsmpDpReqOrdersDao().saveAndFlush(orders);
			}
		} else {
			s.setReqComment(reqComment);
			s.setReviewStatus(nextRs);
			s.setUpdateDateTime(DateTimeUtil.now());
			s.setUpdateUser(signUserName);
			
			TsmpDpReqOrders nextLayer = null;
			for(int i = 0; i < sList.size(); i++) {
				if (sList.get(i).getReqOrdersId().equals(s.getReqOrdersId()) &&
					(i + 1 < sList.size())) {
					nextLayer = ServiceUtil.deepCopy(sList.get(i + 1), TsmpDpReqOrders.class);
				}
			}

			// 沒有下一關了
			if (nextLayer == null) {
				s.setProcFlag(0);
			} else {
				// 同意才會繼續
				if (TsmpDpReqReviewStatus.ACCEPT.value().equals(nextRs)) {
					s.setProcFlag(null);
					nextLayer.setProcFlag(1);

					vo.setNextChkPoint(nextLayer);
				} else {
					s.setProcFlag(0);
					nextLayer.setReviewStatus(TsmpDpReqReviewStatus.END.value());
				}
				nextLayer.setUpdateDateTime(DateTimeUtil.now());
				nextLayer.setUpdateUser(signUserName);
				nextLayer = getTsmpDpReqOrdersDao().saveAndFlush(nextLayer);
			}
			
			s = getTsmpDpReqOrdersDao().saveAndFlush(s);
			
			vo.getResp().setsIds(Arrays.asList(new Long[] {s.getReqOrdersId()}));
			vo.getResp().setLv(s.getVersion());
			rtn = s;
		}
		
		return rtn;
	}
	
	private String getUserAliasBySub(String createUser) {

		List<DgrAcIdpUser> dgrAcIdpUser = getDgrAcIdpUserDao().findByUserName(createUser);
		if (dgrAcIdpUser.size() != 0) {
			if (StringUtils.hasLength(dgrAcIdpUser.get(0).getUserAlias())) {
				return dgrAcIdpUser.get(0).getUserAlias();
			}
			return dgrAcIdpUser.get(0).getUserName();
		}

		return createUser;
	}

	private String getUserAlias(String signUserName) {
		if (!StringUtils.hasLength(signUserName)) {
			return new String();
		}
		String []namearr=signUserName.split("\\.");
		if (namearr.length>=2) {
			DgrAcIdpUser acIdpUser = getDgrAcIdpUserDao().findFirstByUserNameAndIdpType(namearr[1], namearr[0]);
			return acIdpUser.getIdpType()+"."+acIdpUser.getUserAlias();
		}
		return signUserName;
	}

	protected <R extends DpReqServiceResp> R castResp(DpReqServiceResp resp, Class<R> rClass) {
		try {
			return rClass.cast(resp);
		} catch (Exception e) {
			this.logger.error(String.format("轉型失敗: %s -> %s", resp.getClass().getSimpleName(), rClass.getSimpleName()));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	protected <Q extends DpReqServiceSaveDraftReq> Q castSaveDraftReq(DpReqServiceSaveDraftReq req, Class<Q> qClass) {
		try {
			return qClass.cast(req);
		} catch (Exception e) {
			this.logger.error(String.format("轉型失敗: %s -> %s", req.getClass().getSimpleName(), qClass.getSimpleName()));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	protected <Q extends DpReqServiceUpdateReq> Q castUpdateReq(DpReqServiceUpdateReq req, Class<Q> qClass) {
		try {
			return qClass.cast(req);
		} catch (Exception e) {
			this.logger.error(String.format("轉型失敗: %s -> %s", req.getClass().getSimpleName(), qClass.getSimpleName()));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	/**
	 * 檢查生效日期
	 * @param effectiveDateStr
	 * @param sup 如果沒有傳入, 則當 effectiveDateStr 為空時, 就不會拋錯
	 * @param allowPastDate 是否允許傳入過去的日期, throws 1295
	 * @return
	 */
	protected Date checkEffectiveDate(String effectiveDateStr, Supplier<? extends TsmpDpAaException> sup, //
			boolean allowPastDate) {
		Date effectiveDate = null;
		if (StringUtils.isEmpty(effectiveDateStr)) {
			if (sup != null && sup.get() != null) {
				throw sup.get();
			}
		} else {
			effectiveDate = getEffectiveDate(effectiveDateStr);
			if (effectiveDate == null) {
				throw TsmpDpAaRtnCode._1295.throwing();
			}
			if (!allowPastDate) {
				// 不可選過去的日期
				Date now = Date.from(ZonedDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant());
				if (effectiveDate.compareTo(now) < 0) {
					throw TsmpDpAaRtnCode._1227.throwing();
				}
			}
		}
		return effectiveDate;
	}

	protected TsmpDpReqOrderm checkDeleteDraft(Long reqOrdermId) {
		// 確認申請單主檔是否存在
		TsmpDpReqOrderm m = getTsmpDpReqOrdermDao().findById(reqOrdermId).orElse(null);
		if (m == null) {
			throw TsmpDpAaRtnCode._1217.throwing();
		}
		// 確認是否為草稿
		TsmpDpReqOrders s = getTsmpDpReqOrdersDao().queryNextCheckPoint(m.getReqOrdermId());
		// 沒有可進行的關卡就不能刪除
		if (s == null) {
			throw TsmpDpAaRtnCode._1287.throwing();
		}
		// 非申請者待審就不是草稿，不能刪除
		Integer applierLayer = Integer.valueOf(0);
		Integer layer = s.getLayer();
		String wait1 = TsmpDpReqReviewStatus.WAIT1.value();
		String reviewStatus = s.getReviewStatus();
		if (!applierLayer.equals(layer) || !wait1.equals(reviewStatus)) {
			throw TsmpDpAaRtnCode._1287.throwing();
		}
		return m;
	}

	private void deleteAttachments(TsmpDpReqOrderm m) {
		Long refId = m.getReqOrdermId();
		String refFileCateCode = TsmpDpFileType.M_ATTACHMENT.value();
		List<TsmpDpFile> tsmpDpFiles = getTsmpDpFileDao().findByRefFileCateCodeAndRefId(refFileCateCode, refId);
		if (CollectionUtils.isEmpty(tsmpDpFiles)) {
			return;
		}
		tsmpDpFiles.forEach((f) -> {
			getTsmpDpFileDao().delete(f);
		});
	}

	/** 儲存草稿 */

	protected abstract <Q extends DpReqServiceSaveDraftReq> void checkDetailReq(Q q, String locale) throws TsmpDpAaException;

	protected abstract <R extends DpReqServiceResp, Q extends DpReqServiceSaveDraftReq> void saveDetail( //
			TsmpDpReqOrderm m, Q q, R r, InnerInvokeParam iip);

	protected <R extends DpReqServiceResp, Q extends DpReqServiceSaveDraftReq> void postSaveDraft( //
			TsmpDpReqOrderm m, Q q, R r, String locale) {
		// 等待被繼承者覆寫
	}

	/** 更新草稿 */

	protected abstract <Q extends DpReqServiceUpdateReq> void checkDetailUpdateReq(Q q, String locale) throws TsmpDpAaException;

	protected abstract <R extends DpReqServiceResp, Q extends DpReqServiceUpdateReq> void updateDetail( //
			TsmpDpReqOrderm m, Q q, R r, InnerInvokeParam iip);

	protected <R extends DpReqServiceResp, Q extends DpReqServiceUpdateReq> void postUpdate( //
			TsmpDpReqOrderm m, Q q, R r) {
		// 等待被繼承者覆寫
	}

	/** 刪除草稿 */
	
	protected abstract void deleteDraftDetail(TsmpDpReqOrderm m) throws TsmpDpAaException;

	/** 送審 */
	
	protected <R extends DpReqServiceResp> void postSubmit(TsmpDpReqOrderm m, DpReqServiceUpdateReq q, //
			R r, String locale, InnerInvokeParam iip) {
		// 寄發簽核Mail通知
		TsmpAuthorization auth = new TsmpAuthorization();
		auth.setUserName(q.getUpdateUser());
		getSendReviewMailService().sendEmail(auth, m.getReqType(), m.getReqOrdermId(), m.getReqOrderNo(), locale);
	}

	/** 重新送審 */
	
	protected <Q extends DpReqServiceUpdateReq> void checkDetailResubmitReq(Q q, String locale) throws TsmpDpAaException {
		checkDetailUpdateReq(q, locale);
	}

	protected <R extends DpReqServiceResp> void postResubmit(TsmpDpReqOrderm m, DpReqServiceUpdateReq q, //
			R r, String locale, InnerInvokeParam iip) {
		// 寄發簽核Mail通知
		TsmpAuthorization authorization = new TsmpAuthorization();
		authorization.setUserName(q.getUpdateUser());
		getSendReviewMailService().sendEmail(authorization, m.getReqType(), m.getReqOrdermId(), m.getReqOrderNo(), locale);
	}

	/** 簽核: 退回/結案/同意/不同意 */

	protected <R extends DpReqServiceResp> void preDoEnd(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo) {
		// 等待被繼承者覆寫
	}
	
	protected <R extends DpReqServiceResp> void postDoEnd(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo, String locale, InnerInvokeParam iip) {
		// 等待被繼承者覆寫
	}

	protected <R extends DpReqServiceResp> void preDoAccept(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo) {
		// 等待被繼承者覆寫
	}
	
	protected <R extends DpReqServiceResp> void postDoAccept(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo, String locale, InnerInvokeParam iip) {
		// 等待被繼承者覆寫
	}
	
	protected <R extends DpReqServiceResp> void postDoAllAccept(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo) {
		// 預設是全數通過後會發送該簽核類型指定的ApptJob
		final TsmpDpReqOrderm m = vo.getM();

		TsmpDpApptJob job = new TsmpDpApptJob();
		job.setRefItemNo(m.getReqType());
		job.setRefSubitemNo(m.getReqSubtype());
		job.setInParams(m.getReqOrderNo());
		job.setIdentifData(vo.getIndentifData());
		// 生效日期若為null, 就把生效時間改為現在, 表示立即生效
		final Date startDateTime = m.getEffectiveDate() == null ? DateTimeUtil.now() : m.getEffectiveDate();
		job.setStartDateTime(startDateTime);
		job.setCreateUser(m.getCreateUser());
		// 生效日期若為 null, 表示審核通過後立即生效 (前台沒有 ApptJobDispatcher)
		if (m.getEffectiveDate() == null && getApptJobDispatcher() != null) {
			job = getApptJobDispatcher().addAndRefresh(job);
		} else {
			job = getTsmpDpApptJobDao().save(job);
		}
		this.logger.debug("排程工作已建立: " + job.getApptJobId());
	}
	
	@Override
	public void deleteExpired(TsmpDpReqOrderm m) throws TsmpDpAaException {
		//複製deleteDraft的mehtod來修改的
		
		// 刪除明細檔 (D)
		deleteDraftDetail(m);

		// 刪除審核歷程檔 (LOG) (如果是草稿，歷程檔應該不會有資料)
		List<TsmpDpChkLog> logList = getTsmpDpChkLogDao().findByReqOrdermIdOrderByCreateDateTime(m.getReqOrdermId());
		if (!CollectionUtils.isEmpty(logList)) {
			logList.forEach((log) -> {
				getTsmpDpChkLogDao().delete(log);
			});
		}

		// 刪除審核狀態檔 (S)
		getTsmpDpReqOrdersDao().deleteByReqOrdermId(m.getReqOrdermId());
		
		// 刪除申請單附件
		deleteAttachments(m);
		
		// 刪除主檔 (M)
		getTsmpDpReqOrdermDao().delete(m);
	}

	protected <R extends DpReqServiceResp> void preDoDenied(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo) {
		// 等待被繼承者覆寫
	}
	
	protected <R extends DpReqServiceResp> void postDoDenied(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo, String locale, InnerInvokeParam iip) {
		// 等待被繼承者覆寫
	}

	protected <R extends DpReqServiceResp> void preDoReturn(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo) {
		// 等待被繼承者覆寫
	}
	
	protected <R extends DpReqServiceResp> void postDoReturn(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo, String locale, InnerInvokeParam iip) {
		// 等待被繼承者覆寫
	}

	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected TsmpDpReqOrdermDao getTsmpDpReqOrdermDao() {
		return this.tsmpDpReqOrdermDao;
	}

	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}

	protected SeqStoreService getSeqStoreService() {
		return this.seqStoreService;
	}

	protected TsmpDpReqOrdersDao getTsmpDpReqOrdersDao() {
		return this.tsmpDpReqOrdersDao;
	}

	protected FileHelper getFileHelper() {
		return this.fileHelper;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected TsmpDpChkLayerDao getTsmpDpChkLayerDao() {
		return this.tsmpDpChkLayerDao;
	}

	protected TsmpDpChkLogDao getTsmpDpChkLogDao() {
		return this.tsmpDpChkLogDao;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected ApptJobDispatcher getApptJobDispatcher() {
		return this.apptJobDispatcher;
	}

	protected TsmpDpApptJobDao getTsmpDpApptJobDao() {
		return this.tsmpDpApptJobDao;
	}

	protected SendReviewMailService getSendReviewMailService() {
		return this.mailService;
	}

	public SendClientRegMailService getSendClientRegMailService() {
		return sendClientRegMailService;
	}

	public SendAPIApplicationMailService getSendAPIApplicationMailService() {
		return sendAPIApplicationMailService;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return this.dgrAuditLogService;
	}
	
	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return this.dgrAcIdpUserDao;
	}
}
