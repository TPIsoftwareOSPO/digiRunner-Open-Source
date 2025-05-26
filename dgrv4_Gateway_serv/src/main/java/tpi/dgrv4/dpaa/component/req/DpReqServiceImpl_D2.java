package tpi.dgrv4.dpaa.component.req;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType;
import tpi.dgrv4.common.constant.TsmpDpSeqStoreKey;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.component.job.DPB0065Job;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.dpaa.service.SendAPIApplicationMailService;
import tpi.dgrv4.dpaa.service.SendClientRegMailService;
import tpi.dgrv4.dpaa.service.SendReviewMailService;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.jpql.TsmpApiExt;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd2;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd2d;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.entity.sql.TsmpDpThemeCategory;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.SeqStoreDao;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpApiExtDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpApptJobDao;
import tpi.dgrv4.entity.repository.TsmpDpChkLayerDao;
import tpi.dgrv4.entity.repository.TsmpDpChkLogDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd2Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd2dDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdersDao;
import tpi.dgrv4.entity.repository.TsmpDpThemeCategoryDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * 處理"API上下架"簽核流程
 * @author Kim
 *
 */
@Service(value = "dpReqServiceImpl_D2")
public class DpReqServiceImpl_D2 extends DpReqServiceAbstract implements DpReqServiceIfs {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpReqOrderd2Dao tsmpDpReqOrderd2Dao;
	private TsmpDpReqOrderd2dDao tsmpDpReqOrderd2dDao;
	private TsmpApiExtDao tsmpApiExtDao;
	private ApplicationContext ctx;
	private JobHelper jobHelper;
	private TsmpApiDao tsmpApiDao;
	private TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao;
	protected SeqStoreDao seqStoreDao;

	@Autowired
	public DpReqServiceImpl_D2(ApptJobDispatcher apptJobDispatcher, TsmpClientDao tsmpClientDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpDpReqOrdermDao tsmpDpReqOrdermDao, TsmpUserDao tsmpUserDao,
			SeqStoreService seqStoreService, TsmpDpReqOrdersDao tsmpDpReqOrdersDao, FileHelper fileHelper,
			TsmpDpFileDao tsmpDpFileDao, TsmpDpChkLayerDao tsmpDpChkLayerDao, TsmpDpChkLogDao tsmpDpChkLogDao,
			BcryptParamHelper bcryptParamHelper, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpDpApptJobDao tsmpDpApptJobDao, SendReviewMailService mailService,
			SendClientRegMailService sendClientRegMailService,
			SendAPIApplicationMailService sendAPIApplicationMailService, DgrAuditLogService dgrAuditLogService,
			DgrAcIdpUserDao dgrAcIdpUserDao, TsmpDpReqOrderd2Dao tsmpDpReqOrderd2Dao,
			TsmpDpReqOrderd2dDao tsmpDpReqOrderd2dDao, TsmpApiExtDao tsmpApiExtDao, ApplicationContext ctx,
			JobHelper jobHelper, TsmpApiDao tsmpApiDao, TsmpDpThemeCategoryDao tsmpDpThemeCategoryDao,
			SeqStoreDao seqStoreDao) {
		super(apptJobDispatcher, tsmpClientDao, tsmpDpItemsCacheProxy, tsmpDpReqOrdermDao, tsmpUserDao, seqStoreService,
				tsmpDpReqOrdersDao, fileHelper, tsmpDpFileDao, tsmpDpChkLayerDao, tsmpDpChkLogDao, bcryptParamHelper,
				tsmpOrganizationDao, tsmpDpApptJobDao, mailService, sendClientRegMailService,
				sendAPIApplicationMailService, dgrAuditLogService, dgrAcIdpUserDao);
		this.tsmpDpReqOrderd2Dao = tsmpDpReqOrderd2Dao;
		this.tsmpDpReqOrderd2dDao = tsmpDpReqOrderd2dDao;
		this.tsmpApiExtDao = tsmpApiExtDao;
		this.ctx = ctx;
		this.jobHelper = jobHelper;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpDpThemeCategoryDao = tsmpDpThemeCategoryDao;
		this.seqStoreDao = seqStoreDao;
	}

