package tpi.dgrv4.dpaa.component;

import java.io.ByteArrayOutputStream;
import java.sql.SQLTransientConnectionException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.ITsmpDpAaError;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.vo.IReqHeader;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.controller.SignBlockController;
import tpi.dgrv4.dpaa.controller.SignBlockResetController;
import tpi.dgrv4.dpaa.service.TsmpSettingService;
import tpi.dgrv4.dpaa.util.ControllerUtil;
import tpi.dgrv4.dpaa.vo.ResHeaderSignBlock;
import tpi.dgrv4.dpaa.vo.ResHeaderSignBlockReset;
import tpi.dgrv4.dpaa.vo.ResPayload;
import tpi.dgrv4.entity.component.cache.proxy.TsmpRtnCodeCacheProxy;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.ResHeader;

@ControllerAdvice(basePackages = {"tpi.dgrv4.dpaa.controller","tpi.dgrv4.enterprise", "tpi.dgrv4.gateway.service"})
public class DpaaControllerExceptionHandler {

	private TPILogger logger = TPILogger.tl;
	
	private final String RTN_CODE = "RTN_CODE";
	private final String RTN_MSG = "RTN_MSG";

	private ServiceConfig serviceConfig;
	private TsmpRtnCodeDao tsmpRtnCodeDao;
	private TsmpRtnCodeCacheProxy tsmpRtnCodeCacheProxy;
	private TsmpSettingService tsmpSettingService;
	
	@Autowired
	public DpaaControllerExceptionHandler(ServiceConfig serviceConfig, TsmpRtnCodeDao tsmpRtnCodeDao,
			TsmpRtnCodeCacheProxy tsmpRtnCodeCacheProxy, TsmpSettingService tsmpSettingService) {
		super();
		this.serviceConfig = serviceConfig;
		this.tsmpRtnCodeDao = tsmpRtnCodeDao;
		this.tsmpRtnCodeCacheProxy = tsmpRtnCodeCacheProxy;
		this.tsmpSettingService = tsmpSettingService;
	}

	@ExceptionHandler(TsmpDpAaException.class)
	public ModelAndView handleDpAaException(TsmpDpAaException ex) {
		this.logger.debug(String.format("Enter %s", getClass().getSimpleName()));

		writeDpLog(ex);
		
		ModelAndView mav = new ModelAndView(new MappingJackson2JsonView());

		try {
			String identify = ex.getIdentify();
			Map<String, String> rtnCodeMsgMap = getRtnCodeMsgMap(ex);
			String rtnCode = rtnCodeMsgMap.get(RTN_CODE);
			String rtnMsg = rtnCodeMsgMap.get(RTN_MSG);
			
			if(SignBlockController.identify.equals(identify)) {
				ResHeaderSignBlock resHeader = new ResHeaderSignBlock();
				
				resHeader.setRtnCode(rtnCode);
				resHeader.setRtnMsg(rtnMsg);			
				mav.addObject(ResPayload.KEY_OF_HEADER, resHeader);
				
			}else if(SignBlockResetController.identify.equals(identify)) {
				ResHeaderSignBlockReset resHeader = new ResHeaderSignBlockReset();
				
				resHeader.setRtnCode(rtnCode);
				resHeader.setRtnMsg(rtnMsg);			
				mav.addObject(ResPayload.KEY_OF_HEADER, resHeader);
				
			}else {
				ResHeader resHeader = initializeResHeader(ex);
				
				resHeader.setRtnCode(rtnCode);
				resHeader.setRtnMsg(rtnMsg);
				mav.addObject(ResPayload.KEY_OF_HEADER, resHeader);
			}
			
			return mav;
		} catch (Throwable e) {
			this.logger.debug("未預期的錯誤");
			logger.debug("" + e);
			
			//Spring中的doDispatch方法中, return null 表示不處理
			return null;
		}
	}

	private void writeDpLog(TsmpDpAaException ex) {
		Throwable t = null;
		if (ex.getCause() != null) {
			t = ex.getCause();
		} else {
			t = ex;
		}
		
		// exception stack info string
		String msgErr = getThrowableStackInfo(t);
		if(t instanceof TsmpDpAaException) {
			this.logger.info(getShortErrMsg(msgErr));
		}else {
			this.logger.error(msgErr);
		}
	}

	/**
	 * 業務邏輯錯誤應該不要印出所有 stack trace
	 * 把定義明確的 exception, logger Level 改為 INFO
	 * 
	 * @param e
	 * @return
	 */
    public String getShortErrMsg(String errStr) {
    	String errMsg = "";
    	String keyword = this.getTsmpSettingService().getVal_ERRORLOG_KEYWORD();
    	String[] keywordArray = keyword.split(",");
    	if(StringUtils.hasLength(errStr)) {
    		String[] errStrArr = errStr.split("\n");
    		if(errStrArr != null && errStrArr.length > 0) {
    			errMsg = errStrArr[0];
    			for (String str : errStrArr) {
    				//只印出有包含以下文字的錯誤訊息
    				if(checkKeyWord(str , keywordArray) 
    						&& str.contains("Service.java")) {
    					errMsg += "\n" + str;
    				}
    			}
    		}
    	}
    	return errMsg;
    }
    
    private boolean checkKeyWord(String errMsg ,String[] keyword) {
    	
    	for (String key : keyword) {
    		if (errMsg.contains(key.trim())) {
				return true;
			}
		}
    	return false;
    }
	
