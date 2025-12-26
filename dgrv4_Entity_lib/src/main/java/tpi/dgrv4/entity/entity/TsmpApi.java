package tpi.dgrv4.entity.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_api")
@IdClass(TsmpApiId.class)
public class TsmpApi implements Serializable {

	@Id
	@Column(name = "api_key")
	private String apiKey;

	@Id
	@Column(name = "module_name")
	private String moduleName;

	@Column(name = "api_name")
	private String apiName;

	/** 1: Enabled, 2:Disabled (新增預設為Disabled) */
	@Column(name = "api_status")
	private String apiStatus = "2";

	/** 'M': Module(Default); 'R': Registerd; 'C': Composed; 'N': .Net; */
	@Column(name = "api_src")
	private String apiSrc;

	@Column(name = "api_desc")
	private String apiDesc;

	@Column(name = "api_owner")
	private String apiOwner;

	@Column(name = "create_time")
	private Date createTime;

	@Column(name = "create_user")
	private String createUser;

	@Column(name = "update_time")
	private Date updateTime;

	@Column(name = "update_user")
	private String updateUser;

	@Column(name = "org_id")
	private String orgId;

	@Column(name = "public_flag")
	private String publicFlag;

	@Column(name = "src_url")
	private String srcUrl;

	@Column(name = "api_uid")
	private String apiUid;

	@Column(name = "data_format")
	private String dataFormat = "1";

	@Column(name = "jwe_flag")
	private String jewFlag;

	@Column(name = "jwe_flag_resp")
	private String jewFlagResp;

	@Column(name = "api_cache_flag")
	private String apiCacheFlag = "1";

	@Column(name = "mock_status_code")
	private String mockStatusCode;

	/* 有值就存 Json 格式 */
	@Column(name = "mock_headers")
	private String mockHeaders;

	@Column(name = "mock_body")
	private String mockBody;

	@Column(name = "success")
	private Long success = 0L;

	@Column(name = "fail")
	private Long fail = 0L;

	@Column(name = "total")
	private Long total = 0L;

	@Column(name = "elapse")
	private Long elapse = 0L;

	@Column(name = "api_release_time")
	private Date apiReleaseTime;

	@Column(name = "label1")
	private String label1;

	@Column(name = "label2")
	private String label2;

	@Column(name = "label3")
	private String label3;

	@Column(name = "label4")
	private String label4;

	@Column(name = "label5")
	private String label5;

	@Column(name = "fixed_cache_time")
	private Integer fixedCacheTime = 0;

	@Column(name = "scheduled_launch_date")
	private Long scheduledLaunchDate = 0L; // DP API預定上架時間用

	@Column(name = "scheduled_removal_date")
	private Long scheduledRemovalDate = 0L; // DP API預定下架時間用

	@Column(name = "enable_scheduled_date")
	private Long enableScheduledDate = 0L; // DGR API 預定啟用日期

	@Column(name = "disable_scheduled_date")
	private Long disableScheduledDate = 0L; // DGR API 預定停用日期

	/* constructors */

	public TsmpApi() {
	}

	public TsmpApi(String apiKey, String moduleName, String srcUrl) {
		this.apiKey = apiKey;
		this.moduleName = moduleName;
		this.srcUrl = srcUrl;
	}

