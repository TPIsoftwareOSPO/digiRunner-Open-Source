package tpi.dgrv4.entity.exceptions;

import java.util.Map;

import org.springframework.http.HttpHeaders;

import tpi.dgrv4.common.vo.ReqHeader;

@SuppressWarnings("serial")
public class DgrException extends RuntimeException {
	private ReqHeader reqHeader;
	private DgrError<?> error;
	private Map<String, String> params;
	private String identify;
	private int httpCode;
	private ICheck iCheck;

	public DgrException(String message) {
		super(message);
	}

	public DgrException(String message, Throwable cause) {
		super(message, cause);
	}

	public DgrException(DgrError<?> error, String message, Throwable cause) {
		super(DgrError.getLocalizationMessage(error), cause);
		this.error = error;
	}

	public DgrException(DgrError<?> error) {
		this(error, null, null);
	}

	public DgrException(DgrError<?> error, Map<String, String> params) {
		this(error, null, null);
		this.params = params;
	}

	public DgrException(Exception e, ReqHeader reqHeader) {
		super(e.getMessage(), e);
		this.reqHeader = reqHeader;
		if (e instanceof DgrException) {
			this.error = ((DgrException) e).getError();
			this.params = ((DgrException) e).getParams();
		}
	}

	public DgrException(Exception e, ReqHeader reqHeader, String identify) {
		this(e, reqHeader);
		this.identify = identify;
	}

	/**
	 * 因應 John-陳瑞泰 在 2021/03/21 針對 GET API 提出的規範 故增加此建構式讓 Controller 使用，使得 GET
	 * 方法也能拋出含有 ReqHeader 的錯誤
	 * 
	 * @param e
	 * @param headers
	 */
	public DgrException(Exception e, HttpHeaders headers) {
		super(e.getMessage(), e);
		String txSN = headers.getFirst("txSN");
		String txDate = headers.getFirst("txDate");
		String txID = headers.getFirst("txID");
		this.reqHeader = new ReqHeader();
		this.reqHeader.setTxSN(txSN);
		this.reqHeader.setTxDate(txDate);
		this.reqHeader.setTxID(txID);
		if (e instanceof DgrException) {
			this.error = ((DgrException) e).getError();
			this.params = ((DgrException) e).getParams();
		}
	}

	public DgrException(int httpCode, ICheck iCheck) {
		super(iCheck.getRtnCode().getCode() + " - " + iCheck.getRtnCode().getDefaultMessage() + " - " +  iCheck.getClass().getName());
		this.httpCode = httpCode;
		this.iCheck = iCheck;
	}

	public int getHttpCode() {
		return httpCode;
	}

	public void setHttpCode(int httpCode) {
		this.httpCode = httpCode;
	}

	public ICheck getiCheck() {
		return iCheck;
	}

	public void setiCheck(ICheck iCheck) {
		this.iCheck = iCheck;
	}

	public DgrError<?> getError() {
		return this.error;
	}

	public ReqHeader getReqHeader() {
		return this.reqHeader;
	}

	public Map<String, String> getParams() {
		return this.params;
	}

	public String getIdentify() {
		return identify;
	}

	public void setReqHeader(ReqHeader reqHeader) {
		this.reqHeader = reqHeader;
	}

}
