package tpi.dgrv4.dpaa.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0151Req;
import tpi.dgrv4.dpaa.vo.DPB0151Resp;
import tpi.dgrv4.entity.entity.DgrAcIdpInfo;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0151Service {
	private DgrAcIdpInfoDao dgrAcIdpInfoDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0151Service(DgrAcIdpInfoDao dgrAcIdpInfoDao) {
		super();
		this.dgrAcIdpInfoDao = dgrAcIdpInfoDao;
	}

	public DPB0151Resp updateDgrAcIdpInfo(TsmpAuthorization auth, DPB0151Req req) {
		DPB0151Resp resp = new DPB0151Resp();
		try {

			checkParams(req);
			
			String id = req.getId();
			Long idToLong = RandomSeqLongUtil.toLongValue(id);
			DgrAcIdpInfo dgrAcIdpInfo = getDgrAcIdpInfoDao().findById(idToLong).orElse(null);
			if (dgrAcIdpInfo == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			} else {
				String idpType = req.getIdpType();
				String clientId = req.getClientId();
				String clientName = req.getClientName();
				String clientMima = req.getClientMima();
				String clientStatus = req.getClientStatus();
				String callbackUrl = req.getCallbackUrl();
				String idpWellKnownUrl = req.getIdpWellKnownUrl();
				String authUrl = req.getAuthUrl();
				String tokenUrl = req.getAccessTokenUrl();
				String scope = req.getScope();

				dgrAcIdpInfo.setIdpType(idpType);
				dgrAcIdpInfo.setClientId(clientId);
				dgrAcIdpInfo.setClientName(clientName);
				dgrAcIdpInfo.setClientMima(clientMima);
				dgrAcIdpInfo.setClientStatus(clientStatus);
				dgrAcIdpInfo.setCallbackUrl(callbackUrl);
				dgrAcIdpInfo.setWellKnownUrl(idpWellKnownUrl);
				dgrAcIdpInfo.setAuthUrl(authUrl);
				dgrAcIdpInfo.setAccessTokenUrl(tokenUrl);
				dgrAcIdpInfo.setScope(scope);

				dgrAcIdpInfo.setUpdateDateTime(DateTimeUtil.now());
				dgrAcIdpInfo.setUpdateUser(auth.getUserName());
				dgrAcIdpInfo = getDgrAcIdpInfoDao().save(dgrAcIdpInfo);
			}
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1286.throwing();
		}
		return resp;
	}

	protected void checkParams(DPB0151Req req) {
		String id = req.getId();
		long idToLong = RandomSeqLongUtil.toLongValue(id);
		String idpType = req.getIdpType();
		String clientId = req.getClientId();
		
		if (StringUtils.isBlank(id)) {
			throw TsmpDpAaRtnCode._2025.throwing("id");
		}
		
		// 檢查 idpType 和 clientId 在 DGR_AC_IDP_INFO 中是否存在
		DgrAcIdpInfo dgrAcIdpInfo = getDgrAcIdpInfoDao().findFirstByIdpTypeAndClientId(idpType, clientId);
		if (dgrAcIdpInfo != null) {
			long acIdpInfoId = dgrAcIdpInfo.getAcIdpInfoId();
			if (acIdpInfoId != idToLong) {
				// [idpType, clientId] 已存在: GOOGLE, 254605600767-jukgqb0fequprvv53dhut1257fbpoe5f.apps.googleusercontent.com
				throw TsmpDpAaRtnCode._1353.throwing("idpType, clientId", idpType + ", " + clientId);
			}
		}
	}

	protected DgrAcIdpInfoDao getDgrAcIdpInfoDao() {
		return dgrAcIdpInfoDao;
	}
}
