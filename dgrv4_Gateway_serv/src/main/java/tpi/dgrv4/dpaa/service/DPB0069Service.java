package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.vo.DPB0069Items;
import tpi.dgrv4.dpaa.vo.DPB0069Req;
import tpi.dgrv4.dpaa.vo.DPB0069Resp;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.jpql.TsmpDpChkLayer;
import tpi.dgrv4.entity.entity.jpql.TsmpDpChkLog;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpDpChkLayerDao;
import tpi.dgrv4.entity.repository.TsmpDpChkLogDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0069Service {
	
	private TPILogger logger = TPILogger.tl;

	private ServiceConfig serviceConfig;
	private AuthoritiesDao authoritiesDao;
	private TsmpDpChkLayerDao tsmpDpChkLayerDao;
	private TsmpDpReqOrdermDao tsmpDpReqOrdermDao;
	private TsmpUserDao tsmpUserDao;
	private TsmpDpChkLogDao tsmpDpChkLogDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private DPB0067Service dpb0067Service;
	private DgrAcIdpUserDao dgrAcIdpUserDao;

	private Integer pageSize;
	
	@Autowired
	public DPB0069Service(ServiceConfig serviceConfig, AuthoritiesDao authoritiesDao,
			TsmpDpChkLayerDao tsmpDpChkLayerDao, TsmpDpReqOrdermDao tsmpDpReqOrdermDao, TsmpUserDao tsmpUserDao,
			TsmpDpChkLogDao tsmpDpChkLogDao, TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, DPB0067Service dpb0067Service,
			DgrAcIdpUserDao dgrAcIdpUserDao) {
		super();
		this.serviceConfig = serviceConfig;
		this.authoritiesDao = authoritiesDao;
		this.tsmpDpChkLayerDao = tsmpDpChkLayerDao;
		this.tsmpDpReqOrdermDao = tsmpDpReqOrdermDao;
		this.tsmpUserDao = tsmpUserDao;
		this.tsmpDpChkLogDao = tsmpDpChkLogDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.dpb0067Service = dpb0067Service;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
	}

	public DPB0069Resp queryHistoryByPk(TsmpAuthorization tsmpAuthorization, DPB0069Req req, ReqHeader reqHeader) {
		DPB0069Resp resp = new DPB0069Resp();

		try {
			Long reqOrdermId = req.getReqOrdermId();
			String orgId = tsmpAuthorization.getOrgId();
			String userNameForQuery = tsmpAuthorization.getUserNameForQuery();
			String idPType = tsmpAuthorization.getIdpType();
			Long chkLogId = req.getChkLogId();
			
			//chk param
			if(StringUtils.isEmpty(reqOrdermId)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			if(StringUtils.isEmpty(orgId)) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
 
			Integer pageSize = getPageSize();
			TsmpDpChkLog lastRecord = getLastRecordFromPrevPage(chkLogId);
			
			Optional<TsmpDpReqOrderm> opt_m = getTsmpDpReqOrdermDao().findById(reqOrdermId);
			TsmpDpReqOrderm m = null;
			if(opt_m.isPresent()) {
				m = opt_m.get();
			}else {
				throw TsmpDpAaRtnCode._1297.throwing();
			}
 
			/* 檢查是否有權限查看簽核歷呈,必需為單據中的申請者及所有審核者 */
			if(isReqUser(userNameForQuery, idPType, reqOrdermId, m) || isReviewer(userNameForQuery, reqOrdermId, m)) {
				List<TsmpDpChkLog> chkLogList = getTsmpDpChkLogDao().queryHistoryByPk(lastRecord, reqOrdermId, pageSize);
				if(chkLogList == null || chkLogList.isEmpty()) {
					//throw TsmpDpAaRtnCode._1298.throwing();
					chkLogList = Collections.emptyList();
				}
				
				resp = getResp(resp, m, chkLogList, reqHeader.getLocale());

			}else {
				throw TsmpDpAaRtnCode._1294.throwing();
			}
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		
		return resp;
	}
	
	private DPB0069Resp getResp(DPB0069Resp resp, TsmpDpReqOrderm m , List<TsmpDpChkLog> chkLogList, String locale) {
		List<DPB0069Items> itemsList = getItems(chkLogList, locale);
		
		Optional<String> opt_date = DateTimeUtil.dateTimeToString(m.getCreateDateTime(), DateTimeFormatEnum.西元年月日);
		String createDate = null;
		if(opt_date.isPresent()) {
			createDate = opt_date.get();
		}
		
		String reqType = m.getReqType();
		String reqTypeName = getSubitemName("REVIEW_TYPE", reqType, locale);//簽核類別
		String subtype = null;
		String subtypeName = null;
		if(TsmpDpReqReviewType.API_ON_OFF.value().equals(reqType)) {//若為 API上下架管理
			subtype = m.getReqSubtype();
			subtypeName = getSubitemName("API_ON_OFF", subtype, locale);
			
		} else if(TsmpDpReqReviewType.OPEN_API_KEY.value().equals(reqType)) {//若為 Open API Key 管理
			subtype = m.getReqSubtype();
			subtypeName = getSubitemName("OPEN_API_KEY", subtype, locale);
		}
		
		String effectiveDateStr = DateTimeUtil //
			.dateTimeToString(m.getEffectiveDate(), DateTimeFormatEnum.西元年月日_2).orElse(new String());
		
		resp.setReqOrdermId(m.getReqOrdermId());
		resp.setReqOrderNo(m.getReqOrderNo());
		resp.setReqCreateDateTime(createDate);
		resp.setReqType(reqType);
		resp.setReqTypeName(reqTypeName);
		resp.setReqSubtype(subtype);
		resp.setReqSubtypeName(subtypeName);
		resp.setDataList(itemsList);
		resp.setEffectiveDate(effectiveDateStr);
		
		return resp;
	}
	
	private List<DPB0069Items> getItems(List<TsmpDpChkLog> chkLogList, String locale) {
		List<DPB0069Items> itemsList = new ArrayList<DPB0069Items>();
		for (TsmpDpChkLog log : chkLogList) {
			DPB0069Items i = new DPB0069Items();
			
			Optional<String> opt_date = DateTimeUtil.dateTimeToString(log.getCreateDateTime(), DateTimeFormatEnum.西元年月日時分秒_3);
			String createDateTime = null;
			if(opt_date.isPresent()) {
				createDateTime = opt_date.get();
			}
			
			int layer = log.getLayer();			
			String layerName = getSubitemName("CHK_LAYER", layer + "", locale);//關卡名稱
			
			String reviewStatus = log.getReviewStatus();
			String reviewStatusName = getSubitemName("REVIEW_STATUS", reviewStatus, locale);//簽核狀態
			String chkCreateUser = getUserAlias(log.getCreateUser());
			i.setChkLogId(log.getChkLogId());
			i.setChkCreateDateTime(createDateTime);
			i.setChkCreateUser(chkCreateUser);
			i.setLayer(layer + "");		
			i.setChkLayerName(layerName);
			i.setReviewStatus(reviewStatus);
			i.setReviewStatusName(reviewStatusName);
			i.setReqComment(log.getReqComment());
			itemsList.add(i);
		}
		return itemsList;
	}
	
	private String getUserAlias(String createUser) {

		DgrAcIdpUser dgrAcIdpUser = getDgrAcIdpUserDao().findFirstByUserName(createUser).orElse(null);
		if (dgrAcIdpUser != null) {
			if (StringUtils.hasLength(dgrAcIdpUser.getUserAlias())) {
				return dgrAcIdpUser.getUserAlias();
			}
			return dgrAcIdpUser.getUserName();
		} else {
			TsmpUser tsmpUser = getTsmpUserDao().findById(createUser).orElse(null);
			if (tsmpUser != null) {
				return tsmpUser.getUserName();
			}
		}
		return createUser;
	}

	private String getSubitemName(String itemNo, String subItemNo, String locale) {
		/* 20200330; Kim; 改成Cache版
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subItemNo);
		Optional<TsmpDpItems> opt_item = getTsmpDpItemDao().findById(id);
		*/
		Optional<TsmpDpItems> opt_item = getItemsById(itemNo, subItemNo, locale);
		String subitemName = null;
		if(opt_item.isPresent()) {
			TsmpDpItems item = opt_item.get();
			subitemName = item.getSubitemName();
		}
		
		return subitemName;
	}

	protected Optional<TsmpDpItems> getItemsById(String itemNo, String subItemNo, String locale) {
		TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subItemNo, locale);
		TsmpDpItems i = getTsmpDpItemsCacheProxy().findById(id);
		return Optional.ofNullable(i);
	}

	private TsmpDpChkLog getLastRecordFromPrevPage(Long TsmpDpChkLog) {
		if (TsmpDpChkLog != null) {
			Optional<TsmpDpChkLog> opt = getTsmpDpChkLogDao().findById(TsmpDpChkLog);
			if (opt.isPresent()) {
				return opt.get();
			}
		}
		return null;
	}
	
	/**
	 * 判斷查詢者是否為申請單的申請者
	 * 
	 * @param userName
	 * @param reqOrdermId
	 * @return
	 */
	private boolean isReqUser(String userNameForQuery, String idPType, Long reqOrdermId, TsmpDpReqOrderm m) {
		boolean flag = false;
		
		String reqUserId = m.getReqUserId();//申請者
 
		//User
		String userId = getDPB0067Service().getUserId(userNameForQuery, idPType);//查詢者
		
		if(!StringUtils.isEmpty(reqUserId) && reqUserId.equals(userId)) {
			flag = true;
		}
		return flag;
	}
	
	/**
	 * 判斷查詢者是否為簽核者
	 * 
	 * @param userName
	 * @param reqOrdermId
	 * @return
	 */
	private boolean isReviewer(String userNameForQuery, Long reqOrdermId, TsmpDpReqOrderm m) {
		boolean flag = false;
		
		//Authorities
		List<Authorities> authList = getAuthoritiesDao().findByUsername(userNameForQuery);
		List<String> roleIdList = new ArrayList<String>();
		if(authList != null && !authList.isEmpty()) {
			for (Authorities auth : authList) {
				String roleId = auth.getAuthority();
				roleIdList.add(roleId);
			}
		}
		
		String reqType = m.getReqType();
		
		//簽核關卡角色設定檔
		if(roleIdList != null && !roleIdList.isEmpty()) {
			List<TsmpDpChkLayer> chkLayList = getTsmpDpChkLayerDao().queryByReviewTypeAndRoleIdList(reqType, roleIdList);
			if(chkLayList != null && !chkLayList.isEmpty()) {
				flag = true;
			}
		}
		return flag;
	}
	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}
	
	protected AuthoritiesDao getAuthoritiesDao() {
		return this.authoritiesDao;
	}
	
	protected TsmpDpChkLayerDao getTsmpDpChkLayerDao() {
		return this.tsmpDpChkLayerDao;
	}
	
	protected TsmpDpReqOrdermDao getTsmpDpReqOrdermDao() {
		return this.tsmpDpReqOrdermDao;
	}
	
	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}
	
	protected TsmpDpChkLogDao getTsmpDpChkLogDao() {
		return this.tsmpDpChkLogDao;
	}
	
	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0069");
		return this.pageSize;
	}
	
	protected DPB0067Service getDPB0067Service() {
		return dpb0067Service;
	}
}
