package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0150Req;
import tpi.dgrv4.dpaa.vo.DPB0150Resp;
import tpi.dgrv4.entity.entity.DgrAcIdpInfo;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0150Service {
	private DgrAcIdpInfoDao dgrAcIdpInfoDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0150Service(DgrAcIdpInfoDao dgrAcIdpInfoDao) {
		super();
		this.dgrAcIdpInfoDao = dgrAcIdpInfoDao;
	}

	public DPB0150Resp createIdPInfo(TsmpAuthorization auth, DPB0150Req req) {
		DPB0150Resp resp = new DPB0150Resp();
		try {
			checkParams(req);
			
			DgrAcIdpInfo dgrAcIdpInfo = new DgrAcIdpInfo();
			dgrAcIdpInfo.setAuthUrl(req.getAuthUrl());
			dgrAcIdpInfo.setCallbackUrl(req.getCallbackUrl());
			dgrAcIdpInfo.setClientId(req.getClientId());
			dgrAcIdpInfo.setClientMima(req.getClientMima());
			dgrAcIdpInfo.setClientName(req.getClientName());
			dgrAcIdpInfo.setClientStatus(req.getClientStatus());
			dgrAcIdpInfo.setIdpType(req.getIdpType());
			dgrAcIdpInfo.setWellKnownUrl(req.getIdpWellKnownUrl());
			dgrAcIdpInfo.setAccessTokenUrl(req.getAccessTokenUrl());
			dgrAcIdpInfo.setScope(req.getScope());
			
			dgrAcIdpInfo.setCreateDateTime(DateTimeUtil.now());
			dgrAcIdpInfo.setCreateUser(auth.getUserName());
			dgrAcIdpInfo = getDgrAcIdpInfoDao().save(dgrAcIdpInfo);
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		return resp;
	}

	protected void checkParams(DPB0150Req req) {
		String clientId = req.getClientId();
		String idpType = req.getIdpType();

		// 檢查 idpType 和 clientId 在 DGR_AC_IDP_INFO 中是否存在
		DgrAcIdpInfo dgrAcIdpInfo = getDgrAcIdpInfoDao().findFirstByIdpTypeAndClientId(idpType, clientId);
		if (dgrAcIdpInfo != null) {
			// [idpType, clientId] 已存在: GOOGLE, 254605600767-jukgqb0fequprvv53dhut1257fbpoe5f.apps.googleusercontent.com
			throw TsmpDpAaRtnCode._1353.throwing("idpType, clientId", idpType + ", " + clientId);
		}
	}

	protected DgrAcIdpInfoDao getDgrAcIdpInfoDao() {
		return dgrAcIdpInfoDao;
	}
}
