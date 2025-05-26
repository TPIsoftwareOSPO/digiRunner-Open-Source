package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0199Req;
import tpi.dgrv4.dpaa.vo.DPB0199Resp;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoApi;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoA;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoApiDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0199Service {
	private TPILogger logger = TPILogger.tl;
	
	private DgrAcIdpInfoApiDao dgrAcIdpInfoApiDao;

	@Autowired
	public DPB0199Service(DgrAcIdpInfoApiDao dgrAcIdpInfoApiDao) {
		super();
		this.dgrAcIdpInfoApiDao = dgrAcIdpInfoApiDao;
	}

	public DPB0199Resp deleteIdPInfo_api(TsmpAuthorization authorization, DPB0199Req req) {
		DPB0199Resp resp = new DPB0199Resp();
		try {
			String id = req.getId();
			if (!StringUtils.hasLength(id)) {
				throw TsmpDpAaRtnCode._2025.throwing("{{id}}");
			}
			long longId = RandomSeqLongUtil.toLongValue(id);
			DgrAcIdpInfoApi a = getDgrAcIdpInfoApiDao().findById(longId).orElse(null);
			if (a == null) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			getDgrAcIdpInfoApiDao().delete(a);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();
		}

		return resp;
	}

	protected DgrAcIdpInfoApiDao getDgrAcIdpInfoApiDao() {
		return dgrAcIdpInfoApiDao;
	}
}
