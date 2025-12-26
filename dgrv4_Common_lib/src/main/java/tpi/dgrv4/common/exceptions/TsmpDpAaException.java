package tpi.dgrv4.common.exceptions;

import java.util.Map;

import tpi.dgrv4.common.vo.IReqHeader;
import tpi.dgrv4.common.vo.ReqHeader;

@SuppressWarnings("serial")
public class TsmpDpAaException extends RuntimeException {
	private IReqHeader reqHeader;
    private ITsmpDpAaError<?> error;
    private Map<String, String> params;
    private String identify;

	public TsmpDpAaException(String message){ super(message); }

    public TsmpDpAaException(String message, Throwable cause){
        super(message, cause);
    }

    public TsmpDpAaException(ITsmpDpAaError<?> error, String message, Throwable cause){
        super(ITsmpDpAaError.getLocalizationMessage(error), cause);
        this.error = error;
    }
    
    public TsmpDpAaException(ITsmpDpAaError<?> error) {
    	this(error, null, null);
    }
 
    public TsmpDpAaException(ITsmpDpAaError<?> error, Map<String, String> params){
    	this(error, null, null);
        this.params = params;
    }

    public TsmpDpAaException(Exception e, IReqHeader reqHeader) {
    	super(e.getMessage(), e);
    	this.reqHeader = reqHeader;
    	if (e instanceof TsmpDpAaException) {
    		this.error = ((TsmpDpAaException) e).getError();
    		this.params = ((TsmpDpAaException) e).getParams();
    	}
    }
        
    public TsmpDpAaException(Exception e, ReqHeader reqHeader, String identify) {
    	this(e, reqHeader);
    	this.identify = identify;
    }

	public ITsmpDpAaError<?> getError() {
        return this.error;
    }

    public IReqHeader getReqHeader() {
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
