package tpi.dgrv4.entity.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_api_reg")
@IdClass(TsmpApiRegId.class)
public class TsmpApiReg implements Serializable {

	/** API KEY */
	@Id
	@Column(name = "api_key")
	private String apiKey;

	/** Module Name */
	@Id
	@Column(name = "module_name")
	private String moduleName;

	/** 來源URL */
	@Column(name = "src_url")
	private String srcUrl;

	/** URL有ResourceID ("0": 沒有(default); "1":有 ) */
	@Column(name = "url_rid")
	private String urlRid;

	/** 註冊狀態 ("0": 暫存; "1": 確認 (for C:先暫存，回呼再確認; for R: 儲存時直接確認)) */
	@Column(name = "reg_status")
	private String regStatus;

	/** API UUID Only for Composed API */
	@Column(name = "api_uuid")
	private String apiUuid;

	/** 註冊主機代碼 */
	@Column(name = "reghost_id")
	private String reghostId;

	/**  */
	@Column(name = "path_of_json")
	private String pathOfJson;

	/**  */
	@Column(name = "method_of_json")
	private String methodOfJson;

	/**  */
	@Column(name = "params_of_json")
	private String paramsOfJson;

	/**  */
	@Column(name = "headers_of_json")
	private String headersOfJson;

	/**  */
	@Column(name = "consumes_of_json")
	private String consumesOfJson;

	/**  */
	@Column(name = "produces_of_json")
	private String producesOfJson;

	/**  */
	@Column(name = "create_time")
	private Date createTime;

	/**  */
	@Column(name = "create_user")
	private String createUser;

	/**  */
	@Column(name = "update_time")
	private Date updateTime;

	/**  */
	@Column(name = "update_user")
	private String updateUser;

	/** tsmpg 允許轉導的設定 */
	@Column(name = "no_oauth")
	private String noOauth;

	/** 功能flag */
	@Column(name = "fun_flag")
	private Integer funFlag;

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
	
	@Column(name = "FAIL_DISCOVERY_POLICY")
	private String failDiscoveryPolicy = "0";
	
	@Column(name = "FAIL_HANDLE_POLICY")
	private String failHandlePolicy = "0";

	/* constructors */

	public TsmpApiReg() {
	}

	/* methods */

	@Override
	public String toString() {
		return "TsmpApiReg [apiKey=" + apiKey + ", moduleName=" + moduleName + ", srcUrl=" + srcUrl + ", urlRid="
				+ urlRid + ", regStatus=" + regStatus + ", apiUuid=" + apiUuid + ", reghostId=" + reghostId
				+ ", pathOfJson=" + pathOfJson + ", methodOfJson=" + methodOfJson + ", paramsOfJson=" + paramsOfJson
				+ ", headersOfJson=" + headersOfJson + ", consumesOfJson=" + consumesOfJson + ", producesOfJson="
				+ producesOfJson + ", createTime=" + createTime + ", createUser=" + createUser + ", updateTime="
				+ updateTime + ", updateUser=" + updateUser + ", noOauth=" + noOauth + ", funFlag=" + funFlag
				+ ", redirectByIp=" + redirectByIp + ", ipForRedirect1=" + ipForRedirect1 + ", ipSrcUrl1=" + ipSrcUrl1
				+ ", ipForRedirect2=" + ipForRedirect2 + ", ipSrcUrl2=" + ipSrcUrl2 + ", ipForRedirect3="
				+ ipForRedirect3 + ", ipSrcUrl3=" + ipSrcUrl3 + ", ipForRedirect4=" + ipForRedirect4 + ", ipSrcUrl4="
				+ ipSrcUrl4 + ", ipForRedirect5=" + ipForRedirect5 + ", ipSrcUrl5=" + ipSrcUrl5 + ", bodyMaskKeyword="
				+ bodyMaskKeyword + ", bodyMaskPolicy=" + bodyMaskPolicy + ", bodyMaskPolicyNum=" + bodyMaskPolicyNum
				+ ", bodyMaskPolicySymbol=" + bodyMaskPolicySymbol + ", headerMaskKey=" + headerMaskKey
				+ ", headerMaskPolicy=" + headerMaskPolicy + ", headerMaskPolicyNum=" + headerMaskPolicyNum
				+ ", headerMaskPolicySymbol=" + headerMaskPolicySymbol + ", failDiscoveryPolicy=" + failDiscoveryPolicy
				+ ", failHandlePolicy=" + failHandlePolicy + "]";
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

	public String getSrcUrl() {
		return srcUrl;
	}

	public void setSrcUrl(String srcUrl) {
		this.srcUrl = srcUrl;
	}

	public String getUrlRid() {
		return urlRid;
	}

	public void setUrlRid(String urlRid) {
		this.urlRid = urlRid;
	}

	public String getRegStatus() {
		return regStatus;
	}

	public void setRegStatus(String regStatus) {
		this.regStatus = regStatus;
	}

	public String getApiUuid() {
		return apiUuid;
	}

	public void setApiUuid(String apiUuid) {
		this.apiUuid = apiUuid;
	}

	public String getReghostId() {
		return reghostId;
	}

	public void setReghostId(String reghostId) {
		this.reghostId = reghostId;
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

	public String getNoOauth() {
		return noOauth;
	}

	public void setNoOauth(String noOauth) {
		this.noOauth = noOauth;
	}

	public Integer getFunFlag() {
		return funFlag;
	}

	public void setFunFlag(Integer funFlag) {
		this.funFlag = funFlag;
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

	public String getHeaderMaskKey() {
		return headerMaskKey;
	}

	public void setHeaderMaskKey(String headerMaskKey) {
		this.headerMaskKey = headerMaskKey;
	}

	public String getHeaderMaskPolicy() {
		return headerMaskPolicy;
	}

	public void setHeaderMaskPolicy(String headerMaskPolicy) {
		this.headerMaskPolicy = headerMaskPolicy;
	}

	public String getBodyMaskKeyword() {
		return bodyMaskKeyword;
	}

	public void setBodyMaskKeyword(String bodyMaskKeyword) {
		this.bodyMaskKeyword = bodyMaskKeyword;
	}

	public String getBodyMaskPolicy() {
		return bodyMaskPolicy;
	}

	public void setBodyMaskPolicy(String bodyMaskPolicy) {
		this.bodyMaskPolicy = bodyMaskPolicy;
	}

	public Integer getBodyMaskPolicyNum() {
		return bodyMaskPolicyNum;
	}

	public void setBodyMaskPolicyNum(Integer bodyMaskPolicyNum) {
		this.bodyMaskPolicyNum = bodyMaskPolicyNum;
	}

	public String getBodyMaskPolicySymbol() {
		return bodyMaskPolicySymbol;
	}

	public void setBodyMaskPolicySymbol(String bodyMaskPolicySymbol) {
		this.bodyMaskPolicySymbol = bodyMaskPolicySymbol;
	}

	public Integer getHeaderMaskPolicyNum() {
		return headerMaskPolicyNum;
	}

	public void setHeaderMaskPolicyNum(Integer headerMaskPolicyNum) {
		this.headerMaskPolicyNum = headerMaskPolicyNum;
	}

	public String getHeaderMaskPolicySymbol() {
		return headerMaskPolicySymbol;
	}

	public void setHeaderMaskPolicySymbol(String headerMaskPolicySymbol) {
		this.headerMaskPolicySymbol = headerMaskPolicySymbol;
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
}