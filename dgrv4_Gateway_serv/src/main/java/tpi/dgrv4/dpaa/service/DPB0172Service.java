package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0172Req;
import tpi.dgrv4.dpaa.vo.DPB0172Resp;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoO;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoODao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0172Service {

	private DgrGtwIdpInfoODao dgrGtwIdpInfoODao;
	private TsmpClientDao tsmpClientDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0172Service(DgrGtwIdpInfoODao dgrGtwIdpInfoODao, TsmpClientDao tsmpClientDao) {
		super();
		this.dgrGtwIdpInfoODao = dgrGtwIdpInfoODao;
		this.tsmpClientDao = tsmpClientDao;
	}

	public DPB0172Resp updateGtwIdPInfo_oauth2(TsmpAuthorization authorization, DPB0172Req req) {
		DPB0172Resp resp = new DPB0172Resp();
		try {
			String id = req.getId();
			String clientId = req.getClientId();
			checkParams(id, clientId);
			
			Long longId = RandomSeqLongUtil.toLongValue(id);
			
			DgrGtwIdpInfoO dgrGtwIdpInfoO = getDgrGtwIdpInfoODao().findById(longId).orElse(null);
			if (dgrGtwIdpInfoO == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
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
			
			dgrGtwIdpInfoO.setUpdateDateTime(DateTimeUtil.now());
			dgrGtwIdpInfoO.setUpdateUser(authorization.getUserName());
			dgrGtwIdpInfoO = getDgrGtwIdpInfoODao().saveAndFlush(dgrGtwIdpInfoO);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1286.throwing();
		}
		return resp;
	}

	private void checkParams(String id, String clientId) {
		if (!StringUtils.hasLength(id)) {
			throw TsmpDpAaRtnCode._2025.throwing("id");
		}

		if (!StringUtils.hasLength(clientId)) {
			throw TsmpDpAaRtnCode._2025.throwing("clientId");
		}
		
		TsmpClient client = getTsmpClientDao().findById(clientId).orElse(null);
		if (client == null) {
			throw TsmpDpAaRtnCode._1344.throwing();
		}
	}

	protected DgrGtwIdpInfoODao getDgrGtwIdpInfoODao() {
		return dgrGtwIdpInfoODao;
	}
	
	protected TsmpClientDao getTsmpClientDao() {
		return tsmpClientDao;
	}
}
