package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.req.DpReqQueryIfs;
import tpi.dgrv4.dpaa.component.req.DpReqQueryResp;
import tpi.dgrv4.dpaa.component.req.DpReqQueryResp_D1;
import tpi.dgrv4.dpaa.component.req.DpReqQueryResp_D1d;
import tpi.dgrv4.dpaa.component.req.DpReqQueryResp_D2;
import tpi.dgrv4.dpaa.component.req.DpReqQueryResp_D2d;
import tpi.dgrv4.dpaa.component.req.DpReqQueryResp_D3;
import tpi.dgrv4.dpaa.component.req.DpReqQueryResp_D5;
import tpi.dgrv4.dpaa.component.req.DpReqQueryResp_D5d;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpModule;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.constant.TsmpDpQuyType;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0068ApiOnOff;
import tpi.dgrv4.dpaa.vo.DPB0068ApiUserApply;
import tpi.dgrv4.dpaa.vo.DPB0068ClientReg;
import tpi.dgrv4.dpaa.vo.DPB0068D1;
import tpi.dgrv4.dpaa.vo.DPB0068D2;
import tpi.dgrv4.dpaa.vo.DPB0068D5;
import tpi.dgrv4.dpaa.vo.DPB0068OpenApiKey;
import tpi.dgrv4.dpaa.vo.DPB0068Req;
import tpi.dgrv4.dpaa.vo.DPB0068Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.jpql.TsmpDpChkLog;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrders;
import tpi.dgrv4.entity.repository.TsmpDpChkLayerDao;
import tpi.dgrv4.entity.repository.TsmpDpChkLogDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdersDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0068Service {

	private TPILogger logger = TPILogger.tl;

	private DpReqQueryIfs<DpReqQueryResp_D1> dpReqQueryD1;
	private DpReqQueryIfs<DpReqQueryResp_D2> dpReqQueryD2;
	private DpReqQueryIfs<DpReqQueryResp_D3> dpReqQueryD3;
	private DpReqQueryIfs<DpReqQueryResp_D5> dpReqQueryD5;
	private TsmpUserDao tsmpUserDao;
	private TsmpDpReqOrdermDao tsmpDpReqOrdermDao;
	private TsmpDpReqOrdersDao tsmpDpReqOrdersDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpDpChkLayerDao tsmpDpChkLayerDao;
	private BcryptParamHelper bcryptParamHelper;
	private TsmpDpChkLogDao tsmpDpChkLogDao;
	private DPB0067Service dpb0067Service;

	@Autowired
	public DPB0068Service(DpReqQueryIfs<DpReqQueryResp_D1> dpReqQueryD1, DpReqQueryIfs<DpReqQueryResp_D2> dpReqQueryD2,
			DpReqQueryIfs<DpReqQueryResp_D3> dpReqQueryD3, DpReqQueryIfs<DpReqQueryResp_D5> dpReqQueryD5,
			TsmpUserDao tsmpUserDao, TsmpDpReqOrdermDao tsmpDpReqOrdermDao, TsmpDpReqOrdersDao tsmpDpReqOrdersDao,
			TsmpOrganizationDao tsmpOrganizationDao, TsmpDpChkLayerDao tsmpDpChkLayerDao,
			BcryptParamHelper bcryptParamHelper, TsmpDpChkLogDao tsmpDpChkLogDao, DPB0067Service dpb0067Service) {
		super();
		this.dpReqQueryD1 = dpReqQueryD1;
		this.dpReqQueryD2 = dpReqQueryD2;
		this.dpReqQueryD3 = dpReqQueryD3;
		this.dpReqQueryD5 = dpReqQueryD5;
		this.tsmpUserDao = tsmpUserDao;
		this.tsmpDpReqOrdermDao = tsmpDpReqOrdermDao;
		this.tsmpDpReqOrdersDao = tsmpDpReqOrdersDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpDpChkLayerDao = tsmpDpChkLayerDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.tsmpDpChkLogDao = tsmpDpChkLogDao;
		this.dpb0067Service = dpb0067Service;
	}

	public DPB0068Resp queryReqByPk(TsmpAuthorization auth, DPB0068Req req, ReqHeader reqHeader) {
		String local = ServiceUtil.getLocale(reqHeader.getLocale());

		// 各種檢核
		String reqType = checkReq(auth, req, local);
		Long reqOrdermId = req.getReqOrdermId();

		// 依申請單類型使用不同的DpReqQueryIfs查詢資料
		if (TsmpDpReqReviewType.API_APPLICATION.value().equals(reqType)) {
			DpReqQueryResp<DpReqQueryResp_D1> d1Resp = getDpReqQueryD1().doQuery(reqOrdermId, reqHeader.getLocale());
			return createD1Resp(d1Resp);
		} else if (TsmpDpReqReviewType.API_ON_OFF.value().equals(reqType)) {
			DpReqQueryResp<DpReqQueryResp_D2> d2Resp = getDpReqQueryD2().doQuery(reqOrdermId, reqHeader.getLocale());
			return createD2Resp(d2Resp);
		} else if (TsmpDpReqReviewType.CLIENT_REG.value().equals(reqType)) {
			DpReqQueryResp<DpReqQueryResp_D3> d3Resp = getDpReqQueryD3().doQuery(reqOrdermId, reqHeader.getLocale());
			return createD3Resp(d3Resp);
		} else if (TsmpDpReqReviewType.OPEN_API_KEY.value().equals(reqType)) {
			DpReqQueryResp<DpReqQueryResp_D5> d5Resp = getDpReqQueryD5().doQuery(reqOrdermId, reqHeader.getLocale());
			return createD5Resp(d5Resp);
		}
		throw TsmpDpAaRtnCode._1217.throwing();
	}

	private String checkReq(TsmpAuthorization authorization, DPB0068Req req, String locale) {
		// 檢查共同欄位
		final String quyType = checkCommonParams(authorization, req, locale);
		
		// 檢查使用者是否存在;
		final String userId = authorization.getUserNameForQuery();//SUB or userName
		
		if (StringUtils.isEmpty(userId)) {
			this.logger.debug("查無使用者ID: " + userId);
			throw TsmpDpAaRtnCode._1217.throwing();
		}
		
		// 檢查申請單是否存在
		final Long reqOrdermId = req.getReqOrdermId();
		Optional<TsmpDpReqOrderm> opt_m = getTsmpDpReqOrdermDao().findById(reqOrdermId);
		if (!opt_m.isPresent()) {
			this.logger.debug("申請單不存在: " + reqOrdermId);
			throw TsmpDpAaRtnCode._1217.throwing();
		}
		TsmpDpReqOrderm m = opt_m.get();
		
		// 檢查權限
		// 申請單
		if (TsmpDpQuyType.REQ.value().equals(quyType)) {
			// 查詢個人申請單時, 申請單的User Id必須與查詢者的User Id相同
			if (!StringUtils.isEmpty(m.getReqUserId()) && 
				!getDPB0067Service().getAcUserIdOrIdpUserName(userId).equals(m.getReqUserId())) {
				this.logger.debug(String.format("使用者ID(%s)不符: %s", userId, m.getReqUserId()));
				throw TsmpDpAaRtnCode._1217.throwing();
			}
		// 待審單 / 已審單
		} else if (
			TsmpDpQuyType.EXA.value().equals(quyType) ||
			TsmpDpQuyType.REV.value().equals(quyType)
		) {
			// 單子所屬的單位，必須包含在查詢者所屬的單位下
			final String orgId = authorization.getOrgId();
			boolean isInOrgDescList = isInOrgDescList(m, orgId);
			if (!isInOrgDescList) {
				this.logger.error("查詢不符合組織原則");
				throw TsmpDpAaRtnCode._1217.throwing();
			}
			
			// 找出下一關
			TsmpDpReqOrders nextCheckPoint = getTsmpDpReqOrdersDao().queryNextCheckPoint(reqOrdermId);

			// 使用者是否有權限簽核下一關卡
			boolean isCheckable = false;
			if (nextCheckPoint != null) {  //userName 
				final String reqType = m.getReqType();
				isCheckable = getTsmpDpChkLayerDao().isUserAuthorized(reqType, nextCheckPoint.getLayer(), userId);
			}
			
			// 待審單
			if (TsmpDpQuyType.EXA.value().equals(quyType)) {
				
				// 只能查待審中的單
				if (nextCheckPoint == null) {
					this.logger.error("沒有可簽核的關卡了");
					throw TsmpDpAaRtnCode._1217.throwing();
				}
				
				// 若使用者不是此單的下一關簽核人員, 就不能查到這張單
				if (!isCheckable) {
					this.logger.error("非當前合法的簽核人員");
					throw TsmpDpAaRtnCode._1217.throwing();
				}
				
			// 已審單 (不屬於登入者的已審單，則拋出 1217 錯誤)
			} else if (TsmpDpQuyType.REV.value().equals(quyType)) {
				// 若使用者是此單的下一關簽核人員, 就不能查到這張單, 應該去"待審單"查
				if (isCheckable) {
					this.logger.error("此為「待審單」，不應從「已審單」查詢");
					throw TsmpDpAaRtnCode._1217.throwing();
				}
				
				// 曾經可以簽此單 或 之前曾經簽過
				Boolean wasAbleToSign = getTsmpDpReqOrdersDao().wasAbleToSign(reqOrdermId, userId);
				Boolean wasSigned = checkWasSigned(reqOrdermId, userId);
				if ( !(wasAbleToSign || wasSigned) ) {
					this.logger.error(String.format("曾經可簽核:%b/曾經簽過:%b", wasAbleToSign, wasSigned));
					throw TsmpDpAaRtnCode._1217.throwing();
				}

			}
		}
		
		return m.getReqType();
	}

	private String checkCommonParams(TsmpAuthorization auth, DPB0068Req req, String locale) {
		final String orgId = auth.getOrgId();
		final String userName = auth.getUserName();
		//final String isPersonal = toUpper(req.getIsPersonal());
		String encQuyType = req.getEncodeQuyType();
		final Long reqOrdermId = req.getReqOrdermId();
		if (StringUtils.isEmpty(orgId) ||
			StringUtils.isEmpty(userName) ||
			//StringUtils.isEmpty(isPersonal) ||
			//!("Y".equals(isPersonal) || "N".equals(isPersonal)) ||
			StringUtils.isEmpty(encQuyType) ||
			reqOrdermId == null
		) {
			this.logger.debug("基本共同欄位有誤");
			throw TsmpDpAaRtnCode._1217.throwing();
		}
		
		encQuyType = decodeEncodeQuyType(encQuyType, locale);
		req.setEncodeQuyType(encQuyType);
		
		//req.setIsPersonal(isPersonal);
		return encQuyType;
	}
	
	protected String decodeEncodeQuyType(String encodeQuyType, String locale) {
		String quyType = null;
		if (!StringUtils.isEmpty(encodeQuyType)) {
			try {
				quyType = getBcryptParamHelper().decode(encodeQuyType, "ORDERM_QUY_TYPE", locale);
			} catch (BcryptParamDecodeException e) {
				throw TsmpDpAaRtnCode._1299.throwing();
			}
		}
		return quyType;
	}
 
	private boolean isInOrgDescList(TsmpDpReqOrderm m, String orgId) {
		List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
		if (StringUtils.isEmpty(m.getOrgId()) ||
			(!CollectionUtils.isEmpty(orgDescList) && orgDescList.contains(m.getOrgId())) ) {
			return true;
		}
		return false;
	}

	private Boolean checkWasSigned(Long reqOrdermId, String userName) {
		List<TsmpDpChkLog> logs = getTsmpDpChkLogDao().findByReqOrdermIdAndCreateUser(reqOrdermId, userName);
		return !(logs == null || logs.isEmpty());
	}

	private DPB0068Resp createD1Resp(DpReqQueryResp<DpReqQueryResp_D1> d1Resp) {
		if (d1Resp == null) {
			throw TsmpDpAaRtnCode._1217.throwing();
		}
		DPB0068Resp resp = createResp(d1Resp);
		DPB0068ApiUserApply apiUserApply = getApiUserApply(d1Resp);
		resp.setApiUserApply(apiUserApply);
		return resp;
	}

	private DPB0068Resp createD2Resp(DpReqQueryResp<DpReqQueryResp_D2> d2Resp) {
		if (d2Resp == null) {
			throw TsmpDpAaRtnCode._1217.throwing();
		}
		DPB0068Resp resp = createResp(d2Resp);
		DPB0068ApiOnOff apiOnOff = getApiOnOff(d2Resp);
		resp.setApiOnOff(apiOnOff);
		return resp;
	}

	private DPB0068Resp createD3Resp(DpReqQueryResp<DpReqQueryResp_D3> d3Resp) {
		if (d3Resp == null) {
			throw TsmpDpAaRtnCode._1217.throwing();
		}
		DPB0068Resp resp = createResp(d3Resp);
		DPB0068ClientReg clientReg = getClientReg(d3Resp);
		resp.setClientReg(clientReg);
		return resp;
	}
	
	private DPB0068Resp createD5Resp(DpReqQueryResp<DpReqQueryResp_D5> d5Resp) {
		if (d5Resp == null) {
			throw TsmpDpAaRtnCode._1217.throwing();
		}
		DPB0068Resp resp = createResp(d5Resp);
		DPB0068OpenApiKey openApiKey = getOpenApiKey(d5Resp);
		resp.setOpenApiKey(openApiKey);
		return resp;
	}

	private DPB0068Resp createResp(DpReqQueryResp<?> queryResp) {
		DPB0068Resp resp = new DPB0068Resp();
		resp.setReqOrdermId(queryResp.getReqOrdermId());
		resp.setReqType(queryResp.getReqType());
		resp.setTitle(queryResp.getReqTypeName());
		resp.setReqOrderNo(queryResp.getReqOrderNo());
		String createDateTime = DateTimeUtil.dateTimeToString(queryResp.getCreateDateTime(), DateTimeFormatEnum.西元年月日).orElse(new String());
		resp.setCreateDateTime(createDateTime);
		/*
		 * 如果是前台申請就帶出 "clientId / clientName", 後台申請就 "userId / userName"
		resp.setApplyUserName(queryResp.getApplierName());
		*/
		if (!StringUtils.hasLength(queryResp.getApplierName())) {
			resp.setApplyUserName(queryResp.getApplierId() + " / " + getDPB0067Service().getUserName(queryResp.getApplierId()));
		}
		else {
			resp.setApplyUserName(queryResp.getApplierId() + " / " + queryResp.getApplierName());
		}
		String orgName = getOrgName(queryResp.getOrgName());
		resp.setOrgName(orgName);
		resp.setReqSubtype(queryResp.getReqSubtype());
		resp.setSubTitle(queryResp.getReqSubtypeName());
		resp.setChkStatus(queryResp.getCurrentReviewStatus());
		resp.setChkStatusName(queryResp.getCurrentReviewStatusName());
		resp.setNextChkPoint(String.valueOf(queryResp.getCurrentLayer()));
		resp.setChkPointName(queryResp.getCurrentLayerName());
		resp.setLv(queryResp.getLv());
		resp.setReqDesc(queryResp.getReqDesc());
		resp.setEffectiveDate(queryResp.getEffectiveDate());
		return resp;
	}

	/**
	 * 如果沒有orgName, 表示是前台申請的案件, 則申請單位預設要帶"入口網"
	 * @param oriOrgName
	 * @return
	 */
	private String getOrgName(String oriOrgName) {
		if (StringUtils.isEmpty(oriOrgName)) {
			return TsmpDpModule.DP.getChiDesc();
		}
		return oriOrgName;
	}

	private DPB0068ApiUserApply getApiUserApply(DpReqQueryResp<DpReqQueryResp_D1> d1Resp) {
		DPB0068ApiUserApply apiUserApply = new DPB0068ApiUserApply();
		List<DpReqQueryResp_D1> detailList = d1Resp.getDetailList();
		if (detailList == null || detailList.isEmpty()) {
			return apiUserApply;
		}
		
		List<DPB0068D1> apiList = null;
		for(DpReqQueryResp_D1 detail : detailList) {
			apiUserApply.setClientId(detail.getClientId());
			apiUserApply.setClientName(detail.getClientName());
			apiUserApply.setClientAlias(detail.getClientAlias());
			apiList = getApiList(detail.getApiList());
			apiUserApply.setApiList(apiList);
		}

		List<TsmpDpFile> attachments = d1Resp.getAttachments();
		if (attachments != null && !attachments.isEmpty()) {
			TsmpDpFile attachment = attachments.get(0);	// 用戶申請API只會有一個附件
			if (attachment != null) {
				apiUserApply.setFileName(attachment.getFileName());
				apiUserApply.setFilePath(attachment.getFilePath() + attachment.getFileName());
			}
		}
		return apiUserApply;
	}

	private List<DPB0068D1> getApiList(List<DpReqQueryResp_D1d> d1RespApiList) {
		if (d1RespApiList == null || d1RespApiList.isEmpty()) {
			return Collections.emptyList();
		}
		List<DPB0068D1> apiList = new ArrayList<>();
		DPB0068D1 dpb0068D1 = null;
		Map<String, String> themeList = null;
		Map<String, String> docFileInfo = null;
		for(DpReqQueryResp_D1d d1RespApi : d1RespApiList) {
			themeList = getD1ThemeList(d1RespApi.getThemes());
			docFileInfo = getD1DocFileInfo(d1RespApi.getApiAttachments());
			dpb0068D1 = new DPB0068D1();
			dpb0068D1.setReqOrderd1Id(d1RespApi.getReqOrderd1Id());
			dpb0068D1.setApiUid(d1RespApi.getApiUid());
			dpb0068D1.setApiName(d1RespApi.getApiName());
			dpb0068D1.setModuleName(d1RespApi.getModuleName());
			dpb0068D1.setOrgName(d1RespApi.getOrgName());
			dpb0068D1.setThemeList(themeList);
			dpb0068D1.setApiDesc(d1RespApi.getApiDesc());
			dpb0068D1.setDocFileInfo(docFileInfo);
			dpb0068D1.setApiKey(d1RespApi.getApiKey());
			dpb0068D1.setOrgId(d1RespApi.getOrgId());
			dpb0068D1.setApiExtId(d1RespApi.getApiExtId());
			dpb0068D1.setDpStatus(d1RespApi.getDpStatus());
			dpb0068D1.setPublicFlag(d1RespApi.getPublicFlag());
			dpb0068D1.setPublicFlagName(d1RespApi.getPublicFlagName());
			apiList.add(dpb0068D1);
		}
		return apiList;
	}

	private Map<String, String> getD1ThemeList(Map<Long, String> themes){
		if (themes == null || themes.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, String> d1ThemeList = new HashMap<>();
		for (Map.Entry<Long, String> entry : themes.entrySet()) {
			d1ThemeList.put(String.valueOf(entry.getKey()), entry.getValue());
		}
		return d1ThemeList;
	}

	private Map<String, String> getD1DocFileInfo(List<TsmpDpFile> apiAttachments) {
		if (apiAttachments == null || apiAttachments.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, String> d1DocFileInfo = new HashMap<>();
		for (TsmpDpFile apiAttachment : apiAttachments) {
			d1DocFileInfo.put(apiAttachment.getFileName(), apiAttachment.getFilePath() + apiAttachment.getFileName());
		}
		return d1DocFileInfo;
	}

	private DPB0068ApiOnOff getApiOnOff(DpReqQueryResp<DpReqQueryResp_D2> d2Resp) {
		DPB0068ApiOnOff apiOnOff = new DPB0068ApiOnOff();
		
		List<DpReqQueryResp_D2> d2List = d2Resp.getDetailList();
		if (d2List == null || d2List.isEmpty()) {
			return apiOnOff;
		}
		setD2PublicFlag(apiOnOff, d2List);
		List<DPB0068D2> apiOnOffList = getApiOnOffList(d2List);
		apiOnOff.setApiOnOffList(apiOnOffList);
		return apiOnOff;
	}

	private List<DPB0068D2> getApiOnOffList(List<DpReqQueryResp_D2> d2List) {
		List<DPB0068D2> apiOnOffList = new ArrayList<>();
		
		DPB0068D2 apiOnOff = null;
		Map<String, String> themeList = null;
		Map<String, String> docFileInfo = null;
		for(DpReqQueryResp_D2 d2 : d2List) {
			apiOnOff = new DPB0068D2();
			apiOnOff.setReqOrderd2Id(d2.getReqOrderd2Id());
			apiOnOff.setApiName(d2.getApiName());
			apiOnOff.setApiUid(d2.getApiUid());
			themeList = getThemeList(d2.getD2dRespList());
			apiOnOff.setThemeList(themeList);
			docFileInfo = getDocFileInfo(d2);
			apiOnOff.setDocFileInfo(docFileInfo);
			apiOnOff.setModuleName(d2.getModuleName());
			apiOnOff.setApiDesc(d2.getApiDesc());
			apiOnOff.setOrgName(d2.getOrgName());
			apiOnOff.setApiKey(d2.getApiKey());
			apiOnOff.setOrgId(d2.getOrgId());
			apiOnOff.setApiExtId(d2.getApiExtId());
			apiOnOff.setDpStatus(d2.getDpStatus());
			apiOnOff.setPublicFlag(d2.getPublicFlag());
			apiOnOff.setPublicFlagName(d2.getPublicFlagName());
			apiOnOffList.add(apiOnOff);
		}
		
		return apiOnOffList;
	}

	/**
	 * 雖然每一筆D2都有一個publicFlag，但因申請單填寫時，是指定publicFlag給一批API<br>
	 * 所以查詢時，正常是同一張申請單的publicFlag只有一種
	 * @param apiOnOff
	 * @param d2List
	 */
	private void setD2PublicFlag(DPB0068ApiOnOff apiOnOff, List<DpReqQueryResp_D2> d2List) {
		Set<String> publicFlags = new HashSet<String>();
		Set<String> publicFlagNames = new HashSet<>();

		for (DpReqQueryResp_D2 d2 : d2List) {
			publicFlags.add(d2.getPublicFlag());
			publicFlagNames.add(d2.getPublicFlagName());
		}

		if (publicFlags.size() > 1 || publicFlagNames.size() > 1) {
			this.logger.error("同一張API上下架申請單有不同的開放狀態(publicFlag): " + publicFlags);
			throw TsmpDpAaRtnCode._1217.throwing();
		}

		apiOnOff.setPublicFlag( String.join(", ", publicFlags) );
		apiOnOff.setPublicFlagName( String.join(", ", publicFlagNames) );
	}

	private Map<String, String> getThemeList(List<DpReqQueryResp_D2d> d2dList) {
		if (d2dList == null || d2dList.isEmpty()) {
			return Collections.emptyMap();
		}
		
		Map<String, String> themeList = new LinkedHashMap<>();
		String themeId = null;
		String themeName = null;
		for(DpReqQueryResp_D2d d2d : d2dList) {
			themeId = String.valueOf(d2d.getRefThemeId());
			themeName = d2d.getApiThemeName();
			themeList.put(themeId, themeName);
		}
		return themeList;
	}

	private Map<String, String> getDocFileInfo(DpReqQueryResp_D2 d2) {
		if (StringUtils.isEmpty(d2.getFileName()) || StringUtils.isEmpty(d2.getFilePath())) {
			return Collections.emptyMap();
		}
		Map<String, String> docFileInfo = new HashMap<>();
		docFileInfo.put(d2.getFileName(), d2.getFilePath());
		return docFileInfo;
	}

	private DPB0068ClientReg getClientReg(DpReqQueryResp<DpReqQueryResp_D3> d3Resp) {
		DPB0068ClientReg clientReg = new DPB0068ClientReg();
		List<DpReqQueryResp_D3> detailList = d3Resp.getDetailList();
		if (detailList == null || detailList.isEmpty()) {
			return clientReg;
		}
		DpReqQueryResp_D3 detail = detailList.get(0);	// 一張申請單只會有一筆明細
		clientReg.setReqOrderd3Id(detail.getReqOrderd3Id());
		clientReg.setClientId(detail.getClientId());
		clientReg.setClientName(detail.getClientName());
		clientReg.setEmails(detail.getEmails());
		clientReg.setPublicFlag(detail.getPublicFlag());
		clientReg.setPublicFlagName(detail.getPublicFlagName());
		
		List<TsmpDpFile> attachments = d3Resp.getAttachments();
		if (attachments != null && !attachments.isEmpty()) {
			TsmpDpFile attachment = attachments.get(0);	// 用戶註冊只會有一個附件
			if (attachment != null) {
				clientReg.setFileName(attachment.getFileName());
				clientReg.setFilePath(attachment.getFilePath() + attachment.getFileName());
			}
		}
		return clientReg;
	}

	private DPB0068OpenApiKey getOpenApiKey(DpReqQueryResp<DpReqQueryResp_D5> d5Resp) {
		DPB0068OpenApiKey openApiKey = new DPB0068OpenApiKey();
		List<DpReqQueryResp_D5> detailList = d5Resp.getDetailList();
		if (detailList == null || detailList.isEmpty()) {
			return openApiKey;
		}
		
		DpReqQueryResp_D5 detail = detailList.get(0);	// 一張申請單只會有一筆明細
		openApiKey.setReqOrderd5Id(detail.getReqOrderd5Id());
		openApiKey.setClientId(detail.getClientId());//用戶代碼
		openApiKey.setClientName(detail.getClientName());//用戶端代號
		openApiKey.setClientAlias(detail.getClientAlias());//用戶端名稱
		openApiKey.setOpenApiKeyId(detail.getOpenApiKeyId());
		openApiKey.setOpenApiKey(detail.getOpenApiKey());
		openApiKey.setSecretKey(detail.getSecretKey());
		openApiKey.setOpenApiKeyAlias(detail.getOpenApiKeyAlias());
		openApiKey.setExpiredAt(detail.getExpiredAt());
		openApiKey.setTimesThreshold(detail.getTimesThreshold());
		
		List<DpReqQueryResp_D5> d5List = d5Resp.getDetailList();
		if (d5List == null || d5List.isEmpty()) {
			return openApiKey;
		}
		List<DPB0068D5> openApiKeyD5List = getOpenApiKeyD5List(d5List);
		openApiKey.setApiDatas(openApiKeyD5List);
		return openApiKey;
	}

	private List<DPB0068D5> getOpenApiKeyD5List(List<DpReqQueryResp_D5> d5List) {
		List<DPB0068D5> openApiKeyD5List = new ArrayList<>();
		
		for(DpReqQueryResp_D5 d5 : d5List) {
			List<DpReqQueryResp_D5d> d5dList = d5.getD5dRespList();
			for (DpReqQueryResp_D5d d5d : d5dList) {
				DPB0068D5 openApiKeyD5 = new DPB0068D5();
				openApiKeyD5.setReqOrderd5dId(d5d.getReqOrderd5dId());
				openApiKeyD5.setApiKey(d5d.getApiKey());
				openApiKeyD5.setModuleName(d5d.getModuleName());
				openApiKeyD5.setApiName(d5d.getApiName());
				openApiKeyD5.setThemeList(d5d.getThemeList());
				openApiKeyD5.setOrgId(d5d.getOrgId());
				openApiKeyD5.setOrgName(d5d.getOrgName());
				openApiKeyD5.setApiDesc(d5d.getApiDesc());
				openApiKeyD5.setApiExtId(d5d.getApiExtId());
				openApiKeyD5.setApiUid(d5d.getApiUid());
				openApiKeyD5.setDocFileInfo(d5d.getDocFileInfo());
				openApiKeyD5List.add(openApiKeyD5);
			}
		}
		
		return openApiKeyD5List;
	}
	
	/*
	private final String toUpper(String s) {
		return (s == null ? null : s.toUpperCase());
	}
	*/

	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}

	protected TsmpDpReqOrdermDao getTsmpDpReqOrdermDao() {
		return this.tsmpDpReqOrdermDao;
	}

	protected TsmpDpReqOrdersDao getTsmpDpReqOrdersDao() {
		return this.tsmpDpReqOrdersDao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected TsmpDpChkLayerDao getTsmpDpChkLayerDao() {
		return this.tsmpDpChkLayerDao;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

	protected TsmpDpChkLogDao getTsmpDpChkLogDao() {
		return this.tsmpDpChkLogDao;
	}
	
	protected DpReqQueryIfs<DpReqQueryResp_D1> getDpReqQueryD1() {
		return this.dpReqQueryD1;
	}
	
	protected DpReqQueryIfs<DpReqQueryResp_D2> getDpReqQueryD2() {
		return this.dpReqQueryD2;
	}
	
	protected DpReqQueryIfs<DpReqQueryResp_D3> getDpReqQueryD3() {
		return this.dpReqQueryD3;
	}
	
	protected DpReqQueryIfs<DpReqQueryResp_D5> getDpReqQueryD5() {
		return this.dpReqQueryD5;
	}
	
	protected DPB0067Service getDPB0067Service() {
		return dpb0067Service;
	}
}