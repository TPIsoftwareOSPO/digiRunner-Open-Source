package tpi.dgrv4.entity.entity.jpql;

import java.util.Date;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_api_imp")
@IdClass(TsmpApiImpId.class)
public class TsmpApiImp {
	
	@Id
	@Column(name = "api_key")
	private String apiKey;

	@Id
	@Column(name = "module_name")
	private String moduleName;

	@Id
	@Column(name = "record_type")
	private String recordType;

	// 從 1000 開始編號
	@Id
	@Column(name = "batch_no")
	private Integer batchNo;

	@Column(name = "filename")
	private String filename;

	@Column(name = "api_name")
	private String apiName;

	@Column(name = "api_desc")
	private String apiDesc;

	@Column(name = "api_owner")
	private String apiOwner;

	@Column(name = "url_rid")
	private String urlRid = "0";

	@Column(name = "api_src")
	private String apiSrc = "M";

	@Column(name = "src_url")
	private String srcUrl;

	@Column(name = "api_uuid")
	private String apiUuid;

	@Column(name = "path_of_json")
	private String pathOfJson;

	@Column(name = "method_of_json")
	private String methodOfJson;

	@Column(name = "params_of_json")
	private String paramsOfJson;

	@Column(name = "headers_of_json")
	private String headersOfJson;

	@Column(name = "consumes_of_json")
	private String consumesOfJson;

	@Column(name = "produces_of_json")
	private String producesOfJson;

	@Column(name = "flow")
	private String flow;

	@Column(name = "create_time")
	private Date createTime;

	@Column(name = "create_user")
	private String createUser;

	@Column(name = "check_act")
	private String checkAct;

	@Column(name = "result")
	private String result;

	@Column(name = "memo")
	private String memo;

	@Column(name = "no_oauth")
	private String noOauth;

	@Column(name = "jwe_flag")
	private String jweFlag;

	@Column(name = "jwe_flag_resp")
	private String jweFlagResp;

	@Column(name = "fun_flag")
	private Integer funFlag;
	
	@Column(name = "mock_status_code")
	private String mockStatusCode;

	/* 有值就存 Json 格式 */
	@Column(name = "mock_headers")
	private String mockHeaders;

	@Column(name = "mock_body")
	private String mockBody;

	@Column(name = "REDIRECT_BY_IP")
	private String redirectByIp = "N";

	@Column(name = "IP_FOR_REDIRECT1")
	private String ipForRedirect1;

	@Column(name = "IP_SRC_URL1")
	private String ipSrcUrl1;

	@Column(name = "IP_FOR_REDIRECT2")
	private String ipForRedirect2;

	@Column(name = "IP_SRC_URL2")
	private String ipSrcUrl2;

	@Column(name = "IP_FOR_REDIRECT3")
	private String ipForRedirect3;

	@Column(name = "IP_SRC_URL3")
	private String ipSrcUrl3;

	@Column(name = "IP_FOR_REDIRECT4")
	private String ipForRedirect4;

	@Column(name = "IP_SRC_URL4")
	private String ipSrcUrl4;

	@Column(name = "IP_FOR_REDIRECT5")
	private String ipForRedirect5;

	@Column(name = "IP_SRC_URL5")
	private String ipSrcUrl5;

	@Column(name = "BODY_MASK_KEYWORD")
	private String bodyMaskKeyword;

	@Column(name = "BODY_MASK_POLICY")
	private String bodyMaskPolicy = "0";

	@Column(name = "BODY_MASK_POLICY_NUM")
	private Integer bodyMaskPolicyNum;

	@Column(name = "BODY_MASK_POLICY_SYMBOL")
	private String bodyMaskPolicySymbol;

	@Column(name = "HEADER_MASK_KEY")
	private String headerMaskKey;

	@Column(name = "HEADER_MASK_POLICY")
	private String headerMaskPolicy = "0";

	@Column(name = "HEADER_MASK_POLICY_NUM")
	private Integer headerMaskPolicyNum;

	@Column(name = "HEADER_MASK_POLICY_SYMBOL")
	private String headerMaskPolicySymbol;
	
