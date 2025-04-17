package tpi.dgrv4.dpaa.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0160Req;
import tpi.dgrv4.dpaa.vo.DPB0160Resp;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoLdap;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoLdapDao;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0160Service {
	private DgrAcIdpInfoLdapDao dgrAcIdpInfoLdapDao;
	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0160Service(DgrAcIdpInfoLdapDao dgrAcIdpInfoLdapDao) {
		super();
		this.dgrAcIdpInfoLdapDao = dgrAcIdpInfoLdapDao;
	}
	public DPB0160Resp createIdPInfo_ldap(TsmpAuthorization authorization, DPB0160Req req) {
		DPB0160Resp resp =new DPB0160Resp();
		DgrAcIdpInfoLdap ldapinfo= new DgrAcIdpInfoLdap();
		try {
			ldapinfo.setApprovalResultMail(req.getApprovalResultMail());
			ldapinfo.setCreateUser(authorization.getUserName());
			String iconFile = "";
			if (StringUtils.isBlank(req.getIconFile())) {
				iconFile = IdPHelper.DEFULT_ICON_FILE;
			} else {
				iconFile = req.getIconFile();
			}
			ldapinfo.setIconFile(iconFile);
			ldapinfo.setLdapBaseDn(req.getLdapBaseDn());
			ldapinfo.setLdapDn(req.getLdapDn());
			ldapinfo.setLdapStatus(req.getLdapStatus());
			ldapinfo.setLdapTimeout(req.getLdapTimeout());
			ldapinfo.setLdapUrl(req.getLdapUrl());
			ldapinfo.setPageTitle(req.getPageTitle());
			ldapinfo = getDgrAcIdpInfoLdapDao().save(ldapinfo);
		}  catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1288.throwing();
		}
		return resp;
	}
	protected DgrAcIdpInfoLdapDao getDgrAcIdpInfoLdapDao() {
		return dgrAcIdpInfoLdapDao;
	}
}
