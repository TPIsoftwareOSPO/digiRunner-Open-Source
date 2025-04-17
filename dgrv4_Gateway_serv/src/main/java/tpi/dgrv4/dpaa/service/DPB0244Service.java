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
import tpi.dgrv4.dpaa.vo.DPB0244Req;
import tpi.dgrv4.dpaa.vo.DPB0244Resp;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoCus;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoCusDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0244Service {

	private DgrGtwIdpInfoCusDao dgrGtwIdpInfoCusDao;

	@Autowired
	public DPB0244Service(DgrGtwIdpInfoCusDao dgrGtwIdpInfoCusDao) {
		super();
		this.dgrGtwIdpInfoCusDao = dgrGtwIdpInfoCusDao;
	}

	@Transactional
	public DPB0244Resp deleteGtwIdPInfo(TsmpAuthorization authorization, DPB0244Req req) {

		try {
			String gtwIdpInfoCusId = req.getGtwIdpInfoCusId();
			String clientId = req.getClientId();
			Long id = getGtwIdpInfoCusId(gtwIdpInfoCusId);

			if (clientId == null || clientId.isEmpty()) {
				throw TsmpDpAaRtnCode._1350.throwing("clientId");
			}

			Optional<DgrGtwIdpInfoCus> opt = getDgrGtwIdpInfoCusDao().findByGtwIdpInfoCusIdAndClientId(id, clientId);
			if (opt.isEmpty()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			} else {
				getDgrGtwIdpInfoCusDao().delete(opt.get());
			}

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

		return new DPB0244Resp();
	}

	private Long getGtwIdpInfoCusId(String gtwIdpInfoCusId) {

		if (!StringUtils.hasText(gtwIdpInfoCusId)) {
			throw TsmpDpAaRtnCode._2025.throwing("{{id}}");
		}

		return isValidLong(gtwIdpInfoCusId) ? Long.parseLong(gtwIdpInfoCusId)
				: RandomSeqLongUtil.toLongValue(gtwIdpInfoCusId);
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

	protected DgrGtwIdpInfoCusDao getDgrGtwIdpInfoCusDao() {
		return dgrGtwIdpInfoCusDao;
	}
}
