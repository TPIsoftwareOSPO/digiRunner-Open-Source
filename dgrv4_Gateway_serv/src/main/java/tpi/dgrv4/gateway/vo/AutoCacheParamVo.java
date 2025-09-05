package tpi.dgrv4.gateway.vo;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import org.springframework.util.MultiValueMap;

public class AutoCacheParamVo {

	private Map<String, List<String>> header;
	private String srcUrl;
	private TsmpApiLogReq dgrReqVo;
	private String reqMbody;
	private String uuid;
	private String httpMethod;
	private Map<String, String[]> paramMap;
	private Map<String, String> partContentTypes;
	private HashMap<String, String> maskinfo;
	private HttpServletResponse httpRes;
	private HttpServletRequest httpReq;



	public HttpServletResponse getHttpRes() {
		return httpRes;
	}

	public void setHttpRes(HttpServletResponse httpRes) {
		this.httpRes = httpRes;
	}


	// url encode
	private MultiValueMap<String, String> values;

	public Map<String, List<String>> getHeader() {
		return header;
	}

	public void setHeader(Map<String, List<String>> header) {
		this.header = header;
	}

	public String getSrcUrl() {
		return srcUrl;
	}

	public void setSrcUrl(String srcUrl) {
		this.srcUrl = srcUrl;
	}

	public TsmpApiLogReq getDgrReqVo() {
		return dgrReqVo;
	}

	public void setDgrReqVo(TsmpApiLogReq dgrReqVo) {
		this.dgrReqVo = dgrReqVo;
	}

	public String getReqMbody() {
		return reqMbody;
	}

	public void setReqMbody(String reqMbody) {
		this.reqMbody = reqMbody;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public Map<String, String[]> getParamMap() {
		return paramMap;
	}

	public void setParamMap(Map<String, String[]> paramMap) {
		this.paramMap = paramMap;
	}

	public MultiValueMap<String, String> getValues() {
		return values;
	}

	public void setValues(MultiValueMap<String, String> values) {
		this.values = values;
	}

	public Map<String, String> getPartContentTypes() {
		return partContentTypes;
	}

	public void setPartContentTypes(Map<String, String> partContentTypes) {
		this.partContentTypes = partContentTypes;
	}

	public HashMap<String, String> getMaskinfo() {
		return maskinfo;
	}

	public void setMaskinfo(HashMap<String, String> maskinfo) {
		this.maskinfo = maskinfo;
	}

    public HttpServletRequest getHttpReq() {
        return httpReq;
    }

    public void setHttpReq(HttpServletRequest httpReq) {
        this.httpReq = httpReq;
    }
}
