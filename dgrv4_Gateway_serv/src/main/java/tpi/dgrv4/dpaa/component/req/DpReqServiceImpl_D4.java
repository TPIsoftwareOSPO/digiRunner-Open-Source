package tpi.dgrv4.dpaa.component.req;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.dpaa.service.SendAPIApplicationMailService;
import tpi.dgrv4.dpaa.service.SendClientRegMailService;
import tpi.dgrv4.dpaa.service.SendReviewMailService;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd4;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpChkLayerDao;
import tpi.dgrv4.entity.repository.TsmpDpChkLogDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd4Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdersDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.util.InnerInvokeParam;

/**
 * 昕力大學文章簽核
 * ※ 此為開發範例，非正式簽核類型
 * @author Kim
 */
@Service(value = "dpReqServiceImpl_D4")
public class DpReqServiceImpl_D4 extends DpReqServiceAbstract implements DpReqServiceIfs {

	private TsmpDpReqOrderd4Dao tsmpDpReqOrderd4Dao;
	
	@Autowired
	public DpReqServiceImpl_D4(ApptJobDispatcher apptJobDispatcher, TsmpClientDao tsmpClientDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpDpReqOrdermDao tsmpDpReqOrdermDao, TsmpUserDao tsmpUserDao,
			SeqStoreService seqStoreService, TsmpDpReqOrdersDao tsmpDpReqOrdersDao, FileHelper fileHelper,
			TsmpDpFileDao tsmpDpFileDao, TsmpDpChkLayerDao tsmpDpChkLayerDao, TsmpDpChkLogDao tsmpDpChkLogDao,
			BcryptParamHelper bcryptParamHelper, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpDpApptJobDao tsmpDpApptJobDao, SendReviewMailService mailService,
			SendClientRegMailService sendClientRegMailService,
			SendAPIApplicationMailService sendAPIApplicationMailService, DgrAuditLogService dgrAuditLogService,
			DgrAcIdpUserDao dgrAcIdpUserDao, TsmpDpReqOrderd4Dao tsmpDpReqOrderd4Dao) {
		super(apptJobDispatcher, tsmpClientDao, tsmpDpItemsCacheProxy, tsmpDpReqOrdermDao, tsmpUserDao, seqStoreService,
				tsmpDpReqOrdersDao, fileHelper, tsmpDpFileDao, tsmpDpChkLayerDao, tsmpDpChkLogDao, bcryptParamHelper,
				tsmpOrganizationDao, tsmpDpApptJobDao, mailService, sendClientRegMailService,
				sendAPIApplicationMailService, dgrAuditLogService, dgrAcIdpUserDao);
		this.tsmpDpReqOrderd4Dao = tsmpDpReqOrderd4Dao;
	}

	@Override
	protected <Q extends DpReqServiceSaveDraftReq> void checkDetailReq(Q q, String locale) throws TsmpDpAaException {
		// 一定要先轉型
		DpReqServiceSaveDraftReq_D4 req = castSaveDraftReq(q, DpReqServiceSaveDraftReq_D4.class);
		// 驗證傳進來的明細資料
		String userId = req.getUserId();
		String article = req.getArticle();
		if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(article)) {
			// 缺少必填參數
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	@Override
	protected <R extends DpReqServiceResp, Q extends DpReqServiceSaveDraftReq> void saveDetail( //
		TsmpDpReqOrderm m, Q q, R r, InnerInvokeParam iip) {
		// 一定要先轉型
		DpReqServiceSaveDraftReq_D4 req = castSaveDraftReq(q, DpReqServiceSaveDraftReq_D4.class);
		DpReqServiceResp_D4 resp = castResp(r, DpReqServiceResp_D4.class);

		// 將資料儲存進明細表中
		TsmpDpReqOrderd4 d4 = new TsmpDpReqOrderd4();
		d4.setRefReqOrdermId(m.getReqOrdermId());
		
		d4.setUserId(req.getUserId());
		d4.setArticle(req.getArticle());
		d4.setIsPublish(0);	// 預設文章未公開
		
		d4.setCreateDateTime(DateTimeUtil.now());
		d4.setCreateUser(m.getCreateUser());
		d4 = getTsmpDpReqOrderd4Dao().save(d4);
		
		// 將儲存後取得的明細表流水號，帶入 Response 物件
		Long reqOrderd4Id = d4.getReqOrderd4Id();
		resp.setReqOrderd4Id(reqOrderd4Id);
	}

	@Override
	protected void deleteDraftDetail(TsmpDpReqOrderm m) throws TsmpDpAaException {
		Long refReqOrdermId = m.getReqOrdermId();
		List<TsmpDpReqOrderd4> d4List = getTsmpDpReqOrderd4Dao().findByRefReqOrdermId(refReqOrdermId);
		if (!CollectionUtils.isEmpty(d4List)) {
			getTsmpDpReqOrderd4Dao().deleteAll(d4List);
		}
	}

	@Override
	protected <Q extends DpReqServiceUpdateReq> void checkDetailUpdateReq(Q q, String locale) throws TsmpDpAaException {
		// 一定要先轉型
		DpReqServiceUpdateReq_D4 req = castUpdateReq(q, DpReqServiceUpdateReq_D4.class);
		// 驗證傳進來的明細資料
		String userId = req.getUserId();
		if (StringUtils.isEmpty(userId)) {
			// 缺少必填參數
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	@Override
	protected <R extends DpReqServiceResp, Q extends DpReqServiceUpdateReq> void updateDetail( //
			TsmpDpReqOrderm m, Q q, R r, InnerInvokeParam iip) {
		// 一定要先轉型
		DpReqServiceUpdateReq_D4 req = castUpdateReq(q, DpReqServiceUpdateReq_D4.class);
		DpReqServiceResp_D4 resp = castResp(r, DpReqServiceResp_D4.class);

		// 依照 申請單流水號 及 員工(使用者)ID 找出該員工原本的文章
		Long refReqOrdermId = m.getReqOrdermId();
		String userId = req.getUserId();
		TsmpDpReqOrderd4 d4 = getTsmpDpReqOrderd4Dao().findFirstByRefReqOrdermIdAndUserId( //
				refReqOrdermId, userId);
		if (d4 == null) {
			throw TsmpDpAaRtnCode._1217.throwing();	// 單據查詢錯誤
		}
		
		d4.setArticle(req.getArticle());
		d4.setUpdateDateTime(DateTimeUtil.now());
		d4.setUpdateUser(m.getUpdateUser());
		// Entity 類別中的 @Version 會啟動樂觀鎖機制, 避免 lost update
		try {
			d4 = getTsmpDpReqOrderd4Dao().save(d4);
		} catch (ObjectOptimisticLockingFailureException e) {
			throw TsmpDpAaRtnCode.ERROR_DATA_EDITED.throwing();	// 資料已被異動
		}
		
		// 將儲存後取得的明細表流水號，帶入 Response 物件
		resp.setReqOrderd4Id(d4.getReqOrderd4Id());
	}

	protected TsmpDpReqOrderd4Dao getTsmpDpReqOrderd4Dao() {
		return this.tsmpDpReqOrderd4Dao;
	}

}
