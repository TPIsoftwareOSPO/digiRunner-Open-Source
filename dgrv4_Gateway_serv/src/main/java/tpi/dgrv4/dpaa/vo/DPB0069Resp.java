package tpi.dgrv4.dpaa.vo;

import java.util.List;

public class DPB0069Resp {
	
	/** PK */
	private Long reqOrdermId;
	
	/** 案件編號 */
	private String reqOrderNo;
	
	/** 申請日期 */
	private String reqCreateDateTime;
	
	/** 簽核類別 */
	private String reqType;
	
	/** 簽核類別名稱 */
	private String reqTypeName;
	
	/** 申請類別 */
	private String reqSubtype;
	
	/** 申請類別名稱 */
	private String reqSubtypeName;
	
	/** List */
	private List<DPB0069Items> dataList;

	private String effectiveDate;

	public Long getReqOrdermId() {
		return reqOrdermId;
	}

	public void setReqOrdermId(Long reqOrdermId) {
		this.reqOrdermId = reqOrdermId;
	}

	public String getReqOrderNo() {
		return reqOrderNo;
	}

	public void setReqOrderNo(String reqOrderNo) {
		this.reqOrderNo = reqOrderNo;
	}

	public String getReqCreateDateTime() {
		return reqCreateDateTime;
	}

	public void setReqCreateDateTime(String reqCreateDateTime) {
		this.reqCreateDateTime = reqCreateDateTime;
	}

	public String getReqType() {
		return reqType;
	}

	public void setReqType(String reqType) {
		this.reqType = reqType;
	}
	
	public void setReqTypeName(String reqTypeName) {
		this.reqTypeName = reqTypeName;
	}
	
	public String getReqTypeName() {
		return reqTypeName;
	}

	public String getReqSubtype() {
		return reqSubtype;
	}

	public void setReqSubtype(String reqSubtype) {
		this.reqSubtype = reqSubtype;
	}

	public void setReqSubtypeName(String reqSubtypeName) {
		this.reqSubtypeName = reqSubtypeName;
	}
	
	public String getReqSubtypeName() {
		return reqSubtypeName;
	}

	public List<DPB0069Items> getDataList() {
		return dataList;
	}

	public void setDataList(List<DPB0069Items> dataList) {
		this.dataList = dataList;
	}

	public String getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(String effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

}
