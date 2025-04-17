package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0171Req;
import tpi.dgrv4.dpaa.vo.DPB0171Resp;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoO;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoODao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0171Service {
	private TsmpClientDao tsmpClientDao;
	private DgrGtwIdpInfoODao dgrGtwIdpInfoODao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0171Service(TsmpClientDao tsmpClientDao, DgrGtwIdpInfoODao dgrGtwIdpInfoODao) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.dgrGtwIdpInfoODao = dgrGtwIdpInfoODao;
	}

	public DPB0171Resp createGtwIdPInfo_oauth2(TsmpAuthorization authorization, DPB0171Req req) {
		DPB0171Resp resp = new DPB0171Resp();
		try {
			String clientId = req.getClientId();
			checkParams(clientId);
			
			DgrGtwIdpInfoO dgrGtwIdpInfoO = new DgrGtwIdpInfoO();
			dgrGtwIdpInfoO.setClientId(clientId);
			dgrGtwIdpInfoO.setIdpType(req.getIdpType());
			dgrGtwIdpInfoO.setStatus(req.getStatus());
			dgrGtwIdpInfoO.setRemark(req.getRemark());
			dgrGtwIdpInfoO.setIdpClientId(req.getIdpClientId());
			dgrGtwIdpInfoO.setIdpClientMima(req.getIdpClientMima());
			dgrGtwIdpInfoO.setIdpClientName(req.getIdpClientName());
			dgrGtwIdpInfoO.setWellKnownUrl(req.getWellKnownUrl());
			dgrGtwIdpInfoO.setCallbackUrl(req.getCallbackUrl());
			dgrGtwIdpInfoO.setAuthUrl(req.getAuthUrl());
			dgrGtwIdpInfoO.setAccessTokenUrl(req.getAccessTokenUrl());
			dgrGtwIdpInfoO.setScope(req.getScope());
			
			dgrGtwIdpInfoO.setCreateDateTime(DateTimeUtil.now());
			dgrGtwIdpInfoO.setCreateUser(authorization.getUserName());
			dgrGtwIdpInfoO = getDgrGtwIdpInfoODao().save(dgrGtwIdpInfoO);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		return resp;
	}
	
	private void checkParams(String clientId) {
		if (!StringUtils.hasLength(clientId)) {
			throw TsmpDpAaRtnCode._2025.throwing("clientId");
		}
		
		TsmpClient client = getTsmpClientDao().findById(clientId).orElse(null);
		if (client == null) {
			throw TsmpDpAaRtnCode._1344.throwing();
		}
	}

	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}

	protected DgrGtwIdpInfoODao getDgrGtwIdpInfoODao() {
		return dgrGtwIdpInfoODao;
	}
}
