package tpi.dgrv4.dpaa.component.req;

import java.util.Date;
import java.util.List;

import tpi.dgrv4.entity.entity.TsmpDpFile;

public class DpReqQueryResp<T> {

	/** from tsmp_dp_req_orderm.req_orderm_id */
	private Long reqOrdermId;

	/** from tsmp_dp_req_orderm.version */
	private Long lv;

	/** 案件編號, from tsmp_dp_req_orderm.req_order_no */
	private String reqOrderNo;

	/** 簽核類別代碼, from tsmp_dp_req_orderm.req_type */
	private String reqType;

	/**
	 * 簽核類別名稱 from tsmp_dp_items.subitem_name where item_no = 'REVIEW_TYPE' and
	 * subitem_no = m.req_type
	 */
	private String reqTypeName;

	/** 簽核子類別代碼, from tsmp_dp_req_orderm.req_subtype */
	private String reqSubtype;

	/**
	 * 簽核子類別名稱, from tsmp_dp_items.subitem_name where item_no = m.req_type and
	 * subitem_no = m.req_subtype
	 */
	private String reqSubtypeName;

	/** 申請者ClientId, from tsmp_dp_req_orderm.client_id */
	private String clientId;

	/** 申請者UserId(後台申請才有), from tsmp_dp_req_orderm.req_user_id */
	private String reqUserId;

	/** 申請者所屬組織單位(後台申請才有), from tsmp_dp_req_orderm.org_id */
	private String orgId;

	/** 申請者所屬組織單位的名稱 */
	private String orgName;

	/**
	 * 前、後台標籤, 用以區分前台或後台的申請單 有 reqUserId 則此欄位會放"B", 表示為後台申請單; 若有 clientId 則會放"F",
	 * 表示為前台申請單
	 */
	private String fbFlag;

	/**
	 * 申請者ID 有 reqUserId 則此欄位會放reqUserId; 否則放 clientId
	 */
	private String applierId;

	/** 申請者名稱 (有reqUserId -> userName就用, 否則用clientId -> clientName) */
	private String applierName;

	/** 申請說明, from tsmp_dp_req_orderm.req_desc */
	private String reqDesc;

	/** 生效日期, yyyy/MM/dd */
	private String effectiveDate;

	/** 申請(建立)日期 */
	private Date createDateTime;

	/** 申請單附件 */
	private List<TsmpDpFile> attachments;

	/**
	 * 當前申請單狀態代碼, from tsmp_dp_req_orders.review_status<br>
	 * 如果申請單尚未結束, 則會回傳即將要簽核的關卡;<br>
	 * 如果申請單已結束, 則會回傳結束時所停留的關卡。<br>
	 * ex: "使用者結案": 則 reviewStatus=END, layer=0, procFlag=0<br>
	 * ex: "主管待審": 則 reviewStatus=WAIT1, layer=2, procFlag=1
	 */
	private String currentReviewStatus;

	/** 當前申請單狀態名稱 */
	private String currentReviewStatusName;

	/** 當前關卡層數, from tsmp_dp_req_orders.layer */
	private Integer currentLayer;

	/**
	 * 當前關卡名稱 from tsmp_dp_items.subitem_name where item_no = 'CHK_LAYER' and
	 * subitem_no = s.layer
	 */
	private String currentLayerName;

	/** 當前處理狀態, from tsmp_dp_req_orders.proc_flag */
	private Integer currentProcFlag;

	/**
	 * 可否更新<br>
	 * <b>注意</b>: 此方法並未檢查操作者是否有權限操作此申請單
	 */
	private boolean isUpdatable = false;

	/**
	 * 可否送審<br>
	 * <b>注意</b>: 此方法並未檢查操作者是否有權限操作此申請單
	 */
	private boolean isSendable = false;

	/**
	 * 可否重送<br>
	 * <b>注意</b>: 此方法並未檢查操作者是否有權限操作此申請單
	 */
	private boolean isResendable = false;

	/**
	 * 可否結案<br>
	 * <b>注意</b>: 此方法並未檢查操作者是否有權限操作此申請單
	 */
	private boolean isEndable = false;

	/**
	 * 可否簽核<br>
	 * <b>注意</b>: 此方法並未檢查操作者是否有權限操作此申請單
	 */
	private boolean isSignable = false;

	/** 申請單明細 */
	private List<T> detailList;

	public DpReqQueryResp() {
	}

