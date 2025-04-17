package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType.ItemContainer;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.job.DeleteExpiredMailJob;
import tpi.dgrv4.dpaa.component.req.DpReqServiceFactory;
import tpi.dgrv4.dpaa.component.req.DpReqServiceIfs;
import tpi.dgrv4.dpaa.component.req.DpReqServiceResp_D1;
import tpi.dgrv4.dpaa.component.req.DpReqServiceResp_D2;
import tpi.dgrv4.dpaa.component.req.DpReqServiceResp_D3;
import tpi.dgrv4.dpaa.component.req.DpReqServiceResp_D5;
import tpi.dgrv4.dpaa.component.req.DpReqServiceUpdateReq;
import tpi.dgrv4.dpaa.component.req.DpReqServiceUpdateReq_D1;
import tpi.dgrv4.dpaa.component.req.DpReqServiceUpdateReq_D2;
import tpi.dgrv4.dpaa.component.req.DpReqServiceUpdateReq_D2D;
import tpi.dgrv4.dpaa.component.req.DpReqServiceUpdateReq_D3;
import tpi.dgrv4.dpaa.component.req.DpReqServiceUpdateReq_D5;
import tpi.dgrv4.dpaa.vo.DPB0066ApiBindingData;
import tpi.dgrv4.dpaa.vo.DPB0066OpenApiKey;
import tpi.dgrv4.dpaa.vo.DPB0066Req;
import tpi.dgrv4.dpaa.vo.DPB0066Resp;
import tpi.dgrv4.dpaa.vo.DPB0066RespApiApplication;
import tpi.dgrv4.dpaa.vo.DPB0066RespApiOnOff;
import tpi.dgrv4.dpaa.vo.DPB0066RespClientReg;
import tpi.dgrv4.dpaa.vo.DPB0066RespOpenApiKey;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpDpClientext;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd2;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd3;
import tpi.dgrv4.entity.repository.TsmpDpClientextDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd2Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd2dDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd3Dao;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0066Service {

	private final String UPDATE = "U";	// 更新

	private final String SEND = "S";	// 送審

	private final String RESEND = "R";	// 重送

	private TPILogger logger = TPILogger.tl;

	private TsmpDpReqOrderd2Dao tsmpDpReqOrderd2Dao;
	private TsmpDpReqOrderd2dDao tsmpDpReqOrderd2dDao;
	private TsmpDpReqOrderd3Dao tsmpDpReqOrderd3Dao;
	private TsmpDpClientextDao tsmpDpClientextDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private DpReqServiceFactory dpReqServiceFactory;
	private JobHelper jobHelper;
	private ApplicationContext ctx;
	private BcryptParamHelper bcryptParamHelper;

	@Autowired
	public DPB0066Service(TsmpDpReqOrderd2Dao tsmpDpReqOrderd2Dao, TsmpDpReqOrderd2dDao tsmpDpReqOrderd2dDao,
			TsmpDpReqOrderd3Dao tsmpDpReqOrderd3Dao, TsmpDpClientextDao tsmpDpClientextDao, TsmpDpFileDao tsmpDpFileDao,
			DpReqServiceFactory dpReqServiceFactory, JobHelper jobHelper, ApplicationContext ctx,
			BcryptParamHelper bcryptParamHelper) {
		super();
		this.tsmpDpReqOrderd2Dao = tsmpDpReqOrderd2Dao;
		this.tsmpDpReqOrderd2dDao = tsmpDpReqOrderd2dDao;
		this.tsmpDpReqOrderd3Dao = tsmpDpReqOrderd3Dao;
		this.tsmpDpClientextDao = tsmpDpClientextDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.dpReqServiceFactory = dpReqServiceFactory;
		this.jobHelper = jobHelper;
		this.ctx = ctx;
		this.bcryptParamHelper = bcryptParamHelper;
	}

	@Transactional
	public DPB0066Resp resendReq(TsmpAuthorization authorization, DPB0066Req req, ReqHeader reqHeader, InnerInvokeParam iip) {
		final Long reqOrdermId = checkCommonParams(authorization, req);	// 簽核類別
		final String act = req.getAct();
		final String locale = reqHeader.getLocale();
		
		DpReqServiceIfs dpReqService = getDpReqServiceFactory().getDpReqService(reqOrdermId, () -> {
			return TsmpDpAaRtnCode._1217.throwing();
		});
		
		DPB0066Resp resp = new DPB0066Resp();
		// 更新
		if (UPDATE.equals(act)) {
			// 用戶申請API
			if (isRtEquals(TsmpDpReqReviewType.API_APPLICATION, dpReqService)) {
				// 初始化回傳參數
				resp.setApiApplication(new DPB0066RespApiApplication());
				doUpdateD1(authorization, dpReqService, req, resp, reqHeader.getLocale(), iip);
			}
			// API上下架管理
			if (isRtEquals(TsmpDpReqReviewType.API_ON_OFF, dpReqService)) {
				// 初始化回傳參數
				resp.setApiOnOff(new DPB0066RespApiOnOff());
				doUpdateD2(authorization, dpReqService, req, resp, reqHeader.getLocale(), iip);
			}
			// 用戶註冊
			if (isRtEquals(TsmpDpReqReviewType.CLIENT_REG, dpReqService)) {
				// 初始化回傳參數
				resp.setClientReg(new DPB0066RespClientReg());
				doUpdateD3(authorization, dpReqService, req, resp, reqHeader.getLocale(), iip);
			}
			// Open API Key 管理
			if (isRtEquals(TsmpDpReqReviewType.OPEN_API_KEY, dpReqService)) {
				// 初始化回傳參數
				resp.setOpenApiKey(new DPB0066RespOpenApiKey());
				doUpdateD5(authorization, dpReqService, req, resp, reqHeader.getLocale(), iip);
			}
		// 送審
		} else if (SEND.equals(act)) {
			DpReqServiceUpdateReq updateReq = new DpReqServiceUpdateReq();
			updateReq.setReqOrdermId(req.getReqOrdermId());
			updateReq.setLv(req.getLv());
			updateReq.setClientId(authorization.getClientId());
			updateReq.setOrgId(authorization.getOrgId());
			updateReq.setUpdateUser(authorization.getUserNameForQuery());
			
			// 用戶申請API
			if (isRtEquals(TsmpDpReqReviewType.API_APPLICATION, dpReqService)) {
				// 初始化回傳參數
				resp.setApiApplication(new DPB0066RespApiApplication());
				doSendD1(dpReqService, updateReq, resp, locale, iip);
			}
			// API上下架管理
			if (isRtEquals(TsmpDpReqReviewType.API_ON_OFF, dpReqService)) {
				// 初始化回傳參數
				resp.setApiOnOff(new DPB0066RespApiOnOff());
				doSendD2(dpReqService, updateReq, resp, locale, iip);
			}
			// 用戶註冊
			if (isRtEquals(TsmpDpReqReviewType.CLIENT_REG, dpReqService)) {
				// 初始化回傳參數
				resp.setClientReg(new DPB0066RespClientReg());
				doSendD3(dpReqService, updateReq, resp, locale, iip);
			}
			// Open API Key 管理
			if (isRtEquals(TsmpDpReqReviewType.OPEN_API_KEY, dpReqService)) {
				// 初始化回傳參數
				resp.setOpenApiKey(new DPB0066RespOpenApiKey());
				doSendD5(dpReqService, updateReq, resp, locale, iip);
			}
		// 重送
		} else if (RESEND.equals(act)) {
			// 用戶申請API
			if (isRtEquals(TsmpDpReqReviewType.API_APPLICATION, dpReqService)) {
				// 初始化回傳參數
				resp.setApiApplication(new DPB0066RespApiApplication());
				doResendD1(authorization, dpReqService, req, resp, locale, iip);
			}
			// API上下架管理
			if (isRtEquals(TsmpDpReqReviewType.API_ON_OFF, dpReqService)) {
				// 初始化回傳參數
				resp.setApiOnOff(new DPB0066RespApiOnOff());
				doResendD2(authorization, dpReqService, req, resp, locale, iip);
			}
			// 用戶註冊
			if (isRtEquals(TsmpDpReqReviewType.CLIENT_REG, dpReqService)) {
				// 初始化回傳參數
				resp.setClientReg(new DPB0066RespClientReg());
				doResendD3(authorization, dpReqService, req, resp, locale, iip);
			}
			// Open API Key 管理
			if (isRtEquals(TsmpDpReqReviewType.OPEN_API_KEY, dpReqService)) {
				// 初始化回傳參數
				resp.setOpenApiKey(new DPB0066RespOpenApiKey());
				doResendD5(authorization, dpReqService, req, resp, locale, iip);
			}
		}
		
		// 刪除過期的 Mail log
		deleteExpiredMail();
		
		return resp;
	}

	private Long checkCommonParams(TsmpAuthorization auth, DPB0066Req req) {
		String act = req.getAct();
		if (StringUtils.isEmpty(act)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		// 大小寫皆可
		act = act.toUpperCase();
		if (!(UPDATE.equals(act) || SEND.equals(act) || RESEND.equals(act))) {
			this.logger.error("Undefined act: " + act);
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		req.setAct(act);
		
		// 後台更新/送審/重送一定要有 orgId
		final String orgId = auth.getOrgId();
		if (StringUtils.isEmpty(orgId)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		
		Long reqOrdermId = req.getReqOrdermId();
		if (reqOrdermId == null) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}
		return reqOrdermId;
	}

	private DpReqServiceUpdateReq_D1 getUpdateReq_D1(TsmpAuthorization authorization, DPB0066Req req) {
		DpReqServiceUpdateReq_D1 d1Req = new DpReqServiceUpdateReq_D1();
		d1Req.setReqOrdermId(req.getReqOrdermId());
		d1Req.setLv(req.getLv());
		d1Req.setClientId(authorization.getClientId());
		d1Req.setReqUserId(null);
		d1Req.setOrgId(authorization.getOrgId());
		d1Req.setReqDesc(req.getReqDesc());
		d1Req.setEffectiveDate(req.getEffectiveDate());
		d1Req.setUpdateUser(authorization.getUserNameForQuery());
		
		String newFileName = req.getApiApplicationD().getNewFileName();
		String oriFileName = req.getApiApplicationD().getOriFileName();
		
		List<String> newTempFileNames = new ArrayList<>();
		Map<String, String> dpb0066D1_oldFileMapping = new HashMap<>();
		
		// 未異動時(兩者皆空值, 或值相等)不用特別塞參數, 就不會動到原本的檔案了
		if (!StringUtils.isEmpty(oriFileName) || !StringUtils.isEmpty(newFileName)) {
			if (!StringUtils.isEmpty(oriFileName)) {
				// 更新
				if (!StringUtils.isEmpty(newFileName)) {
					if (!oriFileName.equals(newFileName)) {
						dpb0066D1_oldFileMapping.put(oriFileName, newFileName);
					}
				// 刪除
				} else {
					dpb0066D1_oldFileMapping.put(oriFileName, null);
				}
			// 新增
			} else if (!StringUtils.isEmpty(newFileName)) {
				newTempFileNames.add(newFileName);
			}
		}
		
		d1Req.setNewTempFileNames(newTempFileNames);
		d1Req.setOldFileMapping(dpb0066D1_oldFileMapping);
		
		d1Req.set_clientId(req.getApiApplicationD().getClientId());
		d1Req.setApiUids(req.getApiApplicationD().getApiUids());
		return d1Req;
	}

	public void setUpdateResp_D1(DpReqServiceResp_D1 d1Resp, DPB0066Resp resp) {
		if (d1Resp != null) {
			Long reqOrdermId = d1Resp.getReqOrdermId();
			Long lv = d1Resp.getLv();
			List<Long> reqOrdersIds = d1Resp.getsIds();
			List<Long> reqOrderd1Ids = d1Resp.getReqOrderd1Ids();
			List<Long> apiAuthIds = d1Resp.getApiAuthIds();
			
			Long fileId = null;
			String fileName = null;
			List<Long> fileIds = d1Resp.getFileIds();
			if (fileIds != null && !fileIds.isEmpty()) {
				fileId = fileIds.get(0);
				Optional<TsmpDpFile> opt_f = getTsmpDpFileDao().findById(fileId);
				if (opt_f.isPresent()) {
					fileName = opt_f.get().getFileName();
				}
			}
			
			resp.getApiApplication().setReqOrdermId(reqOrdermId);
			resp.getApiApplication().setLv(lv);
			resp.getApiApplication().setReqOrdersIds(reqOrdersIds);
			resp.getApiApplication().setReqOrderd1Ids(reqOrderd1Ids);
			resp.getApiApplication().setApiAuthIds(apiAuthIds);	// 送審後才回傳
			resp.getApiApplication().setFileId(fileId);
			resp.getApiApplication().setFileName(fileName);
		}
	}

	public void doUpdateD1(TsmpAuthorization authorization, DpReqServiceIfs dpReqService, //
			DPB0066Req req, DPB0066Resp resp, String locale, InnerInvokeParam iip) {
		DpReqServiceUpdateReq_D1 d1Req = getUpdateReq_D1(authorization, req);
		DpReqServiceResp_D1 d1Resp = dpReqService.update(d1Req, DpReqServiceResp_D1.class, locale, iip);
		setUpdateResp_D1(d1Resp, resp);
	}

	public DpReqServiceUpdateReq_D2 getUpdateReq_D2(TsmpAuthorization authorization, DPB0066Req req, String locale) {
		// 解密 encPublicFlag
		String publicFlag = new String();
		try {
			publicFlag = getBcryptParamHelper().decode( req.getApiOnOffD().getEncPublicFlag(), "API_AUTHORITY", locale );
		} catch (BcryptParamDecodeException e) {
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		
		DpReqServiceUpdateReq_D2 d2Req = new DpReqServiceUpdateReq_D2();
		d2Req.setReqOrdermId(req.getReqOrdermId());
		d2Req.setLv(req.getLv());
		d2Req.setClientId(authorization.getClientId());
		d2Req.setReqUserId(null);
		d2Req.setOrgId(authorization.getOrgId());
		d2Req.setReqDesc(req.getReqDesc());
		d2Req.setEffectiveDate(req.getEffectiveDate());
		d2Req.setUpdateUser(authorization.getUserNameForQuery());
		// 申請單附件不需要
		d2Req.setNewTempFileNames(null);
		d2Req.setOldFileMapping(null);
		Map<String, List<DPB0066ApiBindingData>> apiUidDatas = req.getApiOnOffD().getApiUidDatas();
		Map<String, List<DpReqServiceUpdateReq_D2D>> newApiUidDatas = new HashMap<>();
		for(Map.Entry<String, List<DPB0066ApiBindingData>> entry : apiUidDatas.entrySet()) {
			List<DpReqServiceUpdateReq_D2D> d2dList = new ArrayList<>();	
			for(DPB0066ApiBindingData bindingData : entry.getValue()) {
				DpReqServiceUpdateReq_D2D newBindingData = new DpReqServiceUpdateReq_D2D();
				newBindingData.setApiUid(bindingData.getApiUid());
				newBindingData.setRefThemeId(bindingData.getRefThemeId());
				d2dList.add(newBindingData);
			}
			newApiUidDatas.put(entry.getKey(), d2dList);
		}
		d2Req.setApiUidDatas(newApiUidDatas);
		d2Req.setOriApiMapFileName(req.getApiOnOffD().getOriApiMapFileName());
		d2Req.setNewApiMapFileName(req.getApiOnOffD().getNewApiMapFileName());
		d2Req.setPublicFlag(publicFlag);
		return d2Req;
	}

	public void setUpdateResp_D2(DpReqServiceResp_D2 d2Resp, DPB0066Resp resp) {
		if (d2Resp != null) {
			Long mId = d2Resp.getReqOrdermId();
			Long lv = d2Resp.getLv();
			List<Long> reqOrdersIds = d2Resp.getsIds();
			Map<Long, List<Long>> reqOrderd2Ids = d2Resp.getReqOrderd2Ids();
			Map<Long, Long> d2FileIds = d2Resp.getReqOrderd2FileIds();
			
			resp.getApiOnOff().setReqOrdermId(mId);
			resp.getApiOnOff().setLv(lv);
			resp.getApiOnOff().setReqOrdersIds(reqOrdersIds);
			resp.getApiOnOff().setReqOrderd2Ids(reqOrderd2Ids);
			Map<String, String> apiFileName = new HashMap<>();
			if (d2FileIds != null && !d2FileIds.isEmpty()) {
				for(Map.Entry<Long, Long> entry : d2FileIds.entrySet()) {
					String apiUid = null;
					String fileName = null;
					if (entry.getKey() == null) {
						continue;
					}
					Optional<TsmpDpReqOrderd2> opt_d2 = getTsmpDpReqOrderd2Dao().findById(entry.getKey());
					if (!opt_d2.isPresent()) {
						continue;
					}
					apiUid = opt_d2.get().getApiUid();
					if (entry.getValue() != null) {
						Optional<TsmpDpFile> opt_f = getTsmpDpFileDao().findById(entry.getValue());
						if (opt_f.isPresent()) {
							fileName = opt_f.get().getFileName();
						}
					}
					apiFileName.put(apiUid, fileName);
				}
			}
			resp.getApiOnOff().setApiFileName(apiFileName);
		}
	}

	public void doUpdateD2(TsmpAuthorization authorization, DpReqServiceIfs dpReqService, //
			DPB0066Req req, DPB0066Resp resp, String locale, InnerInvokeParam iip) {
		DpReqServiceUpdateReq_D2 d2Req = getUpdateReq_D2(authorization, req, locale);
		DpReqServiceResp_D2 d2Resp = dpReqService.update(d2Req, DpReqServiceResp_D2.class, locale, iip);
		setUpdateResp_D2(d2Resp, resp);
	}

	private DpReqServiceUpdateReq_D3 getUpdateReq_D3(TsmpAuthorization authorization, DPB0066Req req) {
		DpReqServiceUpdateReq_D3 d3Req = new DpReqServiceUpdateReq_D3();
		d3Req.setReqOrdermId(req.getReqOrdermId());
		d3Req.setLv(req.getLv());
		d3Req.setClientId(authorization.getClientId());
		d3Req.setReqUserId(null);
		d3Req.setOrgId(authorization.getOrgId());
		d3Req.setReqDesc(req.getReqDesc());
		d3Req.setEffectiveDate(req.getEffectiveDate());
		d3Req.setUpdateUser(authorization.getUserNameForQuery());
		
		String newFileName = req.getClientRegD().getNewFileName();
		String oriFileName = req.getClientRegD().getOriFileName();
		
		List<String> newTempFileNames = new ArrayList<>();
		Map<String, String> dpb0066D3_oldFileMapping = new HashMap<>();
		
		// 未異動時(兩者皆空值, 或值相等)不用特別塞參數, 就不會動到原本的檔案了
		if (!StringUtils.isEmpty(oriFileName) || !StringUtils.isEmpty(newFileName)) {
			if (!StringUtils.isEmpty(oriFileName)) {
				// 更新
				if (!StringUtils.isEmpty(newFileName)) {
					if (!oriFileName.equals(newFileName)) {
						dpb0066D3_oldFileMapping.put(oriFileName, newFileName);
					}
				// 刪除
				} else {
					dpb0066D3_oldFileMapping.put(oriFileName, null);
				}
			// 新增
			} else if (!StringUtils.isEmpty(newFileName)) {
				newTempFileNames.add(newFileName);
			}
		}
		
		d3Req.setNewTempFileNames(newTempFileNames);
		d3Req.setOldFileMapping(dpb0066D3_oldFileMapping);
		
		d3Req.set_clientId(req.getClientRegD().getClientId());
		d3Req.set_clientName(req.getClientRegD().getClientName());
		d3Req.set_emails(req.getClientRegD().getEmails());
		d3Req.set_clientBlock(req.getClientRegD().getClientBlock());
		d3Req.set_encPublicFlag(req.getClientRegD().getEncPublicFlag());
		return d3Req;
	}

	public void setUpdateResp_D3(DpReqServiceResp_D3 d3Resp, DPB0066Resp resp) {
		if (d3Resp != null) {
			Long reqOrdermId = d3Resp.getReqOrdermId();
			Long lv = d3Resp.getLv();
			List<Long> reqOrdersIds = d3Resp.getsIds();
			Long reqOrderd3Id = d3Resp.getReqOrderd3Id();
			
			Long clientSeqId = null;
			Optional<TsmpDpReqOrderd3> opt_d3 = getTsmpDpReqOrderd3Dao().findById(reqOrderd3Id);
			if (opt_d3.isPresent()) {
				String clientId = opt_d3.get().getClientId();
				Optional<TsmpDpClientext> opt_ext = getTsmpDpClientextDao().findById(clientId);
				if (opt_ext.isPresent()) {
					clientSeqId = opt_ext.get().getClientSeqId();
				}
			}
			
			Long fileId = null;
			String fileName = null;
			List<Long> fileIds = d3Resp.getFileIds();
			if (fileIds != null && !fileIds.isEmpty()) {
				fileId = fileIds.get(0);
				Optional<TsmpDpFile> opt_f = getTsmpDpFileDao().findById(fileId);
				if (opt_f.isPresent()) {
					fileName = opt_f.get().getFileName();
				}
			}
			
			resp.getClientReg().setReqOrdermId(reqOrdermId);
			resp.getClientReg().setLv(lv);
			resp.getClientReg().setReqOrdersIds(reqOrdersIds);
			resp.getClientReg().setReqOrderd3Id(reqOrderd3Id);
			resp.getClientReg().setClientSeqId(clientSeqId);
			resp.getClientReg().setFileId(fileId);
			resp.getClientReg().setFileName(fileName);
		}
	}

	public void doUpdateD3(TsmpAuthorization authorization, DpReqServiceIfs dpReqService, //
			DPB0066Req req, DPB0066Resp resp, String locale, InnerInvokeParam iip) {
		DpReqServiceUpdateReq_D3 d3Req = getUpdateReq_D3(authorization, req);
		DpReqServiceResp_D3 d3Resp = dpReqService.update(d3Req, DpReqServiceResp_D3.class, locale, iip);
		setUpdateResp_D3(d3Resp, resp);
	}
	
	public void doUpdateD5(TsmpAuthorization authorization, DpReqServiceIfs dpReqService, //
			DPB0066Req req, DPB0066Resp resp, String locale, InnerInvokeParam iip) {
		DpReqServiceUpdateReq_D5 d5Req = getUpdateReq_D5(authorization, req);
		DpReqServiceResp_D5 d5Resp = dpReqService.update(d5Req, DpReqServiceResp_D5.class, locale, iip);
		setUpdateResp_D5(d5Resp, resp);
	}


	private DpReqServiceUpdateReq_D5 getUpdateReq_D5(TsmpAuthorization authorization, DPB0066Req req) {
		DpReqServiceUpdateReq_D5 d5Req = new DpReqServiceUpdateReq_D5();
		d5Req.setReqOrdermId(req.getReqOrdermId());
		d5Req.setLv(req.getLv());
		d5Req.setClientId(authorization.getClientId());
		d5Req.setReqUserId(null);
		d5Req.setOrgId(authorization.getOrgId());
		d5Req.setReqDesc(req.getReqDesc());
		d5Req.setEffectiveDate(req.getEffectiveDate());
		d5Req.setUpdateUser(authorization.getUserNameForQuery());
		
		DPB0066OpenApiKey openApiKeyD = req.getOpenApiKeyD();
		d5Req.set_clientId(openApiKeyD.getClientId());
		d5Req.setOpenApiKeyId(openApiKeyD.getOpenApiKeyId());
		d5Req.setOpenApiKey(openApiKeyD.getOpenApiKey());
		d5Req.setSecretKey(openApiKeyD.getSecretKey());
		d5Req.setOpenApiKeyAlias(openApiKeyD.getOpenApiKeyAlias());
		d5Req.setTimesThreshold(openApiKeyD.getTimesThreshold());
		d5Req.setExpiredAt(openApiKeyD.getExpiredAt());
		d5Req.setApiUids(openApiKeyD.getApiUids());
		
		return d5Req;
	}

	public void setUpdateResp_D5(DpReqServiceResp_D5 d5Resp, DPB0066Resp resp) {
		if (d5Resp != null) {
			Long reqOrdermId = d5Resp.getReqOrdermId();
			Long lv = d5Resp.getLv();
			List<Long> reqOrdersIds = d5Resp.getsIds();
			Long reqOrderd5Id = d5Resp.getReqOrderd5Id();			
			List<Long> reqOrderd5dIds = d5Resp.getReqOrderd5dIds();
			
			DPB0066RespOpenApiKey respOpenApiKey = resp.getOpenApiKey();
			respOpenApiKey.setReqOrdermId(reqOrdermId);
			respOpenApiKey.setLv(lv);
			respOpenApiKey.setReqOrdersIds(reqOrdersIds);
			respOpenApiKey.setReqOrderd5Id(reqOrderd5Id);
			respOpenApiKey.setReqOrderd5dIds(reqOrderd5dIds);
		}
	}
	
	public void doSendD1(DpReqServiceIfs dpReqService, DpReqServiceUpdateReq updateReq, DPB0066Resp resp, String locale, InnerInvokeParam iip) {
		DpReqServiceResp_D1 d1Resp = dpReqService.submit(updateReq, DpReqServiceResp_D1.class, locale, iip);
		if (d1Resp != null) {
			resp.getApiApplication().setReqOrdermId(d1Resp.getReqOrdermId());
			resp.getApiApplication().setLv(d1Resp.getLv());
			resp.getApiApplication().setReqOrdersIds(d1Resp.getsIds());
			resp.getApiApplication().setApiAuthIds(d1Resp.getApiAuthIds());
		}
	}

	public void doSendD2(DpReqServiceIfs dpReqService, DpReqServiceUpdateReq updateReq, DPB0066Resp resp, String locale, InnerInvokeParam iip) {
		DpReqServiceResp_D2 d2Resp = dpReqService.submit(updateReq, DpReqServiceResp_D2.class, locale, iip);
		if (d2Resp != null) {
			resp.getApiOnOff().setReqOrdermId(d2Resp.getReqOrdermId());
			resp.getApiOnOff().setLv(d2Resp.getLv());
			resp.getApiOnOff().setReqOrdersIds(d2Resp.getsIds());
		}
	}

	public void doSendD3(DpReqServiceIfs dpReqService, DpReqServiceUpdateReq updateReq, DPB0066Resp resp, String locale, InnerInvokeParam iip) {
		DpReqServiceResp_D3 d3Resp = dpReqService.submit(updateReq, DpReqServiceResp_D3.class, locale, iip);
		if (d3Resp != null) {
			resp.getClientReg().setReqOrdermId(d3Resp.getReqOrdermId());
			resp.getClientReg().setLv(d3Resp.getLv());
			resp.getClientReg().setReqOrdersIds(d3Resp.getsIds());
			TsmpDpReqOrderd3 d3 = getTsmpDpReqOrderd3Dao().findFirstByRefReqOrdermId(d3Resp.getReqOrdermId());
			if (d3 != null) {
				Optional<TsmpDpClientext> opt = getTsmpDpClientextDao().findById(d3.getClientId());
				if (opt.isPresent()) {
					resp.getClientReg().setClientSeqId(opt.get().getClientSeqId());
				}
			}
		}
	}
	
	public void doSendD5(DpReqServiceIfs dpReqService, DpReqServiceUpdateReq updateReq, DPB0066Resp resp, String locale, InnerInvokeParam iip) {
		DpReqServiceResp_D5 d5Resp = dpReqService.submit(updateReq, DpReqServiceResp_D5.class, locale, iip);
		if (d5Resp != null) {
			DPB0066RespOpenApiKey respOpenApiKey = resp.getOpenApiKey();
			respOpenApiKey.setReqOrdermId(d5Resp.getReqOrdermId());
			respOpenApiKey.setLv(d5Resp.getLv());
			respOpenApiKey.setReqOrdersIds(d5Resp.getsIds());
		}
	}

	public void doResendD1(TsmpAuthorization authorization, DpReqServiceIfs dpReqService, //
			DPB0066Req req, DPB0066Resp resp, String locale, InnerInvokeParam iip) {
		DpReqServiceUpdateReq_D1 d1Req = getUpdateReq_D1(authorization, req);
		DpReqServiceResp_D1 d1Resp = dpReqService.resubmit(d1Req, DpReqServiceResp_D1.class, locale, iip);
		setUpdateResp_D1(d1Resp, resp);
	}

	public void doResendD2(TsmpAuthorization authorization, DpReqServiceIfs dpReqService, //
			DPB0066Req req, DPB0066Resp resp, String locale, InnerInvokeParam iip) {
		DpReqServiceUpdateReq_D2 d2Req = getUpdateReq_D2(authorization, req, locale);
		DpReqServiceResp_D2 d2Resp = dpReqService.resubmit(d2Req, DpReqServiceResp_D2.class, locale, iip);
		setUpdateResp_D2(d2Resp, resp);
	}
	
	public void doResendD3(TsmpAuthorization authorization, DpReqServiceIfs dpReqService, //
			DPB0066Req req, DPB0066Resp resp, String locale, InnerInvokeParam iip) {
		DpReqServiceUpdateReq_D3 d3Req = getUpdateReq_D3(authorization, req);
		DpReqServiceResp_D3 d3Resp = dpReqService.resubmit(d3Req, DpReqServiceResp_D3.class, locale, iip);
		setUpdateResp_D3(d3Resp, resp);
	}

	public void doResendD5(TsmpAuthorization authorization, DpReqServiceIfs dpReqService, //
			DPB0066Req req, DPB0066Resp resp, String locale, InnerInvokeParam iip) {
		DpReqServiceUpdateReq_D5 d5Req = getUpdateReq_D5(authorization, req);
		DpReqServiceResp_D5 d5Resp = dpReqService.resubmit(d5Req, DpReqServiceResp_D5.class, locale, iip);
		setUpdateResp_D5(d5Resp, resp);
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

	protected TsmpDpReqOrderd2Dao getTsmpDpReqOrderd2Dao() {
		return this.tsmpDpReqOrderd2Dao;
	}

	protected TsmpDpReqOrderd2dDao getTsmpDpReqOrderd2dDao() {
		return this.tsmpDpReqOrderd2dDao;
	}

	protected TsmpDpReqOrderd3Dao getTsmpDpReqOrderd3Dao() {
		return this.tsmpDpReqOrderd3Dao;
	}
	
	protected TsmpDpClientextDao getTsmpDpClientextDao() {
		return this.tsmpDpClientextDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
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

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

}
