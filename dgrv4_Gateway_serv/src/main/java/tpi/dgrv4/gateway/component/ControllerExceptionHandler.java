package tpi.dgrv4.gateway.component;

import java.io.ByteArrayOutputStream;
import java.sql.SQLTransientConnectionException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import tpi.dgrv4.common.utils.ServiceUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.entity.component.cache.proxy.TsmpRtnCodeCacheProxy;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.exceptions.DgrError;
import tpi.dgrv4.entity.exceptions.DgrException;
import tpi.dgrv4.entity.exceptions.DgrRtnCode;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.gateway.vo.ResHeader;
import tpi.dgrv4.gateway.vo.TsmpBaseResp;

@ControllerAdvice(basePackages = {"tpi.dgrv4.gateway.controller","tpi.dgrv4.dpaa.controller"})
public class ControllerExceptionHandler {

	private final String RTN_CODE = "RTN_CODE";
	private final String RTN_MSG = "RTN_MSG";

	private TPILogger logger;
	private ServiceConfig serviceConfig;
	private TsmpRtnCodeCacheProxy tsmpRtnCodeCacheProxy;
	private TsmpRtnCodeDao tsmpRtnCodeDao;
	private TsmpSettingService tsmpSettingService;

	@Autowired
	public ControllerExceptionHandler(TPILogger logger, ServiceConfig serviceConfig,
			TsmpRtnCodeCacheProxy tsmpRtnCodeCacheProxy, TsmpRtnCodeDao tsmpRtnCodeDao,
			TsmpSettingService tsmpSettingService) {
		super();
		this.logger = logger;
		this.serviceConfig = serviceConfig;
		this.tsmpRtnCodeCacheProxy = tsmpRtnCodeCacheProxy;
		this.tsmpRtnCodeDao = tsmpRtnCodeDao;
		this.tsmpSettingService = tsmpSettingService;
	}


	@ExceptionHandler(DgrException.class)
	public ModelAndView handleDgrException(DgrException ex) {
		this.logger.debug("Enter " + getClass().getSimpleName());

		writeDgrLog(ex);

		ModelAndView mav = new ModelAndView(new MappingJackson2JsonView());

		try {
			Map<String, String> rtnCodeMsgMap = getRtnCodeMsgMap(ex);
			String rtnCode = rtnCodeMsgMap.get(RTN_CODE);
			String rtnMsg = rtnCodeMsgMap.get(RTN_MSG);

			ResHeader resHeader = initializeResHeader(ex);
			resHeader.setRtnCode(rtnCode);
			resHeader.setRtnMsg(rtnMsg);
			mav.addObject(TsmpBaseResp.KEY_OF_HEADER, resHeader);

			return mav;
		} catch (Throwable e) {
			this.logger.error("未預期的錯誤\n" + StackTraceUtil.logStackTrace(e));
			//Spring中的doDispatch方法中, return null 表示不處理
			return null;
		}
	}

