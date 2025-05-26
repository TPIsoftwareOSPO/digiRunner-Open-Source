package tpi.dgrv4.entity.entity.jpql;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tsmp_api_detail")
public class TsmpApiDetail {

	@Id
	@Column(name = "id")
	private Long id;

	@Column(name = "api_key")
	private String apiKey;

	@Column(name = "api_name")
	private String apiName;

	@Column(name = "path_of_json")
	private String pathOfJson;

	@Column(name = "method_of_json")
	private String methodOfJson;
	
	@Column(name = "api_module_id")
	private Long apiModuleId;

	@Column(name = "params_of_json")
	private String paramsOfJson;

	@Column(name = "consumes_of_json")
	private String consumesOfJson;

	@Column(name = "produces_of_json")
	private String producesOfJson;
	
	@Column(name = "headers_of_json")
	private String headersOfJson;

	/* constructors */

	public TsmpApiDetail() {}

	/* methods */
	
	@Override
	public String toString() {
		return "TsmpApiDetail [id=" + id + ", apiModuleId=" + apiModuleId + ", apiKey=" + apiKey + ", apiName="
				+ apiName + ", pathOfJson=" + pathOfJson + ", methodOfJson=" + methodOfJson + ", paramsOfJson="
				+ paramsOfJson + ", headersOfJson=" + headersOfJson + ", consumesOfJson=" + consumesOfJson
				+ ", producesOfJson=" + producesOfJson + "]";
	}

	/* getters and setters */

	public Long getId() {
		return id;
	}

	public Long getApiModuleId() {
		return apiModuleId;
	}

	public void setApiModuleId(Long apiModuleId) {
		this.apiModuleId = apiModuleId;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
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
	
	public String getPathOfJson() {
		return pathOfJson;
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

	public void setConsumesOfJson(String consumesOfJson) {
		this.consumesOfJson = consumesOfJson;
	}

	public String getProducesOfJson() {
		return producesOfJson;
	}

	public void setProducesOfJson(String producesOfJson) {
		this.producesOfJson = producesOfJson;
	}
	
	public String getConsumesOfJson() {
		return consumesOfJson;
	}

}