	/**
	 * 取出 exception info String
	 * @param e
	 * @return
	 */
	private String getThrowableStackInfo(Throwable e) {
	    ByteArrayOutputStream buf = new ByteArrayOutputStream();
	    e.printStackTrace(new java.io.PrintWriter(buf, true));
	    String msg = buf.toString();
	    try {
	        buf.close();
	    } catch (Exception t) {
	        return e.getMessage();
	    }
	    return msg;
	}

	private ResHeader initializeResHeader(TsmpDpAaException ex) {
		IReqHeader reqHeader = ex.getReqHeader();
		ResHeader resHeader = new ResHeader();
		if(reqHeader != null) {
			resHeader.setTxSN(reqHeader.getTxSN());
			resHeader.setTxDate(reqHeader.getTxDate());
			resHeader.setTxID(reqHeader.getTxID());
		}
		return resHeader;
	}

	private String getRtnCode(TsmpDpAaException ex) {
		ITsmpDpAaError<?> error = ex.getError();
		if (error == null) {
			// 資料庫錯誤
			if (isDBException(ex)) {
				return TsmpDpAaRtnCode._1293.getCode();
			}
			return TsmpDpAaRtnCode.SYSTEM_ERROR.getCode();
		}
		return error.getCode();
	}
 
	private Map<String, String> getRtnCodeMsgMap(TsmpDpAaException ex) throws Exception {
		Map<String, String> rtnCodeMsgMap = new HashMap<>();
		String rtnCode = getRtnCode(ex);
		rtnCodeMsgMap.put(RTN_CODE, rtnCode);
		
		String rtnMsg = "";
		
		IReqHeader reqHeader = ex.getReqHeader();
		String locale = null;
		if(reqHeader != null) {
			locale = reqHeader.getLocale();		
		}
		locale = ControllerUtil.getLocale(locale);
		
		// 資料庫錯誤
		if (isDBException(ex)) {
			rtnCode = TsmpDpAaRtnCode._1293.getCode();
			rtnCodeMsgMap.put(RTN_CODE, rtnCode);
			rtnCodeMsgMap = getRtnMsgByLocale(TsmpDpAaRtnCode._1293.throwing(), locale, rtnCodeMsgMap);
		}
		
		rtnCodeMsgMap = getRtnMsgByLocale(ex, locale, rtnCodeMsgMap);
		rtnMsg = String.format("%s - %s", rtnCodeMsgMap.get(RTN_CODE), rtnCodeMsgMap.get(RTN_MSG));
		
		rtnCodeMsgMap.put(RTN_MSG, rtnMsg);
		return rtnCodeMsgMap;
	}
	
	/**
	 * 取得資料表中 locale 的 rtn code 訊息
	 * 
	 * @param ex
	 * @return
	 * @throws Exception 
	 */
	private Map<String, String> getRtnMsgByLocale(TsmpDpAaException ex, String locale, 
			Map<String, String> rtnCodeMsgMap) throws Exception {
		String rtnCode = getRtnCode(ex);
		rtnCodeMsgMap.put(RTN_CODE, rtnCode);
		
		String rtnMsg = null;
		TsmpRtnCode tsmpRtnCode = getTsmpRtnCodeCacheProxy().findById(rtnCode, locale).orElse(null);
		
		if(tsmpRtnCode != null) {
			Map<String, String> params = ex.getParams();
			String message = tsmpRtnCode.getTsmpRtnMsg();	
			if(params != null && !params.isEmpty()) {
				rtnMsg = MailHelper.buildContent(message, params);
			}else {
				rtnMsg = message;
			}
			
		}else { // 資料表查無 locale 的 rtn code 訊息錯誤			
			rtnCodeMsgMap.put(RTN_CODE, TsmpDpAaRtnCode._1289.getCode());
			tsmpRtnCode = getTsmpRtnCodeCacheProxy().findById(TsmpDpAaRtnCode._1289.getCode(), locale).orElse(null);
			if(tsmpRtnCode != null) {
				String message = tsmpRtnCode.getTsmpRtnMsg();				
				Map<String, String> params = new HashMap<>();
				params.put("0", locale);
				params.put("1", rtnCode);
				rtnMsg = MailHelper.buildContent(message, params);
				
			}else {// 資料表查無 rtn code 1289 訊息錯誤
				this.logger.debug("TsmpRtnCode = null, 資料表查無 rtn code 1289 訊息錯誤");
				throw new Exception("TsmpRtnCode = null");
			}
		}
		rtnCodeMsgMap.put(RTN_MSG, rtnMsg);
		return rtnCodeMsgMap;
	}

	private boolean isDBException(TsmpDpAaException e) {
		if (e.getCause() instanceof CannotCreateTransactionException ||
			e.getCause() instanceof SQLTransientConnectionException ||
			e.getCause() instanceof DataAccessResourceFailureException ||
			(!ObjectUtils.isEmpty(e.getMessage()) && e.getMessage().matches("(JDBC\\s?)?[c|C]onnection(\\s?[l|L]eak)?"))
		) {
			return true;
		}
		return false;
	}
	
	protected TsmpSettingService getTsmpSettingService() {
		return this.tsmpSettingService;
	}
	
	protected TsmpRtnCodeCacheProxy getTsmpRtnCodeCacheProxy() {
		return this.tsmpRtnCodeCacheProxy;
	}

	protected ServiceConfig getServiceConfig() {
		return serviceConfig;
	}

	protected TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return tsmpRtnCodeDao;
	}
	
}
