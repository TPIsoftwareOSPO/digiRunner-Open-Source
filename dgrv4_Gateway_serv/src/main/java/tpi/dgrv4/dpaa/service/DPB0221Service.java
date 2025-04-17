package tpi.dgrv4.dpaa.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0221Req;
import tpi.dgrv4.dpaa.vo.DPB0221Resp;
import tpi.dgrv4.entity.entity.DgrAcIdpInfoCus;
import tpi.dgrv4.entity.repository.DgrAcIdpInfoCusDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0221Service {

	private DgrAcIdpInfoCusDao dgrAcIdpInfoCusDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public DPB0221Service(DgrAcIdpInfoCusDao dgrAcIdpInfoCusDao) {
		super();
		this.dgrAcIdpInfoCusDao = dgrAcIdpInfoCusDao;
	}

	public DPB0221Resp queryIdPInfoDetail_cus(TsmpAuthorization authorization, DPB0221Req req) {

		try {

			String acIdpInfoCusId = req.getCusId();
			Long id = getAcIdpInfoCusId(acIdpInfoCusId);

			Optional<DgrAcIdpInfoCus> opt = getDgrAcIdpInfoCusDao().findByAcIdpInfoCusId(id);
			if (opt.isEmpty()) {
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			return getResp(opt.get());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}

	}

	private DPB0221Resp getResp(DgrAcIdpInfoCus dgrAcIdpInfoCus) {

		DPB0221Resp resp = new DPB0221Resp();

		if (dgrAcIdpInfoCus != null) {

			resp.setCusId( //
					RandomSeqLongUtil.toHexString( //
							dgrAcIdpInfoCus.getAcIdpInfoCusId(), RandomLongTypeEnum.YYYYMMDD));

			resp.setCusName(dgrAcIdpInfoCus.getAcIdpInfoCusName());
			resp.setCusStatus(dgrAcIdpInfoCus.getCusStatus());
			resp.setCusLoginUrl(dgrAcIdpInfoCus.getCusLoginUrl());
			resp.setCusBackendLoginUrl(dgrAcIdpInfoCus.getCusBackendLoginUrl());
			resp.setCusUserDataUrl(dgrAcIdpInfoCus.getCusUserDataUrl());
			resp.setCreateDateTime(dgrAcIdpInfoCus.getCreateDateTime());
			resp.setCreateUser(dgrAcIdpInfoCus.getCreateUser());
			resp.setUpdateDateTime(dgrAcIdpInfoCus.getUpdateDateTime());
			resp.setUpdateUser(dgrAcIdpInfoCus.getUpdateUser());
		}

		return resp;
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
