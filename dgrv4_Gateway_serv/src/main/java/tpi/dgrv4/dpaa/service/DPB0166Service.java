package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0166Req;
import tpi.dgrv4.dpaa.vo.DPB0166Resp;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoL;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoLDao;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0166Service {
	private TPILogger logger = TPILogger.tl;
	
	private TsmpClientDao tsmpClientDao;
	private DgrGtwIdpInfoLDao dgrGtwIdpInfoLDao;

	@Autowired
	public DPB0166Service(TsmpClientDao tsmpClientDao, DgrGtwIdpInfoLDao dgrGtwIdpInfoLDao) {
		super();
		this.tsmpClientDao = tsmpClientDao;
		this.dgrGtwIdpInfoLDao = dgrGtwIdpInfoLDao;
	}

	public DPB0166Resp createGtwIdPInfo_ldap(TsmpAuthorization authorization, DPB0166Req req) {
		DPB0166Resp resp = new DPB0166Resp();
		try {

			String clientId = req.getClientId();
			checkParams(clientId);
			
			String iconFile = StringUtils.hasLength(req.getIconFile()) ? req.getIconFile() : IdPHelper.DEFULT_ICON_FILE;

			DgrGtwIdpInfoL dgrGtwIdpInfoL = new DgrGtwIdpInfoL();
			dgrGtwIdpInfoL.setClientId(clientId);
			dgrGtwIdpInfoL.setStatus(req.getStatus());
			dgrGtwIdpInfoL.setRemark(req.getRemark());
			dgrGtwIdpInfoL.setLdapUrl(req.getLdapUrl());
			dgrGtwIdpInfoL.setLdapDn(req.getLdapDn());
			dgrGtwIdpInfoL.setLdapTimeout(req.getLdapTimeout());
			dgrGtwIdpInfoL.setIconFile(iconFile);
			dgrGtwIdpInfoL.setPageTitle(req.getPageTitle());
			dgrGtwIdpInfoL.setLdapBaseDn(req.getLdapBaseDn());
			dgrGtwIdpInfoL.setCreateUser(authorization.getUserName());
			dgrGtwIdpInfoL = getDgrGtwIdpInfoLDao().save(dgrGtwIdpInfoL);

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

	protected DgrGtwIdpInfoLDao getDgrGtwIdpInfoLDao() {
		return dgrGtwIdpInfoLDao;
	}
}
