package tpi.dgrv4.gateway.util;


import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.UUID64Util;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

public class InnerInvokeParam {
	private TsmpAuthorization tsmpAuthorization;
	private String txnUid;
	private String userIp;
	private String acToken;
	private String apiUrl;
	private String userHostname;
	private String origApiUrl;
	
	public static InnerInvokeParam getInstance(HttpHeaders headers, HttpServletRequest servletReq,
			TsmpAuthorization auth) throws Exception {
		InnerInvokeParam iip = new InnerInvokeParam(headers, servletReq, auth);
		return iip;
	}

	public static InnerInvokeParam getInstance() {
		InnerInvokeParam iip = new InnerInvokeParam();
		return iip;
	}
	
	protected void initData(TsmpAuthorization tsmpAuthorization, String txnUid, String apiUrl, String userIp, 
			String userHostname, String origApiUrl) {
		this.tsmpAuthorization = tsmpAuthorization;
		this.txnUid = txnUid;
		this.apiUrl = apiUrl;
		this.userIp = userIp;
		this.userHostname = userHostname;
		this.origApiUrl = origApiUrl;
	}

	protected void initTestData(TsmpAuthorization tsmpAuthorization, String txnUid, String apiUrl, String origApiUrl) {
		this.tsmpAuthorization = tsmpAuthorization;
		this.txnUid = txnUid;
		this.apiUrl = apiUrl;
		this.origApiUrl = origApiUrl;
	}

	InnerInvokeParam(HttpHeaders headers, HttpServletRequest servletReq, TsmpAuthorization auth) throws Exception {
		acToken = headers.getFirst("acToken");
		txnUid = headers.getFirst("UUID64");

		if (!StringUtils.hasLength(acToken) || !StringUtils.hasLength(txnUid)) {//直接調用產品的API
			tsmpAuthorization = auth;
			txnUid = UUID64Util.UUID64(UUID.randomUUID());
			userIp = servletReq == null ? "127.0.0.1" : servletReq.getRemoteAddr();//null例如排程程式
		} else {//透過客製包調用產品的API
			tsmpAuthorization = ControllerUtil.parserAuthorization(acToken);
			userIp = headers.getFirst("x-forwarded-for");
			origApiUrl = headers.getFirst("origApiUrl");//客製包API URL
		}

		userHostname = servletReq == null ? "localhost" : servletReq.getRemoteHost();//null,例如排程程式
		apiUrl = servletReq == null ? "" : servletReq.getRequestURI();//null,例如排程程式
	}

	public InnerInvokeParam() {
		userIp = "localhost";
	}

	@Override
	public String toString() {
		return "InnerInvokeParam [tsmpAuthorization=" + tsmpAuthorization + ", txnUid=" + txnUid + ", userIp=" + userIp
				+ ", acToken=" + acToken + ", apiUrl=" + apiUrl + ", userHostname=" + userHostname + ", origApiUrl="
				+ origApiUrl + "]";
	}

	public TsmpAuthorization getTsmpAuthorization() {
		return tsmpAuthorization;
	}

	public void setTsmpAuthorization(TsmpAuthorization tsmpAuthorization) {
		this.tsmpAuthorization = tsmpAuthorization;
	}

	public String getTxnUid() {
		return txnUid;
	}

	public void setTxnUid(String txnUid) {
		this.txnUid = txnUid;
	}

	public String getUserIp() {
		return userIp;
	}

	public void setUserIp(String userIp) {
		this.userIp = userIp;
	}

	public String getAcToken() {
		return acToken;
	}

	public void setAcToken(String acToken) {
		this.acToken = acToken;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public String getUserHostname() {
		return userHostname;
	}

	public void setUserHostname(String userHostname) {
		this.userHostname = userHostname;
	}
	
	public String getOrigApiUrl() {
		return origApiUrl;
	}

	public void setOrigApiUrl(String origApiUrl) {
		this.origApiUrl = origApiUrl;
	}
}
