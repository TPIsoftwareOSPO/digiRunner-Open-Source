package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredMailJob;
import tpi.dgrv4.dpaa.component.req.DpReqServiceFactory;
import tpi.dgrv4.dpaa.component.req.DpReqServiceIfs;
import tpi.dgrv4.dpaa.component.req.DpReqServiceResp_D1;
import tpi.dgrv4.dpaa.component.req.DpReqServiceResp_D2;
import tpi.dgrv4.dpaa.component.req.DpReqServiceResp_D3;
import tpi.dgrv4.dpaa.component.req.DpReqServiceResp_D5;
import tpi.dgrv4.dpaa.component.req.DpReqServiceSaveDraftReq_D1;
import tpi.dgrv4.dpaa.component.req.DpReqServiceSaveDraftReq_D2;
import tpi.dgrv4.dpaa.component.req.DpReqServiceSaveDraftReq_D2D;
import tpi.dgrv4.dpaa.component.req.DpReqServiceSaveDraftReq_D3;
import tpi.dgrv4.dpaa.component.req.DpReqServiceSaveDraftReq_D5;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0065ApiBindingData;
import tpi.dgrv4.dpaa.vo.DPB0065OpenApiKey;
import tpi.dgrv4.dpaa.vo.DPB0065Req;
import tpi.dgrv4.dpaa.vo.DPB0065Resp;
import tpi.dgrv4.dpaa.vo.DPB0065RespApiApplication;
import tpi.dgrv4.dpaa.vo.DPB0065RespApiOnOff;
import tpi.dgrv4.dpaa.vo.DPB0065RespClientReg;
import tpi.dgrv4.dpaa.vo.DPB0065RespOpenApiKey;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0065Service {

	private DpReqServiceFactory dpReqServiceFactory;
	private JobHelper jobHelper;
	private TsmpDpFileDao tsmpDpFileDao;
	private ApplicationContext ctx;
	private BcryptParamHelper bcryptParamHelper;
	private DgrAcIdpUserDao dgrAcIdpUserDao;
	private TsmpUserDao tsmpUserDao;
	
	@Autowired
	public DPB0065Service(DpReqServiceFactory dpReqServiceFactory, JobHelper jobHelper, TsmpDpFileDao tsmpDpFileDao,
			ApplicationContext ctx, BcryptParamHelper bcryptParamHelper, DgrAcIdpUserDao dgrAcIdpUserDao,
			TsmpUserDao tsmpUserDao) {
		super();
		this.dpReqServiceFactory = dpReqServiceFactory;
		this.jobHelper = jobHelper;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.ctx = ctx;
		this.bcryptParamHelper = bcryptParamHelper;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
		this.tsmpUserDao = tsmpUserDao;
	}

	public DPB0065Resp createReq(TsmpAuthorization authorization, DPB0065Req req, ReqHeader reqHeader, InnerInvokeParam iip) {
		Map<String, Object> map = createReq2(authorization, req, reqHeader, iip);
		return (DPB0065Resp) map.get("Resp");
	}
	
	public Map<String, Object> createReq2(TsmpAuthorization authorization, DPB0065Req req, ReqHeader reqHeader, InnerInvokeParam iip) {
		DPB0065Resp resp = new DPB0065Resp();
		String local = ServiceUtil.getLocale(reqHeader.getLocale());
		final String reqType = req.getReqType();	// 簽核類別
		if (StringUtils.isEmpty(reqType)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		DpReqServiceIfs dpReqSerivce = getDpReqServiceFactory().getDpReqService(reqType, null);
		
		// 用戶API申請
		if (TsmpDpReqReviewType.API_APPLICATION.isValueEquals(reqType)) {
			// 初始化回傳參數
			resp.setApiApplication(new DPB0065RespApiApplication());
			
			doSaveD1Draft(dpReqSerivce, authorization, req, resp, local, iip);
		}
		
		// API上下架管理
		if (TsmpDpReqReviewType.API_ON_OFF.isValueEquals(reqType)) {
			// 初始化回傳參數
			resp.setApiOnOff(new DPB0065RespApiOnOff());
			
			doSaveD2Draft(dpReqSerivce, authorization, req, resp, local, iip);
		}

		// 用戶註冊
		if (TsmpDpReqReviewType.CLIENT_REG.isValueEquals(reqType)) {
			// 初始化回傳參數
			resp.setClientReg(new DPB0065RespClientReg());
			
			/*
			 * 2025/4/24-因 SornarQube 掃描到(Change this code to not construct the path from user-controlled data.) 
			 * 先註解呼叫建立用戶註冊"簽核流程 D3 的 method doSaveD3Draft(), 因為是不再使用的功能
			 */
			doSaveD3Draft(dpReqSerivce, authorization, req, resp, local, iip);
		}
		
		// Open API Key 管理
		if (TsmpDpReqReviewType.OPEN_API_KEY.isValueEquals(reqType)) {
			// 初始化回傳參數
			resp.setOpenApiKey(new DPB0065RespOpenApiKey());
			
			doSaveD5Draft(dpReqSerivce, authorization, req, resp, local, iip);
		}

		// 刪除過期的 Mail log
		deleteExpiredMail();
		
		Map<String, Object> map = new HashMap<>();
		map.put("Resp", resp);
		map.put("Job", null);
		
		return map;
	}

	private void doSaveD1Draft(DpReqServiceIfs dpReqService, TsmpAuthorization authorization, //
			DPB0065Req req, DPB0065Resp resp, String locale, InnerInvokeParam iip) {
		DpReqServiceSaveDraftReq_D1 d1Req = new DpReqServiceSaveDraftReq_D1();
		d1Req.setReqType(req.getReqType());
		d1Req.setReqSubtype(req.getReqSubtype());
		d1Req.setClientId(authorization.getClientId());
		d1Req.setOrgId(authorization.getOrgId());
		d1Req.setReqDesc(req.getReqDesc());
		d1Req.setEffectiveDate(req.getEffectiveDate());
		d1Req.setCreateUser(authorization.getUserName());
		String userId = getAcUserIdOrIdpUserName(authorization.getUserNameForQuery());
		d1Req.setReqUserId(userId);
		// 附件
		String tmpFileName = req.getApiApplicationD().getTmpFileName();
		if (!StringUtils.isEmpty(tmpFileName)) {
			List<String> tempFileNames = new ArrayList<>();
			tempFileNames.add(tmpFileName);
			d1Req.setTempFileNames(tempFileNames);
		}
		d1Req.set_clientId(req.getApiApplicationD().getClientId());
		d1Req.setApiUids(req.getApiApplicationD().getApiUids());
		DpReqServiceResp_D1 d1Resp = dpReqService.saveDraft(d1Req, DpReqServiceResp_D1.class, locale, iip);
		
		if (d1Resp != null) {
			Long reqOrdermId = d1Resp.getReqOrdermId();
			Long lv = d1Resp.getLv();
			Long sId = null;
			// 建立草稿時只會有一筆簽核狀態
			if (d1Resp.getsIds() != null && !d1Resp.getsIds().isEmpty()) {
				sId = d1Resp.getsIds().get(0);
			}
			List<Long> d1Ids = d1Resp.getReqOrderd1Ids();
			Long fileId = null;
			if (d1Resp.getFileIds() != null && !d1Resp.getFileIds().isEmpty()) {
				fileId = d1Resp.getFileIds().get(0);
			}
			
			resp.getApiApplication().setReqOrdermId(reqOrdermId);
			resp.getApiApplication().setLv(lv);
			resp.getApiApplication().setReqOrdersId(sId);
			resp.getApiApplication().setReqOrderd1Ids(d1Ids);
			resp.getApiApplication().setFileId(fileId);
			if (fileId != null) {
				Optional<TsmpDpFile> opt_f = getTsmpDpFileDao().findById(fileId);
				if (opt_f.isPresent()) {
					resp.getApiApplication().setFileName(opt_f.get().getFileName());
				}
			}
		}
	}

	private void doSaveD2Draft(DpReqServiceIfs dpReqService, TsmpAuthorization authorization, //
			DPB0065Req req, DPB0065Resp resp, String locale, InnerInvokeParam iip) {
		// 解密 encPublicFlag
		String publicFlag = new String();
		try {
			publicFlag = getBcryptParamHelper().decode( req.getApiOnOffD().getEncPublicFlag(), "API_AUTHORITY", locale);
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		
		DpReqServiceSaveDraftReq_D2 d2Req = new DpReqServiceSaveDraftReq_D2();
		d2Req.setReqType(req.getReqType());
		d2Req.setReqSubtype(req.getReqSubtype());
		d2Req.setClientId(authorization.getClientId());
		d2Req.setOrgId(authorization.getOrgId());
		d2Req.setReqDesc(req.getReqDesc());
		d2Req.setEffectiveDate(req.getEffectiveDate());
		d2Req.setCreateUser(authorization.getUserName());
		String userId = getAcUserIdOrIdpUserName(authorization.getUserNameForQuery());
		d2Req.setReqUserId(userId);
		Map<String, List<DPB0065ApiBindingData>> oldBindingDatas = req.getApiOnOffD().getApiUidDatas();
		if (oldBindingDatas != null && !oldBindingDatas.isEmpty()) {
			Map<String, List<DpReqServiceSaveDraftReq_D2D>> newBindingDatas = new HashMap<>();
			for(Map.Entry<String, List<DPB0065ApiBindingData>> entry : oldBindingDatas.entrySet()) {
				if (entry.getValue() == null || entry.getValue().isEmpty()) {
					continue;
				}
				List<DpReqServiceSaveDraftReq_D2D> d2dList = new ArrayList<>();
				DpReqServiceSaveDraftReq_D2D newBindingData = null;
				for(DPB0065ApiBindingData oldBindingData : entry.getValue()) {
					newBindingData = new DpReqServiceSaveDraftReq_D2D();
					newBindingData.setApiUid(oldBindingData.getApiUid());
					newBindingData.setRefThemeId(oldBindingData.getRefThemeId());
					d2dList.add(newBindingData);
				}
				
				newBindingDatas.put(entry.getKey(), d2dList);
			}
			d2Req.setApiUidDatas(newBindingDatas);
		}
		d2Req.setApiMapFileName(req.getApiOnOffD().getApiMapFileName());
		d2Req.setPublicFlag(publicFlag);
		DpReqServiceResp_D2 d2Resp = dpReqService.saveDraft(d2Req, DpReqServiceResp_D2.class, locale, iip);
		
		if (d2Resp != null) {
			resp.getApiOnOff().setReqOrdermId(d2Resp.getReqOrdermId());
			resp.getApiOnOff().setLv(d2Resp.getLv());
			// 建立草稿時只會有一筆簽核狀態
			if (d2Resp.getsIds() != null && !d2Resp.getsIds().isEmpty()) {
				resp.getApiOnOff().setReqOrdersId(d2Resp.getsIds().get(0));
			}
			resp.getApiOnOff().setReqOrderd2Ids(d2Resp.getReqOrderd2Ids());
			resp.getApiOnOff().setReqOrderd2FileIds(d2Resp.getReqOrderd2FileIds());
		}
	}

	private void doSaveD3Draft(DpReqServiceIfs dpReqService, TsmpAuthorization authorization, DPB0065Req req, DPB0065Resp resp
			, String locale, InnerInvokeParam iip) {
		DpReqServiceSaveDraftReq_D3 d3Req = new DpReqServiceSaveDraftReq_D3();
		d3Req.setReqType(req.getReqType());
		d3Req.setReqSubtype(req.getReqSubtype());
		d3Req.setClientId(authorization.getClientId());
		d3Req.setOrgId(authorization.getOrgId());
		d3Req.setReqDesc(req.getReqDesc());
		d3Req.setEffectiveDate(req.getEffectiveDate());
		d3Req.setCreateUser(authorization.getUserName());
		String userId = getAcUserIdOrIdpUserName(authorization.getUserNameForQuery());
		d3Req.setReqUserId(userId);
		// 附件
		String tmpFileName = req.getClientRegD().getTmpFileName();
		if (!StringUtils.isEmpty(tmpFileName)) {
			List<String> tempFileNames = new ArrayList<>();
			tempFileNames.add(tmpFileName);
			d3Req.setTempFileNames(tempFileNames);
		}
		d3Req.set_clientId(req.getClientRegD().getClientId());
		d3Req.set_clientName(req.getClientRegD().getClientName());
		d3Req.set_emails(req.getClientRegD().getEmails());
		d3Req.set_clientBlock(req.getClientRegD().getClientBlock());
		d3Req.set_encPublicFlag(req.getClientRegD().getEncPublicFlag());
		DpReqServiceResp_D3 d3Resp = dpReqService.saveDraft(d3Req, DpReqServiceResp_D3.class, locale, iip);
		
		if (d3Resp != null) {
			resp.getClientReg().setReqOrdermId(d3Resp.getReqOrdermId());
			resp.getClientReg().setLv(d3Resp.getLv());
			// 建立草稿時只會有一筆簽核狀態
			if (d3Resp.getsIds() != null && !d3Resp.getsIds().isEmpty()) {
				resp.getClientReg().setReqOrdersId(d3Resp.getsIds().get(0));
			}
			resp.getClientReg().setReqOrderd3Id(d3Resp.getReqOrderd3Id());
			if (d3Resp.getFileIds() != null && !d3Resp.getFileIds().isEmpty()) {
				Long fileId = d3Resp.getFileIds().get(0);
				resp.getClientReg().setFileId(fileId);
				Optional<TsmpDpFile> opt_f = getTsmpDpFileDao().findById(fileId);
				if (opt_f.isPresent()) {
					resp.getClientReg().setFileName(opt_f.get().getFileName());
				}
			}
		}
	}
	
	private void doSaveD5Draft(DpReqServiceIfs dpReqService, TsmpAuthorization authorization, DPB0065Req req, DPB0065Resp resp
			, String locale, InnerInvokeParam iip) {
		DpReqServiceSaveDraftReq_D5 d5Req = new DpReqServiceSaveDraftReq_D5();
		d5Req.setReqType(req.getReqType());
		d5Req.setReqSubtype(req.getReqSubtype());
		d5Req.setClientId(authorization.getClientId());
		d5Req.setOrgId(authorization.getOrgId());
		d5Req.setReqDesc(req.getReqDesc());
		d5Req.setEffectiveDate(req.getEffectiveDate());
		d5Req.setCreateUser(authorization.getUserName());
		
		DPB0065OpenApiKey openApiKeyD = req.getOpenApiKeyD();
		d5Req.set_clientId(openApiKeyD.getClientId());
		d5Req.setOpenApiKeyId(openApiKeyD.getOpenApiKeyId());
		d5Req.setOpenApiKey(openApiKeyD.getOpenApiKey());
		d5Req.setSecretKey(openApiKeyD.getSecretKey());
		d5Req.setOpenApiKeyAlias(openApiKeyD.getOpenApiKeyAlias());
		d5Req.setTimesThreshold(openApiKeyD.getTimesThreshold());
		d5Req.setExpiredAt(openApiKeyD.getExpiredAt());
		d5Req.setApiUids(openApiKeyD.getApiUids());
		String userId = getAcUserIdOrIdpUserName(authorization.getUserNameForQuery());
		d5Req.setReqUserId(userId);
		DpReqServiceResp_D5 d5Resp = dpReqService.saveDraft(d5Req, DpReqServiceResp_D5.class, locale, iip);
		
		if (d5Resp != null) {
			DPB0065RespOpenApiKey respOpenApiKey = resp.getOpenApiKey();
			respOpenApiKey.setReqOrdermId(d5Resp.getReqOrdermId());
			respOpenApiKey.setLv(d5Resp.getLv());
			// 建立草稿時只會有一筆簽核狀態
			if (d5Resp.getsIds() != null && !d5Resp.getsIds().isEmpty()) {
				respOpenApiKey.setReqOrdersId(d5Resp.getsIds().get(0));
			}
			respOpenApiKey.setReqOrderd5Id(d5Resp.getReqOrderd5Id());
			respOpenApiKey.setReqOrderd5dIds(d5Resp.getReqOrderd5dIds());
		}
	}
	
	
	private String getAcUserIdOrIdpUserName(String userNameForQuery) {
		DgrAcIdpUser dgrAcIdpUser = getDgrAcIdpUserDao().findFirstByUserName(userNameForQuery).orElse(null);
		if (dgrAcIdpUser != null) {
			return userNameForQuery;
		} else {
			TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userNameForQuery);
			if (tsmpUser != null )
				return tsmpUser.getUserId();
		}
		return new String();
	}
	
	public DeleteExpiredMailJob deleteExpiredMail() {
		DeleteExpiredMailJob job = getDeleteExpiredMailJob();
		getJobHelper().add(job);
		return job;
	}
	
	protected DeleteExpiredMailJob getDeleteExpiredMailJob() {
		return (DeleteExpiredMailJob) getCtx().getBean("deleteExpiredMailJob");
	}

	protected DpReqServiceFactory getDpReqServiceFactory() {
		return this.dpReqServiceFactory;
	}

	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}
	
	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected ApplicationContext getCtx() {
		return this.ctx;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}
	
	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return this.dgrAcIdpUserDao;
	}
	
	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}
}
