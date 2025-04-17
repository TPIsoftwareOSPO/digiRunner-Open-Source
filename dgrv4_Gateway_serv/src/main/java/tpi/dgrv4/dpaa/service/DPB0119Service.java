package tpi.dgrv4.dpaa.service;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.vo.DPB0119Resp;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0119Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpSettingDao tsmpSettingDao;
	private ServiceConfig serviceConfig;

	@Autowired
	public DPB0119Service(TsmpSettingDao tsmpSettingDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpSettingDao = tsmpSettingDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB0119Resp queryCusUrl(HttpServletRequest httpReq, TsmpAuthorization authorization) {
		DPB0119Resp resp = new DPB0119Resp();
		try {
			Optional<TsmpSetting> tsmpSetting_cusModuleExist = getTsmpSettingDao()
					.findById(TsmpSettingDao.Key.CUS_MODULE_EXIST);
			if (tsmpSetting_cusModuleExist.isPresent() && "true".equals(tsmpSetting_cusModuleExist.get().getValue())) {

				Optional<TsmpSetting> tsmpSetting_cusModuleName1 = getTsmpSettingDao()
						.findById(TsmpSettingDao.Key.CUS_MODULE_NAME1);

				if (tsmpSetting_cusModuleName1.isPresent()) {

					// 取得客製包 Moudle 名稱
					String cusModuleName1 = tsmpSetting_cusModuleName1.get().getValue();

					// 取得客製包URL
					String cusUrl = getUrl(httpReq, cusModuleName1);

					resp.setCusUrl(cusUrl);

				} else {
					throw TsmpDpAaRtnCode._1298.throwing();
				}

			} else {
				throw TsmpDpAaRtnCode._1492.throwing();
			}
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	/**
	 * 
	 * 取得客製包URL
	 * 
	 * @param req
	 * @param cusModuleName1
	 * @return
	 * @throws Exception
	 */
	private String getUrl(HttpServletRequest req, String cusModuleName1) throws Exception {
		// 例如: dgr-cus-etb_cg/11/
		String url = cusModuleName1 + "/11";
		return url;
	}

	protected String getScheme(HttpServletRequest req) {
		return req.getScheme();
	}

	protected TsmpSettingDao getTsmpSettingDao() {
		return tsmpSettingDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

}
