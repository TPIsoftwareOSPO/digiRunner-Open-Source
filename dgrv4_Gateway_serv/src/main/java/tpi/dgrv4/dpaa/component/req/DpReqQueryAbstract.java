package tpi.dgrv4.dpaa.component.req;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpFileType;
import tpi.dgrv4.common.constant.TsmpDpModule;
import tpi.dgrv4.common.constant.TsmpDpReqReviewType;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpDpFile;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderm;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrders;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdersDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

public abstract class DpReqQueryAbstract<T> implements DpReqQueryIfs<T> {

	private TPILogger logger = TPILogger.tl;

	private TsmpDpReqOrdermDao tsmpDpReqOrdermDao;
	private TsmpDpReqOrdersDao tsmpDpReqOrdersDao;
	private TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy;
	private TsmpOrganizationDao tsmpOrganizationDao;
	private TsmpUserDao tsmpUserDao;
	private TsmpClientDao tsmpClientDao;
	private TsmpDpFileDao tsmpDpFileDao;
	private DpReqServiceFactory dpReqServiceFactory;
	
	@Autowired
	protected DpReqQueryAbstract(TsmpDpReqOrdermDao tsmpDpReqOrdermDao, TsmpDpReqOrdersDao tsmpDpReqOrdersDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpUserDao tsmpUserDao, TsmpClientDao tsmpClientDao, TsmpDpFileDao tsmpDpFileDao,
			DpReqServiceFactory dpReqServiceFactory) {
		super();
		this.tsmpDpReqOrdermDao = tsmpDpReqOrdermDao;
		this.tsmpDpReqOrdersDao = tsmpDpReqOrdersDao;
		this.tsmpDpItemsCacheProxy = tsmpDpItemsCacheProxy;
		this.tsmpOrganizationDao = tsmpOrganizationDao;
		this.tsmpUserDao = tsmpUserDao;
		this.tsmpClientDao = tsmpClientDao;
		this.tsmpDpFileDao = tsmpDpFileDao;
		this.dpReqServiceFactory = dpReqServiceFactory;
	}

	@Override
	public DpReqQueryResp<T> doQuery(Long reqOrdermId, String locale) {
		if (reqOrdermId == null) {
			this.logger.debug("未傳入主檔ID");
			return null;
		}
		
		Optional<TsmpDpReqOrderm> opt_m = getTsmpDpReqOrdermDao().findById(reqOrdermId);
		if (!opt_m.isPresent()) {
			this.logger.debug(String.format("申請單序號不存在: %d", reqOrdermId));
			return null;
		}
		TsmpDpReqOrderm m = opt_m.get();

		String reqTypeName = getReqTypeName(m.getReqType(), locale);
		String reqSubtype = nvl(m.getReqSubtype());
		String reqSubtypeName = getReqSubtypeName(m.getReqType(), reqSubtype, locale);
		String clientId = nvl(m.getClientId());
		String reqUserId = nvl(m.getReqUserId());
		String orgId = nvl(m.getOrgId());
		String orgName = getOrgName(orgId);
		String reqDesc = nvl(m.getReqDesc());
		String effectiveDate = getEffectiveDate(m.getEffectiveDate());
		List<TsmpDpFile> attachments = getAttachments(reqOrdermId);
		List<T> detailList = doQueryDetail(reqOrdermId, locale);
		
		DpReqQueryResp<T> resp = new DpReqQueryResp<>();
		resp.setReqOrdermId(reqOrdermId);
		resp.setLv(m.getVersion());
		resp.setReqOrderNo(m.getReqOrderNo());
		resp.setReqType(m.getReqType());
		resp.setReqTypeName(reqTypeName);
		resp.setReqSubtype(reqSubtype);
		resp.setReqSubtypeName(reqSubtypeName);
		resp.setClientId(clientId);
		resp.setReqUserId(reqUserId);
		resp.setOrgId(orgId);
		resp.setOrgName(orgName);
		setFBData(resp, m);	// 設置前、後台申請單相異的欄位
		resp.setReqDesc(reqDesc);
		resp.setEffectiveDate(effectiveDate);
		resp.setCreateDateTime(m.getCreateDateTime());
		resp.setAttachments(attachments);
		setCurrentSData(resp, reqOrdermId, m.getReqType(), locale);
		resp.setDetailList(detailList);
		return resp;
	}