	private void writeDgrLog(DgrException ex) {
		Throwable t = null;
		if (ex.getCause() != null) {
			t = ex.getCause();
		} else {
			t = ex;
		}

		// exception stack info string
		String msgErr = getThrowableStackInfo(t);
		
		if(t instanceof DgrException) {
			this.logger.info(getShortErrMsg(msgErr));
		}else {
			this.logger.error(msgErr);
		}
		
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

	/**
	 * 業務邏輯錯誤應該不要印出所有 stack trace
	 * 把定義明確的 exception, logger Level 改為 INFO
	 *
	 * @param e
	 * @return
	 */
    public String getShortErrMsg(String errStr) {
    	String ceh_errMsg = "";
    	String keyword = getTsmpSettingService().getVal_ERRORLOG_KEYWORD();
    	String[] keywordArray = keyword.trim().split(",");
    	if(StringUtils.hasText(errStr)) {
    		String[] errStrArr = errStr.split("\n");
    		if(errStrArr != null && errStrArr.length > 0) {
    			ceh_errMsg = errStrArr[0];
    			for (String str : errStrArr) {
    				//只印出有包含以下文字的錯誤訊息
    				if(checkKeyWord(str , keywordArray)
    						&& str.contains("Service.java")) {
    					ceh_errMsg += "\n" + str;
    				}
    			}
    		}
    	}
    	return ceh_errMsg;
    }
    
   private boolean checkKeyWord(String errMsg ,String[] keyword) {
    	
    	for (String ceh_key : keyword) {
    		if (errMsg.contains(ceh_key.trim())) {
				return true;
			}
		}
    	return false;
    }

	private ResHeader initializeResHeader(DgrException ex) {
		ReqHeader reqHeader = ex.getReqHeader();
		ResHeader resHeader = new ResHeader();
		if(reqHeader != null) {
			resHeader.setTxSN(reqHeader.getTxSN());
			resHeader.setTxDate(reqHeader.getTxDate());
			resHeader.setTxID(reqHeader.getTxID());
		}
		return resHeader;
	}

	private Map<String, String> getRtnCodeMsgMap(DgrException ex) throws Exception {
		ReqHeader reqHeader = ex.getReqHeader();
		String locale = null;
		if(reqHeader != null) {
			locale = reqHeader.getLocale();
		}
		locale = ServiceUtil.getLocale(locale);

		// 資料庫錯誤
		if (isDBException(ex)) {
			return resolve(locale, new DgrException(DgrRtnCode._1293));
		}

		return resolve(locale, ex);
	}

	/**
	 * 處理 {@link DglRtnCode}
	 * @param locale
	 * @param ex
	 * @return
	 */
	public Map<String, String> resolve(String locale, DgrException ex) throws Exception {
		// RTN_CODE
		String rtnCode = "";
		DgrError<?> error = ex.getError();
		if (error == null) {
			rtnCode = DgrRtnCode.SYSTEM_ERROR.getCode();
		} else {
			rtnCode = error.getCode();
		}

		// RTN_MSG
		String rtnMsg = "";
		TsmpRtnCode tsmpRtnCode = getRtnMsgById(rtnCode, locale);
		// 判斷代碼是否存在 TSMP_RTN_CODE 中
		if (tsmpRtnCode == null) {
			// 如果連 1289 都找不到，就直接報錯
			if (DgrRtnCode._1289.getCode().equals(rtnCode)) {
				throw new Exception(String.format("Missing '%s' code in TSMP_RTN_CODE!", rtnCode));
			}

			Map<String, String> params = new HashMap<>();
			params.put("0", locale);
			params.put("1", rtnCode);
			return resolve(locale, new DgrException(DgrRtnCode._1289, params));
		} else {
			rtnMsg = tsmpRtnCode.getTsmpRtnMsg();
			// 取代變數
			Map<String, String> params = ex.getParams();
			if(!ObjectUtils.isEmpty(params)) {
				rtnMsg = ServiceUtil.buildContent(rtnMsg, params);
			}
		}

		// 重組訊息格式
		rtnMsg = String.format("%s - %s", rtnCode, rtnMsg);

		Map<String, String> pair = new HashMap<>();
		pair.put(RTN_CODE, rtnCode);
		pair.put(RTN_MSG, rtnMsg);
		return pair;
	}

	/**
	 * Cache版
	 */
	public TsmpRtnCode getRtnMsgById(String rtnCode, String locale) {
		Optional<TsmpRtnCode> opt = getTsmpRtnCodeCacheProxy().findById(rtnCode, locale);
		return opt.orElse(null);
	}

	private boolean isDBException(DgrException e) {
		if (e.getCause() instanceof CannotCreateTransactionException ||
			e.getCause() instanceof SQLTransientConnectionException ||
			e.getCause() instanceof DataAccessResourceFailureException ||
			(!ObjectUtils.isEmpty(e.getMessage()) && e.getMessage().matches("(JDBC\\s?)?[c|C]onnection(\\s?[l|L]eak)?"))
		) {
			return true;
		}
		return false;
	}

	protected ServiceConfig getServiceConfig() {
		return serviceConfig;
	}

	protected TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return tsmpRtnCodeDao;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
	
	protected TsmpRtnCodeCacheProxy getTsmpRtnCodeCacheProxy() {
		return this.tsmpRtnCodeCacheProxy;
	}

}
