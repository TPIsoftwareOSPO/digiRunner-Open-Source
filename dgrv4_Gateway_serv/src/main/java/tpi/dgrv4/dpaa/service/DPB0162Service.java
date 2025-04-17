package tpi.dgrv4.dpaa.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0162Req;
import tpi.dgrv4.dpaa.vo.DPB0162Resp;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoLdap;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoLdapDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0162Service {
	private DgrAcIdpInfoLdapDao dgrAcIdpInfoLdapDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0162Service(DgrAcIdpInfoLdapDao dgrAcIdpInfoLdapDao) {
		super();
		this.dgrAcIdpInfoLdapDao = dgrAcIdpInfoLdapDao;
	}
	public DPB0162Resp deleteIdPInfo_ldap(TsmpAuthorization authorization, DPB0162Req req) {
		DPB0162Resp resp = new DPB0162Resp();
		try {
			cheakParm(req);
			String id = req.getId();
			Long idToLong = RandomSeqLongUtil.toLongValue(id);
			DgrAcIdpInfoLdap dgrAcIdpInfoLdap = getDgrAcIdpInfoLdapDao().findById(idToLong).orElse(null);
			if (dgrAcIdpInfoLdap == null)
				throw TsmpDpAaRtnCode._1298.throwing();
			getDgrAcIdpInfoLdapDao().delete(dgrAcIdpInfoLdap);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();
		}
		return resp;
	}
	private void cheakParm(DPB0162Req req) {
		if(StringUtils.isBlank(req.getId()) )
			throw TsmpDpAaRtnCode._2025.throwing("id");
	}
	protected DgrAcIdpInfoLdapDao getDgrAcIdpInfoLdapDao() {
		return dgrAcIdpInfoLdapDao;
	}
}
