package tpi.dgrv4.dpaa.component.req;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.*;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.constant.RegexpConstant;
import tpi.dgrv4.dpaa.constant.TsmpDpAaConstant;
import tpi.dgrv4.dpaa.service.DgrAuditLogService;
import tpi.dgrv4.dpaa.service.SendAPIApplicationMailService;
import tpi.dgrv4.dpaa.service.SendClientRegMailService;
import tpi.dgrv4.dpaa.service.SendReviewMailService;
import tpi.dgrv4.dpaa.util.OAuthUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.constant.TsmpSequenceName;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.daoService.SeqStoreService;
import tpi.dgrv4.entity.entity.*;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd3;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.repository.*;
import tpi.dgrv4.gateway.component.FileHelper;
import tpi.dgrv4.gateway.component.job.appt.ApptJobDispatcher;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

import java.util.List;
import java.util.Optional;

/**
 * 處理"用戶註冊"簽核流程
 * @author Kim
 *
 */
@Service(value = "dpReqServiceImpl_D3")
public class DpReqServiceImpl_D3 extends DpReqServiceAbstract implements DpReqServiceIfs {

	private TPILogger logger = TPILogger.tl;

	private BcryptParamHelper bcryptParamHelper;
	private TsmpGroupDao tsmpGroupDao;
	private TsmpClientGroupDao tsmpClientGroupDao;
	private TsmpApiDao tsmpApiDao;
	private TsmpGroupApiDao tsmpGroupApiDao;
	private TsmpDpClientextDao tsmpDpClientextDao;
	private OauthClientDetailsDao oauthClientDetailsDao;
	private TsmpClientDao tsmpClientDao;
	private TsmpDpReqOrderd3Dao tsmpDpReqOrderd3Dao;
	private DgrAuditLogService dgrAuditLogService;

	@Autowired
	public DpReqServiceImpl_D3(ApptJobDispatcher apptJobDispatcher, TsmpClientDao tsmpClientDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpDpReqOrdermDao tsmpDpReqOrdermDao, TsmpUserDao tsmpUserDao,
			SeqStoreService seqStoreService, TsmpDpReqOrdersDao tsmpDpReqOrdersDao, FileHelper fileHelper,
			TsmpDpFileDao tsmpDpFileDao, TsmpDpChkLayerDao tsmpDpChkLayerDao, TsmpDpChkLogDao tsmpDpChkLogDao,
			BcryptParamHelper bcryptParamHelper, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpDpApptJobDao tsmpDpApptJobDao, SendReviewMailService mailService,
			SendClientRegMailService sendClientRegMailService,
			SendAPIApplicationMailService sendAPIApplicationMailService, DgrAuditLogService dgrAuditLogService,
			DgrAcIdpUserDao dgrAcIdpUserDao, TsmpGroupDao tsmpGroupDao, TsmpClientGroupDao tsmpClientGroupDao,
			TsmpApiDao tsmpApiDao, TsmpGroupApiDao tsmpGroupApiDao, TsmpDpClientextDao tsmpDpClientextDao,
			OauthClientDetailsDao oauthClientDetailsDao, TsmpDpReqOrderd3Dao tsmpDpReqOrderd3Dao) {
		super(apptJobDispatcher, tsmpClientDao, tsmpDpItemsCacheProxy, tsmpDpReqOrdermDao, tsmpUserDao, seqStoreService,
				tsmpDpReqOrdersDao, fileHelper, tsmpDpFileDao, tsmpDpChkLayerDao, tsmpDpChkLogDao, bcryptParamHelper,
				tsmpOrganizationDao, tsmpDpApptJobDao, mailService, sendClientRegMailService,
				sendAPIApplicationMailService, dgrAuditLogService, dgrAcIdpUserDao);
		this.bcryptParamHelper = bcryptParamHelper;
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpClientGroupDao = tsmpClientGroupDao;
		this.tsmpApiDao = tsmpApiDao;
		this.tsmpGroupApiDao = tsmpGroupApiDao;
		this.tsmpDpClientextDao = tsmpDpClientextDao;
		this.oauthClientDetailsDao = oauthClientDetailsDao;
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpDpReqOrderd3Dao = tsmpDpReqOrderd3Dao;
		this.dgrAuditLogService = dgrAuditLogService;
	}