	@Column(name = "LABEL1")
	private String label1;
	
	@Column(name = "LABEL2")
	private String label2;
	
	@Column(name = "LABEL3")
	private String label3;
	
	@Column(name = "LABEL4")
	private String label4;
	
	@Column(name = "LABEL5")
	private String label5;
	
	@Column(name = "api_cache_flag")
	private String apiCacheFlag = "1";
	
	@Column(name = "fixed_cache_time")
	private Integer fixedCacheTime = 0;
	
	@Column(name = "FAIL_DISCOVERY_POLICY")
	private String failDiscoveryPolicy = "0";
	
	@Column(name = "FAIL_HANDLE_POLICY")
	private String failHandlePolicy = "0";
	
	@Column(name = "api_status")
	private String apiStatus = "2";
	
	@Column(name = "public_flag")
	private String publicFlag;
	
	@Column(name = "api_release_time")
	private Date apiReleaseTime;
	
	@Column(name = "scheduled_launch_date")
	private Long scheduledLaunchDate = 0L; // DP API預定上架時間用

	@Column(name = "scheduled_removal_date")
	private Long scheduledRemovalDate = 0L; // DP API預定下架時間用

	@Column(name = "enable_scheduled_date")
	private Long enableScheduledDate = 0L; // DGR API 預定啟用日期

	@Column(name = "disable_scheduled_date")
	private Long disableScheduledDate = 0L; // DGR API 預定停用日期

	@Column(name = "notify_Name_List")
	private String notifyNameList;
	
	/**
	 * [ZH] 是否加入 CORS header "Access-Control-Allow-Origin"
	 * [EN] Whether to add CORS header "Access-Control-Allow-Origin"
	 */
	@Column(name = "IS_CORS_ALLOW_ORIGIN")
	private String isCorsAllowOrigin = "N";

	/**
	 * [ZH] 是否加入 CORS header "Access-Control-Allow-Methods"
	 * [EN] Whether to add CORS header "Access-Control-Allow-Methods"
	 */
	@Column(name = "IS_CORS_ALLOW_METHODS")
	private String isCorsAllowMethods = "N";

	/**
	 * [ZH] 是否加入 CORS header "Access-Control-Allow-Headers"
	 * [EN] Whether to add CORS header "Access-Control-Allow-Headers"
	 */
	@Column(name = "IS_CORS_ALLOW_HEADERS")
	private String isCorsAllowHeaders = "N";

	/**
	 * [ZH] CORS Header 的 "Access-Control-Allow-Origin" 要加入的內容
	 * [EN] Contents to be added to the "Access-Control-Allow-Origin" of the CORS Header
	 */
	@Column(name = "CORS_ALLOW_ORIGIN")
	private String corsAllowOrigin;
	
	/**
	 * [ZH] CORS Header 的 "Access-Control-Allow-Methods" 要加入的內容
	 * [EN] Contents to be added to the "Access-Control-Allow-Methods" of the CORS Header
	 */
	@Column(name = "CORS_ALLOW_METHODS")
	private String corsAllowMethods;

	/**
	 * [ZH] CORS Header 的 "Access-Control-Allow-Headers" 要加入的內容
	 * [EN] Contents to be added to the "Access-Control-Allow-Headers" of the CORS Header
	 */
	@Column(name = "CORS_ALLOW_HEADERS")
	private String corsAllowHeaders;
	
	/* constructors */
	public TsmpApiImp() {}

