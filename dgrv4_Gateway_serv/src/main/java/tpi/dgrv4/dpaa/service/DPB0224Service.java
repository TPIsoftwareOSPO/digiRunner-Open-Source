package tpi.dgrv4.dpaa.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0224Req;
import tpi.dgrv4.dpaa.vo.DPB0224Resp;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoCus;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoCusDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0224Service {

	private DgrAcIdpInfoCusDao dgrAcIdpInfoCusDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0224Service(DgrAcIdpInfoCusDao dgrAcIdpInfoCusDao) {
		super();
		this.dgrAcIdpInfoCusDao = dgrAcIdpInfoCusDao;
	}

	@Transactional
	public DPB0224Resp deleteIdPInfo_cus(TsmpAuthorization authorization, DPB0224Req req) {

		try {

			String acIdpInfoCusId = req.getCusId();
			Long id = getAcIdpInfoCusId(acIdpInfoCusId);

			Optional<DgrAcIdpInfoCus> opt = getDgrAcIdpInfoCusDao().findByAcIdpInfoCusId(id);
			if (opt.isEmpty()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			} else {
				getDgrAcIdpInfoCusDao().delete(opt.get());
			}

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return null;
	}

	private Long getAcIdpInfoCusId(String acIdpInfoCusId) {

		if (!StringUtils.hasText(acIdpInfoCusId)) {
			throw TsmpDpAaRtnCode._2025.throwing("{{id}}");
		}

		return isValidLong(acIdpInfoCusId) ? Long.parseLong(acIdpInfoCusId)
				: RandomSeqLongUtil.toLongValue(acIdpInfoCusId);
	}

	private boolean isValidLong(String str) {
		if (str == null || str.isEmpty()) {
			return false;
		}

		try {
			Long.parseLong(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	protected DgrAcIdpInfoCusDao getDgrAcIdpInfoCusDao() {
		return dgrAcIdpInfoCusDao;
	}

}