	@Override
	public int hashCode() {
		return Objects.hash(apiCacheFlag, apiDesc, apiKey, apiName, apiOwner, apiReleaseTime, apiSrc, apiStatus, apiUid,
				createTime, createUser, dataFormat, elapse, fail, fixedCacheTime, jewFlag, jewFlagResp, label1, label2,
				label3, label4, label5, mockBody, mockHeaders, mockStatusCode, moduleName, orgId, publicFlag, srcUrl,
				success, total, updateTime, updateUser, scheduledLaunchDate, scheduledRemovalDate, enableScheduledDate,
				disableScheduledDate);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TsmpApi other = (TsmpApi) obj;
		return Objects.equals(apiCacheFlag, other.apiCacheFlag) && Objects.equals(apiDesc, other.apiDesc)
				&& Objects.equals(apiKey, other.apiKey) && Objects.equals(apiName, other.apiName)
				&& Objects.equals(apiOwner, other.apiOwner) && Objects.equals(apiReleaseTime, other.apiReleaseTime)
				&& Objects.equals(apiSrc, other.apiSrc) && Objects.equals(apiStatus, other.apiStatus)
				&& Objects.equals(apiUid, other.apiUid) && Objects.equals(createTime, other.createTime)
				&& Objects.equals(createUser, other.createUser) && Objects.equals(dataFormat, other.dataFormat)
				&& Objects.equals(elapse, other.elapse) && Objects.equals(fail, other.fail)
				&& Objects.equals(fixedCacheTime, other.fixedCacheTime) && Objects.equals(jewFlag, other.jewFlag)
				&& Objects.equals(jewFlagResp, other.jewFlagResp) && Objects.equals(label1, other.label1)
				&& Objects.equals(label2, other.label2) && Objects.equals(label3, other.label3)
				&& Objects.equals(label4, other.label4) && Objects.equals(label5, other.label5)
				&& Objects.equals(mockBody, other.mockBody) && Objects.equals(mockHeaders, other.mockHeaders)
				&& Objects.equals(mockStatusCode, other.mockStatusCode) && Objects.equals(moduleName, other.moduleName)
				&& Objects.equals(orgId, other.orgId) && Objects.equals(publicFlag, other.publicFlag)
				&& Objects.equals(srcUrl, other.srcUrl) && Objects.equals(success, other.success)
				&& Objects.equals(total, other.total) && Objects.equals(updateTime, other.updateTime)
				&& Objects.equals(updateUser, other.updateUser)
				&& Objects.equals(scheduledLaunchDate, other.scheduledLaunchDate)
				&& Objects.equals(scheduledRemovalDate, other.scheduledRemovalDate)
				&& Objects.equals(enableScheduledDate, other.enableScheduledDate)
				&& Objects.equals(disableScheduledDate, other.disableScheduledDate);
	}

	@Override
	public String toString() {
		return "TsmpApi [apiKey=" + apiKey + ", moduleName=" + moduleName + ", apiName=" + apiName + ", apiStatus="
				+ apiStatus + ", apiSrc=" + apiSrc + ", apiDesc=" + apiDesc + ", apiOwner=" + apiOwner + ", createTime="
				+ createTime + ", createUser=" + createUser + ", updateTime=" + updateTime + ", updateUser="
				+ updateUser + ", orgId=" + orgId + ", publicFlag=" + publicFlag + ", srcUrl=" + srcUrl + ", apiUid="
				+ apiUid + ", dataFormat=" + dataFormat + ", jewFlag=" + jewFlag + ", jewFlagResp=" + jewFlagResp
				+ ", apiCacheFlag=" + apiCacheFlag + ", mockStatusCode=" + mockStatusCode + ", mockHeaders="
				+ mockHeaders + ", mockBody=" + mockBody + ", success=" + success + ", fail=" + fail + ", total="
				+ total + ", elapse=" + elapse + ", apiReleaseTime=" + apiReleaseTime + ", label1=" + label1
				+ ", label2=" + label2 + ", label3=" + label3 + ", label4=" + label4 + ", label5=" + label5
				+ ", fixedCacheTime=" + fixedCacheTime + ", scheduledLaunchDate=" + scheduledLaunchDate
				+ ", scheduledRemovalDate=" + scheduledRemovalDate + "]" + ", enableScheduledDate="
				+ enableScheduledDate + "]" + ", disableScheduledDate=" + disableScheduledDate + "]";
	}

	/* getters and setters */
	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public String getApiStatus() {
		return apiStatus;
	}

	public void setApiStatus(String apiStatus) {
		this.apiStatus = apiStatus;
	}

	public String getApiSrc() {
		return apiSrc;
	}

	public void setApiSrc(String apiSrc) {
		this.apiSrc = apiSrc;
	}

	public String getApiDesc() {
		return apiDesc;
	}

	public void setApiDesc(String apiDesc) {
		this.apiDesc = apiDesc;
	}

	public String getApiOwner() {
		return apiOwner;
	}