	@Override
	protected <Q extends DpReqServiceSaveDraftReq> void checkDetailReq(Q q, String locale) throws TsmpDpAaException {
		DpReqServiceSaveDraftReq_D2 req = castSaveDraftReq(q, DpReqServiceSaveDraftReq_D2.class);
		if (req == null) {
			throw TsmpDpAaRtnCode._1213.throwing();
		}

		// 已知API上下架只會由後端申請, 就一定要帶入生效日期
		checkEffectiveDate(req.getEffectiveDate(), () -> {
			return TsmpDpAaRtnCode._1296.throwing();
		}, false);

		// clientId不存在
		if (!getTsmpClientDao().findById(q.getClientId()).isPresent()) {
			throw TsmpDpAaRtnCode.NO_MEMBER_INFO.throwing();
		}

		final String orgId = req.getOrgId();
		final String userName = req.getCreateUser();
		final String reqSubtype = req.getReqSubtype();	// 簽核子類別
		final String publicFlag = req.getPublicFlag();	// 開放狀態
		if (
			StringUtils.isEmpty(orgId) ||
			StringUtils.isEmpty(userName) ||
			StringUtils.isEmpty(reqSubtype) ||
			StringUtils.isEmpty(publicFlag) ||
			req.getApiUidDatas() == null ||
			req.getApiUidDatas().isEmpty()
		) {
			throw TsmpDpAaRtnCode._1213.throwing();
		}
		
		String reqType = q.getReqType();
		TsmpDpItemsId id = new TsmpDpItemsId(reqType, reqSubtype, locale);
		TsmpDpItems vo = getTsmpDpItemsCacheProxy().findById(id);
		if (vo == null) {
			throw TsmpDpAaRtnCode.NO_ITEMS_DATA.throwing();
		}
	}

	@Override
	protected <R extends DpReqServiceResp, Q extends DpReqServiceSaveDraftReq> void saveDetail(TsmpDpReqOrderm m, Q q,
			R r, InnerInvokeParam iip) {
		// 一定要先轉型
		DpReqServiceSaveDraftReq_D2 req = castSaveDraftReq(q, DpReqServiceSaveDraftReq_D2.class);
		DpReqServiceResp_D2 resp = castResp(r, DpReqServiceResp_D2.class);
		
		Map<String, List<DpReqServiceSaveDraftReq_D2D>> data = req.getApiUidDatas();
		Map<String, String> apiFileMapping = req.getApiMapFileName();
		final String reqSubtype = m.getReqSubtype();
		final String publicFlag = req.getPublicFlag();
		
		TsmpDpReqOrderd2 d2 = null;
		List<DpReqServiceSaveDraftReq_D2D> bindingData = null;
		Long reqOrderd2dId = null;
		List<Long> reqOrderd2dIds = null;
		for(Map.Entry<String, List<DpReqServiceSaveDraftReq_D2D>> entry : data.entrySet()) {
			String apiUid = entry.getKey();
			bindingData = entry.getValue();
			if (bindingData == null || bindingData.isEmpty()) {
				String now = "";
				Optional<String> opt = DateTimeUtil.dateTimeToString(DateTimeUtil.now(), DateTimeFormatEnum.西元年月日時分秒_2);
				if (opt.isPresent()) {
					now = opt.get();
				}
				logger.debug("可能是髒資料導致系統無法匹配, TSMP_DP_API_THEME 查無資料, apiUid:" + apiUid);
				throw TsmpDpAaRtnCode._1504.throwing(now);
			}

			d2 = saveD2(m.getReqOrdermId(), entry.getKey(), publicFlag, m.getCreateUser());
			
			// 存入 mapping table
			reqOrderd2dIds = new ArrayList<>();
			for(DpReqServiceSaveDraftReq_D2D bd : bindingData) {
				TsmpDpReqOrderd2d d2d = new TsmpDpReqOrderd2d();
				d2d.setReqOrderd2Id(d2.getReqOrderd2Id());
				d2d.setApiUid(bd.getApiUid());
				d2d.setRefThemeId(bd.getRefThemeId());
				reqOrderd2dId = getSeqStoreService().nextSequence(TsmpDpSeqStoreKey.TSMP_DP_REQ_ORDERD2D);
				d2d.setReqOrderd2dId(reqOrderd2dId);
				d2d.setCreateDateTime(DateTimeUtil.now());
				d2d.setCreateUser(m.getCreateUser());
				d2d = getTsmpDpReqOrderd2dDao().save(d2d);
				reqOrderd2dIds.add(d2d.getReqOrderd2dId());
			}
			
			// 處理API說明文件
			processApiAttachment(reqSubtype, d2, apiFileMapping, resp);
			
			Map<Long, List<Long>> reqOrderd2Ids = resp.getReqOrderd2Ids() == null ? //
					new HashMap<>() : resp.getReqOrderd2Ids();
			reqOrderd2Ids.put(d2.getReqOrderd2Id(), reqOrderd2dIds);
			resp.setReqOrderd2Ids(reqOrderd2Ids);
		}
	}