	@Override
	public String toString() {
		return "TsmpApiImp [apiKey=" + apiKey + ", moduleName=" + moduleName + ", recordType=" + recordType
				+ ", batchNo=" + batchNo + ", filename=" + filename + ", apiName=" + apiName + ", apiDesc=" + apiDesc
				+ ", apiOwner=" + apiOwner + ", urlRid=" + urlRid + ", apiSrc=" + apiSrc + ", srcUrl=" + srcUrl
				+ ", apiUuid=" + apiUuid + ", pathOfJson=" + pathOfJson + ", methodOfJson=" + methodOfJson
				+ ", paramsOfJson=" + paramsOfJson + ", headersOfJson=" + headersOfJson + ", consumesOfJson="
				+ consumesOfJson + ", producesOfJson=" + producesOfJson + ", flow=" + flow + ", createTime="
				+ createTime + ", createUser=" + createUser + ", checkAct=" + checkAct + ", result=" + result
				+ ", memo=" + memo + ", noOauth=" + noOauth + ", jweFlag=" + jweFlag + ", jweFlagResp=" + jweFlagResp
				+ ", funFlag=" + funFlag + ", mockStatusCode=" + mockStatusCode + ", mockHeaders=" + mockHeaders
				+ ", mockBody=" + mockBody + ", redirectByIp=" + redirectByIp + ", ipForRedirect1=" + ipForRedirect1
				+ ", ipSrcUrl1=" + ipSrcUrl1 + ", ipForRedirect2=" + ipForRedirect2 + ", ipSrcUrl2=" + ipSrcUrl2
				+ ", ipForRedirect3=" + ipForRedirect3 + ", ipSrcUrl3=" + ipSrcUrl3 + ", ipForRedirect4="
				+ ipForRedirect4 + ", ipSrcUrl4=" + ipSrcUrl4 + ", ipForRedirect5=" + ipForRedirect5 + ", ipSrcUrl5="
				+ ipSrcUrl5 + ", bodyMaskKeyword=" + bodyMaskKeyword + ", bodyMaskPolicy=" + bodyMaskPolicy
				+ ", bodyMaskPolicyNum=" + bodyMaskPolicyNum + ", bodyMaskPolicySymbol=" + bodyMaskPolicySymbol
				+ ", headerMaskKey=" + headerMaskKey + ", headerMaskPolicy=" + headerMaskPolicy
				+ ", headerMaskPolicyNum=" + headerMaskPolicyNum + ", headerMaskPolicySymbol=" + headerMaskPolicySymbol
				+ ", label1=" + label1 + ", label2=" + label2 + ", label3=" + label3 + ", label4=" + label4
				+ ", label5=" + label5 + ", apiCacheFlag=" + apiCacheFlag + ", fixedCacheTime=" + fixedCacheTime
				+ ", failDiscoveryPolicy=" + failDiscoveryPolicy + ", failHandlePolicy=" + failHandlePolicy
				+ ", apiStatus=" + apiStatus + ", publicFlag=" + publicFlag + ", apiReleaseTime=" + apiReleaseTime
				+ ", scheduledLaunchDate=" + scheduledLaunchDate + ", scheduledRemovalDate=" + scheduledRemovalDate
				+ ", enableScheduledDate=" + enableScheduledDate + ", disableScheduledDate=" + disableScheduledDate
				+ ", isCorsAllowOrigin=" + isCorsAllowOrigin + ", isCorsAllowMethods=" + isCorsAllowMethods
				+ ", isCorsAllowHeaders=" + isCorsAllowHeaders + ", corsAllowOrigin=" + corsAllowOrigin
				+ ", corsAllowMethods=" + corsAllowMethods + ", corsAllowHeaders=" + corsAllowHeaders + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(apiCacheFlag, apiDesc, apiKey, apiName, apiOwner, apiReleaseTime, apiSrc, apiStatus,
				apiUuid, batchNo, bodyMaskKeyword, bodyMaskPolicy, bodyMaskPolicyNum, bodyMaskPolicySymbol, checkAct,
				consumesOfJson, corsAllowHeaders, corsAllowMethods, corsAllowOrigin, createTime, createUser,
				disableScheduledDate, enableScheduledDate, failDiscoveryPolicy, failHandlePolicy, filename,
				fixedCacheTime, flow, funFlag, headerMaskKey, headerMaskPolicy, headerMaskPolicyNum,
				headerMaskPolicySymbol, headersOfJson, ipForRedirect1, ipForRedirect2, ipForRedirect3, ipForRedirect4,
				ipForRedirect5, ipSrcUrl1, ipSrcUrl2, ipSrcUrl3, ipSrcUrl4, ipSrcUrl5, isCorsAllowHeaders,
				isCorsAllowMethods, isCorsAllowOrigin, jweFlag, jweFlagResp, label1, label2, label3, label4, label5,
				memo, methodOfJson, mockBody, mockHeaders, mockStatusCode, moduleName, noOauth, paramsOfJson,
				pathOfJson, producesOfJson, publicFlag, recordType, redirectByIp, result, scheduledLaunchDate,
				scheduledRemovalDate, srcUrl, urlRid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TsmpApiImp other = (TsmpApiImp) obj;
		return Objects.equals(apiCacheFlag, other.apiCacheFlag) && Objects.equals(apiDesc, other.apiDesc)
				&& Objects.equals(apiKey, other.apiKey) && Objects.equals(apiName, other.apiName)
				&& Objects.equals(apiOwner, other.apiOwner) && Objects.equals(apiReleaseTime, other.apiReleaseTime)
				&& Objects.equals(apiSrc, other.apiSrc) && Objects.equals(apiStatus, other.apiStatus)
				&& Objects.equals(apiUuid, other.apiUuid) && Objects.equals(batchNo, other.batchNo)
				&& Objects.equals(bodyMaskKeyword, other.bodyMaskKeyword)
				&& Objects.equals(bodyMaskPolicy, other.bodyMaskPolicy)
				&& Objects.equals(bodyMaskPolicyNum, other.bodyMaskPolicyNum)
				&& Objects.equals(bodyMaskPolicySymbol, other.bodyMaskPolicySymbol)
				&& Objects.equals(checkAct, other.checkAct) && Objects.equals(consumesOfJson, other.consumesOfJson)
				&& Objects.equals(corsAllowHeaders, other.corsAllowHeaders)
				&& Objects.equals(corsAllowMethods, other.corsAllowMethods)
				&& Objects.equals(corsAllowOrigin, other.corsAllowOrigin)
				&& Objects.equals(createTime, other.createTime) && Objects.equals(createUser, other.createUser)
				&& Objects.equals(disableScheduledDate, other.disableScheduledDate)
				&& Objects.equals(enableScheduledDate, other.enableScheduledDate)
				&& Objects.equals(failDiscoveryPolicy, other.failDiscoveryPolicy)
				&& Objects.equals(failHandlePolicy, other.failHandlePolicy) && Objects.equals(filename, other.filename)
				&& Objects.equals(fixedCacheTime, other.fixedCacheTime) && Objects.equals(flow, other.flow)
				&& Objects.equals(funFlag, other.funFlag) && Objects.equals(headerMaskKey, other.headerMaskKey)
				&& Objects.equals(headerMaskPolicy, other.headerMaskPolicy)
				&& Objects.equals(headerMaskPolicyNum, other.headerMaskPolicyNum)
				&& Objects.equals(headerMaskPolicySymbol, other.headerMaskPolicySymbol)
				&& Objects.equals(headersOfJson, other.headersOfJson)
				&& Objects.equals(ipForRedirect1, other.ipForRedirect1)
				&& Objects.equals(ipForRedirect2, other.ipForRedirect2)
				&& Objects.equals(ipForRedirect3, other.ipForRedirect3)
				&& Objects.equals(ipForRedirect4, other.ipForRedirect4)
				&& Objects.equals(ipForRedirect5, other.ipForRedirect5) && Objects.equals(ipSrcUrl1, other.ipSrcUrl1)
				&& Objects.equals(ipSrcUrl2, other.ipSrcUrl2) && Objects.equals(ipSrcUrl3, other.ipSrcUrl3)
				&& Objects.equals(ipSrcUrl4, other.ipSrcUrl4) && Objects.equals(ipSrcUrl5, other.ipSrcUrl5)
				&& Objects.equals(isCorsAllowHeaders, other.isCorsAllowHeaders)
				&& Objects.equals(isCorsAllowMethods, other.isCorsAllowMethods)
				&& Objects.equals(isCorsAllowOrigin, other.isCorsAllowOrigin) && Objects.equals(jweFlag, other.jweFlag)
				&& Objects.equals(jweFlagResp, other.jweFlagResp) && Objects.equals(label1, other.label1)
				&& Objects.equals(label2, other.label2) && Objects.equals(label3, other.label3)
				&& Objects.equals(label4, other.label4) && Objects.equals(label5, other.label5)
				&& Objects.equals(memo, other.memo) && Objects.equals(methodOfJson, other.methodOfJson)
				&& Objects.equals(mockBody, other.mockBody) && Objects.equals(mockHeaders, other.mockHeaders)
				&& Objects.equals(mockStatusCode, other.mockStatusCode) && Objects.equals(moduleName, other.moduleName)
				&& Objects.equals(noOauth, other.noOauth) && Objects.equals(paramsOfJson, other.paramsOfJson)
				&& Objects.equals(pathOfJson, other.pathOfJson) && Objects.equals(producesOfJson, other.producesOfJson)
				&& Objects.equals(publicFlag, other.publicFlag) && Objects.equals(recordType, other.recordType)
				&& Objects.equals(redirectByIp, other.redirectByIp) && Objects.equals(result, other.result)
				&& Objects.equals(scheduledLaunchDate, other.scheduledLaunchDate)
				&& Objects.equals(scheduledRemovalDate, other.scheduledRemovalDate)
				&& Objects.equals(srcUrl, other.srcUrl) && Objects.equals(urlRid, other.urlRid);
	}

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

	public String getRecordType() {
		return recordType;
	}

	public void setRecordType(String recordType) {
		this.recordType = recordType;
	}

	public Integer getBatchNo() {
		return batchNo;
	}

	public void setBatchNo(Integer batchNo) {
		this.batchNo = batchNo;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
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

	public String getUrlRid() {
		return urlRid;
	}

	public void setUrlRid(String urlRid) {
		this.urlRid = urlRid;
	}

	public String getApiSrc() {
		return apiSrc;
	}

	public void setApiSrc(String apiSrc) {
		this.apiSrc = apiSrc;
	}

	public String getSrcUrl() {
		return srcUrl;
	}

	public void setSrcUrl(String srcUrl) {
		this.srcUrl = srcUrl;
	}

	public String getApiUuid() {
		return apiUuid;
	}

	public void setApiUuid(String apiUuid) {
		this.apiUuid = apiUuid;
	}

	public String getPathOfJson() {
		return pathOfJson;
	}

	public void setPathOfJson(String pathOfJson) {
		this.pathOfJson = pathOfJson;
	}

	public String getMethodOfJson() {
		return methodOfJson;
	}

	public void setMethodOfJson(String methodOfJson) {
		this.methodOfJson = methodOfJson;
	}

	public String getParamsOfJson() {
		return paramsOfJson;
	}

	public void setParamsOfJson(String paramsOfJson) {
		this.paramsOfJson = paramsOfJson;
	}

	public String getHeadersOfJson() {
		return headersOfJson;
	}

	public void setHeadersOfJson(String headersOfJson) {
		this.headersOfJson = headersOfJson;
	}

	public String getConsumesOfJson() {
		return consumesOfJson;
	}

	public void setConsumesOfJson(String consumesOfJson) {
		this.consumesOfJson = consumesOfJson;
	}

	public String getProducesOfJson() {
		return producesOfJson;
	}

	public void setProducesOfJson(String producesOfJson) {
		this.producesOfJson = producesOfJson;
	}

	public String getFlow() {
		return flow;
	}

	public void setFlow(String flow) {
		this.flow = flow;
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

	public String getCheckAct() {
		return checkAct;
	}

	public void setCheckAct(String checkAct) {
		this.checkAct = checkAct;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getNoOauth() {
		return noOauth;
	}

	public void setNoOauth(String noOauth) {
		this.noOauth = noOauth;
	}

	public String getJweFlag() {
		return jweFlag;
	}

	public void setJweFlag(String jweFlag) {
		this.jweFlag = jweFlag;
	}

	public String getJweFlagResp() {
		return jweFlagResp;
	}

	public void setJweFlagResp(String jweFlagResp) {
		this.jweFlagResp = jweFlagResp;
	}

	public Integer getFunFlag() {
		return funFlag;
	}

	public void setFunFlag(Integer funFlag) {
		this.funFlag = funFlag;
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

	public String getRedirectByIp() {
		return redirectByIp;
	}

	public String getIpForRedirect1() {
		return ipForRedirect1;
	}

	public String getIpSrcUrl1() {
		return ipSrcUrl1;
	}

	public String getIpForRedirect2() {
		return ipForRedirect2;
	}

	public String getIpSrcUrl2() {
		return ipSrcUrl2;
	}

	public String getIpForRedirect3() {
		return ipForRedirect3;
	}

	public String getIpSrcUrl3() {
		return ipSrcUrl3;
	}

	public String getIpForRedirect4() {
		return ipForRedirect4;
	}

	public String getIpSrcUrl4() {
		return ipSrcUrl4;
	}

	public String getIpForRedirect5() {
		return ipForRedirect5;
	}

	public String getIpSrcUrl5() {
		return ipSrcUrl5;
	}

	public String getBodyMaskKeyword() {
		return bodyMaskKeyword;
	}

	public String getBodyMaskPolicy() {
		return bodyMaskPolicy;
	}

	public Integer getBodyMaskPolicyNum() {
		return bodyMaskPolicyNum;
	}

	public String getBodyMaskPolicySymbol() {
		return bodyMaskPolicySymbol;
	}

	public String getHeaderMaskKey() {
		return headerMaskKey;
	}

	public String getHeaderMaskPolicy() {
		return headerMaskPolicy;
	}

	public Integer getHeaderMaskPolicyNum() {
		return headerMaskPolicyNum;
	}

	public String getHeaderMaskPolicySymbol() {
		return headerMaskPolicySymbol;
	}

	public void setRedirectByIp(String redirectByIp) {
		this.redirectByIp = redirectByIp;
	}

	public void setIpForRedirect1(String ipForRedirect1) {
		this.ipForRedirect1 = ipForRedirect1;
	}

	public void setIpSrcUrl1(String ipSrcUrl1) {
		this.ipSrcUrl1 = ipSrcUrl1;
	}

	public void setIpForRedirect2(String ipForRedirect2) {
		this.ipForRedirect2 = ipForRedirect2;
	}

	public void setIpSrcUrl2(String ipSrcUrl2) {
		this.ipSrcUrl2 = ipSrcUrl2;
	}

	public void setIpForRedirect3(String ipForRedirect3) {
		this.ipForRedirect3 = ipForRedirect3;
	}

	public void setIpSrcUrl3(String ipSrcUrl3) {
		this.ipSrcUrl3 = ipSrcUrl3;
	}

	public void setIpForRedirect4(String ipForRedirect4) {
		this.ipForRedirect4 = ipForRedirect4;
	}

	public void setIpSrcUrl4(String ipSrcUrl4) {
		this.ipSrcUrl4 = ipSrcUrl4;
	}

	public void setIpForRedirect5(String ipForRedirect5) {
		this.ipForRedirect5 = ipForRedirect5;
	}

	public void setIpSrcUrl5(String ipSrcUrl5) {
		this.ipSrcUrl5 = ipSrcUrl5;
	}

	public void setBodyMaskKeyword(String bodyMaskKeyword) {
		this.bodyMaskKeyword = bodyMaskKeyword;
	}

	public void setBodyMaskPolicy(String bodyMaskPolicy) {
		this.bodyMaskPolicy = bodyMaskPolicy;
	}

	public void setBodyMaskPolicyNum(Integer bodyMaskPolicyNum) {
		this.bodyMaskPolicyNum = bodyMaskPolicyNum;
	}

	public void setBodyMaskPolicySymbol(String bodyMaskPolicySymbol) {
		this.bodyMaskPolicySymbol = bodyMaskPolicySymbol;
	}

	public void setHeaderMaskKey(String headerMaskKey) {
		this.headerMaskKey = headerMaskKey;
	}

	public void setHeaderMaskPolicy(String headerMaskPolicy) {
		this.headerMaskPolicy = headerMaskPolicy;
	}

	public void setHeaderMaskPolicyNum(Integer headerMaskPolicyNum) {
		this.headerMaskPolicyNum = headerMaskPolicyNum;
	}

	public void setHeaderMaskPolicySymbol(String headerMaskPolicySymbol) {
		this.headerMaskPolicySymbol = headerMaskPolicySymbol;
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

	public String getApiCacheFlag() {
		return apiCacheFlag;
	}

	public void setApiCacheFlag(String apiCacheFlag) {
		this.apiCacheFlag = apiCacheFlag;
	}

	public Integer getFixedCacheTime() {
		return fixedCacheTime;
	}

	public void setFixedCacheTime(Integer fixedCacheTime) {
		this.fixedCacheTime = fixedCacheTime;
	}

	public String getFailDiscoveryPolicy() {
		return failDiscoveryPolicy;
	}

	public void setFailDiscoveryPolicy(String failDiscoveryPolicy) {
		this.failDiscoveryPolicy = failDiscoveryPolicy;
	}

	public String getFailHandlePolicy() {
		return failHandlePolicy;
	}

	public void setFailHandlePolicy(String failHandlePolicy) {
		this.failHandlePolicy = failHandlePolicy;
	}

	public String getApiStatus() {
		return apiStatus;
	}

	public void setApiStatus(String apiStatus) {
		this.apiStatus = apiStatus;
	}
	
	public String getPublicFlag() {
		return publicFlag;
	}

	public void setPublicFlag(String publicFlag) {
		this.publicFlag = publicFlag;
	}

	public Date getApiReleaseTime() {
		return apiReleaseTime;
	}

	public void setApiReleaseTime(Date apiReleaseTime) {
		this.apiReleaseTime = apiReleaseTime;
	}


	public Long getScheduledLaunchDate() {
		return scheduledLaunchDate;
	}

	public void setScheduledLaunchDate(Long scheduledLaunchDate) {
		this.scheduledLaunchDate = scheduledLaunchDate;
	}

	public Long getScheduledRemovalDate() {
		return scheduledRemovalDate;
	}

	public void setScheduledRemovalDate(Long scheduledRemovalDate) {
		this.scheduledRemovalDate = scheduledRemovalDate;
	}

	public Long getEnableScheduledDate() {
		return enableScheduledDate;
	}

	public void setEnableScheduledDate(Long enableScheduledDate) {
		this.enableScheduledDate = enableScheduledDate;
	}

	public Long getDisableScheduledDate() {
		return disableScheduledDate;
	}

	public void setDisableScheduledDate(Long disableScheduledDate) {
		this.disableScheduledDate = disableScheduledDate;
	}

    public String getNotifyNameList() {
        return notifyNameList;
    }

    public void setNotifyNameList(String notifyNameList) {
        this.notifyNameList = notifyNameList;
    }
	
	public String getIsCorsAllowOrigin() {
		return isCorsAllowOrigin;
	}

	public void setIsCorsAllowOrigin(String isCorsAllowOrigin) {
		this.isCorsAllowOrigin = isCorsAllowOrigin;
	}

	public String getIsCorsAllowMethods() {
		return isCorsAllowMethods;
	}

	public void setIsCorsAllowMethods(String isCorsAllowMethods) {
		this.isCorsAllowMethods = isCorsAllowMethods;
	}

	public String getIsCorsAllowHeaders() {
		return isCorsAllowHeaders;
	}

	public void setIsCorsAllowHeaders(String isCorsAllowHeaders) {
		this.isCorsAllowHeaders = isCorsAllowHeaders;
	}

	public String getCorsAllowOrigin() {
		return corsAllowOrigin;
	}

	public void setCorsAllowOrigin(String corsAllowOrigin) {
		this.corsAllowOrigin = corsAllowOrigin;
	}

	public String getCorsAllowMethods() {
		return corsAllowMethods;
	}

	public void setCorsAllowMethods(String corsAllowMethods) {
		this.corsAllowMethods = corsAllowMethods;
	}

	public String getCorsAllowHeaders() {
		return corsAllowHeaders;
	}

	public void setCorsAllowHeaders(String corsAllowHeaders) {
		this.corsAllowHeaders = corsAllowHeaders;
	}
}
