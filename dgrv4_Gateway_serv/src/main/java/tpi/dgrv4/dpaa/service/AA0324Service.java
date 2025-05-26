package tpi.dgrv4.dpaa.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.AA0324Req;
import tpi.dgrv4.dpaa.vo.AA0324Resp;
import tpi.dgrv4.entity.entity.TsmpSetting;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.component.cache.proxy.TsmpSettingCacheProxy;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.CApiKeyService;

@Service
public class AA0324Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpSettingCacheProxy tsmpSettingCacheProxy;
	private CApiKeyService capiKeyService;

	@Autowired
	public AA0324Service(TsmpSettingCacheProxy tsmpSettingCacheProxy, CApiKeyService capiKeyService) {
		super();
		this.tsmpSettingCacheProxy = tsmpSettingCacheProxy;
		this.capiKeyService = capiKeyService;
	}

	public AA0324Resp getESLogDisable(AA0324Req req, ReqHeader reqHeader,
			HttpHeaders headers) {
		AA0324Resp resp = new AA0324Resp();

		// 驗證CApiKey
		getCapiKeyService().verifyCApiKey(headers, true, true);

		try {

			Optional<TsmpSetting> opt_tsmpSetting = getTsmpSettingCacheProxy()
					.findById(TsmpSettingDao.Key.ES_LOG_DISABLE);

			if (opt_tsmpSetting.isPresent()) {
				String esLogDisable = opt_tsmpSetting.get().getValue();
				resp.setEsLogDisable(esLogDisable);
			} else {
				resp.setEsLogDisable("false");
			}

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return resp;
	}

	protected TsmpSettingCacheProxy getTsmpSettingCacheProxy() {
		return tsmpSettingCacheProxy;
	}
	
	protected CApiKeyService getCapiKeyService() {
		return capiKeyService;
	}

}