	@Override
	protected <Q extends DpReqServiceSaveDraftReq> void checkDetailReq(Q q, String locale) throws TsmpDpAaException {
		// 一定要先轉型
		DpReqServiceSaveDraftReq_D3 req = castSaveDraftReq(q, DpReqServiceSaveDraftReq_D3.class);
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
		final String clientName = req.get_clientName();
		final String clientBlock = req.get_clientBlock();
		String encPublicFlag = req.get_encPublicFlag();
		if (StringUtils.isEmpty(clientId) ||
			StringUtils.isEmpty(clientName) ||
			StringUtils.isEmpty(clientBlock) ||
			StringUtils.isEmpty(encPublicFlag)
		) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		// 20210108; 用戶帳號(clientId)僅限英、數字、底線及dash
		if (!clientId.matches(RegexpConstant.ENGLISH_NUMBER)) {
			throw TsmpDpAaRtnCode._1477.throwing();
		}
		
		// 20210108; 用戶代號(clientName)僅可輸入英文字母(a~z,A~Z)及數字且不含空白
		if (!ServiceUtil.isNumericOrAlphabetic(clientName)) {
			throw TsmpDpAaRtnCode._1325.throwing();
		}

		// 20210108; Email須符合電子郵件格式
		final String clientEmails = req.get_emails();
		if (!StringUtils.isEmpty(clientEmails) && !clientEmails.matches(RegexpConstant.EMAIL)) {
			throw TsmpDpAaRtnCode._1332.throwing();
		}
		
		// 20210108; 申請內容說明不可超出1000字元
		final String reqDesc = req.getReqDesc();
		if (!StringUtils.isEmpty(reqDesc) && reqDesc.length() > 1000) {
			throw TsmpDpAaRtnCode._1478.throwing(String.valueOf(1000), String.valueOf(reqDesc.length()));
		}
		
		// 會員密碼最少碼
		String pwd = new String(ServiceUtil.base64Decode(clientBlock));
		if (pwd == null || pwd.length() < 6) {
			throw TsmpDpAaRtnCode.ERROR_PASSWORD.throwing();
		}
		
		// Client Name 不可在 tsmp_client 中重複
		List<TsmpClient> clientList = getTsmpClientDao().findByClientName(clientName);
		if (!clientList.isEmpty()) {
			throw TsmpDpAaRtnCode.ERROR_CLIENT_ID_EXISTS.throwing();
		}
		
		try {
			encPublicFlag = getBcryptParamHelper().decode(encPublicFlag, "API_AUTHORITY", locale);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		// 解密完放回, 方便後續使用
		req.set_encPublicFlag(encPublicFlag);
	}

	@Override
	protected <R extends DpReqServiceResp, Q extends DpReqServiceSaveDraftReq> void saveDetail( //
			TsmpDpReqOrderm m, Q q, R r, InnerInvokeParam iip) {
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.ADD_CLIENT.value());
		
		// 一定要先轉型
		DpReqServiceSaveDraftReq_D3 req = castSaveDraftReq(q, DpReqServiceSaveDraftReq_D3.class);
		DpReqServiceResp_D3 resp = castResp(r, DpReqServiceResp_D3.class);

		// 寫入必要Table
		String clientName = req.get_clientName();
		TsmpGroup group = saveGroup(m.getCreateUser(), clientName);

		String clientId = req.get_clientId();
		String emails = req.get_emails();
		TsmpClient client = saveClient(m, clientId, clientName, emails, iip);

		saveClientAndGroup(client, group);

		insertGroupApi(group.getGroupId());

		String contentTxt = m.getReqDesc();
		String publicFlag = req.get_encPublicFlag();
		saveClientExt(m, clientId, clientName, contentTxt, publicFlag, iip);
		
		String clientBlock = req.get_clientBlock();
		saveOAuth2Table(clientId, clientBlock, iip);
		
		// 寫入D3
		TsmpDpReqOrderd3 d3 = new TsmpDpReqOrderd3();
		d3.setRefReqOrdermId(m.getReqOrdermId());
		d3.setClientId(clientId);
		d3.setCreateDateTime(DateTimeUtil.now());
		d3.setCreateUser(m.getCreateUser());
		d3 = getTsmpDpReqOrderd3Dao().saveAndFlush(d3);

		resp.setReqOrderd3Id(d3.getReqOrderd3Id());
	}

