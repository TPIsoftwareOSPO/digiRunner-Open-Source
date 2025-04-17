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
import tpi.dgrv4.dpaa.vo.DPB0241Req;
import tpi.dgrv4.dpaa.vo.DPB0241Resp;
import tpi.dgrv4.entity.entity.DgrGtwIdpInfoCus;
import tpi.dgrv4.entity.repository.DgrGtwIdpInfoCusDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0241Service {

	private DgrGtwIdpInfoCusDao dgrGtwIdpInfoCusDao;

	@Autowired
	public DPB0241Service(DgrGtwIdpInfoCusDao dgrGtwIdpInfoCusDao) {
		super();
		this.dgrGtwIdpInfoCusDao = dgrGtwIdpInfoCusDao;
	}

	public DPB0241Resp queryGtwIdPInfoDetail(TsmpAuthorization authorization, DPB0241Req req) {

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
			}

			return getResp(opt.get());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
	}

	private DPB0241Resp getResp(DgrGtwIdpInfoCus dgrGtwIdpInfoCus) {

		DPB0241Resp resp = new DPB0241Resp();

		if (dgrGtwIdpInfoCus != null) {
			resp.setGtwIdpInfoCusId(
					RandomSeqLongUtil.toHexString(dgrGtwIdpInfoCus.getGtwIdpInfoCusId(), RandomLongTypeEnum.YYYYMMDD));

			resp.setClientId(dgrGtwIdpInfoCus.getClientId());
			resp.setStatus(dgrGtwIdpInfoCus.getStatus());
			resp.setCusLoginUrl(dgrGtwIdpInfoCus.getCusLoginUrl());
			resp.setCusUserDataUrl(dgrGtwIdpInfoCus.getCusUserDataUrl());
			resp.setCreateDateTime(dgrGtwIdpInfoCus.getCreateDateTime());
			resp.setCreateUser(dgrGtwIdpInfoCus.getCreateUser());
			resp.setUpdateDateTime(dgrGtwIdpInfoCus.getUpdateDateTime());
			resp.setUpdateUser(dgrGtwIdpInfoCus.getUpdateUser());
			resp.setPageTitle(dgrGtwIdpInfoCus.getPageTitle());
			resp.setIconFile(dgrGtwIdpInfoCus.getIconFile());
		}

		return resp;
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
