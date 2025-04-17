package tpi.dgrv4.dpaa.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0152Req;
import tpi.dgrv4.dpaa.vo.DPB0152Resp;
import tpi.dgrv4.entity.entity.DgrAcIdpInfo;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
@Service
public class DPB0152Service {
	private DgrAcIdpInfoDao dgrAcIdpInfoDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0152Service(DgrAcIdpInfoDao dgrAcIdpInfoDao) {
		super();
		this.dgrAcIdpInfoDao = dgrAcIdpInfoDao;
	}

	public DPB0152Resp deleteDgrAcIdpInfo(TsmpAuthorization authorization, DPB0152Req req) {
		DPB0152Resp resp =new DPB0152Resp();
		try {
			checkParams(req);
			
			String id = req.getId();
			Long idToLong = RandomSeqLongUtil.toLongValue(id);
			DgrAcIdpInfo dgrAcIdpInfo = getDgrAcIdpInfoDao().findById(idToLong).orElse(null);
			if (dgrAcIdpInfo == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			} else {
				getDgrAcIdpInfoDao().delete(dgrAcIdpInfo);
			}
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			logger.debug(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();
		}
		return resp;
	}

	protected void checkParams(DPB0152Req req) {
		String id = req.getId();
		if (StringUtils.isBlank(id)) {
			throw TsmpDpAaRtnCode._2025.throwing("id");
		}
	}

	protected DgrAcIdpInfoDao getDgrAcIdpInfoDao() {
		return dgrAcIdpInfoDao;
	}
}