	@Override
	public List<TsmpMailEvent> getTsmpMailEvents(TsmpAuthorization auth, Long reqOrdermId, String locale) {
		DpReqQueryResp<T> resp = doQuery(reqOrdermId, locale);
		//收件者
		Map<String, String> recipientsMap = getMailRecipients(resp);
		List<TsmpMailEvent> mailEvents = new ArrayList<>();
		recipientsMap.forEach((k, v) -> {
			String userId = k;
			String recipients = v;
			
			TsmpMailEvent mailEvent = getTsmpMailEvent(userId, recipients, auth, resp);
			if (mailEvent != null) {
				mailEvents.add(mailEvent);
			}
		});
		
		return mailEvents;
	}

	protected Map<String, String> getMailRecipients(DpReqQueryResp<T> resp) {
		Map<String, String> recipientsMap = new LinkedHashMap<>();
		String fbFlag = resp.getFbFlag();
		
		//找出申請者的資料
		if("B".equals(fbFlag)) {//後台的申請單
			String userId = resp.getReqUserId();
			Optional<TsmpUser> opt_user = getTsmpUserDao().findById(userId);
			if(opt_user.isPresent()) {
				TsmpUser user = opt_user.get();
				recipientsMap.put(user.getUserId(), user.getUserEmail());
			}
		}else {//前台的申請單
			String clientId = resp.getClientId();
			Optional<TsmpClient> opt_client = getTsmpClientDao().findById(clientId);
			if(opt_client.isPresent()) {
				TsmpClient client = opt_client.get();
				recipientsMap.put(client.getClientId(), client.getEmails());
			}
		}
		
		//找出簽核者的資料
		//如果已經沒有下一關就不用找簽核者了
		Integer currentProcFlag = resp.getCurrentProcFlag();
		if (currentProcFlag != 0) {
			String layer = String.valueOf(resp.getCurrentLayer());
			try {
				List<TsmpUser> reviewerList = getTsmpUserDao().queryByReviewTypeAndLayer( //
					resp.getReqType(), Integer.parseInt(layer));
				if(reviewerList != null && !reviewerList.isEmpty()) {
					for (TsmpUser tsmpUser : reviewerList) {
						recipientsMap.put(tsmpUser.getUserId(), tsmpUser.getUserEmail());
					}
				}
			} catch (Exception e) {
				this.logger.debug("Fail getting reviewer's emails!");
			}
		}
		
		return recipientsMap;
	}

	private String getReqTypeName(String reqType, String locale) {
		return getSubitemName(TsmpDpReqReviewType.ITEM_NO, reqType, locale);
	}

	private String getReqSubtypeName(String reqType, String reqSubtype, String locale) {
		return getSubitemName(reqType, reqSubtype, locale);
	}

	private String getSubitemName(String itemNo, String subitemNo, String locale) {
		if (!StringUtils.isEmpty(itemNo) && !StringUtils.isEmpty(subitemNo) && !StringUtils.isEmpty(locale)) {
			TsmpDpItemsId id = new TsmpDpItemsId(itemNo, subitemNo, locale);
			TsmpDpItems i = getTsmpDpItemsCacheProxy().findById(id);
			if (i != null) {
				return i.getSubitemName();
			}
		}
		return new String();
	}

	/**
	 * 如果沒有orgName, 表示是前台申請的案件, 則申請單位預設要帶"digiRunner-入口網"
	 * @param oriOrgName
	 * @return
	 */
	protected String getOrgName(String orgId) {
		String orgName = "";
		if (!StringUtils.isEmpty(orgId)) {
			Optional<TsmpOrganization> opt_o = getTsmpOrganizationDao().findById(orgId);
			if (opt_o.isPresent()) {
				orgName = opt_o.get().getOrgName();
			}
		}
		
		if (StringUtils.isEmpty(orgName)) {
			return TsmpDpModule.DP.getChiDesc();
		}		
		return orgName;
	}

	private void setFBData(DpReqQueryResp<T> resp, TsmpDpReqOrderm m) {
		resp.setApplierName(new String());
		
		String reqUserId = m.getReqUserId();
		if (!StringUtils.isEmpty(reqUserId)) {
			resp.setFbFlag("B");
			resp.setApplierId(reqUserId);
			Optional<TsmpUser> opt_u = getTsmpUserDao().findById(reqUserId);
			if (opt_u.isPresent()) {
				resp.setApplierName(opt_u.get().getUserName());
			}
		} else {
			String clientId = nvl(m.getClientId());
			resp.setFbFlag("F");
			resp.setApplierId(clientId);
			if (!StringUtils.isEmpty(clientId)) {
				Optional<TsmpClient> opt_c = getTsmpClientDao().findById(clientId);
				if (opt_c.isPresent()) {
					resp.setApplierName(opt_c.get().getClientName());
				}
			}
		}
	}

