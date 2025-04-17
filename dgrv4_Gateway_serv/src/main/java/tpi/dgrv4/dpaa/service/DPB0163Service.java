package tpi.dgrv4.dpaa.service;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.AuditLogEvent;
import tpi.dgrv4.common.constant.TableAct;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.DPB0163Req;
import tpi.dgrv4.dpaa.vo.DPB0163Resp;
import tpi.dgrv4.entity.entity.Authorities;
import tpi.dgrv4.entity.entity.DgrAcIdpUser;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.DgrAcIdpUserDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.util.InnerInvokeParam;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0163Service {
	private TPILogger logger = TPILogger.tl;
	
	private AuthoritiesDao authoritiesDao;
	private DgrAcIdpUserDao dgrAcIdpUserDao;
	private DgrAuditLogService dgrAuditLogService;
	private TsmpUserDao tsmpUserDao;

	@Autowired
	public DPB0163Service(AuthoritiesDao authoritiesDao, DgrAcIdpUserDao dgrAcIdpUserDao,
			DgrAuditLogService dgrAuditLogService, TsmpUserDao tsmpUserDao) {
		super();
		this.authoritiesDao = authoritiesDao;
		this.dgrAcIdpUserDao = dgrAcIdpUserDao;
		this.dgrAuditLogService = dgrAuditLogService;
		this.tsmpUserDao = tsmpUserDao;
	}

	@Transactional
	public DPB0163Resp createIdPUser(TsmpAuthorization authorization, DPB0163Req req, InnerInvokeParam iip) {
		
		req.switchCusIdpTypeUserName();
		
		DPB0163Resp resp = new DPB0163Resp();
		// 寫入 Audit Log M
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogM(iip, lineNumber, AuditLogEvent.ADD_IDP_USER.value());

		try {
			List<String> roleIdList = req.getRoleIdList();
			String userName = req.getUserName();

			List<DgrAcIdpUser> list = getDgrAcIdpUserDao().findByUserName(userName);
			if (list.size() > 0) {
				userName = ServiceUtil.decodeBase64URL(userName);
				throw TsmpDpAaRtnCode._1353.throwing("{{userName}}", userName);
			}
			
			// 檢查 userName是否與使用者帳號重複
			TsmpUser tsmpUser = getTsmpUserDao().findFirstByUserName(userName);
			if (tsmpUser != null) {
				throw TsmpDpAaRtnCode._1540.throwing();
			}

			if (roleIdList != null && !roleIdList.isEmpty()) {
				for (String roleId : roleIdList) {
					addAuthoritiesTable(authorization, roleId, userName, iip);
				}
			}
			addDgrAcIdpUserTable(authorization, req, iip);
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		return resp;
	}

	private void addAuthoritiesTable(TsmpAuthorization authorization, String roleId, String userName,
			InnerInvokeParam iip) {
		Authorities auth = new Authorities();
		auth.setAuthority(roleId);
		auth.setUsername(userName);
		auth = getAuthoritiesDao().save(auth);
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, Authorities.class.getSimpleName(), TableAct.C.value(),
				null, auth);// C

	}

	private void addDgrAcIdpUserTable(TsmpAuthorization authorization, DPB0163Req req, InnerInvokeParam iip) {
		DgrAcIdpUser dgrAcIdpUser = new DgrAcIdpUser();
		dgrAcIdpUser.setOrgId(req.getOrgId());
		dgrAcIdpUser.setUserAlias(req.getUserAlias());
		dgrAcIdpUser.setUserEmail(req.getUserEmail());
		dgrAcIdpUser.setUserStatus(req.getStatus());
		dgrAcIdpUser.setIdpType(req.getIdpType());
		dgrAcIdpUser.setUserName(req.getUserName());
		dgrAcIdpUser.setCreateUser(authorization.getUserName());
		dgrAcIdpUser = getDgrAcIdpUserDao().save(dgrAcIdpUser);
		// 寫入 Audit Log D
		String lineNumber = StackTraceUtil.getLineNumber();
		getDgrAuditLogService().createAuditLogD(iip, lineNumber, DgrAcIdpUser.class.getSimpleName(), TableAct.C.value(),
				null, dgrAcIdpUser);// C
	}

	protected DgrAcIdpUserDao getDgrAcIdpUserDao() {
		return dgrAcIdpUserDao;
	}

	protected AuthoritiesDao getAuthoritiesDao() { 
		return this.authoritiesDao;
	}

	protected DgrAuditLogService getDgrAuditLogService() {
		return dgrAuditLogService;
	}
	
	protected TsmpUserDao getTsmpUserDao() {
		return tsmpUserDao;
	}
}
