package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.getKeywords;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.component.job.DPB0067Job;
import tpi.dgrv4.dpaa.component.req.DpReqQueryIfs;
import tpi.dgrv4.dpaa.component.req.DpReqQueryResp;
import tpi.dgrv4.dpaa.component.req.DpReqQueryResp_D1;
import tpi.dgrv4.dpaa.component.req.DpReqQueryResp_D2;
import tpi.dgrv4.dpaa.component.req.DpReqQueryResp_D3;
import tpi.dgrv4.dpaa.component.req.DpReqQueryResp_D5;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.constant.TsmpDpQuyType;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0067Req;
import tpi.dgrv4.dpaa.vo.DPB0067Resp;
import tpi.dgrv4.dpaa.vo.DPB0067RespItem;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.entity.vo.DPB0067SearchCriteria;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.component.job.JobHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0067Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpDpReqOrdermDao tsmpDpReqOrdermDao;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpUserDao tsmpUserDao;
	private AuthoritiesDao authoritiesDao;
	private BcryptParamHelper bcryptParamHelper;
	private DpReqQueryIfs<DpReqQueryResp_D1> dpReqQueryD1;
	private DpReqQueryIfs<DpReqQueryResp_D2> dpReqQueryD2;
	private DpReqQueryIfs<DpReqQueryResp_D3> dpReqQueryD3;
	private DpReqQueryIfs<DpReqQueryResp_D5> dpReqQueryD5;
	private ServiceConfig serviceConfig;
	private JobHelper jobHelper;
	private ApplicationContext ctx;
	private DgrAcIdpUserDao dgrAcIdpUserDao;

	private Integer pageSize;

	@Autowired
	public DPB0067Service(TsmpDpReqOrdermDao tsmpDpReqOrdermDao, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpUserDao tsmpUserDao, AuthoritiesDao authoritiesDao, BcryptParamHelper bcryptParamHelper,
			DpReqQueryIfs<DpReqQueryResp_D1> dpReqQueryD1, DpReqQueryIfs<DpReqQueryResp_D2> dpReqQueryD2,
			DpReqQueryIfs<DpReqQueryResp_D3> dpReqQueryD3, DpReqQueryIfs<DpReqQueryResp_D5> dpReqQueryD5,
			ServiceConfig serviceConfig, JobHelper jobHelper, ApplicationContext ctx, DgrAcIdpUserDao dgrAcIdpUserDao) {
		super();
		this.tsmpDpReqOrdermDao = tsmpDpReqOrdermDao;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpUserDao = tsmpUserDao;
		this.authoritiesDao = authoritiesDao;
		this.bcryptParamHelper = bcryptParamHelper;
		this.dpReqQueryD1 = dpReqQueryD1;
		this.dpReqQueryD2 = dpReqQueryD2;
		this.dpReqQueryD3 = dpReqQueryD3;
		this.dpReqQueryD5 = dpReqQueryD5;
		this.serviceConfig = serviceConfig;
		this.jobHelper = jobHelper;
		this.ctx = ctx;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
	}

	public DPB0067Resp queryReqLikeList(TsmpAuthorization authorization, DPB0067Req req, ReqHeader reqHeader) {
		String local = ServiceUtil.getLocale(reqHeader.getLocale());
		// 檢查共同欄位
		String quyType = checkCommonParams(authorization, req, local);
		
		//刪除過期的草稿
		if (req.getReqOrdermId()==null) {
			try {
				DPB0067Job job = (DPB0067Job) getCtx().getBean("dpb0067Job", reqHeader.getLocale());
				getJobHelper().add(job);
			} catch (Exception e) {
				logger.debug("草稿申請單刪除失敗: {}" + e.getMessage());
			}			
		}
		
		List<TsmpDpReqOrderm> mList = null;
		//REQ: 申請單, EXA: 待審單, REV: 已審單。
		if (TsmpDpQuyType.REQ.value().equals(quyType)) {
			mList = queryPersonal(authorization, req);
			
		} else if(TsmpDpQuyType.EXA.value().equals(quyType)) {
			mList = queryReviewWork(authorization, req);
			
		} else if(TsmpDpQuyType.REV.value().equals(quyType)) {
			mList = queryReviewHistory(authorization, req);
			
		}
		
		if (mList == null || mList.isEmpty()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}

		DPB0067Resp resp = new DPB0067Resp();
		List<DPB0067RespItem> dataList = getDataList(quyType, mList, reqHeader.getLocale());
		resp.setDataList(dataList);
		return resp;
	}

	private String checkCommonParams(TsmpAuthorization auth, DPB0067Req req, String locale) {
		final String clientId = auth.getClientId();

		String encodeQuyType = req.getEncodeQuyType();
		String quyType = decodeEncodeQuyType(encodeQuyType, locale);
		final String startDate = req.getStartDate();
		final String endDate = req.getEndDate();
		if (StringUtils.isEmpty(clientId) ||
			StringUtils.isEmpty(quyType) ||
			!(TsmpDpQuyType.REQ.value().equals(quyType) ||
					TsmpDpQuyType.EXA.value().equals(quyType) || 
					TsmpDpQuyType.REV.value().equals(quyType) ) ||
			StringUtils.isEmpty(startDate) ||
			StringUtils.isEmpty(endDate)
		) {
			throw TsmpDpAaRtnCode._1216.throwing();
		}
		
		checkBcryptParams(req, locale);

		return quyType;
	}

	public void checkBcryptParams(DPB0067Req req, String locale) {
		String encodeReqType = req.getEncodeReqType();
		String encodeReqSubtype = req.getEncodeReqSubtype();
		if (!StringUtils.isEmpty(encodeReqType)) {
			try {
				encodeReqType = decodeEncodeReqType(encodeReqType, locale);
				req.setEncodeReqType(encodeReqType);
				
				if (!StringUtils.isEmpty(encodeReqSubtype)) {
					encodeReqSubtype = getBcryptParamHelper().decode(encodeReqSubtype, encodeReqType, locale);
					req.setEncodeReqSubtype(encodeReqSubtype);
				}
			} catch (BcryptParamDecodeException e) {
				throw TsmpDpAaRtnCode._1299.throwing();
			}
		}
	}

	private List<TsmpDpReqOrderm> queryPersonal(TsmpAuthorization authorization, DPB0067Req req) {
		final Date startDate = toDate(req.getStartDate(), false);
		final Date endDate = toDate(req.getEndDate(), true);
		if (startDate == null || endDate == null) {
			throw TsmpDpAaRtnCode._1216.throwing();
		}

		final String clientId = authorization.getClientId();
		String userNameForQuery = authorization.getUserNameForQuery();
		String idPType = authorization.getIdpType();
		final String userId = getAcUserIdOrIdpUserName(userNameForQuery);
		if (!StringUtils.hasLength(userId)) {
			throw TsmpDpAaRtnCode._1216.throwing();
		}

		DPB0067SearchCriteria sc = new DPB0067SearchCriteria();
		sc.setStartDate(startDate);
		sc.setEndDate(endDate);
		sc.setClientId(clientId);
		sc.setUserId(userId);
		String[] words = getKeywords(req.getKeyword(), " ");
		sc.setWords(words);
		sc.setReqType(req.getEncodeReqType());
		sc.setReqSubtype(req.getEncodeReqSubtype());
		sc.setLastRecord(getLastRecordFromPrevPage(req.getReqOrdermId()));
		sc.setPageSize(getPageSize());
		
		return getTsmpDpReqOrdermDao().query_dpb0067_queryPersonal(sc);
	}
	public String getAcUserIdOrIdpUserName(String userNameForQuery) {
		DgrAcIdpUser dgrAcIdpUser = getDgrAcIdpUserDao().findFirstByUserName(userNameForQuery).orElse(null);
		if (dgrAcIdpUser != null) {
			return userNameForQuery;
		} else {
			TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userNameForQuery);
			return tsmpUser.getUserId();
		}
	}
	private List<TsmpDpReqOrderm> queryReviewWork(TsmpAuthorization authorization, DPB0067Req req) {
		final String orgId = authorization.getOrgId();
		String userNameForQuery = authorization.getUserNameForQuery();
		String idPType = authorization.getIdpType();
		final Date startDate = toDate(req.getStartDate(), false);
		final Date endDate = toDate(req.getEndDate(), true);
		if (startDate == null || endDate == null ||
			StringUtils.isEmpty(orgId) ||
			StringUtils.isEmpty(userNameForQuery)
		) {
			throw TsmpDpAaRtnCode._1216.throwing();
		}
		
		List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
		List<String> roleIdList = getRoleIdList(userNameForQuery, idPType);
		if (orgDescList == null || orgDescList.isEmpty() ||
			roleIdList == null || roleIdList.isEmpty()
		) {
			throw TsmpDpAaRtnCode._1216.throwing();
		}
		
		DPB0067SearchCriteria sc = new DPB0067SearchCriteria();
		sc.setStartDate(startDate);
		sc.setEndDate(endDate);
		sc.setReqType(req.getEncodeReqType());
		sc.setReqSubtype(req.getEncodeReqSubtype());
		sc.setOrgDescList(orgDescList);
		sc.setRoleIdList(roleIdList);
		String[] words = getKeywords(req.getKeyword(), " ");
		sc.setWords(words);
		sc.setLastRecord(getLastRecordFromPrevPage(req.getReqOrdermId()));
		sc.setPageSize(getPageSize());
		
		return getTsmpDpReqOrdermDao().query_dpb0067_queryReviewWork(sc);
	}
	
	private List<TsmpDpReqOrderm> queryReviewHistory(TsmpAuthorization authorization, DPB0067Req req) {
		final String orgId = authorization.getOrgId();
		String userNameForQuery = authorization.getUserNameForQuery();
		String idPType = authorization.getIdpType();
		final Date startDate = toDate(req.getStartDate(), false);
		final Date endDate = toDate(req.getEndDate(), true);
		if (startDate == null || endDate == null ||
			StringUtils.isEmpty(orgId) ||
			StringUtils.isEmpty(userNameForQuery)
		) {
			throw TsmpDpAaRtnCode._1216.throwing();
		}
		List<String> orgDescList = getTsmpOrganizationDao().queryOrgDescendingByOrgId_rtn_id(orgId, Integer.MAX_VALUE);
	
		List<String> roleIdList = getRoleIdList(userNameForQuery, idPType);
		if (orgDescList == null || orgDescList.isEmpty() ||
			roleIdList == null || roleIdList.isEmpty()
		) {
			throw TsmpDpAaRtnCode._1216.throwing();
		}

		DPB0067SearchCriteria sc = new DPB0067SearchCriteria();
		sc.setStartDate(startDate);
		sc.setEndDate(endDate);
		String[] words = getKeywords(req.getKeyword(), " ");
		sc.setWords(words);
		sc.setOrgDescList(orgDescList);
		sc.setRoleIdList(roleIdList);
		sc.setUserName(userNameForQuery);
		sc.setReqType(req.getEncodeReqType());
		sc.setReqSubtype(req.getEncodeReqSubtype());
		sc.setPageSize(getPageSize());
		sc.setLastRecord(getLastRecordFromPrevPage(req.getReqOrdermId()));
		
		return getTsmpDpReqOrdermDao().query_dpb0067_queryReviewHistory(sc);
	}
	
	private List<String> getRoleIdList(String userNameForQuery, String idPType){
		List<Authorities> authorities = getAuthoritiesDao().findByUsername(userNameForQuery);
		if (authorities == null || authorities.isEmpty()) {
			return Collections.emptyList();
		}
		return authorities.stream().map((a) -> {
			return a.getAuthority();
		}).collect(Collectors.toList());
	}

	private List<DPB0067RespItem> getDataList(String isPersonal, List<TsmpDpReqOrderm> mList, String locale) {
		List<DPB0067RespItem> dpb0067RespItems = new ArrayList<>();
		DPB0067RespItem dpb0067RespItem = null;
		/* 改用簽核模組
		String createDateTime = null;
		String applyUserName = null;
		String orgName = null;
		*/
		String createDateTime = null;
		String applyType = null;
		DpReqQueryIfs<?> dpReqQuery = null;
		for(TsmpDpReqOrderm m : mList) {
			dpb0067RespItem = new DPB0067RespItem();
			/* 改用簽核模組
			dpb0067RespItem.setReqOrdermId(m.getReqOrdermId());
			createDateTime = getCreateDateTime(m.getCreateDateTime());
			dpb0067RespItem.setCreateDateTime(createDateTime);
			dpb0067RespItem.setReqOrderNo(m.getReqOrderNo());
			dpb0067RespItem.setClientId(m.getClientId());
			dpb0067RespItem.setReqUserId(m.getReqUserId());
			applyUserName = getApplyUserName(m.getClientId(), m.getReqUserId());
			dpb0067RespItem.setApplyUserName(applyUserName);
			applyType = getApplyType(m.getReqType(), m.getReqSubtype());
			dpb0067RespItem.setApplyType(applyType);
			setReqOrdersAndVisible(dpb0067RespItem, m, isPersonal);
			orgName = getOrgName(m.getOrgId());
			dpb0067RespItem.setOrgName(orgName);
			*/
			dpReqQuery = getDpReqQueryByReqType(m.getReqType());
			if (dpReqQuery == null) {
				throw TsmpDpAaRtnCode._1297.throwing();	//執行錯誤
			}
			DpReqQueryResp<?> dpReqQueryResp = dpReqQuery.doQuery(m.getReqOrdermId(), locale);
			if (dpReqQueryResp == null) {
				throw TsmpDpAaRtnCode._1217.throwing();	//單據查詢錯誤
			}
			
			applyType = getApplyType(dpReqQueryResp.getReqTypeName(), dpReqQueryResp.getReqSubtypeName());
			createDateTime = getCreateDateTime(dpReqQueryResp.getCreateDateTime());
			
			dpb0067RespItem.setReqOrdermId(m.getReqOrdermId());
			dpb0067RespItem.setCreateDateTime(createDateTime);
			dpb0067RespItem.setReqOrderNo(dpReqQueryResp.getReqOrderNo());
			dpb0067RespItem.setClientId(dpReqQueryResp.getClientId());
			dpb0067RespItem.setReqUserId(dpReqQueryResp.getReqUserId());
			dpb0067RespItem.setApplyUserName(getUserName(dpReqQueryResp.getReqUserId()));
			dpb0067RespItem.setApplyType(applyType);
			setReqOrdersAndVisible(dpb0067RespItem, dpReqQueryResp, isPersonal);
			dpb0067RespItem.setOrgName(dpReqQueryResp.getOrgName());
			
			dpb0067RespItems.add(dpb0067RespItem);
		}
		return dpb0067RespItems;
	}

	public String getUserName(String reqUserId) {

		DgrAcIdpUser dgrAcIdpUser = getDgrAcIdpUserDao().findFirstByUserName(reqUserId).orElse(null);

		// Table 查不到 user
		if (dgrAcIdpUser != null) {

			if (StringUtils.hasLength(dgrAcIdpUser.getUserAlias())) {
				return dgrAcIdpUser.getUserAlias();

			}
			return dgrAcIdpUser.getUserName();
		} else {// 以 AC 登入
			TsmpUser tsmpUser = getTsmpUserDao().findById(reqUserId).orElse(null);
			// Table 查不到 user
			if (tsmpUser == null) {
				return new String();
			}
			return tsmpUser.getUserName();
		}

	}

	private DpReqQueryIfs<?> getDpReqQueryByReqType(String reqType) {
		if (TsmpDpReqReviewType.API_APPLICATION.isValueEquals(reqType)) {
			return getDpReqQueryD1();
		} else if (TsmpDpReqReviewType.API_ON_OFF.isValueEquals(reqType)) {
			return getDpReqQueryD2();
		} else if (TsmpDpReqReviewType.CLIENT_REG.isValueEquals(reqType)) {
			return getDpReqQueryD3();
		} else if (TsmpDpReqReviewType.OPEN_API_KEY.isValueEquals(reqType)) {
			return getDpReqQueryD5();
		}
		return null;
	}

	private String getCreateDateTime(Date createDateTime) {
		String pattern = "yyyyMMdd HH:mm:ss";
		return new SimpleDateFormat(pattern).format(createDateTime);
	}

	/**
	 * @param rt: reviewType
	 * @param rst: reviewSubtype
	 * @return
	 */
	private String getApplyType(String rt, String rst) {
		if (!StringUtils.isEmpty(rst)) {
			return rst;
		} else {
			return rt;
		}
	}

	// 改用簽核模組
	private void setReqOrdersAndVisible(DPB0067RespItem respItem, DpReqQueryResp<?> dpReqQueryResp, //
			String quyType) {
		respItem.setNextCheckPoint(dpReqQueryResp.getCurrentLayer());
		respItem.setCheckPointName(dpReqQueryResp.getCurrentLayerName());
		respItem.setChkStatus(dpReqQueryResp.getCurrentReviewStatusName());
		
		boolean isSignable = dpReqQueryResp.isSignable();
		boolean isEndable = dpReqQueryResp.isEndable();
		boolean isResendable = dpReqQueryResp.isResendable();
		boolean isupdatable = dpReqQueryResp.isUpdatable();
		boolean isSendable = dpReqQueryResp.isSendable();
		
		// [簽核]:送審了且User為審核者 = true
		respItem.setReviewVisiable("N");
		if (TsmpDpQuyType.EXA.value().equals(quyType) && isSignable) {
			respItem.setReviewVisiable("Y");
		}

		// [歷程]:每個case 都 = true (不限條件,皆可查看歷程)
		respItem.setTrakerVisiable("Y");


		// [結案]:案件在草稿、未受審或是退回狀態下, 且User為申請者時, 回傳"true"
		respItem.setCloseVisiable("N");
		if (TsmpDpQuyType.REQ.value().equals(quyType) && isEndable) {
			respItem.setCloseVisiable("Y");
		}

		// [重送]:被簽核者退回且 User為申請者 = true
		respItem.setResendVisiable("N");
		if (TsmpDpQuyType.REQ.value().equals(quyType) && isResendable) {
			respItem.setResendVisiable("Y");
		}

		// [更新]:尚未送審, 且User為申請者時, 回傳"true"
		respItem.setUpdateVisiable("N");
		if (TsmpDpQuyType.REQ.value().equals(quyType) && isupdatable) {
			respItem.setUpdateVisiable("Y");
		}

		// [送審]:關卡停留在申請者, 且User為申請者時, 回傳"true"
		respItem.setSendVisible("N");
		if (TsmpDpQuyType.REQ.value().equals(quyType) && isSendable) {
			respItem.setSendVisible("Y");
		}
	}

	public String getUserId(String userNameForQuery, String idPType) {
		if (!StringUtils.hasLength(userNameForQuery)) {
			return new String();
		}
		if (StringUtils.hasLength(idPType) ) {// 以 IdP 登入 AC

			DgrAcIdpUser dgrAcIdpUser = getDgrAcIdpUserDao().findFirstByUserNameAndIdpType(userNameForQuery, idPType);
			// Table 查不到 user
			if (dgrAcIdpUser == null) {
				return new String();
			}
			return idPType + "." + dgrAcIdpUser.getUserName();
		} else {// 以 AC 登入
			TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userNameForQuery);
			// Table 查不到 user
			if (tsmpUser == null) {
				return new String();
			}
			return tsmpUser.getUserId();
		}
	}

	private TsmpDpReqOrderm getLastRecordFromPrevPage(Long reqOrdermId) {
		if (reqOrdermId != null) {
			Optional<TsmpDpReqOrderm> opt_m = getTsmpDpReqOrdermDao().findById(reqOrdermId);
			return opt_m.orElse(null);
		}
		return null;
	}

	private Date toDate(String dateString, boolean isEnd) {
		Date d = DateTimeUtil.stringToDateTime(dateString, DateTimeFormatEnum.西元年月日_2).orElse(null);
		if (d != null && isEnd) {
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			c.set(Calendar.HOUR_OF_DAY, 23);
			c.set(Calendar.MINUTE, 59);
			c.set(Calendar.SECOND, 59);
			c.set(Calendar.MILLISECOND, 999);
			d = c.getTime();
		}
		return d;
	}
	
	protected String decodeEncodeReqType(String encodeReqType, String locale) {
		String reqType = null;
		try {
			reqType = getBcryptParamHelper().decode(encodeReqType, TsmpDpReqReviewType.ITEM_NO, locale);
		} catch (BcryptParamDecodeException e) {
			TPILogger.tl.error("encodeQuyType=" + encodeReqType + "\nlocale=" + locale);
			throw TsmpDpAaRtnCode._1299.throwing();
		}
		return reqType;
	}
	
	protected String decodeEncodeQuyType(String encodeQuyType, String locale) {
		String quyType = null;
		if (!StringUtils.isEmpty(encodeQuyType)) {
			try {
				quyType = getBcryptParamHelper().decode(encodeQuyType, "ORDERM_QUY_TYPE", locale);
			} catch (BcryptParamDecodeException e) {
				TPILogger.tl.error("encodeQuyType=" + encodeQuyType + "\nlocale=" + locale);
				throw TsmpDpAaRtnCode._1299.throwing();
			}
		}
		return quyType;
	}

	private final String toUpper(String s) {
		return (s == null ? null : s.toUpperCase());
	}

	protected TsmpDpReqOrdermDao getTsmpDpReqOrdermDao() {
		return this.tsmpDpReqOrdermDao;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}

	protected AuthoritiesDao getAuthoritiesDao() {
		return this.authoritiesDao;
	}

	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
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

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0067");
		return this.pageSize;
	}
	
	protected JobHelper getJobHelper() {
		return this.jobHelper;
	}
	
	protected ApplicationContext getCtx() {
		return this.ctx;
	}
	
	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return this.dgrAcIdpUserDao;
	}

}
