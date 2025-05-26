package tpi.dgrv4.dpaa.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.BcryptFieldValueEnum;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.BcryptParamDecodeException;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.es.DgrESService;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0124Req;
import tpi.dgrv4.dpaa.vo.DPB0124Resp;
import tpi.dgrv4.entity.daoService.BcryptParamHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0124Service {

	private TPILogger logger = TPILogger.tl;

	private DgrESService dgrESService;
	private BcryptParamHelper bcryptParamHelper;

	@Autowired
	public DPB0124Service(DgrESService dgrESService, BcryptParamHelper bcryptParamHelper) {
		super();
		this.dgrESService = dgrESService;
		this.bcryptParamHelper = bcryptParamHelper;
	}

	public DPB0124Resp updateIndexOpenOrClose(TsmpAuthorization auth, DPB0124Req req, ReqHeader reqHeader) {
		String locale = ServiceUtil.getLocale(reqHeader.getLocale());
		boolean isOpen = checkParams(locale, req);
		boolean isConnected = getDgrESService().isConnected();
		if (isConnected) {

			List<String> indexList = req.getIndexList();
			for (String indexName : indexList) {
				// 呼叫外部 API
				callApi(indexName, isOpen);
			}
		}
		return new DPB0124Resp();
	}

	public boolean checkParams(String locale, DPB0124Req req) {
		try {
			String isOpen = req.getIsOpen();
			isOpen = getBcryptParamHelper().decode(isOpen, "ES_INDEX_FLAG", BcryptFieldValueEnum.PARAM1, locale);
			if (!(isOpen.equals("0") || isOpen.equals("1"))) {
				throw TsmpDpAaRtnCode._1296.throwing();
			}
			return isOpen.equals("1");	// 1=啟用, 0=關閉
		} catch (BcryptParamDecodeException e) {
			this.logger.error(e.getMessage());
			throw TsmpDpAaRtnCode._1299.throwing();
		}
	}

	public void callApi(String indexName, boolean isOpen) {
		try {
			if (isOpen) {
				getDgrESService().openIndex(indexName);
			} else {
				getDgrESService().closeIndex(indexName);
			}

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();

		}
	}

	protected DgrESService  getDgrESService() {
		return dgrESService;
	}


	protected BcryptParamHelper getBcryptParamHelper() {
		return this.bcryptParamHelper;
	}

}