package tpi.dgrv4.dpaa.service;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.vo.DPB0120Resp;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0120Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpSettingService tsmpSettingService;

	@Autowired
	public DPB0120Service(TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpSettingService = tsmpSettingService;
	}

	public DPB0120Resp queryCusEnable(HttpServletRequest httpReq, TsmpAuthorization authorization) {
		DPB0120Resp resp = new DPB0120Resp();
		try {

			try {
				
				// 是否有客製包存在
				boolean cus_module_exist = getTsmpSettingService().getVal_CUS_MODULE_EXIST();
				
				boolean cus_func_enable = getTsmpSettingService().getVal_CUS_FUNC_ENABLE(1);

				if (cus_module_exist && cus_func_enable) {
					resp.setIsCusEnable("Y");
				} else {
					resp.setIsCusEnable("N");
				}

			} catch (TsmpDpAaException e) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected TsmpSettingService getTsmpSettingService() {
		return tsmpSettingService;
	}
}