	@Override
	protected void deleteDraftDetail(TsmpDpReqOrderm m) throws TsmpDpAaException {
		// 找出原本申請的ClientId
		final Long reqOrdermId = m.getReqOrdermId();
		TsmpDpReqOrderd3 d3 = getTsmpDpReqOrderd3Dao().findFirstByRefReqOrdermId(reqOrdermId);
		if (d3 == null) {
			this.logger.debug(String.format("TsmpDpReqOrderd3 is not found. (req_orderm_id=%d)", reqOrdermId));
			throw TsmpDpAaRtnCode._1217.throwing();
		}
		final String clientId = d3.getClientId();
		TsmpClient tsmpClient = getTsmpClientDao().findById(clientId).orElse(null);
		if (tsmpClient == null) {
			this.logger.debug(String.format("TsmpClient is not found. (client_id=%s)", clientId));
			throw TsmpDpAaRtnCode._1217.throwing();
		}
		// 刪除 TSMP_CLIENT_GROUP
		List<TsmpClientGroup> cgList = getTsmpClientGroupDao().findByClientId(clientId);
		if (!CollectionUtils.isEmpty(cgList)) {
			getTsmpClientGroupDao().deleteAll(cgList);
		}
		// 刪除 TSMP_GROUP
		final String clientName = tsmpClient.getClientName();
		List<TsmpGroup> gList = getTsmpGroupDao().findByGroupName(clientName);
		if (!CollectionUtils.isEmpty(gList)) {
			gList.forEach((g) -> {
				// 刪除 TSMP_GROUP_API
				List<TsmpGroupApi> gaList = getTsmpGroupApiDao().findByGroupIdAndModuleName(g.getGroupId(), "tsmpdpapi");
				if (!CollectionUtils.isEmpty(gaList)) {
					getTsmpGroupApiDao().deleteAll(gaList);
				}

				getTsmpGroupDao().delete(g);
			});
		}
		
		//目前deleteDraftDetail這個源頭來自DPB0067Job和HousekeepingJob.deleteSignOff呼叫
		InnerInvokeParam iip = null;
		try {
			iip = InnerInvokeParam.getInstance(new HttpHeaders(), null, new TsmpAuthorization());
		}catch(Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
		}
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.DELETE_CLIENT.value());
		
		// 刪除 TSMP_DP_CLIENTEXT
		TsmpDpClientext extVo = getTsmpDpClientextDao().findById(clientId).orElse(null);
		if (extVo != null) {
			String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, extVo); //舊資料統一轉成 String
			
			getTsmpDpClientextDao().delete(extVo);
			