	@Override
	public String toString() {
		return "DpReqQueryResp [\n\treqOrdermId=" + reqOrdermId + ",\n\tlv=" + lv + ",\n\treqOrderNo=" + reqOrderNo
				+ ",\n\treqType=" + reqType + ",\n\treqTypeName=" + reqTypeName + ",\n\treqSubtype=" + reqSubtype
				+ ",\n\treqSubtypeName=" + reqSubtypeName + ",\n\tclientId=" + clientId + ",\n\treqUserId=" + reqUserId
				+ ",\n\torgId=" + orgId + ",\n\torgName=" + orgName + ",\n\tfbFlag=" + fbFlag + ",\n\tapplierId="
				+ applierId + ",\n\tapplierName=" + applierName + ",\n\treqDesc=" + reqDesc + ",\n\teffectiveDate="
				+ effectiveDate + ",\n\tcreateDateTime=" + createDateTime + ",\n\tcurrentReviewStatus="
				+ currentReviewStatus + ",\n\tcurrentReviewStatusName=" + currentReviewStatusName + ",\n\tcurrentLayer="
				+ currentLayer + ",\n\tcurrentLayerName=" + currentLayerName + ",\n\tcurrentProcFlag=" + currentProcFlag
				+ ",\n\tdetailList=" + detailList + "\n]";
	}

	public Long getReqOrdermId() {
		return reqOrdermId;
	}

	public void setReqOrdermId(Long reqOrdermId) {
		this.reqOrdermId = reqOrdermId;
	}

	public Long getLv() {
		return lv;
	}

	public void setLv(Long lv) {
		this.lv = lv;
	}

	public String getReqOrderNo() {
		return reqOrderNo;
	}

	public void setReqOrderNo(String reqOrderNo) {
		this.reqOrderNo = reqOrderNo;
	}

	public String getReqType() {
		return reqType;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
	}

	public String getReqTypeName() {
		return reqTypeName;
	}

	public void setReqTypeName(String reqTypeName) {
		this.reqTypeName = reqTypeName;
	}

	public String getReqSubtype() {
		return reqSubtype;
	}

	public void setReqSubtype(String reqSubtype) {
		this.reqSubtype = reqSubtype;
	}

	public String getReqSubtypeName() {
		return reqSubtypeName;
	}

	public void setReqSubtypeName(String reqSubtypeName) {
		this.reqSubtypeName = reqSubtypeName;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getReqUserId() {
		return reqUserId;
	}

	public void setReqUserId(String reqUserId) {
		this.reqUserId = reqUserId;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getFbFlag() {
		return fbFlag;
	}

	public void setFbFlag(String fbFlag) {
		this.fbFlag = fbFlag;
	}

	public String getApplierId() {
		return applierId;
	}

	public void setApplierId(String applierId) {
		this.applierId = applierId;
	}

	public String getApplierName() {
		return applierName;
	}

	public void setApplierName(String applierName) {
		this.applierName = applierName;
	}

	public String getReqDesc() {
		return reqDesc;
	}

	public void setReqDesc(String reqDesc) {
		this.reqDesc = reqDesc;
	}

	public String getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(String effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	public Date getCreateDateTime() {
		return createDateTime;
	}

	public void setCreateDateTime(Date createDateTime) {
		this.createDateTime = createDateTime;
	}

	public List<TsmpDpFile> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<TsmpDpFile> attachments) {
		this.attachments = attachments;
	}

	public String getCurrentReviewStatus() {
		return currentReviewStatus;
	}

	public void setCurrentReviewStatus(String currentReviewStatus) {
		this.currentReviewStatus = currentReviewStatus;
	}

	public String getCurrentReviewStatusName() {
		return currentReviewStatusName;
	}

	public void setCurrentReviewStatusName(String currentReviewStatusName) {
		this.currentReviewStatusName = currentReviewStatusName;
	}

	public Integer getCurrentLayer() {
		return currentLayer;
	}

	public void setCurrentLayer(Integer currentLayer) {
		this.currentLayer = currentLayer;
	}

	public String getCurrentLayerName() {
		return currentLayerName;
	}

	public void setCurrentLayerName(String currentLayerName) {
		this.currentLayerName = currentLayerName;
	}

	public Integer getCurrentProcFlag() {
		return currentProcFlag;
	}

	public void setCurrentProcFlag(Integer currentProcFlag) {
		this.currentProcFlag = currentProcFlag;
	}

	public boolean isUpdatable() {
		return isUpdatable;
	}

	public void setUpdatable(boolean isUpdatable) {
		this.isUpdatable = isUpdatable;
	}

	public boolean isSendable() {
		return isSendable;
	}

	public void setSendable(boolean isSendable) {
		this.isSendable = isSendable;
	}

	public boolean isResendable() {
		return isResendable;
	}

	public void setResendable(boolean isResendable) {
		this.isResendable = isResendable;
	}

	public boolean isEndable() {
		return isEndable;
	}

	public void setEndable(boolean isEndable) {
		this.isEndable = isEndable;
	}

	public boolean isSignable() {
		return isSignable;
	}

	public void setSignable(boolean isSignable) {
		this.isSignable = isSignable;
	}

	public List<T> getDetailList() {
		return detailList;
	}

	public void setDetailList(List<T> detailList) {
		this.detailList = detailList;
	}

}