	public void setApiOwner(String apiOwner) {
		this.apiOwner = apiOwner;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getPublicFlag() {
		return publicFlag;
	}

	public void setPublicFlag(String publicFlag) {
		this.publicFlag = publicFlag;
	}

	public String getSrcUrl() {
		return srcUrl;
	}

	public void setSrcUrl(String srcUrl) {
		this.srcUrl = srcUrl;
	}

	public String getApiUid() {
		return apiUid;
	}

	public void setApiUid(String apiUid) {
		this.apiUid = apiUid;
	}

	public String getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
	}

	public String getJewFlag() {
		return jewFlag;
	}

	public void setJewFlag(String jewFlag) {
		this.jewFlag = jewFlag;
	}

	public String getJewFlagResp() {
		return jewFlagResp;
	}

	public void setJewFlagResp(String jewFlagResp) {
		this.jewFlagResp = jewFlagResp;
	}

	public String getApiCacheFlag() {
		return apiCacheFlag;
	}

	public String getMockStatusCode() {
		return mockStatusCode;
	}

	public void setMockStatusCode(String mockStatusCode) {
		this.mockStatusCode = mockStatusCode;
	}

	public String getMockHeaders() {
		return mockHeaders;
	}

	public void setMockHeaders(String mockHeaders) {
		this.mockHeaders = mockHeaders;
	}

	public String getMockBody() {
		return mockBody;
	}

	public void setMockBody(String mockBody) {
		this.mockBody = mockBody;
	}

	/**
	 * 1:Auto , 2:None , 3:Fixed
	 */
	public void setApiCacheFlag(String apiCacheFlag) {
		this.apiCacheFlag = apiCacheFlag;
	}

	public Long getSuccess() {
		return success;
	}

	public void setSuccess(Long success) {
		this.success = success;
	}

	public Long getFail() {
		return fail;
	}

	public void setFail(Long fail) {
		this.fail = fail;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public Long getElapse() {
		return elapse;
	}

	public void setElapse(Long elapse) {
		this.elapse = elapse;
	}

	public Date getApiReleaseTime() {
		return apiReleaseTime;
	}

	public void setApiReleaseTime(Date apiReleaseTime) {
		this.apiReleaseTime = apiReleaseTime;
	}

	public String getLabel1() {
		return label1;
	}

	public String getLabel2() {
		return label2;
	}

	public String getLabel3() {
		return label3;
	}

	public String getLabel4() {
		return label4;
	}

	public String getLabel5() {
		return label5;
	}

	public void setLabel1(String label1) {
		this.label1 = label1;
	}

	public void setLabel2(String label2) {
		this.label2 = label2;
	}

	public void setLabel3(String label3) {
		this.label3 = label3;
	}

	public void setLabel4(String label4) {
		this.label4 = label4;
	}

	public void setLabel5(String label5) {
		this.label5 = label5;
	}

	public Integer getFixedCacheTime() {
		return fixedCacheTime;
	}

	public void setFixedCacheTime(Integer fixedCacheTime) {
		this.fixedCacheTime = fixedCacheTime;
	}

	public Long getScheduledLaunchDate() {
		// 避免 DB 裡面沒有值的措施
		if (scheduledLaunchDate == null) {
			return 0L;
		}

		return scheduledLaunchDate;
	}

	public void setScheduledLaunchDate(Long scheduledLaunchDate) {
		this.scheduledLaunchDate = scheduledLaunchDate;
	}

	public Long getScheduledRemovalDate() {
		// 避免 DB 裡面沒有值的措施
		if (scheduledRemovalDate == null) {
			return 0L;
		}
		return scheduledRemovalDate;
	}

	public void setScheduledRemovalDate(Long scheduledRemovalDate) {
		this.scheduledRemovalDate = scheduledRemovalDate;
	}

	public Long getEnableScheduledDate() {
		return enableScheduledDate == null ? 0L : enableScheduledDate;
	}

	public void setEnableScheduledDate(Long enableScheduledDate) {
		this.enableScheduledDate = enableScheduledDate;
	}

	public Long getDisableScheduledDate() {
		return disableScheduledDate == null ? 0L : disableScheduledDate;
	}

	public void setDisableScheduledDate(Long disableScheduledDate) {
		this.disableScheduledDate = disableScheduledDate;
	}

}