			//寫入 Audit Log D
			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
					TsmpDpClientext.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);
			
		}
		// 刪除 OAUTH_CLIENT_DETAILS
		OauthClientDetails oauthVo = getOAuthDao().findById(clientId).orElse(null);
		if (oauthVo != null) {
			String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, oauthVo); //舊資料統一轉成 String
			
			getOAuthDao().delete(oauthVo);
			
			//寫入 Audit Log D
			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
					OauthClientDetails.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);
		}
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, tsmpClient); //舊資料統一轉成 String
		
		// 刪除 TSMP_CLIENT
		getTsmpClientDao().delete(tsmpClient);
		
		//寫入 Audit Log D
		lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
				TsmpClient.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);
		
		// 刪除 TSMP_DP_REQ_ORDERD3
		do {
			getTsmpDpReqOrderd3Dao().delete(d3);
			d3 = getTsmpDpReqOrderd3Dao().findFirstByRefReqOrdermId(reqOrdermId);
		} while(d3 != null);
	}

	@Override
	protected <Q extends DpReqServiceUpdateReq> void checkDetailUpdateReq(Q q, String locale) throws TsmpDpAaException {
		// 一定要先轉型
		DpReqServiceUpdateReq_D3 req = castUpdateReq(q, DpReqServiceUpdateReq_D3.class);
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
		final String clientName = req.get_clientName();
		final String clientBlock = req.get_clientBlock();
		String encPublicFlag = req.get_encPublicFlag();
		if (StringUtils.isEmpty(clientId)){
			throw TsmpDpAaRtnCode._1343.throwing();
		}
		if (StringUtils.isEmpty(clientName)){
			throw TsmpDpAaRtnCode._1324.throwing();
		}
		if (StringUtils.isEmpty(clientBlock)){
			throw TsmpDpAaRtnCode._1330.throwing();
		}
		
		if (StringUtils.isEmpty(encPublicFlag)) {
			throw TsmpDpAaRtnCode._1336.throwing();
		}

		/* 20210108; 更新時，用戶帳號(clientId)不限英、數字、底線及dash，因為可能有舊資料是 base64Encode(clientName) 而來
		if (!clientId.matches(RegexpConstant.ENGLISH_NUMBER)) {
			throw TsmpDpAaRtnCode._1477.throwing();
		}
		*/
		
		// 20210108; 用戶代號(clientName)僅可輸入英文字母(a~z,A~Z)及數字且不含空白
		if (!ServiceUtil.isNumericOrAlphabetic(clientName)) {
			throw TsmpDpAaRtnCode._1325.throwing();
		}

		// 20210108; Email須符合電子郵件格式
		final String clientEmails = req.get_emails();
		if (!StringUtils.isEmpty(clientEmails) && !clientEmails.matches(RegexpConstant.EMAIL)) {
			throw TsmpDpAaRtnCode._1332.throwing();
		}
		
		// 20210108; 申請內容說明不可超出1000字元
		final String reqDesc = req.getReqDesc();
		if (!StringUtils.isEmpty(reqDesc) && reqDesc.length() > 1000) {
			throw TsmpDpAaRtnCode._1478.throwing(String.valueOf(1000), String.valueOf(reqDesc.length()));
		}
		
		// 會員密碼最少碼
		String pwd = new String(ServiceUtil.base64Decode(clientBlock));
		if (pwd == null || pwd.length() < 6) {
			throw TsmpDpAaRtnCode.ERROR_PASSWORD.throwing();
		}
		
		/**
		 * P.S "更新"前不用檢查 req.get_clientId() 是否已存在 tsmp_client 中
		 * 因為在"儲存草稿"時, 就已經寫了一筆到 tsmp_client
		 * 所以如果在這裡檢查, 一定會有一筆已存在
		 */
		
		try {
			encPublicFlag = getBcryptParamHelper().decode(encPublicFlag, "API_AUTHORITY", locale);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		// 解密完放回, 方便後續使用
		req.set_encPublicFlag(encPublicFlag);
	}

	@Override
	protected <R extends DpReqServiceResp, Q extends DpReqServiceUpdateReq> void updateDetail(TsmpDpReqOrderm m, Q q,
			R r, InnerInvokeParam iip) {
		
		// 一定要先轉型
		DpReqServiceUpdateReq_D3 req = castUpdateReq(q, DpReqServiceUpdateReq_D3.class);
		DpReqServiceResp_D3 resp = castResp(r, DpReqServiceResp_D3.class);

		// 找出原本要申請的ClientId
		TsmpDpReqOrderd3 d3 = getTsmpDpReqOrderd3Dao().findFirstByRefReqOrdermId(m.getReqOrdermId());
		if (d3 == null) {
			throw TsmpDpAaRtnCode._1217.throwing();
		}
		final String oriClientId = d3.getClientId();
		final String newClientId = req.get_clientId();
		
		// 如果更新後, ClientId 不變, 則相關 Table 執行 Update 即可; 反之則 Delete + Create
		if (oriClientId.equals(newClientId)) {
			clientCascadeUpdate(m, req, resp, iip);
		} else {
			// 把原本的相關資料都刪掉
			clientCascadeRemove(oriClientId, iip);
			
			// 寫入必要Table
			String clientName = req.get_clientName();
			TsmpGroup group = saveGroup(m.getUpdateUser(), clientName);

			String clientId = req.get_clientId();
			String emails = req.get_emails();
			TsmpClient client = saveClient(m, clientId, clientName, emails, iip);

			saveClientAndGroup(client, group);

			insertGroupApi(group.getGroupId());

			String contentTxt = m.getReqDesc();
			String publicFlag = req.get_encPublicFlag();
			saveClientExt(m, clientId, clientName, contentTxt, publicFlag, iip);
			
			String clientBlock = req.get_clientBlock();
			saveOAuth2Table(clientId, clientBlock, iip);
			
			// 寫入D3
			d3 = new TsmpDpReqOrderd3();
			d3.setRefReqOrdermId(m.getReqOrdermId());
			d3.setClientId(clientId);
			d3.setCreateDateTime(DateTimeUtil.now());
			d3.setCreateUser(m.getUpdateUser());
			d3 = getTsmpDpReqOrderd3Dao().saveAndFlush(d3);

			resp.setReqOrderd3Id(d3.getReqOrderd3Id());
		}
	}
	
	private void clientCascadeRemove(String clientId, InnerInvokeParam iip) {
		Optional<TsmpClient> opt_c = getTsmpClientDao().findById(clientId);
		if (!opt_c.isPresent()) {
			return;
		}
		
		TsmpClient client = opt_c.get();
		final String clientName = client.getClientName();
		TsmpGroup g = getTsmpGroupDao().findFirstByGroupName(clientName);
		if (g != null) {
			TsmpClientGroupId cgId = new TsmpClientGroupId(clientId, g.getGroupId());
			Optional<TsmpClientGroup> opt_cg = getTsmpClientGroupDao().findById(cgId);
			if (opt_cg.isPresent()) {
				// tsmp_client_group
				getTsmpClientGroupDao().delete(opt_cg.get());
			}
			List<TsmpGroupApi> gaList = getTsmpGroupApiDao().findByGroupId(g.getGroupId());
			if (gaList != null && !gaList.isEmpty()) {
				// tsmp_gropu_api
				getTsmpGroupApiDao().deleteAll(gaList);
			}
			// tsmp_group
			getTsmpGroupDao().delete(g);
		}
		// tsmp_dp_clientext
		Optional<TsmpDpClientext> opt_ext = getTsmpDpClientextDao().findById(clientId);
		if (opt_ext.isPresent()) {
			TsmpDpClientext vo = opt_ext.get();
			String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, vo); //舊資料統一轉成 String
			
			getTsmpDpClientextDao().delete(vo);
			
			//寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
					TsmpDpClientext.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);
		}
		// oauth_client_details
		Optional<OauthClientDetails> opt_o = getOAuthDao().findById(clientId);
		if (opt_o.isPresent()) {
			OauthClientDetails vo = opt_o.get();
			String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, vo); //舊資料統一轉成 String
			
			getOAuthDao().delete(vo);
			
			//寫入 Audit Log D
			String lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
					OauthClientDetails.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);
		}
		// tsmp_dp_req_orderd3
		List<TsmpDpReqOrderd3> d3List = getTsmpDpReqOrderd3Dao().findByClientId(clientId);
		if (d3List != null && !d3List.isEmpty()) {
			getTsmpDpReqOrderd3Dao().deleteAll(d3List);
		}
		
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, client); //舊資料統一轉成 String
		// tsmp_client
		getTsmpClientDao().delete(client);
		
		//寫入 Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
				TsmpClient.class.getSimpleName(), TableAct.D.value(), oldRowStr, null);
	}

	private void clientCascadeUpdate(TsmpDpReqOrderm m, DpReqServiceUpdateReq_D3 req, DpReqServiceResp_D3 resp, InnerInvokeParam iip) {
		final String clientId = req.get_clientId();
		Optional<TsmpClient> opt = getTsmpClientDao().findById(clientId);
		if (!opt.isPresent()) {
			throw TsmpDpAaRtnCode._1223.throwing();
		}
		
		TsmpClient oriClient = opt.get();
		
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, oriClient); //舊資料統一轉成 String
		
		final String oriClientName = oriClient.getClientName();
		final String newClientName = req.get_clientName();

		// 比對 client_name 是否更新前/後相同, 若不同, 則須重建 tsmp_group 等相關 Table
		// 因為 tsmp_group 的 group_name 是用 client_name 命名的
		
		// tsmp_client
		oriClient.setClientName(req.get_clientName());
		oriClient.setEmails(req.get_emails());
		oriClient.setUpdateTime(DateTimeUtil.now());
		oriClient.setUpdateUser(req.getUpdateUser());
		oriClient = getTsmpClientDao().save(oriClient);
		
		//寫入 Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
				TsmpClient.class.getSimpleName(), TableAct.U.value(), oldRowStr, oriClient);
		
		if (!oriClientName.equals(newClientName)) {
			TsmpGroup oriG = getTsmpGroupDao().findFirstByGroupName(oriClientName);
			if (oriG != null) {
				// tsmp_client_group
				List<TsmpClientGroup> cgList = getTsmpClientGroupDao().findByClientId(clientId);
				if (cgList != null && !cgList.isEmpty()) {
					for(TsmpClientGroup cg : cgList) {
						if (oriG.getGroupId().equals(cg.getGroupId())) {
							getTsmpClientGroupDao().delete(cg);
						}
					}
					getTsmpClientGroupDao().flush();
				}
				// tsmp_group_api
				List<TsmpGroupApi> gaList = getTsmpGroupApiDao().findByGroupId(oriG.getGroupId());
				if (gaList != null && !gaList.isEmpty()) {
					getTsmpGroupApiDao().deleteAll(gaList);
					getTsmpGroupApiDao().flush();
				}
				// tsmp_group
				getTsmpGroupDao().delete(oriG);
				getTsmpGroupDao().flush();
				oriG = null;
			}
			
			TsmpGroup newG = saveGroup(m.getUpdateUser(), newClientName);

			saveClientAndGroup(oriClient, newG);

			insertGroupApi(newG.getGroupId());
		}
		
		// oauth_client_details
		saveOAuth2Table(clientId, req.get_clientBlock(), iip);
		
		// tsmp_dp_clientext
		Optional<TsmpDpClientext> opt_ext = getTsmpDpClientextDao().findById(clientId);
		if (opt_ext.isPresent()) {
			TsmpDpClientext clientExt = opt_ext.get();
			oldRowStr = getDgrAuditLogService().writeValueAsString(iip, clientExt); //舊資料統一轉成 String
			
			clientExt.setContentTxt(m.getReqDesc());
			clientExt.setPublicFlag(req.get_encPublicFlag());
			clientExt.setUpdateDateTime(DateTimeUtil.now());
			clientExt.setUpdateUser(m.getUpdateUser());
			clientExt = getTsmpDpClientextDao().save(clientExt);
			
			//寫入 Audit Log D
			lineNumber = StackTraceUtil.getLineNumber();
			getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
					TsmpDpClientext.class.getSimpleName(), TableAct.U.value(), oldRowStr, clientExt);
		}
		
		// tsmp_dp_req_orderd3
		TsmpDpReqOrderd3 d3 = getTsmpDpReqOrderd3Dao().findFirstByRefReqOrdermId(m.getReqOrdermId());
		if (d3 != null) {
			d3.setUpdateDateTime(DateTimeUtil.now());
			d3.setUpdateUser(m.getUpdateUser());
			d3 = getTsmpDpReqOrderd3Dao().save(d3);
			resp.setReqOrderd3Id(d3.getReqOrderd3Id());
		}
	}

	@Override
	protected <R extends DpReqServiceResp> void postSubmit(TsmpDpReqOrderm m, DpReqServiceUpdateReq q, R r, String locale, InnerInvokeParam iip) {
		//寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.UPDATE_CLIENT.value());
		
		// 更新 tsmp_dp_clientext
		TsmpDpReqOrderd3 d3 = getTsmpDpReqOrderd3Dao().findFirstByRefReqOrdermId(m.getReqOrdermId());
		if (d3 == null) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		Optional<TsmpDpClientext> opt = getTsmpDpClientextDao().findById(d3.getClientId());
		if (!opt.isPresent()) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		TsmpDpClientext clientExt = opt.get();
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, clientExt); //舊資料統一轉成 String
		
		clientExt.setRegStatus(TsmpDpRegStatus.REVIEWING.value());
		clientExt.setUpdateDateTime(DateTimeUtil.now());
		clientExt.setUpdateUser(q.getUpdateUser());
		clientExt = getTsmpDpClientextDao().save(clientExt);
		
		//寫入 Audit Log D
		lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
				TsmpDpClientext.class.getSimpleName(), TableAct.U.value(), oldRowStr, clientExt);
		
		super.postSubmit(m, q, r, locale, iip);
	}

	@Override
	protected <R extends DpReqServiceResp> void postResubmit(TsmpDpReqOrderm m, DpReqServiceUpdateReq q, R r, String locale, InnerInvokeParam iip) {
		DpReqServiceResp_D3 d3Resp = castResp(r, DpReqServiceResp_D3.class);
		Long d3Id = d3Resp.getReqOrderd3Id();
		if (d3Id == null) {
			throw TsmpDpAaRtnCode._1214.throwing();
		}
		Optional<TsmpDpReqOrderd3> opt_d3 = getTsmpDpReqOrderd3Dao().findById(d3Id);
		if (!opt_d3.isPresent()) {
			throw TsmpDpAaRtnCode._1214.throwing();
		}
		final String clientId = opt_d3.get().getClientId();
		Optional<TsmpDpClientext> opt_ext = getTsmpDpClientextDao().findById(clientId);
		if (!opt_ext.isPresent()) {
			throw TsmpDpAaRtnCode._1214.throwing();
		}
		TsmpDpClientext clientExt = opt_ext.get();
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, clientExt); //舊資料統一轉成 String
		
		clientExt.setRegStatus(TsmpDpRegStatus.RESUBMIT.value());
		clientExt.setResubmitDateTime(DateTimeUtil.now());
		clientExt.setUpdateDateTime(DateTimeUtil.now());
		clientExt.setUpdateUser(q.getUpdateUser());
		clientExt = getTsmpDpClientextDao().save(clientExt);
		
		//寫入 Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
				TsmpDpClientext.class.getSimpleName(), TableAct.U.value(), oldRowStr, clientExt);
		
		super.postResubmit(m, q, r, locale, iip);
	}

	@Override
	protected <R extends DpReqServiceResp> void postDoAccept(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo, String locale, InnerInvokeParam iip) {
		super.postDoAccept(q, vo, locale, iip);

		updateTsmpDpClientextAfterSign(vo, null, q.getSignUserName(), iip);
		
		// 寄發簽核Mail通知
		TsmpAuthorization auth = new TsmpAuthorization();
		auth.setUserName(q.getSignUserName());
		getSendReviewMailService().sendEmail(auth, vo.getM().getReqType(), vo.getM().getReqOrdermId(), vo.getM().getReqOrderNo(), locale);
	}

	@Override
	protected <R extends DpReqServiceResp> void postDoDenied(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo, String locale, InnerInvokeParam iip) {
		super.postDoDenied(q, vo, locale, iip);

		updateTsmpDpClientextAfterSign(vo, TsmpDpRegStatus.RETURN, q.getSignUserName(), iip);
		
		// 寄發簽核Mail通知
		TsmpAuthorization auth = new TsmpAuthorization();
		auth.setUserName(q.getSignUserName());
		getSendReviewMailService().sendEmail(auth, vo.getM().getReqType(), vo.getM().getReqOrdermId(), vo.getM().getReqOrderNo(), locale);
	}

	@Override
	protected <R extends DpReqServiceResp> void postDoEnd(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo, String locale, InnerInvokeParam iip) {
		super.postDoEnd(q, vo, locale, iip);

		updateTsmpDpClientextAfterSign(vo, TsmpDpRegStatus.RETURN, q.getSignUserName(), iip);
		
		// 寄發簽核Mail通知
		TsmpAuthorization auth = new TsmpAuthorization();
		auth.setUserName(q.getSignUserName());
		getSendReviewMailService().sendEmail(auth, vo.getM().getReqType(), vo.getM().getReqOrdermId(), vo.getM().getReqOrderNo(), locale);
	}

	@Override
	protected <R extends DpReqServiceResp> void postDoReturn(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo, String locale, InnerInvokeParam iip) {
		super.postDoReturn(q, vo, locale, iip);

		updateTsmpDpClientextAfterSign(vo, TsmpDpRegStatus.RETURN, q.getSignUserName(), iip);
		
		// 寄發簽核Mail通知
		TsmpAuthorization auth = new TsmpAuthorization();
		auth.setUserName(q.getSignUserName());
		getSendReviewMailService().sendEmail(auth, vo.getM().getReqType(), vo.getM().getReqOrdermId(), vo.getM().getReqOrderNo(), locale);
		
		sendClientReqResultMail(q, vo, TsmpDpRegStatus.RETURN.value());
	}

	private TsmpGroup saveGroup(String createUser, String clientName) {
		TsmpGroup group = new TsmpGroup();
		final Long seq = getSeqStoreService().nextTsmpSequence(TsmpSequenceName.SEQ_TSMP_GROUP_PK);
		if (seq != null) {
			group.setGroupId(seq.toString());
		}
		group.setGroupName(clientName);
		group.setCreateUser(createUser);
		group.setCreateTime(DateTimeUtil.now());
		group = getTsmpGroupDao().save(group);
		return group;
	}

	private TsmpClient saveClient(TsmpDpReqOrderm m, String clientId, String clientName, String emails, InnerInvokeParam iip) {
		TsmpClient client = new TsmpClient();
		client.setClientId(clientId);
		client.setClientName(clientName);
		client.setEmails(emails);
		client.setOwner(TsmpDpAaConstant.TSMP_CLIENT_OWNER);
		client.setCreateTime(DateTimeUtil.now());
		client.setCreateUser(m.getCreateUser());
		client = getTsmpClientDao().save(client);
		
		//寫入 Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
				TsmpClient.class.getSimpleName(), TableAct.C.value(), null, client);
		
		return client;
	}

	private void saveClientAndGroup(TsmpClient client, TsmpGroup group) {
		TsmpClientGroup cg = new TsmpClientGroup();
		cg.setClientId(client.getClientId());
		cg.setGroupId(group.getGroupId());
		getTsmpClientGroupDao().save(cg);
	}

	private void insertGroupApi(String groupId) {
		String moduleName = "tsmpdpapi";
		List<TsmpApi> apiList = getApiList(moduleName);
		if (apiList != null && !apiList.isEmpty()) {
			getTsmpGroupApiDao().deleteByGroupIdAndModuleName(groupId, moduleName);
			//getTsmpGroupApiDao().deleteByGroupIdAndModuleName(groupId, module.getModuleName());
			for(TsmpApi api : apiList) {
				TsmpGroupApi groupApi = new TsmpGroupApi();
				groupApi.setGroupId(groupId);
				groupApi.setApiKey(api.getApiKey());
				groupApi.setModuleName(moduleName);
				groupApi.setModuleVer(null);
				//groupApi.setModuleName(module.getModuleName());
				//groupApi.setModuleVer(module.getModuleVersion());
				groupApi.setCreateTime(DateTimeUtil.now());
				groupApi = getTsmpGroupApiDao().save(groupApi);
			}
		}
	}

	private List<TsmpApi> getApiList(String moduleName) {
		String apiStatus = "1";	// Enabled
		return getTsmpApiDao().findAllByModuleNameAndApiStatus(moduleName, apiStatus);
	}

	private TsmpDpClientext saveClientExt(TsmpDpReqOrderm m, String clientId, //
			String clientName, String contentTxt, String publicFlag, InnerInvokeParam iip) {
		TsmpDpClientext ext = new TsmpDpClientext();
		ext.setClientId(clientId);
		// 取得流水號
		final Long clientSeqId = getSeqStoreService().nextSequence(//
				TsmpDpSeqStoreKey.TSMP_DP_CLIENTEXT, 10000L, 1L);
		if (clientSeqId != null) {
			ext.setClientSeqId(clientSeqId);
		}
		ext.setContentTxt(contentTxt);
		ext.setRegStatus(TsmpDpRegStatus.SAVED.value());
		ext.setPublicFlag(publicFlag);
		ext.setCreateDateTime(DateTimeUtil.now());
		ext.setCreateUser(m.getCreateUser());
		ext = getTsmpDpClientextDao().save(ext);
		
		//寫入 Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
				TsmpDpClientext.class.getSimpleName(), TableAct.C.value(), null, ext);
		
		return ext;
	}

	private <R extends DpReqServiceResp> void updateTsmpDpClientextAfterSign(DpReqServiceSignVo<R> vo //
			, TsmpDpRegStatus regStatus, String signUserName, InnerInvokeParam iip) {
		StringBuffer sb = new StringBuffer();
		sb.append("reqOrderNo=" + vo.getM().getReqOrderNo());
		
		final Long reqOrdermId = vo.getM().getReqOrdermId();
		TsmpDpReqOrderd3 d3 = getTsmpDpReqOrderd3Dao().findFirstByRefReqOrdermId(reqOrdermId);
		if (d3 == null) {
			this.logger.error("簽核後更新時, 找不到明細檔: mId=" + reqOrdermId);
			throw TsmpDpAaRtnCode._1218.throwing();
		}
		final String clientId = d3.getClientId();
		Optional<TsmpDpClientext> opt = getTsmpDpClientextDao().findById(clientId);
		if (!opt.isPresent()) {
			this.logger.error(String.format("簽核後更新時, 從明細檔資料找不到對應的用戶延伸檔: d3Id/clientId=%d/%s", d3.getReqOrderd3Id(), clientId));
			throw TsmpDpAaRtnCode._1218.throwing();
		}
		TsmpDpClientext ext = opt.get();
		String oldRowStr = getDgrAuditLogService().writeValueAsString(iip, ext); //舊資料統一轉成 String
		
		// 20210205; Kim; 當狀態為退回，且申請者要結案時，currentS 必為 NULL
		if (vo.getCurrentS() != null) {
			ext.setReviewRemark(vo.getCurrentS().getReqComment());
		}
		ext.setRefReviewUser(signUserName);
		if (regStatus != null) {
			ext.setRegStatus(regStatus.value());
		}
		ext.setUpdateDateTime(DateTimeUtil.now());
		ext.setUpdateUser(signUserName);
		ext = getTsmpDpClientextDao().save(ext);
		
		//寫入 Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, 
				TsmpDpClientext.class.getSimpleName(), TableAct.U.value(), oldRowStr, ext);

		TsmpClient client = getTsmpClientDao().findById(clientId).orElseThrow();
		sb.append(", clientId=" + clientId);
		sb.append(", clientName=" + client.getClientName());
		sb.append(", emails=" + client.getEmails());
		sb.append(", publicFlag=" + ext.getPublicFlag());
		sb.append(", clientSeqId=" + ext.getClientSeqId());
		vo.setIndentifData(sb.toString());
	}

	private void saveOAuth2Table(String clientId, String clientBlock, InnerInvokeParam iip) {
		OAuthUtil oauthUtil = new OAuthUtil();
		oauthUtil.setOauthClientDetailsDao(getOAuthDao());
		oauthUtil.setTsmpClientDao(getTsmpClientDao());
		oauthUtil.setDgrAuditLogService(getDgrAuditLogService());
		OAuthUtil.saveAuth(clientId, TsmpDpAaConstant.OAUTH_API_RESOURCE_ID, clientBlock, iip);
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return this.tsmpGroupDao;
	}

	protected TsmpClientGroupDao getTsmpClientGroupDao() {
		return this.tsmpClientGroupDao;
	}

	protected TsmpApiDao getTsmpApiDao() {
		return this.tsmpApiDao;
	}

	protected TsmpGroupApiDao getTsmpGroupApiDao() {
		return this.tsmpGroupApiDao;
	}

	protected TsmpDpClientextDao getTsmpDpClientextDao() {
		return this.tsmpDpClientextDao;
	}
	
	protected OauthClientDetailsDao getOAuthDao() {
		return this.oauthClientDetailsDao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}

	protected TsmpDpReqOrderd3Dao getTsmpDpReqOrderd3Dao() {
		return this.tsmpDpReqOrderd3Dao;
	}
	
	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <R extends DpReqServiceResp> void postDoAllAccept(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo) {
		super.postDoAllAccept(q, vo);
		sendClientReqResultMail(q, vo, TsmpDpRegStatus.PASS.value());
	}

	private <R extends DpReqServiceResp> void sendClientReqResultMail(DpReqServiceSignReq q, DpReqServiceSignVo<R> vo,  String regStatus) {
		TsmpAuthorization auth = new TsmpAuthorization();
		auth.setUserName(q.getSignUserName());
		
		final Long reqOrdermId = vo.getM().getReqOrdermId();
		TsmpDpReqOrderd3 d3 = getTsmpDpReqOrderd3Dao().findFirstByRefReqOrdermId(reqOrdermId);
		if (d3 != null) {
			this.logger.error("簽核後, 找不到明細檔: mId=" + reqOrdermId);
		
			Optional<TsmpDpClientext> opt = getTsmpDpClientextDao().findById(d3.getClientId());
			if (opt.isPresent()) {
				// 寄發審核通過Mail通知
				getSendClientRegMailService().sendEmail(opt.get(), regStatus, auth, vo.getM().getReqOrdermId());
			}
		}
	}
	
	
	
	


}
