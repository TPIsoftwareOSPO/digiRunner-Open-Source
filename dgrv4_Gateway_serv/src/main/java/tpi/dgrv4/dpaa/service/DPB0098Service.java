package tpi.dgrv4.dpaa.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0098Req;
import tpi.dgrv4.dpaa.vo.DPB0098Resp;
import tpi.dgrv4.entity.entity.TsmpRtnCode;
import tpi.dgrv4.entity.entity.TsmpRtnCodeId;
import tpi.dgrv4.entity.repository.TsmpRtnCodeDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0098Service {

	private TsmpRtnCodeDao tsmpRtnCodeDao;
	private ServiceConfig serviceConfig;
	
	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0098Service(TsmpRtnCodeDao tsmpRtnCodeDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpRtnCodeDao = tsmpRtnCodeDao;
		this.serviceConfig = serviceConfig;
	}



	/**
	 * 依照回覆代碼、語言地區查詢API回覆代碼明細，以帶出更新視窗所需的資料。				
	 * 在查詢結果中的欄位按下「顯示更多」，以帶出完整的訊息內容。				
	 * 顯示的回覆訊息、說明不需再截斷，顯示完整內容				
	 * 1.若未帶入回覆代碼或語言地區，則拋出 1296 錯誤。				
	 * 2.若查不到資料則拋出 1298 錯誤。							
	 * 
	 * @param tsmpAuthorization
	 * @param req
	 * @return
	 */
	public DPB0098Resp qureyApiRtnCodeByPk(TsmpAuthorization tsmpAuthorization, DPB0098Req req) {
		DPB0098Resp resp = new DPB0098Resp();
		
		try {
			String rtnCode = req.getTsmpRtnCode();
			String locale = req.getLocale();
			
			//1.若未帶入回覆代碼或語言地區，則拋出 1296 錯誤
			if (StringUtils.isEmpty(rtnCode) ){
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			if (StringUtils.isEmpty(locale)){
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			
			resp = qureyApiRtnCodeByPk(rtnCode, locale);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	
	
	public DPB0098Resp qureyApiRtnCodeByPk(String rtnCode, String locale) throws Exception {
		DPB0098Resp resp = new DPB0098Resp();
		TsmpRtnCode tsmpRtnCode = null;
		TsmpRtnCodeId id = new TsmpRtnCodeId(rtnCode,locale);
		Optional<TsmpRtnCode> opt_rtnCode = getTsmpRtnCodeDao().findById(id);
	
		//2.若查不到資料則拋出 1298 錯誤。
		if(!opt_rtnCode.isPresent()) {
			throw TsmpDpAaRtnCode._1298.throwing();
		}else {
			tsmpRtnCode = opt_rtnCode.get();
			resp.setTsmpRtnCode(tsmpRtnCode.getTsmpRtnCode());
			resp.setLocale(tsmpRtnCode.getLocale());
			resp.setTsmpRtnMsg(tsmpRtnCode.getTsmpRtnMsg());
			resp.setTsmpRtnDesc(ServiceUtil.nvl(tsmpRtnCode.getTsmpRtnDesc()));
		}
		
		return resp;
	}
	
	protected TsmpRtnCodeDao getTsmpRtnCodeDao() {
		return this.tsmpRtnCodeDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

}
