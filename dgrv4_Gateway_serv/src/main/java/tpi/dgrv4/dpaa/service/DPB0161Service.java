package tpi.dgrv4.dpaa.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0161Req;
import tpi.dgrv4.dpaa.vo.DPB0161Resp;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoLdap;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoLdapDao;
import tpi.dgrv4.gateway.component.IdPHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0161Service {

	private DgrAcIdpInfoLdapDao dgrAcIdpInfoLdapDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0161Service(DgrAcIdpInfoLdapDao dgrAcIdpInfoLdapDao) {
		super();
		this.dgrAcIdpInfoLdapDao = dgrAcIdpInfoLdapDao;
	}
	public DPB0161Resp updateIdPInfo_ldap(TsmpAuthorization authorization, DPB0161Req req) {
		DPB0161Resp resp = new DPB0161Resp();
		try {
			cheakParm(req);
			String id = req.getId();
		

			Long idToLong = RandomSeqLongUtil.toLongValue(id);
			DgrAcIdpInfoLdap dgrAcIdpInfoLdap = getDgrAcIdpInfoLdapDao().findById(idToLong).orElse(null);
			if (dgrAcIdpInfoLdap == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			dgrAcIdpInfoLdap.setApprovalResultMail(req.getApprovalResultMail());
			String iconFile = "";
			if (StringUtils.isBlank(req.getIconFile())) {
				iconFile = IdPHelper.DEFULT_ICON_FILE;
			} else {
				iconFile = req.getIconFile();
			}
			dgrAcIdpInfoLdap.setIconFile(iconFile);
			dgrAcIdpInfoLdap.setLdapBaseDn(req.getLdapBaseDn());
			dgrAcIdpInfoLdap.setLdapDn(req.getLdapDn());
			dgrAcIdpInfoLdap.setLdapStatus(req.getLdapStatus());
			dgrAcIdpInfoLdap.setLdapTimeout(req.getLdapTimeout());
			dgrAcIdpInfoLdap.setLdapUrl(req.getLdapUrl());
			dgrAcIdpInfoLdap.setUpdateDateTime(DateTimeUtil.now());
			dgrAcIdpInfoLdap.setUpdateUser(authorization.getUserName());
			dgrAcIdpInfoLdap.setPageTitle(req.getPageTitle());
			dgrAcIdpInfoLdap = getDgrAcIdpInfoLdapDao().save(dgrAcIdpInfoLdap);
			
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1286.throwing();
		}

		return resp;
	}
	private void cheakParm(DPB0161Req req) {
		if(StringUtils.isBlank(req.getId()) )
			throw TsmpDpAaRtnCode._2025.throwing("id");
	}
	protected DgrAcIdpInfoLdapDao getDgrAcIdpInfoLdapDao() {
		return dgrAcIdpInfoLdapDao;
	}
}
