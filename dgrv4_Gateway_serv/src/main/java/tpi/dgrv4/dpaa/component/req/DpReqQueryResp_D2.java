package tpi.dgrv4.dpaa.component.req;

import java.util.List;

public class DpReqQueryResp_D2 {

	/** from tsmp_dp_req_orderd2.req_orderd2_id */
	private Long reqOrderd2Id;

	/** from tsmp_dp_req_orderd2.api_uid */
	private String apiUid;

	private String apiName;

	private String publicFlag;

	private String publicFlagName;

	private List<DpReqQueryResp_D2d> d2dRespList;

	/** ex: 11/D2_ATTACHMENT/test.txt */
	private String filePath;

	/** ex: test.txt */
	private String fileName;

	private String moduleName;

	private String apiDesc;

	private String orgName;

	/** API KEY */
	private String apiKey;

	/** 組織ID */
	private String orgId;

	/** API延伸檔ID */
	private Long apiExtId;

	/** 上下架狀態代碼 */
	private String dpStatus;

	public DpReqQueryResp_D2() {
	}

	@Override
	public String toString() {
		return "DpReqQueryResp_D2 [reqOrderd2Id=" + reqOrderd2Id + ", apiUid=" + apiUid + ", apiName=" + apiName
				+ ", publicFlag=" + publicFlag + ", publicFlagName=" + publicFlagName + ", d2dRespList=" + d2dRespList
				+ ", filePath=" + filePath + ", fileName=" + fileName + ", moduleName=" + moduleName + ", apiDesc="
				+ apiDesc + ", orgName=" + orgName + "]";
	}

	public Long getReqOrderd2Id() {
		return reqOrderd2Id;
	}

	public void setReqOrderd2Id(Long reqOrderd2Id) {
		this.reqOrderd2Id = reqOrderd2Id;
	}

	public String getApiUid() {
		return apiUid;
	}

	public void setApiUid(String apiUid) {
		this.apiUid = apiUid;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public String getPublicFlag() {
		return publicFlag;
	}

	public void setPublicFlag(String publicFlag) {
		this.publicFlag = publicFlag;
	}

	public String getPublicFlagName() {
		return publicFlagName;
	}

	public void setPublicFlagName(String publicFlagName) {
		this.publicFlagName = publicFlagName;
	}

	public List<DpReqQueryResp_D2d> getD2dRespList() {
		return d2dRespList;
	}

	public void setD2dRespList(List<DpReqQueryResp_D2d> d2dRespList) {
		this.d2dRespList = d2dRespList;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public String getModuleName() {
		return moduleName;
	}


	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}


	public String getApiDesc() {
		return apiDesc;
	}


	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}


	public String getOrgName() {
		return orgName;
	}


	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public Long getApiExtId() {
		return apiExtId;
	}

	public void setApiExtId(Long apiExtId) {
		this.apiExtId = apiExtId;
	}

	public String getDpStatus() {
		return dpStatus;
	}

	public void setDpStatus(String dpStatus) {
		this.dpStatus = dpStatus;
	}

}