	private String getEffectiveDate(Date effectiveDate) {
		return DateTimeUtil.dateTimeToString(effectiveDate, DateTimeFormatEnum.西元年月日_2).orElse(new String());
	}

	/**
	 * 找出申請單附件
	 * @param reqOrdermId
	 * @return
	 */
	private List<TsmpDpFile> getAttachments(Long reqOrdermId) {
		List<TsmpDpFile> dpFiles = getTsmpDpFileDao().findByRefFileCateCodeAndRefId(TsmpDpFileType.M_ATTACHMENT.value(), reqOrdermId);
		if (dpFiles == null || dpFiles.isEmpty()) {
			return Collections.emptyList();
		}
		return dpFiles;
	}

	private void setCurrentSData(DpReqQueryResp<T> resp, Long reqOrdermId, String reqType, String locale) {
		String currentReviewStatus = new String();
		String currentReviewStatusName = new String();
		Integer currentLayer = null;
		String currentLayerName = new String();
		Integer currentProcFlag = null;
		
		TsmpDpReqOrders currentS = getTsmpDpReqOrdersDao().queryCurrentStatus(reqOrdermId);
		if (currentS != null) {
			currentReviewStatus = currentS.getReviewStatus();
			currentReviewStatusName = getCurrentReviewStatusName(currentReviewStatus, locale);
			currentLayer = currentS.getLayer();
			currentLayerName = getCurrentLayerName(currentLayer,locale);
			currentProcFlag = currentS.getProcFlag();
		}
		
		resp.setCurrentReviewStatus(currentReviewStatus);
		resp.setCurrentReviewStatusName(currentReviewStatusName);
		resp.setCurrentLayer(currentLayer);
		resp.setCurrentLayerName(currentLayerName);
		resp.setCurrentProcFlag(currentProcFlag);
		setStatusFlags(resp, reqOrdermId, reqType);
	}

	private void setStatusFlags(DpReqQueryResp<T> resp, Long reqOrdermId, String reqType) {
		DpReqServiceIfs dpReqService = getDpReqServiceFactory().getDpReqService(reqType, null);
		if (dpReqService == null) {
			return;
		}

		boolean isUpdatable = dpReqService.isUpdatable(reqOrdermId);
		boolean isSubmittable = dpReqService.isSubmittable(reqOrdermId);
		boolean isResubmittable = dpReqService.isResubmittable(reqOrdermId);
		boolean isEndable = dpReqService.isEndable(reqOrdermId);
		boolean isSignable = dpReqService.isSignable(reqOrdermId);

		resp.setUpdatable(isUpdatable);
		resp.setSendable(isSubmittable);
		resp.setResendable(isResubmittable);
		resp.setEndable(isEndable);
		resp.setSignable(isSignable);
	}

	private String getCurrentReviewStatusName(String currentReviewStatus, String locale) {
		return getSubitemName("REVIEW_STATUS", currentReviewStatus, locale);
	}

	private String getCurrentLayerName(Integer currentLayer, String locale) {
		return getSubitemName("CHK_LAYER", String.valueOf(currentLayer), locale);
	}

	protected abstract List<T> doQueryDetail(Long reqOrdermId, String locale);

	protected abstract TsmpMailEvent getTsmpMailEvent(String userId, String recipients, //
		TsmpAuthorization auth, DpReqQueryResp<T> resp);

	protected String nvl(Object obj) {
		return nvl(obj, null);
	}

	protected String nvl(Object obj, Supplier<String> sup) {
		if (obj == null) {
			return new String();
		}
		if (sup != null) {
			return sup.get();
		}
		return String.valueOf(obj);
	}

	protected TsmpDpReqOrdermDao getTsmpDpReqOrdermDao() {
		return this.tsmpDpReqOrdermDao;
	}

	protected TsmpDpReqOrdersDao getTsmpDpReqOrdersDao() {
		return this.tsmpDpReqOrdersDao;
	}

	protected TsmpDpItemsCacheProxy getTsmpDpItemsCacheProxy() {
		return this.tsmpDpItemsCacheProxy;
	}

	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}

	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}

	protected TsmpDpFileDao getTsmpDpFileDao() {
		return this.tsmpDpFileDao;
	}

	protected DpReqServiceFactory getDpReqServiceFactory() {
		return this.dpReqServiceFactory;
	}

}
