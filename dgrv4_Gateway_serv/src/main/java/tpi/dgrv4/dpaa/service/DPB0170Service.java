package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.vo.DPB0170Req;
import tpi.dgrv4.dpaa.vo.DPB0170Resp;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoO;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoODao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0170Service {

	private DgrGtwIdpInfoODao dgrGtwIdpInfoODao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0170Service(DgrGtwIdpInfoODao dgrGtwIdpInfoODao) {
		super();
		this.dgrGtwIdpInfoODao = dgrGtwIdpInfoODao;
	}

	public DPB0170Resp queryGtwIdPInfoDetail_oauth2(TsmpAuthorization authorization, DPB0170Req req, ReqHeader reqHeader) {
		DPB0170Resp resp = new DPB0170Resp();
		try {
			String id = req.getId();
			checkParams(id);

			Long longId = RandomSeqLongUtil.toLongValue(id);
			DgrGtwIdpInfoO gtwIdpInfoO = getDgrGtwIdpInfoODao().findById(longId).orElse(null);
			if (gtwIdpInfoO == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			resp.setId(id);
			resp.setLongId(String.valueOf(longId));
			resp.setClientId(gtwIdpInfoO.getClientId());
			resp.setIdpType(gtwIdpInfoO.getIdpType());
			resp.setStatus(gtwIdpInfoO.getStatus());
			resp.setRemark(gtwIdpInfoO.getRemark());
			resp.setIdpClientId(gtwIdpInfoO.getIdpClientId());
			resp.setIdpClientMima(gtwIdpInfoO.getIdpClientMima());
			resp.setIdpClientName(gtwIdpInfoO.getIdpClientName());
			resp.setWellKnownUrl(gtwIdpInfoO.getWellKnownUrl());
			resp.setCallbackUrl(gtwIdpInfoO.getCallbackUrl());
			resp.setAuthUrl(gtwIdpInfoO.getAuthUrl());
			resp.setAccessTokenUrl(gtwIdpInfoO.getAccessTokenUrl());
			resp.setScope(gtwIdpInfoO.getScope());
			resp.setCreateDateTime(gtwIdpInfoO.getCreateDateTime());
			resp.setCreateUser(gtwIdpInfoO.getCreateUser());
			resp.setUpdateDateTime(gtwIdpInfoO.getUpdateDateTime());
			resp.setUpdateUser(gtwIdpInfoO.getUpdateUser());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private void checkParams(String id) {
		if (!StringUtils.hasLength(id)) {
			throw TsmpDpAaRtnCode._2025.throwing("id");
		}
	}

	protected DgrGtwIdpInfoODao getDgrGtwIdpInfoODao() {
		return dgrGtwIdpInfoODao;
	}
}
