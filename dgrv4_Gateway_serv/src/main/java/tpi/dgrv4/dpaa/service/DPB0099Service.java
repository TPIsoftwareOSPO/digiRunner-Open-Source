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
import tpi.dgrv4.dpaa.vo.DPB0099Req;
import tpi.dgrv4.dpaa.vo.DPB0099Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.entity.TsmpRtnCodeId;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0099Service {
	
	private TsmpRtnCodeDao tsmpRtnCodeDao;
	private BcryptParamHelper bcryptParamHelper;
	
	private TPILogger logger = TPILogger.tl;
	
	@Autowired
	public DPB0099Service(TsmpRtnCodeDao tsmpRtnCodeDao, BcryptParamHelper bcryptParamHelper) {
		super();
		this.tsmpRtnCodeDao = tsmpRtnCodeDao;
		this.bcryptParamHelper = bcryptParamHelper;
	}

	public DPB0099Resp updateApiRtnCode(TsmpAuthorization authorization, DPB0099Req req, ReqHeader reqHeader) {
		DPB0099Resp resp = new DPB0099Resp();
		try {
			String local = ServiceUtil.getLocale(reqHeader.getLocale());

			checkParmas(authorization, req, local);
			
			resp = updateTsmpRtnCode(authorization, req, local);
			
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
	 * 1. 若依照回覆代碼、語言地區找不到可更新的資料，則拋出 1298 錯誤。					
	 * 2. 顯示的回覆訊息若為空值，或是未傳入回覆代碼、語言地區則拋出 1296 錯誤。					
	 * 3. 若顯示的回覆訊息或說明字數過長，無法儲存至DB，則拋出 1220 錯誤					
		 P.S 可利用 ServiceUtil.isValueTooLargeException(Throwable t) 判斷是否資料長度過長				
	 * 4. 若發生其他更新失敗，則拋出 1297 錯誤。					
	 * 
	 * @param authorization
	 * @param req
	 */
	public void checkParmas(TsmpAuthorization authorization, DPB0099Req req, String reqHeaderLocale) {
		String userName = authorization.getUserName();
		String rtnCode = req.getTsmpRtnCode();
		String locale = req.getLocale();
		String encodeRtnMsg = req.getTsmpRtnMsg();
		
		locale = decodeLocale(locale, reqHeaderLocale);
		
		// 0. 檢查使用者是否有登入(Authorization中是否有userName值)，否則拋出 1219 錯誤
		if (StringUtils.isEmpty(userName))
			throw TsmpDpAaRtnCode._1219.throwing();

		//2. 顯示的回覆訊息若為空值，或是未傳入回覆代碼、語言地區則拋出 1296 錯誤。
		if(StringUtils.isEmpty(encodeRtnMsg) || StringUtils.isEmpty(rtnCode)
				|| StringUtils.isEmpty(locale))
			throw TsmpDpAaRtnCode._1296.throwing();
		
		//1. 若依照回覆代碼、語言地區找不到可更新的資料，則拋出 1298 錯誤。	
		qureyApiRtnCodeByPk(rtnCode, locale);
		
	}
	
	public TsmpRtnCode qureyApiRtnCodeByPk(String rtnCode, String locale) {
		TsmpRtnCode tsmpRtnCode = null;
		TsmpRtnCodeId id = new TsmpRtnCodeId(rtnCode,locale);
		Optional<TsmpRtnCode> opt_rtnCode = getTsmpRtnCodeDao().findById(id);
	
		//2.若查不到資料則拋出 1298 錯誤。
		if(!opt_rtnCode.isPresent()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}else {
			tsmpRtnCode = opt_rtnCode.get();
		}
		
		return tsmpRtnCode;
	}
	
	public DPB0099Resp updateTsmpRtnCode(TsmpAuthorization authorization, DPB0099Req req, String reqHeaderLocale){
		DPB0099Resp resp = new DPB0099Resp();
		int cnt = 0;
		String rtnCode = req.getTsmpRtnCode();
		String locale = req.getLocale();
		
		String oldMsg = ServiceUtil.nvl(req.getOldMsg());
		String msg = ServiceUtil.nvl(req.getTsmpRtnMsg());
		String desc =ServiceUtil.nvl(req.getTsmpRtnDesc());
		
		// 深層拷貝<有lv才需要用>
		// TsmpRtnCode newRtnCode = ServiceUtil.deepCopy(tsmpRtnCode, TsmpRtnCode.class);
		
		try {
			
			locale = decodeLocale(locale, reqHeaderLocale);
			cnt = getTsmpRtnCodeDao().update_dpb0099service_01(msg,desc,rtnCode,locale,oldMsg);
			
		} catch (Exception e) {
			if (isValueTooLargeException(e)) {
				throw TsmpDpAaRtnCode._1220.throwing();
			}else {
				this.logger.error(StackTraceUtil.logStackTrace(e));
				throw TsmpDpAaRtnCode._1297.throwing();
			}
		}
		
		return resp;
	}
	
	protected String decodeLocale(String locale, String reqHeaderLocale) {
		if (!StringUtils.isEmpty(locale)) {
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
