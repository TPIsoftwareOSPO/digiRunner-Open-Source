package tpi.dgrv4.dpaa.service;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0183Req;
import tpi.dgrv4.dpaa.vo.DPB0183Resp;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoMLdapM;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoMLdapDDao;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoMLdapMDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0183Service {
	private TPILogger logger = TPILogger.tl;
	
	private DgrAcIdpInfoMLdapDDao dgrAcIdpInfoMLdapDDao;
	private DgrAcIdpInfoMLdapMDao dgrAcIdpInfoMLdapMDao;

	@Autowired
	public DPB0183Service(DgrAcIdpInfoMLdapDDao dgrAcIdpInfoMLdapDDao, DgrAcIdpInfoMLdapMDao dgrAcIdpInfoMLdapMDao) {
		super();
		this.dgrAcIdpInfoMLdapDDao = dgrAcIdpInfoMLdapDDao;
		this.dgrAcIdpInfoMLdapMDao = dgrAcIdpInfoMLdapMDao;
	}

	@Transactional
	public DPB0183Resp deleteIdPInfo_mldap(TsmpAuthorization authorization, DPB0183Req req) {
		DPB0183Resp resp = new DPB0183Resp();
		try {
			String masterId = req.getMasterId();
			if (!StringUtils.hasLength(masterId)) {
				throw TsmpDpAaRtnCode._2025.throwing("masterId");
			}

			long masterLongId = RandomSeqLongUtil.toLongValue(masterId);
			DgrAcIdpInfoMLdapM ldapM = getDgrAcIdpInfoMLdapMDao().findById(masterLongId).orElse(null);
			if (ldapM == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			} else {
				getDgrAcIdpInfoMLdapMDao().delete(ldapM);
				getDgrAcIdpInfoMLdapDDao().deleteByRefAcIdpInfoMLdapMId(masterLongId);
			}

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();
		}

		return resp;
	}

	protected DgrAcIdpInfoMLdapDDao getDgrAcIdpInfoMLdapDDao() {
		return dgrAcIdpInfoMLdapDDao;
	}

	protected DgrAcIdpInfoMLdapMDao getDgrAcIdpInfoMLdapMDao() {
		return dgrAcIdpInfoMLdapMDao;
	}
}
