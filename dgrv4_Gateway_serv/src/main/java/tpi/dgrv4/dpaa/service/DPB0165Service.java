package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0165Req;
import tpi.dgrv4.dpaa.vo.DPB0165Resp;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoL;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoLDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0165Service {

	private DgrGtwIdpInfoLDao dgrGtwIdpInfoLDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0165Service(DgrGtwIdpInfoLDao dgrGtwIdpInfoLDao) {
		super();
		this.dgrGtwIdpInfoLDao = dgrGtwIdpInfoLDao;
	}

	public DPB0165Resp queryGtwIdPInfoDetail_ldap(TsmpAuthorization authorization, DPB0165Req req) {
		DPB0165Resp resp = new DPB0165Resp();
		try {

			chekParm(req);
			String id = req.getId();
			Long longId = RandomSeqLongUtil.toLongValue(id);
			DgrGtwIdpInfoL gtwIdpInfoL = getDgrGtwIdpInfoLDao().findById(longId).orElse(null);
			if (gtwIdpInfoL == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			resp.setId(id);
			resp.setLongId(String.valueOf(longId));
			resp.setClientId(gtwIdpInfoL.getClientId());
			resp.setStatus(gtwIdpInfoL.getStatus());
			resp.setRemark(gtwIdpInfoL.getRemark());
			resp.setLdapUrl(gtwIdpInfoL.getLdapUrl());
			resp.setLdapDn(gtwIdpInfoL.getLdapDn());
			resp.setLdapTimeout(gtwIdpInfoL.getLdapTimeout());
			resp.setIconFile(gtwIdpInfoL.getIconFile());
			resp.setPageTitle(gtwIdpInfoL.getPageTitle());
			resp.setCreateDateTime(gtwIdpInfoL.getCreateDateTime());
			resp.setCreateUser(gtwIdpInfoL.getCreateUser());
			resp.setUpdateDateTime(gtwIdpInfoL.getUpdateDateTime());
			resp.setUpdateUser(gtwIdpInfoL.getUpdateUser());
			resp.setLdapBaseDn(gtwIdpInfoL.getLdapBaseDn());
			

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private void chekParm(DPB0165Req req) {
		if (!StringUtils.hasLength(req.getId())) {
			throw TsmpDpAaRtnCode._2025.throwing("id");
		}
	}

	protected DgrGtwIdpInfoLDao getDgrGtwIdpInfoLDao() {
		return dgrGtwIdpInfoLDao;
	}
}
