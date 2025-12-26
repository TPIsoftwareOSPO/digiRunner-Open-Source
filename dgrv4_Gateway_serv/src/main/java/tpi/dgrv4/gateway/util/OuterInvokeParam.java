package tpi.dgrv4.gateway.util;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.UUID64Util;

public class OuterInvokeParam {

	private String uuid64;
	private Map<String,String> invokeHttpHeader ;
	private String scheme;
	private String origApiUrl;

	public static OuterInvokeParam getInstance(HttpHeaders headers, HttpServletRequest servletReq, String origApiUrl) {
		OuterInvokeParam oip = new OuterInvokeParam(headers, servletReq, origApiUrl);
		return oip;
	}

	public static OuterInvokeParam getInstance() {
		OuterInvokeParam oip = new OuterInvokeParam();
		return oip;
	}

	OuterInvokeParam(){
		uuid64 = UUID64Util.UUID64(java.util.UUID.randomUUID());
		// UUID64
		invokeHttpHeader = new HashMap<>();
		invokeHttpHeader.put("UUID64", uuid64);
		invokeHttpHeader.put("x-forwarded-for", "localhostTest" );
		invokeHttpHeader.put("acToken", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiWVdSdGFXNUJVRWsiXSwidXNlcl9uYW1lIjoibWFuYWdlciIsIm9yZ19pZCI6IjEwMDAwMCIsInNjb3BlIjpbIjEwMDAiXSwiZXhwIjoxNjQ1NjY3OTExLCJhdXRob3JpdGllcyI6WyIxMDAwIiwiMTAwNyIsIjEwMTciXSwianRpIjoiVkUzSldlVHFiQWRpY2VXTXJxbmNwSmJMVXdJIiwiY2xpZW50X2lkIjoiWVdSdGFXNURiMjV6YjJ4bCJ9.pJKmbQH1vLBmllHj9PPai_LFgA2EXrRCxMbrZKmlUrsojg5qcyRyTqNn8XEDv14TV4qnh9JfSkGZyPRdStAOaPnCE89KQW-OS6HrziI2ZgaoozX19jDWIF_K4sjEGdYIzZwRBX6ck4Y7Imzrx1PsY7No5zpuzDVXi8GjvGXC0SCL38UFoCaE7VEFi6Pz3Vxai3O2JnQmNaCl91j3Cw69X7nLB2PlDMo8nA1QBdFEaCTf9VTxQ8jt1dxCKOlYPeYc-2zoed5JKB_5BNJ7OMJdHKxRxlc1BIr1qxcuFxeIoJSidzu-oPTNeBxQ9iNIz8QpkyJEhg8GuU40LQBS5MIPWg");
		String dummyApiUrl = "/dummyApiUrl";
		invokeHttpHeader.put("origApiUrl", dummyApiUrl);
		origApiUrl = dummyApiUrl;

		scheme = "http";
	}

	OuterInvokeParam(HttpHeaders headers, HttpServletRequest servletReq, String origApiUrl) {
		uuid64 = UUID64Util.UUID64(java.util.UUID.randomUUID());
		// UUID64
		invokeHttpHeader = new HashMap<>();
		invokeHttpHeader.put("UUID64", uuid64);
		invokeHttpHeader.put("x-forwarded-for", StringUtils.isEmpty(headers.getFirst("x-forwarded-for")) ? servletReq.getRemoteAddr() : headers.getFirst("x-forwarded-for") );
		invokeHttpHeader.put("acToken", headers.getFirst("authorization"));
		invokeHttpHeader.put("origApiUrl", origApiUrl);
		this.origApiUrl = origApiUrl;

		scheme = servletReq.getScheme();
	}

	public String getUuid64() {
		return uuid64;
	}

	public Map<String, String> getInvokeHttpHeader() {
		return invokeHttpHeader;
	}

	public String getScheme() {
		return scheme;
	}

	public String getOrigApiUrl() {
		return origApiUrl;
	}
}
