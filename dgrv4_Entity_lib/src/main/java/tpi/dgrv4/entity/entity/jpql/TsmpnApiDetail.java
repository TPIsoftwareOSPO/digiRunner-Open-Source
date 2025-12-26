package tpi.dgrv4.entity.entity.jpql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmpn_api_detail")
public class TsmpnApiDetail {

	@Id
	@Column(name = "id")
	private Long id;

	@Column(name = "api_module_id")
	private Long apiModuleId;

	@Column(name = "api_name")
	private String apiName;

	@Column(name = "path_of_json")
	private String pathOfJson;

	@Column(name = "method_of_json")
	private String methodOfJson;
	
	@Column(name = "url_rid")
	private String urlRid;

	@Column(name = "params_of_json")
	private String paramsOfJson;

	@Column(name = "headers_of_json")
	private String headersOfJson;

	@Column(name = "consumes_of_json")
	private String consumesOfJson;

	@Column(name = "produces_of_json")
	private String producesOfJson;
	
	@Column(name = "api_key")
	private String apiKey;

	/* constructors */

	public TsmpnApiDetail() {}

	/* methods */

	@Override
	public String toString() {
		return "TsmpnApiDetail [id=" + id + ", apiModuleId=" + apiModuleId + ", apiKey=" + apiKey + ", apiName="
				+ apiName + ", pathOfJson=" + pathOfJson + ", methodOfJson=" + methodOfJson + ", paramsOfJson="
				+ paramsOfJson + ", headersOfJson=" + headersOfJson + ", consumesOfJson=" + consumesOfJson
				+ ", producesOfJson=" + producesOfJson + ", urlRid=" + urlRid + "]";
	}

	/* getters and setters */
	
	public String getApiKey() {
		return apiKey;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getApiModuleId() {
		return apiModuleId;
	}

	public void setApiModuleId(Long apiModuleId) {
		this.apiModuleId = apiModuleId;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getApiName() {
		return apiName;
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
	
	public void setApiName(String apiName) {
		this.apiName = apiName;
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

	public void setUrlRid(String urlRid) {
		this.urlRid = urlRid;
	}

	public String getUrlRid() {
		return urlRid;
	}
}