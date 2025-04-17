package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.isValueTooLargeException;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0096Req;
import tpi.dgrv4.dpaa.vo.DPB0096Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.entity.TsmpRtnCodeId;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0096Service {
	
	private TPILogger logger = TPILogger.tl;
	
	private TsmpRtnCodeDao tsmpRtnCodeDao;
	private BcryptParamHelper bcryptParamHelper;
	
	@Autowired
	public DPB0096Service(TsmpRtnCodeDao tsmpRtnCodeDao, BcryptParamHelper bcryptParamHelper) {
		super();
		this.tsmpRtnCodeDao = tsmpRtnCodeDao;
		this.bcryptParamHelper = bcryptParamHelper;
	}

	public DPB0096Resp createApiRtnCode(TsmpAuthorization tsmpAuthorization, DPB0096Req req, ReqHeader reqHeader) {
		DPB0096Resp resp = new DPB0096Resp();
	
		try {
			String local = ServiceUtil.getLocale(reqHeader.getLocale());

			TsmpRtnCode tsmpRtnCode = checkParams(tsmpAuthorization, req, local);
			tsmpRtnCode = getTsmpRtnCodeDao().saveAndFlush(tsmpRtnCode);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			if (isValueTooLargeException(e)) {
				throw TsmpDpAaRtnCode._1220.throwing();
			} else {
				this.logger.error(StackTraceUtil.logStackTrace(e));
				throw TsmpDpAaRtnCode._1297.throwing();
			}
		}
		return resp;
	}
	
	/**
	 * 0. 檢查使用者是否有登入(Authorization中是否有userName值)，否則拋出 1219 錯誤
	 * 1. 回覆代碼、語言地區、顯示的回覆訊息不得空值，否則拋出 1296 錯誤
	 * 2. (回覆代碼 + 語言地區)不得與現有資料重複，否則拋出 1226 錯誤				
	 * 3. 若任一欄位字數過長，無法儲存至DB，則拋出 1220 錯誤					
	 * 
	 * @param tsmpAuthorization
	 * @param req
	 */
	protected TsmpRtnCode checkParams(TsmpAuthorization tsmpAuthorization, DPB0096Req req, String reqHeaderLocale) {
		TsmpRtnCode tsmpRtnCode = new TsmpRtnCode();
		String userName = tsmpAuthorization.getUserName();
		String rtnCode = req.getTsmpRtnCode();
		String locale = req.getLocale();
		String rtnMsg = req.getTsmpRtnMsg();
		String rtnDesc = ServiceUtil.nvl(req.getTsmpRtnDesc()); 
		
		locale = decodeLocale(locale, reqHeaderLocale);
		
		// 0. 檢查使用者是否有登入(Authorization中是否有userName值)，否則拋出 1219 錯誤
		if (!StringUtils.hasLength(userName))
			throw TsmpDpAaRtnCode._1219.throwing();

		// 1. 回覆代碼、語言地區、顯示的回覆訊息不得空值，否則拋出 1296 錯誤
		if (!StringUtils.hasLength(rtnCode) || !StringUtils.hasLength(locale) || !StringUtils.hasLength(rtnMsg)) {
			throw TsmpDpAaRtnCode._1296.throwing();
		}

		//2. (回覆代碼 + 語言地區)不得與現有資料重複，否則拋出 1226 錯誤	
		TsmpRtnCodeId id = new TsmpRtnCodeId(rtnCode,locale);
		Optional<TsmpRtnCode> opt_rtnCode = getTsmpRtnCodeDao().findById(id);
	
		if (opt_rtnCode.isPresent()) {
			throw TsmpDpAaRtnCode._1226.throwing();	//不得與現有資料重複
		}
		
		tsmpRtnCode.setLocale(locale);
		tsmpRtnCode.setTsmpRtnCode(rtnCode);
		tsmpRtnCode.setTsmpRtnDesc(rtnDesc);
		tsmpRtnCode.setTsmpRtnMsg(rtnMsg);
		
		return tsmpRtnCode;
	}
	
	protected String decodeLocale(String locale, String reqHeaderLocale) {
		if (StringUtils.hasLength(locale)) {
			try {
				locale = getBcryptParamHelper().decode(locale, "RTN_CODE_LOCALE", reqHeaderLocale);
			} catch (BcryptParamDecodeException e) {
				throw TsmpDpAaRtnCode._1299.throwing();
			}
		}
		return locale;
		
	}

	protected TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return this.tsmpRtnCodeDao;
	}
	
	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}
}
