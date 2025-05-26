package tpi.dgrv4.dpaa.component.req;

import java.util.List;
import java.util.Map;

import tpi.dgrv4.entity.entity.TsmpDpFile;

public class DpReqQueryResp_D1d {

	private Long reqOrderd1Id;

	private String apiUid;

	private String apiName;

	private String moduleName;

	private String orgName;

	private String apiDesc;

	/** themeId : themeName */
	private Map<Long, String> themes;

	/** API說明文件 */
	private List<TsmpDpFile> apiAttachments;

	/** API KEY */
	private String apiKey;

	/** 組織ID */
	private String orgId;

	/** API延伸檔ID */
	private Long apiExtId;

	/** 上下架狀態代碼 */
	private String dpStatus;

	/** 開放狀態代碼	from tsmp_api.public_flag */
	private String publicFlag;

	/** 開放狀態名稱 */
	private String publicFlagName;

	public DpReqQueryResp_D1d() {
	}

	public Long getReqOrderd1Id() {
		return reqOrderd1Id;
	}

	public void setReqOrderd1Id(Long reqOrderd1Id) {
		this.reqOrderd1Id = reqOrderd1Id;
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

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getApiDesc() {
		return apiDesc;
	}

	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}

	public Map<Long, String> getThemes() {
		return themes;
	}

	public void setThemes(Map<Long, String> themes) {
		this.themes = themes;
	}

	public List<TsmpDpFile> getApiAttachments() {
		return apiAttachments;
	}

	public void setApiAttachments(List<TsmpDpFile> apiAttachments) {
		this.apiAttachments = apiAttachments;
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

}