	@Override
	protected <R extends DpReqServiceResp, Q extends DpReqServiceSaveDraftReq> void postSaveDraft(TsmpDpReqOrderm m, Q q,
			R r, String locale) {
		// 刪除過期的申請單序號檔
		DPB0065Job dpb0065Job = getDpb0065Job(locale);
		getJobHelper().add(dpb0065Job);
	}

	@Override
	protected void deleteDraftDetail(TsmpDpReqOrderm m) throws TsmpDpAaException {
		Long refReqOrdermId = m.getReqOrdermId();
		List<TsmpDpReqOrderd2> d2List = getTsmpDpReqOrderd2Dao().findByRefReqOrdermId(refReqOrdermId);
		if (!CollectionUtils.isEmpty(d2List)) {
			removeOldD2(d2List);
		}
	}

	@Override
	protected <Q extends DpReqServiceUpdateReq> void checkDetailUpdateReq(Q q, String locale) throws TsmpDpAaException {
		DpReqServiceUpdateReq_D2 req = castUpdateReq(q, DpReqServiceUpdateReq_D2.class);
		if (req == null) {
			throw TsmpDpAaRtnCode._1223.throwing();
		}

		// 已知API上下架只會由後端申請, 就一定要帶入生效日期
		checkEffectiveDate(req.getEffectiveDate(), () -> {
			return TsmpDpAaRtnCode._1296.throwing();
		}, false);
		
		final String orgId = req.getOrgId();
		final String userName = req.getUpdateUser();
		if (
			StringUtils.isEmpty(orgId) ||
			StringUtils.isEmpty(userName)
		) {
			this.logger.debug("Invalid username or orgId!");
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		if (
			StringUtils.isEmpty(req.getPublicFlag()) ||
			req.getApiUidDatas() == null ||
			req.getApiUidDatas().isEmpty() ||
			req.getOriApiMapFileName() == null ||
			req.getNewApiMapFileName() == null
		) {
			this.logger.debug("未傳入明細資料");
			throw TsmpDpAaRtnCode._1296.throwing();
		}
	}

	@Override
	protected <R extends DpReqServiceResp, Q extends DpReqServiceUpdateReq> void updateDetail(TsmpDpReqOrderm m, Q q,
			R r, InnerInvokeParam iip) {
		DpReqServiceUpdateReq_D2 req = castUpdateReq(q, DpReqServiceUpdateReq_D2.class);
		DpReqServiceResp_D2 resp = castResp(r, DpReqServiceResp_D2.class);
		
		// 重建新資料
		Map<String, List<DpReqServiceUpdateReq_D2D>> data = req.getApiUidDatas();

		String apiUid = null;
		String publicFlag = req.getPublicFlag();
		List<DpReqServiceUpdateReq_D2D> bindingData = null;
		TsmpDpReqOrderd2 d2 = null;
		List<Long> reqOrderd2dIds = null;
		List<TsmpDpReqOrderd2> oldD2List = getTsmpDpReqOrderd2Dao() // 舊的D2資料
				.findByRefReqOrdermId(m.getReqOrdermId());
		for(Map.Entry<String, List<DpReqServiceUpdateReq_D2D>> entry : data.entrySet()) {
			apiUid = entry.getKey();
			bindingData = entry.getValue();
			if (bindingData == null || bindingData.isEmpty()) {
				String now = "";
				Optional<String> opt = DateTimeUtil.dateTimeToString(DateTimeUtil.now(), DateTimeFormatEnum.西元年月日時分秒_2);
				if (opt.isPresent()) {
					now = opt.get();
				}
				logger.debug("可能是髒資料導致系統無法匹配, TSMP_DP_API_THEME 查無資料, apiUid:" + apiUid);
				throw TsmpDpAaRtnCode._1504.throwing(now);
			}
			// 儲存D2
			d2 = saveD2(m.getReqOrdermId(), apiUid, publicFlag, m.getUpdateUser());
			
			// 儲存D2D
			reqOrderd2dIds = new ArrayList<>();
			for(DpReqServiceUpdateReq_D2D bd : bindingData) {
				TsmpDpReqOrderd2d d2d = new TsmpDpReqOrderd2d();
				d2d.setReqOrderd2Id(d2.getReqOrderd2Id());
				d2d.setApiUid(bd.getApiUid());
				d2d.setRefThemeId(bd.getRefThemeId());
				d2d.setReqOrderd2dId( getSeqStoreService().nextSequence(TsmpDpSeqStoreKey.TSMP_DP_REQ_ORDERD2D) );
				d2d.setCreateDateTime(DateTimeUtil.now());
				d2d.setCreateUser(m.getCreateUser());
				d2d = getTsmpDpReqOrderd2dDao().save(d2d);
				reqOrderd2dIds.add(d2d.getReqOrderd2dId());
			}
			
			// 儲存API說明文件
			saveD2Attachment(d2, oldD2List, req, resp);
			
			Map<Long, List<Long>> reqOrderd2Ids = resp.getReqOrderd2Ids() == null ? //
					new HashMap<>() : resp.getReqOrderd2Ids();
			reqOrderd2Ids.put(d2.getReqOrderd2Id(), reqOrderd2dIds);
			resp.setReqOrderd2Ids(reqOrderd2Ids);
		}
		
		// 清空舊明細資料
		removeOldD2(oldD2List);
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
	 * D2 不像 D1、D3 在 postDoAccept() 方法中組成 identif_data，<br>
	 * 是因為它們都需要在"同意"後更新其它明細表(ex: tsmp_dp_api_auth2)<br>
	 * 所以才"順便"組資料, 減少資料庫存取次數
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected <R extends DpReqServiceResp> void postDoAllAccept(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo) {
		StringBuffer sb = new StringBuffer();
		sb.append("reqOrderNo=" + vo.getM().getReqOrderNo());

		final Long reqOrdermId = vo.getM().getReqOrdermId();
		List<TsmpDpReqOrderd2> d2List = getTsmpDpReqOrderd2Dao().findByRefReqOrdermId(reqOrdermId);
		if (d2List != null && !d2List.isEmpty()) {
			List<Map<String, Object>> d2Datas = new ArrayList<>();

			Map<String, Object> d2Data = null;
			List<TsmpDpReqOrderd2d> d2dList = null;
			TsmpDpThemeCategory tc = null;
			for(TsmpDpReqOrderd2 d2 : d2List) {
				d2Data = new HashMap<>();
				d2Data.put("apiName", getApiName(d2.getApiUid()));
				d2Data.put("themeName", new ArrayList<String>());

				d2dList = getTsmpDpReqOrderd2dDao().findByReqOrderd2Id(d2.getReqOrderd2Id());
				if (d2dList != null && !d2dList.isEmpty()) {
					for(TsmpDpReqOrderd2d d2d : d2dList) {
						tc = getTsmpDpThemeCategoryDao().findById(d2d.getRefThemeId()).orElse(null);
						if (tc != null) {
							((ArrayList<String>) d2Data.get("themeName")).add( tc.getApiThemeName() );
						}
					}
				}
				
				d2Datas.add(d2Data);
			}
			
			sb.append(", " + d2Datas);
		}
		
		vo.setIndentifData(sb.toString());
		super.postDoAllAccept(q, vo);
	}

	@Override
	protected <R extends DpReqServiceResp> void postDoDenied(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo, String locale, InnerInvokeParam iip) {
		super.postDoDenied(q, vo, locale, iip);

		// 寄發簽核Mail通知
		TsmpAuthorization auth = new TsmpAuthorization();
		auth.setUserName(q.getSignUserName());
		getSendReviewMailService().sendEmail(auth, vo.getM().getReqType(), vo.getM().getReqOrdermId(), vo.getM().getReqOrderNo(), locale);
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

	protected void processApiAttachment(String reqSubtype, TsmpDpReqOrderd2 d2, Map<String, String> apiFileMapping //
			, DpReqServiceResp_D2 resp) {
		this.logger.debug("apiFileMapping: " + apiFileMapping);
		
		List<Long> fileIds = new ArrayList<>();

		final String apiUid = d2.getApiUid();

		// 依照簽核類型做不同處理
		if (TsmpDpReqReviewType.API_ON_OFF.API_ON.isValueEquals(reqSubtype)) {
			// 將暫存檔案(API說明文件)搬到d2的目錄下
			if (apiFileMapping != null && !apiFileMapping.isEmpty()) {
				String tempFilename = apiFileMapping.get(apiUid);
				fileIds = saveApiAttachment(d2, tempFilename, resp);
			}
		} else if (TsmpDpReqReviewType.API_ON_OFF.API_OFF.isValueEquals(reqSubtype)) {
			// 將已存在API_ATTCHMENT下的檔案複製到新d2的目錄下
			String oriFileName = (apiFileMapping == null ? null : apiFileMapping.get(apiUid));
			fileIds = copyFileFromExtToD2(d2, apiUid, oriFileName);
		} else if (TsmpDpReqReviewType.API_ON_OFF.API_ON_UPDATE.isValueEquals(reqSubtype)) {
			// 若檔名為暫存格式, 則將暫存檔案搬到d2的目錄下; 若為一般檔名, 則將已存在API_ATTCHMENT下的檔案複製到新d2的目錄下
			if (apiFileMapping != null && !apiFileMapping.isEmpty()) {
				String filename = apiFileMapping.get(apiUid);
				boolean isTempFile = getFileHelper().isTempFile(filename);
				this.logger.debug(String.format("判斷是否為暫存檔: %s -> %s", filename, isTempFile));
				if ( isTempFile ) {
					fileIds = saveApiAttachment(d2, filename, resp);
				} else {
					fileIds = copyFileFromExtToD2(d2, apiUid, filename);
				}
			}
		}

		Map<Long, Long> d2FileMapping = resp.getReqOrderd2FileIds() == null ? //
				new HashMap<>() : resp.getReqOrderd2FileIds();
		Long fileId = fileIds.isEmpty() ? null : fileIds.get(0);
		d2FileMapping.put(d2.getReqOrderd2Id(), fileId);
		resp.setReqOrderd2FileIds(d2FileMapping);
	}

	private List<Long> saveApiAttachment(TsmpDpReqOrderd2 d2, String tempFilename, DpReqServiceResp_D2 resp) {
		final Long d2Id = d2.getReqOrderd2Id();
		if (StringUtils.isEmpty(tempFilename) || d2Id == null) {
			return Collections.emptyList();
		}
		List<Long> fileIds = new ArrayList<>();
		//String tsmpDpFilePath = FileHelper.getTsmpDpFilePath(TsmpDpFileType.D2_ATTACHMENT, d2Id);
		try {
			TsmpDpFile dpFile = getFileHelper().moveTemp(d2.getCreateUser(), TsmpDpFileType.D2_ATTACHMENT, d2Id, tempFilename, true, false);
			fileIds.add(dpFile.getFileId());
			/*Path apiAttachment = getFileHelper().moveTemp(tsmpDpFilePath, tempFilename);
			if (apiAttachment != null) {
				// 紀錄資料庫
				TsmpDpFile dpFile = new TsmpDpFile();
				dpFile.setFileName(apiAttachment.getFileName().toString());	// 若檔名有重複, 上傳後會自動更名, 需儲存修改後的檔名
				dpFile.setFilePath(tsmpDpFilePath);
				dpFile.setRefFileCateCode(TsmpDpFileType.D2_ATTACHMENT.value());
				dpFile.setRefId(d2Id);
				dpFile.setCreateUser(d2.getCreateUser());
				dpFile = getTsmpDpFileDao().save(dpFile);
				fileIds.add(dpFile.getFileId());
			}*/
		} catch (Exception e) {
			logger.debug("" + e);
		}
		return fileIds;
	}

	private List<Long> copyFileFromExtToD2(TsmpDpReqOrderd2 d2, String apiUid, String oriFileName) {
		if (StringUtils.isEmpty(oriFileName)) {
			return Collections.emptyList();
		}
		
		TsmpApiExt ext = getTsmpApiExtDao().queryByApiUid(apiUid);
		if (ext == null) {
			this.logger.debug("找不到API延伸檔, 無法複製檔案, apiUid=" + apiUid);
			return Collections.emptyList();
		}
		
		List<TsmpDpFile> oriDpFiles = getTsmpDpFileDao().findByRefFileCateCodeAndRefIdAndFileName( //
				TsmpDpFileType.API_ATTACHMENT.value(), ext.getApiExtId(), oriFileName);
		if (oriDpFiles == null || oriDpFiles.isEmpty()) {
			this.logger.debug("沒有可複製的API說明文件, apiExtId=" + ext.getApiExtId());
			return Collections.emptyList();
		}
		List<Long> fileIds = new ArrayList<>();
		String d2FilePath = FileHelper.getTsmpDpFilePath(TsmpDpFileType.D2_ATTACHMENT, d2.getReqOrderd2Id());
		oriDpFiles.forEach((oriDpFile) -> {
			try {
				byte[] oriFile = null;
				if("Y".equals(oriDpFile.getIsBlob()) && oriDpFile.getBlobData() != null) {
					oriFile = getFileHelper().downloadByPathAndName(oriDpFile.getFilePath(), oriDpFile.getFileName());
				}else {
					oriFile = getFileHelper().download01(oriDpFile.getFilePath(), oriDpFile.getFileName());
				}
				
				if (oriFile != null) {
					TsmpDpFile dpFile = getFileHelper().upload(d2.getCreateUser(), TsmpDpFileType.D2_ATTACHMENT
							, d2.getReqOrderd2Id(), oriFileName, oriFile, "N");
					if(dpFile != null) {
						fileIds.add(dpFile.getFileId());
					}
					/*Path d2File = getFileHelper().upload(d2FilePath, oriFileName, oriFile);
					if (d2File != null) {
						// 紀錄資料庫
						TsmpDpFile dpFile = new TsmpDpFile();
						dpFile.setFileName(d2File.getFileName().toString());	// 若檔名有重複, 上傳後會自動更名, 需儲存修改後的檔名
						dpFile.setFilePath(d2FilePath);
						dpFile.setRefFileCateCode(TsmpDpFileType.D2_ATTACHMENT.value());
						dpFile.setRefId(d2.getReqOrderd2Id());
						dpFile.setCreateUser(d2.getCreateUser());
						dpFile = getTsmpDpFileDao().save(dpFile);
						fileIds.add(dpFile.getFileId());
					}*/
				}
			} catch (Exception e) {
				logger.debug("" + e);
			}
		});
		return fileIds;
	}

	private TsmpDpReqOrderd2 saveD2(Long mId, String apiUid, String publicFlag, String createUser) {
		TsmpDpReqOrderd2 d2 = new TsmpDpReqOrderd2();
		d2.setRefReqOrdermId(mId);
		d2.setApiUid(apiUid);
		d2.setPublicFlag(publicFlag);
		d2.setCreateDateTime(DateTimeUtil.now());
		d2.setCreateUser(createUser);
		d2 = getTsmpDpReqOrderd2Dao().save(d2);
		return d2;
	}

	private void saveD2Attachment(TsmpDpReqOrderd2 d2, List<TsmpDpReqOrderd2> oldD2List //
			, DpReqServiceUpdateReq_D2 req, DpReqServiceResp_D2 resp) {
		final Long d2Id = d2.getReqOrderd2Id();
		final String apiUid = d2.getApiUid();
		if (d2Id == null || StringUtils.isEmpty(apiUid)) {
			return;
		}

		final Long oldD2Id = getOldD2Id(apiUid, oldD2List);
		final String oriFileName = req.getOriApiMapFileName().get(apiUid);	// 實際檔名
		String newFileName = req.getNewApiMapFileName().get(apiUid);	// 可能是暫存檔名
		Long newFileId = null;
		if (!StringUtils.isEmpty(oriFileName) && !StringUtils.isEmpty(newFileName) && oriFileName.equals(newFileName)) {
			// 如果檔案無異動, 還是要把舊D2資料夾下的檔案, 搬到新的D2資料夾裡面
			List<Long> newFileIds = copyApiAttachment(d2Id, oldD2Id, oriFileName, d2.getCreateUser());
			if (newFileIds != null && !newFileIds.isEmpty()) {
				newFileId = newFileIds.get(0);
			}
		} else if (!StringUtils.isEmpty(newFileName)) {
			// 新增檔案
			boolean isTempFile = getFileHelper().isTempFile(newFileName);
			this.logger.debug(String.format("判斷是否為暫存檔: %s -> %s", newFileName, isTempFile));
			List<Long> newFileIds = null;
			if (isTempFile) {
				// 如果是暫存檔, 表示更新後有上傳新的API附件
				newFileIds = saveApiAttachment(d2, newFileName, resp);
			} else {
				// 否則複製原有的 API 附件, 作為 D2 申請單的附件
				newFileIds = copyFileFromExtToD2(d2, apiUid, newFileName);
			}
			if (!CollectionUtils.isEmpty(newFileIds)) {
				newFileId = newFileIds.get(0);
			}
			/*
			try {
				TsmpDpFile dpFile = getFileHelper().moveTemp(d2.getCreateUser(), TsmpDpFileType.D2_ATTACHMENT, d2Id, newFileName, true, false);
				newFileName = dpFile.getFileName();
				newFileId = dpFile.getFileId();
			} catch (Exception e) {
				logger.debug("" + e);
			}
			*/
		}
		
		Map<Long, Long> reqOrderd2FileIds = resp.getReqOrderd2FileIds() == null ?
				new HashMap<>() : resp.getReqOrderd2FileIds();
		reqOrderd2FileIds.put(d2.getReqOrderd2Id(), newFileId);
		resp.setReqOrderd2FileIds(reqOrderd2FileIds);
	}

	private Long getOldD2Id(String apiUid, List<TsmpDpReqOrderd2> oldD2List) {
		if (oldD2List != null && !oldD2List.isEmpty()) {
			for(TsmpDpReqOrderd2 d2 : oldD2List) {
				if (d2.getApiUid().equals(apiUid)) {
					return d2.getReqOrderd2Id();
				}
			}
		}
		
		return null;
	}

	private List<Long> copyApiAttachment(Long newD2Id, Long oldD2Id, String fileName, String userName) {
		if (oldD2Id == null) {
			return Collections.emptyList();
		}

		List<TsmpDpFile> oldDpFiles = getTsmpDpFileDao().findByRefFileCateCodeAndRefIdAndFileName( //
				TsmpDpFileType.D2_ATTACHMENT.value(), oldD2Id, fileName);
		if (oldDpFiles == null || oldDpFiles.isEmpty()) {
			return Collections.emptyList();
		}

		List<Long> newFileIds = new ArrayList<>();

		for(TsmpDpFile oldDpFile : oldDpFiles) {
			try {
				byte[] oriFile = null;
				if("Y".equals(oldDpFile.getIsBlob()) && oldDpFile.getBlobData() != null) {
					oriFile = getFileHelper().downloadByPathAndName(oldDpFile.getFilePath(), oldDpFile.getFileName());
				}else {
					oriFile = getFileHelper().download01(oldDpFile.getFilePath(), oldDpFile.getFileName());
				}

				if (oriFile != null) {
					TsmpDpFile dpFile = getFileHelper().upload(userName, TsmpDpFileType.D2_ATTACHMENT
							, newD2Id, fileName, oriFile, "N");
					if(dpFile != null) {
						newFileIds.add(dpFile.getFileId());
					}
					/*String newTsmpDpFilePath = FileHelper.getTsmpDpFilePath(TsmpDpFileType.D2_ATTACHMENT, newD2Id);
					Path newFile = getFileHelper().upload(newTsmpDpFilePath, fileName, oriFile);
					if (newFile != null) {
						// 紀錄資料庫
						TsmpDpFile dpFile = new TsmpDpFile();
						dpFile.setFileName(newFile.getFileName().toString());	// 若檔名有重複, 上傳後會自動更名, 需儲存修改後的檔名
						dpFile.setFilePath(newTsmpDpFilePath);
						dpFile.setRefFileCateCode(TsmpDpFileType.D2_ATTACHMENT.value());
						dpFile.setRefId(newD2Id);
						dpFile.setCreateUser(userName);
						dpFile = getTsmpDpFileDao().save(dpFile);
						newFileIds.add(dpFile.getFileId());
					}*/
				}
			} catch (Exception e) {
				this.logger.error(String.format("搬移檔案(%s)時出錯: from d2Id %d to %d", fileName, oldD2Id, newD2Id));
			}
		}

		return newFileIds;
	}

	private void removeOldD2(List<TsmpDpReqOrderd2> oldD2List) {
		if (oldD2List == null || oldD2List.isEmpty()) {
			return;
		}

		Long oldD2Id = null;
		for(TsmpDpReqOrderd2 oldD2 : oldD2List) {
			oldD2Id = oldD2.getReqOrderd2Id();

			// #1. 刪除舊的D2d
			List<TsmpDpReqOrderd2d> oldD2dList = getTsmpDpReqOrderd2dDao().findByReqOrderd2Id(oldD2Id);
			if (oldD2dList != null) {
				getTsmpDpReqOrderd2dDao().deleteAll(oldD2dList);
			}
			// #2. 刪除舊檔案
			List<TsmpDpFile> oldDpFiles = getTsmpDpFileDao() //
					.findByRefFileCateCodeAndRefId(TsmpDpFileType.D2_ATTACHMENT.value(), oldD2Id);
			if (oldDpFiles != null && !oldDpFiles.isEmpty()) {
				boolean isFileExists = false;
				for(TsmpDpFile oldDpFile : oldDpFiles) {
					/* 20210205; Kim; 無論檔案是否為空，資料都應刪除
					if("Y".equals(oldDpFile.getIsBlob()) && oldDpFile.getBlobData() != null) {
					*/
					if("Y".equals(oldDpFile.getIsBlob())) {
						getTsmpDpFileDao().delete(oldDpFile);
					} else {
						isFileExists = getFileHelper().exists(oldDpFile.getFilePath(), oldDpFile.getFileName());
						if (isFileExists) {
							try {
								getFileHelper().remove01(oldDpFile.getFilePath(), oldDpFile.getFileName(), (filename) -> {
									getTsmpDpFileDao().delete(oldDpFile);
								});
							} catch (Exception e) {
								logger.debug("" + e);
							}
						}
					}
				}
				
				// 如果舊檔案都刪完了, 就把資料夾也刪了
				oldDpFiles = getTsmpDpFileDao().findByRefFileCateCodeAndRefId( //
						TsmpDpFileType.D2_ATTACHMENT.value(), oldD2Id);
				if (oldDpFiles == null || oldDpFiles.isEmpty()) {
					try {
						getFileHelper().remove01(TsmpDpFileType.D2_ATTACHMENT, oldD2Id, null, null);
					} catch (Exception e) {
						this.logger.warn("無法刪除舊的D2資料夾: " + FileHelper.getTsmpDpFilePath(TsmpDpFileType.D2_ATTACHMENT, oldD2Id));
					}
				}
			}
			// #3. 刪除舊的D2
			getTsmpDpReqOrderd2Dao().delete(oldD2);
		}
	}

	private String getApiName(String apiUid) {
		List<TsmpApi> apiList = getTsmpApiDao().findByApiUid(apiUid);
		if (apiList != null && !apiList.isEmpty()) {
			return apiList.get(0).getApiName();
		}
		return new String();
	}

	protected TsmpDpReqOrderd2Dao getTsmpDpReqOrderd2Dao() {
		return this.tsmpDpReqOrderd2Dao;
	}

	protected TsmpDpReqOrderd2dDao getTsmpDpReqOrderd2dDao() {
		return this.tsmpDpReqOrderd2dDao;
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

	protected DPB0065Job getDpb0065Job(String locale) {
		return (DPB0065Job) getCtx().getBean("dpb0065Job", locale, this.tsmpDpItemsCacheProxy, this.seqStoreDao);
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpDpThemeCategoryDao getTsmpDpThemeCategoryDao() {
		return this.tsmpDpThemeCategoryDao;
	}

}
