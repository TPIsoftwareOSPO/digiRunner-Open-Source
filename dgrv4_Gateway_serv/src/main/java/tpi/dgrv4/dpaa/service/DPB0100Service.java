package tpi.dgrv4.dpaa.service;

import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0100Req;
import tpi.dgrv4.dpaa.vo.DPB0100Resp;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.entity.TsmpRtnCodeId;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0100Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpRtnCodeDao tsmpRtnCodeDao;
	
	@Autowired
	public DPB0100Service(TsmpRtnCodeDao tsmpRtnCodeDao) {
		super();
		this.tsmpRtnCodeDao = tsmpRtnCodeDao;
	}
	public DPB0100Resp deleteApiRtnCode(TsmpAuthorization tsmpAuthorization, DPB0100Req req) {
		DPB0100Resp resp = new DPB0100Resp();
		try {

			checkParmas(tsmpAuthorization, req);

			resp = deleteTsmpRtnCode(tsmpAuthorization, req);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}
	/**
	 * 0. 檢查使用者是否有登入(Authorization中是否有userName值)，否則拋出 1219 錯誤
	 * 1. 未傳入回覆代碼、語言地區則拋出 1296 錯誤。	
	 * 2. 若依照回覆代碼、語言地區找不到可刪除的資料，則拋出 1298 錯誤。
	 * 3. 其他刪除發生的錯誤，則拋出 1297 錯誤。
	 * 
	 * @param authorization
	 * @param req
	 */
	public void checkParmas(TsmpAuthorization authorization, DPB0100Req req)  throws Exception{
		String userName = authorization.getUserName();
		String rtnCode = req.getTsmpRtnCode();
		String locale = req.getLocale();
		
		// 0. 檢查使用者是否有登入(Authorization中是否有userName值)，否則拋出 1219 錯誤
		if (StringUtils.isEmpty(userName))
			throw TsmpDpAaRtnCode._1219.throwing();

		//1.未傳入回覆代碼、語言地區則拋出 1296 錯誤
		if(StringUtils.isEmpty(rtnCode)	|| StringUtils.isEmpty(locale))
			throw TsmpDpAaRtnCode._1296.throwing();
		
		//2. 若依照回覆代碼、語言地區找不到可更新的資料，則拋出 1298 錯誤。
		qureyApiRtnCodeByPk(rtnCode, locale);
		
	}
	
	@Transactional
	public DPB0100Resp deleteTsmpRtnCode(TsmpAuthorization tsmpAuthorization, DPB0100Req req) {
		DPB0100Resp resp = new DPB0100Resp();
		String rtnCode = req.getTsmpRtnCode();
		String locale = req.getLocale();
		
		TsmpRtnCode tsmpRtnCode = qureyApiRtnCodeByPk(rtnCode, locale);
		
		TsmpRtnCodeId id = new TsmpRtnCodeId(rtnCode,locale);
		getTsmpRtnCodeDao().deleteById(id);
		
		resp.setTsmpRtnCode(rtnCode);
		resp.setLocale(locale);
		resp.setTsmpRtnMsg(tsmpRtnCode.getTsmpRtnMsg());
		resp.setTsmpRtnDesc(tsmpRtnCode.getTsmpRtnDesc());
		
		return resp;
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
	
	protected TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return this.tsmpRtnCodeDao;
	}
	
}
